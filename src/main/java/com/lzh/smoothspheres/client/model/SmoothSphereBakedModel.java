package com.lzh.smoothspheres.client.model;

import com.lzh.smoothspheres.SmoothSpheresMod;
import com.lzh.smoothspheres.client.config.SmoothSpheresConfig;
import com.lzh.smoothspheres.registry.ModBlocks;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SmoothSphereBakedModel implements BakedModel {
    private static final float CENTER = 0.5F;
    private static final float RADIUS = 0.5F;
    private static final Map<SmoothSpheresConfig.Quality, SphereQuad[]> GEOMETRY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, SphereMaterial> MATERIALS = Map.of(
            SmoothSpheresMod.id("polished_metal_sphere"), SphereMaterial.POLISHED_METAL,
            SmoothSpheresMod.id("glowing_crystal_sphere"), SphereMaterial.GLOWING_CRYSTAL,
            SmoothSpheresMod.id("obsidian_black_sphere"), SphereMaterial.OBSIDIAN_BLACK,
            SmoothSpheresMod.id("white_ceramic_sphere"), SphereMaterial.WHITE_CERAMIC,
            SmoothSpheresMod.id("blue_glass_sphere"), SphereMaterial.BLUE_GLASS,
            SmoothSpheresMod.id("clear_glass_sphere"), SphereMaterial.CLEAR_GLASS,
            SmoothSpheresMod.id("frosted_glass_sphere"), SphereMaterial.FROSTED_GLASS,
            SmoothSpheresMod.id("luminous_glass_sphere"), SphereMaterial.LUMINOUS_GLASS,
            SmoothSpheresMod.id("chrome_metal_sphere"), SphereMaterial.CHROME_METAL
    );
    private static final Set<Block> SPHERE_BLOCKS = Set.of(
            ModBlocks.POLISHED_METAL_SPHERE,
            ModBlocks.GLOWING_CRYSTAL_SPHERE,
            ModBlocks.OBSIDIAN_BLACK_SPHERE,
            ModBlocks.WHITE_CERAMIC_SPHERE,
            ModBlocks.BLUE_GLASS_SPHERE,
            ModBlocks.CLEAR_GLASS_SPHERE,
            ModBlocks.FROSTED_GLASS_SPHERE,
            ModBlocks.LUMINOUS_GLASS_SPHERE,
            ModBlocks.CHROME_METAL_SPHERE
    );

    private final BakedModel original;
    private final List<BakedQuad> quads;
    private final Sprite particleSprite;

    private SmoothSphereBakedModel(BakedModel original, SphereMaterial material) {
        this.original = original;
        this.particleSprite = original.getParticleSprite();
        this.quads = bakeQuads(material, particleSprite, SmoothSpheresConfig.get().quality());
    }

    public static void registerModelPlugin() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register((model, modifierContext) -> {
            ModelIdentifier modelId = modifierContext.id();
            SphereMaterial material = MATERIALS.get(modelId.id());
            if (material == null || !"".equals(modelId.variant())) {
                return model;
            }
            return new SmoothSphereBakedModel(model, material);
        }));
    }

    public static boolean isSphereBlock(Block block) {
        return SPHERE_BLOCKS.contains(block);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return face == null ? quads : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return particleSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return original.getTransformation();
    }

    private static List<BakedQuad> bakeQuads(SphereMaterial material, Sprite sprite, SmoothSpheresConfig.Quality quality) {
        return java.util.Arrays.stream(geometry(quality))
                .map(quad -> quad.bake(material, sprite))
                .toList();
    }

    private static SphereQuad[] geometry(SmoothSpheresConfig.Quality quality) {
        return GEOMETRY_CACHE.computeIfAbsent(quality, SmoothSphereBakedModel::buildGeometry);
    }

    private static SphereQuad[] buildGeometry(SmoothSpheresConfig.Quality quality) {
        int latitudeSegments = quality.latitudeSegments();
        int longitudeSegments = quality.longitudeSegments();
        SphereQuad[] quads = new SphereQuad[latitudeSegments * longitudeSegments];
        int index = 0;

        for (int lat = 0; lat < latitudeSegments; lat++) {
            float theta0 = (float) Math.PI * lat / latitudeSegments;
            float theta1 = (float) Math.PI * (lat + 1) / latitudeSegments;

            for (int lon = 0; lon < longitudeSegments; lon++) {
                float phi0 = (float) (Math.PI * 2.0D * lon / longitudeSegments);
                float phi1 = (float) (Math.PI * 2.0D * (lon + 1) / longitudeSegments);
                SphereVertex v00 = vertex(theta0, phi0);
                SphereVertex v10 = vertex(theta1, phi0);
                SphereVertex v11 = vertex(theta1, phi1);
                SphereVertex v01 = vertex(theta0, phi1);
                quads[index++] = new SphereQuad(v00, v10, v11, v01);
            }
        }

        return quads;
    }

    private static SphereVertex vertex(float theta, float phi) {
        float sinTheta = (float) Math.sin(theta);
        float normalX = (float) Math.cos(phi) * sinTheta;
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) Math.sin(phi) * sinTheta;
        return new SphereVertex(
                CENTER + normalX * RADIUS,
                CENTER + normalY * RADIUS,
                CENTER + normalZ * RADIUS,
                normalX,
                normalY,
                normalZ,
                phi / ((float) Math.PI * 2.0F),
                theta / (float) Math.PI
        );
    }

    private record SphereQuad(SphereVertex v00, SphereVertex v10, SphereVertex v11, SphereVertex v01) {
        private BakedQuad bake(SphereMaterial material, Sprite sprite) {
            Direction face = Direction.getFacing(
                    (v00.normalX + v10.normalX + v11.normalX + v01.normalX) * 0.25F,
                    (v00.normalY + v10.normalY + v11.normalY + v01.normalY) * 0.25F,
                    (v00.normalZ + v10.normalZ + v11.normalZ + v01.normalZ) * 0.25F
            );
            int[] vertexData = new int[32];
            packVertex(vertexData, 0, v00, material, sprite);
            packVertex(vertexData, 1, v10, material, sprite);
            packVertex(vertexData, 2, v11, material, sprite);
            packVertex(vertexData, 3, v01, material, sprite);
            return new BakedQuad(vertexData, -1, face, sprite, false, material.lightEmission);
        }
    }

    private static void packVertex(int[] data, int vertexIndex, SphereVertex vertex, SphereMaterial material, Sprite sprite) {
        int offset = vertexIndex * 8;
        int color = material.color(vertex);
        data[offset] = Float.floatToRawIntBits(vertex.x);
        data[offset + 1] = Float.floatToRawIntBits(vertex.y);
        data[offset + 2] = Float.floatToRawIntBits(vertex.z);
        data[offset + 3] = packAbgr(color);
        data[offset + 4] = Float.floatToRawIntBits(sprite.getFrameU(vertex.u));
        data[offset + 5] = Float.floatToRawIntBits(sprite.getFrameV(vertex.v));
    }

    private static int packAbgr(int argb) {
        int alpha = (argb >> 24) & 255;
        int red = (argb >> 16) & 255;
        int green = (argb >> 8) & 255;
        int blue = argb & 255;
        return alpha << 24 | blue << 16 | green << 8 | red;
    }

    private record SphereVertex(float x, float y, float z, float normalX, float normalY, float normalZ, float u, float v) {
    }

    private enum SphereMaterial {
        POLISHED_METAL(62, 66, 70, 152, 160, 166, 232, 238, 242, 255, 0, 0.42F, 96.0F, 0.12F, 0.72F, 0.01F),
        GLOWING_CRYSTAL(42, 56, 138, 112, 170, 245, 230, 250, 255, 115, 14, 0.24F, 54.0F, 0.36F, 0.58F, 0.04F),
        OBSIDIAN_BLACK(2, 2, 7, 14, 12, 24, 82, 54, 128, 255, 0, 0.18F, 76.0F, 0.10F, 0.62F, 0.015F),
        WHITE_CERAMIC(166, 164, 156, 232, 230, 220, 246, 244, 235, 255, 0, 0.035F, 22.0F, 0.025F, 0.78F, 0.018F),
        BLUE_GLASS(22, 78, 156, 62, 166, 245, 212, 244, 255, 80, 0, 0.48F, 118.0F, 0.44F, 0.48F, 0.0F),
        CLEAR_GLASS(128, 170, 184, 198, 240, 255, 255, 255, 255, 45, 0, 0.50F, 124.0F, 0.36F, 0.42F, 0.0F),
        FROSTED_GLASS(135, 178, 184, 186, 228, 226, 232, 255, 248, 110, 0, 0.075F, 28.0F, 0.20F, 0.55F, 0.09F),
        LUMINOUS_GLASS(72, 180, 150, 150, 255, 220, 255, 255, 245, 90, 15, 0.20F, 42.0F, 0.34F, 0.48F, 0.025F),
        CHROME_METAL(28, 32, 36, 156, 168, 176, 255, 255, 255, 255, 0, 0.80F, 142.0F, 0.18F, 0.62F, 0.0F);

        private final int shadowRed;
        private final int shadowGreen;
        private final int shadowBlue;
        private final int baseRed;
        private final int baseGreen;
        private final int baseBlue;
        private final int highlightRed;
        private final int highlightGreen;
        private final int highlightBlue;
        private final int alpha;
        private final int lightEmission;
        private final float specularStrength;
        private final float specularPower;
        private final float rimStrength;
        private final float diffuseStrength;
        private final float surfaceNoise;

        SphereMaterial(
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
                int lightEmission,
                float specularStrength,
                float specularPower,
                float rimStrength,
                float diffuseStrength,
                float surfaceNoise
        ) {
            this.shadowRed = shadowRed;
            this.shadowGreen = shadowGreen;
            this.shadowBlue = shadowBlue;
            this.baseRed = baseRed;
            this.baseGreen = baseGreen;
            this.baseBlue = baseBlue;
            this.highlightRed = highlightRed;
            this.highlightGreen = highlightGreen;
            this.highlightBlue = highlightBlue;
            this.alpha = alpha;
            this.lightEmission = lightEmission;
            this.specularStrength = specularStrength;
            this.specularPower = specularPower;
            this.rimStrength = rimStrength;
            this.diffuseStrength = diffuseStrength;
            this.surfaceNoise = surfaceNoise;
        }

        private int color(SphereVertex vertex) {
            int red = shade(shadowRed, baseRed, highlightRed, vertex);
            int green = shade(shadowGreen, baseGreen, highlightGreen, vertex);
            int blue = shade(shadowBlue, baseBlue, highlightBlue, vertex);
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        private int shade(int shadow, int base, int highlight, SphereVertex vertex) {
            float diffuse = Math.max(0.0F, vertex.normalX * -0.45F + vertex.normalY * 0.72F + vertex.normalZ * -0.53F);
            float facing = Math.max(0.0F, -vertex.normalZ);
            float rim = (float) Math.pow(1.0F - facing, 2.4F) * rimStrength;
            float specAngle = Math.max(0.0F, vertex.normalX * -0.26F + vertex.normalY * 0.42F + vertex.normalZ * -0.87F);
            float specular = (float) Math.pow(specAngle, specularPower) * specularStrength;
            float noise = ((float) Math.sin(vertex.normalX * 43.0F + vertex.normalY * 61.0F + vertex.normalZ * 29.0F) * 0.5F + 0.5F) * surfaceNoise;
            float baseMix = clamp(0.34F + diffuse * diffuseStrength + rim * 0.12F + noise);
            float value = mix(shadow, base, baseMix);
            return Math.round(mix(value, highlight, clamp(specular + rim * 0.72F)));
        }
    }

    private static float mix(float a, float b, float amount) {
        return a + (b - a) * clamp(amount);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
