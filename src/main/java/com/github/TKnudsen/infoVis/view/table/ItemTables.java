package com.github.TKnudsen.infoVis.view.table;

import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.javagl.common.ui.table.TristateTableRowSorter;

/**
 * <p>
 * Row-sorting and search-filtering for a JTable. infoVis's own
 * {@link com.github.TKnudsen.infoVis.view.table.model.ItemTableModel
 * ItemTableModel}-based tables provide no such behavior themselves (only
 * rendering, e.g. {@link MyTableCellDefaultRenderer}) -- every consumer had
 * to wire sorting/filtering individually, or not at all.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ItemTables {

	private ItemTables() {
	}

	/**
	 * Installs a {@link TristateTableRowSorter} (sortable ascending, descending,
	 * or unsorted -- unlike the default two-state {@link TableRowSorter}) as
	 * the table's row sorter.
	 *
	 * @return the installed sorter
	 */
	public static TristateTableRowSorter<TableModel> installTristateSorting(JTable table) {
		TristateTableRowSorter<TableModel> sorter = new TristateTableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);
		return sorter;
	}

	/**
	 * Creates a search field that live-filters {@code table} across all
	 * columns as the user types (case-insensitive substring match). Reuses the
	 * table's existing row sorter if it already has a {@link TableRowSorter},
	 * or installs one via {@link #installTristateSorting(JTable)} otherwise.
	 *
	 * @return the search field. The caller places it in the UI (e.g. above the
	 *         table); this method only wires its filtering behavior.
	 */
	public static JTextField createSearchField(JTable table) {
		RowSorter<? extends TableModel> existing = table.getRowSorter();

		TableRowSorter<TableModel> sorter;
		if (existing instanceof TableRowSorter<?>) {
			@SuppressWarnings("unchecked")
			TableRowSorter<TableModel> cast = (TableRowSorter<TableModel>) existing;
			sorter = cast;
		} else
			sorter = installTristateSorting(table);

		JTextField searchField = new JTextField();
		searchField.getDocument().addDocumentListener(new DocumentListener() {

			private void update() {
				String text = searchField.getText();
				sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
		});

		return searchField;
	}

}
