package com.lzh.smoothspheres.entity;

import com.lzh.smoothspheres.registry.ModBlocks;
import com.lzh.smoothspheres.registry.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PhysicsSphereEntity extends Entity {
    private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(
            PhysicsSphereEntity.class,
            TrackedDataHandlerRegistry.BLOCK_STATE
    );
    private static final double GRAVITY = 0.045D;
    private static final double GROUND_FRICTION = 0.982D;
    private static final double AIR_DRAG = 0.995D;
    private static final double BOUNCE = 0.36D;
    private static final double BODY_PUSH = 0.18D;
    private static final double HIT_PUSH = 0.86D;
    private static final double RIGHT_CLICK_PUSH = 1.15D;

    public float rollX;
    public float rollZ;
    public float previousRollX;
    public float previousRollZ;

    public PhysicsSphereEntity(EntityType<? extends PhysicsSphereEntity> type, World world) {
        super(type, world);
    }

    public PhysicsSphereEntity(World world, BlockPos pos, BlockState state) {
        this(ModEntities.PHYSICS_SPHERE, world);
        setBlockState(state);
        setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(BLOCK_STATE, ModBlocks.POLISHED_METAL_SPHERE.getDefaultState());
    }

    public BlockState getBlockState() {
        return getDataTracker().get(BLOCK_STATE);
    }

    public void setBlockState(BlockState state) {
        getDataTracker().set(BLOCK_STATE, state);
    }

    @Override
    public void tick() {
        super.tick();
        previousRollX = rollX;
        previousRollZ = rollZ;

        Vec3d velocity = getVelocity();
        if (!hasNoGravity()) {
            velocity = velocity.add(0.0D, -GRAVITY, 0.0D);
        }

        move(MovementType.SELF, velocity);
        pushTouchingEntities();

        if (horizontalCollision) {
            velocity = new Vec3d(-velocity.x * BOUNCE, velocity.y, -velocity.z * BOUNCE);
        }

        if (verticalCollision && velocity.y < 0.0D) {
            velocity = new Vec3d(velocity.x, -velocity.y * BOUNCE, velocity.z);
        }

        double drag = isOnGround() ? GROUND_FRICTION : AIR_DRAG;
        velocity = new Vec3d(velocity.x * drag, velocity.y * 0.98D, velocity.z * drag);
        if (isOnGround() && Math.abs(velocity.y) < 0.045D) {
            velocity = new Vec3d(velocity.x, 0.0D, velocity.z);
        }

        setVelocity(velocity);
        velocityModified = true;

        rollX += (float) (velocity.z * 74.0D);
        rollZ -= (float) (velocity.x * 74.0D);

        if (!getWorld().isClient() && (age > 20 * 60 * 5 || getY() < getWorld().getBottomY() - 16)) {
            discard();
        }
    }

    private void pushTouchingEntities() {
        Box box = getBoundingBox().expand(0.08D);
        for (Entity other : getWorld().getOtherEntities(this, box, Entity::isPushable)) {
            pushAwayFrom(other);
            Vec3d direction = getPos().subtract(other.getPos());
            if (direction.horizontalLengthSquared() > 1.0E-5D) {
                Vec3d impulse = direction.normalize().multiply(BODY_PUSH);
                addVelocity(impulse.x, 0.035D, impulse.z);
                other.addVelocity(-impulse.x * 0.35D, 0.0D, -impulse.z * 0.35D);
                velocityModified = true;
            }
        }
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return other.isPushable();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }

        launchFrom(player, RIGHT_CLICK_PUSH, 0.34D);
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Block.dropStack(world, getBlockPos(), new ItemStack(getBlockState().getBlock()));
        discard();
        return true;
    }

    private void launchFrom(Entity source, double strength, double lift) {
        Vec3d direction = getPos().subtract(source.getPos());
        if (direction.horizontalLengthSquared() < 1.0E-5D) {
            direction = source.getRotationVec(1.0F);
        }
        launch(direction, strength, lift);
    }

    private void launch(Vec3d direction, double strength, double lift) {
        Vec3d horizontal = new Vec3d(direction.x, 0.0D, direction.z);
        if (horizontal.horizontalLengthSquared() < 1.0E-5D) {
            horizontal = new Vec3d(0.0D, 0.0D, 1.0D);
        }

        Vec3d impulse = horizontal.normalize().multiply(strength);
        addVelocity(impulse.x, lift, impulse.z);
        velocityModified = true;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        Identifier id = Registries.BLOCK.getId(getBlockState().getBlock());
        nbt.putString("Block", id.toString());
        nbt.putFloat("RollX", rollX);
        nbt.putFloat("RollZ", rollZ);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        Identifier id = Identifier.tryParse(nbt.getString("Block"));
        if (id != null && Registries.BLOCK.containsId(id)) {
            setBlockState(Registries.BLOCK.get(id).getDefaultState());
        }
        rollX = nbt.getFloat("RollX");
        rollZ = nbt.getFloat("RollZ");
        previousRollX = rollX;
        previousRollZ = rollZ;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket(this, entityTrackerEntry);
    }
}
