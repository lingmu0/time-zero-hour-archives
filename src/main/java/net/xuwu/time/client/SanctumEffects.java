package net.xuwu.time.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public final class SanctumEffects extends DimensionSpecialEffects {
    public SanctumEffects() { super(Float.NaN, false, SkyType.NONE, true, true); }
    @Override public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) { return new Vec3(.97, .965, .94); }
    @Override public boolean isFoggyAt(int x, int z) { return true; }
    @Override public float[] getSunriseColor(float time, float partial) { return null; }
}
