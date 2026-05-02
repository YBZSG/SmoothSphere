package com.lzh.smoothspheres.registry;

import com.lzh.smoothspheres.SmoothSpheresMod;
import com.lzh.smoothspheres.block.SphereBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block POLISHED_METAL_SPHERE = registerSphere(
            "polished_metal_sphere",
            settings("polished_metal_sphere")
                    .strength(3.5F, 6.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL)
    );

    public static final Block GLOWING_CRYSTAL_SPHERE = registerSphere(
            "glowing_crystal_sphere",
            settings("glowing_crystal_sphere")
                    .strength(1.2F, 3.0F)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .luminance(state -> 10)
                    .nonOpaque()
    );

    public static final Block OBSIDIAN_BLACK_SPHERE = registerSphere(
            "obsidian_black_sphere",
            settings("obsidian_black_sphere")
                    .strength(25.0F, 600.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.DEEPSLATE)
    );

    public static final Block WHITE_CERAMIC_SPHERE = registerSphere(
            "white_ceramic_sphere",
            settings("white_ceramic_sphere")
                    .strength(1.5F, 4.0F)
                    .sounds(BlockSoundGroup.DECORATED_POT)
    );

    public static final Block BLUE_GLASS_SPHERE = registerSphere(
            "blue_glass_sphere",
            settings("blue_glass_sphere")
                    .strength(0.3F, 0.3F)
                    .sounds(BlockSoundGroup.GLASS)
                    .nonOpaque()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
    );

    public static final Block CLEAR_GLASS_SPHERE = registerSphere(
            "clear_glass_sphere",
            transparentSettings("clear_glass_sphere")
                    .strength(0.3F, 0.3F)
                    .sounds(BlockSoundGroup.GLASS)
    );

    public static final Block LUMINOUS_GLASS_SPHERE = registerSphere(
            "luminous_glass_sphere",
            transparentSettings("luminous_glass_sphere")
                    .strength(0.4F, 0.4F)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 14)
    );

    public static final Block CHROME_METAL_SPHERE = registerSphere(
            "chrome_metal_sphere",
            settings("chrome_metal_sphere")
                    .strength(4.0F, 7.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL)
    );

    private ModBlocks() {
    }

    public static void initialize() {
    }

    private static Block registerSphere(String name, AbstractBlock.Settings settings) {
        return register(name, SphereBlock::new, settings);
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Identifier id = SmoothSpheresMod.id(name);
        Block block = factory.apply(settings);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings().registryKey(itemKey(id))));
        return block;
    }

    private static AbstractBlock.Settings settings(String name) {
        Identifier id = SmoothSpheresMod.id(name);
        return AbstractBlock.Settings.create().registryKey(blockKey(id));
    }

    private static AbstractBlock.Settings transparentSettings(String name) {
        return settings(name)
                .nonOpaque()
                .allowsSpawning(Blocks::never)
                .solidBlock(Blocks::never)
                .suffocates(Blocks::never)
                .blockVision(Blocks::never);
    }

    private static RegistryKey<Block> blockKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    private static RegistryKey<Item> itemKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.ITEM, id);
    }
}
