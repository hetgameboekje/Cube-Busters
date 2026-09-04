package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.BlueZombie;
import dev.bergthaler.cubebuster.entity.EnderZombie;
import dev.bergthaler.cubebuster.entity.GreenZombie;
import dev.bergthaler.cubebuster.entity.HordeBoss;
import dev.bergthaler.cubebuster.entity.RedZombie;
import dev.bergthaler.cubebuster.entity.Screamer;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Cubebuster.MODID);

    // Same hitbox/eye-height as vanilla zombie so it fits through the same 1x2 spaces and doors.
    public static final DeferredHolder<EntityType<?>, EntityType<SiegeZombie>> SIEGE_ZOMBIE = ENTITY_TYPES.register("siege_zombie",
            () -> EntityType.Builder.<SiegeZombie>of(SiegeZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("siege_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<GreenZombie>> GREEN_ZOMBIE = ENTITY_TYPES.register("green_zombie",
            () -> EntityType.Builder.<GreenZombie>of(GreenZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("green_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlueZombie>> BLUE_ZOMBIE = ENTITY_TYPES.register("blue_zombie",
            () -> EntityType.Builder.<BlueZombie>of(BlueZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("blue_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<RedZombie>> RED_ZOMBIE = ENTITY_TYPES.register("red_zombie",
            () -> EntityType.Builder.<RedZombie>of(RedZombie::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("red_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderZombie>> ENDER_ZOMBIE = ENTITY_TYPES.register("ender_zombie",
            () -> EntityType.Builder.<EnderZombie>of(EnderZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("ender_zombie"));

    // Force-spawned only by the aggro system (AggroSpawnHandler) - no natural spawn placement.
    public static final DeferredHolder<EntityType<?>, EntityType<Screamer>> SCREAMER = ENTITY_TYPES.register("screamer",
            () -> EntityType.Builder.<Screamer>of(Screamer::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("screamer"));

    // Force-spawned only by the Horde Boss integration layer (HordeBossSpawnHandler) - no natural spawn
    // placement. Bigger hitbox than the other variants so it reads as a boss at a glance.
    public static final DeferredHolder<EntityType<?>, EntityType<HordeBoss>> HORDE_BOSS = ENTITY_TYPES.register("horde_boss",
            () -> EntityType.Builder.<HordeBoss>of(HordeBoss::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F)
                    .eyeHeight(2.3F)
                    .clientTrackingRange(10)
                    .build("horde_boss"));

    private ModEntityTypes() {
    }
}
