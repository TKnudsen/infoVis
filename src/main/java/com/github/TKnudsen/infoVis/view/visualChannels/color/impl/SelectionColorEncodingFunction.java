package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Paint;
import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Provides a constant color for all entities, selected entities are highlighted
 * instead.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class SelectionColorEncodingFunction<T> extends ConstantColorEncodingFunction<T> {

	private Paint highlightPaint;
	private final Function<? super T, Boolean> selectedFunction;

	public SelectionColorEncodingFunction(Function<? super T, Boolean> selectedFunction, Paint defaultPaint,
			Paint highlightPaint) {
		super(defaultPaint);

		this.highlightPaint = highlightPaint;
		this.selectedFunction = selectedFunction;
	}

	@Override
	public String getName() {
		return "SelectionColorEncodingFunction";
	}

	@Override
	public String getDescription() {
		return "provides a constant color for all entities, whereas selected entities are highlighted";
	}

	@Override
	public Paint apply(T t) {
		if (!selectedFunction.apply(t))
			return super.apply(t);
		return highlightPaint;
	}

	public Paint getHighlightPaint() {
		return highlightPaint;
	}

	public void setHighlightPaint(Paint highlightPaint) {
		this.highlightPaint = highlightPaint;
	}

}
