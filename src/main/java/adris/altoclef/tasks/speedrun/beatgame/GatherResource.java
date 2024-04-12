package adris.altoclef.tasks.speedrun.beatgame;

import adris.altoclef.AltoClef;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;


public final class GatherResource {
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

    public GatherResource(int minCount, int maxCount, PriorityCalculator priorityCalculator,
                          Predicate<Item[]> canGather, Item... toCollect) {
        this(minCount, maxCount, priorityCalculator, canGather, Optional.empty(), toCollect);
    }

    public GatherResource(int minCount, int maxCount, PriorityCalculator priorityCalculator,
                          Predicate<Item[]> canGather, Optional<Object> data, Item... toCollect) {
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

    public GatherResource withPriorityCalculator(PriorityCalculator calculator) {
        this.priorityCalculator = calculator;
        return this;
    }

    public GatherResource withBypassForceCooldown(boolean value) {
        this.bypassForceCooldown = value;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GatherResource withDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean bypassesForceCooldown() {
        return bypassForceCooldown;
    }

    public GatherResource withShouldForce(boolean value) {
        this.shouldForce = value;
        return this;
    }

    public boolean shouldForceTask() {
        return shouldForce;
    }

    public GatherResource withCanCache(boolean value) {
        this.canCache = value;
        return this;
    }

    public boolean canCacheTask() {
        return canCache;
    }

    public GatherResource withNeedsCraftingOnStart(Predicate<AltoClef> value) {
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
        var that = (GatherResource) obj;
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

    public interface PriorityCalculator {
        double calculate(Item[] items, int count, int minCount, int maxCount);
    }


}
