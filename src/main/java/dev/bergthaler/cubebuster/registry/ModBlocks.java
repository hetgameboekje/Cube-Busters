package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.block.MushBlock;
import dev.bergthaler.cubebuster.block.BarbedWireBlock;
import dev.bergthaler.cubebuster.block.CollapsingTrapdoorBlock;
import dev.bergthaler.cubebuster.block.ThornedBushBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Protected building blocks. Behaviour (which tiers are breakable when/where) lives entirely in
 * {@link dev.bergthaler.cubebuster.tags.ModBlockTags#canBreak} - membership in the
 * zombiemechanics:protected_tierN tags (see src/main/resources/data/zombiemechanics/tags/block) is what actually
 * makes a block Tier 2/3, not the Java class. {@link ProtectedGlassBlock} is otherwise identical to vanilla clear
 * glass ({@code Blocks.GLASS}, a {@link TransparentBlock}) - same rendering/light/sound behaviour - just tougher
 * and tool-gated: needs an iron pickaxe or better to harvest (see the minecraft:needs_iron_tool block tag), and
 * drops itself unconditionally once that's met, no Silk Touch required. Zombie breaking bypasses hardness and
 * tool checks entirely (it calls {@code Level#destroyBlock} directly), so none of this affects it.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Cubebuster.MODID);

    public static final DeferredBlock<Block> PROTECTED_GLASS_T1 = BLOCKS.registerBlock("protected_glass_t1",
            ProtectedGlassBlock::new, glassProperties(MapColor.COLOR_GREEN));

    public static final DeferredBlock<Block> PROTECTED_GLASS_T2 = BLOCKS.registerBlock("protected_glass_t2",
            ProtectedGlassBlock::new, glassProperties(MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<Block> PROTECTED_GLASS_T3 = BLOCKS.registerBlock("protected_glass_t3",
            ProtectedGlassBlock::new, glassProperties(MapColor.COLOR_PURPLE));

    public static final DeferredBlock<Block> PROTECTED_GLASS_PANE_T1 = BLOCKS.registerBlock("protected_glass_pane_t1",
            ProtectedGlassPaneBlock::new, glassProperties(MapColor.COLOR_GREEN));

    public static final DeferredBlock<Block> PROTECTED_GLASS_PANE_T2 = BLOCKS.registerBlock("protected_glass_pane_t2",
            ProtectedGlassPaneBlock::new, glassProperties(MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<Block> PROTECTED_GLASS_PANE_T3 = BLOCKS.registerBlock("protected_glass_pane_t3",
            ProtectedGlassPaneBlock::new, glassProperties(MapColor.COLOR_PURPLE));

    // Mush / Infected mechanic - see MushBlock's own doc for the spread behaviour. Not solid (partial-height
    // layer collision, like snow), not a spawn platform, and randomly ticks so it can grow on its own.
    public static final DeferredBlock<Block> MUSH_BLOCK = BLOCKS.registerBlock("mush_block",
            MushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .randomTicks()
                    .strength(0.2F));

    // --- Cactus Economy blocks ---

    // Thin, non-solid hazard: same "walkable, but slowed and hurt while inside" shape as vanilla cobweb, with
    // damage-over-time added in BarbedWireBlock#entityInside. Easy early-game perimeter defense.
    public static final DeferredBlock<Block> BARBED_WIRE = BLOCKS.registerBlock("barbed_wire",
            BarbedWireBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .noCollission()
                    .strength(1.5F)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY));

    // A plantable, cactus-like hazard bush - damages entities that touch it, but (unlike cactus) doesn't need
    // sand and isn't a full block.
    public static final DeferredBlock<Block> THORNED_BUSH = BLOCKS.registerBlock("thorned_bush",
            ThornedBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY));

    // A trapdoor that springs open on its own a short time after something steps on it, then re-closes and
    // resets itself - a reusable floor trap rather than a one-shot block.
    public static final DeferredBlock<Block> COLLAPSING_TRAPDOOR = BLOCKS.registerBlock("collapsing_trapdoor",
            CollapsingTrapdoorBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    // Plain building block, crafted from shaved cactus + planks - functionally identical to vanilla planks.
    public static final DeferredBlock<Block> CACTUS_PLANKS = BLOCKS.registerSimpleBlock("cactus_planks",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    private static BlockBehaviour.Properties glassProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .instrument(NoteBlockInstrument.HAT)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .requiresCorrectToolForDrops()
                // Tougher than vanilla glass (0.3) and reasonably blast-resistant, but genuinely breakable -
                // tool gating (needs_iron_tool) is what actually keeps it out of reach of weak tools.
                .strength(3.0F, 30.0F);
    }

    /** Only exists because {@link TransparentBlock}'s constructor is protected. */
    private static class ProtectedGlassBlock extends TransparentBlock {
        ProtectedGlassBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }
    }

    /** Only exists because {@link IronBarsBlock}'s constructor is protected - vanilla glass panes use it too. */
    private static class ProtectedGlassPaneBlock extends IronBarsBlock {
        ProtectedGlassPaneBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }
    }

    private ModBlocks() {
    }
}
