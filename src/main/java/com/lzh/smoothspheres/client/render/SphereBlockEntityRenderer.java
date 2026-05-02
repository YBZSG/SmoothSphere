package com.lzh.smoothspheres.client.render;

import com.lzh.smoothspheres.SmoothSpheresMod;
import com.lzh.smoothspheres.block.SphereBlockEntity;
import com.lzh.smoothspheres.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SphereBlockEntityRenderer implements BlockEntityRenderer<SphereBlockEntity> {
    private static final int LATITUDE_SEGMENTS = 96;
    private static final int LONGITUDE_SEGMENTS = 192;
    private static final float RADIUS = 0.4375F;
    private static final Identifier SURFACE_TEXTURE = SmoothSpheresMod.id("textures/block/sphere_surface.png");
    private static final float LIGHT_X = -0.45F;
    private static final float LIGHT_Y = 0.72F;
    private static final float LIGHT_Z = -0.53F;

    public SphereBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(SphereBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SphereMaterial material = SphereMaterial.of(entity.getCachedState().getBlock());
        VertexConsumer vertices = vertexConsumers.getBuffer(material.renderLayer());
        int renderLight = material.emissive() ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;

        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);

        for (int lat = 0; lat < LATITUDE_SEGMENTS; lat++) {
            float theta0 = (float) Math.PI * lat / LATITUDE_SEGMENTS;
            float theta1 = (float) Math.PI * (lat + 1) / LATITUDE_SEGMENTS;

            for (int lon = 0; lon < LONGITUDE_SEGMENTS; lon++) {
                float phi0 = (float) (Math.PI * 2.0D * lon / LONGITUDE_SEGMENTS);
                float phi1 = (float) (Math.PI * 2.0D * (lon + 1) / LONGITUDE_SEGMENTS);

                SphereVertex v00 = vertex(theta0, phi0, lon, lat);
                SphereVertex v10 = vertex(theta1, phi0, lon, lat + 1);
                SphereVertex v11 = vertex(theta1, phi1, lon + 1, lat + 1);
                SphereVertex v01 = vertex(theta0, phi1, lon + 1, lat);

                drawVertex(vertices, matrices, v00, material, renderLight);
                drawVertex(vertices, matrices, v10, material, renderLight);
                drawVertex(vertices, matrices, v11, material, renderLight);
                drawVertex(vertices, matrices, v01, material, renderLight);
            }
        }

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(SphereBlockEntity blockEntity) {
        return false;
    }

    private static SphereVertex vertex(float theta, float phi, int lon, int lat) {
        float sinTheta = (float) Math.sin(theta);
        float x = (float) Math.cos(phi) * sinTheta;
        float y = (float) Math.cos(theta);
        float z = (float) Math.sin(phi) * sinTheta;
        float u = (float) lon / LONGITUDE_SEGMENTS;
        float v = (float) lat / LATITUDE_SEGMENTS;
        return new SphereVertex(x, y, z, u, v);
    }

    private static void drawVertex(VertexConsumer vertices, MatrixStack matrices, SphereVertex vertex, SphereMaterial material, int light) {
        int red = shade(material.shadowRed(), material.baseRed(), material.highlightRed(), vertex, material);
        int green = shade(material.shadowGreen(), material.baseGreen(), material.highlightGreen(), vertex, material);
        int blue = shade(material.shadowBlue(), material.baseBlue(), material.highlightBlue(), vertex, material);

        vertices.vertex(matrices.peek(), vertex.x() * RADIUS, vertex.y() * RADIUS, vertex.z() * RADIUS)
                .color(red, green, blue, material.alpha())
                .texture(0.5F, 0.5F)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrices.peek(), vertex.x(), vertex.y(), vertex.z());
    }

    private static int shade(int shadow, int base, int highlight, SphereVertex vertex, SphereMaterial material) {
        float diffuse = Math.max(0.0F, vertex.x() * LIGHT_X + vertex.y() * LIGHT_Y + vertex.z() * LIGHT_Z);
        float facing = Math.max(0.0F, -vertex.z());
        float rim = (float) Math.pow(1.0F - facing, 2.4F) * material.rimStrength();
        float halfX = -0.26F;
        float halfY = 0.42F;
        float halfZ = -0.87F;
        float specAngle = Math.max(0.0F, vertex.x() * halfX + vertex.y() * halfY + vertex.z() * halfZ);
        float specular = (float) Math.pow(specAngle, material.specularPower()) * material.specularStrength();
        float baseMix = clamp(0.30F + diffuse * 0.62F + rim * 0.18F);
        float value = mix(shadow, base, baseMix);
        return Math.round(mix(value, highlight, clamp(specular + rim)));
    }

    private static float mix(float a, float b, float amount) {
        return a + (b - a) * clamp(amount);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private record SphereVertex(float x, float y, float z, float u, float v) {
    }

    private record SphereMaterial(
            int shadowRed,
            int shadowGreen,
            int shadowBlue,
            int baseRed,
            int baseGreen,
            int baseBlue,
            int highlightRed,
            int highlightGreen,
            int highlightBlue,
            int alpha,
            boolean translucent,
            boolean emissive,
            float specularStrength,
            float specularPower,
            float rimStrength
    ) {
        private static SphereMaterial of(Block block) {
            if (block == ModBlocks.GLOWING_CRYSTAL_SPHERE) {
                return glass(48, 88, 180, 130, 196, 255, 248, 255, 255, 150, true);
            }
            if (block == ModBlocks.OBSIDIAN_BLACK_SPHERE) {
                return glossy(3, 2, 8, 18, 16, 28, 126, 74, 180, 255, 0.95F);
            }
            if (block == ModBlocks.WHITE_CERAMIC_SPHERE) {
                return matte(150, 146, 136, 232, 230, 220, 255, 255, 250);
            }
            if (block == ModBlocks.BLUE_GLASS_SPHERE) {
                return glass(20, 74, 150, 58, 160, 245, 225, 248, 255, 115, false);
            }
            if (block == ModBlocks.CLEAR_GLASS_SPHERE) {
                return glass(112, 160, 178, 190, 238, 255, 255, 255, 255, 92, false);
            }
            if (block == ModBlocks.LUMINOUS_GLASS_SPHERE) {
                return glass(72, 180, 150, 150, 255, 220, 255, 255, 245, 135, true);
            }
            if (block == ModBlocks.CHROME_METAL_SPHERE) {
                return metal(38, 42, 46, 172, 184, 190, 255, 255, 255);
            }
            return metal(58, 63, 67, 168, 174, 178, 245, 250, 255);
        }

        private static SphereMaterial metal(int sr, int sg, int sb, int br, int bg, int bb, int hr, int hg, int hb) {
            return new SphereMaterial(sr, sg, sb, br, bg, bb, hr, hg, hb, 255, false, false, 1.0F, 96.0F, 0.20F);
        }

        private static SphereMaterial glossy(int sr, int sg, int sb, int br, int bg, int bb, int hr, int hg, int hb, int alpha, float specular) {
            return new SphereMaterial(sr, sg, sb, br, bg, bb, hr, hg, hb, alpha, false, false, specular, 72.0F, 0.24F);
        }

        private static SphereMaterial matte(int sr, int sg, int sb, int br, int bg, int bb, int hr, int hg, int hb) {
            return new SphereMaterial(sr, sg, sb, br, bg, bb, hr, hg, hb, 255, false, false, 0.22F, 36.0F, 0.08F);
        }

        private static SphereMaterial glass(int sr, int sg, int sb, int br, int bg, int bb, int hr, int hg, int hb, int alpha, boolean emissive) {
            return new SphereMaterial(sr, sg, sb, br, bg, bb, hr, hg, hb, alpha, true, emissive, 1.2F, 128.0F, 0.58F);
        }

        private RenderLayer renderLayer() {
            return translucent ? RenderLayer.getEntityTranslucent(SURFACE_TEXTURE) : RenderLayer.getEntityCutoutNoCull(SURFACE_TEXTURE);
        }
    }
}
