# Circular Rendering

English | [中文](README_zh.md)

This is a Fabric mod that modifies chunk rendering to a circular (or elliptical) shape around the player.  
By default, it renders chunks within a perfect circle (radius = view distance × 16 blocks).  
You can optionally scale the left/right radius to create an ellipse, culling more chunks to the sides while keeping forward/backward visibility unchanged. This reduces GPU load and improves FPS without affecting gameplay mechanics.

## Features

- Renders chunks in a circle centered on the player (default behavior).
- Optional elliptical rendering: the forward/backward radius remains fixed, while the left/right radius is scaled by a configurable factor (`renderRadiusScale`).
  - When `renderRadiusScale = 1.0`, it's a perfect circle.
  - When `renderRadiusScale < 1.0`, it becomes an ellipse (narrower left/right).
- Optional vertical range limitation: limit rendering to a certain number of chunk layers above and below the player.
- Chunk loading (logic updates) remains square, so all chunks are still loaded and updated.
- Presets & Custom Mode: Quickly switch between predefined configurations (Balanced, Performance, Aggressive) or choose **Custom** to freely adjust all parameters.
- Compatible with Sodium:
  - When Sodium is present, configuration appears in the Video Settings screen.
  - Without Sodium, the mod works standalone using a JSON config file.
- No effect on entities or other objects – only chunk rendering is affected.

## Dependencies

- **Required:** [Fabric Loader](https://fabricmc.net/) ≥0.18.4
- **Optional:** [Sodium](https://modrinth.com/mod/sodium) 0.8.6+ (for in-game GUI configuration)

## Installation

1. Install Fabric Loader.
2. Download the latest `circular-rendering-<version>.jar` from the [Releases](https://github.com/Uniaball/circular-rendering/releases) page.
3. Put the JAR into your `mods` folder.
4. (Optional) Install Sodium if you want the in-game configuration sliders and presets.

## Configuration

### With Sodium installed

1. Go to **Options → Video Settings**.
2. Scroll down to find the **Circular Rendering** section.

#### Presets
- **Render Preset** dropdown:
  - **Balanced**: `Render Radius Scale = 100%`, vertical range disabled (vanilla behavior, but still optimized).
  - **Performance**: `Render Radius Scale = 80%`, vertical range enabled, 10 layers.
  - **Aggressive**: `Render Radius Scale = 40%`, vertical range enabled, 3 layers.
  - **Custom**: All sliders become unlocked, allowing you to freely adjust the parameters below.

Selecting a non‑Custom preset automatically applies its parameters and locks the manual controls.

#### Manual Configurations (available only when “Custom” preset is selected)
- **Render Radius Scale** (10% – 100%):  
  - **100%** = perfect circle (radius = view distance × 16).  
  - **Lower values** make the left/right radius smaller, turning the circle into an ellipse and culling more chunks to the sides.
- **Enable Custom Vertical Range** (toggle) – when enabled, you can set:
  - **Vertical Range** (1–32 layers): Number of chunk layers (16 blocks each) to render above and below the player.

### Without Sodium

The mod creates a JSON config file at `config/circular-rendering.json`.  
Example content:

```json
{
  "renderRadiusScale": 1.0,
  "enableVerticalRange": false,
  "verticalRange": 16,
  "preset": "BALANCED"
}
```

- `renderRadiusScale` – a double between 0.1 and 1.0.  
  - `1.0` = perfect circle.  
  - `< 1.0` = ellipse (narrower left/right).  
- `enableVerticalRange` – a boolean, enables vertical range limiting when true.  
- `verticalRange` – an integer (1–32), number of chunk layers to render above and below the player (each layer = 16 blocks).  
- `preset` – one of `"BALANCED"`, `"PERFORMANCE"`, `"AGGRESSIVE"`, or `"CUSTOM"`.  
  - If the preset is not `"CUSTOM"`, the three manual parameters are ignored and the preset's values are used.  
  - If the preset is `"CUSTOM"`, the manual parameters are used exactly as set.

Changes take effect after restarting the game or reloading chunks.

## How It Works

- **Vanilla mode (no Sodium):** The mod injects into `LevelRenderer.cullTerrain` and filters the chunk list using the shape defined by:
  ```
  (forward² / a²) + (right² / b²) ≤ 1
  ```
  where `a = view distance × 16` (fixed forward/backward radius) and `b = a × renderRadiusScale` (left/right radius).  
  When `b = a`, the shape is a circle. If vertical range is enabled, it also checks chunk Y layers.

- **Sodium mode:** The injection point depends on the Minecraft version:
  - **MC 26.1.2 and above:** The mod redirects the `testDistance` call inside `OcclusionCuller.visitNode`. This replaces Sodium's default cylindrical distance check with the elliptical formula directly within the graph‑based occlusion culling traversal, preserving the full culling structure.
  - **MC 26.1.1 and below:** The mod injects into `OcclusionCuller.isWithinRenderDistance` and returns `false` for chunks outside the ellipse (or vertical range), effectively filtering them from Sodium's visible set.

Both approaches only affect chunk rendering; chunk loading remains square, so game mechanics (redstone, entity AI, etc.) work normally everywhere.

## Compatibility

- Support status of Minecraft versions:

| Versions | Support Status |
|----------|----------------|
| 26.2     | ✅ |
| 26.1.2   | ✅ |
| 26.1.1   | ✅ |
| 26.1     | ✅ |
| 1.21.11  | ✅ |
| Lower versions | Will not support |

- Fully compatible with Sodium 0.8.6+ (both mods can be used together).
- Should be compatible with most other mods that don't heavily modify chunk rendering. If you encounter issues, please report them.

## Building from Source

Clone the repository and run:

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.