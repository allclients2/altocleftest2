package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

// TODO (COMPLETE):  Make this collect more than just coal. It should smartly pick alternative sources if coal is too far away or if we simply cannot get a wooden pick.

public class CollectFuelTask extends Task {

    private static final Item[] wrappedCharCoal = new Item[]{ Items.CHARCOAL };
    private static final Item[] wrappedCoal = new Item[]{ Items.COAL };

    private final double targetFuel;

    private Item[] fuelTargetItem;
    private float fuelTargetEfficiency;

    public CollectFuelTask(double targetFuel) {
        this.targetFuel = targetFuel;
    }

    @Override
    protected void onStart(AltoClef mod) {
        // Nothing
    }

    @Override
    protected Task onTick(AltoClef mod) {

        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                // Just collect coal for now.

                // Divide by the number of operations per fuel, for coal its 8, for logs its 1.5. Refer to https://minecraft.fandom.com/wiki/Smelting#Fuel/
                if (!mod.getBlockScanner().anyFound(Blocks.COAL_ORE)) {
                    if (mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getBlockScanner().anyFound(ItemHelper.itemsToBlocks(ItemHelper.LOG))) {
                        if (mod.getItemStorage().hasItem(Items.FURNACE)) {
                            setDebugState("Collecting charcoal. count: " + Math.ceil(targetFuel / 8));
                            fuelTargetEfficiency = 8;
                            fuelTargetItem = wrappedCharCoal;
                            return TaskCatalogue.getItemTask(Items.CHARCOAL, (int) Math.ceil(targetFuel / 8));
                        } else { // if (mod.getItemStorage().getItemCount(ItemHelper.LOG) > (int) Math.ceil(targetFuel / 1.5)) {
                            setDebugState("Collecting logs. count: " + Math.ceil(targetFuel / 1.5));
                            fuelTargetEfficiency = 1.5f;
                            fuelTargetItem = ItemHelper.LOG;
                            return TaskCatalogue.getItemTask("log", (int) Math.ceil(targetFuel / 1.5));
                        }
                    } else if (mod.getItemStorage().hasItem(ItemHelper.PLANKS) || mod.getBlockScanner().anyFound(ItemHelper.itemsToBlocks(ItemHelper.PLANKS))) {
                        setDebugState("Collecting planks. count: " + Math.ceil(targetFuel / 1.5));
                        fuelTargetEfficiency = 1.5f;
                        fuelTargetItem = ItemHelper.PLANKS;
                        return TaskCatalogue.getItemTask("planks", (int) Math.ceil(targetFuel / 1.5));
                    }
                }

                setDebugState("Collecting coal. count: " + Math.ceil(targetFuel / 8));

                fuelTargetEfficiency = 8;
                fuelTargetItem = wrappedCoal;
                return TaskCatalogue.getItemTask(Items.COAL, (int) Math.ceil(targetFuel / 8));
            }
            case END -> {
                setDebugState("Going to overworld, since, well, no more fuel can be found here.");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            case NETHER -> {
                setDebugState("Going to overworld, since we COULD use wood but wood confuses the bot. A bug at the moment.");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }

        Debug.logError("INVALID DIMENSION: " + WorldHelper.getCurrentDimension());


        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        // Nothing
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CollectFuelTask task) {
            return Math.abs(task.targetFuel - targetFuel) < 0.01;
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return mod.getItemStorage().getItemCount(fuelTargetItem) >= (targetFuel / fuelTargetEfficiency);
    }

    @Override
    protected String toDebugString() {
        return "Collect Fuel: x" + targetFuel;
    }
}
