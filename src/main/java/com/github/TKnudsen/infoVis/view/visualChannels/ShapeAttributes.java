package com.github.TKnudsen.infoVis.view.visualChannels;

import java.awt.Paint;
import java.awt.Stroke;

/**
 * <p>
 * Data structure storing encoding information (paint and stroke). can be used
 * to encode individual objects.
 * 
 * In general, one may prefer visual mapping functions that maintain encodings
 * for every instance.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public class ShapeAttributes {

	private Paint color;
	private Stroke stroke;

	public ShapeAttributes(Paint color, Stroke stroke) {
		this.color = color;
		this.stroke = stroke;
	}

	@Override
	public String toString() {
		return "Color: " + getColor() + ", Stroke: " + getStroke();
	}

	public Paint getColor() {
		return color;
	}

	public void setColor(Paint color) {
		this.color = color;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}
}
