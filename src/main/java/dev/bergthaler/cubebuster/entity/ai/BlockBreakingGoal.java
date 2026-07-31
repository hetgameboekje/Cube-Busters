package dev.bergthaler.cubebuster.entity.ai;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Lets a Zombie (SiegeZombie, BlueZombie, ...) dig through allowed blocks when its normal pathfinding towards its
 * target gets stuck. Nothing in here is Siege-specific - works for any {@link Zombie} subtype.
 * <p>
 * Deliberately conservative for server performance:
 * - only runs its (cheap, ~3x2x3 block) scan when {@link net.minecraft.world.entity.ai.navigation.PathNavigation#isStuck()}
 *   reports the mob isn't making progress - most of the time, most zombies never scan at all.
 * - even then, re-scans are throttled by {@link #SCAN_INTERVAL_TICKS}.
 * - never scans further than melee reach, so it never triggers expensive pathfinding of its own.
 */
public class BlockBreakingGoal extends Goal {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int SCAN_RADIUS = 1;
    private static final double REACH_SQR = 3.0 * 3.0;

    private final Zombie zombie;
    private int scanCooldown;
    private BlockPos targetPos;
    private int breakProgress;
    private int lastBroadcastStage = -1;

    public BlockBreakingGoal(Zombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!Config.breaksBlocks) {
            return false;
        }
        LivingEntity target = zombie.getTarget();
        if (target == null) {
            return false;
        }
        // isStuck() only updates while a path is being actively followed, so a zombie with no path at all
        // (fully sealed in - the exact case this goal exists for) never reports stuck. Treat "no path" the
        // same as "stuck".
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

        // Meaningfully above/below the target: dig straight through the ceiling/floor towards it instead of
        // whatever's nearest in the local box - that's the intended "dig up/down toward me" siege behaviour.
        double dy = target.getY() - zombie.getY();
        if (Math.abs(dy) > 1.5) {
            BlockPos vertical = dy > 0 ? origin.above(2) : origin.below();
            BlockState verticalState = level.getBlockState(vertical);
            if (!verticalState.isAir() && ModBlockTags.canBreak(verticalState, level)) {
                this.targetPos = vertical.immutable();
                return true;
            }
        }

        BlockPos best = null;
        double bestDistToTarget = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -1, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, 2, SCAN_RADIUS))) {
            if (pos.distSqr(origin) > REACH_SQR) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !ModBlockTags.canBreak(state, level)) {
                continue;
            }
            double distToTarget = pos.distToCenterSqr(target.position());
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
        if (zombie.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            breakTicks = Math.max(1, (int) (breakTicks * Config.enrageBreakSpeedMultiplier));
        }
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
