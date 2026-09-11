package com.github.TKnudsen.infoVis.view.painters.grid.test;

import java.awt.Color;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.grid.AdjacencyMatrixPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Interactive demo of {@link AdjacencyMatrixPainter}: an unhighlighted
 * matrix, one with a single row highlighted, and one with a pair of rows
 * highlighted.
 *
 * @since 2026
 */
public class AdjacencyMatrixPainterTester {

	private static final String[] LABELS = { "Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig" };

	public static void main(String[] args) {
		JPanel plain = createPanel(null);
		JPanel singleHighlight = createPanel(new int[] { 2 });
		JPanel pairHighlight = createPanel(new int[] { 1, 4 });

		SVGFrameTools.dropSVGFrameHorizontal(java.util.Arrays.asList(plain, singleHighlight, pairHighlight),
				"AdjacencyMatrixPainter");
	}

	private static JPanel createPanel(int[] highlightedCoordinate) {
		Color[][] data = createSymmetricColorMatrix(LABELS.length);

		AdjacencyMatrixPainter painter = new AdjacencyMatrixPainter(data, LABELS);
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
