package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * Wires the last step of the Cactus Economy's sap -> juice -> mocktail chain into the vanilla brewing stand:
 * cactus juice (registered as a valid bottom-slot "container") filtered with ash (top-slot ingredient) becomes
 * the mocktail. No alcohol anywhere in this chain - the brewing stand is only reused as generic
 * item + reagent -> item plumbing.
 */
@EventBusSubscriber(modid = Cubebuster.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModBrewingRecipes {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addContainer(ModItems.CACTUS_JUICE.get());
        builder.addContainerRecipe(ModItems.CACTUS_JUICE.get(), ModItems.ASH.get(), ModItems.MOCKTAIL.get());
    }

    private ModBrewingRecipes() {
    }
}
