package dev.bergthaler.cubebuster.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.HordeBoss;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla zombie model, scaled up, so the Horde Boss reads as bigger/tankier at a glance without
 * needing a bespoke model - texture is a placeholder (functional correctness over art, per project convention)
 * until a dedicated one is made.
 */
@OnlyIn(Dist.CLIENT)
public class HordeBossRenderer extends AbstractZombieRenderer<HordeBoss, ZombieModel<HordeBoss>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/horde_boss.png");
    private static final float SCALE = 1.6F;

    public HordeBossRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))
        );
    }

    @Override
    public ResourceLocation getTextureLocation(HordeBoss entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(HordeBoss entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
