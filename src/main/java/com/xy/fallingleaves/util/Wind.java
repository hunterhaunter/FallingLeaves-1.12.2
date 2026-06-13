package com.xy.fallingleaves.util;

import com.xy.fallingleaves.config.FallingLeavesConfig;
import com.xy.fallingleaves.math.SmoothNoise;
import com.xy.fallingleaves.math.TriangularDistribution;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wind {
    private static final Logger LOGGER = LogManager.getLogger("fallingleaves");
    public static float windX;
    public static float windZ;
    protected static final Random rng;
    protected static final float TAU = (float) Math.PI * 2;
    protected static SmoothNoise velocityNoise;
    protected static SmoothNoise directionTrendNoise;
    protected static SmoothNoise directionNoise;
    protected static boolean wasRaining;
    protected static boolean wasThundering;
    protected static State state;
    protected static State originalState;
    protected static int stateDuration;

    public static void debug() {
        state = State.values()[(state.ordinal() + 1) % State.values().length];
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
                new TextComponentString("set wind state to " + state));
    }

    public static void init() {
        LOGGER.debug("Wind.init");
        wasRaining = false;
        wasThundering = false;
        state = State.CALM;
        stateDuration = 0;
        windZ = 0.0f;
        windX = 0.0f;
        velocityNoise = new SmoothNoise(40, 0.0f, old -> Wind.state.velocityDistribution.sample());
        directionTrendNoise = new SmoothNoise(36000, rng.nextFloat() * ((float) Math.PI * 2), old -> rng.nextFloat() * ((float) Math.PI * 2));
        directionNoise = new SmoothNoise(200, 0.0f, old -> (2.0f * rng.nextFloat() - 1.0f) * ((float) Math.PI * 2) / 8.0f);
    }

    protected static void tickState(World world) {
        boolean weatherChanged;
        --stateDuration;
        String dim = world.provider.getDimensionType().getName();
        List<String> windless = Arrays.asList(FallingLeavesConfig.windlessDimension);
        if (FallingLeavesConfig.disableWind || windless.contains(dim)) {
            originalState = state;
            state = State.CALM;
            return;
        }
        if (originalState != null) {
            state = originalState;
            originalState = null;
        }
        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();
        boolean bl = weatherChanged = wasRaining != isRaining || wasThundering != isThundering;
        if (weatherChanged || stateDuration <= 0) {
            if (isThundering) {
                state = State.STORMY;
            } else {
                int index = rng.nextInt(2);
                state = State.values()[isRaining ? index + 1 : index];
            }
            stateDuration = 7200;
            LOGGER.debug("new wind state {}", state);
        }
        wasRaining = isRaining;
        wasThundering = isThundering;
    }

    public static void tick(World world) {
        Wind.tickState(world);
        velocityNoise.tick();
        directionTrendNoise.tick();
        directionNoise.tick();
        float strength = velocityNoise.getNoise();
        float direction = directionTrendNoise.getLerp() + directionNoise.getNoise();
        windX = strength * MathHelper.cos(direction);
        windZ = strength * MathHelper.sin(direction);
    }

    static {
        rng = new Random();
    }

    protected enum State {
        CALM(0.05f, 0.05f, 0.2f),
        WINDY(0.05f, 0.3f, 0.7f),
        STORMY(0.05f, 0.6f, 1.1f);

        public final TriangularDistribution velocityDistribution;

        State(float minSpeed, float likelySpeed, float maxSpeed) {
            this.velocityDistribution = new TriangularDistribution(minSpeed, maxSpeed, likelySpeed, rng);
        }
    }
}
