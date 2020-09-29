package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * a panel that combines an InfoVisRangeSlider and an underlying panel with a
 * XAxisNumericalPainter.
 * 
 * Note that the axis painter is aligned to the InfoVisRangeSlider without
 * knowing it. Problem here was that both the range slider as well as the axis
 * painter are IXPositionEncoders not used to correspond to some external master
 * component regarding x axis mapping.
 * 
 */
public class InfoVisRangeSliderPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final InfoVisRangeSlider infoVisRangeSlider;
	private final InfoVisChartPanel xAxisChartPanel;

	private boolean showXAxis = true;

	/**
	 * Creates a horizontal slider using the specified min, max, low and high value.
	 *
	 * @param min      the minimum int value of the slider.
	 * @param max      the maximum int value of the slider.
	 * @param worldMin the min value of the world value domain which will be aligned
	 *                 with the min int value provided for the slider
	 * @param worldMax the max value of the world value domain which will be aligned
	 *                 with the max int value provided for the slider
	 */
	public InfoVisRangeSliderPanel(int min, int max, Number worldMin, Number worldMax) {
		super(new BorderLayout());

		this.infoVisRangeSlider = new InfoVisRangeSlider(min, max);
		this.xAxisChartPanel = new InfoVisChartPanel(new XAxisNumericalPainter<Number>(worldMin, worldMax));

		refreshView();
	}

	/**
	 * Creates a horizontal slider using the specified min, max, low and high value.
	 *
	 * @param min      the minimum int value of the slider.
	 * @param max      the maximum int value of the slider.
	 * @param low      the low value of the slider since it is a range.
	 * @param high     the high value of the slider since it is a range.
	 * @param worldMin the min value of the world value domain which will be aligned
	 *                 with the min int value provided for the slider
	 * @param worldMax the max value of the world value domain which will be aligned
	 *                 with the max int value provided for the slider
	 */
	public InfoVisRangeSliderPanel(int min, int max, int low, int high, Number worldMin, Number worldMax) {
		super(new BorderLayout());

		this.infoVisRangeSlider = new InfoVisRangeSlider(min, max, low, high);
		this.infoVisRangeSlider.setPreferredSize(new Dimension(0, 28));

		XAxisNumericalPainter<Number> painter = new XAxisNumericalPainter<Number>(worldMin, worldMax);
		painter.setBackgroundPaint(null);
		this.xAxisChartPanel = new InfoVisChartPanel(painter);
		this.xAxisChartPanel.setPreferredSize(new Dimension(0, 22));

		refreshView();
	}

	private void refreshView() {
		this.removeAll();

		this.add(infoVisRangeSlider, BorderLayout.SOUTH);

		if (showXAxis) {
			JPanel marginEast = new JPanel();
			marginEast.setPreferredSize(new Dimension((int) (InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));

			JPanel marginWest = new JPanel();
			marginWest.setPreferredSize(new Dimension((int) (InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));

			JPanel south = new JPanel(new BorderLayout());
			south.add(marginEast, BorderLayout.EAST);
			south.add(marginWest, BorderLayout.WEST);
			south.add(xAxisChartPanel, BorderLayout.CENTER);
			this.add(south, BorderLayout.NORTH);
		}
	}

	public boolean isShowXAxis() {
		return showXAxis;
	}

	public void setShowXAxis(boolean showXAxis) {
		this.showXAxis = showXAxis;

		refreshView();
	}

	public InfoVisRangeSlider getRangeSlider() {
		return infoVisRangeSlider;
	}

	public InfoVisChartPanel getxAxisChartPanel() {
		return xAxisChartPanel;
	}
}
