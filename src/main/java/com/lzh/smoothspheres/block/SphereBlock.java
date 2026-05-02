package com.lzh.smoothspheres.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SphereBlock extends BlockWithEntity {
    public static final MapCodec<SphereBlock> CODEC = createCodec(SphereBlock::new);

    private static final VoxelShape SPHERE_SHAPE = VoxelShapes.union(
            createCuboidShape(5.0, 0.0, 5.0, 11.0, 1.0, 11.0),
            createCuboidShape(3.5, 1.0, 3.5, 12.5, 2.5, 12.5),
            createCuboidShape(2.5, 2.5, 2.5, 13.5, 4.0, 13.5),
            createCuboidShape(1.5, 4.0, 1.5, 14.5, 6.0, 14.5),
            createCuboidShape(1.0, 6.0, 1.0, 15.0, 10.0, 15.0),
            createCuboidShape(1.5, 10.0, 1.5, 14.5, 12.0, 14.5),
            createCuboidShape(2.5, 12.0, 2.5, 13.5, 13.5, 13.5),
            createCuboidShape(3.5, 13.5, 3.5, 12.5, 15.0, 12.5),
            createCuboidShape(5.0, 15.0, 5.0, 11.0, 16.0, 11.0)
    ).simplify();

    public SphereBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SphereBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SPHERE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SPHERE_SHAPE;
    }
}
