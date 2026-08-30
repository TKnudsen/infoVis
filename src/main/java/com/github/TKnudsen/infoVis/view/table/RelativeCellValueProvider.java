package com.github.TKnudsen.infoVis.view.table;

/**
 * <p>
 * Table model contract exposing a normalized [0,1]-ish relative value per
 * cell, used by {@link MyTableCellDefaultRenderer} to size/color encode
 * cells.
 * </p>
 *
 * @version 1.0
 */
public interface RelativeCellValueProvider {

	double getRelativeValueAt(int row, int col, boolean rowWise);
}
