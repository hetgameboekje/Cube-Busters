package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.Screamer;
import dev.bergthaler.cubebuster.event.OpenAirSpawner;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * While the Screamer has a target, periodically ({@link Config#screamerSummonCooldownTicks}) calls in
 * {@link Config#screamerSummonCount} SiegeZombies in open air nearby - the whole reason it exists as a distinct
 * mob. Every attempt (whether or not a spot is actually found for each summoned zombie) plays a ghast scream as
 * a telegraph/placeholder summon sound. Only ever returns false from canUse() - it does its work as a side
 * effect of the periodic check itself, same idiom as ZombieInfectionGoal/NightBuffGoal.
 */
public class ScreamerSummonGoal extends Goal {
    private final Screamer screamer;
    private int cooldown;

    public ScreamerSummonGoal(Screamer screamer) {
        this.screamer = screamer;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (screamer.getTarget() == null || !(screamer.level() instanceof ServerLevel level)) {
            return false;
        }
        cooldown = Config.screamerSummonCooldownTicks;
        level.playSound(null, screamer.blockPosition(), SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 1.0F, 1.0F);
        for (int i = 0; i < Config.screamerSummonCount; i++) {
            OpenAirSpawner.trySpawnNear(level, ModEntityTypes.SIEGE_ZOMBIE.get(), screamer.blockPosition(),
                    Config.screamerSummonMinRadius, Config.screamerSummonMaxRadius, Config.screamerSummonMaxRadius, false);
        }
        return false;
    }
}
