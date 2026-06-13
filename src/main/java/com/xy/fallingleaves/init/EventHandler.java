package com.xy.fallingleaves.init;

import com.xy.fallingleaves.config.FallingLeavesConfig;
import com.xy.fallingleaves.config.LeafSettingsEntry;
import com.xy.fallingleaves.util.LeafUtil;
import com.xy.fallingleaves.util.Wind;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

/**
 * Forge event handlers for client tick, world load, and punch-burst leaf effects.
 *
 * <p>Registered on {@link MinecraftForge#EVENT_BUS}. Replaces the 1.20.1 mixin
 * classes {@code ParticleManagerMixin}, {@code LeafTickMixin}, and
 * {@code MinecraftClientMixin} with pure event-driven logic.
 */
public class EventHandler {

    private final Random random = new Random();

    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.isGamePaused()) return;
        Wind.tick(mc.world);
        // Seasonal spawn multiplier (1.0 without Serene Seasons). Resolved once per tick,
        // not per sampled block, to keep the reflection lookup off the hot loop.
        this.seasonMultiplier = SeasonCompat.getSeasonFallMultiplier(mc.world);
        spawnLeavesAroundPlayer(mc);
    }

    private double seasonMultiplier = 1.0;

    /**
     * Replicates vanilla {@code WorldClient.doVoidFogParticles} position sampling:
     * 667 iterations × two radii (16, 32) around the player.
     * Applies per-block spawn-chance math from the reference {@code LeafTickMixin}.
     */
    private void spawnLeavesAroundPlayer(Minecraft mc) {
        int px = MathHelper.floor(mc.player.posX);
        int py = MathHelper.floor(mc.player.posY);
        int pz = MathHelper.floor(mc.player.posZ);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 667; i++) {
            sampleAndMaybeSpawn(mc, mutablePos, px, py, pz, 16);
            sampleAndMaybeSpawn(mc, mutablePos, px, py, pz, 32);
        }
    }

    private void sampleAndMaybeSpawn(Minecraft mc, BlockPos.MutableBlockPos pos, int px, int py, int pz, int radius) {
        int bx = px + random.nextInt(radius) - random.nextInt(radius);
        int by = py + random.nextInt(radius) - random.nextInt(radius);
        int bz = pz + random.nextInt(radius) - random.nextInt(radius);
        pos.setPos(bx, by, bz);

        IBlockState state = mc.world.getBlockState(pos);
        LeafSettingsEntry leafSettings = ClientMod.getLeafSetting(state.getBlock().getRegistryName());
        if (leafSettings == null && !LeafUtil.isLeafBlock(state, mc.world, pos)) return;

        // player-placed guard: skip if DECAYABLE property exists and is false
        if (!FallingLeavesConfig.dropFromPlayerPlacedBlocks
                && state.getPropertyKeys().contains(BlockLeaves.DECAYABLE)
                && !state.getValue(BlockLeaves.DECAYABLE)) return;

        // spawn-chance math ported exactly from LeafTickMixin.animateTick
        double spawnChance = 1.0;
        double modifier = FallingLeavesConfig.leafSpawnRate;
        if (leafSettings != null) {
            spawnChance = leafSettings.spawnRateFactor();
            if (leafSettings.considerAsConifer()) modifier = FallingLeavesConfig.coniferLeafSpawnRate;
        }
        modifier = modifier / 10.0 / 75.0;
        spawnChance *= modifier * this.seasonMultiplier;
        while (spawnChance > 0.0) {
            if (random.nextDouble() < spawnChance)
                LeafUtil.trySpawnLeafParticle(state, mc.world, pos, random, leafSettings);
            spawnChance -= 1.0;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) Wind.init();
    }

    @SubscribeEvent
    public void onAttackLeavesBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isRemote) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        LeafSettingsEntry leafSettings = ClientMod.getLeafSetting(state.getBlock().getRegistryName());
        if (leafSettings == null && !LeafUtil.isLeafBlock(state, event.getWorld(), event.getPos())) return;
        Random rng = new Random();
        for (int i = 0; i < 3; i++)
            if (rng.nextBoolean())
                LeafUtil.trySpawnLeafParticle(state, event.getWorld(), event.getPos(), rng, leafSettings);
    }
}
