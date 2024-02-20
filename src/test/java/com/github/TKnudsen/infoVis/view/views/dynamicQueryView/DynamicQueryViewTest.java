package com.github.TKnudsen.infoVis.view.views.dynamicQueryView;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryView;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryViews;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class DynamicQueryViewTest {

	private static DoubleParser doubleParser = new DoubleParser();
	private static List<ComplexDataObject> titanicData = DataSets.titanicDataSet();

	public static void main(String[] args) {

		SelectionModel<ComplexDataObject> selectionModel = SelectionModels.create();

		DynamicQueryView<ComplexDataObject> dqFare = createDynamicQuery("FARE", 25, selectionModel);
		DynamicQueryView<ComplexDataObject> dqAge = createDynamicQuery("AGE", 10,selectionModel);

		dqFare.addFilterStatusListener(dqAge);
		dqAge.addFilterStatusListener(dqFare);

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(dqFare);
		panel.add(dqAge);

		SVGFrameTools.dropSVGFrame(panel, "", 400, 200);

		selectionModel.addSelectionListener(new SelectionListener<ComplexDataObject>() {

			@Override
			public void selectionChanged(SelectionEvent<ComplexDataObject> selectionEvent) {
				System.out.println(selectionEvent.getSelectionModel().getSelection().size());

			}
		});
	}

	private static DynamicQueryView<ComplexDataObject> createDynamicQuery(String attribute, int bins,
			SelectionModel<ComplexDataObject> selectionModel) {
		Function<ComplexDataObject, Number> toNumberFunction = e -> doubleParser.apply(e.getAttribute(attribute));

		// filter NaNs
		List<ComplexDataObject> titanicDataSet = VisualMappings.sanityCheckFilter(titanicData, toNumberFunction, true);

		// view
		DynamicQueryView<ComplexDataObject> dynamicQuery = DynamicQueryViews.createDynamicQuery(titanicDataSet,
				toNumberFunction, selectionModel,
				"Dynamic Query View, Test with the " + attribute + " Attribute of the Titanic Dataset", bins);

		DynamicQueryViews.setAllDataColor(dynamicQuery, Color.LIGHT_GRAY);
		DynamicQueryViews.setFilterColor(dynamicQuery, Color.GRAY);

		return dynamicQuery;
	}

}
