package com.github.TKnudsen.infoVis.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import com.github.TKnudsen.infoVis.view.frames.SVGFrame;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;

public class CategoryLabelingFrame extends SVGFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * input category
	 */
	private final String rawCategory;

	/**
	 * all target categories that are planned for. Do not need to be populated in
	 * the mapping history yet. The other way around: can be kept null, then only
	 * the mapping history will populate the label alphabet.
	 */
	private final Set<String> labelAlphabet;

	/**
	 * for the operation that uses the category labeling.
	 */
	private final Consumer<String> resultConsumer;

	/**
	 * automatic guidance on how likely a potential category may be decided for.
	 */
	private final BiFunction<String, String, Double> categoryGuesser;

	/**
	 * Ask chatGPT for an opinion. Raw category, label alphabet => chatGPT output
	 * with probabilities
	 */
	private final BiFunction<String, Set<String>, String> chatGPTIntegration;

	private final boolean fixedAlphabet;

	private String category = null;
	private JButton button = null;
	protected boolean finished = false;
	private JPanel canvas;
//	private String textAsLabel = "New Label";

	private List<ChangeListener> listeners = new ArrayList<>();

	/**
	 * 
	 * @param category           input category
	 * @param labelAlphabet      can be null/empty, will be extended by the data
	 *                           from the file with existing mappings
	 * @param resultConsumer     for the operation that uses the category labeling
	 * @param guessedCategories  automatic guidance on how likely a potential
	 *                           category may be decided for. Can be null.
	 * @param fixedAlphabet      when fixed the GUI will not allow adding new
	 *                           labels.
	 * @param chatGPTIntegration ask chatGPT for an opinion. Raw category, label
	 *                           alphabet => chatGPT output with probabilities
	 */
	public CategoryLabelingFrame(String category, Set<String> labelAlphabet, Consumer<String> resultConsumer,
			BiFunction<String, String, Double> categoryGuesser, boolean fixedAlphabet,
			BiFunction<String, Set<String>, String> chatGPTIntegration) {
		this.rawCategory = category;
		this.labelAlphabet = labelAlphabet;
		this.resultConsumer = resultConsumer;
		this.categoryGuesser = categoryGuesser;
		this.fixedAlphabet = fixedAlphabet;
		this.chatGPTIntegration = chatGPTIntegration;

		setLayout(new GridLayout(1, 1));
		canvas = new JPanel(new BorderLayout());
		this.add(canvas);

		initialize();

		revalidate();
		repaint();
	}

	private void initialize() {
		setTitle("Chose category for raw category string " + rawCategory
				+ ". Will be used in the next runtime (not now due to the EDT vs sleep problem)");

		//////////////////////
		JPanel chatGPTPanel = new JPanel(new BorderLayout());
		chatGPTPanel.setPreferredSize(new Dimension(400, 0));
		chatGPTPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//		JLabel chatGPTLabel = new JLabel("no ChatGPT integration output");
		JTextPane chatGPTTextPane = new JTextPane();
		chatGPTTextPane.setText("no ChatGPT integration output");
//		chatGPTPanel.add(chatGPTLabel, BorderLayout.CENTER);
		chatGPTPanel.add(chatGPTTextPane, BorderLayout.CENTER);
		canvas.add(chatGPTPanel, BorderLayout.EAST);

		//////////////////////
		JPanel northPanel = new JPanel(new BorderLayout());
		JButton chatGPTButton = new JButton("Ask ChatGPT");
		chatGPTButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (chatGPTIntegration != null) {
//					chatGPTLabel.setText(chatGPTIntegration.apply(rawCategory));
					chatGPTTextPane.setText(chatGPTIntegration.apply(rawCategory, labelAlphabet));
				} else
//					chatGPTLabel.setText("Error: no ChatGPT integration provided");
					chatGPTTextPane.setText("Error: no ChatGPT integration provided");

				revalidate();
				repaint();
			}
		});
		northPanel.add(chatGPTButton, BorderLayout.EAST);

		JLabel rawLabel = new JLabel("Input: " + rawCategory);
		// rawLabel.setPreferredSize(new Dimension(0, 32));
		northPanel.add(rawLabel, BorderLayout.WEST);
		canvas.add(northPanel, BorderLayout.NORTH);

		//////////////////////
		int col = 16;
		int row = labelAlphabet.size();
		col = Math.max(1, (int) (Math.ceil(row / (double) 16)));
		row = (int) (Math.ceil(labelAlphabet.size() / (double) col));
		row = (int) (row * Math.max(1, labelAlphabet.size() / 75.0));

		JPanel alphabetPanel = new JPanel(new GridLayout(1, col));

		int buttonWidth = 30;
		for (String label : labelAlphabet)
			buttonWidth = Math.max(buttonWidth, label.length() * 5);

		JPanel column = new JPanel(new GridLayout(row, 1));
		int i = 0;
		for (String label : labelAlphabet) {
			JLabel l = new JLabel(label);
			l.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					category = label;
					button.setText("Apply " + label + " for " + rawCategory);
				}
			});

			l.setToolTipText(label);
			l.setPreferredSize(new Dimension(buttonWidth, 30));
			l.setMaximumSize(new Dimension(2 * buttonWidth, 30));
			l.setHorizontalAlignment(SwingConstants.LEFT);

			RectanglePainter rectanglePainter = new RectanglePainter();
			rectanglePainter.setBackgroundPaint(null);

			Color color = Color.BLACK;
			if (categoryGuesser != null) {
				double d = categoryGuesser.apply(rawCategory, label);
				color = (!Double.isNaN(d)) ? new Color(55, (int) (255 * d), 55) : Color.BLACK;
			}
			rectanglePainter.setPaint(color);

			InfoVisChartPanel rectangle = new InfoVisChartPanel(rectanglePainter);
			QuadraticPanel q = new QuadraticPanel(rectangle);
			q.setPreferredSize(new Dimension(30, 30));

			JPanel legendSingle = new JPanel(new BorderLayout());
			legendSingle.add(q, BorderLayout.WEST);
			legendSingle.add(l);

			column.add(legendSingle);
			if (i > 0 && (i + 1) % row == 0) {
				alphabetPanel.add(column);
				column = new JPanel(new GridLayout(row, 1));
			}
			i++;
		}

		if (!fixedAlphabet) {
			JTextField textField = new JTextField("");
			textField.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String s = textField.getText();
					category = s;
					button.setText("Apply " + s + " for " + rawCategory);
				}
			});
			textField.setPreferredSize(new Dimension(buttonWidth, 30));
			textField.setMaximumSize(new Dimension(2 * buttonWidth, 30));
			textField.setHorizontalAlignment(SwingConstants.LEFT);
			column.add(textField);
		}

		if (column.getComponentCount() != 0)
			alphabetPanel.add(column);

		canvas.add(alphabetPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 0));

		if (!fixedAlphabet) {
			JButton button1 = new JButton("Input as new label:" + rawCategory);
			button1.setPreferredSize(new Dimension(0, 32));
			button1.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					labelAlphabet.add(rawCategory);
					resultConsumer.accept(rawCategory);

					finished = true;
					for (ChangeListener listener : listeners)
						listener.stateChanged(null);
				}
			});
			buttonsPanel.add(button1);
		}

		button = new JButton("Apply");
		button.setPreferredSize(new Dimension(0, 32));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (category != null) {
					resultConsumer.accept(category);

					finished = true;
					for (ChangeListener listener : listeners)
						listener.stateChanged(null);
				}
			}

		});
		buttonsPanel.add(button);
		canvas.add(buttonsPanel, BorderLayout.SOUTH);
	}

	/**
	 * can be used to dispose the frame externally
	 * 
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		this.listeners.add(listener);
	}

	public String getRawCategory() {
		return rawCategory;
	}
}
