package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.Screamer;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * The periodic (every {@link Config#aggroSpawnCheckIntervalTicks}) roll that turns a player's current aggro
 * level into actual spawns, all searched for within {@link Config#aggroSafeZoneRadius}..
 * {@link Config#aggroSpawnMaxRadius} blocks of the player via {@link OpenAirSpawner}:
 * <pre>
 * Level 1: always 1 SiegeZombie
 * Level 2: 1-3 SiegeZombies
 * Level 3: 1 Screamer, plus a 50% chance of 1-3 more SiegeZombies
 * Level 4: 1-2 Screamers, plus 2-3 SiegeZombies
 * Level 5: always 2 Screamers, plus 3 SiegeZombies
 * </pre>
 */
final class AggroSpawnHandler {

    static void trySpawnFor(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        int aggroLevel = AggroManager.level(AggroManager.getScore(player));
        if (aggroLevel <= 0) {
            return;
        }

        RandomSource random = level.getRandom();
        int siegeZombieCount;
        int screamerCount;
        switch (aggroLevel) {
            case 1 -> {
                siegeZombieCount = 1;
                screamerCount = 0;
            }
            case 2 -> {
                siegeZombieCount = 1 + random.nextInt(3);
                screamerCount = 0;
            }
            case 3 -> {
                siegeZombieCount = random.nextBoolean() ? 1 + random.nextInt(3) : 0;
                screamerCount = 1;
            }
            case 4 -> {
                siegeZombieCount = 2 + random.nextInt(2);
                screamerCount = 1 + random.nextInt(2);
            }
            default -> {
                siegeZombieCount = 3;
                screamerCount = 2;
            }
        }

        BlockPos center = player.blockPosition();
        int maxVerticalDelta = level.getServer().getPlayerList().getViewDistance() * 16;

        for (int i = 0; i < screamerCount && countAliveScreamers(level, player) < Config.aggroMaxScreamersPerPlayer; i++) {
            Screamer screamer = OpenAirSpawner.trySpawnNear(level, ModEntityTypes.SCREAMER.get(), center,
                    Config.aggroSafeZoneRadius, Config.aggroSpawnMaxRadius, maxVerticalDelta, true);
            if (screamer != null) {
                screamer.setOwnerUUID(player.getUUID());
            }
        }
        for (int i = 0; i < siegeZombieCount; i++) {
            OpenAirSpawner.trySpawnNear(level, ModEntityTypes.SIEGE_ZOMBIE.get(), center,
                    Config.aggroSafeZoneRadius, Config.aggroSpawnMaxRadius, maxVerticalDelta, true);
        }
    }

    private static int countAliveScreamers(ServerLevel level, ServerPlayer owner) {
        return level.getEntitiesOfClass(Screamer.class, owner.getBoundingBox().inflate(256),
                screamer -> owner.getUUID().equals(screamer.getOwnerUUID())).size();
    }

    private AggroSpawnHandler() {
    }
}
