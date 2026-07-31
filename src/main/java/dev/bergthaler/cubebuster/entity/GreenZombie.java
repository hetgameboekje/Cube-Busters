package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.entity.ai.RadiusBlockBreakingGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * A close-quarters harasser: climbs walls like a vanilla Spider (same {@link WallClimberNavigation} +
 * horizontal-collision-driven climbing flag) and only breaks blocks near its target, rather than digging.
 */
public class GreenZombie extends Zombie {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(GreenZombie.class, EntityDataSerializers.BYTE);

    public GreenZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(3, new RadiusBlockBreakingGoal(this, () -> Config.enableGreenZombie, () -> Config.greenZombieBreakRadius));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // A wall climber shouldn't be punished for falling off what it was just climbing.
        return false;
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte flags = this.entityData.get(DATA_FLAGS_ID);
        flags = climbing ? (byte) (flags | 1) : (byte) (flags & -2);
        this.entityData.set(DATA_FLAGS_ID, flags);
    }
}
