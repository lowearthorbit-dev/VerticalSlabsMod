# Vertical Slabs Mod

Ever wanted to build walls without full blocks? Vertical Slabs adds a vertical variant of **every vanilla slab** — discovered automatically, so any slab added by Minecraft gets its own vertical counterpart.

Place them on the edge of a block to orient them freely, craft them the same way as regular slabs, and use them for trim, paneling, and detailed walls.

**Downloads:** [Modrinth](https://modrinth.com/project/vertical-slabs-mod) — available for **NeoForge** and **Fabric** on Minecraft 26.2.

## Features

- Vertical slab versions of all vanilla slab blocks
- Generated automatically from each slab's own assets — always in sync with Minecraft
- Works on both NeoForge and Fabric from one shared code base

## Installation

1. Download the latest jar from [Modrinth](https://modrinth.com/project/vertical-slabs-mod) for your loader (NeoForge or Fabric).
2. Drop it into your `mods` folder — that's it.

## For developers

### Project layout

- `common/` — loader-independent code (the vertical slab block, vanilla slab discovery), shared resources, and shared data generators
- `neoforge/` — NeoForge mod (registration, `neoforge.mods.toml`)
- `fabric/` — Fabric mod (registration, `fabric.mod.json`)

### Building

Requires a Java 25+ JDK (Gradle toolchains auto-download JDK 25 for compilation).

```
./gradlew build                # builds all modules
./gradlew :neoforge:build      # NeoForge jar -> neoforge/build/libs
./gradlew :fabric:build        # Fabric jar   -> fabric/build/libs
```

### Regenerating data and assets

When Minecraft adds new slabs, regenerate everything:

```
./gradlew :neoforge:runData        # client data (lang, blockstates, models, item definitions)
./gradlew :neoforge:runDataServer  # server data (loot tables, recipes)
./gradlew build                    # rebuild jars with new data
```

Alternatively, Fabric can run the same generation:

```
./gradlew :fabric:runDatagen
```

Blockstates, models and item definitions are generated natively by the shared
`VerticalSlabAssetProvider` (in `common/src/main/java/.../datagen/`), which
derives the textures from each vanilla slab's own assets — no manual texture
map required.

### Run the game

```
./gradlew :neoforge:runClient
./gradlew :fabric:runClient
```

## License

Distributed under the [MIT License](LICENSE.md).

## Authors

- [Low Earth Orbit](https://github.com/lowearthorbit-dev)