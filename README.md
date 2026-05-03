# Smooth Spheres / 光滑球体

Smooth Spheres 是一个 Minecraft Java 版 Fabric 装饰与轻量物理模组，目标版本为 Minecraft `1.21.4`。

它添加了 9 种球体方块。球体不是普通立方体外观，而是通过自定义 baked model 渲染成圆球，并支持右键转换为可移动的物理球实体。

## 基本信息

- Minecraft：`1.21.4`
- Loader：Fabric Loader `0.18.4+`
- Java：`21`
- 构建系统：Gradle
- 映射：Yarn
- Mod ID：`smooth_spheres`
- 包名：`com.lzh.smoothspheres`
- 可选集成：Mod Menu

## 球体列表

模组目前包含 9 个球体：

- Polished Metal Sphere / 抛光金属球
- Glowing Crystal Sphere / 发光水晶球
- Obsidian Black Sphere / 黑曜石黑球
- White Ceramic Sphere / 白色陶瓷球
- Blue Glass Sphere / 蓝色玻璃球
- Clear Glass Sphere / 透明玻璃球
- Frosted Glass Sphere / 磨砂玻璃球
- Luminous Glass Sphere / 发光玻璃球
- Chrome Metal Sphere / 铬金属球

所有球体都会出现在自定义创造模式物品栏 `Smooth Spheres / 光滑球体` 中。

## 物理特性

球体默认支持轻量物理效果：

- 右键普通球体方块：转换为物理球实体并击飞
- 右键物理球实体：继续击飞
- 玩家身体接触：可以推动物理球
- 箭、火球、鸡蛋等投射物命中：推动球体，不直接打碎
- 左键攻击物理球：打碎并掉落对应球体物品
- 物理球会受重力、碰撞、摩擦和反弹影响
- 物理球渲染时会根据速度产生滚动旋转

这是轻量级 Minecraft 实体物理，不是完整刚体物理引擎。它适合装饰互动和简单推动/弹跳效果。

## 游戏内配置

如果安装了 Mod Menu，可以在模组设置页面修改：

- 球体质量
  - Balanced：`128x64`
  - High：`192x96`
  - Ultra：`256x128`
- 物理效果开关
  - 开启：右键可转换/击飞物理球
  - 关闭：球体只作为普通装饰方块使用

配置文件位于：

```text
config/smooth_spheres.json
```

切换球体质量会重新加载资源；物理开关不需要重启游戏。

## 材质与光影

材质文件位于：

```text
src/main/resources/assets/smooth_spheres/textures/block/
```

每个球体包含以下贴图：

- `<name>.png`：基础颜色 / albedo
- `<name>_n.png`：法线贴图占位
- `<name>_s.png`：粗糙度/高光/PBR 管线占位
- `<name>_e.png`：自发光贴图占位

示例：

```text
polished_metal_sphere.png
polished_metal_sphere_n.png
polished_metal_sphere_s.png
polished_metal_sphere_e.png
```

注意：原版 Minecraft 本身不支持真正的 PBR 材质。真实金属、玻璃、粗糙度、法线和自发光效果取决于 Iris 兼容光影或其它支持 PBR 的资源包/渲染管线。没有光影时，球体仍会以普通材质正常显示。

## 构建

Linux/macOS：

```sh
./gradlew build
```

Windows PowerShell：

```powershell
.\gradlew.bat build
```

构建完成后的 jar 位于：

```text
build/libs/
```

## 运行客户端

Linux/macOS：

```sh
./gradlew runClient
```

Windows PowerShell：

```powershell
.\gradlew.bat runClient
```

## 重新生成资源

资源生成脚本：

```text
scripts/generate_sphere_assets.py
```

运行：

```powershell
python scripts/generate_sphere_assets.py
```

它会重新生成：

- blockstates
- block model JSON
- item model JSON
- Minecraft 1.21.4 item definition JSON
- 英文/中文语言文件
- PBR 命名占位贴图

## 项目结构

```text
src/main/java/com/lzh/smoothspheres/
  SmoothSpheresMod.java
  SmoothSpheresClient.java
  block/SphereBlock.java
  entity/PhysicsSphereEntity.java
  registry/ModBlocks.java
  registry/ModEntities.java
  registry/ModItemGroups.java
  client/config/
  client/model/
  client/render/

src/main/resources/assets/smooth_spheres/
  blockstates/
  items/
  lang/
  models/
  textures/
```

## 已知限制

- 物理球是自定义 Minecraft 实体，不是完整刚体模拟。
- 透明玻璃效果会受到光影包、渲染管线和排序方式影响。
- PBR 贴图命名兼容常见资源包/光影管线，但实际效果取决于玩家使用的 shader。
- 球体质量越高，模型面数越多；大量放置时建议使用 Balanced 或 High。

## 许可证

MIT
