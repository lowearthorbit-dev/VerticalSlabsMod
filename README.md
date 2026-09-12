# Vertical Slabs Mod

This mod loads all existing vanilla slab blocks and registers a vertical slab version of them.

Multi-loader: builds for **NeoForge** and **Fabric** on Minecraft 26.2 from a shared code base.

## Project layout

- `common/` — loader-independent code (the vertical slab block, vanilla slab discovery), shared resources, and shared data generators
- `neoforge/` — NeoForge mod (registration, `neoforge.mods.toml`)
- `fabric/` — Fabric mod (registration, `fabric.mod.json`)

## Building

Requires a Java 25+ JDK (Gradle toolchains auto-download JDK 25 for compilation).

```
./gradlew build                # builds all modules
./gradlew :neoforge:build      # NeoForge jar -> neoforge/build/libs
./gradlew :fabric:build        # Fabric jar   -> fabric/build/libs
```

## Regenerating data and assets

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

## Run the game

```
./gradlew :neoforge:runClient
./gradlew :fabric:runClient
```

## License

Distributed under the [MIT License](LICENSE.md).

## Authors

- [Low Earth Orbit](https://github.com/LowEarthOrbit)