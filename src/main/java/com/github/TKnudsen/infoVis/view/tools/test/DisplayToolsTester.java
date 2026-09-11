package com.github.TKnudsen.infoVis.view.tools.test;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Interactive demo showing what every drawing method of {@link DisplayTools}
 * draws, one grid cell per method.
 *
 * @version 1.0
 * @since 2026
 */
public class DisplayToolsTester {

	public static void main(String[] args) {
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

		List<JPanel> panels = new ArrayList<>();
		for (Map.Entry<String, BiConsumer<Graphics2D, Rectangle2D>> entry : demos.entrySet())
			panels.add(labeled(entry.getKey(), entry.getValue()));

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFramePanelMatrix(panels, "DisplayTools methods"));
	}

	private static JPanel labeled(String title, BiConsumer<Graphics2D, Rectangle2D> demo) {
		JPanel wrapper = new JPanel(new BorderLayout());

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		wrapper.add(label, BorderLayout.NORTH);

		JPanel canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.DARK_GRAY);
				g2.setStroke(new BasicStroke(1.5f));

				int padding = 12;
				Rectangle2D bounds = new Rectangle2D.Double(padding, padding, getWidth() - 2 * padding,
						getHeight() - 2 * padding);
				demo.accept(g2, bounds);
			}
		};
		canvas.setPreferredSize(new Dimension(160, 160));
		wrapper.add(canvas, BorderLayout.CENTER);

		return wrapper;
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
		float headLength = (float) r.getWidth() * 0.35f;
		float shaftLength = headLength * 1.6f;

		DisplayTools.drawArrow(g2, (float) r.getMinX() + headLength, (float) r.getMinY() + headLength, headLength,
				0.35f, 0f, shaftLength);
		DisplayTools.drawArrow(g2, (float) r.getMaxX() - headLength, (float) r.getMaxY() - headLength, headLength,
				0.35f, 180f, shaftLength);
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
