/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.common.Mod
 */
package net.conczin.immersive_optimization;

import net.conczin.immersive_optimization.CommonClass;
import net.conczin.immersive_optimization.ForgeForcedChunkLookup;
import net.minecraftforge.fml.common.Mod;

@Mod(value="immersive_optimization")
public class ImmersiveOptimization {
    public ImmersiveOptimization() {
        CommonClass.forcedChunkLookup = new ForgeForcedChunkLookup();
        CommonClass.init();
    }
}

