package com.github.TKnudsen.infoVis.view.painters.string;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class StringPainterTester {

	public static void main(String[] args) {

		StringPainter stringPainter = new StringPainter("Peter, Paul, and Mary");
		// stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		stringPainter.setVerticalOrientation(true);
		stringPainter.setVerticalStringAlignment(VerticalStringAlignment.DOWN);

		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(stringPainter);

		SVGFrameTools.dropSVGFrame(panel, "asdf", 200, 200);
	}

}
