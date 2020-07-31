package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class JLabels {

	/**
	 * turns the text to bold font
	 * 
	 * @param label
	 */
	public static void boldify(JLabel label) {
		label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | Font.BOLD));
	}

	/**
	 * 
	 * @param text       enter the text to be displayed here
	 * @param boldify    as to whether the text shall be displayd with bold font
	 * @param preferredX set width or leave null
	 * @param preferredY set height or leave null
	 * @param xAlignment use SwingConstants or leave null
	 * @param yAlignment use SwingConstants or leave null
	 * @return
	 */
	public static JLabel create(String text, boolean boldify, Integer preferredX, Integer preferredY,
			Integer xAlignment, Integer yAlignment) {
		JLabel jLabel = new JLabel(text);

		if (boldify)
			JLabels.boldify(jLabel);

		if (preferredX != null && preferredY != null)
			jLabel.setPreferredSize(new Dimension(preferredX.intValue(), preferredY.intValue()));

		if (xAlignment != null)
			jLabel.setHorizontalAlignment(xAlignment.intValue());

		if (yAlignment != null)
			jLabel.setHorizontalAlignment(yAlignment.intValue());

		return jLabel;
	}
}
