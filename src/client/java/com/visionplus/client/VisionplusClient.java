package com.visionplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class VisionplusClient implements ClientModInitializer {
	public static KeyMapping toggleKey;
	public static KeyMapping menuKey;

	@Override
	public void onInitializeClient() {
		VPConfig.load();

		KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("visionplus", "main"));
		toggleKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.visionplus.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, cat));
		menuKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.visionplus.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, cat));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (toggleKey.consumeClick()) {
				VPConfig.I.enabled = !VPConfig.I.enabled;
				VPConfig.save();
				if (mc.player != null) {
					mc.player.sendOverlayMessage(Component.literal("Vision Plus: " + (VPConfig.I.enabled ? "ACTIVADO" : "DESACTIVADO")));
				}
			}
			while (menuKey.consumeClick()) {
				mc.setScreenAndShow(new ConfigScreen());
			}
			Scanner.tick(mc);
			ChunkFinder.tick(mc);
			PlayerWatch.tick(mc);
		});

		EspRenderer.register();
	}
}
