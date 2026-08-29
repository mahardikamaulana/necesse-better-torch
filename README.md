# Let there be Light!

A high-performance illumination and lighting infrastructure mod for **Necesse 1.3.3**, providing high-intensity light sources, architectural outdoor fixtures, aquatic lighting, zero-allocation wire control, non-destructive engine bytecode patches, and velocity-aware **AutoTorch** integration.

---

## 1. Architectural Overview

The mod implements custom lighting fixtures through modular class hierarchies, runtime bytecode instrumentation via ByteBuddy, and a server-authoritative networking layer:

```
GameObject (Necesse Engine)
 ├── TorchObject
 │    └── BetterTorchObject (Light Level: 300, 2x vanilla radius)
 ├── WallTorchObject
 │    └── BetterWallTorchObject (Internal wall decor layer fixture)
 └── OutdoorLampObject (Base for wire-controlled outdoor fixtures)
      ├── SteelLampPostObject (Light Level: 350, dynamic sparks)
      ├── WoodenHangingLanternObject (Light Level: 350, 4-way mounting)
      ├── PhoenixLampObject (Light Level: 400-550, golden solar glow)
      └── AbyssLanternObject (Light Level: 400-450, floating aquatic fixture)
```

### Key Technical Characteristics:
- **Non-Destructive Bytecode Injection**: Uses ByteBuddy `@ModMethodPatch` annotations to intercept vanilla torch methods and light computation loops without modifying vanilla class files or breaking compatibility with other mods.
- **Zero Garbage-Collection Overhead**: Replaces heap stream allocations and intermediate collection objects in high-frequency game ticks with direct primitive array lookups and bitmask operations.
- **Server-Authoritative Synchronization**: Full configuration state is serialized into a custom binary packet (`PacketModConfigSync`) and synchronized upon player connection and runtime reload.

---

## 2. Lighting Engine & Performance Optimizations

### 2.1 Direct Static Light Map Lookups ($O(1)$ Fast Path)
Vanilla Necesse computes static lighting across discrete tile coordinates. Rather than iterating across surrounding tiles with $O(R^2)$ distance checks, `BetterAutoTorchServerListener` directly queries the engine's internal static light map:
- Reads the light value directly from `level.lightManager.getStaticLight(tileX, tileY)`.
- If the existing static light at the target coordinate meets or exceeds `autoTorchMinLightThreshold` (default: `90`), the entire placement evaluation loop exits in $O(1)$ time, eliminating over 95% of placement overhead in illuminated zones.

### 2.2 Velocity-Aware Lookahead Projection
When a player moves at high speeds (e.g., using mounts, speed equipment, or dash abilities), standard periodic placement algorithms lag behind the player's true position. `BetterAutoTorchGameLoop.calculateTargetTile()` projects placement vectors dynamically:
- Analyzes the normalized player movement vector $(dx, dy)$ and magnitude $\sqrt{dx^2 + dy^2}$.
- Computes an adaptive lookahead stride scaled proportionally to torch light intensity:
  $$\text{lookaheadStride} = \text{clamp}\left(\text{candidateRadius} \times 0.60, 3, 16\right)$$
- Projects the target coordinate:
  $$\text{targetX} = \text{playerTileX} + \text{round}\left(dx \times \text{lookaheadStride}\right)$$
  $$\text{targetY} = \text{playerTileY} + \text{round}\left(dy \times \text{lookaheadStride}\right)$$
- Supports full 8-directional and diagonal movement vectors.

### 2.3 Expanded Spatial Scanning Window
To prevent low-radius light sources (such as vanilla torches) from dropping adjacent to high-intensity fixtures (such as Phoenix Lamps or Abyssal Lanterns), `isTorchNearby` evaluates an expanded bounding box:
$$\text{scanRadius} = \max\left(\text{candidateHalfRadius}, \text{LightCache.MAX\_LIGHT\_RADIUS}\right)$$
Where `LightCache.MAX_LIGHT_RADIUS = 20` tiles. This guarantees that existing high-intensity sources up to 20 tiles away are detected, suppressing redundant placements.

### 2.4 Placement Hysteresis & Asynchronous Lighting Cache
Necesse processes static light propagation asynchronously on worker threads (`StaticLightUpdater`). During rapid transit, multiple game ticks may execute before newly placed torches propagate static light to the map array.
- `RecentPlacement` maintains an in-memory spatial cache tracking recent placements with coordinate keys and expiration timestamps.
- Suppresses subsequent placement requests within radius $R$ for `SUPPRESSION_WINDOW_MS = 250` ms, eliminating torch clumping during asynchronous light calculation.

---

## 3. Bytecode Instrumentation & Wire Logic

### 3.1 `TorchObjectPatch` & `WallTorchObjectPatch`
- **`isActive` Interception**: Overrides active state evaluation using direct bitmask inspection:
  ```java
  returned = !level.wireManager.isWireActiveAny(tileX, tileY);
  ```
  Eliminates stream iteration over wire colors, resolving in $O(1)$ time per check.
- **`onWireUpdate` Hook**: Triggers localized static light invalidation:
  ```java
  level.lightManager.updateStaticLight(tileX, tileY, tileX, tileY, true);
  ```
  Forces immediate recalculation of light values upon wire logic state transitions.
- **`tickEffect` Optimization**: Suppresses client particle calculations if the fixture is inactive or executed on dedicated servers.

### 3.2 `StaticLightUpdaterPatch`
- Hooks into engine static light calculation passes to optimize tile bounds scanning and thread dispatching.

### 3.3 `ServerClientConnectPatch`
- Intercepts player initial connection events (`onFirstConnecting`) to dispatch `PacketModConfigSync` immediately following protocol handshakes, ensuring client configuration parity with zero desync.

---

## 4. AutoTorch Integration Matrix

The mod integrates directly with the **AutoTorch** framework through `BetterAutoTorchServerListener`, offering selectable inventory consumption and spatial strategies:

### 4.1 Selection Strategies (`autoTorchTorchSelection`)
| Strategy | Description |
| :--- | :--- |
| `BETTER_THEN_VANILLA` | Consumes Better Torches first; falls back to vanilla torches if empty. |
| `VANILLA_THEN_BETTER` | Consumes vanilla torches first; falls back to Better Torches if empty. |
| `BETTER_ONLY` | Strictly consumes Better Torches; ignores vanilla torches in inventory. |
| `VANILLA_ONLY` | Strictly consumes vanilla torches; ignores Better Torches. |
| `ANY_LIGHT` | Dynamic scan evaluating all lighting fixtures in player inventory. |

### 4.2 Priority Ordering (`autoTorchAnyLightPriority`)
When operating in `ANY_LIGHT` mode:
- **`SLOT_ORDER`**: Consumes items sequentially by inventory slot index (0 to 39).
- **`BRIGHTEST_FIRST`**: Sorts candidates descending by effective light level ($\text{Phoenix Lamp} \to \text{Abyss Lantern} \to \text{Steel/Wooden Lantern} \to \text{Better Torch} \to \text{Vanilla Torch}$).
- **`CONSERVATIVE_FIRST`**: Sorts candidates ascending by effective light level to conserve high-tier materials.

### 4.3 Terrain Substrate Detection
In `ANY_LIGHT` mode, the placement engine checks tile substrate via `level.isLiquidTile(targetX, targetY)`:
- **Liquid / Water Tiles**: Selects aquatic-capable fixtures (`AbyssLanternObject`, vanilla water lanterns).
- **Solid Terrain**: Selects standard ground/wall fixtures (`BetterTorchObject`, `SteelLampPostObject`, `WoodenHangingLanternObject`, `PhoenixLampObject`).

---

## 5. Crafting Matrix & Item Economy

| Item ID | Display Name | Workstation | Ingredients | Output |
| :--- | :--- | :--- | :--- | :--- |
| `bettertorch` | Better Torch | Handcrafting / Workstation | 4x `torch` *(Configurable: EASY=2, MEDIUM=4, HARD=4+4 stone)* | 1x |
| `torch` | Vanilla Torch (Uncraft) | Handcrafting / Workstation | 1x `bettertorch` | 4x *(EASY=2, MEDIUM=4, HARD=4)* |
| `steelLampPost` | Steel Lamp Post | Iron Anvil | 1x `ironbar` + 1x `torch` | 1x |
| `woodenHangingLantern` | Wooden Hanging Lantern | Carpenter | 1x `anylog` + 1x `torch` | 1x |
| `phoenixLamp` | Phoenix Lamp Post | Iron Anvil | 1x `ironbar` + 1x `goldbar` + 3x `torch` | 1x |
| `abyssLantern` | Abyssal Crystal Lantern (Upgrade) | Workstation | 1x `waterlantern` + 1x `quartz` + 1x `anygem` | 1x |
| `abyssLantern` | Abyssal Crystal Lantern (Direct) | Carpenter | 1x `torch` + 1x `anylog` + 1x `quartz` | 1x |

---

## 6. Configuration Reference

Configuration file path: `<game_save_directory>/settings/lighting/settings.cfg`

```properties
# ==============================================================================
# Better Torch Configuration
# ==============================================================================
torchLightLevel=300
torchLightHue=50.0
torchLightSat=0.2
recipeDifficulty=MEDIUM

# ==============================================================================
# Outdoor & Aquatic Fixture Configuration
# ==============================================================================
steelLampLightLevel=350
woodenLanternLightLevel=350
phoenixLampLightLevel=550
abyssLanternLightLevel=450
outdoorLampHue=50.0
outdoorLampSat=0.2
abyssLanternHue=30.0
abyssLanternSat=0.75

# ==============================================================================
# AutoTorch Mod Integration & Travel Optimization
# ==============================================================================
autoTorchIntegration=true
autoTorchTorchSelection=BETTER_THEN_VANILLA
autoTorchAnyLightPriority=SLOT_ORDER
autoTorchConsiderAllLights=true
autoTorchDynamicTravelSpacing=true
autoTorchMinLightThreshold=90
```

### Parameter Specification:
| Property | Type | Range / Options | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `torchLightLevel` | Integer | `50` - `5000` | `300` | Static light intensity for Better Torches. |
| `torchLightHue` | Float | `0.0` - `360.0` | `50.0` | Light color hue component for Better Torches. |
| `torchLightSat` | Float | `0.0` - `1.0` | `0.2` | Light color saturation component for Better Torches. |
| `recipeDifficulty` | Enum | `EASY`, `MEDIUM`, `HARD` | `MEDIUM` | Crafting ingredient cost scaling. |
| `steelLampLightLevel` | Integer | `50` - `5000` | `350` | Light intensity for Steel Lamp Posts. |
| `woodenLanternLightLevel` | Integer | `50` - `5000` | `350` | Light intensity for Wooden Hanging Lanterns. |
| `phoenixLampLightLevel` | Integer | `50` - `5000` | `550` | Light intensity for Phoenix Lamp Posts. |
| `abyssLanternLightLevel` | Integer | `50` - `5000` | `450` | Light intensity for Abyssal Crystal Lanterns. |
| `outdoorLampHue` | Float | `0.0` - `360.0` | `50.0` | Light color hue for street lamps and lanterns. |
| `outdoorLampSat` | Float | `0.0` - `1.0` | `0.2` | Light color saturation for street lamps and lanterns. |
| `abyssLanternHue` | Float | `0.0` - `360.0` | `30.0` | Light color hue for Abyssal Crystal Lanterns. |
| `abyssLanternSat` | Float | `0.0` - `1.0` | `0.75` | Light color saturation for Abyssal Crystal Lanterns. |
| `autoTorchIntegration` | Boolean | `true`, `false` | `true` | Enables high-performance AutoTorch hook listener. |
| `autoTorchTorchSelection` | Enum | `BETTER_THEN_VANILLA`, `VANILLA_THEN_BETTER`, `BETTER_ONLY`, `VANILLA_ONLY`, `ANY_LIGHT` | `BETTER_THEN_VANILLA` | Priority strategy for inventory torch selection. |
| `autoTorchAnyLightPriority` | Enum | `SLOT_ORDER`, `BRIGHTEST_FIRST`, `CONSERVATIVE_FIRST` | `SLOT_ORDER` | Sorting strategy when in `ANY_LIGHT` mode. |
| `autoTorchConsiderAllLights`| Boolean | `true`, `false` | `true` | Enables surrounding light source detection. |
| `autoTorchDynamicTravelSpacing` | Boolean | `true`, `false` | `true` | Enables velocity lookahead and dynamic spacing. |
| `autoTorchMinLightThreshold` | Integer | `30` - `200` | `90` | Minimum static light threshold triggering placement. |

---

## 7. Build, Testing & Deployment

### 7.1 Gradle Build Pipeline
Build the compiled JAR archive into `build/jar/`:
```bash
./gradlew clean build
```

### 7.2 Automated Test Suite
The project includes 142 automated unit, integration, and asset validation tests across the following test packages:
- `xeraphire.lighting.autotorch`: Velocity projection, spatial scanning, dynamic radius, hysteresis, and terrain awareness tests.
- `xeraphire.lighting.model`: Fixture properties, light guardrails, item generation, and tool types.
- `xeraphire.lighting.config`: Parsing, clamping, sync state, and recipe difficulty matrix tests.
- `xeraphire.lighting.network`: Binary packet serialization roundtrip and synchronization tests.
- `xeraphire.lighting.assets`: PNG headers, texture dimensions, localization keys, and JAR packaging structure tests.

Execute test suite:
```bash
./gradlew test
```

### 7.3 Steam Workshop Deployment
The repository includes an automated upload tool [`upload_workshop.sh`](file:///Users/dika/Project/Games/necesse-better-torch/upload_workshop.sh) utilizing `workshop_item.vdf`:
```bash
./upload_workshop.sh
```

---

## 8. Metadata
- **Mod ID**: `xeraphire.lighting`
- **Mod Name**: `Let there be Light!`
- **Mod Version**: `1.0.0`
- **Target Game Version**: `1.3.3`
- **Authors**: `Xeraphire`, `Erick Hasse`, `Bolo`
- **Repository**: [https://github.com/mahardikamaulana/necesse-better-torch](https://github.com/mahardikamaulana/necesse-better-torch)
