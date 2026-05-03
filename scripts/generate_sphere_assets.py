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
        "base": (112, 170, 245, 90),
        "highlight": (230, 250, 255, 220),
        "shadow": (42, 56, 138, 160),
        "normal": (128, 128, 255, 255),
        "spec": (24, 38, 54, 255),
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
        "base": (62, 166, 245, 44),
        "highlight": (212, 244, 255, 180),
        "shadow": (22, 78, 156, 120),
        "normal": (128, 128, 255, 255),
        "spec": (24, 48, 64, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.5,
        "normal_noise": 0.0,
    },
    "clear_glass_sphere": {
        "name": "Clear Glass Sphere",
        "base": (190, 238, 255, 26),
        "highlight": (255, 255, 255, 180),
        "shadow": (128, 170, 184, 72),
        "normal": (128, 128, 255, 255),
        "spec": (18, 34, 42, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.52,
        "normal_noise": 0.0,
    },
    "frosted_glass_sphere": {
        "name": "Frosted Glass Sphere",
        "base": (186, 228, 226, 72),
        "highlight": (232, 255, 248, 178),
        "shadow": (135, 178, 184, 150),
        "normal": (128, 128, 255, 255),
        "spec": (12, 18, 18, 255),
        "emissive": (0, 0, 0, 255),
        "specular": 0.075,
        "normal_noise": 0.1,
    },
    "luminous_glass_sphere": {
        "name": "Luminous Glass Sphere",
        "base": (150, 255, 220, 58),
        "highlight": (255, 255, 245, 200),
        "shadow": (72, 180, 150, 130),
        "normal": (128, 128, 255, 255),
        "spec": (18, 42, 36, 255),
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

ZH_NAMES = {
    "polished_metal_sphere": "抛光金属球",
    "glowing_crystal_sphere": "发光水晶球",
    "obsidian_black_sphere": "黑曜石黑球",
    "white_ceramic_sphere": "白色陶瓷球",
    "blue_glass_sphere": "蓝色玻璃球",
    "clear_glass_sphere": "透明玻璃球",
    "frosted_glass_sphere": "磨砂玻璃球",
    "luminous_glass_sphere": "发光玻璃球",
    "chrome_metal_sphere": "铬金属球",
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


def material_texture(info: dict[str, tuple[int, int, int, int]], kind: str, size: int = 16) -> bytes:
    if kind == "normal":
        color = info["normal"]
    elif kind == "spec":
        color = info["spec"]
    elif kind == "emissive":
        color = info["emissive"]
    else:
        color = info["base"]
    return png_bytes(size, size, [color] * (size * size))


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
        "config.smooth_spheres.physics": "Physics: %s",
        "config.smooth_spheres.enabled": "On",
        "config.smooth_spheres.disabled": "Off",
        "config.smooth_spheres.apply": "Apply",
        "config.smooth_spheres.reload_hint": "Changing quality reloads resources.",
    }
    zh_lang = {
        "itemGroup.smooth_spheres.smooth_spheres": "光滑球体",
        "config.smooth_spheres.title": "光滑球体",
        "config.smooth_spheres.quality": "球体质量：%s",
        "config.smooth_spheres.quality.balanced": "均衡（128x64）",
        "config.smooth_spheres.quality.high": "高（192x96）",
        "config.smooth_spheres.quality.ultra": "极高（256x128）",
        "config.smooth_spheres.physics": "物理效果：%s",
        "config.smooth_spheres.enabled": "开启",
        "config.smooth_spheres.disabled": "关闭",
        "config.smooth_spheres.apply": "应用",
        "config.smooth_spheres.reload_hint": "修改球体质量会重新加载资源。",
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
        zh_lang[f"block.smooth_spheres.{block_id}"] = ZH_NAMES[block_id]

        (TEXTURES / f"{block_id}.png").write_bytes(material_texture(info, "base"))
        (TEXTURES / f"{block_id}_n.png").write_bytes(material_texture(info, "normal"))
        (TEXTURES / f"{block_id}_s.png").write_bytes(material_texture(info, "spec"))
        (TEXTURES / f"{block_id}_e.png").write_bytes(material_texture(info, "emissive"))

    (TEXTURES / "sphere_surface.png").write_bytes(png_bytes(1, 1, [(255, 255, 255, 255)]))
    write_json(LANG / "en_us.json", lang)
    write_json(LANG / "zh_cn.json", zh_lang)


if __name__ == "__main__":
    main()
