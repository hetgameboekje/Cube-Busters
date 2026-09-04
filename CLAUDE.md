# CLAUDE.md

Guidance for Claude Code when working in this repo.

## What this is

NeoForge Minecraft mod (**1.21.1**, Neo `21.1.244`), mod id `cubebuster`,
package `dev.bergthaler.cubebuster`. See `README.md` for the full feature
rundown of what's currently shipped (zombie variants, AI goals, spawn
events, protected glass tier system, config). See `NEW_MECHANICS.md` for
planned/in-progress features and their status.

## Subagents

Work in this repo is split across a `manager` subagent and seven domain
subagents defined in `.claude/agents/`. Default to talking to `manager` —
it routes requests to the right domain agent(s), sequences work that has
cross-domain dependencies (e.g. Horde Boss needs the Aggro persistence
rework first), and reports back in one voice instead of you juggling
`aggro-agent`, `mob-agent`, `mush-agent`, `horde-boss-agent`,
`turret-agent`, `cactus-agent`, and `structures-agent` yourself. See
`.claude/agents/manager.md` for the full routing table and dependency
notes.

## Build / test

```
./gradlew build          # compile + build mod jar
./gradlew runClient      # dev client
./gradlew runServer      # dev server
```

No unit test suite exists. The only way to verify gameplay logic is
`runClient`/`runServer` and playing it — say so explicitly if you can't run it.

## Layout

```
src/main/java/dev/bergthaler/cubebuster/
  Config.java              NeoForge common config spec (ModConfigSpec) - single source of tunables
  Cubebuster.java          mod entrypoint: registry wiring, renderer registration
  client/                  entity renderers, one per variant
  entity/                  zombie variant entity classes
  entity/ai/               custom Goal implementations
  event/                   spawning & combat @SubscribeEvent handlers
  registry/                DeferredRegister holders: blocks, items, entity types, creative tab
  tags/                    ModBlockTags - block-break permission logic for protected glass,
                           plus the cubebuster:mush_blocks density tag used by Horde Boss
src/main/resources/
  assets/cubebuster/   textures, models, blockstates, lang
  data/cubebuster/     recipes, loot tables, biome modifiers (neoforge spawn placements),
                       block tags (mush_blocks - currently a placeholder, see Horde Boss above)
  data/zombiemechanics/       datapack-editable block tags (breakable/unbreakable/protected_tierN)
```

## Conventions in this codebase

- **Config-driven, not hardcoded.** Every tunable (spawn chances, radii,
  durations, multipliers, enable flags) lives in `Config.java` as a
  `ModConfigSpec` value with a `.comment(...)` explaining it, cached into a
  plain `public static` field on `onLoad`. New tunables should follow the
  same pattern, not literals buried in event/goal code.
- **Force-spawn handlers share a spawner helper.** `SiegeZombieSpawner` /
  `BlueZombieSpawner` are the pattern for "spawn N of this mob near a player
  in the open" — reuse/extend this pattern rather than duplicating
  placement-search logic in a new handler.
- **Block-break permission is tag-driven**, not per-entity-class
  (`ModBlockTags.canBreak`). New breakable/protected block behavior should
  extend the tag tiers, not add per-mob special cases.
- **AI goals check config live** via suppliers/`Config.xxx` field reads (not
  captured at construction), so a config reload takes effect without
  restarting the world.
- **Owner-tagged mobs use a `UUID ownerUUID` field + NBT persistence** —
  see `Screamer` for the reference pattern (`setOwnerUUID`/`getOwnerUUID`,
  saved under `"AggroOwner"` in `addAdditionalSaveData`/
  `readAdditionalSaveData`). Any mob whose behavior should stay tied to the
  specific player that caused its spawn (not whichever player it happens
  to be nearest to) needs this — `SiegeZombie` has it too now (see
  Known bugs below).
- Comments in `Config.java` are user-facing documentation (shown by config
  GUIs) — write them for a server admin, not a fellow programmer.

## Currently shipped: Aggro system

Per-player aggro score, **persisted via a NeoForge data attachment**
(`event/AggroManager.java`, `registry/ModAttachmentTypes.java`,
`event/AggroState.java`), driving escalating force-spawns (levels 1-5) via
`event/AggroSpawnHandler.java` and `event/AggroTickHandler.java`, plus the
`Screamer` mob (`entity/Screamer.java`) that calls in more SiegeZombies.
See `README.md` for the level table and config knobs. `Screamer` uses its
own texture, `textures/entity/scream_siege.png`, via
`client/ScreamerRenderer.java`.

The score is stored as an `AggroState(score, decayResumeTick)` record on
the player itself (`ModAttachmentTypes.AGGRO`), so it survives disconnects
and server restarts — unlike the old in-memory `UUID -> Integer` map. Each
score gain pushes `decayResumeTick` forward by `Config.aggroDecayHoldTicks`
(default 100 ticks / 5s); passive decay only resumes once that hold window
has elapsed, so the score holds steady right after a gain instead of
ticking down continuously. It still resets to 0 on death
(`AggroInteractionHandler.onPlayerDeath`), and the attachment isn't copied
across respawn (no `copyOnDeath()`), so this fires on the same instance the
event is raised on.

This was the blocking prerequisite for the Horde Boss cap system
(see `NEW_MECHANICS.md`) — it's unblocked now.

## Currently shipped: Horde Boss (integration layer)

Recurring boss event, triggered per-player every `Config.hordeBossCheckIntervalTicks`
by `event/HordeBossSpawnHandler.java`: (aggro score, read from `AggroManager.getScore` -
this layer never writes to it) × (mush block density near the player) crossing
`Config.hordeBossTriggerThreshold`, scaled by a 7-Days-to-Die-style day/night pacing
curve (`event/HordeBossPacing.java`) and gated by the daily/cluster/server spawn caps
and a post-boss cooldown (`event/HordeBossCapManager.java`). Spawns `entity/HordeBoss.java`
(a bigger, tankier `Zombie` variant, same `ownerUUID` pattern as `SiegeZombie`/`Screamer`)
via `OpenAirSpawner`, with loot at `data/cubebuster/loot_table/entities/horde_boss.json`.

Density is a **placeholder block-count scan**: `ModBlockTags.MUSH_BLOCKS`
(`cubebuster:mush_blocks`) is the tag `event/HordeBossDensity.java` counts within
`Config.hordeBossDensityRadius` blocks of the player. The actual Mush/Infected mechanic
(real mush blocks) is a separate, not-yet-merged PR — until it lands and tags its blocks
into `data/cubebuster/tags/block/mush_blocks.json`, that file instead tags
`minecraft:brown_mushroom_block` as a harmless stand-in so this code has something real
to count. Remove the placeholder once real mush blocks exist.

Two cap-tracking mechanisms, chosen per scope:
- **Per-player daily count + cooldown** (`hordeBossPlayerDailyCap`, `hordeBossCooldownTicks`):
  the same NeoForge data-attachment pattern as `AggroState`/`ModAttachmentTypes.AGGRO` -
  see `event/HordeBossState.java` + `ModAttachmentTypes.HORDE_BOSS_CAP`.
- **Server-wide daily count + Chebyshev chunk-distance clustering**
  (`hordeBossServerDailyCap`, `hordeBossClusterCap`, `hordeBossClusterChunkRadius`):
  vanilla `SavedData` on the Overworld, not an attachment - this state is genuinely
  server-scoped rather than per-player. See `event/HordeBossSavedData.java`.

Both roll over at the Minecraft day boundary, computed from the Overworld's total game
time divided by 24000 (not `Level.getDayTime()`, which sleeping/commands can shift) -
see `HordeBossCapManager.currentDay`.

## Known bugs (fixed)

### 1. Aggro gain ignored creative/spectator mode — fixed
Both `event/AggroInteractionHandler.java` (`maybeGainScore()`) and
`entity/ai/SiegeZombieSightAggroGoal.java` (`canUse()`) now guard on
`isCreative()`/`isSpectator()` before adding score.

### 2. SiegeZombie had no owner, could raise the wrong player's aggro — fixed
`SiegeZombie` now carries an `ownerUUID` field (mirroring `Screamer`'s
pattern) with NBT persistence under `"AggroOwner"`.
`AggroSpawnHandler.trySpawnFor()` tags each force-spawned SiegeZombie with
the triggering player's UUID, and `SiegeZombieSightAggroGoal` credits
sight-based aggro to that owner rather than whoever the zombie currently
has targeted — so a zombie spawned for player A can no longer raise
player B's score by retargeting onto B.

Naturally-spawned SiegeZombies (village sieges, timed night spawns — not
from the aggro system) have `ownerUUID == null` and so never grant
sight-aggro, since there's no owner to credit.

---

## Planned new mechanics

Design work for these is done (or in progress) in `NEW_MECHANICS.md` —
check that file for full detail and current status per item before
implementing. High-level summary of what's coming and how it fits the
existing codebase:

- ~~**Aggro persistence rework**~~ — done, see "Currently shipped: Aggro
  system" above. Was the blocking prerequisite for the Horde Boss cap
  system below.
- **Mush / Infected mechanic** — new creeper variant, spore/mush blocks,
  mush zombies, cure-item crafting chain (mortar & pestle). Net-new
  system, doesn't touch existing zombie variants directly. Still not
  merged — the Horde Boss system below currently scans a placeholder tag
  (`ModBlockTags.MUSH_BLOCKS`) in its place, see "Currently shipped:
  Horde Boss" above.
- ~~**Horde Boss**~~ — done, see "Currently shipped: Horde Boss
  (integration layer)" above. Density check method (block-count scan) and
  the post-boss cooldown were both left open in the original design and
  have since been decided — see `NEW_MECHANICS.md`.
- **Turret** — reuse vanilla skeleton AI goals
  (`RangedAttackGoal`/`RangedBowAttackGoal` + `NearestAttackableTargetGoal`)
  for targeting/combat rather than writing custom AI. Still needs:
  immobility handling, sentry/manual toggle via `GoalSelector`, and a
  custom multi-projectile interface (fireball/firework/thorn-arrow).
  Architecture choice (entity-based vs. dispenser-style block-entity) not
  yet decided.
- **Cactus economy** — early-game survival items (barbed wire, thorned
  bush, collapsing trapdoor) and a cactus sap → juice → mocktail crafting
  chain. Net-new, no dependency on existing systems.
- **Cactus/Pumpkin Golem** — new mob, elytra-style durability item with
  Looting-enchant transfer. Net-new.
- **Custom structures** — mineshafts/sieged villages/bunkers as
  SiegeZombie/mush spawn points. Concept only, not designed in detail yet.

See `NEW_MECHANICS.md` for the per-item status table (decided / partially
decided / open questions) — update that file's status as items move from
design to implementation, so this file doesn't go stale.
