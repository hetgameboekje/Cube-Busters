# New Mechanics — Status Overview

Tracks design status per mechanic discussed for Cube Busters. Status legend:

- ✅ **Decided** — design is settled, ready to implement
- 🟡 **Partially decided** — core approach agreed, open sub-questions remain
- ❓ **Open** — not yet decided
- ⬜ **Not started** — concept only, no design work done

---

## Aggro System
**Status: 🟡 Partially decided — blocking dependency**

| Aspect | Status | Notes |
|---|---|---|
| Score triggers (chests, sprinting, noise) | ✅ | |
| Decay rate | 🟡 | Currently decays too fast; target ~100 ticks / 5s hold before decay |
| Score reset | ✅ | Never resets — persists indefinitely, including after boss events and player disconnects |
| Per-player tracking | ❓ | **Not implemented yet — blocks the Horde Boss cap system below** |
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
