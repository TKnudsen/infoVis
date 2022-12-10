package com.github.TKnudsen.infoVis.view.painters.string;

import com.github.TKnudsen.infoVis.view.frames.SVGFrame;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
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

		final VerticalStringAlignment va = VerticalStringAlignment.UP;

		StringPainter stringPainter1 = new StringPainter("1");
		// stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		stringPainter1.setVerticalOrientation(true);
		stringPainter1.setVerticalStringAlignment(va);

		StringPainter stringPainter2 = new StringPainter("2");
		// stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		stringPainter2.setVerticalOrientation(true);
		stringPainter2.setVerticalStringAlignment(va);

		StringPainter[][] painters = new StringPainter[1][2];
		painters[0][0] = stringPainter1;
		painters[0][1] = stringPainter2;

		Grid2DPainterPainter<StringPainter> grid = new Grid2DPainterPainter<>(painters);
		grid.setDrawOutline(true);

		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(grid);

		SVGFrame frame = SVGFrameTools.dropSVGFrame(panel, "asdf", 300, 200);
		frame.isAlwaysOnTop();
	}

}
