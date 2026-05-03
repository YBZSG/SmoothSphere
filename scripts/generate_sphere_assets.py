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
        "base": (168, 174, 178, 255),
        "highlight": (238, 244, 248, 255),
        "shadow": (58, 63, 67, 255),
        "normal": (128, 128, 255, 255),
        "spec": (150, 185, 210, 255),
        "emissive": (0, 0, 0, 255),
    },
    "glowing_crystal_sphere": {
        "name": "Glowing Crystal Sphere",
        "base": (96, 126, 255, 190),
        "highlight": (234, 205, 255, 220),
        "shadow": (34, 22, 102, 190),
        "normal": (128, 128, 255, 255),
        "spec": (90, 175, 255, 255),
        "emissive": (75, 90, 190, 255),
    },
    "obsidian_black_sphere": {
        "name": "Obsidian Black Sphere",
        "base": (18, 16, 24, 255),
        "highlight": (116, 74, 160, 255),
        "shadow": (3, 2, 7, 255),
        "normal": (128, 128, 255, 255),
        "spec": (90, 75, 140, 255),
        "emissive": (0, 0, 0, 255),
    },
    "white_ceramic_sphere": {
        "name": "White Ceramic Sphere",
        "base": (230, 228, 220, 255),
        "highlight": (255, 255, 252, 255),
        "shadow": (150, 146, 136, 255),
        "normal": (128, 128, 255, 255),
        "spec": (52, 52, 52, 255),
        "emissive": (0, 0, 0, 255),
    },
    "blue_glass_sphere": {
        "name": "Blue Glass Sphere",
        "base": (56, 154, 238, 135),
        "highlight": (196, 238, 255, 190),
        "shadow": (14, 50, 112, 145),
        "normal": (128, 128, 255, 255),
        "spec": (120, 210, 255, 255),
        "emissive": (0, 0, 0, 255),
    },
    "clear_glass_sphere": {
        "name": "Clear Glass Sphere",
        "base": (190, 238, 255, 92),
        "highlight": (255, 255, 255, 180),
        "shadow": (112, 160, 178, 82),
        "normal": (128, 128, 255, 255),
        "spec": (210, 245, 255, 255),
        "emissive": (0, 0, 0, 255),
    },
    "luminous_glass_sphere": {
        "name": "Luminous Glass Sphere",
        "base": (150, 255, 220, 135),
        "highlight": (255, 255, 245, 210),
        "shadow": (72, 180, 150, 120),
        "normal": (128, 128, 255, 255),
        "spec": (180, 255, 230, 255),
        "emissive": (70, 205, 160, 255),
    },
    "chrome_metal_sphere": {
        "name": "Chrome Metal Sphere",
        "base": (172, 184, 190, 255),
        "highlight": (255, 255, 255, 255),
        "shadow": (38, 42, 46, 255),
        "normal": (128, 128, 255, 255),
        "spec": (230, 240, 245, 255),
        "emissive": (0, 0, 0, 255),
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
            normal = (math.cos(phi) * sin_theta, cos_theta, math.sin(phi) * sin_theta)
            diffuse = max(0.0, dot(normal, light_dir))
            fresnel = (1.0 - max(0.0, -normal[2])) ** 2.2
            specular = max(0.0, dot(normal, half_dir)) ** 80

            if kind == "normal":
                pixels.append((
                    round((normal[0] * 0.5 + 0.5) * 255),
                    round((-normal[1] * 0.5 + 0.5) * 255),
                    round((normal[2] * 0.5 + 0.5) * 255),
                    255,
                ))
            elif kind == "spec":
                pixels.append(mix(info["spec"], info["highlight"], max(specular, fresnel * 0.35)))
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
