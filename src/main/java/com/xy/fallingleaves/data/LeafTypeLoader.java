package com.xy.fallingleaves.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads leaf type texture lists from all mod containers.
 * Walks every mod's assets via {@link CraftingHelper#findFiles},
 * parses {@code fallingingleaves/leaftypes/<name>.json}, and returns
 * a map of leaf type key → sprite locations (with {@code particle/} prefix
 * for block-atlas stitching).
 */
public class LeafTypeLoader {
    private static final Logger LOGGER = LogManager.getLogger("fallingleaves");

    /**
     * Scan all active mod containers for leaf type JSONs.
     *
     * @return map from leaf-type ResourceLocation to list of block-atlas sprite locations
     */
    public Map<ResourceLocation, List<ResourceLocation>> load() {
        Map<ResourceLocation, List<ResourceLocation>> leafTypes = new HashMap<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            CraftingHelper.findFiles(mod, "assets", null, (root, file) -> {
                if (!"json".equalsIgnoreCase(FilenameUtils.getExtension(file.toString()))) return true;

                String rel = root.relativize(file).toString().replace('\\', '/');
                String[] parts = rel.split("/");
                // Expected: [ns, "fallingleaves", "leaftypes", ...name...]
                if (parts.length < 4 || !"fallingleaves".equals(parts[1])) return true;

                String ns = parts[0];
                String kind = parts[2];
                if (!"leaftypes".equals(kind)) return true;

                String name = FilenameUtils.removeExtension(
                        rel.substring(rel.indexOf(kind) + kind.length() + 1));
                ResourceLocation key = new ResourceLocation(ns, name);

                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonObject object = new com.google.gson.Gson().fromJson(reader, JsonObject.class);
                    JsonArray textures = object.getAsJsonArray("textures");
                    List<ResourceLocation> spriteLocs = new ArrayList<>(textures.size());

                    for (int i = 0; i < textures.size(); i++) {
                        String texId = textures.get(i).getAsString();
                        ResourceLocation texRl = new ResourceLocation(texId);
                        // PNG lives at textures/particle/<path>.png on the block atlas
                        spriteLocs.add(new ResourceLocation(texRl.getNamespace(),
                                "particle/" + texRl.getPath()));
                    }
                    leafTypes.put(key, spriteLocs);
                } catch (Exception e) {
                    LOGGER.error("Error reading leaf type {}: {}", key, e.getMessage());
                }
                return true;
            }, true, true);
        }

        return leafTypes;
    }
}
