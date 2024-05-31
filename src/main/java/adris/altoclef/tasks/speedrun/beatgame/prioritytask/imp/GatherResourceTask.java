package adris.altoclef.tasks.speedrun.beatgame.prioritytask.imp;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.imp.tasks.PriorityTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;


@Deprecated
public final class GatherResourceTask extends PriorityTask {
    public final int minCount;
    public final int maxCount;
    public final Item[] toCollect;
    private final Predicate<Item[]> canGather;
    private boolean shouldForce = false;
    private boolean canCache = true;
    private PriorityCalculator priorityCalculator;
    public Optional<Object> data;
    public boolean minCountSatisfied = false;
    public boolean maxCountSatisfied = false;

    //this can be used if we are sure we would want to end this task earlier than 3 seconds after being invoked
    private boolean bypassForceCooldown = false;
    private Predicate<AltoClef> needsCraftingOnStart = ((mod) -> false);
    private String description;

    public GatherResourceTask(int minCount, int maxCount, PriorityCalculator priorityCalculator,
                              Predicate<Item[]> canGather, Item... toCollect) {
        this(minCount, maxCount, priorityCalculator, canGather, Optional.empty(), toCollect);
    }

    public GatherResourceTask(int minCount, int maxCount, PriorityCalculator priorityCalculator,
                              Predicate<Item[]> canGather, Optional<Object> data, Item... toCollect) {
        super(a -> true, false, false, false);

        this.minCount = minCount;
        this.maxCount = maxCount;
        this.priorityCalculator = priorityCalculator;
        this.canGather = canGather;
        this.data = data;
        this.toCollect = toCollect;

        this.description = "gather items: " + Arrays.toString(toCollect);
    }


    /**
     * @return the priority of this resource
     */
    public double getPriority(AltoClef mod) {
        int count = mod.getItemStorage().getItemCount(toCollect);

        //special case for ores
        if (toCollect.length == 1) {
            Item item = toCollect[0];
            if (item == Items.RAW_IRON || item == Items.RAW_GOLD || item == Items.DIAMOND) {
                count = BeatMinecraftTask.getCountWithCraftedFromOre(mod, item);
            }
        }

        if (minCountSatisfied) {
            count = Math.max(count, minCount);
        }
        if (count >= minCount) {
            minCountSatisfied = true;
        }

        if (count >= maxCount || maxCountSatisfied) {
            maxCountSatisfied = true;
            return Double.NEGATIVE_INFINITY;
        }

        if (!canGather.test(toCollect)) return Double.NEGATIVE_INFINITY;

        return priorityCalculator.calculate(toCollect, count, minCount, maxCount);
    }

    public GatherResourceTask withPriorityCalculator(PriorityCalculator calculator) {
        this.priorityCalculator = calculator;
        return this;
    }

    public GatherResourceTask withBypassForceCooldown(boolean value) {
        this.bypassForceCooldown = value;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GatherResourceTask withDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean bypassesForceCooldown() {
        return bypassForceCooldown;
    }

    public GatherResourceTask withShouldForce(boolean value) {
        this.shouldForce = value;
        return this;
    }

    public boolean shouldForceTask() {
        return shouldForce;
    }

    public GatherResourceTask withCanCache(boolean value) {
        this.canCache = value;
        return this;
    }

    public boolean canCacheTask() {
        return canCache;
    }

    public GatherResourceTask withNeedsCraftingOnStart(Predicate<AltoClef> value) {
        this.needsCraftingOnStart = value;
        return this;
    }

    public boolean needsCraftingOnStart(AltoClef mod) {
        return needsCraftingOnStart.test(mod);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GatherResourceTask) obj;
        return this.minCount == that.minCount &&
                this.maxCount == that.maxCount &&
                Objects.equals(this.priorityCalculator, that.priorityCalculator) &&
                Objects.equals(this.canGather, that.canGather) &&
                Objects.equals(this.data, that.data) &&
                Objects.equals(this.toCollect, that.toCollect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minCount, maxCount, priorityCalculator, canGather, data, toCollect);
    }

    @Override
    public String toString() {
        return "GatherResource[" +
                "minCount=" + minCount + ", " +
                "maxCount=" + maxCount + ", " +
                "priorityCalculator=" + priorityCalculator + ", " +
                "canGather=" + canGather + ", " +
                "data=" + data + ", " +
                "toCollect=" + Arrays.toString(toCollect) + ']';
    }

    @Override
    public Task getTask(AltoClef mod) {
        Task task;

        if (this.data.isPresent()) {
            Object data = this.data.get();
            if (data instanceof String codeName) {
                task = TaskCatalogue.getItemTask(codeName, this.maxCount);
            } else if (data instanceof Task buildInTask) {
                task = buildInTask;
            } else {
                throw new IllegalStateException("Invalid gather resource data!");
            }
        } else {
            // if code name isn't present toGather shouldn't have more params
            task = TaskCatalogue.getItemTask(this.toCollect[0], this.maxCount);
        }

        return task;
    }

    @Override
    public boolean shouldForce() {
        return shouldForce;
    }

    @Override
    public boolean canCache() {
        return canCache;
    }

    @Override
    public String getDebugString() {
        return "[Deprecated-GatherResource] "+ description;
    }

    public interface PriorityCalculator {
        double calculate(Item[] items, int count, int minCount, int maxCount);
    }


}
