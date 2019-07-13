package com.github.TKnudsen.infoVis.view.tools;

import javax.swing.JComponent;

import org.apache.batik.svggen.SVGGraphics2D;

import de.javagl.svggraphics.SvgGraphics;
import de.javagl.svggraphics.SvgGraphicsWriter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * allows saving a JComponent as a SVG image.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class SVGTools {

	public static void saveSVG(JComponent component) {
		SVGGraphics2D g = SvgGraphics.create();
		component.printAll(g);
		SvgGraphicsWriter.save(g);
	}
}
