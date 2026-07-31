package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * Keeps SiegeZombie's night-buff attribute modifiers (speed, attack) in sync with day/night + dimension, using
 * fixed-id modifiers that are simply added when the buff should be active and removed when it shouldn't - same
 * "goal that never runs, just gets ticked" idiom as {@link ZombieInfectionGoal}.
 */
public class NightBuffGoal extends Goal {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "night_buff_speed");
    private static final ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "night_buff_attack");

    private final SiegeZombie zombie;
    private int checkCooldown;

    public NightBuffGoal(SiegeZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    /** Whether night-buffed behaviour (block-break speed included) should currently apply to this entity. */
    public static boolean isActive(LivingEntity entity) {
        Level level = entity.level();
        return level.dimension() == Level.OVERWORLD && level.isNight();
    }

    @Override
    public boolean canUse() {
        if (checkCooldown > 0) {
            checkCooldown--;
            return false;
        }
        checkCooldown = CHECK_INTERVAL_TICKS;
        sync(zombie.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, Config.nightSpeedMultiplier);
        sync(zombie.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_ID, Config.nightAttackMultiplier);
        return false;
    }

    private void sync(AttributeInstance attribute, ResourceLocation id, double multiplier) {
        if (attribute == null) {
            return;
        }
        boolean shouldBeActive = isActive(zombie);
        boolean isActive = attribute.getModifier(id) != null;
        if (shouldBeActive == isActive) {
            return;
        }
        if (shouldBeActive) {
            attribute.addTransientModifier(new AttributeModifier(id, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            attribute.removeModifier(id);
        }
    }
}
