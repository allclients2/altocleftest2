package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CubeBounds;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.schematic.ISchematic;
import baritone.api.utils.Pair;
import baritone.process.BuilderProcess;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchematicBuildTask extends Task {
    private boolean finished;
    private BuilderProcess builder;
    private String schematicFileName;
    private BlockPos startPos;
    private int allowedResourceStackCount;
    private Map<BlockState, Integer> missing;
    private ISchematic schematic;
    private Vec3i origin;
    private static final int FOOD_UNITS = 80;
    private static final int MIN_FOOD_UNITS = 10;
    private final TimerGame _clickTimer = new TimerGame(120);
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);
    private Task walkAroundTask;

    public SchematicBuildTask(final String schematicFileName) {
        this(schematicFileName,  MinecraftClient.getInstance().player.getBlockPos());
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos) {
        this(schematicFileName, startPos, 64);
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos, final int allowedResourceStackCount) {
        this.schematicFileName = schematicFileName;
        this.startPos = startPos;
        this.allowedResourceStackCount = allowedResourceStackCount;
    }

    @Override
    protected void onStart(AltoClef mod) {
        this.finished = false;

        if (isNull(builder)) {
            builder = mod.getClientBaritone().getBuilderProcess();
        }

        final File file = new File("schematics/" + schematicFileName);

        if (!file.exists()) {
            Debug.logMessage("Could not locate schematic file. Terminating...");
            this.finished = true;
            return;
        }

        builder.build(schematicFileName, startPos);


        updateBuilderData();
        setupAvoidancePredicate(mod);



        _moveChecker.reset();
        _clickTimer.reset();
    }

    private List<BlockState> getTodoList(final AltoClef mod, final Map<BlockState, Integer> missing) {
        final ItemStorageTracker inventory = mod.getItemStorage();
        int finishedStacks = 0;
        final List<BlockState> listOfFinished = new ArrayList<>();

        for (final BlockState state : missing.keySet()) {
            final Item item = state.getBlock().asItem();
            final int count = inventory.getItemCount(item);
            final int maxCount = item.getMaxCount();

            if (finishedStacks < this.allowedResourceStackCount) {
                listOfFinished.add(state);
                if (count >= missing.get(state)) {
                    finishedStacks++;
                    listOfFinished.remove(state);
                } else if (count >= maxCount) {
                    finishedStacks += count / maxCount;

                    if (finishedStacks >= this.allowedResourceStackCount) {
                        listOfFinished.remove(state);
                    }
                }
            }
        }

        return listOfFinished;
    }

    private boolean isNull(Object o) {
        return o == null;
    }

    private void updateBuilderData() {
        Map<BlockState, Integer> missingcheck = builder.getMissing();
        if (missingcheck != null) {
            missing = missingcheck;
        }

        if (schematic == null || origin == null) {
            Pair<ISchematic, BlockPos> SchemAndOrigin = builder.getSchemAndOrigin();
            schematic = SchemAndOrigin.first();
            origin = SchemAndOrigin.second();
        }
    }


    @Override
    protected Task onTick(AltoClef mod) {
        updateBuilderData();

        if (!isNull(missing) && !missing.isEmpty() && (builder.isPaused() || !builder.isActive())) {
            if (mod.getPlayer().getHungerManager().getFoodLevel() < MIN_FOOD_UNITS) {
                return new CollectFoodTask(FOOD_UNITS);
            }
            setDebugState("Checking list");
            for (final BlockState state : missing.keySet()) { //getTodoList(mod, missing)
                Item item = state.getBlock().asItem();
                int countRequired = missing.get(state);
                if (mod.getItemStorage().getItemCount(item) < countRequired) {
                    setDebugState("Getting " + state.getBlock().asItem().toString());
                    return TaskCatalogue.getItemTask(state.getBlock().asItem(), countRequired);
                }
            }
            //Then we couldnt get anything so just resume.
            missing.clear();
        } else if (builder.isPaused() || !builder.isActive()) {
            builder.resume();
            System.out.println("Resuming builder...");
        }

        System.out.println("missing altoclef perspective: " + missing);
        if (_moveChecker.check(mod)) {
            _clickTimer.reset();
        }

        if (_clickTimer.elapsed()) {
            if (walkAroundTask == null) {
                walkAroundTask = new TimeoutWanderTask(5);
            }
            Debug.logMessage("Anti stuck timer elapsed.");
        }

        if (walkAroundTask != null) {
            if (!walkAroundTask.isFinished(mod)) {
                return walkAroundTask;
            } else {
                walkAroundTask = null;
                builder.popStack();
                _clickTimer.reset();
                _moveChecker.reset();
            }
        }

        return null;
    }

    private void setupAvoidancePredicate(AltoClef mod) {
        /*
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();
        int widthMax = schematic.widthX() + originX;
        int heightMax = schematic.heightY() + originY;
        int depthMax = schematic.lengthZ() + originZ; // Corrected variable name from widthMax to depthMax
        mod.getBehaviour().avoidBlockBreaking(block -> {
            int blockX = block.getX();
            int blockY = block.getY();
            int blockZ = block.getZ();
            // check if the blocks position is in the schematic build area
            if (
                blockX >= originX && blockX < widthMax && // X
                blockY >= originY && blockY < heightMax && // Y
                blockZ >= originZ && blockZ < depthMax // Z
            ) {
                //mod.log("said to avoid breaking at " + blockX + "," + blockY + "," + blockZ + ".");
                return true;
            } else {
                return false;
            }
        });
        */

        BlockPos originBlockPos = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
        CubeBounds avoidanceBound = new CubeBounds(originBlockPos, schematic.widthX(), schematic.heightY(), schematic.lengthZ());
        mod.getBehaviour().avoidBlockBreaking(block -> avoidanceBound.inside(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        builder.pause();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SchematicBuildTask;
    }

    @Override
    protected String toDebugString() {
        return "Building Schematic...";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (!isNull(builder) && builder.isFromAltoclefFinished() || this.finished) {
            return true;
        }
        return false;
    }
}