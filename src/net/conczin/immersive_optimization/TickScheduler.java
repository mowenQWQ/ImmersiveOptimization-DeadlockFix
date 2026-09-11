/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.longs.LongSets
 *  net.minecraft.core.SectionPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  org.jetbrains.annotations.Nullable
 */
package net.conczin.immersive_optimization;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.conczin.immersive_optimization.CommonClass;
import net.conczin.immersive_optimization.Constants;
import net.conczin.immersive_optimization.config.Config;
import net.conczin.immersive_optimization.mixin.EntityTickListAccessor;
import net.conczin.immersive_optimization.mixin.ServerLevelAccessor;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class TickScheduler {
    public static TickScheduler INSTANCE = new TickScheduler();
    public static final int THREAD_SLEEP = 500;
    public static final int MAX_STRESS_TICKS = 600;
    public static final int CLEAR_BLOCK_ENTITIES_INTERVAL = 207;
    public FrustumProxy frustum;
    public final Map<ResourceLocation, LevelData> levelData = new ConcurrentHashMap<ResourceLocation, LevelData>();
    private ResourceLocation currentTickDimension;
    private LevelData currentTickLevelData;

    public static void setServer(MinecraftServer server) {
        INSTANCE.reset();
        Thread worker = new Thread((Runnable)new Worker(INSTANCE, server), "Immersive Optimization Worker");
        worker.setPriority(1);
        worker.setDaemon(true);
        worker.start();
    }

    @Nullable
    public LevelData getLevelData(Level level) {
        return this.levelData.get(level.m_46472_().m_135782_());
    }

    public void reset() {
        this.levelData.clear();
        this.currentTickDimension = null;
        this.currentTickLevelData = null;
        this.frustum = null;
    }

    public void startLevelTick(ServerLevel level) {
        boolean stressed;
        LevelData data = this.getLevelData((Level)level);
        this.currentTickDimension = level.m_46472_().m_135782_();
        this.currentTickLevelData = data;
        if (data == null) {
            return;
        }
        int stressedThreshold = Config.getInstance().stressedThreshold;
        boolean bl = stressed = stressedThreshold > 0 && level.m_7654_().m_129903_() > (float)stressedThreshold;
        if (stressed) {
            data.stressedTicks = Math.min(600, data.stressedTicks + 2);
            ++data.lifeTimeStressedTicks;
        } else {
            data.stressedTicks = Math.max(0, data.stressedTicks - 1);
        }
        ++data.tick;
        data.time = System.nanoTime();
    }

    void tickLevel(ServerLevel level) {
        LevelData data = this.levelData.computeIfAbsent(level.m_46472_().m_135782_(), LevelData::new);
        long tick = level.m_46467_();
        Stats previousStats = data.previousStats;
        data.previousStats = data.stats;
        data.stats = previousStats;
        data.stats.reset();
        Config config = Config.getInstance();
        Object object = data.forcedChunks = config.optimizeForceLoadedChunks ? LongSets.EMPTY_SET : CommonClass.getForcedChunks(level);
        if (tick % 207L == 0L) {
            data.blockEntityPriorities.clear();
        }
        if (!config.enableEntities) {
            data.priorities = new Int2IntOpenHashMap();
            return;
        }
        Int2IntOpenHashMap newPriorities = new Int2IntOpenHashMap();
        Int2ObjectMap<Entity> entities = ((EntityTickListAccessor)((ServerLevelAccessor)level).getEntityTickList()).getActive();
        entities.values().forEach(entity -> {
            if (entity != null) {
                int priority = this.getPriority(data, level, (Entity)entity);
                if (priority > 1) {
                    newPriorities.put(entity.m_19879_(), priority);
                }
                data.stats.tickRate += 1.0 / (double)Math.max(1, priority);
                ++data.stats.entities;
            }
        });
        data.priorities = newPriorities;
    }

    public boolean shouldTick(Entity entity) {
        LevelData data;
        if (entity.f_19811_ || INSTANCE == null) {
            return true;
        }
        ResourceLocation dimension = entity.m_9236_().m_46472_().m_135782_();
        LevelData levelData = data = dimension.equals((Object)this.currentTickDimension) ? this.currentTickLevelData : this.levelData.get(dimension);
        if (data == null) {
            return true;
        }
        int priority = data.priorities.get(entity.m_19879_());
        if (priority <= 1) {
            return true;
        }
        return (data.tick + (long)entity.m_19879_()) % (long)priority == 0L;
    }

    public int getPriority(LevelData data, ServerLevel level, Entity entity) {
        boolean integratedAndSinglePlayer;
        Config config = Config.getInstance();
        if (config.isBlacklisted(entity.m_6095_())) {
            return 0;
        }
        if (data.forcedChunks.contains(entity.m_146902_().m_45588_())) {
            return 0;
        }
        double minDistance = 999999.0;
        for (Player player : level.m_6907_()) {
            minDistance = Math.min(minDistance, player.m_20280_(entity));
        }
        AABB box = entity.m_20191_();
        int blocksPerLevel = config.blocksPerLevel;
        boolean bl = integratedAndSinglePlayer = !level.m_7654_().m_6982_() && level.m_6907_().size() == 1;
        if (config.enableDistanceCulling && integratedAndSinglePlayer && !entity.m_6783_(minDistance)) {
            blocksPerLevel = config.blocksPerLevelDistanceCulled;
            ++data.stats.distanceCulledEntities;
        }
        if (config.enableTrackingCulling && blocksPerLevel > config.blocksPerLevelTrackingCulled) {
            int trackingRange = entity.m_6095_().m_20681_();
            int scaledTrackingDistance = level.m_7654_().m_7186_(trackingRange * 16);
            if (minDistance > (double)(scaledTrackingDistance * scaledTrackingDistance)) {
                blocksPerLevel = config.blocksPerLevelTrackingCulled;
                ++data.stats.trackingCulledEntities;
            }
        }
        if (config.enableViewportCulling && blocksPerLevel > config.blocksPerLevelViewportCulled && integratedAndSinglePlayer && this.frustum != null && !this.frustum.isVisible(box)) {
            blocksPerLevel = config.blocksPerLevelViewportCulled;
            ++data.stats.viewportCulledEntities;
        }
        double antiStress = 1.0 - (double)data.stressedTicks / 600.0 * (double)config.minDecreaseFactor;
        int finalBlocksPerLevel = (int)((double)blocksPerLevel * antiStress);
        int distanceLevel = (int)((Math.sqrt(minDistance) - (double)config.minDistance) / (double)Math.max(2, finalBlocksPerLevel));
        return Math.min(config.maxLevel, Math.max(1, distanceLevel + 1));
    }

    public boolean shouldTick(Level level, long pos) {
        if (!Config.getInstance().enableBlockEntities) {
            return true;
        }
        LevelData data = this.getLevelData(level);
        if (data == null) {
            return true;
        }
        if (data.forcedChunks.contains(pos)) {
            return true;
        }
        int priority = data.blockEntityPriorities.computeIfAbsent(pos, p -> this.getBlockEntityPriority(level, (long)p));
        return priority < 1 || (level.m_46467_() + pos) % (long)priority == 0L;
    }

    private int getBlockEntityPriority(Level level, long p) {
        int x = ChunkPos.m_45592_((long)p);
        int z = ChunkPos.m_45602_((long)p);
        if (x < -300000 || x > 300000 || z < -300000 || z > 300000) {
            return -1;
        }
        double minDistance = Double.MAX_VALUE;
        for (Player player : level.m_6907_()) {
            double dz;
            double dx = SectionPos.m_235865_((double)player.m_20185_()) - x;
            double distance = dx * dx + (dz = (double)(SectionPos.m_235865_((double)player.m_20189_()) - z)) * dz;
            if (!(distance < minDistance)) continue;
            minDistance = distance;
        }
        return Math.min((int)((Math.sqrt(minDistance * 16.0) - (double)Config.getInstance().minDistance) / (double)Config.getInstance().blocksPerLevelBlockEntities) + 1, Config.getInstance().maxLevel);
    }

    public static class Worker
    implements Runnable {
        TickScheduler scheduler;
        MinecraftServer server;

        public Worker(TickScheduler scheduler, MinecraftServer server) {
            this.scheduler = scheduler;
            this.server = server;
        }

        @Override
        public void run() {
            while (this.server.m_130010_()) {
                try {
                    for (ServerLevel level : this.server.m_129785_()) {
                        this.scheduler.tickLevel(level);
                    }
                    Thread.sleep(500L);
                }
                catch (ArrayIndexOutOfBoundsException | ConcurrentModificationException runtimeException) {
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Constants.LOG.info("Shutting down Immersive Optimization worker");
        }
    }

    public static class LevelData {
        public boolean active;
        public long tick = 0L;
        public long time = 0L;
        public Stats stats = new Stats();
        public Stats previousStats = new Stats();
        public int stressedTicks = 0;
        public int lifeTimeStressedTicks = 0;
        public volatile Int2IntOpenHashMap priorities = new Int2IntOpenHashMap();
        public volatile LongSet forcedChunks = LongSets.EMPTY_SET;
        public Map<Long, Integer> blockEntityPriorities = new ConcurrentHashMap<Long, Integer>();

        public LevelData(ResourceLocation location) {
            this.active = Config.getInstance().dimensions.getOrDefault(location.toString(), true);
        }

        public int getEntities() {
            return this.previousStats.entities;
        }

        public String toLog() {
            return "Rate %2.1f%%, %d entities, %d stress, %d stressed | culled %2.1f%% distance, %2.1f%% tracking, %2.1f%% viewport".formatted(this.previousStats.tickRate / (double)this.previousStats.entities * 100.0, this.previousStats.entities, this.stressedTicks, this.lifeTimeStressedTicks, Float.valueOf((float)this.previousStats.distanceCulledEntities / (float)this.previousStats.entities * 100.0f), Float.valueOf((float)this.previousStats.trackingCulledEntities / (float)this.previousStats.entities * 100.0f), Float.valueOf((float)this.previousStats.viewportCulledEntities / (float)this.previousStats.entities * 100.0f));
        }
    }

    public static interface FrustumProxy {
        public boolean isVisible(AABB var1);
    }

    public static class Stats {
        public double tickRate = 0.0;
        public int entities = 0;
        public int distanceCulledEntities = 0;
        public int trackingCulledEntities = 0;
        public int viewportCulledEntities = 0;

        public void reset() {
            this.tickRate = 0.0;
            this.entities = 0;
            this.distanceCulledEntities = 0;
            this.trackingCulledEntities = 0;
            this.viewportCulledEntities = 0;
        }
    }
}

