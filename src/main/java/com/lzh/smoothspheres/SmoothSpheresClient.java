package com.lzh.smoothspheres;

import com.lzh.smoothspheres.client.model.SmoothSphereBakedModel;
import com.lzh.smoothspheres.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class SmoothSpheresClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLOWING_CRYSTAL_SPHERE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BLUE_GLASS_SPHERE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CLEAR_GLASS_SPHERE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LUMINOUS_GLASS_SPHERE, RenderLayer.getTranslucent());
        SmoothSphereBakedModel.registerModelPlugin();
    }
}
