package com.github.TKnudsen.infoVis.view.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
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
import com.github.TKnudsen.infoVis.view.painters.bufferedImage.BufferedImagePainter;
import com.github.TKnudsen.infoVis.view.painters.donutchart.DonutChartPainter;
import com.github.TKnudsen.infoVis.view.painters.number.ScorePainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.CirclePainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.ui.Orientation;

/**
 * <p>
 * Table cell renderer that formats numeric values and, when the model
 * implements {@link RelativeCellValueProvider}, augments or replaces the
 * cell with a size/color-encoded glyph (rectangle, heat map, dot, donut, bar,
 * or bipolar rectangle).
 * </p>
 *
 * @version 1.0
 */
public class MyTableCellDefaultRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8549890298778331359L;

	private int decimalPlaces = 2;

	private Color myDataEncodingColor = new Color(51, 98, 140);
	private Function<Integer, Color> colorFunctionFromRow;
	private Function<Double, Color> valueToColorEncodingFunction;

	/**
	 * Fill color for NaN/infinite cells -- a flat, neutral, unmistakably
	 * "not part of the value scale" gray, distinct from anything
	 * {@link #valueToColorEncodingFunction} would ever produce for a real value.
	 */
	private Color missingValueColor = new Color(80, 80, 80);

	private Color darkBackgroundColor = NimbusUITools.standardBackgroundColor;

	private boolean encodeNumbersWithRectangleSize = true;
	private BiFunction<Double, Color, ChartPainter> sizeEncodingChartPanelFunction;
	private boolean rectangleSizeEncodingOnly = false;

	protected boolean enableToolTipping = true;

	protected List<String> ignoreRectangleEncodigForAttributes = new ArrayList<>();

	public enum Glyph {
		Rectangle, HeatMap, Dot, Donut, HorizontalBar, VerticalBar, Bipolar
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
				else {
				}
			else {
			}
		else {
			if (value != null && value instanceof BufferedImage) {
				JPanel cell = new JPanel(new BorderLayout());
				cell.setBackground(component.getBackground());

				JPanel panel = new JPanel(new GridLayout(1, 1));
				panel.setBackground(component.getBackground());

				TableCellChartPanel tccp = new TableCellChartPanel(
						new BufferedImagePainter((BufferedImage) value, true));
				tccp.setBackground(null);
				panel.add(tccp);

				component = panel;
			}
		}

		// tool tipping
		if (enableToolTipping)
			if (value != null)
				attachToolTip(component, value);

		return component;
	}

	/**
	 * 
	 * @param table          table
	 * @param component      component
	 * @param row            row
	 * @param column         column
	 * @param glyphPlacement whether or not the component receives an additional
	 *                       size encoding. otherwise it is replaced.
	 * @return the component
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

		// relativeSize is NaN for two unrelated reasons: (a) this column doesn't
		// participate in relative/glyph encoding at all -- true for every plain
		// numeric column (Min/Max/Median/Mean/...), which RelativeCellValueProvider
		// implementations commonly signal by returning NaN unconditionally for any
		// non-glyph column (see AttributesOverviewTableModel.getRelativeValueAt) --
		// or (b) this glyph column's specific cell value is genuinely NaN/infinite.
		// Only (b) should get the missing-value swatch below; conflating the two
		// (checking relativeSize alone) drew that swatch next to every ordinary
		// number in every non-glyph column, since relativeSize is NaN there
		// regardless of how valid the real value is. Checking the raw model value
		// directly is what tells the two cases apart.
		Object rawValue = table.getModel().getValueAt(row, column);
		boolean cellValueMissing = rawValue instanceof Number && (Double.isNaN(((Number) rawValue).doubleValue())
				|| Double.isInfinite(((Number) rawValue).doubleValue()));

		if (Double.isNaN(relativeSize) && !cellValueMissing)
			return component;

		JPanel cell = new JPanel(new BorderLayout());
		cell.setBackground(component.getBackground());

		JPanel panel;
		if (cellValueMissing) {
			// Was: return component unchanged, i.e. fall back to the raw
			// DefaultTableCellRenderer text ("NaN") on a plain background --
			// jarring next to the colored glyphs everywhere else in the column.
			// A same-size, same-shape swatch in a fixed neutral color keeps the
			// grid's visual rhythm intact instead. The literal value is still
			// available via the tooltip (attachToolTip), so nothing is lost.
			panel = createMissingValueJPanel();
		} else {
			Color dataColor = myDataEncodingColor;
			if (colorFunctionFromRow != null)
				dataColor = colorFunctionFromRow.apply(row);
			else if (valueToColorEncodingFunction != null)
				try {
					dataColor = valueToColorEncodingFunction.apply(relativeSize);
				} catch (Exception e) {
					e.printStackTrace();
				}

			panel = createRectangleSizeEncodingJPanel(relativeSize, dataColor);
		}
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
	 * @param relativeSize relative size
	 * @param dataColor    the color
	 * @return the panel
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
				rectanglePainter.setBackgroundPaint(Color.MAGENTA);
				rectanglePainter.setPaint(dataColor);

				chartPanel = new SizeEncodingChartPanel(rectanglePainter, relativeSize);

				panel.setPreferredSize(new Dimension(16, 16));
				break;
			case HeatMap:
				RectanglePainter heatMapCellPainter = new RectanglePainter();
				heatMapCellPainter.setDrawOutline(true);
				heatMapCellPainter.setPaint(dataColor);

				chartPanel = new TableCellChartPanel(heatMapCellPainter);

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
			case Bipolar:
				RectanglePainter bipolarRectangle = new RectanglePainter();
				bipolarRectangle.setDrawOutline(true);
				bipolarRectangle.setBackgroundPaint(null);
				bipolarRectangle.setPaint(new Color(59, 197, 85));

				if (relativeSize >= 0.7)
					chartPanel = new SizeEncodingChartPanel(bipolarRectangle, relativeSize);
				else if (relativeSize <= 0.3) {
					chartPanel = new SizeEncodingChartPanel(bipolarRectangle, 1 - relativeSize);
					bipolarRectangle.setPaint(new Color(181, 75, 160));
				} else {
					chartPanel = new SizeEncodingChartPanel(bipolarRectangle, 0.1);
					bipolarRectangle.setPaint(Color.GRAY);
				}
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

	/**
	 * Same size/shape as a normal glyph cell (see the {@code HeatMap} case in
	 * {@link #createRectangleSizeEncodingJPanel(double, Color)}), but always a
	 * flat {@link #missingValueColor} rather than anything derived from a value
	 * -- there is no meaningful size or hue to encode for NaN/infinite data.
	 */
	protected JPanel createMissingValueJPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 1));

		RectanglePainter rectanglePainter = new RectanglePainter();
		rectanglePainter.setDrawOutline(true);
		rectanglePainter.setPaint(missingValueColor);

		TableCellChartPanel chartPanel = new TableCellChartPanel(rectanglePainter);
		chartPanel.setBackground(null);
		panel.add(chartPanel);

		panel.setPreferredSize(new Dimension(16, 16));

		return panel;
	}

	protected void attachToolTip(Component component, Object toolTipValue) {
		if (component instanceof JComponent) {
			String toolTipText = "";

			if (toolTipValue == null)
				return;
			else if (toolTipValue instanceof Number)
				toolTipText = String.valueOf(MathFunctions.round(((Number) toolTipValue).doubleValue(), decimalPlaces));
			else if (toolTipValue instanceof BufferedImage)
				toolTipText = "Icon: w=" + ((BufferedImage) toolTipValue).getWidth() + ", h="
						+ ((BufferedImage) toolTipValue).getHeight();
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

	public Function<Double, Color> getValueToColorEncodingFunction() {
		return valueToColorEncodingFunction;
	}

	public void setValueToColorEncodingFunction(Function<Double, Color> valueToColorEncodingFunction) {
		this.valueToColorEncodingFunction = valueToColorEncodingFunction;
	}

	public Color getMissingValueColor() {
		return missingValueColor;
	}

	public void setMissingValueColor(Color missingValueColor) {
		this.missingValueColor = missingValueColor;
	}

}