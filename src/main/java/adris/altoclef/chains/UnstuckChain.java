package adris.altoclef.chains;

import adris.altoclef.AltoClef;
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

    private boolean isProbablyStuck = false;
    private LinkedList<Vec3d> posHistory = new LinkedList<>();
    private TimerGame forceTimer = new TimerGame(2);

    public UnstuckChain(TaskRunner runner) {
        super(runner);
    }


    private void checkStuckInWater(AltoClef mod) {
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
            if (Math.abs(pos1.getX() - pos2.getX()) > 0.75 || Math.abs(pos1.getZ() - pos2.getZ()) > 0.75 || pos1.equals(pos2)) {
                return;
            }
        }

        isProbablyStuck = true;
       // setTask(new SafeRandomShimmyTask());
        mod.getInputControls().tryPress(Input.CLICK_LEFT);
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
        if (posHistory.size() < 10) return Float.NEGATIVE_INFINITY;

        checkStuckInWater(mod);


        if (isProbablyStuck || !forceTimer.elapsed()) {
            if (isProbablyStuck && forceTimer.elapsed()) {
                forceTimer.reset();
            } else {
                posHistory.clear();
            }
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
