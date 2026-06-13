# Falling Leaves

A 1.12.2 backport of [Falling Leaves](https://www.curseforge.com/minecraft/mc-mods/falling-leaves-forge) by Cheaterpaul.

A neat little particle effect for leaf blocks: leaves gently break off the underside of the canopy, drift down on the wind, tumble as they fall, settle on the ground, and fade away. Each particle is tinted to match the actual leaf texture and biome colour, so birch, oak, spruce, jungle and modded leaves all drop the right shade.

Requires Forge 14.23.5.2860+. 100% client-side.

## Features

- **Wind simulation** — smooth-noise wind with CALM / WINDY / STORMY states that follow the weather. Leaves sway while airborne, then land and stick.
- **Texture-accurate colour** — every particle samples the leaf block's own texture and biome tint, so the falling leaf matches the tree it came from.
- **Land, stick, and fade** — leaves settle on the first surface they hit, rest for their lifespan, then cleanly fade out.
- **Punch a leaf, lose a leaf** — left-clicking a leaf block knocks a small burst of leaves loose.
- **Conifer support** — spruce and other conifers use dedicated needle particles with their own spawn rate.
- **Rain-aware** — leaves fall a little faster under an open, rainy sky.
- **Water-aware** — if a leave lands on water it glides slightly before stopping.
- **Data-driven** — per-block settings (spawn rate, conifer flag, custom leaf textures) load from `assets/<modid>/fallingleaves/` in any mod's jar, so other mods can ship their own leaf configs.
- **Configurable** — leaf size, lifespan, spawn rate, conifer spawn rate, free space below, player-placed leaves, wind toggle, and windless dimensions. Edit `config/fallingleaves.cfg` or use the in-game config screen from the mod list.

## Mod Compatibility

Built for maximum compatibility. There are **no mixins and no coremod** — the whole mod runs
through Forge events (`ClientTickEvent`, `TextureStitchEvent`, `PlayerInteractEvent`,
`WorldEvent.Load`), so nothing vanilla is patched or replaced.

Leaf detection uses three layers — `instanceof BlockLeaves`, Forge's `isLeaves()` virtual
method, and the `treeLeaves` OreDictionary entry — so modded leaves are picked up without any
patches.

### Tested mods

- **Dynamic Trees** — works out of the box. Its `BlockDynamicLeaves extends BlockLeaves`, so
  leaves fall from dynamic trees with no extra config. (Dynamic Trees has no particle system of
  its own, so there's nothing to double up.)
- **Serene Seasons** — leaves recolour with the season automatically. The particle samples the
  block's colour live each time it spawns, and Serene Seasons feeds its seasonal tint into that
  same `BlockColors` pipeline, so a leaf that breaks off in autumn falls in autumn colours.
- **Weather2 / Weather2 Remastered** — these add their own wind-blown foliage particles, so you
  may see leaves doubled up. Falling Leaves shows a **one-time in-game notice** when it detects
  them (see below); pick whichever leaf effect you prefer and turn the other off in its config.

### Conflict notice

When a mod that also spawns falling-leaf or foliage particles is present, Falling Leaves prints
a single chat notice the first time you load a world (Quark-style — shown once, ever). Turn it
off with `showCompatWarning=false` in `config/fallingleaves.cfg`. Colour mods like Serene
Seasons are *not* flagged — they recolour leaves rather than adding particles, so they're fully
compatible.

## License

Code is [LGPL-3.0](LICENSE), matching the original. Particle textures are Cheaterpaul's
original assets and remain © their respective authors.

Original Forge mod by Cheaterpaul, based on the Fabric mod by Fourmisain, RandomMcSomethin, and BrekiTomasson.
