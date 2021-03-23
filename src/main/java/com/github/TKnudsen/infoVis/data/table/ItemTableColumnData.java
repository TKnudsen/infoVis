package com.github.TKnudsen.infoVis.data.table;

import java.util.Objects;
import java.util.function.Function;

/**
 * describes how the column of a table shall be represented visually
 *
 */
public class ItemTableColumnData {

	private final String name;
	private final Function<String, ? extends Object> values;
	private final Integer minSize;
	private final Integer preferredSize;
	private final Integer maxSize;
	private boolean show;
	private final ColumnPosition position;

	public ItemTableColumnData(String name, Function<String, ? extends Object> values, Integer minSize,
			Integer preferredSize, Integer maxSize, ColumnPosition position) {
		this(name, values, minSize, preferredSize, maxSize, true, position);
	}

	public ItemTableColumnData(String name, Function<String, ? extends Object> values, Integer minSize,
			Integer preferredSize, Integer maxSize, boolean show, ColumnPosition position) {
		Objects.requireNonNull(values);

		this.name = name;
		this.values = values;
		this.minSize = minSize;
		this.preferredSize = preferredSize;
		this.maxSize = maxSize;
		this.show = show;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public Object getValue(String primaryKey) {
		return values.apply(primaryKey);
	}

	/**
	 * can be null
	 * 
	 * @return
	 */
	public Integer getMinWidth() {
		return minSize;
	}

	/**
	 * can be null
	 * 
	 * @return
	 */
	public Integer getPreferredWidth() {
		return preferredSize;
	}

	/**
	 * can be null
	 * 
	 * @return
	 */
	public Integer getMaxWidth() {
		return maxSize;
	}

	public boolean isShowColumn() {
		return show;
	}

	public void setShowColumn(boolean show) {
		this.show = show;
	}

	public ColumnPosition getColumnPosition() {
		if (position == null)
			return ColumnPosition.EAST;
		return position;
	}

	public enum ColumnPosition {
		WEST, EAST;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("name:\t" + name + "\n");
		stringBuilder.append("minSize:\t" + minSize + "\n");
		stringBuilder.append("preferredSize:\t" + preferredSize + "\n");
		stringBuilder.append("maxSize:\t" + maxSize + "\n");
		stringBuilder.append("position:\t" + position + "\n");

		return stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;

		hash = 31 * hash + (name == null ? 0 : name.hashCode());
		hash = 31 * hash + (values == null ? 0 : values.hashCode());
		hash = 31 * hash + (minSize == null ? 0 : minSize.hashCode());
		hash = 31 * hash + (preferredSize == null ? 0 : preferredSize.hashCode());
		hash = 31 * hash + (maxSize == null ? 0 : maxSize.hashCode());
		hash = 31 * hash + (position == null ? 0 : position.hashCode());

		return hash;
	}

}
