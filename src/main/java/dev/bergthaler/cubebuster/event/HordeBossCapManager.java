package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * Enforces the Horde Boss daily spawn caps: {@link Config#hordeBossPlayerDailyCap} per player,
 * {@link Config#hordeBossClusterCap} shared by players within {@link Config#hordeBossClusterChunkRadius} chunks
 * (Chebyshev distance) of each other, and {@link Config#hordeBossServerDailyCap} as the hard ceiling - whichever
 * is hit first blocks the spawn. Also owns the post-boss per-player cooldown
 * ({@link Config#hordeBossCooldownTicks}).
 * <p>
 * Cap consumption is "sticky": once {@link #recordSpawn} attributes a spawn to a player, it counts against that
 * player's {@link Config#hordeBossPlayerDailyCap} (and, if clustered, the cluster's {@link Config#hordeBossClusterCap})
 * for the rest of the day even if they move away afterward - nothing here re-checks distance retroactively.
 * <p>
 * "Day" is computed from the world's total (never-reset) game time divided by {@link #TICKS_PER_MINECRAFT_DAY},
 * not {@code Level#getDayTime()} (which sleeping/commands can shift) - see {@link #currentDay}.
 */
public final class HordeBossCapManager {
    /** Vanilla's fixed length of one Minecraft day, in ticks - the divisor for turning game time into a day index. */
    private static final long TICKS_PER_MINECRAFT_DAY = 24000L;

    /** Whether {@code player}'s post-boss cooldown has expired and isn't blocking a new trigger right now. */
    public static boolean isOffCooldown(ServerPlayer player) {
        HordeBossState state = player.getData(ModAttachmentTypes.HORDE_BOSS_CAP);
        return player.level().getGameTime() >= state.cooldownUntilTick();
    }

    /**
     * Whether a Horde Boss spawn attempt for {@code player} at {@code pos} is allowed by every cap (server,
     * cluster, and per-player), without consuming any of them. Call {@link #recordSpawn} immediately afterward
     * if the spawn actually happens.
     */
    public static boolean canSpawn(ServerPlayer player, BlockPos pos) {
        long day = currentDay(player);
        HordeBossSavedData savedData = savedData(player);

        if (savedData.isServerCapReached(day)) {
            return false;
        }
        if (playerSpawnsToday(player, day) >= Config.hordeBossPlayerDailyCap) {
            return false;
        }
        return !savedData.isClusterCapReached(day, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /** Attributes a spawn to {@code player} at {@code pos}: bumps every cap counter and starts their cooldown. */
    public static void recordSpawn(ServerPlayer player, BlockPos pos) {
        long day = currentDay(player);
        savedData(player).recordSpawn(day, pos.getX() >> 4, pos.getZ() >> 4, player.getUUID());

        HordeBossState current = normalizedState(player, day);
        long cooldownUntil = player.level().getGameTime() + Config.hordeBossCooldownTicks;
        player.setData(ModAttachmentTypes.HORDE_BOSS_CAP,
                new HordeBossState(current.spawnsToday() + 1, day, cooldownUntil));
    }

    private static int playerSpawnsToday(ServerPlayer player, long day) {
        return normalizedState(player, day).spawnsToday();
    }

    /** The player's cap state, rolled over to spawnsToday=0 if it's for an earlier day than {@code day}. */
    private static HordeBossState normalizedState(ServerPlayer player, long day) {
        HordeBossState state = player.getData(ModAttachmentTypes.HORDE_BOSS_CAP);
        if (state.lastResetDay() != day) {
            return new HordeBossState(0, day, state.cooldownUntilTick());
        }
        return state;
    }

    private static long currentDay(ServerPlayer player) {
        // Anchored on the Overworld's game time (Horde Boss triggers are Overworld-only) so the day index stays
        // consistent no matter which dimension the player happens to be in when this is checked.
        return player.getServer().overworld().getGameTime() / TICKS_PER_MINECRAFT_DAY;
    }

    private static HordeBossSavedData savedData(ServerPlayer player) {
        return player.getServer().overworld().getDataStorage().computeIfAbsent(HordeBossSavedData.FACTORY, HordeBossSavedData.ID);
    }

    private HordeBossCapManager() {
    }
}
