package xeraphire.lighting;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.engine.registries.PacketRegistry;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import xeraphire.lighting.model.AbyssLanternObject;
import xeraphire.lighting.model.BetterTorchObject;
import xeraphire.lighting.model.BetterWallTorchObject;
import xeraphire.lighting.model.PhoenixLampObject;
import xeraphire.lighting.model.SteelLampPostObject;
import xeraphire.lighting.model.WoodenHangingLanternObject;
import xeraphire.lighting.network.PacketModConfigSync;

@ModEntry
public class LightingMod {

    private static Config config;

    public static Config getConfig() {
        return config;
    }

    public void preInit() {
        System.out.println("Pre-Init Let there be Light! Mod...");
        config = new Config("settings.cfg");
        xeraphire.lighting.autotorch.BetterAutoTorchServerListener.LightCache.init(config);
        System.out.println("Let there be Light! Mod initialized successfully.");
    }

    public void init() {
        System.out.println("Registering objects and packets for Let there be Light! Mod...");

        // Pre-warm particle and projectile classes to avoid first-placement JIT compilation stalls
        try {
            Class.forName("necesse.entity.projectile.BombProjectile");
            Class.forName("necesse.entity.particle.ParticleOption");
        } catch (Throwable ignored) {
        }

        // Register Network Packets
        PacketRegistry.registerPacket(PacketModConfigSync.class);

        // 1. Register Better Wall Torch (internal wall decor layer object)
        ObjectRegistry.registerObject(
                "betterwalltorch",
                new BetterWallTorchObject(config.getTorchLightHue(), config.getTorchLightSat(), config.getTorchLightLevel()),
                0.0f,
                false
        );

        // 2. Register Better Torch with wall-mount link
        BetterTorchObject betterTorch = new BetterTorchObject(config.getTorchLightHue(), config.getTorchLightSat(), config.getTorchLightLevel());
        betterTorch.setWallPlaceObjectStringID("betterwalltorch");
        ObjectRegistry.registerObject(
                "bettertorch",
                betterTorch,
                config.getTorchBrokerValue(),
                true
        );

        // Register Outdoor Lighting Fixtures
        ObjectRegistry.registerObject(
                "steelLampPost",
                new SteelLampPostObject(config.getSteelLampLightLevel(), config.getOutdoorLampHue(), config.getOutdoorLampSat()),
                0.0f,
                true
        );

        ObjectRegistry.registerObject(
                "woodenHangingLantern",
                new WoodenHangingLanternObject(config.getWoodenLanternLightLevel(), config.getOutdoorLampHue(), config.getOutdoorLampSat()),
                0.0f,
                true
        );

        ObjectRegistry.registerObject(
                "phoenixLamp",
                new PhoenixLampObject(config.getPhoenixLampLightLevel(), config.getOutdoorLampHue(), config.getOutdoorLampSat()),
                0.0f,
                true
        );

        ObjectRegistry.registerObject(
                "abyssLantern",
                new AbyssLanternObject(config.getAbyssLanternLightLevel(), config.getAbyssLanternHue(), config.getAbyssLanternSat()),
                0.0f,
                true
        );
    }

    public void initResources() {
        // Pre-warm and cache GameTextures to eliminate first-placement disk I/O latency
        try {
            // Object textures
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/bettertorch");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/bettertorch_off");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/betterwalltorch");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/steelLampPost");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/steelLampPost_off");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/woodenHangingLantern");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/woodenHangingLantern_off");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/phoenixLamp");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/phoenixLamp_off");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/abyssLantern");
            necesse.gfx.gameTexture.GameTexture.fromFile("objects/abyssLantern_off");

            // Item inventory textures
            necesse.gfx.gameTexture.GameTexture.fromFile("items/bettertorch");
            necesse.gfx.gameTexture.GameTexture.fromFile("items/steelLampPost");
            necesse.gfx.gameTexture.GameTexture.fromFile("items/woodenHangingLantern");
            necesse.gfx.gameTexture.GameTexture.fromFile("items/phoenixLamp");
            necesse.gfx.gameTexture.GameTexture.fromFile("items/abyssLantern");
        } catch (Throwable t) {
            System.err.println("[Let there be Light!] Resource pre-warming error: " + t.getMessage());
        }
    }

    public void postInit() {
        System.out.println("Registering recipes for Let there be Light! Mod...");

        // Better Torch Recipes
        Recipes.registerModRecipe(config.getTorchCraftingRecipe());
        Recipes.registerModRecipe(config.getTorchUncraftingRecipe());

        // Outdoor & Aquatic Lighting Recipes
        Recipes.registerModRecipe(new Recipe(
                "steelLampPost",
                1,
                RecipeTechRegistry.IRON_ANVIL,
                new Ingredient[]{
                        new Ingredient("ironbar", 1),
                        new Ingredient("torch", 1)
                }
        ).showAfter("torch"));

        Recipes.registerModRecipe(new Recipe(
                "woodenHangingLantern",
                1,
                RecipeTechRegistry.CARPENTER,
                new Ingredient[]{
                        new Ingredient("anylog", 1),
                        new Ingredient("torch", 1)
                }
        ).showAfter("torch"));

        Recipes.registerModRecipe(new Recipe(
                "phoenixLamp",
                1,
                RecipeTechRegistry.IRON_ANVIL,
                new Ingredient[]{
                        new Ingredient("ironbar", 1),
                        new Ingredient("goldbar", 1),
                        new Ingredient("torch", 3)
                }
        ).showAfter("torch"));

        // Abyssal Crystal Lantern (Upgraded Water Lantern)
        Recipes.registerModRecipe(new Recipe(
                "abyssLantern",
                1,
                RecipeTechRegistry.WORKSTATION,
                new Ingredient[]{
                        new Ingredient("waterlantern", 1),
                        new Ingredient("quartz", 1),
                        new Ingredient("anygem", 1)
                }
        ).showAfter("waterlantern"));

        Recipes.registerModRecipe(new Recipe(
                "abyssLantern",
                1,
                RecipeTechRegistry.CARPENTER,
                new Ingredient[]{
                        new Ingredient("torch", 1),
                        new Ingredient("anylog", 1),
                        new Ingredient("quartz", 1)
                }
        ).showAfter("waterlantern"));
    }
}
