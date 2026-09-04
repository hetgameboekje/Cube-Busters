package dev.bergthaler.cubebuster.effect;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * The "you touched mush" debuff: periodic non-lethal damage, same non-lethal-floor rule vanilla Poison uses
 * (never drops the victim below 1 HP) so it's an annoyance to clear (see the Antibiotic Firework cure), not a
 * death sentence on its own. Ticks faster at higher amplifiers, same idea as vanilla Poison's amplifier scaling.
 */
public class MushInfectionMobEffect extends MobEffect {
    public MushInfectionMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.hurt(entity.damageSources().magic(), 1.0F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(10, 40 >> amplifier);
        return duration % interval == 0;
    }
}
