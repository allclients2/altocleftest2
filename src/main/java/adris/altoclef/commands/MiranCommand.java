package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.maintasks.BeatMinecraftTask;

public class MiranCommand extends Command {
    public MiranCommand() {
        super("miran", "Beats the game (Miran version)");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatMinecraftTask(mod), this::finish);
    }
}