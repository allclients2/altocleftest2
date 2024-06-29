package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static adris.altoclef.multiversion.LiteralTextVer.constructLiteralText;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Lists all commands");
    }

    private static String truncate(String str, int maxLength) {
        if (str == null) {
            return null; // Handle null input gracefully
        }

        if (str.length() <= maxLength) {
            return str;
        } else {
            return str.substring(0, maxLength - 2) + "..";
        }
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        Debug.logMessage("--- COMMANDS ---", MessagePriority.OPTIONAL);
        int padSize = 10;
        for (Command command : AltoClef.getCommandExecutor().allCommands()) {
            StringBuilder line = new StringBuilder();
            line.append(command.getName()).append(": ");
            int toAdd = padSize - command.getName().length();
            for (int i = 0; i < toAdd; ++i) {
                line.append(" ");
            }

            line.append(command.getDescription());
            String names = String.join("/", command.getName());
            String name = command.getName();

            MutableText shortDescText = constructLiteralText(" - " + truncate(command.getDescription(), 45 - name.length()));
            shortDescText.setStyle(shortDescText.getStyle().withColor(Formatting.GRAY));

            MutableText namesText = constructLiteralText(names);
            namesText.setStyle(namesText.getStyle().withColor(Formatting.WHITE));

            MutableText hoverText = constructLiteralText("");
            hoverText.setStyle(hoverText.getStyle().withColor(Formatting.GRAY));
            hoverText.append(namesText);
            hoverText.append("\n" + command.getDescription());
            hoverText.append("\nUsage: " + mod.getModSettings().getCommandPrefix() + command.getHelpRepresentation());

            String clickCommand = mod.getModSettings().getCommandPrefix() + command.getName();

            MutableText message = constructLiteralText(""); // indentation
            message.append(name);
            message.setStyle(message.getStyle().withColor(Formatting.WHITE));
            message.append(" ");
            message.append(shortDescText);
            message.setStyle(message.getStyle()
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
            Debug.logMessage(message, true);
        }
        Debug.logMessage("---------------", MessagePriority.OPTIONAL);
        finish();
    }
}
