package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * When a player takes damage, nearby SiegeZombies get enraged: faster, and (via {@link SiegeZombie#isEnraged()})
 * quicker at breaking blocks. Works identically in multiplayer since it's driven by the server-side damage event,
 * not by anything client-local.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class SiegeEnrageHandler {

    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        AABB area = player.getBoundingBox().inflate(Config.enrageRadius);
        for (SiegeZombie zombie : level.getEntitiesOfClass(SiegeZombie.class, area)) {
            zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Config.enrageDurationTicks, Config.enrageSpeedAmplifier, true, false));
        }
    }

    private SiegeEnrageHandler() {
    }
}
