# New Mechanics — Status Overview

Tracks design status per mechanic discussed for Cube Busters. Status legend:

- ✅ **Decided** — design is settled, ready to implement
- 🟡 **Partially decided** — core approach agreed, open sub-questions remain
- ❓ **Open** — not yet decided
- ⬜ **Not started** — concept only, no design work done

---

## Aggro System
**Status: ✅ Persistence rework built**

| Aspect | Status | Notes |
|---|---|---|
| Score triggers (chests, sprinting, noise) | ✅ | |
| Decay rate | ✅ | Score now holds for `aggroDecayHoldTicks` (default 100 ticks / 5s) after the last gain before decay resumes, instead of decaying continuously |
| Score reset | ✅ | Persists indefinitely across disconnects and server restarts (data attachment on the player, `ModAttachmentTypes.AGGRO`); still resets to 0 on death via `AggroInteractionHandler` |
| Per-player tracking | ✅ | Implemented via NeoForge data attachment (`AggroState` record: score + decay-resume tick), replacing the old in-memory `UUID -> Integer` map — unblocks the Horde Boss cap system below |
| Eye-icon difficulty mapping | ✅ | disabled → hidden → easy (closed) → medium (slightly open, see-through-walls) → hard (wide open, stat boost) |

---

## Mob Cap / Performance System
**Status: ✅ Design decided — not built**

| Aspect | Status | Notes |
|---|---|---|
| Hidden potential-spawn counter | ✅ | Increments on screamer scream / aggro-triggered spawn instead of spawning more mobs |
| Scaling behavior | ✅ | Existing zombies get faster/tougher/stronger as counter rises |
| Reuse of vanilla mobcap | 🟡 | Only the per-player density-check *pattern* is reusable; the counter itself must be custom (vanilla mobcap is a snapshot, not a persistent value) |

---

## Mob Variants & Behavior
**Status: 🟡 Partially decided**

| Aspect | Status | Notes |
|---|---|---|
| Lore-based zombie names | ❓ | Concept agreed (e.g. "Corrupted Traveler"), no names chosen |
| Green zombie climb-stuck bug | ❓ | Known bug, not fixed |
| Wall-aggro extension | 🟡 | Works as intended currently; wants further extension tied to aggro tiers |
| Siege zombie day-based gear scaling | 🟡 | Capped at diamond chestplate/leggings; no cap on the scaling *curve* itself yet |
| Siege pickaxe | 🟡 | Drop-only (not craftable) — decided. Custom material — ❓ still using netherite pickaxe as placeholder |
| Screamer hitboxes | ❓ | Want smaller/slower variant to separate from crowd; not designed |

---

## Mush / Infected Mechanic
**Status: ✅ Built**

| Aspect | Status | Notes |
|---|---|---|
| Infected creeper behavior | ✅ | `entity/InfectedCreeper.java` (plain Creeper subtype) + `event/InfectedCreeperHandler.java` (an `ExplosionEvent.Detonate` listener that zeroes out the blast's block/entity damage, applies the `mush_infection` effect to anyone caught in it, and seeds MushBlock spores in a radius - `explodeCreeper()` itself is private in vanilla Creeper, so this couldn't be done as a method override) |
| Mush block spread | ✅ | `block/MushBlock.java` extends vanilla `SnowLayerBlock` (reuses its 1-8 layer stacking/partial-height collision), overrides `randomTick` to thicken, then either climb onto the block above (vine-like) or creep onto a neighboring grass/sand/gravel/stone block once fully thickened |
| Mush zombie/skeleton spawn | ✅ | `entity/MushZombie.java` / `entity/MushSkeleton.java` - full base-variant behavior, max HP scaled down at construction time via `Config.mushMobHealthMultiplier`; MushZombie also mines slower via a new `SlowMiningMob` interface hook in `BlockBreakingGoal` |
| Mush ball crafting | ✅ | `data/cubebuster/loot_table/blocks/mush_block.json`: hoe → Mush Ball, shovel/shears → Mush Block (silk-touch-like), anything else → 25% chance of an Infection Potion. Tool-to-outcome mapping was a judgment call, spec didn't nail it down further |
| Cure chain (mortar & pestle → antibiotic firework) | ✅ | `item/MortarAndPestleItem.java` grinds a Mush Ball into Antibiotic Paste (right-click, not a crafting recipe - see below); `data/cubebuster/recipe/antibiotic_firework.json` assembles Paste + paper + gunpowder into the cure; `item/AntibioticFireworkItem.java` removes the `mush_infection` effect and grants a short Regeneration burst on use |
| Mortar & pestle secondary uses | ✅ | Same item, two right-click behaviors: `useOn` grinds world blocks in place (cobblestone→gravel, gravel→sand, dirt→sand), `use` grinds a held item in the other hand (sand→Dust, cactus→green dye, Mush Ball→Antibiotic Paste). Implemented as right-click behavior rather than crafting recipes because `Item#getCraftingRemainingItem()` is `final` in vanilla and can't cleanly self-reference without extra indirection - the spec explicitly allowed either approach |
| No-alcohol constraint | ✅ | Nothing in this chain touches the brewing stand or potion items - Infection Potion and Antibiotic Firework are both plain custom `Item` subclasses with their own use logic |

Also added: `mush_infection` MobEffect (`effect/MushInfectionMobEffect.java`, `registry/ModMobEffects.java`) - periodic non-lethal damage (same "never below 1 HP" rule as vanilla Poison), the thing Infection Potions/creeper blasts apply and Antibiotic Fireworks cure. The Dust item currently has no further use beyond being a base ingredient for future recipes (design call, per the brief's "don't over-design this").

---

## Horde Boss (integration layer)
**Status: ✅ Built (needs real mush blocks to test end-to-end)**

| Aspect | Status | Notes |
|---|---|---|
| Trigger condition | ✅ | Aggro score × mush block density near player, checked every `hordeBossCheckIntervalTicks`; see `event/HordeBossSpawnHandler.java` |
| Mush density check method | ✅ | **Decided**: simple block-count within `hordeBossDensityRadius` blocks (cube scan), compared against `hordeBossTriggerThreshold` as the aggro×count product. Chosen over % ground-coverage - simpler to implement/tune, no need to define "ground" in 3D or normalize against terrain. See `event/HordeBossDensity.java` |
| Recurring + loot | ✅ | `entity/HordeBoss.java` (bigger/tankier Zombie variant), loot at `data/cubebuster/loot_table/entities/horde_boss.json` |
| Base daily cap | ✅ | `hordeBossPlayerDailyCap` (default 3) |
| Server-wide cap | ✅ | `hordeBossServerDailyCap` (default 15), whichever cap hits first applies |
| Chunk clustering formula | ✅ | Chebyshev distance via `hordeBossClusterChunkRadius` (default 5, inclusive - no dead zone). Clustered spawns share `hordeBossClusterCap` (default 9) *on top of* each player's own individual cap still applying - see `event/HordeBossSavedData.java` |
| Cap stickiness | ✅ | Per-player daily count lives in the `HORDE_BOSS_CAP` data attachment and is never decremented/re-checked against current position - see `event/HordeBossCapManager.java` |
| Config exposure | ✅ | All thresholds config-adjustable under the "Horde Boss" section of `Config.java` |
| Daily cap counter implementation | ✅ | **Decided**: per-player count via the same data-attachment pattern as `AggroState`/`ModAttachmentTypes.AGGRO` (`event/HordeBossState.java` + `ModAttachmentTypes.HORDE_BOSS_CAP`); server-wide count + clustering via vanilla `SavedData` on the Overworld (`event/HordeBossSavedData.java`) - genuinely server-scoped state, not per-player, so `SavedData` rather than another attachment. Day boundary = world game time / 24000, not `getDayTime()` (sleep/commands can shift that) - see `HordeBossCapManager.currentDay` |
| Day/night pacing | ✅ | Modeled on 7 Days to Die - `hordeBossDayPacingMultiplier` (default 0.05) during full daylight, linear ramp to full strength across a `hordeBossPacingRampTicks`-wide window (default 500) centered on dusk, full strength overnight, ramps back down across the same window centered on dawn. See `event/HordeBossPacing.java` |
| Post-boss cooldown | ✅ | **Decided**: yes, a per-player cooldown (`hordeBossCooldownTicks`, default 12000 = half a day) starting the moment a boss spawns, tracked in the same `HordeBossState` attachment - day/night pacing alone doesn't prevent an immediate re-trigger right after a fight ends |

**Still needed before this can be considered fully verified:** the Mush/Infected mechanic has since merged, and
`data/cubebuster/tags/block/mush_blocks.json` now tags the real `cubebuster:mush_block` instead of the
placeholder `minecraft:brown_mushroom_block`. The density scan, trigger math, caps, and pacing are all
implemented and compile-verified, but haven't been played against real mush terrain in a running client/server.

---

## Turret
**Status: ✅ Architecture decided and built (MVP) — some sub-items are rough/placeholder, see notes**

| Aspect | Status | Notes |
|---|---|---|
| Architecture choice | ✅ | **Entity-based**, decided and built. `entity/Turret.java` extends `PathfinderMob` + `RangedAttackMob` — the natural fit for reusing vanilla ranged-attack `Goal`s, which the AI-base line below required anyway. No block/block-entity involved at all. |
| AI base | ✅ | Uses vanilla `RangedAttackGoal` (not `RangedBowAttackGoal` — see Immobility note) + `NearestAttackableTargetGoal<Monster>` unmodified, wired in `Turret#registerGoals()`. Targets hostile mobs, not the player — it's a defensive mob. |
| Immobility | ✅ | `Turret#travel(Vec3)` discards horizontal input (`super.travel(Vec3.ZERO)`) regardless of what any goal tries to do — stronger than just omitting wander goals, since `RangedAttackGoal` itself calls `moveTo()` when a target is out of range. Verified by inspection that `RangedAttackGoal` still aims/fires correctly at a stationary mob (it just never manages to close distance beyond its fixed radius, which for a turret is the desired behavior) — **not verified in a running client/server**, see Verification note below. |
| Sentry/manual toggle | ✅ | Shift-right-click with an empty hand flips `Turret#isActive()`, which adds/removes the attack goal from the `GoalSelector` at runtime (`updateAttackGoal()`). Also polled against `level().hasNeighborSignal(...)` every 10 ticks in `aiStep()` — a redstone signal forces the turret off regardless of its manual state, satisfying the "or a nearby lever/redstone signal" alternative from the spec without needing a companion block. |
| Multi-projectile support | ✅ | Single-slot ammo (`SynchedEntityData` `ItemStack`, right-click to load/withdraw, mirrors item frames). `Turret#performRangedAttack` switches on the loaded item: `minecraft:firework_rocket` → `FireworkRocketEntity`, `minecraft:fire_charge` → `SmallFireball`, anything else (including `ModItems.THORNS`) → a plain `Arrow` with configurable damage (`Config.turretThornDamage`). No ammo loaded = turret aims but never fires. |
| Crafting recipe | ✅ | Two recipes, both shaped 3x3: `data/cubebuster/recipe/turret.json` (iron ingots + dispenser + crossbow + **`minecraft:oak_stairs`** as the "seat" placeholder — no seat item exists anywhere in the codebase yet, oak stairs picked as the closest vanilla stand-in, flagged here rather than silently guessed) and `turret_sentry.json` (same plus an eye of ender, per spec, producing the sentry variant that starts active instead of needing a manual toggle). |
| "Watching eye" core item | ⬜ | Still concept-only, not built — deliberately. Turret's ammo/toggle mechanisms don't hardcode "only turrets can hold an item slot" (the ammo slot is a plain field on `Turret`, not a shared/global registry), so a future standalone "watching eye" item for doors/traps isn't architecturally blocked, it just doesn't exist yet. |
| Thorn ammo | ✅ | Wired directly to the real `ModItems.THORNS` item from the Cactus Economy chain (the placeholder `THORN_AMMO` item was removed once both branches were merged together). |

**Verification note:** built and compiles clean (`./gradlew compileJava` / `./gradlew build`), but not run in a live client/server in this environment — no in-game confirmation that targeting, firing, the toggle, or the recipes actually behave as intended. Treat as implemented-but-unplaytested.

---

## Cactus Economy (early game)
**Status: ✅ Built**

Blocks/behavior in `dev.bergthaler.cubebuster.block` (`BarbedWireBlock`, `ThornedBushBlock`,
`CollapsingTrapdoorBlock`); shearing in `event/CactusShearHandler.java`; brewing wiring in
`event/ModBrewingRecipes.java`; recipes under `data/cubebuster/recipes/`; tunables in `Config.java`
("Cactus Economy" section).

| Aspect | Status | Notes |
|---|---|---|
| Barbed wire / thorned bush / collapsing trapdoor | ✅ | Barbed wire = cobweb-shaped hazard (slow + DoT); thorned bush = non-solid `BushBlock` with cactus-style contact damage; collapsing trapdoor springs open a configurable delay after something steps on it, then auto-recloses (reusable trap, not a one-shot break) |
| Shearing → thorns + shaved cactus | ✅ | `CactusShearHandler` - shears + right-click on a cactus block |
| Shaved cactus → planks or sap | ✅ | Both are separate crafting-table recipes off the same item |
| Sap → juice → mocktail chain | ✅ | 1 cactus (or shaved cactus) = 1 sap, 3 sap = 1 juice; juice + ash at a brewing stand = mocktail (`ModBrewingRecipes`, `AnvilUpdateEvent`-style `RegisterBrewingRecipesEvent` hook). Ash is smelted from rotten flesh |
| No alcohol | ✅ | No alcohol-themed items/flavor text anywhere in the chain |

---

## Cactus/Pumpkin Golem
**Status: ✅ Built**

`entity/CactusGolem.java` (extends vanilla `IronGolem`, goals fully replaced - targets `Zombie` and
subclasses, never players); Looting/degrade/repair logic in `event/CactusLimbHandler.java`; tunables
in `Config.java` ("Cactus/Pumpkin Golem" section). Reuses vanilla's `IronGolemRenderer`/model
wholesale rather than bespoke art - see the CLAUDE.md note for the follow-up.

| Aspect | Status | Notes |
|---|---|---|
| Base behavior | ✅ | Iron golem–style, hugs zombie-family mobs to death (never players) |
| Looting enchant transfer | ✅ | Cactus-limb items only. Vanilla's own Looting enchantment effect is hard-gated to player attackers (see `data/minecraft/enchantment/looting.json`'s `entity_properties: player` requirement), so this is a custom `LivingDropsEvent` handler that reads the limb's Looting level and duplicates drops with a per-level chance - an approximation of vanilla's loot reroll, not a byte-for-byte reproduction |
| Item degradation | ✅ | Elytra-style: damage value is set directly rather than via `ItemStack#hurtAndBreak`, so it never vanishes/breaks at max damage, just stops granting Looting |
| Repair | ✅ | Mending works via the `enchantable/durability` tag; anvil repair with thorns is fixed at 1 XP level via a custom `AnvilUpdateEvent` handler (bypasses vanilla's scaling cost) |

---

## Custom Structures
**Status: 🟡 Partially decided**

Design proposal below covers the first structure to build (Bunker), the
second in line (Sieged Village), and defers Mineshaft. Nothing is
implemented yet — this is the design writeup requested to unblock
implementation, no structure JSON/NBT/Java has been written.

| Aspect | Status | Notes |
|---|---|---|
| Which structure(s) first | ✅ | Bunker first, Sieged Village second, Mineshaft deferred — see reasoning below |
| Generation approach | ✅ | Hand-placed NBT + `structure_set` for Bunker; jigsaw injection into vanilla village pools for Sieged Village |
| Spawn/placement conditions | 🟡 | Approach decided (structure_set spacing/separation + biome tags); concrete numeric values (spacing, separation, biome list) not chosen |
| SiegeZombie/mush tie-in | ✅ | Baked-in `minecraft:mob_spawner` block entities in the structure template, mirroring vanilla dungeons — deliberately *not* routed through `AggroSpawnHandler`/`AggroManager` (see reasoning below) |
| Loot table shape | ✅ | `minecraft:chest`-type loot table, multi-pool, referencing existing items (Siege Pickaxe, etc.) |
| Effort estimate | ✅ | See below |

### Why Bunker first, Sieged Village second, Mineshaft deferred

- **Bunker** — a small, fully hand-authored room/pod (player-built-lore
  "survivor bunker"). It's a single self-contained structure NBT with no
  dependency on vanilla structure internals, so it's the cheapest way to
  prove out the whole pipeline (structure NBT authoring → `structure_set`
  placement → spawner block → loot table → in-game test loop) before
  investing in anything more complex. **Build this first.**
- **Sieged Village** — reuses vanilla's existing village jigsaw pools
  rather than building a village from scratch: inject a new "siege camp"
  jigsaw piece (barricades, a SiegeZombie spawner, broken carts) into the
  vanilla `village/plains/houses`-style pools via a data pack addition, so
  it naturally appears as a variant encounter across existing village
  biome variants. Higher payoff (ties directly into the existing siege
  theme, feels native to normal village exploration) but meaningfully more
  work than Bunker because it means composing with vanilla's jigsaw pool
  system across multiple village biome variants instead of a single fixed
  NBT. **Build second**, once the Bunker pipeline is proven.
- **Mineshaft** — deferred. Vanilla mineshafts are irregular *procedural*
  corridor structures (`MineshaftPieces`), not template-pool/jigsaw driven
  like villages — there's no equivalent "add a jigsaw piece" injection
  point, so hooking in would mean either patching vanilla's generator code
  (bad — invasive, brittle to updates) or growing a wholly separate custom
  mineshaft generator from scratch (expensive, and the payoff — visually
  differentiating it from vanilla mineshafts — is lower than Bunker or
  Sieged Village). Revisit only after both other structures have shipped
  and the team has jigsaw/structure-gen experience from Sieged Village.

### Spawn / placement conditions

Recommend the standard vanilla worldgen datapack shape, same layer the
`neoforge/biome_modifier/*.json` files already sit in
(`src/main/resources/data/cubebuster/`), extended with new folders:

- `data/cubebuster/worldgen/structure/bunker.json` — the structure
  definition (type `minecraft:jigsaw` with a single-piece pool for
  Bunker, or `minecraft:structure` referencing a static NBT if the
  simpler non-jigsaw structure type suffices — recommend starting with
  the simpler static NBT structure type for Bunker since it's a single
  fixed room, saving jigsaw complexity for Sieged Village where it's
  actually needed).
- `data/cubebuster/worldgen/structure_set/bunker.json` — placement:
  spacing/separation (vanilla-style grid jitter) and a biome filter tag
  (recommend restricting to overworld land biomes excluding oceans/rivers
  to start, narrowing later once playtested). Exact spacing/separation
  numbers are the one open sub-question — start conservative (rarer than
  vanilla villages, denser than strongholds) and tune from playtesting
  rather than guessing a final number now.
- `data/cubebuster/worldgen/template_pool/` — only needed once Sieged
  Village's jigsaw pieces exist; Bunker doesn't need this if it uses the
  static-NBT structure type.
- Biome restriction expressed the same way the existing
  `neoforge/biome_modifier/*_zombie_spawns.json` files already do (biome
  tag references), for consistency with the rest of the datapack.

### How SiegeZombie/mush spawning ties in

Recommend **baked-in spawner block entities inside the structure
template** (a `minecraft:mob_spawner` block with `SpawnData` set to
`cubebuster:siege_zombie`, same pattern vanilla dungeons/trial chambers
use), not a code-driven trigger. Reasoning:

- `AggroSpawnHandler`/`OpenAirSpawner` are built around a *live, timed,
  per-player* loop — periodic checks against a player's current aggro
  score, searching for open-air spots near wherever that specific player
  currently is (see `AggroSpawnHandler.trySpawnFor`,
  `OpenAirSpawner.trySpawnNear`). That model doesn't fit a static
  structure sitting at a fixed world location that any player might find
  at any aggro level (including zero) — a structure's spawner should fire
  based on a player entering/breaking into the structure, not on their
  aggro score.
- A baked spawner block requires no new Java code at all — it's pure
  worldgen data, consistent with the "config/data-driven, not hardcoded"
  convention in `CLAUDE.md`, and it's the exact mechanism vanilla already
  uses for "structure you break into has guards inside."
- **Explicitly not reusing `SiegeZombieSpawner`/`BlueZombieSpawner`** — those
  helpers implement the "spawn N mobs in the open air near a player"
  pattern for the *force-spawn* systems, which is a different problem
  (searching for a valid position near a moving player) than "place a
  spawner block at a structure-author-chosen fixed position inside a
  hand-built room." Reusing them here would add a dependency in the wrong
  direction.
- Optional future integration point (flagged, not decided): entering a
  Bunker/Sieged Village could call into `AggroManager` to bump the
  player's score, tying structure exploration into the aggro/Horde Boss
  economy. This is a nice-to-have worth a follow-up design note once the
  Horde Boss system (which this file already flags as blocked on the
  Aggro persistence rework) is closer to landing — not required for a
  first structure ship.
- Mush blocks: for the first pass, place them **statically** in the
  structure NBT (decorative pre-infested rooms) rather than wiring live
  mush-spread logic at structure-generation time — the Mush mechanic's
  spread behavior (see the "Mush / Infected Mechanic" section above) is
  designed to run at tick-time on existing blocks, not at structure
  placement time, so there's nothing structure-specific to build there
  beyond authoring the template with mush blocks already present.

### Loot table shape

Follow the existing loot table pattern in
`src/main/resources/data/cubebuster/loot_table/blocks/protected_glass_t1.json`
(shown above — `type`, `pools`, `rolls`, `entries`) but using
`"type": "minecraft:chest"` and multiple pools, the same way vanilla
structure loot tables are laid out, under a new
`data/cubebuster/loot_table/chests/` folder:

- `loot_table/chests/bunker_stash.json` — pool 1: common survival loot
  (food, basic tools, low-tier protected glass) at `rolls: 3-5`; pool 2:
  rare loot (a chance at a Siege Pickaxe drop, referencing the existing
  siege pickaxe item) at `rolls: 0-1` with a low weight/low quality roll,
  matching the "drop-only, not craftable" design already decided for the
  Siege Pickaxe in the Mob Variants section above.
- `loot_table/chests/sieged_village_camp.json` — similar two-pool shape,
  reusing the same common pool where reasonable, with village-raid-themed
  rare loot (still TBD which specific items — flagged as an open item to
  fill in once Bunker's loot table pattern is validated in-game).
- The loot table is referenced from a chest block placed directly inside
  the structure NBT/template (`LootTable` NBT tag on the chest block
  entity), exactly how vanilla structures wire up their loot — no custom
  Java loot-injection code needed.

### Rough effort / scope estimate

- **Bunker** (build first): small, roughly 1-2 focused implementation
  sessions once the structure NBT is modeled/built-and-saved in a test
  world — `structure_set` + `structure` JSON, one `mob_spawner` block, one
  chest + loot table, biome/spacing tuning via playtesting. No jigsaw, no
  vanilla pool interaction, minimal surface area for bugs.
- **Sieged Village** (build second): noticeably larger — composing new
  jigsaw pieces into vanilla's existing village template pools, testing
  across each village biome variant (plains/desert/savanna/taiga/snowy),
  making sure the injected piece doesn't break existing village jigsaw
  connectivity. Budget meaningfully more time than Bunker; exact sizing
  TBD until Bunker's pipeline surfaces how much friction the jigsaw
  tooling actually has.
- **Mineshaft**: not estimated — deferred, see reasoning above. Revisit
  and re-scope only after Bunker and Sieged Village have shipped.

---

## Datapack Integration (original issue scope)
**Status: ⬜ Not revisited**

The original issue (#2) was titled around integrating an existing datapack/texturepack into the mod. This got superseded by the broader feature discussion above and hasn't been addressed directly yet.
