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
 * "or when it rains" half of BlueZombie's spawning: on top of its normal water-based natural spawns (see
 * {@link dev.bergthaler.cubebuster.event.ModSetupEvents}), force-spawns one near a random player, in the
 * open, whenever it's raining in the Overworld. Checked every {@link #CHECK_INTERVAL_TICKS} rather than every
 * tick, and gated by {@link Config#blueZombieRainSpawnChance} so it doesn't flood every rainstorm with zombies.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class BlueZombieRainSpawnHandler {
    private static final int CHECK_INTERVAL_TICKS = 200;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!Config.enableBlueZombie || !level.isRaining() || level.getGameTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        if (level.getRandom().nextDouble() >= Config.blueZombieRainSpawnChance) {
            return;
        }

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.get(level.getRandom().nextInt(players.size()));
        BlueZombieSpawner.trySpawnNear(level, player);
    }

    private BlueZombieRainSpawnHandler() {
    }
}
