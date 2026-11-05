package com.visionplus.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Pantalla de inicio de Mercury Debug: negra, olas suaves, burbujas y menu minimo. */
public class MercuryTitle extends Screen {
	private static final Identifier LOGO = Identifier.fromNamespaceAndPath("visionplus", "textures/gui/logo.png");
	private static final String[] LABELS = {"Un jugador", "Multijugador", "Ajustes de Minecraft", "Modulos", "Salir del juego"};
	private static final int ROW = 22;

	private long opened;

	public MercuryTitle() {
		super(Component.literal("Mercury Debug"));
	}

	@Override
	protected void init() {
		opened = System.currentTimeMillis();
		int cx = this.width / 2;
		int top = this.height / 2 - 14;
		for (int i = 0; i < LABELS.length; i++) {
			final int idx = i;
			Button b = Button.builder(Component.empty(), btn -> click(idx))
				.bounds(cx - 90, top + i * ROW, 180, 20).build();
			b.setAlpha(0f); // invisible: solo dibujamos nuestro texto encima
			addRenderableWidget(b);
		}
	}

	private void click(int i) {
		switch (i) {
			case 0 -> this.minecraft.setScreenAndShow(new SelectWorldScreen(this));
			case 1 -> this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(this));
			case 2 -> this.minecraft.setScreenAndShow(new OptionsScreen(this, this.minecraft.options, false));
			case 3 -> this.minecraft.setScreenAndShow(new ConfigScreen());
			default -> this.minecraft.stop();
		}
	}

	private static int argb(int a, int rgb) { return (Mth.clamp(a, 0, 255) << 24) | (rgb & 0xFFFFFF); }

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
		// vacio a proposito: el fondo se dibuja en extractRenderState
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		long now = System.currentTimeMillis();
		double time = (now - opened) / 1000.0;
		double t = Mth.clamp((now - opened) / 700.0, 0, 1);       // fundido de entrada
		int a = (int) (255 * t);
		int slide = (int) ((1 - t) * 10);

		g.fill(0, 0, this.width, this.height, 0xFF050507);

		// Olas suaves a los lados
		for (int y = 0; y < this.height; y += 3) {
			int x1 = (int) (34 + 26 * Math.sin(y * 0.018 + time * 0.35) + 12 * Math.sin(y * 0.045 - time * 0.25));
			int x2 = (int) (x1 * 0.55);
			g.fill(0, y, x1, y + 3, 0xFF0C0C10);
			g.fill(0, y, x2, y + 3, 0xFF101015);
			g.fill(this.width - x1, y, this.width, y + 3, 0xFF0C0C10);
			g.fill(this.width - x2, y, this.width, y + 3, 0xFF101015);
		}

		// Burbujas lentas
		for (int i = 0; i < 6; i++) {
			int bx = (int) (this.width * (0.08 + 0.15 * i + (i % 2 == 0 ? 0 : 0.7 * (i / 5.0))));
			int by = (int) (((this.height - (time * (4 + i)) % this.height) + i * 53) % this.height);
			g.fill(bx, by, bx + 3, by + 3, 0xFF1C1C23);
		}

		int cx = this.width / 2;
		int top = this.height / 2 - 14;

		// Simbolo arriba del nombre
		g.blit(RenderPipelines.GUI_TEXTURED, LOGO, cx - 16, top - 78 + slide, 0f, 0f, 32, 32, 64, 64);
		g.centeredText(this.font, Component.literal("Mercury Debug"), cx, top - 38 + slide, argb(a, 0xFFFFFF));

		// Opciones con hover suave
		for (int i = 0; i < LABELS.length; i++) {
			int y = top + i * ROW;
			boolean hover = mx >= cx - 90 && mx <= cx + 90 && my >= y && my < y + 20;
			g.centeredText(this.font, Component.literal(LABELS[i]), cx, y + 6 + slide,
				argb(a, hover ? 0xFFFFFF : 0x8A8A93));
		}

		g.centeredText(this.font, Component.literal("Mercury Debug - 26.2"), cx, this.height - 14, argb(a, 0x4B4B55));

		super.extractRenderState(g, mx, my, pt);
	}
}
