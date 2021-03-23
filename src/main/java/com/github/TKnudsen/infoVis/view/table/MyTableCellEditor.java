package com.github.TKnudsen.infoVis.view.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4341533984549368068L;

	JComponent component = new JTextField();

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
			int vColIndex) {

		((JTextField) component).setText(String.valueOf(value));

		component.setBorder(null);

		return component;
	}

	public Object getCellEditorValue() {
		return ((JTextField) component).getText();
	}
}