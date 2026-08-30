package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

/**
 * <p>
 * Snaps a continuous {@code (value, uncertainty) in [0,1]x[0,1]} pair to the
 * center of a discrete bin, the way a legend for a discrete bivariate map
 * would. Ported from the reference JavaScript implementation of Correll,
 * Moritz, and Heer, "Value-Suppressing Uncertainty Palettes," CHI (2018)
 * (https://github.com/uwdata/vsup, {@code src/quantization.js}), simplified
 * to the fixed {@code [0,1]} domain every colormap in this package already
 * assumes (the reference implementation supports arbitrary data domains via
 * d3 scales; this port skips that layer since {@link
 * com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D}
 * only ever receives normalized coordinates).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
@FunctionalInterface
public interface BivariateQuantization {

	Quantized quantize(double value, double uncertainty);

	/**
	 * result of a quantization: the center coordinates of the bin the input
	 * {@code (value, uncertainty)} pair fell into.
	 */
	final class Quantized {
		public final double value;
		public final double uncertainty;

		public Quantized(double value, double uncertainty) {
			this.value = value;
			this.uncertainty = uncertainty;
		}
	}

	/**
	 * The "traditional bivariate map" quantization: a uniform {@code n x n}
	 * grid, {@code n} uncertainty layers with exactly {@code n} equally-sized
	 * value bins each, regardless of uncertainty. Ported from the reference
	 * implementation's {@code squareQuantization(n)}.
	 *
	 * @param n number of layers (uncertainty bins) and, per layer, number of
	 *          value bins
	 */
	static BivariateQuantization uniformGrid(int n) {
		if (n < 1)
			throw new IllegalArgumentException("BivariateQuantization.uniformGrid: n must be >= 1, was " + n);

		return (value, uncertainty) -> {
			int i = 0;
			while (i < n - 1 && uncertainty < 1.0 - (i + 1) / (double) n)
				i++;

			int j = 0;
			double binWidth = 1.0 / n;
			while (j < n - 1 && value > (j + 0.5) * binWidth + binWidth / 2.0)
				j++;

			double uCenter = 1.0 - (i + 0.5) / n;
			double vCenter = (j + 0.5) * binWidth;
			return new Quantized(vCenter, uCenter);
		};
	}

	/**
	 * The VSUP quantization tree: layer {@code i} (0 = maximum uncertainty,
	 * the single-node "root") has {@code branchingFactor^i} evenly-spaced
	 * value bins. As uncertainty decreases, the tree branches, revealing
	 * finer value distinctions -- while uncertainty above the top layer's
	 * threshold collapses every value to the single root bin. Ported from the
	 * reference implementation's {@code treeQuantization(branchingFactor,
	 * treeLayers)}.
	 *
	 * @param branchingFactor number of children per node, e.g. 2 for a binary
	 *                        tree
	 * @param layers          tree depth (number of uncertainty bins); layer 0
	 *                        is the root
	 */
	static BivariateQuantization tree(int branchingFactor, int layers) {
		if (branchingFactor < 1)
			throw new IllegalArgumentException(
					"BivariateQuantization.tree: branchingFactor must be >= 1, was " + branchingFactor);
		if (layers < 1)
			throw new IllegalArgumentException("BivariateQuantization.tree: layers must be >= 1, was " + layers);

		double epsilon = 1e-9;

		return (value, uncertainty) -> {
			int i = 0;
			while (i < layers - 1 && uncertainty < 1.0 - (i + 1) / (double) layers - epsilon)
				i++;

			double uCenter = 1.0 - (i + 0.5) / layers;

			if (i == 0)
				return new Quantized(0.5, uCenter);

			int leaves = (int) Math.round(Math.pow(branchingFactor, i));
			double binWidth = 1.0 / leaves;

			int j = 0;
			while (j < leaves - 1 && value > (j + 0.5) * binWidth + binWidth / 2.0)
				j++;

			double vCenter = (j + 0.5) * binWidth;
			return new Quantized(vCenter, uCenter);
		};
	}

}
