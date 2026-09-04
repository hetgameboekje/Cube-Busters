package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.item.AntibioticFireworkItem;
import dev.bergthaler.cubebuster.item.InfectionPotionItem;
import dev.bergthaler.cubebuster.item.MortarAndPestleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cubebuster.MODID);

    // Netherite tier for mining speed; the Unbreakable component means its durability value never actually matters.
    public static final DeferredItem<PickaxeItem> SIEGE_PICKAXE = ITEMS.register("siege_pickaxe",
            () -> new PickaxeItem(Tiers.NETHERITE, new Item.Properties()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
                    .rarity(Rarity.EPIC)));

    // Same base/dot layout as the vanilla zombie egg, just recoloured red - DeferredSpawnEggItem (rather than
    // plain SpawnEggItem) resolves the entity type lazily, so it doesn't matter whether Items or EntityTypes
    // finishes registering first.
    public static final DeferredItem<DeferredSpawnEggItem> SIEGE_ZOMBIE_SPAWN_EGG = ITEMS.register("siege_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.SIEGE_ZOMBIE, 0xB02E26, 0x450A0A, new Item.Properties()));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_T1 =
            ITEMS.registerSimpleBlockItem("protected_glass_t1", ModBlocks.PROTECTED_GLASS_T1);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_T2 =
            ITEMS.registerSimpleBlockItem("protected_glass_t2", ModBlocks.PROTECTED_GLASS_T2);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_T3 =
            ITEMS.registerSimpleBlockItem("protected_glass_t3", ModBlocks.PROTECTED_GLASS_T3);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_PANE_T1 =
            ITEMS.registerSimpleBlockItem("protected_glass_pane_t1", ModBlocks.PROTECTED_GLASS_PANE_T1);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_PANE_T2 =
            ITEMS.registerSimpleBlockItem("protected_glass_pane_t2", ModBlocks.PROTECTED_GLASS_PANE_T2);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> PROTECTED_GLASS_PANE_T3 =
            ITEMS.registerSimpleBlockItem("protected_glass_pane_t3", ModBlocks.PROTECTED_GLASS_PANE_T3);

    public static final DeferredItem<DeferredSpawnEggItem> GREEN_ZOMBIE_SPAWN_EGG = ITEMS.register("green_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.GREEN_ZOMBIE, 0x2E7D32, 0x145A18, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> BLUE_ZOMBIE_SPAWN_EGG = ITEMS.register("blue_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.BLUE_ZOMBIE, 0x1565C0, 0x0D3A75, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> RED_ZOMBIE_SPAWN_EGG = ITEMS.register("red_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.RED_ZOMBIE, 0xB71C1C, 0x3E0A0A, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> ENDER_ZOMBIE_SPAWN_EGG = ITEMS.register("ender_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ENDER_ZOMBIE, 0x1A1A1A, 0x6A1B9A, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> SCREAMER_SPAWN_EGG = ITEMS.register("screamer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.SCREAMER, 0xFF00FF, 0xFFB3DE, new Item.Properties()));

    // --- Mush / Infected mechanic ---

    public static final DeferredItem<net.minecraft.world.item.BlockItem> MUSH_BLOCK =
            ITEMS.registerSimpleBlockItem("mush_block", ModBlocks.MUSH_BLOCK);

    // Cure-chain reagent: harvested from MushBlock with a hoe (see the mush_block loot table).
    public static final DeferredItem<Item> MUSH_BALL = ITEMS.registerSimpleItem("mush_ball");

    // Wrong-tool harvest result from MushBlock - see InfectionPotionItem's doc for why this isn't a brewing
    // potion (no-alcohol/no-brewing-stand constraint on this whole mechanic).
    public static final DeferredItem<InfectionPotionItem> INFECTION_POTION = ITEMS.register("infection_potion",
            () -> new InfectionPotionItem(new Item.Properties().stacksTo(16)));

    // Generic ground-fine reagent from the mortar & pestle's sand-grinding use - not consumed by anything yet,
    // just a base ingredient for future recipes (see the design-call note in newmechanics.md/CLAUDE.md).
    public static final DeferredItem<Item> DUST = ITEMS.registerSimpleItem("dust");

    // A hand tool, not a crafting-grid reagent - see its own class doc for why it uses right-click grinding
    // instead of a craft-remainder recipe ingredient.
    public static final DeferredItem<MortarAndPestleItem> MORTAR_AND_PESTLE = ITEMS.register("mortar_and_pestle",
            () -> new MortarAndPestleItem(new Item.Properties().stacksTo(1)));

    // Intermediate cure-chain step: Mush Ball ground in a mortar & pestle. Assembled into an Antibiotic
    // Firework via ordinary crafting (see data/cubebuster/recipe/antibiotic_firework.json).
    public static final DeferredItem<Item> ANTIBIOTIC_PASTE = ITEMS.registerSimpleItem("antibiotic_paste");

    // The cure itself - see AntibioticFireworkItem for its use behaviour.
    public static final DeferredItem<AntibioticFireworkItem> ANTIBIOTIC_FIREWORK = ITEMS.register("antibiotic_firework",
            () -> new AntibioticFireworkItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<DeferredSpawnEggItem> INFECTED_CREEPER_SPAWN_EGG = ITEMS.register("infected_creeper_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.INFECTED_CREEPER, 0x5B7B4A, 0x2E4A1F, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> MUSH_ZOMBIE_SPAWN_EGG = ITEMS.register("mush_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.MUSH_ZOMBIE, 0x6B8E23, 0x3D5115, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> MUSH_SKELETON_SPAWN_EGG = ITEMS.register("mush_skeleton_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.MUSH_SKELETON, 0xC7C7A6, 0x6B8E23, new Item.Properties()));

    private ModItems() {
    }
}
