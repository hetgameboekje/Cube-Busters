package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.entity.ai.BlockBreakingGoal;
import dev.bergthaler.cubebuster.entity.ai.NightBuffGoal;
import dev.bergthaler.cubebuster.entity.ai.SiegeZombieSightAggroGoal;
import dev.bergthaler.cubebuster.entity.ai.ZombieInfectionGoal;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * An aggressive zombie variant that can chew through most blocks to reach its target.
 * Extends vanilla {@link Zombie} directly (rather than replacing it) so everything Zombie already does -
 * drowning conversion, reinforcements, door breaking on hard, mob effects, other mods' attribute/behaviour
 * patches keyed off Zombie - keeps working unchanged, aside from the explicit overrides below (sun immunity,
 * aggro-gated despawning).
 */
public class SiegeZombie extends Zombie {
    // Which player's aggro score this SiegeZombie was force-spawned for (null for naturally-spawned ones, e.g.
    // village sieges / timed night spawns). Mirrors Screamer's ownerUUID pattern - see
    // SiegeZombieSightAggroGoal, which credits sight-aggro to this owner rather than whichever player it
    // currently targets, so a zombie spawned for player A can't raise player B's score just by retargeting.
    private UUID ownerUUID;

    public SiegeZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        // Set directly in the constructor (not populateDefaultEquipmentSlots) so every SiegeZombie always has
        // the pickaxe, regardless of spawn source (natural spawn, siege spawn-in, summon command, spawn egg).
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SIEGE_PICKAXE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                // Extended player detection radius: vanilla NearestAttackableTargetGoal uses getFollowRange()
                // as its search radius, so bumping this attribute is all "extended detection" needs - no custom
                // target-selector goal required. Vanilla zombie default is 35.0.
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        // Priority 3: below the attack goal (2) so it never fights the zombie's attack behaviour, above
        // general wandering (7) so it takes over once navigation gets stuck.
        this.goalSelector.addGoal(3, new BlockBreakingGoal(this));
        // Priority is irrelevant here - these goals never actually "run" (see their own doc comments), they
        // just need to be ticked.
        this.goalSelector.addGoal(4, new ZombieInfectionGoal(this));
        this.goalSelector.addGoal(4, new NightBuffGoal(this));
        this.goalSelector.addGoal(4, new SiegeZombieSightAggroGoal(this));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Vanilla Zombie adds a Player-targeting goal at priority 2 with mustSee=true. Siege zombies are meant
        // to always know exactly where their target is, walls or no walls, so swap it for a mustSee=false copy
        // rather than reimplementing the whole target selector (HurtByTargetGoal, villager/golem targeting, etc.
        // stay exactly vanilla).
        this.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getPriority() == 2)
                .findFirst()
                .ifPresent(wrapped -> this.targetSelector.removeGoal(wrapped.getGoal()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected boolean isSunSensitive() {
        // Siege zombies spawn out in the open on purpose (see the open-sky spawn placement requirement) -
        // burning them the moment day breaks would defeat that.
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.getTarget() != null) {
            // Aggroed (or still within the target selector's recent-sight memory window) - never despawn while
            // that's true, no matter how far from a player vanilla's distance-based despawn would otherwise allow.
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
