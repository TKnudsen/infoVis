package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class AxisCartTools {

	public static String suggestMeaningfulValueString(double value) {
		if (value < 0.5)
			return String.valueOf(roundToDigitsUnequalsZero(value, 2));
		if (value < 1)
			return String.valueOf(roundToDigitsUnequalsZero(value, 3));
		if (value < 10)
			return String.valueOf((int) roundToDigitsUnequalsZero(value, 3));
		if (value < 100)
			return String.valueOf((int) roundToDigitsUnequalsZero(value, 3));
		if (value < 1000)
			return String.valueOf((int) MathFunctions.round(value, 0));
		if (value < 2000)
			return String.valueOf(MathFunctions.round(value / 1000.0, 2)) + "k";
		if (value < 1000000)
			return String.valueOf(MathFunctions.round(value / 1000.0, 1)) + "k";
		if (value < 1000000000)
			return String.valueOf((int) MathFunctions.round(value / 1000000.0, 3)) + "m";
		if (value < 1000000000000L)
			return String.valueOf((int) MathFunctions.round(value / 1000000000.0, 3)) + "g";
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

	@Deprecated
	public static double roundToDigitsUnequalsZero(double value, int decimals) {
		if (Double.isNaN(value))
			return value;

		double fact = Math.pow(10, decimals);
		double d = value * fact;
		d = Math.round(d);
		d /= fact;
		return d;
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
		else if (tmp < 1.66)
			tmp = 1;
		else if (tmp < 4.5)
			tmp = 2;
		else if (tmp < 8)
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
	 * @param markerPositionsWithLabels
	 * @param pow2
	 * @param chartRectangle
	 * @return
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
