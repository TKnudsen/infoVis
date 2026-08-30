package com.github.TKnudsen.infoVis.view.views;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class AttributeDistributionsViewTest {

	private static List<ComplexDataObject> titanicData = DataSets.titanicDataSet();

	public static void main(String[] args) {

		SelectionModel<Long> selectionModel = SelectionModels.create();

		ComplexDataContainer container = new ComplexDataContainer(titanicData);

		AttributeDistributionsView attributeDistributionsView = new AttributeDistributionsView(container,
				selectionModel);

		SVGFrameTools.dropSVGFrame(attributeDistributionsView, "", 400, 200);

		selectionModel.addSelectionListener(new SelectionListener<Long>() {

			@Override
			public void selectionChanged(SelectionEvent<Long> selectionEvent) {
				System.out.println(selectionEvent.getSelectionModel().getSelection().size());

			}
		});

		System.out.println();
	}
}
