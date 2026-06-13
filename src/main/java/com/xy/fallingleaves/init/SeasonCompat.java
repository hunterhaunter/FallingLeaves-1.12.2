package com.xy.fallingleaves.init;

import com.xy.fallingleaves.config.FallingLeavesConfig;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

/**
 * Optional Serene Seasons integration (soft dependency, reflection only — the mod
 * runs fine without Serene Seasons). Reads the world's current season and turns it
 * into a leaf spawn-rate multiplier via {@link FallingLeavesConfig#seasonFallRate}.
 *
 * <p>Serene Seasons already handles the seasonal <em>colour</em> automatically (it
 * feeds the vanilla {@code BlockColors} pipeline our particle samples); this only
 * adds the seasonal <em>amount</em> of falling leaves.
 */
public final class SeasonCompat {

    private static boolean checked = false;
    private static boolean available = false;
    private static Method getSeasonState; // SeasonHelper.getSeasonState(World) -> ISeasonState
    private static Method getSeason;      // ISeasonState.getSeason() -> Season (enum)

    private SeasonCompat() {
    }

    private static void init() {
        checked = true;
        if (!Loader.isModLoaded("sereneseasons")) {
            return;
        }
        try {
            Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
            getSeasonState = helper.getMethod("getSeasonState", World.class);
            Class<?> stateClass = Class.forName("sereneseasons.api.season.ISeasonState");
            getSeason = stateClass.getMethod("getSeason");
            available = true;
        } catch (Throwable t) {
            available = false; // API shape changed or class missing — degrade gracefully
        }
    }

    /**
     * @return season ordinal (0 = spring, 1 = summer, 2 = autumn, 3 = winter), or -1 when
     *         Serene Seasons is absent / unavailable.
     */
    public static int getSeasonOrdinal(World world) {
        if (!checked) {
            init();
        }
        if (!available || world == null) {
            return -1;
        }
        try {
            Object state = getSeasonState.invoke(null, world);
            if (state == null) {
                return -1;
            }
            Object season = getSeason.invoke(state);
            if (season instanceof Enum) {
                return ((Enum<?>) season).ordinal();
            }
        } catch (Throwable t) {
            // ignore — fall through to no-modifier
        }
        return -1;
    }

    /**
     * Leaf spawn-rate multiplier for the world's current season, or {@code 1.0} when
     * Serene Seasons is not installed (so behaviour is unchanged without it).
     */
    public static double getSeasonFallMultiplier(World world) {
        int ordinal = getSeasonOrdinal(world);
        double[] rates = FallingLeavesConfig.seasonFallRate;
        if (ordinal < 0 || rates == null || ordinal >= rates.length) {
            return 1.0;
        }
        return rates[ordinal];
    }
}
