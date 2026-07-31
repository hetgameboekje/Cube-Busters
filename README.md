# CubeBuster

A NeoForge mod for Minecraft **1.21.1** (Neo `21.1.244`) that adds a family of
tougher, more dangerous zombie variants and a tier system of "protected
glass" blocks for players to build defenses out of.

- Mod id: `cubebuster`
- Base package: `dev.bergthaler.cubebuster`
- License: All Rights Reserved (see `gradle.properties`)

## History

Git history is short and squashed (`first mod` → `extra commits`) — this is
effectively a from-scratch NeoForge mod project, still at version
`1.0-SNAPSHOT`. No changelog exists yet beyond commit messages.

## Build / run

Standard NeoForge Gradle project:

```
./gradlew build          # build the mod jar
./gradlew runClient      # launch a dev client
./gradlew runServer      # launch a dev server
```

## Features

### Zombie variants (`entity/`)

| Variant | Base class | Theme |
|---|---|---|
| **SiegeZombie** | `Zombie` | Base-raiding digger — spawns with an unbreakable netherite pickaxe, breaks through walls to reach players, infects nearby zombies, buffed at night. |
| **GreenZombie** | `Zombie` | Wall-climbing jungle intruder — climbs like a spider, only breaks blocks close to its target. |
| **BlueZombie** | `Drowned` | Aquatic variant — inherits swimming/trident behavior, can dig when stuck near water, extra spawns during rain. |
| **RedZombie** | `Husk` | Fire/lava-immune desert/Nether variant — walks through lava, buffed HP/damage/armor. |
| **EnderZombie** | `Zombie` | End variant — carries a chorus fruit for a single one-shot teleport near its target. |
| **Screamer** | `Zombie` | Aggro-system "caller" — no natural spawn, only force-spawned at high aggro; periodically calls in SiegeZombies near itself. |

The first five register their own spawn eggs and are individually toggleable
via config; Screamer only ever appears through the aggro system below (plus
a spawn egg for testing).

### AI goals (`entity/ai/`)

- **BlockBreakingGoal** — generic "dig toward the player" goal (used by
  SiegeZombie, BlueZombie, RedZombie): scans blocks around the zombie,
  chews through breakable ones over time, speed scaled by enrage/night buffs.
- **RadiusBlockBreakingGoal** — same, but restricted to blocks within a
  configurable radius of the target (GreenZombie — close-quarters only, not
  a tunneler).
- **ZombieInfectionGoal** — SiegeZombie converts nearby vanilla Zombies into
  SiegeZombies within a radius.
- **NightBuffGoal** — SiegeZombie gets speed/attack buffs at night in the
  Overworld.
- **ChorusFruitTeleportGoal** — EnderZombie's one-shot teleport, consumes
  the chorus fruit.
- **ScreamerSummonGoal** — while the Screamer has a target, periodically
  calls in more SiegeZombies near itself (see the aggro system below).

### Spawning & events (`event/`)

Spawning is a mix of vanilla spawn-placement predicates (biome/light/sky
conditions, config-gated chances) and forced spawns for cases NeoForge can't
express as a single placement rule:

- Village sieges can additionally spawn a SiegeZombie (`SiegeIntegrationHandler`).
- SiegeZombies force-spawn periodically at night (`SiegeZombieTimedSpawnHandler`).
- BlueZombies get extra forced spawns while it's raining (`BlueZombieRainSpawnHandler`).
- Damaging a player enrages nearby SiegeZombies, speeding up their attacks
  and digging (`SiegeEnrageHandler`).
- `ModSetupEvents` wires up attributes and the natural spawn-placement rules
  for all five naturally-spawning variants.
- Player interactions feed the **aggro system** (below), which does its own
  escalating force-spawning.

### Aggro system (`event/Aggro*.java`, `entity/Screamer.java`)

Every player has an aggro score (0..`aggroMaxScore`, default 600) tracked
in-memory (not saved — resets on server restart):

- **Gain** — opening a container or using a bed adds `aggroGainPerInteraction`
  points (per-player cooldown `aggroInteractionCooldownTicks`) —
  `AggroInteractionHandler`. This replaces the old direct
  interaction→spawn behavior. Separately, every `aggroSightGainIntervalTicks`
  a SiegeZombie with actual line of sight on its target adds
  `aggroSightGainAmount` — `SiegeZombieSightAggroGoal`. A SiegeZombie that's
  lost sight (behind a wall, out of view) still hunts its target relentlessly
  (that's the existing mustSee=false target selector), it just stops adding
  aggro until it can see them again.
- **Decay** — every `aggroDecayIntervalTicks`, the score drops by
  `aggroDecayAmount` (default 1 point/second → ~10 minutes from max to 0) —
  `AggroTickHandler`.
- **Display** — an actionbar readout ("Aggro: 340/600 (Level 3)"),
  refreshed every second.
- **Spawn check** — every `aggroSpawnCheckIntervalTicks` (default 30s), the
  player's score is mapped to a level (1-5, via the `aggroLevelNThreshold`
  config values) and rolled into spawns, via `AggroSpawnHandler`:

  | Level | SiegeZombies | Screamers |
  |---|---|---|
  | 1 | always 1 | — |
  | 2 | 1-3 | — |
  | 3 | 50% chance of 1-3 | always 1 |
  | 4 | 2-3 | 1-2 |
  | 5 | always 3 | always 2 |

  Spawns search for an open-air spot (`OpenAirSpawner`) between
  `aggroSafeZoneRadius` and `aggroSpawnMaxRadius` blocks of the player
  (never inside the safe zone, regardless of light), preferring sky-visible
  positions but falling back to fully dark (light level 0) ones — so a lit
  base (torches, etc.) is naturally off-limits without a dedicated "base"
  concept, while an unlit room close to the player isn't protected.
  Vertical search range is capped by the server's view distance.

- **Screamer** (`entity/Screamer.java`) — a `Zombie` subtype with no
  natural spawn, force-spawned only by the aggro system from level 3 up.
  While it has a target, `ScreamerSummonGoal` periodically
  (`screamerSummonCooldownTicks`) calls in `screamerSummonCount` more
  SiegeZombies in open air near itself
  (`screamerSummonMinRadius`..`screamerSummonMaxRadius`), playing a ghast
  scream every time it tries (placeholder sound). Capped at
  `aggroMaxScreamersPerPlayer` (default 2) live Screamers per player.
  Currently reuses the SiegeZombie texture as a placeholder; its spawn egg
  is pink/magenta.

### Protected glass (`registry/`, `tags/`)

Six blocks: plain "protected glass" and glass panes, each in tiers 1-3.
Tougher than vanilla glass (hardness 3.0, blast resistance 30, needs an iron
tool), craftable via normal recipes, with matching loot tables.

Whether a zombie is allowed to break a given block is entirely tag-driven
(`zombiemechanics:` block tags), not per-entity-class logic, and layered:

1. Hardcoded unbreakable safety list (bedrock, portals, command blocks,
   storage blocks, obsidian, beds, ...) — always wins.
2. Config blacklist (`siegeZombieUnbreakableBlocks`) — for blacklisting
   modded containers etc.
3. Tiered protected-glass rules:
   - **Tier 1** — breakable any time, toggle only (`enableTier1AlwaysBreakable`).
   - **Tier 2** — Overworld: night-only; Nether: always; End: never (toggle
     to fully-protected instead).
   - **Tier 3** — never breakable (toggle only, mainly for testing).
4. Generic `breakable`/`fragile_glass` tags otherwise (fragile glass breaks
   4x faster).

### Config (`Config.java`)

Everything above is tunable through a NeoForge common config: per-variant
enable flags and spawn chances, SiegeZombie block-break speed/enrage/infection/
timed-spawn/interaction-spawn settings, the protected-glass tier toggles, and
night-time speed/attack/dig multipliers. See the file for the full list —
every option has an inline comment explaining it.

## Project layout

```
src/main/java/dev/bergthaler/cubebuster/
  Config.java                 mod config spec
  Cubebuster.java      mod entrypoint, registry wiring, renderers
  client/                     entity renderers (one per variant)
  entity/                     the 5 zombie variants
  entity/ai/                  custom AI goals
  event/                      spawning & combat event handlers
  registry/                   blocks, items, entity types, creative tab
  tags/                       block-tag logic for protected glass
src/main/resources/
  assets/cubebuster/   textures, models, blockstates, lang
  data/cubebuster/     recipes, loot tables, biome modifiers
  data/zombiemechanics/       datapack-editable block tags
```
