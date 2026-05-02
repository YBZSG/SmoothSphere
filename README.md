# Smooth Spheres

Smooth Spheres is a Fabric mod for Minecraft 1.21.4 that adds five decorative sphere blocks:

- Polished Metal Sphere
- Glowing Crystal Sphere
- Obsidian Black Sphere
- White Ceramic Sphere
- Blue Glass Sphere

The blocks use generated vanilla JSON models made from many thin cuboids, centered inside one block space at roughly 14x14x14 pixels. Collision and outline shapes use stacked cuboids so they feel closer to round blocks than full cubes.

## Build

```sh
./gradlew build
```

On Windows PowerShell:

```powershell
.\gradlew.bat build
```

The compiled mod jar will be written to `build/libs/`.

## Run Client

```sh
./gradlew runClient
```

On Windows PowerShell:

```powershell
.\gradlew.bat runClient
```

## PBR Textures

Texture placeholders live in:

`src/main/resources/assets/smooth_spheres/textures/block/`

Each sphere has:

- base color / albedo: `<name>.png`
- normal map: `<name>_n.png`
- roughness or specular-style map: `<name>_s.png`
- emissive map: `<name>_e.png`

For example:

`polished_metal_sphere.png`
`polished_metal_sphere_n.png`
`polished_metal_sphere_s.png`
`polished_metal_sphere_e.png`

Vanilla Minecraft does not support real PBR materials by itself. These files are named for common shader/resource-pack pipelines, and true PBR effects require Iris-compatible shaders or another compatible resource-pack pipeline. Without shaders, the normal vanilla texture still renders.

## Regenerating Models and Placeholder Textures

The helper script can regenerate model JSON, blockstates, 1.21.4 item model definitions, legacy item model files, language entries, and placeholder PNG texture maps:

```powershell
python scripts/generate_sphere_assets.py
```

The generated block models are written to:

`src/main/resources/assets/smooth_spheres/models/block/`

## Known Limitations

- Vanilla block model JSON only supports cuboids, so the visual spheres are carefully stepped approximations rather than mathematically smooth meshes.
- Real metallic, roughness, normal, and emissive behavior depends on shader support.
- The Blue Glass Sphere uses a translucent render layer on the client. If another renderer or shader pack handles translucency differently, it may fall back to vanilla-style alpha blending.
