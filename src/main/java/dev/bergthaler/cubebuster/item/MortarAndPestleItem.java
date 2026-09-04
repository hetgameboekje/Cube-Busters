package dev.bergthaler.cubebuster.item;

import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A hand tool, not a crafting-grid ingredient - grinding happens via right-click, not a consumed recipe slot, so
 * the tool is never used up (see the class doc on why: {@code Item#getCraftingRemainingItem()} is {@code final}
 * in vanilla and can only be set via a remainder item that already exists at this item's own construction time,
 * which a self-remainder can't satisfy without extra indirection - a right-click behaviour sidesteps that
 * entirely, and the design brief explicitly allows either approach).
 * <p>
 * Two independent conversion tables:
 * <ul>
 *     <li>{@link #useOn} - right-click a world block (cobblestone/gravel/dirt) to grind it in place.</li>
 *     <li>{@link #use} - right-click empty air while holding an item in the other hand (sand, a dye source,
 *     a Mush Ball) to grind that held item.</li>
 * </ul>
 * Both tables are built lazily (not as static fields) since they reference other mod items via
 * {@code DeferredItem#get()}, which isn't safe to call before the registry event has fired.
 */
public class MortarAndPestleItem extends Item {
    public MortarAndPestleItem(Properties properties) {
        super(properties);
    }

    // Block -> output block, for grinding world blocks in place.
    private static Map<Block, Block> blockGrindMap() {
        return Map.of(
                Blocks.COBBLESTONE, Blocks.GRAVEL,
                Blocks.GRAVEL, Blocks.SAND,
                Blocks.DIRT, Blocks.SAND
        );
    }

    // Item -> (output item, output count), for grinding held items.
    private static Map<Item, Supplier<ItemStack>> itemGrindMap() {
        return Map.of(
                Blocks.SAND.asItem(), () -> new ItemStack(ModItems.DUST.get()),
                Items.CACTUS, () -> new ItemStack(Items.GREEN_DYE, 2),
                ModItems.MUSH_BALL.get(), () -> new ItemStack(ModItems.ANTIBIOTIC_PASTE.get())
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block output = blockGrindMap().get(state.getBlock());
        if (output == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, output.defaultBlockState());
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (context.getPlayer() != null) {
            context.getPlayer().swing(context.getHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack mortar = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        if (target.isEmpty()) {
            return InteractionResultHolder.pass(mortar);
        }
        Supplier<ItemStack> outputSupplier = itemGrindMap().get(target.getItem());
        if (outputSupplier == null) {
            return InteractionResultHolder.pass(mortar);
        }
        if (!level.isClientSide) {
            ItemStack output = outputSupplier.get();
            target.shrink(1);
            if (target.isEmpty()) {
                player.setItemInHand(otherHand, output);
            } else if (!player.getInventory().add(output)) {
                player.drop(output, false);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(mortar, level.isClientSide);
    }
}
