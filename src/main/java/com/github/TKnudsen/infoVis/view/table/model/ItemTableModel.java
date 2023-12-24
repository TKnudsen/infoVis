package com.github.TKnudsen.infoVis.view.table.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.table.RelativeCellValueProvider;

public abstract class ItemTableModel extends AbstractTableModel implements RelativeCellValueProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1914350444679891919L;

	protected static boolean DEBUG = true;

	private int itemFilterCount;
	private String comparableAttribute;

	protected String[] columnNames;
	protected Object[][] data;
	private Map<Integer, List<Object>> rowValues = new HashMap<>();
	private Map<Integer, List<Object>> columnValues = new HashMap<>();
	private Map<Integer, StatisticsSupport> rowValueStatistics = new HashMap<>();
	private Map<Integer, StatisticsSupport> columnValueStatistics = new HashMap<>();

	private DoubleParser doubleParser = new DoubleParser(true);

	private int detaultSortColumn;

	public ItemTableModel(int itemFilterCount, String comparableAttribute) {
		this.setItemFilterCount(itemFilterCount);
		this.setComparableAttribute(comparableAttribute);
	}

	public abstract void initialize();

	public Object getValueAt(int row, int col) {
		return getData()[row][col];
	}

	@Override
	/**
	 * RelativeCellValueProvider
	 * 
	 * @param row
	 * @param col
	 * @param rowWise
	 * @return
	 */
	public double getRelativeValueAt(int row, int col, boolean rowWise) {
		Object value = getValueAt(row, col);

		StatisticsSupport statistics = null;
		if (rowWise)
			statistics = getRowStatistics(row);
		else
			statistics = getColumnStatistics(col);

		if (statistics == null)
			return Double.NaN;

		if (value instanceof Number)
			return MathFunctions.linearScale(statistics.getMin(), statistics.getMax(), ((Number) value).doubleValue());

		return 0.0;
	}

	public Object getRowMin(int row) {
		StatisticsSupport statistics = getRowStatistics(row);
//		return statistics != null ? statistics.getMin() : Double.NaN;
		double min = 0.0;
		for (int y = 0; y < getData()[row].length; y++)
			if (getData()[row][y] instanceof Number) {
				Number n = (Number) getData()[row][y];
				if (!Double.isNaN(n.doubleValue()))
					min = Math.min(min, n.doubleValue());
			}

		return min;
	}

	public Object getRowMax(int row) {
		StatisticsSupport statistics = getRowStatistics(row);
//		return statistics != null ? statistics.getMax() : Double.NaN;
		double max = 0.0;
		for (int y = 0; y < getData()[row].length; y++)
			if (getData()[row][y] instanceof Number) {
				Number n = (Number) getData()[row][y];
				if (!Double.isNaN(n.doubleValue()))
					max = Math.max(max, n.doubleValue());
			}

		return max;
	}

	/**
	 * iterates over the second dimension
	 * 
	 * @param row
	 * @return
	 */
	public List<Object> getRowValues(int row) {
		if (!rowValues.containsKey(row)) {
			List<Object> objects = new ArrayList<>();

			for (int y = 0; y < getData()[row].length; y++)
				objects.add(getData()[row][y]);

			rowValues.put(row, objects);
		}
		return rowValues.get(row);
	}

	protected StatisticsSupport getRowStatistics(int row) {
		if (!rowValueStatistics.containsKey(row)) {
			List<Object> values = getRowValues(row);

			List<Double> doubleValues = new ArrayList<>();
			for (Object o : values)
				if (o != null)
					if (o instanceof Number)
						if (!Double.isNaN(((Number) o).doubleValue()))
							doubleValues.add(((Number) o).doubleValue());
			StatisticsSupport statistics = doubleValues.size() > 0 ? new StatisticsSupport(doubleValues) : null;
			rowValueStatistics.put(row, statistics);
		}
		return rowValueStatistics.get(row);
	}

	/**
	 * iterates over the first dimension
	 * 
	 * @param column
	 * @return
	 */
	public List<Object> getColumnValues(int column) {
		if (!columnValues.containsKey(column)) {
			List<Object> objects = new ArrayList<>();

			for (int row = 0; row < getData().length; row++)
				objects.add(getData()[row][column]);

			columnValues.put(column, objects);
		}
		return columnValues.get(column);
	}

	protected StatisticsSupport getColumnStatistics(int column) {
		if (!columnValueStatistics.containsKey(column)) {
			List<Object> values = getColumnValues(column);

			List<Double> doubleValues = new ArrayList<>();
			for (Object o : values)
				if (o != null)
					if (o instanceof Number)
						if (!Double.isNaN(((Number) o).doubleValue()))
							doubleValues.add(((Number) o).doubleValue());
			StatisticsSupport statistics = doubleValues.size() > 0 ? new StatisticsSupport(doubleValues) : null;
			columnValueStatistics.put(column, statistics);
		}
		return columnValueStatistics.get(column);
	}

	protected void resetValuesBuffer() {
		rowValues.clear();
		columnValues.clear();
		rowValueStatistics.clear();
		columnValueStatistics.clear();
	}

	protected void resetRowValuesBuffer(int row) {
		rowValues.remove(row);
		rowValueStatistics.remove(row);
	}

	protected void resetColumnValuesBuffer(int column) {
		columnValues.remove(column);
		columnValueStatistics.remove(column);
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for each
	 * cell. If we didn't implement this method, then the last column would contain
	 * text ("true"/"false"), rather than a check box.
	 */
	public Class<?> getColumnClass(int c) {
		Object object = getValueAt(0, c);
		if (object == null)
			return Double.class;

		return object.getClass();
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears on-screen.
		if (col < 2) {
			return false;
		} else {
			return true;
		}
	}

	public int getColumnCount() {
		return getColumnNames().length;
	}

	public int getRowCount() {
		return getData().length;
	}

	public String getColumnName(int col) {
		return getColumnNames()[col];
	}

	public String[] getColumnNames() {
		if (columnNames == null)
			initialize();

		return columnNames;
	}

	public Object[][] getData() {
		if (data == null)
			initialize();

		return data;
	}

	public DoubleParser getDoubleParser() {
		return doubleParser;
	}

	public void setDoubleParser(DoubleParser doubleParser) {
		this.doubleParser = doubleParser;
	}

	public String getComparableAttribute() {
		return comparableAttribute;
	}

	public void setComparableAttribute(String comparableAttribute) {
		this.comparableAttribute = comparableAttribute;
	}

	public int getItemFilterCount() {
		return itemFilterCount;
	}

	public void setItemFilterCount(int itemFilterCount) {
		this.itemFilterCount = itemFilterCount;
	}

	public int getDetaultSortColumn() {
		return detaultSortColumn;
	}

	public void setDetaultSortColumn(int detaultSortColumn) {
		this.detaultSortColumn = detaultSortColumn;
	}

}