package xeraphire.lighting.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TextureIntegrityTest {

    private static final byte[] PNG_HEADER = new byte[]{
            (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
            (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
    };

    private void assertValidPng(File file) throws IOException {
        assertThat(file).exists().isFile();
        assertThat(file.length()).isGreaterThan(0);

        // Verify PNG magic bytes
        byte[] header = new byte[8];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(header);
            assertThat(read).isEqualTo(8);
            assertThat(header).isEqualTo(PNG_HEADER);
        }

        // Verify BufferedImage decoder can parse image
        BufferedImage img = ImageIO.read(file);
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isGreaterThan(0);
        assertThat(img.getHeight()).isGreaterThan(0);
    }

    @ParameterizedTest(name = "Object texture {0} exists and is valid PNG")
    @ValueSource(strings = {
            "bettertorch.png",
            "bettertorch_off.png",
            "betterwalltorch.png",
            "steelLampPost.png",
            "steelLampPost_off.png",
            "woodenHangingLantern.png",
            "woodenHangingLantern_off.png",
            "phoenixLamp.png",
            "phoenixLamp_off.png",
            "abyssLantern.png",
            "abyssLantern_off.png"
    })
    void testObjectTextures(String filename) throws IOException {
        File file = new File("src/main/resources/objects/" + filename);
        assertValidPng(file);
    }

    @ParameterizedTest(name = "Item texture {0} exists and is valid PNG")
    @ValueSource(strings = {
            "bettertorch.png",
            "steelLampPost.png",
            "woodenHangingLantern.png",
            "phoenixLamp.png",
            "abyssLantern.png"
    })
    void testItemTextures(String filename) throws IOException {
        File file = new File("src/main/resources/items/" + filename);
        assertValidPng(file);
    }

    @Test
    @DisplayName("Mod workshop preview image exists and is valid PNG")
    void testPreviewImage() throws IOException {
        File file = new File("src/main/resources/preview.png");
        assertValidPng(file);
    }
}
