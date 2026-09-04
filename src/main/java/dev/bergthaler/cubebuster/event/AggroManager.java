package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player aggro score: builds up from player interactions (see {@link AggroInteractionHandler}), decays
 * slowly over time once a short hold period has passed ({@link AggroTickHandler}), and drives the escalating
 * spawn table in {@link AggroSpawnHandler}.
 * <p>
 * Persisted via a {@link ModAttachmentTypes#AGGRO} data attachment on the player, so (unlike the old in-memory
 * map) it survives disconnects and server restarts. It's still not copied across death/respawn (no
 * {@code copyOnDeath()} on the attachment), which is what gives {@link AggroInteractionHandler}'s
 * reset-to-zero-on-death behavior its effect.
 */
public final class AggroManager {

    public static int getScore(ServerPlayer player) {
        return player.getData(ModAttachmentTypes.AGGRO).score();
    }

    public static void addScore(ServerPlayer player, int amount) {
        AggroState current = player.getData(ModAttachmentTypes.AGGRO);
        int updated = Math.min(Config.aggroMaxScore, current.score() + amount);
        long decayResumeTick = player.level().getGameTime() + Config.aggroDecayHoldTicks;
        player.setData(ModAttachmentTypes.AGGRO, new AggroState(updated, decayResumeTick));
    }

    public static void reset(ServerPlayer player) {
        player.setData(ModAttachmentTypes.AGGRO, AggroState.EMPTY);
    }

    static void decay(ServerPlayer player) {
        AggroState current = player.getData(ModAttachmentTypes.AGGRO);
        if (current.score() == 0 || player.level().getGameTime() < current.decayResumeTick()) {
            return;
        }
        int updated = Math.max(0, current.score() - Config.aggroDecayAmount);
        player.setData(ModAttachmentTypes.AGGRO, new AggroState(updated, current.decayResumeTick()));
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
