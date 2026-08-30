package com.github.TKnudsen.infoVis.view.painters.string;

/**
 * <p>
 * StringPainter that is calibrated to paint titles
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public class TitlePainter extends StringPainter {

	public TitlePainter(String string) {
		super(string);
		this.setBackgroundPaint(null);
		this.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);
		this.setVerticalStringAlignment(VerticalStringAlignment.UP);
	}

}
