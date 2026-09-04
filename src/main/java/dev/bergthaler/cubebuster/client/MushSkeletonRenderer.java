package dev.bergthaler.cubebuster.client;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.MushSkeleton;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla skeleton model/armor layers wholesale (SkeletonRenderer is generic over
 * {@code AbstractSkeleton} subtypes, unlike CreeperRenderer/AbstractZombieRenderer's fixed type params) - only
 * the texture is custom.
 */
@OnlyIn(Dist.CLIENT)
public class MushSkeletonRenderer extends SkeletonRenderer<MushSkeleton> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/mush_skeleton.png");

    public MushSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MushSkeleton entity) {
        return TEXTURE;
    }
}
