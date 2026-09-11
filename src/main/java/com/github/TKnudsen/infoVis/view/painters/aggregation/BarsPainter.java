package com.github.TKnudsen.infoVis.view.painters.aggregation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Draws one bar per element bucket, with each bar's length proportional to its
 * bucket's element count (not to a numeric value) -- unlike
 * {@link com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter},
 * which draws one bar per number. Used where the underlying data is a grouping
 * of elements (e.g. bins of an aggregation) rather than a plain numeric series.
 *
 * @param <O> the element type held in each bin
 * @since 2013
 */
public class BarsPainter<O> extends ChartPainter implements ITooltip {

	// external data
	protected List<List<O>> elementMapping = null;
	protected List<String> labeling = null;
	private List<Color> colors = null;

	// calculated attributes
	private volatile List<Rectangle2D> bars = Collections.emptyList();
	protected int count = 0;
	private boolean drawable = false;

	// state attributes
	protected boolean fill = true;
	protected boolean verticalOrientation = true;
	protected double offset = 0.0;
	private boolean alignInverseBorder = false;
	private boolean invertLegendAlignment = false;
	private boolean enableToolTipping = true;

	public BarsPainter(List<List<O>> elementMapping, List<String> labeling) {
		if (elementMapping == null) {
			if (labeling != null) {
				this.elementMapping = new ArrayList<List<O>>();
				for (int i = 0; i < labeling.size(); i++)
					this.elementMapping.add(new ArrayList<O>((Collection<? extends O>) Arrays.asList(1L)));
			}
		} else
			this.elementMapping = elementMapping;

		if (this.elementMapping != null)
			this.count = this.elementMapping.size();

		this.labeling = labeling;

		this.colors = new ArrayList<>();
		if (this.elementMapping != null) {
			for (int i = 0; i < this.elementMapping.size(); i++) {
				this.colors.add(color);
			}
		}
	}

	public BarsPainter(List<List<O>> elementMapping, List<String> labeling, List<Color> colors) {
		if (elementMapping == null) {
			if (labeling != null) {
				this.elementMapping = new ArrayList<List<O>>();
				for (int i = 0; i < labeling.size(); i++)
					this.elementMapping.add(new ArrayList<O>((Collection<? extends O>) Arrays.asList(1L)));
			} else if (colors != null) {
				this.elementMapping = new ArrayList<List<O>>();
				for (int i = 0; i < colors.size(); i++)
					this.elementMapping.add(new ArrayList<O>((Collection<? extends O>) Arrays.asList(1L)));
			}
		} else
			this.elementMapping = elementMapping;

		if (this.elementMapping != null)
			this.count = this.elementMapping.size();

		this.labeling = labeling;
		this.colors = colors;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		// local snapshot of bars and colors
		final List<Rectangle2D> bars = this.bars;
		final List<Color> colors = this.colors;

		if (!drawable || bars == null || bars.isEmpty())
			return;

		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		if (colors != null && bars != null && bars.size() > 0)
			for (int i = 0; i < bars.size(); i++) {
				if (fill && colors.size() > i && colors.get(i) != null && bars.size() > i && bars.get(i) != null) {
					g2.setColor(colors.get(i));
					if (bars.get(i) != null)
						g2.fill(bars.get(i));
				}

				if (isDrawOutline() && bars.size() > i && bars.get(i) != null && colors != null) {
					if (colors.size() > i && colors.get(i) == null
							&& (bars.get(i).getWidth() < 2.0 || bars.get(i).getHeight() < 2.0)) {
						g2.setStroke(DisplayTools.standardStroke);
						g2.setPaint(getBorderPaint());
						g2.fill(bars.get(i));
						g2.setStroke(s);
					} else {
						g2.setPaint(getBorderPaint());
						g2.draw(bars.get(i));
					}
				}
			}

		g2.setStroke(s);
		g2.setColor(c);
	}

	public void drawLegend(Graphics2D g2) {
		if (labeling == null)
			return;

		Color c = g2.getColor();

		FontMetrics fm = g2.getFontMetrics();
		g2.setColor(fontColor);

		for (int i = 0; i < bars.size(); i++) {
			String s = labeling.get(i);
			while (fm.stringWidth(s) * 1.1 + 5 > this.rectangle.getWidth() && s.length() > 0)
				s = s.substring(0, s.length() - 1);
			float startX = (float) (bars.get(i).getX() + 5.0f);
			if (invertLegendAlignment)
				startX = (float) (this.rectangle.getMaxX() - fm.stringWidth(s) - offset);
			g2.drawString(s, startX,
					(float) (bars.get(i).getY() + fm.getHeight() * 0.4 + bars.get(i).getHeight() * 0.5));
		}

		g2.setColor(c);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		recalculate();
	}

	private void recalculate() {
		// Snapshot references to avoid races if external code swaps
		// elementMapping/labeling
		final Rectangle2D r = this.rectangle;
		final List<List<O>> em = this.elementMapping;
		final int c = (em != null) ? em.size() : 0;

		if (r == null || em == null || c <= 0 || r.getWidth() <= 0 || r.getHeight() <= 0) {
			this.bars = Collections.emptyList();
			drawable = false;
			return;
		}

		final double width = r.getWidth();
		final double height = r.getHeight();
		if (width <= 0 || height <= 0) {
			this.bars = Collections.emptyList();
			drawable = false;
			return;
		}

		final List<Rectangle2D> newBars = new ArrayList<>(c);

		if (verticalOrientation) {
			final double spaceForEachBar = ((width - 2 * offset) / (double) c);
			final double distBetweenBars = (spaceForEachBar < 2) ? 0
					: Math.min(5.0, Math.max(1.00, spaceForEachBar / 10));

			final double barWidth = ((width - 2 * offset) - (c - 1) * distBetweenBars) / (double) c;
			if (barWidth <= 0 || Double.isNaN(barWidth) || Double.isInfinite(barWidth)) {
				this.bars = Collections.emptyList();
				drawable = false;
				return;
			}

			double firstX = r.getX() + offset;

			double yAxisMaxValue = 0;
			for (int i = 0; i < c; i++) {
				List<O> bin = em.get(i);
				int size = (bin != null) ? bin.size() : 0;
				yAxisMaxValue = Math.max(yAxisMaxValue, size);
			}

			if (yAxisMaxValue <= 0) {
				// All empty -> draw "zero bars" safely
				for (int i = 0; i < c; i++) {
					newBars.add(new Rectangle2D.Double(firstX, r.getMaxY(), barWidth, 0));
					firstX += (barWidth + distBetweenBars);
				}
				this.bars = newBars;
				drawable = true;
				return;
			}

			for (int i = 0; i < c; i++) {
				List<O> bin = em.get(i);
				double v = (bin != null) ? bin.size() : 0;
				double h = height * (v / yAxisMaxValue);

				if (!alignInverseBorder) {
					newBars.add(new Rectangle2D.Double(firstX, r.getY() + height - h, barWidth, h));
				} else {
					newBars.add(new Rectangle2D.Double(firstX, r.getY(), barWidth, h));
				}
				firstX += (barWidth + distBetweenBars);
			}

		} else {
			// Horizontal
			final double spaceForEachBar = ((height - 2 * offset) / (double) c);
			final double distBetweenBars = (spaceForEachBar < 2) ? 0
					: Math.min(5.0, Math.max(1.00, spaceForEachBar / 10));

			final double barHeight = ((height - 2 * offset) - (c - 1) * distBetweenBars) / (double) c;
			if (barHeight <= 0 || Double.isNaN(barHeight) || Double.isInfinite(barHeight)) {
				this.bars = Collections.emptyList();
				drawable = false;
				return;
			}

			double firstY = r.getY() + offset;

			double xAxisMaxValue = 0;
			for (int i = 0; i < c; i++) {
				List<O> bin = em.get(i);
				int size = (bin != null) ? bin.size() : 0;
				xAxisMaxValue = Math.max(xAxisMaxValue, size);
			}

			if (xAxisMaxValue <= 0) {
				for (int i = 0; i < c; i++) {
					newBars.add(new Rectangle2D.Double(r.getX(), firstY, 0, barHeight));
					firstY += (barHeight + distBetweenBars);
				}
				this.bars = newBars;
				drawable = true;
				return;
			}

			for (int i = 0; i < c; i++) {
				List<O> bin = em.get(i);
				double v = (bin != null) ? bin.size() : 0;
				double w = width * (v / xAxisMaxValue);

				if (!alignInverseBorder) {
					newBars.add(new Rectangle2D.Double(r.getX(), firstY, Math.max(0, w - 1), barHeight));
				} else {
					newBars.add(new Rectangle2D.Double(r.getX() + (r.getWidth() - w + 1), firstY, Math.max(0, w - 1),
							barHeight));
				}
				firstY += (barHeight + distBetweenBars);
			}
		}

		// Atomic publish of a fully built list
		this.bars = newBars;
		drawable = true;
	}

	public List<O> getClickedEntities(MouseEvent e) {
		for (int i = 0; i < bars.size(); i++)
			if (bars.get(i).contains(e.getPoint())) {
				return elementMapping.get(i);
			}
		return null;
	}

	@Override
	public void setColor(Color color) {
		this.setPaint(color);

		colors = new ArrayList<Color>();
		for (int i = 0; i < elementMapping.size(); i++)
			colors.add(color);
	}

	public List<List<O>> getData() {
		return getElementMapping();
	}

	public int getCount() {
		return count;
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public List<List<O>> getElementMapping() {
		return elementMapping;
	}

	public List<String> getLabeling() {
		return labeling;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public boolean isVerticalOrientation() {
		return verticalOrientation;
	}

	public void setVerticalOrientation(boolean verticalOrientation) {
		this.verticalOrientation = verticalOrientation;
	}

	public boolean isAlignInverseBorder() {
		return alignInverseBorder;
	}

	public void setAlignInverseBorder(boolean alignInverseBorder) {
		this.alignInverseBorder = alignInverseBorder;
	}

	public boolean isInvertLegendAlignment() {
		return invertLegendAlignment;
	}

	public void setInvertLegendAlignment(boolean invertLegendAlignment) {
		this.invertLegendAlignment = invertLegendAlignment;
	}

	public List<Color> getColors() {
		return colors;
	}

	public void setColors(List<Color> colors) {
		this.colors = colors;
	}

	public List<Rectangle2D> getBars() {
		return Collections.unmodifiableList(bars);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (enableToolTipping) {

			// to avoid modification issues
			List<Rectangle2D> bars = this.bars;

			if (bars != null)
				for (int i = 0; i < bars.size(); i++)
					if (bars.get(i) != null && bars.get(i).contains(p)) {
						StringPainter sr = new StringPainter(labeling.get(i));
						sr.setRectangle(new Rectangle2D.Double(p.getX() - ((String) sr.getData()).length() * 5,
								p.getY() - 40, ((String) sr.getData()).length() * 10, 40));
						return sr;
					}
		}

		return null;
	}

	public int getBarIndexAt(Point point) {
		if (bars != null) {

			// to avoid modification issues
			List<Rectangle2D> bars = this.bars;

			for (int i = 0; i < bars.size(); i++)
				if (bars.get(i) != null && bars.get(i).contains(point)) {
					return i;
				}
		}

		return -1;
	}

	@Override
	public boolean isToolTipping() {
		return enableToolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.enableToolTipping = enableToolTipping;
	}

	public boolean isDrawable() {
		return drawable;
	}
}
