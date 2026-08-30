package com.github.TKnudsen.infoVis.view.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.interaction.IToolTipPaintable;
import com.github.TKnudsen.infoVis.view.interaction.handlers.TooltipHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ComponentTools;

/**
 * <p>
 * Basic chart functionality to maintain, layout, and draw painters.
 * </p>
 *
 * @version 2.07
 * @since 2016
 */
public class InfoVisChartPanel extends JPanel implements IToolTipPaintable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8297484424829585871L;

	/**
	 * the space that is allocated for the border depending on the size of the
	 * component
	 */
	private static double borderSpaceRatio = 0.01;

	private final ChartRectangleLayout chartRectangleLayout;

	private boolean autoMargin = true;
	private double margin = Double.NaN;

	private final List<ChartPainter> chartPainters = new CopyOnWriteArrayList<>();

	/**
	 * contours color that will be used to draw the outline (if != null)
	 */
	private Color outlineColor = null;

	/**
	 * whether or not the panel shall have quadratic bounds
	 */
	private boolean quadraticBounds = false;

	/**
	 * enable tool tipping support
	 */
	private boolean showingTooltips;

	private TooltipHandler tooltipHandler;

	/**
	 * tool tip
	 */
	private ChartPainter toolTipPainter = null;

	public InfoVisChartPanel() {
		this.chartRectangleLayout = createChartRectangleLayout();

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateBounds();
			}
		});

		// updateBounds();

		setShowingTooltips(true);
	}

	public InfoVisChartPanel(ChartPainter chartPainter) {
		this();

		addChartPainter(chartPainter);
	}

	/**
	 * Ensures that the layout or its inherited class matches the panel layout
	 * requirements.
	 * 
	 * @return
	 */
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new ChartRectangleLayout();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color color = g2.getColor();

		Rectangle2D rect = ComponentTools.getDrawableRectangle(this);

		drawChart(g2);

		if (rect != null && outlineColor != null) {
			g2.setColor(outlineColor);
			g2.draw(rect);
		}

		if (toolTipPainter != null && toolTipPainter.getRectangle() != null && isShowingTooltips())
			toolTipPainter.draw(g2);

		g2.setColor(color);
	}

	protected void drawChart(Graphics2D g2) {
		for (ChartPainter chartPainter : chartPainters) {
			chartPainter.draw(g2);
		}
	}

	@Override
	public void setToolTipPainter(ChartPainter toolTipPainter) {
		this.toolTipPainter = toolTipPainter;
		repaint();
	}

	/**
	 * Updates the chart bounds, margins, and layout when the component is resized.
	 * 
	 * <p>
	 * Recalculates the drawable area accounting for insets, applies appropriate
	 * margins, and updates all chart painters with the new bounds.
	 * </p>
	 */
	protected final void updateBounds() {
		// Component readiness check
		if (!isDisplayable() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		Rectangle2D rectangle = ComponentTools.getDrawableRectangle(this);
		if (rectangle == null)
			return;

		// Calculate margin based on settings
		double calculatedMargin = calculateMargin(rectangle);

		// Apply updates
		chartRectangleLayout.setMargin(calculatedMargin);
		chartRectangleLayout.setRectangle(rectangle);
		updatePainterRectangles();

		repaint();
		revalidate();
	}

	/**
	 * Calculates the margin for the chart based on auto-margin settings.
	 * 
	 * @param rectangle the drawable rectangle
	 * @return the calculated margin in pixels
	 */
	private double calculateMargin(Rectangle2D rectangle) {
		double minDimension = Math.min(rectangle.getWidth(), rectangle.getHeight());

		if (autoMargin)
			return Math.max(1.0, Math.min(3.0, minDimension * borderSpaceRatio));

		if (!Double.isNaN(margin))
			return Math.min(minDimension * 0.5, margin);

		return 0.0;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (quadraticBounds) {
			int min = Math.min(width, height);
			super.setBounds(x, y, min, min);
		} else
			super.setBounds(x, y, width, height);
	}

	/**
	 * Uses the rectangle information provided with the layout and assigns it to the
	 * painters
	 */
	protected void updatePainterRectangles() {
		Rectangle2D chartRectangle = chartRectangleLayout.getChartRectangle();

		for (ChartPainter chartPainter : chartPainters) {
			chartPainter.setRectangle(chartRectangle);
		}
	}

	public BufferedImage getBufferedImage() {
		int w = this.getWidth();
		int h = this.getHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);

		Graphics2D g = bi.createGraphics();
		this.paint(g);

		return bi;
	}

	public void addChartPainter(ChartPainter chartPainter) {
		Objects.requireNonNull(chartPainter, "InfoVisChartPanel.addChartPainter: ChartPainter may not be null");

		addChartPainter(getChartPainters().size(), chartPainter);
	}

	/**
	 * Inserts the specified ChartPainter at the specified position in this list
	 * (optional operation). Shifts the element currently at that position (if any)
	 * and any subsequent elements to the right (adds one to their indices).
	 * 
	 * Helps to manage layers of chart painters.
	 * 
	 * Works like the add (int , E) method of the List interface.
	 * 
	 * @param index        where the chart painter is added
	 * @param chartPainter the chart painter
	 */
	public void addChartPainter(int index, ChartPainter chartPainter) {
		Objects.requireNonNull(chartPainter, "InfoVisChartPanel.addChartPainter: ChartPainter may not be null");

		// because it is not guaranteed that updateBounds will be triggered implicitly
		chartPainter.setRectangle(chartRectangleLayout.getChartRectangle());

		// Apply panel font once
		if (getFont() != null)
			chartPainter.setFont(getFont());

		this.chartPainters.add(index, chartPainter);

		if (isDisplayable())
			updateBounds();
	}

	public void removeChartPainters() {
		this.chartPainters.clear();
	}

	public boolean removeChartPainter(ChartPainter chartPainter) {
		Objects.requireNonNull(chartPainter, "The ChartPainter may not be null");

		return this.chartPainters.remove(chartPainter);
	}

	/**
	 * @deprecated This color is ill-defined. It may be expected to be from the
	 *             panel, or the primary chart painter. Avoid usage, find a better
	 *             implementation solution.
	 * @return
	 */
	public Color getBackgroundColor() {
		return this.getBackground();
	}

	@Override
	/**
	 * Sets the background color of this panel and manages chart painter
	 * backgrounds.
	 * <p>
	 * <b>Non-null color:</b> Sets a unified panel background and clears all chart
	 * painter backgrounds (making them transparent).
	 * <p>
	 * <b>Null:</b> Clears the panel background and preserves individual chart
	 * painter backgrounds.
	 * <p>
	 * <b>Note:</b> Painter backgrounds cleared by a non-null color are not restored
	 * when switching back to null. Manage externally if restoration is needed.
	 *
	 * @param backgroundColor the background color for the panel, or null to allow
	 *                        individual chart painter backgrounds to be visible
	 */
	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (chartPainters != null && backgroundColor != null) {
			for (ChartPainter chartPainter : chartPainters) {
				chartPainter.setBackgroundPaint(null);
			}
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (chartPainters != null && fg != null)
			for (ChartPainter chartPainter : chartPainters)
				chartPainter.setFontColor(fg);
	}

	public boolean isShowingTooltips() {
		return showingTooltips;
	}

	public void setShowingTooltips(boolean showingTooltips) {

		if (tooltipHandler == null) {
			tooltipHandler = new TooltipHandler();
		}
		if (showingTooltips) {
			tooltipHandler.attachTo(this);
		} else {
			tooltipHandler.attachTo(null);
		}

		this.showingTooltips = showingTooltips;
	}

	public boolean isAutoMargin() {
		return autoMargin;
	}

	public void setAutoMargin(boolean autoMargin) {
		this.autoMargin = autoMargin;
	}

	public ChartRectangleLayout getChartRectangleLayout() {
		return chartRectangleLayout;
	}

	public Color getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(Color outlineColor) {
		this.outlineColor = outlineColor;
	}

	public List<ChartPainter> getChartPainters() {
		return Collections.unmodifiableList(chartPainters);
	}

	@SuppressWarnings("unchecked")
	public <P extends ChartPainter> List<P> getChartPainters(Class<P> painterClass) {
		List<P> painters = new ArrayList<>();
		for (ChartPainter p : chartPainters)
			if (p.getClass().equals(painterClass))
				painters.add((P) p);
		return painters;
	}

	public boolean isQuadraticBounds() {
		return quadraticBounds;
	}

	public void setQuadraticBounds(boolean quadraticBounds) {
		this.quadraticBounds = quadraticBounds;
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
		this.setAutoMargin(false);

		updateBounds();
	}

}