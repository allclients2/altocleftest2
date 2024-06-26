package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunAwayFromHostilesTask extends CustomBaritoneGoalTask {

    private final double distanceToRun;
    private final boolean includeSkeletons;
    private Stream<LivingEntity> runAwayStream;

    public RunAwayFromHostilesTask(double distance, boolean includeSkeletons) {
        distanceToRun = distance;
        this.includeSkeletons = includeSkeletons;
    }

    public RunAwayFromHostilesTask(double distance) {
        this(distance, false);
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getClientBaritoneSettings().blockPlacementPenalty.value = 35.0; // Make sure later paths don't fall for it.
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        _checker.reset();
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // We want to run away NOW
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        return new GoalRunAwayFromHostiles(mod, distanceToRun);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getPathingBehavior().forceCancel();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromHostilesTask task) {
            return Math.abs(task.distanceToRun - distanceToRun) < 1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "NIGERUNDAYOO, SUMOOKEYY! distance: "+ String.format("%.2f", distanceToRun) +", skeletons: "+ includeSkeletons;
    }

    public boolean isSafe() {
        if (runAwayStream != null) {
            return runAwayStream.findAny().isEmpty();
        } else {
            return false; // Assume we are not safe.
        }
    }


    private class GoalRunAwayFromHostiles extends GoalRunAwayFromEntities {

        public GoalRunAwayFromHostiles(AltoClef mod, double distance) {
            super(mod, distance, false, 0.8);
        }

        @Override
        protected List<Entity> getEntities(AltoClef mod) {
            Stream<LivingEntity> stream = mod.getEntityTracker().getHostiles().stream();
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (!includeSkeletons) {
                    stream = stream.filter(hostile -> !(hostile instanceof SkeletonEntity));
                }
                stream = stream.filter(hostile -> !mod.getPlayer().canSee(hostile)); // Only run away from mobs we can see..
                runAwayStream = stream;
                final List<Entity> entities = stream.collect(Collectors.toList());
                return entities;
            }
        }
    }
}
