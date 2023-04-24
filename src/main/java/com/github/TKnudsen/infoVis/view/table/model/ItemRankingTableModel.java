package com.github.TKnudsen.infoVis.view.table.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.data.table.ItemTableColumnData;
import com.github.TKnudsen.infoVis.data.table.ItemTableColumnData.ColumnPosition;

public class ItemRankingTableModel extends ItemTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Set<String> primaryKeys;

	private final Function<String, ComplexDataObject> stockInformationFunction;
	private final Function<String, Integer> rankingFunction;
	private final Function<String, Double> scoreFunction;
	private final Function<String, Double> uncertaintyFunction;

//	/**
//	 * additional columns to be displayed
//	 */
//	private final Map<String, Boolean> attributesAsColumnsSelectedStatus;

	private final List<ItemTableColumnData> itemTableColumnData;

//	/**
//	 * @deprecated use ItemTableColumnData.isShow()
//	 */
//	private Function<ItemTableColumnData, Boolean> itemRankingTableColumnDataShowFunctions;

	private final String primaryKeyAttribute;

//	private Function<String, Boolean> chartAvailableFunction;

//	private boolean showDate;
	private boolean showPrimaryKey;

	// name to ISIN. problem: //TODO name may be not unique!!
//	private Map<String, String> itemLookup;
//	private int detaultSortColumn;
	private int nameColumn;

	private boolean initializing = false;

	/**
	 * 
	 * @param primaryKeys              primary keys
	 * @param stockInformationFunction stock information
	 * @param rankingFunction          ranking
	 * @param scoreFunction            score function
	 * @param uncertaintyFunction      uncertainty
	 * @param showPrimaryKey           is show primary key
	 * @param itemTableColumnData      item table column data
	 * @param primaryKeyAttribute      primaryKeyAttribute
	 */
	public ItemRankingTableModel(Set<String> primaryKeys, Function<String, ComplexDataObject> stockInformationFunction,
			Function<String, Integer> rankingFunction, Function<String, Double> scoreFunction,
			Function<String, Double> uncertaintyFunction, boolean showPrimaryKey,
			List<ItemTableColumnData> itemTableColumnData, String primaryKeyAttribute) {

		super(Integer.MAX_VALUE, "SUM");

		this.primaryKeys = primaryKeys;

		this.stockInformationFunction = stockInformationFunction;
		this.rankingFunction = rankingFunction;
		this.scoreFunction = scoreFunction;
		this.uncertaintyFunction = uncertaintyFunction;
//		this.chartAvailableFunction = chartAvailableFunction;

//		this.showDate = showDate;
		this.showPrimaryKey = showPrimaryKey;

		this.itemTableColumnData = itemTableColumnData;

		this.primaryKeyAttribute = primaryKeyAttribute;
	}

	@Override
	public void initialize() {

		initializing = true;

		int tmp = 0;

		// columns
		List<String> columns = new ArrayList<>();
		columns.add("#");
		tmp++;

		columns.add("Score");
		setDetaultSortColumn(tmp++);

		columns.add("Uncert.");
		tmp++;

//		columns.add("Name");
//		nameColumn = tmp++;

//		if (attributesAsColumnsSelectedStatus != null)
//			for (String column : attributesAsColumnsSelectedStatus.keySet())
//				if (attributesAsColumnsSelectedStatus.get(column)) {
//					columns.add(column);
//					tmp++;
//				}
		if (itemTableColumnData != null)
			for (ItemTableColumnData column : itemTableColumnData)
				if (column.getColumnPosition().equals(ColumnPosition.WEST))
					if (column.isShowColumn()) {
						columns.add(column.getName());
						tmp++;
					}

//		columns.add("Land");
//		tmp++;

//		if (isShowDate()) {
//			columns.add("Date");
//			tmp++;
//		}

		if (isShowPrimaryKeyAttribute()) {
			columns.add("ISIN");
			tmp++;
		}

//		columns.add("Chart");
//		tmp++;

		addColumnNamesToList(columns);

		if (itemTableColumnData != null)
			for (ItemTableColumnData column : itemTableColumnData)
				if (column.isShowColumn())
					if (column.getColumnPosition().equals(ColumnPosition.EAST))
						columns.add(column.getName());

		columnNames = DataConversion.listToArray(columns, String.class);

		// define table size
		int tableSize = primaryKeys.size();

		data = new Object[tableSize][columnNames.length];

//		itemLookup = new HashMap<String, String>();
		// fill data matrix
		int y = 0;
		for (String primaryKey : primaryKeys) {
			int i = 0;

			// rank
			Integer rank = null;
			try {
				rank = rankingFunction.apply(primaryKey);
			} catch (Exception e) {
			}
			data[y][i++] = (rank == null) ? "n.a." : rank;

			// score
			Double sc = scoreFunction.apply(primaryKey);
			data[y][i++] = (sc == null) ? "n.a." : MathFunctions.round(sc, 4);

			// uncertainty
			data[y][i++] = MathFunctions.round(uncertaintyFunction.apply(primaryKey), 4);

			/**
			 * TODO try to get rid of this! use the itemTableColumnData solution!
			 */
			ComplexDataObject stockInformation = stockInformationFunction.apply(primaryKey);

//			// name
//			if (stockInformation != null) {
//				data[y][i++] = stockInformation.getName();
////				itemLookup.put(stockInformation.getName(), primaryKey);
//			} else
//				i += 1;

			// additional columns
//			if (attributesAsColumnsSelectedStatus != null)
//				for (String column : attributesAsColumnsSelectedStatus.keySet())
//					if (attributesAsColumnsSelectedStatus.get(column)) {
//						if (stockInformation != null) {
//							data[y][i++] = stockInformation.getAttribute(column);
//						} else
//							i++;
//					}
			if (itemTableColumnData != null)
				for (ItemTableColumnData column : itemTableColumnData)
					if (column.isShowColumn())
						if (column.getColumnPosition().equals(ColumnPosition.WEST))
							data[y][i++] = column.getValue(primaryKey);

//			// Land
//			if (stockInformation != null)
//				data[y][i++] = stockInformation.getAttribute("Land");
//			else
//				i++;

//			// Parse Date
//			if (isShowDate()) {
//				String dateString = "";
//				if (stockInformation != null)
//					if (stockInformation.getAttribute("Parse Date") != null)
//						dateString = dateFormatFileSystem.format(stockInformation.getAttribute("Parse Date"));
//				data[y][i++] = dateString;
//			}

			if (isShowPrimaryKeyAttribute() && primaryKeyAttribute != null)
				data[y][i++] = stockInformation.getAttribute(primaryKeyAttribute);

//			// chart available?
//			if (chartAvailableFunction != null && primaryKey != null)
//				if (chartAvailableFunction.apply(primaryKey))
//					data[y][i++] = "+";
//				else
//					data[y][i++] = "-";
//			else
//				data[y][i++] = "--";

			// add additional column values
			i += addAdditionalColumsData(primaryKey, y, i);

			// add ItemTableColumnData
			if (itemTableColumnData != null)
				for (ItemTableColumnData column : itemTableColumnData)
					if (column.isShowColumn())
						if (column.getColumnPosition().equals(ColumnPosition.EAST))
							data[y][i++] = column.getValue(primaryKey);

			y++;
		}
		
		resetValuesBuffer();
		
		initializing = false;
	}

	protected void addColumnNamesToList(List<String> columns) {
		// nothing to do here
	}

	/**
	 * 
	 * @param primaryKey         pk
	 * @param y                  y
	 * @param currentColumnIndex index
	 * @return number of columns that have been filled (i has to be incremented,
	 *         accordingly)
	 */
	protected int addAdditionalColumsData(String primaryKey, int y, int currentColumnIndex) {
		// nothing to do here
		return 0;
	}

	public boolean isShowPrimaryKeyAttribute() {
		return showPrimaryKey;
	}

	public void setShowPrimaryKey(boolean showPrimaryKey) {
		if (this.showPrimaryKey == showPrimaryKey)
			return;

		this.showPrimaryKey = showPrimaryKey;

		initialize();
	}

//	public int getDetaultSortColumn() {
//		return detaultSortColumn;
//	}

}