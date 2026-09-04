package dev.bergthaler.cubebuster.block;

import dev.bergthaler.cubebuster.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared "place a fresh mush spore here" logic used by both {@link MushBlock}'s own random-tick spread and
 * InfectedCreeper's blast (see {@code event.InfectedCreeperHandler}) - kept out of the block class since it's
 * about seeding brand-new patches from an external source, not the block's own growth.
 */
public final class MushSpread {
    private MushSpread() {
    }

    /**
     * Attempts to seed a single-layer MushBlock at {@code pos}: only succeeds if {@code pos} is currently air
     * and the block below qualifies as mush-friendly ground (grass/sand/gravel/stone/dirt/existing mush).
     */
    public static boolean trySeed(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        if (!MushBlock.isQualifyingGround(below)) {
            return false;
        }
        level.setBlockAndUpdate(pos, ModBlocks.MUSH_BLOCK.get().defaultBlockState());
        return true;
    }

    /** Rolls {@code chance} per qualifying block in a horizontal square of the given radius around {@code center}. */
    public static void spreadInRadius(ServerLevel level, BlockPos center, int radius, double chance, RandomSource random) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
            if (random.nextDouble() < chance) {
                trySeed(level, pos.immutable());
            }
        }
    }
}
