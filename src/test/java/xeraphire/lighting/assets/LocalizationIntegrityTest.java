package xeraphire.lighting.assets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalizationIntegrityTest {

    private static final Map<String, Map<String, String>> sections = new HashMap<>();

    @BeforeAll
    static void loadLanguageFile() throws Exception {
        File langFile = new File("src/main/resources/locale/en.lang");
        assertThat(langFile).exists().isFile();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8))) {
            String line;
            String currentSection = "";
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).trim();
                    sections.putIfAbsent(currentSection, new HashMap<>());
                } else if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String val = parts[1].trim();
                    sections.computeIfAbsent(currentSection, k -> new HashMap<>()).put(key, val);
                }
            }
        }
    }

    @ParameterizedTest(name = "Item localization key ''{0}'' exists with non-empty translation")
    @ValueSource(strings = {
            "bettertorch",
            "steelLampPost",
            "woodenHangingLantern",
            "phoenixLamp",
            "abyssLantern"
    })
    void testItemLocalizations(String itemKey) {
        Map<String, String> itemMap = sections.get("item");
        assertThat(itemMap).isNotNull();
        assertThat(itemMap).containsKey(itemKey);
        assertThat(itemMap.get(itemKey)).isNotEmpty();
    }

    @ParameterizedTest(name = "Object localization key ''{0}'' exists with non-empty translation")
    @ValueSource(strings = {
            "bettertorch",
            "betterwalltorch",
            "steelLampPost",
            "woodenHangingLantern",
            "phoenixLamp",
            "abyssLantern"
    })
    void testObjectLocalizations(String objectKey) {
        Map<String, String> objectMap = sections.get("object");
        assertThat(objectMap).isNotNull();
        assertThat(objectMap).containsKey(objectKey);
        assertThat(objectMap.get(objectKey)).isNotEmpty();
    }

    @ParameterizedTest(name = "Tooltip localization key ''{0}'' exists with non-empty text")
    @ValueSource(strings = {
            "lightlevelinfo",
            "waterplacetip",
            "wirecontrolinfo",
            "bettertorchtip",
            "steelLampPosttip",
            "woodenHangingLanterntip",
            "phoenixLamptip",
            "abyssLanterntip"
    })
    void testTooltipLocalizations(String tooltipKey) {
        Map<String, String> tooltipMap = sections.get("itemtooltip");
        assertThat(tooltipMap).isNotNull();
        assertThat(tooltipMap).containsKey(tooltipKey);
        assertThat(tooltipMap.get(tooltipKey)).isNotEmpty();
    }

    @Test
    @DisplayName("lightlevelinfo tooltip contains parameter placeholder <light>")
    void testPlaceholder() {
        Map<String, String> tooltipMap = sections.get("itemtooltip");
        assertThat(tooltipMap).isNotNull();
        assertThat(tooltipMap.get("lightlevelinfo")).contains("<light>");
    }
}
