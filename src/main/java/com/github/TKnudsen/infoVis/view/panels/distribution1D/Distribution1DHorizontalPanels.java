package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;

public class Distribution1DHorizontalPanels {

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<>(data, worldToDoubleMapping);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Color color) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		Distribution1DHorizontalPanel<Double> horizontalPanel = new Distribution1DHorizontalPanel<>(data,
				worldToDoubleMapping);
		horizontalPanel.setColorEncodingFunction(new ConstantColorEncodingFunction<>(color));

		return horizontalPanel;
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		return createForDoubles(data, colors, Double.NaN, Double.NaN);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Double minGlobal,
			Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Color color,
			Double minGlobal, Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color), minGlobal, maxGlobal);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data,
			List<? extends Paint> colors, Double minGlobal, Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping, colorMapping, minGlobal,
				maxGlobal);
	}

	public static <T> Distribution1DHorizontalPanel<T> create(List<T> data,
			Function<? super T, Double> worldToDoubleMapping) {
		return create(data, worldToDoubleMapping, null, Double.NaN, Double.NaN);
	}

	public static <T> Distribution1DHorizontalPanel<T> create(List<T> data,
			Function<? super T, Double> worldToDoubleMapping, Double minGlobal, Double maxGlobal) {
		return create(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public static <T> Distribution1DHorizontalPanel<T> create(List<T> data,
			Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, Double minGlobal, Double maxGlobal) {
		return new Distribution1DHorizontalPanel<T>(data, worldToDoubleMapping, colorEncodingFunction, minGlobal,
				maxGlobal);
	}

	public static <T> void addInteraction(Distribution1DHorizontalPanel<T> distributionPanel,
			SelectionModel<T> selectionModel) {

		addInteraction(distributionPanel, selectionModel, null);
	}

	/**
	 * 
	 * @param <T>
	 * @param distributionPanel
	 * @param selectionModel
	 * @param selectionShapeAttribtes adds visual encodings to selected objects.
	 *                                ShapeAttributes include a selection color and
	 *                                a stroke.
	 */
	public static <T> void addInteraction(Distribution1DHorizontalPanel<T> distributionPanel,
			SelectionModel<T> selectionModel, ShapeAttributes selectionShapeAttribtes) {

		// SELECTION HANDLER
		SelectionHandler<T> selectionHandler = new SelectionHandler<T>(selectionModel);
		selectionHandler.attachTo(distributionPanel);

		// add rectangle selection
		selectionHandler.setRectangleSelection(distributionPanel);

		distributionPanel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		if (selectionShapeAttribtes != null) {
			selectionModel.addSelectionListener(new SelectionListener<T>() {

				@Override
				public void selectionChanged(SelectionEvent<T> selectionEvent) {
					distributionPanel.clearSpecialValues();

					for (T t : selectionEvent.getSelectionModel().getSelection()) {
						{
							distributionPanel.addSpecialValue(t, selectionShapeAttribtes);
						}
					}

					distributionPanel.repaint();
				}
			});
		}
	}

	/**
	 * applies a filter operation using a list of data. Returns a new list, only
	 * containing those elements which can be applied by the position mapping
	 * function.
	 * 
	 * @param data
	 * @param worldPositionMapping
	 * @param warnForQualityLeaks
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldPositionMapping,
			boolean warnForQualityLeaks) {
		return VisualMappings.sanityCheckFilter(data, worldPositionMapping, warnForQualityLeaks);
	}

}
