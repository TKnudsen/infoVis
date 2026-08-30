package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * @version 1.05
 * @since 2016
 */
public class AxisCartTools {

	/**
	 * 
	 * @param value
	 * @param maxValue determines the formatting rule
	 * @return
	 */
	public static String suggestMeaningfulValueString(double value, double maxValue) {
		if (maxValue <= 0.05)
			return String.format(java.util.Locale.ROOT, "%.3f", MathFunctions.round(value, 3));
		if (maxValue <= 0.5)
			return String.format(java.util.Locale.ROOT, "%.2f", MathFunctions.round(value, 2));
		if (maxValue <= 5)
			return String.format(java.util.Locale.ROOT, "%.2f", MathFunctions.round(value, 2));
		if (maxValue <= 50)
			return String.format(java.util.Locale.ROOT, "%.1f", MathFunctions.round(value, 1));
		if (maxValue < 1000)
			return String.format(java.util.Locale.ROOT, "%.0f", MathFunctions.round(value, 0));
		if (maxValue < 5000)
			return String.valueOf(MathFunctions.round(value / 1000.0, 2)) + "k";
		if (maxValue < 1000000)
			return String.valueOf(MathFunctions.round(value / 1000.0, 1)) + "k";
		if (maxValue < 1000000000)
			return String.valueOf(MathFunctions.round(value / 1000000.0, 1)) + "M";
		if (maxValue < 1000000000000L)
			return String.valueOf(MathFunctions.round(value / 1000000000.0, 1)) + "G";
		return "";
	}

	public static List<Double> suggestMeaningfulValueIntervalLogarithmic(double minValue, double maxValue,
			int targetNumber) {
		if (Double.isNaN(minValue))
			minValue = 0;
		if (Double.isNaN(maxValue))
			return null;

		List<Double> markerValues = new ArrayList<>();

		if (targetNumber <= 2) {
			markerValues.add(minValue);
			markerValues.add(maxValue);
			return markerValues;
		}

		// step1 identify nr of pows of 10
		double lowP;
		if (minValue <= 0)
			lowP = 0;
		else
			lowP = Math.log10(minValue);
		double highP = Math.log10(maxValue);
		double diff = Math.abs(highP - lowP);

		double interval = 1.0;
		while (diff / interval < targetNumber * 0.66)
			interval /= 2;
		while (diff / interval > targetNumber * 1.33)
			interval *= 2;

		// step2 identify first marker
		double first = Math.floor(lowP);
		while (first < lowP)
			first += interval;

		if (interval == 0) {
			markerValues.add(minValue);
			markerValues.add(maxValue);
			return markerValues;
		}

		// iterate and add
		for (double d = first; d <= highP; d += interval)
			markerValues.add(Math.pow(10, d));

		return markerValues;
	}

	public static double suggestMeaningfulValueIntervalLinear(double valueInterval) {
		if (Double.isNaN(valueInterval))
			return Double.NaN;

		if (Double.isInfinite(valueInterval))
			return Double.NaN;

		double tmp = valueInterval;
		int pow = 0;
		while (tmp > 10) {
			tmp /= 10;
			pow++;
		}
		if (tmp < 0.005)
			tmp = 0.005;
		else if (tmp < 0.02)
			tmp = 0.01;
		else if (tmp < 0.066)
			tmp = 0.05;
		else if (tmp < 0.10)
			tmp = 0.1;
		else if (tmp < 0.5)
			tmp = 0.25;
		else if (tmp < 0.85)
			tmp = 0.5;
		else if (tmp < 1.33)
			tmp = 1;
		else if (tmp < 2.5)
			tmp = 2;
		else if (tmp < 6.0)
			tmp = 5;
		else
			tmp = 10;
		tmp = tmp * Math.pow(10, pow);
		return tmp;
	}

	public static double suggestMeaningfulValueIntervalLinear(int valueInterval) {
		if (Double.isNaN(valueInterval))
			return Double.NaN;

		if (Double.isInfinite(valueInterval))
			return Double.NaN;

		double tmp = valueInterval;
		int pow = 0;
		while (tmp > 10) {
			tmp /= 10;
			pow++;
		}
		if (tmp < 0.5)
			tmp = 0.0;
		else if (tmp < 1.5)
			tmp = 1;
		else if (tmp < 2.5)
			tmp = 2;
		else if (tmp < 3.5)
			tmp = 3;
		else if (tmp < 8)
			tmp = 5;
		else
			tmp = 10;
		tmp = tmp * Math.pow(10, pow);
		return tmp;
	}

	public static List<Integer> suggestMeaningfulValueIntervalLogarithmic(int minValue, int maxValue,
			int targetNumber) {
		if (Double.isNaN(minValue))
			minValue = 0;
		if (Double.isNaN(maxValue))
			return null;

		List<Integer> markerValues = new ArrayList<>();

		if (targetNumber <= 2) {
			markerValues.add(minValue);
			markerValues.add(maxValue);
			return markerValues;
		}

		// step1 identify nr of pows of 10
		double lowP = Math.log10(minValue);
		double highP = Math.log10(maxValue);
		double diff = Math.abs(highP - lowP);

		double interval = 1.0;
		while (diff / interval < targetNumber * 0.66)
			interval /= 2;
		while (diff / interval > targetNumber * 1.33)
			interval *= 2;

		// step2 identify first marker
		double first = Math.floor(lowP);
		while (first < lowP)
			first += interval;

		if (interval == 0) {
			markerValues.add(minValue);
			markerValues.add(maxValue);
			return markerValues;
		}

		// iterate and add
		for (double d = first; d <= highP; d += interval)
			markerValues.add((int) MathFunctions.round(Math.pow(10, d), 0));

		return markerValues;
	}

	/**
	 * can be used to re-scale the number of markers, given the marker positions
	 * calculated be an axis painter.
	 * 
	 * @param markerPositionsWithLabels positions
	 * @param pow2                      pow
	 * @param lowerBound                lower
	 * @param upperBound                higher
	 * @return sorted set
	 */
	public static SortedSet<Double> refineMarkerPositionResolution(
			List<Entry<Double, String>> markerPositionsWithLabels, double pow2, double lowerBound, double upperBound) {

		SortedSet<Double> markersNew = new TreeSet<>();

		// use the raster of the markerPositionsWithLabels
		// but iterate in the frequency of the bars
		double deltaM = markerPositionsWithLabels.get(1).getKey() - markerPositionsWithLabels.get(0).getKey();
		deltaM /= pow2;

		double pixelStart = markerPositionsWithLabels.get(0).getKey();
		while (pixelStart - deltaM > lowerBound)
			pixelStart -= deltaM;

		// while (pixelStart - bw * 0.5 <= chartRectangle.getMaxX()) {
		while (pixelStart <= upperBound) {
			markersNew.add(pixelStart);

			// already prepare for next iteration
			pixelStart += deltaM;
		}

		return markersNew;
	}
}
