package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commands.random.CycleTestCommand;
import adris.altoclef.commands.random.DummyTaskCommand;
import adris.altoclef.commands.random.ScanCommand;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public static void init() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new HelpCommand(),
                new StatusCommand(),
                new StopCommand(),
                new SetGammaCommand(),
                new TestCommand(),
                new GamerCommand(),
                new MarvionCommand(),
                new DummyTaskCommand(),
                new ReloadSettingsCommand(),
                new GetCommand(),
                //new CycleTestCommand(),
                new ScanCommand()
        );
    }
}
