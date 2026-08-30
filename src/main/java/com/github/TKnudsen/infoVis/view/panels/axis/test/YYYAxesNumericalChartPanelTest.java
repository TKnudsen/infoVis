package com.github.TKnudsen.infoVis.view.panels.axis.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YYYAxesNumericalChartPanel;

/**
 * <p>
 * Standalone demo showing a {@link YYYAxesNumericalChartPanel} with three
 * independent Y axes (temperature, humidity, pressure).
 * </p>
 *
 * @version 1.0
 */
public class YYYAxesNumericalChartPanelTest {

	public static void main(String[] args) {
		// Create a concrete instance (remove 'abstract' from class!)
		YYYAxesNumericalChartPanel<Double> panel = new YYYAxesNumericalChartPanel<Double>(3) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			// Empty body - just instantiate it!
		};

		// Create 3 Y-axis painters for: Temperature, Humidity, Pressure
		List<YAxisNumericalPainter<Double>> yAxisPainters = new ArrayList<>();

		// Temperature axis (0-40 deg C)
		YAxisNumericalPainter<Double> tempAxis = new YAxisNumericalPainter<>(0.0, 40.0);
		tempAxis.setPhysicalUnit("Temperature (deg C)");
		yAxisPainters.add(tempAxis);

		// Humidity axis (0-100%)
		YAxisNumericalPainter<Double> humidityAxis = new YAxisNumericalPainter<>(0.0, 100.0);
		humidityAxis.setPhysicalUnit("Humidity (%)");
		yAxisPainters.add(humidityAxis);

		// Pressure axis (980-1020 hPa)
		YAxisNumericalPainter<Double> pressureAxis = new YAxisNumericalPainter<>(980.0, 1020.0);
		pressureAxis.setPhysicalUnit("Pressure (hPa)");
		yAxisPainters.add(pressureAxis);

		// Set the painters
		panel.setYAxisPainters(yAxisPainters);

		// Configure the panel
		panel.setDrawYAxes(true);
		panel.setDrawAxesNames(true);
		panel.setBackground(Color.WHITE);
		panel.setYAxesLegendOffset(60);

		// Display in a window
		JFrame frame = new JFrame("Simple YYY Chart Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}
