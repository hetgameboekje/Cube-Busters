package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * The Horde Boss trigger's mush-density check: a simple count of {@link ModBlockTags#MUSH_BLOCKS}-tagged blocks
 * within a radius of a position.
 * <p>
 * Design call (was left open in the spec as "radius and counting method - block count vs. % coverage
 * undecided"): block-count-within-radius was chosen over a % ground-coverage calculation because it's simpler
 * to implement and tune (no need to define what "ground" means in 3D, or normalize against varying terrain),
 * and it degrades gracefully - denser infestations always count higher, without a coverage denominator that can
 * behave oddly around cliffs/overhangs/caves.
 */
public final class HordeBossDensity {

    /** Counts tagged blocks in the cube of side {@code 2*radius+1} centered on {@code center}. */
    public static int countMushBlocksNear(ServerLevel level, BlockPos center, int radius) {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = Math.max(-radius, minY - center.getY()); y <= Math.min(radius, maxY - center.getY()); y++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.setWithOffset(center, x, y, z);
                    if (level.getBlockState(pos).is(ModBlockTags.MUSH_BLOCKS)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private HordeBossDensity() {
    }
}
