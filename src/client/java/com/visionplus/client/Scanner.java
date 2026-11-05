package com.visionplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

/** Busca contenedores cerca del jugador (cada 10 ticks). */
public class Scanner {
	public record Target(BlockPos pos, int color) {}

	public static volatile List<Target> targets = List.of();
	private static int tick = 0;

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null || !VPConfig.I.enabled) {
			targets = List.of();
			return;
		}
		if (tick++ % 10 != 0) return;

		VPConfig c = VPConfig.I;
		int range = c.range;
		double r2 = (double) range * range;
		BlockPos p = mc.player.blockPosition();
		int cr = (range >> 4) + 1;
		int pcx = p.getX() >> 4, pcz = p.getZ() >> 4;

		List<Target> out = new ArrayList<>();
		for (int cx = pcx - cr; cx <= pcx + cr; cx++) {
			for (int cz = pcz - cr; cz <= pcz + cr; cz++) {
				LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) continue;
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					BlockPos bp = be.getBlockPos();
					if (bp.distSqr(p) > r2) continue;
					int color;
					if (be instanceof TrappedChestBlockEntity) color = c.trapped;
					else if (be instanceof ChestBlockEntity) color = c.chest;
					else if (be instanceof EnderChestBlockEntity) color = c.ender;
					else if (be instanceof BarrelBlockEntity) color = c.barrel;
					else if (be instanceof ShulkerBoxBlockEntity) color = c.shulker;
					else continue;
					out.add(new Target(bp.immutable(), color));
				}
			}
		}
		targets = out;
	}
}
