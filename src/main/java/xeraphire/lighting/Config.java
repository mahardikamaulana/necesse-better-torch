package xeraphire.lighting;

import necesse.engine.GlobalData;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class Config {

    private Float torchLightHue = 50.0F;
    private Float torchLightSat = 0.2F;
    private Integer torchLightLevel = 300;
    private String recipeDifficulty = "MEDIUM";

    private Integer steelLampLightLevel = 350;
    private Integer woodenLanternLightLevel = 350;
    private Integer phoenixLampLightLevel = 550;
    private Integer abyssLanternLightLevel = 450;
    private Float outdoorLampHue = 50.0F;
    private Float outdoorLampSat = 0.2F;
    private Float abyssLanternHue = 30.0F;
    private Float abyssLanternSat = 0.75F;

    private Boolean autoTorchIntegration = true;
    private String autoTorchTorchSelection = "BETTER_THEN_VANILLA";
    private String autoTorchAnyLightPriority = "SLOT_ORDER";
    private Boolean autoTorchConsiderAllLights = true;
    private Boolean autoTorchDynamicTravelSpacing = true;
    private Integer autoTorchMinLightThreshold = 90;

    public Config() {
        this("settings.cfg");
    }

    public Config(File file) {
        if (file != null) {
            try {
                if (!file.exists()) {
                    createNewFile(file);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
                    loadConfig(br);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Config(String configFileName) {
        System.out.println("Loading config for Let there be Light! Mod...");
        String filename = GlobalData.rootPath() + "/settings/lighting/" + configFileName;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                createNewFile(file);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
                loadConfig(br);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void loadConfig(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                String[] temp = line.split("=", 2);
                if (temp.length == 2) {
                    String key = temp[0].trim();
                    String val = temp[1].trim();
                    if (Objects.equals(key, "torchLightHue") || Objects.equals(key, "lightHue")) {
                        this.torchLightHue = Float.parseFloat(val);
                    } else if (Objects.equals(key, "torchLightSat") || Objects.equals(key, "lightSat")) {
                        this.torchLightSat = Float.parseFloat(val);
                    } else if (Objects.equals(key, "torchLightLevel") || Objects.equals(key, "lightLevel")) {
                        this.torchLightLevel = clamp(Integer.parseInt(val), 50, 5000);
                    } else if (Objects.equals(key, "recipeDifficulty")) {
                        this.recipeDifficulty = val.toUpperCase();
                    } else if (Objects.equals(key, "steelLampLightLevel")) {
                        this.steelLampLightLevel = clamp(Integer.parseInt(val), 50, 5000);
                    } else if (Objects.equals(key, "woodenLanternLightLevel")) {
                        this.woodenLanternLightLevel = clamp(Integer.parseInt(val), 50, 5000);
                    } else if (Objects.equals(key, "phoenixLampLightLevel")) {
                        this.phoenixLampLightLevel = clamp(Integer.parseInt(val), 50, 5000);
                    } else if (Objects.equals(key, "abyssLanternLightLevel")) {
                        this.abyssLanternLightLevel = clamp(Integer.parseInt(val), 50, 5000);
                    } else if (Objects.equals(key, "outdoorLampHue")) {
                        this.outdoorLampHue = Float.parseFloat(val);
                    } else if (Objects.equals(key, "outdoorLampSat")) {
                        this.outdoorLampSat = Float.parseFloat(val);
                    } else if (Objects.equals(key, "abyssLanternHue")) {
                        this.abyssLanternHue = Float.parseFloat(val);
                    } else if (Objects.equals(key, "abyssLanternSat")) {
                        this.abyssLanternSat = Float.parseFloat(val);
                    } else if (Objects.equals(key, "autoTorchIntegration")) {
                        this.autoTorchIntegration = Boolean.parseBoolean(val);
                    } else if (Objects.equals(key, "autoTorchTorchSelection")) {
                        this.autoTorchTorchSelection = normalizeTorchSelection(val);
                    } else if (Objects.equals(key, "autoTorchAnyLightPriority")) {
                        this.autoTorchAnyLightPriority = normalizeAnyLightPriority(val);
                    } else if (Objects.equals(key, "autoTorchPreferBetterTorch")) {
                        // Backward compatibility
                        if (Boolean.parseBoolean(val)) {
                            this.autoTorchTorchSelection = "BETTER_THEN_VANILLA";
                        } else {
                            this.autoTorchTorchSelection = "VANILLA_THEN_BETTER";
                        }
                    } else if (Objects.equals(key, "autoTorchConsiderAllLights")) {
                        this.autoTorchConsiderAllLights = Boolean.parseBoolean(val);
                    } else if (Objects.equals(key, "autoTorchDynamicTravelSpacing")) {
                        this.autoTorchDynamicTravelSpacing = Boolean.parseBoolean(val);
                    } else if (Objects.equals(key, "autoTorchMinLightThreshold")) {
                        this.autoTorchMinLightThreshold = clamp(Integer.parseInt(val), 30, 200);
                    }
                }
            }
        }
    }

    public void loadFromString(String content) {
        if (content != null) {
            try (BufferedReader br = new BufferedReader(new StringReader(content))) {
                loadConfig(br);
            } catch (IOException ignored) {
            }
        }
    }

    static String normalizeTorchSelection(String input) {
        if (input == null) {
            return "BETTER_THEN_VANILLA";
        }
        String upper = input.trim().toUpperCase();
        switch (upper) {
            case "BETTER_THEN_VANILLA":
            case "VANILLA_THEN_BETTER":
            case "BETTER_ONLY":
            case "VANILLA_ONLY":
            case "ANY_LIGHT":
                return upper;
            default:
                return "BETTER_THEN_VANILLA";
        }
    }

    static String normalizeAnyLightPriority(String input) {
        if (input == null) {
            return "SLOT_ORDER";
        }
        String upper = input.trim().toUpperCase();
        switch (upper) {
            case "SLOT_ORDER":
            case "BRIGHTEST_FIRST":
            case "CONSERVATIVE_FIRST":
                return upper;
            default:
                return "SLOT_ORDER";
        }
    }

    static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private void createNewFile(File file) throws IOException {
        if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
            throw new IOException("Error creating directory: " + file.getParentFile().toPath());
        }
        if (!file.createNewFile()) {
            throw new IOException("Error creating file: " + file.toPath());
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            writer.write("# Let there be Light! Mod Configuration\n");
            writer.write("# Lines starting with # are comments and will be ignored.\n\n");

            writer.write("# === Better Torch Settings ===\n");
            writer.write("# default torchLightLevel is 300 (2x vanilla torch)\n");
            writer.write("torchLightLevel=300\n");
            writer.write("# default torchLightHue is 50.0 (warm amber matching vanilla torch)\n");
            writer.write("torchLightHue=50.0\n");
            writer.write("# default torchLightSat is 0.2 (subtle flame saturation matching vanilla torch)\n");
            writer.write("torchLightSat=0.2\n");
            writer.write("# recipeDifficulty can be EASY, MEDIUM, or HARD\n");
            writer.write("# EASY: 2 torches -> 1 better torch\n");
            writer.write("# MEDIUM: 4 torches -> 1 better torch\n");
            writer.write("# HARD: 4 torches + 4 any stone -> 1 better torch\n");
            writer.write("recipeDifficulty=MEDIUM\n\n");

            writer.write("# === Outdoor Lighting Settings ===\n");
            writer.write("# default steelLampLightLevel is 350\n");
            writer.write("steelLampLightLevel=350\n");
            writer.write("# default woodenLanternLightLevel is 350\n");
            writer.write("woodenLanternLightLevel=350\n");
            writer.write("# default phoenixLampLightLevel is 550\n");
            writer.write("phoenixLampLightLevel=550\n");
            writer.write("# default abyssLanternLightLevel is 450 (3x water lantern)\n");
            writer.write("abyssLanternLightLevel=450\n");
            writer.write("# default outdoorLampHue is 50.0 (warm amber/yellow)\n");
            writer.write("outdoorLampHue=50.0\n");
            writer.write("# default outdoorLampSat is 0.2\n");
            writer.write("outdoorLampSat=0.2\n");
            writer.write("# default abyssLanternHue is 30.0 (warm aquatic glow matching vanilla water lantern)\n");
            writer.write("abyssLanternHue=30.0\n");
            writer.write("# default abyssLanternSat is 0.75 (rich saturation matching vanilla water lantern)\n");
            writer.write("abyssLanternSat=0.75\n\n");

            writer.write("# === AutoTorch Integration Settings ===\n");
            writer.write("# Automatically integrates with AutoTorch mod if installed\n");
            writer.write("autoTorchIntegration=true\n");
            writer.write("# Select which torches AutoTorch will use from player inventory.\n");
            writer.write("# Options: BETTER_THEN_VANILLA, VANILLA_THEN_BETTER, BETTER_ONLY, VANILLA_ONLY, ANY_LIGHT\n");
            writer.write("# - BETTER_THEN_VANILLA: Places Better Torches first; falls back to regular torches when empty\n");
            writer.write("# - VANILLA_THEN_BETTER: Places regular torches first; falls back to Better Torches\n");
            writer.write("# - BETTER_ONLY: Strictly places Better Torches only (never consumes regular torches)\n");
            writer.write("# - VANILLA_ONLY: Strictly places regular torches only (never consumes Better Torches)\n");
            writer.write("# - ANY_LIGHT: Places any available light source (Better Torches, torches, lanterns, lamps)\n");
            writer.write("autoTorchTorchSelection=BETTER_THEN_VANILLA\n");
            writer.write("# Priority sorting when in ANY_LIGHT mode.\n");
            writer.write("# Options: SLOT_ORDER, BRIGHTEST_FIRST, CONSERVATIVE_FIRST\n");
            writer.write("# - SLOT_ORDER (Default): Hotbar / earliest slot items take precedence\n");
            writer.write("# - BRIGHTEST_FIRST: Uses brightest light fixtures first (Phoenix -> Abyss -> Wood/Steel -> Better Torch -> Torch)\n");
            writer.write("# - CONSERVATIVE_FIRST: Uses cheapest/basic torches first to preserve rare/expensive lamps\n");
            writer.write("autoTorchAnyLightPriority=SLOT_ORDER\n");
            writer.write("# Consider all light sources (Better Torches, wall torches, lamps) to prevent duplicate placements\n");
            writer.write("autoTorchConsiderAllLights=true\n");
            writer.write("# Dynamically optimize lookahead and spacing when traveling with high light level torches\n");
            writer.write("autoTorchDynamicTravelSpacing=true\n");
            writer.write("# Minimum static light level threshold (30-200) below which a torch will be placed when traveling\n");
            writer.write("autoTorchMinLightThreshold=90\n");
        }
    }

    public Float getTorchLightHue() {
        return torchLightHue;
    }

    public Float getTorchLightSat() {
        return torchLightSat;
    }

    public Integer getTorchLightLevel() {
        return torchLightLevel;
    }

    public String getRecipeDifficulty() {
        return recipeDifficulty;
    }

    public Integer getSteelLampLightLevel() {
        return steelLampLightLevel;
    }

    public Integer getWoodenLanternLightLevel() {
        return woodenLanternLightLevel;
    }

    public Integer getPhoenixLampLightLevel() {
        return phoenixLampLightLevel;
    }

    public Integer getAbyssLanternLightLevel() {
        return abyssLanternLightLevel;
    }

    public Float getOutdoorLampHue() {
        return outdoorLampHue;
    }

    public Float getOutdoorLampSat() {
        return outdoorLampSat;
    }

    public Float getAbyssLanternHue() {
        return abyssLanternHue;
    }

    public Float getAbyssLanternSat() {
        return abyssLanternSat;
    }

    public Boolean isAutoTorchIntegration() {
        return autoTorchIntegration;
    }

    public String getAutoTorchTorchSelection() {
        return autoTorchTorchSelection;
    }

    public String getAutoTorchAnyLightPriority() {
        return autoTorchAnyLightPriority;
    }

    public Boolean isAutoTorchConsiderAllLights() {
        return autoTorchConsiderAllLights;
    }

    public Boolean isAutoTorchDynamicTravelSpacing() {
        return autoTorchDynamicTravelSpacing;
    }

    public Integer getAutoTorchMinLightThreshold() {
        return autoTorchMinLightThreshold;
    }

    public Recipe getTorchCraftingRecipe() {
        switch (this.recipeDifficulty) {
            case "EASY":
                return new Recipe(
                        "bettertorch",
                        1,
                        RecipeTechRegistry.NONE,
                        new Ingredient[]{
                                new Ingredient("torch", 2)
                        }
                ).showAfter("torch");
            case "HARD":
                return new Recipe(
                        "bettertorch",
                        1,
                        RecipeTechRegistry.WORKSTATION,
                        new Ingredient[]{
                                new Ingredient("torch", 4),
                                new Ingredient("anystone", 4)
                        }
                ).showAfter("torch");
            case "MEDIUM":
            default:
                return new Recipe(
                        "bettertorch",
                        1,
                        RecipeTechRegistry.NONE,
                        new Ingredient[]{
                                new Ingredient("torch", 4)
                        }
                ).showAfter("torch");
        }
    }

    public Recipe getTorchUncraftingRecipe() {
        switch (this.recipeDifficulty) {
            case "EASY":
                return new Recipe(
                        "torch",
                        2,
                        RecipeTechRegistry.NONE,
                        new Ingredient[]{
                                new Ingredient("bettertorch", 1)
                        }
                ).showAfter("bettertorch");
            case "HARD":
                return new Recipe(
                        "torch",
                        4,
                        RecipeTechRegistry.WORKSTATION,
                        new Ingredient[]{
                                new Ingredient("bettertorch", 1)
                        }
                ).showAfter("bettertorch");
            case "MEDIUM":
            default:
                return new Recipe(
                        "torch",
                        4,
                        RecipeTechRegistry.NONE,
                        new Ingredient[]{
                                new Ingredient("bettertorch", 1)
                        }
                ).showAfter("bettertorch");
        }
    }

    public float getTorchBrokerValue() {
        switch (this.recipeDifficulty) {
            case "EASY":
                return 0.2F;
            case "HARD":
                return 0.8F;
            case "MEDIUM":
            default:
                return 0.4F;
        }
    }

    public void applyServerSync(String recipeDifficulty, int torchLightLevel, float torchLightHue, float torchLightSat,
                                int steelLampLightLevel, int woodenLanternLightLevel, int phoenixLampLightLevel,
                                int abyssLanternLightLevel, float outdoorLampHue, float outdoorLampSat,
                                float abyssLanternHue, float abyssLanternSat,
                                boolean autoTorchIntegration, String autoTorchTorchSelection, String autoTorchAnyLightPriority, boolean autoTorchConsiderAllLights) {
        applyServerSync(recipeDifficulty, torchLightLevel, torchLightHue, torchLightSat, steelLampLightLevel, woodenLanternLightLevel,
                phoenixLampLightLevel, abyssLanternLightLevel, outdoorLampHue, outdoorLampSat, abyssLanternHue, abyssLanternSat,
                autoTorchIntegration, autoTorchTorchSelection, autoTorchAnyLightPriority, autoTorchConsiderAllLights, true, 90);
    }

    public void applyServerSync(String recipeDifficulty, int torchLightLevel, float torchLightHue, float torchLightSat,
                                int steelLampLightLevel, int woodenLanternLightLevel, int phoenixLampLightLevel,
                                int abyssLanternLightLevel, float outdoorLampHue, float outdoorLampSat,
                                float abyssLanternHue, float abyssLanternSat,
                                boolean autoTorchIntegration, String autoTorchTorchSelection, String autoTorchAnyLightPriority,
                                boolean autoTorchConsiderAllLights, boolean autoTorchDynamicTravelSpacing, int autoTorchMinLightThreshold) {
        if (recipeDifficulty != null && !recipeDifficulty.isEmpty()) {
            this.recipeDifficulty = recipeDifficulty.toUpperCase();
        }
        this.torchLightLevel = clamp(torchLightLevel, 50, 5000);
        this.torchLightHue = torchLightHue;
        this.torchLightSat = torchLightSat;
        this.steelLampLightLevel = clamp(steelLampLightLevel, 50, 5000);
        this.woodenLanternLightLevel = clamp(woodenLanternLightLevel, 50, 5000);
        this.phoenixLampLightLevel = clamp(phoenixLampLightLevel, 50, 5000);
        this.abyssLanternLightLevel = clamp(abyssLanternLightLevel, 50, 5000);
        this.outdoorLampHue = outdoorLampHue;
        this.outdoorLampSat = outdoorLampSat;
        this.abyssLanternHue = abyssLanternHue;
        this.abyssLanternSat = abyssLanternSat;
        this.autoTorchIntegration = autoTorchIntegration;
        this.autoTorchTorchSelection = normalizeTorchSelection(autoTorchTorchSelection);
        this.autoTorchAnyLightPriority = normalizeAnyLightPriority(autoTorchAnyLightPriority);
        this.autoTorchConsiderAllLights = autoTorchConsiderAllLights;
        this.autoTorchDynamicTravelSpacing = autoTorchDynamicTravelSpacing;
        this.autoTorchMinLightThreshold = clamp(autoTorchMinLightThreshold, 30, 200);
        System.out.println("Applied server lighting config: recipeDifficulty=" + this.recipeDifficulty + ", torchLightLevel=" + this.torchLightLevel + ", autoTorchIntegration=" + this.autoTorchIntegration + ", autoTorchTorchSelection=" + this.autoTorchTorchSelection + ", autoTorchAnyLightPriority=" + this.autoTorchAnyLightPriority + ", autoTorchDynamicTravelSpacing=" + this.autoTorchDynamicTravelSpacing + ", autoTorchMinLightThreshold=" + this.autoTorchMinLightThreshold);
    }
}
