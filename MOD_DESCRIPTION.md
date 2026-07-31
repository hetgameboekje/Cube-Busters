# CubeBuster

CubeBuster is a Minecraft mod that turns the zombie horde from a
background nuisance into a real threat. It adds six new zombie types, each
with its own special ability, plus a tension system that watches what you do
and sends stronger enemies after you the more careless you get. To help you
fight back, it also adds a line of reinforced glass blocks so your windows
don't become your weak point.

## The zombies

Every variant below is a genuine upgrade over a vanilla zombie — more
health, more damage, and a unique trick that vanilla zombies don't have.

- **Siege Zombie** — The base-raider. It spawns already holding an
  unbreakable netherite pickaxe and will dig straight through your walls to
  reach you — no door, no wall, no safe room stops it for long. If it finds
  other ordinary zombies nearby, it infects them and turns them into more
  Siege Zombies, so one can turn into a small horde. At night it gets faster
  and hits harder, and hurting a player enrages nearby Siege Zombies,
  making them dig and attack even quicker.

- **Green Zombie** — The climber. Found lurking in jungles, it can climb
  straight up walls like a spider, so height is no longer a safe escape.
  It only breaks blocks that are in its immediate way when it's closing in
  on you, rather than tunneling from a distance — making it a relentless
  close-quarters threat.

- **Blue Zombie** — The aquatic hunter. Built on top of a drowned, it
  swims fast, throws tridents, and can dig its way out if it gets stuck
  near water trying to reach you. It spawns more often during rainstorms,
  so a rainy night is more dangerous than a clear one.

- **Red Zombie** — The desert/Nether menace. Immune to fire and lava, it
  will happily walk through lava lakes to get to you, and comes with boosted
  health, damage, and armor on top of that. A perfect ambush threat in hot
  biomes and the Nether where players let their guard down.

- **Ender Zombie** — The teleporter. It carries a chorus fruit and gets
  one single free short-range teleport straight to you when it needs to
  close the distance — a nasty surprise if you thought you'd out-run it.

- **Screamer** — The horde caller. This one never spawns naturally; it
  only appears when a player has been careless for too long (see the aggro
  system below). Once it has a target, it periodically lets out a scream
  and summons more Siege Zombies around itself, escalating a fight into a
  siege if you don't deal with it quickly.

## The aggro system — the game is watching you

Instead of zombies just spawning at random, CubeBuster tracks an invisible
"aggro" score per player that goes up the more risks you take and slowly
decays over time if you play it safe:

- Opening chests/containers or sleeping in a bed raises your aggro.
- Being seen (in the open, not hidden behind walls) by a Siege Zombie that's
  hunting you raises it further the longer it can see you.
- Your aggro score is shown on your actionbar (e.g. "Aggro: 340/600 (Level 3)")
  so you always know how much heat you're building up.
- As your score climbs through 5 escalating levels, the game starts
  force-spawning tougher waves near you: first a lone Siege Zombie, then
  small packs of 2-3, then Screamers start showing up alongside them,
  eventually spawning multiple Screamers each calling in their own waves of
  Siege Zombies.
- These spawns intelligently avoid appearing right on top of you or inside
  a lit-up base, but they will spawn in any unlit area nearby — so a
  well-lit home is meaningfully safer than a dark one.
- If you back off and stop taking risks, the score decays back down over
  roughly ten minutes and the spawns taper off.

In short: raiding chests and sleeping carelessly makes the world notice you,
and the longer you ignore the warning signs, the worse what comes for you
gets — up to and including zombies calling in reinforcements on themselves.

## Reinforced defenses — protected glass

To give players a fighting chance at building a real base, CubeBuster adds
protected glass blocks and matching glass panes, each available in three
tiers of toughness:

- **Tier 1** — Tougher than vanilla glass, but always breakable by
  zombies given enough time.
- **Tier 2** — Breakable at night in the Overworld and always in the
  Nether, but safe during the day and completely safe in the End.
- **Tier 3** — Never breakable by any zombie — the strongest option for a
  true panic room.

All tiers are harder to break than plain glass (needs an iron tool, high
blast resistance), craft with normal recipes, and drop themselves when
mined, so you can wall yourself in with confidence instead of watching
zombies punch through your windows.

## Fully configurable

Every number behind these systems — spawn chances, aggro thresholds and
decay rate, dig speed, night-time buffs, which blocks count as
"protected," and more — is exposed in an in-game config file, so server
owners and pack makers can tune the difficulty up or down, or disable any
individual zombie variant entirely, without needing to edit code.
