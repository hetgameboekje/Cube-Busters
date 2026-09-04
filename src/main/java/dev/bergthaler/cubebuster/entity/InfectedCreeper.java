package dev.bergthaler.cubebuster.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * A Creeper variant that never deals explosion damage. Its blast is fully neutralized in
 * {@code event.InfectedCreeperHandler} (an {@code ExplosionEvent.Detonate} listener - vanilla Creeper's
 * {@code explodeCreeper()} is private, so it can't be overridden here) which, instead of damage, infects nearby
 * players with the mush infection effect and seeds MushBlock spores on the surrounding ground. Otherwise
 * behaves exactly like a normal Creeper (fuse, swelling, targeting, powered-by-lightning) - no goal overrides
 * needed here, {@link Creeper#registerGoals()} is reused entirely unmodified.
 */
public class InfectedCreeper extends Creeper {
    public InfectedCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes();
    }
}
