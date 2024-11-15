package com.github.TKnudsen.infoVis.view.panels.legend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class LegendPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LayoutManager layoutManager = new GridLayout(0, 1);
	private List<LegendItemPanel> legendItemPanels;

	/**
	 * selection model that contains the scoring functions as string identifiers
	 */
	private SelectionModel<String> selectionModel = SelectionModels.create();

	public LegendPanel() {
		this(new ArrayList<>());
	}

	public LegendPanel(List<LegendItemPanel> legendItemPanels) {
		Objects.requireNonNull(legendItemPanels);

		this.legendItemPanels = legendItemPanels;

		super.setLayout(new GridLayout(1, 1));

		for (LegendItemPanel lp : legendItemPanels)
			lp.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					String label = lp.getLabel();
					if (selectionModel != null)
						selectionModel.setSelection(Arrays.asList(label));
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});

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

	public SelectionModel<String> getSelectionModel() {
		return selectionModel;
	}

	public void setSelectionModel(SelectionModel<String> selectionModel) {
		this.selectionModel = selectionModel;
	}
}
