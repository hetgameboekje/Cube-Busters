package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.EnderZombie;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * One-shot chorus fruit teleport, usually aimed near the target, occasionally a long-range random hop instead -
 * same safety check as vanilla's {@link net.minecraft.world.item.ChorusFruitItem} ({@link net.minecraft.world.entity.Entity#randomTeleport}).
 * Once it succeeds, the EnderZombie's chorus fruit is gone (see {@link EnderZombie#consumeChorusFruit()}) and this
 * goal never does anything again - same "goal that never runs, just gets ticked" idiom as {@link ZombieInfectionGoal}.
 */
public class ChorusFruitTeleportGoal extends Goal {
    private static final int CHECK_INTERVAL_TICKS = 40;

    private final EnderZombie zombie;
    private int checkCooldown;

    public ChorusFruitTeleportGoal(EnderZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (zombie.hasUsedChorusFruit() || !Config.enableEnderZombie) {
            return false;
        }
        if (checkCooldown > 0) {
            checkCooldown--;
            return false;
        }
        checkCooldown = CHECK_INTERVAL_TICKS;

        LivingEntity target = zombie.getTarget();
        if (target == null || zombie.getRandom().nextDouble() >= Config.enderZombieTeleportChance) {
            return false;
        }

        boolean far = zombie.getRandom().nextDouble() < Config.enderZombieFarTeleportChance;
        boolean teleported = far ? teleportFar() : teleportTowards(target);
        if (teleported) {
            zombie.consumeChorusFruit();
        }
        return false;
    }

    private boolean teleportTowards(LivingEntity target) {
        RandomSource random = zombie.getRandom();
        double x = target.getX() + (random.nextDouble() - 0.5) * 8.0;
        double y = target.getY() + random.nextInt(3) - 1;
        double z = target.getZ() + (random.nextDouble() - 0.5) * 8.0;
        return zombie.randomTeleport(x, y, z, true);
    }

    private boolean teleportFar() {
        RandomSource random = zombie.getRandom();
        double x = zombie.getX() + (random.nextDouble() - 0.5) * 32.0;
        double y = zombie.getY() + random.nextInt(16) - 8;
        double z = zombie.getZ() + (random.nextDouble() - 0.5) * 32.0;
        return zombie.randomTeleport(x, y, z, true);
    }
}
