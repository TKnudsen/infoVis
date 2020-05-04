package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DoubleMappingTools;
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
public abstract class Distribution1DPainter<T> extends ChartPainter
		implements IColorEncoding<T>, IRectangleSelection<T>, ITooltip {

	protected Collection<T> data;

	protected List<Entry<T, ShapeAttributes>> specialValues = new ArrayList<>();

	private boolean dynamicAlpha = true;
	protected float alpha = 1.0f;
	protected float externalAlpha = Float.NaN;
	private boolean toolTipping = true;

	/**
	 * world coordinates/position/values of the x dimension
	 */
	private final Function<? super T, Double> worldToDoubleMapping;
	private IPositionEncodingFunction positionEncodingFunction;
	protected boolean externalPositionEncodingFunction = false;

	private Function<? super T, ? extends Paint> colorEncodingFunction;

	public Distribution1DPainter(Collection<T> values, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {
		this(values, worldToDoubleMapping);

		if (colorEncodingFunction != null)
			this.colorEncodingFunction = colorEncodingFunction;
	}

	public Distribution1DPainter(Collection<T> values, Function<? super T, Double> worldToDoubleMapping) {
		if (values != null)
			this.data = Collections
					.unmodifiableCollection(DoubleMappingTools.sanityCheckFilter(values, worldToDoubleMapping, true));
		else
			this.data = new ArrayList<>();
		if (data.isEmpty())
			throw new IllegalArgumentException(getClass().getSimpleName() + ": data must not be empty.");

		this.worldToDoubleMapping = worldToDoubleMapping;

		initializePositionEncodingFunction();

		this.colorEncodingFunction = new ConstantColorEncodingFunction<>(Color.BLACK);
	}

	protected final void initializePositionEncodingFunction() {
		List<Double> xValues = new ArrayList<>();

		for (T t : data)
			xValues.add(getWorldToDoubleMapping().apply(t).doubleValue());

		StatisticsSupport xStatistics = new StatisticsSupport(xValues);

		this.setPositionEncodingFunction(
				new PositionEncodingFunction(xStatistics.getMin(), xStatistics.getMax(), 0d, 1d));
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		// adjust alpha
		if (dynamicAlpha)
			if (chartRectangle != null)
				alpha = (float) Math.min(1.0f,
						Math.max(0.01f, chartRectangle.getWidth() / data.size() / stroke.getLineWidth() * 0.5f));

		if (!externalPositionEncodingFunction)
			updatePositionEncoding(rectangle);
	}

	protected abstract void updatePositionEncoding(Rectangle2D rectangle);

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		g2.setStroke(stroke);

		for (T t : data) {
			Paint color = getColorEncodingFunction().apply(t);
			color = ColorTools.setAlpha(color, alpha);
			drawValue(g2, t, color);
		}

		// draw special values
		for (Entry<T, ShapeAttributes> entry : specialValues) {
			T worldValue = entry.getKey();
			ShapeAttributes shapeAttributes = entry.getValue();
			if (shapeAttributes != null) {
				g2.setStroke(shapeAttributes.getStroke());
				Paint color = (shapeAttributes.getColor() != null) ? shapeAttributes.getColor()
						: getColorEncodingFunction().apply(worldValue);
				// color = ColorTools.setAlpha(color, alpha);//no dynamic alpha here
				drawValue(g2, worldValue, color);
			}
		}

		g2.setStroke(s);
		g2.setColor(c);
	}

	public final void drawValue(Graphics2D g2, T worldData, Paint paint) {
		Color c = g2.getColor();

		double worldX = getWorldToDoubleMapping().apply(worldData).doubleValue();

		double size = 0.0;
		if (g2.getStroke() != null) {
			Stroke stroke = g2.getStroke();
			if (stroke instanceof BasicStroke)
				size = Math.max(1.0, ((BasicStroke) stroke).getLineWidth() * 0.5 - 1);
		}

		if (!Double.isNaN(worldX)) {
			double screen = getPositionEncodingFunction().apply(worldX);

			if (paint == null)
				paint = ColorTools.setAlpha(getPaint(), alpha);
			g2.setPaint(paint);

			drawLine(g2, screen, size);
		}

		g2.setColor(c);
	}

	public abstract void drawLine(Graphics2D g2, Double positionValue, double capSize);

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
		this.dynamicAlpha = false;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	public void addSpecialValue(T worldValue, ShapeAttributes shapeAttributes) {
		this.specialValues.add(new AbstractMap.SimpleEntry<T, ShapeAttributes>(worldValue, shapeAttributes));
	}

	public void clearSpecialValues() {
		this.specialValues.clear();
	}

	public Function<? super T, ? extends Paint> getColorEncodingFunction() {
		return colorEncodingFunction;
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		Objects.requireNonNull(colorEncodingFunction);

		this.colorEncodingFunction = colorEncodingFunction;
	}

	public IPositionEncodingFunction getPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction = positionEncodingFunction;
		this.externalPositionEncodingFunction = true;
	}

	public Function<? super T, Double> getWorldToDoubleMapping() {
		return worldToDoubleMapping;
	}

}
