package com.github.TKnudsen.infoVis.view.painters.string.test;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

public class TitlePainterTest {

	public static void main(String[] args) {
		
		TitlePainter titlePainter = new TitlePainter("This is a Test");
		titlePainter.setBackgroundPaint(Color.GRAY);
		titlePainter.setVerticalOrientation(true);
		
		InfoVisChartPanel panel = new InfoVisChartPanel(titlePainter);
		SVGFrameTools.dropSVGFrame(panel, null, 100, 100);

	}

}
