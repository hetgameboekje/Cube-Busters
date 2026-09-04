package dev.bergthaler.cubebuster.item;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * The cure: right-click to set off a small firework burst and, if the player is carrying the mush infection
 * effect, remove it and grant a short Regeneration burst. Works even without the infection (just a firework
 * pop) so it isn't a dead item to hold "just in case".
 */
public class AntibioticFireworkItem extends Item {
    public AntibioticFireworkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            boolean cured = player.removeEffect(ModMobEffects.MUSH_INFECTION);
            if (cured) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        Config.antibioticFireworkCureRegenDurationTicks, Config.antibioticFireworkCureRegenAmplifier));
            }
            serverLevel.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getEyeY(), player.getZ(),
                    24, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, player.getSoundSource(), 1.0F, cured ? 1.2F : 0.9F);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
