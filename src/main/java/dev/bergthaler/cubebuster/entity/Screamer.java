package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.entity.ai.ScreamerSummonGoal;
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
 * The aggro system's "caller": rather than digging or hitting especially hard on its own, it repeatedly calls in
 * SiegeZombies near itself while it has a target (see {@link ScreamerSummonGoal}). Only ever force-spawned by
 * {@link dev.bergthaler.cubebuster.event.AggroSpawnHandler} from aggro level 3 upward - no natural
 * spawning.
 * <p>
 * {@code ownerUUID} tracks which player's aggro score it was spawned for, so
 * {@link dev.bergthaler.cubebuster.event.AggroSpawnHandler} can enforce the per-player Screamer cap.
 */
public class Screamer extends Zombie {
    private UUID ownerUUID;

    public Screamer(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                // Same reasoning as SiegeZombie: needs to reliably keep a target to keep summoning.
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(4, new ScreamerSummonGoal(this));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Same mustSee=false swap as SiegeZombie - it needs to keep tracking its target through walls to keep
        // summoning reliably, not just when it happens to have line of sight.
        this.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getPriority() == 2)
                .findFirst()
                .ifPresent(wrapped -> this.targetSelector.removeGoal(wrapped.getGoal()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected boolean isSunSensitive() {
        // Force-spawned by the aggro system at any time of day - burning at the next sunrise would defeat it.
        return false;
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
