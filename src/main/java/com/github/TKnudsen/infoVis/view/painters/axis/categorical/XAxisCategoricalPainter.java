package com.github.TKnudsen.infoVis.view.painters.axis.categorical;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;

/**
 * 
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints categorical labels along a X axis.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class XAxisCategoricalPainter<T extends List<String>> extends AxisCategoricalPainter<T> implements ITooltip {

	public XAxisCategoricalPainter(T markerLabels) {
		super(markerLabels);
	}

	public XAxisCategoricalPainter(T markerLabels, List<Color> labelColors) {
		super(markerLabels, labelColors);
	}

	@Override
	protected void refreshLabelPainters() {
		labelPainters = new ArrayList<>();

		if (rectangle == null)
			return;

		axisRectangle = new Rectangle2D.Double(rectangle.getMinX() + offsetWithinChartRectangle, rectangle.getMinY(),
				rectangle.getWidth() - (2 * offsetWithinChartRectangle), rectangle.getHeight());

		if (markerLabels != null)
			for (int i = 0; i < markerLabels.size(); i++) {
				String string = markerLabels.get(i);
				StringPainter painter = new StringPainter(string);
				painter.setHighlighted(false);
				painter.setBackgroundPaint(null);
				painter.setDrawOutline(false);
				painter.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);
				painter.setVerticalStringAlignment(VerticalStringAlignment.CENTER);
				painter.setFont(this.font);
				painter.setFontColor(fontColor);

				if (this.labelColors != null && labelColors.size() > i)
					painter.setFontColor(labelColors.get(i));

				labelPainters.add(painter);
			}

		setMarkerPositionsWithLabels(new ArrayList<>());
		double deltaX = axisRectangle.getWidth() / (double) labelPainters.size();
		for (int i = 0; i < labelPainters.size(); i++) {
			labelPainters.get(i).setRectangle(new Rectangle2D.Double(axisRectangle.getMinX() + i * deltaX,
					axisRectangle.getMinY(), deltaX, axisRectangle.getHeight()));
			markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(
					labelPainters.get(i).getRectangle().getCenterX(), markerLabels.get(i)));
		}

		for (StringPainter stringPainter : labelPainters) {
			stringPainter.setFontSize(getFontSize());
			stringPainter.setFontColor(fontColor);
		}
	}
}
