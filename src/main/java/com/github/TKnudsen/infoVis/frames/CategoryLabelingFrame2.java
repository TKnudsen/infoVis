package com.github.TKnudsen.infoVis.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.frames.SVGFrame;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;

public class CategoryLabelingFrame2 extends SVGFrame {

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
	 * automatic guidance on how likely a potential category may be decided for.
	 */
	private BiFunction<String, String, Double> categoryGuesser;

	/**
	 * Ask chatGPT for an opinion. Raw category, label alphabet => chatGPT output
	 * with probabilities
	 */
	private BiFunction<String, Set<String>, String> chatGPTIntegration;

	private final boolean fixedAlphabet;
	private final boolean waitWhileFeedbackIsPending;

	private String categoryLabel = null;
	private JButton button = null;
	private SwingWorker<Void, String> worker;
	private final int TIMEOUT_MS = 30000; // 30 seconds timeout
	private long startTime = 0;
	private long pauseStartTime = 0;
	private long pauseHistory = 0;
	private boolean userInputReceived = false;

	private JPanel canvas;
	private JLabel timeRemainingLabel;
	private JProgressBar progressBar;
	private JButton pauseButton;
	private boolean isPaused = false;

	/**
	 * 
	 * @param category                   input category
	 * @param labelAlphabet              can be null/empty, will be extended by the
	 *                                   data from the file with existing mappings
	 * @param categoryGuesser            automatic guidance on how likely a
	 *                                   potential category may be decided for. Can
	 *                                   be null.
	 * @param fixedAlphabet              when fixed the GUI will not allow adding
	 *                                   new labels.
	 * @param waitWhileFeedbackIsPending if the system waits until user feedback is
	 *                                   given (or time is over). Avoids that a)
	 *                                   multiple pop-ups occur and b) that the
	 *                                   system continues without the information
	 *                                   present, e.g., causing
	 *                                   ConcurrentModificationException. If the
	 *                                   feedback is planned for the next runtime,
	 *                                   then do not wait.
	 * @param chatGPTIntegration         ask chatGPT for an opinion. Raw category,
	 *                                   label alphabet => chatGPT output with
	 *                                   probabilities
	 */
	public CategoryLabelingFrame2(String category, Set<String> labelAlphabet,
			BiFunction<String, String, Double> categoryGuesser, boolean fixedAlphabet,
			boolean waitWhileFeedbackIsPending, BiFunction<String, Set<String>, String> chatGPTIntegration) {
		this.rawCategory = category;
		this.labelAlphabet = labelAlphabet;
		this.categoryGuesser = categoryGuesser;
		this.fixedAlphabet = fixedAlphabet;
		this.waitWhileFeedbackIsPending = waitWhileFeedbackIsPending;
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
		JTextPane chatGPTTextPane = new JTextPane();
		chatGPTTextPane.setText("no ChatGPT integration output");
		chatGPTPanel.add(chatGPTTextPane, BorderLayout.CENTER);
		canvas.add(chatGPTPanel, BorderLayout.EAST);

		//////////////////////
		JPanel northPanel = new JPanel(new BorderLayout());
		JButton chatGPTButton = new JButton("Ask ChatGPT");
		chatGPTButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (chatGPTIntegration != null) {
					chatGPTTextPane.setText(chatGPTIntegration.apply(rawCategory, labelAlphabet));
				} else
					chatGPTTextPane.setText("Error: no ChatGPT integration provided");

				revalidate();
				repaint();
			}
		});
		northPanel.add(chatGPTButton, BorderLayout.EAST);

		JLabel rawLabel = new JLabel("Input: " + rawCategory);
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
					categoryLabel = label;
					button.setText("Apply " + label + " for " + rawCategory);

					if (e.getClickCount() == 2) {
						userInputReceived = true;
						worker.cancel(true);
						dispose();
					}
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
					categoryLabel = s;
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

		JPanel south = new JPanel(new GridLayout(1, 0));
		JPanel southWest = new JPanel(new GridLayout(1, 0));

		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
		timePanel.add(new JLabel("Time remaining: "));
		timeRemainingLabel = new JLabel((TIMEOUT_MS / 1000) + "", SwingConstants.RIGHT);
		timePanel.add(timeRemainingLabel);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setString("0% ");
		timePanel.add(progressBar);
		southWest.add(timePanel);

		pauseButton = new JButton("Pause");
		pauseButton.setEnabled(true);
		pauseButton.addActionListener(e -> togglePause());
		southWest.add(pauseButton);

		if (!fixedAlphabet) {
			JButton button1 = new JButton("Input as new label:" + rawCategory);
			button1.setPreferredSize(new Dimension(0, 32));
			button1.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					labelAlphabet.add(rawCategory);
					categoryLabel = new String(rawCategory);
					button.setText(rawCategory + " => " + categoryLabel);
				}
			});
			southWest.add(button1);
		}
		south.add(southWest);

		JButton other = new JButton(rawCategory + " => 'Other'");
		other.setPreferredSize(new Dimension(0, 32));
		other.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				labelAlphabet.add("Other");
				categoryLabel = "Other";
				userInputReceived = true;
				worker.cancel(true);
				dispose();
			}

		});
		south.add(other);

		button = new JButton("Apply");
		button.setPreferredSize(new Dimension(0, 32));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				userInputReceived = true;
				worker.cancel(true);
				dispose();
			}

		});
		south.add(button);

		canvas.add(south, BorderLayout.SOUTH);

		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(true);
				dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});

		initializeWorker();
	}

	private void initializeWorker() {
		startTime = System.currentTimeMillis();

		worker = new SwingWorker<Void, String>() {
			@Override
			protected Void doInBackground() throws InterruptedException {
				System.out.println("doInBackground");

				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - startTime;
				long remainingTime = TIMEOUT_MS - elapsedTime;
				remainingTime = (long) (MathFunctions.round(remainingTime / 1000d, 2) * 1000);

				if (waitWhileFeedbackIsPending) {
					while (!userInputReceived && remainingTime > 0) {
						publish(formatTime(remainingTime));

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {

						}

						currentTime = System.currentTimeMillis();
						elapsedTime = currentTime - startTime;

						elapsedTime -= pauseHistory;
						if (isPaused) {
							long pauseDurationCurrent = currentTime - pauseStartTime;
							elapsedTime -= pauseDurationCurrent;
						}
						remainingTime = TIMEOUT_MS - elapsedTime;
					}
				} else {
					while (!userInputReceived && remainingTime > 0) {
						publish(formatTime(remainingTime));

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							System.out.println("CategoryLabelingFrame: doInBackground sleep interrupted");
						}

						currentTime = System.currentTimeMillis();
						elapsedTime = currentTime - startTime;

						elapsedTime -= pauseHistory;
						if (isPaused) {
							long pauseDurationCurrent = currentTime - pauseStartTime;
							elapsedTime -= pauseDurationCurrent;
						}
						remainingTime = TIMEOUT_MS - elapsedTime;
					}
				}

				return null;
			}

			@Override
			protected void process(List<String> chunks) {
				// System.out.println("process");

				for (String message : chunks) {
					String m = message.length() <= 6 ? message : message.substring(0, 6);
					SwingUtilities.invokeLater(() -> timeRemainingLabel.setText(m));
				}

				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - startTime;
				elapsedTime -= pauseHistory;
				if (isPaused) {
					long pauseDurationCurrent = currentTime - pauseStartTime;
					elapsedTime -= pauseDurationCurrent;
				}
				long remainingTime = TIMEOUT_MS - elapsedTime;

				remainingTime = Math.max(0, remainingTime);
				int val = (int) Math.ceil((1 - remainingTime / (double) TIMEOUT_MS) * 100);

				SwingUtilities.invokeLater(() -> {
					progressBar.setValue(val);
					progressBar.setString(val + "%");
				});
			}

			@Override
			protected void done() {
				System.out.println("done");

				dispose();

				worker = null;
				categoryGuesser = null;
				chatGPTIntegration = null;
			}
		};

		worker.execute();
	}

	private void togglePause() {
		isPaused = !isPaused;
		if (isPaused) {
			pauseStartTime = System.currentTimeMillis();
			pauseButton.setText("Resume");
			progressBar.setString("Paused");
		} else {
			long currentTime = System.currentTimeMillis();
			pauseHistory += (currentTime - pauseStartTime);
			pauseButton.setText("Pause");
		}
	}

	private String formatTime(long milliseconds) {
		if (milliseconds < 0)
			return "---";
		Duration duration = Duration.ofMillis(milliseconds);
		String milli = duration.toMillisPart() + "";
		while (milli.length() < 3)
			milli = milli + "0";
		long seconds = (long) (milliseconds / 1000.0);
		return seconds + ":" + milli;
//		return  duration.toSecondsPart() + ":" + milli;
	}

	public void waitUntilDone() throws InterruptedException {
		if (!userInputReceived)
			try {
				if (worker != null)
					worker.get();
			} catch (InterruptedException | ExecutionException x) {
				x.printStackTrace();
			} catch (CancellationException e) {
				System.out.println("CategoryLabelingFrame: waitUntilDone cancelation");
			}
	}

	public String getRawCategory() {
		return rawCategory;
	}

	public String getCategoryLabel() {
		return categoryLabel;
	}
}
