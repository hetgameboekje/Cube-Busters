package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player aggro score: builds up from player interactions (see {@link AggroInteractionHandler}), decays
 * slowly over time ({@link AggroTickHandler}), and drives the escalating spawn table in
 * {@link AggroSpawnHandler}. In-memory only, like the other per-player cooldown maps in this package - resets
 * on server restart rather than being saved to player data.
 */
public final class AggroManager {
    private static final Map<UUID, Integer> scores = new HashMap<>();

    public static int getScore(ServerPlayer player) {
        return scores.getOrDefault(player.getUUID(), 0);
    }

    public static void addScore(ServerPlayer player, int amount) {
        scores.put(player.getUUID(), Math.min(Config.aggroMaxScore, getScore(player) + amount));
    }

    public static void reset(ServerPlayer player) {
        scores.remove(player.getUUID());
    }

    static void decay(ServerPlayer player) {
        int updated = Math.max(0, getScore(player) - Config.aggroDecayAmount);
        if (updated == 0) {
            scores.remove(player.getUUID());
        } else {
            scores.put(player.getUUID(), updated);
        }
    }

    /** 0 (no spawns) through 5 (max), based on the aggroLevelNThreshold config values. */
    static int level(int score) {
        if (score >= Config.aggroLevel5Threshold) return 5;
        if (score >= Config.aggroLevel4Threshold) return 4;
        if (score >= Config.aggroLevel3Threshold) return 3;
        if (score >= Config.aggroLevel2Threshold) return 2;
        if (score >= Config.aggroLevel1Threshold) return 1;
        return 0;
    }

    static void sendActionBar(ServerPlayer player) {
        int score = getScore(player);
        int level = level(score);
        ChatFormatting color = switch (level) {
            case 0 -> ChatFormatting.GRAY;
            case 1, 2 -> ChatFormatting.YELLOW;
            case 3, 4 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
        player.displayClientMessage(
                Component.literal("Aggro: " + score + "/" + Config.aggroMaxScore + " (Level " + level + ")").withStyle(color),
                true);
    }

    private AggroManager() {
    }
}
