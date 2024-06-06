package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.block.InteractWithBlockTask;
import adris.altoclef.tasks.resources.CollectBucketLiquidTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Items;

/**
 * use when bot has too many water buckets but you dont want to throw them out
 */
public class GetRidOfExtraWaterBucketTask extends Task {

    private boolean needsPickup = false;

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) != 0 && !needsPickup) {
            return new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1),mod.getPlayer().getBlockPos().down(), false);
        }

        needsPickup = true;
        if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) < 1) {
            return new CollectBucketLiquidTask.CollectWaterBucketTask(1);
        }

        return null;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return mod.getItemStorage().getItemCount(Items.WATER_BUCKET) == 1 && needsPickup;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof GetRidOfExtraWaterBucketTask;
    }

    @Override
    protected String toDebugString() {
        return null;
    }
}
