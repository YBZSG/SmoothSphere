package com.lzh.smoothspheres.registry;

import com.lzh.smoothspheres.SmoothSpheresMod;
import com.lzh.smoothspheres.entity.PhysicsSphereEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<PhysicsSphereEntity> PHYSICS_SPHERE = registerPhysicsSphere();

    private ModEntities() {
    }

    public static void initialize() {
    }

    private static EntityType<PhysicsSphereEntity> registerPhysicsSphere() {
        Identifier id = SmoothSpheresMod.id("physics_sphere");
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        EntityType<PhysicsSphereEntity> type = EntityType.Builder
                .<PhysicsSphereEntity>create(PhysicsSphereEntity::new, SpawnGroup.MISC)
                .dimensions(1.0F, 1.0F)
                .maxTrackingRange(10)
                .trackingTickInterval(1)
                .build(key);
        return Registry.register(Registries.ENTITY_TYPE, id, type);
    }
}
