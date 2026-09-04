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
**Status: 🟡 Partially decided**

| Aspect | Status | Notes |
|---|---|---|
| Trigger condition | ✅ | Aggro score × mush block density near player |
| Mush density check method | ❓ | Radius and counting method (block count vs. % coverage) undecided |
| Recurring + loot | ✅ | Repeatable event, not one-time; drops loot |
| Base daily cap | ✅ | 3 boss spawns per player per day |
| Server-wide cap | ✅ | 15/day hard ceiling, whichever cap hits first applies |
| Chunk clustering formula | ✅ | Chebyshev distance. ≤5 chunks = clustered (9 total shared cap); ≥6 chunks = spread (3/player individual cap). No dead zone between tiers |
| Cap stickiness | ✅ | Cap consumption stays with the player even if they leave the cluster |
| Config exposure | ✅ | All thresholds (3/9/15/5-chunk cutoff) must be config-adjustable |
| Daily cap counter implementation | 🟡 | Cannot reuse vanilla mobcap (snapshot vs. persistent); needs custom per-player persistent counter, reset at day boundary |
| Day/night pacing | ✅ | Modeled on 7 Days to Die — minimal buildup by day, ramps at night, ~500 ticks buffer |
| Post-boss cooldown | ❓ | Undecided whether a cooldown separate from day/night pacing is needed |

---

## Turret
**Status: ✅ Architecture decided and built (MVP) — some sub-items are rough/placeholder, see notes**

| Aspect | Status | Notes |
|---|---|---|
| Architecture choice | ✅ | **Entity-based**, decided and built. `entity/Turret.java` extends `PathfinderMob` + `RangedAttackMob` — the natural fit for reusing vanilla ranged-attack `Goal`s, which the AI-base line below required anyway. No block/block-entity involved at all. |
| AI base | ✅ | Uses vanilla `RangedAttackGoal` (not `RangedBowAttackGoal` — see Immobility note) + `NearestAttackableTargetGoal<Monster>` unmodified, wired in `Turret#registerGoals()`. Targets hostile mobs, not the player — it's a defensive mob. |
| Immobility | ✅ | `Turret#travel(Vec3)` discards horizontal input (`super.travel(Vec3.ZERO)`) regardless of what any goal tries to do — stronger than just omitting wander goals, since `RangedAttackGoal` itself calls `moveTo()` when a target is out of range. Verified by inspection that `RangedAttackGoal` still aims/fires correctly at a stationary mob (it just never manages to close distance beyond its fixed radius, which for a turret is the desired behavior) — **not verified in a running client/server**, see Verification note below. |
| Sentry/manual toggle | ✅ | Shift-right-click with an empty hand flips `Turret#isActive()`, which adds/removes the attack goal from the `GoalSelector` at runtime (`updateAttackGoal()`). Also polled against `level().hasNeighborSignal(...)` every 10 ticks in `aiStep()` — a redstone signal forces the turret off regardless of its manual state, satisfying the "or a nearby lever/redstone signal" alternative from the spec without needing a companion block. |
| Multi-projectile support | ✅ | Single-slot ammo (`SynchedEntityData` `ItemStack`, right-click to load/withdraw, mirrors item frames). `Turret#performRangedAttack` switches on the loaded item: `minecraft:firework_rocket` → `FireworkRocketEntity`, `minecraft:fire_charge` → `SmallFireball`, anything else (including the new `THORN_AMMO` placeholder) → a plain `Arrow` with configurable damage (`Config.turretThornDamage`). No ammo loaded = turret aims but never fires. |
| Crafting recipe | ✅ | Two recipes, both shaped 3x3: `data/cubebuster/recipe/turret.json` (iron ingots + dispenser + crossbow + **`minecraft:oak_stairs`** as the "seat" placeholder — no seat item exists anywhere in the codebase yet, oak stairs picked as the closest vanilla stand-in, flagged here rather than silently guessed) and `turret_sentry.json` (same plus an eye of ender, per spec, producing the sentry variant that starts active instead of needing a manual toggle). |
| "Watching eye" core item | ⬜ | Still concept-only, not built — deliberately. Turret's ammo/toggle mechanisms don't hardcode "only turrets can hold an item slot" (the ammo slot is a plain field on `Turret`, not a shared/global registry), so a future standalone "watching eye" item for doors/traps isn't architecturally blocked, it just doesn't exist yet. |
| Thorn ammo | 🟡 | Turret accepts a placeholder `cubebuster:thorn_ammo` item (plain `Item`, see `ModItems.THORN_AMMO`) since no real "thorns" item exists in this branch. **Follow-up once the Cactus Economy branch merges**: delete the placeholder and point `Turret#isValidAmmo`/`performRangedAttack` at the real thorns item instead. |

**Verification note:** built and compiles clean (`./gradlew compileJava` / `./gradlew build`), but not run in a live client/server in this environment — no in-game confirmation that targeting, firing, the toggle, or the recipes actually behave as intended. Treat as implemented-but-unplaytested.

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
