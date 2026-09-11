/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.conczin.immersive_optimization.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonConfig {
    public static final Logger LOGGER = LogManager.getLogger();
    public int version = 0;
    public final String name;

    int getVersion() {
        return 1;
    }

    public JsonConfig(String name) {
        this.name = name;
    }

    public static File getConfigFile(String id) {
        return new File("./config/" + id + ".json");
    }

    public void save() {
        try (FileWriter writer = new FileWriter(JsonConfig.getConfigFile(this.name));){
            this.version = this.getVersion();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson((Object)this, (Appendable)writer);
        }
        catch (IOException e) {
            LOGGER.error((Object)e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T extends JsonConfig> T loadOrCreate(T defaultConfig, Class<T> jsonClass) {
        String name = defaultConfig.name;
        if (!JsonConfig.getConfigFile(name).exists()) {
            defaultConfig.save();
            return defaultConfig;
        }
        try (FileReader reader = new FileReader(JsonConfig.getConfigFile(name));){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonConfig config = (JsonConfig)gson.fromJson((Reader)reader, jsonClass);
            if (config.version != config.getVersion()) {
                config = defaultConfig;
            }
            config.save();
            JsonConfig jsonConfig = config;
            return (T)jsonConfig;
        }
        catch (Exception e) {
            LOGGER.error("Failed to load config for '{}'! Default config is used for now. Delete the file to reset.", (Object)name);
            LOGGER.error((Object)e);
            return defaultConfig;
        }
    }
}

