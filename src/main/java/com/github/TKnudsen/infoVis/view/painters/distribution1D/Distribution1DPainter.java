package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints the distribution of numerical values.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public abstract class Distribution1DPainter extends ChartPainter
		implements IColorEncoding<Double>, IRectangleSelection<Double>, ITooltip {

	protected Collection<Double> values;

	protected List<Entry<Double, ShapeAttributes>> specialValues = new ArrayList<>();

	private boolean dynamicAlpha = true;
	protected float alpha = 1.0f;
	private boolean toolTipping = true;

	private IPositionEncodingFunction positionEncodingFunction;
	protected boolean externalPositionEncodingFunction = false;

	private Function<? super Double, ? extends Paint> colorEncodingFunction;

	public Distribution1DPainter(Collection<Double> values) {
		if (values != null)
			this.values = Collections.unmodifiableCollection(values);
		else
			this.values = new ArrayList<>();

		StatisticsSupport statistics = new StatisticsSupport(values);
		positionEncodingFunction = new PositionEncodingFunction(statistics.getMin(), statistics.getMax(), 0.0, 1.0);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		// adjust alpha
		if (dynamicAlpha)
			if (chartRectangle != null)
				alpha = (float) Math.min(1.0f,
						Math.max(0.01f, chartRectangle.getWidth() / values.size() / stroke.getLineWidth() * 0.5f));

		if (!externalPositionEncodingFunction)
			updatePositionEncoding(rectangle);
	}

	protected abstract void updatePositionEncoding(Rectangle2D rectangle);

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		g2.setStroke(stroke);

		if (colorEncodingFunction == null)
			this.colorEncodingFunction = new ConstantColorEncodingFunction<>(Color.BLACK);

		for (Double v : values)
			drawValue(g2, v, ColorTools.setAlpha(colorEncodingFunction.apply(v), alpha));

		// draw special values
		for (Entry<Double, ShapeAttributes> entry : specialValues) {
			Double worldValue = entry.getKey();
			ShapeAttributes shapeAttributes = entry.getValue();
			if (shapeAttributes != null) {
				g2.setStroke(shapeAttributes.getStroke());
				drawValue(g2, worldValue, ColorTools.setAlpha(shapeAttributes.getColor(), alpha));
			}
		}

		g2.setStroke(s);
		g2.setColor(c);
	}

	public abstract void drawValue(Graphics2D g2, double worldValue, Paint color);

	public boolean isDynamicAlpha() {
		return dynamicAlpha;
	}

	public void setDynamicAlpha(boolean dynamicAlpha) {
		this.dynamicAlpha = dynamicAlpha;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	public void addSpecialValue(Double worldValue, ShapeAttributes shapeAttributes) {
		this.specialValues.add(new EntryWithComparableKey<Double, ShapeAttributes>(worldValue, shapeAttributes));
	}

	public void clearSpecialValues() {
		this.specialValues.clear();
	}

	public Function<? super Double, ? extends Paint> getColorEncodingFunction() {
		return colorEncodingFunction;
	}

	@Override
	public void setColorEncodingFunction(Function<? super Double, ? extends Paint> colorEncodingFunction) {
		this.colorEncodingFunction = colorEncodingFunction;
	}

	public IPositionEncodingFunction getPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction = positionEncodingFunction;
		this.externalPositionEncodingFunction = true;
	}

}
