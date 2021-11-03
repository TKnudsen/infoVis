package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Scatter plot matrix chart panel.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.07 TODO the individual scatterplots are not yet connected with the
 *          interaction design which is triggered from outside. To do so, create
 *          ScatterPlotMatrix class that accepts T
 */
public class ScatterPlotMatrixChartPanel extends InfoVisChartPanel implements IRectangleSelection<Double[]>,
		IClickSelection<Double[]>, ISelectionVisualizer<Double[]>, ISizeEncoding<Double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2949962927634263599L;

	private List<Double[]> data;

	private List<String> attributeNames;

	private Function<? super Double[], ? extends Paint> colorMapping;

	private ScatterPlot<Double[]>[][] infoVisScatterPlotChartPanels;

	/**
	 * 
	 * @param data multi-dimensional data
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data) {
		this(data, null, null);
	}

	/**
	 * 
	 * @param data   multi-dimensional data
	 * @param colors
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data, List<Color> colors) {
		this(data, colors, null);
	}

	/**
	 * 
	 * @param data           multi-dimensional data
	 * @param colors
	 * @param attributeNames
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data, List<Color> colors, List<String> attributeNames) {

		if (this.data != null && colors != null && data.size() != colors.size())
			throw new IllegalArgumentException("InfoVisScatterPlotMatrixChartPanel: unequal list sizes.");

		this.data = Collections.unmodifiableList(data);
		this.colorMapping = new ColorEncodingFunction<>(data, colors, Color.BLACK);

		this.attributeNames = attributeNames;

		refreshScatterplotMatrix();
	}

	public ScatterPlotMatrixChartPanel(ColorEncodingFunction<Double[]> colorMapping) {
		this.colorMapping = colorMapping;

		Set<Double[]> data = colorMapping.getData();
		List<Double[]> temp = new CopyOnWriteArrayList<>(data);
		this.data = Collections.unmodifiableList(temp);

		refreshScatterplotMatrix();
	}

	public ScatterPlotMatrixChartPanel(List<Double[]> data, ColorEncodingFunction<Double[]> colorMapping) {
		this.data = Collections.unmodifiableList(data);
		this.colorMapping = colorMapping;

		refreshScatterplotMatrix();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	@SuppressWarnings("unchecked")
	private void refreshScatterplotMatrix() {
		if (data == null)
			return;

		if (attributeNames == null)
			attributeNames = new ArrayList<>();

		while (attributeNames.size() < data.get(0).length)
			attributeNames.add("");

		infoVisScatterPlotChartPanels = new ScatterPlot[attributeNames.size()][attributeNames.size()];

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {

				List<Double[]> localdata = new ArrayList<>();
				List<Paint> localColors = new ArrayList<>();

				for (int i = 0; i < data.size(); i++)
					if (Double.isNaN(data.get(i)[x]) || Double.isNaN(data.get(i)[y]))
						continue;
					else {
						localdata.add(new Double[] { data.get(i)[x], data.get(i)[y] });
						localColors.add(colorMapping.apply(data.get(i)));
					}

//				ScatterPlotChartPanel panel = new ScatterPlotChartPanel(localdata, localColors);
				ScatterPlot<Double[]> panel = ScatterPlots.createForDoubles(localdata, localColors);

				infoVisScatterPlotChartPanels[x][y] = panel;

				// TODO
				// panel.setDynamicAlphaAdjustment(true);

				// TODO
				// panel.setLogarithmicScale(logarithmicScale);
			}

		removeAll();
		this.setLayout(new GridLayout(attributeNames.size(), attributeNames.size()));

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				add(infoVisScatterPlotChartPanels[x][y]);

		setBackground(null);
	}

	@Override
	public List<Double[]> getElementsAtPoint(Point p) {
		List<Double[]> selectedElements = new ArrayList<>();

		// TODO buggy! uses 2D instead of world coordinates!
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					selectedElements.addAll(infoVisScatterPlotChartPanels[x][y].getElementsAtPoint(p));

		return selectedElements;
	}

	@Override
	public List<Double[]> getElementsInRectangle(RectangularShape rectangle) {
		List<Double[]> selectedElements = new ArrayList<>();

		// TODO buggy! uses 2D instead of world coordinates!
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					selectedElements.addAll(infoVisScatterPlotChartPanels[x][y].getElementsInRectangle(rectangle));

		return selectedElements;
	}

	@Override
	public void setSelectedFunction(Function<? super Double[], Boolean> selectedFunction) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setSelectedFunction(selectedFunction);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super Double[], Double> sizeEncodingFunction) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setSizeEncodingFunction(sizeEncodingFunction);
	}

	public void setDrawAxes(boolean drawAxes) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null) {
					infoVisScatterPlotChartPanels[x][y].setDrawXAxis(drawAxes);
					infoVisScatterPlotChartPanels[x][y].setDrawYAxis(drawAxes);
				}
	}
}