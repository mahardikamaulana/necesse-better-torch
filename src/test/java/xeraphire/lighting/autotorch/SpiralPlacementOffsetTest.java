package xeraphire.lighting.autotorch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SpiralPlacementOffsetTest {

    @Test
    @DisplayName("Spiral offset arrays have identical lengths and exactly 24 coordinates")
    void testArrayLengths() {
        int[] dx = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DX;
        int[] dy = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DY;

        assertThat(dx).isNotNull();
        assertThat(dy).isNotNull();
        assertThat(dx.length).isEqualTo(24);
        assertThat(dy.length).isEqualTo(24);
    }

    @Test
    @DisplayName("Spiral offsets contain no duplicate coordinates and exclude center (0,0)")
    void testUniqueAndExcludesCenter() {
        int[] dx = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DX;
        int[] dy = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DY;

        Set<Point> coordinates = new HashSet<>();
        for (int i = 0; i < dx.length; i++) {
            Point p = new Point(dx[i], dy[i]);
            assertThat(p).isNotEqualTo(new Point(0, 0));
            boolean added = coordinates.add(p);
            assertThat(added)
                    .as("Coordinate at index %d (%d, %d) must be unique", i, dx[i], dy[i])
                    .isTrue();
        }

        // Exactly 24 points covering full 5x5 neighborhood around (0,0)
        assertThat(coordinates).hasSize(24);
    }

    @Test
    @DisplayName("Spiral offsets are bounded within Chebyshev distance of 2")
    void testBounds() {
        int[] dx = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DX;
        int[] dy = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.SPIRAL_DY;

        for (int i = 0; i < dx.length; i++) {
            assertThat(Math.abs(dx[i])).isLessThanOrEqualTo(2);
            assertThat(Math.abs(dy[i])).isLessThanOrEqualTo(2);
        }
    }
}
