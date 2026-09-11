/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.conczin.immersive_optimization.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.immersive_optimization.TickScheduler;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public class LevelRendererMixin {
    @Shadow
    private Frustum f_172938_;

    @Inject(method={"prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"}, at={@At(value="RETURN")})
    private void immersiveOptimization$prepareCullFrustum(PoseStack $$0, Vec3 $$1, Matrix4f $$2, CallbackInfo ci) {
        if (TickScheduler.INSTANCE.frustum == null) {
            TickScheduler.INSTANCE.frustum = aabb -> this.f_172938_.m_113029_(aabb.m_82400_(1.0));
        }
    }
}

