package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotSpriteGPU;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotIndexedGPU;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * Shared builder for demos that render the same dataset through all three
 * scatterplot implementations side by side -- {@link ScatterPlot} (CPU),
 * {@link ScatterPlotIndexedGPU} and {@link ScatterPlotSpriteGPU} (both
 * GPU) -- with a single {@link SelectionModel} shared across all three, so a
 * selection made in any one panel (click, rectangle, or lasso via the right
 * mouse button) is reflected in the other two as well.
 * </p>
 *
 * <p>
 * Linked brushing across the three panels needs one thing beyond what each
 * panel's own {@code SelectionHandler} already does: a panel's handler only
 * repaints the panel it is attached to when a selection is made there, so
 * without an explicit shared listener, a selection made in one panel would
 * never visually appear in the other two. {@link #show} adds that listener.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class ScatterPlotSideBySideTesters {

	private ScatterPlotSideBySideTesters() {
	}

	/**
	 * Builds and shows a frame with all three scatterplot implementations
	 * rendering the same data side by side, with linked click/rectangle/lasso
	 * selection.
	 *
	 * @param <T>            the data element type
	 * @param frameTitle     window title
	 * @param data           the data to plot; identical instances are shared by
	 *                       all three panels, which is what makes linked
	 *                       selection possible
	 * @param colorMapping   per-element color
	 * @param worldPositionMappingX per-element X value
	 * @param worldPositionMappingY per-element Y value
	 * @param panelWidth     width of each of the three panels
	 * @param panelHeight    height of each of the three panels
	 * @return the shown frame, in case a caller wants to enable performance
	 *         logging on the individual panels or otherwise configure them
	 *         further (see {@link Panels#cpuPanel}, {@link Panels#indexedGpuPanel},
	 *         {@link Panels#spriteGpuPanel} on the {@link Panels} returned by
	 *         {@link #showAndReturnPanels})
	 */
	public static <T> JFrame show(String frameTitle, List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY,
			int panelWidth, int panelHeight) {
		return showAndReturnPanels(frameTitle, data, colorMapping, worldPositionMappingX, worldPositionMappingY,
				panelWidth, panelHeight).frame;
	}

	/**
	 * Same as {@link #show}, but also returns the three individual panels (e.g.
	 * for enabling performance logging on the GPU ones).
	 */
	public static <T> Panels<T> showAndReturnPanels(String frameTitle, List<T> data,
			Function<? super T, ? extends Paint> colorMapping, Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY, int panelWidth, int panelHeight) {

		SelectionModel<T> selectionModel = SelectionModels.create();

		ScatterPlot<T> cpuPanel = ScatterPlots.create(data, worldPositionMappingX, worldPositionMappingY,
				colorMapping, true, selectionModel);

		ScatterPlotIndexedGPU<T> indexedGpuPanel = new ScatterPlotIndexedGPU<>(data, colorMapping,
				worldPositionMappingX, worldPositionMappingY);
		indexedGpuPanel.setXAxisOverlay(false);
		indexedGpuPanel.setYAxisOverlay(false);
		indexedGpuPanel.setRenderMode(RenderMode.GPU);
		wireInteraction(indexedGpuPanel, selectionModel);

		ScatterPlotSpriteGPU<T> spriteGpuPanel = new ScatterPlotSpriteGPU<>(data, colorMapping,
				worldPositionMappingX, worldPositionMappingY);
		spriteGpuPanel.setXAxisOverlay(false);
		spriteGpuPanel.setYAxisOverlay(false);
		spriteGpuPanel.setRenderMode(RenderMode.GPU);
		wireInteraction(spriteGpuPanel, selectionModel);

		// ZOOM on all three (mouse wheel, double-click to reset) -- independent per
		// panel, not linked across the three like selection is. No pan: it would
		// collide with rectangle selection's left-mouse drag.
		ScatterPlots.addZoomInteraction(cpuPanel);
		ScatterPlots.addZoomInteraction(indexedGpuPanel);
		ScatterPlots.addZoomInteraction(spriteGpuPanel);

		// Linked brushing: a selection made via any one panel's own
		// SelectionHandler only repaints that panel. Repaint all three here so a
		// selection is visible everywhere it applies, not just where it was made.
		selectionModel.addSelectionListener(selectionEvent -> {
			cpuPanel.repaint();
			indexedGpuPanel.repaint();
			spriteGpuPanel.repaint();
		});
		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		JPanel content = new JPanel(new GridLayout(1, 3, 8, 0));
		content.add(labeled("CPU - ScatterPlot", cpuPanel));
		content.add(labeled("GPU - ScatterPlotIndexedGPU", indexedGpuPanel));
		content.add(labeled("GPU - ScatterPlotSpriteGPU", spriteGpuPanel));

		JFrame frame = new JFrame(frameTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(content);
		frame.setSize(panelWidth * 3 + 40, panelHeight + 70);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		return new Panels<>(frame, cpuPanel, indexedGpuPanel, spriteGpuPanel);
	}

	/** The frame plus its three panels, for callers that want to configure them further (e.g. performance logging). */
	public static final class Panels<T> {
		public final JFrame frame;
		public final ScatterPlot<T> cpuPanel;
		public final ScatterPlotIndexedGPU<T> indexedGpuPanel;
		public final ScatterPlotSpriteGPU<T> spriteGpuPanel;

		private Panels(JFrame frame, ScatterPlot<T> cpuPanel, ScatterPlotIndexedGPU<T> indexedGpuPanel,
				ScatterPlotSpriteGPU<T> spriteGpuPanel) {
			this.frame = frame;
			this.cpuPanel = cpuPanel;
			this.indexedGpuPanel = indexedGpuPanel;
			this.spriteGpuPanel = spriteGpuPanel;
		}
	}

	/**
	 * Wires click, rectangle, and lasso (right mouse button) selection for one
	 * GPU panel against a shared selection model -- identical for both GPU
	 * panel families, since both implement the same selection interfaces.
	 */
	private static <T, P extends InfoVisChartPanel & IClickSelection<T> & IRectangleSelection<T>
			& IShapeSelection<T> & ISelectionVisualizer<T>> void wireInteraction(P panel,
			SelectionModel<T> selectionModel) {

		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(panel);
		selectionHandler.setClickSelection(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		panel.setSelectedFunction(t -> selectionHandler.getSelectionModel().isSelected(t));

		LassoSelectionHandler<T> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel, MouseButton.RIGHT);
		lassoSelectionHandler.attachTo(panel);
		lassoSelectionHandler.setShapeSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				lassoSelectionHandler.draw(g2);
			}
		});
	}

	private static JPanel labeled(String title, java.awt.Component panel) {
		JPanel wrapper = new JPanel(new BorderLayout());
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
		label.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
		wrapper.add(label, BorderLayout.NORTH);
		wrapper.add(panel, BorderLayout.CENTER);
		return wrapper;
	}
}
