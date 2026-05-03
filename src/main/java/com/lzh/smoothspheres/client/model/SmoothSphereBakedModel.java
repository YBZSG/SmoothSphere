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
    private static final float RADIUS = 0.4375F;
    private static final Map<SmoothSpheresConfig.Quality, SphereQuad[]> GEOMETRY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, SphereMaterial> MATERIALS = Map.of(
            SmoothSpheresMod.id("polished_metal_sphere"), SphereMaterial.POLISHED_METAL,
            SmoothSpheresMod.id("glowing_crystal_sphere"), SphereMaterial.GLOWING_CRYSTAL,
            SmoothSpheresMod.id("obsidian_black_sphere"), SphereMaterial.OBSIDIAN_BLACK,
            SmoothSpheresMod.id("white_ceramic_sphere"), SphereMaterial.WHITE_CERAMIC,
            SmoothSpheresMod.id("blue_glass_sphere"), SphereMaterial.BLUE_GLASS,
            SmoothSpheresMod.id("clear_glass_sphere"), SphereMaterial.CLEAR_GLASS,
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
        return true;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
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
                normalZ
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
            return new BakedQuad(vertexData, -1, face, sprite, true, material.lightEmission);
        }
    }

    private static void packVertex(int[] data, int vertexIndex, SphereVertex vertex, SphereMaterial material, Sprite sprite) {
        int offset = vertexIndex * 8;
        int color = material.color(vertex);
        data[offset] = Float.floatToRawIntBits(vertex.x);
        data[offset + 1] = Float.floatToRawIntBits(vertex.y);
        data[offset + 2] = Float.floatToRawIntBits(vertex.z);
        data[offset + 3] = packAbgr(color);
        data[offset + 4] = Float.floatToRawIntBits(sprite.getFrameU(0.5F));
        data[offset + 5] = Float.floatToRawIntBits(sprite.getFrameV(0.5F));
    }

    private static int packAbgr(int rgb) {
        int red = (rgb >> 16) & 255;
        int green = (rgb >> 8) & 255;
        int blue = rgb & 255;
        return 255 << 24 | blue << 16 | green << 8 | red;
    }

    private record SphereVertex(float x, float y, float z, float normalX, float normalY, float normalZ) {
    }

    private enum SphereMaterial {
        POLISHED_METAL(58, 63, 67, 168, 174, 178, 245, 250, 255, 0, 1.0F, 96.0F, 0.20F),
        GLOWING_CRYSTAL(48, 88, 180, 130, 196, 255, 248, 255, 255, 14, 1.2F, 128.0F, 0.58F),
        OBSIDIAN_BLACK(3, 2, 8, 18, 16, 28, 126, 74, 180, 0, 0.95F, 72.0F, 0.24F),
        WHITE_CERAMIC(150, 146, 136, 232, 230, 220, 255, 255, 250, 0, 0.22F, 36.0F, 0.08F),
        BLUE_GLASS(20, 74, 150, 58, 160, 245, 225, 248, 255, 0, 1.2F, 128.0F, 0.58F),
        CLEAR_GLASS(112, 160, 178, 190, 238, 255, 255, 255, 255, 0, 1.2F, 128.0F, 0.58F),
        LUMINOUS_GLASS(72, 180, 150, 150, 255, 220, 255, 255, 245, 15, 1.2F, 128.0F, 0.58F),
        CHROME_METAL(38, 42, 46, 172, 184, 190, 255, 255, 255, 0, 1.0F, 96.0F, 0.20F);

        private final int shadowRed;
        private final int shadowGreen;
        private final int shadowBlue;
        private final int baseRed;
        private final int baseGreen;
        private final int baseBlue;
        private final int highlightRed;
        private final int highlightGreen;
        private final int highlightBlue;
        private final int lightEmission;
        private final float specularStrength;
        private final float specularPower;
        private final float rimStrength;

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
                int lightEmission,
                float specularStrength,
                float specularPower,
                float rimStrength
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
            this.lightEmission = lightEmission;
            this.specularStrength = specularStrength;
            this.specularPower = specularPower;
            this.rimStrength = rimStrength;
        }

        private int color(SphereVertex vertex) {
            int red = shade(shadowRed, baseRed, highlightRed, vertex);
            int green = shade(shadowGreen, baseGreen, highlightGreen, vertex);
            int blue = shade(shadowBlue, baseBlue, highlightBlue, vertex);
            return red << 16 | green << 8 | blue;
        }

        private int shade(int shadow, int base, int highlight, SphereVertex vertex) {
            float diffuse = Math.max(0.0F, vertex.normalX * -0.45F + vertex.normalY * 0.72F + vertex.normalZ * -0.53F);
            float facing = Math.max(0.0F, -vertex.normalZ);
            float rim = (float) Math.pow(1.0F - facing, 2.4F) * rimStrength;
            float specAngle = Math.max(0.0F, vertex.normalX * -0.26F + vertex.normalY * 0.42F + vertex.normalZ * -0.87F);
            float specular = (float) Math.pow(specAngle, specularPower) * specularStrength;
            float baseMix = clamp(0.30F + diffuse * 0.62F + rim * 0.18F);
            float value = mix(shadow, base, baseMix);
            return Math.round(mix(value, highlight, clamp(specular + rim)));
        }
    }

    private static float mix(float a, float b, float amount) {
        return a + (b - a) * clamp(amount);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
