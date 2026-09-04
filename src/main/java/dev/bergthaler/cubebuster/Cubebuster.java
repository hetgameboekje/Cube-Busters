package dev.bergthaler.cubebuster;

import com.mojang.logging.LogUtils;
import dev.bergthaler.cubebuster.client.BlueZombieRenderer;
import dev.bergthaler.cubebuster.client.EnderZombieRenderer;
import dev.bergthaler.cubebuster.client.GreenZombieRenderer;
import dev.bergthaler.cubebuster.client.InfectedCreeperRenderer;
import dev.bergthaler.cubebuster.client.MushSkeletonRenderer;
import dev.bergthaler.cubebuster.client.MushZombieRenderer;
import dev.bergthaler.cubebuster.client.RedZombieRenderer;
import dev.bergthaler.cubebuster.client.ScreamerRenderer;
import dev.bergthaler.cubebuster.client.SiegeZombieRenderer;
import dev.bergthaler.cubebuster.registry.ModAttachmentTypes;
import dev.bergthaler.cubebuster.registry.ModBlocks;
import dev.bergthaler.cubebuster.registry.ModCreativeModeTabs;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import dev.bergthaler.cubebuster.registry.ModItems;
import dev.bergthaler.cubebuster.registry.ModMobEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Cubebuster.MODID)
public class Cubebuster {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cubebuster";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Cubebuster(IEventBus modEventBus, ModContainer modContainer) {
        // Register our Deferred Registers to the mod event bus so items/entity types get registered
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // ModSetupEvents (attributes, spawn placements) and SiegeIntegrationHandler (siege spawn hook) register
        // themselves via @EventBusSubscriber - no wiring needed here.
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            LOGGER.info("CubeBuster loaded - SiegeZombies are active");
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntityTypes.SIEGE_ZOMBIE.get(), SiegeZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.GREEN_ZOMBIE.get(), GreenZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.BLUE_ZOMBIE.get(), BlueZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.RED_ZOMBIE.get(), RedZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.ENDER_ZOMBIE.get(), EnderZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.SCREAMER.get(), ScreamerRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.INFECTED_CREEPER.get(), InfectedCreeperRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.MUSH_ZOMBIE.get(), MushZombieRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.MUSH_SKELETON.get(), MushSkeletonRenderer::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Plain (non-stained) glass, like vanilla's own GlassBlock - cutout render layer, not the default
            // solid one, so the texture's transparent pixels actually show through on the placed block (the
            // item icon was already transparent since item rendering isn't affected by block render layers).
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_T1.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_T2.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_T3.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_PANE_T1.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_PANE_T2.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROTECTED_GLASS_PANE_T3.get(), RenderType.cutout());
            });
        }
    }
}
