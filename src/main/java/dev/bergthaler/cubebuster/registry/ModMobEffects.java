package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.effect.MushInfectionMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The one custom status effect the Mush/Infected mechanic needs - see {@link MushInfectionMobEffect} for the
 * actual damage-over-time behaviour. Applied by InfectedCreeper blasts and Infection Potions; removed by
 * Antibiotic Fireworks.
 */
public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Cubebuster.MODID);

    public static final DeferredHolder<MobEffect, MushInfectionMobEffect> MUSH_INFECTION = MOB_EFFECTS.register("mush_infection",
            () -> new MushInfectionMobEffect(MobEffectCategory.HARMFUL, 0x6B8E23));

    private ModMobEffects() {
    }
}
