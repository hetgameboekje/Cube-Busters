package dev.bergthaler.cubebuster;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Cubebuster.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("cubebuster-config");
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue BREAKS_BLOCKS = BUILDER
            .comment("Whether SiegeZombies are allowed to break blocks at all. Set to false to make them behave like normal zombies (minus the pickaxe).")
            .define("siegeZombieBreaksBlocks", true);

    private static final ModConfigSpec.IntValue BLOCK_BREAK_TICKS = BUILDER
            .comment("How many ticks it takes a SiegeZombie to break one block (20 ticks = 1 second). Kept short so it doesn't feel unresponsive, but long enough to give players time to react.")
            .defineInRange("siegeZombieBlockBreakTicks", 60, 10, 20 * 60);

    private static final ModConfigSpec.BooleanValue DROPS_BLOCK_LOOT = BUILDER
            .comment("Whether blocks broken by SiegeZombies drop their normal loot. Defaults to false to avoid free resource farms.")
            .define("siegeZombieDropsBlockLoot", false);

    private static final ModConfigSpec.DoubleValue NATURAL_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) that a zombie spawn attempt that already passed vanilla's monster spawn rules (plus the open-sky requirement in the spawn placement) becomes a SiegeZombie instead of a normal zombie.",
                    "This is the 'suitable conditions' gate mentioned in the spawn placement.")
            .defineInRange("siegeZombieNaturalSpawnChance", 0.4, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue SIEGE_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0), per vanilla zombie siege spawn, that an additional SiegeZombie is spawned alongside it.",
                    "Set to 0 to disable SiegeZombie involvement in sieges entirely; set to 1 to add one SiegeZombie for every siege zombie.")
            .defineInRange("siegeZombieSiegeSpawnChance", 0.5, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ENRAGE_RADIUS = BUILDER
            .comment("Radius (blocks) around a damaged player within which SiegeZombies get enraged.")
            .defineInRange("siegeZombieEnrageRadius", 32.0, 0.0, 128.0);

    private static final ModConfigSpec.IntValue ENRAGE_DURATION_TICKS = BUILDER
            .comment("How long (ticks, 20 = 1 second) an enrage lasts. Refreshed, not stacked, by further hits.")
            .defineInRange("siegeZombieEnrageDurationTicks", 100, 20, 20 * 60);

    private static final ModConfigSpec.DoubleValue ENRAGE_BREAK_SPEED_MULTIPLIER = BUILDER
            .comment("Block-break time multiplier while enraged (e.g. 0.7 = 30% faster).")
            .defineInRange("siegeZombieEnrageBreakSpeedMultiplier", 0.7, 0.1, 1.0);

    private static final ModConfigSpec.IntValue ENRAGE_SPEED_AMPLIFIER = BUILDER
            .comment("Speed effect amplifier applied while enraged (0 = Speed I, 1 = Speed II, ...).",
                    "Zombies' base movement speed attribute is already higher than a player's, but their AI walk code doesn't use all of it - 1 (Speed II) is a good starting point to let an enraged zombie keep pace with a sprinting player.")
            .defineInRange("siegeZombieEnrageSpeedAmplifier", 1, 0, 5);

    private static final ModConfigSpec.DoubleValue INFECTION_RADIUS = BUILDER
            .comment("Radius (blocks) around a SiegeZombie within which plain Zombies get converted into SiegeZombies.")
            .defineInRange("siegeZombieInfectionRadius", 6.0, 0.0, 32.0);

    private static final ModConfigSpec.IntValue TIMED_SPAWN_INTERVAL_TICKS = BUILDER
            .comment("How often (ticks, 20 = 1 second) each overworld level tries to force-spawn one SiegeZombie at night, on top of natural spawning.",
                    "Set to a huge number to effectively disable this.")
            .defineInRange("siegeZombieTimedSpawnIntervalTicks", 5 * 60 * 20, 20, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue AGGRO_GAIN_PER_INTERACTION = BUILDER
            .comment("How many aggro score points a player gains per qualifying interaction (opening a container, using a bed). See the 'Aggro / Screamer system' section below for what the score does.")
            .defineInRange("aggroGainPerInteraction", 15, 1, 1000);

    private static final ModConfigSpec.IntValue AGGRO_INTERACTION_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks (20 = 1 second) between aggro score gains from interactions for the same player, so rapidly opening/closing a container can't spam score.")
            .defineInRange("aggroInteractionCooldownTicks", 100, 0, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue AGGRO_SIGHT_GAIN_AMOUNT = BUILDER
            .comment("Aggro score points a player gains each aggroSightGainIntervalTicks while a SiegeZombie has line of sight on them.",
                    "A SiegeZombie that has lost sight (behind walls, out of view) still knows where its target is and keeps hunting - it just doesn't add aggro while it can't see them.",
                    "Keep this above aggroDecayAmount (both tick once per second by default) or sustained sight can never outpace the passive decay.")
            .defineInRange("aggroSightGainAmount", 4, 0, 1000);

    private static final ModConfigSpec.IntValue AGGRO_SIGHT_GAIN_INTERVAL_TICKS = BUILDER
            .comment("How often (ticks, 20 = 1 second) a SiegeZombie with line of sight on its target adds aggroSightGainAmount to that player's score.")
            .defineInRange("aggroSightGainIntervalTicks", 20, 1, 20 * 60 * 60);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> UNBREAKABLE_BLOCKS = BUILDER
            .comment("Extra blocks and/or block tags SiegeZombies may never break, on top of the built-in safety list",
                    "(bedrock, portals, command blocks, chests, barrels, shulker boxes, beacons, enchanting tables, beds, ...).",
                    "Entries are block IDs (\"minecraft:crafting_table\") or tags prefixed with '#' (\"#c:chests\").",
                    "Use this to blacklist modded containers (e.g. Lootr chests) without needing a datapack.")
            .defineListAllowEmpty("siegeZombieUnbreakableBlocks", List.of("#c:chests", "minecraft:crafting_table", "minecraft:furnace"),
                    () -> "minecraft:air", entry -> entry instanceof String s && !s.isBlank());

    private static final ModConfigSpec.BooleanValue TIER1_ALWAYS_BREAKABLE = BUILDER
            .comment("Tier 1 protected blocks (zombiemechanics:protected_tier1): breakable day/night, in every dimension, whenever this is true.",
                    "Set to false to make Tier 1 blocks fully protected instead.")
            .define("enableTier1AlwaysBreakable", true);

    private static final ModConfigSpec.BooleanValue TIER2_NIGHT_ONLY_OVERWORLD = BUILDER
            .comment("Tier 2 protected blocks (zombiemechanics:protected_tier2): in the Overworld, breakable only at night; in the Nether, always breakable; in the End, never breakable.",
                    "Set to false to make Tier 2 blocks fully protected instead (same as Tier 3).")
            .define("enableTier2NightOnlyOverworld", true);

    private static final ModConfigSpec.BooleanValue TIER3_FULLY_PROTECTED = BUILDER
            .comment("Tier 3 protected blocks (zombiemechanics:protected_tier3): never breakable, in any dimension, whenever this is true.",
                    "Set to false to make Tier 3 blocks breakable like a normal 'breakable' block instead - mainly useful for testing.")
            .define("enableTier3FullyProtected", true);

    private static final ModConfigSpec.DoubleValue NIGHT_SPEED_MULTIPLIER = BUILDER
            .comment("Movement speed multiplier applied to SiegeZombies at night in the Overworld (1.0 = no change).")
            .defineInRange("nightSpeedMultiplier", 1.3, 1.0, 3.0);

    private static final ModConfigSpec.DoubleValue NIGHT_ATTACK_MULTIPLIER = BUILDER
            .comment("Attack damage multiplier applied to SiegeZombies at night in the Overworld (1.0 = no change).")
            .defineInRange("nightAttackMultiplier", 1.2, 1.0, 3.0);

    private static final ModConfigSpec.DoubleValue NIGHT_BLOCK_BREAK_SPEED_MULTIPLIER = BUILDER
            .comment("Block-break time multiplier applied to SiegeZombies at night in the Overworld (e.g. 0.8 = 20% faster). Stacks with the enrage multiplier.")
            .defineInRange("nightBlockBreakSpeedMultiplier", 0.8, 0.1, 1.0);

    private static final ModConfigSpec.BooleanValue ENABLE_GREEN_ZOMBIE = BUILDER
            .comment("Whether GreenZombies (wall-climbing intruders) are enabled: can spawn naturally, and their block-breaking goal is active.")
            .define("enableGreenZombie", true);

    private static final ModConfigSpec.DoubleValue GREEN_ZOMBIE_SPAWN_CHANCE = BUILDER
            .comment("Baseline chance (0.0-1.0), anywhere in the Overworld, that a zombie spawn attempt that already passed vanilla's monster spawn rules becomes a GreenZombie instead of a normal zombie.")
            .defineInRange("greenZombieSpawnChance", 0.15, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue GREEN_ZOMBIE_JUNGLE_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) in jungle biomes specifically (#minecraft:is_jungle) - GreenZombies are more at home in plant-dense biomes.")
            .defineInRange("greenZombieJungleSpawnChance", 0.4, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue GREEN_ZOMBIE_BREAK_RADIUS = BUILDER
            .comment("Radius (blocks) around a GreenZombie's target within which it's willing to break blocks - keeps it a close-quarters harasser rather than a tunneler.")
            .defineInRange("greenZombieBreakRadius", 4.0, 1.0, 16.0);

    private static final ModConfigSpec.BooleanValue ENABLE_BLUE_ZOMBIE = BUILDER
            .comment("Whether BlueZombies (Drowned-based swimmers) are enabled: can spawn naturally, and their block-breaking goal is active.")
            .define("enableBlueZombie", true);

    private static final ModConfigSpec.DoubleValue BLUE_ZOMBIE_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) that a natural Drowned spawn attempt (vanilla's own ocean/river/deep-water rules) becomes a BlueZombie instead of a normal Drowned.")
            .defineInRange("blueZombieSpawnChance", 0.2, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue BLUE_ZOMBIE_RAIN_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0), anywhere in the Overworld while it's raining, that a qualifying zombie spawn attempt becomes a BlueZombie - not just in/near water.")
            .defineInRange("blueZombieRainSpawnChance", 0.1, 0.0, 1.0);

    private static final ModConfigSpec.BooleanValue ENABLE_RED_ZOMBIE = BUILDER
            .comment("Whether RedZombies (Husk-based, fire/lava-immune) are enabled: can spawn naturally, and their block-breaking goal is active.")
            .define("enableRedZombie", true);

    private static final ModConfigSpec.DoubleValue RED_ZOMBIE_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) that a Nether zombie spawn attempt that already passed vanilla's monster spawn rules becomes a RedZombie.")
            .defineInRange("redZombieSpawnChance", 0.35, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue RED_ZOMBIE_OVERWORLD_BASE_SPAWN_CHANCE = BUILDER
            .comment("Baseline chance (0.0-1.0), anywhere in the Overworld, that a qualifying zombie spawn attempt becomes a RedZombie.")
            .defineInRange("redZombieOverworldBaseSpawnChance", 0.03, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue RED_ZOMBIE_WARM_BIOME_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) in warm biomes specifically (desert/savanna/badlands-like) - RedZombies are more at home there.")
            .defineInRange("redZombieWarmBiomeSpawnChance", 0.15, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue RED_ZOMBIE_OVERWORLD_LAVA_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) that a qualifying Overworld zombie spawn attempt right next to lava becomes a RedZombie. Kept low - this is meant to be a rare event.")
            .defineInRange("redZombieOverworldLavaSpawnChance", 0.02, 0.0, 1.0);

    private static final ModConfigSpec.BooleanValue ENABLE_ENDER_ZOMBIE = BUILDER
            .comment("Whether EnderZombies (End siege variant) are enabled: can spawn naturally, and their chorus-fruit teleport goal is active.")
            .define("enableEnderZombie", true);

    private static final ModConfigSpec.DoubleValue ENDER_ZOMBIE_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) that an End zombie spawn attempt that already passed vanilla's monster spawn rules becomes an EnderZombie.")
            .defineInRange("enderZombieSpawnChance", 0.3, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ENDER_ZOMBIE_OVERWORLD_BASE_SPAWN_CHANCE = BUILDER
            .comment("Baseline chance (0.0-1.0), anywhere in the Overworld, that a qualifying zombie spawn attempt becomes an EnderZombie.")
            .defineInRange("enderZombieOverworldBaseSpawnChance", 0.03, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ENDER_ZOMBIE_COLD_BIOME_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0-1.0) in cold biomes specifically (snowy/taiga-like) - EnderZombies are more at home there.")
            .defineInRange("enderZombieColdBiomeSpawnChance", 0.15, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ENDER_ZOMBIE_TELEPORT_CHANCE = BUILDER
            .comment("Chance (0.0-1.0), checked every couple of seconds while it has a target and hasn't used its chorus fruit yet, that an EnderZombie teleports.")
            .defineInRange("enderZombieTeleportChance", 0.15, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ENDER_ZOMBIE_FAR_TELEPORT_CHANCE = BUILDER
            .comment("Once an EnderZombie decides to teleport, the chance (0.0-1.0) that it's a random long-range teleport instead of one aimed near its target.")
            .defineInRange("enderZombieFarTeleportChance", 0.2, 0.0, 1.0);

    // --- Aggro / Screamer system ---
    // Every player has an aggro score (0..aggroMaxScore) that rises from interactions (see
    // aggroGainPerInteraction above) and decays slowly over time. A periodic check turns the player's current
    // level (1-5, derived from the thresholds below) into a spawn near them: SiegeZombies at every level,
    // Screamers (which themselves periodically call in more SiegeZombies) from level 3 up.

    private static final ModConfigSpec.IntValue AGGRO_MAX_SCORE = BUILDER
            .comment("Upper bound on a player's aggro score. Also the effective ceiling for the level 5 threshold below.")
            .defineInRange("aggroMaxScore", 600, 10, 100000);

    private static final ModConfigSpec.IntValue AGGRO_DECAY_INTERVAL_TICKS = BUILDER
            .comment("How often (ticks, 20 = 1 second) a player's aggro score ticks down by aggroDecayAmount.")
            .defineInRange("aggroDecayIntervalTicks", 20, 1, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue AGGRO_DECAY_AMOUNT = BUILDER
            .comment("How many points the aggro score loses every aggroDecayIntervalTicks. Defaults (3 points/second) roughly match the max sustained gain rate from repeated interactions, so the score can trend back down even while you keep interacting, not just once you stop. A maxed-out score takes about 3-4 minutes to fully cool down if you stop.")
            .defineInRange("aggroDecayAmount", 3, 1, 1000);

    private static final ModConfigSpec.IntValue AGGRO_DECAY_HOLD_TICKS = BUILDER
            .comment("How long (ticks, 20 = 1 second) a player's aggro score holds steady after their last gain before passive decay resumes. Each new gain (interaction or sustained sight) pushes this hold window forward, so decay only kicks in once the player has actually stopped provoking, not the instant an interaction cooldown elapses.")
            .defineInRange("aggroDecayHoldTicks", 100, 0, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue AGGRO_SPAWN_CHECK_INTERVAL_TICKS = BUILDER
            .comment("How often (ticks, 20 = 1 second) each player's current aggro level is rolled into a spawn.")
            .defineInRange("aggroSpawnCheckIntervalTicks", 600, 20, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue AGGRO_LEVEL_1_THRESHOLD = BUILDER
            .comment("Aggro score at which level 1 starts: always spawns 1 SiegeZombie.")
            .defineInRange("aggroLevel1Threshold", 1, 0, 100000);

    private static final ModConfigSpec.IntValue AGGRO_LEVEL_2_THRESHOLD = BUILDER
            .comment("Aggro score at which level 2 starts: spawns 1-3 SiegeZombies.")
            .defineInRange("aggroLevel2Threshold", 120, 0, 100000);

    private static final ModConfigSpec.IntValue AGGRO_LEVEL_3_THRESHOLD = BUILDER
            .comment("Aggro score at which level 3 starts: always 1 Screamer, plus a chance of 1-3 SiegeZombies.")
            .defineInRange("aggroLevel3Threshold", 250, 0, 100000);

    private static final ModConfigSpec.IntValue AGGRO_LEVEL_4_THRESHOLD = BUILDER
            .comment("Aggro score at which level 4 starts: 1-2 Screamers plus 2-3 SiegeZombies.")
            .defineInRange("aggroLevel4Threshold", 400, 0, 100000);

    private static final ModConfigSpec.IntValue AGGRO_LEVEL_5_THRESHOLD = BUILDER
            .comment("Aggro score at which level 5 starts: always 2 Screamers plus 3 SiegeZombies (the maximum).")
            .defineInRange("aggroLevel5Threshold", 550, 0, 100000);

    private static final ModConfigSpec.IntValue AGGRO_SAFE_ZONE_RADIUS = BUILDER
            .comment("Radius (blocks) around the player within which aggro spawns never place a mob, regardless of light level.")
            .defineInRange("aggroSafeZoneRadius", 10, 0, 128);

    private static final ModConfigSpec.IntValue AGGRO_SPAWN_MAX_RADIUS = BUILDER
            .comment("Maximum radius (blocks) around the player that aggro spawns search for a spot in.")
            .defineInRange("aggroSpawnMaxRadius", 30, 1, 256);

    private static final ModConfigSpec.IntValue AGGRO_MAX_SCREAMERS_PER_PLAYER = BUILDER
            .comment("Maximum number of live Screamers that can be attributed to (spawned for) a single player at once.")
            .defineInRange("aggroMaxScreamersPerPlayer", 2, 1, 10);

    private static final ModConfigSpec.IntValue SCREAMER_SUMMON_COUNT = BUILDER
            .comment("How many SiegeZombies a Screamer calls in per summon.")
            .defineInRange("screamerSummonCount", 4, 1, 20);

    private static final ModConfigSpec.IntValue SCREAMER_SUMMON_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks (20 = 1 second) between a Screamer's summons, while it has a target.")
            .defineInRange("screamerSummonCooldownTicks", 600, 20, 20 * 60 * 60);

    private static final ModConfigSpec.IntValue SCREAMER_SUMMON_MIN_RADIUS = BUILDER
            .comment("Minimum radius (blocks) around a Screamer that its summoned SiegeZombies can appear.")
            .defineInRange("screamerSummonMinRadius", 4, 0, 64);

    private static final ModConfigSpec.IntValue SCREAMER_SUMMON_MAX_RADIUS = BUILDER
            .comment("Maximum radius (blocks) around a Screamer that its summoned SiegeZombies can appear. Also used as the vertical search cap for that summon.")
            .defineInRange("screamerSummonMaxRadius", 16, 1, 128);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean breaksBlocks;
    public static int blockBreakTicks;
    public static boolean dropsBlockLoot;
    public static double naturalSpawnChance;
    public static double siegeSpawnChance;
    public static double enrageRadius;
    public static int enrageDurationTicks;
    public static double enrageBreakSpeedMultiplier;
    public static int enrageSpeedAmplifier;
    public static double infectionRadius;
    public static int timedSpawnIntervalTicks;
    public static int aggroGainPerInteraction;
    public static int aggroInteractionCooldownTicks;
    public static int aggroSightGainAmount;
    public static int aggroSightGainIntervalTicks;
    public static int aggroMaxScore;
    public static int aggroDecayIntervalTicks;
    public static int aggroDecayAmount;
    public static int aggroDecayHoldTicks;
    public static int aggroSpawnCheckIntervalTicks;
    public static int aggroLevel1Threshold;
    public static int aggroLevel2Threshold;
    public static int aggroLevel3Threshold;
    public static int aggroLevel4Threshold;
    public static int aggroLevel5Threshold;
    public static int aggroSafeZoneRadius;
    public static int aggroSpawnMaxRadius;
    public static int aggroMaxScreamersPerPlayer;
    public static int screamerSummonCount;
    public static int screamerSummonCooldownTicks;
    public static int screamerSummonMinRadius;
    public static int screamerSummonMaxRadius;
    public static Set<Block> unbreakableBlocks = Set.of();
    public static Set<TagKey<Block>> unbreakableBlockTags = Set.of();
    public static boolean enableTier1AlwaysBreakable;
    public static boolean enableTier2NightOnlyOverworld;
    public static boolean enableTier3FullyProtected;
    public static double nightSpeedMultiplier;
    public static double nightAttackMultiplier;
    public static double nightBlockBreakSpeedMultiplier;
    public static boolean enableGreenZombie;
    public static double greenZombieSpawnChance;
    public static double greenZombieJungleSpawnChance;
    public static double greenZombieBreakRadius;
    public static boolean enableBlueZombie;
    public static double blueZombieSpawnChance;
    public static double blueZombieRainSpawnChance;
    public static boolean enableRedZombie;
    public static double redZombieSpawnChance;
    public static double redZombieOverworldBaseSpawnChance;
    public static double redZombieWarmBiomeSpawnChance;
    public static double redZombieOverworldLavaSpawnChance;
    public static boolean enableEnderZombie;
    public static double enderZombieSpawnChance;
    public static double enderZombieOverworldBaseSpawnChance;
    public static double enderZombieColdBiomeSpawnChance;
    public static double enderZombieTeleportChance;
    public static double enderZombieFarTeleportChance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        breaksBlocks = BREAKS_BLOCKS.get();
        blockBreakTicks = BLOCK_BREAK_TICKS.get();
        dropsBlockLoot = DROPS_BLOCK_LOOT.get();
        naturalSpawnChance = NATURAL_SPAWN_CHANCE.get();
        siegeSpawnChance = SIEGE_SPAWN_CHANCE.get();
        enrageRadius = ENRAGE_RADIUS.get();
        enrageDurationTicks = ENRAGE_DURATION_TICKS.get();
        enrageBreakSpeedMultiplier = ENRAGE_BREAK_SPEED_MULTIPLIER.get();
        enrageSpeedAmplifier = ENRAGE_SPEED_AMPLIFIER.get();
        infectionRadius = INFECTION_RADIUS.get();
        timedSpawnIntervalTicks = TIMED_SPAWN_INTERVAL_TICKS.get();
        aggroGainPerInteraction = AGGRO_GAIN_PER_INTERACTION.get();
        aggroInteractionCooldownTicks = AGGRO_INTERACTION_COOLDOWN_TICKS.get();
        aggroSightGainAmount = AGGRO_SIGHT_GAIN_AMOUNT.get();
        aggroSightGainIntervalTicks = AGGRO_SIGHT_GAIN_INTERVAL_TICKS.get();
        aggroMaxScore = AGGRO_MAX_SCORE.get();
        aggroDecayIntervalTicks = AGGRO_DECAY_INTERVAL_TICKS.get();
        aggroDecayAmount = AGGRO_DECAY_AMOUNT.get();
        aggroDecayHoldTicks = AGGRO_DECAY_HOLD_TICKS.get();
        aggroSpawnCheckIntervalTicks = AGGRO_SPAWN_CHECK_INTERVAL_TICKS.get();
        aggroLevel1Threshold = AGGRO_LEVEL_1_THRESHOLD.get();
        aggroLevel2Threshold = AGGRO_LEVEL_2_THRESHOLD.get();
        aggroLevel3Threshold = AGGRO_LEVEL_3_THRESHOLD.get();
        aggroLevel4Threshold = AGGRO_LEVEL_4_THRESHOLD.get();
        aggroLevel5Threshold = AGGRO_LEVEL_5_THRESHOLD.get();
        aggroSafeZoneRadius = AGGRO_SAFE_ZONE_RADIUS.get();
        aggroSpawnMaxRadius = AGGRO_SPAWN_MAX_RADIUS.get();
        aggroMaxScreamersPerPlayer = AGGRO_MAX_SCREAMERS_PER_PLAYER.get();
        screamerSummonCount = SCREAMER_SUMMON_COUNT.get();
        screamerSummonCooldownTicks = SCREAMER_SUMMON_COOLDOWN_TICKS.get();
        screamerSummonMinRadius = SCREAMER_SUMMON_MIN_RADIUS.get();
        screamerSummonMaxRadius = SCREAMER_SUMMON_MAX_RADIUS.get();

        Set<Block> blocks = new HashSet<>();
        Set<TagKey<Block>> tags = new HashSet<>();
        for (String entry : UNBREAKABLE_BLOCKS.get()) {
            if (entry.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id == null) {
                    LOGGER.warn("Ignoring invalid tag id '{}' in siegeZombieUnbreakableBlocks", entry);
                    continue;
                }
                tags.add(TagKey.create(Registries.BLOCK, id));
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                    LOGGER.warn("Ignoring unknown block id '{}' in siegeZombieUnbreakableBlocks", entry);
                    continue;
                }
                blocks.add(BuiltInRegistries.BLOCK.get(id));
            }
        }
        unbreakableBlocks = blocks;
        unbreakableBlockTags = tags;

        enableTier1AlwaysBreakable = TIER1_ALWAYS_BREAKABLE.get();
        enableTier2NightOnlyOverworld = TIER2_NIGHT_ONLY_OVERWORLD.get();
        enableTier3FullyProtected = TIER3_FULLY_PROTECTED.get();

        nightSpeedMultiplier = NIGHT_SPEED_MULTIPLIER.get();
        nightAttackMultiplier = NIGHT_ATTACK_MULTIPLIER.get();
        nightBlockBreakSpeedMultiplier = NIGHT_BLOCK_BREAK_SPEED_MULTIPLIER.get();

        enableGreenZombie = ENABLE_GREEN_ZOMBIE.get();
        greenZombieSpawnChance = GREEN_ZOMBIE_SPAWN_CHANCE.get();
        greenZombieJungleSpawnChance = GREEN_ZOMBIE_JUNGLE_SPAWN_CHANCE.get();
        greenZombieBreakRadius = GREEN_ZOMBIE_BREAK_RADIUS.get();

        enableBlueZombie = ENABLE_BLUE_ZOMBIE.get();
        blueZombieSpawnChance = BLUE_ZOMBIE_SPAWN_CHANCE.get();
        blueZombieRainSpawnChance = BLUE_ZOMBIE_RAIN_SPAWN_CHANCE.get();

        enableRedZombie = ENABLE_RED_ZOMBIE.get();
        redZombieSpawnChance = RED_ZOMBIE_SPAWN_CHANCE.get();
        redZombieOverworldBaseSpawnChance = RED_ZOMBIE_OVERWORLD_BASE_SPAWN_CHANCE.get();
        redZombieWarmBiomeSpawnChance = RED_ZOMBIE_WARM_BIOME_SPAWN_CHANCE.get();
        redZombieOverworldLavaSpawnChance = RED_ZOMBIE_OVERWORLD_LAVA_SPAWN_CHANCE.get();

        enableEnderZombie = ENABLE_ENDER_ZOMBIE.get();
        enderZombieSpawnChance = ENDER_ZOMBIE_SPAWN_CHANCE.get();
        enderZombieOverworldBaseSpawnChance = ENDER_ZOMBIE_OVERWORLD_BASE_SPAWN_CHANCE.get();
        enderZombieColdBiomeSpawnChance = ENDER_ZOMBIE_COLD_BIOME_SPAWN_CHANCE.get();
        enderZombieTeleportChance = ENDER_ZOMBIE_TELEPORT_CHANCE.get();
        enderZombieFarTeleportChance = ENDER_ZOMBIE_FAR_TELEPORT_CHANCE.get();
    }
}
