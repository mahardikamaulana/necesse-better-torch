# Let there be Light!

A lighting overhaul mod for **Necesse 1.3.3** that introduces high-intensity torches, decorative outdoor and aquatic lighting fixtures, full wire control, and smart **AutoTorch** mod integration.

---

## Features

- **Better Torch**: Emits **2x vanilla light** (300 light level). Mounts on both ground and walls, and can be uncrafted back to regular torches.
- **Steel Lamp Post**: Industrial metal street lamp (350 light level) with dynamic spark particles, ideal for town pathways and plazas.
- **Wooden Hanging Lantern**: Rustic wooden lantern (350 light level) supporting 4-way wall and ceiling directional mounting.
- **Phoenix Lamp Post**: Ultra-bright ornate golden street lamp (550 light level) radiating a wide, warm glow.
- **Abyssal Crystal Lantern**: Supercharged tier-2 aquatic lantern (450 light level, **3x brighter than vanilla water lantern**) with floating wave physics, bioluminescent particles, and dual land/water placement.
- **Full Wire Support**: All outdoor and aquatic fixtures connect to wire logic and switches, turning off when receiving an active wire signal.
- **Optimized Vanilla Torches**: Patches optimize all vanilla and colored torches with direct static light updates.
- **Multiplayer Synchronized**: Server settings automatically synchronize to connected players upon joining.

---

## AutoTorch Mod Integration

When used alongside the **AutoTorch** mod, this mod enhances automatic torch placement with smart travel and terrain features:

- **Velocity-Aware Lookahead**: Predicts player movement (including diagonal sprinting) and places torches ahead along your travel path instead of lagging behind.
- **Dynamic Torch Spacing**: Automatically spaces torches further apart (up to 12-16 tiles) when holding high-intensity fixtures like Better Torches or Phoenix Lamps.
- **Smart Light Detection**: Scans surrounding areas up to 20 tiles away to detect existing lamps and fixtures, preventing unnecessary torch drops.
- **Anti-Clumping Protection**: Prevents torch pile-ups during mount sprints while new light calculations finish processing.
- **Terrain Awareness**: In `ANY_LIGHT` mode, automatically places floating Abyssal Lanterns over water/liquids and solid torches on land.
- **Configurable Inventory Priority**: Choose whether to consume Better Torches first, vanilla torches first, or rank by brightness.

---

## Crafting Recipes

| Item | Workstation | Ingredients |
| :--- | :--- | :--- |
| **Better Torch** | Handcrafting / Workstation | 4x Torch *(Configurable: EASY=2, MEDIUM=4, HARD=4+4 Stone)* |
| **Vanilla Torch (Uncraft)** | Handcrafting / Workstation | 1x Better Torch -> 4x Torch |
| **Steel Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Torch |
| **Wooden Hanging Lantern** | Carpenter | 1x Any Log + 1x Torch |
| **Phoenix Lamp Post** | Iron Anvil | 1x Iron Bar + 1x Gold Bar + 3x Torch |
| **Abyssal Crystal Lantern (Upgrade)** | Workstation | 1x Water Lantern + 1x Quartz + 1x Any Gem |
| **Abyssal Crystal Lantern (Direct)** | Carpenter | 1x Torch + 1x Any Log + 1x Quartz |

---

## Configuration

The configuration file is automatically created at:
`<game_save_directory>/settings/lighting/settings.cfg`

```properties
# === Better Torch Settings ===
torchLightLevel=300
torchLightHue=50.0
torchLightSat=0.2
recipeDifficulty=MEDIUM

# === Outdoor & Aquatic Fixture Settings ===
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
# Enables dynamic spacing and velocity lookahead when traveling with bright fixtures
autoTorchDynamicTravelSpacing=true
# Minimum light level below which a torch will be placed
autoTorchMinLightThreshold=90
```

### Config Options Reference

| Setting | Options / Range | Default | Description |
| :--- | :--- | :--- | :--- |
| `torchLightLevel` | 50 - 5000 | 300 | Light level for Better Torches. |
| `recipeDifficulty` | EASY, MEDIUM, HARD | MEDIUM | Crafting cost difficulty for Better Torches. |
| `steelLampLightLevel` | 50 - 5000 | 350 | Light level for Steel Lamp Posts. |
| `woodenLanternLightLevel` | 50 - 5000 | 350 | Light level for Wooden Hanging Lanterns. |
| `phoenixLampLightLevel` | 50 - 5000 | 550 | Light level for Phoenix Lamp Posts. |
| `abyssLanternLightLevel` | 50 - 5000 | 450 | Light level for Abyssal Crystal Lanterns. |
| `autoTorchIntegration` | true / false | true | Enables custom AutoTorch placement listener. |
| `autoTorchTorchSelection` | BETTER_THEN_VANILLA, etc. | BETTER_THEN_VANILLA | Inventory torch consumption preference. |
| `autoTorchAnyLightPriority` | SLOT_ORDER, BRIGHTEST_FIRST, CONSERVATIVE_FIRST | SLOT_ORDER | Sort order when in ANY_LIGHT mode. |
| `autoTorchConsiderAllLights` | true / false | true | Checks nearby existing fixtures before placing. |
| `autoTorchDynamicTravelSpacing` | true / false | true | Adjusts spacing dynamically based on torch brightness and speed. |
| `autoTorchMinLightThreshold` | 30 - 200 | 90 | Darkness threshold that triggers torch placement. |

---

## Installation

1. Download the latest `LettherebeLight!-1.3.3-1.0.0.jar` from the [Releases](https://github.com/mahardikamaulana/necesse-better-torch/releases) page.
2. Place the JAR file into your Necesse `mods/` directory.
3. Launch Necesse and enable the mod in the Mods menu.

---

## Building from Source

Requirements: Java 8 JDK (or higher) and Gradle.

```bash
# Clone the repository
git clone https://github.com/mahardikamaulana/necesse-better-torch.git
cd necesse-better-torch

# Build the mod JAR and run all tests
./gradlew clean build
```

Compiled JAR will be generated at `build/jar/LettherebeLight!-1.3.3-1.0.0.jar`.

---

## Mod Information

- **Mod ID**: `xeraphire.lighting`
- **Mod Name**: `Let there be Light!`
- **Version**: `1.0.0`
- **Target Game Version**: `1.3.3`
- **Authors**: `Xeraphire`, `Erick Hasse`, `Bolo`
- **Repository**: [https://github.com/mahardikamaulana/necesse-better-torch](https://github.com/mahardikamaulana/necesse-better-torch)
