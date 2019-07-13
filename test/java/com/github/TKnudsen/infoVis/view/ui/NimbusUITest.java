package com.github.TKnudsen.infoVis.view.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class NimbusUITest {

	public static void main(String[] args) {

		NimbusUITools.switchToNimbus();

		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panel1.setBorder(BorderFactory.createTitledBorder("Test Title1"));

		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(1, 2));
		panel2.setBorder(BorderFactory.createTitledBorder("Test Title2"));
		panel1.add(panel2, BorderLayout.CENTER);

		JPanel panel3a = new JPanel();
		panel3a.setBorder(BorderFactory.createTitledBorder("Test Title3a"));
		panel2.add(panel3a);

		JPanel panel3b = new JPanel();
		panel3b.setBorder(BorderFactory.createTitledBorder("Test Title3b"));
		panel2.add(panel3b);

		SVGFrameTools.dropSVGFrame(panel1, "UITest", 800, 600);
	}

}
