package com.github.TKnudsen.infoVis.view.painters.color.test;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.TKnudsen.ComplexDataObject.data.color.ColorHistogram;
import com.github.TKnudsen.ComplexDataObject.model.tools.BufferedImageTools;
import com.github.TKnudsen.ComplexDataObject.model.tools.ColorHistogramTools;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.color.ColorDistributionPainter;
import com.github.TKnudsen.infoVis.view.painters.color.ColorHuePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * Worked example: lets the user pick one or more image files, then renders
 * each one's {@link ColorHistogram} as a combined chart -- a
 * {@link ColorHuePainter} (radial hue chart) layered with two
 * {@link ColorDistributionPainter}s (saturation, brightness), the same
 * composition a caller would build to reproduce the old monolithic
 * ColorHistogramPainter's look.
 * </p>
 *
 * @version 1.0
 * @since 2024
 */
public class ColorHistogramPainterTester {

	public static void main(String[] args) {
		List<File> files = chooseImageFiles();
		if (files == null || files.isEmpty())
			return;

		int cols = (int) Math.ceil(Math.sqrt(files.size()));
		int rows = (int) Math.ceil(files.size() / (double) cols);

		JPanel grid = new JPanel(new GridLayout(rows, cols));

		for (File file : files) {
			BufferedImage bi;
			try {
				bi = BufferedImageTools.loadBufferedImage(file);
			} catch (Exception e) {
				System.err.println("ColorHistogramPainterTester: could not load " + file + ": " + e);
				continue;
			}

			if (bi == null) {
				System.err.println("ColorHistogramPainterTester: could not load " + file);
				continue;
			}

			grid.add(createHistogramPanel(bi));
		}

		SVGFrameTools.dropSVGFrame(grid, "Color Histogram Demo", 800, 800);
	}

	/**
	 * @return the selected files, or null/empty if the dialog was cancelled
	 */
	private static List<File> chooseImageFiles() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select one or more images");
		fc.setMultiSelectionEnabled(true);
		fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "bmp", "gif"));

		if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "No image(s) selected. Aborting.");
			return null;
		}

		return Arrays.asList(fc.getSelectedFiles());
	}

	/**
	 * combines a {@link ColorHuePainter} with two {@link ColorDistributionPainter}s
	 * (saturation, brightness) layered onto one panel.
	 */
	private static InfoVisChartPanel createHistogramPanel(BufferedImage bi) {
		ColorHistogram histo = ColorHistogramTools.getColorHistogram(bi);

		InfoVisChartPanel panel = new InfoVisChartPanel(new ColorHuePainter(histo));
		panel.addChartPainter(
				new ColorDistributionPainter(histo.getSaturations(), ColorDistributionPainter.saturationColorFunction()));
		panel.addChartPainter(
				new ColorDistributionPainter(histo.getBrightnesses(), ColorDistributionPainter.brightnessColorFunction()));

		return panel;
	}

}
