package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
        mod.log("--- COMMANDS ---", MessagePriority.OPTIONAL);
        int padSize = 10;
        for (Command command : AltoClef.getCommandExecutor().allCommands()) {
            StringBuilder line = new StringBuilder();
            //line.append("");
            line.append(command.getName()).append(": ");
            int toAdd = padSize - command.getName().length();
            for (int i = 0; i < toAdd; ++i) {
                line.append(" ");
            }

            //Ripped from baritone because noob ;(
            line.append(command.getDescription());
            String names = String.join("/", command.getName());
            String name = command.getName();
            MutableText shortDescText = Text.literal(" - " + truncate(command.getDescription(), 45 - name.length()));
            shortDescText.setStyle(shortDescText.getStyle().withColor(Formatting.DARK_GRAY));
            MutableText namesText = Text.literal(names);
            namesText.setStyle(namesText.getStyle().withColor(Formatting.WHITE));
            MutableText hoverText = Text.literal("");
            hoverText.setStyle(hoverText.getStyle().withColor(Formatting.GRAY));
            hoverText.append(namesText);
            hoverText.append("\n" + command.getDescription());
            hoverText.append("\nUsage: " + command.getHelpRepresentation());
            String clickCommand = mod.getModSettings().getCommandPrefix() + command.getName();
            MutableText Message = Text.literal(""); //indentation
            Message.append(name);
            Message.setStyle(Message.getStyle().withColor(Formatting.GRAY));
            Message.append(" ");
            Message.append(shortDescText);
            Message.setStyle(Message.getStyle()
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
            Debug.logMessage(Message, true);
        }
        mod.log("---------------", MessagePriority.OPTIONAL);
        finish();
    }
}
