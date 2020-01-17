package com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics;

import java.awt.geom.Rectangle2D;

public interface IAxisSizeCharacteristics {

	public Rectangle2D getAxisRectangle();

	public double getAxisLegendOffset();

	public boolean isDrawAxis();

	public double getMinSizeToDrawAxis();

}
