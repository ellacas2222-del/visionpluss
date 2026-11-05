package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/**
 * Freecam: la camara se despega de tu personaje (que se queda quieto).
 * Solo cambia lo que VES; mano, hotbar e inventario siguen normales.
 * Movimiento: W A S D, Espacio (subir), Shift (bajar), Ctrl (rapido).
 */
public class Freecam {
	public static boolean active = false;
	public static double x, y, z;
	public static float yaw, pitch;

	private static float lockYaw, lockPitch;
	private static long last = 0;

	public static void toggle(Minecraft mc) {
		if (active) disable(mc); else enable(mc);
	}

	private static void enable(Minecraft mc) {
		if (mc.player == null || mc.level == null) return;
		Vec3 c = mc.gameRenderer.mainCamera().position();
		x = c.x; y = c.y; z = c.z;
		yaw = lockYaw = mc.player.getYRot();
		pitch = lockPitch = mc.player.getXRot();
		last = System.nanoTime();
		active = true;
		mc.player.sendOverlayMessage(Component.literal("Freecam: ACTIVADO"));
	}

	private static void disable(Minecraft mc) {
		active = false;
		if (mc.player != null) {
			mc.player.setYRot(lockYaw);
			mc.player.setXRot(lockPitch);
			mc.player.sendOverlayMessage(Component.literal("Freecam: DESACTIVADO"));
		}
	}

	public static Vec3 look() {
		double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
		return new Vec3(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
	}

	/** Cada tick: suelta las teclas de movimiento para que tu personaje no se mueva. */
	public static void startTick(Minecraft mc) {
		if (!active) return;
		if (mc.player == null || mc.level == null) { active = false; return; }
		mc.options.keyUp.setDown(false);
		mc.options.keyDown.setDown(false);
		mc.options.keyLeft.setDown(false);
		mc.options.keyRight.setDown(false);
		mc.options.keyJump.setDown(false);
		mc.options.keyShift.setDown(false);
		mc.options.keySprint.setDown(false);
		mc.options.keyAttack.setDown(false);
		mc.options.keyUse.setDown(false);
	}

	private static boolean key(Minecraft mc, int code) {
		return InputConstants.isKeyDown(mc.getWindow(), code);
	}

	/** Se llama una vez por frame (desde el mixin de la camara). */
	public static void frame() {
		if (!active) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		long now = System.nanoTime();
		double dt = (now - last) / 1.0e9;
		if (dt < 0.0005) return; // ya se actualizo en este frame
		last = now;
		if (dt > 0.1) dt = 0.1;

		// El mouse gira la camara, no a tu personaje
		float dy = mc.player.getYRot() - lockYaw;
		float dp = mc.player.getXRot() - lockPitch;
		yaw += dy;
		pitch = Mth.clamp(pitch + dp, -90f, 90f);
		mc.player.setYRot(lockYaw);
		mc.player.setXRot(lockPitch);

		if (!mc.mouseHandler.isMouseGrabbed()) return; // chat o menu abierto: no mover

		double f = (key(mc, GLFW.GLFW_KEY_W) ? 1 : 0) - (key(mc, GLFW.GLFW_KEY_S) ? 1 : 0);
		double s = (key(mc, GLFW.GLFW_KEY_D) ? 1 : 0) - (key(mc, GLFW.GLFW_KEY_A) ? 1 : 0);
		double v = (key(mc, GLFW.GLFW_KEY_SPACE) ? 1 : 0) - (key(mc, GLFW.GLFW_KEY_LEFT_SHIFT) ? 1 : 0);
		double speed = VPConfig.I.freecamSpeed * dt * (key(mc, GLFW.GLFW_KEY_LEFT_CONTROL) ? 3 : 1);

		double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
		double fx = -Math.sin(yr) * Math.cos(pr), fy = -Math.sin(pr), fz = Math.cos(yr) * Math.cos(pr);
		double rx = -Math.cos(yr), rz = -Math.sin(yr);

		x += (fx * f + rx * s) * speed;
		y += (fy * f + v) * speed;
		z += (fz * f + rz * s) * speed;
	}
}
