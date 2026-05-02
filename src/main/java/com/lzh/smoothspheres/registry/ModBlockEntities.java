package com.lzh.smoothspheres.registry;

import com.lzh.smoothspheres.SmoothSpheresMod;
import com.lzh.smoothspheres.block.SphereBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    public static final BlockEntityType<SphereBlockEntity> SPHERE_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            SmoothSpheresMod.id("sphere"),
            FabricBlockEntityTypeBuilder.create(
                    SphereBlockEntity::new,
                    ModBlocks.POLISHED_METAL_SPHERE,
                    ModBlocks.GLOWING_CRYSTAL_SPHERE,
                    ModBlocks.OBSIDIAN_BLACK_SPHERE,
                    ModBlocks.WHITE_CERAMIC_SPHERE,
                    ModBlocks.BLUE_GLASS_SPHERE,
                    ModBlocks.CLEAR_GLASS_SPHERE,
                    ModBlocks.LUMINOUS_GLASS_SPHERE,
                    ModBlocks.CHROME_METAL_SPHERE
            ).build()
    );

    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}
