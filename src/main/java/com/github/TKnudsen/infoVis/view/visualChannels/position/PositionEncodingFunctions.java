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
	 * Same as {@link #computeRange(Collection, Function, String)}, except a
	 * degenerate (min == max) range does not throw {@link DegenerateRangeException}
	 * -- it re-derives the single repeated (finite) mapped value and returns a
	 * {@code [value, value]} range instead. A scatterplot/axis is expected to
	 * render single-value/filtered/strip-style data just fine: the underlying
	 * {@code LinearNormalizationFunction} already maps a degenerate range without
	 * dividing by zero (see {@code MathFunctions.linearScale}'s {@code max == min}
	 * branch), so the strict rejection in {@link #computeRange} is a caller-side
	 * policy choice, not something the downstream math actually requires.
	 * <p>
	 * Previously duplicated across three call sites (a scatterplot painter's
	 * position-encoding setup, a GPU scatterplot painter's world-bounds
	 * computation, and a scatterplot panel's axis initialization) before being
	 * consolidated here.
	 *
	 * @param context short label identifying what this range is for, passed
	 *                through to {@link #computeRange} (and thus only surfaces if
	 *                data has no finite mapped value at all, since a degenerate
	 *                range itself no longer throws here)
	 */
	public static <T> NumericRange computeRangeTolerant(Collection<? extends T> data,
			Function<? super T, ? extends Number> mapping, String context) {
		try {
			return computeRange(data, mapping, context);
		} catch (DegenerateRangeException e) {
			double value = data.stream().map(mapping).filter(v -> v != null && !Double.isNaN(v.doubleValue())
					&& !Double.isInfinite(v.doubleValue())).map(Number::doubleValue).findFirst().orElse(0d);
			return new NumericRange(value, value, data.size());
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

	/**
	 * Same as {@link #createPositionEncodingFunction(Collection, Function, Double,
	 * Double, boolean, String)}, except the underlying range comes from
	 * {@link #computeRangeTolerant(Collection, Function, String)} -- a degenerate
	 * (min == max) dataset does not throw, it builds a {@code [value, value]}
	 * position encoding function instead.
	 */
	public static <T> PositionEncodingFunction createPositionEncodingFunctionTolerant(Collection<? extends T> data,
			Function<? super T, ? extends Number> mapping, Double minPixel, Double maxPixel,
			boolean flipAxisValues, String context) {
		NumericRange range = computeRangeTolerant(data, mapping, context);
		return new PositionEncodingFunction(range.getMin(), range.getMax(), minPixel, maxPixel, flipAxisValues);
	}
}
