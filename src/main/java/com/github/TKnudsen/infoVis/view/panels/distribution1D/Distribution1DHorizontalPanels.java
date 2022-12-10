package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;

/**
 * Deprecated use Distribution1DPanels
 *
 */
public class Distribution1DHorizontalPanels {

	@Deprecated // use Distribution1DPanels
	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<>(data, worldToDoubleMapping);
	}

	@Deprecated // use Distribution1DPanels
	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Color color,
			double minGlobal, double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color), minGlobal, maxGlobal);
	}

	/**
	 * Deprecated //use Distribution1DPanels
	 * 
	 * @param <T>                     t
	 * @param distributionPanel       the panel
	 * @param selectionModel          the selection model
	 * @param selectionShapeAttribtes adds visual encodings to selected objects.
	 *                                ShapeAttributes include a selection color and
	 *                                a stroke.
	 * @return the listener
	 */
	public static <T> SelectionListener<T> addInteraction(Distribution1DHorizontalPanel<T> distributionPanel,
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

		// set current selection
		distributionPanel.clearSpecialValues();
		for (T t : selectionModel.getSelection())
			distributionPanel.addSpecialValue(t, selectionShapeAttribtes);
		distributionPanel.repaint();

		if (selectionShapeAttribtes != null) {
			SelectionListener<T> selectionListener = new SelectionListener<T>() {

				@Override
				public void selectionChanged(SelectionEvent<T> selectionEvent) {
					distributionPanel.clearSpecialValues();
					for (T t : selectionEvent.getSelectionModel().getSelection())
						distributionPanel.addSpecialValue(t, selectionShapeAttribtes);

					distributionPanel.repaint();
				}
			};
			selectionModel.addSelectionListener(selectionListener);
			return selectionListener;
		}

		return null;
	}

	@Deprecated // use VisualMappings.sanityCheckFilter
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
