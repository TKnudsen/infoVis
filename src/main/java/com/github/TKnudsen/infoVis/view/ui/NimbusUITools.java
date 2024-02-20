package com.github.TKnudsen.infoVis.view.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2024 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 */
public class NimbusUITools {

	public static final Font standardFont = new Font("Tahoma", Font.PLAIN, 12);
	public static final Font italicFont = new Font("Tahoma", Font.ITALIC, 12);
	public static final Color standardFontColor = Color.WHITE;
	public static final Color standardBackgroundColor = new Color(45, 45, 45);
	public static final Color baseColor = new Color(0, 0, 0);
	public static final Color textFieldColor = new Color(30, 30, 30);
	public static final Color focusColor = new Color(200, 200, 200);
	public static final Color nimbusBase = new Color(51, 98, 140);

	public static final int STANDARD_BUTTON_SIZE = 32;

	public static void switchToNimbus() {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		// standard background color
		UIManager.put("control", standardBackgroundColor);

		// used for tool tip background color
		UIManager.put("info", nimbusBase);

		// slider and button color
		UIManager.put("nimbusBase", baseColor);

		// text field background color
		UIManager.put("nimbusLightBackground", textFieldColor);

		// text color
		UIManager.put("text", standardFontColor);

		// menu text
		UIManager.put("menuText", standardFontColor);
		UIManager.put("textForeground", standardFontColor);
		UIManager.put("Menu[Enabled].textForeground", standardFontColor);
		UIManager.put("MenuItem[Enabled].textForeground", standardFontColor);

		UIManager.put("nimbusFocus", focusColor);
	}

	/**
	 * problem: UI sometimes produces an exception as follows:
	 * 
	 * java.lang.ClassCastException: class java.awt.Color cannot be cast to class
	 * java.awt.Font (java.awt.Color and java.awt.Font are in module java.desktop of
	 * loader 'bootstrap')
	 * 
	 * workaround: safe environment for JPanel creation
	 * 
	 * @param text
	 * @return JPanel if successful, null instead
	 */
	public static JPanel createJPanel(LayoutManager layoutManager) {
		try {
			return new JPanel(layoutManager);
		} catch (Exception e) {
			// e.printStackTrace();
		}

		try {
			JPanel panel = new JPanel();
			panel.setLayout(layoutManager);
			return panel;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static JPanel createJPanel() {
		return createJPanel(new FlowLayout());
	}

	/**
	 * problem: UI sometimes produces an exception as follows:
	 * 
	 * java.lang.ClassCastException: class java.awt.Color cannot be cast to class
	 * java.awt.Font (java.awt.Color and java.awt.Font are in module java.desktop of
	 * loader 'bootstrap')
	 * 
	 * workaround: safe environment for JLabel creation
	 * 
	 * @param text
	 * @return JLabel if successful, null instead
	 */
	public static JLabel createJLabel(String text) {
		try {
			return new JLabel(text);
		} catch (Exception e) {
			// e.printStackTrace();
		}

		try {
			JLabel label = new JLabel();
			label.setText(text);
			return label;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static JLabel createJLabel() {
		return createJLabel("");
	}

}
