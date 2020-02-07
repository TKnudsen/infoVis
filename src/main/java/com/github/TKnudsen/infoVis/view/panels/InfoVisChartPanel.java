package com.github.TKnudsen.infoVis.view.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
 * InfoVis
 * </p>
 * 
 * <p>
 * Basic chart functionality to maintain, layout, and draw painters.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
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

	private boolean allowBorder = true;

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
	 * enable tooltipping support
	 */
	private boolean showingTooltips;

	private TooltipHandler tooltipHandler;

	/**
	 * tooltip
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

		updateBounds();

		setShowingTooltips(true);
	}

	public InfoVisChartPanel(ChartPainter chartPainter) {
		this();

		addChartPainter(chartPainter);
	}

	protected ChartRectangleLayout createChartRectangleLayout() {
		return new ChartRectangleLayout();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color color = g2.getColor();

		Rectangle2D rect = ComponentTools.getCompontentDrawableRectangle(this);

		// // background
		// if (rect != null) {
		// g2.setColor(backgroundColor);
		// g2.fill(rect);
		// }

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
			chartPainter.setFont(this.getFont());
			chartPainter.draw(g2);
		}
	}

	@Override
	public void setToolTipPainter(ChartPainter toolTipPainter) {
		this.toolTipPainter = toolTipPainter;
		repaint();
	}

	protected final void updateBounds() {
		Rectangle2D rectangle = ComponentTools.getCompontentDrawableRectangle(this);

		if (rectangle == null)
			return;

		double border = 0;
		if (allowBorder) {
			double min = Math.min(rectangle.getWidth(), rectangle.getHeight());
			border = Math.max(1, min * borderSpaceRatio);
		}

		chartRectangleLayout.setBorder(border);

		chartRectangleLayout.setRectangle(rectangle);

		updatePainterRectangles();

		repaint();
		revalidate();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (quadraticBounds) {
			int min = (width > height) ? height : width;
			super.setBounds(x, y, min, min);
		} else
			super.setBounds(x, y, width, height);
	}

	/**
	 * uses the rectangle information provided with the layout and assigns it to the
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
		Objects.requireNonNull(chartPainter, "The ChartPainter may not be null");

		// because it is not guaranteed that updateBounds will be triggered implicitly
		chartPainter.setRectangle(chartRectangleLayout.getChartRectangle());

		this.chartPainters.add(chartPainter);
	}

	public void removeChartPainters() {
		this.chartPainters.clear();
	}

	public boolean removeChartPainter(ChartPainter chartPainter) {
		Objects.requireNonNull(chartPainter, "The ChartPainter may not be null");

		return this.chartPainters.remove(chartPainter);
	}

	public Color getBackgroundColor() {
		return this.getBackground();
	}

	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (chartPainters != null)
			for (ChartPainter chartPainter : chartPainters) {
				chartPainter.setBackgroundPaint(null);
			}
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

	public boolean isAllowBorder() {
		return allowBorder;
	}

	public void setAllowBorder(boolean allowBorder) {
		this.allowBorder = allowBorder;
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

	public boolean isQuadraticBounds() {
		return quadraticBounds;
	}

	public void setQuadraticBounds(boolean quadraticBounds) {
		this.quadraticBounds = quadraticBounds;
	}

}