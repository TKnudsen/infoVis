package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanels;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class RangeSliderValueDistributionTest {

	public static void main(String[] args) {

		// data
		int min = 0;
		int max = 100;

		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < 200; i++)
			values.add(Math.random() * 100);
		values.add(0.0);
		values.add(100.0);

		// range slider
		InfoVisRangeSlider rangeSlider = new InfoVisRangeSlider(min, max, 25, 75);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(rangeSlider, BorderLayout.SOUTH);

		SelectionModel<Double> selectionModel = SelectionModels.create();
		rangeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				List<Double> selected = new ArrayList<Double>();

				Predicate<Number> predicate = InfoVisRangeSliders.predicate(rangeSlider);
				for (Double d : values)
					if (predicate.test(d))
						selected.add(d);

				selectionModel.setSelection(selected);
			}
		});

		Distribution1DHorizontalPanel<Double> distributionPanel = Distribution1DHorizontalPanels
				.createForDoubles(values);
		// THIS MAGIC LINE COORDINATES THE WORLD AND PIXEL COORDINATES OF THE X-AXIS
		distributionPanel.setXPositionEncodingFunction(rangeSlider.getXPositionEncodingFunction());
		distributionPanel.setShowTrianglesForSelection(false);
		
		//add boxplot
		BoxPlotHorizontalPainter boxPlotPainter = new BoxPlotHorizontalPainter(values);
		// THIS MAGIC LINE COORDINATES THE WORLD AND PIXEL COORDINATES OF THE X-AXIS
		boxPlotPainter.setXPositionEncodingFunction(rangeSlider.getXPositionEncodingFunction());
		boxPlotPainter.setBackgroundPaint(null);
		boxPlotPainter.setPaint(ColorTools.setAlpha(Color.GRAY, 0.66f));
		distributionPanel.addChartPainter(boxPlotPainter);

		// interaction
		ShapeAttributes shapeAttributes = new ShapeAttributes(Color.BLUE, DisplayTools.standardStroke);
		Distribution1DHorizontalPanels.addInteraction(distributionPanel, selectionModel, shapeAttributes);

		contentPane.add(distributionPanel, BorderLayout.CENTER);

		SVGFrameTools.dropSVGFrame(contentPane, "Slider Test", 800, 200);
	}

}
