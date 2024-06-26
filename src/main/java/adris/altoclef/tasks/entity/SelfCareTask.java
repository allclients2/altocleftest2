package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class SelfCareTask extends Task {
    private static final ItemTarget[] woodToolTargets = ItemHelper.toItemTargets(ItemHelper.woodToolSet);
    private static final ItemTarget[] stoneToolTargets = ItemHelper.toItemTargets(ItemHelper.stoneToolSet);
    private static final ItemTarget[] ironToolTargets = ItemHelper.toItemTargets(ItemHelper.ironToolSet);
    private static final ItemTarget[] diamondToolSet = ItemHelper.toItemTargets(ItemHelper.diamondToolSet);
    private static final ItemTarget[] netheriteToolSet = ItemHelper.toItemTargets(ItemHelper.netheriteToolSet);
    private static final Item[] ironArmorSet = ItemHelper.ironArmorSet;
    private static final Item[] diamondArmorSet = ItemHelper.diamondArmorSet;
    private static final Item[] netheriteArmorSet = ItemHelper.netheriteArmorSet;

    private static final Task getBed = TaskCatalogue.getItemTask("bed", 1);
    private static final Task getFood = new CollectFoodTask(65);
    private static final Task sleepThroughNight = new SleepThroughNightTask();
    private static final Task equipShield = new EquipArmorTask(Items.SHIELD);
    private static final Task getWaterBucket = new EquipArmorTask(Items.SHIELD);

    private static Task getToolSet;
    private static Task equipArmorSet;

    private static boolean taskUnfinished(AltoClef mod, Task task) {
        //Debug.logMessage("Task is null:" + (task == null));
        return !task.isFinished(mod);
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        final boolean hasWoodToolSet = mod.getItemStorage().hasItem(woodToolTargets);
        final boolean hasStoneToolSet = mod.getItemStorage().hasItem(stoneToolTargets);
        final boolean hasIronToolSet = mod.getItemStorage().hasItem(ironToolTargets);
        final boolean hasBed = mod.getItemStorage().hasItem(ItemHelper.BED);
        final boolean hasWaterBucket = mod.getItemStorage().hasItem(Items.WATER_BUCKET);
        final boolean hasIronArmorSet = StorageHelper.isArmorEquippedAll(mod, ironArmorSet);
        final boolean hasDiamondToolSet = mod.getItemStorage().hasItem(diamondToolSet);
        final boolean hasDiamondArmorSet = StorageHelper.isArmorEquippedAll(mod, diamondArmorSet);
        final boolean hasNetheriteToolSet = mod.getItemStorage().hasItem(netheriteToolSet);
        final boolean hasNetheriteArmorSet = StorageHelper.isArmorEquippedAll(mod, netheriteArmorSet);


        // FIXME: If you lose lets say a waterbucket after getting a DiamondToolSet, its never checked again so it will never obtain another. This concept is also true for beds, shields, etc..

        if (!hasNetheriteToolSet) {
            if (!hasDiamondToolSet) {
                if (!hasIronToolSet) {
                    if (!hasStoneToolSet) {
                        if (!hasWoodToolSet) {
                            getToolSet = TaskCatalogue.getSquashedItemTask(woodToolTargets);
                        } else {
                            getToolSet = TaskCatalogue.getSquashedItemTask(stoneToolTargets);
                        }
                    } else if (taskUnfinished(mod, equipShield)) { // AFTER stone tool set Get shield and bed before iron toolset, i think it's very important.
                        return equipShield;
                    } else if (taskUnfinished(mod, getBed)) { // AFTER shield get a bed
                        return getBed;
                    } else {
                        getToolSet = TaskCatalogue.getSquashedItemTask(ironToolTargets);
                    }
                } else if (taskUnfinished(mod, getWaterBucket)) { // AFTER iron tool Get water bucket for falls.
                    return getWaterBucket;
                } else if (!hasIronArmorSet) { // AFTER water bucket get iron armor.
                    equipArmorSet = new EquipArmorTask(ironArmorSet);
                } else {
                    getToolSet = TaskCatalogue.getSquashedItemTask(diamondToolSet);
                }
            } else if (!hasDiamondArmorSet) {
                equipArmorSet = new EquipArmorTask(diamondArmorSet);
            } else {
                getToolSet = TaskCatalogue.getSquashedItemTask(netheriteToolSet);
            }
        } else if (!hasNetheriteArmorSet) {
            equipArmorSet = new EquipArmorTask(netheriteArmorSet);
        }


        // Priority
        if (mod.IsNight()) {
            setDebugState("Sleeping through night");
            return sleepThroughNight;
        } else if (taskUnfinished(mod, getFood)) {
            setDebugState("Getting food");
            return getFood;
        } else if (getToolSet != null && taskUnfinished(mod, getToolSet)) {
            setDebugState("Getting a toolset");
            return getToolSet;
        } else if (equipArmorSet != null && taskUnfinished(mod, equipArmorSet)) {
            setDebugState("Getting an armor set");
            return equipArmorSet;
        }

        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("Returning to overworld");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        setDebugState("All tasks done; wandering indefinitely.");
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SelfCareTask;
    }

    @Override
    protected String toDebugString() {
        return "Caring self";
    }
}
