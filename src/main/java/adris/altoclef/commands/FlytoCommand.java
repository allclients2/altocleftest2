package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.misc.ElytraToXZTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;

/**
 * Out of all the commands, this one probably demonstrates
 * why we need a better arg parsing system. Please.
 */
public class FlytoCommand extends Command {

    public FlytoCommand() throws CommandException {
        // x z
        // x y z
        // x y z dimension
        // (dimension)
        // (x z dimension)
        super("elytra", "Tell bot to use elytra fly to a set of coordinates",
                new Arg(GotoTarget.class, "[x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]")
        );
    }

    public static Task getMovementTaskFor(GotoTarget target) {
        return new ElytraToXZTask(target.getX(), target.getZ(), target.getDimension());
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        GotoTarget target = parser.get(GotoTarget.class);
        mod.runUserTask(getMovementTaskFor(target), this::finish);
    }
}
