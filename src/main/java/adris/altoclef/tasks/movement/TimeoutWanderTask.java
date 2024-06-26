package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Call this when the place you're currently at is bad for some reason, and you just want to get away.
 */
public class TimeoutWanderTask extends Task implements ITaskRequiresGrounded {
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    private final float _distanceToWander;
    private final MovementProgressChecker _progressChecker = new MovementProgressChecker();
    private final boolean _increaseRange;
    private final TimerGame _timer = new TimerGame(60);
    private final Block[] annoyingBlocks = AltoClef.INSTANCE.getModSettings().getAnnoyingBlocks();
    private Vec3d _origin;
    //private DistanceProgressChecker _distanceProgressChecker = new DistanceProgressChecker(10, 0.1f);
    private boolean _forceExplore;
    private Task _unstuckTask = null;
    private int _failCounter;
    private double _wanderDistanceExtension;

    public TimeoutWanderTask(float distanceToWander, boolean increaseRange) {
        _distanceToWander = distanceToWander;
        _increaseRange = increaseRange;
        _forceExplore = false;
    }

    public TimeoutWanderTask(float distanceToWander) {
        this(distanceToWander, false);
    }

    public TimeoutWanderTask() {
        this(Float.POSITIVE_INFINITY, false);
    }

    public TimeoutWanderTask(boolean forceExplore) {
        this();
        _forceExplore = forceExplore;
    }

    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{
                pos.add(1, 0, 0),
                pos.add(-1, 0, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1),
                pos.add(1, 0, -1),
                pos.add(1, 0, 1),
                pos.add(-1, 0, -1),
                pos.add(-1, 0, 1)
        };
    }

    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        for (Block AnnoyingBlocks : annoyingBlocks) {
            return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;
        }
        return false;
    }

    public void resetWander() {
        _wanderDistanceExtension = 0;
    }

    // This happens all the time in mineshaft and swamps/jungles
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        if (isAnnoying(mod, p)) return p;
        if (isAnnoying(mod, p.up())) return p.up();
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    @Override
    protected void onStart(AltoClef mod) {

        //Don't spend too much time on finding an optimized path. Doesn't really matter.
        //But also don't be really dumb and try to cross an ocean. (it tried to do so when it was set at 21.0)
        mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 3.25;
        mod.getClientBaritoneSettings().blockPlacementPenalty.value = 23.5;

        _timer.reset();
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        _origin = mod.getPlayer().getPos();
        _progressChecker.reset();
        stuckCheck.reset();
        _failCounter = 0;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _progressChecker.reset();
        }
        if (WorldHelper.isInNetherPortal(mod)) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("Getting out from nether portal");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        }
        if (_unstuckTask != null && _unstuckTask.isActive() && !_unstuckTask.isFinished(mod) && stuckInBlock(mod) != null) {
            setDebugState("Getting unstuck from block.");
            stuckCheck.reset();
            // Stop other tasks, we are JUST shimmying
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return _unstuckTask;
        }
        if (!_progressChecker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                _failCounter++;
                _unstuckTask = getFenceUnstuckTask();
                return _unstuckTask;
            }
            stuckCheck.reset();
        }
        switch (WorldHelper.getCurrentDimension()) {
            case END -> {
                if (_timer.getDuration() >= 30) {
                    _timer.reset();
                }
            }
            case OVERWORLD, NETHER -> {
                // Commented out only because empty if statement
                // if (_timer.getDuration() >= 30) {}
                if (_timer.elapsed()) {
                    _timer.reset();
                }
            }
        }
        if (!mod.getClientBaritone().getExploreProcess().isActive()) {
            mod.getClientBaritone().getExploreProcess().explore((int) _origin.getX(), (int) _origin.getZ());
        }
        if (!_progressChecker.check(mod)) {
            _progressChecker.reset();
            _failCounter++;
            if (!_forceExplore) {
                Debug.logMessage("Failed exploring.");
            }
        }
        // We are getting a little worried. Let us try more options...
        if (_failCounter > 5 && _failCounter < 10 || _forceExplore) {
            setDebugState("Exploring; worried.");
            mod.getBehaviour().setBlockBreakAdditionalPenalty(0.2);
            mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 0.2;
            mod.getBehaviour().setBlockPlacePenalty(15.0);
            mod.getClientBaritoneSettings().blockPlacementPenalty.value = 15.0;
            mod.getClientBaritoneSettings().costHeuristic.value = 6.5;
        } else if (_failCounter > 10) {
            setDebugState("Exploring; desperately.");
            mod.getBehaviour().setBlockBreakAdditionalPenalty(0.0);
            mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 0.0;
            mod.getBehaviour().setBlockPlacePenalty(0.0);
            mod.getClientBaritoneSettings().blockPlacementPenalty.value = 0.0;
            mod.getClientBaritoneSettings().costHeuristic.value = 40.5;
        } else {
            setDebugState("Exploring; normal.");
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        //Reset
        mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 0.0;
        mod.getClientBaritoneSettings().blockPlacementPenalty.reset();


        mod.getClientBaritone().getPathingBehavior().forceCancel();
        if (isFinished(mod)) {
            if (_increaseRange) {
                _wanderDistanceExtension += _distanceToWander;
                Debug.logMessage("Increased wander range");

            }
        }
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        // Why the heck did I add this in?
        //if (_origin == null) return true;

        if (Float.isInfinite(_distanceToWander)) return false;

        // If we fail 2 times, we may as well try the previous task again.
        if (_failCounter >= 10) {
            return true;
        }

        if (mod.getPlayer() != null && mod.getPlayer().getPos() != null && (mod.getPlayer().isOnGround() ||
                mod.getPlayer().isTouchingWater()) && _origin != null) {
            double sqDist = mod.getPlayer().getPos().squaredDistanceTo(_origin);
            double toWander = _distanceToWander + _wanderDistanceExtension;
            return sqDist > toWander * toWander;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof TimeoutWanderTask task) {
            if (Float.isInfinite(task._distanceToWander) || Float.isInfinite(_distanceToWander)) {
                return Float.isInfinite(task._distanceToWander) == Float.isInfinite(_distanceToWander);
            }
            return Math.abs(task._distanceToWander - _distanceToWander) < 0.5f;
        }
        return false;
    }


    @Override
    protected String toDebugString() {
        return "Wander for " + (_distanceToWander + _wanderDistanceExtension) + " blocks";
    }
}