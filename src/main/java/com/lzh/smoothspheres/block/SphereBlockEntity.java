package com.lzh.smoothspheres.block;

import com.lzh.smoothspheres.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class SphereBlockEntity extends BlockEntity {
    public SphereBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPHERE_BLOCK_ENTITY, pos, state);
    }
}
