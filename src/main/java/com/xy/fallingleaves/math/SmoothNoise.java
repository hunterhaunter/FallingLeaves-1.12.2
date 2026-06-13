package com.xy.fallingleaves.math;

import javax.annotation.Nonnull;

public class SmoothNoise {
    protected final int tickInterval;
    protected final FloatFunction nextNoise;
    protected float leftNoise;
    protected float rightNoise;
    protected int ticks = 0;
    protected float t;

    public SmoothNoise(int tickInterval, float initial, @Nonnull FloatFunction nextNoise) {
        if (tickInterval < 1) {
            throw new IllegalArgumentException(String.format("tickInterval %d < 1", tickInterval));
        }
        this.tickInterval = tickInterval;
        this.nextNoise = nextNoise;
        this.leftNoise = initial;
        this.rightNoise = nextNoise.apply(this.leftNoise);
    }

    public static float smoothstep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    public void tick() {
        ++this.ticks;
        if (this.ticks == this.tickInterval) {
            this.ticks = 0;
            this.leftNoise = this.rightNoise;
            this.rightNoise = this.nextNoise.apply(this.leftNoise);
        }
        this.t = (float) this.ticks / (float) this.tickInterval;
    }

    public float getLeftNoise() {
        return this.leftNoise;
    }

    public float getRightNoise() {
        return this.rightNoise;
    }

    public float getLerp() {
        return this.leftNoise + this.t * (this.rightNoise - this.leftNoise);
    }

    public float getNoise() {
        return this.leftNoise + SmoothNoise.smoothstep(this.t) * (this.rightNoise - this.leftNoise);
    }
}
