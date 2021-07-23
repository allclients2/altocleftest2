package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.misc.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.WorldUtil;
import adris.altoclef.util.csharpisbetter.TimerGame;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class ConstructNetherPortalObsidianTask extends Task {

    // There's some code duplication between here and ConstructNetherPortalBucketTask...
    // but it's so heavily intertwined/changed that it would take forever to untangle and
    // retangle the two together.

    // Order here matters
    private static final Vec3i[] PORTAL_FRAME = new Vec3i[]{
            // Bottom
            new Vec3i(0, -1, 0),
            new Vec3i(0, -1, 1),
            // Left side
            new Vec3i(0, 0, -1),
            new Vec3i(0, 1, -1),
            new Vec3i(0, 2, -1),
            // Right side
            new Vec3i(0, 0, 2),
            new Vec3i(0, 1, 2),
            new Vec3i(0, 2, 2),
            // Top
            new Vec3i(0, 3, 0),
            new Vec3i(0, 3, 1)
    };

    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            new Vec3i(0, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 2, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 2, 1)
    };

    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(3, 6, 6);
    private static final Vec3i PORTAL_ORIGIN_RELATIVE_TO_REGION = new Vec3i(1, 0, 2);

    private final TimerGame _areaSearchTimer = new TimerGame(5);

    private BlockPos _origin;

    private BlockPos _destroyTarget;

    private static BlockPos getBuildableAreaNearby(AltoClef mod) {
        BlockPos checkOrigin = mod.getPlayer().getBlockPos();
        for (BlockPos toCheck : WorldUtil.scanRegion(mod, checkOrigin, checkOrigin.add(PORTALABLE_REGION_SIZE))) {
            BlockState state = MinecraftClient.getInstance().world.getBlockState(toCheck);
            boolean validToWorld = (WorldUtil.canPlace(mod, toCheck) || WorldUtil.canBreak(mod, toCheck));
            if (!validToWorld || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK) {
                return null;
            }
        }
        return checkOrigin;
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();

        // Avoid breaking portal frame if we're obsidian.
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (_origin != null) {
                // Don't break frame
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = _origin.add(framePosRelative);
                    if (block.equals(framePos)) {
                        return mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN;
                    }
                }
            }
            return false;
        });
        mod.getBehaviour().addProtectedItems(Items.FLINT_AND_STEEL);
    }

    @Override
    protected Task onTick(AltoClef mod) {

        int neededObsidian = 10;
        BlockPos placeTarget = null;
        if (_origin != null) {
            for (Vec3i frameOffs : PORTAL_FRAME) {
                BlockPos framePos = _origin.add(frameOffs);
                if (!mod.getBlockTracker().blockIsValid(framePos, Blocks.OBSIDIAN)) {
                    placeTarget = framePos;
                    break;
                }
                neededObsidian--;
            }
        }

        // Get obsidian if we don't have.
        if (mod.getInventoryTracker().getItemCount(Items.OBSIDIAN) < neededObsidian) {
            setDebugState("Getting obsidian");
            return TaskCatalogue.getItemTask("obsidian", neededObsidian);
        }

        // Find spot
        if (_origin == null) {
            if (_areaSearchTimer.elapsed()) {
                _areaSearchTimer.reset();
                Debug.logMessage("(Searching for area to build portal nearby...)");
                _origin = getBuildableAreaNearby(mod);
            }
            setDebugState("Looking for portalable area...");
            return new TimeoutWanderTask();
        }

        // Get flint and steel
        if (!mod.getInventoryTracker().hasItem(Items.FLINT_AND_STEEL)) {
            setDebugState("Getting flint and steel");
            return TaskCatalogue.getItemTask("flint_and_steel", 1);
        }

        // Place frame
        if (placeTarget != null) {
            setDebugState("Placing frame...");
            return new PlaceBlockTask(placeTarget, Blocks.OBSIDIAN);
        }

        // Clear middle
        if (_destroyTarget != null && !WorldUtil.isAir(mod, _destroyTarget)) {
            return new DestroyBlockTask(_destroyTarget);
        }
        for (Vec3i middleOffs : PORTAL_INTERIOR) {
            BlockPos middlePos = _origin.add(middleOffs);
            if (!WorldUtil.isAir(mod, middlePos)) {
                _destroyTarget = middlePos;
                return new DestroyBlockTask(_destroyTarget);
            }
        }

        // Flint and steel
        return new InteractWithBlockTask(new ItemTarget("flint_and_steel", 1), Direction.UP, _origin.down(), true);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof ConstructNetherPortalObsidianTask;
    }

    @Override
    protected String toDebugString() {
        return "Building nether portal with obsidian";
    }
}
