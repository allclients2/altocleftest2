package adris.altoclef.tasks.speedrun.beatgame.prioritytask.imp.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.imp.prioritycalculators.PriorityCalculator;
import adris.altoclef.tasksystem.Task;

import java.util.function.Function;

/**
 * most general way to create priority tasks (basically all other subclasses could be replaced by this).
 * Returns a task and a priority given a PriorityCalculator and TaskProvider
 * (kinda works like the old GatherResource I guess)
 */
public class ActionPriorityTask extends PriorityTask {


    private final TaskProvider taskProvider;
    private final PriorityCalculator priorityCalculator;

    // just for making the debug string
    private double lastPriority = 0;
    private Task lastTask = null;


    public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.taskProvider = taskProvider;
        this.priorityCalculator = priorityCalculator;
    }

    @Override
    public Task getTask(AltoClef mod) {
        lastTask =  taskProvider.getTask(mod);
        return lastTask;
    }

    @Override
    public String getDebugString() {
        return "Performing an action: "+lastTask + " with priority: "+lastPriority;
    }

    @Override
    protected double getPriority(AltoClef mod) {
        lastPriority =  priorityCalculator.getPriority();
        return lastPriority;
    }


    public interface TaskProvider {
        Task getTask(AltoClef mod);
    }


}
