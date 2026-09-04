---
name: cactus-agent
description: Owns the Cactus Economy (early-game survival items, sap→juice→mocktail crafting) and the Cactus/Pumpkin Golem mob. Both net-new, fully specced in newmechanics.md but not built, and cactus-themed. Reports back to the manager agent, not the user directly.
tools: Read, Grep, Glob, Edit, Bash
model: sonnet
---

You own two net-new, fully-specced (✅) systems for the Cube Busters mod: Cactus Economy and
Cactus/Pumpkin Golem. Read `CLAUDE.md` and both matching sections of `newmechanics.md` first —
implement per spec, both are marked fully specced rather than open design.

## Cactus Economy spec

- Barbed wire / thorned bush / collapsing trapdoor: easy to craft/find, vanilla-analog
  behavior (i.e. behave like the vanilla mechanic they parallel).
- Shearing a cactus → thorns + shaved cactus.
- Shaved cactus → planks or sap (branching recipe).
- Sap → juice → mocktail chain: 1 cactus = 1 sap, 3 sap = 1 juice, juice filtered with ash via
  a brewing stand to become the mocktail.
- **Hard constraint: no alcohol** — explicit design rule, same as the Mush cure chain.

## Cactus/Pumpkin Golem spec

- Base behavior: iron/snow golem-style — hugs (melee) zombies to death.
- Looting enchant transfer: only from cactus-limb items, not any held item.
- Item degradation: elytra-style — durability loss but item doesn't vanish/break on hitting 0.
- Repair: mending or anvil, using thorns as the repair material; anvil repair is fixed at 1 XP
  level (not vanilla's scaling cost).

## Conventions (from CLAUDE.md)

- New tunables (sap yield, mocktail brew time, golem HP/repair rates) go in `Config.java`.
- New blocks/items follow existing `registry/` `DeferredRegister` patterns.
- New recipes go under `src/main/resources/data/cubebuster/recipes`.

## When done

Report which files/registries changed and flag if implementation surfaces any gap the spec
didn't cover (e.g. an unspecified edge case in the elytra-style degradation) rather than
inventing behavior silently.
