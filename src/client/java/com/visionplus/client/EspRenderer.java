package com.visionplus.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.Optional;

/** Dibuja cajas y lineas A TRAVES de las paredes (sin prueba de profundidad). */
public class EspRenderer {
	private static RenderType type;

	private static RenderType type() {
		if (type == null) {
			try {
				// Sin estado de profundidad = se ve a traves de las paredes
				RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation("pipeline/visionplus_lines")
					.withDepthStencilState(Optional.empty())
					.build();

				Object builder = RenderSetup.builder(pipeline);
				RenderSetup setup = null;
				for (Method m : builder.getClass().getDeclaredMethods()) {
					if (m.getParameterCount() == 0 && m.getReturnType() == RenderSetup.class) {
						m.setAccessible(true);
						setup = (RenderSetup) m.invoke(builder);
						break;
					}
				}
				if (setup == null) throw new IllegalStateException("No se encontro el metodo que crea RenderSetup");

				Method create = RenderType.class.getDeclaredMethod("create", String.class, RenderSetup.class);
				create.setAccessible(true);
				type = (RenderType) create.invoke(null, "visionplus_lines", setup);
			} catch (Exception e) {
				throw new RuntimeException("VisionPlus: no se pudo crear el RenderType", e);
			}
		}
		return type;
	}

	public static void register() {
		LevelRenderEvents.COLLECT_SUBMITS.register(ctx -> {
			VPConfig c = VPConfig.I;
			if (!c.enabled) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			boolean anyTargets = !Scanner.targets.isEmpty();
			boolean anyChunks = c.chunkFinder && !ChunkFinder.cells.isEmpty();
			boolean anyInvis = c.invisBoxes;
			if (!anyTargets && !anyChunks && !anyInvis) return;

			Vec3 cam = mc.gameRenderer.mainCamera().position();
			Vec3 look = Freecam.active ? Freecam.look() : mc.player.getLookAngle();
			double ox = look.x * 0.5, oy = look.y * 0.5, oz = look.z * 0.5;

			ctx.submitNodeCollector().submitCustomGeometry(ctx.poseStack(), type(), (pose, vc) -> {
				// Cofres
				for (Scanner.Target t : Scanner.targets) {
					float r = ((t.color() >> 16) & 255) / 255f;
					float g = ((t.color() >> 8) & 255) / 255f;
					float b = (t.color() & 255) / 255f;
					double x0 = t.pos().getX() - cam.x, y0 = t.pos().getY() - cam.y, z0 = t.pos().getZ() - cam.z;
					if (c.boxes) box(pose, vc, x0, y0, z0, x0 + 1, y0 + 1, z0 + 1, r, g, b, c.alpha, c.lineWidth);
					if (c.tracers) line(pose, vc, ox, oy, oz, x0 + 0.5, y0 + 0.5, z0 + 0.5, r, g, b, c.alpha, c.lineWidth);
				}

				// Chunk finder
				if (c.chunkFinder) {
					for (ChunkFinder.Cell cell : ChunkFinder.cells) {
						int col = ChunkFinder.color(cell.count());
						float r = ((col >> 16) & 255) / 255f, g = ((col >> 8) & 255) / 255f, b = (col & 255) / 255f;
						double x0 = cell.cx() * 16 - cam.x, z0 = cell.cz() * 16 - cam.z;
						double y0 = cell.minY() - 1 - cam.y, y1 = cell.maxY() + 2 - cam.y;
						box(pose, vc, x0, y0, z0, x0 + 16, y1, z0 + 16, r, g, b, c.alpha, c.lineWidth);
					}
				}

				// Hitbox de jugadores invisibles (no espectadores)
				if (c.invisBoxes) {
					float r = ((c.invis >> 16) & 255) / 255f, g = ((c.invis >> 8) & 255) / 255f, b = (c.invis & 255) / 255f;
					for (Player p : mc.level.players()) {
						if (p == mc.player || p.isSpectator() || !p.isInvisible()) continue;
						AABB bb = p.getBoundingBox();
						box(pose, vc, bb.minX - cam.x, bb.minY - cam.y, bb.minZ - cam.z,
							bb.maxX - cam.x, bb.maxY - cam.y, bb.maxZ - cam.z, r, g, b, c.alpha, c.lineWidth);
						if (c.tracers) line(pose, vc, ox, oy, oz, (bb.minX + bb.maxX) / 2 - cam.x,
							(bb.minY + bb.maxY) / 2 - cam.y, (bb.minZ + bb.maxZ) / 2 - cam.z, r, g, b, c.alpha, c.lineWidth);
					}
				}
			});
		});
	}

	private static void line(PoseStack.Pose p, VertexConsumer vc, double x1, double y1, double z1,
							 double x2, double y2, double z2, float r, float g, float b, float a, float w) {
		float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0) return;
		dx /= len; dy /= len; dz /= len;
		vc.addVertex(p, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(w);
		vc.addVertex(p, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(w);
	}

	private static void box(PoseStack.Pose p, VertexConsumer vc, double x1, double y1, double z1,
							double x2, double y2, double z2, float r, float g, float b, float a, float w) {
		// 8 esquinas por bits (x=1, y=2, z=4); 12 aristas, sin crear objetos
		for (int i = 0; i < 8; i++) {
			for (int bit = 1; bit <= 4; bit <<= 1) {
				if ((i & bit) != 0) continue;
				int j = i | bit;
				line(p, vc,
					(i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2,
					(j & 1) == 0 ? x1 : x2, (j & 2) == 0 ? y1 : y2, (j & 4) == 0 ? z1 : z2,
					r, g, b, a, w);
			}
		}
	}
}
