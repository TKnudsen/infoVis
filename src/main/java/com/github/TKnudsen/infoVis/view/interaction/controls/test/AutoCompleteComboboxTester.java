package com.github.TKnudsen.infoVis.view.interaction.controls.test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.interaction.controls.AutoCompleteComboBox;

public class AutoCompleteComboboxTester {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				List<String> items = new ArrayList<>();
				items.add("-");
				items.add("XxxZxx Snowboarding");
				items.add("AaaBbb Rowing");
				items.add("CccDdd Knitting");
				items.add("Eee Fff Speed reading");
				items.add("Eee Fff Pool");
				items.add("Eee Fff None of the above");

				AutoCompleteComboBox<String> comboBox = new AutoCompleteComboBox<String>(items, false, false);

				comboBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("AutoCompleteComboBox: selected item is now " + comboBox.getSelectedItem());
					}
				});

				JFrame frame = new JFrame();
				frame.setLayout(new GridLayout(0, 1, 10, 10));
				frame.add(comboBox);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLocation(100, 100);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}
