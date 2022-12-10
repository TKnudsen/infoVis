package com.github.TKnudsen.infoVis.view.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
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
}
