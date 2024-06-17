package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.KillPlayerTask;

public class KillPlayerCommand extends Command {
    public KillPlayerCommand() throws CommandException {
        super("killplr", "Kill named player.", new Arg(String.class, "playerName"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.get(String.class);
        mod.runUserTask(new KillPlayerTask(playerName), this::finish);
    }
}