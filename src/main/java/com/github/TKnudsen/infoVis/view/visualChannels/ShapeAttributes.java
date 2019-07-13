package com.github.TKnudsen.infoVis.view.visualChannels;

import java.awt.Paint;
import java.awt.Stroke;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Data structure storing encoding information (paint and stroke). can be used
 * to encode individual objects.
 * 
 * In general, one may prefer visual mapping functions that maintain encodings
 * for every instance.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
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
		return "Color: " + color + ", Stroke: " + stroke;
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
