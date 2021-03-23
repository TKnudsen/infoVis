package com.github.TKnudsen.infoVis.view.table;

public interface RelativeCellValueProvider {

	double getRelativeValueAt(int row, int col, boolean rowWise);
}
