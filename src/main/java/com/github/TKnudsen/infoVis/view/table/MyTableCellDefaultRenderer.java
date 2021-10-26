package com.github.TKnudsen.infoVis.view.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.donutchart.DonutChartPainter;
import com.github.TKnudsen.infoVis.view.painters.number.ScorePainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.CirclePainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.ui.Orientation;

public class MyTableCellDefaultRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8549890298778331359L;

	private int decimalPlaces = 2;

	private Color myDataEncodingColor = new Color(51, 98, 140);
	private Function<Integer, Color> colorFunctionFromRow;

	private Color darkBackgroundColor = NimbusUITools.standardBackgroundColor;

	private boolean encodeNumbersWithRectangleSize = true;
	private BiFunction<Double, Color, ChartPainter> sizeEncodingChartPanelFunction;
	private boolean rectangleSizeEncodingOnly = false;

	protected boolean enableToolTipping = true;

	protected List<String> ignoreRectangleEncodigForAttributes = new ArrayList<>();

	public enum Glyph {
		Rectangle, Dot, Donut, HorizontalBar, VerticalBar
	}

	public enum GlyphPlacement {
		Left, Right, Replace
	}

	private Glyph glyphType = Glyph.HorizontalBar;

	public MyTableCellDefaultRenderer(boolean encodeNumbersWithRectangleSize, boolean rectangleSizeEncodingOnly,
			boolean enableToolTipping) {
		this.encodeNumbersWithRectangleSize = encodeNumbersWithRectangleSize;
		this.setRectangleSizeEncodingOnly(rectangleSizeEncodingOnly);
		this.enableToolTipping = enableToolTipping;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (value instanceof Number)
			if (!(value instanceof Integer))
				value = MathFunctions.round(((Number) value).doubleValue(), decimalPlaces);

		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// handle colors
		if (component instanceof JComponent) {
			JComponent jComponent = (JComponent) component;
			MyTableCellRendererTools.manageComponentColors(table, jComponent, ui, row, column, isSelected, hasFocus);
		} else if (row % 2 == 0)
			component.setBackground(darkBackgroundColor.brighter().brighter());
		else
			component.setBackground(darkBackgroundColor.brighter());

		// conditional formatting
		Object headerValue = table.getColumnModel().getColumn(column).getHeaderValue();
		if (!ignoreRectangleEncodigForAttributes.contains(headerValue))
			if (encodeNumbersWithRectangleSize && table.getModel() instanceof RelativeCellValueProvider)
				if (value instanceof Number)
					if (isRectangleSizeEncodingOnly())
						component = setRectangleSizeEncoding(table, component, row, column, GlyphPlacement.Replace);
					else
						component = setRectangleSizeEncoding(table, component, row, column, GlyphPlacement.Left);

		// tool tipping
		if (enableToolTipping)
			if (value != null)
				attachToolTip(component, value);

		return component;
	}

	/**
	 * 
	 * @param table
	 * @param component
	 * @param row
	 * @param column
	 * @param extendComponent whether or not the component receives an additional
	 *                        size encoding. otherwise it is replaced.
	 * @return
	 */
	protected Component setRectangleSizeEncoding(JTable table, Component component, int row, int column,
			GlyphPlacement glyphPlacement) {
		if (!(table.getModel() instanceof RelativeCellValueProvider))
			return component;

		RelativeCellValueProvider model = (RelativeCellValueProvider) table.getModel();

		// convert indices to model indices:
		column = table.convertColumnIndexToModel(column);
		row = table.convertRowIndexToModel(row);

		double relativeSize = model.getRelativeValueAt(row, column, false);
		if (Double.isNaN(relativeSize))
			return component;

		Color dataColor = myDataEncodingColor;
		if (colorFunctionFromRow != null)
			dataColor = colorFunctionFromRow.apply(row);

		JPanel cell = new JPanel(new BorderLayout());
		cell.setBackground(component.getBackground());

		JPanel panel = createRectangleSizeEncodingJPanel(relativeSize, dataColor);
		panel.setBackground(component.getBackground());

		switch (glyphPlacement) {
		case Left:
			cell.add(component, BorderLayout.CENTER);
			cell.add(panel, BorderLayout.WEST);
			break;
		case Right:
			cell.add(component, BorderLayout.CENTER);
			cell.add(panel, BorderLayout.EAST);
			break;
		case Replace:
			cell.add(panel, BorderLayout.CENTER);
			break;
		default:
			cell.add(panel, BorderLayout.CENTER);
		}

		return cell;
	}

	/**
	 * 
	 * @param relativeSize
	 * @return
	 */
	protected JPanel createRectangleSizeEncodingJPanel(double relativeSize, Color dataColor) {

		JPanel panel = new JPanel(new GridLayout(1, 1));

		Color unselectedForeground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.foreground");
		Color unselectedBackground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.background");

		JPanel chartPanel = null;
		if (sizeEncodingChartPanelFunction != null) {
			ChartPainter chart = sizeEncodingChartPanelFunction.apply(relativeSize, dataColor);
			chartPanel = new TableCellChartPanel(chart);

			panel.setPreferredSize(new Dimension(24, 16));
		} else {
			switch (glyphType) {
			case Rectangle:
				RectanglePainter rectanglePainter = new RectanglePainter();
				rectanglePainter.setDrawOutline(true);
				rectanglePainter.setBackgroundPaint(null);
				rectanglePainter.setPaint(dataColor);

				chartPanel = new SizeEncodingChartPanel(rectanglePainter, relativeSize);

				panel.setPreferredSize(new Dimension(16, 16));
				break;
			case Dot:
				CirclePainter circlePainter = new CirclePainter();
				circlePainter.setDrawOutline(false);
				circlePainter.setBackgroundPaint(null);

				if (relativeSize >= 0)
					circlePainter.setPaint(dataColor);// unselectedForeground);
				else
					circlePainter.setPaint(unselectedBackground);

				chartPanel = new SizeEncodingChartPanel(circlePainter, Math.abs(relativeSize));

				panel.setPreferredSize(new Dimension(16, 16));
				break;
			case Donut:
				// data
				List<Double> fractions = new ArrayList<Double>();
				List<Color> colors = new ArrayList<>();
				fractions.add(relativeSize);
				colors.add(dataColor);

				fractions.add(1.0 - relativeSize);
				colors.add(ColorTools.setAlpha(unselectedForeground, 0.1f));

				// painter and panel
				DonutChartPainter painter = new DonutChartPainter(fractions, colors);
				painter.setDonutRadiusRelative(0.39);
				painter.setBackgroundPaint(null);
				painter.setDrawOutline(true);
				chartPanel = new TableCellChartPanel(painter);

				panel.setPreferredSize(new Dimension(16, 16));

				break;
			case HorizontalBar:
				ScorePainter scorePainterH = new ScorePainter(relativeSize, dataColor, Orientation.HORIZONTAL);
				scorePainterH.setBackgroundPaint(null);
				scorePainterH.setDrawOutline(true);
				chartPanel = new TableCellChartPanel(scorePainterH);

				panel.setPreferredSize(new Dimension(24, 16));

				break;
			case VerticalBar:
				ScorePainter scorePainterV = new ScorePainter(relativeSize, dataColor, Orientation.VERTICAL);
				scorePainterV.setBackgroundPaint(null);
				scorePainterV.setDrawOutline(true);
				chartPanel = new TableCellChartPanel(scorePainterV);

				panel.setPreferredSize(new Dimension(16, 16));

				break;
			default:
				chartPanel = new TableCellChartPanel(new StringPainter(" "));
				break;
			}

		}

		chartPanel.setBackground(null);
		panel.add(chartPanel);

		return panel;
	}

	protected void attachToolTip(Component component, Object toolTipValue) {
		if (component instanceof JComponent) {
			String toolTipText = "";

			if (toolTipValue == null)
				return;
			else if (toolTipValue instanceof Number)
				toolTipText = String.valueOf(MathFunctions.round(((Number) toolTipValue).doubleValue(), decimalPlaces));
			else
				toolTipText = toolTipValue.toString();

			((JComponent) component).setToolTipText(toolTipText);
		}
	}

	public void addAttributeToBeIgnoredForRectangleEncodig(String attribute) {
		ignoreRectangleEncodigForAttributes.add(attribute);
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public Color getMyDataEncodingColor() {
		return myDataEncodingColor;
	}

	public void setMyDataEncodingColor(Color myDataEncodingColor) {
		this.myDataEncodingColor = myDataEncodingColor;
	}

	public Color getDarkBackgroundColor() {
		return darkBackgroundColor;
	}

	public void setDarkBackgroundColor(Color darkBackgroundColor) {
		this.darkBackgroundColor = darkBackgroundColor;
	}

	public Glyph getGlyphType() {
		return glyphType;
	}

	public void setGlyphType(Glyph glyphType) {
		this.glyphType = glyphType;
	}

	public Function<Integer, Color> getColorFunctionFromRow() {
		return colorFunctionFromRow;
	}

	public void setColorFunctionFromRow(Function<Integer, Color> colorFunctionFromRow) {
		this.colorFunctionFromRow = colorFunctionFromRow;
	}

	public boolean isEncodeNumbersWithRectangleSize() {
		return encodeNumbersWithRectangleSize;
	}

	public void setEncodeNumbersWithRectangleSize(boolean encodeNumbersWithRectangleSize) {
		this.encodeNumbersWithRectangleSize = encodeNumbersWithRectangleSize;
	}

	public boolean isRectangleSizeEncodingOnly() {
		return rectangleSizeEncodingOnly;
	}

	public void setRectangleSizeEncodingOnly(boolean rectangleSizeEncodingOnly) {
		this.rectangleSizeEncodingOnly = rectangleSizeEncodingOnly;
	}

	public BiFunction<Double, Color, ChartPainter> getSizeEncodingChartPanelFunction() {
		return sizeEncodingChartPanelFunction;
	}

	public void setSizeEncodingChartPanelFunction(
			BiFunction<Double, Color, ChartPainter> sizeEncodingChartPanelFunction) {
		this.sizeEncodingChartPanelFunction = sizeEncodingChartPanelFunction;
	}

}