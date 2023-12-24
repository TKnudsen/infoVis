package com.github.TKnudsen.infoVis.view.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VisualMappings {

	private VisualMappings() {

	}

	/**
	 * checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping function.
	 * 
	 * @param <D>                  d
	 * @param data                 the data collection
	 * @param worldToDoubleMapping mapping
	 * @return checked data
	 */
	public static <D> List<D> sanityCheckFilter(Iterable<D> data,
			Function<? super D, ? extends Number> worldToDoubleMapping) {
		return sanityCheckFilter(data, worldToDoubleMapping, true);
	}

	/**
	 * Checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping function.
	 * 
	 * @param <D>                  d
	 * @param data                 the data collection
	 * @param worldToDoubleMapping mapping
	 * @param warnForQualityLeaks  if warnings shall not be printed out
	 *                             (functionality remains the same)
	 * @return the checked data
	 */
	public static <D> List<D> sanityCheckFilter(Iterable<D> data,
			Function<? super D, ? extends Number> worldToDoubleMapping, boolean warnForQualityLeaks) {
		List<D> returnList = new ArrayList<D>();

		if (data == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.sanityCheckFilter: given data was null");
			return returnList;
		}

		if (worldToDoubleMapping == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.sanityCheckFilter: given worldToDoubleMapping was null");
			return returnList;
		}

		try {
			for (D t : data) {
				Number d = worldToDoubleMapping.apply(t);
				if (d != null && !Double.isNaN(d.doubleValue()))
					returnList.add(t);
				else if (warnForQualityLeaks)
					System.err.println("VisualMappings.sanityCheckFilter: object " + t
							+ " did not pass the sanity check and was ignored");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnList;
	}

	/**
	 * Checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping functions.
	 * 
	 * @param <D>                   d
	 * @param <V>                   v
	 * @param data                  the data collection
	 * @param worldToDoubleMappingX mapping
	 * @param worldToDoubleMappingY mapping
	 * @return checked data
	 */
	public static <D, V> List<D> sanityCheckFilter(Iterable<D> data,
			Function<? super D, ? extends Number> worldToDoubleMappingX,
			Function<? super D, ? extends Number> worldToDoubleMappingY) {
		return sanityCheckFilter(data, worldToDoubleMappingX, worldToDoubleMappingY, true);
	}

	/**
	 * Checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping functions.
	 * 
	 * @param <D>                   d
	 * @param data                  the data collection
	 * @param worldToDoubleMappingX mapping
	 * @param worldToDoubleMappingY mapping
	 * @param warnForQualityLeaks   if warnings shall not be printed out
	 *                              (functionality remains the same)
	 * @return checked data
	 */
	public static <D> List<D> sanityCheckFilter(Iterable<D> data,
			Function<? super D, ? extends Number> worldToDoubleMappingX,
			Function<? super D, ? extends Number> worldToDoubleMappingY, boolean warnForQualityLeaks) {
		List<D> returnList = new ArrayList<D>();

		try {
			for (D t : data) {
				Number x = worldToDoubleMappingX.apply(t);
				Number y = worldToDoubleMappingY.apply(t);
				if (x != null && !Double.isNaN(x.doubleValue()) && y != null && !Double.isNaN(y.doubleValue()))
					returnList.add(t);
				else if (warnForQualityLeaks)
					System.err.println("VisualMappings.sanityCheckFilter: object " + t
							+ " did not pass the sanity check and was ignored");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnList;
	}

	/**
	 * Checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping functions.
	 * 
	 * @param <D>                   d
	 * @param data                  the data
	 * @param worldToDoubleMappings list of mappings, as, e.g., available for
	 *                              parallel coordinates plots
	 * @return the checked data
	 */
	public static <D> List<D> sanityCheckFilter(Iterable<D> data,
			Iterable<Function<? super D, Double>> worldToDoubleMappings) {
		return sanityCheckFilter(data, worldToDoubleMappings, true);
	}

	/**
	 * Checks whether a given data object D can be mapped to Double that is neither
	 * null nor NaN. Returns a new list, only containing those elements which can be
	 * applied by the given position mapping functions.
	 * 
	 * @param <D>                   d
	 * @param data                  the data collection
	 * @param worldToDoubleMappings list of mappings, as, e.g., available for
	 *                              parallel coordinates plots
	 * @param warnForQualityLeaks   if warnings shall not be printed out
	 *                              (functionality remains the same)
	 * @return checked data
	 */
	public static <D> List<D> sanityCheckFilter(Iterable<D> data,
			Iterable<Function<? super D, Double>> worldToDoubleMappings, boolean warnForQualityLeaks) {
		List<D> returnList = new ArrayList<D>();

		try {
			for (D t : data) {
				boolean accepted = true;

				for (Function<? super D, ? extends Number> mapping : worldToDoubleMappings) {
					Number d = mapping.apply(t);
					if (d == null || Double.isNaN(d.doubleValue())) {
						accepted = false;
						if (warnForQualityLeaks)
							System.err.println("VisualMappings.sanityCheckFilter: object " + t
									+ " did not pass the sanity check and was ignored");
						break;
					}
				}
				if (accepted)
					returnList.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnList;
	}

	/**
	 * Maps world data to Numbers
	 * 
	 * @param <D>                  d
	 * @param data                 the data collection
	 * @param worldToNumberMapping mapping
	 * @param warnForQualityLeaks  warning
	 * @return mapped data
	 */
	public static <D> List<Number> map(Iterable<D> data, Function<? super D, ? extends Number> worldToNumberMapping,
			boolean warnForQualityLeaks) {
		List<Number> values = new ArrayList<>();

		if (data == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.sanityCheckFilter: given data was null");
			return values;
		}

		if (worldToNumberMapping == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.sanityCheckFilter: given worldToDoubleMapping was null");
			return values;
		}

		try {
			for (D t : data) {
				Number d = worldToNumberMapping.apply(t);
				if (d != null && !Double.isNaN(d.doubleValue()))
					values.add(d);
				else if (warnForQualityLeaks)
					System.err.println("VisualMappings.sanityCheckFilter: object " + t
							+ " did not pass the sanity check and was ignored");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;
	}

	public static <D> Number[] minMax(Iterable<D> data, Function<? super D, ? extends Number> worldToNumberMapping) {
		Number[] minMax = new Number[2];

		minMax[0] = Double.POSITIVE_INFINITY;
		minMax[1] = Double.NEGATIVE_INFINITY;

		if (data != null)
			for (D d : data) {
				Number apply = worldToNumberMapping.apply(d);
				if (apply == null || Double.isNaN(apply.doubleValue()))
					continue;

				double x = apply.doubleValue();
				minMax[0] = Math.min(minMax[0].doubleValue(), x);
				minMax[1] = Math.max(minMax[1].doubleValue(), x);
			}

		return minMax;
	}

	/**
	 * 
	 * @param <D>
	 * @param data
	 * @param worldToDoubleMappingX relative coordinates required
	 * @param worldToDoubleMappingY relative coordinates required
	 * @param x
	 * @param y
	 * @param warnForQualityLeaks
	 * @return
	 */
	public static <D> List<D>[][] visualAggregation2D(Iterable<D> data,
			Function<? super D, ? extends Number> worldToDoubleMappingX,
			Function<? super D, ? extends Number> worldToDoubleMappingY, int x, int y, boolean warnForQualityLeaks) {

		if (data == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.visualAggregation2D: given data was null");
			return null;
		}

		if (worldToDoubleMappingX == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.visualAggregation2D: given worldToDoubleMappingX was null");
			return null;
		}

		if (worldToDoubleMappingY == null) {
			if (warnForQualityLeaks)
				System.err.println("VisualMappings.visualAggregation2D: given worldToDoubleMappingY was null");
			return null;
		}

		if (x < 1 || y < 1) {
			if (warnForQualityLeaks)
				System.err
						.println("VisualMappings.visualAggregation2D: given aggregation count < 1: " + x + " and " + y);
			return null;
		}

		@SuppressWarnings("unchecked")
		List<D>[][] aggregates = new ArrayList[x][y];
		for (int xG = 0; xG < x; xG++)
			for (int yG = 0; yG < y; yG++)
				aggregates[xG][yG] = new ArrayList<>();

		try {
			for (D t : data) {
				Number xV = worldToDoubleMappingX.apply(t);
				if (xV != null && !Double.isNaN(xV.doubleValue())) {
					Number yV = worldToDoubleMappingY.apply(t);
					if (yV != null && !Double.isNaN(yV.doubleValue())) {

						if (xV.doubleValue() < 0.0 || xV.doubleValue() > 1.0) {
							if (warnForQualityLeaks)
								System.err.println("VisualMappings.visualAggregation2D: given value for " + t
										+ " was not relative: " + xV);
							return null;
						}
						if (yV.doubleValue() < 0.0 || yV.doubleValue() > 1.0) {
							if (warnForQualityLeaks)
								System.err.println("VisualMappings.visualAggregation2D: given value for " + t
										+ " was not relative: " + yV);
							return null;
						}

						int xG = xV.doubleValue() == 1.0 ? x - 1 : (int) Math.floor(xV.doubleValue() * x);
						int yG = yV.doubleValue() == 1.0 ? y - 1 : (int) Math.floor(yV.doubleValue() * y);

						aggregates[xG][yG].add(t);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aggregates;
	}
}
