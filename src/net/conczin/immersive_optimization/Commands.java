/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 */
package net.conczin.immersive_optimization;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.function.Consumer;
import net.conczin.immersive_optimization.TickScheduler;
import net.conczin.immersive_optimization.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Commands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"io").requires(source -> source.m_6761_(4))).then(LiteralArgumentBuilder.literal((String)"report").executes(context -> {
            StringBuilder sb = new StringBuilder();
            sb.append("\u00a7l\u00a7a[Immersive Optimization Report]\u00a7r\n");
            TickScheduler.INSTANCE.levelData.forEach((key, data) -> {
                if (data.getEntities() > 0) {
                    sb.append("%s: %s\n".formatted(key.m_135815_(), data.toLog()));
                }
            });
            Commands.send((CommandContext<CommandSourceStack>)context, sb.toString());
            return 0;
        }))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"config").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"preset").then(LiteralArgumentBuilder.literal((String)"performance").executes(context -> {
            Commands.setConfigPreset(0.5f);
            return 0;
        }))).then(LiteralArgumentBuilder.literal((String)"default").executes(context -> {
            Commands.setConfigPreset(1.0f);
            return 0;
        }))).then(LiteralArgumentBuilder.literal((String)"quality").executes(context -> {
            Commands.setConfigPreset(2.0f);
            return 0;
        })))).then(Commands.toggle("enableEntities", enabled -> {
            Config.getInstance().enableEntities = enabled;
        }))).then(Commands.toggle("enableBlockEntities", enabled -> {
            Config.getInstance().enableBlockEntities = enabled;
        }))).then(Commands.toggle("optimizeForceLoadedChunks", enabled -> {
            Config.getInstance().optimizeForceLoadedChunks = enabled;
        }))).then(Commands.toggle("enableDistanceCulling", enabled -> {
            Config.getInstance().enableDistanceCulling = enabled;
        }))).then(Commands.toggle("enableTrackingCulling", enabled -> {
            Config.getInstance().enableTrackingCulling = enabled;
        }))).then(Commands.toggle("enableViewportCulling", enabled -> {
            Config.getInstance().enableViewportCulling = enabled;
        }))).then(Commands.toggle("enabledStress", enabled -> {
            Config.getInstance().stressedThreshold = enabled != false ? new Config().stressedThreshold : 0;
        }))).then(Commands.entityRule("blacklist", false))).then(Commands.entityRule("whitelist", true))).then(LiteralArgumentBuilder.literal((String)"reload").executes(context -> {
            Config.getInstance().reload();
            Commands.send((CommandContext<CommandSourceStack>)context, "Config reloaded!");
            return 0;
        }))));
    }

    private static void setConfigPreset(float quality) {
        Config d = new Config();
        Config c = Config.getInstance();
        c.minDistance = (int)((float)d.minDistance * quality);
        c.blocksPerLevel = (int)((float)d.blocksPerLevel * quality);
        c.blocksPerLevelDistanceCulled = (int)((float)d.blocksPerLevelDistanceCulled * quality);
        c.blocksPerLevelTrackingCulled = (int)((float)d.blocksPerLevelTrackingCulled * quality);
        c.blocksPerLevelViewportCulled = (int)((float)d.blocksPerLevelViewportCulled * quality);
        c.maxLevel = (int)((float)d.maxLevel / quality);
        c.save();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> toggle(String name, Consumer<Boolean> consumer) {
        return LiteralArgumentBuilder.literal((String)name).then(RequiredArgumentBuilder.argument((String)"enabled", (ArgumentType)BoolArgumentType.bool()).executes(context -> {
            consumer.accept(BoolArgumentType.getBool((CommandContext)context, (String)"enabled"));
            TickScheduler.INSTANCE.reset();
            Config.getInstance().save();
            Commands.send((CommandContext<CommandSourceStack>)context, "Config updated!");
            return 0;
        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> entityRule(String name, boolean enabled) {
        return LiteralArgumentBuilder.literal((String)name).then(RequiredArgumentBuilder.argument((String)"id", (ArgumentType)StringArgumentType.greedyString()).executes(context -> {
            String id = StringArgumentType.getString((CommandContext)context, (String)"id");
            Config config = Config.getInstance();
            boolean added = config.toggleEntityRule(id, enabled);
            config.save();
            Commands.send((CommandContext<CommandSourceStack>)context, added ? "Added %s to %s!".formatted(id, name) : "Removed %s from %s!".formatted(id, name));
            return 0;
        }));
    }

    private static void send(CommandContext<CommandSourceStack> context, String message) {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).m_230896_();
        if (player != null) {
            player.m_213846_((Component)Component.m_237113_((String)message));
        }
    }
}

