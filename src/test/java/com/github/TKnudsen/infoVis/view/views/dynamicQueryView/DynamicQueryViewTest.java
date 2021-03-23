package com.github.TKnudsen.infoVis.view.views.dynamicQueryView;

import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryView;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryViews;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class DynamicQueryViewTest {

	private static DoubleParser doubleParser = new DoubleParser();

	public static void main(String[] args) {

		List<ComplexDataObject> titanicDataSet = DataSets.titanicDataSet();
		Function<ComplexDataObject, Number> toNumberFunction = e -> doubleParser.apply(e.getAttribute("FARE"));

		// filter NaNs
		titanicDataSet = VisualMappings.sanityCheckFilter(titanicDataSet, toNumberFunction, true);

		SelectionModel<ComplexDataObject> selectionModel = SelectionModels.create();

		// view
		DynamicQueryView<ComplexDataObject> dynamicQuery = DynamicQueryViews.createDynamicQuery(titanicDataSet,
				toNumberFunction, selectionModel,
				"Dynamic Query View, Test with the Fares Attribute of the Titanic Dataset", 25);

		SVGFrameTools.dropSVGFrame(dynamicQuery, "", 400, 200);
	}

}
