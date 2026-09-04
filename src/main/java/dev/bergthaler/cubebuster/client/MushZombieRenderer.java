package dev.bergthaler.cubebuster.client;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.MushZombie;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla zombie model wholesale - only the texture is custom.
 */
@OnlyIn(Dist.CLIENT)
public class MushZombieRenderer extends AbstractZombieRenderer<MushZombie, ZombieModel<MushZombie>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/mush_zombie.png");

    public MushZombieRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))
        );
    }

    @Override
    public ResourceLocation getTextureLocation(MushZombie entity) {
        return TEXTURE;
    }
}
