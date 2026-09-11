/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ForcedChunksSavedData
 *  net.minecraftforge.common.world.ForgeChunkManager$TicketTracker
 */
package net.conczin.immersive_optimization;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.conczin.immersive_optimization.CommonClass;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraftforge.common.world.ForgeChunkManager;

public class ForgeForcedChunkLookup
implements CommonClass.ForcedChunkLookup {
    @Override
    public LongSet getForcedChunks(ServerLevel level) {
        // Modified by March7th: only read the in-memory forced chunk set (lock-free).
        // The storage-backed lookup (ForcedChunksSavedData / ForgeChunkManager tickets) holds
        // internal locks and, when read from the worker thread, can deadlock against the
        // main thread's chunk management (ServerChunkCache). Reading only level.getForcedChunks()
        // avoids the storage lock entirely while preserving forced-chunk culling behavior.
        return new LongOpenHashSet((LongCollection)level.m_8902_());
    }

    private static <T extends Comparable<? super T>> void addAll(LongSet chunks, ForgeChunkManager.TicketTracker<T> tracker) {
        ForgeForcedChunkLookup.addAll(chunks, tracker.getChunks());
        ForgeForcedChunkLookup.addAll(chunks, tracker.getTickingChunks());
    }

    private static void addAll(LongSet chunks, Map<?, LongSet> tickets) {
        for (LongSet ticketChunks : tickets.values()) {
            chunks.addAll((LongCollection)ticketChunks);
        }
    }
}

