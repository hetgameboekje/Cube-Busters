package dev.bergthaler.cubebuster.client;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.Screamer;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla zombie model with the Screamer's own texture ({@code scream_siege.png}).
 */
@OnlyIn(Dist.CLIENT)
public class ScreamerRenderer extends AbstractZombieRenderer<Screamer, ZombieModel<Screamer>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/scream_siege.png");

    public ScreamerRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))
        );
    }

    @Override
    public ResourceLocation getTextureLocation(Screamer entity) {
        return TEXTURE;
    }
}
