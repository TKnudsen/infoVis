package com.github.TKnudsen.infoVis.view.painters.string;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * StringPainter that is calibrated to paint titles
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class TitlePainter extends StringPainter {

	public TitlePainter(String string) {
		super(string);
		this.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);
		this.setVerticalStringAlignment(VerticalStringAlignment.UP);
	}

}
