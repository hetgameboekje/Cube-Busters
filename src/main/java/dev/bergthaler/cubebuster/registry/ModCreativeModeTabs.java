package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cubebuster.MODID);

    // Icon is plain stone bricks, not anything of ours - just a themed icon for the tab, not a mod item.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CUBEBUSTER_TAB =
            CREATIVE_MODE_TABS.register("cubebuster_tab",
                    () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                            .title(Component.translatable("creativetab.cubebuster"))
                            .icon(() -> new ItemStack(Items.STONE_BRICKS))
                            .displayItems((params, output) -> {
                                output.accept(ModItems.SIEGE_ZOMBIE_SPAWN_EGG.get());
                                output.accept(ModItems.GREEN_ZOMBIE_SPAWN_EGG.get());
                                output.accept(ModItems.BLUE_ZOMBIE_SPAWN_EGG.get());
                                output.accept(ModItems.RED_ZOMBIE_SPAWN_EGG.get());
                                output.accept(ModItems.ENDER_ZOMBIE_SPAWN_EGG.get());
                                output.accept(ModItems.SCREAMER_SPAWN_EGG.get());
                                output.accept(ModItems.SIEGE_PICKAXE.get());
                                output.accept(ModItems.PROTECTED_GLASS_T1.get());
                                output.accept(ModItems.PROTECTED_GLASS_T2.get());
                                output.accept(ModItems.PROTECTED_GLASS_T3.get());
                                output.accept(ModItems.PROTECTED_GLASS_PANE_T1.get());
                                output.accept(ModItems.PROTECTED_GLASS_PANE_T2.get());
                                output.accept(ModItems.PROTECTED_GLASS_PANE_T3.get());
                                output.accept(ModItems.TURRET.get());
                                output.accept(ModItems.SENTRY_TURRET.get());
                                output.accept(ModItems.THORN_AMMO.get());
                            })
                            .build());

    private ModCreativeModeTabs() {
    }
}
