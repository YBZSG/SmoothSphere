package com.lzh.smoothspheres.registry;

import com.lzh.smoothspheres.SmoothSpheresMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    private ModItemGroups() {
    }

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, SmoothSpheresMod.id("smooth_spheres"), FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.smooth_spheres.smooth_spheres"))
                .icon(() -> new ItemStack(ModBlocks.POLISHED_METAL_SPHERE))
                .entries((displayContext, entries) -> {
                    entries.add(ModBlocks.POLISHED_METAL_SPHERE);
                    entries.add(ModBlocks.GLOWING_CRYSTAL_SPHERE);
                    entries.add(ModBlocks.OBSIDIAN_BLACK_SPHERE);
                    entries.add(ModBlocks.WHITE_CERAMIC_SPHERE);
                    entries.add(ModBlocks.BLUE_GLASS_SPHERE);
                    entries.add(ModBlocks.CLEAR_GLASS_SPHERE);
                    entries.add(ModBlocks.FROSTED_GLASS_SPHERE);
                    entries.add(ModBlocks.LUMINOUS_GLASS_SPHERE);
                    entries.add(ModBlocks.CHROME_METAL_SPHERE);
                })
                .build());
    }
}
