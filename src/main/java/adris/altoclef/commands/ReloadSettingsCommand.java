package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.ConfigHelper;

public class ReloadSettingsCommand extends Command {
    public ReloadSettingsCommand() {
        super("reloadconfig", "Reloads mod config and butler whitelist/blacklist.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        ConfigHelper.reloadAllConfigs();
        Debug.logMessage("Reload successful!");
        finish();
    }
}