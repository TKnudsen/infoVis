package com.github.TKnudsen.infoVis.view.panels.featureSpace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Lays out a row of {@link FeatureBoxPlotWithClassInformationPanel}s (one per
 * feature), optionally with a per-feature checkbox to include/exclude it from
 * a downstream selection, and a detail panel on the east showing whichever
 * box plot was last clicked.
 *
 * @author Christian Ritter, Juergen Bernard
 * @version 1.02
 */
public class FeatureSpaceView extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Consumer<String> featureSelectionStatusChangedConsumer;

	private List<JCheckBox> checkboxes = null;
	private boolean userBasedFeatureSelection;
	private List<String> featureNames;
	private JScrollPane fssp;
	private List<FeatureBoxPlotWithClassInformationPanel> boxplotPanels;
	private JPanel contentPane;

	private FeatureBoxPlotWithClassInformationPanel highlightedPanel;
	private JPanel detailView;

	public FeatureSpaceView(List<FeatureBoxPlotWithClassInformationPanel> boxplotPanels,
			boolean userBasedFeatureSelection, List<String> featureNames,
			Consumer<String> featureSelectionStatusChangedConsumer) {
		this.boxplotPanels = boxplotPanels;
		this.featureNames = featureNames;
		this.featureSelectionStatusChangedConsumer = featureSelectionStatusChangedConsumer;

		this.userBasedFeatureSelection = userBasedFeatureSelection;
		if (this.userBasedFeatureSelection)
			initializeCheckBoxes();

		if (boxplotPanels != null)
			initialize();
	}

	public void setFeaturePanels(List<FeatureBoxPlotWithClassInformationPanel> boxplotPanels,
			List<String> featureNames) {
		this.boxplotPanels = boxplotPanels;
		this.featureNames = featureNames;
		initializeCheckBoxes();
		initialize();
	}

	public List<Boolean> getSelectedFeatures() {
		if (checkboxes == null)
			return null;
		return checkboxes.stream().map(x -> x.isSelected()).collect(Collectors.toList());
	}

	public List<String> getUnselectedFeatureNames() {
		List<String> result = new ArrayList<>();
		if (checkboxes == null)
			return result;
		for (int i = 0; i < checkboxes.size(); i++) {
			if (!checkboxes.get(i).isSelected())
				result.add(featureNames.get(i));
		}
		return result;
	}

	public List<String> getSelectedFeatureNames() {
		List<String> result = new ArrayList<>();
		if (checkboxes == null)
			return result;
		for (int i = 0; i < checkboxes.size(); i++) {
			if (checkboxes.get(i).isSelected())
				result.add(featureNames.get(i));
		}
		return result;
	}

	public void unselectCheckboxes(Set<String> featureNames) {
		if (checkboxes == null)
			return;
		for (JCheckBox cb : checkboxes) {
			cb.setSelected(true);
		}
		if (featureNames == null)
			return;
		for (String fn : featureNames) {
			int index = this.featureNames.indexOf(fn);
			if (index > -1) {
				checkboxes.get(index).setSelected(false);
				boxplotPanels.get(index).setDisabled(true);
			}
		}
		this.repaint();
	}

	private void initialize() {
		this.removeAll();
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayout(1, boxplotPanels.size()));
		this.setLayout(new BorderLayout());
		detailView = new JPanel();
		detailView.setLayout(new BorderLayout());
		if (checkboxes == null) {
			for (int i = 0; i < boxplotPanels.size(); i++) {
				FeatureBoxPlotWithClassInformationPanel bp = boxplotPanels.get(i);
				bp.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
						if (highlightedPanel != null)
							highlightedPanel.setHighlighted(false);
						detailView.removeAll();
						detailView.add(bp.getDetailedPanel(), BorderLayout.CENTER);
						bp.setHighlighted(true);
						highlightedPanel = bp;
						detailView.revalidate();
						detailView.repaint();
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
				contentPane.add(bp, BorderLayout.CENTER);
			}
		} else {
			for (int i = 0; i < boxplotPanels.size(); i++) {
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				FeatureBoxPlotWithClassInformationPanel bp = boxplotPanels.get(i);
				bp.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
						if (highlightedPanel != null)
							highlightedPanel.setHighlighted(false);
						detailView.removeAll();
						detailView.add(bp.getDetailedPanel(), BorderLayout.CENTER);
						bp.setHighlighted(true);
						highlightedPanel = bp;
						detailView.revalidate();
						detailView.repaint();
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
				panel.add(bp, BorderLayout.CENTER);
				JCheckBox cb = checkboxes.get(i);
				for (ActionListener al : cb.getActionListeners()) {
					cb.removeActionListener(al);
				}
				cb.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						bp.setDisabled(!cb.isSelected());

						featureSelectionStatusChangedConsumer.accept(cb.getName());
					}
				});
				panel.add(cb, BorderLayout.SOUTH);
				contentPane.add(panel);
			}
		}
		if (featureNames != null) {
			contentPane.setMinimumSize(new Dimension(20 * featureNames.size(), 10));
			contentPane.setPreferredSize(new Dimension(20 * featureNames.size(), 10));
			contentPane.setMaximumSize(new Dimension(20 * featureNames.size(), 3000));
		}
		fssp = new JScrollPane(contentPane);
		fssp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		fssp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		fssp.getHorizontalScrollBar().setUnitIncrement(16);
		this.add(fssp, BorderLayout.CENTER);
		this.add(detailView, BorderLayout.EAST);
	}

	private void initializeCheckBoxes() {
		if (boxplotPanels == null || boxplotPanels.size() == 0)
			return;
		checkboxes = new ArrayList<>();
		int numberOfCheckboxes = boxplotPanels.size();
		for (int i = 0; i < numberOfCheckboxes; i++) {
			JCheckBox checkBox = new JCheckBox();
			String name = (featureNames != null) ? featureNames.get(i) : String.valueOf(i);
			checkBox.setName(name);
			checkBox.setToolTipText(name);
			checkBox.setSelected(true);
			checkboxes.add(checkBox);
		}
	}

}
