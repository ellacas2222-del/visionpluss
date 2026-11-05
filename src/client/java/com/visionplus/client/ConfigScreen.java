package com.visionplus.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menu de ajustes de Vision Plus. */
public class ConfigScreen extends Screen {
	private static final int[] PALETTE = {
		0xFF3030, 0xFFA500, 0xFFFF00, 0x40FF40, 0x40E0FF, 0x4060FF, 0xB040FF, 0xFF60C0, 0xFFFFFF, 0x8B5A2B
	};
	private static final String[] NAMES = {
		"Rojo", "Naranja", "Amarillo", "Verde", "Celeste", "Azul", "Morado", "Rosa", "Blanco", "Cafe"
	};

	public ConfigScreen() {
		super(Component.literal("Vision Plus"));
	}

	private static String colorName(int c) {
		for (int i = 0; i < PALETTE.length; i++) if (PALETTE[i] == c) return NAMES[i];
		return "Personal";
	}

	private static int next(int c) {
		for (int i = 0; i < PALETTE.length; i++) if (PALETTE[i] == c) return PALETTE[(i + 1) % PALETTE.length];
		return PALETTE[0];
	}

	private static String onOff(boolean b) { return b ? "SI" : "NO"; }

	@Override
	protected void init() {
		VPConfig c = VPConfig.I;
		int w = 200, h = 20, x = this.width / 2 - w - 5, x2 = this.width / 2 + 5, y = 30, s = 24;

		// Columna izquierda
		addRenderableWidget(Button.builder(Component.literal("Activado: " + onOff(c.enabled)), b -> {
			c.enabled = !c.enabled; b.setMessage(Component.literal("Activado: " + onOff(c.enabled)));
		}).bounds(x, y, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Cajas x-ray: " + onOff(c.boxes)), b -> {
			c.boxes = !c.boxes; b.setMessage(Component.literal("Cajas x-ray: " + onOff(c.boxes)));
		}).bounds(x, y + s, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Lineas guia: " + onOff(c.tracers)), b -> {
			c.tracers = !c.tracers; b.setMessage(Component.literal("Lineas guia: " + onOff(c.tracers)));
		}).bounds(x, y + s * 2, w, h).build());

		addRenderableWidget(new AbstractSliderButton(x, y + s * 3, w, h,
			Component.literal("Rango: " + c.range), (c.range - 8) / 248.0) {
			@Override protected void updateMessage() { setMessage(Component.literal("Rango: " + (int) (8 + value * 248))); }
			@Override protected void applyValue() { c.range = (int) (8 + value * 248); }
		});
		addRenderableWidget(new AbstractSliderButton(x, y + s * 4, w, h,
			Component.literal("Grosor linea: " + c.lineWidth), (c.lineWidth - 1) / 7.0) {
			@Override protected void updateMessage() { setMessage(Component.literal("Grosor linea: " + String.format("%.1f", 1 + value * 7))); }
			@Override protected void applyValue() { c.lineWidth = (float) (1 + value * 7); }
		});
		addRenderableWidget(new AbstractSliderButton(x, y + s * 5, w, h,
			Component.literal("Transparencia: " + (int) (c.alpha * 100) + "%"), (c.alpha - 0.1) / 0.9) {
			@Override protected void updateMessage() { setMessage(Component.literal("Opacidad: " + (int) ((0.1 + value * 0.9) * 100) + "%")); }
			@Override protected void applyValue() { c.alpha = (float) (0.1 + value * 0.9); }
		});

		// Columna derecha: colores
		addRenderableWidget(Button.builder(Component.literal("Cofre: " + colorName(c.chest)), b -> {
			c.chest = next(c.chest); b.setMessage(Component.literal("Cofre: " + colorName(c.chest)));
		}).bounds(x2, y, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Cofre trampa: " + colorName(c.trapped)), b -> {
			c.trapped = next(c.trapped); b.setMessage(Component.literal("Cofre trampa: " + colorName(c.trapped)));
		}).bounds(x2, y + s, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Ender chest: " + colorName(c.ender)), b -> {
			c.ender = next(c.ender); b.setMessage(Component.literal("Ender chest: " + colorName(c.ender)));
		}).bounds(x2, y + s * 2, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Barril: " + colorName(c.barrel)), b -> {
			c.barrel = next(c.barrel); b.setMessage(Component.literal("Barril: " + colorName(c.barrel)));
		}).bounds(x2, y + s * 3, w, h).build());
		addRenderableWidget(Button.builder(Component.literal("Shulker: " + colorName(c.shulker)), b -> {
			c.shulker = next(c.shulker); b.setMessage(Component.literal("Shulker: " + colorName(c.shulker)));
		}).bounds(x2, y + s * 4, w, h).build());

		addRenderableWidget(Button.builder(Component.literal("Listo"), b -> onClose())
			.bounds(this.width / 2 - 100, y + s * 7, 200, h).build());
	}

	@Override
	public void onClose() {
		VPConfig.save();
		super.onClose();
	}
}
