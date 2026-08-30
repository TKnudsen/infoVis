package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;

/**
 * <p>
 * Renders the Titanic dataset (AGE on X, FARE on Y) through all three
 * scatterplot implementations side by side -- {@code ScatterPlot} (CPU),
 * {@code ScatterPlotIndexedGPU} and {@code ScatterPlotSpriteGPU} (both
 * GPU) -- with linked click/rectangle/lasso (right mouse button) selection
 * across all three, via {@link ScatterPlotSideBySideTesters}.
 * </p>
 *
 * <p>
 * Supersedes the previous single-implementation Titanic demos
 * (one for the indexed-geometry painter, one for the point-sprite painter):
 * with both GPU implementations now rendering correctly, comparing them side
 * by side -- against the CPU baseline, and against each other -- is strictly
 * more useful than either alone.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class ScatterPlotSideBySideTitanicTester {

	public static void main(String[] args) {
		List<ComplexDataObject> titanicDataSet = DataSets.titanicDataSet();

		Function<ComplexDataObject, Paint> colorMapping = cdo -> Color.GRAY;
		Function<ComplexDataObject, Double> mapX = cdo -> Parsers.parseDouble(cdo.getAttribute("AGE"));
		Function<ComplexDataObject, Double> mapY = cdo -> Parsers.parseDouble(cdo.getAttribute("FARE"));

		ScatterPlotSideBySideTesters.show("Titanic dataset -- CPU vs. GPU (Indexed) vs. GPU (Sprite)",
				titanicDataSet, colorMapping, mapX, mapY, 480, 480);
	}
}
