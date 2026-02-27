# Circular Rendering

English | [中文](README.md)

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
- Compatible with Sodium:
  - When Sodium is present, configuration sliders appear in the Video Settings screen.
  - Without Sodium, the mod works standalone using a JSON config file.
- No effect on entities or other objects – only chunk rendering is affected.

## Dependencies

- **Required:** [Fabric Loader](https://fabricmc.net/) ≥0.18.4
- **Required:** [Fabric API](https://modrinth.com/mod/fabric-api) (any version for 1.21.11)
- **Optional:** [Sodium](https://modrinth.com/mod/sodium) 0.8.6+ (for in-game GUI configuration)

## Installation

1. Install Fabric Loader and place `fabric-api` in your `mods` folder.
2. Download the latest `circular-rendering-<version>.jar` from the [Releases](https://github.com/Uniaball/circular-rendering/releases) page.
3. Put the JAR into your `mods` folder.
4. (Optional) Install Sodium if you want the in-game configuration slider.

## Configuration

### With Sodium installed

1. Go to **Options → Video Settings**.
2. Scroll down to find the **Circular Rendering** section.
3. Adjust the **Render Radius Scale** slider (10% – 100%):  
   - **100%** = perfect circle (radius = view distance × 16).  
   - **Lower values** make the left/right radius smaller, turning the circle into an ellipse and culling more chunks to the sides.
4. Optionally enable **Custom Vertical Range** and set the **Vertical Range** (in chunk layers) to limit rendering above and below the player.

### Without Sodium

The mod creates a JSON config file at `.minecraft/config/circular-rendering.json`.  
Example content:

```json
{
  "renderRadiusScale": 1.0,
  "enableVerticalRange": false,
  "verticalRange": 16
}
```

- `renderRadiusScale` – a double between 0.1 and 1.0.  
  - `1.0` = perfect circle.  
  - `< 1.0` = ellipse (narrower left/right).
- `enableVerticalRange` – a boolean, enables vertical range limiting when true.  
- `verticalRange` – an integer (1–32), number of chunk layers to render above and below the player (each layer = 16 blocks).  

Changes take effect after restarting the game or reloading chunks.

## How It Works

- **Vanilla mode (no Sodium):** The mod injects into `WorldRenderer.renderBlockLayers` and filters the chunk list using the shape defined by:
  ```
  (forward² / a²) + (right² / b²) ≤ 1
  ```
  where `a = view distance × 16` (fixed forward/backward radius) and `b = a × renderRadiusScale` (left/right radius).  
  When `b = a`, the shape is a circle. If vertical range is enabled, it also checks chunk Y layers.
- **Sodium mode:** The mod injects into Sodium's `OcclusionCuller.isWithinRenderDistance` and returns `false` for chunks outside this shape or vertical range.

Both approaches only affect chunk rendering; chunk loading remains square, so game mechanics (redstone, entity AI, etc.) work normally everywhere.

## Compatibility

- Works with Minecraft 1.21.11.
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