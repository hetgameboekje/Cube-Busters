package dev.bergthaler.cubebuster.block;

import com.mojang.serialization.MapCodec;
import dev.bergthaler.cubebuster.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A plantable, cactus-analog hazard: unlike vanilla cactus it isn't a full block and doesn't need sand,
 * but touching it still hurts (see {@link #entityInside}), mirroring cactus's contact damage.
 */
public class ThornedBushBlock extends BushBlock {
    private static final MapCodec<ThornedBushBlock> CODEC = simpleCodec(ThornedBushBlock::new);

    public ThornedBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ThornedBushBlock> codec() {
        return CODEC;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && Config.thornedBushDamage > 0.0) {
            living.hurt(level.damageSources().cactus(), (float) Config.thornedBushDamage);
        }
    }
}
