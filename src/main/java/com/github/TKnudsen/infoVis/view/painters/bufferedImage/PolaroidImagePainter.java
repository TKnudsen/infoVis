package com.github.TKnudsen.infoVis.view.painters.bufferedImage;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;

/**
 * A "photo card": an image with a caption label beneath it.
 *
 * @version 1.0
 * @since 2023
 */
public class PolaroidImagePainter extends ChartPainter {

	private StringPainter stringPainter;
	private BufferedImagePainter imagePainter;
	private BufferedImage image;
	private double imageToLabelRatio = 0.85;
	private int border = 2;

	public PolaroidImagePainter(BufferedImage image, String label) {
		super();
		this.image = image;
		setStringPainter(new StringPainter(label));
		setImagePainter(new BufferedImagePainter(image));
		getImagePainter().setKeepRatio(true);
		drawOutline = true;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			getImagePainter().setRectangle(null);
			getStringPainter().setRectangle(null);
			return;
		}

		double height = rectangle.getHeight() - 2 * border;
		getImagePainter().setRectangle(new Rectangle2D.Double(rectangle.getX() + border, rectangle.getY() + border,
				rectangle.getWidth() - 2 * border, height * imageToLabelRatio));
		getStringPainter().setRectangle(new Rectangle2D.Double(rectangle.getX() + border,
				rectangle.getY() + border + height * imageToLabelRatio, rectangle.getWidth() - 2 * border,
				height * (1 - imageToLabelRatio)));
	}

	public void draw(Graphics2D g2) {
		super.draw(g2);
		getImagePainter().draw(g2);
		getStringPainter().draw(g2);
	}

	public BufferedImage getData() {
		return image;
	}

	/**
	 * @return the imageToLabelRatio
	 */
	public double getImageToLabelRatio() {
		return imageToLabelRatio;
	}

	/**
	 * @param imageToLabelRatio
	 *            the imageToLabelRatio to set
	 */
	public void setImageToLabelRatio(double imageToLabelRatio) {
		this.imageToLabelRatio = imageToLabelRatio;
	}

	/**
	 * @return the stringPainter
	 */
	public StringPainter getStringPainter() {
		return stringPainter;
	}

	/**
	 * @param stringPainter
	 *            the stringPainter to set
	 */
	public void setStringPainter(StringPainter stringPainter) {
		this.stringPainter = stringPainter;
	}

	/**
	 * @return the imagePainter
	 */
	public BufferedImagePainter getImagePainter() {
		return imagePainter;
	}

	/**
	 * @param imagePainter
	 *            the imagePainter to set
	 */
	public void setImagePainter(BufferedImagePainter imagePainter) {
		this.imagePainter = imagePainter;
	}

	@Override
	public void setBackgroundPaint(Paint backgroundColor) {
		super.setBackgroundPaint(backgroundColor);

		if (imagePainter != null)
			imagePainter.setBackgroundPaint(backgroundColor);
		if (stringPainter != null)
			stringPainter.setBackgroundPaint(backgroundColor);
	}

	/**
	 * @return the border
	 */
	public int getBorder() {
		return border;
	}

	/**
	 * @param border
	 *            the border to set
	 */
	public void setBorder(int border) {
		this.border = border;
	}

}
