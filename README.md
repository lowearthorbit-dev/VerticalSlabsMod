# Vertical Slabs Mod

This mod loads all existing vanilla slab blocks and registers a vertical slab version of them.

Multi-loader: builds for **NeoForge** and **Fabric** on Minecraft 26.2 from a shared code base.

## Project layout

- `common/` — loader-independent code (the vertical slab block, vanilla slab discovery) and shared resources plus generated JSON data
- `neoforge/` — NeoForge mod (registration, datagen, `neoforge.mods.toml`)
- `fabric/` — Fabric mod (registration, `fabric.mod.json`)
- `scripts/` — asset generation helper (blockstates/models) and the slab-to-texture map

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
./gradlew :neoforge:runData        # client data (lang)
./gradlew :neoforge:runDataServer  # server data (loot tables, recipes)
python3 scripts/generate_assets.py # blockstates + models
./gradlew build                     # rebuild jars with new assets
```

If a new slab is missing from `scripts/slab_textures.json`, update that file
(it maps each vanilla slab base name to the `side` texture used by vanilla's
own slab model).

## Run the game

```
./gradlew :neoforge:runClient
./gradlew :fabric:runClient
```

## Authors

- [@Brainterminator](https://github.com/Brainterminator)