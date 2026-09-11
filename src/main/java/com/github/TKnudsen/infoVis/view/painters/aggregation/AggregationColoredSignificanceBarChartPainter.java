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
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * A {@link BarChartSelectablePainter} with an extra strip of dot-like bars
 * (colored independently via {@link #setSignificanceDotsColors(List)}) drawn
 * alongside the main bars -- e.g. to encode a per-bar significance value
 * distinct from the bar's own selection state.
 *
 * @param <O> the element type held in each bucket
 * @since 2013
 */
public class AggregationColoredSignificanceBarChartPainter<O> extends BarChartSelectablePainter<O> {

	// internal attributes
	protected BarsPainter<O> significanceDotsBarsRenderer;
	protected List<Color> significanceDotsColors;

	public AggregationColoredSignificanceBarChartPainter(List<List<O>> elementMapping, List<String> labeling, String headline) {
		super(elementMapping, labeling, headline);

		// initialize significanceDotsBarsRenderer with a pseudo-bucket per bar, sized
		// the same as elementMapping (or labeling, if elementMapping is absent)
		List<List<O>> pseudoBars = new ArrayList<List<O>>();
		List<O> al = new ArrayList<O>();
		al.add((O) new Long(22));
		if (elementMapping != null)
			for (int i = 0; i < elementMapping.size(); i++)
				pseudoBars.add(al);
		else if (labeling != null)
			for (@SuppressWarnings("unused")
			String s : labeling)
				pseudoBars.add(al);
		significanceDotsBarsRenderer = new BarsPainter<O>(pseudoBars, null);
		significanceDotsBarsRenderer.setVerticalOrientation(isVerticalOrientation());
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

		double off = Math.max(1.66, getOffset());

		if (!isVerticalOrientation()) {
			if (isInvertLegendAlignment()) {
				Rectangle2D.Double barChartSelectableRendererRectangle = new Rectangle2D.Double(
						rectangle.getMinX() + rectangle.getWidth() * 0.21 + off, rectangle.getMinY(), rectangle.getWidth() * 0.79 - off,
						rectangle.getHeight());
				super.setRectangle(barChartSelectableRendererRectangle);

				Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.getMinX() + off, chartRectangle.getY(),
						rectangle.getWidth() * 0.20, chartRectangle.getHeight());
				if (significanceDotsBarsRenderer != null)
					significanceDotsBarsRenderer.setRectangle(rect);
			} else {
				Rectangle2D.Double barChartSelectableRendererRectangle = new Rectangle2D.Double(rectangle.getMinX(),
						rectangle.getMinY(), rectangle.getWidth() * 0.79 - off, rectangle.getHeight());
				super.setRectangle(barChartSelectableRendererRectangle);

				Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.getMinX() + rectangle.getWidth() * 0.80 + off,
						chartRectangle.getY(), rectangle.getWidth() * 0.20, chartRectangle.getHeight());
				if (significanceDotsBarsRenderer != null)
					significanceDotsBarsRenderer.setRectangle(rect);
			}
		} else {
			Rectangle2D.Double barChartSelectableRendererRectangle = new Rectangle2D.Double(rectangle.getMinX(), rectangle.getMinY(),
					rectangle.getWidth(), rectangle.getHeight() * 0.79 - off);
			super.setRectangle(barChartSelectableRendererRectangle);

			Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.getMinX(), rectangle.getMaxY() - rectangle.getHeight() * 0.20,
					rectangle.getWidth(), rectangle.getHeight() * 0.20);
			if (significanceDotsBarsRenderer != null)
				significanceDotsBarsRenderer.setRectangle(rect);
		}

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
	public void setColors(List<Color> colors) {
		this.getBarsPainter().setColors(colors);

		List<Color> cols = new ArrayList<Color>();
		for (Color c : colors)
			cols.add(c == null ? null : ColorTools.setAlpha(c, 1.0f));

		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setColors(cols);
	}

	@Override
	public void setBorderPaint(Paint borderColor) {
		super.setBorderPaint(borderColor);

		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setBorderPaint(borderColor);
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

	public List<Color> getSignificanceDotsColors() {
		return significanceDotsColors;
	}

	public void setSignificanceDotsColors(List<Color> significanceDotsColors) {
		this.significanceDotsColors = significanceDotsColors;

		if (significanceDotsBarsRenderer != null)
			significanceDotsBarsRenderer.setColors(significanceDotsColors);
	}
}
