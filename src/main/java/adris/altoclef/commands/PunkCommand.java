package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.misc.KillPlayerTask;

public class PunkCommand extends Command {
    public PunkCommand() throws CommandException {
        super("punk", "Punk 'em", new Arg(String.class, "playerName"));
    }

    @Override
    protected void Call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.Get(String.class);
        mod.runUserTask(new KillPlayerTask(playerName), nothing -> finish());
    }
}