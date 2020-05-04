package com.github.TKnudsen.infoVis.view.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DoubleMappingTools {

	private DoubleMappingTools() {

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
	public static <D> List<D> sanityCheckFilter(Iterable<D> data, Function<? super D, Double> worldToDoubleMapping) {
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
	public static <D> List<D> sanityCheckFilter(Iterable<D> data, Function<? super D, Double> worldToDoubleMapping,
			boolean warnForQualityLeaks) {
		List<D> returnList = new ArrayList<D>();

		try {
			for (D t : data) {
				Double d = worldToDoubleMapping.apply(t);
				if (d != null && !Double.isNaN(d))
					returnList.add(t);
				else if (warnForQualityLeaks)
					System.err.println("DoubleMappingTools.sanityCheckFilter: object " + t
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
	public static <D, V> List<D> sanityCheckFilter(Iterable<D> data, Function<? super D, Double> worldToDoubleMappingX,
			Function<? super D, Double> worldToDoubleMappingY) {
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
	public static <D> List<D> sanityCheckFilter(Iterable<D> data, Function<? super D, Double> worldToDoubleMappingX,
			Function<? super D, Double> worldToDoubleMappingY, boolean warnForQualityLeaks) {
		List<D> returnList = new ArrayList<D>();

		try {
			for (D t : data) {
				Double x = worldToDoubleMappingX.apply(t);
				Double y = worldToDoubleMappingY.apply(t);
				if (x != null && !Double.isNaN(x) && y != null && !Double.isNaN(y))
					returnList.add(t);
				else if (warnForQualityLeaks)
					System.err.println("DoubleMappingTools.sanityCheckFilter: object " + t
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

				for (Function<? super D, Double> mapping : worldToDoubleMappings) {
					Double d = mapping.apply(t);
					if (d == null || Double.isNaN(d)) {
						accepted = false;
						if (warnForQualityLeaks)
							System.err.println("DoubleMappingTools.sanityCheckFilter: object " + t
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
}
