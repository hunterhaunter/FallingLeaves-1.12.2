package com.xy.fallingleaves.init;

import com.xy.fallingleaves.config.LeafSettingsEntry;
import com.xy.fallingleaves.data.LeafSettingLoader;
import com.xy.fallingleaves.data.LeafTypeLoader;
import com.xy.fallingleaves.util.TextureCache;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Client-side registry for leaf types and settings.
 *
 * <p>Loads leaf type texture lists and block settings at mod init,
 * registers particle sprites onto the vanilla block atlas during
 * {@link TextureStitchEvent.Pre}, and resolves them after stitching
 * in {@link TextureStitchEvent.Post}.
 */
public class ClientMod {
    public static final ResourceLocation DEFAULT = new ResourceLocation("fallingleaves", "default");
    public static final ResourceLocation CONIFER = new ResourceLocation("fallingleaves", "conifer");

    /** leafType → sprite LOCATIONS (with {@code particle/} prefix), loaded at init */
    private static Map<ResourceLocation, List<ResourceLocation>> leafTypeTextures;
    /** leafType → resolved sprites, filled after TextureStitchEvent.Post */
    private static final Map<ResourceLocation, TextureAtlasSprite[]> leafTypeSprites = new HashMap<>();
    /** block id → leaf settings entry */
    private static Map<ResourceLocation, LeafSettingsEntry> settings = new HashMap<>();

    /**
     * Load leaf type textures and settings from all mod containers.
     * Call during mod construction or pre-init, before resource reload.
     */
    public static void init() {
        leafTypeTextures = new LeafTypeLoader().load();
        settings = new LeafSettingLoader().load();
    }

    /**
     * Register this handler on {@link MinecraftForge#EVENT_BUS} to receive
     * {@link TextureStitchEvent} callbacks.
     */
    public static void registerEventBus() {
        MinecraftForge.EVENT_BUS.register(new ClientMod());
    }

    /**
     * Register our particle sprites on the block atlas before stitching.
     */
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        if (!"textures".equals(map.getBasePath())) return;

        if (leafTypeTextures != null) {
            for (List<ResourceLocation> spriteLocs : leafTypeTextures.values()) {
                for (ResourceLocation loc : spriteLocs) {
                    map.registerSprite(loc);
                }
            }
        }
    }

    /**
     * Resolve registered sprite locations into stitched TextureAtlasSprite arrays.
     */
    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        TextureMap map = event.getMap();
        if (!"textures".equals(map.getBasePath())) return;

        leafTypeSprites.clear();

        if (leafTypeTextures != null) {
            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : leafTypeTextures.entrySet()) {
                ResourceLocation leafType = entry.getKey();
                List<ResourceLocation> spriteLocs = entry.getValue();
                TextureAtlasSprite[] sprites = new TextureAtlasSprite[spriteLocs.size()];
                for (int i = 0; i < spriteLocs.size(); i++) {
                    sprites[i] = map.getAtlasSprite(spriteLocs.get(i).toString());
                }
                leafTypeSprites.put(leafType, sprites);
            }
        }

        TextureCache.INST.clear();
    }

    /**
     * Get the sprite array for a leaf type, falling back to DEFAULT.
     */
    public static TextureAtlasSprite[] getSpriteForLeafType(ResourceLocation leafType) {
        TextureAtlasSprite[] sprites = leafTypeSprites.get(leafType);
        if (sprites == null || sprites.length == 0) {
            sprites = leafTypeSprites.get(DEFAULT);
        }
        return sprites;
    }

    /**
     * Pick a random sprite from the leaf type's sprite set.
     */
    public static TextureAtlasSprite getRandomSprite(ResourceLocation leafType, Random rand) {
        TextureAtlasSprite[] sprites = getSpriteForLeafType(leafType);
        if (sprites == null || sprites.length == 0) {
            return null;
        }
        return sprites[rand.nextInt(sprites.length)];
    }

    /**
     * Whether a leaf type was registered with at least one resolved sprite.
     * Lets callers decide the CONIFER-vs-DEFAULT fallback themselves before
     * {@link #getSpriteForLeafType} silently falls back to DEFAULT.
     */
    public static boolean hasLeafType(ResourceLocation leafType) {
        TextureAtlasSprite[] sprites = leafTypeSprites.get(leafType);
        return sprites != null && sprites.length > 0;
    }

    /**
     * Get the leaf settings for a block, or null if none registered.
     */
    public static LeafSettingsEntry getLeafSetting(ResourceLocation location) {
        return settings.get(location);
    }
}
