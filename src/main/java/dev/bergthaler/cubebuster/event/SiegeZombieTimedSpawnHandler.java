package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Guaranteed periodic SiegeZombie spawn: natural spawning through the biome modifier / spawn placement is too
 * unreliable in practice (mob caps, per-chunk spawn attempt limits, competing with vanilla zombies for the same
 * slots), so this force-spawns one directly near a random player every {@link Config#timedSpawnIntervalTicks},
 * as long as it's night and an open-sky (no roof) spot can be found nearby.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class SiegeZombieTimedSpawnHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!level.isNight() || level.getGameTime() % Config.timedSpawnIntervalTicks != 0) {
            return;
        }

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.get(level.getRandom().nextInt(players.size()));
        SiegeZombieSpawner.trySpawnNear(level, player);
    }

    private SiegeZombieTimedSpawnHandler() {
    }
}
