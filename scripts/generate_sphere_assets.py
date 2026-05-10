from __future__ import annotations

import json
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
        "name": "Polished Steel Sphere",
        "zh": "\u629b\u5149\u94a2\u7403",
        "base": (152, 160, 166, 255),
        "normal": (128, 128, 255, 255),
        "spec": (165, 170, 176, 255),
        "emissive": (0, 0, 0, 255),
    },
    "glowing_crystal_sphere": {
        "name": "Luminous Crystal Sphere",
        "zh": "\u8f89\u5149\u6c34\u6676\u7403",
        "base": (112, 170, 245, 90),
        "normal": (128, 128, 255, 255),
        "spec": (24, 38, 54, 255),
        "emissive": (75, 90, 190, 255),
    },
    "obsidian_black_sphere": {
        "name": "Glossy Obsidian Sphere",
        "zh": "\u4eae\u9762\u9ed1\u66dc\u77f3\u7403",
        "base": (14, 12, 24, 255),
        "normal": (128, 128, 255, 255),
        "spec": (42, 35, 68, 255),
        "emissive": (0, 0, 0, 255),
    },
    "white_ceramic_sphere": {
        "name": "Matte Ceramic Sphere",
        "zh": "\u54d1\u5149\u9676\u74f7\u7403",
        "base": (230, 228, 220, 255),
        "normal": (128, 128, 255, 255),
        "spec": (18, 18, 18, 255),
        "emissive": (0, 0, 0, 255),
    },
    "blue_glass_sphere": {
        "name": "Azure Glass Sphere",
        "zh": "\u6e5b\u84dd\u73bb\u7483\u7403",
        "base": (62, 166, 245, 44),
        "normal": (128, 128, 255, 255),
        "spec": (24, 48, 64, 255),
        "emissive": (0, 0, 0, 255),
    },
    "clear_glass_sphere": {
        "name": "Clear Glass Sphere",
        "zh": "\u900f\u660e\u73bb\u7483\u7403",
        "base": (190, 238, 255, 26),
        "normal": (128, 128, 255, 255),
        "spec": (18, 34, 42, 255),
        "emissive": (0, 0, 0, 255),
    },
    "frosted_glass_sphere": {
        "name": "Frosted Glass Sphere",
        "zh": "\u78e8\u7802\u73bb\u7483\u7403",
        "base": (186, 228, 226, 72),
        "normal": (128, 128, 255, 255),
        "spec": (12, 18, 18, 255),
        "emissive": (0, 0, 0, 255),
    },
    "luminous_glass_sphere": {
        "name": "Glowglass Sphere",
        "zh": "\u8367\u5149\u73bb\u7483\u7403",
        "base": (150, 255, 220, 58),
        "normal": (128, 128, 255, 255),
        "spec": (18, 42, 36, 255),
        "emissive": (70, 205, 160, 255),
    },
    "chrome_metal_sphere": {
        "name": "Mirror Chrome Sphere",
        "zh": "\u955c\u9762\u94ec\u7403",
        "base": (156, 168, 176, 255),
        "normal": (128, 128, 255, 255),
        "spec": (230, 240, 245, 255),
        "emissive": (0, 0, 0, 255),
    },
}


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


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


def material_texture(info: dict[str, tuple[int, int, int, int]], kind: str, size: int = 16) -> bytes:
    color = info[kind if kind in info else "base"]
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


def sphere_item_model(texture_name: str) -> dict:
    return {
        "parent": f"smooth_spheres:block/{texture_name}",
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.76, 0.76, 0.76]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.34, 0.34, 0.34]},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.62, 0.62, 0.62]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 1], "scale": [0.38, 0.38, 0.38]},
            "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 1], "scale": [0.38, 0.38, 0.38]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [1.2, 2.2, 0], "scale": [0.42, 0.42, 0.42]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [1.2, 2.2, 0], "scale": [0.42, 0.42, 0.42]},
        },
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
        "itemGroup.smooth_spheres.smooth_spheres": "\u5149\u6ed1\u7403\u4f53",
        "config.smooth_spheres.title": "\u5149\u6ed1\u7403\u4f53",
        "config.smooth_spheres.quality": "\u7403\u4f53\u8d28\u91cf\uff1a%s",
        "config.smooth_spheres.quality.balanced": "\u5747\u8861\uff08128x64\uff09",
        "config.smooth_spheres.quality.high": "\u9ad8\uff08192x96\uff09",
        "config.smooth_spheres.quality.ultra": "\u6781\u9ad8\uff08256x128\uff09",
        "config.smooth_spheres.physics": "\u7269\u7406\u6548\u679c\uff1a%s",
        "config.smooth_spheres.enabled": "\u5f00\u542f",
        "config.smooth_spheres.disabled": "\u5173\u95ed",
        "config.smooth_spheres.apply": "\u5e94\u7528",
        "config.smooth_spheres.reload_hint": "\u4fee\u6539\u7403\u4f53\u8d28\u91cf\u4f1a\u91cd\u65b0\u52a0\u8f7d\u8d44\u6e90\u3002",
    }

    for block_id, info in SPHERES.items():
        write_json(BLOCKSTATES / f"{block_id}.json", {
            "variants": {"": {"model": f"smooth_spheres:block/{block_id}"}}
        })
        write_json(BLOCK_MODELS / f"{block_id}.json", sphere_model(block_id))
        write_json(ITEM_MODELS / f"{block_id}.json", sphere_item_model(block_id))
        write_json(ITEM_DEFINITIONS / f"{block_id}.json", {
            "model": {"type": "minecraft:model", "model": f"smooth_spheres:item/{block_id}"}
        })

        lang[f"block.smooth_spheres.{block_id}"] = info["name"]
        zh_lang[f"block.smooth_spheres.{block_id}"] = info["zh"]

        (TEXTURES / f"{block_id}.png").write_bytes(material_texture(info, "base"))
        (TEXTURES / f"{block_id}_n.png").write_bytes(material_texture(info, "normal"))
        (TEXTURES / f"{block_id}_s.png").write_bytes(material_texture(info, "spec"))
        (TEXTURES / f"{block_id}_e.png").write_bytes(material_texture(info, "emissive"))

    (TEXTURES / "sphere_surface.png").write_bytes(png_bytes(1, 1, [(255, 255, 255, 255)]))
    write_json(LANG / "en_us.json", lang)
    write_json(LANG / "zh_cn.json", zh_lang)


if __name__ == "__main__":
    main()
