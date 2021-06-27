package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.DefaultGoToDimensionTask;
import adris.altoclef.tasks.GetToBlockTask;
import adris.altoclef.tasks.GetToXZTask;
import adris.altoclef.util.Dimension;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GotoCommand extends Command {
    private static final int EMPTY = -1;

    public GotoCommand() throws CommandException {
        super("goto", "Tell bot to travel to a set of coordinates.", new Arg(String.class, "dimension"), new Arg(Integer.class, "X"), new Arg(Integer.class, "Y"), new Arg(Integer.class, "Z"));
    }
    @Override
    protected void Call(AltoClef mod, ArgParser parser) throws CommandException {
        String dimension = (String) parser.Get(String.class);
        int x = parser.Get(Integer.class),
                y = parser.Get(Integer.class),
                z = parser.Get(Integer.class);
        List<String> validDimensions = Arrays.asList("overworld", "nether", "end");
        HashMap<String, Dimension> dimensionHashMap = new HashMap<String, Dimension>();
        dimensionHashMap.put("overworld", Dimension.OVERWORLD);
        dimensionHashMap.put("nether", Dimension.NETHER);
        dimensionHashMap.put("end", Dimension.END);
        if(!validDimensions.contains(dimension)) {
            mod.log(dimension + "does not seem to be a valid dimension. Here are the valid ones:");
            mod.log(String.join(", ", validDimensions));
            finish();
            return;
        }
        if (y != EMPTY) {
            mod.runUserTask(new DefaultGoToDimensionTask(dimensionHashMap.get(dimension)), nothing -> {
                mod.runUserTask(new GetToBlockTask(new BlockPos(x, y, z), false), nothing1 -> finish());
            });
        } else {
            mod.runUserTask(new DefaultGoToDimensionTask(dimensionHashMap.get(dimension)), nothing -> {
                mod.runUserTask(new GetToXZTask(x, z), nothing1 -> finish());
            });
        }
    }
}
