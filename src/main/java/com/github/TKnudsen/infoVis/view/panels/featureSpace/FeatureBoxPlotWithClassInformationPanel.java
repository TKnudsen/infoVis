package com.github.TKnudsen.infoVis.view.panels.featureSpace;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DVerticalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DVerticalPainters;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotVerticalChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;

/**
 * A vertical box plot for one feature's value distribution, with per-class
 * cluster markers overlaid and a disabled/highlighted overlay state for use
 * inside a {@link FeatureSpaceView}.
 *
 * @author Christian Ritter, Juergen Bernard
 * @version 1.0
 */
public class FeatureBoxPlotWithClassInformationPanel extends BoxPlotVerticalChartPanel {

	private static final long serialVersionUID = -4010807870281817684L;

	private Distribution1DVerticalPainter<Double> distribution1DVerticalPainter;

	private List<Double> detailWorldValues;

	private List<ShapeAttributes> detailedShapeAttributes;

	// required to create detailed version of this panel
	private final List<Double> saveddata;
	private final double savedminGlobal;
	private final double savedmaxGlobal;
	private String name = "";

	private boolean highlighted = false;
	private boolean disabled = false;

	private final Color standardBackgroundColor;
	private final Color standardFontColor;

	public FeatureBoxPlotWithClassInformationPanel(List<Double> data, double minGlobal, double maxGlobal,
			Color standardBackgroundColor, Color standardFontColor) {
		super(data, minGlobal, maxGlobal);
		saveddata = data;
		savedminGlobal = minGlobal;
		savedmaxGlobal = maxGlobal;

		this.standardBackgroundColor = standardBackgroundColor;
		this.standardFontColor = standardFontColor;
		this.setColor(Color.GRAY);
		this.setBackground(null);

		initializeDistibution1DPainter(data);
	}

	private void initializeDistibution1DPainter(List<Double> data) {

		detailWorldValues = new ArrayList<>();
		detailedShapeAttributes = new ArrayList<>();

		distribution1DVerticalPainter = Distribution1DVerticalPainters.createVerticalForDoubles(data);
		distribution1DVerticalPainter.setBackgroundPaint(null);

		addChartPainter(distribution1DVerticalPainter, true);
	}

	public void addCluster(Double worldValue, ShapeAttributes shapeAttributes) {

		detailWorldValues.add(worldValue);
		detailedShapeAttributes.add(shapeAttributes);
		this.distribution1DVerticalPainter.addSpecialValue(worldValue, shapeAttributes);
	}

	public void addDetailElements(List<Double> worldValues, List<ShapeAttributes> shapeAttributes) {
	}

	public void setName(String name) {
		this.name = name;
	}

	// creates a more detailed version of itself
	public JPanel getDetailedPanel() {
		FeatureBoxPlotWithClassInformationPanel bp = new FeatureBoxPlotWithClassInformationPanel(saveddata,
				savedminGlobal, savedmaxGlobal, standardBackgroundColor, standardFontColor);
		for (int i = 0; i < detailWorldValues.size(); i++)
			bp.addCluster(detailWorldValues.get(i), detailedShapeAttributes.get(i));
		bp.setBackground(standardBackgroundColor);
		bp.setDrawYAxis(false);
		bp.setShowingTooltips(false);
		// align with others by adding an invisible checkbox
		JPanel dummypanel = new JPanel();
		dummypanel.setLayout(new BorderLayout());
		dummypanel.add(bp, BorderLayout.CENTER);
		dummypanel.add(new JLabel(" "), BorderLayout.SOUTH);
		JPanel res = new JPanel();
		res.setLayout(new BorderLayout());
		res.add(dummypanel, BorderLayout.CENTER);
		res.add(new JLabel(name), BorderLayout.SOUTH);
		res.setMinimumSize(new Dimension(100, 10));
		res.setPreferredSize(new Dimension(100, 300));
		res.setMaximumSize(new Dimension(100, 3000));
		Border b = BorderFactory.createLineBorder(Color.BLACK, 1);
		res.setBorder(b);
		return res;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color c = g.getColor();

		Rectangle bounds = this.getBounds();

		// if disabled overlay with gray rectangle
		if (disabled) {
			Color bg = (standardBackgroundColor != null) ? ColorTools.setAlpha(standardBackgroundColor.brighter(), 0.7f)
					: ColorTools.setAlpha(Color.GRAY, 0.5f);
			g.setColor(bg);
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		// if highlighted print light border
		if (highlighted) {
			Graphics2D g2 = (Graphics2D) g;
			Stroke s = g2.getStroke();
			g2.setStroke(new BasicStroke(3f));
			g2.setColor(ColorTools.setAlpha(standardFontColor, 0.5f));
			g2.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
			g2.setStroke(s);
		}

		g.setColor(c);
	}

	public void setHighlighted(boolean b) {
		this.highlighted = b;
		this.repaint();
	}

	public void setDisabled(boolean b) {
		this.disabled = b;
		this.repaint();
	}
}
