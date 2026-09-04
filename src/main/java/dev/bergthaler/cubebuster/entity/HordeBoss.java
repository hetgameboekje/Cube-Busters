package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * The Horde Boss integration layer's mob: a recurring, tougher, tankier zombie variant force-spawned when a
 * player's (aggro score) x (mush block density) crosses the configured threshold - see
 * {@code event.HordeBossSpawnHandler}. Extends vanilla {@link Zombie} directly, same pattern as
 * {@link SiegeZombie}/{@link Screamer}, rather than replacing it, so unrelated Zombie behaviour (reinforcements,
 * door breaking, other mods' patches) keeps working unchanged.
 * <p>
 * {@code ownerUUID} tracks which player's trigger it was spawned for, mirroring {@link SiegeZombie}/
 * {@link Screamer} - used by {@code event.HordeBossCapManager} to attribute the daily-cap consumption "sticky"
 * to that player even after the boss wanders away from them.
 */
public class HordeBoss extends Zombie {
    // No explicit loot table override needed: EntityType resolves the default loot table id from the entity's
    // own registry name ("cubebuster:horde_boss" -> "cubebuster:entities/horde_boss"), same as every other
    // zombie variant here - see data/cubebuster/loot_table/entities/horde_boss.json.
    private UUID ownerUUID;

    public HordeBoss(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, Config.hordeBossMaxHealth)
                .add(Attributes.ATTACK_DAMAGE, Config.hordeBossAttackDamage)
                .add(Attributes.MOVEMENT_SPEED, Config.hordeBossMovementSpeed)
                // Wide detection radius - a boss event should reliably find and commit to its target rather than
                // losing track of them, same reasoning as SiegeZombie/Screamer.
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Same mustSee=false swap as SiegeZombie/Screamer - a boss shouldn't lose its target just from a
        // moment's lost line of sight.
        this.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getPriority() == 2)
                .findFirst()
                .ifPresent(wrapped -> this.targetSelector.removeGoal(wrapped.getGoal()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected boolean isSunSensitive() {
        // Force-spawned at any time of day/night by the pacing curve - burning at the next sunrise would defeat
        // the "recurring boss event" design.
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.getTarget() != null) {
            // Same as SiegeZombie: never despawn a boss that's actively engaged, no matter the distance-based
            // vanilla despawn rules.
            this.noActionTime = 0;
            return;
        }
        super.checkDespawn();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("AggroOwner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("AggroOwner")) {
            ownerUUID = tag.getUUID("AggroOwner");
        }
    }
}
