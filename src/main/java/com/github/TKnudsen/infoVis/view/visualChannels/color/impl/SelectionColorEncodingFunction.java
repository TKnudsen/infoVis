package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Paint;
import java.util.function.Function;

/**
 * <p>
 * Provides a constant color for all entities, selected entities are highlighted
 * instead.
 * </p>
 *
 * @version 1.01
 * @since 2017
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
