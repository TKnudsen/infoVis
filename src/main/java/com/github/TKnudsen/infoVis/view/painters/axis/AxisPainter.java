package com.github.TKnudsen.infoVis.view.painters.axis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.lang.reflect.Field;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Base class for axis painters. Adds tooltip support on top of
 * {@link ChartPainter}: {@link #getTooltip(Point)} reflectively scans this
 * painter's own fields for embedded {@link ITooltip}-typed sub-painters and
 * forwards to the first one that returns a tooltip for the given point.
 *
 * @version 2.02
 * @since 2016
 */
public abstract class AxisPainter extends ChartPainter implements ITooltip {

	private boolean toolTipping = true;

	public AxisPainter() {
		this.setBackgroundPaint(null);
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.toolTipping = enableToolTipping;
	}

	@Override
	public ChartPainter getTooltip(Point p) {

		for (Field field : this.getClass().getDeclaredFields()) {
			Class<?>[] interfaces = field.getType().getInterfaces();
			for (Class<?> interface_ : interfaces)
				if (interface_.equals(ITooltip.class)) {
					try {
						field.setAccessible(true);
						ITooltip tooltipProvider = (ITooltip) field.get(this);
						ChartPainter abstractPainter = tooltipProvider.getTooltip(p);
						if (abstractPainter != null)
							return abstractPainter;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
		}

		return null;
	}

	@Override
	public void setBackgroundPaint(Paint backgroundColor) {
		super.setBackgroundPaint(backgroundColor);

		// TODO validate that this reflection-based approach is really not needed

//		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
//				true, true);
//		for (ChartPainter painter : painters)
//			if (painter != null && painter != this)
//				painter.setBackgroundPaint(null);
	}

	@Override
	public void setFontColor(Color fontColor) {
		super.setFontColor(fontColor);

		// TODO validate that this reflection-based approach is really not needed

//		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
//				true, true);
//		for (ChartPainter painter : painters)
//			if (painter != null && painter != this)
//				painter.setFontColor(fontColor);
	}

	@Override
	public void setFontSize(int fontSize) {
		super.setFontSize(fontSize);

		// TODO validate that this reflection-based approach is really not needed
//
//		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
//				true, true);
//		for (ChartPainter painter : painters)
//			if (painter != null && painter != this)
//				painter.setFontSize(fontSize);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);

		// TODO validate that this reflection-based approach is really not needed
//
//		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
//				true, true);
//		for (ChartPainter painter : painters)
//			if (painter != null)
//				painter.setFont(font);
	}
}
