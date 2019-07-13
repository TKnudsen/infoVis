package com.github.TKnudsen.infoVis.view.painters.axis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.ReflectionTools;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Baseline functionality for all Axis Painters.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */
public abstract class AxisPainter extends ChartPainter implements ITooltip {

	private boolean toolTipping = true;

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

		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
				true, true);
		for (ChartPainter painter : painters)
			if (painter != null)
				painter.setBackgroundPaint(null);
	}

	@Override
	public void setFontColor(Color fontColor) {
		super.setFontColor(fontColor);

		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
				true, true);
		for (ChartPainter painter : painters)
			if (painter != null)
				painter.setFontColor(fontColor);
	}

	@Override
	public void setFontSize(int fontSize) {
		super.setFontSize(fontSize);

		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
				true, true);
		for (ChartPainter painter : painters)
			if (painter != null)
				painter.setFontSize(fontSize);

		// if (labelPainters != null)
		// for (StringPainter stringPainter : labelPainters)
		// if (stringPainter != null)
		// stringPainter.setFontSize(fontSize);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);

		List<ChartPainter> painters = ReflectionTools.getAllFieldsObjectsOfInstance(null, this, ChartPainter.class,
				true, true);
		for (ChartPainter painter : painters)
			if (painter != null)
				painter.setFont(font);

		// if (labelPainters != null)
		// for (StringPainter stringPainter : labelPainters)
		// if (stringPainter != null)
		// stringPainter.setFont(font);
	}
}
