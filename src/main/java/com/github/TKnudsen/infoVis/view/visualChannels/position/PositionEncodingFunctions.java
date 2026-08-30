package com.github.TKnudsen.infoVis.view.visualChannels.position;

import java.util.Collection;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DegenerateRangeException;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRangeTools;

/**
 * <p>
 * Shared support for building a {@link PositionEncodingFunction} from a data
 * collection. Delegates the actual min/max scan to
 * {@link NumericRangeTools#computeFiniteRangeStrict(Collection, Function)}
 * (from ComplexDataObject) rather than reimplementing it here: it already
 * excludes non-finite mapped values (NaN AND +/-Infinity, not just NaN),
 * null-guards each element and its mapped value, and rejects a degenerate
 * min == max range with a {@link DegenerateRangeException} instead of
 * silently building a broken axis.
 * 
 * Replaces what several painters previously did independently: materializing
 * an intermediate List&lt;Double&gt; and constructing a full StatisticsSupport
 * (a descriptive-statistics object -- variance, percentiles, etc. -- far more
 * than a min/max needs) just to read getMin()/getMax() off it
 * (Distribution1DPainter, ScatterPlotPainter, ScatterPlotSpriteGPUPainter,
 * ScatterPlotIndexedGPUPainter).
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public final class PositionEncodingFunctions {

	private PositionEncodingFunctions() {
	}

	/**
	 * Computes the finite value range via
	 * {@link NumericRangeTools#computeFiniteRangeStrict(Collection, Function)}.
	 * If that throws {@link DegenerateRangeException}, rethrows with
	 * {@code context} prepended -- {@code NumericRangeTools} itself only ever
	 * sees a bare Collection + mapping, so it can never say WHICH chart/axis
	 * failed; the caller (a painter, at its own construction site) is the one
	 * place that actually knows that. Without this, a batch process building
	 * many charts in a loop gets a bare "degenerate range (min==max==5.2)" on
	 * failure with no way to tell which of the N charts it came from.
	 *
	 * @param context short label identifying what this range is for (e.g.
	 *                "ScatterPlotPainter (x-axis)"). May be null, in which case
	 *                the exception is rethrown unchanged.
	 * @throws DegenerateRangeException if data has no finite mapped value, or
	 *                                  the resulting range is degenerate
	 *                                  (min == max)
	 */
	public static <T> NumericRange computeRange(Collection<? extends T> data,
			Function<? super T, ? extends Number> mapping, String context) {
		try {
			return NumericRangeTools.computeFiniteRangeStrict(data, mapping);
		} catch (DegenerateRangeException e) {
			if (context == null)
				throw e;
			throw new DegenerateRangeException(context + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Convenience wrapper: computes the range via {@link #computeRange} and
	 * wraps it directly into a non-inverted {@link PositionEncodingFunction}.
	 *
	 * @see #computeRange(Collection, Function, String)
	 */
	public static <T> PositionEncodingFunction createPositionEncodingFunction(Collection<? extends T> data,
			Function<? super T, ? extends Number> mapping, Double minPixel, Double maxPixel, String context) {
		return createPositionEncodingFunction(data, mapping, minPixel, maxPixel, false, context);
	}

	/**
	 * Convenience wrapper: computes the range via {@link #computeRange} and
	 * wraps it directly into a {@link PositionEncodingFunction}.
	 *
	 * @see #computeRange(Collection, Function, String)
	 */
	public static <T> PositionEncodingFunction createPositionEncodingFunction(Collection<? extends T> data,
			Function<? super T, ? extends Number> mapping, Double minPixel, Double maxPixel,
			boolean flipAxisValues, String context) {
		NumericRange range = computeRange(data, mapping, context);
		return new PositionEncodingFunction(range.getMin(), range.getMax(), minPixel, maxPixel, flipAxisValues);
	}
}
