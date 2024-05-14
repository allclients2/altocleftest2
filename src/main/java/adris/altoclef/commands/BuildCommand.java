//TODO: Implement this
/*
package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.construction.SchematicBuildTask;

public class BuildCommand extends Command {
    public BuildCommand() throws CommandException {
        super("build", "(CRASHES GAME) Build a structure from schematic data, much in development.", new Arg(String.class, "filename", "", 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String name = "";
        try {
            name = parser.get(String.class);
        } catch (CommandException e) {
            Debug.logError("Cannot parse parameter. Input format: '@build house.schem'");
        }

        mod.runUserTask(new SchematicBuildTask(name));
    }
}
*/