package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.entity.BlueZombie;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Force-spawns a BlueZombie in the open near a player, same shape as {@link SiegeZombieSpawner} - used by
 * {@link BlueZombieRainSpawnHandler} for the "or when it rains" half of its spawning that NeoForge's
 * one-placement-type-per-entity spawn placement system can't express directly.
 */
final class BlueZombieSpawner {
    private static final int MAX_ATTEMPTS = 20;
    private static final int MIN_RADIUS = 24;
    private static final int MAX_RADIUS = 48;

    static void trySpawnNear(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = Mth.nextDouble(random, MIN_RADIUS, MAX_RADIUS);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            pos.set(x, y, z);

            // Has to actually be under open sky (so it's genuinely raining on it) and otherwise pass vanilla's
            // normal monster-spawn checks.
            if (!level.canSeeSky(pos)
                    || !Monster.checkMonsterSpawnRules(ModEntityTypes.BLUE_ZOMBIE.get(), level, MobSpawnType.EVENT, pos, random)) {
                continue;
            }

            BlueZombie zombie = ModEntityTypes.BLUE_ZOMBIE.get().create(level);
            if (zombie == null) {
                return;
            }
            zombie.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(zombie);
            return;
        }
    }

    private BlueZombieSpawner() {
    }
}
