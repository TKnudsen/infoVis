package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.github.TKnudsen.infoVis.view.interaction.handlers.ZoomingHandler;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;

/**
 * <p>
 * InfoVis
 * </p>
 *
 * <p>
 * Copyright: (c) 2018-2026 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 *
 * <p>
 * Demonstrates {@link ZoomingHandler} -- the "linked axis" zoom handler, as
 * opposed to {@link com.github.TKnudsen.infoVis.view.interaction.handlers.ZoomInteractionHandler}
 * (see {@link ScatterPlotTester} for that one). Two independent
 * {@link ScatterPlot}s share the same x (world) value range but plot
 * unrelated y data. A single {@code ZoomingHandler}, attached only to the
 * left panel's mouse wheel, reads the left panel's own x position-encoding
 * function for the zoom math, then broadcasts the resulting interval to
 * listeners on <b>both</b> panels -- scrolling over the left panel zooms the
 * x axis of both panels in sync, exactly the cross-panel linked-brushing
 * pattern {@code ZoomingHandler} was built for (and the one thing
 * {@code ZoomInteractionHandler} cannot do, since it always drives exactly
 * one {@code IZooming} target).
 * </p>
 *
 * <p>
 * The right panel never gets its own handler attached -- it is purely a
 * listener, matching how {@code TimeSeriesLib}'s
 * {@code TimeSeriesUnivariateChartTest} links several time series panels to
 * one shared zoom/pan gesture.
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.00
 */
public class ZoomingHandlerTester {

	private static final double X_MIN = 0.0;
	private static final double X_MAX = 1000.0;

	public static void main(String[] args) {
		Random random = new Random();

		List<Double[]> pointsA = new ArrayList<>();
		List<Double[]> pointsB = new ArrayList<>();
		for (int i = 0; i < 400; i++) {
			double x = random.nextDouble() * X_MAX;
			pointsA.add(new Double[] { x, random.nextDouble() * 100 });
			pointsB.add(new Double[] { x, Math.sin(x / 50.0) * 50 + 50 });
		}

		Function<Double[], Paint> colorMapping = p -> Color.DARK_GRAY;
		Function<Double[], Double> mapX = p -> p[0];
		Function<Double[], Double> mapY = p -> p[1];

		ScatterPlot<Double[]> panelA = new ScatterPlot<>(pointsA, colorMapping, mapX, mapY);
		ScatterPlot<Double[]> panelB = new ScatterPlot<>(pointsB, colorMapping, mapX, mapY);

		// ZoomingHandler reads panelA's own x position-encoding function for the zoom
		// math (cursor position, current/global range), but never writes to it --
		// the actual commit happens in the listener below, applied identically to
		// both panels via their own axis setters (never the function directly; see
		// PositionEncodingRangeTools for why that distinction matters).
		ZoomingHandler xZoomHandler = new ZoomingHandler(X_MIN, X_MAX, panelA.getXPositionEncodingFunction());
		xZoomHandler.addNumberIntervalListener(event -> {
			double min = event.getNewNumberInterval().getStart().doubleValue();
			double max = event.getNewNumberInterval().getEnd().doubleValue();

			panelA.setXAxisMinValue(min);
			panelA.setXAxisMaxValue(max);
			panelB.setXAxisMinValue(min);
			panelB.setXAxisMaxValue(max);
		});
		xZoomHandler.attachTo(panelA);

		JPanel content = new JPanel(new GridLayout(1, 2, 8, 0));
		content.add(labeled("Panel A -- scroll here (mouse wheel to zoom, double-click to reset)", panelA));
		content.add(labeled("Panel B -- x axis follows Panel A, no handler attached here", panelB));

		JFrame frame = new JFrame("ZoomingHandler -- linked x-axis zoom across two panels");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(content);
		frame.setSize(1000, 520);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static JPanel labeled(String title, java.awt.Component panel) {
		JPanel wrapper = new JPanel(new BorderLayout());
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		wrapper.add(label, BorderLayout.NORTH);
		wrapper.add(panel, BorderLayout.CENTER);
		return wrapper;
	}
}
