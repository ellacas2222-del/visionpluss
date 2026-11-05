package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/** Marca los chunks que tienen muchos contenedores (posibles bases). */
public class ChunkFinder {
	public record Cell(int cx, int cz, int count, int minY, int maxY) {}

	public static volatile List<Cell> cells = List.of();
	private static int tick = 0;
	private static final Set<Long> announced = new HashSet<>();
	private static Object lastLevel = null;

	public static void tick(Minecraft mc) {
		VPConfig c = VPConfig.I;
		if (mc.level == null || mc.player == null || !c.enabled || !c.chunkFinder) {
			cells = List.of();
			return;
		}
		if (tick++ % 60 != 0) return;

		if (mc.level != lastLevel) { announced.clear(); lastLevel = mc.level; }

		int pcx = mc.player.blockPosition().getX() >> 4;
		int pcz = mc.player.blockPosition().getZ() >> 4;
		int r = c.chunkRange;
		List<Cell> out = new ArrayList<>();
		for (int cx = pcx - r; cx <= pcx + r; cx++) {
			for (int cz = pcz - r; cz <= pcz + r; cz++) {
				LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) continue;
				int count = 0, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (!(be instanceof Container)) continue;
					count++;
					int y = be.getBlockPos().getY();
					if (y < minY) minY = y;
					if (y > maxY) maxY = y;
				}
				if (count >= c.chunkMin) out.add(new Cell(cx, cz, count, minY, maxY));
			}
		}
		// Aviso tipo logro: maximo 2 por revision para no llenar la pantalla
		int shown = 0;
		for (Cell cell : out) {
			if (shown >= 2) break;
			if (cell.count() < c.chunkAlertMin) continue;
			long key = ((long) cell.cx() << 32) ^ (cell.cz() & 0xFFFFFFFFL);
			if (announced.add(key)) {
				mc.getToastManager().addToast(new PlayerToast(null, "Mercury - Suspicious chunk",
					cell.count() + " containers at " + (cell.cx() * 16) + ", " + (cell.cz() * 16)));
				shown++;
			}
		}

		if (out.size() > 64) {
			out.sort(java.util.Comparator.comparingInt(cell -> -cell.count()));
			out = new ArrayList<>(out.subList(0, 64));
		}
		cells = out;
	}

	/** Color segun cantidad: verde -> naranja -> rojo. */
	public static int color(int count) {
		if (count >= 25) return 0xFF3030;
		if (count >= 10) return 0xFFA500;
		return 0x40FF40;
	}
}
