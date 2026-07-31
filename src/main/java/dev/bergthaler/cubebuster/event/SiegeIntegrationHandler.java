package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Injects extra SiegeZombies into vanilla zombie sieges ({@link net.minecraft.world.entity.ai.village.VillageSiege})
 * without touching or replacing any vanilla class.
 * <p>
 * VillageSiege spawns its zombies with {@link MobSpawnType#EVENT}, which fires {@link FinalizeSpawnEvent} for
 * every one of them just like any other mob spawn. Rather than cancelling that event and swapping the entity
 * (which would fight other mods also listening for zombie siege spawns), this additively spawns a SiegeZombie
 * alongside the vanilla one. The vanilla siege's own zombie count/state machine is left completely alone, so
 * this stays compatible with other mods that adjust siege frequency or spawn additional siege mobs the same way.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class SiegeIntegrationHandler {

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() != MobSpawnType.EVENT) {
            return;
        }
        if (!(event.getEntity() instanceof Zombie) || event.getEntity() instanceof SiegeZombie) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getRandom().nextDouble() >= Config.siegeSpawnChance) {
            return;
        }

        SiegeZombie siegeZombie = ModEntityTypes.SIEGE_ZOMBIE.get().create(serverLevel);
        if (siegeZombie == null) {
            return;
        }

        siegeZombie.moveTo(event.getX(), event.getY(), event.getZ(), event.getEntity().getYRot(), 0.0F);
        siegeZombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(siegeZombie.blockPosition()), MobSpawnType.EVENT, null);
        serverLevel.addFreshEntity(siegeZombie);
    }

    private SiegeIntegrationHandler() {
    }
}
