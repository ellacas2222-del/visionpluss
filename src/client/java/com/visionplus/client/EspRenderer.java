package com.visionplus.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;

/**
 * Dibuja cajas y lineas A TRAVES de las paredes (sin prueba de profundidad).
 * ESTE ARCHIVO es el mas probable de necesitar ajustes en la 26.2,
 * porque la API de render cambia seguido.
 */
public class EspRenderer {
	private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation("pipeline/visionplus_lines")
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.build();
	private static final RenderType LINES = RenderType.create("visionplus_lines", RenderSetup.builder(PIPELINE).createRenderSetup());

	public static void register() {
		LevelRenderEvents.END_MAIN.register(ctx -> {
			VPConfig c = VPConfig.I;
			if (!c.enabled || Scanner.targets.isEmpty()) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			PoseStack pose = ctx.poseStack();
			MultiBufferSource buffers = ctx.bufferSource();
			VertexConsumer vc = buffers.getBuffer(LINES);
			Vec3 cam = mc.gameRenderer.getMainCamera().position();

			// Origen de las lineas: justo delante de la camara
			Vec3 look = mc.player.getLookAngle();
			double ox = look.x * 0.5, oy = look.y * 0.5, oz = look.z * 0.5;

			for (Scanner.Target t : Scanner.targets) {
				float r = ((t.color() >> 16) & 255) / 255f;
				float g = ((t.color() >> 8) & 255) / 255f;
				float b = (t.color() & 255) / 255f;
				double x0 = t.pos().getX() - cam.x, y0 = t.pos().getY() - cam.y, z0 = t.pos().getZ() - cam.z;

				if (c.boxes) box(pose, vc, x0, y0, z0, x0 + 1, y0 + 1, z0 + 1, r, g, b, c.alpha, c.lineWidth);
				if (c.tracers) line(pose, vc, ox, oy, oz, x0 + 0.5, y0 + 0.5, z0 + 0.5, r, g, b, c.alpha, c.lineWidth);
			}
		});
	}

	private static void line(PoseStack pose, VertexConsumer vc, double x1, double y1, double z1,
							 double x2, double y2, double z2, float r, float g, float b, float a, float w) {
		PoseStack.Pose p = pose.last();
		float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0) return;
		dx /= len; dy /= len; dz /= len;
		vc.addVertex(p, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(w);
		vc.addVertex(p, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(w);
	}

	private static void box(PoseStack pose, VertexConsumer vc, double x1, double y1, double z1,
							double x2, double y2, double z2, float r, float g, float b, float a, float w) {
		double[][] v = {
			{x1, y1, z1}, {x2, y1, z1}, {x2, y1, z2}, {x1, y1, z2},
			{x1, y2, z1}, {x2, y2, z1}, {x2, y2, z2}, {x1, y2, z2}
		};
		int[][] e = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};
		for (int[] k : e) {
			line(pose, vc, v[k[0]][0], v[k[0]][1], v[k[0]][2], v[k[1]][0], v[k[1]][1], v[k[1]][2], r, g, b, a, w);
		}
	}
}
