package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.Settings;
import net.minecraft.util.math.Vec3d;

public abstract class LookAtPos {
    private static Vec3d lookPos;
    private static final TimerGame lookTimeOut = new TimerGame(1);
    private static final TimerGame updateDelay = new TimerGame(0.2);

    public static void lookAtPos(AltoClef mod, Vec3d newLookPos) {
        lookPos = newLookPos;
        lookTimeOut.reset();
    }

    private static boolean isLookingAtPosition(AltoClef mod, Vec3d position) {
        final double LOOK_CLOSENESS_THRESHOLD = 0.9925; // is 1.0 when we are looking straight at mob, and -1.0, when we are looking opposite to the mob
        double lookCloseness = LookHelper.getLookCloseness(mod.getPlayer(), position);
        return Math.abs(lookCloseness) > LOOK_CLOSENESS_THRESHOLD;
    }

    public static boolean updateFreeLook(AltoClef mod) { //Returns true if the camera will look where its moving.
        Settings.Setting<Boolean> freeLook = mod.getClientBaritoneSettings().freeLook;
        if (lookTimeOut.elapsed()) {
            if (freeLook.value) {
                freeLook.value = false; //when freeLook is true, it won't make the camera look where its moving.
            }
            return true;
        } else {
            if (!freeLook.value) {
                freeLook.value = true;
            }
            return false;
        }
    }

    public static boolean updatePosLook(AltoClef mod) {
        if (updateFreeLook(mod)) return false; //We don't want camera to look while moving.

        // Don't interrupt if building or killing.
        Task currentTask = mod.getUserTaskChain().getCurrentTask();
        if (currentTask != null) {
            if (currentTask.getClass() == DestroyBlockTask.class || mod.getController().getBlockBreakingProgress() > 0) {
                return false;
            } else if (currentTask.getClass() == PlaceBlockTask.class || mod.getPlayer().isBlocking()) {
                return false;
            } else if (currentTask.getClass() == KillEntitiesTask.class) {
                return false;
            }
        }

        if (lookPos != null && !isLookingAtPosition(mod, lookPos) && updateDelay.elapsed()) {
            LookHelper.lookAt(mod, lookPos);
            updateDelay.reset();
            return true;
        }

        return false;
    }
}
