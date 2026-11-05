package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;

/** Ultra optimizador: baja los ajustes pesados del juego y los restaura al apagarlo. */
public class Optimizer {
	private static boolean on = false;
	private static int oRender, oSim, oBlend;
	private static double oEntity;
	private static boolean oShadows;
	private static ParticleStatus oParticles;

	public static boolean isOn() { return on; }

	public static void toggle(Minecraft mc) {
		Options o = mc.options;
		if (!on) {
			oRender = o.renderDistance().get();
			oSim = o.simulationDistance().get();
			oEntity = o.entityDistanceScaling().get();
			oBlend = o.biomeBlendRadius().get();
			oShadows = o.entityShadows().get();
			oParticles = o.particles().get();

			o.renderDistance().set(8);
			o.simulationDistance().set(6);
			o.entityDistanceScaling().set(0.5);
			o.biomeBlendRadius().set(0);
			o.entityShadows().set(false);
			o.particles().set(ParticleStatus.MINIMAL);
		} else {
			o.renderDistance().set(oRender);
			o.simulationDistance().set(oSim);
			o.entityDistanceScaling().set(oEntity);
			o.biomeBlendRadius().set(oBlend);
			o.entityShadows().set(oShadows);
			o.particles().set(oParticles);
		}
		on = !on;
		o.save();
	}
}
