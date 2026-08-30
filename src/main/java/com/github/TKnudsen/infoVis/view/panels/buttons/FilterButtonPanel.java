package com.github.TKnudsen.infoVis.view.panels.buttons;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import com.github.TKnudsen.ComplexDataObject.model.tools.BufferedImageTools;
import com.github.TKnudsen.infoVis.view.painters.buttons.IconBasedButtonPainter;

/**
 * <p>
 * Loads its icon from {@value #filterIconPath}, relative to the working
 * directory, matching the convention used by the app-level {@code icons/}
 * folders elsewhere in this ecosystem (e.g. {@code stocksapi/icons},
 * {@code stocks-explorer/icons}) -- the icon itself is not, and has never
 * been, bundled as a classpath resource inside this library's jar.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class FilterButtonPanel extends InteractiveButtonPanel<IconBasedButtonPainter> {

	private static final long serialVersionUID = 1L;

	private final static String filterIconPath = "icons/filterWhite.png";

	public FilterButtonPanel(Color focusedColor, Color notFocusedColor) {
		super(new IconBasedButtonPainter(focusedColor, notFocusedColor, loadIcon()), "filter");
	}

	private static BufferedImage loadIcon() {
		BufferedImage inputBufferedImage = BufferedImageTools.loadBufferedImage(new File(filterIconPath));
		Image transparentImage = BufferedImageTools.setTransparentColor(inputBufferedImage, Color.WHITE);
		return BufferedImageTools.toBufferedImage(transparentImage);
	}

}
