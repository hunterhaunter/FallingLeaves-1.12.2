package com.xy.fallingleaves.particle;

import com.xy.fallingleaves.config.FallingLeavesConfig;
import com.xy.fallingleaves.util.Wind;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FallingLeafParticle extends Particle {

    private static final float TAU = (float) Math.PI * 2;
    private static final int FADE_DURATION = 16;

    private final float windCoefficient;
    private final float maxRotateSpeed;
    private final int maxRotateTime;
    private int rotateTime = 0;
    // Fabric-original mechanism the 1.20 decompile dropped: once a leaf lands it latches
    // here and stays frozen, instead of relying on onGround (which move(0,0,0) clears the
    // next tick → gravity re-applies → jitter).
    private boolean stuckInGround = false;

    public FallingLeafParticle(World world, double x, double y, double z,
                                double r, double g, double b,
                                TextureAtlasSprite sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.setParticleTexture(sprite);
        this.particleGravity = 0.08f + this.rand.nextFloat() * 0.04f;
        // Authentic Fabric wind coupling [0.6, 1.0). Leaves respond to wind while airborne
        // but land + stick quickly (see stuckInGround), so they don't fly off.
        this.windCoefficient = 0.6f + this.rand.nextFloat() * 0.4f;
        this.motionX = this.motionY = this.motionZ = 0.0;
        this.canCollide = true;
        this.particleMaxAge = FallingLeavesConfig.leafLifespan;
        this.particleRed = (float) r;
        this.particleGreen = (float) g;
        this.particleBlue = (float) b;
        this.maxRotateTime = (3 + this.rand.nextInt(5)) * 20;
        this.maxRotateSpeed = (this.rand.nextBoolean() ? -1f : 1f)
                * (0.1f + 2.4f * this.rand.nextFloat()) * TAU / 20.0f;
        this.particleAngle = this.prevParticleAngle = this.rand.nextFloat() * TAU;
        // Fabric quadSize = leafSize/50 used directly as render half-size. 1.12.2 render
        // does 0.1F * particleScale, so particleScale = leafSize/5 reproduces it exactly.
        this.particleScale = FallingLeavesConfig.leafSize / 5.0f;
        // Fabric varies size: 50% of leaves are 1.5x larger.
        if (this.rand.nextBoolean()) {
            this.particleScale *= 1.5f;
        }
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevParticleAngle = this.particleAngle;
        this.particleAge++;

        // Fade over the last FADE_DURATION ticks (runs even while stuck → stick, then fade).
        if (this.particleAge >= this.particleMaxAge + 1 - FADE_DURATION) {
            this.particleAlpha -= 0.0625f;
        }

        if (this.particleAge >= this.particleMaxAge) {
            this.setExpired();
            return;
        }

        // Landed and latched: rest in place (no motion, no spin) until it fades out.
        if (this.stuckInGround) {
            return;
        }

        if (this.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ))
                .getMaterial() == Material.WATER) {
            this.motionY = 0.0;
            this.rotateTime = 0;
            this.motionX *= 0.95;
            this.motionZ *= 0.95;
        } else {
            // Gravity, with Fabric's rain bonus when the leaf is exposed to a raining sky.
            float rainBonus = (this.world.isRaining()
                    && this.world.canSeeSky(new BlockPos(this.posX, this.posY, this.posZ)))
                    ? 0.04f : 0.0f;
            this.motionY -= 0.04 * (double) (this.particleGravity + rainBonus);
            if (!this.onGround) {
                this.rotateTime = Math.min(this.rotateTime + 1, this.maxRotateTime);
                this.particleAngle += (float) this.rotateTime / (float) this.maxRotateTime * this.maxRotateSpeed;
            } else {
                this.rotateTime = 0;
            }
            // Asymptotic wind sway (Fabric model — approaches wind velocity, never reaches it).
            this.motionX += ((double) Wind.windX - this.motionX) * (double) this.windCoefficient / 60.0;
            this.motionZ += ((double) Wind.windZ - this.motionZ) * (double) this.windCoefficient / 60.0;
        }

        this.move(this.motionX, this.motionY, this.motionZ);

        // Latch on first ground contact so the leaf settles and stays (Fabric stuckInGround).
        if (this.onGround) {
            this.stuckInGround = true;
            this.motionX = this.motionY = this.motionZ = 0.0;
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
