/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.conczin.immersive_optimization.mixin;

import java.util.function.BooleanSupplier;
import net.conczin.immersive_optimization.TickScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerLevel.class})
public abstract class ServerLevelMixin {
    @Inject(method={"tick(Ljava/util/function/BooleanSupplier;)V"}, at={@At(value="HEAD")})
    private void immersiveOptimization$tick(BooleanSupplier $$0, CallbackInfo ci) {
        TickScheduler.INSTANCE.startLevelTick((ServerLevel)this);
    }

    @Inject(method={"tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void immersiveOptimization$tickNonPassenger(Entity entity, CallbackInfo ci) {
        if (TickScheduler.INSTANCE != null && !TickScheduler.INSTANCE.shouldTick(entity)) {
            ci.cancel();
        }
    }
}

