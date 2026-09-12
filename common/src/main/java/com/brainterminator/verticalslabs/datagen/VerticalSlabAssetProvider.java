package com.brainterminator.verticalslabs.datagen;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates blockstates, block models and item definitions for every
 * registered vertical slab. A pure vanilla {@link DataProvider} so it runs
 * identically on NeoForge and Fabric datagen.
 *
 * <p>Textures are derived automatically from the vanilla slab's own assets:
 * the blockstate's {@code type=bottom} variant points at the slab model, whose
 * textures are read from the Minecraft jar on the classpath. New vanilla slabs
 * are therefore picked up without a manual texture map.
 */
public class VerticalSlabAssetProvider implements DataProvider {
    private final PackOutput output;

    public VerticalSlabAssetProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        PackOutput.PathProvider blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        PackOutput.PathProvider models = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        PackOutput.PathProvider items = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Block slab : VerticalSlabsCommon.allSlabs()) {
            Block vanilla = VanillaSlabs.getVanillaOf(slab);
            if (vanilla == null) {
                continue;
            }
            String verticalName = BuiltInRegistries.BLOCK.getKey(slab).getPath();
            String vanillaName = BuiltInRegistries.BLOCK.getKey(vanilla).getPath();
            Identifier side = findVanillaSideTexture(vanillaName);
            if (side == null) {
                continue;
            }
            Identifier modelId = Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, "block/" + verticalName);
            futures.add(DataProvider.saveStable(cache, verticalModel(side),
                    models.json(modelId)));
            futures.add(DataProvider.saveStable(cache, verticalBlockState(modelId),
                    blockStates.json(Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, verticalName))));
            futures.add(DataProvider.saveStable(cache, itemDefinition(modelId),
                    items.json(Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, verticalName))));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Finds the texture the vanilla slab model uses: resolves the slab
     * blockstate's {@code type=bottom} variant to its model and reads the
     * {@code side} (or {@code all}) texture slot from that model.
     */
    private static Identifier findVanillaSideTexture(String vanillaSlabName) {
        JsonObject blockState = readVanillaJson("assets/minecraft/blockstates/" + vanillaSlabName + ".json");
        if (blockState == null) {
            return null;
        }
        JsonElement bottomVariant = blockState.deepCopy().getAsJsonObject("variants").get("type=bottom");
        if (bottomVariant == null) {
            return null;
        }
        String modelName = bottomVariant.getAsJsonObject().get("model").getAsString();
        JsonObject model = readVanillaJson("assets/" + Identifier.parse(modelName).getNamespace()
                + "/models/" + Identifier.parse(modelName).getPath() + ".json");
        if (model == null || !model.has("textures")) {
            return null;
        }
        JsonObject textures = model.getAsJsonObject("textures");
        for (String key : new String[]{"side", "all"}) {
            if (textures.has(key)) {
                return Identifier.parse(textures.get(key).getAsString());
            }
        }
        return null;
    }

    private static JsonObject readVanillaJson(String path) {
        try (InputStream stream = VerticalSlabAssetProvider.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    /** A thin vertical plate against the north face, rotated per facing by the blockstate. */
    private static JsonObject verticalModel(Identifier side) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", side.toString());
        textures.addProperty("side", side.toString());
        model.add("textures", textures);
        JsonObject element = new JsonObject();
        element.add("from", arrayOf(0, 0, 0));
        element.add("to", arrayOf(16, 16, 8));
        JsonObject faces = new JsonObject();
        faces.add("down", face("[0,8,16,16]", "#side", null));
        faces.add("up", face("[0,0,16,8]", "#side", null));
        faces.add("north", face("[0,0,16,16]", "#side", "north"));
        faces.add("south", face("[0,0,16,16]", "#side", null));
        faces.add("west", face("[8,0,16,16]", "#side", null));
        faces.add("east", face("[0,0,8,16]", "#side", null));
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        model.add("elements", elements);
        return model;
    }

    private static JsonObject face(String uv, String texture, String cullface) {
        JsonObject face = new JsonObject();
        face.add("uv", JsonParser.parseString(uv));
        face.addProperty("texture", texture);
        if (cullface != null) {
            face.addProperty("cullface", cullface);
        }
        return face;
    }

    private static JsonArray arrayOf(int... values) {
        JsonArray array = new JsonArray();
        for (int v : values) {
            array.add(v);
        }
        return array;
    }

    private static final Map<Direction, Integer> Y_ROTATIONS = new EnumMap<>(Direction.class);

    static {
        Y_ROTATIONS.put(Direction.NORTH, 0);
        Y_ROTATIONS.put(Direction.EAST, 90);
        Y_ROTATIONS.put(Direction.SOUTH, 180);
        Y_ROTATIONS.put(Direction.WEST, 270);
    }

    private static JsonObject verticalBlockState(Identifier model) {
        JsonObject variants = new JsonObject();
        for (Direction facing : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            for (Half half : Half.values()) {
                for (StairsShape shape : StairsShape.values()) {
                    for (boolean waterlogged : new boolean[]{false, true}) {
                        String key = "facing=" + facing.getSerializedName()
                                + ",half=" + half.getSerializedName()
                                + ",shape=" + shape.getSerializedName()
                                + ",waterlogged=" + waterlogged;
                        JsonObject variant = new JsonObject();
                        variant.addProperty("model", model.toString());
                        variant.addProperty("y", Y_ROTATIONS.get(facing));
                        variant.addProperty("uvlock", false);
                        variants.add(key, variant);
                    }
                }
            }
        }
        JsonObject state = new JsonObject();
        state.add("variants", variants);
        return state;
    }

    private static JsonObject itemDefinition(Identifier model) {
        JsonObject modelRef = new JsonObject();
        modelRef.addProperty("type", "minecraft:model");
        modelRef.addProperty("model", model.toString());
        JsonObject definition = new JsonObject();
        definition.add("model", modelRef);
        return definition;
    }

    @Override
    public String getName() {
        return "Vertical Slabs Assets";
    }
}