package adris.altoclef.chains;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import adris.altoclef.control.LookAtPos;
import adris.altoclef.multiversion.ItemVer;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.control.KillAura;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.Baritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import adris.altoclef.util.Weapons;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


// TODO: Optimise shielding against spiders and skeletons

public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 5.5;
    private static final double CREEPER_KEEP_DISTANCE = 15;
    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2;
    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10;
    private static final double SAFE_KEEP_DISTANCE = 10;
    private static boolean shielding = false;
    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();
    private final KillAura killAura = new KillAura();
    private final HashMap<Entity, TimerGame> closeAnnoyingEntities = new HashMap<>();
    private Entity targetEntity;
    private static boolean doingFunkyStuff = false;
    private boolean wasPuttingOutFire = false;
    private CustomBaritoneGoalTask runAwayTask;
    private double prevHealth = 20;
    private List<Entity> dealWithSupply;
    private Supplier<List<Entity>> dealWithSupplier = this::dealWithSupplyGet;

    //FIXME: Think some lag is occurring because of this new runAwayEntities task

    private double dangerKeepDistanceAdjusted = DANGER_KEEP_DISTANCE;
    private boolean evadingHostilesLastTick = false;
    private float cachedLastPriority;

    private RunAwayFromEntitiesTask runAwayHostilesCustom;

    public List<Entity> dealWithSupplyGet() {
        return dealWithSupply;
    }

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
        double distance = creeper.squaredDistanceTo(pos);
        float fuse = creeper.getClientFuseTime(1);

        // Not fusing.
        if (fuse <= 0.001f) return distance;
        return distance * 0.2; // less is WORSE
    }


    private static void startShielding(AltoClef mod) {
        shielding = true;
        mod.getClientBaritone().getPathingBehavior().requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                for (ItemStack spaceSlot : spaceSlots) {
                    if (spaceSlot.isEmpty()) {
                        mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.QUICK_MOVE);
                        return;
                    }
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
    }

    @Override
    public float getPriority(AltoClef mod) {
        cachedLastPriority = StepDefense(mod);
        prevHealth = mod.getPlayer().getHealth();

        if (runAwayHostilesCustom == null) {
            runAwayHostilesCustom = new RunAwayFromEntitiesTask(dealWithSupplier, SAFE_KEEP_DISTANCE * 1.5, 1) {
                @Override
                protected boolean isEqual(Task other) {
                    return other instanceof RunAwayFromEntitiesTask;
                }
            };
        }

        return cachedLastPriority;
    }

    private void stopShielding(AltoClef mod) {
        if (shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (ItemVer.isFood(cursor)) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            shielding = false;
        }
    }

    public boolean isShielding() {
        return shielding || killAura.isShielding();
    }

    private static boolean willBlockedByShield(@NotNull LivingEntity entity, @Nullable Vec3d pos) {
        if (entity.isBlocking()) {
            if (pos != null) {
                Vec3d vec3d = entity.getRotationVec(1.0F);
                Vec3d vec3d2 = pos.relativize(entity.getPos()).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0, vec3d2.z);
                return vec3d2.dotProduct(vec3d) < 0.0;
            }
        }
        return false;
    }

    private static int getBlockingProcess(PlayerEntity player, Boolean ignored) {
        if (player.isUsingItem() && !player.getActiveItem().isEmpty()) {
            Item activeItem = player.getActiveItem().getItem();
            if (ignored) {
                return 6;
            } else if (activeItem.getUseAction(player.getActiveItem()) != UseAction.BLOCK) {
                return 0;
            } else {
                return Math.min(activeItem.getMaxUseTime(player.getActiveItem()) - player.getItemUseTimeLeft(), 6);
            }
        }
        return 0;
    }

    private boolean escapeDragonBreath(AltoClef mod) {
        dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
            if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                return true;
            }
        }
        return false;
    }

    public float StepDefense(AltoClef mod) //Return priority of defense actions
    {
        // Voids
        if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;
        if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) return Float.NEGATIVE_INFINITY;
        if (!mod.getModSettings().isMobDefense()) return Float.NEGATIVE_INFINITY;

        // Pause if we're not loaded into a world.
        if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;

        // Put out fire if we're standing on one like an idiot
        BlockPos fireBlock = isInsideFireAndOnFire(mod);
        if (fireBlock != null) {
            putOutFire(mod, fireBlock);
            wasPuttingOutFire = true;
        } else {
            // Stop putting stuff out if we no longer need to put out a fire.
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
            wasPuttingOutFire = false;
        }

        ClientPlayerEntity Player = mod.getPlayer();
        dangerKeepDistanceAdjusted = DANGER_KEEP_DISTANCE + (1 - (Player.getHealth() / Player.getMaxHealth())) * 10;

        if (Player.isTouchingWater()) {
            dangerKeepDistanceAdjusted *= 0.5;
        }

        int avoidanceRadius = (int) (dangerKeepDistanceAdjusted + 1);
        mod.HostileAvoidanceRadius = avoidanceRadius;
        mod.getClientBaritoneSettings().mobAvoidanceRadius.value = avoidanceRadius;

        //Variables to update on step
        Weapons.Weapon BestWeapon = Weapons.getBestWeapon(mod);
        float BestDamage = Weapons.getBestDamage(BestWeapon, mod);

        // No idea
        doingFunkyStuff = false;
        PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
        Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();

        // Run away from creepers
        CreeperEntity blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp != null) {
            if ((!mod.getFoodChain().needsToEat() || Player.getHealth() < 9) && (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) && !mod.getEntityTracker().entityFound(PotionEntity.class) && !Player.getItemCooldownManager().isCoolingDown(offhandItem) && mod.getClientBaritone().getPathingBehavior().isSafeToCancel() && blowingUp.getClientFuseTime(blowingUp.getFuseSpeed()) > 0.5) {
                LookHelper.lookAt(mod, blowingUp.getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                doingFunkyStuff = true;
                runAwayTask = new RunAwayFromHostilesTask(CREEPER_KEEP_DISTANCE);
                setTask(runAwayTask);
                return 50 + blowingUp.getClientFuseTime(1) * 50;
            }
        }

        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            // Block projectiles with shield
            if (mod.getModSettings().isDodgeProjectiles() && (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) && !Player.getItemCooldownManager().isCoolingDown(offhandItem) && mod.getClientBaritone().getPathingBehavior().isSafeToCancel() && !mod.getEntityTracker().entityFound(PotionEntity.class) && isProjectileClose(mod)) {
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
                return 60;
            }
            if (blowingUp == null && !isProjectileClose(mod)) {
                stopShielding(mod);
            }

            // Deal with close cave spiders before others as they jump at the player and give poison.
            Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(e -> {
                if (e instanceof CaveSpiderEntity sEntity) {
                    return sEntity.distanceTo(Player) < 5 && EntityHelper.isAngryAtPlayer(mod, sEntity) && LookHelper.seesPlayer(sEntity, Player, SAFE_KEEP_DISTANCE);
                }
                return false;
            }, SpiderEntity.class, CaveSpiderEntity.class);
            if (entity.isPresent() && !WorldHelper.isSurroundedByHostiles(mod) && mod.getItemStorage().hasItem(Items.SHIELD) && mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) {
                if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                    LookHelper.lookAt(mod, entity.get().getEyePos());
                    startShielding(mod);
                    doForceField(mod);
                }
                return 60;
            }
        }

        if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFalling(mod) || !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting()) {
            killAura.stopShielding(mod);
            stopShielding(mod);
            return Float.NEGATIVE_INFINITY;
        }

        escapeDragonBreath(mod);

        // Dodge projectiles, if no shield. Or block.
        if (Player.getHealth() <= 10 && (!mod.getItemStorage().hasItem(Items.SHIELD) && !mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) && mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod)) {
            if (StorageHelper.getNumberOfThrowawayBlocks(mod) > 2 && !mod.getFoodChain().needsToEat()) {
                doingFunkyStuff = true;
                setTask(new ProjectileProtectionWallTask(mod));
                return 70;
            }

            runAwayTask = new DodgeProjectilesTask(ARROW_KEEP_DISTANCE_HORIZONTAL, ARROW_KEEP_DISTANCE_VERTICAL);
            setTask(runAwayTask);
            return 67;
        }

        // Kill mobs, if settings enabled of course.
        if (mod.getModSettings().shouldDealWithAnnoyingHostiles()) {
            // First list the hostiles.

            // Deal with hostiles because they are annoying.
            List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();
            hostiles.sort(Comparator.comparingDouble(Player::distanceTo));

            List<Entity> toDealWith = new ArrayList<>();

            // Make sure its correct + add extra things
            if (!hostiles.isEmpty()) {
                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity hostile : hostiles) {
                        boolean isRangedOrPoisnous = (hostile instanceof SkeletonEntity || hostile instanceof WitchEntity || hostile instanceof PillagerEntity || hostile instanceof PiglinEntity || hostile instanceof StrayEntity || hostile instanceof CaveSpiderEntity);
                        double annoyingRange = dangerKeepDistanceAdjusted;
                        if (isRangedOrPoisnous && !mod.getItemStorage().hasItem(Items.SHIELD) && !mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
                            annoyingRange = dangerKeepDistanceAdjusted * 1.5;
                        else if (isRangedOrPoisnous) annoyingRange = dangerKeepDistanceAdjusted * 1.25;

                        // Give each hostile a timer, if they're close for too long deal with them.
                        if (hostile.isInRange(Player, annoyingRange) && LookHelper.seesPlayer(hostile, Player, annoyingRange)) {
                            if (!closeAnnoyingEntities.containsKey(hostile)) {
                                boolean wardenAttacking = hostile instanceof WardenEntity;
                                boolean witherAttacking = hostile instanceof WitherEntity;
                                boolean endermanAttacking = hostile instanceof EndermanEntity;
                                boolean blazeAttacking = hostile instanceof BlazeEntity;
                                boolean witherSkeletonAttacking = hostile instanceof WitherSkeletonEntity;
                                boolean hoglinAttacking = hostile instanceof HoglinEntity;
                                boolean zoglinAttacking = hostile instanceof ZoglinEntity;
                                boolean piglinBruteAttacking = hostile instanceof PiglinBruteEntity;
                                boolean vindicatorAttacking = hostile instanceof VindicatorEntity;
                                if (blazeAttacking || witherSkeletonAttacking || hoglinAttacking || zoglinAttacking || piglinBruteAttacking || endermanAttacking || witherAttacking || wardenAttacking || vindicatorAttacking) {

                                    if (Player.getHealth() <= 10) {
                                        closeAnnoyingEntities.put(hostile, new TimerGame(0));
                                    } else {
                                        closeAnnoyingEntities.put(hostile, new TimerGame(Float.POSITIVE_INFINITY));
                                    }
                                } else {
                                    closeAnnoyingEntities.put(hostile, new TimerGame(0));
                                }
                                closeAnnoyingEntities.get(hostile).reset();
                            }
                            if (closeAnnoyingEntities.get(hostile).elapsed()) {
                                toDealWith.add(hostile);
                            }
                        } else {
                            closeAnnoyingEntities.remove(hostile);
                        }
                    }
                }
            }


            // Clear some enemies that can't attack.
            List<Entity> toRemove = new ArrayList<>();
            for (Entity check : closeAnnoyingEntities.keySet()) {
                if (!check.isAlive() || !Player.canSee(check)) {
                    toRemove.add(check);
                }
            }
            if (!toRemove.isEmpty()) {
                for (Entity remove : toRemove) closeAnnoyingEntities.remove(remove);
            }

            // Count a score based upon the hostiles we have.
            int entityScore = getEntityscore(toDealWith, Player);
            if (entityScore > 0) //Then we fight!
            {
                // Depending on our weapons/armor, we may choose to straight up kill hostiles if we're not dodging their arrows.
                // we're not dodging their arrows.

                // wood 0 : 1 skeleton
                // stone 1 : 1 skeleton
                // iron 2 : 2 hostiles
                // diamond 3 : 3 hostiles
                // netherite 4 : 4 hostiles

                // Armor: (do the math I'm not boutta calculate this)
                // leather: ?1 skeleton
                // iron: ?2 hostiles
                // diamond: ?3 hostiles

                // 7 is full set of leather
                // 15 is full set of iron.
                // 20 is full set of diamond.
                // Diamond+netherite have bonus "toughness" parameter (we can simply add them I think, for now.)
                // full diamond has 8 bonus toughness
                // full netherite has 12 bonus toughness

                Entity closestOpponent = toDealWith.get(0);

                AtomicBoolean RangedPresent = new AtomicBoolean(false);
                toDealWith.forEach(entity -> {
                    if (entity instanceof SkeletonEntity && mod.getPlayer().canSee(entity)) {
                        if (((MobEntity) entity).getEquippedStack(EquipmentSlot.MAINHAND).getItem() == Items.BOW) {
                            if (entity.isSubmergedInWater() && !mod.getPlayer().isSubmergedInWater()) {
                                return;
                            }
                            RangedPresent.set(true);
                        }
                    } else if (entity instanceof WitchEntity) { // Take no chances
                        RangedPresent.set(true);
                    } else if (entity instanceof DrownedEntity) {
                        if (((MobEntity) entity).getEquippedStack(EquipmentSlot.MAINHAND).getItem() == Items.TRIDENT) { // Can easily kill us
                            RangedPresent.set(true);
                        }
                    }
                });

                // Calculating canDealWith
                int canDealWith;
                {
                    boolean hasShield = mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
                    double shield = 0;
                    if (hasShield) {
                        // We will need a shield more with skeletons with bows
                        if (RangedPresent.get()) {
                            shield = 3.25;
                        } else {
                            shield = 2.05;
                        }
                    }

                    float damage = Player.getMaxHealth() - Player.getHealth();
                    int armor = Player.getArmor();
                    float weaponDamage = BestWeapon.WeaponItem == null ? 0 : (1 + BestDamage);
                    canDealWith = (int) Math.ceil(((double) armor / 4) + (weaponDamage * 2.15) + shield);
                    canDealWith -= (int) Math.floor(damage * 0.125);
                    if (mod.getPlayer().isSubmergedInWater()) {
                        canDealWith -= 1;
                    }
                }

                // Debug
                // System.out.println("candealwith: " + canDealWith);
                // System.out.println("entityscore: " + entityscore);

                // Distance we fight or we run from
                final double distanceDefense = dangerKeepDistanceAdjusted * (RangedPresent.get() ? 2.35 : 1.15);

                // This is self-defense, so only defend if in range.
                if (closestOpponent.getPos().isInRange(mod.getPlayer().getPos(), distanceDefense)) {
                    // Determine if we can fight them or we have to run away.
                    if (
                            (canDealWith > entityScore && entityScore < 12 && Player.getHealth() > 7) &&
                                    (!evadingHostilesLastTick) &&
                                    (!closestOpponent.isSubmergedInWater()) // Because baritone can't path-find to enemy.
                    ) {
                        // We can deal with it; Fight.
                        runAwayTask = null;
                        setTask(new KillEntitiesTask(closestOpponent.getClass()));
                        return 65;
                    } else {
                        // We can't deal with it; Flight.
                        evadingHostilesLastTick = true;

                        dealWithSupply = toDealWith;
                        runAwayTask = runAwayHostilesCustom;
                        runAwayHostilesCustom.updateDistance(distanceDefense * 1.5);

                        setTask(runAwayTask);
                        doForceField(mod); // To protect ourselves as we escape.
                        return 75;
                    }
                }
            } else {
                if (shielding) {
                    stopShielding(mod);
                }
            }
        }

        evadingHostilesLastTick = false;

        // By default, if we aren't "immediately" in danger but were running away, keep
        // running away until we're good.
        if (runAwayTask != null && !runAwayTask.isFinished(mod)) {
            setTask(runAwayTask);
            return cachedLastPriority;
        } else {
            runAwayTask = null;
        }

        doForceField(mod); // Last protection measure (this seems important though)

        return 0;
    }

    private static int getEntityscore(List<Entity> toDealWith, ClientPlayerEntity player) {
        int entityscore = toDealWith.size();
        if (!toDealWith.isEmpty()) {
            for (Entity ToDealWith : toDealWith) {
                if (ToDealWith.getClass() == SlimeEntity.class || ToDealWith.getClass() == MagmaCubeEntity.class || !(ToDealWith instanceof SkeletonEntity) && !(ToDealWith instanceof EndermanEntity) && !(ToDealWith instanceof DrownedEntity)) {
                    // Entities that have a sword or can split into more entities after being killed count as two entities as they are more dangerous then one entity of same type
                    entityscore += 2;
                } else if (ToDealWith instanceof SkeletonEntity && ((SkeletonEntity) ToDealWith).getHandItems() == Items.BOW) {
                    // Any skeleton with a bow is REALLY dangerous so we'll count them as 5 entities
                    entityscore += 5;
                } else if (ToDealWith instanceof EndermanEntity) {
                    // Enderman can be also really dangerous as they hit hard.
                    entityscore += 3;
                } else if (ToDealWith instanceof DrownedEntity && ((DrownedEntity) ToDealWith).getHandItems() == Items.TRIDENT) {
                    // Drowned with tridents are also REALLY dangerous, maybe we should increase this??
                    entityscore += 5;
                }
            }
        }
        return entityscore;
    }

    private BlockPos isInsideFireAndOnFire(AltoClef mod) {
        boolean onFire = mod.getPlayer().isOnFire();
        if (!onFire) return null;
        BlockPos p = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = new BlockPos[]{p, p.add(1, 0, 0), p.add(1, 0, -1), p.add(0, 0, -1), p.add(-1, 0, -1), p.add(-1, 0, 0), p.add(-1, 0, 1), p.add(0, 0, 1), p.add(1, 0, 1)};
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof AbstractFireBlock) {
                return check;
            }
        }
        return null;
    }

    private void putOutFire(AltoClef mod, BlockPos pos) {
        Optional<Rotation> reach = LookHelper.getReach(pos);
        if (reach.isPresent()) {
            Baritone b = mod.getClientBaritone();
            if (LookHelper.isLookingAt(mod, pos)) {
                b.getPathingBehavior().requestPause();
                b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                return;
            }
            LookHelper.lookAt(mod, reach.get());
        }
    }

    private void doForceField(AltoClef mod) {
        killAura.tickStart(mod);

        // Hit all hostiles close to us.
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            for (Entity entity : entities) {
                boolean shouldForce = false;
                if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
                if (entity instanceof MobEntity) {
                    if (EntityHelper.isProbablyHostileToPlayer(mod, entity) || EntityHelper.isAngryAtPlayer(mod, entity)) {
                        if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10)) {
                            shouldForce = true;
                        }
                    }
                } else if (entity instanceof FireballEntity) {
                    // Ghast ball
                    shouldForce = true;
                }

                if (shouldForce) {
                    killAura.applyAura(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        killAura.tickEnd(mod);
    }


    private CreeperEntity getClosestFusingCreeper(AltoClef mod) {
        double worstSafety = Float.POSITIVE_INFINITY;
        CreeperEntity target = null;
        try {
            List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
            for (CreeperEntity creeper : creepers) {
                if (creeper == null) continue;
                if (creeper.getClientFuseTime(1) < 0.001) continue;

                // We want to pick the closest creeper, but FIRST pick creepers about to blow
                // At max fuse, the cost goes to basically zero.
                double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
                if (safety < worstSafety) {
                    target = creeper;
                }
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            // IDK why but these exceptions happen sometimes. It's extremely bizarre and I
            // have no idea why.
            Debug.logWarning("Weird Exception caught and ignored while scanning for creepers: " + e.getMessage());
            return target;
        }
        return target;
    }

    private boolean isProjectileClose(AltoClef mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();

        try {
            for (CachedProjectile projectile : projectiles) {
                if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150) {
                    boolean isGhastBall = projectile.projectileType == FireballEntity.class;
                    if (isGhastBall) {
                        Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(FireballEntity.class);
                        Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(GhastEntity.class);
                        if (ghastBall.isPresent() && ghast.isPresent() && runAwayTask == null
                                && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, ghast.get().getEyePos());
                        }
                        return false;
                        // Ignore ghast balls
                    }
                    if (projectile.projectileType == DragonFireballEntity.class) {
                        // Ignore dragon fireballs
                        continue;
                    }
                    if (projectile.projectileType == ArrowEntity.class || projectile.projectileType == SpectralArrowEntity.class || projectile.projectileType == SmallFireballEntity.class) {
                        // check if the projectile is going away from us
                        // not so fancy math... this should work better than the previous approach (I hope just adding the velocity doesn't cause any issues..)
                        PlayerEntity player = mod.getPlayer();
                        if (player.squaredDistanceTo(projectile.position) < player.squaredDistanceTo(projectile.position.add(projectile.velocity))) {
                            continue;
                        }
                        if (projectile.velocity.length() < 0.2) {
                            continue;
                        }
                    }

                    Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

                    Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);

                    double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                    double verticalDistance = Math.abs(delta.y);
                    if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL
                            && verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL) {
                        if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
                                && (mod.getItemStorage().hasItem(Items.SHIELD)
                                || mod.getItemStorage().hasItemInOffhand(Items.SHIELD))) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, projectile.position.add(0, 0.3, 0));
                        }
                        return true;
                    }
                }
            }

        } catch (ConcurrentModificationException e) {
            Debug.logWarning(e.getMessage());
        }

        // TODO: Replace this with something better, because it may lead to not defending against other close range mobs.
        // TODO refactor this into something more reliable for all mobs
        for (SkeletonEntity skeleton : mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class)) {
            if (skeleton.distanceTo(mod.getPlayer()) > 20 || !skeleton.canSee(mod.getPlayer())) continue;

            // when the skeleton is about to shoot (it takes 5 ticks to raise the shield)
            if (skeleton.getItemUseTime() > 15) {
                return true;
            }
        }

        return false;
    }

    private Optional<Entity> getUniversallyDangerousMob(AltoClef mod) {
        // Wither skeletons are dangerous because of the wither effect. Oof kinda obvious.
        // If we merely force field them, we will run into them and get the wither effect which will kill us.

        Class<?>[] dangerousMobs = new Class[]{WardenEntity.class, WitherEntity.class, WitherSkeletonEntity.class,
                HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class};

        double range = SAFE_KEEP_DISTANCE - 2;

        for (Class<?> dangerous : dangerousMobs) {
            Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(dangerous);

            if (entity.isPresent()) {
                if (entity.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, entity.get())) {
                    return entity;
                }
            }
        }

        return Optional.empty();
    }

    private boolean isInDanger(AltoClef mod) {
        boolean witchNearby = mod.getEntityTracker().entityFound(WitchEntity.class);

        float health = mod.getPlayer().getHealth();
        if (health <= 10 && !witchNearby) {
            return true;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER) ||
                (mod.getPlayer().hasStatusEffect(StatusEffects.POISON) && !witchNearby)) {
            return true;
        }
        if (WorldHelper.isVulnerable(mod)) {
            // If hostile mobs are nearby...
            try {
                ClientPlayerEntity player = mod.getPlayer();
                List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();

                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity entity : hostiles) {
                        if (entity.isInRange(player, SAFE_KEEP_DISTANCE)
                                && !mod.getBehaviour().shouldExcludeFromForcefield(entity)
                                && EntityHelper.isAngryAtPlayer(mod, entity)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Debug.logWarning("Weird multithreading exception. Will fix later. " + e.getMessage());
            }
        }
        return false;
    }

    public void setTargetEntity(Entity entity) {
        targetEntity = entity;
    }

    public void resetTargetEntity() {
        targetEntity = null;
    }

    public void setForceFieldRange(double range) {
        killAura.setRange(range);
    }

    public void resetForceField() {
        killAura.setRange(Double.POSITIVE_INFINITY);
    }

    public boolean isDoingAcrobatics() {
        return doingFunkyStuff;
    }

    public boolean isPuttingOutFire() {
        return wasPuttingOutFire;
    }

    @Override
    public boolean isActive() {
        // We're always checking for mobs
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // Task is done, so I guess we move on?
    }

    @Override
    public String getName() {
        return "Mob Defense";
    }
}