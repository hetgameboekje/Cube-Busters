package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Stationary defensive mob (see {@code newmechanics.md} "Turret" section for the design write-up and the
 * architecture decision - entity-based rather than block-entity/dispenser-based - this class is that decision).
 * <p>
 * Reuses vanilla {@link RangedAttackGoal} + {@link NearestAttackableTargetGoal} for targeting/combat rather than
 * any custom AI, per spec. What's custom here is everything vanilla's {@code RangedAttackMob} contract doesn't
 * cover on its own: immobility, a manual sentry on/off toggle, and a single-slot "ammo" mechanism that swaps
 * which projectile {@link #performRangedAttack} fires (vanilla {@code RangedAttackMob} only ever fires one kind
 * of projectile per mob).
 */
public class Turret extends PathfinderMob implements RangedAttackMob {
    private static final EntityDataAccessor<ItemStack> DATA_AMMO =
            SynchedEntityData.defineId(Turret.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(Turret.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SENTRY =
            SynchedEntityData.defineId(Turret.class, EntityDataSerializers.BOOLEAN);

    // Priority the attack goal is added/removed at when toggling sentry mode - see updateAttackGoal(). Value
    // itself doesn't matter beyond "lower than nothing else this mob has", there's only ever this one goal.
    private static final int ATTACK_GOAL_PRIORITY = 1;

    private final RangedAttackGoal attackGoal =
            new RangedAttackGoal(this, 1.0, Config.turretAttackIntervalTicks, (float) Config.turretRange);
    private boolean attackGoalAdded = false;

    public Turret(EntityType<? extends Turret> type, Level level) {
        super(type, level);
        // No wandering/wall-avoiding goals are ever added (see registerGoals()) so the vanilla PathfinderMob
        // navigation this constructor sets up is never actually asked to path anywhere by our own goals -
        // travel() below is the actual immobility guarantee, this is just "don't even try."
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Config.turretMaxHealth)
                .add(Attributes.FOLLOW_RANGE, Config.turretRange)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AMMO, ItemStack.EMPTY);
        builder.define(DATA_ACTIVE, true);
        builder.define(DATA_SENTRY, false);
    }

    @Override
    protected void registerGoals() {
        // Deliberately not calling super.registerGoals() - PathfinderMob/Mob's default registerGoals() is empty
        // at this level (subclasses add their own), so there's nothing to inherit, and skipping it avoids
        // future vanilla changes silently adding a movement goal underneath us.
        // Defensive mob: targets hostile mobs, never the player who placed it.
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        // Attack goal itself is added/removed at runtime by updateAttackGoal() depending on sentry/active state,
        // not unconditionally here - see that method and isActive().
        if (isActive()) {
            addAttackGoal();
        }
    }

    // --- Immobility -------------------------------------------------------------------------------------------
    // Decision (see newmechanics.md): rather than relying on "just never add a movement goal" (RangedAttackGoal
    // itself calls getNavigation().moveTo(...) when its target is out of attackRadius, which would otherwise
    // walk the turret toward the target), travel() is overridden to discard horizontal input outright. Gravity
    // still applies (so a turret placed on an unsupported block still falls to rest), and knockback still nudges
    // it slightly before friction reasserts itself next tick, but it never walks/paths anywhere. This is a
    // stronger guarantee than "no wander goal" and doesn't depend on every future goal staying well-behaved.
    @Override
    public void travel(Vec3 travelVector) {
        super.travel(Vec3.ZERO);
    }

    // --- Targeting / combat (vanilla goals, see registerGoals()) ------------------------------------------------

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack ammo = getAmmo();
        if (ammo.isEmpty()) {
            // Unloaded: aims (LookControl still runs via RangedAttackGoal) but never fires. Matches the
            // "dispenser-like item-based switch" framing from the spec - no ammo slotted, no shots.
            return;
        }
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            fireFirework(target);
        } else if (ammo.is(Items.FIRE_CHARGE)) {
            fireFireball(target);
        } else {
            // Thorn ammo (see ModItems.THORN_AMMO) and anything else not specifically handled above falls back
            // to a plain arrow shot - functionally the "thorn-arrow" projectile until the real Cactus Economy
            // thorns item/behavior lands (see class javadoc on ModItems.THORN_AMMO for that follow-up).
            fireArrow(target, ammo);
        }
        // Ammo is consumed 1-per-shot regardless of mode - infinite-ammo sentries aren't part of the spec.
        ammo.shrink(1);
        setAmmo(ammo);
    }

    private void fireArrow(LivingEntity target, ItemStack ammoStack) {
        Arrow arrow = new Arrow(this.level(), this, ammoStack.copyWithCount(1), null);
        aimAtTarget(arrow, target, Config.turretThornDamage);
        this.level().addFreshEntity(arrow);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 1.0F) + 0.2F);
    }

    private void aimAtTarget(AbstractArrow arrow, LivingEntity target, double damage) {
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontalDist * 0.2, dz, 1.6F, 4.0F);
        arrow.setBaseDamage(damage);
    }

    private void fireFireball(LivingEntity target) {
        Vec3 dir = new Vec3(
                target.getX() - this.getX(),
                target.getY(0.5) - this.getY(0.5),
                target.getZ() - this.getZ()
        ).normalize();
        SmallFireball fireball = new SmallFireball(this.level(), this, dir);
        fireball.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        this.level().addFreshEntity(fireball);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
    }

    private void fireFirework(LivingEntity target) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkRocketEntity firework =
                new FireworkRocketEntity(this.level(), this, this.getX(), this.getEyeY(), this.getZ(), rocket);
        Vec3 dir = new Vec3(
                target.getX() - this.getX(),
                target.getY(0.5) - this.getY(0.5),
                target.getZ() - this.getZ()
        ).normalize();
        firework.setDeltaMovement(dir.scale(1.2));
        this.level().addFreshEntity(firework);
        this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
    }

    // --- Ammo slot ------------------------------------------------------------------------------------------

    public ItemStack getAmmo() {
        return this.entityData.get(DATA_AMMO);
    }

    private void setAmmo(ItemStack stack) {
        this.entityData.set(DATA_AMMO, stack);
    }

    private static boolean isValidAmmo(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET) || stack.is(Items.FIRE_CHARGE) || stack.is(Items.ARROW)
                || stack.is(ModItems.THORN_AMMO.get());
    }

    // --- Sentry / manual toggle -------------------------------------------------------------------------------

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
        updateAttackGoal();
    }

    public boolean isSentryVariant() {
        return this.entityData.get(DATA_SENTRY);
    }

    public void setSentryVariant(boolean sentry) {
        this.entityData.set(DATA_SENTRY, sentry);
    }

    // Adds/removes the ranged attack goal from the goal selector so an "off" turret truly does nothing (doesn't
    // acquire targets, doesn't aim, doesn't fire) rather than just skipping the fire step - toggled by
    // right-clicking with an empty, non-sneaking hand (see mobInteract), or by a redstone signal (see
    // aiStep/isPowered) which forces the turret off regardless of its manually-set state.
    private void updateAttackGoal() {
        boolean shouldBeActive = isActive() && !isRedstonePowered();
        if (shouldBeActive && !attackGoalAdded) {
            addAttackGoal();
        } else if (!shouldBeActive && attackGoalAdded) {
            removeAttackGoal();
        }
    }

    private void addAttackGoal() {
        this.goalSelector.addGoal(ATTACK_GOAL_PRIORITY, attackGoal);
        attackGoalAdded = true;
    }

    private void removeAttackGoal() {
        this.goalSelector.removeGoal(attackGoal);
        this.setTarget(null);
        attackGoalAdded = false;
    }

    private boolean isRedstonePowered() {
        return this.level().hasNeighborSignal(this.blockPosition());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Redstone is polled rather than event-driven (no block at this position to receive a neighbor-changed
        // callback) - cheap enough at once/tick for a single BlockPos lookup, and keeps the "signal disables it"
        // behavior live without needing a companion block entity.
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            updateAttackGoal();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!this.level().isClientSide) {
            if (player.isSecondaryUseActive() && held.isEmpty()) {
                // Shift-right-click, empty hand: sentry on/off toggle.
                setActive(!isActive());
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                isActive() ? "message.cubebuster.turret_activated" : "message.cubebuster.turret_deactivated"),
                        true);
                return InteractionResult.CONSUME;
            }
            if (!held.isEmpty() && isValidAmmo(held) && getAmmo().isEmpty()) {
                // Load one item from the player's stack into the turret's single ammo slot.
                ItemStack toLoad = held.copyWithCount(1);
                setAmmo(toLoad);
                held.shrink(1);
                return InteractionResult.CONSUME;
            }
            if (held.isEmpty() && !getAmmo().isEmpty()) {
                // Empty hand, no toggle intent (not sneaking - handled above), some ammo loaded: withdraw it,
                // mirroring item frames/lecterns rather than dropping it on the ground.
                player.getInventory().placeItemBackInInventory(getAmmo());
                setAmmo(ItemStack.EMPTY);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                         MobSpawnType spawnReason, SpawnGroupData spawnGroupData) {
        // No equipment/gear randomization to do (turret carries no items in equipment slots, only the ammo
        // slot above) - still routes through super for vanilla bookkeeping (persistence flags etc).
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        // Placed deliberately by a player, like an armor stand - never vanilla-despawns.
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Active", isActive());
        tag.putBoolean("Sentry", isSentryVariant());
        if (!getAmmo().isEmpty()) {
            tag.put("Ammo", getAmmo().save(this.registryAccess(), new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Active")) {
            this.entityData.set(DATA_ACTIVE, tag.getBoolean("Active"));
        }
        if (tag.contains("Sentry")) {
            this.entityData.set(DATA_SENTRY, tag.getBoolean("Sentry"));
        }
        if (tag.contains("Ammo")) {
            ItemStack.parse(this.registryAccess(), tag.getCompound("Ammo")).ifPresent(this::setAmmo);
        }
        updateAttackGoal();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        // No-op - a stationary turret shouldn't be shoved around by mobs walking into it either.
    }
}
