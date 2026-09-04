package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Shearing a cactus block yields {@link ModItems#THORNS} + {@link ModItems#SHAVED_CACTUS} (see "Cactus Economy"
 * in CLAUDE.md) instead of vanilla's usual "no effect" for shears-on-cactus. Implemented as an interact handler
 * rather than a loot table, since shearing needs to consume the targeted cactus block (matching how shearing a
 * pumpkin/beehive/vine works vanilla-side) rather than only firing on break.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class CactusShearHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (!held.is(Items.SHEARS)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.CACTUS)) {
            return;
        }

        level.destroyBlock(pos, false);
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.THORNS.get()));
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.SHAVED_CACTUS.get()));
        // Not damaging the shears here (unlike vanilla shearing) - avoids depending on the exact 1.21.1
        // ItemStack#hurtAndBreak overload, which isn't otherwise used anywhere in this codebase to confirm against.
        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private CactusShearHandler() {
    }
}
