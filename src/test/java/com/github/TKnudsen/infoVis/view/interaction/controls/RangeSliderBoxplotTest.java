package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSlider;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

public class RangeSliderBoxplotTest {

	public static void main(String[] args) {

		int min = 0;
		int max = 100;

		// no need any more: range slider was replaced
		// LookAndFeelFactory.setDefaultStyle(1);
		InfoVisRangeSlider rangeSlider = new InfoVisRangeSlider(min, max, 25, 75);
		rangeSlider.setOrientation(JSlider.HORIZONTAL);
		rangeSlider.setFlipThumb(false);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(rangeSlider, BorderLayout.SOUTH);

		List<Double> values = new ArrayList<Double>();

		for (int i = 0; i < 200; i++)
			values.add(Math.random() * 100);
		values.add(0.0);
		values.add(100.0);

		BoxPlotHorizontalPainter boxPlotPainter = new BoxPlotHorizontalPainter(values);

		// THIS MAGIC LINE COORDINATES THE WORLD AND PIXEL COORDINATES OF THE X-AXIS
		boxPlotPainter.setXPositionEncodingFunction(rangeSlider.getXPositionEncodingFunction());

		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(boxPlotPainter);
		contentPane.add(panel, BorderLayout.CENTER);

		SVGFrameTools.dropSVGFrame(contentPane, "Slider Test", 800, 400);
	}

}
