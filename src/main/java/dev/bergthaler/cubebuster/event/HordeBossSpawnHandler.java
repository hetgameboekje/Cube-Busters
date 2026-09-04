package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.HordeBoss;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Horde Boss trigger: every {@link Config#hordeBossCheckIntervalTicks}, rolls each Overworld player's
 * (aggro score) x (mush block density) against {@link Config#hordeBossTriggerThreshold}, scaled by the
 * day/night pacing curve ({@link HordeBossPacing}) and gated by the daily/cluster/server caps
 * ({@link HordeBossCapManager}) and the per-player post-boss cooldown.
 * <p>
 * Purely an integration layer: reads {@link AggroManager#getScore} and scans {@code ModBlockTags#MUSH_BLOCKS},
 * without modifying the aggro system or (once merged) the mush system.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class HordeBossSpawnHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % Config.hordeBossCheckIntervalTicks != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tryTrigger(player);
        }
    }

    private static void tryTrigger(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!HordeBossCapManager.isOffCooldown(player)) {
            return;
        }

        int aggroScore = AggroManager.getScore(player);
        if (aggroScore <= 0) {
            return;
        }

        BlockPos center = player.blockPosition();
        int mushCount = HordeBossDensity.countMushBlocksNear(level, center, Config.hordeBossDensityRadius);
        if (mushCount <= 0) {
            return;
        }

        long product = (long) aggroScore * mushCount;
        if (product < Config.hordeBossTriggerThreshold) {
            return;
        }

        double pacing = HordeBossPacing.multiplier(level.getDayTime());
        double chance = Config.hordeBossBaseTriggerChance * pacing;
        RandomSource random = level.getRandom();
        if (random.nextDouble() >= chance) {
            return;
        }

        if (!HordeBossCapManager.canSpawn(player, center)) {
            return;
        }

        int maxVerticalDelta = player.getServer().getPlayerList().getViewDistance() * 16;
        HordeBoss boss = OpenAirSpawner.trySpawnNear(level, ModEntityTypes.HORDE_BOSS.get(), center,
                Config.hordeBossSpawnSafeZoneRadius, Config.hordeBossSpawnMaxRadius, maxVerticalDelta, true);
        if (boss == null) {
            return;
        }
        boss.setOwnerUUID(player.getUUID());
        HordeBossCapManager.recordSpawn(player, center);
    }

    private HordeBossSpawnHandler() {
    }
}
