package adris.altoclef.util;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.trackers.blacklisting.WorldLocateBlacklist;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.function.Predicate;

public class BlockScanner {

    private static boolean LOG = false;
    private static final int RESCAN_TICK_DELAY = 4 * 20;
    private static final int CACHED_POSITIONS_PER_BLOCK = 40;


    private final AltoClef mod;
    private final HashMap<ChunkPos, Long> scannedChunks = new HashMap<>();
    private final TimerGame rescanTimer = new TimerGame(1);
    private final HashMap<Block, HashSet<BlockPos>> cachedCloseBlocks = new HashMap<>();
    private final HashMap<Block, HashSet<BlockPos>> newBlocks = new HashMap<>();
    private final WorldLocateBlacklist blacklist = new WorldLocateBlacklist();
    private HashMap<Block, HashSet<BlockPos>> cachedBlocks = new HashMap<>();
    private boolean scanning = false;
    private boolean forceStop = false;
    private Dimension scanDimension = Dimension.OVERWORLD;
    private World scanWorld = null;


    public BlockScanner(AltoClef mod) {
        this.mod = mod;

        EventBus.subscribe(BlockPlaceEvent.class, evt -> addBlock(evt.blockState.getBlock(), evt.blockPos));
    }


    public void addBlock(Block block, BlockPos pos) {
        if (!isBlockAtPosition(pos, block)) {
            Debug.logInternal("INVALID SET: " + block + " " + pos);
            return;
        }

        if (cachedCloseBlocks.containsKey(block)) {
            cachedCloseBlocks.get(block).add(pos);
        } else {
            HashSet<BlockPos> set = new HashSet<>();
            set.add(pos);

            cachedCloseBlocks.put(block, set);
        }
    }


    public void requestBlockUnreachable(BlockPos pos, int allowedFailures) {
        blacklist.blackListItem(mod, pos, allowedFailures);
    }

    //TODO replace four with config
    public void requestBlockUnreachable(BlockPos pos) {
        blacklist.blackListItem(mod, pos, 4);
    }


    public boolean isUnreachable(BlockPos pos) {
        return blacklist.unreachable(pos);
    }

    public List<BlockPos> getKnownLocations(Block... blocks) {
        List<BlockPos> locations = new LinkedList<>();

        for (Block block : blocks) {
            if (!cachedCloseBlocks.containsKey(block)) continue;

            locations.addAll(cachedCloseBlocks.get(block));
        }
        locations.removeIf(this::isUnreachable);

        return locations;
    }

    /**
     * Scans a radius for the closest block of a given type .
     *
     * @param pos    The center of this radius
     * @param range  Radius to scan for
     * @param blocks What blocks to check for
     */
    public Optional<BlockPos> getNearestWithinRange(Vec3d pos, double range, Block... blocks) {
        Optional<BlockPos> nearest = getNearestBlock(pos, blocks);

        if (nearest.isEmpty() || nearest.get().isWithinDistance(pos, range)) return nearest;

        return Optional.empty();
    }

    public Optional<BlockPos> getNearestWithinRange(BlockPos pos, double range, Block... blocks) {
        return getNearestWithinRange(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), range, blocks);
    }


    public boolean anyFound(Block... blocks) {
        return anyFound((block) -> true, blocks);
    }


    public boolean anyFound(Predicate<BlockPos> isValidTest, Block... blocks) {
        for (Block block : blocks) {
            if (!cachedCloseBlocks.containsKey(block)) continue;

            for (BlockPos pos : cachedCloseBlocks.get(block)) {
                if (isValidTest.test(pos) && mod.getWorld().getBlockState(pos).getBlock().equals(block) && !this.isUnreachable(pos))
                    return true;
            }
        }

        return false;
    }

    public Optional<BlockPos> getNearestBlock(Block... blocks) {
        // Add juuust a little, to prevent digging down all the time/bias towards blocks BELOW the player
        return getNearestBlock(mod.getPlayer().getPos().add(0, 0.6f, 0), blocks);
    }

    public Optional<BlockPos> getNearestBlock(Vec3d pos, Block... blocks) {
        return getNearestBlock(pos, p -> true, blocks);
    }

    public Optional<BlockPos> getNearestBlock(Predicate<BlockPos> isValidTest, Block... blocks) {
        return getNearestBlock(mod.getPlayer().getPos().add(0, 0.6f, 0), isValidTest, blocks);
    }

    public Optional<BlockPos> getNearestBlock(Vec3d pos, Predicate<BlockPos> isValidTest, Block... blocks) {
        Optional<BlockPos> closest = Optional.empty();

        for (Block block : blocks) {
            Optional<BlockPos> p = getNearestBlock(block, isValidTest, pos);

            if (p.isPresent()) {
                if (closest.isEmpty()) closest = p;
                else {
                    if (BaritoneHelper.calculateGenericHeuristic(pos, WorldHelper.toVec3d(closest.get())) > BaritoneHelper.calculateGenericHeuristic(pos, WorldHelper.toVec3d(p.get()))) {
                        closest = p;
                    }
                }
            }
        }

        return closest;
    }

    public Optional<BlockPos> getNearestBlock(Block block, Vec3d fromPos) {
        return getNearestBlock(block, (pos) -> true, fromPos);
    }

    public Optional<BlockPos> getNearestBlock(Block block, Predicate<BlockPos> isValidTest, Vec3d fromPos) {
        BlockPos pos = null;
        double nearest = Double.POSITIVE_INFINITY;

        if (!cachedCloseBlocks.containsKey(block)) {
            return Optional.empty();
        }

        for (BlockPos p : cachedCloseBlocks.get(block)) {
            //ensure the block is there (can change upon rescan)
            if (!mod.getWorld().getBlockState(p).getBlock().equals(block)) continue;
            if (!isValidTest.test(p) || isUnreachable(p)) continue;

            double dist = BaritoneHelper.calculateGenericHeuristic(fromPos, WorldHelper.toVec3d(p));

            if (dist < nearest) {
                nearest = dist;
                pos = p;
            }
        }

        return pos != null ? Optional.of(pos) : Optional.empty();
    }

    public boolean anyFoundWithinDistance(double distance, Block... blocks) {
        return anyFoundWithinDistance(mod.getPlayer().getPos().add(0, 0.6f, 0), distance, blocks);
    }

    public boolean anyFoundWithinDistance(Vec3d pos, double distance, Block... blocks) {
        Optional<BlockPos> blockPos = getNearestBlock(blocks);
        return blockPos.map(value -> value.isWithinDistance(pos, distance)).orElse(false);
    }

    public double distanceToClosest(Block... blocks) {
        return distanceToClosest(mod.getPlayer().getPos().add(0, 0.6f, 0), blocks);
    }

    public double distanceToClosest(Vec3d pos, Block... blocks) {
        Optional<BlockPos> blockPos = getNearestBlock(blocks);
        return blockPos.map(value -> Math.sqrt(value.getSquaredDistance(pos))).orElse(Double.POSITIVE_INFINITY);
    }

    // Checks if 'pos' one of 'blocks' block
    // Returns false if incorrect or undetermined/unsure
    public boolean isBlockAtPosition(BlockPos pos, Block... blocks) {
        if (isUnreachable(pos)) {
            return false;
        }

        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            return false;
        }

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return false;
        }
        try {
            for (Block block : blocks) {
                if (world.isAir(pos) && WorldHelper.isAir(block)) {
                    return true;
                }
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() == block) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            // Probably out of chunk. This means we can't judge its state.
            return false;
        }
    }

    public void reset() {
        cachedCloseBlocks.clear();
        newBlocks.clear();
        scannedChunks.clear();
        rescanTimer.forceElapse();
        blacklist.clear();
        forceStop = true;
    }

    public void tick() {
        if (mod.getWorld() == null || mod.getPlayer() == null) return;
        //be maximally aware of the closest blocks around you
        scanCloseBlocks();
        if (!rescanTimer.elapsed() || scanning) return;

        if (scanDimension != WorldHelper.getCurrentDimension() || mod.getWorld() != scanWorld) {
            if (LOG) {
                Debug.logInternal("BlockScanner: new dimension or world detected, resetting data!");
            }
            reset();
            scanWorld = mod.getWorld();
            scanDimension = WorldHelper.getCurrentDimension();
        }

        cachedBlocks = new HashMap<>(newBlocks.size());
        for (Map.Entry<Block, HashSet<BlockPos>> entry : newBlocks.entrySet()) {
            cachedBlocks.put(entry.getKey(), (HashSet<BlockPos>) entry.getValue().clone());
        }

        if (LOG) {
            Debug.logInternal("Updating BlockScanner.. size: " + cachedCloseBlocks.size() + " : " + cachedBlocks.size());
        }

        scanning = true;
        forceStop = false;
        new Thread(() -> {
            try {
                rescan(Integer.MAX_VALUE, Integer.MAX_VALUE);
            } catch (Exception e) {
                scanning = false;
                rescanTimer.reset();
                e.printStackTrace();
            }
        }).start();
    }

    private void scanCloseBlocks() {
        for (Map.Entry<Block, HashSet<BlockPos>> entry : cachedBlocks.entrySet()) {
            Block block = entry.getKey();
            HashSet<BlockPos> blockPositions = entry.getValue();

            cachedCloseBlocks.computeIfAbsent(block, k -> new HashSet<>()).clear();
            cachedCloseBlocks.get(block).addAll(blockPositions);
        }

        final BlockPos pos = mod.getPlayer().getBlockPos();
        final World world = mod.getPlayer().getWorld();

        final int startX = pos.getX() - 8;
        final int endX = pos.getX() + 8;
        final int startY = pos.getY() - 8;
        final int endY = pos.getY() + 8;
        final int startZ = pos.getZ() - 8;
        final int endZ = pos.getZ() + 8;

        BlockPos.Mutable p = new BlockPos.Mutable();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    p.set(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    cachedCloseBlocks.computeIfAbsent(block, k -> new HashSet<>()).add(p.toImmutable());
                }
            }
        }
    }

    private void rescan(int maxCount, int cutOffRadius) {
        final long ms = System.currentTimeMillis();

        final World world = mod.getWorld();
        final PlayerEntity player = mod.getPlayer();
        final ChunkPos playerChunkPos = player.getChunkPos();
        final Vec3d playerPos = player.getPos();
        final ClientChunkManager chunkManager = mod.getWorld().getChunkManager();


        final HashSet<ChunkPos> visited = new HashSet<>();
        final Queue<Node> queue = new ArrayDeque<>();
        queue.add(new Node(playerChunkPos, 0));

        while (!queue.isEmpty() && visited.size() < maxCount && !forceStop) {
            final Node node = queue.poll();
            final ChunkPos nodePos = node.pos;

            if (node.distance > cutOffRadius || visited.contains(nodePos) || !chunkManager.isChunkLoaded(nodePos.x, nodePos.z)) {
                continue;
            }

            final boolean isPriorityChunk = getChunkDist(nodePos, playerChunkPos) <= 2;
            final long currentTime = world.getTime();
            final Long lastScanTime = scannedChunks.get(nodePos);
            final boolean shouldRescan = lastScanTime == null || currentTime - lastScanTime >= RESCAN_TICK_DELAY;

            if (!isPriorityChunk && lastScanTime != null && !shouldRescan) {
                continue;
            }

            visited.add(nodePos);
            scanChunk(nodePos, playerChunkPos);

            queue.add(new Node(new ChunkPos(nodePos.x + 1, nodePos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(nodePos.x - 1, nodePos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(nodePos.x - 1, nodePos.z - 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(nodePos.x + 1, nodePos.z - 1), node.distance + 1));
        }

        if (forceStop) {
            reset();
            forceStop = false;
            return;
        }

        for (Iterator<ChunkPos> iterator = scannedChunks.keySet().iterator(); iterator.hasNext(); ) {
            ChunkPos pos = iterator.next();
            final int distance = getChunkDist(pos, playerChunkPos);

            if (distance > cutOffRadius) {
                iterator.remove();
            }
        }

        for (HashSet<BlockPos> set : newBlocks.values()) {
            if (set.size() >= CACHED_POSITIONS_PER_BLOCK) {
                getFirstFewPositions(set, playerPos);
            }
        }

        if (LOG) {
            Debug.logInternal("Rescanned in: " + (System.currentTimeMillis() - ms) + " ms; visited: " + visited.size() + " chunks");
        }

        rescanTimer.reset();
        scanning = false;
    }

    private int getChunkDist(ChunkPos pos1, ChunkPos pos2) {
        return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.z - pos2.z);
    }


    //TODO rename
    private void getFirstFewPositions(HashSet<BlockPos> set, Vec3d playerPos) {
        Queue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingDouble((pos) -> -BaritoneHelper.calculateGenericHeuristic(playerPos, WorldHelper.toVec3d(pos))));

        for (BlockPos pos : set) {
            queue.add(pos);

            if (queue.size() > CACHED_POSITIONS_PER_BLOCK) {
                queue.poll();
            }
        }

        set.clear();

        for (int i = 0; i < CACHED_POSITIONS_PER_BLOCK; i++) {
            set.add(queue.poll());
        }
    }

    /**
     * scans a chunk and adds block positions corresponding to a specific block in a list
     *
     * @param chunkPos position of the scanned chunk
     */
    private void scanChunk(ChunkPos chunkPos, ChunkPos playerChunkPos) {
        final World world = mod.getWorld();
        final WorldChunk chunk = mod.getWorld().getChunk(chunkPos.x, chunkPos.z);
        scannedChunks.put(chunkPos, world.getTime());

        final boolean isPriorityChunk = getChunkDist(chunkPos, playerChunkPos) <= 2;

        final int startX = chunkPos.getStartX();
        final int endX = chunkPos.getEndX();
        final int startZ = chunkPos.getStartZ();
        final int endZ = chunkPos.getEndZ();
        final int bottomY = world.getBottomY();
        final int topY = world.getTopY();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = startX; x <= endX; x++) {
            for (int y = bottomY; y < topY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    pos.set(x, y, z);
                    if (this.isUnreachable(pos) || world.isOutOfHeightLimit(pos)) continue;

                    BlockState state = chunk.getBlockState(pos);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    HashSet<BlockPos> set = newBlocks.get(block);

                    if (set != null) {
                        if ((set.size() > CACHED_POSITIONS_PER_BLOCK * 750 && !isPriorityChunk)) continue;
                        set.add(pos.toImmutable());
                    } else {
                        set = new HashSet<>();
                        set.add(pos.toImmutable());
                        newBlocks.put(block, set);
                    }
                }
            }
        }
    }

    private record Node(ChunkPos pos, int distance) {
    }


}
