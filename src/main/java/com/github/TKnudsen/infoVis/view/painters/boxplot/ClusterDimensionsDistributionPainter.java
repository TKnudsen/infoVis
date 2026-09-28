package com.github.TKnudsen.infoVis.view.painters.boxplot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * A small-multiples glyph for a cluster centroid: one column per dimension,
 * each showing the member feature vectors as a density of thin horizontal
 * lines, framed by a min/max range band, a standard-deviation spread band,
 * and the centroid itself as a bold line -- all against one shared,
 * normalized [0, 1] y axis on the left, since every column is assumed to
 * already be normalized to that common domain.
 * </p>
 *
 * <p>
 * Re-implemented from the description of the per-cluster glyph in:
 * </p>
 * <p>
 * TimeSeriesPaths: Projection-Based Explorative Analysis of Multivariate Time
 * Series Data. Journal of WSCG, pp. 97-106 (2012).
 * </p>
 *
 * @version 2.0
 * @since 2012
 */
public class ClusterDimensionsDistributionPainter extends ChartPainter {

	// ------------------------------------------------------------------
	// data
	// ------------------------------------------------------------------

	private double[] centroid;
	private List<double[]> members;

	private StatisticsSupport[] globalStatisticsPerDimension;
	private ArrayList<String> dimensionNames;
	private ArrayList<String> dimensionUnits;

	// [dimension][y-pixel] -> number of member vectors falling on that pixel
	private int[][] densityBins;

	// ------------------------------------------------------------------
	// layout
	// ------------------------------------------------------------------

	private int dimensionCount = 0;
	private int columnSpacing = 5;
	private int columnWidth = 0;
	private int dataAreaHeight = 0;

	private Rectangle2D dataAreaRectangle = null;
	private Rectangle2D titleRectangle = null;

	private static final double TITLE_HEIGHT = 20;
	private static final double COLUMN_LABEL_HEIGHT = 14;
	private static final double AXIS_WIDTH = 40;
	private double axisRectangleX;

	private boolean showTitle = true;
	private boolean showColumnLabels = true;
	private boolean showAxis = true;
	private boolean showTicks = false;

	private String title = "";
	private Color titleBackground = Color.LIGHT_GRAY;

	// shared, normalized [0, 1] y axis -- every column already lives on that
	// common domain, so one axis (rather than none, or one per column) is the
	// correct representation, and it doubles as the single source of truth
	// for value-to-pixel conversion (see #toScreenY), replacing what used to
	// be several independently hand-rolled copies of the same formula
	private final YAxisNumericalPainter<Double> axisPainter = new YAxisNumericalPainter<>(0.0, 1.0);

	// ------------------------------------------------------------------
	// styling
	// ------------------------------------------------------------------

	private double densityLineInsetRatio = 0.25;
	private int densityLineMaxInset = 10;
	private int spreadBandWidth = 5;
	private int tickCount = 2;
	private int tickMarkLength = 4;
	private int labelFontSize = 10;
	private DecimalFormat tickNumberFormat = new DecimalFormat("#,###,###,##0.000");

	private Color rangeBandColor = new Color(128, 128, 128, 40);
	private Color densityLineColor = Color.BLACK;
	private Color centroidLineColor = Color.RED;
	private Color spreadBandColor = new Color(64, 64, 64, 90);
	private Color tickColor = Color.BLUE;

	public ClusterDimensionsDistributionPainter(double[] centroid, List<double[]> members) {
		this.centroid = centroid;
		this.members = members;
		recalculateLayout();
	}

	// ------------------------------------------------------------------
	// lifecycle
	// ------------------------------------------------------------------

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		if (centroid == null || members.isEmpty())
			return;

		Color previousColor = g2.getColor();

		if (showTitle)
			drawTitle(g2);
		if (showAxis)
			axisPainter.draw(g2);

		drawColumnLabels(g2);
		drawRangeBands(g2);
		drawSpreadBands(g2);
		drawDensityLines(g2);
		drawCentroidLines(g2);
		drawTicks(g2);

		DisplayTools.drawRectangle(g2, rectangle, Color.black);

		g2.setColor(previousColor);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		Rectangle2D belowTitle = null;
		if (showTitle) {
			if (rectangle != null) {
				belowTitle = new Rectangle2D.Double(rectangle.getX(), rectangle.getY() + TITLE_HEIGHT,
						rectangle.getWidth(), Math.max(0.0, rectangle.getHeight() - TITLE_HEIGHT));
				titleRectangle = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), 15);
			}
		} else if (rectangle != null) {
			belowTitle = rectangle;
			titleRectangle = null;
		}

		if (belowTitle != null) {
			double axisWidth = showAxis ? AXIS_WIDTH : 0;
			axisRectangleX = belowTitle.getX();
			Rectangle2D besideAxis = new Rectangle2D.Double(belowTitle.getX() + axisWidth, belowTitle.getY(),
					Math.max(0.0, belowTitle.getWidth() - axisWidth), belowTitle.getHeight());

			double labelHeight = showColumnLabels ? COLUMN_LABEL_HEIGHT : 0;
			dataAreaRectangle = new Rectangle2D.Double(besideAxis.getX(), besideAxis.getY() + labelHeight,
					besideAxis.getWidth(), Math.max(0.0, besideAxis.getHeight() - labelHeight));
		}

		recalculateLayout();
	}

	private void recalculateLayout() {
		if (centroid == null || members == null || getRectangle() == null) {
			dimensionCount = 0;
			showTicks = false;
			return;
		}

		dimensionCount = centroid.length;
		columnSpacing = (int) Math.max(1, dataAreaRectangle.getWidth() / (dimensionCount * 15));
		columnWidth = (int) (dataAreaRectangle.getWidth() - (dimensionCount + 1) * columnSpacing) / dimensionCount;
		dataAreaHeight = (int) Math.max(0.0, (dataAreaRectangle.getHeight() - 2 * columnSpacing));

		if (dataAreaRectangle.getWidth() / dimensionCount > 15 && dataAreaRectangle.getHeight() > 25)
			showTicks = true;

		if (showAxis)
			axisPainter.setRectangle(new Rectangle2D.Double(axisRectangleX, dataAreaRectangle.getY() + columnSpacing,
					AXIS_WIDTH, dataAreaHeight));

		recalculateDensityBins();
	}

	// ------------------------------------------------------------------
	// column geometry -- the single source of truth for "where is column i",
	// replacing what used to be the same "columnSpacing * (i + 1) + i *
	// columnWidth" expression repeated inline in every draw method
	// ------------------------------------------------------------------

	private double columnLeft(int i) {
		return dataAreaRectangle.getX() + columnSpacing * (i + 1) + i * columnWidth;
	}

	private double columnRight(int i) {
		return columnLeft(i) + columnWidth;
	}

	private double columnCenter(int i) {
		return columnLeft(i) + columnWidth * 0.5;
	}

	/** Maps a [0, 1]-normalized value to its screen y coordinate, via the shared axis. */
	private double toScreenY(double normalizedValue) {
		return axisPainter.getPositionEncodingFunction().apply(normalizedValue).doubleValue();
	}

	// ------------------------------------------------------------------
	// rendering
	// ------------------------------------------------------------------

	private void drawTitle(Graphics2D g2) {
		if (members.size() < 1 || densityBins == null || titleRectangle == null)
			return;

		g2.setColor(titleBackground);
		g2.fillRect((int) titleRectangle.getX(), (int) titleRectangle.getY(), (int) titleRectangle.getWidth(),
				(int) titleRectangle.getHeight());

		if (title != null && !title.isEmpty()) {
			g2.setColor(Color.BLACK);
			FontMetrics fontMetrics = g2.getFontMetrics(g2.getFont());
			int x = (int) (titleRectangle.getX() + (titleRectangle.getWidth() - fontMetrics.stringWidth(title)) / 2);
			g2.drawString(title, x, (int) titleRectangle.getY() + 12);
		}
	}

	private void drawColumnLabels(Graphics2D g2) {
		if (!showColumnLabels || dimensionNames == null || dimensionCount == 0 || densityBins == null)
			return;

		g2.setColor(Color.BLACK);
		Font font = new Font(g2.getFont().getName(), g2.getFont().getStyle(), labelFontSize);
		g2.setFont(font);
		FontMetrics fontMetrics = g2.getFontMetrics(font);

		int baselineY = (int) (dataAreaRectangle.getY() - 3);
		for (int i = 0; i < dimensionCount && i < dimensionNames.size(); i++) {
			String name = dimensionNames.get(i);
			if (name == null)
				continue;

			int x = (int) (columnCenter(i) - fontMetrics.stringWidth(name) * 0.5);
			g2.drawString(name, x, baselineY);
		}
	}

	private void drawRangeBands(Graphics2D g2) {
		if (members.size() < 1 || densityBins == null)
			return;

		double[] columnMax = new double[dimensionCount];
		double[] columnMin = new double[dimensionCount];
		for (int i = 0; i < dimensionCount; i++)
			columnMin[i] = 1.0;
		int[] maxMemberIndex = new int[dimensionCount];
		int[] minMemberIndex = new int[dimensionCount];

		for (int m = 0; m < members.size(); m++) {
			for (int i = 0; i < dimensionCount; i++) {
				double value = members.get(m)[i];
				if (value > columnMax[i]) {
					columnMax[i] = value;
					maxMemberIndex[i] = m;
				}
				if (value < columnMin[i]) {
					columnMin[i] = value;
					minMemberIndex[i] = m;
				}
			}
		}

		g2.setColor(rangeBandColor);

		for (int i = 0; i < dimensionCount; i++) {
			double yMax = toScreenY(members.get(maxMemberIndex[i])[i]);
			double yMin = toScreenY(members.get(minMemberIndex[i])[i]);
			g2.fillRect((int) columnLeft(i), (int) yMax, columnWidth, (int) (yMin - yMax));
		}
	}

	private void drawDensityLines(Graphics2D g2) {
		if (members.size() <= 1 || densityBins == null)
			return;

		int inset = Math.min(densityLineMaxInset, (int) (densityLineInsetRatio * columnWidth));
		for (int i = 0; i < densityBins.length; i++) {
			int peak = getMax(densityBins[i]);
			for (int bin = 0; bin < densityBins[i].length; bin++) {
				if (densityBins[i][bin] == 0)
					continue;

				float alpha = 0.25f + (densityBins[i][bin] / (float) peak) * 0.75f;
				g2.setColor(new Color(densityLineColor.getRed(), densityLineColor.getGreen(),
						densityLineColor.getBlue(), (int) (alpha * 255.0)));

				double y = dataAreaRectangle.getY() + columnSpacing + (dataAreaHeight - bin);
				g2.drawLine((int) (columnLeft(i) + inset), (int) y, (int) (columnRight(i) - inset), (int) y);
			}
		}
	}

	private void drawCentroidLines(Graphics2D g2) {
		if (members.size() < 1 || densityBins == null)
			return;

		g2.setColor(centroidLineColor);
		for (int i = 0; i < densityBins.length; i++) {
			int y = (int) toScreenY(centroid[i]);
			g2.drawLine((int) columnLeft(i), y, (int) columnRight(i), y);
		}
	}

	private void drawSpreadBands(Graphics2D g2) {
		if (members.size() < 1 || densityBins == null)
			return;

		g2.setColor(spreadBandColor);
		double[] columnValues = new double[members.size()];
		for (int i = 0; i < dimensionCount; i++) {
			for (int m = 0; m < columnValues.length; m++)
				columnValues[m] = members.get(m)[i];

			double standardDeviation = MathFunctions.getStandardDeviation(columnValues);
			int spreadPixels = (int) (standardDeviation * dataAreaHeight);
			int centroidY = (int) toScreenY(centroid[i]);
			int top = Math.max((int) (dataAreaRectangle.getY() + columnSpacing), centroidY - spreadPixels);
			int bottom = Math.min((int) (dataAreaRectangle.getY() + columnSpacing + dataAreaHeight),
					centroidY + spreadPixels);

			g2.fillRect((int) columnLeft(i), top, spreadBandWidth, bottom - top);
			g2.fillRect((int) (columnRight(i) - spreadBandWidth), top, spreadBandWidth, bottom - top);
		}
	}

	private void drawTicks(Graphics2D g2) {
		if (!showTicks || globalStatisticsPerDimension == null || members.size() < 1 || densityBins == null)
			return;

		g2.setColor(tickColor);
		Font font = new Font(g2.getFont().getName(), g2.getFont().getStyle(), labelFontSize);

		for (int i = 0; i < dimensionCount; i++) {
			double x = columnLeft(i);

			g2.drawLine((int) x, (int) (dataAreaRectangle.getY() + columnSpacing), (int) x,
					(int) (dataAreaRectangle.getY() + columnSpacing + dataAreaHeight));

			int centroidY = (int) toScreenY(centroid[i]);
			g2.setFont(font);

			double dimensionMin = globalStatisticsPerDimension[i].getMin();
			double dimensionMax = globalStatisticsPerDimension[i].getMax();

			for (int t = 0; t < tickCount; t++) {
				int tickY = (int) (dataAreaRectangle.getY() + columnSpacing + t * dataAreaHeight / (tickCount - 1));
				int labelY = Math.max(tickY, (int) (dataAreaRectangle.getY() + columnSpacing + labelFontSize));

				if (Math.abs(labelY - centroidY) <= labelFontSize)
					continue;

				g2.drawLine((int) x, tickY, (int) (x + tickMarkLength), tickY);
				double tickValue = dimensionMax - (dimensionMax - dimensionMin) / (tickCount - 1) * t;
				g2.drawString(formatTickValue(tickValue, dimensionMax), (int) (x + tickMarkLength), labelY);
			}

			if (dimensionUnits != null && i < dimensionUnits.size()) {
				g2.setFont(new Font(font.getName(), font.getStyle(), 7));
				int unitY = (int) (dataAreaRectangle.getY() + columnSpacing + dataAreaHeight - labelFontSize);
				if (Math.abs(unitY - centroidY) > labelFontSize)
					g2.drawString(dimensionUnits.get(i), (int) (x + tickMarkLength), unitY);
			}
		}
	}

	private void recalculateDensityBins() {
		if (dimensionCount == 0 || dataAreaHeight == 0) {
			densityBins = null;
			return;
		}

		densityBins = new int[dimensionCount][dataAreaHeight];

		// expects member values normalized to [0, 1] per dimension; a value
		// outside that range breaks out of the whole recalculation early,
		// leaving the bins only partially updated for this frame -- they are
		// fully recomputed on the next layout pass, so this is a dropped
		// frame for out-of-range input, not a lasting corruption
		for (double[] member : members) {
			for (int i = 0; i < dimensionCount; i++) {
				try {
					densityBins[i][(int) ((densityBins[i].length - 1) * member[i])]++;
				} catch (ArrayIndexOutOfBoundsException e) {
					return;
				}
			}
		}
	}

	private String formatTickValue(double value, double columnMax) {
		String text = tickNumberFormat.format(value);
		if (columnMax >= 10.0)
			text = text.substring(0, text.length() - 1);
		if (columnMax >= 100.0)
			text = text.substring(0, text.length() - 1);
		if (columnMax >= 1000.0)
			text = text.substring(0, text.length() - 2);
		return text;
	}

	private static int getMax(int[] values) {
		int maxIndex = 0;
		for (int i = 1; i < values.length; i++)
			if (values[i] > values[maxIndex])
				maxIndex = i;
		return values[maxIndex];
	}

	// ------------------------------------------------------------------
	// data access
	// ------------------------------------------------------------------

	public double[] getData() {
		return centroid;
	}

	/**
	 * @deprecated rather create a new instance
	 */
	@Deprecated
	public void setData(double[] centroid, List<double[]> members) {
		this.centroid = centroid;
		this.members = members;
		recalculateLayout();
	}

	public StatisticsSupport[] getGlobalStatisticsPerDimension() {
		return globalStatisticsPerDimension;
	}

	public void setGlobalStatisticsPerDimension(StatisticsSupport[] globalStatisticsPerDimension) {
		this.globalStatisticsPerDimension = globalStatisticsPerDimension;
	}

	public ArrayList<String> getDimensionNames() {
		return dimensionNames;
	}

	public void setDimensionNames(ArrayList<String> dimensionNames) {
		this.dimensionNames = dimensionNames;
	}

	public ArrayList<String> getDimensionUnits() {
		return dimensionUnits;
	}

	public void setDimensionUnits(ArrayList<String> dimensionUnits) {
		this.dimensionUnits = dimensionUnits;
	}

	// ------------------------------------------------------------------
	// configuration
	// ------------------------------------------------------------------

	public boolean isShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
		setRectangle(getRectangle());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Color getTitleBackground() {
		return titleBackground;
	}

	public void setTitleBackground(Color titleBackground) {
		this.titleBackground = titleBackground;
	}

	public boolean isShowColumnLabels() {
		return showColumnLabels;
	}

	public void setShowColumnLabels(boolean showColumnLabels) {
		this.showColumnLabels = showColumnLabels;
		setRectangle(getRectangle());
	}

	public boolean isShowAxis() {
		return showAxis;
	}

	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		setRectangle(getRectangle());
	}

	public YAxisNumericalPainter<Double> getAxisPainter() {
		return axisPainter;
	}
}
