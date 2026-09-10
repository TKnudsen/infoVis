package com.github.TKnudsen.infoVis.view.panels.featureSpace;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;

/**
 * Factory for building one {@link FeatureBoxPlotWithClassInformationPanel}
 * per (double-valued) feature across a collection of feature vectors, with a
 * per-class average marker derived from the given per-vector colors.
 *
 * @author Christian Ritter, Juergen Bernard
 * @version 1.0
 */
public class FeatureBoxPlotWithClassInformationPanels {

	public static <FV extends IFeatureVectorObject<?, ?>> List<FeatureBoxPlotWithClassInformationPanel> createFeatureSpacePainters(
			Collection<FV> featureVectors, List<Color> colors, List<String> sortedFeatureNames,
			Color standardBackgroundColor, Color standardFontColor) {
		if (featureVectors == null || featureVectors.isEmpty() || colors == null
				|| colors.size() != featureVectors.size())
			throw new IllegalArgumentException("List must be not null and of same length");

		List<String> featureNames;
		if (sortedFeatureNames != null)
			featureNames = new ArrayList<>(sortedFeatureNames);
		else
			featureNames = new ArrayList<>(featureVectors.iterator().next().getFeatureKeySet());

		List<List<Double>> vals = new ArrayList<>();
		List<Map<Color, Double>> averages = new ArrayList<>();
		Map<Color, Integer> numberPerColor = new HashMap<>();
		for (Color c : new HashSet<>(colors))
			numberPerColor.put(c, colors.stream().filter(x -> x.equals(c)).collect(Collectors.toList()).size());
		for (String s : sortedFeatureNames) {
			List<Double> v = new ArrayList<>();
			Map<Color, Double> m = new HashMap<>();
			for (Color c : numberPerColor.keySet())
				m.put(c, 0.0);
			int i = 0;
			for (FV fv : featureVectors) {
				Feature<?> f = fv.getFeature(s);
				if (f.getFeatureType() == FeatureType.DOUBLE) {
					v.add((Double) f.getFeatureValue());
					m.put(colors.get(i), m.get(colors.get(i)) + (Double) f.getFeatureValue());
				} else {
					featureNames.remove(s);
					break;
				}
				i++;
			}
			for (Color c : m.keySet())
				m.put(c, m.get(c) / numberPerColor.get(c));
			vals.add(v);
			averages.add(m);
		}
		List<FeatureBoxPlotWithClassInformationPanel> result = new ArrayList<>();
		for (int i = 0; i < vals.size(); i++) {
			FeatureBoxPlotWithClassInformationPanel bp = new FeatureBoxPlotWithClassInformationPanel(vals.get(i), 0.0,
					1.0, standardBackgroundColor, standardFontColor);
			bp.setName(sortedFeatureNames.get(i));
			bp.setBackground(standardBackgroundColor);
			bp.setDrawYAxis(false);
			bp.setShowingTooltips(false);
			for (Color c : averages.get(i).keySet())
				bp.addCluster(averages.get(i).get(c), new ShapeAttributes(c, DisplayTools.thickStroke));
			List<ShapeAttributes> shapes = new ArrayList<>();
			for (Color c : colors)
				shapes.add(new ShapeAttributes(ColorTools.setAlpha(c, 0.5f), new BasicStroke(1)));
			bp.addDetailElements(vals.get(i), shapes);
			result.add(bp);
		}
		return result;
	}
}
