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
