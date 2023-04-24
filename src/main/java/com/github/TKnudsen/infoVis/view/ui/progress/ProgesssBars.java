package com.github.TKnudsen.infoVis.view.ui.progress;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JProgressBar;

public class ProgesssBars {

	public static void resetProgressBar(JProgressBar progressBar, long estimatedTime) {
		String durInMin = new SimpleDateFormat("mm:ss:SSSS").format(new Date(estimatedTime));

		progressBar.setValue(0);
		progressBar.setMaximum(0);
		progressBar.setString(progressBar.getValue() + "/" + progressBar.getMaximum() + ", min: " + durInMin);

		progressBar.repaint();
		progressBar.revalidate();
	}

	public static void updateProgressBar(JProgressBar progressBar, int current, int overallCount,
			long durationInMilliseconds) {
		progressBar.setValue(current);
		progressBar.setMaximum(overallCount);

		String remainingTime = "N/A";
		if (current > 0) {
			long remaining = (long) ((overallCount - current) * (durationInMilliseconds / current));

			int h = (int) (remaining / (60 * 60 * 1000));
			remaining -= (h * 60 * 60 * 1000);
			int min = (int) (remaining / (60 * 1000));
			remaining -= (min * 60 * 1000);
			int sec = (int) (remaining / (1000));

			remainingTime = "";
			if (h < 10)
				remainingTime += "0";
			remainingTime += h + ":";
			if (min < 10)
				remainingTime += "0";
			remainingTime += min + ":";
			if (sec < 10)
				remainingTime += "0";
			remainingTime += sec;
		}

		progressBar.setString(
				"Process: finished " + current + "/" + overallCount + "  Remaining Time (h): " + remainingTime);
		if (current >= overallCount)
			progressBar.setString("Process: " + current + "/" + overallCount + "  Finished");

		progressBar.repaint();
		progressBar.revalidate();
		progressBar.update(progressBar.getGraphics());
	}
}
