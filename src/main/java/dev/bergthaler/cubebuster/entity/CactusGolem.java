package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A melee-only utility golem: "hugs" (attacks) zombie-family mobs to death - {@link Zombie} and everything that
 * extends it, including Husks, Drowned, and every cubebuster zombie variant - and never targets players.
 * <p>
 * Base AI is modelled on vanilla {@link IronGolem}/SnowGolem goal setups rather than the zombie AI goals used
 * elsewhere in this mod: {@link #registerGoals()} fully replaces IronGolem's village-defense goals with a plain
 * wander/look/melee-attack set, since this golem has no village to defend.
 * <p>
 * Can carry one cactus-limb item (see {@link ModItems#CACTUS_LIMB}) in its MAINHAND equipment slot (set via
 * right-clicking the golem with one in hand - see {@link #mobInteract}). That's a real equipment slot, not just
 * bookkeeping: vanilla's own loot-context code already reads the killer's mainhand enchantments when rolling
 * drops, so a cactus-limb item with a Looting enchantment on it transfers that level to this golem's kills for
 * free, no custom loot code needed. Persisted automatically by {@link net.minecraft.world.entity.Mob}'s normal
 * equipment save/load - no extra NBT handling required here.
 */
public class CactusGolem extends IronGolem {

    public CactusGolem(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        // Read live rather than baked into createAttributes(), which runs at mod-bus registration time (config
        // may not be loaded yet) - same "config checked live" spirit as the AI goals elsewhere in this mod.
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Config.cactusGolemMaxHealth);
            this.setHealth((float) Config.cactusGolemMaxHealth);
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Config.cactusGolemAttackDamage);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return IronGolem.createAttributes();
    }

    @Override
    protected void registerGoals() {
        // Deliberately does not call super.registerGoals() - IronGolem's own goals are all village-defense
        // flavored (MoveTowardsTargetGoal at a village, DefendVillageTargetGoal, ...), none of which apply here.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.CACTUS_LIMB.get()) && this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            if (!this.level().isClientSide) {
                ItemStack limb = held.split(1);
                this.setItemSlot(EquipmentSlot.MAINHAND, limb);
                this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty() && player.isSecondaryUseActive() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            if (!this.level().isClientSide) {
                ItemStack limb = this.getItemBySlot(EquipmentSlot.MAINHAND);
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(limb);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}
