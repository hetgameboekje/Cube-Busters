package dev.bergthaler.cubebuster.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Block tags used to configure SiegeZombie block-breaking via datapacks, under the "zombiemechanics" namespace.
 * <p>
 * Tag membership on a {@link BlockState} is a simple bitset lookup baked once per state when tags are (re)loaded
 * (see {@code BlockState#is(TagKey)}), so there is nothing to hand-roll here for performance - a manual cache
 * would just duplicate what vanilla already precomputes.
 */
public final class ModBlockTags {
    private static final String NAMESPACE = "zombiemechanics";

    public static final TagKey<Block> BREAKABLE = tag("breakable");
    public static final TagKey<Block> UNBREAKABLE = tag("unbreakable");
    public static final TagKey<Block> FRAGILE_GLASS = tag("fragile_glass");
    public static final TagKey<Block> REINFORCED_GLASS = tag("reinforced_glass");

    /**
     * Tiered protection, independent of {@link #BREAKABLE}/{@link #UNBREAKABLE} - see
     * {@link #canBreak(BlockState, Level)} for the day/night and per-dimension rules each tier applies, and
     * {@link dev.bergthaler.cubebuster.Config} for the toggles.
     */
    public static final TagKey<Block> PROTECTED_TIER1 = tag("protected_tier1");
    public static final TagKey<Block> PROTECTED_TIER2 = tag("protected_tier2");
    public static final TagKey<Block> PROTECTED_TIER3 = tag("protected_tier3");

    /**
     * Blocks counted by the Horde Boss trigger's mush-density scan (see {@code event.HordeBossDensity}). Lives
     * under the {@code cubebuster} namespace (not {@value #NAMESPACE}) since it isn't part of the
     * SiegeZombie block-break tag family above - it's a plain membership tag for a different system.
     * <p>
     * <b>Placeholder dependency:</b> the actual Mush/Infected mechanic (mush blocks, spore spread) is designed
     * but not yet merged into this branch (separate PR). Until that PR lands and tags its real mush block(s)
     * into {@code data/cubebuster/tags/block/mush_blocks.json}, that file instead tags
     * {@code minecraft:brown_mushroom_block} as a harmless placeholder purely so the density-scan code has
     * something real to count during testing. Remove the placeholder entry once real mush blocks exist.
     */
    public static final TagKey<Block> MUSH_BLOCKS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("cubebuster", "mush_blocks"));

    /**
     * Hardcoded on top of the datapack tags so a modpack/datapack mistake (or a missing tag entry) can never
     * make these breakable. These are the blocks explicitly called out as "must never break".
     */
    private static final Set<Block> HARDCODED_UNBREAKABLE = Set.of(
            Blocks.BEDROCK,
            Blocks.END_PORTAL_FRAME,
            Blocks.END_PORTAL,
            Blocks.END_GATEWAY,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.STRUCTURE_BLOCK,
            Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.BARREL,
            Blocks.SHULKER_BOX,
            Blocks.BEACON,
            Blocks.ENCHANTING_TABLE,
            Blocks.RESPAWN_ANCHOR,
            // Obsidian is treated as unbreakable outright rather than trying to detect "is this obsidian part of
            // a portal frame" - that distinction isn't reliably determinable from block state alone.
            Blocks.OBSIDIAN,
            Blocks.CRYING_OBSIDIAN,
            Blocks.NETHER_PORTAL
    );

    private ModBlockTags() {
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(NAMESPACE, path));
    }

    /** Whether a SiegeZombie is allowed to break this block, at this position, right now. */
    public static boolean canBreak(BlockState state, Level level) {
        Block block = state.getBlock();
        if (HARDCODED_UNBREAKABLE.contains(block)) {
            return false;
        }
        if (state.is(UNBREAKABLE) || state.is(REINFORCED_GLASS)) {
            return false;
        }
        // User-configurable blacklist (siegeZombieUnbreakableBlocks), so end users can add e.g. modded
        // containers without needing a datapack.
        if (dev.bergthaler.cubebuster.Config.unbreakableBlocks.contains(block)) {
            return false;
        }
        for (TagKey<Block> configTag : dev.bergthaler.cubebuster.Config.unbreakableBlockTags) {
            if (state.is(configTag)) {
                return false;
            }
        }
        // Beds are a pair of blocks with no single vanilla tag catching both halves reliably in all cases,
        // so fold them into the hardcoded check via their tag rather than listing every bed block by hand.
        if (state.is(net.minecraft.tags.BlockTags.BEDS)) {
            return false;
        }
        if (state.is(PROTECTED_TIER3)) {
            return !dev.bergthaler.cubebuster.Config.enableTier3FullyProtected;
        }
        if (state.is(PROTECTED_TIER2)) {
            if (!dev.bergthaler.cubebuster.Config.enableTier2NightOnlyOverworld) {
                return false;
            }
            if (level.dimension() == Level.NETHER) {
                return true;
            }
            if (level.dimension() == Level.END) {
                return false;
            }
            return level.isNight();
        }
        if (state.is(PROTECTED_TIER1)) {
            return dev.bergthaler.cubebuster.Config.enableTier1AlwaysBreakable;
        }
        return state.is(BREAKABLE) || state.is(FRAGILE_GLASS);
    }

    /** How many ticks it should take to break this block, scaled off the configured base duration. */
    public static int breakTicks(BlockState state, int baseTicks) {
        if (state.is(FRAGILE_GLASS)) {
            return Math.max(1, baseTicks / 4);
        }
        return baseTicks;
    }
}
