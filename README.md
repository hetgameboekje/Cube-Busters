# Zombie Survival

A Minecraft mod/plugin concept where zombies become smarter and more dangerous by gaining custom survival abilities.

## Concept

This project recreates an older server idea where zombies were no longer limited to vanilla behavior.  
Instead, they could detect players from far away, alter the environment in specific ways, and build upwards to reach their targets.

## Planned abilities

### 1. Long-distance player detection
Zombies can detect and track players from a much greater distance than vanilla mobs.

### 2. Selective environment manipulation
Zombies may only interact with a limited whitelist of blocks:
- Oak Logs
- Glass
- Stone Bricks

Possible interactions:
- Breaking blocking structures
- Replacing or using blocks to continue pursuit
- Opening paths toward the player

### 3. Upward building
If a player is above them, zombies can place blocks to climb upward.

Rules:
- No teleporting
- No clipping through blocks
- Movement must remain physically valid
- Building should only happen when pathfinding cannot otherwise reach the target

## Technical direction

Preferred implementation:
1. Fabric mod
2. Forge/NeoForge mod
3. Spigot plugin only if the same behavior can be implemented cleanly

## Design goals

- Keep zombie behavior believable
- Avoid unfair “cheating” movement
- Restrict griefing to a small whitelist of blocks
- Make the system configurable
- Keep the logic modular per ability

## Planned architecture

Suggested systems:
- DetectionGoal
- SelectiveBlockBreakGoal
- ZombieTowerBuildGoal
- Configurable block whitelist
- Cooldowns and range limits per ability

## Configuration ideas

Config options:
- Detection range
- Enabled block whitelist
- Build cooldown
- Break cooldown
- Max vertical build height
- Per-world or per-dimension enable/disable

## Notes

This project is based on an old Minecraft server concept and is being rebuilt as a modern implementation.
