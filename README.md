# 圆形渲染 (Circular Rendering)

[English](README_en.md) | 中文

这是一个 Fabric 模组，将区块渲染改为以玩家为中心的圆形（或椭圆形）区域。  
默认情况下，它渲染一个正圆（半径 = 视距 × 16 格）。  
你可以选择缩放左右方向半径，使其变成椭圆，从而更激进地剔除侧面区块，同时保持前后方向视野不变。这减少了 GPU 负载，提升帧率，且不影响游戏机制。

## 功能

- 以玩家为中心，渲染圆形区域内的区块（默认行为）。
- 可选椭圆渲染：前后方向半径固定，左右方向半径可配置缩放因子（`renderRadiusScale`）。
  - 当 `renderRadiusScale = 1.0` 时，为正圆。
  - 当 `renderRadiusScale < 1.0` 时，变为椭圆（左右方向更窄）。
- 可选的垂直范围限制：限制玩家上方和下方的渲染区块层数。
- 区块加载（逻辑更新）保持方形，所有区块仍会加载和更新。
- 兼容 Sodium：
  - 当 Sodium 存在时，视频设置中会出现配置滑块。
  - 无 Sodium 时，模组通过 JSON 配置文件独立工作。
- 不影响实体或其他对象——只修改区块渲染。

## 依赖

- **必需：** [Fabric Loader](https://fabricmc.net/) ≥0.18.4
- **必需：** [Fabric API](https://modrinth.com/mod/fabric-api)（1.21.11 的任何版本）
- **可选：** [Sodium](https://modrinth.com/mod/sodium) 0.8.6+（用于游戏内配置界面）

## 安装

1. 安装 Fabric Loader，并将 `fabric-api` 放入 `mods` 文件夹。
2. 从 [Releases](https://github.com/Uniaball/circular-rendering/releases) 页面下载最新的 `circular-rendering-<版本>.jar`。
3. 将 JAR 放入 `mods` 文件夹。
4. （可选）如需游戏内配置滑块，请同时安装 Sodium。

## 配置

### 安装了 Sodium 时

1. 进入 **选项 → 视频设置**。
2. 向下滚动找到 **圆形渲染** 部分。
3. 调整 **渲染半径缩放** 滑块（10% – 100%）：  
   - **100%** = 正圆（半径 = 视距 × 16）。  
   - **数值越小**，左右方向半径越小，圆形变为椭圆，剔除侧面区块更激进。
4. 可选择开启 **启用自定义垂直渲染范围** 并设置 **垂直范围**（以区块层为单位），限制玩家上方和下方的渲染。

### 无 Sodium 时

模组会在 `.minecraft/config/circular-rendering.json` 创建 JSON 配置文件。  
示例内容：

```json
{
  "renderRadiusScale": 1.0,
  "enableVerticalRange": false,
  "verticalRange": 16
}
```

- `renderRadiusScale` – 介于 0.1 和 1.0 之间的浮点数。  
  - `1.0` = 正圆。  
  - `< 1.0` = 椭圆（左右方向更窄）。
- `enableVerticalRange` – 布尔值，设为 true 时启用垂直范围限制。  
- `verticalRange` – 整数（1–32），玩家上下各渲染的区块层数（每层 16 格）。  

修改后需重启游戏或重新加载区块才能生效。

## 工作原理

- **原版模式（无 Sodium）：** 模组注入 `WorldRenderer.renderBlockLayers` 方法，过滤区块列表，保留符合以下形状的区块：
  ```
  (forward² / a²) + (right² / b²) ≤ 1
  ```
  其中 `a = 视距 × 16`（固定前后半径），`b = a × renderRadiusScale`（左右半径）。  
  当 `b = a` 时，形状为正圆。若启用了垂直范围，还会检查区块 Y 层。
- **Sodium 模式：** 模组注入 Sodium 的 `OcclusionCuller.isWithinRenderDistance` 方法，对形状外或超出垂直范围的区块返回 `false`。

两种方式都只影响区块渲染；区块加载保持方形，因此游戏机制（红石、实体 AI 等）在所有位置均正常工作。

## 兼容性

- 适用于 Minecraft 1.21.11。
- 与 Sodium 0.8.6+ 完全兼容（可同时使用）。
- 应与大多数不深度修改区块渲染的模组兼容。如果遇到问题，请反馈。

## 从源码构建

克隆仓库后运行：

```bash
./gradlew build
```

构建的 JAR 文件位于 `build/libs/`。

## 许可证

本项目采用 MIT 许可证。详情请见 [LICENSE](LICENSE) 文件。