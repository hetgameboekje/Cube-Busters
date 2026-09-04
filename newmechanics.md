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
**Status: ✅ Fully specced — not built**

| Aspect | Status | Notes |
|---|---|---|
| Infected creeper behavior | ✅ | No explosion damage; spreads spores on grass/sand/gravel/stone |
| Mush block spread | ✅ | Snow-layer-like spread, should extend vertically like vines |
| Mush zombie/skeleton spawn | ✅ | Retains base variant powers, lower max HP, slower to mine |
| Mush ball crafting | ✅ | Harvest with shovel/shears/hoe → mush block or infection potion |
| Cure chain (mortar & pestle → antibiotic firework) | ✅ | Full recipe chain defined |
| Mortar & pestle secondary uses | ✅ | Dyes, cobblestone→gravel, gravel→sand, dirt→sand, sand→dust |
| No-alcohol constraint | ✅ | Explicit design rule |

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

**Still needed before this can be considered fully verified:** the Mush/Infected mechanic (separate PR) hasn't
merged yet, so `ModBlockTags.MUSH_BLOCKS` is currently tagged with a placeholder
(`minecraft:brown_mushroom_block`, see `data/cubebuster/tags/block/mush_blocks.json`) rather than real mush
blocks. The density scan, trigger math, caps, and pacing are all implemented and compile-verified, but haven't
been played against real mush terrain.

---

## Turret
**Status: 🟡 Partially decided**

| Aspect | Status | Notes |
|---|---|---|
| AI base | ✅ | Reuse vanilla skeleton `RangedAttackGoal`/`RangedBowAttackGoal` + `NearestAttackableTargetGoal` |
| Immobility | ❓ | Needs movement/wander goals stripped; some attack-goals assume repositioning — needs handling |
| Sentry/manual toggle | ❓ | Needs custom `GoalSelector` add/remove switch based on block config |
| Multi-projectile support | ❓ | Vanilla `RangedAttackMob` only supports one projectile type; needs custom interface, dispenser-like item-based switch |
| Architecture choice | ❓ | Undecided: skeleton-based entity AI vs. block-entity with dispenser-style `DispenseItemBehavior` |
| Crafting recipe | 🟡 | Seat + dispenser + crossbow + iron (+ eye of ender for sentry, placeholder material) |
| "Watching eye" core item | ⬜ | Concept only — future reuse for doors/traps not designed |
| Thorn ammo | ✅ | Thorns usable as turret ammunition |

---

## Cactus Economy (early game)
**Status: ✅ Fully specced — not built**

| Aspect | Status | Notes |
|---|---|---|
| Barbed wire / thorned bush / collapsing trapdoor | ✅ | Easy to craft/find, vanilla-analog behavior |
| Shearing → thorns + shaved cactus | ✅ | |
| Shaved cactus → planks or sap | ✅ | |
| Sap → juice → mocktail chain | ✅ | 1 cactus = 1 sap, 3 sap = 1 juice, filtered with ash via brewing stand |
| No alcohol | ✅ | |

---

## Cactus/Pumpkin Golem
**Status: ✅ Fully specced — not built**

| Aspect | Status | Notes |
|---|---|---|
| Base behavior | ✅ | Iron/snow golem–style, hugs zombies to death |
| Looting enchant transfer | ✅ | Cactus-limb items only |
| Item degradation | ✅ | Elytra-style (doesn't vanish on break) |
| Repair | ✅ | Mending or anvil, thorns as repair material, fixed at 1 XP level |

---

## Custom Structures
**Status: ⬜ Not started**

| Aspect | Status | Notes |
|---|---|---|
| Structure types (mineshafts, sieged villages, bunkers) | ⬜ | Named as concepts only |
| Spawn conditions | ⬜ | Not defined |
| Loot tables | ⬜ | Not defined |

---

## Datapack Integration (original issue scope)
**Status: ⬜ Not revisited**

The original issue (#2) was titled around integrating an existing datapack/texturepack into the mod. This got superseded by the broader feature discussion above and hasn't been addressed directly yet.
