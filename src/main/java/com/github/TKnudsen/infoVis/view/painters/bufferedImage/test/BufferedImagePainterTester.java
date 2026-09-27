package com.github.TKnudsen.infoVis.view.painters.bufferedImage.test;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.TKnudsen.ComplexDataObject.model.tools.BufferedImageTools;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.bufferedImage.BufferedImagePainter;
import com.github.TKnudsen.infoVis.view.painters.bufferedImage.PolaroidImagePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * Worked example: lets the user pick one or more image files, then renders
 * each one twice side by side -- once with a plain {@link BufferedImagePainter}
 * and once with a {@link PolaroidImagePainter} (image plus caption label).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class BufferedImagePainterTester {

	public static void main(String[] args) {
		List<File> files = chooseImageFiles();
		if (files == null || files.isEmpty())
			return;

		JPanel grid = new JPanel(new GridLayout(files.size(), 2));

		for (File file : files) {
			BufferedImage bi;
			try {
				bi = BufferedImageTools.loadBufferedImage(file);
			} catch (Exception e) {
				System.err.println("BufferedImagePainterTester: could not load " + file + ": " + e);
				continue;
			}

			if (bi == null) {
				System.err.println("BufferedImagePainterTester: could not load " + file);
				continue;
			}

			grid.add(new InfoVisChartPanel(new BufferedImagePainter(bi)));
			grid.add(new InfoVisChartPanel(new PolaroidImagePainter(bi, file.getName())));
		}

		SVGFrameTools.dropSVGFrame(grid, "Buffered Image Painter Demo", 800, 800);
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

}
