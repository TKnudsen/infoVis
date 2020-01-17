package com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics;

import java.awt.geom.Rectangle2D;

public class AxisSizeCharacteristics implements IAxisSizeCharacteristics {

	private Rectangle2D axisRectangle = null;
	private double axisLegendOffset = 30;
	private boolean drawAxis = true;
	private double minSizeToDrawAxis = 100.0;

	@Override
	public Rectangle2D getAxisRectangle() {
		return axisRectangle;
	}

	public void setAxisRectangle(Rectangle2D axisRectangle) {
		this.axisRectangle = axisRectangle;
	}

	@Override
	public double getAxisLegendOffset() {
		return axisLegendOffset;
	}

	public void setAxisLegendOffset(double axisLegendOffset) {
		this.axisLegendOffset = axisLegendOffset;
	}

	@Override
	public boolean isDrawAxis() {
		return drawAxis;
	}

	public void setDrawAxis(boolean drawAxis) {
		this.drawAxis = drawAxis;
	}

	@Override
	public double getMinSizeToDrawAxis() {
		return minSizeToDrawAxis;
	}

	public void setMinSizeToDrawAxis(double minSizeToDrawAxis) {
		this.minSizeToDrawAxis = minSizeToDrawAxis;
	}

}
