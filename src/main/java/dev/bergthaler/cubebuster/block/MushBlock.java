package dev.bergthaler.cubebuster.block;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * The mush spore's ground presence. Reuses {@link SnowLayerBlock}'s layer-stacking (1-8 layers, partial-height
 * collision box) wholesale - only the survival rule and the random-tick behaviour differ: instead of melting in
 * bright light, a MushBlock thickens over time and, once fully thickened, spreads - sideways onto adjacent
 * grass/sand/gravel/stone, or (the "climbs like vines" requirement from the design) by stacking a fresh
 * MushBlock directly on top of itself when that space is free, so a patch grows upward rather than only
 * creeping outward.
 * <p>
 * Placeholder art: every layer count uses the same simple cube model (see the blockstate JSON) rather than
 * vanilla snow's per-height models - the partial-height hitbox from SnowLayerBlock is unaffected by that, only
 * the visual doesn't get shorter/taller with layer count. Acceptable per the "don't over-invest in art" brief.
 */
public class MushBlock extends SnowLayerBlock {
    /** Blocks mush is willing to grow on top of, on top of anything already tagged as this block itself. */
    private static final Set<Block> SUPPORTS = Set.of(Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.STONE, Blocks.DIRT);

    public MushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** Whether the block at this position is something mush is allowed to spread onto/grow on top of. */
    public static boolean isQualifyingGround(BlockState state) {
        return SUPPORTS.contains(state.getBlock()) || state.is(ModBlocks.MUSH_BLOCK.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            return true;
        }
        return isQualifyingGround(below);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int layers = state.getValue(LAYERS);
        if (layers < 8) {
            if (random.nextDouble() < Config.mushBlockThickenChance) {
                level.setBlockAndUpdate(pos, state.setValue(LAYERS, layers + 1));
            }
            return;
        }

        // Fully thickened: try to climb up first (the "extends vertically like vines" requirement), then
        // fall back to creeping sideways onto fresh ground.
        if (random.nextDouble() < Config.mushBlockVerticalGrowChance) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlockAndUpdate(above, this.defaultBlockState());
                return;
            }
        }

        if (random.nextDouble() < Config.mushBlockHorizontalSpreadChance) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockState(neighbor).isAir() && isQualifyingGround(level.getBlockState(neighbor.below()))) {
                level.setBlockAndUpdate(neighbor, this.defaultBlockState());
            }
        }
    }
}
