package com.lzh.smoothspheres.client.render;

import com.lzh.smoothspheres.entity.PhysicsSphereEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class PhysicsSphereEntityRenderer extends EntityRenderer<PhysicsSphereEntity, PhysicsSphereEntityRenderState> {
    public PhysicsSphereEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.48F;
    }

    @Override
    public PhysicsSphereEntityRenderState createRenderState() {
        return new PhysicsSphereEntityRenderState();
    }

    @Override
    public void updateRenderState(PhysicsSphereEntity entity, PhysicsSphereEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.blockState = entity.getBlockState();
        state.rollX = MathHelper.lerp(tickProgress, entity.previousRollX, entity.rollX);
        state.rollZ = MathHelper.lerp(tickProgress, entity.previousRollZ, entity.rollZ);
    }

    @Override
    public void render(PhysicsSphereEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (state.blockState == null) {
            return;
        }

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.rollX), 0.0F, 0.5F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(state.rollZ), 0.0F, 0.5F, 0.0F);
        matrices.translate(-0.5D, 0.0D, -0.5D);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state.blockState, matrices, vertexConsumers, light, 0);
        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }
}
