package com.github.TKnudsen.infoVis.view.panels.legend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

public class LegendPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LayoutManager layoutManager = new GridLayout(0, 1);
	private List<LegendItemPanel> legendItemPanels;

	public LegendPanel() {
		this(new ArrayList<>());
	}

	public LegendPanel(List<LegendItemPanel> legendItemPanels) {
		Objects.requireNonNull(legendItemPanels);

		this.legendItemPanels = legendItemPanels;

		super.setLayout(new GridLayout(1, 1));

		refresh();
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		this.layoutManager = mgr;

		refresh();
	}

	private void refresh() {
		this.removeAll();

		JPanel legend = new JPanel(layoutManager);

		if (legendItemPanels != null)
			for (LegendItemPanel legendItemPanel : legendItemPanels)
				legend.add(legendItemPanel);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(legend, BorderLayout.NORTH);
		panel.add(new JPanel(), BorderLayout.CENTER);

		this.add(panel);
	}

	public void addLegendItemPanel(LegendItemPanel legendItemPanel) {
		if (legendItemPanels.add(legendItemPanel))
			refresh();

		repaint();
	}

	public void addAllLegendItemPanels(Collection<LegendItemPanel> legendItemPanels) {
		for (LegendItemPanel legendItemPanel : legendItemPanels)
			this.legendItemPanels.add(legendItemPanel);

		refresh();

		repaint();
	}

	public void setLegendItemPanels(Collection<LegendItemPanel> legendItemPanels) {
		this.legendItemPanels.clear();

		addAllLegendItemPanels(legendItemPanels);
	}

	public void removeLegendItemPanel(LegendItemPanel legendItemPanel) {
		if (legendItemPanels.remove(legendItemPanel))
			refresh();

		repaint();
	}

	public void sortByLabelName() {
		legendItemPanels.sort(new Comparator<LegendItemPanel>() {

			@Override
			public int compare(LegendItemPanel o1, LegendItemPanel o2) {
				if (o2 == null)
					return -1;
				if (o1 == null)
					return 1;

				return o1.getLabel().compareTo(o2.getLabel());
			}
		});

		refresh();

		repaint();
	}
}
