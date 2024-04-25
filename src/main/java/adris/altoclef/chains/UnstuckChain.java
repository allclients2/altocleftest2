package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;

public class UnstuckChain extends SingleTaskChain {

    private final LinkedList<Vec3d> posHistory = new LinkedList<>();
    private boolean isProbablyStuck = false;
    private int eatingTicks = 0;
    private boolean interruptedEating = false;

    public UnstuckChain(TaskRunner runner) {
        super(runner);
    }


    private void checkStuckInWater(AltoClef mod) {
        if (posHistory.size() < 12) return;

        // is not in water
        if (!mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos()).getBlock().equals(Blocks.WATER)
                && !mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos().down()).getBlock().equals(Blocks.WATER))
            return;

        // do NOT do anything if underwater
        if (mod.getPlayer().getAir() < mod.getPlayer().getMaxAir()) {
            posHistory.clear();
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
       // setTask(new SafeRandomShimmyTask());
        mod.getInputControls().tryPress(Input.CLICK_LEFT);
        mod.getInputControls().tryPress(Input.JUMP);
    }

    private void checkStuckInPowderedSnow(AltoClef mod) {
        PlayerEntity player = mod.getPlayer();

        if (player.inPowderSnow) {
            isProbablyStuck = true;
            setTask(new DestroyBlockTask(mod.getBlockScanner().getNearestBlock(Blocks.POWDER_SNOW).get()));
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


        if (isProbablyStuck) {
            return 55;
        }

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
