package com.github.TKnudsen.infoVis.view.frames;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.github.TKnudsen.infoVis.view.tools.SVGTools;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationTheme;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeManager;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeManager.Subscription;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeManager.ThemePreset;

/**
 * <p>
 * InfoVis
 * 
 * With 1.03 setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE) was added, to
 * encourage Swing to clear native resources and break internal references.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public class SVGFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4033938661812967876L;

	private static final String MENU_SCREENSHOT = "SCREENSHOT";

	private static final String MENU_FONT_INCREASE = "FONT_INCREASE";
	private static final String MENU_FONT_DECREASE = "FONT_DECREASE";
	private static final String MENU_FONT_RESET = "FONT_RESET";

	// ==================== THEME MANAGEMENT ====================

	protected final VisualizationThemeManager themeManager;
	protected volatile VisualizationTheme currentTheme;
	protected Subscription themeSubscription;

	// ==================== MENU MANAGEMENT ====================

	private JMenuBar menuBar;
	private JMenu menu;

	protected final Color fontColor;

	public SVGFrame() {
		this(Color.BLACK, "");
	}

	public SVGFrame(String s) {
		this(Color.BLACK, s);
	}

	public SVGFrame(Color fontColor) {
		this(fontColor, "");
	}

	public SVGFrame(Color fontColor, String s) {
		super(s);

		// ========== THEME SYSTEM INITIALIZATION (FIRST!) ==========

		// Get theme manager singleton
		this.themeManager = VisualizationThemeManager.getInstance();

		// Set initial theme (uses NimbusUITools integration)
		themeManager.setThemePreset(ThemePreset.NIMBUS_UI_TOOLS, false);
		this.currentTheme = themeManager.getTheme();

		// ========== INITIALIZATION ==========

		this.fontColor = fontColor;
		initializeMenu();

		// that way Swing itself clears native resources and breaks internal references.
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (MENU_SCREENSHOT.equals(command)) {
			JPanel contentPane = (JPanel) getContentPane();
			SVGTools.saveSVG(contentPane);
		}

		// Handle menu actions
		switch (command) {

		case MENU_FONT_INCREASE:
			themeManager.increaseFontSize();
			break;

		case MENU_FONT_DECREASE:
			themeManager.decreaseFontSize();
			break;

		case MENU_FONT_RESET:
			themeManager.setBaseFontSize(12);
			break;
		}
	}

	protected void initializeMenu() {
		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Menu");
		menu.setForeground(fontColor);
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
		menuBar.add(menu);

		// a group of JMenuItems
		JMenuItem screenCaptureItem;
		screenCaptureItem = new JMenuItem("Take Screenshot", KeyEvent.VK_P);
		screenCaptureItem.setForeground(fontColor);
		screenCaptureItem.setActionCommand(MENU_SCREENSHOT);
		screenCaptureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		screenCaptureItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menu.add(screenCaptureItem);

		// ...for each JMenuItem instance:
		screenCaptureItem.addActionListener(this);

		// ...theme menu
		menuBar.add(createThemeMenu());

		// ...font menu
		menuBar.add(createFontMenu());

		setJMenuBar(menuBar);
	}

	/**
	 * Creates the Theme menu.
	 */
	private JMenu createThemeMenu() {
		JMenu menu = new JMenu("Theme");
		menu.setMnemonic(KeyEvent.VK_T);

		// Theme presets (type-safe via enum)
		for (ThemePreset preset : ThemePreset.values()) {
			JMenuItem item = new JMenuItem(preset.name().replace('_', ' '));
			item.addActionListener(e -> {
				themeManager.setThemePreset(preset, true); // Preserve metrics
			});
			menu.add(item);
		}

		return menu;
	}

	/**
	 * Creates the View menu.
	 */
	private JMenu createFontMenu() {
		JMenu menu = new JMenu("Font");
		menu.setMnemonic(KeyEvent.VK_F);

		JMenuItem increaseFontSize = new JMenuItem("Increase Font Size");
		increaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));
		increaseFontSize.setActionCommand(MENU_FONT_INCREASE);
		increaseFontSize.addActionListener(this);
		menu.add(increaseFontSize);

		JMenuItem decreaseFontSize = new JMenuItem("Decrease Font Size");
		decreaseFontSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
		decreaseFontSize.setActionCommand(MENU_FONT_DECREASE);
		decreaseFontSize.addActionListener(this);
		menu.add(decreaseFontSize);

		JMenuItem resetFontSize = new JMenuItem("Reset Font Size");
		resetFontSize.setActionCommand(MENU_FONT_RESET);
		resetFontSize.addActionListener(this);
		menu.add(resetFontSize);

		return menu;
	}
}
