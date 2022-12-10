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
	 * @param data
	 * @param worldToDoubleMapping
	 * @return
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
	 * @param data
	 * @param worldToDoubleMapping
	 * @param warnForQualityLeaks  if warnings shall not be printed out
	 *                             (functionality remains the same)
	 * @return
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
	 * @param data
	 * @param worldToDoubleMappingX
	 * @param worldToDoubleMappingY
	 * @return
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
	 * @param data
	 * @param worldToDoubleMappingX
	 * @param worldToDoubleMappingY
	 * @param warnForQualityLeaks   if warnings shall not be printed out
	 *                              (functionality remains the same)
	 * @return
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
	 * @param data
	 * @param worldToDoubleMappings list of mappings, as, e.g., available for
	 *                              parallel coordinates plots
	 * @return
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
	 * @param data
	 * @param worldToDoubleMappings list of mappings, as, e.g., available for
	 *                              parallel coordinates plots
	 * @param warnForQualityLeaks   if warnings shall not be printed out
	 *                              (functionality remains the same)
	 * @return
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
	 * @param <D>
	 * @param data
	 * @param worldToNumberMapping
	 * @param warnForQualityLeaks
	 * @return
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
}
