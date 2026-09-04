package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;

/**
 * Day/night trigger-chance pacing for the Horde Boss, modeled on 7 Days to Die's "minimal buildup by day, ramps
 * at night" horde pacing: low during full daylight, ramping up to full strength across a
 * {@link Config#hordeBossPacingRampTicks}-wide window (~500 ticks by default) centered on dusk, holding at full
 * strength overnight, then ramping back down across the same window centered on dawn.
 * <p>
 * Uses vanilla's day-time convention: 0 = sunrise, 6000 = noon, 12000 = sunset/dusk, 18000 = midnight,
 * 23999 = just before sunrise, wrapping back to 0.
 */
public final class HordeBossPacing {
    private static final long DAY_TICKS = 24000L;
    private static final long DUSK = 12000L;
    private static final long DAWN = 24000L; // == 0, expressed on the same axis as dusk + a full night

    /** Trigger-chance multiplier (hordeBossDayPacingMultiplier..1.0) for the given vanilla day-time (0..23999). */
    public static double multiplier(long dayTime) {
        long t = Math.floorMod(dayTime, DAY_TICKS);
        long ramp = Math.max(1, Config.hordeBossPacingRampTicks);
        double low = Config.hordeBossDayPacingMultiplier;

        if (t <= DUSK) {
            // Before dusk: full daylight until the ramp window starts, then linearly climb into it.
            long rampStart = DUSK - ramp;
            if (t < rampStart) {
                return low;
            }
            double progress = (double) (t - rampStart) / ramp;
            return lerp(progress, low, 1.0);
        }

        // After dusk: full night strength until the pre-dawn ramp window, then linearly fall back to daytime.
        long rampStart = DAWN - ramp;
        if (t < rampStart) {
            return 1.0;
        }
        double progress = (double) (t - rampStart) / ramp;
        return lerp(progress, 1.0, low);
    }

    private static double lerp(double progress, double start, double end) {
        return start + progress * (end - start);
    }

    private HordeBossPacing() {
    }
}
