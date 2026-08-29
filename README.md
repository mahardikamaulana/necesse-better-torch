# Let there be Light!

**Let there be Light!** is a gameplay and lighting overhaul mod for **Necesse 1.3.3** designed to make cave exploration safer, base building more stylish, and automated lighting effortless.

---

## Why You Need This Mod

- **Stop Squinting in Dark Caves**: Better Torches cast light twice as far as vanilla torches, helping you spot ores, chests, traps, and lurking monsters long before they reach you.
- **Save Inventory Space & Resources**: Because brighter fixtures cover wider areas, you use 50% fewer torches. This means fewer wood-chopping trips and more open inventory slots for dungeon loot.
- **Craft and Uncraft Anywhere**: Upgrade 4 vanilla torches into 1 Better Torch directly in your handcrafting menu while exploring underground. If you ever need regular torches back, you can uncraft them on the fly with zero material loss.
- **No More "Stick Graveyard" Bases**: Replace messy ground torch spam with elegant Steel Lamp Posts, 4-way Wooden Hanging Lanterns, and majestic Phoenix Lamps.
- **Conquer Flooded Caves & Deep Oceans**: Normal torches cannot be placed in water, and vanilla water lanterns are barely visible. The Abyssal Crystal Lantern floats on water with realistic wave bobbing and illuminates deep flooded trenches.
- **Wire Automation for Modern Towns**: Connect lights to daylight sensors or switches to automatically turn off your settlement's lighting during the day or when settlers go to sleep.
- **Sprint & Ride Without Torch Clumping**: If you use the AutoTorch mod, this mod dynamically projects placement ahead of your movement vector, so sprinting or riding mounts never causes torch lag or clumped placements.

---

## New Lighting Fixtures

### Better Torch
- **Light Level**: 300 (2x vanilla brightness)
- **Mounting**: Ground and wall mountable
- **How to get**: Handcraftable anywhere (4x Torch -> 1x Better Torch)
- **Best for**: Cave spelunking, mining tunnels, and dungeon exploration

### Steel Lamp Post
- **Light Level**: 350
- **Appearance**: Industrial metal post with dynamic spark particles
- **How to get**: Crafted at the Iron Anvil (1x Iron Bar + 1x Torch)
- **Best for**: Town pathways, plazas, castle courtyards, and perimeter security

### Wooden Hanging Lantern
- **Light Level**: 350
- **Mounting**: 4-way mounting (attaches to left/right walls, back walls, or ceilings)
- **How to get**: Crafted at the Carpenter Bench (1x Any Log + 1x Torch)
- **Best for**: Taverns, cozy cabins, mine shafts, and indoor ceiling lighting

### Phoenix Lamp Post
- **Light Level**: 550
- **Appearance**: Ornate golden beacon radiating a massive, warm solar glow
- **How to get**: Crafted at the Iron Anvil (1x Iron Bar + 1x Gold Bar + 3x Torch)
- **Best for**: Town centers, arenas, boss arenas, and grand monuments

### Abyssal Crystal Lantern
- **Light Level**: 450 (3x brighter than vanilla water lanterns)
- **Special**: Floats on water surfaces with wave bobbing and bioluminescent aquatic particles; also placeable on land
- **How to get**: Crafted at Workstation (1x Water Lantern + 1x Quartz + 1x Any Gem) or Carpenter Bench (1x Torch + 1x Any Log + 1x Quartz)
- **Best for**: Ocean exploration, flooded cave caverns, docks, and fishing harbors

---

## Wire Logic & Base Automation

Every fixture in this mod (as well as all vanilla torches) fully supports wire circuits:
- **Automatic Daylight Shutoff**: Connect your fixtures to a daylight sensor to turn off town lights at sunrise and save power.
- **Master Switches**: Hook an entire base or dungeon floor to a single lever or pressure plate.
- **Performance-Friendly**: Wire checks use lightweight bitmasks with zero FPS drops or lag spikes.

---

## Smart AutoTorch Integration

If you have the **AutoTorch** mod installed, this mod seamlessly upgrades its placement behavior:

- **Lookahead Travel Placement**: Predicts your movement direction (including diagonal sprinting) and places torches ahead in the dark instead of trailing behind you.
- **Dynamic Spacing**: When holding Better Torches or Phoenix Lamps, torches are automatically placed 12 to 16 tiles apart instead of 3 tiles, saving your inventory.
- **Surrounding Light Awareness**: Senses existing fixtures up to 20 tiles away and avoids dropping duplicate torches into already-lit hallways.
- **Water & Land Smart Switching**: When running in `ANY_LIGHT` mode, it automatically places floating Abyssal Lanterns over water and solid torches on land.
- **Anti-Clumping Protection**: Eliminates torch pile-ups against walls when dashing or riding fast mounts.

---

## Crafting Summary

| Item | Where to Craft | Ingredients |
| :--- | :--- | :--- |
| **Better Torch** | Handcrafting / Workstation | 4x Torch |
| **Vanilla Torch (Uncraft)** | Handcrafting / Workstation | 1x Better Torch -> 4x Torch |
| **Steel Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Torch |
| **Wooden Hanging Lantern** | Carpenter Bench | 1x Any Log + 1x Torch |
| **Phoenix Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Gold Bar + 3x Torch |
| **Abyssal Crystal Lantern (Upgrade)** | Workstation | 1x Water Lantern + 1x Quartz + 1x Any Gem |
| **Abyssal Crystal Lantern (Direct)** | Carpenter Bench | 1x Torch + 1x Any Log + 1x Quartz |

---

## Configuration & Customization

Settings can be customized in:
`<game_save_directory>/settings/lighting/settings.cfg`

### Key Settings Players Can Tweak:
- **Light Brightness**: Change the light level of any fixture (supports up to 5000 light level).
- **Light Color**: Customize the warmth, hue, and saturation of torches and lamps.
- **Crafting Difficulty**: Switch between `EASY` (2 torches per craft), `MEDIUM` (4 torches), or `HARD` (4 torches + 4 stone).
- **AutoTorch Priorities**: Choose whether AutoTorch prefers Better Torches, vanilla torches, or brightest available lights.

*All settings automatically synchronize from the host server to connected players in multiplayer.*

---

## Installation

1. Download `LettherebeLight!-1.3.3-1.0.0.jar` from the [Releases](https://github.com/mahardikamaulana/necesse-better-torch/releases) page.
2. Place the JAR file into your `Necesse/mods/` folder.
3. Launch Necesse and make sure the mod is enabled in the Mods menu.

---

## Credits & Acknowledgements

This mod builds upon, integrates, and draws inspiration from incredible work by community modders:

- **[Better Torch](https://steamcommunity.com/sharedfiles/filedetails/?id=3311404008)** by **[Bolo](https://steamcommunity.com/id/bolo42)** — Original Better Torch concept and mechanics.
- **[AutoTorch](https://steamcommunity.com/sharedfiles/filedetails/?id=3132344296)** by **[Erick Hasse (Crow)](https://steamcommunity.com/profiles/76561197988408858)** — Automatic torch placement framework and listener architecture.
- **[Outdoor Lighting](https://steamcommunity.com/sharedfiles/filedetails/?id=3754847143)** by **[ceyoda](https://steamcommunity.com/id/ceyoda)** — Outdoor lighting fixtures and hanging lantern visual concepts.

---

## Mod Details
- **Mod Name**: Let there be Light!
- **Mod ID**: `xeraphire.lighting`
- **Target Game Version**: `1.3.3`
- **Repository**: [https://github.com/mahardikamaulana/necesse-better-torch](https://github.com/mahardikamaulana/necesse-better-torch)
