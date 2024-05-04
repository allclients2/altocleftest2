package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Playground;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectBedTask;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;

import java.util.Optional;

public class TestCommand extends Command {

    public TestCommand() throws CommandException {
        super("test", "Generic command for testing", new Arg(String.class, "extra", "", 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        Playground.TEMP_TEST_FUNCTION(mod, parser.get(String.class));
        finish();
    }
}