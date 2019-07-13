package com.github.TKnudsen.infoVis.view.ui.slider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018 Christian Ritter, Juergen Bernard,
 * https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public class SliderUIColored extends BasicSliderUI {

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
		g2d.setStroke(new BasicStroke((float) thumbRect.getWidth() / 2));
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
		Graphics2D g2d = (Graphics2D) g;

		float f = slider.getValue() / (float) (slider.getMaximum());

		// create the shape of the slider
		int x1 = thumbRect.x + 2;
		int x2 = thumbRect.x + thumbRect.width - 2;
		int topY = thumbRect.y + thumbRect.height / 2 - thumbRect.width / 3;
		int bottomY = thumbRect.y + thumbRect.height - 3;
		GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		shape.moveTo(x1, topY - 1); // oben links
		shape.lineTo(x2, topY - 1); // oben rechts
		shape.lineTo(x2 + 1, topY); // oben rechts
		shape.lineTo(x2 + 1, bottomY); // unten rechts
		shape.lineTo(x2, bottomY + 1); // unten rechts
		shape.lineTo(x1, bottomY + 1); // unten links
		shape.lineTo(x1 - 1, bottomY); // unten links eins eingerueckt
		shape.lineTo(x1 - 1, topY); // oben links eins eingerueckt
		shape.closePath();
		// get color
		g2d.setPaint(colorMapfunction.apply(f));

		// fill shape
		g2d.fill(shape);

		// draw shape with the color
		Stroke old = g2d.getStroke();
		g2d.setStroke(new BasicStroke(2f));
		g2d.setPaint(colorMapfunction.apply(f).darker());
		g2d.draw(shape);
		g2d.setStroke(old);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(25);
		slider.setUI(new SliderUIColored(slider, new Function<Float, Color>() {

			@Override
			public Color apply(Float t) {
				t = Math.max(0, Math.min(1, t));
				return new Color(0, t, t);
			}
		}));

		frame.add(slider);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private class ChangeHandler implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			calculateThumbLocation();
			slider.repaint();
		}
	}

}
