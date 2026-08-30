package com.github.TKnudsen.infoVis.view.visualChannels.color.impl.test;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToOrangeColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorMapEncodingFunction;

/**
 * <p>
 * Worked example for {@link ColorMapEncodingFunction}: the two-layer design
 * behind quantitative color encoding in this package.
 * 
 * Why not skip the adapter and have {@code AbstractColorMap1D} implement
 * {@code IColorEncodingFunction} directly? It already implements
 * {@code Function<Float, Color>}; a class cannot also implement
 * {@code Function<T, Paint>} (what {@code IColorEncodingFunction} extends)
 * without a method-erasure clash. The adapter is not an accident of
 * convenience, it is the only way to compose the two.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ColorMapEncodingFunctionTest {

	/**
	 * A tiny stand-in for "some domain object with a numeric field you want to
	 * color by" -- in a real application this might be a stock, a sensor
	 * reading, a table row, anything. The point is that {@code
	 * IColorEncodingFunction<T>} does not care what T is.
	 */
	private static final class DataPoint {
		final String label;
		final double momentum; // raw value, known fixed range [-1, 1]

		DataPoint(String label, double momentum) {
			this.label = label;
			this.momentum = momentum;
		}
	}

	public static void main(String[] args) {

		// Step 1: pick a palette. Every *ColorMap class in
		// visualChannels.color.colormaps.quantitative.impl exposes a static
		// getInstance() singleton, returning the AbstractColorMap base type --
		// cast down to AbstractColorMap1D to get at getColor(float)/apply(Float).
		AbstractColorMap1D palette = (AbstractColorMap1D) DarkGrayToOrangeColorMap.getInstance();

		// Step 2: describe the extraction (T -> raw number) and the rescale (raw
		// number -> [0,1]) as two separate, explicit pieces. Momentum lives in a
		// known, fixed range [-1, 1], so LinearNormalizationFunction -- the shared
		// rescale-to-[0,1] utility used throughout the ecosystem -- does the
		// rescale; use its Collection<Number> constructor instead when the bounds
		// are not known upfront and should be derived from the actual data.
		LinearNormalizationFunction momentumNormalization = new LinearNormalizationFunction(-1.0, 1.0);

		// Step 3: compose extraction + rescale + palette into a proper,
		// domain-typed color encoding function in one call -- the
		// (extractor, NormalizationFunction, colorMap) constructor does the
		// composition, so no caller has to hand-write it.
		IColorEncodingFunction<DataPoint> colorEncoding = new ColorMapEncodingFunction<>(dp -> dp.momentum,
				momentumNormalization, palette);

		// From here on, `colorEncoding` is just a Function<DataPoint, Paint> --
		// exactly what an IColorEncoding<DataPoint>-typed painter or panel would
		// receive via setColorEncodingFunction(...). No caller-side code needs to
		// know a colormap is involved at all.
		List<DataPoint> stocks = Arrays.asList(new DataPoint("Strong sell-off", -0.9),
				new DataPoint("Mild decline", -0.3), new DataPoint("Flat", 0.0), new DataPoint("Mild gain", 0.3),
				new DataPoint("Strong rally", 0.9));

		List<JPanel> swatches = new ArrayList<>();
		for (DataPoint dp : stocks) {
			Paint color = colorEncoding.apply(dp);
			System.out.println(dp.label + " (momentum=" + dp.momentum + ") -> " + color);
			swatches.add(swatch(dp.label, color));
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (JPanel s : swatches)
			panel.add(s);

		SVGFrameTools.dropSVGFrame(panel, "ColorMapEncodingFunction: momentum -> color", 220, 220);
	}

	private static JPanel swatch(String label, Paint color) {
		JPanel row = new JPanel();
		row.setBackground((Color) color);
		row.add(new JLabel(label));
		return row;
	}

}
