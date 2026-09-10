package com.github.TKnudsen.infoVis.view.painters.glyph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import java.util.Objects;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders a snowflake-like glyph deterministically derived from an
 * {@link Object#hashCode()} -- the same hash always produces the same glyph
 * (peak count, rotation, spike shape all derive from arithmetic on the hash
 * value), making it useful as a compact per-object visual identifier (an
 * "identicon" for arbitrary objects).
 *
 * <p>
 * The derivation constants were tuned against the 32-bit range of
 * {@code Object.hashCode()}; that is why the constructor takes an
 * {@code int}, not a {@code long} -- feeding it a wider value defeats the
 * tuning and tends to push the glyph's spikes outside its own rectangle.
 * </p>
 *
 * @version 1.1
 * @since 2013
 */
public class HashCodePainter extends ChartPainter {

	private final int hashcode;
	private final int maxPeakCount;

	// derived rendering parameters, computed once from the hash code
	private double initRotation;
	private int numberOfPeaks;
	private double orbitDistance;
	private double offshotAngle;
	private double offshotDistance;
	private double centerDotSize;

	public HashCodePainter(int hashcode, int maxPeakCount) {
		this.hashcode = hashcode;
		this.maxPeakCount = maxPeakCount;

		initialize();
	}

	/** @param object the object whose {@link Object#hashCode()} is used to derive the glyph; null is treated as hash code 0 */
	public HashCodePainter(Object object, int maxPeakCount) {
		this(Objects.hashCode(object), maxPeakCount);
	}

	private void initialize() {
		initRotation = Math.abs(hashcode) / 222222.2 % (int) ((360.0 / maxPeakCount) * 0.5);

		double tmp = Math.abs(hashcode * 0.44);
		for (int i = 0; i < 12; i++) {
			numberOfPeaks = (int) (tmp % maxPeakCount);
			if (numberOfPeaks > 2)
				break;
			else
				tmp /= 9.0;
			if (i == 11)
				numberOfPeaks = 6;
		}

		orbitDistance = 0.25 + (Math.abs(hashcode / 3333.3) % 100 - 50) * 0.005;

		offshotAngle = (Math.abs(hashcode / 66666.6) % 4500) * 0.01 + 45;

		offshotDistance = (Math.abs(hashcode / 188888.0) % 20) * 0.01 + 0.05;
		// widen when numberOfPeaks is small
		offshotDistance += (1.0 / (double) numberOfPeaks) * 0.1;

		centerDotSize = (1.0 - orbitDistance) * ((hashcode / 10000.0) % 50) * 0.0066;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		Color oldColor = g2.getColor();
		g2.setColor(color);
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(stroke);

		super.draw(g2);

		double half = Math.min(getRectangle().getWidth(), getRectangle().getHeight()) * 0.5;

		double angle = 2 * Math.PI / (double) numberOfPeaks;
		AffineTransform old = g2.getTransform();
		AffineTransform rotate = AffineTransform.getRotateInstance(angle, rectangle.getCenterX(),
				rectangle.getCenterY());
		AffineTransform initRotationTransform = AffineTransform.getRotateInstance(initRotation, rectangle.getCenterX(),
				rectangle.getCenterY());

		g2.transform(initRotationTransform);
		for (int i = 0; i < numberOfPeaks; i++) {
			double orbitX = rectangle.getCenterX();
			double orbitY = rectangle.getCenterY() - orbitDistance * half;
			DisplayTools.drawLine(g2, rectangle.getCenterX(), rectangle.getCenterY(), orbitX, orbitY);

			double offshotPointX1 = orbitX - Math.sin(Math.toRadians(offshotAngle)) * offshotDistance * half;
			double offshotPointX2 = orbitX + Math.sin(Math.toRadians(offshotAngle)) * offshotDistance * half;
			double offshotPointY = orbitY - Math.cos(Math.toRadians(offshotAngle)) * offshotDistance * half;
			DisplayTools.drawLine(g2, orbitX, orbitY, offshotPointX1, offshotPointY);
			DisplayTools.drawLine(g2, orbitX, orbitY, offshotPointX2, offshotPointY);

			// spline towards the border and back
			double curveScaleX = rectangle.getCenterX();
			Path2D.Double path = new Path2D.Double();
			path.moveTo(offshotPointX1, offshotPointY);
			path.quadTo(curveScaleX - 0.1 * half, rectangle.getMinY(), curveScaleX, rectangle.getMinY());
			path.quadTo(curveScaleX + 0.1 * half, rectangle.getMinY(), offshotPointX2, offshotPointY);
			g2.draw(path);

			DisplayTools.drawPoint(g2, rectangle.getCenterX(), rectangle.getCenterY(), centerDotSize * half, color,
					false);

			g2.transform(rotate);
		}

		g2.transform(old);

		g2.setStroke(oldStroke);
		g2.setColor(oldColor);
	}

	public int getHashcode() {
		return hashcode;
	}

}
