package com.xy.fallingleaves.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.xy.fallingleaves.config.LeafSettingsEntry;
import com.xy.fallingleaves.init.ClientMod;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Loads leaf settings JSONs from all mod containers.
 * Walks every mod's assets via {@link CraftingHelper#findFiles},
 * parses {@code fallingleaves/settings/<name>.json}, and returns
 * a map of block id → settings entry.
 */
public class LeafSettingLoader {
    private static final Logger LOGGER = LogManager.getLogger("fallingleaves");

    /**
     * Scan all active mod containers for leaf settings JSONs.
     *
     * @return map from block ResourceLocation to LeafSettingsEntry
     */
    public Map<ResourceLocation, LeafSettingsEntry> load() {
        Map<ResourceLocation, LeafSettingsEntry> settings = new HashMap<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            CraftingHelper.findFiles(mod, "assets", null, (root, file) -> {
                if (!"json".equalsIgnoreCase(FilenameUtils.getExtension(file.toString()))) return true;

                String rel = root.relativize(file).toString().replace('\\', '/');
                String[] parts = rel.split("/");
                // Expected: [ns, "fallingleaves", "settings", ...name...]
                if (parts.length < 4 || !"fallingleaves".equals(parts[1])) return true;

                String ns = parts[0];
                String kind = parts[2];
                if (!"settings".equals(kind)) return true;

                String name = FilenameUtils.removeExtension(
                        rel.substring(rel.indexOf(kind) + kind.length() + 1));
                ResourceLocation key = new ResourceLocation(ns, name);

                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonObject object = new com.google.gson.Gson().fromJson(reader, JsonObject.class);

                    double spawnRate = object.has("spawnrate")
                            ? object.get("spawnrate").getAsDouble()
                            : 1.0;

                    ResourceLocation leafType = object.has("leaf_type")
                            ? new ResourceLocation(object.get("leaf_type").getAsString())
                            : key;

                    boolean considerAsConifer = false;
                    if (object.has("isConifer") && object.get("isConifer").getAsBoolean()) {
                        leafType = ClientMod.CONIFER;
                        considerAsConifer = true;
                    }
                    considerAsConifer = object.has("consider_as_conifer")
                            ? object.get("consider_as_conifer").getAsBoolean()
                            : considerAsConifer;

                    settings.put(key, new LeafSettingsEntry(key, spawnRate, leafType, considerAsConifer));
                } catch (JsonParseException | IllegalArgumentException e) {
                    LOGGER.error("Parsing error loading leaf settings {}: {}", key, e.getMessage());
                } catch (Exception e) {
                    LOGGER.error("Error reading leaf settings {}: {}", key, e.getMessage());
                }
                return true;
            }, true, true);
        }

        return settings;
    }
}
