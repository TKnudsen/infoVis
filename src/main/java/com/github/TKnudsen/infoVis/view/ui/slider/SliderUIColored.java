package com.github.TKnudsen.infoVis.view.ui.slider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import com.github.TKnudsen.infoVis.view.tools.BasicStrokes;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2024 Christian Ritter, Juergen Bernard,
 * https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
 */
public class SliderUIColored extends BasicSliderUI {

	private boolean flipThumb = false;

	private final Function<Float, Color> colorMapfunction;

	public SliderUIColored(JSlider b, Function<Float, Color> colorMapfunction) {
		super(b);

		this.colorMapfunction = colorMapfunction;
	}

	/**
	 * Creates a listener to handle change events in the specified slider.
	 */
	@Override
	protected ChangeListener createChangeListener(JSlider slider) {
		return new ChangeHandler();
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g, c);
	}

	@Override
	protected Dimension getThumbSize() {
		return new Dimension(16, 16);
	}

	@Override
	public void paintTrack(Graphics g) {
		super.paintTrack(g);
		Graphics2D g2d = (Graphics2D) g;
		Color old = g2d.getColor();
		Stroke oldStroke = g2d.getStroke();
		float f = slider.getValue() / (float) (slider.getMaximum());
		g2d.setPaint(colorMapfunction.apply(f));
		g2d.setStroke(BasicStrokes.get((float) thumbRect.getWidth() / 2));
		if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
			g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2, trackRect.x + trackRect.width,
					trackRect.y + trackRect.height / 2);
		} else {
			g2d.drawLine(trackRect.x + trackRect.width / 2, thumbRect.y + thumbRect.height,
					trackRect.x + trackRect.width / 2, trackRect.y + trackRect.height);
			g2d.setColor(Color.white);
			g2d.drawLine(trackRect.x + trackRect.width / 2, trackRect.y, trackRect.x + trackRect.width / 2,
					thumbRect.y);
		}
		g2d.setStroke(oldStroke);
		g2d.setColor(old);
	}

	@Override
	public void paintThumb(Graphics g) {
//		Graphics2D g2d = (Graphics2D) g;
//
//		float f = slider.getValue() / (float) (slider.getMaximum());
//
//		// create the shape of the slider
//		int x1 = thumbRect.x + 2;
//		int x2 = thumbRect.x + thumbRect.width - 2;
//		int topY = thumbRect.y + thumbRect.height / 2 - thumbRect.width / 3;
//		int bottomY = thumbRect.y + thumbRect.height - 3;
//		GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
//		shape.moveTo(x1, topY - 1); // oben links
//		shape.lineTo(x2, topY - 1); // oben rechts
//		shape.lineTo(x2 + 1, topY); // oben rechts
//		shape.lineTo(x2 + 1, bottomY); // unten rechts
//		shape.lineTo(x2, bottomY + 1); // unten rechts
//		shape.lineTo(x1, bottomY + 1); // unten links
//		shape.lineTo(x1 - 1, bottomY); // unten links eins eingerueckt
//		shape.lineTo(x1 - 1, topY); // oben links eins eingerueckt
//		shape.closePath();
//		// get color
//		g2d.setPaint(colorMapfunction.apply(f));
//
//		// fill shape
//		g2d.fill(shape);
//
//		// draw shape with the color
//		Stroke old = g2d.getStroke();
//		g2d.setStroke(BasicStrokes.get(2f));
//		g2d.setPaint(colorMapfunction.apply(f).darker());
//		g2d.draw(shape);
//		g2d.setStroke(old);

		Rectangle knobBounds = thumbRect;
		int w = knobBounds.width;
		int h = knobBounds.height;

		// Create graphics copy.
		Graphics2D g2d = (Graphics2D) g.create();

		// Create thumb shape.
		Shape thumbShape = (slider.getOrientation() == JSlider.VERTICAL)
				? Sliders.createThumbShapeVertical(w - 1, h - 1, isFlipThumb())
				: Sliders.createThumbShapeHorizontal(w - 1, h - 1, isFlipThumb());

		// Draw thumb.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(knobBounds.x, knobBounds.y);

		g2d.setColor((Color) UIManager.get("Slider.foreground"));
		g2d.fill(thumbShape);

		g2d.setColor(Color.DARK_GRAY);
		g2d.draw(thumbShape);

		// Dispose graphics.
		g2d.dispose();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(2, 0));
		JSlider vertical = new JSlider(JSlider.VERTICAL, 0, 100, 50);
		vertical.setPaintTicks(true);
		vertical.setPaintLabels(true);
		vertical.setMinorTickSpacing(5);
		vertical.setMajorTickSpacing(25);
		vertical.setUI(new SliderUIColored(vertical, new Function<Float, Color>() {

			@Override
			public Color apply(Float t) {
				t = Math.max(0, Math.min(1, t));
				return new Color(0, t, t);
			}
		}));

		frame.add(vertical);

		JSlider horizontal = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		horizontal.setPaintTicks(true);
		horizontal.setPaintLabels(true);
		horizontal.setMinorTickSpacing(5);
		horizontal.setMajorTickSpacing(25);
		horizontal.setUI(new SliderUIColored(horizontal, new Function<Float, Color>() {

			@Override
			public Color apply(Float t) {
				t = Math.max(0, Math.min(1, t));
				return new Color(0, t, t);
			}
		}));

		frame.add(horizontal);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public boolean isFlipThumb() {
		return flipThumb;
	}

	public void setFlipThumb(boolean flipThumb) {
		this.flipThumb = flipThumb;
	}

	private class ChangeHandler implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			calculateThumbLocation();
			slider.repaint();
		}
	}

}
