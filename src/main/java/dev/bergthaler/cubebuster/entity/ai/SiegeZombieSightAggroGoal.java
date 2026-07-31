package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.event.AggroManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Raises the target player's aggro score while this SiegeZombie actually has line of sight on them. A
 * SiegeZombie's target-selector already ignores walls (see SiegeZombie's mustSee=false swap) so it keeps
 * hunting a player it can't see - it just doesn't build aggro while it can't see them, only once it does. Same
 * side-effect-in-canUse() idiom as ZombieInfectionGoal/NightBuffGoal - never actually "runs".
 */
public class SiegeZombieSightAggroGoal extends Goal {
    private final SiegeZombie siegeZombie;
    private int scanCooldown;

    public SiegeZombieSightAggroGoal(SiegeZombie siegeZombie) {
        this.siegeZombie = siegeZombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = Config.aggroSightGainIntervalTicks;

        LivingEntity target = siegeZombie.getTarget();
        if (target instanceof ServerPlayer player && siegeZombie.hasLineOfSight(target)) {
            AggroManager.addScore(player, Config.aggroSightGainAmount);
        }
        return false;
    }
}
