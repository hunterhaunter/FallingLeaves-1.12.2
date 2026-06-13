package com.xy.fallingleaves.config;

import net.minecraft.util.ResourceLocation;

public final class LeafSettingsEntry {
    private final ResourceLocation id;
    private final double spawnRateFactor;
    private final ResourceLocation leafType;
    private final boolean considerAsConifer;

    public LeafSettingsEntry(ResourceLocation id, double spawnRateFactor, ResourceLocation leafType, boolean considerAsConifer) {
        this.id = id;
        this.spawnRateFactor = spawnRateFactor;
        this.leafType = leafType;
        this.considerAsConifer = considerAsConifer;
    }

    public LeafSettingsEntry(ResourceLocation id, double spawnRateFactor, boolean isConiferBlock) {
        this(id, spawnRateFactor, id, isConiferBlock);
    }

    public LeafSettingsEntry(ResourceLocation id, double spawnRateFactor) {
        this(id, spawnRateFactor, id, false);
    }

    public ResourceLocation id() {
        return id;
    }

    public double spawnRateFactor() {
        return spawnRateFactor;
    }

    public ResourceLocation leafType() {
        return leafType;
    }

    public boolean considerAsConifer() {
        return considerAsConifer;
    }
}
