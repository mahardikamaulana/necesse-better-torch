package xeraphire.lighting.autotorch;

import autotorch.AutoTorchMod;
import necesse.engine.GameEventInterface;
import necesse.engine.commands.ChatCommand;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.gameLoop.GameLoopListener;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.Packet;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.network.server.network.ServerNetwork;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventory;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.TorchObject;
import necesse.level.gameTile.GameTile;
import necesse.level.maps.Level;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.model.OutdoorLampObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class BetterAutoTorchServerListener implements GameEventInterface<ServerStartEvent> {

    private Runnable disposer;

    @Override
    public void init(Runnable disposer) {
        this.disposer = disposer;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void onEvent(ServerStartEvent event) {
        Server server = event.server;
        if (server == null) {
            return;
        }

        try {
            // Register AutoTorch Chat Command via reflection
            Class<?> cmdClass = Class.forName("autotorch.AutoTorchMod$AutoTorchCommand");
            Constructor<?> constructor = cmdClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            ChatCommand command = (ChatCommand) constructor.newInstance();
            server.commandsManager.getServerCommands().add(command);
        } catch (Exception e) {
            System.err.println("[Let there be Light!] Error registering AutoTorch command: " + e.getMessage());
        }

        // Initialize fast-path primitive lookup cache
        LightCache.init(LightingMod.getConfig());

        // Attach Enhanced Game Loop Listener to Server Loop
        server.serverThread.gameLoop.addGameLoopListener(new BetterAutoTorchGameLoop(server));
        System.out.println("[Let there be Light!] Enhanced AutoTorch high-performance loop listener registered.");
    }

    /**
     * O(1) primitive array lookup table for light properties indexed by integer objectID.
     */
    public static class LightCache {
        public static int MAX_LIGHT_RADIUS = 20;
        public static int[] LIGHT_RADIUS_CACHE;
        public static int[] ITEM_LIGHT_LEVEL_CACHE;
        public static boolean[] IS_LIGHT_SOURCE;
        public static boolean[] CAN_PLACE_ON_LIQUID;
        public static int[] WALL_VARIANT_ID_CACHE;

        public static void init(Config config) {
            int count = Math.max(ObjectRegistry.getObjectsCount() + 128, 2048);
            LIGHT_RADIUS_CACHE = new int[count];
            ITEM_LIGHT_LEVEL_CACHE = new int[count];
            IS_LIGHT_SOURCE = new boolean[count];
            CAN_PLACE_ON_LIQUID = new boolean[count];
            WALL_VARIANT_ID_CACHE = new int[count];

            for (int i = 0; i < count; i++) {
                WALL_VARIANT_ID_CACHE[i] = -1;
            }

            int betterTorchID = ObjectRegistry.getObjectID("bettertorch");
            int betterWallTorchID = ObjectRegistry.getObjectID("betterwalltorch");
            int torchID = ObjectRegistry.getObjectID("torch");
            int wallTorchID = ObjectRegistry.getObjectID("walltorch");
            int abyssLanternID = ObjectRegistry.getObjectID("abyssLantern");
            int phoenixLampID = ObjectRegistry.getObjectID("phoenixLamp");
            int steelLampID = ObjectRegistry.getObjectID("steelLampPost");
            int woodenLanternID = ObjectRegistry.getObjectID("woodenHangingLantern");
            int waterLanternID = ObjectRegistry.getObjectID("waterlantern");

            if (betterTorchID != -1 && betterWallTorchID != -1) {
                WALL_VARIANT_ID_CACHE[betterTorchID] = betterWallTorchID;
            }
            if (torchID != -1 && wallTorchID != -1) {
                WALL_VARIANT_ID_CACHE[torchID] = wallTorchID;
            }

            int maxRadius = 5;

            for (int i = 0; i < ObjectRegistry.getObjectsCount(); i++) {
                GameObject obj = ObjectRegistry.getObject(i);
                if (obj == null) continue;

                int lightLevel = 0;

                if (i == betterTorchID || i == betterWallTorchID) {
                    lightLevel = config != null ? config.getTorchLightLevel() : 300;
                } else if (i == phoenixLampID) {
                    lightLevel = config != null ? config.getPhoenixLampLightLevel() : 550;
                } else if (i == abyssLanternID) {
                    lightLevel = config != null ? config.getAbyssLanternLightLevel() : 450;
                } else if (i == steelLampID) {
                    lightLevel = config != null ? config.getSteelLampLightLevel() : 350;
                } else if (i == woodenLanternID) {
                    lightLevel = config != null ? config.getWoodenLanternLightLevel() : 350;
                } else if (i == torchID || i == wallTorchID || i == waterLanternID) {
                    lightLevel = 150;
                } else if (obj instanceof TorchObject || obj instanceof OutdoorLampObject || obj.roomProperties.contains("lights") || obj.lightLevel > 0) {
                    lightLevel = Math.max(100, obj.lightLevel);
                }

                if (lightLevel > 0) {
                    IS_LIGHT_SOURCE[i] = true;
                    int radius = Math.max(5, Math.min(20, Math.round(lightLevel / 30.0f)));
                    LIGHT_RADIUS_CACHE[i] = radius;
                    ITEM_LIGHT_LEVEL_CACHE[i] = lightLevel;
                    if (radius > maxRadius) {
                        maxRadius = radius;
                    }
                }

                if (i == abyssLanternID || i == waterLanternID) {
                    CAN_PLACE_ON_LIQUID[i] = true;
                }
            }

            MAX_LIGHT_RADIUS = Math.max(20, maxRadius);
        }
    }

    public static class BetterAutoTorchGameLoop implements GameLoopListener {
        private final Server server;
        private final Map<Long, Long> lastPlaceTime = new HashMap<>();
        private final Map<Long, Boolean> wasUnderground = new HashMap<>();

        public static class RecentPlacement {
            public final int tileX;
            public final int tileY;
            public final int lightRadius;
            public final long timestamp;

            public RecentPlacement(int tileX, int tileY, int lightRadius, long timestamp) {
                this.tileX = tileX;
                this.tileY = tileY;
                this.lightRadius = lightRadius;
                this.timestamp = timestamp;
            }
        }
        private final Map<Long, RecentPlacement> recentPlacements = new HashMap<>();

        // Static zero-allocation spiral search offsets
        public static final int[] SPIRAL_DX = {
             0,  1, -1,  0,  1, -1,  1, -1,
             2, -2,  0,  0,  2, -2,  2, -2,  1, -1,  1, -1,  2, -2,  2, -2
        };
        public static final int[] SPIRAL_DY = {
            -1,  0,  0,  1, -1, -1,  1,  1,
             0,  0,  2, -2,  1,  1, -1, -1,  2,  2, -2, -2,  2,  2, -2, -2
        };

        public BetterAutoTorchGameLoop(Server server) {
            this.server = server;
        }

        @Override
        public void frameTick(TickManager tickManager, GameWindow gameWindow) {
            long now = System.currentTimeMillis();
            Config config = LightingMod.getConfig();

            if (config != null && !config.isAutoTorchIntegration()) {
                return;
            }

            // Ensure cache is populated
            if (LightCache.IS_LIGHT_SOURCE == null) {
                LightCache.init(config);
            }

            for (ServerClient client : server.getClients()) {
                PlayerMob player = client.playerMob;
                if (player == null) {
                    continue;
                }

                long playerID = player.getUniqueID();

                // Fast-path 1: Player health check
                if (player.getHealth() <= 0) {
                    if (AutoTorchMod.playerEnabled.getOrDefault(playerID, false)) {
                        AutoTorchMod.playerEnabled.put(playerID, false);
                        sendTogglePacket(server.network, playerID, false, false);
                    }
                    wasUnderground.remove(playerID);
                    recentPlacements.remove(playerID);
                    continue;
                }

                // Handle Underground Auto-Toggle
                if (AutoTorchMod.underground) {
                    Level level = player.getLevel();
                    if (level != null) {
                        boolean isCave = level.isCave;
                        Boolean prevUnderground = wasUnderground.get(playerID);
                        if (prevUnderground != null && prevUnderground != isCave) {
                            boolean currentEnabled = AutoTorchMod.playerEnabled.getOrDefault(playerID, false);
                            if (currentEnabled != isCave) {
                                AutoTorchMod.playerEnabled.put(playerID, isCave);
                                sendTogglePacket(server.network, playerID, isCave, true);
                            }
                        }
                        wasUnderground.put(playerID, isCave);
                    }
                } else {
                    wasUnderground.remove(playerID);
                }

                // Fast-path 2: Check if AutoTorch is enabled for this player
                if (!AutoTorchMod.playerEnabled.getOrDefault(playerID, false)) {
                    continue;
                }

                // Fast-path 3: Placement cooldown short-circuit
                Long lastPlaced = lastPlaceTime.get(playerID);
                if (lastPlaced != null && (now - lastPlaced) < AutoTorchMod.currentCooldown) {
                    continue;
                }

                Level level = player.getLevel();
                if (level == null) {
                    continue;
                }

                int tileX = player.getX() / 32;
                int tileY = player.getY() / 32;
                int dir = player.getDir();

                // 1. Single-pass inventory scan for optimal torch candidate
                String selectionMode = config != null ? config.getAutoTorchTorchSelection() : "BETTER_THEN_VANILLA";
                String anyLightPriority = config != null ? config.getAutoTorchAnyLightPriority() : "SLOT_ORDER";
                TorchCandidate candidate = findTorchCandidate(player, false, selectionMode, anyLightPriority);
                if (candidate == null) {
                    continue;
                }

                boolean dynamicTravelSpacing = config == null || config.isAutoTorchDynamicTravelSpacing();
                int[] targetCoords = calculateTargetTile(tileX, tileY, player.dx, player.dy, dir, AutoTorchMod.currentDistance, candidate.lightLevel, dynamicTravelSpacing);
                int targetX = targetCoords[0];
                int targetY = targetCoords[1];

                // Fast-path 4: Recent placement hysteresis check (avoids clustering during async static light calculation)
                RecentPlacement recent = recentPlacements.get(playerID);
                if (recent != null && (now - recent.timestamp) < 4000) {
                    int distFromRecent = Math.max(Math.abs(targetX - recent.tileX), Math.abs(targetY - recent.tileY));
                    if (distFromRecent <= recent.lightRadius) {
                        continue;
                    }
                }

                // Fast-path 5: O(1) Static map light check (if already brightly lit, skip!)
                float minLightThreshold = config != null ? (float) config.getAutoTorchMinLightThreshold() : 90.0f;
                float staticLight = level.getStaticLightLevelFloat(targetX, targetY);
                if (staticLight >= minLightThreshold) {
                    continue;
                }

                // Check terrain liquid status
                GameTile targetTile = level.getTile(targetX, targetY);
                boolean isLiquid = targetTile != null && targetTile.isLiquid;
                if (isLiquid && !candidate.canPlaceOnLiquid) {
                    candidate = findTorchCandidate(player, true, selectionMode, anyLightPriority);
                    if (candidate == null) {
                        continue;
                    }
                }

                GameObject torchObj = candidate.gameObject;
                int itemLightLevel = candidate.lightLevel;
                int dynamicRadius = calculateDynamicRadius(itemLightLevel, AutoTorchMod.currentRadius);

                // 2. Fast spatial nearby check using O(1) objectID table with full scan range
                boolean considerAllLights = config == null || config.isAutoTorchConsiderAllLights();
                if (isTorchNearby(targetX, targetY, level, dynamicRadius, considerAllLights)) {
                    continue;
                }

                // 3. Zero-allocation placement validation & spiral search
                int placeX = targetX;
                int placeY = targetY;
                boolean canLiquid = candidate.canPlaceOnLiquid;
                boolean validPlacement = isTileValidForTorch(level, placeX, placeY, torchObj, canLiquid);

                if (!validPlacement) {
                    for (int s = 0; s < SPIRAL_DX.length; s++) {
                        int sx = targetX + SPIRAL_DX[s];
                        int sy = targetY + SPIRAL_DY[s];
                        if (isTileValidForTorch(level, sx, sy, torchObj, canLiquid)) {
                            placeX = sx;
                            placeY = sy;
                            validPlacement = true;
                            break;
                        }
                    }
                }

                if (!validPlacement) {
                    continue;
                }

                // 4. Place torch server-side and broadcast packet
                if (placeTorchServerSide(server, level, placeX, placeY, client, candidate.slotIndex, candidate.item, torchObj)) {
                    lastPlaceTime.put(playerID, now);
                    int placedRadius = torchObj.getID() >= 0 && torchObj.getID() < LightCache.LIGHT_RADIUS_CACHE.length
                            ? LightCache.LIGHT_RADIUS_CACHE[torchObj.getID()]
                            : dynamicRadius / 2;
                    recentPlacements.put(playerID, new RecentPlacement(placeX, placeY, placedRadius, now));
                }
            }
        }

        @Override
        public void drawTick(TickManager tickManager) {
        }

        @Override
        public boolean isDisposed() {
            return false;
        }

        public static int calculateDynamicRadius(int itemLightLevel, int baseRadius) {
            float scale = (float) itemLightLevel / 150.0f;
            int dynamicRadius = Math.round(baseRadius * scale);
            return Math.max(8, Math.min(40, dynamicRadius));
        }

        public static int[] calculateTargetTile(int tileX, int tileY, float moveDx, float moveDy, int dir, int baseDistance, int lightLevel, boolean dynamicTravelSpacing) {
            int lookahead = baseDistance;
            if (dynamicTravelSpacing && lightLevel > 150) {
                int dynamicRadius = calculateDynamicRadius(lightLevel, 10);
                lookahead = Math.max(baseDistance, Math.min(16, Math.round(dynamicRadius / 2.0f)));
            }

            int targetX = tileX;
            int targetY = tileY;

            // Movement vector projection with diagonal travel support
            if (Math.abs(moveDx) > 0.01f || Math.abs(moveDy) > 0.01f) {
                int stepX = moveDx > 0.2f ? lookahead : (moveDx < -0.2f ? -lookahead : 0);
                int stepY = moveDy > 0.2f ? lookahead : (moveDy < -0.2f ? -lookahead : 0);
                if (stepX != 0 || stepY != 0) {
                    return new int[]{tileX + stepX, tileY + stepY};
                }
            }

            // Cardinal direction fallback
            switch (dir) {
                case 0: // UP
                    targetY -= lookahead;
                    break;
                case 1: // RIGHT
                    targetX += lookahead;
                    break;
                case 2: // DOWN
                    targetY += lookahead;
                    break;
                case 3: // LEFT
                    targetX -= lookahead;
                    break;
            }
            return new int[]{targetX, targetY};
        }

        /**
         * Fast O(1) spatial nearby light scanner with full fixture radius coverage.
         */
        public boolean isTorchNearby(int centerX, int centerY, Level level, int radius, boolean considerAllLights) {
            int candidateHalf = radius / 2;
            int scanRange = considerAllLights ? Math.max(candidateHalf, LightCache.MAX_LIGHT_RADIUS) : candidateHalf;
            int maxId = LightCache.IS_LIGHT_SOURCE.length;

            for (int dx = -scanRange; dx <= scanRange; dx++) {
                for (int dy = -scanRange; dy <= scanRange; dy++) {
                    int checkX = centerX + dx;
                    int checkY = centerY + dy;
                    int objID = level.getObjectID(checkX, checkY);
                    if (objID > 0 && objID < maxId && LightCache.IS_LIGHT_SOURCE[objID]) {
                        if (!considerAllLights) {
                            if (objID == ObjectRegistry.getObjectID("torch") || objID == ObjectRegistry.getObjectID("bettertorch")) {
                                if (Math.max(Math.abs(dx), Math.abs(dy)) <= candidateHalf) {
                                    return true;
                                }
                            }
                        } else {
                            int fixtureRadius = LightCache.LIGHT_RADIUS_CACHE[objID];
                            int dist = Math.max(Math.abs(dx), Math.abs(dy));
                            if (dist <= fixtureRadius) {
                                return true; // Target tile is within the illumination zone of this fixture
                            }
                            if (dist <= candidateHalf && fixtureRadius >= candidateHalf) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
         * High-performance single-pass inventory scanner supporting all selection modes,
         * terrain compatibility (liquid vs land), and ANY_LIGHT priorities.
         */
        public TorchCandidate findTorchCandidate(PlayerMob player, boolean isTargetLiquid,
                                                 String selectionMode, String anyLightPriority) {
            PlayerInventory inv = player.getInv().main;
            if (inv == null) {
                return null;
            }

            int invSize = inv.getSize();
            int maxId = LightCache.IS_LIGHT_SOURCE.length;

            if ("ANY_LIGHT".equals(selectionMode)) {
                TorchCandidate bestCandidate = null;
                for (int i = 0; i < invSize; i++) {
                    InventoryItem invItem = inv.getItem(i);
                    if (invItem == null || invItem.item == null) continue;

                    int objID = getObjectItemID(invItem.item);
                    if (objID <= 0 || objID >= maxId || !LightCache.IS_LIGHT_SOURCE[objID]) {
                        continue;
                    }

                    boolean canLiquid = LightCache.CAN_PLACE_ON_LIQUID[objID];
                    if (isTargetLiquid && !canLiquid) {
                        continue; // Cannot place on water
                    }

                    GameObject obj = ObjectRegistry.getObject(objID);
                    if (obj == null) continue;

                    int light = LightCache.ITEM_LIGHT_LEVEL_CACHE[objID];
                    TorchCandidate current = new TorchCandidate(i, invItem, obj, light, canLiquid);

                    if ("SLOT_ORDER".equals(anyLightPriority)) {
                        return current; // First matching slot
                    } else if ("BRIGHTEST_FIRST".equals(anyLightPriority)) {
                        if (bestCandidate == null || light > bestCandidate.lightLevel) {
                            bestCandidate = current;
                        }
                    } else if ("CONSERVATIVE_FIRST".equals(anyLightPriority)) {
                        if (bestCandidate == null || light < bestCandidate.lightLevel) {
                            bestCandidate = current;
                        }
                    }
                }
                return bestCandidate;
            }

            // Direct mode resolution (BETTER_THEN_VANILLA, VANILLA_THEN_BETTER, etc.)
            int primaryID = -1;
            int fallbackID = -1;

            int betterTorchID = ObjectRegistry.getObjectID("bettertorch");
            int vanillaTorchID = ObjectRegistry.getObjectID("torch");

            switch (selectionMode) {
                case "VANILLA_THEN_BETTER":
                    primaryID = vanillaTorchID;
                    fallbackID = betterTorchID;
                    break;
                case "BETTER_ONLY":
                    primaryID = betterTorchID;
                    break;
                case "VANILLA_ONLY":
                    primaryID = vanillaTorchID;
                    break;
                case "BETTER_THEN_VANILLA":
                default:
                    primaryID = betterTorchID;
                    fallbackID = vanillaTorchID;
                    break;
            }

            // 1. Primary ID scan
            if (primaryID != -1 && (!isTargetLiquid || LightCache.CAN_PLACE_ON_LIQUID[primaryID])) {
                for (int i = 0; i < invSize; i++) {
                    InventoryItem invItem = inv.getItem(i);
                    if (invItem != null && invItem.item != null && getObjectItemID(invItem.item) == primaryID) {
                        GameObject obj = ObjectRegistry.getObject(primaryID);
                        if (obj != null) {
                            return new TorchCandidate(i, invItem, obj, LightCache.ITEM_LIGHT_LEVEL_CACHE[primaryID], LightCache.CAN_PLACE_ON_LIQUID[primaryID]);
                        }
                    }
                }
            }

            // 2. Fallback ID scan
            if (fallbackID != -1 && (!isTargetLiquid || LightCache.CAN_PLACE_ON_LIQUID[fallbackID])) {
                for (int i = 0; i < invSize; i++) {
                    InventoryItem invItem = inv.getItem(i);
                    if (invItem != null && invItem.item != null && getObjectItemID(invItem.item) == fallbackID) {
                        GameObject obj = ObjectRegistry.getObject(fallbackID);
                        if (obj != null) {
                            return new TorchCandidate(i, invItem, obj, LightCache.ITEM_LIGHT_LEVEL_CACHE[fallbackID], LightCache.CAN_PLACE_ON_LIQUID[fallbackID]);
                        }
                    }
                }
            }

            return null;
        }

        private static int getObjectItemID(Item item) {
            if (item instanceof ObjectItem) {
                return ((ObjectItem) item).objectID;
            }
            return ObjectRegistry.getObjectID(item.getStringID());
        }

        static boolean isTileValidForTorch(Level level, int tileX, int tileY, GameObject torchObj, boolean canPlaceOnLiquid) {
            GameTile tile = level.getTile(tileX, tileY);
            if (tile == null || (!canPlaceOnLiquid && tile.isLiquid) || level.isSolidTile(tileX, tileY)) {
                return false;
            }
            GameTile aboveTile = level.getTile(tileX, tileY - 1);
            if (aboveTile != null && (!canPlaceOnLiquid && aboveTile.isLiquid || level.isSolidTile(tileX, tileY - 1))) {
                return false;
            }
            return findTorchPlaceLayer(level, tileX, tileY, torchObj) != -1;
        }

        private static int findTorchPlaceLayer(Level level, int tileX, int tileY, GameObject torchObj) {
            LinkedHashSet<Integer> validLayers = torchObj.getValidObjectLayers();
            for (int layer : validLayers) {
                if (torchObj.canPlace(level, layer, tileX, tileY, 0, true, false) == null) {
                    return layer;
                }
            }
            return -1;
        }

        private boolean placeTorchServerSide(Server server, Level level, int tileX, int tileY,
                                            ServerClient client, int slotIndex, InventoryItem torchItem, GameObject torchObj) {
            int placeLayer = findTorchPlaceLayer(level, tileX, tileY, torchObj);
            if (placeLayer == -1) {
                return false;
            }

            // Wall variant substitution if applicable
            GameObject finalPlacedObj = torchObj;
            if (placeLayer == 1) { // 1 = ObjectLayer.WALL / decor layer
                int objID = torchObj.getID();
                if (objID >= 0 && objID < LightCache.WALL_VARIANT_ID_CACHE.length) {
                    int wallID = LightCache.WALL_VARIANT_ID_CACHE[objID];
                    if (wallID != -1) {
                        GameObject wallObj = ObjectRegistry.getObject(wallID);
                        if (wallObj != null) {
                            finalPlacedObj = wallObj;
                        }
                    }
                }
            }

            // Place object in world
            finalPlacedObj.placeObject(level, placeLayer, tileX, tileY, 0, true);
            level.onObjectPlaced(finalPlacedObj, placeLayer, tileX, tileY, client);

            // Broadcast packet to all clients with exact object ID placed
            try {
                Packet placePacket = new AutoTorchMod.AutoTorchPlacePacket(tileX, tileY, placeLayer, finalPlacedObj.getID());
                server.network.sendToAllClients(placePacket);
            } catch (Exception e) {
                System.err.println("[Let there be Light!] Error sending AutoTorchPlacePacket: " + e.getMessage());
            }

            // Deduct 1 item from player inventory
            PlayerInventory inv = client.playerMob.getInv().main;
            if (inv != null) {
                int remaining = torchItem.getAmount() - 1;
                if (remaining <= 0) {
                    inv.setItem(slotIndex, null);
                } else {
                    torchItem.setAmount(remaining);
                }
                inv.markDirty(slotIndex);
            }
            return true;
        }

        private void sendTogglePacket(ServerNetwork network, long playerAuth, boolean newState, boolean underground) {
            try {
                Packet togglePacket = new AutoTorchMod.PacketAutoTorchToggle(playerAuth, newState, underground);
                network.sendToAllClients(togglePacket);
            } catch (Exception e) {
                System.err.println("[Let there be Light!] Error sending PacketAutoTorchToggle: " + e.getMessage());
            }
        }
    }

    public static class TorchCandidate {
        public final int slotIndex;
        public final InventoryItem item;
        public final GameObject gameObject;
        public final int lightLevel;
        public final boolean canPlaceOnLiquid;

        public TorchCandidate(int slotIndex, InventoryItem item, GameObject gameObject, int lightLevel, boolean canPlaceOnLiquid) {
            this.slotIndex = slotIndex;
            this.item = item;
            this.gameObject = gameObject;
            this.lightLevel = lightLevel;
            this.canPlaceOnLiquid = canPlaceOnLiquid;
        }
    }
}
