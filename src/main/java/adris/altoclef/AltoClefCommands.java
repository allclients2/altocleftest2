package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public static void init() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new HelpCommand(),
                new GetCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new SelfCareCommand(),
                new GotoCommand(),
                new IdleCommand(),
                new InventoryCommand(),
                new LocateStructureCommand(),
                new StopCommand(),
                new SetGammaCommand(),
                new TestCommand(),
                new FoodCommand(),
                new ReloadSettingsCommand(),
                new KillPlayerCommand(),
                new HeroCommand(),
                new CoverWithBlocksCommand(),
                new ScanCommand(),
                new ListCommand(),
                new FlytoCommand(),
                new BranchMineCommand(),
                new MiranCommand(),
                new GamerCommand(),
                new MarvionCommand()
        );
    }
}
