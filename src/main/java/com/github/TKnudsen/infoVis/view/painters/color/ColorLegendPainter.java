package com.github.TKnudsen.infoVis.view.painters.color;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.aggregation.BarsPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;

/**
 * A static color legend: one color swatch per entry (drawn via
 * {@link BarsPainter}, used purely for its coloring rather than any bar-
 * height variation), each paired with a text label to its side.
 *
 * @since 2013
 */
public class ColorLegendPainter extends ChartPainter {

	// constructor attributes
	private Color[] colors;
	private String[] labels;

	// internal attributes
	private double offset;
	private Rectangle2D.Double barsRendererRectangle;
	private BarsPainter<Long> barsRenderer;
	private StringPainter[] stringRenderers;

	public ColorLegendPainter(Color[] colors, String[] labels) {
		this.colors = colors;
		this.labels = labels;

		List<Color> co = DataConversion.arrayToList(colors);
		barsRenderer = new BarsPainter<Long>(null, null, co);
		barsRenderer.setVerticalOrientation(false);
		barsRenderer.setBackgroundPaint(null);
		barsRenderer.setOffset(2);

		if (labels != null) {
			this.stringRenderers = new StringPainter[labels.length];
			for (int i = 0; i < labels.length; i++) {
				stringRenderers[i] = new StringPainter(labels[i]);
				stringRenderers[i].setBackgroundPaint(null);
				stringRenderers[i].setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
			}
		}

		setBackgroundPaint(null);
	}

	public ColorLegendPainter(List<Color> colors, List<String> labels) {
		this.colors = DataConversion.listToArray(colors, Color.class);
		this.labels = DataConversion.listToArray(labels, String.class);

		barsRenderer = new BarsPainter<Long>(null, null, colors);
		barsRenderer.setVerticalOrientation(false);
		barsRenderer.setBackgroundPaint(null);
		barsRenderer.setOffset(2);

		if (labels != null) {
			this.stringRenderers = new StringPainter[this.labels.length];
			for (int i = 0; i < this.labels.length; i++) {
				stringRenderers[i] = new StringPainter(this.labels[i]);
				stringRenderers[i].setBackgroundPaint(null);
			}
		}

		setBackgroundPaint(null);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (barsRenderer != null)
			barsRenderer.draw(g2);

		for (StringPainter stringRenderer : stringRenderers)
			stringRenderer.draw(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		offset = Math.min(rectangle.getHeight(), rectangle.getWidth()) * 0.025;
		double rest = (rectangle.getHeight() - 2 * offset - (colors.length - 1) * offset) / (double) colors.length;

		barsRendererRectangle = new Rectangle2D.Double(offset, rectangle.getY() + offset, rest, rectangle.getHeight() - 2 * offset);
		barsRenderer.setRectangle(barsRendererRectangle);

		int index = 0;
		for (Rectangle2D rect : barsRenderer.getBars()) {
			Rectangle2D r = new Rectangle2D.Double(rect.getMaxX() + offset, rect.getMinY(), rectangle.getWidth() - 3 * offset - rect.getWidth(),
					rect.getHeight());
			stringRenderers[index].setRectangle(r);
			stringRenderers[index].setDrawOutline(r.getHeight() > 26);
			index++;
		}
	}

	@Override
	public void setFont(Font font) {
		this.font = font;

		for (StringPainter stringRenderer : stringRenderers)
			if (stringRenderer != null)
				stringRenderer.setFont(font);
	}

	public Color[] getColors() {
		return colors;
	}

	public String[] getDescriptions() {
		return labels;
	}
}
