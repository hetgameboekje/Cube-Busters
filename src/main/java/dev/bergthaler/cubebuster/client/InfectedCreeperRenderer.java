package dev.bergthaler.cubebuster.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.InfectedCreeper;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla Creeper model and swelling-scale animation wholesale (duplicated here rather than
 * extending {@link net.minecraft.client.renderer.entity.CreeperRenderer} - that class is hardwired to
 * {@code Creeper}, not generic over subtypes, same reason GreenZombieRenderer can't extend a vanilla renderer
 * either) - only the texture is custom. No CreeperPowerLayer: InfectedCreeper never deals blast damage, so a
 * lightning-charged one isn't meaningfully different and isn't worth the extra layer.
 */
@OnlyIn(Dist.CLIENT)
public class InfectedCreeperRenderer extends MobRenderer<InfectedCreeper, CreeperModel<InfectedCreeper>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/infected_creeper.png");

    public InfectedCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }

    @Override
    protected void scale(InfectedCreeper entity, PoseStack poseStack, float partialTick) {
        float f = entity.getSwelling(partialTick);
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        poseStack.scale(f2, f3, f2);
    }

    @Override
    protected float getWhiteOverlayProgress(InfectedCreeper entity, float partialTick) {
        float f = entity.getSwelling(partialTick);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedCreeper entity) {
        return TEXTURE;
    }
}
