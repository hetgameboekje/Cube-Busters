package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Drives the three periodic aggro effects off the server's global tick counter: score decay
 * ({@link Config#aggroDecayIntervalTicks}), the actionbar readout (every second), and the level-based spawn
 * check ({@link Config#aggroSpawnCheckIntervalTicks}). Runs per-player rather than per-level since aggro is a
 * per-player stat, not a world one.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class AggroTickHandler {
    private static final int ACTION_BAR_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long tick = server.getTickCount();

        boolean decay = tick % Config.aggroDecayIntervalTicks == 0;
        boolean display = tick % ACTION_BAR_INTERVAL_TICKS == 0;
        boolean spawnCheck = tick % Config.aggroSpawnCheckIntervalTicks == 0;
        if (!decay && !display && !spawnCheck) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (decay) {
                AggroManager.decay(player);
            }
            if (display) {
                AggroManager.sendActionBar(player);
            }
            if (spawnCheck) {
                AggroSpawnHandler.trySpawnFor(player);
            }
        }
    }

    private AggroTickHandler() {
    }
}
