/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderSet$Named
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.EntityType
 */
package net.conczin.immersive_optimization.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.conczin.immersive_optimization.config.JsonConfig;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class Config
extends JsonConfig {
    private static Config INSTANCE = Config.loadOrCreate(new Config(), Config.class);
    public String _documentation = "https://github.com/Luke100000/ImmersiveOptimization/wiki";
    public boolean enableEntities = true;
    public boolean enableBlockEntities = false;
    public boolean optimizeForceLoadedChunks = false;
    public boolean enableDistanceCulling = true;
    public boolean enableTrackingCulling = true;
    public boolean enableViewportCulling = true;
    public int minDistance = 6;
    public int blocksPerLevel = 64;
    public int blocksPerLevelDistanceCulled = 10;
    public int blocksPerLevelTrackingCulled = 10;
    public int blocksPerLevelViewportCulled = 20;
    public int maxLevel = 20;
    public int blocksPerLevelBlockEntities = 32;
    public int stressedThreshold = 45;
    public float minDecreaseFactor = 0.25f;
    public Map<String, Boolean> dimensions = new HashMap<String, Boolean>();
    public Map<String, Boolean> entities;
    private final transient Map<ResourceLocation, Boolean> entityBlacklistCache;

    public Config() {
        super("immersive_optimization");
        this.dimensions.put("minecraft:overworld", true);
        this.dimensions.put("minecraft:the_nether", true);
        this.dimensions.put("minecraft:the_end", true);
        this.entityBlacklistCache = new ConcurrentHashMap<ResourceLocation, Boolean>();
        this.entities = new HashMap<String, Boolean>();
        this.entities.put("minecraft:player", false);
        this.entities.put("minecraft:ender_dragon", false);
        this.entities.put("minecraft:ender_pearl", false);
        this.entities.put("fromanotherworld:starship", false);
        this.entities.put("create", false);
        this.entities.put("valkyrienskies", false);
        this.entities.put("#minecraft:arrows", false);
        this.entities.put("*", true);
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    @Override
    int getVersion() {
        return 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isBlacklisted(EntityType<?> entityType) {
        ResourceLocation id = BuiltInRegistries.f_256780_.m_7981_(entityType);
        Boolean cached = this.entityBlacklistCache.get(id);
        if (cached != null) {
            return cached;
        }
        Config config = this;
        synchronized (config) {
            return this.entityBlacklistCache.computeIfAbsent(id, ignored -> this.resolveBlacklist(entityType, id));
        }
    }

    private boolean resolveBlacklist(EntityType<?> entityType, ResourceLocation id) {
        Boolean enabled = this.entities.get(id.toString());
        if (enabled != null) {
            return enabled == false;
        }
        enabled = BuiltInRegistries.f_256780_.m_263177_(entityType).m_203616_().filter(tag -> this.entities.containsKey("#" + String.valueOf(tag.f_203868_()))).sorted(Comparator.comparingInt(this::getTagSize).thenComparing(tag -> tag.f_203868_().toString())).map(tag -> this.entities.get("#" + String.valueOf(tag.f_203868_()))).findFirst().orElse(null);
        if (enabled != null) {
            return enabled == false;
        }
        enabled = this.entities.get(id.m_135827_());
        if (enabled != null) {
            return enabled == false;
        }
        return this.entities.getOrDefault("*", true) == false;
    }

    private int getTagSize(TagKey<EntityType<?>> tag) {
        return ((HolderSet.Named)BuiltInRegistries.f_256780_.m_203431_(tag).orElseThrow()).m_203632_();
    }

    public synchronized boolean toggleEntityRule(String id, boolean enabled) {
        boolean added;
        if (Objects.equals(this.entities.get(id), enabled)) {
            this.entities.remove(id);
            added = false;
        } else {
            this.entities.put(id, enabled);
            added = true;
        }
        this.entityBlacklistCache.clear();
        return added;
    }

    public void reload() {
        INSTANCE = Config.loadOrCreate(new Config(), Config.class);
    }
}

