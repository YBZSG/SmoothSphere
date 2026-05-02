package com.lzh.smoothspheres;

import com.lzh.smoothspheres.client.render.SphereBlockEntityRenderer;
import com.lzh.smoothspheres.registry.ModBlockEntities;
import com.lzh.smoothspheres.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class SmoothSpheresClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLOWING_CRYSTAL_SPHERE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BLUE_GLASS_SPHERE, RenderLayer.getTranslucent());
        BlockEntityRendererRegistry.register(ModBlockEntities.SPHERE_BLOCK_ENTITY, SphereBlockEntityRenderer::new);
    }
}
