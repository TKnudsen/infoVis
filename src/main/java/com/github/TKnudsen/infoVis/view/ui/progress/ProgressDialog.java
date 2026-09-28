package com.github.TKnudsen.infoVis.view.ui.progress;

import java.awt.BorderLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

/**
 * <p>
 * Simple modal progress dialog with a progress bar; no remaining references in
 * this codebase.
 * </p>
 *
 * @version 1.1
 * @since 2020
 */
public class ProgressDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JProgressBar progressBar;

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getLblInfolabel() {
		return lblInfolabel;
	}

	private JLabel lblInfolabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ProgressDialog dialog = new ProgressDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ProgressDialog() {
		initialize();
	}

	public ProgressDialog(JFrame f, String s) {
		super(f, s);
		initialize();
	}

	public void initialize() {
		setBounds(100, 100, 541, 194);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		progressBar = new JProgressBar();

		lblInfolabel = new JLabel("infoLabel");
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup().addGap(31)
						.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPanel.createSequentialGroup()
										.addComponent(lblInfolabel, GroupLayout.PREFERRED_SIZE, 379,
												GroupLayout.PREFERRED_SIZE)
										.addContainerGap())
								.addGroup(gl_contentPanel.createSequentialGroup()
										.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
										.addGap(33)))));
		gl_contentPanel
				.setVerticalGroup(
						gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPanel.createSequentialGroup().addGap(22).addComponent(lblInfolabel)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(progressBar,
												GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
										.addContainerGap(66, Short.MAX_VALUE)));
		contentPanel.setLayout(gl_contentPanel);
	}

}
