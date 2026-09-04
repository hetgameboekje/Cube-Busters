package dev.bergthaler.cubebuster.item;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModMobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * The "wrong tool" harvest result from a MushBlock - a drinkable debuff item, not a brewing-stand potion (the
 * no-alcohol/no-brewing constraint applies to this whole mechanic, so this is a plain custom {@link Item} with
 * its own drink behaviour, not a {@code PotionItem}). Drinking it applies the mush infection effect to self.
 */
public class InfectionPotionItem extends Item {
    public InfectionPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(ModMobEffects.MUSH_INFECTION,
                    Config.infectionEffectDurationTicks, Config.infectionEffectAmplifier));
            level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_DRINK, entity.getSoundSource(), 1.0F, 1.0F);
        }
        stack.shrink(1);
        return stack;
    }
}
