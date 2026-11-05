package com.visionplus.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/** Toast con la cabeza de la skin del jugador cercano. */
public class PlayerToast implements Toast {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/system");
	private static final long DISPLAY_MS = 5000L;

	private final PlayerSkin skin;
	private final Component title;
	private final Component message;
	private Visibility wanted = Visibility.HIDE;

	public PlayerToast(PlayerSkin skin, String title, String line) {
		this.skin = skin;
		this.title = Component.literal(title).withStyle(st -> st.withColor(0xFFFF55));
		this.message = Component.literal(line);
	}

	@Override
	public Visibility getWantedVisibility() { return wanted; }

	@Override
	public void update(ToastManager manager, long fullyVisibleForMs) {
		wanted = fullyVisibleForMs >= DISPLAY_MS * manager.getNotificationDisplayTimeMultiplier()
			? Visibility.HIDE : Visibility.SHOW;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, width(), height());
		if (skin != null) PlayerFaceExtractor.extractRenderState(graphics, skin, 8, 8, 16);
		graphics.text(font, title, 30, 7, 0xFFFFFFFF, false);
		graphics.text(font, message, 30, 18, 0xFFFFFFFF, false);
	}
}
