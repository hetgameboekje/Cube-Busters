package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.entity.ai.BlockBreakingGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * A tougher, desert-skinned Nether siege variant - a Husk (already immune to sun-burning), on top of which fire
 * and lava can't hurt it at all. Fire/lava immunity is set on its {@link EntityType} (see
 * {@link dev.bergthaler.cubebuster.registry.ModEntityTypes#RED_ZOMBIE}, {@code .fireImmune()}) rather than
 * overridden here - that's the same flag vanilla Blazes/Striders/Wither use, and it already makes
 * {@link net.minecraft.world.entity.Entity#lavaHurt()} and fire-tick damage no-ops.
 */
public class RedZombie extends Husk {

    public RedZombie(EntityType<? extends Husk> type, Level level) {
        super(type, level);
        // Willing to path through lava instead of avoiding it, same technique Drowned uses for water
        // (setPathfindingMalus(WATER, 0.0F)).
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(3, new BlockBreakingGoal(this));
    }
}
