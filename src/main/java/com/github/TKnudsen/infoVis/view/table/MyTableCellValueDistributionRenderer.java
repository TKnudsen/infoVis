package com.github.TKnudsen.infoVis.view.table;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTable;
import javax.swing.UIManager;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalPainters;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChart;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarCharts;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

public class MyTableCellValueDistributionRenderer extends MyTableCellDefaultRenderer {

	private Color darkBackgroundColor = Color.DARK_GRAY;

	private Color dataEncodingColor = new Color(51, 98, 140);

	private DistributionGlyph distributionGlyph = DistributionGlyph.Barchart;

	public enum DistributionGlyph {
		Barchart, Boxplot, Distribution1D
	}

	public MyTableCellValueDistributionRenderer(boolean encodeNumbersWithRectangleSize,
			boolean rectangleSizeEncodingOnly, boolean enableToolTipping) {
		super(encodeNumbersWithRectangleSize, rectangleSizeEncodingOnly, enableToolTipping);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2541734523862096033L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component component = null;

		if (value instanceof Collection<?> && !((Collection<?>) value).isEmpty()) {

			ChartPainter chartPainter = null;
			// this bar chart painter is needed because the info vis bar chart causes
			// problems with the position encoding that comes from the surrounding bar chart
			// panel. this, however, does not re-scale painters when paintComponent
			switch (distributionGlyph) {
			case Barchart:
				@SuppressWarnings("unchecked")
				BarChart barchart = BarCharts.createHistogramBarchart((Collection<Number>) value, 10, Color.GRAY);
//				barchart.setLineColor(Color.DARK_GRAY.darker());
				barchart.setDrawYAxis(false);
				chartPainter = barchart.getChartPainters().get(0);
				break;
			case Boxplot:
				@SuppressWarnings("unchecked")
				StatisticsSupport statistics = new StatisticsSupport((Collection<Number>) value);
				BoxPlotHorizontalPainter painter = new BoxPlotHorizontalPainter(statistics);
				painter.setBackgroundPaint(null);
				painter.setFillAlpha(0.75f);
				if (!isSelected)
					painter.setPaint((Color) UIManager.getLookAndFeelDefaults().get("TableHeader.foreground"));
				else
					painter.setPaint(table.getSelectionForeground());
				if (!isSelected)
					painter.setBorderPaint((Color) UIManager.getLookAndFeelDefaults().get("TableHeader.foreground"));
				else
					painter.setBorderPaint(table.getSelectionForeground());

				chartPainter = painter;
				break;
			case Distribution1D:
				@SuppressWarnings("unchecked")
				List<Double> data = new ArrayList<Double>(((Collection<Double>) value));
				Distribution1DHorizontalPainter<Double> dp = Distribution1DHorizontalPainters
						.createHorizontalForDoubles(data);
				dp.setColorEncodingFunction(new ConstantColorEncodingFunction<>(Color.WHITE));
				dp.setDrawOutline(true);

				chartPainter = dp;
				break;

			default:
				break;
			}

			// component resized event is consumed in the table.
			// a Panel is needed that sets rectangle size just before painting
			MyTableCellPainterPanel panel = new MyTableCellPainterPanel();
			panel.addPainter(chartPainter);

			MyTableCellRendererTools.manageComponentColors(table, panel, ui, row, column, isSelected, hasFocus);

			component = panel;
		} else
			// return super.getTableCellRendererComponent(table, value, isSelected,
			// hasFocus, row, column);
			component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		return component;
	}

	public Color getBackgroundColor() {
		return darkBackgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.darkBackgroundColor = backgroundColor;
	}

	public Color getDataEncodingColor() {
		return dataEncodingColor;
	}

	public void setDataEncodingColor(Color dataEncodingColor) {
		this.dataEncodingColor = dataEncodingColor;
	}

	public DistributionGlyph getDistributionGlyph() {
		return distributionGlyph;
	}

	public void setDistributionGlyph(DistributionGlyph distributionGlyph) {
		this.distributionGlyph = distributionGlyph;
	}

}