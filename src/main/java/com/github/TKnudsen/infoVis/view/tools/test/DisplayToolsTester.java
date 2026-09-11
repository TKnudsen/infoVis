package com.github.TKnudsen.infoVis.view.tools.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders one grid cell per drawing method of {@link DisplayTools}, each
 * showing what that method draws. Writes the result to a PNG file rather than
 * opening a Swing window, so it can be inspected without a display.
 *
 * @since 2026
 */
public class DisplayToolsTester {

	private static final int CELL_SIZE = 160;
	private static final int COLUMNS = 4;
	private static final int LABEL_HEIGHT = 18;
	private static final int PADDING = 10;

	public static void main(String[] args) throws IOException {
		Map<String, BiConsumer<Graphics2D, Rectangle2D>> demos = new LinkedHashMap<>();

		demos.put("drawPoint", DisplayToolsTester::demoDrawPoint);
		demos.put("drawRectangle", DisplayToolsTester::demoDrawRectangle);
		demos.put("fillRectangle", DisplayToolsTester::demoFillRectangle);
		demos.put("drawShape", DisplayToolsTester::demoDrawShape);
		demos.put("drawCross", DisplayToolsTester::demoDrawCross);
		demos.put("drawLine", DisplayToolsTester::demoDrawLine);
		demos.put("drawPath", DisplayToolsTester::demoDrawPath);
		demos.put("drawCube", DisplayToolsTester::demoDrawCube);
		demos.put("drawRoundRect", DisplayToolsTester::demoDrawRoundRect);
		demos.put("drawButton", DisplayToolsTester::demoDrawButton);
		demos.put("drawRotatedString", DisplayToolsTester::demoDrawRotatedString);
		demos.put("createDiamond", DisplayToolsTester::demoCreateDiamond);
		demos.put("drawArrow", DisplayToolsTester::demoDrawArrow);
		demos.put("drawCurvedArrow", DisplayToolsTester::demoDrawCurvedArrow);

		int rows = (int) Math.ceil(demos.size() / (double) COLUMNS);
		int width = COLUMNS * CELL_SIZE;
		int height = rows * (CELL_SIZE + LABEL_HEIGHT);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, width, height);

		int index = 0;
		for (Map.Entry<String, BiConsumer<Graphics2D, Rectangle2D>> entry : demos.entrySet()) {
			int col = index % COLUMNS;
			int row = index / COLUMNS;

			int cellX = col * CELL_SIZE;
			int cellY = row * (CELL_SIZE + LABEL_HEIGHT);

			g2.setColor(Color.BLACK);
			g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
			g2.drawString(entry.getKey(), cellX + 4, cellY + 13);

			g2.setColor(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(1));
			g2.drawRect(cellX, cellY + LABEL_HEIGHT, CELL_SIZE - 1, CELL_SIZE - 1);

			Rectangle2D cellBounds = new Rectangle2D.Double(cellX + PADDING, cellY + LABEL_HEIGHT + PADDING,
					CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);

			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(1.5f));
			entry.getValue().accept(g2, cellBounds);

			index++;
		}

		g2.dispose();

		File outFile = new File(System.getProperty("java.io.tmpdir"), "DisplayToolsTester.png");
		ImageIO.write(image, "png", outFile);
		System.out.println("Wrote " + outFile.getAbsolutePath());
	}

	private static void demoDrawPoint(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawPoint(g2, r.getCenterX() - 25, r.getCenterY(), 12, true);
		DisplayTools.drawPoint(g2, r.getCenterX() + 25, r.getCenterY(), 12, false);
	}

	private static void demoDrawRectangle(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawRectangle(g2, shrink(r, 20));
	}

	private static void demoFillRectangle(Graphics2D g2, Rectangle2D r) {
		DisplayTools.fillRectangle(g2, shrink(r, 20));
	}

	private static void demoDrawShape(Graphics2D g2, Rectangle2D r) {
		Path2D.Double diamond = new Path2D.Double(DisplayTools.createDiamond((float) r.getCenterX(),
				(float) r.getCenterY(), (float) r.getWidth() - 20, (float) r.getHeight() - 20));
		DisplayTools.drawShape(g2, diamond, true);
	}

	private static void demoDrawCross(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawCross(g2, (float) r.getCenterX(), (float) r.getCenterY(), (float) r.getWidth() * 0.35f);
	}

	private static void demoDrawLine(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawLine(g2, (float) r.getMinX(), (float) r.getMinY(), (float) r.getMaxX(), (float) r.getMaxY());
		DisplayTools.drawLine(g2, (float) r.getMinX(), (float) r.getMaxY(), (float) r.getMaxX(), (float) r.getMinY());
	}

	private static void demoDrawPath(Graphics2D g2, Rectangle2D r) {
		double[] xs = { r.getMinX(), r.getCenterX(), r.getMaxX(), r.getCenterX() };
		double[] ys = { r.getCenterY(), r.getMinY(), r.getCenterY(), r.getMaxY() };
		DisplayTools.drawPath(g2, xs, ys, true, false);
	}

	private static void demoDrawCube(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawCube(g2, shrink(r, 15), 12, 12, Color.LIGHT_GRAY, new BasicStroke(1.5f), Color.DARK_GRAY);
	}

	private static void demoDrawRoundRect(Graphics2D g2, Rectangle2D r) {
		Rectangle2D shrunk = shrink(r, 15);
		DisplayTools.drawRoundRect(g2, (int) shrunk.getX(), (int) shrunk.getY(), (int) shrunk.getWidth(),
				(int) shrunk.getHeight(), 14, 14, Color.LIGHT_GRAY, true, new BasicStroke(1.5f));
	}

	private static void demoDrawButton(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawButton(g2, (int) r.getCenterX(), (int) r.getCenterY(), (int) (r.getWidth() * 0.4));
	}

	private static void demoDrawRotatedString(Graphics2D g2, Rectangle2D r) {
		g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
		for (int i = 0; i < 4; i++)
			DisplayTools.drawRotatedString(g2, "text", (float) r.getCenterX(), (float) r.getCenterY(),
					Math.toRadians(i * 45));
	}

	private static void demoCreateDiamond(Graphics2D g2, Rectangle2D r) {
		Path2D.Float diamond = DisplayTools.createDiamond((float) r.getCenterX(), (float) r.getCenterY(),
				(float) r.getWidth() - 10, (float) r.getHeight() - 10);
		g2.fill(diamond);
	}

	private static void demoDrawArrow(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawArrow(g2, (float) r.getCenterX(), (float) r.getCenterY(), (float) r.getWidth() * 0.7f, 0.5f,
				0f);
		DisplayTools.drawArrow(g2, (float) r.getCenterX(), (float) r.getCenterY(), (float) r.getWidth() * 0.7f, 0.5f,
				90f);
	}

	private static void demoDrawCurvedArrow(Graphics2D g2, Rectangle2D r) {
		DisplayTools.drawCurvedArrow(g2, (float) r.getMinX(), (float) r.getMinY(), (float) r.getMaxX(),
				(float) r.getMaxY(), 0.3f, 12f, 0.5f, null);
	}

	private static Rectangle2D shrink(Rectangle2D r, double inset) {
		return new Rectangle2D.Double(r.getX() + inset, r.getY() + inset, r.getWidth() - 2 * inset,
				r.getHeight() - 2 * inset);
	}

}
