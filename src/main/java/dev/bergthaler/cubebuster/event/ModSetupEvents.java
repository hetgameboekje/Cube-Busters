package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.entity.BlueZombie;
import dev.bergthaler.cubebuster.entity.EnderZombie;
import dev.bergthaler.cubebuster.entity.GreenZombie;
import dev.bergthaler.cubebuster.entity.RedZombie;
import dev.bergthaler.cubebuster.entity.Screamer;
import dev.bergthaler.cubebuster.entity.SiegeZombie;
import dev.bergthaler.cubebuster.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Registration-time (mod bus) wiring for every zombie variant: attributes and natural spawn placement rules.
 * Split from the main mod class purely to keep that class from becoming a junk drawer.
 */
@EventBusSubscriber(modid = Cubebuster.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModSetupEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.SIEGE_ZOMBIE.get(), SiegeZombie.createAttributes().build());
        event.put(ModEntityTypes.GREEN_ZOMBIE.get(), GreenZombie.createAttributes().build());
        event.put(ModEntityTypes.BLUE_ZOMBIE.get(), BlueZombie.createAttributes().build());
        event.put(ModEntityTypes.RED_ZOMBIE.get(), RedZombie.createAttributes().build());
        event.put(ModEntityTypes.ENDER_ZOMBIE.get(), EnderZombie.createAttributes().build());
        event.put(ModEntityTypes.SCREAMER.get(), Screamer.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Reuses vanilla's monster spawn rules (darkness, difficulty, block collision), additionally requires
        // open sky access (so it only spawns outdoors under the night sky, never in a roofed base or a cave -
        // SiegeZombies not needing darkness-only-indoors like vanilla cave zombies is the whole point), and
        // gates on siegeZombieNaturalSpawnChance so they stay rarer than plain zombies even where both can spawn.
        event.register(
                ModEntityTypes.SIEGE_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.canSeeSky(pos)
                        && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                        && random.nextDouble() < Config.naturalSpawnChance,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        // No open-sky requirement, unlike SiegeZombie - GreenZombies are wall-climbing intruders, just as at
        // home spawning in caves/ravines/structures as out in the open. Baseline chance anywhere in the
        // Overworld (see the green_zombie_spawns biome modifier), boosted in jungle biomes.
        event.register(
                ModEntityTypes.GREEN_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> {
                    if (!Config.enableGreenZombie || !Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) {
                        return false;
                    }
                    double chance = Config.greenZombieSpawnChance;
                    if (isJungleBiome(level, pos)) {
                        chance = Math.max(chance, Config.greenZombieJungleSpawnChance);
                    }
                    return random.nextDouble() < chance;
                },
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        // Reuses vanilla's own Drowned spawn rules wholesale (water depth, darkness, ocean biome frequency) -
        // the entity type argument is unused inside checkDrownedSpawnRules, so the unchecked cast is safe.
        event.register(
                ModEntityTypes.BLUE_ZOMBIE.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> Config.enableBlueZombie
                        && Drowned.checkDrownedSpawnRules((EntityType<Drowned>) (EntityType<?>) type, level, spawnType, pos, random)
                        && random.nextDouble() < Config.blueZombieSpawnChance,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        // The "or when it rains, even on dry land" half of BlueZombie's spawning can't be expressed as a second
        // placement here - NeoForge only allows one placement/heightmap pair (IN_WATER, above) per entity type,
        // OR-ing more predicates onto it still only ever samples water-column positions. See
        // BlueZombieRainSpawnHandler for the rain-triggered force-spawn instead.

        // In the Nether, spawns like a normal monster. In the Overworld, a low baseline everywhere, boosted in
        // warm biomes and right next to lava (the original "rare event" near lava pools). Never in the End.
        event.register(
                ModEntityTypes.RED_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> {
                    if (!Config.enableRedZombie || !Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) {
                        return false;
                    }
                    ResourceKey<Level> dimension = level.getLevel().dimension();
                    if (dimension == Level.NETHER) {
                        return random.nextDouble() < Config.redZombieSpawnChance;
                    }
                    if (dimension == Level.OVERWORLD) {
                        double chance = Config.redZombieOverworldBaseSpawnChance;
                        if (isWarmBiome(level, pos)) {
                            chance = Math.max(chance, Config.redZombieWarmBiomeSpawnChance);
                        }
                        if (isNearLava(level, pos)) {
                            chance = Math.max(chance, Config.redZombieOverworldLavaSpawnChance);
                        }
                        return random.nextDouble() < chance;
                    }
                    return false;
                },
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        // In the End, spawns like a normal monster. In the Overworld, a low baseline everywhere, boosted in
        // cold biomes. Never in the Nether.
        event.register(
                ModEntityTypes.ENDER_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> {
                    if (!Config.enableEnderZombie || !Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) {
                        return false;
                    }
                    ResourceKey<Level> dimension = level.getLevel().dimension();
                    if (dimension == Level.END) {
                        return random.nextDouble() < Config.enderZombieSpawnChance;
                    }
                    if (dimension == Level.OVERWORLD) {
                        double chance = Config.enderZombieOverworldBaseSpawnChance;
                        if (isColdBiome(level, pos)) {
                            chance = Math.max(chance, Config.enderZombieColdBiomeSpawnChance);
                        }
                        return random.nextDouble() < chance;
                    }
                    return false;
                },
                RegisterSpawnPlacementsEvent.Operation.OR
        );
    }

    private static boolean isNearLava(ServerLevelAccessor level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (level.getFluidState(nearby).is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWarmBiome(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).value().getBaseTemperature() >= 0.9F;
    }

    private static boolean isColdBiome(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).value().getBaseTemperature() <= 0.2F;
    }

    private static boolean isJungleBiome(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(BiomeTags.IS_JUNGLE);
    }

    private ModSetupEvents() {
    }
}
