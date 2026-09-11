package com.github.TKnudsen.infoVis.view.painters.grid.test;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.grid.AdjacencyMatrixPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Interactive demo of {@link AdjacencyMatrixPainter}: a click-to-highlight
 * panel (clicking a label or a cell selects that row, or that pair, via
 * {@link AdjacencyMatrixPainter#getElementsAtPoint(java.awt.Point)}), next
 * to two statically pre-highlighted panels for reference.
 *
 * @since 2026
 */
public class AdjacencyMatrixPainterTester {

	private static final String[] LABELS = { "Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig" };

	public static void main(String[] args) {
		JPanel clickToHighlight = createClickToHighlightPanel();
		JPanel singleHighlight = createStaticPanel(new int[] { 2 });
		JPanel pairHighlight = createStaticPanel(new int[] { 1, 4 });

		SVGFrameTools.dropSVGFrameHorizontal(java.util.Arrays.asList(clickToHighlight, singleHighlight, pairHighlight),
				"AdjacencyMatrixPainter");
	}

	private static JPanel createClickToHighlightPanel() {
		AdjacencyMatrixPainter painter = new AdjacencyMatrixPainter(createSymmetricColorMatrix(LABELS.length), LABELS);
		InfoVisChartPanel panel = new InfoVisChartPanel(painter);

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				List<Integer> clicked = painter.getElementsAtPoint(e.getPoint());
				painter.setHighLightedCoordinate(clicked == null ? null : clicked.stream().mapToInt(Integer::intValue).toArray());
				panel.repaint();
			}
		});

		return panel;
	}

	private static JPanel createStaticPanel(int[] highlightedCoordinate) {
		AdjacencyMatrixPainter painter = new AdjacencyMatrixPainter(createSymmetricColorMatrix(LABELS.length), LABELS);
		painter.setHighLightedCoordinate(highlightedCoordinate);

		return new InfoVisChartPanel(painter);
	}

	private static Color[][] createSymmetricColorMatrix(int n) {
		Color[][] data = new Color[n][n];
		for (int x = 0; x < n; x++)
			for (int y = x; y < n; y++) {
				float t = (x + y) / (float) (2 * (n - 1));
				Color c = x == y ? Color.LIGHT_GRAY : new Color(t, 0.4f, 1.0f - t);
				data[x][y] = c;
				data[y][x] = c;
			}
		return data;
	}
}
