package com.github.TKnudsen.infoVis.view.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * <p>
 * Utility methods for configuring Nimbus look and feel and common Swing
 * components.
 * </p>
 *
 * @version 1.20
 * @since 2018
 */
public final class NimbusUITools {

//	/**
//	 * use getter
//	 */
//	private static final Font standardFont = new Font("Tahoma", Font.PLAIN, 12);
//
//	/**
//	 * use getter
//	 */
//	private static final Font boldFont = new Font("Tahoma", Font.BOLD, 12);
//
//	/**
//	 * use getter
//	 */
//	private static final Font italicFont = new Font("Tahoma", Font.ITALIC, 12);

	/**
	 * @deprecated use getter
	 */
	public static final Color standardFontColor = Color.WHITE;
	public static final Color standardBackgroundColor = new Color(45, 45, 45);
	public static final Color baseColor = new Color(0, 0, 0);
	public static final Color textFieldColor = new Color(30, 30, 30);
	public static final Color focusColor = new Color(200, 200, 200);
	public static final Color nimbusBase = new Color(51, 98, 140);
	/**
	 * @deprecated use getter
	 */
	public static final Color backgroundColor = UIManager.getColor("nimbusLightBackground") != null
			? UIManager.getColor("nimbusLightBackground")
			: new Color(30, 30, 30);

	/**
	 * @deprecated use getter
	 */
	public static final Color borderColor = new Color(15, 15, 15);

	public static final int STANDARD_BUTTON_SIZE = 32;

	private static final Logger LOGGER = Logger.getLogger(NimbusUITools.class.getName());

	// scale factor for fonts and sizes
	private static double scaleFactor = 1.0;

	// base font snapshot to avoid compounding scaling
	private static Font baseLabelFont;

	private NimbusUITools() {
		throw new AssertionError("Do not instantiate");
	}

	/**
	 * Sets the global UI scale factor. Values larger than 1 increase font size and
	 * component sizes. Values below 1 decrease them. Call this on the EDT.
	 */
	public static void setScaleFactor(double factor) {
		if (factor <= 0.1) {
			throw new IllegalArgumentException("scaleFactor too small");
		}
		scaleFactor = factor;

		// re-apply scaled fonts and update existing windows
		applyScaledFonts();
		updateAllWindows();
	}

	public static double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * 
	 * @return
	 */
	public static Color getBackgroundColor() {
		Color c = UIManager.getColor("nimbusLightBackground");
		return c != null ? c : new Color(30, 30, 30);
	}

	/**
	 * 
	 * @return
	 */
	public static Color getBorderColor() {
		Color c = new Color(15, 15, 15);
		return c;
	}

	/**
	 * 
	 * @return
	 */
	public static Color getFonColor() {
		Color c = Color.WHITE;
		return c;
	}

	/**
	 * Returns a system derived standard font scaled by scaleFactor. Uses the stored
	 * baseLabelFont if available.
	 */
	public static Font getStandardFont() {
		Font base = getBaseLabelFont();
		return base.deriveFont((float) (base.getSize2D() * scaleFactor));
	}

	/**
	 * Returns a system derived bold font scaled by scaleFactor. Uses the stored
	 * baseLabelFont if available.
	 */
	public static Font getBoldFont() {
		Font base = getBaseLabelFont();
		return base.deriveFont(Font.BOLD, (float) (base.getSize2D() * scaleFactor));
	}

	/**
	 * Returns a system derived italic font scaled by scaleFactor.
	 */
	public static Font getItalicFont() {
		Font base = getBaseLabelFont();
		return base.deriveFont(Font.ITALIC, (float) (base.getSize2D() * scaleFactor));
	}

	public static int getStandardButtonSize() {
		return (int) Math.round(STANDARD_BUTTON_SIZE * scaleFactor);
	}

	/**
	 * Switches to Nimbus look and feel and configures a dark theme. Call this on
	 * the EDT.
	 */
	public static void switchToNimbus() {
		if (!setNimbusLookAndFeel()) {
			return;
		}
		configureNimbusDefaults();
		// applyScaledFonts();
	}

	private static boolean setNimbusLookAndFeel() {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
					return true;
				} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException
						| IllegalAccessException e) {
					LOGGER.log(Level.WARNING, "Failed to set Nimbus look and feel", e);
					return false;
				}
			}
		}
		LOGGER.warning("Nimbus look and feel not available");
		return false;
	}

	private static void configureNimbusDefaults() {
		UIManager.put("control", standardBackgroundColor);
		UIManager.put("info", nimbusBase);
		UIManager.put("nimbusBase", baseColor);
		UIManager.put("nimbusLightBackground", textFieldColor);
		UIManager.put("text", getFonColor());

		UIManager.put("menuText", getFonColor());
		UIManager.put("textForeground", getFonColor());
		UIManager.put("Menu[Enabled].textForeground", getFonColor());
		UIManager.put("MenuItem[Enabled].textForeground", getFonColor());

		UIManager.put("nimbusFocus", focusColor);

		// always capture base label font from *current* LAF (Nimbus)
		Font lfFont = UIManager.getFont("Label.font");
		if (lfFont == null) {
			lfFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		}
		baseLabelFont = lfFont;

		applyScaledFonts();
	}

	private static void applyScaledFonts() {
		if (baseLabelFont == null) {
			Font lfFont = UIManager.getFont("Label.font");
			if (lfFont == null) {
				lfFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			}
			baseLabelFont = lfFont;
		}

		Font scaled = baseLabelFont.deriveFont((float) (baseLabelFont.getSize2D() * scaleFactor));

		String[] fontKeys = { "defaultFont",

				"Label.font", "Button.font", "ToggleButton.font", "CheckBox.font", "RadioButton.font", "ComboBox.font",
				"List.font", "Table.font", "TableHeader.font", "Tree.font", "Spinner.font",

				"TextField.font", "PasswordField.font", "FormattedTextField.font", "TextArea.font",

				"TabbedPane.font", "TitledBorder.font", "ToolTip.font",

				"MenuBar.font", "Menu.font", "MenuItem.font", "CheckBoxMenuItem.font", "RadioButtonMenuItem.font",
				"PopupMenu.font" };

		for (String key : fontKeys) {
			UIManager.put(key, scaled);
		}
	}

	/**
	 * Updates all existing windows to reflect new UIManager settings. Call this
	 * only on the EDT.
	 * 
	 * @param pack
	 */
	private static void updateAllWindows() {
		for (Window window : Window.getWindows()) {
			if (!window.isDisplayable())
				continue;

			SwingUtilities.updateComponentTreeUI(window);
			window.invalidate();
			window.validate();
			window.repaint();

			if (window instanceof JDialog)
				window.pack();
		}
	}

	private static Font getBaseLabelFont() {
		if (baseLabelFont == null) {
			Font lfFont = UIManager.getFont("Label.font");
			if (lfFont == null) {
				lfFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			}
			baseLabelFont = lfFont;
		}
		return baseLabelFont;
	}

	public static JPanel createJPanel(LayoutManager layoutManager) {
		if (layoutManager == null) {
			throw new IllegalArgumentException("layoutManager must not be null");
		}
		return new JPanel(layoutManager);
	}

	public static JPanel createJPanel() {
		return new JPanel(new FlowLayout());
	}

	public static JLabel createJLabel(String text) {
		return new JLabel(text);
	}

	public static JLabel createJLabel() {
		return new JLabel();
	}
}
