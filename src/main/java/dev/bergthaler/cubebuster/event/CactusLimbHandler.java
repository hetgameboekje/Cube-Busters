package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.CactusGolem;
import dev.bergthaler.cubebuster.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime behaviour for {@link ModItems#CACTUS_LIMB}: elytra-style degrade on every hit its wielding
 * {@link CactusGolem} lands, Looting transfer to that golem's kills, and a fixed-cost anvil repair with
 * {@link ModItems#THORNS} (vanilla's own scaling repair-cost formula is bypassed entirely by always overriding
 * the anvil's computed cost).
 * <p>
 * Looting transfer can't just rely on vanilla reading the golem's MAINHAND equipment slot at loot time - vanilla's
 * own Looting enchantment effect (`equipment_drops`) is hard-gated to attackers that are players (see
 * data/minecraft/enchantment/looting.json's `minecraft:entity_properties` requirement), so a golem holding an
 * enchanted item gets nothing from that path. Instead, {@link #onLivingDrops} reads the Looting level directly off
 * the golem's limb and manually duplicates drops with a per-level chance, approximating (not byte-for-byte
 * reproducing) vanilla's own loot-table reroll.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class CactusLimbHandler {

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof CactusGolem golem) {
            ItemStack limb = golem.getItemBySlot(EquipmentSlot.MAINHAND);
            if (limb.is(ModItems.CACTUS_LIMB.get()) && Config.cactusGolemLimbDamagePerHit > 0) {
                int max = limb.getMaxDamage();
                if (max > 0) {
                    // Deliberately not ItemStack#hurtAndBreak - that triggers vanilla's break/vanish callback at
                    // max damage. Setting the damage value directly lets it sit at max damage indefinitely,
                    // mirroring an elytra: still present, just non-functional (see the Looting check below).
                    int newDamage = Math.min(limb.getDamageValue() + Config.cactusGolemLimbDamagePerHit, max);
                    limb.setDamageValue(newDamage);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof CactusGolem golem)) {
            return;
        }
        ItemStack limb = golem.getItemBySlot(EquipmentSlot.MAINHAND);
        // Maxed-out damage = "stops functioning" (elytra-style), same as a normal repair check.
        if (!limb.is(ModItems.CACTUS_LIMB.get()) || limb.getDamageValue() >= limb.getMaxDamage()) {
            return;
        }
        Level level = golem.level();
        Holder<Enchantment> looting = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING);
        int lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(looting, limb);
        if (lootingLevel <= 0) {
            return;
        }
        RandomSource random = golem.getRandom();
        List<ItemEntity> extraDrops = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            for (int i = 0; i < lootingLevel; i++) {
                if (random.nextFloat() < 0.5F) {
                    extraDrops.add(new ItemEntity(level, drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy()));
                }
            }
        }
        event.getDrops().addAll(extraDrops);
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!left.is(ModItems.CACTUS_LIMB.get()) || !right.is(ModItems.THORNS.get())) {
            return;
        }
        if (left.getDamageValue() <= 0) {
            return;
        }
        ItemStack output = left.copy();
        output.setDamageValue(0);
        event.setOutput(output);
        event.setCost(Config.cactusGolemAnvilRepairCostLevels);
        event.setMaterialCost(1);
    }

    private CactusLimbHandler() {
    }
}
