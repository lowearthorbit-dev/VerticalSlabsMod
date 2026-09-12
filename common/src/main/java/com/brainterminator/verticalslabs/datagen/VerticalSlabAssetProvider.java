package com.brainterminator.verticalslabs.datagen;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.LinkedHashMap;
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
 *
 * <p>The vertical slab behaves like a stair: besides the straight model it
 * gets inner and outer corner models, and the blockstate dispatches on
 * facing, half and stair shape with stair-style rotations. Item definitions
 * point at the straight model.
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
            Identifier straight = Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, "block/" + verticalName);
            Identifier inner = Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, "block/" + verticalName + "_inner");
            Identifier outer = Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, "block/" + verticalName + "_outer");

            futures.add(DataProvider.saveStable(cache, straightModel(side), models.json(straight)));
            futures.add(DataProvider.saveStable(cache, innerModel(side), models.json(inner)));
            futures.add(DataProvider.saveStable(cache, outerModel(side), models.json(outer)));
            futures.add(DataProvider.saveStable(cache, blockstate(straight, inner, outer),
                    blockStates.json(Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, verticalName))));
            futures.add(DataProvider.saveStable(cache, itemDefinition(verticalName),
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
        JsonElement bottomVariant = blockState.getAsJsonObject("variants").get("type=bottom");
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

    // ------------------------------------------------------------------
    // Blockstate: 20 (facing, half, shape) variants with stair-style
    // rotations, mirroring vanilla stairs blockstates.
    // ------------------------------------------------------------------

    /** Bottom-half y rotation per facing and shape. */
    private static final Map<String, Map<String, Integer>> BOTTOM_Y = new LinkedHashMap<>();

    static {
        BOTTOM_Y.put("east", Map.of("straight", 0, "inner_left", 270, "inner_right", 0, "outer_left", 270, "outer_right", 0));
        BOTTOM_Y.put("south", Map.of("straight", 90, "inner_left", 0, "inner_right", 90, "outer_left", 0, "outer_right", 90));
        BOTTOM_Y.put("west", Map.of("straight", 180, "inner_left", 90, "inner_right", 180, "outer_left", 90, "outer_right", 180));
        BOTTOM_Y.put("north", Map.of("straight", 270, "inner_left", 180, "inner_right", 270, "outer_left", 180, "outer_right", 270));
    }

    private static final int TOP_X = 180;
    private static final List<String> SHAPES = List.of("straight", "inner_left", "inner_right", "outer_left", "outer_right");

    private static JsonObject blockstate(Identifier straight, Identifier inner, Identifier outer) {
        JsonObject variants = new JsonObject();
        for (Map.Entry<String, Map<String, Integer>> facingEntry : BOTTOM_Y.entrySet()) {
            String facing = facingEntry.getKey();
            for (String shape : SHAPES) {
                Identifier model = shape.equals("straight") ? straight
                        : shape.startsWith("inner") ? inner : outer;
                int bottomY = facingEntry.getValue().get(shape);
                variants.add("facing=" + facing + ",half=bottom,shape=" + shape,
                        variant(model, null, bottomY));

                int topY = shape.equals("straight") ? bottomY : (bottomY + 90) % 360;
                variants.add("facing=" + facing + ",half=top,shape=" + shape,
                        variant(model, TOP_X, topY));
            }
        }
        JsonObject state = new JsonObject();
        state.add("variants", variants);
        return state;
    }

    private static JsonObject variant(Identifier model, Integer x, int y) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model.toString());
        if (x != null) {
            variant.addProperty("x", x);
        }
        if (y != 0) {
            variant.addProperty("y", y);
        }
        variant.addProperty("uvlock", true);
        return variant;
    }

    // ------------------------------------------------------------------
    // Models: straight, inner and outer corner templates.
    // ------------------------------------------------------------------

    private static JsonObject straightModel(Identifier texture) {
        JsonObject model = baseModel(texture);
        model.add("elements", elements(JsonObjectBuilder.element()
                .from(8, 0, 0).to(16, 16, 16)
                .face("north", face(4, 0, 12, 16, "#0"))
                .face("east", face(0, 0, 16, 16, "#0"))
                .face("south", face(4, 0, 12, 16, "#0"))
                .face("west", face(0, 0, 16, 16, "#0"))
                .face("up", rotatedFace(0, 4, 16, 12, 90, "#0"))
                .face("down", rotatedFace(0, 4, 16, 12, 270, "#0"))
                .build()));
        model.add("display", DisplayBuilder.display()
                .entry("thirdperson_righthand", null, null, scale(0.4))
                .entry("thirdperson_lefthand", null, null, scale(0.4))
                .entry("firstperson_righthand", null, null, scale(0.4))
                .entry("firstperson_lefthand", null, null, scale(0.4))
                .entry("ground", null, translation(0, 3, 0), scale(0.3))
                .entry("gui", rotation(20, 39, 0), translation(-2, -0.5, 0), scale(0.7))
                .entry("fixed", null, null, scale(0.4))
                .build());
        return model;
    }

    private static JsonObject innerModel(Identifier texture) {
        JsonObject model = baseModel(texture);
        JsonArray elements = new JsonArray();
        elements.add(JsonObjectBuilder.element()
                .from(0, 0, 8).to(16, 16, 16)
                .rotation(0, "y", 14, 0, 16)
                .face("north", face(0, 0, 16, 16, "#0"))
                .face("east", face(0, 0, 8, 16, "#0"))
                .face("south", face(0, 0, 16, 16, "#0"))
                .face("west", face(4, 0, 12, 16, "#0"))
                .face("up", rotatedFace(0, 0, 16, 8, 180, "#0"))
                .face("down", rotatedFace(0, 8, 16, 16, 180, "#0"))
                .build());
        elements.add(JsonObjectBuilder.element()
                .from(8, 0, 0).to(16, 16, 8)
                .rotation(0, "y", 14, 0, 16)
                .face("north", face(4, 0, 12, 16, "#0"))
                .face("east", face(8, 0, 16, 16, "#0"))
                .face("south", face(0, 0, 8, 16, "#missing"))
                .face("west", face(0, 0, 8, 16, "#0"))
                .face("up", rotatedFace(0, 0, 8, 8, 90, "#0"))
                .face("down", rotatedFace(0, 8, 8, 16, 270, "#0"))
                .build());
        model.add("elements", elements);
        model.add("display", DisplayBuilder.display()
                .entry("thirdperson_righthand", null, null, scale(0.3))
                .entry("thirdperson_lefthand", null, null, scale(0.3))
                .entry("firstperson_righthand", null, null, scale(0.5))
                .entry("firstperson_lefthand", null, null, scale(0.5))
                .entry("ground", null, translation(0, -1, 0), scale(0.3))
                .entry("gui", rotation(15, -34, -2), translation(-1.5, -0.5, 0), scale(0.7))
                .entry("fixed", rotation(0, -90, 0), null, scale(0.81))
                .build());
        return model;
    }

    private static JsonObject outerModel(Identifier texture) {
        JsonObject model = baseModel(texture);
        model.add("elements", elements(JsonObjectBuilder.element()
                .from(8, 0, 8).to(16, 16, 16)
                .face("north", face(0, 0, 8, 16, "#0"))
                .face("east", face(8, 0, 16, 16, "#0"))
                .face("south", face(0, 0, 8, 16, "#0"))
                .face("west", face(8, 0, 16, 16, "#0"))
                .face("up", rotatedFace(8, 8, 16, 16, 180, "#0"))
                .face("down", rotatedFace(8, 0, 16, 8, 180, "#0"))
                .build()));
        model.add("display", DisplayBuilder.display()
                .entry("thirdperson_righthand", null, null, scale(0.3))
                .entry("thirdperson_lefthand", null, null, scale(0.3))
                .entry("firstperson_righthand", null, null, scale(0.5))
                .entry("firstperson_lefthand", null, null, scale(0.5))
                .entry("ground", null, translation(0, -1, 0), scale(0.3))
                .entry("gui", rotation(15, -34, -2), translation(-1.5, -0.5, 0), scale(0.7))
                .entry("fixed", rotation(0, -90, 0), null, scale(0.81))
                .build());
        return model;
    }

    private static JsonObject baseModel(Identifier texture) {
        JsonObject model = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("0", texture.toString());
        textures.addProperty("particle", texture.toString());
        model.add("textures", textures);
        return model;
    }

    // ------------------------------------------------------------------
    // Item definition (MC 1.21.4+): drives inventory/handheld rendering.
    // ------------------------------------------------------------------

    private static JsonObject itemDefinition(String verticalName) {
        JsonObject modelRef = new JsonObject();
        modelRef.addProperty("type", "minecraft:model");
        modelRef.addProperty("model", Identifier.fromNamespaceAndPath(VerticalSlabsCommon.MODID, "block/" + verticalName).toString());
        JsonObject definition = new JsonObject();
        definition.add("model", modelRef);
        return definition;
    }

    // ------------------------------------------------------------------
    // Small JSON building helpers.
    // ------------------------------------------------------------------

    private static JsonArray elements(JsonObject... elementObjects) {
        JsonArray array = new JsonArray();
        for (JsonObject element : elementObjects) {
            array.add(element);
        }
        return array;
    }

    private static JsonArray ints(int... values) {
        JsonArray array = new JsonArray();
        for (int v : values) {
            array.add(v);
        }
        return array;
    }

    private static JsonObject face(int u1, int v1, int u2, int v2, String texture) {
        JsonObject face = new JsonObject();
        face.add("uv", ints(u1, v1, u2, v2));
        face.addProperty("texture", texture);
        return face;
    }

    private static JsonObject rotatedFace(int u1, int v1, int u2, int v2, int rotation, String texture) {
        JsonObject face = face(u1, v1, u2, v2, texture);
        face.addProperty("rotation", rotation);
        return face;
    }

    private static JsonArray numbers(double... values) {
        JsonArray array = new JsonArray();
        for (double v : values) {
            array.add(v);
        }
        return array;
    }

    private static JsonArray scale(double uniform) {
        return numbers(uniform, uniform, uniform);
    }

    private static JsonArray translation(double x, double y, double z) {
        return numbers(x, y, z);
    }

    private static JsonArray rotation(double x, double y, double z) {
        return numbers(x, y, z);
    }

    /** Fluent builder for a model's display transforms. */
    private static final class DisplayBuilder {
        private final JsonObject display = new JsonObject();

        private static DisplayBuilder display() {
            return new DisplayBuilder();
        }

        DisplayBuilder entry(String name, JsonArray rotation, JsonArray translation, JsonArray scale) {
            JsonObject transform = new JsonObject();
            if (rotation != null) {
                transform.add("rotation", rotation);
            }
            if (translation != null) {
                transform.add("translation", translation);
            }
            if (scale != null) {
                transform.add("scale", scale);
            }
            display.add(name, transform);
            return this;
        }

        JsonObject build() {
            return display;
        }
    }

    /** Fluent builder for a single model element. */
    private static final class JsonObjectBuilder {
        private final JsonObject element = new JsonObject();

        private static JsonObjectBuilder element() {
            return new JsonObjectBuilder();
        }

        JsonObjectBuilder from(int x, int y, int z) {
            element.add("from", ints(x, y, z));
            return this;
        }

        JsonObjectBuilder to(int x, int y, int z) {
            element.add("to", ints(x, y, z));
            return this;
        }

        JsonObjectBuilder rotation(int angle, String axis, int ox, int oy, int oz) {
            JsonObject rotation = new JsonObject();
            rotation.addProperty("angle", angle);
            rotation.addProperty("axis", axis);
            rotation.add("origin", ints(ox, oy, oz));
            element.add("rotation", rotation);
            return this;
        }

        JsonObjectBuilder face(String direction, JsonObject face) {
            JsonObject faces = element.has("faces") ? element.getAsJsonObject("faces") : new JsonObject();
            faces.add(direction, face);
            element.add("faces", faces);
            return this;
        }

        JsonObject build() {
            return element;
        }
    }

    @Override
    public String getName() {
        return "Vertical Slabs Assets";
    }
}