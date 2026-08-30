package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Static helper for downsampling a color list to a target count by cropping
 * evenly-distributed indices rather than truncating from one end.
 * </p>
 *
 * @version 1.0
 */
public class QualitativeColormapsTools {

	public static List<Color> cropSampledSubset(List<Color> colors, int count) {
		List<Color> returnColors = new ArrayList<>();

		// strategy crop highest indices first to avoid index shifts
		// strategy: gartenzaunproblem. crop indices between count+1 fix points
		// rotate backwards in the hue cicle with maximum of 4 steps per round
		while (colors.size() > 0) {
			List<Integer> indices = calculateIndicesForColorCropping(colors.size(), Math.min(count, 4));
			for (Integer i : indices)
				returnColors.add(colors.remove(i.intValue()));
		}

		return returnColors;
	}

	/**
	 * calculates a list of indices to be cropped from the color array. given the
	 * example of 4 indices from a color array size of 19 the indices 16, 11, 7, and
	 * 2 are calculated. For the array size of 11 the indices 9, 6, 4, and 1 are
	 * calculated.
	 * 
	 * @param size
	 * @param count
	 * @return
	 */
	protected static List<Integer> calculateIndicesForColorCropping(int size, int count) {
		Set<Integer> indices = new LinkedHashSet<>();

		for (int i = count; i > 0; i--) {
			indices.add((int) (((2 * i - 1) / (double) (count * 2)) * size));
		}

		return new ArrayList<>(indices);
	}
}
