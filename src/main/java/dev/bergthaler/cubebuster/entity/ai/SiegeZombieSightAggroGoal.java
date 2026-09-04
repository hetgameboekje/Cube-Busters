package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.event.AggroManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Raises the owning player's aggro score while this SiegeZombie actually has line of sight on them. A
 * SiegeZombie's target-selector already ignores walls (see SiegeZombie's mustSee=false swap) so it keeps
 * hunting a player it can't see - it just doesn't build aggro while it can't see them, only once it does. Same
 * side-effect-in-canUse() idiom as ZombieInfectionGoal/NightBuffGoal - never actually "runs".
 * <p>
 * Credits the score to {@link SiegeZombie#getOwnerUUID()}, not whichever player the zombie currently has
 * targeted - with 2+ players online a zombie force-spawned for player A could otherwise retarget to player B
 * and raise B's score for something B had no part in. Naturally-spawned SiegeZombies have no owner and so
 * never grant sight-aggro.
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
        UUID owner = siegeZombie.getOwnerUUID();
        if (target instanceof ServerPlayer player
                && owner != null && owner.equals(player.getUUID())
                && !player.isCreative() && !player.isSpectator()
                && siegeZombie.hasLineOfSight(target)) {
            AggroManager.addScore(player, Config.aggroSightGainAmount);
        }
        return false;
    }
}
