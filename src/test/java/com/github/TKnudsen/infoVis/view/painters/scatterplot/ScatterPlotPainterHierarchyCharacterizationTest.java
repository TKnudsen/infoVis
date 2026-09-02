package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

/**
 * <p>
 * Characterization tests pinning down the behavior of {@link ScatterPlotPainter},
 * {@link ScatterPlotIndexedGPUPainter}, and {@link ScatterPlotSpriteGPUPainter}
 * across the extraction of the shared {@link AbstractScatterPlotPainter}/
 * {@link AbstractGPUScatterPlotPainter} hierarchy (code review finding #27/#28).
 * </p>
 *
 * <p>
 * Two behaviors were deliberately buggy before Phase 3 of that extraction and
 * are now fixed, updated here to assert the fixed behavior rather than the old
 * one (see {@code AbstractGPUScatterPlotPainter}'s class-level javadoc for the
 * full rationale):
 * </p>
 * <ul>
 * <li>{@link #degenerateRangeDataset_indexedGpuPainterTolerates()}/
 * {@link #degenerateRangeDataset_spriteGpuPainterTolerates()} -- both GPU
 * painters used to reject a single-value dataset the CPU painter already
 * tolerated (each had its own separate, strict world-bounds computation);
 * now both inherit {@code AbstractGPUScatterPlotPainter}'s tolerant
 * {@code computeWorldBounds()}.</li>
 * <li>{@link #nullColorMapping_indexedGpuPainterFallsBackToPainterPaint()} --
 * the indexed painter used to fall back to gray for a null color mapping,
 * while the sprite/CPU painters fell back to {@code getPaint()}; all three
 * now agree.</li>
 * </ul>
 */
public class ScatterPlotPainterHierarchyCharacterizationTest {

	// a two-point, non-degenerate dataset: world (0,0) and (10,10)
	private static final List<double[]> TWO_POINTS = Arrays.asList(new double[] { 0, 0 }, new double[] { 10, 10 });

	// a single-point dataset: world (5,5) for both axes -- min == max, i.e.
	// "degenerate" in PositionEncodingFunctions.computeRange's sense
	private static final List<double[]> DEGENERATE_POINT = Arrays.asList(new double[] { 5, 5 });

	private static final Function<double[], Double> WORLD_X = p -> p[0];
	private static final Function<double[], Double> WORLD_Y = p -> p[1];

	// ==================== calculatePointSize (pure function) ====================

	@Test
	public void calculatePointSize_isSixTenthsOfOnePercentOfShorterSide_withThreePixelFloor() {
		// shorter side dominates, 0.6% of it, floored at 3
		assertEquals(3.0, ScatterPlotPainter.calculatePointSize(100, 100), 0.0001);
		assertEquals(3.0, ScatterPlotPainter.calculatePointSize(1000, 100), 0.0001);
		assertEquals(6.0, ScatterPlotPainter.calculatePointSize(1000, 1000), 0.0001);
		assertEquals(3.0, ScatterPlotPainter.calculatePointSize(1000, 400), 0.0001);

		// the two GPU painters carry their own byte-identical copy of this formula
		assertEquals(ScatterPlotPainter.calculatePointSize(1000, 1000),
				ScatterPlotIndexedGPUPainter.calculatePointSize(1000, 1000), 0.0001);
		assertEquals(ScatterPlotPainter.calculatePointSize(1000, 1000),
				ScatterPlotSpriteGPUPainter.calculatePointSize(1000, 1000), 0.0001);
	}

	// ==================== position encoding output ====================

	@Test
	public void positionEncoding_mapsWorldExtremesToOppositeScreenCorners() {
		ScatterPlotPainter<double[]> painter = new ScatterPlotPainter<>(TWO_POINTS, null, WORLD_X, WORLD_Y);
		painter.setRectangle(new Rectangle2D.Double(0, 0, 100, 100));

		// x is not flipped: world min (0) -> pixel min (0), world max (10) -> pixel
		// max (100). y IS flipped: world min (0) -> pixel max (100), world max (10)
		// -> pixel min (0) -- so the (0,0) world point renders at screen (0,100) and
		// the (10,10) world point renders at screen (100,0).
		List<double[]> atBottomLeft = painter.getElementsAtPoint(new Point(0, 100));
		assertEquals(1, atBottomLeft.size());
		assertEquals(0.0, atBottomLeft.get(0)[0], 0.0001);

		List<double[]> atTopRight = painter.getElementsAtPoint(new Point(100, 0));
		assertEquals(1, atTopRight.size());
		assertEquals(10.0, atTopRight.get(0)[0], 0.0001);
	}

	// ==================== degenerate-range dataset ====================

	@Test
	public void degenerateRangeDataset_cpuPainterTolerates() {
		// today: the CPU painter renders a single-value dataset fine (finding #10's
		// fix) -- this must keep working, both now and after the refactor. A
		// degenerate world range normalizes to 0 (see MathFunctions.linearScale's
		// max==min branch), which PositionEncodingFunction then maps onto the pixel
		// range's own boundary -- so the check pads the rectangle by 1px on every
		// side to reliably include that boundary point (Rectangle2D.contains uses a
		// half-open interval and would otherwise flakily exclude an exact-edge point
		// depending on the flip direction, which is not what this test is about).
		ScatterPlotPainter<double[]> painter = new ScatterPlotPainter<>(DEGENERATE_POINT, null, WORLD_X, WORLD_Y);
		painter.setRectangle(new Rectangle2D.Double(0, 0, 100, 100));

		assertFalse(painter.getElementsInShape(new Rectangle2D.Double(-1, -1, 102, 102)).isEmpty());
	}

	@Test
	public void degenerateRangeDataset_indexedGpuPainterTolerates() {
		// fixed by Phase 3: both GPU painters previously had their own separate,
		// strict computeWorldBounds() and threw on a degenerate dataset before ever
		// reaching the CPU painter's already-tolerant position-encoding setup. Now
		// both inherit AbstractGPUScatterPlotPainter's tolerant computeWorldBounds().
		// default render mode is GPU, so getElementsInShape here goes through
		// worldToScreenX/Y -- i.e. worldMinX/MaxX/MinY/MaxY, exactly the fields
		// computeWorldBounds() fixes -- without needing any real GL context.
		ScatterPlotIndexedGPUPainter<double[]> painter = new ScatterPlotIndexedGPUPainter<>(DEGENERATE_POINT, null,
				WORLD_X, WORLD_Y);
		painter.setRectangle(new Rectangle2D.Double(0, 0, 100, 100));

		assertFalse(painter.getElementsInShape(new Rectangle2D.Double(-1, -1, 102, 102)).isEmpty());
	}

	@Test
	public void degenerateRangeDataset_spriteGpuPainterTolerates() {
		ScatterPlotSpriteGPUPainter<double[]> painter = new ScatterPlotSpriteGPUPainter<>(DEGENERATE_POINT, null,
				WORLD_X, WORLD_Y);
		painter.setRectangle(new Rectangle2D.Double(0, 0, 100, 100));

		assertFalse(painter.getElementsInShape(new Rectangle2D.Double(-1, -1, 102, 102)).isEmpty());
	}

	// ==================== null-colorMapping GPU fallback color ====================

	/**
	 * Records the color passed to the last {@code addPointSprite} call instead of
	 * touching any real GL state -- both {@code GPURendererJOGLIndexed} and
	 * {@code GPURendererJOGLSprite} allocate only CPU-side NIO buffers in their
	 * no-arg constructors, so subclassing them and overriding just this one method
	 * keeps the test free of any real OpenGL/JOGL context.
	 */
	private static Color captureAddPointToGPUColor(Object painter, Object gpuRendererStub, String gpuRendererField)
			throws ReflectiveOperationException {
		Field field = painter.getClass().getDeclaredField(gpuRendererField);
		field.setAccessible(true);
		field.set(painter, gpuRendererStub);

		Method addPointToGPU = painter.getClass().getDeclaredMethod("addPointToGPU", Object.class, double.class,
				boolean.class);
		addPointToGPU.setAccessible(true);
		addPointToGPU.invoke(painter, TWO_POINTS.get(0), 3.0, false);

		Field captured = gpuRendererStub.getClass().getDeclaredField("captured");
		captured.setAccessible(true);
		return (Color) captured.get(gpuRendererStub);
	}

	@Test
	public void nullColorMapping_indexedGpuPainterFallsBackToPainterPaint() throws ReflectiveOperationException {
		// fixed by Phase 3 (code review finding #28): addPointToGPU used to build
		// `paint = colorMapping != null ? colorMapping.apply(t) : null`, so a null
		// colorMapping reached extractColor(null) -> Color.GRAY. Now falls back to
		// getPaint() instead, matching the sprite/CPU painters. Compared against
		// painter.getPaint() itself, not a hardcoded Color.BLACK -- ChartPainter's
		// default paint is resolved from the active look-and-feel (UIManager's
		// "Label.foreground") since it's not necessarily literal black on every
		// L&F/platform this test runs under.
		ScatterPlotIndexedGPUPainter<double[]> painter = new ScatterPlotIndexedGPUPainter<>(TWO_POINTS, null, WORLD_X,
				WORLD_Y);

		class RecordingRenderer extends com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLIndexed {
			Color captured;

			@Override
			public void addPointSprite(float worldX, float worldY, float size, Color color) {
				this.captured = color;
			}
		}

		Color color = captureAddPointToGPUColor(painter, new RecordingRenderer(), "gpuRenderer");
		assertEquals(painter.getPaint(), color);
	}

	@Test
	public void nullColorMapping_spriteGpuPainterFallsBackToPainterPaint() throws ReflectiveOperationException {
		// ScatterPlotSpriteGPUPainter.addPointToGPU already fell back to getPaint()
		// even before Phase 3 -- this was the "correct" side of finding #28's
		// divergence, unaffected by the fix above. See the comment in the indexed
		// test above for why this compares against painter.getPaint() rather than a
		// hardcoded Color.BLACK.
		ScatterPlotSpriteGPUPainter<double[]> painter = new ScatterPlotSpriteGPUPainter<>(TWO_POINTS, null, WORLD_X,
				WORLD_Y);

		class RecordingRenderer extends com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLSprite {
			Color captured;

			@Override
			public void addPointSprite(float worldX, float worldY, float size, Color color) {
				this.captured = color;
			}
		}

		Color color = captureAddPointToGPUColor(painter, new RecordingRenderer(), "gpuRenderer");
		assertEquals(painter.getPaint(), color);
		assertTrue(painter.getPaint() instanceof Color);
	}
}
