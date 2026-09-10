package com.github.TKnudsen.infoVis.view.painters.glyph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a triangular, beveled "plastic button" glyph: three trapezoid/
 * triangle facets (top, left, right, and an inner center triangle), each
 * shaded a different tone of the base color to fake a 3D-embossed look, with
 * a pressed/unpressed appearance controlled by {@link #setClicked(boolean)}.
 *
 * @version 1.0
 * @since 2012
 */
public class TriangularPlasticPainter extends ChartPainter {

	private static final double DEFAULT_DEPTH_EFFECT = 0.2;

	/** scales the vertical (but not horizontal) component of the inward bevel offset */
	private static final double DEPTH_EFFECT_Y_FACTOR = 0.66;

	/** the bevel's inward offset shrinks as the glyph widens, via this width-dependent saturation factor */
	private static final double SATURATION_WIDTH_COEFFICIENT = -0.0133;
	private static final double SATURATION_BASE = 1.0;
	private static final double SATURATION_MIN = 0.4;
	private static final double SATURATION_MAX = 1.0;

	/** facet outlines are only drawn once the glyph is large enough for them to read cleanly */
	private static final double MIN_HEIGHT_FOR_OUTLINE = 25;
	private static final double MIN_WIDTH_FOR_OUTLINE = 30;

	private Color baseColor;
	private Color darker;
	private Color darker2;
	private Color darkest;

	private Point2D topLeft;
	private Point2D topRight;
	private Point2D bottom;
	private Point2D topLeftInner;
	private Point2D topRightInner;
	private Point2D bottomInner;

	private double depthEffect = DEFAULT_DEPTH_EFFECT;

	private Path2D topTrapezoid;
	private Path2D leftTrapezoid;
	private Path2D rightTrapezoid;
	private Path2D centerTriangle;

	private boolean clicked = false;

	public TriangularPlasticPainter(Color color) {
		setBaseColor(color);
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null || topTrapezoid == null)
			return;

		Color old = g2.getColor();

		if (!clicked) {
			g2.setColor(baseColor);
			g2.fill(topTrapezoid);
			g2.fill(leftTrapezoid);
			g2.setColor(darkest);
			g2.fill(rightTrapezoid);
			g2.setColor(darker);
			g2.fill(centerTriangle);
		} else {
			g2.setColor(darkest);
			g2.fill(topTrapezoid);
			g2.fill(leftTrapezoid);
			g2.setColor(baseColor);
			g2.fill(rightTrapezoid);
			g2.setColor(darker2);
			g2.fill(centerTriangle);
		}

		if (rectangle.getHeight() > MIN_HEIGHT_FOR_OUTLINE && rectangle.getWidth() > MIN_WIDTH_FOR_OUTLINE) {
			g2.draw(topTrapezoid);
			g2.draw(leftTrapezoid);
			g2.draw(rightTrapezoid);
		}

		g2.setColor(old);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null)
			return;

		topLeft = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
		topRight = new Point2D.Double(rectangle.getMaxX(), rectangle.getMinY());
		bottom = new Point2D.Double(rectangle.getCenterX(), rectangle.getMaxY());

		double saturation = Math.max(SATURATION_MIN, Math.min(SATURATION_MAX,
				SATURATION_WIDTH_COEFFICIENT * rectangle.getWidth() + SATURATION_BASE));

		double depthOffsetX = depthEffect * rectangle.getWidth() * saturation;
		double depthOffsetY = depthEffect * rectangle.getHeight() * DEPTH_EFFECT_Y_FACTOR * saturation;

		topLeftInner = new Point2D.Double(rectangle.getMinX() + depthOffsetX, rectangle.getMinY() + depthOffsetY);
		topRightInner = new Point2D.Double(rectangle.getMaxX() - depthOffsetX, rectangle.getMinY() + depthOffsetY);
		bottomInner = new Point2D.Double(rectangle.getCenterX(), rectangle.getMaxY() - 2 * depthOffsetY);

		topTrapezoid = new Path2D.Double();
		topTrapezoid.moveTo(topLeft.getX(), topLeft.getY());
		topTrapezoid.lineTo(topRight.getX(), topRight.getY());
		topTrapezoid.lineTo(topRightInner.getX(), topRightInner.getY());
		topTrapezoid.lineTo(topLeftInner.getX(), topLeftInner.getY());
		topTrapezoid.lineTo(topLeft.getX(), topLeft.getY());

		leftTrapezoid = new Path2D.Double();
		leftTrapezoid.moveTo(topLeft.getX(), topLeft.getY());
		leftTrapezoid.lineTo(topLeftInner.getX(), topLeftInner.getY());
		leftTrapezoid.lineTo(bottomInner.getX(), bottomInner.getY());
		leftTrapezoid.lineTo(bottom.getX(), bottom.getY());
		leftTrapezoid.lineTo(topLeft.getX(), topLeft.getY());

		rightTrapezoid = new Path2D.Double();
		rightTrapezoid.moveTo(topRight.getX(), topRight.getY());
		rightTrapezoid.lineTo(topRightInner.getX(), topRightInner.getY());
		rightTrapezoid.lineTo(bottomInner.getX(), bottomInner.getY());
		rightTrapezoid.lineTo(bottom.getX(), bottom.getY());
		rightTrapezoid.lineTo(topRight.getX(), topRight.getY());

		centerTriangle = new Path2D.Double();
		centerTriangle.moveTo(topLeftInner.getX(), topLeftInner.getY());
		centerTriangle.lineTo(topRightInner.getX(), topRightInner.getY());
		centerTriangle.lineTo(bottomInner.getX(), bottomInner.getY());
		centerTriangle.lineTo(topLeftInner.getX(), topLeftInner.getY());
	}

	public Color getBaseColor() {
		return baseColor;
	}

	public void setBaseColor(Color color) {
		this.baseColor = color;
		setPaint(color);

		if (color == null) {
			darker = null;
			darker2 = null;
			darkest = null;
		} else {
			darker = color.darker();
			darker2 = darker.darker();
			darkest = darker2.darker();
		}
	}

	/** @return the glyph's outline as a closed triangle (top-left, top-right, bottom) */
	public Path2D getOutline() {
		if (topLeft == null || topRight == null || bottom == null)
			return null;

		Path2D outline = new Path2D.Double();
		outline.moveTo(topLeft.getX(), topLeft.getY());
		outline.lineTo(topRight.getX(), topRight.getY());
		outline.lineTo(bottom.getX(), bottom.getY());
		outline.lineTo(topLeft.getX(), topLeft.getY());
		return outline;
	}

	public double getDepthEffect() {
		return depthEffect;
	}

	public void setDepthEffect(double depthEffect) {
		this.depthEffect = depthEffect;
	}

	public boolean isClicked() {
		return clicked;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

}
