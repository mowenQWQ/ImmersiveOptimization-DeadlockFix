/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.conczin.immersive_optimization.mixin;

import net.conczin.immersive_optimization.TickScheduler;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Level.class})
public abstract class LevelMixin {
    @Inject(method={"shouldTickBlocksAt(J)Z"}, at={@At(value="HEAD")}, cancellable=true)
    public void immersiveOptimization$shouldTickBlocksAt(long pos, CallbackInfoReturnable<Boolean> cir) {
        if (!TickScheduler.INSTANCE.shouldTick((Level)this, pos)) {
            cir.setReturnValue((Object)false);
        }
    }
}

