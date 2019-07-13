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

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class SVGFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4033938661812967876L;

	public static final String SCREENSHOT_COMMAND = "screenshot";

	private JMenuBar menuBar;
	private JMenu menu;

	protected Color fontColor = Color.BLACK;

	public SVGFrame() {
		initializeMenu();
	}

	public SVGFrame(String s) {
		super(s);
		initializeMenu();
	}

	public SVGFrame(Color fontColor, String s) {
		super(s);
		this.fontColor = fontColor;
		initializeMenu();
	}

	public SVGFrame(Color fontColor) {
		this.fontColor = fontColor;
		initializeMenu();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (SCREENSHOT_COMMAND.equals(e.getActionCommand())) {
			JPanel contentPane = (JPanel) getContentPane();
			SVGTools.saveSVG(contentPane);
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
		screenCaptureItem.setActionCommand(SCREENSHOT_COMMAND);
		screenCaptureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		screenCaptureItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menu.add(screenCaptureItem);

		// ...for each JMenuItem instance:
		screenCaptureItem.addActionListener(this);

		setJMenuBar(menuBar);
	}
}
