package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Avisa cuando un jugador NUEVO se acerca (rango en chunks).
 * Cada jugador solo se avisa una vez: "X is watching you" y luego "Good luck, X".
 * Los jugadores en modo espectador se ignoran.
 */
public class PlayerWatch {
	private static final Set<UUID> seen = new HashSet<>();
	private static final ArrayDeque<String> queue = new ArrayDeque<>();
	private static int cooldown = 0;
	private static Object lastLevel = null;

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) {
			seen.clear(); queue.clear(); lastLevel = null;
			return;
		}
		if (mc.level != lastLevel) { // cambio de mundo/servidor
			seen.clear(); queue.clear(); cooldown = 0;
			lastLevel = mc.level;
		}

		VPConfig c = VPConfig.I;
		if (c.enabled && c.playerAlert) {
			double max = c.alertChunks * 16.0;
			for (Player p : mc.level.players()) {
				if (p == mc.player || p.isSpectator()) continue;
				if (mc.player.distanceTo(p) > max) continue;
				if (seen.add(p.getUUID())) {
					String name = p.getName().getString();
					queue.add(name + " is watching you");
					queue.add("Good luck, " + name);
				}
			}
		}

		if (cooldown > 0) cooldown--;
		if (cooldown == 0 && !queue.isEmpty()) {
			mc.player.sendOverlayMessage(Component.literal(queue.poll()));
			cooldown = 60; // 3 segundos entre mensajes
		}
	}
}
