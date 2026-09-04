package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
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

    // Admin/testing spawn egg - in normal play HordeBoss is only force-spawned by HordeBossSpawnHandler.
    public static final DeferredItem<DeferredSpawnEggItem> HORDE_BOSS_SPAWN_EGG = ITEMS.register("horde_boss_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.HORDE_BOSS, 0x4A0E0E, 0xB02E26, new Item.Properties()));

    private ModItems() {
    }
}
