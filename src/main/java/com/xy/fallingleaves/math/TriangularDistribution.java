package com.xy.fallingleaves.math;

import java.util.Random;
import javax.annotation.Nullable;

public class TriangularDistribution {
    public final float a;
    public final float b;
    public final float c;
    protected final Random rng;
    protected final float f;

    public TriangularDistribution(float a, float b, float c, @Nullable Random rng) {
        if (!(a < b && a <= c && c <= b)) {
            throw new IllegalArgumentException(String.format("not %f <= %f <= %f", a, b, c));
        }
        this.a = a;
        this.b = b;
        this.c = c;
        this.f = (c - a) / (b - a);
        this.rng = rng == null ? new Random() : rng;
    }

    public float sample() {
        float u = this.rng.nextFloat();
        if (u < this.f) {
            return this.a + (float) Math.sqrt(u * (this.b - this.a) * (this.c - this.a));
        }
        return this.b - (float) Math.sqrt((1.0f - u) * (this.b - this.a) * (this.b - this.c));
    }
}
