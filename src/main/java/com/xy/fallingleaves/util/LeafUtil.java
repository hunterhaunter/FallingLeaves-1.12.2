package com.xy.fallingleaves.util;

import com.xy.fallingleaves.config.FallingLeavesConfig;
import com.xy.fallingleaves.config.LeafSettingsEntry;
import com.xy.fallingleaves.init.ClientMod;
import com.xy.fallingleaves.particle.FallingLeafParticle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class LeafUtil {

    private static final Random renderRandom = new Random();

    private LeafUtil() {
    }

    public static boolean isLeafBlock(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockLeaves) return true;
        try { if (block.isLeaves(state, world, pos)) return true; } catch (Exception ignored) {}
        try {
            ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
            if (!stack.isEmpty())
                for (int id : OreDictionary.getOreIDs(stack))
                    if ("treeLeaves".equals(OreDictionary.getOreName(id))) return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static void trySpawnLeafParticle(IBlockState state, World world, BlockPos pos,
                                             Random random, LeafSettingsEntry leafSettings) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() - random.nextDouble() / 3.0;
        double z = pos.getZ() + random.nextDouble();

        if (shouldSpawnParticle(world, pos, x, y, z)) {
            double[] color = getBlockTextureColor(state, world, pos);

            ResourceLocation type = (leafSettings != null)
                    ? leafSettings.leafType()
                    : state.getBlock().getRegistryName();
            ResourceLocation key;
            if (type != null && ClientMod.hasLeafType(type)) {
                key = type;
            } else {
                key = (leafSettings != null && leafSettings.considerAsConifer())
                        ? ClientMod.CONIFER
                        : ClientMod.DEFAULT;
            }

            TextureAtlasSprite sprite = ClientMod.getRandomSprite(key, random);
            if (sprite == null) return;

            FallingLeafParticle p = new FallingLeafParticle(
                    world, x, y, z, color[0], color[1], color[2], sprite);
            Minecraft.getMinecraft().effectRenderer.addEffect(p);
        }
    }

    public static boolean shouldSpawnParticle(World world, BlockPos pos,
                                                double x, double y, double z) {
        if (isLeafBlock(world.getBlockState(pos.down()), world, pos.down())) return false;

        double y2 = y - FallingLeavesConfig.minimumFreeSpaceBelow * 0.5;
        AxisAlignedBB box = new AxisAlignedBB(x - 0.1, y, z - 0.1, x + 0.1, y2, z + 0.1);
        return world.getCollisionBoxes(null, box).isEmpty();
    }

    public static double[] getBlockTextureColor(IBlockState state, World world, BlockPos pos) {
        IBakedModel model = Minecraft.getMinecraft()
                .getBlockRendererDispatcher().getModelForState(state);
        long seed = MathHelper.getPositionRandom(new Vec3i(pos.getX(), pos.getY(), pos.getZ()));
        List<BakedQuad> quads = model.getQuads(state, EnumFacing.DOWN, seed);
        TextureAtlasSprite sprite;
        boolean shouldColor;
        if (!quads.isEmpty()) {
            BakedQuad q = quads.get(0);
            sprite = q.getSprite();
            shouldColor = q.hasTintIndex();
        } else {
            sprite = model.getParticleTexture();
            shouldColor = true;
        }
        // Live biome/season tint. Serene Seasons (and similar) inject seasonal colour
        // straight into this BlockColors pipeline, so the particle follows the season for
        // free. Guarded so a misbehaving third-party leaf colour handler can never break
        // particle spawning — fall back to the plain texture colour.
        int blockColor = -1;
        if (shouldColor) {
            try {
                blockColor = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);
            } catch (Exception e) {
                blockColor = -1;
            }
        }
        return calculateLeafColor(sprite, blockColor);
    }

    public static double[] averageColor(TextureAtlasSprite sprite) {
        int[] pixels = sprite.getFrameTextureData(0)[0];
        double r = 0, g = 0, b = 0;
        int n = 0;
        for (int c : pixels) {
            int ca = (c >> 24) & 0xFF;
            if (ca == 0) continue;
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
            n++;
        }
        if (n == 0) return new double[]{1.0, 1.0, 1.0};
        return new double[]{ r / n / 255.0, g / n / 255.0, b / n / 255.0 };
    }

    public static double[] calculateLeafColor(TextureAtlasSprite sprite, int blockColor) {
        ResourceLocation key = new ResourceLocation(sprite.getIconName());
        double[] textureColor = TextureCache.INST
                .computeIfAbsent(key, k -> new TextureCache.Data(averageColor(sprite)))
                .getColor();
        if (blockColor != -1) {
            textureColor[0] = textureColor[0] * (((blockColor >> 16) & 0xFF) / 255.0);
            textureColor[1] = textureColor[1] * (((blockColor >> 8) & 0xFF) / 255.0);
            textureColor[2] = textureColor[2] * ((blockColor & 0xFF) / 255.0);
        }
        return textureColor;
    }
}
