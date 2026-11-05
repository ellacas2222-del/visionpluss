package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Avisa con un toast (cabeza de skin + nombre) cuando un jugador NUEVO se acerca.
 * Cada jugador avisa una sola vez: "X is watching you" y luego "Good luck, X".
 * Los jugadores en modo espectador se ignoran.
 */
public class PlayerWatch {
	private record Msg(PlayerSkin skin, String title, String text) {}

	private static final Set<UUID> seen = new HashSet<>();
	private static final ArrayDeque<Msg> queue = new ArrayDeque<>();
	private static int cooldown = 0;
	private static Object lastLevel = null;

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) {
			seen.clear(); queue.clear(); lastLevel = null;
			return;
		}
		if (mc.level != lastLevel) {
			seen.clear(); queue.clear(); cooldown = 0;
			lastLevel = mc.level;
		}

		VPConfig c = VPConfig.I;
		if (c.enabled && c.playerAlert) {
			double max = c.alertChunks * 16.0;
			double max2 = max * max;
			for (AbstractClientPlayer p : mc.level.players()) {
				if (p == mc.player) continue;
				boolean hunter = isHunter(p, c.hunterTeam, c.hunterNames);
				if (p.isSpectator() && !hunter) continue; // solo se avisan los espectadores del equipo hunter
				if (p.distanceToSqr(mc.player) > max2) continue;
				if (seen.add(p.getUUID())) {
					String name = p.getName().getString();
					PlayerSkin skin = p.getSkin();
					String title = hunter ? "Mercury - Hunter detected" : "Mercury - Player Detection";
					queue.add(new Msg(skin, title, name + " is watching you"));
					queue.add(new Msg(skin, title, "Good luck, " + name));
				}
			}
		}

		if (cooldown > 0) cooldown--;
		if (cooldown == 0 && !queue.isEmpty()) {
			Msg m = queue.poll();
			mc.gui.toastManager().addToast(new PlayerToast(m.skin(), m.title(), m.text()));
			cooldown = 60;
		}
	}

	/** Un jugador es hunter si esta en el equipo configurado (ej. /team join Hunter Nombre). */
	private static boolean isHunter(AbstractClientPlayer p, String teamName, java.util.List<String> names) {
		String n = p.getName().getString();
		if (names != null) for (String h : names) if (h.equalsIgnoreCase(n)) return true;
		if (teamName == null || teamName.isEmpty()) return false;
		var team = p.getTeam();
		return team != null && team.getName().equalsIgnoreCase(teamName);
	}
}
