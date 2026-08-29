# Let there be Light!

A comprehensive lighting mod for **Necesse** combining enhanced torches with decorative outdoor lighting fixtures, offering superior illumination, wiring support, and full configuration.

---

## Features

- **Better Torch**: A brighter torch emitting **2x vanilla light** (`300` light level). Can be uncrafted back into vanilla torches at a workstation.
- **Steel Lamp Post**: Industrial metal street lamp (`350` light level) with dynamic spark particles and wire support.
- **Wooden Hanging Lantern**: Rustic wooden lantern (`350` light level) supporting 4-way wall/ceiling directional mounting and wire support.
- **Phoenix Lamp Post**: Ultra-bright ornate gold lamp post (`550` light level) emitting a radiant warm glow with spark particles.
- **Abyssal Crystal Lantern**: Supercharged tier-2 water lantern (`450` light level, **3x brighter than vanilla water lantern**) with floating wave physics, bioluminescent aquatic particles, wire control, and land/water dual placement.
- **Full Wire Support**: All outdoor and aquatic fixtures can be wired to logic circuits and switches (turns off when wired signal is active).
- **Global Vanilla Torch Optimizations**: Non-destructive ByteBuddy patches globally optimize all vanilla and colored torches, replacing stream allocations with direct bitmask wire checks and direct static light map updates.
- **Multiplayer Synchronized**: Server settings automatically sync to connected clients upon joining.
- **AutoTorch Mod Seamless Integration**: High-performance, zero-allocation integration with the **AutoTorch** mod:
  - **Dynamic Light-Level Placement Range**: Automatically and seamlessly expands placement spacing and detection radius based on torch brightness (2x wider spacing for Better Torches).
  - **$O(1)$ Static Light Map Fast Path**: Direct engine light array lookups bypass 95%+ of tile loops when traversing already illuminated areas.
  - **Multi-Fixture Light Awareness**: Proactively checks all surrounding light sources (Better Torches, wall torches, lanterns, lamps) to eliminate torch clutter near brighter fixtures.
  - **Terrain-Aware ANY_LIGHT Mode**: Intelligently places floating Abyssal Crystal Lanterns / Water Lanterns when walking over water/liquid tiles, and ground/wall fixtures on solid terrain.
  - **Configurable Priority**: Control inventory consumption (`BETTER_THEN_VANILLA`, `VANILLA_THEN_BETTER`, `BETTER_ONLY`, `VANILLA_ONLY`, `ANY_LIGHT`) with customizable sorting (`SLOT_ORDER`, `BRIGHTEST_FIRST`, `CONSERVATIVE_FIRST`).
  - Full support for wall-mounted Better Torches, hotkey toggling (`V`), cave auto-activation, and multiplayer synchronization.
- **Customizable**: Configurable light levels, hues, saturation, recipe difficulties, and AutoTorch behavior in `settings/lighting/settings.cfg`. Supports up to `5000` light levels with automatic out-of-the-box performance guardrails.

---

## Crafting Recipes

| Fixture | Workstation | Ingredients |
| :--- | :--- | :--- |
| **Better Torch** | Workstation / Hand | 4x Torch *(or configurable difficulty)* |
| **Vanilla Torch (Uncraft)** | Workstation | 1x Better Torch &rarr; 4x Torch |
| **Steel Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Torch |
| **Wooden Hanging Lantern** | Carpenter | 1x Any Log + 1x Torch |
| **Phoenix Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Gold Bar + 3x Torch |
| **Abyssal Crystal Lantern (Upgrade)** | Workstation | 1x Water Lantern + 1x Quartz + 1x Any Gem |
| **Abyssal Crystal Lantern (Direct)** | Carpenter | 1x Torch + 1x Any Log + 1x Quartz |

---

## Configuration

The configuration file is automatically created at:
`<game_save_directory>/settings/lighting/settings.cfg`

### Config Options:
```properties
# === Better Torch Settings ===
torchLightLevel=300
torchLightHue=50.0
torchLightSat=0.2
recipeDifficulty=MEDIUM

# === Outdoor & Aquatic Lighting Settings ===
steelLampLightLevel=350
woodenLanternLightLevel=350
phoenixLampLightLevel=550
abyssLanternLightLevel=450
outdoorLampHue=50.0
outdoorLampSat=0.2
abyssLanternHue=30.0
abyssLanternSat=0.75

# === AutoTorch Integration Settings ===
autoTorchIntegration=true
# Options: BETTER_THEN_VANILLA, VANILLA_THEN_BETTER, BETTER_ONLY, VANILLA_ONLY, ANY_LIGHT
autoTorchTorchSelection=BETTER_THEN_VANILLA
# Priority when in ANY_LIGHT mode: SLOT_ORDER, BRIGHTEST_FIRST, CONSERVATIVE_FIRST
autoTorchAnyLightPriority=SLOT_ORDER
autoTorchConsiderAllLights=true
# Dynamically optimize lookahead and spacing when traveling with high light level torches
autoTorchDynamicTravelSpacing=true
# Minimum static light level threshold (30-200) below which a torch will be placed when traveling
autoTorchMinLightThreshold=90
```

---

## Mod Metadata
- **Mod ID**: `xeraphire.lighting`
- **Mod Name**: `Let there be Light!`
- **Author**: `Xeraphire`
- **Target Game Version**: `1.3.3`


