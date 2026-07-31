package dev.bergthaler.cubebuster.entity;

import dev.bergthaler.cubebuster.entity.ai.ChorusFruitTeleportGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * The End siege variant. Doesn't break blocks - instead it holds a single chorus fruit (offhand, purely visual)
 * that {@link ChorusFruitTeleportGoal} spends once to teleport itself, usually near its target, occasionally
 * further away. Once spent, it's just a regular zombie with no more tricks.
 */
public class EnderZombie extends Zombie {
    private boolean usedChorusFruit;

    public EnderZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.CHORUS_FRUIT));
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(3, new ChorusFruitTeleportGoal(this));
    }

    public boolean hasUsedChorusFruit() {
        return usedChorusFruit;
    }

    public void consumeChorusFruit() {
        usedChorusFruit = true;
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }
}
