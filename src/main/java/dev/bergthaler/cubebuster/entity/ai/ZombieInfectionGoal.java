package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Converts nearby plain Zombies into SiegeZombies, so the siege "spreads" through a horde rather than staying a
 * fixed handful of special mobs. Only ever returns false from canUse() - it does its work as a side effect of
 * the periodic check itself rather than actually running as a goal, same throttling idiom as
 * {@link BlockBreakingGoal}'s scan cooldown.
 */
public class ZombieInfectionGoal extends Goal {
    private static final int SCAN_INTERVAL_TICKS = 20;

    private final SiegeZombie siegeZombie;
    private int scanCooldown;

    public ZombieInfectionGoal(SiegeZombie siegeZombie) {
        this.siegeZombie = siegeZombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL_TICKS;
        if (siegeZombie.level() instanceof ServerLevel level) {
            infectNearby(level);
        }
        return false;
    }

    private void infectNearby(ServerLevel level) {
        AABB area = siegeZombie.getBoundingBox().inflate(Config.infectionRadius);
        for (Zombie zombie : level.getEntitiesOfClass(Zombie.class, area, z -> z.getType() == EntityType.ZOMBIE)) {
            convert(level, zombie);
        }
    }

    private void convert(ServerLevel level, Zombie zombie) {
        SiegeZombie converted = ModEntityTypes.SIEGE_ZOMBIE.get().create(level);
        if (converted == null) {
            return;
        }
        converted.moveTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), zombie.getXRot());
        converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()), MobSpawnType.CONVERSION, null);
        converted.setHealth(zombie.getHealth());
        level.addFreshEntity(converted);
        zombie.discard();
    }
}
