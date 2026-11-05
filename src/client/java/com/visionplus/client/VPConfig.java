package com.visionplus.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Configuracion guardada en config/visionplus.json */
public class VPConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visionplus.json");
	public static VPConfig I = new VPConfig();

	public boolean enabled = true;
	public int range = 64;
	public boolean boxes = true;
	public boolean tracers = true;
	public float lineWidth = 2.0f;
	public float alpha = 1.0f;
	// colores RGB (0xRRGGBB)
	public int chest = 0xFFA500;
	public int trapped = 0xFF3030;
	public int ender = 0xB040FF;
	public int barrel = 0x8B5A2B;
	public int shulker = 0x40E0FF;
	// chunk finder
	public boolean chunkFinder = true;
	public int chunkMin = 3;       // minimo de contenedores para marcar un chunk
	public int chunkRange = 10;    // radio en chunks
	// jugadores
	public boolean playerAlert = true;
	public int alertChunks = 9;
	public boolean invisBoxes = true;
	public int invis = 0xFFFFFF;

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				VPConfig c = GSON.fromJson(Files.readString(FILE), VPConfig.class);
				if (c != null) I = c;
			}
		} catch (Exception e) {
			System.err.println("[VisionPlus] No se pudo leer la config: " + e);
		}
		save();
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(I));
		} catch (Exception e) {
			System.err.println("[VisionPlus] No se pudo guardar la config: " + e);
		}
	}
}
