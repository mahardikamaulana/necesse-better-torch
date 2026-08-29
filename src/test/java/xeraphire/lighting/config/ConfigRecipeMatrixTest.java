package xeraphire.lighting.config;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.GNDRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.engine.registries.TileRegistry;
import necesse.inventory.recipe.Recipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigRecipeMatrixTest {

    @BeforeAll
    static void setupRegistries() {
        try {
            GNDRegistry.instance.registerCore();
        } catch (Throwable t) {
        }
        try {
            DamageTypeRegistry.instance.registerCore();
        } catch (Throwable t) {
        }
        try {
            RecipeTechRegistry.instance.registerCore();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            TileRegistry.instance.registerCore();
        } catch (Throwable t) {
        }
        try {
            ObjectRegistry.instance.registerCore();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            ItemRegistry.instance.registerCore();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            LightingMod mod = new LightingMod();
            mod.preInit();
            mod.init();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    @DisplayName("EASY difficulty crafting recipe: 2 torches -> 1 bettertorch (NONE tech)")
    void testEasyCraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=EASY\n");

        Recipe recipe = config.getTorchCraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("bettertorch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(1);
        assertThat(recipe.matchTech(RecipeTechRegistry.NONE)).isTrue();
        assertThat(recipe.ingredients).hasSize(1);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("torch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("MEDIUM difficulty crafting recipe: 4 torches -> 1 bettertorch (NONE tech)")
    void testMediumCraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=MEDIUM\n");

        Recipe recipe = config.getTorchCraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("bettertorch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(1);
        assertThat(recipe.matchTech(RecipeTechRegistry.NONE)).isTrue();
        assertThat(recipe.ingredients).hasSize(1);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("torch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(4);
    }

    @Test
    @DisplayName("HARD difficulty crafting recipe: 4 torches + 4 anystone -> 1 bettertorch (WORKSTATION tech)")
    void testHardCraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=HARD\n");

        Recipe recipe = config.getTorchCraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("bettertorch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(1);
        assertThat(recipe.matchTech(RecipeTechRegistry.WORKSTATION)).isTrue();
        assertThat(recipe.ingredients).hasSize(2);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("torch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(4);
        assertThat(recipe.ingredients[1].ingredientStringID).isEqualTo("anystone");
        assertThat(recipe.ingredients[1].getIngredientAmount()).isEqualTo(4);
    }

    @Test
    @DisplayName("EASY difficulty uncrafting recipe: 1 bettertorch -> 2 torches (NONE tech)")
    void testEasyUncraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=EASY\n");

        Recipe recipe = config.getTorchUncraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("torch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(2);
        assertThat(recipe.matchTech(RecipeTechRegistry.NONE)).isTrue();
        assertThat(recipe.ingredients).hasSize(1);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("bettertorch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("MEDIUM difficulty uncrafting recipe: 1 bettertorch -> 4 torches (NONE tech)")
    void testMediumUncraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=MEDIUM\n");

        Recipe recipe = config.getTorchUncraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("torch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(4);
        assertThat(recipe.matchTech(RecipeTechRegistry.NONE)).isTrue();
        assertThat(recipe.ingredients).hasSize(1);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("bettertorch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("HARD difficulty uncrafting recipe: 1 bettertorch -> 4 torches (WORKSTATION tech)")
    void testHardUncraftingRecipe() {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=HARD\n");

        Recipe recipe = config.getTorchUncraftingRecipe();
        assertThat(recipe.resultItem.item.getStringID()).isEqualTo("torch");
        assertThat(recipe.resultItem.getAmount()).isEqualTo(4);
        assertThat(recipe.matchTech(RecipeTechRegistry.WORKSTATION)).isTrue();
        assertThat(recipe.ingredients).hasSize(1);
        assertThat(recipe.ingredients[0].ingredientStringID).isEqualTo("bettertorch");
        assertThat(recipe.ingredients[0].getIngredientAmount()).isEqualTo(1);
    }

    @ParameterizedTest(name = "Broker value for {0} is {1}")
    @CsvSource({
            "EASY, 0.2",
            "MEDIUM, 0.4",
            "HARD, 0.8",
            "UNKNOWN, 0.4"
    })
    void testBrokerValues(String difficulty, float expectedBrokerValue) {
        Config config = new Config((java.io.File) null);
        config.loadFromString("recipeDifficulty=" + difficulty + "\n");

        assertThat(config.getTorchBrokerValue()).isEqualTo(expectedBrokerValue);
    }
}
