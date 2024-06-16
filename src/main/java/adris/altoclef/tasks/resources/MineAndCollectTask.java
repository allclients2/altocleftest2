package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.ToolMaterialVer;
import adris.altoclef.tasks.block.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.inventory.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.CursorSlot;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class MineAndCollectTask extends ResourceTask {

    private final Block[] blocksToMine;

    private final MiningRequirement requirement;

    private final TimerGame cursorStackTimer = new TimerGame(3);

    private final MineOrCollectTask subtask;

    public MineAndCollectTask(ItemTarget[] itemTargets, Block[] blocksToMine, MiningRequirement requirement) {
        super(itemTargets);
        this.requirement = requirement;
        this.blocksToMine = blocksToMine;
        subtask = new MineOrCollectTask(this.blocksToMine, itemTargets);
    }

    public MineAndCollectTask(ItemTarget[] blocksToMine, MiningRequirement requirement) {
        this(blocksToMine, itemTargetToBlockList(blocksToMine), requirement);
    }

    public MineAndCollectTask(ItemTarget target, Block[] blocksToMine, MiningRequirement requirement) {
        this(new ItemTarget[]{target}, blocksToMine, requirement);
    }

    public MineAndCollectTask(Item item, int count, Block[] blocksToMine, MiningRequirement requirement) {
        this(new ItemTarget(item, count), blocksToMine, requirement);
    }

    public static Block[] itemTargetToBlockList(ItemTarget[] targets) {
        List<Block> result = new ArrayList<>(targets.length);
        for (ItemTarget target : targets) {
            for (Item item : target.getMatches()) {
                Block block = Block.getBlockFromItem(item);
                if (block != null && !WorldHelper.isAir(block)) {
                    result.add(block);
                }
            }
        }
        return result.toArray(Block[]::new);
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(blocksToMine);

        // We're mining, so don't throw away pickaxes.
        mod.getBehaviour().addProtectedItems(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);

        subtask.resetSearch();
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        // Picking up is controlled by a separate task here.
        return true;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {

        if (!StorageHelper.miningRequirementMet(mod, requirement)) {
            return new SatisfyMiningRequirementTask(requirement);
        }

        if (subtask.isMining()) {
            makeSureToolIsEquipped(mod);
        }

        // Wrong dimension check.
        if (subtask.wasWandering() && isInWrongDimension(mod)) {
            return getToCorrectDimensionTask(mod);
        }

        return subtask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(blocksToMine);
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof MineAndCollectTask task) {
            return Arrays.equals(task.blocksToMine, blocksToMine);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Mine And Collect";
    }

    private void makeSureToolIsEquipped(AltoClef mod) {
        if (cursorStackTimer.elapsed() && !mod.getFoodChain().isTryingToEat()) {
            assert MinecraftClient.getInstance().player != null;
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (cursorStack != null && !cursorStack.isEmpty()) {
                // We have something in our cursor stack
                Item item = cursorStack.getItem();
                if (item.getDefaultStack().isSuitableFor(mod.getWorld().getBlockState(subtask.miningPos()))) {
                    // Our cursor stack would help us mine our current block
                    Item currentlyEquipped = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
                    if (item instanceof MiningToolItem) {
                        if (currentlyEquipped instanceof MiningToolItem currentPick) {
                            MiningToolItem swapPick = (MiningToolItem) item;
                            if (ToolMaterialVer.getMiningLevel(swapPick) > ToolMaterialVer.getMiningLevel(currentPick)) {
                                // We can equip a better pickaxe.
                                mod.getSlotHandler().forceEquipSlot(CursorSlot.SLOT);
                            }
                        } else {
                            // We're not equipped with a pickaxe...
                            mod.getSlotHandler().forceEquipSlot(CursorSlot.SLOT);
                        }
                    }
                }
            }
            cursorStackTimer.reset();
        }
    }

    public static class MineOrCollectTask extends AbstractDoToClosestObjectTask<Object> {

        private final Block[] blocks;
        private final ItemTarget[] targets;
        private final Set<BlockPos> blacklist = new HashSet<>();
        private final MovementProgressChecker _progressChecker = new MovementProgressChecker(1);
        private final Task pickupTask;
        private BlockPos miningPos;
        private AltoClef currentMod;

        public MineOrCollectTask(Block[] blocks, ItemTarget[] targets) {
            this.blocks = blocks;
            this.targets = targets;
            pickupTask = new PickupDroppedItemTask(this.targets, true);
        }

        @Override
        protected Vec3d getPos(AltoClef mod, Object obj) {
            if (obj instanceof BlockPos b) {
                return WorldHelper.toVec3d(b);
            }
            if (obj instanceof ItemEntity item) {
                return item.getPos();
            }
            throw new UnsupportedOperationException("Shouldn't try to get the position of object " + obj + " of type " + (obj != null ? obj.getClass().toString() : "(null object)"));
        }

        @Override
        protected Optional<Object> getClosestTo(AltoClef mod, Vec3d pos) {
            Pair<Double, Optional<BlockPos>> closestBlock = getClosestBlock(mod,pos,  blocks);
            Pair<Double, Optional<ItemEntity>> closestDrop = getClosestItemDrop(mod,pos,  targets);

            double blockSq = closestBlock.getLeft();
            double dropSq = closestDrop.getLeft();

            // We can't mine right now.
            if (mod.getExtraBaritoneSettings().isInteractionPaused()) {
                return closestDrop.getRight().map(Object.class::cast);
            }

            if (dropSq <= blockSq) {
                return closestDrop.getRight().map(Object.class::cast);
            } else {
                return closestBlock.getRight().map(Object.class::cast);
            }
        }

        public static Pair<Double, Optional<ItemEntity>> getClosestItemDrop(AltoClef mod,Vec3d pos, ItemTarget... items) {
            Optional<ItemEntity> closestDrop = Optional.empty();
            if (mod.getEntityTracker().itemDropped(items)) {
                closestDrop = mod.getEntityTracker().getClosestItemDrop(pos, items);
            }

            return new Pair<>(
                    // + 5 to make the bot stop mining a bit less
                    closestDrop.map(itemEntity -> itemEntity.squaredDistanceTo(pos) + 10).orElse(Double.POSITIVE_INFINITY),
                    closestDrop
            );
        }

        public static Pair<Double,Optional<BlockPos> > getClosestBlock(AltoClef mod,Vec3d pos ,Block... blocks) {
            Optional<BlockPos> closestBlock = mod.getBlockScanner().getNearestBlock(pos, check -> {

                if (mod.getBlockScanner().isUnreachable(check)) return false;
                return WorldHelper.canBreak(mod, check);
            }, blocks);

            return new Pair<>(
                    closestBlock.map(blockPos -> blockPos.getSquaredDistance(pos)).orElse(Double.POSITIVE_INFINITY),
                    closestBlock
            );
        }

        @Override
        protected Vec3d getOriginPos(AltoClef mod) {
            return mod.getPlayer().getPos();
        }

        @Override
        protected Task onTick(AltoClef mod) {
            currentMod = mod;
            if (miningPos != null && !_progressChecker.check(mod)) {
                Debug.logMessage("Failed to mine block. Suggesting it may be unreachable.");
                mod.getBlockTracker().requestBlockUnreachable(miningPos, 2);
                blacklist.add(miningPos);
                miningPos = null;
                _progressChecker.reset();
            }
            return super.onTick(mod);
        }

        @Override
        protected Task getGoalTask(Object obj) {
            if (obj instanceof BlockPos newPos) {
                if (miningPos == null || !miningPos.equals(newPos)) {
                    _progressChecker.reset();
                }
                miningPos = newPos;
                return new DestroyBlockTask(miningPos);
            }
            if (obj instanceof ItemEntity itemEntity) {
                miningPos = null;

                if (currentMod.getItemStorage().getSlotThatCanFitInPlayerInventory(itemEntity.getStack(), false).or(() -> StorageHelper.getGarbageSlot(currentMod)).isEmpty()) {
                    return new EnsureFreeInventorySlotTask();
                }

                return pickupTask;
            }
            throw new UnsupportedOperationException("Shouldn't try to get the goal from object " + obj + " of type " + (obj != null ? obj.getClass().toString() : "(null object)"));
        }

        @Override
        protected boolean isValid(AltoClef mod, Object obj) {
            if (obj instanceof BlockPos b) {
                return mod.getBlockTracker().blockIsValid(b, blocks) && WorldHelper.canBreak(mod, b);
            }
            if (obj instanceof ItemEntity drop) {
                Item item = drop.getStack().getItem();
                for (ItemTarget target : targets) {
                    if (target.matches(item)) return true;
                }
                return false;
            }
            return false;
        }

        @Override
        protected void onStart(AltoClef mod) {
            _progressChecker.reset();
            miningPos = null;
        }

        @Override
        protected void onStop(AltoClef mod, Task interruptTask) {

        }

        @Override
        protected boolean isEqual(Task other) {
            if (other instanceof MineOrCollectTask task) {
                return Arrays.equals(task.blocks, blocks) && Arrays.equals(task.targets, targets);
            }
            return false;
        }

        @Override
        protected String toDebugString() {
            return "Mining or Collecting";
        }

        public boolean isMining() {
            return miningPos != null;
        }

        public BlockPos miningPos() {
            return miningPos;
        }
    }

}