package dev.bergthaler.cubebuster.block;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Cheap, early-game perimeter defense: a fence+cobweb hybrid. {@link BlockBehaviour.Properties#noCollission()}
 * (set at registration in {@link dev.bergthaler.cubebuster.registry.ModBlocks}) already makes it walkable-through
 * rather than solid, mirroring cobweb; this class adds the damage-over-time and movement slow while an entity is
 * inside it.
 */
public class BarbedWireBlock extends Block {
    public BarbedWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(Config.barbedWireSlowMultiplier, 1.0, Config.barbedWireSlowMultiplier));
        if (!level.isClientSide && entity instanceof LivingEntity living && Config.barbedWireDamage > 0.0) {
            living.hurt(damageSource(level), (float) Config.barbedWireDamage);
        }
    }

    private static DamageSource damageSource(Level level) {
        return level.damageSources().cactus();
    }
}
