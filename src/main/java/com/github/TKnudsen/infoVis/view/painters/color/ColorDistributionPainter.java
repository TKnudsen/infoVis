package com.github.TKnudsen.infoVis.view.painters.color;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPolygon;

/**
 * Line/area chart of a value distribution (e.g. a color histogram's
 * saturation or brightness bins), colored per-point via a caller-supplied
 * function of the point's normalized x-position (0..1). Use
 * {@link #saturationColorFunction()}/{@link #brightnessColorFunction()} for
 * the presets matching a color histogram's usual look, or supply any other
 * {@link Color} function.
 *
 * @version 1.0
 * @since 2026
 */
public class ColorDistributionPainter extends ChartPainter {

	private final double[] values;
	private final Function<Double, Color> colorFunction;
	private final double valueMax;

	public ColorDistributionPainter(double[] values, Function<Double, Color> colorFunction) {
		this.values = values;
		this.colorFunction = colorFunction;
		this.valueMax = new StatisticsSupport(values).getMax();
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		g2.setStroke(DisplayTools.thickStroke);

		ColoredPolygon poly = new ColoredPolygon();
		for (int i = 0; i < values.length; i++) {
			double dx = MathFunctions.linearScale(0, values.length, i, true);
			double x = dx * chartRectangle.getWidth();
			double y = chartRectangle.getMaxY()
					- (MathFunctions.linearScale(0, valueMax, values[i], true) * (chartRectangle.getHeight() - 1) + 1);
			Point2D.Double p = new Point2D.Double(x, y);
			poly.addPoint((int) p.getX(), (int) p.getY(), colorFunction.apply(dx));
		}

		ColorPolygonPainter.draw(g2, poly);
	}

	/**
	 * Preset matching a color histogram's usual saturation-distribution look: a
	 * blue hue whose brightness/saturation is modulated by x-position.
	 */
	public static Function<Double, Color> saturationColorFunction() {
		return dx -> ColorTools.setBrightnessAndSaturation(new Color(18, 126, 218), 128, (int) (255 * dx));
	}

	/**
	 * Preset matching a color histogram's usual brightness-distribution look:
	 * plain grayscale by x-position.
	 */
	public static Function<Double, Color> brightnessColorFunction() {
		return dx -> new Color((int) (255 * dx), (int) (255 * dx), (int) (255 * dx));
	}
}
