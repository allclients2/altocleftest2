package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.utils.input.Input;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ElytraToXZTask extends Task {

    private final int _x, _z;
    private final Dimension _dimension;

    protected AltoClef CurrentMod;

    // Configuration
    protected final double FlightAltitudeDescend = 120; //Remember that y-level 63 is sea level.
    protected final int RangeDescend = 230; //Range from goal which descend will start.
    protected final double FlightAltitude = 250;
    protected final int maxRangeFromGoal = 20; //Max range where within, to be considered that the destination is reached.
    protected final int RocketsGetCount = 60; //Min number of rockets.



    protected double FlyAltitude;

    protected boolean Descending;
    protected Vec3d GoalPosition;

    protected boolean IsFlying = false;
    protected boolean RocketBoosted = true;
    protected boolean FailToStart = false;

    protected Task _test_task_; //(I don't know).
    protected Optional<Input> InputToRelease = Optional.empty();
    protected Optional<BlockPos> TakeOffPosition = Optional.empty();
    protected boolean FlightControls = true;

    protected ItemStack CurrentElytra;

    public ElytraToXZTask(int x, int z) {
        this(x, z, null);
    }

    public ElytraToXZTask(int x, int z, Dimension dimension) {
        if (dimension == Dimension.NETHER) {
            System.out.println("Flying in nether is not supported. Prepare to die.");
        }
        _x = x;
        _z = z;
        _dimension = dimension;
    }



    protected void SendInput(AltoClef mod, Input input) {
        mod.getInputControls().hold(input);
        InputToRelease = Optional.of(input);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        InputToRelease.ifPresent(input -> mod.getInputControls().release(input));

        Vec3d Velocity = mod.getPlayer().getVelocity();
        Vec3d Position = mod.getPlayer().getPos();
        double Diff = (FlyAltitude - Position.y);

        //To keep track of damage
        if (CurrentElytra == null) {
            Iterable<ItemStack> armorItems = mod.getPlayer().getArmorItems();
            for (ItemStack ArmorPiece : armorItems) {
                if (ArmorPiece.getItem() == Items.ELYTRA) {
                    CurrentElytra = ArmorPiece;
                    return _test_task_;
                }
            }
            Debug.logInternal("Alto clef Failed to detect Elytra.");
            FailToStart = true;
            return _test_task_;
        }

        // Always equip Elytra
        if (CurrentElytra.getDamage() > Items.ELYTRA.getDefaultStack().getMaxDamage() * 0.95) {
            Debug.logInternal("Low Elytra health warning, equip new elytra..");
            FailToStart = true;
            return _test_task_;
        }

        //Present TakeOffPosition Indicates we are landed and need more rockets. We need else it's going to fight to equip the firework rocket.
        if (TakeOffPosition.isPresent() || !mod.getSlotHandler().forceEquipItem(Items.FIREWORK_ROCKET)) {//Land
            if (mod.getPlayer().isFallFlying()) {
                Descending = true;
                double CurrentAlt = Position.y;
                if ((FlightAltitudeDescend + 10) > CurrentAlt && (FlightAltitudeDescend - 10) < CurrentAlt) {
                    FlightControls = false;
                    Descending = false;
                }
                double AngleMultiplier = (Math.max(0, 0.5 - Velocity.y) + 0.5); //Range 0.5 to 1 multiplier
                LookHelper.lookAt(mod, new Vec3d(GoalPosition.x, Position.y + Position.distanceTo(GoalPosition) * AngleMultiplier, GoalPosition.z));
            } else if (TakeOffPosition.isEmpty()) {
                Debug.logInternal("Getting firework rockets..");
                TakeOffPosition = Optional.of(mod.getPlayer().getBlockPos());
                Debug.logInternal(mod.getPlayer().getEquippedStack(EquipmentSlot.MAINHAND).getCount() + " Firework count");
                _test_task_ = TaskCatalogue.getItemTask(Items.FIREWORK_ROCKET, RocketsGetCount);
            }
            return _test_task_;
        } else if (TakeOffPosition.isPresent()) {
            FlightControls = false; //No Flight
            _test_task_ = new GetToBlockTask(TakeOffPosition.get());
            TakeOffPosition = Optional.empty();
            return _test_task_;
        } else if (_test_task_ == null || _test_task_.isFinished(mod)) {
            _test_task_ = null;
            FlightControls = true;
        }

        if (FlightControls) {
            double YVelocity = Velocity.y;

            // Take off
            if (!IsFlying) {
                Debug.logInternal("Try flying...");

                LookHelper.lookAt(mod, Position.add(0, 2, 0));

                if (mod.getPlayer().isFallFlying()) {
                    Debug.logInternal("Flying!");

                    SendInput(mod, Input.CLICK_RIGHT); // Fly away!
                    IsFlying = true;
                } else if (YVelocity < -0.05) {
                    Debug.logInternal("Falling!");

                    SendInput(mod, Input.JUMP); //Enable elytra
                } else if (mod.getPlayer().groundCollision) {
                    SendInput(mod, Input.JUMP); //Jumpstart
                }
            } else { //In Flight
                if (isRangeFromGoal(RangeDescend)) { // Descending, at `RangeClose` blocks range.
                    if (!Descending) {
                        Descending = true;
                        Debug.logInternal("Descending now.");
                    }
                    FlyAltitude = FlightAltitudeDescend;
                    //Took this path as we don't want rocket boosting while landing.
                }

                if (Velocity.length() < 1.12 && !RocketBoosted && !Descending) {
                    SendInput(mod, Input.CLICK_RIGHT);
                    RocketBoosted = true;
                } else if (Velocity.length() < 0.45) {
                    SendInput(mod, Input.CLICK_RIGHT); // Critical, Might stall.
                } else {
                    RocketBoosted = false; // To prevent double rocket when slow speed.
                }

                // Diff was made for 400
                LookHelper.lookAt(mod, GoalPosition.add(0, FlyAltitude + ((Diff / 10) * Position.distanceTo(GoalPosition)), 0));
            }
        }

        return _test_task_;
    }

    @Override
    public void onStart(AltoClef mod) {
        FlyAltitude = FlightAltitude;
        Descending = false;
        RocketBoosted = true;
        FailToStart = false;
        CurrentMod = mod;

        GoalPosition = new Vec3d(_x, mod.getPlayer().getPos().y, _z);
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            if (_dimension == Dimension.NETHER) {
                _test_task_ = new GetToBlockTask(new BlockPos(0, 128, 0), Dimension.NETHER);
            } else {
                _test_task_ = new DefaultGoToDimensionTask(_dimension);
            }
        }
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        InputToRelease.ifPresent(input -> mod.getInputControls().release(input));
        LookHelper.lookAt(mod, GoalPosition.add(0, FlyAltitude + (3 * GoalPosition.length()), 0));
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ElytraToXZTask task) {
            return task._x == _x && task._z == _z && task._dimension == _dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        CurrentMod = mod;
        return (isRangeFromGoal(maxRangeFromGoal) && (_dimension == null || _dimension == WorldHelper.getCurrentDimension())) || FailToStart;
    }


    protected double getDistanceSquaredToGoal() { //Returning squared is faster (i think)
        BlockPos cur = CurrentMod.getPlayer().getBlockPos();
        int dx = cur.getX() - _x;
        int dz = cur.getZ() - _z;
        return dx * dx + dz * dz; // Returning the squared distance
    }

    protected boolean isRangeFromGoal(int maxRange) {
        double distanceSquaredToGoal = getDistanceSquaredToGoal();
        return distanceSquaredToGoal <= (maxRange * maxRange);
    }


    @Override
    protected String toDebugString() {
        if (!IsFlying) {
            return "Attempting to takeoff..";
        } else {
            double distance = Math.sqrt(getDistanceSquaredToGoal());
            String debugString = "Flying to (" + _x + "," + _z + ") ";

            if (!FlightControls) {
                debugString += "Now landing, Altitude: " + String.format("%.2f", FlyAltitude);
            } else if (Descending) { //To save some space
                debugString += "Now descending, Altitude: " +  String.format("%.2f", FlyAltitude);
            } else {
                debugString += (_dimension != null ? "in dimension " + _dimension : "");
            }
            debugString += ", Distance to goal: " + String.format("%.2f", distance) + " blocks."; //Dimension
            return debugString;
        }
    }
}
