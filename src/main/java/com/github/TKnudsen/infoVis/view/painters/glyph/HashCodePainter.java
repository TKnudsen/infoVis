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
 * The derivation constants below were tuned by hand against the 32-bit range
 * of {@code Object.hashCode()} to produce visually varied but compact
 * glyphs; that is why the constructor takes an {@code int}, not a
 * {@code long} -- feeding it a wider value defeats the tuning and tends to
 * push the glyph's spikes outside its own rectangle. There is no formal
 * derivation behind the individual numbers -- they are named for the role
 * they play, not for any documented meaning of the value itself.
 * </p>
 *
 * @version 1.1
 * @since 2013
 */
public class HashCodePainter extends ChartPainter {

	// -- initRotation --
	private static final double ROTATION_DIVISOR = 222222.2;

	// -- numberOfPeaks --
	private static final double PEAK_COUNT_SEED_FACTOR = 0.44;
	private static final int PEAK_COUNT_MAX_ATTEMPTS = 12;
	private static final int PEAK_COUNT_MIN_ACCEPTED = 2;
	private static final double PEAK_COUNT_DECAY_DIVISOR = 9.0;
	private static final int PEAK_COUNT_FALLBACK = 6;

	// -- orbitDistance: fraction of the glyph's half-size the peaks orbit at --
	private static final double ORBIT_DISTANCE_BASE = 0.25;
	private static final double ORBIT_DISTANCE_DIVISOR = 3333.3;
	private static final double ORBIT_DISTANCE_MODULO = 100;
	private static final double ORBIT_DISTANCE_CENTER_OFFSET = 50;
	private static final double ORBIT_DISTANCE_SCALE = 0.005;

	// -- offshotAngle: half-angle (degrees) of the spike's fork at the orbit point --
	private static final double OFFSHOT_ANGLE_DIVISOR = 66666.6;
	private static final double OFFSHOT_ANGLE_MODULO = 4500;
	private static final double OFFSHOT_ANGLE_SCALE = 0.01;
	private static final double OFFSHOT_ANGLE_BASE_DEGREES = 45;

	// -- offshotDistance: fraction of the glyph's half-size each fork tip reaches --
	private static final double OFFSHOT_DISTANCE_DIVISOR = 188888.0;
	private static final double OFFSHOT_DISTANCE_MODULO = 20;
	private static final double OFFSHOT_DISTANCE_SCALE = 0.01;
	private static final double OFFSHOT_DISTANCE_BASE = 0.05;
	private static final double OFFSHOT_DISTANCE_PEAK_WIDENING = 0.1;

	// -- centerDotSize: fraction of the glyph's half-size the center dot's radius spans --
	private static final double CENTER_DOT_SIZE_DIVISOR = 10000.0;
	private static final double CENTER_DOT_SIZE_MODULO = 50;
	private static final double CENTER_DOT_SIZE_SCALE = 0.0066;

	// -- draw(): how far the spline's control points bulge sideways from the spike's centerline --
	private static final double CURVE_CONTROL_POINT_OFFSET = 0.1;

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
		initRotation = Math.abs(hashcode) / ROTATION_DIVISOR % (int) ((360.0 / maxPeakCount) * 0.5);

		double tmp = Math.abs(hashcode * PEAK_COUNT_SEED_FACTOR);
		for (int i = 0; i < PEAK_COUNT_MAX_ATTEMPTS; i++) {
			numberOfPeaks = (int) (tmp % maxPeakCount);
			if (numberOfPeaks > PEAK_COUNT_MIN_ACCEPTED)
				break;
			else
				tmp /= PEAK_COUNT_DECAY_DIVISOR;
			if (i == PEAK_COUNT_MAX_ATTEMPTS - 1)
				numberOfPeaks = PEAK_COUNT_FALLBACK;
		}

		orbitDistance = ORBIT_DISTANCE_BASE + (Math.abs(hashcode / ORBIT_DISTANCE_DIVISOR) % ORBIT_DISTANCE_MODULO
				- ORBIT_DISTANCE_CENTER_OFFSET) * ORBIT_DISTANCE_SCALE;

		offshotAngle = (Math.abs(hashcode / OFFSHOT_ANGLE_DIVISOR) % OFFSHOT_ANGLE_MODULO) * OFFSHOT_ANGLE_SCALE
				+ OFFSHOT_ANGLE_BASE_DEGREES;

		offshotDistance = (Math.abs(hashcode / OFFSHOT_DISTANCE_DIVISOR) % OFFSHOT_DISTANCE_MODULO)
				* OFFSHOT_DISTANCE_SCALE + OFFSHOT_DISTANCE_BASE;
		// widen when numberOfPeaks is small
		offshotDistance += (1.0 / (double) numberOfPeaks) * OFFSHOT_DISTANCE_PEAK_WIDENING;

		centerDotSize = (1.0 - orbitDistance) * ((hashcode / CENTER_DOT_SIZE_DIVISOR) % CENTER_DOT_SIZE_MODULO)
				* CENTER_DOT_SIZE_SCALE;
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
			DisplayTools.drawLine(g2, (float) rectangle.getCenterX(), (float) rectangle.getCenterY(), (float) orbitX,
					(float) orbitY);

			double offshotPointX1 = orbitX - Math.sin(Math.toRadians(offshotAngle)) * offshotDistance * half;
			double offshotPointX2 = orbitX + Math.sin(Math.toRadians(offshotAngle)) * offshotDistance * half;
			double offshotPointY = orbitY - Math.cos(Math.toRadians(offshotAngle)) * offshotDistance * half;
			DisplayTools.drawLine(g2, (float) orbitX, (float) orbitY, (float) offshotPointX1, (float) offshotPointY);
			DisplayTools.drawLine(g2, (float) orbitX, (float) orbitY, (float) offshotPointX2, (float) offshotPointY);

			// spline towards the border and back
			double curveScaleX = rectangle.getCenterX();
			Path2D.Double path = new Path2D.Double();
			path.moveTo(offshotPointX1, offshotPointY);
			path.quadTo(curveScaleX - CURVE_CONTROL_POINT_OFFSET * half, rectangle.getMinY(), curveScaleX,
					rectangle.getMinY());
			path.quadTo(curveScaleX + CURVE_CONTROL_POINT_OFFSET * half, rectangle.getMinY(), offshotPointX2,
					offshotPointY);
			g2.draw(path);

			// g2's paint is already `color` (set at the top of this method)
			DisplayTools.drawPoint(g2, rectangle.getCenterX(), rectangle.getCenterY(), centerDotSize * half, false);

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
