package com.lzh.smoothspheres.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lzh.smoothspheres.SmoothSpheresMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class SmoothSpheresConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(SmoothSpheresMod.MOD_ID + ".json");
    private static SmoothSpheresConfig INSTANCE;

    private Quality quality = Quality.HIGH;
    private boolean physicsEnabled = true;

    private SmoothSpheresConfig() {
    }

    public static SmoothSpheresConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public Quality quality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public boolean physicsEnabled() {
        return physicsEnabled;
    }

    public void setPhysicsEnabled(boolean physicsEnabled) {
        this.physicsEnabled = physicsEnabled;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save Smooth Spheres config", exception);
        }
    }

    private static SmoothSpheresConfig load() {
        if (!Files.exists(PATH)) {
            SmoothSpheresConfig config = new SmoothSpheresConfig();
            config.save();
            return config;
        }

        try {
            SmoothSpheresConfig config = GSON.fromJson(Files.readString(PATH), SmoothSpheresConfig.class);
            if (config == null || config.quality == null) {
                config = new SmoothSpheresConfig();
            }
            return config;
        } catch (Exception exception) {
            SmoothSpheresConfig config = new SmoothSpheresConfig();
            config.save();
            return config;
        }
    }

    public enum Quality {
        BALANCED(64, 128, "balanced"),
        HIGH(96, 192, "high"),
        ULTRA(128, 256, "ultra");

        private final int latitudeSegments;
        private final int longitudeSegments;
        private final String id;

        Quality(int latitudeSegments, int longitudeSegments, String id) {
            this.latitudeSegments = latitudeSegments;
            this.longitudeSegments = longitudeSegments;
            this.id = id;
        }

        public int latitudeSegments() {
            return latitudeSegments;
        }

        public int longitudeSegments() {
            return longitudeSegments;
        }

        public String translationKey() {
            return "config.smooth_spheres.quality." + id;
        }

        public Quality next() {
            Quality[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static Quality fromString(String value) {
            if (value == null) {
                return HIGH;
            }
            try {
                return Quality.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return HIGH;
            }
        }
    }
}
