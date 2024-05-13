package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.Stopwatch;

public abstract class SingleTaskChain extends TaskChain {

    protected Task mainTask = null;
    private boolean interrupted = false;

    private final AltoClef mod;

    public SingleTaskChain(TaskRunner runner) {
        super(runner);
        mod = runner.getMod();
    }

    @Override
    protected void onTick(AltoClef mod) {
        if (!isActive()) return;

        if (interrupted) {
            interrupted = false;
            if (mainTask != null) {
                mainTask.reset();
            }
        }

        if (mainTask != null) {
            if ((mainTask.isFinished(mod)) || mainTask.stopped()) {
                onTaskFinish(mod);
            } else {
                mainTask.tick(mod, this);
            }
        }
    }

    protected void onStop(AltoClef mod) {
        if (isActive() && mainTask != null) {
            mainTask.stop(mod);
            mainTask = null;
        }
    }

    public void setTask(Task task) {
        if (mainTask == null || !mainTask.equals(task)) {
            if (mainTask != null) {
                mainTask.stop(mod, task);
            }
            mainTask = task;
            if (task != null) task.reset();
        }
    }


    @Override
    public boolean isActive() {
        return mainTask != null;
    }

    protected abstract void onTaskFinish(AltoClef mod);

    @Override
    public void onInterrupt(AltoClef mod, TaskChain other) {
        if (other != null) {
            Debug.logInternal("Chain Interrupted: " + this + " by " + other);
        }
        // Stop our task. When we're started up again, let our task know we need to run.
        interrupted = true;
        if (mainTask != null && mainTask.isActive()) {
            mainTask.interrupt(mod, null);
        }
    }

    protected boolean isCurrentlyRunning(AltoClef mod) {
        return !interrupted && mainTask.isActive() && !mainTask.isFinished(mod);
    }

    public Task getCurrentTask() {
        return mainTask;
    }
}
