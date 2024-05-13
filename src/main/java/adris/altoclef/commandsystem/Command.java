package adris.altoclef.commandsystem;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;

public abstract class Command {

    private final ArgParser parser;
    private final String name;
    private final String description;
    private AltoClef mod;
    private Runnable onFinish = null;

    public Command(String name, String description, ArgBase... args) {
        this.name = name;
        this.description = description;
        parser = new ArgParser(args);
    }

    public void run(AltoClef mod, String line, Runnable onFinish) throws CommandException {
        this.onFinish = onFinish;
        this.mod = mod;
        parser.loadArgs(line, true);
        call(mod, parser);
    }

    protected void finish() {
        if (onFinish != null)
            //noinspection unchecked
            onFinish.run();
    }

    public String getHelpRepresentation() {
        StringBuilder sb = new StringBuilder(name);
        for (ArgBase arg : parser.getArgs()) {
            sb.append(" ");
            sb.append(arg.getHelpRepresentation());
        }
        return sb.toString();
    }

    protected void log(Object message) {
        Debug.logMessage(message.toString());
    }

    protected void logError(Object message) {
        Debug.logError(message.toString());
    }

    protected abstract void call(AltoClef mod, ArgParser parser) throws CommandException;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
