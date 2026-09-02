# CLAUDE.md

Guidance for Claude Code when working in this repo.

## What this is

NeoForge Minecraft mod (**1.21.1**, Neo `21.1.244`), mod id `cubebuster`,
package `dev.bergthaler.cubebuster`. See `README.md` for the full feature
rundown of what's currently shipped (zombie variants, AI goals, spawn
events, protected glass tier system, config). See `NEW_MECHANICS.md` for
planned/in-progress features and their status.

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
  tags/                    ModBlockTags - block-break permission logic for protected glass
src/main/resources/
  assets/cubebuster/   textures, models, blockstates, lang
  data/cubebuster/     recipes, loot tables, biome modifiers (neoforge spawn placements)
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
  to be nearest to) needs this — `SiegeZombie` currently does **not** have
  it (see Known Bugs below).
- Comments in `Config.java` are user-facing documentation (shown by config
  GUIs) — write them for a server admin, not a fellow programmer.

## Currently shipped: Aggro system

Per-player aggro score, **in-memory only** (`event/AggroManager.java`),
driving escalating force-spawns (levels 1-5) via
`event/AggroSpawnHandler.java` and `event/AggroTickHandler.java`, plus the
`Screamer` mob (`entity/Screamer.java`) that calls in more SiegeZombies.
See `README.md` for the level table and config knobs. `Screamer` uses its
own texture, `textures/entity/scream_siege.png`, via
`client/ScreamerRenderer.java`.

**Known gap vs. planned design (see `NEW_MECHANICS.md`):** this aggro
score decays within ~10 minutes and resets on server restart. The Horde
Boss system being designed needs aggro that **never decays/resets** and is
**persistent** — this is a behavior change to `AggroManager`, not a new
system.

## Known bugs (confirmed by source review, not yet fixed)

### 1. Aggro gain ignores creative/spectator mode
Two gain paths add score regardless of the player's game mode:

- `event/AggroInteractionHandler.java`, `maybeGainScore()` (~line 60) —
  container-open and bed-use gain has no `isCreative()`/`isSpectator()`
  check.
- `entity/ai/SiegeZombieSightAggroGoal.java`, `canUse()` (~line 36) —
  sight-based gain has the same gap. Vanilla mobs can still target/see
  creative players (they just take no damage), so this fires normally.

**Fix:** guard both call sites:
```java
// AggroInteractionHandler.maybeGainScore(...)
private static void maybeGainScore(ServerPlayer player) {
    if (player.isCreative() || player.isSpectator()) {
        return;
    }
    // ...existing cooldown + addScore logic unchanged
}
```
```java
// SiegeZombieSightAggroGoal.canUse(...)
if (target instanceof ServerPlayer player
        && !player.isCreative() && !player.isSpectator()
        && siegeZombie.hasLineOfSight(target)) {
    AggroManager.addScore(player, Config.aggroSightGainAmount);
}
```

### 2. SiegeZombie has no owner — can raise the wrong player's aggro
`SiegeZombie`'s target selector
(`entity/SiegeZombie.java`, `registerGoals()`) is a plain
`NearestAttackableTargetGoal<>(this, Player.class, false)` with
`FOLLOW_RANGE` 48 — it targets **whichever player is nearest**, not the
player whose aggro caused it to spawn. `SiegeZombieSightAggroGoal` then
credits sight-based aggro to `siegeZombie.getTarget()` — i.e. whoever it's
currently targeting.

With 2+ players online, a SiegeZombie spawned because of Player A's aggro
can retarget to Player B the moment B comes within its follow range,
crediting **B's** score for a mob B had no part in causing — which reads
as "my aggro went up and the other player wasn't even near [whatever
caused it]."

`Screamer` already solved exactly this class of problem with an
`ownerUUID` field (used today to enforce the per-player Screamer cap) —
`SiegeZombie` never got the same treatment.

**Fix:** give `SiegeZombie` the same ownership field, and gate the
sight-aggro credit on ownership instead of current AI target:

```java
// SiegeZombie.java — mirror Screamer's ownerUUID field + NBT save/load
private UUID ownerUUID;

public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
public UUID getOwnerUUID() { return ownerUUID; }

@Override
protected void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    if (ownerUUID != null) tag.putUUID("AggroOwner", ownerUUID);
}

@Override
protected void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.hasUUID("AggroOwner")) ownerUUID = tag.getUUID("AggroOwner");
}
```
```java
// AggroSpawnHandler.trySpawnFor(...) — tag SiegeZombies the same way Screamers already are
for (int i = 0; i < siegeZombieCount; i++) {
    SiegeZombie zombie = OpenAirSpawner.trySpawnNear(level, ModEntityTypes.SIEGE_ZOMBIE.get(), center,
            Config.aggroSafeZoneRadius, Config.aggroSpawnMaxRadius, maxVerticalDelta, true);
    if (zombie != null) {
        zombie.setOwnerUUID(player.getUUID());
    }
}
```
```java
// SiegeZombieSightAggroGoal.canUse(...) — credit the owner, not the current AI target
LivingEntity target = siegeZombie.getTarget();
UUID owner = siegeZombie.getOwnerUUID();
if (target instanceof ServerPlayer player
        && owner != null && owner.equals(player.getUUID())
        && !player.isCreative() && !player.isSpectator()
        && siegeZombie.hasLineOfSight(target)) {
    AggroManager.addScore(player, Config.aggroSightGainAmount);
}
```

**Note:** naturally-spawned SiegeZombies (village sieges, timed night
spawns — not from the aggro system) will have `ownerUUID == null`. Decide
whether those should ever grant sight-aggro at all (currently: no, under
this fix, since there's no owner to credit) — probably correct, since
they weren't caused by anyone's aggro score.

---

## Planned new mechanics

Design work for these is done (or in progress) in `NEW_MECHANICS.md` —
check that file for full detail and current status per item before
implementing. High-level summary of what's coming and how it fits the
existing codebase:

- **Aggro persistence rework** — make `AggroManager` scores
  never-decaying and persistent across restarts (likely player capability
  or saved data), instead of the current in-memory/decaying model. This is
  a **blocking prerequisite** for the Horde Boss cap system below.
- **Mush / Infected mechanic** — new creeper variant, spore/mush blocks,
  mush zombies, cure-item crafting chain (mortar & pestle). Net-new
  system, doesn't touch existing zombie variants directly.
- **Horde Boss** — recurring boss event, triggered by (per-player aggro
  score) × (mush block density near player). Integration layer only —
  reads from Aggro and Mush systems, they don't call into each other or
  into the boss.
  - Daily cap: 3/player, 15/server hard ceiling.
  - Chunk clustering (Chebyshev distance): ≤5 chunks = clustered group
    cap of 9 total; ≥6 chunks = individual 3/player cap.
  - Cap consumption is sticky to the player (survives leaving a cluster).
  - Cannot reuse vanilla mobcap for the daily quota (snapshot vs.
    persistent) — needs a custom per-player counter, reset at day
    boundary. Vanilla's per-player density-check *pattern* is reusable
    for the live clustering calculation only.
  - All thresholds config-adjustable, following the existing
    `Config.java` pattern.
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
