package com.github.TKnudsen.infoVis.view.painters.aggregation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;

/**
 * A {@link BarChartSelectablePainter} with a narrow strip of "significance
 * dot" bars alongside the main bars, all sharing one color (set via
 * {@link #setColorsForSelection(List)}) -- e.g. to flag which bars are
 * statistically significant without a separate legend.
 *
 * @param <O> the element type held in each bucket
 * @since 2013
 */
public class AggregateSignificanceBarChartPainter<O> extends BarChartSelectablePainter<O> {

	// internal attributes
	private BarsPainter<O> significanceDotsBarsRenderer;

	public AggregateSignificanceBarChartPainter(List<List<O>> elementMapping, List<String> labels, String headline) {
		super(elementMapping, labels, headline);

		// initialize significanceDotsBarsRenderer with a pseudo-bucket per label
		List<List<O>> pseudoBars = new ArrayList<List<O>>();
		List<O> al = new ArrayList<O>();
		al.add((O) new Long(22));
		for (@SuppressWarnings("unused")
		String s : labels)
			pseudoBars.add(al);
		significanceDotsBarsRenderer = new BarsPainter<O>(pseudoBars, null);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.draw(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		Rectangle2D.Double barChartSelectableRendererRectangle = new Rectangle2D.Double(
				rectangle.getMinX() + rectangle.getWidth() * 0.07 + getOffset(), rectangle.getMinY(), rectangle.getWidth() * 0.93 - getOffset(),
				rectangle.getHeight());
		super.setRectangle(barChartSelectableRendererRectangle);

		Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.getMinX() + getOffset(), chartRectangle.getY(), rectangle.getWidth() * 0.06,
				chartRectangle.getHeight());
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setRectangle(rect);

		this.rectangle = rectangle;
	}

	@Override
	public void setOffset(double offset) {
		super.setOffset(offset);
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setOffset(offset);
	}

	@Override
	public void setColor(Color color) {
		super.setColor(color);
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setColor(color);
	}

	@Override
	public void setBackgroundPaint(Paint backgroundColor) {
		super.setBackgroundPaint(backgroundColor);

		// ChartPainter's own constructor calls setBackgroundPaint(null) before this
		// subclass's field initializers run, so significanceDotsBarsRenderer can
		// still be null the first time this is invoked
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setBackgroundPaint(backgroundColor);
	}

	@Override
	public void setFill(boolean fill) {
		super.setFill(fill);
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setFill(fill);
	}

	@Override
	public void setVerticalOrientation(boolean verticalOrientation) {
		super.setVerticalOrientation(verticalOrientation);
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setVerticalOrientation(verticalOrientation);
	}

	@Override
	public void setColorsForSelection(List<Color> colorsForSelection) {
		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setColors(colorsForSelection);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!enableToolTipping)
			return null;

		if (getBarsPainter() != null && getBarsPainter().getRectangle().contains(p))
			return getBarsPainter().getTooltip(p);

		if (significanceDotsBarsRenderer != null && significanceDotsBarsRenderer.getRectangle().contains(p)
				&& significanceDotsBarsRenderer.getBars() != null)
			for (int i = 0; i < significanceDotsBarsRenderer.getBars().size(); i++)
				if (significanceDotsBarsRenderer.getBars().get(i) != null && significanceDotsBarsRenderer.getBars().get(i).contains(p)) {
					StringPainter sr = new StringPainter(getBarsPainter().getLabeling().get(i));
					sr.setRectangle(new Rectangle2D.Double(p.getX() - ((String) sr.getData()).length() * 5, p.getY() - 40,
							((String) sr.getData()).length() * 10, 40));
					return sr;
				}

		return null;
	}
}
