package com.github.TKnudsen.infoVis.data.table;

import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.table.MyTableCellDefaultRenderer.Glyph;

public class NumericalItemTableColumnData extends ItemTableColumnData {

	private final Glyph glyph;
	private final Color constantColor;
	private boolean rectangleSizeEncodingOnly;
	private final BiFunction<Double, Color, ChartPainter> sizeEncodingChartPanelFunction;

	public NumericalItemTableColumnData(String name, Function<String, ? extends Number> values) {
		this(name, values, Glyph.HorizontalBar, null);
	}

	public NumericalItemTableColumnData(String name, Function<String, ? extends Number> values, Glyph glyph,
			Color constantColor) {
		this(name, values, glyph, constantColor, 30, 50, 80, true, null, true, ColumnPosition.EAST);
	}

	public NumericalItemTableColumnData(String name, Function<String, ? extends Number> values, Glyph glyph,
			Color constantColor, Integer minSize, Integer preferredSize, Integer maxSize,
			boolean rectangleSizeEncodingOnly, BiFunction<Double, Color, ChartPainter> sizeEncodingChartPanelFunction,
			boolean show, ColumnPosition position) {
		super(name, values, minSize, preferredSize, maxSize, show, position);

		this.glyph = glyph;
		this.constantColor = constantColor;
		this.rectangleSizeEncodingOnly = rectangleSizeEncodingOnly;
		this.sizeEncodingChartPanelFunction = sizeEncodingChartPanelFunction;
	}

	public Glyph getDefaultGlyphType() {
		if (glyph == null)
			return Glyph.HorizontalBar;
		return glyph;
	}

	public Color getConstantColor() {
		return constantColor;
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

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("glyph:\t" + glyph + "\n");
		stringBuilder.append("constantColor:\t" + constantColor + "\n");
		stringBuilder.append("rectangleSizeEncodingOnly:\t" + rectangleSizeEncodingOnly + "\n");

		return super.toString() + stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();

		hash = 31 * hash + (glyph == null ? 0 : glyph.hashCode());
		hash = 31 * hash + (constantColor == null ? 0 : constantColor.hashCode());
		hash = 31 * hash + (sizeEncodingChartPanelFunction == null ? 0 : sizeEncodingChartPanelFunction.hashCode());

		return hash;
	}
}
