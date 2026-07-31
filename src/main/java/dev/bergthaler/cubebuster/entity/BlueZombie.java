package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.entity.ai.BlockBreakingGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.Level;

/**
 * A Drowned-based siege variant: gets all of Drowned's swimming, trident-throwing and water pathfinding for free,
 * on top of which it can dig through blocks near water like a SiegeZombie ({@link BlockBreakingGoal} isn't
 * Siege-specific - it works for any {@link net.minecraft.world.entity.monster.Zombie} subtype).
 */
public class BlueZombie extends Drowned {

    public BlueZombie(EntityType<? extends Drowned> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Drowned.createAttributes();
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        // Priority 3: below Drowned's own trident/melee attack goals (2) so it never fights its attack
        // behaviour, above go-to-beach/swim-up (5/6) so it takes over once navigation gets stuck.
        this.goalSelector.addGoal(3, new BlockBreakingGoal(this));
    }
}
