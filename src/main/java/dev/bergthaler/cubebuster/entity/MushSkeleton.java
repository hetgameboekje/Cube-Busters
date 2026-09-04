package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

/**
 * A weaker, mush-touched Skeleton: keeps every normal Skeleton power (bow attacks, stray conversion in powder
 * snow) but with lower max HP. Skeletons don't mine blocks in this mod (no BlockBreakingGoal), so unlike
 * MushZombie there's no mining-speed side to this variant - just the HP reduction.
 */
public class MushSkeleton extends Skeleton {
    public MushSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        // See MushZombie's constructor for why this is applied per-instance rather than in createAttributes().
        var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * Config.mushMobHealthMultiplier);
            this.setHealth(this.getMaxHealth());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Skeleton.createAttributes();
    }
}
