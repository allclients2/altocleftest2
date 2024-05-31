package adris.altoclef.tasks.speedrun.beatgame.prioritytask.imp.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;

import java.util.function.Function;

/**
 crafts an item with constant priority... Only runs when it has all the ingredients (NOT including the crafting table itself)
 */
public class CraftItemPriorityTask extends PriorityTask{

    public final double priority;
    public final RecipeTarget recipeTarget;
    private boolean satisfied = false;

    // note: bypassForceCooldown is set to true, because we usually don't want to wait when crafting
    public CraftItemPriorityTask(double priority, RecipeTarget toCraft) {
        this(priority, toCraft, mod -> true);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall) {
        this(priority, toCraft, mod -> true, false, true, true);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this(priority, toCraft, mod -> true, shouldForce, canCache, bypassForceCooldown);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.priority = priority;
        this.recipeTarget = toCraft;
    }

    @Override
    public Task getTask(AltoClef mod) {
        if (recipeTarget.getRecipe().isBig()) {
            return new CraftInTableTask(recipeTarget);
        }

        return new CraftInInventoryTask(recipeTarget);
    }

    @Override
    public String getDebugString() {
        return "Crafting "+recipeTarget;
    }

    @Override
    protected double getPriority(AltoClef mod) {
        if (mod.getItemStorage().hasItem(recipeTarget.getOutputItem())) {
            satisfied = true;
        }
        if (!hasAllItems(mod.getItemStorage())) return Double.NEGATIVE_INFINITY;
        if (satisfied) return Double.NEGATIVE_INFINITY;

        return priority;
    }

    private boolean hasAllItems(ItemStorageTracker itemStorage) {
        for (ItemTarget target : recipeTarget.getRecipe().getSlots()) {
            if (target == ItemTarget.EMPTY) continue;

            if (!itemStorage.hasItem(target.getMatches())) return false;
        }

        return true;
    }

    @Override
    public boolean needCraftingOnStart() {
        return true;
    }

    public boolean isSatisfied() {
        return satisfied;
    }
}
