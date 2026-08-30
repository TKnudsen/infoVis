package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.github.TKnudsen.infoVis.view.interaction.handlers.TooltipHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;

/**
 * <p>
 * An advanced range slider with position encoding support for InfoVis
 * applications. Extends {@link RangeSlider} and implements
 * {@link IXPositionEncoder} to provide position mapping capabilities between
 * slider values and pixel coordinates. The slider automatically adjusts its
 * position encoding when resized.
 * 
 * Orientation and flip of the slider thumb can be adjusted through
 * {@link javax.swing.JSlider#HORIZONTAL}/{@link javax.swing.JSlider#VERTICAL}
 * orientation and by setting {@code setFlipThumb(true)}.
 * </p>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class InfoVisRangeSlider extends RangeSlider implements IXPositionEncoder {

	private static final long serialVersionUID = 1L;

	/**
	 * Slider has a width of 16 pixels, 8 of these pixels lead to a margin on both
	 * sides (left and right).
	 */
	public static final int SLIDER_POINTER_WIDTH = 16;

	/** Factor for calculating slider pointer margin (half width on each side) */
	private static final double POINTER_MARGIN_FACTOR = 0.5;

	private final PositionEncodingFunction positionEncodingFunction;

	// Listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshPositionMapping;

	// Component listener for handling resize events
	private ComponentListener componentListener;

	/**
	 * Tooltip-related fields
	 */
	private ChartPainter toolTipPainter = null;
	private boolean toolTipping = true;
	private TooltipHandler tooltipHandler;

	/**
	 * Creates a horizontal slider using the specified min and max with an initial
	 * value spanning the full range.
	 * 
	 * <p>
	 * Attention: the com.jidesoft.swing.RangeSlider basic class produced an
	 * IllegalAccessError in the constructor for some configurations. The following
	 * line executed beforehand helps in some cases:
	 * {@code LookAndFeelFactory.setDefaultStyle(1);}
	 * </p>
	 *
	 * @param min the minimum value of the slider
	 * @param max the maximum value of the slider
	 * @throws IllegalArgumentException if min >= max
	 */
	public InfoVisRangeSlider(int min, int max) {
		this(min, max, min, max);
	}

	/**
	 * Creates a horizontal slider using the specified min, max, low and high value.
	 * 
	 * <p>
	 * Attention: the com.jidesoft.swing.RangeSlider basic class produced an
	 * IllegalAccessError in the constructor for some configurations. The following
	 * line executed beforehand helps in some cases:
	 * {@code LookAndFeelFactory.setDefaultStyle(1);}
	 * </p>
	 *
	 * @param min  the minimum value of the slider
	 * @param max  the maximum value of the slider
	 * @param low  the low value of the slider since it is a range
	 * @param high the high value of the slider since it is a range
	 * @throws IllegalArgumentException if min >= max or low > high
	 */
	public InfoVisRangeSlider(int min, int max, int low, int high) {
		super(min, max, low, high);

		// Validation is already done in RangeSlider constructor, but adding here for
		// clarity
		if (min >= max) {
			throw new IllegalArgumentException("min (" + min + ") must be < max (" + max + ")");
		}
		if (low > high) {
			throw new IllegalArgumentException("low (" + low + ") must be <= high (" + high + ")");
		}

		this.positionEncodingFunction = new PositionEncodingFunction(getMinimum(), getMaximum(), 0.0, 0.0, false);
		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		initialize();
	}

	/**
	 * Initializes the slider by setting up position mapping and component
	 * listeners.
	 */
	private void initialize() {
		refreshPositionMapping();

		componentListener = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				refreshPositionMapping();
			}
		};

		addComponentListener(componentListener);
	}

	/**
	 * Checks whether the given world value falls within the current slider range.
	 * 
	 * @param worldValue the value to check
	 * @return true if the value is within the range [getValue(), getValue() +
	 *         getExtent()], false otherwise
	 */
	public boolean inRange(double worldValue) {
		if (Double.isNaN(worldValue)) {
			return false;
		}
		return worldValue >= getValue() && worldValue <= (getExtent() + getValue());
	}

	/**
	 * Updates the position mapping based on current component bounds. Adjusts for
	 * slider pointer width margins on both sides.
	 */
	private void refreshPositionMapping() {
		Rectangle bounds = getBounds();

		if (bounds == null || bounds.width == 0 || bounds.height == 0) {
			this.positionEncodingFunction.setMinPixel(0.0);
			this.positionEncodingFunction.setMaxPixel(0.0);
			return;
		}

		double margin = SLIDER_POINTER_WIDTH * POINTER_MARGIN_FACTOR;
		this.positionEncodingFunction.setMinPixel(bounds.getMinX() + margin);
		this.positionEncodingFunction.setMaxPixel(bounds.getMaxX() - margin);
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	/**
	 * Returns whether the slider thumb is flipped.
	 * 
	 * @return true if the thumb is flipped, false otherwise
	 */
	public boolean isFlipThumb() {
		return getRangeSliderUI().isFlipThumb();
	}

	/**
	 * Sets whether the slider thumb should be flipped.
	 * 
	 * @param flipThumb true to flip the thumb, false for normal orientation
	 */
	public void setFlipThumb(boolean flipThumb) {
		this.getRangeSliderUI().setFlipThumb(flipThumb);
	}

	/**
	 * Returns the tooltip painter.
	 * 
	 * @return the ChartPainter used for tooltips, or null if none is set
	 */
	public ChartPainter getToolTipPainter() {
		return toolTipPainter;
	}

	/**
	 * Sets the tooltip painter.
	 * 
	 * @param toolTipPainter the ChartPainter to use for tooltips
	 */
	public void setToolTipPainter(ChartPainter toolTipPainter) {
		this.toolTipPainter = toolTipPainter;
	}

	/**
	 * Returns whether tooltips are enabled.
	 * 
	 * @return true if tooltips are enabled, false otherwise
	 */
	public boolean isToolTipping() {
		return toolTipping;
	}

	/**
	 * Sets whether tooltips should be enabled.
	 * 
	 * @param toolTipping true to enable tooltips, false to disable
	 */
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	/**
	 * Returns the tooltip handler.
	 * 
	 * @return the TooltipHandler instance, or null if none is set
	 */
	public TooltipHandler getTooltipHandler() {
		return tooltipHandler;
	}

	/**
	 * Sets the tooltip handler.
	 * 
	 * @param tooltipHandler the TooltipHandler to use
	 */
	public void setTooltipHandler(TooltipHandler tooltipHandler) {
		this.tooltipHandler = tooltipHandler;
	}

	/**
	 * Cleans up resources used by this slider. Should be called when the slider is
	 * no longer needed to prevent memory leaks.
	 */
	public void dispose() {
		if (componentListener != null) {
			removeComponentListener(componentListener);
			componentListener = null;
		}

		if (positionEncodingFunction != null) {
			positionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		}
	}
}
