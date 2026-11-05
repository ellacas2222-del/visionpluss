package com.visionplus.client.mixin;

import com.visionplus.client.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Cuando el Freecam esta activo, reemplaza la posicion y rotacion de la camara. */
@Mixin(Camera.class)
public abstract class CameraMixin {

	@ModifyVariable(method = "setPosition(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double vp$x(double v) { Freecam.frame(); return Freecam.active ? Freecam.x : v; }

	@ModifyVariable(method = "setPosition(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private double vp$y(double v) { return Freecam.active ? Freecam.y : v; }

	@ModifyVariable(method = "setPosition(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private double vp$z(double v) { return Freecam.active ? Freecam.z : v; }

	@ModifyVariable(method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), argsOnly = true)
	private Vec3 vp$vec(Vec3 v) { Freecam.frame(); return Freecam.active ? new Vec3(Freecam.x, Freecam.y, Freecam.z) : v; }

	@ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float vp$yaw(float v) { return Freecam.active ? Freecam.yaw : v; }

	@ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private float vp$pitch(float v) { return Freecam.active ? Freecam.pitch : v; }
}
