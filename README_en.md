# Circular Rendering

English | [中文](README.md)

This is a Fabric mod that changes the vanilla rendering method to 3D spherical rendering.  
It renders chunks only within a circle around the player, while chunk loading remains square. This reduces GPU load and improves FPS without affecting gameplay mechanics.

## Features

- Renders chunks in a circle (radius = view distance × 16 blocks) centered on the player.
- Chunk loading (logic updates) remains square, so all chunks are still loaded and updated.
- Configurable render radius scale – make the circle smaller for even more aggressive culling.
- Compatible with Sodium:
  - When Sodium is present, a configuration slider appears in the Video Settings screen.
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
3. Adjust the **Render Radius Scale** slider (10% – 100%).  
   - 100% = normal circle (radius = view distance × 16).  
   - Lower values make the circle smaller, culling more distant chunks.

### Without Sodium

The mod creates a JSON config file at `.minecraft/config/circular-rendering.json`.  
Example content:

```json
{
  "renderRadiusScale": 1.0
}
```

- `renderRadiusScale` – a double between 0.1 and 1.0.  
  Changes take effect after restarting the game or reloading chunks.

## How It Works

- **Vanilla mode (no Sodium):** The mod injects into `WorldRenderer.renderBlockLayers` and filters the chunk list, keeping only those inside the circle.
- **Sodium mode:** The mod injects into Sodium's `OcclusionCuller.isWithinRenderDistance` and returns `false` for chunks outside the circle.

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