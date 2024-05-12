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
import baritone.process.BuilderProcess;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

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
    private CubeBounds bounds;
    private Map<BlockState, Integer> missing;
    private boolean sourced;
    private boolean pause;
    private boolean clearRunning = false;
    private String name;
    private ISchematic schematic;
    private static final int FOOD_UNITS = 80;
    private static final int MIN_FOOD_UNITS = 10;
    private final TimerGame _clickTimer = new TimerGame(120);
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);
    private Task walkAroundTask;

    public SchematicBuildTask(final String schematicFileName) {
        this(schematicFileName,  MinecraftClient.getInstance().player.getBlockPos());
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos) {
        this(schematicFileName, startPos, 3);
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos, final int allowedResourceStackCount) {
        this();
        this.schematicFileName = schematicFileName;
        this.startPos = startPos;
        this.allowedResourceStackCount = allowedResourceStackCount;
    }

    public SchematicBuildTask() {
        this.sourced = false;
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

        if (isNull(this.schematic)) {
            builder.build(schematicFileName, startPos); //TODO: I think there should be a state queue in baritone
        } else {
            builder.build(this.name, this.schematic, startPos);
        }

        this.pause = false;

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

    private void overrideMissing() {
        this.missing = builder.getMissing();
    }

    private Map<BlockState, Integer> getMissing() {
        if (isNull(this.missing)) {
            overrideMissing();
        }
        return this.missing;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (clearRunning && builder.isActive()) {
            return null;
        }

        clearRunning = false;
        overrideMissing();
        this.sourced = false;

        System.out.println(getMissing());
        if (!isNull(getMissing()) && !getMissing().isEmpty() && (builder.isPaused() || !builder.isActive())) {
            if (mod.getPlayer().getHungerManager().getFoodLevel() < MIN_FOOD_UNITS) {
                return new CollectFoodTask(FOOD_UNITS);
            }
            mod.log("checking list");
            for (final BlockState state : getTodoList(mod, missing)) {
                mod.log("getting" + state.toString());
                return TaskCatalogue.getItemTask(state.getBlock().asItem(), missing.get(state));
            }
            this.sourced = true;
        }

        if (this.sourced && !builder.isActive()) {

            builder.resume();
            Debug.logMessage("Resuming build process...");
            System.out.println("Resuming builder...");
        }

        if (_moveChecker.check(mod)) {
            _clickTimer.reset();
        }

        if (_clickTimer.elapsed()) {
            if (isNull(walkAroundTask)) {
                walkAroundTask = new TimeoutWanderTask(5);
            }
            Debug.logMessage("Timer elapsed.");
        }

        if (!isNull(walkAroundTask)) {
            if (!walkAroundTask.isFinished(mod)) {
                return walkAroundTask;
            } else {
                walkAroundTask = null;
                builder.popStack();
                _clickTimer.reset();
                _moveChecker.reset();
            }
        }

        /*/
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable() != null) {
            if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable().stream().anyMatch(e ->  e != null && e.getBlock() instanceof BedBlock)) {
                System.out.println("ALT: true");
            }
            BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable().forEach(e -> {
                if (Utils.isSet(e) && e.getBlock().asItem().toString() != "air") {
                    System.out.println(e.getBlock().getName());
                    System.out.println(e.getBlock().asItem().getName());
                    System.out.println(e.getBlock().asItem().toString());
                    System.out.println(e.getBlock().toString());
                    System.out.println("(((((((((");
                    System.out.println(e.getBlock().getDefaultState());
                    System.out.println(")))))))))");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                }
            });
        }*/
        //mod.getInventoryTracker().getItemStackInSlot(mod.getInventoryTracker().getInventorySlotsWithItem(Items.OAK_DOOR).get(0)).getItem().
        /*
        missing.forEach((k,e) -> {
            if (Utils.isSet(k)) {
                System.out.println(k);
            }
        });*/

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        builder.pause();
        this.pause = true;
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