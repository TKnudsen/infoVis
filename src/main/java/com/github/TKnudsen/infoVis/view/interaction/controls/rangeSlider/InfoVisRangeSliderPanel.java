package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * A panel that combines an {@link InfoVisRangeSlider} and an underlying panel
 * with a {@code XAxisNumericalPainter}.
 * 
 * Note that the axis painter is aligned to the InfoVisRangeSlider without
 * knowing it. The challenge here is that both the range slider and the axis
 * painter are IXPositionEncoders not used to correspond to some external master
 * component regarding x-axis mapping.
 * </p>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class InfoVisRangeSliderPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Default height for the range slider component */
	private static final int DEFAULT_SLIDER_HEIGHT = 28;

	/** Default height for the X-axis chart panel */
	private static final int DEFAULT_AXIS_HEIGHT = 22;

	/** Factor for calculating margin width relative to slider pointer width */
	private static final double MARGIN_FACTOR = 0.5;

	private final InfoVisRangeSlider infoVisRangeSlider;
	private final InfoVisChartPanel xAxisChartPanel;

	private boolean showXAxis = true;

	// Cached margin panels to avoid recreation
	private JPanel marginEast;
	private JPanel marginWest;

	/**
	 * Creates a horizontal slider using the specified min and max values. The
	 * slider range initially spans the full min-max range.
	 *
	 * @param min      the minimum int value of the slider
	 * @param max      the maximum int value of the slider
	 * @param worldMin the min value of the world value domain which will be aligned
	 *                 with the min int value provided for the slider
	 * @param worldMax the max value of the world value domain which will be aligned
	 *                 with the max int value provided for the slider
	 * @throws IllegalArgumentException if worldMin or worldMax is null, or if min
	 *                                  >= max, or if worldMin >= worldMax
	 */
	public InfoVisRangeSliderPanel(int min, int max, Number worldMin, Number worldMax) {
		this(min, max, min, max, worldMin, worldMax);
	}

	/**
	 * Creates a horizontal slider using the specified min, max, low and high
	 * values.
	 *
	 * @param min      the minimum int value of the slider
	 * @param max      the maximum int value of the slider
	 * @param low      the initial low value of the slider
	 * @param high     the initial high value of the slider
	 * @param worldMin the min value of the world value domain which will be aligned
	 *                 with the min int value provided for the slider
	 * @param worldMax the max value of the world value domain which will be aligned
	 *                 with the max int value provided for the slider
	 * @throws IllegalArgumentException if worldMin or worldMax is null, or if min
	 *                                  >= max, or if worldMin >= worldMax, or if
	 *                                  low > high
	 */
	public InfoVisRangeSliderPanel(int min, int max, int low, int high, Number worldMin, Number worldMax) {
		super(new BorderLayout());

		// Validate inputs
		if (worldMin == null || worldMax == null) {
			throw new IllegalArgumentException("worldMin and worldMax cannot be null");
		}
		if (min >= max) {
			throw new IllegalArgumentException("min (" + min + ") must be < max (" + max + ")");
		}
		if (worldMin.doubleValue() >= worldMax.doubleValue()) {
			throw new IllegalArgumentException("worldMin must be < worldMax");
		}
		if (low > high) {
			throw new IllegalArgumentException("low (" + low + ") must be <= high (" + high + ")");
		}

		this.infoVisRangeSlider = new InfoVisRangeSlider(min, max, low, high);
		this.infoVisRangeSlider.setPreferredSize(new Dimension(0, DEFAULT_SLIDER_HEIGHT));

		XAxisNumericalPainter<Number> painter = new XAxisNumericalPainter<Number>(worldMin, worldMax);
		painter.setBackgroundPaint(null);
		this.xAxisChartPanel = new InfoVisChartPanel(painter);
		this.xAxisChartPanel.setPreferredSize(new Dimension(0, DEFAULT_AXIS_HEIGHT));

		refreshView();
	}

	/**
	 * Rebuilds the component hierarchy based on current settings.
	 */
	private void refreshView() {
		this.removeAll();

		this.add(infoVisRangeSlider, BorderLayout.SOUTH);

		if (showXAxis) {
			// Lazily create margin panels
			if (marginEast == null || marginWest == null) {
				int marginWidth = (int) (InfoVisRangeSlider.SLIDER_POINTER_WIDTH * MARGIN_FACTOR);

				marginEast = new JPanel();
				marginEast.setPreferredSize(new Dimension(marginWidth, 0));

				marginWest = new JPanel();
				marginWest.setPreferredSize(new Dimension(marginWidth, 0));
			}

			JPanel north = new JPanel(new BorderLayout());
			north.add(marginEast, BorderLayout.EAST);
			north.add(marginWest, BorderLayout.WEST);
			north.add(xAxisChartPanel, BorderLayout.CENTER);
			this.add(north, BorderLayout.NORTH);
		}

		revalidate();
		repaint();
	}

	/**
	 * Returns whether the X-axis is currently visible.
	 * 
	 * @return true if the X-axis is shown, false otherwise
	 */
	public boolean isShowXAxis() {
		return showXAxis;
	}

	/**
	 * Sets whether the X-axis should be visible.
	 * 
	 * @param showXAxis true to show the X-axis, false to hide it
	 */
	public void setShowXAxis(boolean showXAxis) {
		if (this.showXAxis == showXAxis) {
			return; // No change needed
		}

		this.showXAxis = showXAxis;
		refreshView();
	}

	/**
	 * Returns the range slider component.
	 * 
	 * @return the InfoVisRangeSlider instance
	 */
	public InfoVisRangeSlider getRangeSlider() {
		return infoVisRangeSlider;
	}

	/**
	 * Returns the X-axis chart panel.
	 * 
	 * @return the InfoVisChartPanel containing the X-axis painter
	 */
	public InfoVisChartPanel getXAxisChartPanel() {
		return xAxisChartPanel;
	}

	/**
	 * Returns the current low value from the range slider.
	 * 
	 * @return the low value
	 */
	public int getLowValue() {
		return infoVisRangeSlider.getValue();
	}

	/**
	 * Returns the current high value from the range slider.
	 * 
	 * @return the high value
	 */
	public int getHighValue() {
		return infoVisRangeSlider.getHighValue();
	}

	/**
	 * Sets the low value of the range slider.
	 * 
	 * @param value the new low value
	 */
	public void setLowValue(int value) {
		infoVisRangeSlider.setValue(value);
	}

	/**
	 * Sets the high value of the range slider.
	 * 
	 * @param value the new high value
	 */
	public void setHighValue(int value) {
		infoVisRangeSlider.setHighValue(value);
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (infoVisRangeSlider != null)
			infoVisRangeSlider.setForeground(fg);

		if (xAxisChartPanel != null)
			xAxisChartPanel.setForeground(fg);
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);

		if (infoVisRangeSlider != null)
			infoVisRangeSlider.setBackground(bg);

		if (xAxisChartPanel != null)
			xAxisChartPanel.setBackground(bg);

		if (marginEast != null)
			marginEast.setBackground(bg);

		if (marginWest != null)
			marginWest.setBackground(bg);
	}
}
