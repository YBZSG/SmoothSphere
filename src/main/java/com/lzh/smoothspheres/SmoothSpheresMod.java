package com.lzh.smoothspheres;

import com.lzh.smoothspheres.registry.ModBlocks;
import com.lzh.smoothspheres.registry.ModEntities;
import com.lzh.smoothspheres.registry.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class SmoothSpheresMod implements ModInitializer {
    public static final String MOD_ID = "smooth_spheres";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModEntities.initialize();
        ModItemGroups.initialize();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
