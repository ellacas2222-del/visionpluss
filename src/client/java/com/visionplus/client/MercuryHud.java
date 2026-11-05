package com.visionplus.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** HUD en el juego: logo transparente arriba a la derecha y lista de modulos activos. */
public class MercuryHud {
	private static final Identifier LOGO = Identifier.fromNamespaceAndPath("visionplus", "textures/gui/logo_hud.png");

	public static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("visionplus", "hud"), (g, tick) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;
			int w = mc.getWindow().getGuiScaledWidth();

			// Logo + nombre, arriba a la derecha
			g.blit(RenderPipelines.GUI_TEXTURED, LOGO, w - 76, 6, 0f, 0f, 14, 14, 64, 64);
			g.text(mc.font, Component.literal("Mercury"), w - 58, 10, 0x99FFFFFF, true);

			// Modulos activos, arriba a la izquierda
			VPConfig c = VPConfig.I;
			List<String> on = new ArrayList<>();
			if (c.enabled && (c.boxes || c.tracers)) on.add("ESP de cofres");
			if (c.enabled && c.chunkFinder) on.add("Chunk finder");
			if (c.enabled && c.playerAlert) on.add("Aviso de jugadores");
			if (Freecam.active) on.add("Freecam");
			if (Optimizer.isOn()) on.add("Optimizador");
			int y = 6;
			for (String s : on) {
				g.text(mc.font, Component.literal(s), 6, y, 0xCCFFFFFF, true);
				y += 10;
			}
		});
	}
}
