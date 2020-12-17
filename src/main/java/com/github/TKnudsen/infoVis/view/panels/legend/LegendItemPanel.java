package com.github.TKnudsen.infoVis.view.panels.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;

public class LegendItemPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String label;
	private final Color textColor;
	private final Color color;
	private final boolean colorRectangleLeft;

	private final String toolTipString;

	public LegendItemPanel(String label, Color textColor, Color color) {
		this(label, textColor, color, label, true);
	}

	public LegendItemPanel(String label, Color textColor, Color color, String toolTipString,
			boolean colorRectangleLeft) {
		this.label = label;
		this.textColor = textColor;
		this.color = color;
		this.toolTipString = toolTipString;
		this.colorRectangleLeft = colorRectangleLeft;

		this.setLayout(new GridLayout(1, 1));

		initialize();
	}

	private void initialize() {
		RectanglePainter rectanglePainter = new RectanglePainter();
		rectanglePainter.setBackgroundPaint(null);
		rectanglePainter.setPaint(getColor());
		InfoVisChartPanel rectangle = new InfoVisChartPanel(rectanglePainter);
		QuadraticPanel q = new QuadraticPanel(rectangle);

		JLabel jLabel = new JLabel(label);
		jLabel.setToolTipText(toolTipString);
		jLabel.setForeground(textColor);

		JPanel legendSingle = new JPanel(new BorderLayout());
		legendSingle.add(q, (colorRectangleLeft) ? BorderLayout.WEST : BorderLayout.EAST);
		legendSingle.add(jLabel);

		add(legendSingle);
	}

	public boolean isColorRectangleLeft() {
		return colorRectangleLeft;
	}

	public String getLabel() {
		return label;
	}

	public Color getColor() {
		return color;
	}

}
