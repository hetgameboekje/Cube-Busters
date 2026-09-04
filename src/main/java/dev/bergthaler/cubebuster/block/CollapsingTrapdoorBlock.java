package dev.bergthaler.cubebuster.block;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A trap, not a door: something walking onto a closed one triggers a delayed scheduled tick
 * ({@link Config#collapsingTrapdoorDelayTicks}) after which it springs open on its own (dropping whatever was
 * standing on it through), then automatically re-closes after {@link Config#collapsingTrapdoorRecloseTicks} so
 * the same trapdoor can be reused rather than needing to be replaced.
 */
public class CollapsingTrapdoorBlock extends TrapDoorBlock {
    public CollapsingTrapdoorBlock(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && !state.getValue(BlockStateProperties.OPEN) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, Config.collapsingTrapdoorDelayTicks);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean nowOpen = !state.getValue(BlockStateProperties.OPEN);
        level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, nowOpen), 3);
        level.playSound(null, pos, nowOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        if (nowOpen) {
            level.scheduleTick(pos, this, Config.collapsingTrapdoorRecloseTicks);
        }
    }
}
