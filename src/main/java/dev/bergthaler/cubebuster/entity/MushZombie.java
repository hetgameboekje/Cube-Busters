package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.ai.BlockBreakingGoal;
import dev.bergthaler.cubebuster.entity.ai.SlowMiningMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * A weaker, mush-touched Zombie: keeps every normal Zombie power (attacks, drowning conversion, reinforcements,
 * door breaking on hard) but with lower max HP and, since it also digs like SiegeZombie/RedZombie via
 * {@link BlockBreakingGoal}, noticeably slower mining ({@link SlowMiningMob}) - being covered in mush is a
 * handicap, not an upgrade.
 */
public class MushZombie extends Zombie implements SlowMiningMob {
    public MushZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        // Applied here rather than baked into createAttributes()'s AttributeSupplier.Builder: that builder runs
        // once at entity-type registration time (before Config has necessarily loaded), so reading Config there
        // would risk capturing the pre-load default. Setting the base value per-instance, at construction time,
        // keeps this live with config reloads like the rest of the mod's config reads.
        var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * Config.mushMobHealthMultiplier);
            this.setHealth(this.getMaxHealth());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(3, new BlockBreakingGoal(this));
    }

    @Override
    public double miningSlowdownMultiplier() {
        return Config.mushZombieMiningSlowdownMultiplier;
    }
}
