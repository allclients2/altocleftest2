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
        Debug.logMessage("--- COMMANDS ---", MessagePriority.OPTIONAL);
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
            shortDescText.setStyle(shortDescText.getStyle().withColor(Formatting.GRAY));

            MutableText namesText = Text.literal(names);
            namesText.setStyle(namesText.getStyle().withColor(Formatting.WHITE));

            MutableText hoverText = Text.literal("");
            hoverText.setStyle(hoverText.getStyle().withColor(Formatting.GRAY));
            hoverText.append(namesText);
            hoverText.append("\n" + command.getDescription());
            hoverText.append("\nUsage: " + mod.getModSettings().getCommandPrefix() + command.getHelpRepresentation());

            String clickCommand = mod.getModSettings().getCommandPrefix() + command.getName();

            MutableText Message = Text.literal(""); //indentation
            Message.append(name);
            Message.setStyle(Message.getStyle().withColor(Formatting.WHITE));
            Message.append(" ");
            Message.append(shortDescText);
            Message.setStyle(Message.getStyle()
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
            Debug.logMessage(Message, true);
        }
        Debug.logMessage("---------------", MessagePriority.OPTIONAL);
        finish();
    }
}

public class CommandFormatter {

    public static MutableText formatCommandDescription(Command command, Mod mod) {
        // Extract command details
        String description = command.getDescription();
        String names = String.join("/", command.getName());
        String name = command.getName();

        // Create short description text
        MutableText shortDescText = new LiteralText(" - " + truncate(description, 45 - name.length()));
        shortDescText.setStyle(shortDescText.getStyle().withColor(Formatting.GRAY));

        // Create names text
        MutableText namesText = new LiteralText(names);
        namesText.setStyle(namesText.getStyle().withColor(Formatting.WHITE));

        // Create hover text
        MutableText hoverText = new LiteralText("");
        hoverText.setStyle(hoverText.getStyle().withColor(Formatting.GRAY));
        hoverText.append(namesText);
        hoverText.append("\n" + description);
        hoverText.append("\nUsage: " + mod.getModSettings().getCommandPrefix() + command.getHelpRepresentation());

        // Create click command
        String clickCommand = mod.getModSettings().getCommandPrefix() + command.getName();

        // Create the final message text
        MutableText message = new LiteralText(""); // Indentation
        message.append(name);
        message.setStyle(message.getStyle().withColor(Formatting.WHITE));
        message.append(" ");
        message.append(shortDescText);
        message.setStyle(message.getStyle()
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));

        return message;
    }

    private static String truncate(String str, int maxLength) {
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }

    public static void main(String[] args) {
        // Example usage
        Command command = new Command("example", "This is an example command description.", "example");
        Mod mod = new Mod();
        MutableText message = formatCommandDescription(command, mod);
        // Print or log the message
        System.out.println(message.getString());
    }
}



