package com.github.TKnudsen.infoVis.view.painters.grid.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DCircularPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DColorPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DColorTransparencyPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DEllipsisPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DRectangleSizePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Interactive demo of {@link Grid2DColorPainter},
 * {@link Grid2DColorTransparencyPainter}, {@link Grid2DCircularPainter},
 * {@link Grid2DRectangleSizePainter}, and {@link Grid2DEllipsisPainter} side
 * by side on the same color grid -- the last two also shown on a
 * non-square grid to highlight why the ellipsis variant exists.
 *
 * @version 1.2
 * @since 2026
 */
public class Grid2DColorPaintersTester {

	private static final int GRID_SIZE = 6;

	public static void main(String[] args) {
		Color[][] colors = new Color[GRID_SIZE][GRID_SIZE];
		for (int i = 0; i < GRID_SIZE; i++)
			for (int j = 0; j < GRID_SIZE; j++)
				colors[i][j] = new Color(30 + i * 35, 60 + j * 30, 200 - i * 20);

		Grid2DColorPainter colorPainter = new Grid2DColorPainter(colors);
		colorPainter.setBorderPaint(Color.DARK_GRAY);
		colorPainter.setDrawOutline(true);

		float[][] alpha = new float[GRID_SIZE][GRID_SIZE];
		for (int i = 0; i < GRID_SIZE; i++)
			for (int j = 0; j < GRID_SIZE; j++)
				alpha[i][j] = (i + j) / (float) (2 * (GRID_SIZE - 1));
		Grid2DColorTransparencyPainter transparencyPainter = new Grid2DColorTransparencyPainter(colors, alpha);
		transparencyPainter.setBorderPaint(Color.DARK_GRAY);
		transparencyPainter.setDrawOutline(true);
		transparencyPainter.setBackgroundPaint(Color.WHITE);

		double[][] sizes = new double[GRID_SIZE][GRID_SIZE];
		for (int i = 0; i < GRID_SIZE; i++)
			for (int j = 0; j < GRID_SIZE; j++)
				sizes[i][j] = Math.max(0.05, (i + 1) * (j + 1) / (double) (GRID_SIZE * GRID_SIZE));
		Grid2DCircularPainter circularPainter = new Grid2DCircularPainter(sizes, colors);
		circularPainter.setBorderPaint(Color.DARK_GRAY);
		circularPainter.setDrawOutline(true);

		Grid2DRectangleSizePainter sizePainter = new Grid2DRectangleSizePainter(sizes);
		sizePainter.setColor(Color.DARK_GRAY.brighter());
		sizePainter.setBorderPaint(Color.DARK_GRAY);
		sizePainter.setDrawOutline(true);

		// non-square grid: 3 columns x 6 rows, so cells are wider than tall --
		// shows why a locked-aspect circle can't fill a cell the way an ellipse can
		Color[][] wideColors = new Color[3][GRID_SIZE];
		double[][] wideSizes = new double[3][GRID_SIZE];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < GRID_SIZE; j++) {
				wideColors[i][j] = colors[i][j];
				wideSizes[i][j] = Math.max(0.05, (i + 1) * (j + 1) / (double) (3 * GRID_SIZE));
			}

		Grid2DCircularPainter circularOnWideCells = new Grid2DCircularPainter(wideSizes, wideColors);
		circularOnWideCells.setBorderPaint(Color.DARK_GRAY);
		circularOnWideCells.setDrawOutline(true);

		Grid2DEllipsisPainter ellipsisPainter = new Grid2DEllipsisPainter(wideSizes, wideColors);
		ellipsisPainter.setBorderPaint(Color.DARK_GRAY);
		ellipsisPainter.setDrawOutline(true);

		List<JPanel> panels = new ArrayList<>();
		panels.add(labeled("Grid2DColorPainter", colorPainter));
		panels.add(labeled("Grid2DColorTransparencyPainter", transparencyPainter));
		panels.add(labeled("Grid2DCircularPainter", circularPainter));
		panels.add(labeled("Grid2DRectangleSizePainter", sizePainter));
		panels.add(labeled("Grid2DCircularPainter (wide cells)", circularOnWideCells));
		panels.add(labeled("Grid2DEllipsisPainter (wide cells)", ellipsisPainter));

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFramePanelMatrix(panels, "Grid2D color painters"));
	}

	private static JPanel labeled(String title, com.github.TKnudsen.infoVis.view.painters.ChartPainter painter) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		wrapper.add(label);

		InfoVisChartPanel chartPanel = new InfoVisChartPanel();
		chartPanel.addChartPainter(painter);
		chartPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		wrapper.add(chartPanel);

		return wrapper;
	}

}
