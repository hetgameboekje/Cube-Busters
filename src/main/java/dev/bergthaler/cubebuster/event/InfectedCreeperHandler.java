package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.block.MushSpread;
import dev.bergthaler.cubebuster.entity.InfectedCreeper;
import dev.bergthaler.cubebuster.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * InfectedCreeper's blast never deals damage or breaks blocks - see {@link InfectedCreeper}'s class doc for why
 * that's done here (an {@code ExplosionEvent.Detonate} listener) rather than overriding Creeper's own explosion
 * method. Instead: any living entities caught in the blast get the mush infection effect, and the surrounding
 * ground gets seeded with MushBlock spores (see {@link MushSpread}).
 */
@EventBusSubscriber(modid = Cubebuster.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class InfectedCreeperHandler {

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof InfectedCreeper)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        for (Entity entity : event.getAffectedEntities()) {
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(ModMobEffects.MUSH_INFECTION,
                        Config.infectionEffectDurationTicks, Config.infectionEffectAmplifier));
            }
        }
        // Zero out both lists so vanilla's post-Detonate step does no block destruction or damage at all.
        event.getAffectedBlocks().clear();
        event.getAffectedEntities().clear();

        BlockPos center = BlockPos.containing(event.getExplosion().center());
        MushSpread.spreadInRadius(level, center, Config.infectedCreeperSporeRadius, Config.infectedCreeperSporeChance, level.getRandom());
    }

    private InfectedCreeperHandler() {
    }
}
