package dev.bergthaler.cubebuster.entity.ai;

/**
 * Implemented by zombie variants that mine slower than the baseline {@link BlockBreakingGoal} timing (currently
 * just MushZombie). Kept as a generic multiplier hook rather than an {@code instanceof MushZombie} special case
 * in the goal itself, so any future "slower miner" variant can opt in the same way.
 */
public interface SlowMiningMob {
    /** Multiplies the normal block-break duration; &gt;1.0 = slower. */
    double miningSlowdownMultiplier();
}
