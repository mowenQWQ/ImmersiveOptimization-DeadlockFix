/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.DebugScreenOverlay
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.conczin.immersive_optimization.mixin.client;

import java.util.List;
import net.conczin.immersive_optimization.TickScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DebugScreenOverlay.class})
public class DebugHudMixin {
    @Inject(at={@At(value="RETURN")}, method={"getGameInformation"})
    protected void getLeftText(CallbackInfoReturnable<List<String>> info) {
        TickScheduler.LevelData data;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level != null && (data = TickScheduler.INSTANCE.getLevelData((Level)level)) != null) {
            ((List)info.getReturnValue()).add("[IO] " + data.toLog());
        }
    }
}

