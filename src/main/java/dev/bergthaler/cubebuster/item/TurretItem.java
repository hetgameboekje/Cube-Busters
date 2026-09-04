package dev.bergthaler.cubebuster.item;

import dev.bergthaler.cubebuster.entity.Turret;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Deploys a {@link Turret} entity on right-click against a block, the same way vanilla's {@code ArmorStandItem}/
 * {@code BoatItem} place their entity rather than a block. Two variants are registered from this one class
 * (see ModItems) - a plain turret (starts off, needs a manual right-click to activate, see Turret#mobInteract)
 * and a "sentry" turret (starts active immediately) - crafted with different recipes (see
 * data/cubebuster/recipe/turret.json and turret_sentry.json).
 */
public class TurretItem extends Item {
    private final boolean sentry;

    public TurretItem(boolean sentry, Properties properties) {
        super(properties);
        this.sentry = sentry;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos placeAt = context.getClickedPos().relative(context.getClickedFace());
        Turret turret = ModEntityTypes.TURRET.get().create(serverLevel, null, placeAt, MobSpawnType.SPAWN_EGG, true, false);
        if (turret == null) {
            return InteractionResult.FAIL;
        }
        // Face away from whoever placed it, like a dispenser placed by hand - a reasonable default facing since
        // the turret never turns its body, only its head (see Turret#travel/registerGoals immobility notes).
        Direction facing = context.getPlayer() != null
                ? context.getPlayer().getDirection().getOpposite()
                : Direction.NORTH;
        turret.setYRot(facing.toYRot());
        turret.setYHeadRot(facing.toYRot());
        turret.setSentryVariant(this.sentry);
        // Plain turret starts deactivated - the eye-of-ender "sentry" variant is the one that comes online
        // immediately (see the recipe/crafting split in newmechanics.md).
        turret.setActive(this.sentry);
        turret.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(placeAt), MobSpawnType.SPAWN_EGG, null);
        serverLevel.addFreshEntity(turret);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
