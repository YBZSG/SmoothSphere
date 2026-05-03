package com.lzh.smoothspheres.block;

import com.mojang.serialization.MapCodec;
import com.lzh.smoothspheres.entity.PhysicsSphereEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SphereBlock extends Block {
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
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SPHERE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SPHERE_SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        PhysicsSphereEntity entity = new PhysicsSphereEntity(world, pos, state);
        Vec3d push = player.getRotationVec(1.0F).multiply(0.42D);
        entity.setVelocity(push.x, Math.max(0.22D, push.y + 0.18D), push.z);
        world.removeBlock(pos, false);
        world.spawnEntity(entity);
        player.sendMessage(Text.literal("Sphere physics enabled"), true);
        return ActionResult.SUCCESS_SERVER;
    }
}
