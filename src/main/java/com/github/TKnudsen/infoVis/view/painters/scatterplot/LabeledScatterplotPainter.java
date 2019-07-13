package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 *
 */
public class LabeledScatterplotPainter extends ScatterPlotPainter<Double[]> {

	private final List<String> labels;

	List<StringPainter> stringPainters;

	public LabeledScatterplotPainter(List<Double[]> data, List<String> labels) {
		this(data, null, p -> p[0], p -> p[1], labels);
	}

	public LabeledScatterplotPainter(List<Double[]> data, List<Color> colors, List<String> labels) {
		this(data, new ColorEncodingFunction<>(data, colors), p -> p[0], p -> p[1], labels);
	}

	public LabeledScatterplotPainter(List<Double[]> data, Function<? super Double[], ? extends Paint> colorMapping,
			List<String> labels) {
		this(data, colorMapping, p -> p[0], p -> p[1], labels);
	}

	public LabeledScatterplotPainter(List<Double[]> data, Function<? super Double[], ? extends Paint> colorMapping,
			Function<? super Double[], Double> worldPositionMappingX,
			Function<? super Double[], Double> worldPositionMappingY, List<String> labels) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);

		this.labels = labels;
		initializeLabels();
	}

	private void initializeLabels() {
		stringPainters = new ArrayList<>();
		for (int i = 0; i < labels.size(); i++) {
			stringPainters.add(new StringPainter(labels.get(i)));
			stringPainters.get(i).setFontColor(fontColor);
			stringPainters.get(i).setBackgroundPaint(null);
			stringPainters.get(i).setDrawOutline(false);
			stringPainters.get(i).setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		for (int i = 0; i < stringPainters.size(); i++) {
			Point2D point = screenPoints.get(i);

			double w = 14 + stringPainters.get(i).getData().length() * getFontSize();
			stringPainters.get(i)
					.setRectangle(new Rectangle2D.Double(point.getX() + 6, point.getY(), w, getFontSize()));
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		for (int i = 0; i < stringPainters.size(); i++)
			stringPainters.get(i).draw(g2);
	}
}
