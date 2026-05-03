from __future__ import annotations

import json
import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET_ROOT = ROOT / "src/main/resources/assets/smooth_spheres"
BLOCKSTATES = ASSET_ROOT / "blockstates"
BLOCK_MODELS = ASSET_ROOT / "models/block"
ITEM_MODELS = ASSET_ROOT / "models/item"
ITEM_DEFINITIONS = ASSET_ROOT / "items"
TEXTURES = ASSET_ROOT / "textures/block"
LANG = ASSET_ROOT / "lang"

SPHERES = {
    "polished_metal_sphere": {
        "name": "Polished Metal Sphere",
        "base": (152, 160, 166, 255),
        "highlight": (232, 238, 242, 255),
        "shadow": (62, 66, 70, 255),
        "normal": (128, 128, 255, 255),
        "spec": (165, 170, 176, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.45,
        "normal_noise": 0.01,
    },
    "glowing_crystal_sphere": {
        "name": "Glowing Crystal Sphere",
        "base": (112, 170, 245, 185),
        "highlight": (230, 250, 255, 220),
        "shadow": (42, 56, 138, 160),
        "normal": (128, 128, 255, 255),
        "spec": (74, 116, 170, 255),
        "emissive": (75, 90, 190, 255),
        "specular": 0.24,
        "normal_noise": 0.045,
    },
    "obsidian_black_sphere": {
        "name": "Obsidian Black Sphere",
        "base": (14, 12, 24, 255),
        "highlight": (82, 54, 128, 255),
        "shadow": (2, 2, 7, 255),
        "normal": (128, 128, 255, 255),
        "spec": (42, 35, 68, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.18,
        "normal_noise": 0.015,
    },
    "white_ceramic_sphere": {
        "name": "White Ceramic Sphere",
        "base": (230, 228, 220, 255),
        "highlight": (246, 244, 235, 255),
        "shadow": (166, 164, 156, 255),
        "normal": (128, 128, 255, 255),
        "spec": (18, 18, 18, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.035,
        "normal_noise": 0.02,
    },
    "blue_glass_sphere": {
        "name": "Blue Glass Sphere",
        "base": (62, 166, 245, 132),
        "highlight": (212, 244, 255, 180),
        "shadow": (22, 78, 156, 120),
        "normal": (128, 128, 255, 255),
        "spec": (118, 205, 245, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.5,
        "normal_noise": 0.0,
    },
    "clear_glass_sphere": {
        "name": "Clear Glass Sphere",
        "base": (190, 238, 255, 92),
        "highlight": (255, 255, 255, 180),
        "shadow": (128, 170, 184, 72),
        "normal": (128, 128, 255, 255),
        "spec": (170, 220, 240, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.52,
        "normal_noise": 0.0,
    },
    "frosted_glass_sphere": {
        "name": "Frosted Glass Sphere",
        "base": (186, 228, 226, 164),
        "highlight": (232, 255, 248, 178),
        "shadow": (135, 178, 184, 150),
        "normal": (128, 128, 255, 255),
        "spec": (30, 42, 42, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.075,
        "normal_noise": 0.1,
    },
    "luminous_glass_sphere": {
        "name": "Luminous Glass Sphere",
        "base": (150, 255, 220, 148),
        "highlight": (255, 255, 245, 200),
        "shadow": (72, 180, 150, 130),
        "normal": (128, 128, 255, 255),
        "spec": (70, 150, 130, 255),
        "emissive": (70, 205, 160, 255),
        "specular": 0.2,
        "normal_noise": 0.025,
    },
    "chrome_metal_sphere": {
        "name": "Chrome Metal Sphere",
        "base": (156, 168, 176, 255),
        "highlight": (255, 255, 255, 255),
        "shadow": (28, 32, 36, 255),
        "normal": (128, 128, 255, 255),
        "spec": (230, 240, 245, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.8,
        "normal_noise": 0.0,
    },
}


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def png_bytes(width: int, height: int, pixels: list[tuple[int, int, int, int]]) -> bytes:
    raw_rows = []
    for y in range(height):
        row = bytearray([0])
        for pixel in pixels[y * width:(y + 1) * width]:
            row.extend(pixel)
        raw_rows.append(bytes(row))
    raw = b"".join(raw_rows)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)) + chunk(
        b"IDAT", zlib.compress(raw, 9)
    ) + chunk(b"IEND", b"")


def mix(a: tuple[int, int, int, int], b: tuple[int, int, int, int], t: float) -> tuple[int, int, int, int]:
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))


def normalize(vector: tuple[float, float, float]) -> tuple[float, float, float]:
    length = math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2])
    return (vector[0] / length, vector[1] / length, vector[2] / length)


def dot(a: tuple[float, float, float], b: tuple[float, float, float]) -> float:
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def shaded_texture(info: dict[str, tuple[int, int, int, int]], kind: str, size: int = 512) -> bytes:
    pixels = []
    light_dir = normalize((-0.45, 0.72, -0.53))
    view_dir = normalize((0.0, 0.0, -1.0))
    half_dir = normalize((light_dir[0] + view_dir[0], light_dir[1] + view_dir[1], light_dir[2] + view_dir[2]))
    for y in range(size):
        v = y / (size - 1)
        theta = math.pi * v
        sin_theta = math.sin(theta)
        cos_theta = math.cos(theta)
        for x in range(size):
            u = x / (size - 1)
            phi = math.pi * 2.0 * u
            noise = math.sin(x * 12.9898 + y * 78.233) * math.sin(x * 4.173 + y * 19.19)
            normal_noise = info.get("normal_noise", 0.0)
            normal = normalize((
                math.cos(phi) * sin_theta + noise * normal_noise,
                cos_theta + math.sin(x * 0.31 + y * 0.17) * normal_noise * 0.65,
                math.sin(phi) * sin_theta + math.cos(x * 0.21 - y * 0.37) * normal_noise,
            ))
            diffuse = max(0.0, dot(normal, light_dir))
            fresnel = (1.0 - max(0.0, -normal[2])) ** 2.2
            specular = (max(0.0, dot(normal, half_dir)) ** 80) * info.get("specular", 0.35)

            if kind == "normal":
                pixels.append((
                    round((normal[0] * 0.5 + 0.5) * 255),
                    round((-normal[1] * 0.5 + 0.5) * 255),
                    round((normal[2] * 0.5 + 0.5) * 255),
                    255,
                ))
            elif kind == "spec":
                pixels.append(mix(info["spec"], info["highlight"], max(specular, fresnel * info.get("specular", 0.35) * 0.24)))
            elif kind == "emissive":
                glow = 0.35 + 0.45 * diffuse + 0.2 * fresnel
                pixels.append(mix((0, 0, 0, 255), info["emissive"], glow))
            else:
                light = 0.42 + diffuse * 0.48
                color = mix(info["shadow"], info["base"], light)
                color = mix(color, info["highlight"], min(0.78, specular * 0.65 + fresnel * 0.2))
                pixels.append(color)
    return png_bytes(size, size, pixels)


def sphere_model(texture_name: str) -> dict:
    return {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "textures": {
            "particle": f"smooth_spheres:block/{texture_name}",
        },
        "elements": [],
    }


def main() -> None:
    for folder in (BLOCKSTATES, BLOCK_MODELS, ITEM_MODELS, ITEM_DEFINITIONS, TEXTURES, LANG):
        folder.mkdir(parents=True, exist_ok=True)

    lang = {
        "itemGroup.smooth_spheres.smooth_spheres": "Smooth Spheres",
        "config.smooth_spheres.title": "Smooth Spheres",
        "config.smooth_spheres.quality": "Sphere quality: %s",
        "config.smooth_spheres.quality.balanced": "Balanced (128x64)",
        "config.smooth_spheres.quality.high": "High (192x96)",
        "config.smooth_spheres.quality.ultra": "Ultra (256x128)",
        "config.smooth_spheres.apply": "Apply",
        "config.smooth_spheres.reload_hint": "Changing quality reloads resources.",
    }

    for block_id, info in SPHERES.items():
        write_json(BLOCKSTATES / f"{block_id}.json", {
            "variants": {
                "": {"model": f"smooth_spheres:block/{block_id}"}
            }
        })
        write_json(BLOCK_MODELS / f"{block_id}.json", sphere_model(block_id))
        write_json(ITEM_MODELS / f"{block_id}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"smooth_spheres:block/{block_id}"
            }
        })
        write_json(ITEM_DEFINITIONS / f"{block_id}.json", {
            "model": {
                "type": "minecraft:model",
                "model": f"smooth_spheres:item/{block_id}"
            }
        })
        lang[f"block.smooth_spheres.{block_id}"] = info["name"]

        (TEXTURES / f"{block_id}.png").write_bytes(shaded_texture(info, "base"))
        (TEXTURES / f"{block_id}_n.png").write_bytes(shaded_texture(info, "normal"))
        (TEXTURES / f"{block_id}_s.png").write_bytes(shaded_texture(info, "spec"))
        (TEXTURES / f"{block_id}_e.png").write_bytes(shaded_texture(info, "emissive"))

    (TEXTURES / "sphere_surface.png").write_bytes(png_bytes(1, 1, [(255, 255, 255, 255)]))
    write_json(LANG / "en_us.json", lang)


if __name__ == "__main__":
    main()
