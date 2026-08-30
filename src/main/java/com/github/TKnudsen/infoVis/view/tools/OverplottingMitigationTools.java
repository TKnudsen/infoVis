package com.github.TKnudsen.infoVis.view.tools;

/**
 * <p>
 * Shared logic for overplotting mitigation via dynamic alpha adjustment, used
 * by every painter implementing
 * {@code com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation}
 * (previously duplicated, with inconsistent formulas, across the individual
 * painter classes), plus the dataset-size thresholds used to decide when a
 * chart panel should turn alpha adjustment on -- and, for GPU-backed panels,
 * when to switch rendering backends.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class OverplottingMitigationTools {

	/**
	 * the alpha value never drops below this, however dense the data
	 */
	private static final float MIN_ALPHA = 0.05f;

	/**
	 * point count below which {@link #computeAlpha(int)} returns full opacity
	 */
	private static final float ALPHA_THRESHOLD = 3000f;

	/**
	 * dataset size above which alpha adjustment is worth enabling automatically
	 */
	private static final int ALPHA_ADJUSTMENT_DATA_SIZE_THRESHOLD = 1_000;

	/**
	 * dataset size above which a GPU-backed panel should render on the GPU
	 */
	private static final int GPU_RENDERING_DATA_SIZE_THRESHOLD = 10_000;

	private OverplottingMitigationTools() {
	}

	/**
	 * @param pointCount number of points currently being rendered
	 * @return an alpha value in [{@value #MIN_ALPHA}, 1.0]; full opacity up to
	 *         {@value #ALPHA_THRESHOLD} points, decreasing (inversely
	 *         proportional to {@code pointCount}) beyond that, so denser point
	 *         clouds fade rather than sparse ones
	 */
	public static float computeAlpha(int pointCount) {
		return Math.max(MIN_ALPHA, Math.min(1.0f, ALPHA_THRESHOLD / pointCount));
	}

	/**
	 * @param dataSize the number of elements a chart panel is about to render
	 * @return whether alpha adjustment should be turned on automatically for a
	 *         dataset this size
	 */
	public static boolean shouldEnableAlphaAdjustment(int dataSize) {
		return dataSize > ALPHA_ADJUSTMENT_DATA_SIZE_THRESHOLD;
	}

	/**
	 * @param dataSize the number of elements a GPU-backed chart panel is about to
	 *                 render
	 * @return whether that panel should switch to GPU rendering for a dataset
	 *         this size
	 */
	public static boolean shouldUseGPURendering(int dataSize) {
		return dataSize > GPU_RENDERING_DATA_SIZE_THRESHOLD;
	}
}
