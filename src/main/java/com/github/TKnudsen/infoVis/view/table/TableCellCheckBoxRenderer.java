package com.github.TKnudsen.infoVis.view.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import sun.swing.DefaultLookup;

public class TableCellCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6697146784848697432L;

	private final Color unselectedForeground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.foreground");
	private final Color unselectedBackground = (Color) UIManager.getLookAndFeelDefaults().get("TableHeader.background");

	private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		this.setHorizontalAlignment(SwingConstants.CENTER);

		if (value == null || !(value instanceof Boolean))
			throw new IllegalArgumentException();

		if (table == null) {
			return this;
		}

		MyTableCellRendererTools.manageComponentColors(table, this, ui, row, column, isSelected, hasFocus);

		this.setSelected((boolean) value);

		setOpaque(true);

		return this;
	}

	private Border getNoFocusBorder() {
		Border border = DefaultLookup.getBorder(this, ui, "Table.cellNoFocusBorder");
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
