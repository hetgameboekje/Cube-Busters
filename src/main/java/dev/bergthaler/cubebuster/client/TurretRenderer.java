package dev.bergthaler.cubebuster.client;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.Turret;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder visuals: reuses the vanilla player/humanoid model wholesale (wrong proportions for a "turret", but
 * wires up a working renderer + texture without needing a bespoke model) with a dedicated texture. Swap for a
 * real dispenser/turret-shaped model later - see newmechanics.md "Turret" section, flagged there as a follow-up.
 */
@OnlyIn(Dist.CLIENT)
public class TurretRenderer extends MobRenderer<Turret, HumanoidModel<Turret>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Cubebuster.MODID, "textures/entity/turret.png");

    public TurretRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Turret entity) {
        return TEXTURE;
    }
}
