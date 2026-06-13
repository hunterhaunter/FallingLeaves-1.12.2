package com.xy.fallingleaves.config;

import net.minecraftforge.common.config.Config;

@Config(modid = "fallingleaves", type = Config.Type.INSTANCE, name = "fallingleaves")
public class FallingLeavesConfig {
    @Config.RangeInt(min = 1, max = 20)
    @Config.Comment("modifies the size of the leaves")
    public static int leafSize = 5;

    @Config.RangeInt(min = 100, max = Integer.MAX_VALUE)
    @Config.Comment({"modifies how long it takes for the leaves to disappear", "Values over 2000 are not recommended"})
    public static int leafLifespan = 200;

    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    @Config.Comment("modifies the amount of leaves spawning")
    public static int leafSpawnRate = 20;

    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    @Config.Comment("amount of leaves spawning from conifer trees")
    public static int coniferLeafSpawnRate = 2;

    @Config.Comment("whether player-placed blocks should also drop leaves")
    public static boolean dropFromPlayerPlacedBlocks = true;

    @Config.RangeInt(min = 1, max = 20)
    @Config.Comment("free space below a leaf block needed for leaves to spawn")
    public static int minimumFreeSpaceBelow = 1;

    @Config.Comment("disable wind effects")
    public static boolean disableWind = false;

    @Config.Comment("windless dimensions (by dimension name, e.g. 'the_nether', 'the_end')")
    public static String[] windlessDimension = new String[] { "the_nether", "the_end" };

    @Config.Comment("show a one-time in-game notice when another mod that also adds leaf particles is detected")
    public static boolean showCompatWarning = true;

    @Config.Comment({
            "per-season leaf spawn-rate multiplier, used only when Serene Seasons is installed",
            "order is [spring, summer, autumn, winter] - e.g. more leaves fall in autumn, fewer in winter"
    })
    public static double[] seasonFallRate = new double[] { 1.0, 1.0, 3.0, 0.25 };
}
