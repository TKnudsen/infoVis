package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class Distribution1DPanels {

	public static Distribution1DPanel<Double> createForDoubles(Collection<Double> data, boolean vertical) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		if (vertical)
			return new Distribution1DVerticalPanel<>(data, worldToDoubleMapping);
		else
			return new Distribution1DHorizontalPanel<>(data, worldToDoubleMapping);
	}

	public static Distribution1DPanel<Double> createForDoubles(Collection<Double> data, Color color, boolean vertical) {
		Distribution1DPanel<Double> distribution1dPanel = createForDoubles(data, vertical);

		distribution1dPanel.setColorEncodingFunction(new ConstantColorEncodingFunction<>(color));

		return distribution1dPanel;
	}

	public static Distribution1DPanel<Double> createForDoubles(List<Double> data, Color color, Double minGlobal,
			Double maxGlobal, boolean vertical) {

		Function<Double, Double> worldToDoubleMapping = p -> p;

		Distribution1DPanel<Double> distribution1dPanel = (vertical)
				? new Distribution1DVerticalPanel<>(data, worldToDoubleMapping, minGlobal, maxGlobal)
				: new Distribution1DHorizontalPanel<>(data, worldToDoubleMapping, minGlobal, maxGlobal);

		distribution1dPanel.setColorEncodingFunction(new ConstantColorEncodingFunction<>(color));

		return distribution1dPanel;
	}

	public static Distribution1DPanel<Double> createForDoubles(List<Double> data, List<? extends Paint> colors,
			boolean vertical) {

		return createForDoubles(data, colors, Double.NaN, Double.NaN, vertical);
	}

	public static Distribution1DPanel<Double> createForDoubles(List<Double> data, List<? extends Paint> colors,
			Double minGlobal, Double maxGlobal, boolean vertical) {

		return create(data, p -> p, new ColorEncodingFunction<Double>(data, colors), minGlobal, maxGlobal, vertical);
	}

	public static <T> Distribution1DPanel<T> create(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, boolean vertical) {
		return create(data, worldToDoubleMapping, null, Double.NaN, Double.NaN, vertical);
	}

	public static <T> Distribution1DPanel<T> create(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, double minGlobal, double maxGlobal,
			boolean vertical) {
		return create(data, worldToDoubleMapping, null, minGlobal, maxGlobal, vertical);
	}

	public static <T> Distribution1DPanel<T> create(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, double minGlobal, double maxGlobal,
			boolean vertical) {

		return (vertical)
				? new Distribution1DVerticalPanel<T>(data, worldToDoubleMapping, colorEncodingFunction, minGlobal,
						maxGlobal)
				: new Distribution1DHorizontalPanel<T>(data, worldToDoubleMapping, colorEncodingFunction, minGlobal,
						maxGlobal);
	}

	public static <T> SelectionListener<T> addInteraction(Distribution1DPanel<T> distributionPanel,
			SelectionModel<T> selectionModel) {

		return addInteraction(distributionPanel, selectionModel, null);
	}

	/**
	 * 
	 * @param <T>                     t
	 * @param distributionPanel       the panel
	 * @param selectionModel          selection model
	 * @param selectionShapeAttribtes adds visual encodings to selected objects.
	 *                                ShapeAttributes include a selection color and
	 *                                a stroke.
	 * @return the selection listener
	 */
	public static <T> SelectionListener<T> addInteraction(Distribution1DPanel<T> distributionPanel,
			SelectionModel<T> selectionModel, ShapeAttributes selectionShapeAttribtes) {

		return addInteraction(distributionPanel, selectionModel,
				new ConstantColorEncodingFunction<>(selectionShapeAttribtes.getColor()),
				selectionShapeAttribtes.getStroke());
	}

	/**
	 * 
	 * @param <T>                   t
	 * @param distributionPanel     panel
	 * @param selectionModel        selection model
	 * @param colorEncodingFunction used to encode selected objects
	 * @param stroke                used to highlight selected objects
	 * @return the selection listener
	 */
	public static <T> SelectionListener<T> addInteraction(Distribution1DPanel<T> distributionPanel,
			SelectionModel<T> selectionModel, Function<? super T, ? extends Paint> colorEncodingFunction,
			Stroke stroke) {

		Objects.requireNonNull(stroke);

		SelectionModel<T> sm = (selectionModel != null) ? selectionModel : SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<T> selectionHandler = new SelectionHandler<T>(sm);

		if (distributionPanel instanceof Component)
			selectionHandler.attachTo((Component) distributionPanel);

		// add rectangle selection
		selectionHandler.setRectangleSelection(distributionPanel);

		if (distributionPanel instanceof InfoVisChartPanel)
			((InfoVisChartPanel) distributionPanel).addChartPainter(new ChartPainter() {
				@Override
				public void draw(Graphics2D g2) {
					selectionHandler.draw(g2);
				}
			});

		// set current selection
		distributionPanel.clearSpecialValues();
		for (T t : sm.getSelection())
			distributionPanel.addSpecialValue(t, new ShapeAttributes(colorEncodingFunction.apply(t), stroke));
		if (distributionPanel instanceof Component)
			((Component) distributionPanel).repaint();

		SelectionListener<T> selectionListener = new SelectionListener<T>() {

			@Override
			public void selectionChanged(SelectionEvent<T> selectionEvent) {
				distributionPanel.clearSpecialValues();
				for (T t : selectionEvent.getSelectionModel().getSelection())
					distributionPanel.addSpecialValue(t, new ShapeAttributes(colorEncodingFunction.apply(t), stroke));
				if (distributionPanel instanceof Component)
					((Component) distributionPanel).repaint();
			}
		};
		sm.addSelectionListener(selectionListener);
		return selectionListener;
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
	public static <T> List<T> sanityCheckFilter(Collection<T> data, Function<? super T, Double> worldPositionMapping,
			boolean warnForQualityLeaks) {
		return VisualMappings.sanityCheckFilter(data, worldPositionMapping, warnForQualityLeaks);
	}
}
