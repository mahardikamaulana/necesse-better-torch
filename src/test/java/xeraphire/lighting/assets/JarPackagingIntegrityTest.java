package xeraphire.lighting.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

public class JarPackagingIntegrityTest {

    @Test
    @DisplayName("Mod JAR contains mod.info, root preview, and all assets under resources/ directory")
    void testJarPackageStructure() throws IOException {
        File jarFile = new File("build/jar/LettherebeLight!-1.3.3-1.0.0.jar");
        if (!jarFile.exists()) {
            return; // Skip if jar hasn't been built yet during initial compile
        }

        Set<String> entries = new HashSet<>();
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().map(JarEntry::getName).forEach(entries::add);
        }

        // Must have root metadata
        assertThat(entries).contains("mod.info");
        assertThat(entries).contains("preview.png");

        // Necesse ResourceFolder and Translation loaders strictly require resources/ prefix inside JAR
        assertThat(entries).contains("resources/locale/en.lang");
        assertThat(entries).contains("resources/items/bettertorch.png");
        assertThat(entries).contains("resources/items/steelLampPost.png");
        assertThat(entries).contains("resources/items/woodenHangingLantern.png");
        assertThat(entries).contains("resources/items/phoenixLamp.png");
        assertThat(entries).contains("resources/items/abyssLantern.png");

        assertThat(entries).contains("resources/objects/bettertorch.png");
        assertThat(entries).contains("resources/objects/bettertorch_off.png");
        assertThat(entries).contains("resources/objects/betterwalltorch.png");
        assertThat(entries).contains("resources/objects/steelLampPost.png");
        assertThat(entries).contains("resources/objects/steelLampPost_off.png");
        assertThat(entries).contains("resources/objects/woodenHangingLantern.png");
        assertThat(entries).contains("resources/objects/woodenHangingLantern_off.png");
        assertThat(entries).contains("resources/objects/phoenixLamp.png");
        assertThat(entries).contains("resources/objects/phoenixLamp_off.png");
        assertThat(entries).contains("resources/objects/abyssLantern.png");
        assertThat(entries).contains("resources/objects/abyssLantern_off.png");

        // Must NOT contain redundant duplicate preview under resources/ (saving bundle size)
        assertThat(entries).doesNotContain("resources/preview.png");

        // Must NOT contain OS junk files
        assertThat(entries.stream().noneMatch(e -> e.contains(".DS_Store") || e.contains("Thumbs.db"))).isTrue();

        // Optimized JAR bundle must stay strictly under 800 KB
        assertThat(jarFile.length()).isLessThan(800 * 1024L);
    }

    @Test
    @DisplayName("Workshop preview image is within Steam 1.0 MB limit")
    void testPreviewImageSizeLimit() {
        File previewFile = new File("src/main/resources/preview.png");
        assertThat(previewFile).exists().isFile();
        // Steam Workshop has a strict 1,048,576 byte hard ceiling for preview images
        assertThat(previewFile.length()).isLessThan(1024 * 1024L);
    }
}
