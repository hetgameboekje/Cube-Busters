package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.BlueZombie;
import dev.bergthaler.cubebuster.entity.CactusGolem;
import dev.bergthaler.cubebuster.entity.EnderZombie;
import dev.bergthaler.cubebuster.entity.GreenZombie;
import dev.bergthaler.cubebuster.entity.InfectedCreeper;
import dev.bergthaler.cubebuster.entity.MushSkeleton;
import dev.bergthaler.cubebuster.entity.MushZombie;
import dev.bergthaler.cubebuster.entity.HordeBoss;
import dev.bergthaler.cubebuster.entity.RedZombie;
import dev.bergthaler.cubebuster.entity.Screamer;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.entity.Turret;
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

    // Player-deployed, stationary (see Turret#travel) - not naturally spawned, no natural spawn placement.
    // Sized/eye-height like a dispenser-ish stubby block rather than a humanoid, but the renderer currently
    // reuses the zombie model as a placeholder (see client.TurretRenderer) - functional over pretty for now.
    public static final DeferredHolder<EntityType<?>, EntityType<Turret>> TURRET = ENTITY_TYPES.register("turret",
            () -> EntityType.Builder.<Turret>of(Turret::new, MobCategory.MISC)
                    .sized(0.8F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build("turret"));

    public static final DeferredHolder<EntityType<?>, EntityType<InfectedCreeper>> INFECTED_CREEPER = ENTITY_TYPES.register("infected_creeper",
            () -> EntityType.Builder.<InfectedCreeper>of(InfectedCreeper::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(8)
                    .build("infected_creeper"));

    public static final DeferredHolder<EntityType<?>, EntityType<MushZombie>> MUSH_ZOMBIE = ENTITY_TYPES.register("mush_zombie",
            () -> EntityType.Builder.<MushZombie>of(MushZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("mush_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<MushSkeleton>> MUSH_SKELETON = ENTITY_TYPES.register("mush_skeleton",
            () -> EntityType.Builder.<MushSkeleton>of(MushSkeleton::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("mush_skeleton"));

    // Same hitbox as vanilla IronGolem, which this extends.
    public static final DeferredHolder<EntityType<?>, EntityType<CactusGolem>> CACTUS_GOLEM = ENTITY_TYPES.register("cactus_golem",
            () -> EntityType.Builder.<CactusGolem>of(CactusGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .eyeHeight(2.3F)
                    .clientTrackingRange(10)
                    .build("cactus_golem"));

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
