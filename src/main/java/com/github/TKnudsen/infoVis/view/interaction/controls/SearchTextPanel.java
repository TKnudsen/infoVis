package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JPanel;

/**
 * Swing panel wrapping an auto-completing combo box that lets the user search
 * for and select an entry from a set of keys, optionally also matching against
 * a secondary search space (e.g. a display name or description for each key).
 * The selected entry is resolved back to its key and passed to the supplied
 * search result consumer.
 *
 * @version 2.0 revised in September 2026
 * @since 2022
 */
public class SearchTextPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * the keys that can be searched
	 */
	private final Set<String> keySet;

	/**
	 * an additional search space that can be searched, such as names or
	 * descriptions; may be null if the keys themselves are the only searchable text
	 */
	private final Function<String, String> optionalSearchSpace;
	private final Consumer<String> searchResultConsumer;

	private AutoCompleteComboBox<String> itemSelectionCombobox;
	private boolean externalSet = false;

	private Map<String, String> searchTextPanelToKeyLookup;

	public SearchTextPanel(Set<String> keySet, Function<String, String> optionalSearchSpace,
			Consumer<String> searchResultConsumer) {
		Objects.requireNonNull(keySet);

		this.keySet = keySet;
		this.optionalSearchSpace = optionalSearchSpace;
		this.searchResultConsumer = searchResultConsumer;

		this.setLayout(new GridLayout(1, 1));

		initializeSearchControls();
	}

	private void initializeSearchControls() {
		List<String> sorted = new ArrayList<String>(getSearchTextPanelToKeyLookup().keySet());
		Collections.sort(sorted);

		itemSelectionCombobox = new AutoCompleteComboBox<>(sorted, true, false);
		itemSelectionCombobox.setMinimumSize(new Dimension(200, 28));
		itemSelectionCombobox.setPreferredSize(new Dimension(500, 28));
		itemSelectionCombobox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (externalSet)
					return;

				String name = (String) itemSelectionCombobox.getSelectedItem();

				if (getSearchTextPanelToKeyLookup().keySet().contains(name))
					searchResultConsumer.accept(getSearchTextPanelToKeyLookup().get(name));
				else {
					System.err.println("SearchTextPanel: unknown search item that will be returned: " + name);
					searchResultConsumer.accept(name);
				}
			}
		});

		add(itemSelectionCombobox);
	}

	public void setItem(String name) {
		externalSet = true;
		itemSelectionCombobox.setSelectedItem(name);
		externalSet = false;

		repaint();
		revalidate();
	}

	private Map<String, String> getSearchTextPanelToKeyLookup() {
		if (searchTextPanelToKeyLookup == null) {
			searchTextPanelToKeyLookup = new HashMap<String, String>();

			for (String key : keySet) {
				if (optionalSearchSpace != null) {
					String name = optionalSearchSpace.apply(key);
					if (name != null)
						searchTextPanelToKeyLookup.put(name, key);
				}
				searchTextPanelToKeyLookup.put(key, key);
			}
		}

		return searchTextPanelToKeyLookup;
	}
}
