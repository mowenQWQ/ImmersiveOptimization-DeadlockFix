/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.server.level.ServerLevel
 */
package net.conczin.immersive_optimization;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerLevel;

public class CommonClass {
    public static ForcedChunkLookup forcedChunkLookup = level -> new LongOpenHashSet((LongCollection)level.m_8902_());

    public static void init() {
    }

    public static LongSet getForcedChunks(ServerLevel level) {
        return forcedChunkLookup.getForcedChunks(level);
    }

    public static interface ForcedChunkLookup {
        public LongSet getForcedChunks(ServerLevel var1);
    }
}

