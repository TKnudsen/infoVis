package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class BivariateValueDistributionPanel<T> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<T> data;
	private final Function<T, Double> worldPositionMappingX;
	private final Function<T, Double> worldPositionMappingY;
	private SelectionModel<T> selectionModel = null;
	private final String chartTitle;

	// internal
	private ScatterPlot<T> scatterplot;

	private Function<? super T, ? extends Paint> colorMapping = new ConstantColorEncodingFunction<>(Color.GRAY);

	public BivariateValueDistributionPanel(List<T> data, Function<T, Double> worldPositionMappingX,
			Function<T, Double> worldPositionMappingY, SelectionModel<T> selectionModel, String chartTitle) {

		this.data = VisualMappings.sanityCheckFilter(data, worldPositionMappingX, worldPositionMappingY, true);
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;
		this.selectionModel = selectionModel;
		this.chartTitle = chartTitle;

		initializeScatterplot();
	}

	private void initializeScatterplot() {
		this.setLayout(new BorderLayout());
		if (chartTitle != null) {
			JLabel label = new JLabel(chartTitle);
			label.setAlignmentX(CENTER_ALIGNMENT);
			this.add(label, BorderLayout.NORTH);
		}

		scatterplot = new ScatterPlot<T>(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
//		scatterplot.setSizeEncodingFunction(new ConstantSizeEncodingFunction<>(2.5));
		scatterplot.setSizeEncodingFunction(new SizeEncodingFunction<>(scatterplot, 0.95, 2.0));
		scatterplot.setXAxisOverlay(true);
		scatterplot.setYAxisOverlay(true);
		scatterplot.setDrawXAxis(false);
		scatterplot.setDrawYAxis(false);
		scatterplot.setAlphaAdjustment(true);

		// SELECTION MODEL
		SelectionModel<T> selectionModel = (this.selectionModel == null) ? SelectionModels.create()
				: this.selectionModel;

		// SELECTION HANDLER
		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(scatterplot);
		selectionHandler.setClickSelection(scatterplot);
		selectionHandler.setRectangleSelection(scatterplot);

		scatterplot.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		scatterplot.setSelectedFunction(new Function<T, Boolean>() {
			@Override
			public Boolean apply(T t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		// LASSO SELECTION (RIGHT MOUSE BUTTON)
		LassoSelectionHandler<T> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel, MouseButton.RIGHT);
		lassoSelectionHandler.attachTo(scatterplot);
		lassoSelectionHandler.setShapeSelection(scatterplot);

		scatterplot.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				lassoSelectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(new SelectionListener<T>() {

			@Override
			public void selectionChanged(SelectionEvent<T> selectionEvent) {
				repaint();
				revalidate();
			}
		});

		this.add(scatterplot, BorderLayout.CENTER);
	}

	public Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	public void setColorMapping(Function<? super T, Paint> colorMapping) {
		this.colorMapping = colorMapping;
		this.scatterplot.setColorEncodingFunction(colorMapping);
	}

	public void setSelectionModel(SelectionModel<T> selectionModel) {
		this.selectionModel = selectionModel;
	}

	public ScatterPlot<T> getScatterplot() {
		return scatterplot;
	}

	public void setScatterplot(ScatterPlot<T> scatterplot) {
		this.scatterplot = scatterplot;
	}

}
