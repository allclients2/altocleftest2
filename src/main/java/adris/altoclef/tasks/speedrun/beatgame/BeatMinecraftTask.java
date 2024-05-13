package adris.altoclef.tasks.speedrun.beatgame;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.commands.SetGammaCommand;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.GetRidOfExtraWaterBucketTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.SafeNetherPortalTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.container.DoStuffInContainerTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.SmeltInSmokerTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasks.resources.*;
import adris.altoclef.tasks.speedrun.*;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.EntityTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static adris.altoclef.tasks.resources.CollectMeatTask.COOKABLE_FOODS;
import static net.minecraft.client.MinecraftClient.getInstance;


public class BeatMinecraftTask extends Task {
    private static final Block[] TRACK_BLOCKS = new Block[]{
            Blocks.FURNACE,
            Blocks.SMOKER,
            Blocks.END_PORTAL_FRAME,
            Blocks.END_PORTAL,

            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,

            Blocks.CRAFTING_TABLE, // For pearl trading + gold crafting
            Blocks.CHEST, // For ruined portals
            Blocks.SPAWNER, // For silverfish,
            Blocks.STONE_PRESSURE_PLATE // For desert temples
    };
    private static final Item[] COLLECT_EYE_ARMOR = new Item[]{
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS
    };

    private static final Item[] COLLECT_IRON_ARMOR = ItemHelper.IRON_ARMORS;
    private static final Item[] COLLECT_EYE_ARMOR_END = ItemHelper.DIAMOND_ARMORS;


    private static final ItemTarget[] COLLECT_EYE_GEAR_MIN = combine(
            toItemTargets(Items.DIAMOND_SWORD),
            toItemTargets(Items.DIAMOND_PICKAXE)
    );

    private static final int END_PORTAL_FRAME_COUNT = 12;
    private static final double END_PORTAL_BED_SPAWN_RANGE = 8;

    // We don't want curse of binding
    private static final Predicate<ItemStack> noCurseOfBinding = stack -> {
        for (NbtElement elm : stack.getEnchantments()) {
            NbtCompound comp = (NbtCompound) elm;
            if (comp.getString("id").equals("minecraft:binding_curse")) {
                return false;
            }
        }
        return true;
    };

    //dont even pick up these items
    private static final Item[] uselessItems = new Item[]{
            //sapling
            Items.OAK_SAPLING,
            Items.SPRUCE_SAPLING,
            Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING,
            Items.ACACIA_SAPLING,
            Items.DARK_OAK_SAPLING,
            Items.MANGROVE_PROPAGULE,
            Items.CHERRY_SAPLING,

            // seeds
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.WHEAT_SEEDS,
            Items.TORCHFLOWER_SEEDS,

            // random jung, might add more things in the future
            Items.FEATHER,
            Items.EGG,
            Items.PINK_PETALS,
            Items.BONE,
            Items.LEATHER,
            Items.RAW_COPPER,
            Items.WARPED_ROOTS,
            Items.GUNPOWDER,
            Items.MOSSY_COBBLESTONE,
            Items.SPRUCE_TRAPDOOR,
            Items.SANDSTONE_STAIRS,
            Items.STONE_BRICKS,
            Items.COARSE_DIRT,
            Items.SMOOTH_STONE,
            Items.FLOWER_POT,
            Items.MANGROVE_ROOTS,
            Items.POPPY,
            Items.MUDDY_MANGROVE_ROOTS,
            Items.SPIDER_EYE,
            Items.PINK_TULIP,

            Items.SPRUCE_STAIRS,
            Items.OAK_STAIRS,

            Items.LAPIS_LAZULI,
            Items.SUNFLOWER,
            Items.REDSTONE,
            Items.CRIMSON_ROOTS,
            Items.OAK_DOOR,

            Items.STRING,
            Items.WHITE_TERRACOTTA,
            Items.RED_TERRACOTTA,

            Items.MOSS_BLOCK,
            Items.MOSS_CARPET,
            Items.BOW,

            Items.EMERALD,
            Items.GOLD_NUGGET,
            Items.IRON_NUGGET,
            Items.SHORT_GRASS,
            Items.COBBLESTONE_WALL,
            Items.COBBLESTONE_STAIRS,
            Items.COBBLESTONE_SLAB,
            Items.CLAY_BALL,
            Items.DANDELION,
            Items.SUGAR_CANE,
            Items.CHEST,
            Items.RAIL,
            Items.CALCITE,
            Items.AMETHYST_BLOCK,
            Items.AMETHYST_CLUSTER,
            Items.AMETHYST_SHARD,
            Items.BUDDING_AMETHYST,
            Items.SMOOTH_BASALT,
            Items.AZURE_BLUET,

            Items.ACACIA_DOOR,
            Items.OAK_FENCE,
            Items.COMPOSTER,
            Items.OAK_PRESSURE_PLATE,
            Items.JUNGLE_DOOR,
            Items.CHISELED_SANDSTONE,
            Items.CACTUS,
            Items.MUD,
            Items.MANGROVE_LEAVES,
            Items.SMOOTH_SANDSTONE_SLAB,
            Items.SANDSTONE_WALL,
            Items.TNT,
            Items.PRISMARINE_CRYSTALS,
            Items.SNOWBALL,
            Items.DRIPSTONE_BLOCK,
            Items.POINTED_DRIPSTONE,
            Items.ARROW,
            Items.YELLOW_TERRACOTTA,
            Items.TUFF,
            Items.SPRUCE_STAIRS,
            Items.SPRUCE_DOOR,
            Items.SPRUCE_FENCE,
            Items.SPRUCE_FENCE_GATE,
            Items.ORANGE_TERRACOTTA,
            Items.HEART_OF_THE_SEA,
            Items.POTION,
            Items.FLOWERING_AZALEA,
            Items.COPPER_INGOT,
            Items.ACACIA_SLAB,

            // nether stuff
            Items.SOUL_SAND,
            Items.SOUL_SOIL,
            Items.NETHER_BRICK,
            Items.NETHER_BRICK_FENCE,
    };
    private static BeatMinecraftConfig config;
    private static GoToStrongholdPortalTask locateStrongholdTask;
    private static boolean openingEndPortal = false;

    static {
        ConfigHelper.loadConfig("configs/beat_minecraft.json", BeatMinecraftConfig::new, BeatMinecraftConfig.class, newConfig -> config = newConfig);
    }

    private final HashMap<Item, Integer> cachedEndItemDrops = new HashMap<>();
    // For some reason, after death there's a frame where the game thinks there are NO items in the end.
    private final TimerGame cachedEndItemNothingWaitTime = new TimerGame(10);
    private final Task buildMaterialsTask;
    private final PlaceBedAndSetSpawnTask setBedSpawnTask = new PlaceBedAndSetSpawnTask();
    private final Task getOneBedTask = TaskCatalogue.getItemTask("bed", 1);
    private final Task sleepThroughNightTask = new SleepThroughNightTask();
    private final Task killDragonBedStratsTask = new KillEnderDragonWithBedsTask(new WaitForDragonAndPearlTask());
    // End specific dragon breath avoidance
    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();
    private final TimerGame timer1 = new TimerGame(5);
    private final TimerGame timer2 = new TimerGame(35);
    private final TimerGame timer3 = new TimerGame(60);
    private final List<GatherResource> gatherResources = new LinkedList<>();
    private final TimerGame changedTaskTimer = new TimerGame(3);
    private final TimerGame forcedTaskTimer = new TimerGame(10);
    private final List<BlockPos> blacklistedChests = new LinkedList<>();
    private final TimerGame searchBiomeTask = new TimerGame(20);
    private final TimerGame waterPlacedTimer = new TimerGame(1.5);
    private final TimerGame fortressTimer = new TimerGame(20);
    private GatherResource lastGather = null;
    private Task lastTask = null;
    private boolean pickupFurnace = false;
    private boolean pickupSmoker = false;
    private boolean pickupCrafting = false;
    private Task rePickupTask = null;
    private Task searchTask = null;
    private boolean hasRods = false;
    private boolean gotToBiome = false;
    private GetRidOfExtraWaterBucketTask getRidOfExtraWaterBucketTask = null;
    private int repeated = 0;
    private boolean gettingPearls = false;
    private SafeNetherPortalTask safeNetherPortalTask;
    private boolean escaped = false;
    private boolean gotToFortress = false;
    private GetWithinRangeOfBlockTask cachedFortressTask = null;
    private boolean resetFortressTask = false;
    private BlockPos prevPos = null;
    private Task goToNetherTask = new DefaultGoToDimensionTask(Dimension.NETHER); // To keep the portal build cache.
    private boolean dragonIsDead = false;
    private BlockPos endPortalCenterLocation;
    private boolean ranStrongholdLocator;
    private boolean endPortalOpened;
    private BlockPos bedSpawnLocation;
    private int cachedFilledPortalFrames = 0;
    // Controls whether we CAN walk on the end portal.
    private boolean enterindEndPortal = false;
    private Task lootTask;
    private boolean collectingEyes;
    private boolean escapingDragonsBreath = false;
    private Task getBedTask;

    private record TaskChange(GatherResource original, GatherResource interrupt, BlockPos pos){
    }

    private List<TaskChange> taskChanges = new ArrayList<>();

    public BeatMinecraftTask(AltoClef mod) {
        locateStrongholdTask = new GoToStrongholdPortalTask(config.targetEyes);
        buildMaterialsTask = new GetBuildingMaterialsTask(config.buildMaterialCount);

        SetGammaCommand.changeGamma(20d);

        if (mod.getWorld().getDifficulty() != Difficulty.EASY) {
            mod.logWarning("Detected that the difficulty is other than easy!");
            if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                mod.logWarning("No mobs spawn on peaceful difficulty, so the bot will not be able to beat the game. Please change it!");
            } else {
                mod.logWarning("This could cause the bot to die sooner, please consider changing it...");
            }
        }

        gatherResources.add(new GatherResource(4, 10,
                getBlockCalculator(mod, ItemHelper.itemsToBlocks(ItemHelper.LOG), 3)
                , item -> mod.getItemStorage().hasItem(Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE) &&
                mod.getItemStorage().getItemCount(ItemHelper.LOG) < 5, Optional.of("log"), ItemHelper.LOG));

        addOreMiningTasks(mod);
        addCollectFoodTask(mod);

        // gear
        addStoneToolsTasks(mod);
        addPickaxeTasks(mod);
        addDiamondArmorTasks(mod);
        addLootChestsTasks(mod);
        addPickupImportantItemsTask(mod);

        gatherResources.add(new GatherResource(0, 1,
                getBlockCalculator(mod, new Block[]{Blocks.GRAVEL}, 50)
                , item -> mod.getItemStorage().hasItem(Items.STONE_SHOVEL) && !mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL), Items.FLINT));

        gatherResources.add(new GatherResource(getTargetBeds(mod), getTargetBeds(mod),
                getBlockCalculator(mod, ItemHelper.itemsToBlocks(ItemHelper.BED), 45)
                , item -> true, Optional.of("bed"), ItemHelper.BED));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (hasItem(mod, Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD) || mod.getItemStorage().hasItem(Items.SHIELD))
                return Double.NEGATIVE_INFINITY;

            return 200;
        }, item -> mod.getItemStorage().hasItem(Items.IRON_INGOT), Items.SHIELD).withBypassForceCooldown(true)
                .withNeedsCraftingOnStart((m) -> mod.getItemStorage().getItemCount(ItemHelper.PLANKS) + mod.getItemStorage().getItemCount(ItemHelper.LOG) * 4 >= 6));


        gatherResources.add(new GatherResource(2, 2, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().getItemCount(Items.BUCKET) >= 2)
                return Double.NEGATIVE_INFINITY;

            return 300;
        }, item -> mod.getItemStorage().getItemCount(Items.IRON_INGOT) >= 6, Items.BUCKET).withNeedsCraftingOnStart(m -> true));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL) || mod.getItemStorage().getItemCount(Items.FIRE_CHARGE) >= 2)
                return Double.NEGATIVE_INFINITY;

            return 100;
        }, item -> mod.getItemStorage().hasItem(Items.IRON_INGOT) && mod.getItemStorage().hasItem(Items.FLINT), Items.FLINT_AND_STEEL).withNeedsCraftingOnStart((m) -> true));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.DIAMOND_SWORD) || !mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE))
                return Double.NEGATIVE_INFINITY;

            return 330;
        }, item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 2, Items.DIAMOND_SWORD)
                .withNeedsCraftingOnStart(m -> mod.getItemStorage().hasItem(Items.STICK) || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.GOLDEN_HELMET)) return Double.NEGATIVE_INFINITY;

            return 400;
        }, item -> mod.getItemStorage().getItemCount(Items.GOLD_INGOT) >= 5, Items.GOLDEN_HELMET).withNeedsCraftingOnStart((m) -> true));

        gatherResources.add(new GatherResource(999, 999_999, (items, count, minCount, maxCount) -> {
            Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(ItemHelper.BED));
            if (pos.isPresent() && pos.get().isWithinDistance(mod.getPlayer().getPos(), 30)) return 1_000_000;

            return Double.NEGATIVE_INFINITY;
        }, item -> WorldHelper.canSleep(), Optional.of(new SleepThroughNightTask()), Items.BEDROCK).withDescription("sleeping in nearby bed"));

        GatherResource resource = new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.WATER_BUCKET) || hasItem(mod, Items.WATER_BUCKET))
                return Double.NEGATIVE_INFINITY;

            Optional<BlockPos> optionalPos = mod.getBlockScanner().getNearestBlock(Blocks.WATER);
            if (optionalPos.isEmpty()) return Double.NEGATIVE_INFINITY;

            double distance = Math.sqrt(optionalPos.get().getSquaredDistance(mod.getPlayer().getPos()));
            if (distance > 55) return Double.NEGATIVE_INFINITY;

            return 10 / distance * 77.3;
        }, item -> mod.getItemStorage().hasItem(Items.BUCKET), Items.WATER_BUCKET).withBypassForceCooldown(true);
        gatherResources.add(resource);

        addSmeltTasks(mod);
        addCookFoodTasks(mod);
    }

    private void addPickupImportantItemsTask(AltoClef mod) {
        List<Item> importantItems = List.of(Items.IRON_PICKAXE,Items.DIAMOND_PICKAXE,Items.GOLDEN_HELMET,Items.DIAMOND_SWORD,
                Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS, Items.FLINT_AND_STEEL);

        GatherResource pickupImportantItems = new GatherResource(99,99,null,item->true,Items.BEDROCK)
                .withDescription("Picking up important item...");

        pickupImportantItems.withPriorityCalculator((items, count, minCount, maxCount) -> {
            for (Item item : importantItems) {
                if (item == Items.IRON_PICKAXE && mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) continue;

                if (!mod.getItemStorage().hasItem(item) && mod.getEntityTracker().itemDropped(item)) {
                    pickupImportantItems.data = Optional.of(new PickupDroppedItemTask(item,1));
                    return 8_000;
                }
            }
            return 0;
        });

        gatherResources.add(pickupImportantItems);
    }

    /**
     * Returns the BeatMinecraftConfig instance.
     * If it is not already initialized, it initializes and returns a new instance.
     *
     * @return the BeatMinecraftConfig instance
     */
    public static BeatMinecraftConfig getConfig() {
        if (config == null) {
            Debug.logInternal("Initializing BeatMinecraftConfig");
            config = new BeatMinecraftConfig();
        }
        return config;
    }

    /**
     * Retrieves the frame blocks surrounding the end portal center.
     *
     * @param endPortalCenter the center position of the end portal
     * @return a list of block positions representing the frame blocks
     */
    private static List<BlockPos> getFrameBlocks(BlockPos endPortalCenter) {
        List<BlockPos> frameBlocks = new ArrayList<>();

        if (endPortalCenter != null) {
            int[][] frameOffsets = {
                    {2, 0, 1},
                    {2, 0, 0},
                    {2, 0, -1},
                    {-2, 0, 1},
                    {-2, 0, 0},
                    {-2, 0, -1},
                    {1, 0, 2},
                    {0, 0, 2},
                    {-1, 0, 2},
                    {1, 0, -2},
                    {0, 0, -2},
                    {-1, 0, -2}
            };

            for (int[] offset : frameOffsets) {
                BlockPos frameBlock = endPortalCenter.add(offset[0], offset[1], offset[2]);

                frameBlocks.add(frameBlock);
            }
        }

        Debug.logInternal("Frame blocks: " + frameBlocks);

        return frameBlocks;
    }

    /**
     * Converts an array of `Item` objects into an array of `ItemTarget` objects.
     *
     * @param items the array of `Item` objects to convert
     * @return the array of `ItemTarget` objects
     */
    public static ItemTarget[] toItemTargets(Item... items) {
        // Use the `Arrays.stream()` method to create a stream of `Item` objects
        return Arrays.stream(items)
                // Use the `map()` method to convert each `Item` object into an `ItemTarget` object
                .map(item -> {
                    // Add logging statement to print the item being converted
                    Debug.logInternal("Converting item: " + item);
                    return new ItemTarget(item);
                })
                // Use the `toArray()` method to convert the stream of `ItemTarget` objects into an array
                .toArray(ItemTarget[]::new);
    }


    /**
     * Combines multiple arrays of ItemTarget objects into a single array.
     *
     * @param targets The arrays of ItemTarget objects to combine.
     * @return The combined array of ItemTarget objects.
     */
    private static ItemTarget[] combine(ItemTarget[]... targets) {
        List<ItemTarget> combinedTargets = new ArrayList<>();

        for (ItemTarget[] targetArray : targets) {
            combinedTargets.addAll(Arrays.asList(targetArray));
        }

        Debug.logInternal("Combined Targets: " + combinedTargets);

        ItemTarget[] combinedArray = combinedTargets.toArray(new ItemTarget[0]);
        Debug.logInternal("Combined Array: " + Arrays.toString(combinedArray));

        return combinedArray;
    }

    /**
     * Checks if the End Portal Frame at the given position is filled with an Eye of Ender.
     *
     * @param mod The AltoClef mod instance.
     * @param pos The position of the End Portal Frame.
     * @return True if the End Portal Frame is filled, false otherwise.
     */
    private static boolean isEndPortalFrameFilled(AltoClef mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            Debug.logInternal("Chunk is not loaded");
            return false;
        }


        BlockState blockState = mod.getWorld().getBlockState(pos);
        if (blockState.getBlock() != Blocks.END_PORTAL_FRAME) {
            Debug.logInternal("Block is not an End Portal Frame");
            return false;
        }

        boolean isFilled = blockState.get(EndPortalFrameBlock.EYE);

        Debug.logInternal("End Portal Frame is " + (isFilled ? "filled" : "not filled"));
        return isFilled;
    }

    /**
     * Checks if a task is running eg. task is active and not finished.
     *
     * @param mod  The AltoClef mod.
     * @param task The task to check.
     * @return True if the task is running, false otherwise.
     */
    public static boolean isTaskRunning(AltoClef mod, Task task) {
        if (task == null) {
            Debug.logInternal("Task is null");
            return false;
        }

        boolean taskActive = task.isActive();
        boolean taskFinished = task.isFinished(mod);

        Debug.logInternal("Task is not null");
        Debug.logInternal("Task is " + (taskActive ? "active" : "not active"));
        Debug.logInternal("Task is " + (taskFinished ? "finished" : "not finished"));

        return taskActive && !taskFinished;
    }

    public static void throwAwayItems(AltoClef mod, Item... items) {
        throwAwaySlots(mod, mod.getItemStorage().getSlotsWithItemPlayerInventory(false, items));
    }

    public static void throwAwaySlots(AltoClef mod, List<Slot> slots) {
        for (Slot slot : slots) {
            if (Slot.isCursor(slot)) {
              /*  if (!mod.getControllerExtras().isBreakingBlock()) {
                    LookHelper.randomOrientation(mod);
                }*/
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
            }
        }
    }

    //TODO add some checks for how many food we already have, how hungry we are etc...
    private void addCookFoodTasks(AltoClef mod) {
        GatherResource cookFoodTask = new GatherResource(999, 999_999, null,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE),
                ItemHelper.COOKED_FOODS).withShouldForce(true).withCanCache(false);

        cookFoodTask.withPriorityCalculator((items, c, minCount, maxCount) -> {
            int rawFoodCount = mod.getItemStorage().getItemCount(ItemHelper.RAW_FOODS);
            int readyFoodCount = mod.getItemStorage().getItemCount(ItemHelper.COOKED_FOODS) + mod.getItemStorage().getItemCount(Items.BREAD);

            double priority = rawFoodCount >= 8 ? 450 : rawFoodCount * 25;

            if (lastTask instanceof SmeltInSmokerTask) {
                priority = Double.POSITIVE_INFINITY;
            }

            if (readyFoodCount > 5 && priority < Double.POSITIVE_INFINITY) {
                // always smelt the food at some point
                // smelt all at once to waste as little coal as possible
                priority = 0.01;
            }


            for (CollectMeatTask.CookableFoodTarget cookable : COOKABLE_FOODS) {
                int rawCount = mod.getItemStorage().getItemCount(cookable.getRaw());
                if (rawCount == 0) continue;

                int toSmelt = rawCount + mod.getItemStorage().getItemCount(cookable.getCooked());

                SmeltTarget target = new SmeltTarget(new ItemTarget(cookable.cookedFood, toSmelt), new ItemTarget(cookable.rawFood, rawCount));
                cookFoodTask.data = Optional.of(new SmeltInSmokerTask(target));

                return priority;
            }

            return Double.NEGATIVE_INFINITY;
        });
        gatherResources.add(cookFoodTask);
    }

    private void addSmeltTasks(AltoClef mod) {
        GatherResource smeltIronTask = new GatherResource(999, 999_999, null,
                item -> mod.getItemStorage().hasItem(Items.RAW_IRON), Items.IRON_INGOT).withShouldForce(true);

        smeltIronTask.withPriorityCalculator((items, c, minCount, maxCount) -> {

            int smeltedIronCount = getCountWithCraftedFromOre(mod, Items.RAW_IRON) - mod.getItemStorage().getItemCount(Items.RAW_IRON);
            if (smeltedIronCount + mod.getItemStorage().getItemCount(Items.RAW_IRON) < 3)
                return Double.NEGATIVE_INFINITY;

            //FIXME hardcoded value (11) for iron count
            if (getCountWithCraftedFromOre(mod, Items.RAW_IRON) - mod.getItemStorage().getItemCount(Items.RAW_IRON) >= 11) {
                return Double.NEGATIVE_INFINITY;
            }

            int count = mod.getItemStorage().getItemCount(Items.RAW_IRON);
            int includedCount = count + mod.getItemStorage().getItemCount(Items.IRON_INGOT);

            //exactly for one coal
            if (count >= 8) {
                includedCount = 8 + mod.getItemStorage().getItemCount(Items.IRON_INGOT);
                smeltIronTask.data = Optional.of(new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, includedCount), new ItemTarget(Items.RAW_IRON, includedCount))));
                return 450;
            }

            smeltIronTask.data = Optional.of(new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, includedCount), new ItemTarget(Items.RAW_IRON, includedCount))));

            //for getting iron pickaxe
            if (!mod.getItemStorage().hasItem(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE) && count >= 3) {
                return 460;
            }

            // if we have pickaxe, shield and one bucket dont smelt until we have 4 iron (3 for bucket, 1 for flint and steel)
            //FIXME hardcoded value (11; 4) + hardcoded items
            if (hasItem(mod, Items.SHIELD) && mod.getItemStorage().hasItem(Items.BUCKET, Items.WATER_BUCKET) && count < 4)
                return Double.NEGATIVE_INFINITY;

            // if we have all the items we need, do not smelt any more iron
            if (hasItem(mod, Items.SHIELD) && mod.getItemStorage().hasItem(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE)
                    && mod.getItemStorage().getItemCount(Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET) >= 2 && mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL))
                return Double.NEGATIVE_INFINITY;

            return count * 25;
        });
        gatherResources.add(smeltIronTask);

        //FIXME hardcoded number of gold ingots - 5
        GatherResource smeltGoldTask = new GatherResource(5, 5, (items, count, minCount, maxCount) -> 140, item -> mod.getItemStorage().getItemCount(Items.RAW_GOLD, Items.GOLD_INGOT) >= 5,
                Optional.of(new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, 5),
                        new ItemTarget(Items.RAW_GOLD, 5)))),
                Items.GOLD_INGOT).withShouldForce(true);

        gatherResources.add(smeltGoldTask);

    }

    private GatherResource.PriorityCalculator getBlockCalculator(AltoClef mod, Block[] blocks, float multiplier) {
        return (items, count, minCount, maxCount) -> {
            double closestDist = Integer.MAX_VALUE;

            Optional<ItemEntity> optionalDroppedItem = mod.getEntityTracker().getClosestItemDrop(items);

            if (optionalDroppedItem.isPresent()) {
                closestDist = optionalDroppedItem.get().distanceTo(mod.getPlayer());
            }

            for (Block block : blocks) {
                Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(block);
                if (pos.isPresent()) {
                    BlockPos bp = pos.get();
                    double distance = MathHelper.sqrt((float) bp.getSquaredDistance(mod.getPlayer().getPos()));
                    closestDist = Math.min(distance, closestDist);
                }
            }

            // no block was found
            if (closestDist == Integer.MAX_VALUE) {
                return Double.NEGATIVE_INFINITY;
            }

            if (count >= minCount) {
                if (closestDist < 5) {
                    return 10 / closestDist * 15 * multiplier;
                }
                return Double.NEGATIVE_INFINITY;
            }

            return 10 / closestDist * 35 * multiplier;
        };
    }

    private void addLootChestsTasks(AltoClef mod) {
        // TODO lower priority is player already has most of the items

        GatherResource resource = new GatherResource(999, 999999, null, (item) -> true, Items.BEDROCK)
                .withCanCache(false).withDescription("looting nearby chest");

        resource.withPriorityCalculator( (items, count, minCount, maxCount) -> {
            //the chest is open and being looted
            if (mod.getPlayer().currentScreenHandler instanceof GenericContainerScreenHandler && lastTask instanceof LootContainerTask && isTaskRunning(mod, lastTask)) {
                return Double.POSITIVE_INFINITY;
            }
            if (mod.getItemStorage().hasItemAll(Items.DIAMOND_PICKAXE, Items.DIAMOND_SWORD) && CollectFoodTask.calculateFoodPotential(mod) >= 20) {
                return Double.NEGATIVE_INFINITY;
            }

            Optional<BlockPos> chest = locateClosestUnopenedChest(mod);
            if (chest.isEmpty()) return Double.NEGATIVE_INFINITY;

            //just dont dig a lot of blocks for a chest
            if (Math.abs(chest.get().getY() - mod.getPlayer().getY()) > 30) {
                return Double.NEGATIVE_INFINITY;
            }

            lootTask = new LootContainerTask(chest.get(), lootableItems(mod), noCurseOfBinding);
            resource.data = Optional.of(lootTask);
            //mod.log(chest.get() + "");

            double dst = Math.sqrt(chest.get().getSquaredDistance(mod.getPlayer().getPos()));
            return 30 / dst * 175;
        });

        gatherResources.add(resource);

    }

    private void addCollectFoodTask(AltoClef mod) {
        List<Item> food = new LinkedList<>(ItemHelper.cookableFoodMap.values());
        food.addAll(ItemHelper.cookableFoodMap.keySet());
        food.addAll(List.of(Items.WHEAT, Items.BREAD));

        gatherResources.add(new GatherResource(999, 999_999, new CollectFoodPriorityCalculator(mod, config.foodUnits),
                item -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE) && mod.getItemStorage().hasItem(Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD),
                Optional.of(new CollectFoodTask(9999)), food.toArray(new Item[0])));


        //TODO do some more sophisticated calculation including food potential etc
        //craft bread
        GatherResource craftWheatTask = new GatherResource(999, 999_999, null,
                item -> mod.getItemStorage().hasItem(Items.HAY_BLOCK), Items.BREAD);

        craftWheatTask.withPriorityCalculator( (items, count, minCount, maxCount) -> {
            craftWheatTask.data = Optional.ofNullable(TaskCatalogue.getItemTask(Items.WHEAT, mod.getItemStorage().getItemCount(Items.HAY_BLOCK) * 9 + mod.getItemStorage().getItemCount(Items.WHEAT)));

            if (StorageHelper.calculateInventoryFoodScore(mod) < 5) return 270;
            return 10;
        });

        gatherResources.add(craftWheatTask);

        GatherResource craftBreadTask = new GatherResource(999, 999_999, null,
                item -> mod.getItemStorage().getItemCount(Items.WHEAT) >= 3, Items.BREAD);
        craftBreadTask.withPriorityCalculator( (items, count, minCount, maxCount) -> {
            craftBreadTask.data = Optional.ofNullable(TaskCatalogue.getItemTask("bread", mod.getItemStorage().getItemCount(Items.WHEAT) / 3 + mod.getItemStorage().getItemCount(Items.BREAD)));

            if (StorageHelper.calculateInventoryFoodScore(mod) < 5) return 250;
            return 5;
        });

        gatherResources.add(craftBreadTask);
    }

    private void addOreMiningTasks(AltoClef mod) {
        GatherResource.PriorityCalculator orePriorityCalculator = (items, count, minCount, maxCount) -> {
            if (items.length != 1) {
                throw new IllegalStateException("Ore calculator cannot have anything else than one item!");
            }
            Item item = items[0];
            Block[] blocks = mapOreItemToBlocks(item);

            double closestDist = Integer.MAX_VALUE;

            Optional<ItemEntity> optionalDroppedItem = mod.getEntityTracker().getClosestItemDrop(item);
            if (optionalDroppedItem.isPresent()) {
                closestDist = optionalDroppedItem.get().distanceTo(mod.getPlayer());
            }

            for (Block block : blocks) {
                Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(block);
                if (pos.isPresent()) {
                    BlockPos bp = pos.get();
                    double distance = MathHelper.sqrt((float) bp.getSquaredDistance(mod.getPlayer().getPos()));
                    closestDist = Math.min(distance, closestDist);
                }
            }

            // no ore was found
            if (closestDist == Integer.MAX_VALUE) {
                return Double.NEGATIVE_INFINITY;
            }

            if (count >= minCount) {
                if (closestDist < 5) {
                    return 10 / closestDist * 25;
                }
                return Double.NEGATIVE_INFINITY;
            }

            return 30 / closestDist * 35;
        };

        //arbitrary amount, might change later
        gatherResources.add(new GatherResource(4, 7, orePriorityCalculator,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE), Items.COAL));

        //min: iron pickaxe, shield, two buckets, flint and steel
        // max: -
        gatherResources.add(new GatherResource(11, 11, orePriorityCalculator,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE), Items.RAW_IRON));


        //min: golden helmet
        //max: -
        gatherResources.add(new GatherResource(5, 5, orePriorityCalculator,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.IRON), Items.RAW_GOLD));

        //min: full dia (no helmet), 2*pickaxe, sword
        //max: extra pickaxe
        gatherResources.add(new GatherResource(27, 30, orePriorityCalculator,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.IRON), Items.DIAMOND));
    }

    /**
     * Adds stone tools not including pickaxe
     */
    private void addStoneToolsTasks(AltoClef mod) {
        // all at once
        GatherResource gatherAllResource = new GatherResource(1, 9999,
                null,
                (item) -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE),
                Optional.of(TaskCatalogue.getSquashedItemTask(toItemTargets(Items.STONE_AXE, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_HOE))),
                Items.STONE_AXE, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_HOE).withShouldForce(true);

        gatherAllResource.withPriorityCalculator( (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.STONE_AXE, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_HOE)) {
                gatherAllResource.maxCountSatisfied = true;
                gatherAllResource.minCountSatisfied = true;
                return Double.NEGATIVE_INFINITY;
            }

            return 520;
        });

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.DIAMOND_SWORD,Items.IRON_SWORD)) return Double.NEGATIVE_INFINITY;

            return 300;
        }, item -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE),Items.STONE_SWORD));


        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.DIAMOND_AXE,Items.IRON_AXE)) return Double.NEGATIVE_INFINITY;

            return 300;
        }, item -> StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE),Items.STONE_AXE));

        gatherResources.add(gatherAllResource);
    }

    /**
     * adds tasks for CRAFTING diamond armor from already obtained diamonds.
     * the helmet isnt crafted because we have a golden one
     */

    private void addDiamondArmorTasks(AltoClef mod) {
        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (hasItem(mod, Items.DIAMOND_CHESTPLATE)) return Double.NEGATIVE_INFINITY;

            return 350;
        }, item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 8, Items.DIAMOND_CHESTPLATE)
                .withBypassForceCooldown(true).withNeedsCraftingOnStart((a) -> true));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (hasItem(mod, Items.DIAMOND_LEGGINGS)) return Double.NEGATIVE_INFINITY;

            return 300;
        }, item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 7, Items.DIAMOND_LEGGINGS)
                .withBypassForceCooldown(true).withNeedsCraftingOnStart((a) -> true));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (hasItem(mod, Items.DIAMOND_BOOTS)) return Double.NEGATIVE_INFINITY;

            return 220;
        }, item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 4, Items.DIAMOND_BOOTS)
                .withBypassForceCooldown(true).withNeedsCraftingOnStart((a) -> true));
    }

    private void addPickaxeTasks(AltoClef mod) {
        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE))
                return Double.NEGATIVE_INFINITY;

            return 400;
        }, item -> true, Items.WOODEN_PICKAXE)
                .withNeedsCraftingOnStart(m -> mod.getItemStorage().getItemCount(Items.STICK) >= 2 || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2));

        GatherResource stonePickaxe =new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE))
                return Double.NEGATIVE_INFINITY;

            return 410;
        }, item -> StorageHelper.miningRequirementMet(mod, MiningRequirement.WOOD), Items.STONE_PICKAXE)
                .withNeedsCraftingOnStart(m -> mod.getItemStorage().getItemCount(Items.STICK) >= 2 || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2);
        gatherResources.add(stonePickaxe);

        gatherResources.add(new GatherResource(999, 999, (items, count, minCount, maxCount) -> {
            List<Slot> list = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, items);
            boolean hasSafeIronPick = false;
            for (Slot slot : list) {
                if (slot.getInventorySlot() == -1) continue;
                ItemStack stack = mod.getPlayer().getInventory().getStack(slot.getInventorySlot());
                if (!StorageHelper.shouldSaveStack(mod, Blocks.STONE, stack) && stack.getItem().equals(Items.IRON_PICKAXE)) {
                    hasSafeIronPick = true;
                    break;
                }
            }

            if (mod.getItemStorage().hasItem(Items.STONE_PICKAXE) || hasSafeIronPick || mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE))
                return Double.NEGATIVE_INFINITY;

            if (stonePickaxe.minCountSatisfied || stonePickaxe.maxCountSatisfied) return 10_000;

            return 300;
        }, item -> StorageHelper.miningRequirementMet(mod, MiningRequirement.WOOD),
                Optional.ofNullable(TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1)), Items.STONE_PICKAXE));

        gatherResources.add(new GatherResource(1, 1, (items, count, minCount, maxCount) -> {
            if (mod.getItemStorage().hasItem(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE))
                return Double.NEGATIVE_INFINITY;

            return 420;
        }, item -> mod.getItemStorage().getItemCount(Items.IRON_INGOT) >= 3, Items.IRON_PICKAXE).withBypassForceCooldown(true)
                .withNeedsCraftingOnStart(m -> mod.getItemStorage().getItemCount(Items.STICK) >= 2 || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2));

        GatherResource diamondPickResource = new GatherResource(1, 1, (a, b, c, d) -> 430,
                item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 3, Items.DIAMOND_PICKAXE);
        diamondPickResource.withBypassForceCooldown(true);
        diamondPickResource.withNeedsCraftingOnStart(m -> mod.getItemStorage().getItemCount(Items.STICK) >= 2 || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2);

        GatherResource secondDiamondPickResource = new GatherResource(1, 1, (a, b, c, d) -> 100,
                item -> mod.getItemStorage().getItemCount(Items.DIAMOND) >= 3, Items.DIAMOND_PICKAXE).withBypassForceCooldown(true);
        secondDiamondPickResource.withNeedsCraftingOnStart(m -> mod.getItemStorage().getItemCount(Items.STICK) >= 2 || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 2);

        gatherResources.add(diamondPickResource);
        gatherResources.add(secondDiamondPickResource);
    }

    private static boolean hasItem(AltoClef mod, Item item) {
        ClientPlayerEntity player = mod.getPlayer();
        PlayerInventory inv = player.getInventory();
        List<DefaultedList<ItemStack>> combinedInventory = List.of(inv.main, inv.armor, inv.offHand);

        for (List<ItemStack> list : combinedInventory) {
            for (ItemStack itemStack : list) {
                if (itemStack.getItem().equals(item)) return true;
            }
        }

        return false;
    }

    private Block[] mapOreItemToBlocks(Item item) {
        if (item.equals(Items.RAW_IRON)) {
            return new Block[]{Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_ORE};
        } else if (item.equals(Items.RAW_GOLD)) {
            return new Block[]{Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_ORE};
        } else if (item.equals(Items.DIAMOND)) {
            return new Block[]{Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_ORE};
        } else if (item.equals(Items.COAL)) {
            return new Block[]{Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_ORE};
        }

        throw new IllegalStateException("Invalid ore: " + item);
    }

    /**
     * Checks if the task is finished.
     *
     * @param mod The instance of the AltoClef mod.
     * @return True if the task is finished, false otherwise.
     */
    @Override
    public boolean isFinished(AltoClef mod) {
        if (getInstance().currentScreen instanceof CreditsScreen) {
            Debug.logInternal("isFinished - Current screen is CreditsScreen");
            return true;
        }

        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD && dragonIsDead) {
            Debug.logInternal("isFinished - Dragon is dead in the Overworld");
            return true;
        }

        Debug.logInternal("isFinished - Returning false");
        return false;
    }

    /**
     * Checks if the mod needs building materials.
     *
     * @param mod The AltoClef mod instance.
     * @return True if building materials are needed, false otherwise.
     */
    private boolean needsBuildingMaterials(AltoClef mod) {
        int materialCount = StorageHelper.getBuildingMaterialCount(mod);
        boolean shouldForce = isTaskRunning(mod, buildMaterialsTask);

        // Check if the material count is below the minimum required count
        // or if the build materials task should be forced.
        if (materialCount < config.minBuildMaterialCount || shouldForce) {
            Debug.logInternal("Building materials needed: " + materialCount);
            Debug.logInternal("Force build materials: " + shouldForce);
            return true;
        } else {
            Debug.logInternal("Building materials not needed");
            return false;
        }
    }

    /**
     * Updates the cached end items based on the dropped items in the entity tracker.
     *
     * @param mod The AltoClef mod instance.
     */
    private void updateCachedEndItems(AltoClef mod) {
        List<ItemEntity> droppedItems = mod.getEntityTracker().getDroppedItems();

        // If there are no dropped items and the cache wait time has not elapsed, return.
        if (droppedItems.isEmpty() && !cachedEndItemNothingWaitTime.elapsed()) {
            Debug.logInternal("No dropped items and cache wait time not elapsed.");
            return;
        }

        // Reset the cache wait time and clear the cached end item drops.
        cachedEndItemNothingWaitTime.reset();
        cachedEndItemDrops.clear();

        for (ItemEntity entity : droppedItems) {
            Item item = entity.getStack().getItem();
            int count = entity.getStack().getCount();

            cachedEndItemDrops.put(item, cachedEndItemDrops.getOrDefault(item, 0) + count);

            Debug.logInternal("Added dropped item: " + item + " with count: " + count);
        }
    }

    /**
     * Retrieves a list of lootable items based on certain conditions.
     *
     * @param mod The AltoClef mod instance.
     * @return The list of lootable items.
     */
    private List<Item> lootableItems(AltoClef mod) {
        List<Item> lootable = new ArrayList<>();

        // Add initial lootable items
        lootable.add(Items.APPLE);
        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.GOLDEN_CARROT);
        lootable.add(Items.OBSIDIAN);
        lootable.add(Items.STICK);
        lootable.add(Items.COAL);
        lootable.addAll(Arrays.stream(ItemHelper.LOG).toList());

        lootable.add(Items.BREAD);

        // Check if golden helmet is equipped or available in inventory
        boolean isGoldenHelmetEquipped = StorageHelper.isArmorEquipped(mod, Items.GOLDEN_HELMET);
        boolean hasGoldenHelmet = mod.getItemStorage().hasItemInventoryOnly(Items.GOLDEN_HELMET);

        if (!mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
            lootable.add(Items.IRON_PICKAXE);
        }
        if (mod.getItemStorage().getItemCount(Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET) < 2) {
            lootable.add(Items.BUCKET);
        }

        // Check if there are enough gold ingots
        boolean hasEnoughGoldIngots = mod.getItemStorage().getItemCountInventoryOnly(Items.GOLD_INGOT) >= 5;

        // Add golden helmet if not equipped or available in inventory
        if (!isGoldenHelmetEquipped && !hasGoldenHelmet) {
            lootable.add(Items.GOLDEN_HELMET);
        }


        if ((!hasEnoughGoldIngots && !isGoldenHelmetEquipped && !hasGoldenHelmet) || config.barterPearlsInsteadOfEndermanHunt) {
            lootable.add(Items.GOLD_INGOT);
        }

        // Add flint and steel and fire charge if not available in inventory
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.FLINT_AND_STEEL)) {
            lootable.add(Items.FLINT_AND_STEEL);
            if (!mod.getItemStorage().hasItemInventoryOnly(Items.FIRE_CHARGE)) {
                lootable.add(Items.FIRE_CHARGE);
            }
        }

        // Add iron ingot if neither bucket nor water bucket is available in inventory
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.BUCKET) && !mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            lootable.add(Items.IRON_INGOT);
        }

        // Add diamond if item targets for eye gear are not met in inventory
        if (!StorageHelper.itemTargetsMetInventory(mod, COLLECT_EYE_GEAR_MIN)) {
            lootable.add(Items.DIAMOND);
        }

        // Add flint if not available in inventory
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.FLINT)) {
            lootable.add(Items.FLINT);
        }

        Debug.logInternal("Lootable items: " + lootable); // Logging statement

        return lootable;
    }

    /**
     * Overrides the onStop method.
     * Performs necessary cleanup and logging when the task is interrupted or stopped.
     *
     * @param mod           The AltoClef mod instance.
     * @param interruptTask The task that interrupted the current task.
     */
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getExtraBaritoneSettings().canWalkOnEndPortal(false);

        mod.getBehaviour().pop();

        Debug.logInternal("Stopped onStop method");
        Debug.logInternal("canWalkOnEndPortal set to false");
        Debug.logInternal("Behaviour popped");
        Debug.logInternal("Stopped tracking BED blocks");
        Debug.logInternal("Stopped tracking TRACK_BLOCKS");
    }

    /**
     * Check if the given task is equal to this BeatMinecraftTask.
     *
     * @param other The task to compare.
     * @return True if the tasks are equal, false otherwise.
     */
    @Override
    protected boolean isEqual(Task other) {
        boolean isSameTask = other instanceof BeatMinecraftTask;

        if (!isSameTask)
            Debug.logInternal("The 'other' task is not of type BeatMinecraftTask");

        return isSameTask;
    }

    /**
     * Returns a debug string for the object.
     *
     * @return The debug string.
     */
    @Override
    protected String toDebugString() {
        return "Beating the game (Miran version).";
    }

    /**
     * Checks if the end portal has been found.
     *
     * @param mod             The instance of the AltoClef mod.
     * @param endPortalCenter The center position of the end portal.
     * @return True if the end portal has been found, false otherwise.
     */
    private boolean endPortalFound(AltoClef mod, BlockPos endPortalCenter) {
        if (endPortalCenter == null) {
            Debug.logInternal("End portal center is null");
            return false;
        }
        return true;

     /*   if (endPortalOpened(mod, endPortalCenter)) {
            Debug.logInternal("End portal is already opened");
            return true;
          }

        List<BlockPos> frameBlocks = getFrameBlocks(endPortalCenter);

        for (BlockPos frame : frameBlocks) {
            // Check if the frame block is a valid end portal frame
            if (mod.getBlockTracker().blockIsValid(frame, Blocks.END_PORTAL_FRAME)) {
                Debug.logInternal("Found valid end portal frame at " + frame.toString());
                return true;
            }
        }

        Debug.logInternal("No valid end portal frame found");
        return false;*/
    }

    /**
     * Checks if the end portal is opened.
     *
     * @param mod             The AltoClef mod instance.
     * @param endPortalCenter The center position of the end portal.
     * @return True if the end portal is opened, false otherwise.
     */
    private boolean endPortalOpened(AltoClef mod, BlockPos endPortalCenter) {
        if (endPortalOpened && endPortalCenter != null) {
            BlockScanner blockTracker = mod.getBlockScanner();

            if (blockTracker != null) {
                boolean isValid = blockTracker.isBlockAtPosition(endPortalCenter, Blocks.END_PORTAL);

                Debug.logInternal("End Portal is " + (isValid ? "valid" : "invalid"));
                return isValid;
            }
        }

        Debug.logInternal("End Portal is not opened yet");
        return false;
    }

    /**
     * Checks if the bed spawn location is near the given end portal center.
     *
     * @param mod             The AltoClef mod instance.
     * @param endPortalCenter The center position of the end portal.
     * @return True if the bed spawn location is near the end portal, false otherwise.
     */
    private boolean spawnSetNearPortal(AltoClef mod, BlockPos endPortalCenter) {
        if (bedSpawnLocation == null) {
            Debug.logInternal("Bed spawn location is null");
            return false;
        }

        BlockScanner blockTracker = mod.getBlockScanner();
        boolean isValid = blockTracker.isBlockAtPosition(bedSpawnLocation, ItemHelper.itemsToBlocks(ItemHelper.BED));

        Debug.logInternal("Spawn set near portal: " + isValid);

        return isValid;
    }

    /**
     * Finds the closest unopened chest.
     *
     * @param mod The AltoClef mod instance.
     * @return An Optional containing the closest BlockPos of the unopened chest, or empty if not found.
     */
    private Optional<BlockPos> locateClosestUnopenedChest(AltoClef mod) {
        if (!WorldHelper.getCurrentDimension().equals(Dimension.OVERWORLD)) {
            return Optional.empty();
        }

        // Find the nearest tracking block position
        return mod.getBlockScanner().getNearestBlock(blockPos -> {
            if (blacklistedChests.contains(blockPos)) return false;

            boolean isUnopenedChest = WorldHelper.isUnopenedChest(mod, blockPos);
            boolean isWithinDistance = mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 150);
            boolean isLootableChest = canBeLootablePortalChest(mod, blockPos);

            // TODO make more sophisticated
            //dont open spawner chests
            Optional<BlockPos> nearestSpawner = mod.getBlockScanner().getNearestBlock(WorldHelper.toVec3d(blockPos), Blocks.SPAWNER);
            if (nearestSpawner.isPresent() && nearestSpawner.get().isWithinDistance(blockPos, 6)) {
                blacklistedChests.add(blockPos);
                return false;
            }

            // TODO use shipwreck finder instead

            Stream<BlockState> states = mod.getWorld().getStatesInBox(new Box(blockPos.getX() - 5, blockPos.getY() - 5, blockPos.getZ() - 5,
                    blockPos.getX() + 5, blockPos.getY() + 5, blockPos.getZ() + 5));

            if (states.anyMatch((state) -> state.getBlock().equals(Blocks.WATER))) {
                blacklistedChests.add(blockPos);
                return false;
            }

            Debug.logInternal("isUnopenedChest: " + isUnopenedChest);
            Debug.logInternal("isWithinDistance: " + isWithinDistance);
            Debug.logInternal("isLootableChest: " + isLootableChest);

            return isUnopenedChest && isWithinDistance && isLootableChest;
        }, Blocks.CHEST);
    }

    /**
     * This method is called when the mod starts.
     * It performs several tasks to set up the mod.
     */
    @Override
    protected void onStart(AltoClef mod) {
        resetTimers();
        pushBehaviour(mod);
        addThrowawayItemsWarning(mod);
        trackBlocks(mod);
        addProtectedItems(mod);
        allowWalkingOnEndPortal(mod);
        avoidDragonBreath(mod);
        avoidBreakingBed(mod);

        mod.getBehaviour().avoidBlockBreaking((pos) -> mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.NETHER_PORTAL));
    }

    /**
     * Resets the timers.
     */
    private void resetTimers() {
        timer1.reset();
        timer2.reset();
        timer3.reset();
    }

    /**
     * Pushes the current behaviour onto the behaviour stack.
     * Logs the process for internal debugging.
     *
     * @param mod The AltoClef instance.
     */
    private void pushBehaviour(AltoClef mod) {
        Debug.logInternal("Pushing behaviour...");

        mod.getBehaviour().push();

        Debug.logInternal("Behaviour pushed successfully.");
    }

    /**
     * Adds a warning message if certain conditions are not met.
     *
     * @param mod The AltoClef mod instance.
     */
    private void addThrowawayItemsWarning(AltoClef mod) {
        // Warning message tail that will be appended to the warning message.
        String settingsWarningTail = "in \".minecraft/altoclef_settings.json\". @gamer may break if you don't add this! (sorry!)";

        // Check if "end_stone" is not part of the "throwawayItems" list and log a warning.
        if (!ArrayUtils.contains(mod.getModSettings().getThrowawayItems(mod), Items.END_STONE)) {
            Debug.logWarning("\"end_stone\" is not part of your \"throwawayItems\" list " + settingsWarningTail);
        }

        // Check if "throwawayUnusedItems" is not set to true and log a warning.
        if (!mod.getModSettings().shouldThrowawayUnusedItems()) {
            Debug.logWarning("\"throwawayUnusedItems\" is not set to true " + settingsWarningTail);
        }
    }

    /**
     * Tracks specific blocks using the BlockTracker.
     *
     * @param mod The AltoClef mod instance.
     */
    private void trackBlocks(AltoClef mod) {
        BlockScanner blockTracker = mod.getBlockScanner();

        // Add logging statements
        Debug.logInternal("Tracking blocks...");
        Debug.logInternal("BlockTracker: " + blockTracker);
        Debug.logInternal("Bed block: " + Arrays.toString(ItemHelper.itemsToBlocks(ItemHelper.BED)));
        Debug.logInternal("TRACK_BLOCKS: " + Arrays.toString(TRACK_BLOCKS));
    }

    /**
     * Adds protected items to the behaviour of the given AltoClef instance.
     *
     * @param mod The AltoClef instance.
     */
    private void addProtectedItems(AltoClef mod) {
        mod.getBehaviour().addProtectedItems(Items.ENDER_EYE, Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.ENDER_PEARL, Items.CRAFTING_TABLE, Items.IRON_INGOT, Items.WATER_BUCKET, Items.FLINT_AND_STEEL, Items.SHIELD, Items.SHEARS, Items.BUCKET, Items.GOLDEN_HELMET, Items.SMOKER, Items.FURNACE);

        // Add protected items using helper classes
        mod.getBehaviour().addProtectedItems(ItemHelper.BED);
        mod.getBehaviour().addProtectedItems(ItemHelper.IRON_ARMORS);
        mod.getBehaviour().addProtectedItems(ItemHelper.LOG);

        Debug.logInternal("Protected items added successfully.");
    }

    /**
     * Allows the player to walk on an end portal block.
     *
     * @param mod The AltoClef mod instance.
     */
    private void allowWalkingOnEndPortal(AltoClef mod) {
        mod.getBehaviour().allowWalkingOn(blockPos -> {
            if (enterindEndPortal && (mod.getChunkTracker().isChunkLoaded(blockPos))) {
                BlockState blockState = mod.getWorld().getBlockState(blockPos);
                boolean isEndPortal = blockState.getBlock() == Blocks.END_PORTAL;
                if (isEndPortal) {
                    Debug.logInternal("Walking on End Portal at " + blockPos.toString());
                }
                return isEndPortal;

            }
            return false;
        });
    }

    /**
     * Avoids walking through dragon breath in the End dimension.
     *
     * @param mod The AltoClef mod instance.
     */
    private void avoidDragonBreath(AltoClef mod) {
        mod.getBehaviour().avoidWalkingThrough(blockPos -> {
            Dimension currentDimension = WorldHelper.getCurrentDimension();
            boolean isEndDimension = currentDimension == Dimension.END;
            boolean isTouchingDragonBreath = dragonBreathTracker.isTouchingDragonBreath(blockPos);

            if (isEndDimension && !escapingDragonsBreath && isTouchingDragonBreath) {
                Debug.logInternal("Avoiding dragon breath at blockPos: " + blockPos);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Avoid breaking the bed by adding a behavior to avoid breaking specific block positions.
     *
     * @param mod The AltoClef mod instance.
     */
    private void avoidBreakingBed(AltoClef mod) {
        mod.getBehaviour().avoidBlockBreaking(blockPos -> {
            if (bedSpawnLocation != null) {
                // Get the head and foot positions of the bed
                BlockPos bedHead = WorldHelper.getBedHead(mod, bedSpawnLocation);
                BlockPos bedFoot = WorldHelper.getBedFoot(mod, bedSpawnLocation);

                boolean shouldAvoidBreaking = blockPos.equals(bedHead) || blockPos.equals(bedFoot);

                if (shouldAvoidBreaking) {
                    Debug.logInternal("Avoiding breaking bed at block position: " + blockPos);
                }

                return shouldAvoidBreaking;
            }

            return false;
        });
    }

    private void blackListDangerousBlock(AltoClef mod, Block block) {
        Optional<BlockPos> nearestTracking = mod.getBlockScanner().getNearestBlock(block);

        if (nearestTracking.isPresent()) {
            Iterable<Entity> entities = mod.getWorld().getEntities();
            for (Entity entity : entities) {

                if (mod.getBlockScanner().isUnreachable(nearestTracking.get()) || !(entity instanceof HostileEntity))
                    continue;

                if (mod.getPlayer().squaredDistanceTo(entity.getPos()) < 150 && nearestTracking.get().isWithinDistance(entity.getPos(), 30)) {

                    Debug.logMessage("Blacklisting dangerous " + block.toString());
                    mod.getBlockScanner().requestBlockUnreachable(nearestTracking.get(), 0);
                }
            }
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getPlayer().getMainHandStack().getItem() instanceof EnderEyeItem && !openingEndPortal) {
            List<ItemStack> itemStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);
            for (ItemStack itemStack : itemStacks) {
                Item item = itemStack.getItem();
                if (item instanceof SwordItem) {
                    mod.getSlotHandler().forceEquipItem(item);
                }
            }
        }


        boolean shouldSwap = false;
        boolean hasInHotbar = false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mod.getPlayer().getInventory().getStack(i);

            //FIXME do some more general approach
            if (stack.getItem().equals(Items.IRON_PICKAXE) && StorageHelper.shouldSaveStack(mod, Blocks.STONE, stack)) {
                shouldSwap = true;
            }
            if (stack.getItem().equals(Items.STONE_PICKAXE)) {
                hasInHotbar = true;
            }
        }

        if (shouldSwap && !hasInHotbar) {
            if (mod.getItemStorage().hasItem(Items.STONE_PICKAXE)) {
                mod.getSlotHandler().forceEquipItem(Items.STONE_PICKAXE);
            }
        }


        boolean eyeGearSatisfied = StorageHelper.isArmorEquippedAll(mod, COLLECT_EYE_ARMOR);
        boolean ironGearSatisfied = StorageHelper.isArmorEquippedAll(mod, COLLECT_IRON_ARMOR);

        if (mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) {
            mod.getBehaviour().setBlockBreakAdditionalPenalty(1.2);
        } else {
            mod.getBehaviour().setBlockBreakAdditionalPenalty(mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.defaultValue);
        }
        Predicate<Task> isCraftingTableTask = task -> {
            if (task instanceof DoStuffInContainerTask cont) {
                return cont.getContainerTarget().matches(Items.CRAFTING_TABLE);
            }
            return false;
        };
        List<BlockPos> craftingTables = mod.getBlockScanner().getKnownLocations(Blocks.CRAFTING_TABLE);
        for (BlockPos craftingTable : craftingTables) {
            if (mod.getItemStorage().hasItem(Items.CRAFTING_TABLE) && !thisOrChildSatisfies(isCraftingTableTask) && (!mod.getBlockScanner().isUnreachable(craftingTable))) {
                Debug.logMessage("Blacklisting extra crafting table.");
                mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);

            }
            if (!mod.getBlockScanner().isUnreachable(craftingTable)) {
                BlockState craftingTablePosUp = mod.getWorld().getBlockState(craftingTable.up(2));
                if (mod.getEntityTracker().entityFound(WitchEntity.class)) {
                    Optional<Entity> witch = mod.getEntityTracker().getClosestEntity(WitchEntity.class);
                    if (witch.isPresent() && (craftingTable.isWithinDistance(witch.get().getPos(), 15))) {
                        Debug.logMessage("Blacklisting witch crafting table.");
                        mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);

                    }
                }
                if (craftingTablePosUp.getBlock() == Blocks.WHITE_WOOL) {
                    Debug.logMessage("Blacklisting pillage crafting table.");
                    mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);
                }
            }
        }
        List<BlockPos> smokers = mod.getBlockScanner().getKnownLocations(Blocks.SMOKER);

        for (BlockPos smoker : smokers) {
            if (mod.getItemStorage().hasItem(Items.SMOKER) && !mod.getBlockScanner().isUnreachable(smoker)) {
                Debug.logMessage("Blacklisting extra smoker.");
                mod.getBlockScanner().requestBlockUnreachable(smoker, 0);
            }
        }

        List<BlockPos> furnaces = mod.getBlockScanner().getKnownLocations(Blocks.FURNACE);

        for (BlockPos furnace : furnaces) {
            if (mod.getItemStorage().hasItem(Items.FURNACE) && !goToNetherTask.isActive() && !ranStrongholdLocator && !mod.getBlockScanner().isUnreachable(furnace)) {
                Debug.logMessage("Blacklisting extra furnace.");
                mod.getBlockScanner().requestBlockUnreachable(furnace, 0);
            }
        }

        List<BlockPos> logs = mod.getBlockScanner().getKnownLocations(ItemHelper.itemsToBlocks(ItemHelper.LOG));

        for (BlockPos log : logs) {
            Iterable<Entity> entities = mod.getWorld().getEntities();
            for (Entity entity : entities) {
                if (entity instanceof PillagerEntity && !mod.getBlockScanner().isUnreachable(log) && log.isWithinDistance(entity.getPos(), 40)) {
                    Debug.logMessage("Blacklisting pillage log.");
                    mod.getBlockScanner().requestBlockUnreachable(log, 0);
                }
            }
            if (log.getY() < 62 && !mod.getBlockScanner().isUnreachable(log) && !ironGearSatisfied && !eyeGearSatisfied) {
                Debug.logMessage("Blacklisting dangerous log.");
                mod.getBlockScanner().requestBlockUnreachable(log, 0);
            }
        }


        if (!ironGearSatisfied && !eyeGearSatisfied) {
            blackListDangerousBlock(mod, Blocks.DEEPSLATE_COAL_ORE);
            blackListDangerousBlock(mod, Blocks.COAL_ORE);
            blackListDangerousBlock(mod, Blocks.DEEPSLATE_IRON_ORE);
            blackListDangerousBlock(mod, Blocks.IRON_ORE);
        }

        List<Block> ancientCityBlocks = List.of(Blocks.DEEPSLATE_BRICKS, Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.SOUL_LANTERN, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
        final int radius = 5;
        for (BlockPos pos : mod.getBlockScanner().getKnownLocations(ItemHelper.itemsToBlocks(ItemHelper.WOOL))) {

            searchLoop:
            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    for (int z = -radius; z < radius; z++) {
                        BlockPos p = pos.add(x, y, z);
                        Block block = mod.getWorld().getBlockState(p).getBlock();

                        if (ancientCityBlocks.contains(block)) {
                            Debug.logMessage("Blacklisting ancient city wool " + pos);
                            mod.getBlockScanner().requestBlockUnreachable(pos, 0);
                            break searchLoop;
                        }
                    }
                }
            }
        }

        if (locateStrongholdTask.isActive() && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD && !mod.getClientBaritone().getExploreProcess().isActive() && timer1.elapsed()) {
            timer1.reset();
        }
        if ((getOneBedTask != null && getOneBedTask.isActive() || (sleepThroughNightTask.isActive() && !mod.getItemStorage().hasItem(ItemHelper.BED))) && getBedTask == null && !mod.getClientBaritone().getExploreProcess().isActive() && timer3.elapsed()) {
            timer3.reset();
        }

        //armor quipping logic
        if (WorldHelper.getCurrentDimension() != Dimension.END && mod.getItemStorage().hasItem(Items.SHIELD) && !mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) {
            return new EquipArmorTask(Items.SHIELD);
        }

        if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
            if (mod.getItemStorage().hasItem(Items.GOLDEN_HELMET)) {
                return new EquipArmorTask(Items.GOLDEN_HELMET);
            } else if (mod.getItemStorage().hasItem(Items.DIAMOND_HELMET) && !hasItem(mod, Items.GOLDEN_HELMET)) {
                return new EquipArmorTask(Items.DIAMOND_HELMET);
            }
        } else {
            if (mod.getItemStorage().hasItem(Items.DIAMOND_HELMET)) {
                return new EquipArmorTask(Items.DIAMOND_HELMET);
            }
        }

        if (mod.getItemStorage().hasItem(Items.DIAMOND_CHESTPLATE)) {
            return new EquipArmorTask(Items.DIAMOND_CHESTPLATE);
        }
        if (mod.getItemStorage().hasItem(Items.DIAMOND_LEGGINGS)) {
            return new EquipArmorTask(Items.DIAMOND_LEGGINGS);
        }
        if (mod.getItemStorage().hasItem(Items.DIAMOND_BOOTS)) {
            return new EquipArmorTask(Items.DIAMOND_BOOTS);
        }


        if (!StorageHelper.isBigCraftingOpen() && !StorageHelper.isFurnaceOpen() && !StorageHelper.isSmokerOpen() && !StorageHelper.isBlastFurnaceOpen() && !StorageHelper.isChestOpen()) {
            //can cause the bot to get stuck
            if (mod.getItemStorage().getItemCount(Items.FURNACE) > 1) {
                return new PlaceBlockNearbyTask(Blocks.FURNACE);
            }
            if (mod.getItemStorage().getItemCount(Items.CRAFTING_TABLE) > 1) {
                return new PlaceBlockNearbyTask(Blocks.CRAFTING_TABLE);
            }
            throwAwayItems(mod, Items.SAND, Items.RED_SAND);
            throwAwayItems(mod, Items.TORCH);
            throwAwayItems(mod, uselessItems);


            if (mod.getItemStorage().hasItem(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE)) {
                throwAwayItems(mod, Items.WOODEN_PICKAXE);
            }
            if (mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) {
                throwAwayItems(mod, Items.IRON_PICKAXE, Items.STONE_PICKAXE);
            }

            if (mod.getItemStorage().hasItem(Items.DIAMOND_SWORD)) {
                throwAwayItems(mod, Items.STONE_SWORD, Items.IRON_SWORD);
            }

            if (mod.getItemStorage().hasItem(Items.GOLDEN_HELMET)) {
                throwAwayItems(mod, Items.RAW_GOLD, Items.GOLD_INGOT);
            }

            if (mod.getItemStorage().hasItem(Items.FLINT) || mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
                throwAwayItems(mod, Items.GRAVEL);
            }
            if (mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
                throwAwayItems(mod, Items.FLINT);
            }
            if (isTaskRunning(mod, getRidOfExtraWaterBucketTask)) {
                return getRidOfExtraWaterBucketTask;
            }
            if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) > 1) {
                getRidOfExtraWaterBucketTask = new GetRidOfExtraWaterBucketTask();
                return getRidOfExtraWaterBucketTask;
            }
            if (mod.getItemStorage().getItemCount(Items.FLINT_AND_STEEL) > 1) {
                throwAwayItems(mod, Items.FLINT_AND_STEEL);
            }
            if (mod.getItemStorage().getItemCount(ItemHelper.BED) > getTargetBeds(mod) && !endPortalFound(mod, endPortalCenterLocation) && WorldHelper.getCurrentDimension() != Dimension.END) {
                throwAwayItems(mod, ItemHelper.BED);
            }
        }


        /*
        if in the overworld:
          if end portal found:
            if end portal opened:
              @make sure we have iron gear and enough beds to kill the dragon first, considering whether that gear was dropped in the end
              @enter end portal
            else if we have enough eyes of ender:
              @fill in the end portal
          else if we have enough eyes of ender:
            @locate the end portal
          else:
            if we don't have diamond gear:
              if we have no food:
                @get a little bit of food
              @get diamond gear
            @go to the nether
        if in the nether:
          if we don't have enough blaze rods:
            @kill blazes till we do
          else if we don't have enough pearls:
            @kill enderman till we do
          else:
            @leave the nether
        if in the end:
          if we have a bed:
            @do bed strats
          else:
            @just hit the dragon normally
         */

        // By default, don't walk over end portals.
        enterindEndPortal = false;

        // End stuff.
        if (WorldHelper.getCurrentDimension() == Dimension.END) {
            if (!mod.getWorld().isChunkLoaded(0, 0)) {
                setDebugState("Waiting for chunks to load");
                return null;
            }

            // If we have bed, do bed strats, otherwise punk normally.
            updateCachedEndItems(mod);
            // Grab beds
            if (mod.getEntityTracker().itemDropped(ItemHelper.BED) && (needsBeds(mod) || WorldHelper.getCurrentDimension() == Dimension.END))
                return new PickupDroppedItemTask(new ItemTarget(ItemHelper.BED), true);
            // Grab tools
            if (!mod.getItemStorage().hasItem(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE)) {
                if (mod.getEntityTracker().itemDropped(Items.IRON_PICKAXE))
                    return new PickupDroppedItemTask(Items.IRON_PICKAXE, 1);
                if (mod.getEntityTracker().itemDropped(Items.DIAMOND_PICKAXE))
                    return new PickupDroppedItemTask(Items.DIAMOND_PICKAXE, 1);
            }
            if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET) && mod.getEntityTracker().itemDropped(Items.WATER_BUCKET))
                return new PickupDroppedItemTask(Items.WATER_BUCKET, 1);
            // Grab armor
            for (Item armorCheck : COLLECT_EYE_ARMOR_END) {
                if (!StorageHelper.isArmorEquipped(mod, armorCheck)) {
                    if (mod.getItemStorage().hasItem(armorCheck)) {
                        setDebugState("Equipping armor.");
                        return new EquipArmorTask(armorCheck);
                    }
                    if (mod.getEntityTracker().itemDropped(armorCheck)) {
                        return new PickupDroppedItemTask(armorCheck, 1);
                    }
                }
            }
            // Dragons breath avoidance
            dragonBreathTracker.updateBreath(mod);
            for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
                if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                    setDebugState("ESCAPE dragons breath");
                    escapingDragonsBreath = true;
                    return dragonBreathTracker.getRunAwayTask();
                }
            }
            escapingDragonsBreath = false;

            // If we find an ender portal, just GO to it!!!
            if (mod.getBlockScanner().anyFound(Blocks.END_PORTAL)) {
                setDebugState("WOOHOO");
                dragonIsDead = true;
                enterindEndPortal = true;
                if (!mod.getExtraBaritoneSettings().isCanWalkOnEndPortal()) {
                    mod.getExtraBaritoneSettings().canWalkOnEndPortal(true);
                }
                return new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), (pos) -> Math.abs(pos.getX()) + Math.abs(pos.getZ()) <= 1, Blocks.END_PORTAL);
            }
            if (mod.getItemStorage().hasItem(ItemHelper.BED) || mod.getBlockScanner().anyFound(ItemHelper.itemsToBlocks(ItemHelper.BED))) {
                setDebugState("Bed strats");
                return killDragonBedStratsTask;
            }
            setDebugState("No beds, regular strats.");
            return new KillEnderDragonTask();
        } else {
            // We're not in the end so reset our "end cache" timer
            cachedEndItemNothingWaitTime.reset();
        }

        // Check for end portals. Always.
        if (!endPortalOpened(mod, endPortalCenterLocation) && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) {
            Optional<BlockPos> endPortal = mod.getBlockScanner().getNearestBlock(Blocks.END_PORTAL);
            if (endPortal.isPresent()) {
                endPortalCenterLocation = endPortal.get();
                endPortalOpened = true;
            } else {
                // TODO: Test that this works, for some reason the bot gets stuck near the stronghold and it keeps "Searching" for the portal
                endPortalCenterLocation = doSimpleSearchForEndPortal(mod);
            }
        }
        if (isTaskRunning(mod, rePickupTask)) {
            return rePickupTask;
        }


        // Portable crafting table.
        // If we're NOT using our crafting table right now and there's one nearby, grab it.
        if (!endPortalOpened && WorldHelper.getCurrentDimension() != Dimension.END && config.rePickupCraftingTable && !mod.getItemStorage().hasItem(Items.CRAFTING_TABLE) && !thisOrChildSatisfies(isCraftingTableTask) && (mod.getBlockScanner().anyFound(blockPos -> WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos), Blocks.CRAFTING_TABLE) || mod.getEntityTracker().itemDropped(Items.CRAFTING_TABLE)) && pickupCrafting) {
            setDebugState("Picking up the crafting table while we are at it.");
            return new MineAndCollectTask(Items.CRAFTING_TABLE, 1, new Block[]{Blocks.CRAFTING_TABLE}, MiningRequirement.HAND);
        }
        if (config.rePickupSmoker && !endPortalOpened && WorldHelper.getCurrentDimension() != Dimension.END && !mod.getItemStorage().hasItem(Items.SMOKER) && (mod.getBlockScanner().anyFound(blockPos -> WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos), Blocks.SMOKER) || mod.getEntityTracker().itemDropped(Items.SMOKER)) && pickupSmoker) {
            setDebugState("Picking up the smoker while we are at it.");
            rePickupTask = new MineAndCollectTask(Items.SMOKER, 1, new Block[]{Blocks.SMOKER}, MiningRequirement.WOOD);
            return rePickupTask;
        }
        if (config.rePickupFurnace && !endPortalOpened && WorldHelper.getCurrentDimension() != Dimension.END && !mod.getItemStorage().hasItem(Items.FURNACE) && (mod.getBlockScanner().anyFound(blockPos -> WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos), Blocks.FURNACE) || mod.getEntityTracker().itemDropped(Items.FURNACE)) && !goToNetherTask.isActive() && !ranStrongholdLocator && pickupFurnace) {
            setDebugState("Picking up the furnace while we are at it.");
            rePickupTask = new MineAndCollectTask(Items.FURNACE, 1, new Block[]{Blocks.FURNACE}, MiningRequirement.WOOD);
            return rePickupTask;
        }
        pickupFurnace = false;
        pickupSmoker = false;
        pickupCrafting = false;

        // Sleep through night.
        if (config.sleepThroughNight && !endPortalOpened && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) {
            if (WorldHelper.canSleep()) {
                if (timer2.elapsed()) {
                    timer2.reset();
                }

                if (timer2.getDuration() >= 30 && !mod.getPlayer().isSleeping()) {
                    if (mod.getEntityTracker().itemDropped(ItemHelper.BED) && needsBeds(mod)) {
                        setDebugState("Resetting sleep through night task.");
                        return new PickupDroppedItemTask(new ItemTarget(ItemHelper.BED), true);
                    }
                    if (anyBedsFound(mod)) {
                        setDebugState("Resetting sleep through night task.");
                        return new DoToClosestBlockTask(DestroyBlockTask::new, ItemHelper.itemsToBlocks(ItemHelper.BED));
                    }
                }

                setDebugState("Sleeping through night");
                return sleepThroughNightTask;
            }
            if (!mod.getItemStorage().hasItem(ItemHelper.BED) && (mod.getBlockScanner().anyFound(blockPos -> WorldHelper.canBreak(mod, blockPos), ItemHelper.itemsToBlocks(ItemHelper.BED)) || isTaskRunning(mod, getOneBedTask))) {
                setDebugState("Getting one bed to sleep in at night.");
                return getOneBedTask;
            }
        }

        // Do we need more eyes?
        boolean needsEyes = !endPortalOpened(mod, endPortalCenterLocation) && WorldHelper.getCurrentDimension() != Dimension.END;
        int filledPortalFrames = getFilledPortalFrames(mod, endPortalCenterLocation);
        int eyesNeededMin = needsEyes ? config.minimumEyes - filledPortalFrames : 0;
        int eyesNeeded = needsEyes ? config.targetEyes - filledPortalFrames : 0;

        int eyes = mod.getItemStorage().getItemCount(Items.ENDER_EYE);
        if (eyes < eyesNeededMin || (!ranStrongholdLocator && collectingEyes && eyes < eyesNeeded)) {
            collectingEyes = true;
            return getEyesOfEnderTask(mod, eyesNeeded);
        } else {
            collectingEyes = false;
        }

        // make new pickaxe if old one breaks
        ItemStorageTracker itemStorage = mod.getItemStorage();
        if (itemStorage.getItemCount(Items.DIAMOND) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
            return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
        } else if (itemStorage.getItemCount(Items.IRON_INGOT) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
            return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
        } else if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE)) {
            return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
        }
        if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE)) {
            return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
        }

        // We have eyes. Locate our portal + enter.
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) {
            if (mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) {
                Item[] throwGearItems = {Items.STONE_SWORD, Items.STONE_PICKAXE, Items.IRON_SWORD, Items.IRON_PICKAXE};
                List<Slot> ironArmors = mod.getItemStorage().getSlotsWithItemPlayerInventory(true, COLLECT_IRON_ARMOR);
                List<Slot> throwGears = mod.getItemStorage().getSlotsWithItemPlayerInventory(true, throwGearItems);
                if (!StorageHelper.isBigCraftingOpen() && !StorageHelper.isFurnaceOpen() && !StorageHelper.isSmokerOpen() && !StorageHelper.isBlastFurnaceOpen() && (mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL) || mod.getItemStorage().hasItem(Items.FIRE_CHARGE))) {

                    for (Slot throwGear : throwGears) {
                        if (Slot.isCursor(throwGear)) {
                            if (!mod.getControllerExtras().isBreakingBlock()) {
                                LookHelper.randomOrientation(mod);
                            }
                            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                        } else {
                            mod.getSlotHandler().clickSlot(throwGear, 0, SlotActionType.PICKUP);
                        }
                    }


                    for (Slot ironArmor : ironArmors) {
                        if (Slot.isCursor(ironArmor)) {
                            if (!mod.getControllerExtras().isBreakingBlock()) {
                                LookHelper.randomOrientation(mod);
                            }
                            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                        } else {
                            mod.getSlotHandler().clickSlot(ironArmor, 0, SlotActionType.PICKUP);
                        }
                    }

                }
            }
                ranStrongholdLocator = true;
                // Get beds before starting our portal location.
                if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD && needsBeds(mod)) {
                    setDebugState("Getting beds before stronghold search.");
                    if (!mod.getClientBaritone().getExploreProcess().isActive() && timer1.elapsed()) {
                        timer1.reset();
                    }
                    getBedTask = getBedTask(mod);
                    return getBedTask;
                } else {
                    getBedTask = null;
                }
                if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                    setDebugState("Getting water bucket.");
                    return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
                }
                if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
                    setDebugState("Getting flint and steel.");
                    return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
                }
                if (needsBuildingMaterials(mod)) {
                    setDebugState("Collecting building materials.");
                    return buildMaterialsTask;
                }

                if (!endPortalFound(mod,endPortalCenterLocation)) {
                    // Portal Location
                    setDebugState("Locating End Portal...");
                    return locateStrongholdTask;
                }

                // WE FOUND END PORTAL AND SHOULD HAVE ALL THE NECESSARY STUFF
                // Destroy silverfish spawner
                if (StorageHelper.miningRequirementMetInventory(mod, MiningRequirement.WOOD)) {
                    Optional<BlockPos> silverfish = mod.getBlockScanner().getNearestBlock(blockPos -> (WorldHelper.getSpawnerEntity(mod, blockPos) instanceof SilverfishEntity)
                            , Blocks.SPAWNER);

                    if (silverfish.isPresent()) {
                        setDebugState("Breaking silverfish spawner.");
                        return new DestroyBlockTask(silverfish.get());
                    }
                }
                if (endPortalOpened(mod, endPortalCenterLocation)) {
                    openingEndPortal = false;
                    if (needsBuildingMaterials(mod)) {
                        setDebugState("Collecting building materials.");
                        return buildMaterialsTask;
                    }
                    if (config.placeSpawnNearEndPortal && mod.getItemStorage().hasItem(ItemHelper.BED) && (!spawnSetNearPortal(mod, endPortalCenterLocation))) {
                        setDebugState("Setting spawn near end portal");
                        return setSpawnNearPortalTask(mod);

                    }
                    // We're as ready as we'll ever be, hop into the portal!
                    setDebugState("Entering End");
                    enterindEndPortal = true;
                    if (!mod.getExtraBaritoneSettings().isCanWalkOnEndPortal()) {
                        mod.getExtraBaritoneSettings().canWalkOnEndPortal(true);
                    }
                    return new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), Blocks.END_PORTAL);
                } else {
                    if (!mod.getItemStorage().hasItem(Items.OBSIDIAN)) {
                        if (mod.getBlockScanner().anyFoundWithinDistance(10, Blocks.OBSIDIAN) || mod.getEntityTracker().itemDropped(Items.OBSIDIAN)) {
                            if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                                return new CollectBucketLiquidTask.CollectWaterBucketTask(1);
                            }
                            if (!waterPlacedTimer.elapsed()) {
                                setDebugState("waitin " + waterPlacedTimer.getDuration());
                                return null;
                            }
                            return TaskCatalogue.getItemTask(Items.OBSIDIAN, 1);
                        } else {
                            if (repeated > 2 && !mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                                return new CollectBucketLiquidTask.CollectWaterBucketTask(1);
                            }
                            if (waterPlacedTimer.elapsed()) {
                                if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                                    repeated++;
                                    waterPlacedTimer.reset();
                                    return null;
                                } else {
                                    repeated = 0;
                                }

                                return new PlaceObsidianBucketTask(
                                        mod.getBlockScanner().getNearestBlock(WorldHelper.toVec3d(endPortalCenterLocation), (blockPos) -> !blockPos.isWithinDistance(endPortalCenterLocation, 8), Blocks.LAVA).get());
                            }
                            setDebugState(waterPlacedTimer.getDuration() + "");
                            return null;
                        }
                    }
                    // Open the portal! (we have enough eyes, do it)
                    setDebugState("Opening End Portal");
                    openingEndPortal = true;
                    return new DoToClosestBlockTask(blockPos -> new InteractWithBlockTask(Items.ENDER_EYE, blockPos), blockPos -> !isEndPortalFrameFilled(mod, blockPos), Blocks.END_PORTAL_FRAME);
                }
        } else if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
            Item[] throwGearItems = {Items.STONE_SWORD, Items.STONE_PICKAXE, Items.IRON_SWORD, Items.IRON_PICKAXE};
            List<Slot> ironArmors = mod.getItemStorage().getSlotsWithItemPlayerInventory(true, COLLECT_IRON_ARMOR);
            List<Slot> throwGears = mod.getItemStorage().getSlotsWithItemPlayerInventory(true, throwGearItems);
            if (!StorageHelper.isBigCraftingOpen() && !StorageHelper.isFurnaceOpen() && !StorageHelper.isSmokerOpen() && !StorageHelper.isBlastFurnaceOpen() && (mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL) || mod.getItemStorage().hasItem(Items.FIRE_CHARGE))) {

                for (Slot throwGear : throwGears) {
                    if (Slot.isCursor(throwGear)) {
                        if (!mod.getControllerExtras().isBreakingBlock()) {
                            LookHelper.randomOrientation(mod);
                        }
                        mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    } else {
                        mod.getSlotHandler().clickSlot(throwGear, 0, SlotActionType.PICKUP);
                    }
                }


                for (Slot ironArmor : ironArmors) {
                    if (Slot.isCursor(ironArmor)) {
                        if (!mod.getControllerExtras().isBreakingBlock()) {
                            LookHelper.randomOrientation(mod);
                        }
                        mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    } else {
                        mod.getSlotHandler().clickSlot(ironArmor, 0, SlotActionType.PICKUP);
                    }
                }

            }
            // Portal Location
            setDebugState("Locating End Portal...");
            return locateStrongholdTask;
        }
        return null;
    }

    /**
     * Sets the spawn point near the portal.
     *
     * @param mod The AltoClef mod instance.
     * @return The task to set the spawn point near the portal.
     */
    private Task setSpawnNearPortalTask(AltoClef mod) {
        if (setBedSpawnTask.isSpawnSet()) {
            bedSpawnLocation = setBedSpawnTask.getBedSleptPos();
        } else {
            bedSpawnLocation = null;
        }

        if (isTaskRunning(mod, setBedSpawnTask)) {
            setDebugState("Setting spawnpoint now.");
            return setBedSpawnTask;
        }

        // Check if the player is within range of the portal
        if (WorldHelper.inRangeXZ(mod.getPlayer(), WorldHelper.toVec3d(endPortalCenterLocation), END_PORTAL_BED_SPAWN_RANGE)) {
            return setBedSpawnTask;
        } else {
            setDebugState("Approaching portal (to set spawnpoint)");
            return new GetToXZTask(endPortalCenterLocation.getX(), endPortalCenterLocation.getZ());
        }
    }

    /**
     * Returns a Task to handle Blaze Rods based on the given count.
     *
     * @param mod   The AltoClef mod instance.
     * @param count The desired count of Blaze Rods.
     * @return A Task to handle Blaze Rods.
     */
    private Task getBlazeRodsTask(AltoClef mod, int count) {
        EntityTracker entityTracker = mod.getEntityTracker();

        if (entityTracker.itemDropped(Items.BLAZE_ROD)) {
            Debug.logInternal("Blaze Rod dropped, picking it up.");
            return new PickupDroppedItemTask(Items.BLAZE_ROD, 1);
        } else if (entityTracker.itemDropped(Items.BLAZE_POWDER)) {
            Debug.logInternal("Blaze Powder dropped, picking it up.");
            return new PickupDroppedItemTask(Items.BLAZE_POWDER, 1);
        } else {
            Debug.logInternal("No Blaze Rod or Blaze Powder dropped, collecting Blaze Rods.");
            return new CollectBlazeRodsTask(count);
        }
    }

    /**
     * Returns a Task to obtain Ender Pearls.
     *
     * @param mod   The mod instance.
     * @param count The desired number of Ender Pearls.
     * @return The Task to obtain Ender Pearls.
     */
    private Task getEnderPearlTask(AltoClef mod, int count) {
        if (mod.getEntityTracker().itemDropped(Items.ENDER_PEARL)) {
            return new PickupDroppedItemTask(Items.ENDER_PEARL, 1);
        }

        // Check if we should barter Pearls instead of hunting Endermen.
        if (config.barterPearlsInsteadOfEndermanHunt) {
            // Check if Golden Helmet is not equipped, and equip it.
            if (!StorageHelper.isArmorEquipped(mod, Items.GOLDEN_HELMET)) {
                return new EquipArmorTask(Items.GOLDEN_HELMET);
            }
            // Trade with Piglins for Ender Pearls.
            return new TradeWithPiglinsTask(32, Items.ENDER_PEARL, count);
        }

        boolean endermanFound = mod.getEntityTracker().entityFound(EndermanEntity.class);
        boolean pearlDropped = mod.getEntityTracker().itemDropped(Items.ENDER_PEARL);

        // Check if we have found an Enderman or Ender Pearl and have enough Twisting Vines.
        if (endermanFound || pearlDropped) {
            Optional<Entity> toKill = mod.getEntityTracker().getClosestEntity(EndermanEntity.class);
            if (toKill.isPresent() && mod.getEntityTracker().isEntityReachable(toKill.get())) {
                return new KillEndermanTask(count);
            }
        }

        // Search for Ender Pearls within the warped forest biome.
        setDebugState("Waiting for endermen to spawn... ");
        return null;
    }

    /**
     * Calculates the target number of beds based on the configuration settings.
     *
     * @param mod The AltoClef mod instance.
     * @return The target number of beds.
     */
    private int getTargetBeds(AltoClef mod) {
        // Check if spawn needs to be set near the end portal
        boolean needsToSetSpawn = config.placeSpawnNearEndPortal && (!spawnSetNearPortal(mod, endPortalCenterLocation) && !isTaskRunning(mod, setBedSpawnTask));

        // Calculate the number of beds in the end
        int bedsInEnd = Arrays.stream(ItemHelper.BED).mapToInt(bed -> cachedEndItemDrops.getOrDefault(bed, 0)).sum();

        // Calculate the target number of beds
        int targetBeds = config.requiredBeds + (needsToSetSpawn ? 1 : 0) - bedsInEnd;

        // Output debug information
        Debug.logInternal("needsToSetSpawn: " + needsToSetSpawn);
        Debug.logInternal("bedsInEnd: " + bedsInEnd);
        Debug.logInternal("targetBeds: " + targetBeds);

        return targetBeds;
    }

    /**
     * Checks if the player needs to acquire more beds.
     *
     * @param mod The instance of the AltoClef mod.
     * @return True if the player needs more beds, false otherwise.
     */
    private boolean needsBeds(AltoClef mod) {
        // Calculate the total number of end items obtained from breaking beds
        int totalEndItems = 0;
        for (Item bed : ItemHelper.BED) {
            totalEndItems += cachedEndItemDrops.getOrDefault(bed, 0);
        }

        // Get the current number of beds in the player's inventory
        int itemCount = mod.getItemStorage().getItemCount(ItemHelper.BED);

        // Get the target number of beds to have
        int targetBeds = getTargetBeds(mod);

        // Log the values for debugging purposes
        Debug.logInternal("Total End Items: " + totalEndItems);
        Debug.logInternal("Item Count: " + itemCount);
        Debug.logInternal("Target Beds: " + targetBeds);

        // Check if the player needs to acquire more beds
        boolean needsBeds = (itemCount + totalEndItems) < targetBeds;

        // Log the result for debugging purposes
        Debug.logInternal("Needs Beds: " + needsBeds);

        // Return whether the player needs more beds
        return needsBeds;
    }

    /**
     * Retrieves a task to obtain the desired number of beds.
     *
     * @param mod The AltoClef mod instance.
     * @return The task to obtain the beds.
     */
    private Task getBedTask(AltoClef mod) {
        int targetBeds = getTargetBeds(mod);
        if (!mod.getItemStorage().hasItem(Items.SHEARS) && !anyBedsFound(mod)) {
            Debug.logInternal("Getting shears.");
            return TaskCatalogue.getItemTask(Items.SHEARS, 1);
        }
        Debug.logInternal("Getting beds.");
        return TaskCatalogue.getItemTask("bed", targetBeds);
    }

    /**
     * Checks if any beds are found in the game.
     *
     * @param mod The AltoClef mod instance.
     * @return true if beds are found either in blocks or entities, false otherwise.
     */
    private boolean anyBedsFound(AltoClef mod) {
        // Get the block and entity trackers from the mod instance.
        BlockScanner blockTracker = mod.getBlockScanner();
        EntityTracker entityTracker = mod.getEntityTracker();

        // Check if any beds are found in blocks.
        boolean bedsFoundInBlocks = blockTracker.anyFound(ItemHelper.itemsToBlocks(ItemHelper.BED));

        // Check if any beds are dropped by entities.
        boolean bedsFoundInEntities = entityTracker.itemDropped(ItemHelper.BED);

        // Log a message if beds are found in blocks.
        if (bedsFoundInBlocks) {
            Debug.logInternal("Beds found in blocks");
        }

        // Log a message if beds are found in entities.
        if (bedsFoundInEntities) {
            Debug.logInternal("Beds found in entities");
        }

        // Return true if beds are found either in blocks or entities.
        return bedsFoundInBlocks || bedsFoundInEntities;
    }

    /**
     * Searches for the position of an end portal frame by averaging the known locations of the frames.
     * Returns the center position of the frames if enough frames are found, otherwise returns null.
     *
     * @param mod The AltoClef instance.
     * @return The position of the end portal frame, or null if not enough frames are found.
     */
    private BlockPos doSimpleSearchForEndPortal(AltoClef mod) {
        List<BlockPos> frames = mod.getBlockScanner().getKnownLocations(Blocks.END_PORTAL_FRAME);

        if (frames.size() >= END_PORTAL_FRAME_COUNT) {
            // Calculate the average position of the frames.
            Vec3d average = frames.stream().reduce(Vec3d.ZERO, (accum, bpos) -> accum.add((int) Math.round(bpos.getX() + 0.5), (int) Math.round(bpos.getY() + 0.5), (int) Math.round(bpos.getZ() + 0.5)), Vec3d::add).multiply(1d / frames.size());

            // Log the average position.
            mod.log("Average Position: " + average);

            return new BlockPos(new Vec3i((int) average.x, (int) average.y, (int) average.z));
        }

        // Log that there are not enough frames.
        Debug.logInternal("Not enough frames");

        return null;
    }

    /**
     * Returns the number of filled portal frames around the end portal center.
     * If the end portal is found, it returns the constant END_PORTAL_FRAME_COUNT.
     * Otherwise, it checks each frame block around the end portal center and counts the filled frames.
     * The count is cached for subsequent calls.
     *
     * @param mod             The AltoClef mod instance.
     * @param endPortalCenter The center position of the end portal.
     * @return The number of filled portal frames.
     */
    private int getFilledPortalFrames(AltoClef mod, BlockPos endPortalCenter) {
        if (endPortalFound(mod, endPortalCenter)) {
            return END_PORTAL_FRAME_COUNT;
        }

        // Get all the frame blocks around the end portal center.
        List<BlockPos> frameBlocks = getFrameBlocks(endPortalCenter);

        // Check if all the frame blocks are loaded.
        if (frameBlocks.stream().allMatch(blockPos -> mod.getChunkTracker().isChunkLoaded(blockPos))) {
            // Calculate the sum of filled frames using a stream and mapToInt.
            cachedFilledPortalFrames = frameBlocks.stream().mapToInt(blockPos -> {
                boolean isFilled = isEndPortalFrameFilled(mod, blockPos);
                // Log whether the frame is filled or not.
                if (isFilled) {
                    Debug.logInternal("Portal frame at " + blockPos + " is filled.");
                } else {
                    Debug.logInternal("Portal frame at " + blockPos + " is not filled.");
                }
                return isFilled ? 1 : 0;
            }).sum();
        }

        return cachedFilledPortalFrames;
    }

    /**
     * Checks if a chest at the given block position can be looted.
     *
     * @param mod      The instance of the mod.
     * @param blockPos The block position of the chest to check.
     * @return True if the chest can be looted as a portal chest, false otherwise.
     */
    private boolean canBeLootablePortalChest(AltoClef mod, BlockPos blockPos) {
        // Check if the block above is water or if the y-coordinate is below 50
        return mod.getWorld().getBlockState(blockPos.up()).getBlock() != Blocks.WATER && blockPos.getY() >= 50;

       /* // Define the minimum and maximum positions to scan for NETHERRACK blocks
        BlockPos minPos = blockPos.add(-4, -2, -4);
        BlockPos maxPos = blockPos.add(4, 2, 4);

        // Log the scanning region
        Debug.logInternal("Scanning region from " + minPos + " to " + maxPos);

        // Scan the region defined by minPos and maxPos
        for (BlockPos checkPos : WorldHelper.scanRegion(mod, minPos, maxPos)) {
            // Check if the block at checkPos is NETHERRACK
            if (mod.getWorld().getBlockState(checkPos).getBlock() == Blocks.NETHERRACK) {
                return true;
            }
        }

        // Log that the blockPos is added to the list of not ruined portal chests
        Debug.logInternal("Adding blockPos " + blockPos + " to the list of not ruined portal chests");

        // Add the blockPos to the list of not ruined portal chests
        notRuinedPortalChests.add(blockPos);

        return false;*/
    }
    private GatherResource prevLastGather = null;

    private Task getEyesOfEnderTask(AltoClef mod, int targetEyes) {
        if (mod.getEntityTracker().itemDropped(Items.ENDER_EYE)) {
            setDebugState("Picking up Dropped Eyes");
            return new PickupDroppedItemTask(Items.ENDER_EYE, targetEyes);
        }

        int eyeCount = mod.getItemStorage().getItemCount(Items.ENDER_EYE);
        int blazePowderCount = mod.getItemStorage().getItemCount(Items.BLAZE_POWDER);
        int blazeRodCount = mod.getItemStorage().getItemCount(Items.BLAZE_ROD);

        int blazeRodTarget = (int) Math.ceil(((double) targetEyes - eyeCount - blazePowderCount) / 2.0);
        int enderPearlTarget = targetEyes - eyeCount;

        boolean needsBlazeRods = blazeRodCount < blazeRodTarget;
        boolean needsBlazePowder = eyeCount + blazePowderCount < targetEyes;
        boolean needsEnderPearls = mod.getItemStorage().getItemCount(Items.ENDER_PEARL) < enderPearlTarget;

        if (needsBlazePowder && !needsBlazeRods) {
            // We have enough blaze rods.
            setDebugState("Crafting blaze powder");
            return TaskCatalogue.getItemTask(Items.BLAZE_POWDER, targetEyes - eyeCount);
        }

        if (!needsBlazePowder && !needsEnderPearls) {
            // Craft ender eyes
            setDebugState("Crafting Ender Eyes");
            return TaskCatalogue.getItemTask(Items.ENDER_EYE, targetEyes);
        }


        // Get blaze rods + pearls...
        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                if (!forcedTaskTimer.elapsed() && isTaskRunning(mod,lastTask)) {
                    return lastTask;
                }

                if (!changedTaskTimer.elapsed() && lastTask != null && !lastGather.bypassesForceCooldown() && isTaskRunning(mod,lastTask)) {
                    return lastTask;
                }
                if (isTaskRunning(mod, lastTask) && lastGather != null && lastGather.shouldForceTask()) {
                    return lastTask;
                }

                double maxPriority = 0;
                GatherResource toGather = null;

                for (GatherResource gatherResource : gatherResources) {
                    double priority = gatherResource.getPriority(mod);

                    if (priority > maxPriority) {
                        maxPriority = priority;
                        toGather = gatherResource;
                    }
                }

                if (toGather != null) {
                    setDebugState("Priority: " + String.format(Locale.US,"%.2f",maxPriority) + ", "+toGather.getDescription());
                    if (prevLastGather == toGather && lastTask != null && lastGather.getPriority(mod) > 0 && isTaskRunning(mod, lastTask)) {
                        mod.logWarning("might be stuck or switching too much, forcing current resource for a bit more");
                        changedTaskTimer.reset();
                        prevLastGather = null; //do not force infinitely, 3 sec should be enough I hope
                        setDebugState("Priority: FORCED, "+lastGather.getDescription());
                        return lastTask;
                    }
                    if (lastGather == toGather && toGather.canCacheTask()) {
                        return lastTask;
                    }
                    if (lastGather != toGather) {
                        taskChanges.add(0,new TaskChange(lastGather,toGather,mod.getPlayer().getBlockPos()));
                    }


                    if (taskChanges.size() >= 3) {
                        TaskChange t1 = taskChanges.get(0);
                        TaskChange t2 = taskChanges.get(1);
                        TaskChange t3 = taskChanges.get(2);

                        if (t1.original == t2.interrupt && t1.pos.isWithinDistance(t3.pos,5) && t3.original == t1.interrupt) {
                            forcedTaskTimer.reset();
                            mod.logWarning("Probably stuck! Forcing timer...");
                            taskChanges.clear();
                            return lastTask;
                        }
                        if (taskChanges.size() > 3) {
                            taskChanges.remove(taskChanges.size()-1);
                        }
                    }


                    prevLastGather = lastGather;
                    lastGather = toGather;

                    Task task;

                    if (toGather.data.isPresent()) {
                        Object data = toGather.data.get();
                        if (data instanceof String codeName) {
                            task = TaskCatalogue.getItemTask(codeName, toGather.maxCount);
                        } else if (data instanceof Task buildInTask) {
                            task = buildInTask;
                        } else {
                            throw new IllegalStateException("Invalid gather resource data!");
                        }
                    } else {
                        // if code name isn't present toGather shouldn't have more params
                        task = TaskCatalogue.getItemTask(toGather.toCollect[0], toGather.maxCount);
                    }

                    if (lastTask instanceof SmeltInFurnaceTask && !(task instanceof SmeltInFurnaceTask) && !mod.getItemStorage().hasItem(Items.FURNACE)) {
                        pickupFurnace = true;
                        lastGather = null;
                        lastTask = null;
                        StorageHelper.closeScreen();
                        return null;
                    } else if (lastTask instanceof SmeltInSmokerTask && !(task instanceof SmeltInSmokerTask) && !mod.getItemStorage().hasItem(Items.SMOKER)) {
                        pickupSmoker = true;
                        lastGather = null;
                        lastTask = null;
                        StorageHelper.closeScreen();
                        return null;
                    } else if (lastTask != null && task != null && !toGather.needsCraftingOnStart(mod)) {
                        pickupCrafting = true;
                        lastGather = null;
                        lastTask = null;
                        StorageHelper.closeScreen();
                        return null;
                    }

                    lastTask = task;

                    changedTaskTimer.reset();
                    return task;
                }

                if (needsBuildingMaterials(mod)) {
                    setDebugState("Collecting building materials.");
                    return buildMaterialsTask;
                }
                // Then go to the nether.
                setDebugState("Going to Nether");

                // make new pickaxe if old one breaks
                // TODO refactor duplicated code
                ItemStorageTracker itemStorage = mod.getItemStorage();
                if (itemStorage.getItemCount(Items.DIAMOND) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                } else if (itemStorage.getItemCount(Items.IRON_INGOT) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
                } else if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
                }
                if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
                }

                // DO NOT INTERRUPT GOING TO NETHER
                gatherResources.clear();

                //prevents from getting stuck
                if (!(lastTask instanceof DefaultGoToDimensionTask)) {
                    goToNetherTask = new DefaultGoToDimensionTask(Dimension.NETHER);
                }
                lastTask = goToNetherTask;
                return goToNetherTask;
            }
            case NETHER -> {
                if (isTaskRunning(mod, safeNetherPortalTask)) {
                    return safeNetherPortalTask;
                }

                if (mod.getPlayer().getPortalCooldown() != 0 && safeNetherPortalTask == null) {
                    safeNetherPortalTask = new SafeNetherPortalTask();
                    return safeNetherPortalTask;
                }

                mod.getInputControls().release(Input.MOVE_FORWARD);
                mod.getInputControls().release(Input.MOVE_LEFT);
                mod.getInputControls().release(Input.SNEAK);

                BlockPos pos = mod.getPlayer().getSteppingPos();
                if (!escaped && mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.SOUL_SAND) &&
                        (mod.getWorld().getBlockState(pos.east()).getBlock().equals(Blocks.OBSIDIAN) ||
                                mod.getWorld().getBlockState(pos.west()).getBlock().equals(Blocks.OBSIDIAN) ||
                                mod.getWorld().getBlockState(pos.south()).getBlock().equals(Blocks.OBSIDIAN) ||
                                mod.getWorld().getBlockState(pos.north()).getBlock().equals(Blocks.OBSIDIAN))) {

                    LookHelper.lookAt(mod, pos);
                    mod.getInputControls().hold(Input.CLICK_LEFT);
                    return null;
                }
                if (!escaped) {
                    escaped = true;
                    mod.getInputControls().release(Input.CLICK_LEFT);
                }


                // make new pickaxe if old one breaks
                ItemStorageTracker itemStorage = mod.getItemStorage();
                if (itemStorage.getItemCount(Items.DIAMOND) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                } else if (itemStorage.getItemCount(Items.IRON_INGOT) >= 3 && !itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
                } else if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
                }
                if (!itemStorage.hasItem(Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE)) {
                    return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
                }

                if (mod.getItemStorage().getItemCount(Items.BLAZE_ROD) * 2 + mod.getItemStorage().getItemCount(Items.BLAZE_POWDER) + mod.getItemStorage().getItemCount(Items.ENDER_EYE) >= 14) {
                    hasRods = true;
                }

                double rodDistance = mod.getBlockScanner().distanceToClosest(Blocks.NETHER_BRICKS);
                double pearlDistance = mod.getBlockScanner().distanceToClosest(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM);

                if (pearlDistance == Double.POSITIVE_INFINITY && rodDistance == Double.POSITIVE_INFINITY) {
                    setDebugState("Neither fortress or warped forest found... wandering");
                    if (isTaskRunning(mod, searchTask)) {
                        return searchTask;
                    }

                    searchTask = new SearchChunkForBlockTask(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM, Blocks.NETHER_BRICKS);
                    return searchTask;
                }

                if ((rodDistance < pearlDistance && !hasRods && !gettingPearls) || !needsEnderPearls) {
                    if (!gotToFortress) {
                        if (mod.getBlockScanner().anyFoundWithinDistance(5, Blocks.NETHER_BRICKS)) {
                            gotToFortress = true;
                        } else {
                            if (!mod.getBlockScanner().anyFound(Blocks.NETHER_BRICKS)) {
                                setDebugState("Searching for fortress");
                                return new TimeoutWanderTask();
                            }

                            if (WorldHelper.inRangeXZ(mod.getPlayer().getPos(),
                                    WorldHelper.toVec3d(mod.getBlockScanner().getNearestBlock(Blocks.NETHER_BRICKS).get()), 2)) {

                                setDebugState("trying to get to fortress");
                                return new GetToBlockTask(mod.getBlockScanner().getNearestBlock(Blocks.NETHER_BRICKS).get());
                            }

                            setDebugState("Getting close to fortress");

                            if (cachedFortressTask != null && !fortressTimer.elapsed() &&
                                    mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(cachedFortressTask.blockPos)) - 1 > prevPos.getManhattanDistance(cachedFortressTask.blockPos) / 2d) {
                                mod.log(mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(cachedFortressTask.blockPos)) + " : " + prevPos.getManhattanDistance(cachedFortressTask.blockPos) / 2);
                                return cachedFortressTask;
                            }

                            // 'isEqual' is fucking me up here, so I have to reset the task
                            if (resetFortressTask) {
                                resetFortressTask = false;
                                return null;
                            }
                            resetFortressTask = true;

                            fortressTimer.reset();
                            mod.log("new");

                            prevPos = mod.getPlayer().getBlockPos();

                            BlockPos p = mod.getBlockScanner().getNearestBlock(Blocks.NETHER_BRICKS).get();
                            int distance = (int) (mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(p)) / 2);
                            if (cachedFortressTask != null) {
                                // prevents from getting stuck in place
                                distance = Math.min(cachedFortressTask.range - 1, distance);
                            }
                            if (distance < 0) {
                                gotToFortress = true;
                            } else {
                                cachedFortressTask = new GetWithinRangeOfBlockTask(p, distance);
                                return cachedFortressTask;
                            }
                        }
                    }
                    setDebugState("Getting Blaze Rods");
                    return getBlazeRodsTask(mod, blazeRodTarget);
                }

                if (mod.getBlockScanner().anyFound(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM) || hasRods) {
                    gettingPearls = true;
                    setDebugState("Getting Ender Pearls");
                    Optional<BlockPos> closestBlock = mod.getBlockScanner().getNearestBlock(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM);


                    searchBiomeTask.reset();
                    if (closestBlock.isPresent()) {
                        if (!WorldHelper.inRangeXZ(mod.getPlayer(), closestBlock.get(), 30) && !gotToBiome) {
                            setDebugState("Going to biome");

                            return new GetToBlockTask(closestBlock.get());
                        } else {
                            gotToBiome = true;
                        }
                    } else {
                        setDebugState("biome not found, wandering");
                        return new TimeoutWanderTask();
                    }

                    return getEnderPearlTask(mod, enderPearlTarget);
                }
                setDebugState("TIMEOUT.. SHIT");

                return new TimeoutWanderTask();
            }
            case END -> throw new UnsupportedOperationException("You're in the end. Don't collect eyes here.");
        }
        return null;
    }

    //TODO move to ItemHelper
    public static int getCountWithCraftedFromOre(AltoClef mod, Item item) {
        if (item == Items.RAW_IRON) {
            int count = mod.getItemStorage().getItemCount(Items.RAW_IRON, Items.IRON_INGOT);
            count += mod.getItemStorage().getItemCount(Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.AXOLOTL_BUCKET, Items.POWDER_SNOW_BUCKET) * 3;
            count += hasItem(mod, Items.SHIELD) ? 1 : 0;
            count += hasItem(mod, Items.FLINT_AND_STEEL) ? 1 : 0;

            count += hasItem(mod, Items.IRON_SWORD) ? 2 : 0;
            count += hasItem(mod, Items.IRON_PICKAXE) ? 3 : 0;

            count += hasItem(mod, Items.IRON_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.IRON_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.IRON_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.IRON_BOOTS) ? 4 : 0;

            return count;
        } else if (item == Items.RAW_GOLD) {
            int count = mod.getItemStorage().getItemCount(Items.RAW_GOLD, Items.GOLD_INGOT);
            count += hasItem(mod, Items.GOLDEN_PICKAXE) ? 3 : 0;

            count += hasItem(mod, Items.GOLDEN_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.GOLDEN_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.GOLDEN_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.GOLDEN_BOOTS) ? 4 : 0;

            return count;
        } else if (item == Items.DIAMOND) {
            int count = mod.getItemStorage().getItemCount(Items.DIAMOND);
            count += hasItem(mod, Items.DIAMOND_SWORD) ? 2 : 0;
            count += hasItem(mod, Items.DIAMOND_PICKAXE) ? 3 : 0;

            count += hasItem(mod, Items.DIAMOND_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.DIAMOND_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.DIAMOND_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.DIAMOND_BOOTS) ? 4 : 0;

            return count;
        }

        throw new IllegalStateException("Invalid ore item: " + item);
    }


}
