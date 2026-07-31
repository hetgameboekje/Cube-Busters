package dev.bergthaler.cubebuster.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.BlueZombie;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Reuses the vanilla Drowned model/swim-tilt animation (see {@link net.minecraft.client.renderer.entity.DrownedRenderer})
 * wholesale - only the texture is custom.
 */
@OnlyIn(Dist.CLIENT)
public class BlueZombieRenderer extends AbstractZombieRenderer<BlueZombie, DrownedModel<BlueZombie>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/water_siege_zombie.png");

    public BlueZombieRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED)),
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR))
        );
        this.addLayer(new DrownedOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(BlueZombie entity, PoseStack pose, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, pose, bob, yBodyRot, partialTick, scale);
        float swimAmount = entity.getSwimAmount(partialTick);
        if (swimAmount > 0.0F) {
            float targetAngle = -10.0F - entity.getXRot();
            float angle = Mth.lerp(swimAmount, 0.0F, targetAngle);
            pose.rotateAround(Axis.XP.rotationDegrees(angle), 0.0F, entity.getBbHeight() / 2.0F / scale, 0.0F);
        }
    }
}
