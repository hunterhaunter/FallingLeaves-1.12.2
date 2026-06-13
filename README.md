# Falling Leaves

A 1.12.2 backport of [Falling Leaves](https://www.curseforge.com/minecraft/mc-mods/falling-leaves-forge) by Cheaterpaul.

A neat little particle effect for leaf blocks. Leaves break off the underside of the canopy
around you, drift down on the wind, tumble end-over-end, land and settle, then fade away — each
one tinted to match the leaf it came from.

Requires Forge 14.23.5.2860+. 100% client-side, no mixins, no coremod.

## How it works

Falling Leaves watches the leaf blocks near you and lets leaves break loose from the bottom of
the canopy (so they fall into open air, not through other leaves). Once loose, a leaf is driven
by a small physics model:

- **Wind** is procedural — a smooth-noise field with **calm**, **windy**, and **stormy** states
  that follow the weather. Leaves sway toward the wind while airborne; calm days barely nudge
  them, storms blow them sideways.
- **Falling** leaves tumble as they go, spin up over a few seconds, and fall a little faster in
  the rain when they're under open sky.
- **Landing** snaps them in place — they stick where they touch down and rest there rather than
  sliding around, then fade out cleanly over their last few ticks.
- **Water** catches a leaf and lets it glide briefly across the surface before it stops.

Punching a leaf block knocks a small burst of leaves loose, and conifers (spruce and friends)
drop needle-shaped particles with their own spawn rate.

## Colour and seasons

A leaf particle isn't a flat sprite it samples the **actual leaf block's texture** and its
**biome tint**, so oak, birch, jungle, spruce and modded leaves all fall in
the right shade for where you're standing.

Because that colour is read live from the game's block-colour pipeline, **[Serene Seasons](https://www.curseforge.com/minecraft/mc-mods/serene-seasons) support is automatic and dynamic**: Serene Seasons recolours leaves per season, and the falling leaves follow along — a leaf that breaks
off in autumn falls in autumn colours, and the same blocks fade toward winter as the season
turns. On top of that, the `seasonFallRate` setting scales **how many** leaves fall each season,
so autumn can be a steady drift of leaves and winter a sparse trickle. With Serene Seasons absent,
none of this gets in the way — the multiplier is simply 1.0.

## Compatibility

Built to stay out of other mods' way: there are **no mixins and no coremod**. Everything runs
through ordinary Forge events, so no vanilla class is patched or replaced. Leaves are recognised
through three independent checks — `instanceof BlockLeaves`, Forge's `isLeaves()` method, and the
`treeLeaves` OreDictionary entry — so modded leaves are picked up without any per-mod patching.

| Mod | How it interacts |
| --- | --- |
| **Dynamic Trees** | Works out of the box — its leaves extend `BlockLeaves`, so leaves fall from dynamic trees with no setup. It has no particle system of its own, so nothing doubles up. |
| **Serene Seasons** | Fully supported. Leaves recolour with the season automatically, and `seasonFallRate` adjusts how many fall per season. |
| **Weather2 / Weather2 Remastered** | These add their own wind-blown foliage particles, so leaves may visually double up. Falling Leaves detects them and shows a one-time notice (below). |

When a mod that also spawns leaf or foliage particles is present, Falling Leaves prints a single
chat notice the first time you load a world (shown once, ever) so you can disable one effect if
you'd rather not see both. Turn the notice off with `showCompatWarning` in the config.

## Configuration

Edit `config/fallingleaves.cfg`, or use the in-game config screen from the mod list.

| Setting | Default | What it does |
| --- | --- | --- |
| `leafSize` | `5` | Size of the leaf particles. |
| `leafLifespan` | `200` | How long (in ticks) a leaf lasts before it fades out. |
| `leafSpawnRate` | `20` | How many leaves spawn overall. |
| `coniferLeafSpawnRate` | `2` | Spawn rate for conifer (needle) leaves. |
| `dropFromPlayerPlacedBlocks` | `true` | Whether player-placed leaf blocks also drop leaves. |
| `minimumFreeSpaceBelow` | `1` | Open space needed below a leaf block for a leaf to spawn. |
| `disableWind` | `false` | Turns off wind, so leaves fall straight down. |
| `windlessDimension` | `the_nether, the_end` | Dimension names where wind is disabled. |
| `showCompatWarning` | `true` | Shows the one-time notice when another leaf-particle mod is detected. |
| `seasonFallRate` | `1.0, 1.0, 3.0, 0.25` | Per-season spawn multiplier when Serene Seasons is installed, in the order spring, summer, autumn, winter. |

## License

Code is [LGPL-3.0](LICENSE), (matching the original). Particle textures are Cheaterpaul's original
assets and remain © their respective authors.

Original Forge mod by Cheaterpaul, based on the Fabric mod by Fourmisain, RandomMcSomethin, and BrekiTomasson.
