package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;

public class UnstuckChain extends SingleTaskChain {

    private final LinkedList<Vec3d> posHistory = new LinkedList<>();
    private boolean isProbablyStuck = false;
    private int eatingTicks = 0;
    private boolean interruptedEating = false;
    private TimerGame shimmyTaskTimer = new TimerGame(5);
    private boolean startedShimmying = false;

    public UnstuckChain(TaskRunner runner) {
        super(runner);
    }


    private void checkStuckInWater(AltoClef mod) {
        if (posHistory.size() < 12) return;

        // is not in water
        if (!mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos()).getBlock().equals(Blocks.WATER)
                && !mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos().down()).getBlock().equals(Blocks.WATER))
            return;

        // everything should be fine
        if (mod.getPlayer().isOnGround()) {
            posHistory.clear();
            return;
        }

        // do NOT do anything if underwater
        if (mod.getPlayer().getAir() < mod.getPlayer().getMaxAir()) {
            return;
        }

        if (posHistory.size() < 100) return;

        Vec3d pos1 = posHistory.get(0);
        for (int i = 1; i < 100; i++) {
            Vec3d pos2 = posHistory.get(i);
            if (Math.abs(pos1.getX() - pos2.getX()) > 0.75 || Math.abs(pos1.getZ() - pos2.getZ()) > 0.75) {
                return;
            }
        }

        isProbablyStuck = true;

        mod.getInputControls().tryPress(Input.JUMP);

        boolean hasBlockBelow = false;
        for (int i = 0; i < 3; i++) {
            if (mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos().down(i)).getBlock() != Blocks.WATER) {
                hasBlockBelow = true;
            }
        }

        if (hasBlockBelow) {
            if (mod.getPlayer().isOnGround()) {
                setTask(new SafeRandomShimmyTask());
                if (!startedShimmying) {
                    startedShimmying = true;
                    shimmyTaskTimer.reset();
                }
                return;
            }

            mod.getSlotHandler().forceEquipItem(mod.getClientBaritoneSettings().acceptableThrowawayItems.value.toArray(new Item[0]));
            LookHelper.lookAt(mod, mod.getPlayer().getSteppingPos().down());
            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
        } else {
            mod.getInputControls().tryPress(Input.MOVE_FORWARD);
            mod.getInputControls().tryPress(Input.CLICK_LEFT);
            if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult) {
                LookHelper.lookAt(mod,blockHitResult.getBlockPos());
            } else {
                LookHelper.lookAt(mod, new Rotation(0, 90));
            }
        }
    }

    private void checkStuckInPowderedSnow(AltoClef mod) {
        PlayerEntity player = mod.getPlayer();

        if (player.inPowderSnow) {
            isProbablyStuck = true;
            setTask(new DestroyBlockTask(mod.getBlockScanner().getNearestBlock(Blocks.POWDER_SNOW).get()));
        }
    }

    private void checkStuckOnEndPortalFrame(AltoClef mod) {
        BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos());

        // if we are standing on an end portal frame that is NOT filled, get off otherwise we will get stuck
        if (state.getBlock() == Blocks.END_PORTAL_FRAME && !state.get(EndPortalFrameBlock.EYE)) {
            if (!mod.getFoodChain().isTryingToEat()) {
                isProbablyStuck = true;

                // for now let's just hope the other mechanisms will take care of cases where moving forward will get us in danger
                mod.getInputControls().tryPress(Input.MOVE_FORWARD);
            }
        }
    }

    private void checkEatingGlitch(AltoClef mod) {
        if (interruptedEating) {
            mod.getFoodChain().shouldStop(false);
            interruptedEating = false;
        }

        if (mod.getFoodChain().isTryingToEat()) {
            eatingTicks++;
        } else {
            eatingTicks = 0;
        }

        if (eatingTicks > 7*20) {
            mod.log("the bot is probably stuck trying to eat... resetting action");
            mod.getFoodChain().shouldStop(true);

            eatingTicks = 0;
            interruptedEating = true;
            isProbablyStuck = true;
        }
    }

    @Override
    public float getPriority(AltoClef mod) {
        isProbablyStuck = false;

        if (!AltoClef.inGame() || MinecraftClient.getInstance().isPaused() || !mod.getUserTaskChain().isActive())
            return Float.NEGATIVE_INFINITY;

        if (StorageHelper.isBlastFurnaceOpen() || StorageHelper.isSmokerOpen() || StorageHelper.isChestOpen() || StorageHelper.isBigCraftingOpen()) {
            return Float.NEGATIVE_INFINITY;
        }

        PlayerEntity player = mod.getPlayer();
        posHistory.addFirst(player.getPos());
        if (posHistory.size() > 500) {
            posHistory.removeLast();
        }

        checkStuckInWater(mod);
        checkStuckInPowderedSnow(mod);
        checkEatingGlitch(mod);
        checkStuckOnEndPortalFrame(mod);


        if (isProbablyStuck) {
            return 55;
        }

        if (startedShimmying && !shimmyTaskTimer.elapsed()) {
            setTask(new SafeRandomShimmyTask());
            return 55;
        }
        startedShimmying = false;

        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {

    }

    @Override
    public String getName() {
        return "Unstuck Chain";
    }
}
