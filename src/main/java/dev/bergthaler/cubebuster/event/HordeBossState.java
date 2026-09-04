package dev.bergthaler.cubebuster.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persisted per-player Horde Boss cap state (see {@link HordeBossCapManager}): how many boss spawns have been
 * attributed ("stuck") to this player today, the in-game day that count is for, and the tick until which this
 * player is on post-boss cooldown.
 * <p>
 * Same NeoForge data-attachment pattern as {@link AggroState} - see {@code ModAttachmentTypes.HORDE_BOSS_CAP} -
 * chosen specifically because the daily cap must be a real persistent counter (survives restarts), unlike
 * vanilla mobcap which is a live snapshot and can't be reused for a quota like this.
 */
public record HordeBossState(int spawnsToday, long lastResetDay, long cooldownUntilTick) {
    public static final HordeBossState EMPTY = new HordeBossState(0, -1, 0);

    public static final Codec<HordeBossState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("spawns_today").forGetter(HordeBossState::spawnsToday),
            Codec.LONG.fieldOf("last_reset_day").forGetter(HordeBossState::lastResetDay),
            Codec.LONG.fieldOf("cooldown_until_tick").forGetter(HordeBossState::cooldownUntilTick)
    ).apply(instance, HordeBossState::new));
}
