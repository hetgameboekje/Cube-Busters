package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Same "dig when stuck" idiom as {@link BlockBreakingGoal}, but scoped to blocks within a given radius of the
 * target - for zombie variants that harass at close range rather than tunnel (currently just GreenZombie).
 * Enabled flag and radius are read live via suppliers so they follow config reloads, same as everything else in
 * {@link Config}.
 */
public class RadiusBlockBreakingGoal extends Goal {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int SCAN_RADIUS = 1;
    private static final double REACH_SQR = 3.0 * 3.0;

    private final Zombie zombie;
    private final BooleanSupplier enabled;
    private final DoubleSupplier radius;
    private int scanCooldown;
    private BlockPos targetPos;
    private int breakProgress;
    private int lastBroadcastStage = -1;

    public RadiusBlockBreakingGoal(Zombie zombie, BooleanSupplier enabled, DoubleSupplier radius) {
        this.zombie = zombie;
        this.enabled = enabled;
        this.radius = radius;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!Config.breaksBlocks || !enabled.getAsBoolean()) {
            return false;
        }
        LivingEntity target = zombie.getTarget();
        if (target == null || !target.closerThan(zombie, radius.getAsDouble() + Math.sqrt(REACH_SQR))) {
            return false;
        }
        var path = zombie.getNavigation().getPath();
        if (path != null && !zombie.getNavigation().isStuck()) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL_TICKS;
        return findBlockToBreak(target);
    }

    private boolean findBlockToBreak(LivingEntity target) {
        Level level = zombie.level();
        BlockPos origin = zombie.blockPosition();
        double radiusValue = radius.getAsDouble();
        double radiusSqr = radiusValue * radiusValue;

        BlockPos best = null;
        double bestDistToTarget = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -1, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, 2, SCAN_RADIUS))) {
            if (pos.distSqr(origin) > REACH_SQR) {
                continue;
            }
            double distToTarget = pos.distToCenterSqr(target.position());
            if (distToTarget > radiusSqr) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !ModBlockTags.canBreak(state, level)) {
                continue;
            }
            if (distToTarget < bestDistToTarget) {
                bestDistToTarget = distToTarget;
                best = pos.immutable();
            }
        }

        this.targetPos = best;
        return best != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) {
            return false;
        }
        BlockState state = zombie.level().getBlockState(targetPos);
        return !state.isAir() && ModBlockTags.canBreak(state, zombie.level()) && targetPos.closerToCenterThan(zombie.position(), Math.sqrt(REACH_SQR) + 1.0);
    }

    @Override
    public void start() {
        breakProgress = 0;
        lastBroadcastStage = -1;
    }

    @Override
    public void stop() {
        if (targetPos != null) {
            zombie.level().destroyBlockProgress(zombie.getId(), targetPos, -1);
        }
        targetPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            return;
        }
        zombie.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        if (!zombie.swinging && zombie.getRandom().nextInt(4) == 0) {
            zombie.swing(zombie.getUsedItemHand());
        }

        BlockState state = zombie.level().getBlockState(targetPos);
        int breakTicks = ModBlockTags.breakTicks(state, Config.blockBreakTicks);
        if (NightBuffGoal.isActive(zombie)) {
            breakTicks = Math.max(1, (int) (breakTicks * Config.nightBlockBreakSpeedMultiplier));
        }

        breakProgress++;
        int stage = Mth.clamp((int) ((float) breakProgress / breakTicks * 10.0F), 0, 9);
        if (stage != lastBroadcastStage) {
            zombie.level().destroyBlockProgress(zombie.getId(), targetPos, stage);
            lastBroadcastStage = stage;
        }

        if (breakProgress >= breakTicks) {
            zombie.level().destroyBlock(targetPos, Config.dropsBlockLoot, zombie);
            targetPos = null;
        }
    }
}
