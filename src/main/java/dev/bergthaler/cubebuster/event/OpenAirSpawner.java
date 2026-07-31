package dev.bergthaler.cubebuster.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Shared "find an open-air spot around a center point and spawn a mob there" search, used by the aggro
 * spawn-check timer ({@link AggroSpawnHandler}) and the Screamer's summon goal. Unlike
 * {@link SiegeZombieSpawner}/{@link BlueZombieSpawner} (fixed radius, sky-visible only), this supports an
 * excluded inner radius, a vertical search cap, and an optional fallback to fully dark (light level 0)
 * non-sky spots once no sky-visible spot is found - letting torches (which raise the light level
 * checkMonsterSpawnRules requires to be low) protect a base without needing a dedicated "base" concept.
 */
public final class OpenAirSpawner {
    private static final int ATTEMPTS_PER_PASS = 20;

    /**
     * Tries to spawn one {@code type} between {@code minRadius} and {@code maxRadius} blocks (horizontally) from
     * {@code center}, no more than {@code maxVerticalDelta} blocks above/below it. Prefers a sky-visible spot;
     * if {@code allowDarkFallback} is true and none is found, retries allowing any spot with light level 0.
     * Returns the spawned entity, or null if no valid spot was found.
     */
    public static <T extends Monster> T trySpawnNear(ServerLevel level, EntityType<T> type, BlockPos center,
            int minRadius, int maxRadius, int maxVerticalDelta, boolean allowDarkFallback) {
        BlockPos pos = findSpot(level, type, center, minRadius, maxRadius, maxVerticalDelta, true);
        if (pos == null && allowDarkFallback) {
            pos = findSpot(level, type, center, minRadius, maxRadius, maxVerticalDelta, false);
        }
        if (pos == null) {
            return null;
        }

        T mob = type.create(level);
        if (mob == null) {
            return null;
        }
        RandomSource random = level.getRandom();
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        level.addFreshEntity(mob);
        return mob;
    }

    private static BlockPos findSpot(ServerLevel level, EntityType<? extends Monster> type, BlockPos center,
            int minRadius, int maxRadius, int maxVerticalDelta, boolean requireSky) {
        RandomSource random = level.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < ATTEMPTS_PER_PASS; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = Mth.nextDouble(random, minRadius, maxRadius);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            pos.set(x, y, z);

            if (Math.abs(pos.getY() - center.getY()) > maxVerticalDelta) {
                continue;
            }
            if (requireSky) {
                if (!level.canSeeSky(pos)) {
                    continue;
                }
            } else if (level.canSeeSky(pos) || level.getMaxLocalRawBrightness(pos) > 0) {
                continue;
            }
            if (!Monster.checkMonsterSpawnRules(type, level, MobSpawnType.EVENT, pos, random)) {
                continue;
            }
            return pos.immutable();
        }
        return null;
    }

    private OpenAirSpawner() {
    }
}
