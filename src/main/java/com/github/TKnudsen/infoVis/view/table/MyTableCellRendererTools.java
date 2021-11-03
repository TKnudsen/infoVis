package com.github.TKnudsen.infoVis.view.table;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;

public class MyTableCellRendererTools {

	private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

	public static void manageComponentColors(JTable table, JComponent targetComponent, ComponentUI ui, int row,
			int column, boolean isSelected, boolean hasFocus) {

		Color unselectedForeground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.foreground");
		Color unselectedBackground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.background");

		Color fg = null;
		Color bg = null;

		JTable.DropLocation dropLocation = table.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsertRow() && !dropLocation.isInsertColumn()
				&& dropLocation.getRow() == row && dropLocation.getColumn() == column) {

			// fg = DefaultLookup.getColor(targetComponent, ui, "Table.dropCellForeground");
			// TODO needs testing
			fg = UIManager.getColor("Table.dropCellForeground");

			// bg = DefaultLookup.getColor(targetComponent, ui, "Table.dropCellBackground");
			// TODO needs testing
			bg = UIManager.getColor("Table.dropCellBackground");

			isSelected = true;
		}

		if (isSelected) {
			targetComponent.setForeground(fg == null ? table.getSelectionForeground() : fg);
			targetComponent.setBackground(bg == null ? table.getSelectionBackground() : bg);
		} else {
			if (row % 2 == 0)
				targetComponent.setBackground(unselectedBackground.brighter().brighter());
			else
				targetComponent.setBackground(unselectedBackground.brighter());

			targetComponent.setForeground(unselectedForeground != null ? unselectedForeground : table.getForeground());
		}

		targetComponent.setFont(table.getFont());

		if (hasFocus) {
			Border border = null;
			if (isSelected) {
				// border = DefaultLookup.getBorder(targetComponent, ui,
				// "Table.focusSelectedCellHighlightBorder");
				// TODO needs testing
				border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
			}
			if (border == null) {
				// border = DefaultLookup.getBorder(targetComponent, ui,
				// "Table.focusCellHighlightBorder");
				// TODO needs testing
				border = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			targetComponent.setBorder(border);

			if (!isSelected && table.isCellEditable(row, column)) {
				Color col;
				// col = DefaultLookup.getColor(targetComponent, ui,
				// "Table.focusCellForeground");
				// TODO needs testing
				col = UIManager.getColor("Table.focusCellForeground");
				if (col != null) {
					targetComponent.setForeground(col);
				}

				// col = DefaultLookup.getColor(targetComponent, ui,
				// "Table.focusCellBackground");
				// TODO needs testing
				col = UIManager.getColor("Table.focusCellBackground");
				if (col != null) {
					targetComponent.setBackground(col);
				}
			}
		} else {
			targetComponent.setBorder(getNoFocusBorder(targetComponent, ui));
		}
	}

	private static Border getNoFocusBorder(JComponent targetComponent, ComponentUI ui) {
		// Border border = DefaultLookup.getBorder(targetComponent, ui,
		// "Table.cellNoFocusBorder");
		// TODO needs testing
		Border border = UIManager.getBorder("Table.cellNoFocusBorder");
		if (System.getSecurityManager() != null) {
			if (border != null)
				return border;
			return SAFE_NO_FOCUS_BORDER;
		} else if (border != null) {
			if (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER) {
				return border;
			}
		}
		return noFocusBorder;
	}
}
