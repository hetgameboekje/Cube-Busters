package dev.bergthaler.cubebuster.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persisted per-player aggro state (see {@link AggroManager}): the score itself, plus the tick at which passive
 * decay is next allowed to resume. Score gains push {@code decayResumeTick} forward so a freshly-raised score
 * holds for {@link dev.bergthaler.cubebuster.Config#aggroDecayHoldTicks} before it starts trending back down,
 * rather than decaying continuously the instant a gain interval elapses.
 */
public record AggroState(int score, long decayResumeTick) {
    public static final AggroState EMPTY = new AggroState(0, 0);

    public static final Codec<AggroState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("score").forGetter(AggroState::score),
            Codec.LONG.fieldOf("decay_resume_tick").forGetter(AggroState::decayResumeTick)
    ).apply(instance, AggroState::new));
}
