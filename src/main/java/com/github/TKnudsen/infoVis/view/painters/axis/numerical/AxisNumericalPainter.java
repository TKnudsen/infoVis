package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisCartTools;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;

/**
 * @version 2.05 -- fixed a narrow-value-range case (e.g. min=49.5, max=50.5)
 *          where tick label precision was chosen from maxValue's magnitude
 *          alone, ignoring how fine the computed quantization actually was;
 *          adjacent ticks rounded to the same label text, got collapsed by
 *          the redundant-label removal below, and left just one marker --
 *          which draws no visible axis line at all (drawAxisBetweenAxeMarkersOnly
 *          has nothing to span between), just a single floating label, in
 *          September 2026; fixed tooltip sizing (font tied to the axis's own
 *          font, not the enclosing rectangle's height) and honored
 *          isToolTipping() in September 2026; removed the dead pruneMinValue
 *          field/getter/setter (never read by calculateMarkerPositions() or
 *          anything else) in September 2026
 * @since 2016
 */
public abstract class AxisNumericalPainter<T extends Number> extends AxisPainter
		implements ITooltip, IPositionEncoder, IAxisLogarithmicScale {

	private T minValue;
	private T maxValue;

	protected int markerDistanceInPixels = 45;
	protected double markerLineWidth = 3.0;
	protected boolean drawAxisBetweenAxeMarkersOnly = true;
	protected boolean enableToolTipping = true;

	protected boolean drawLabels = true;

	protected final PositionEncodingFunction positionEncodingFunction;
	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = () -> markerPositionsWithLabels = null;

	private int tickCount = -1;

	protected AxisLineAlignment axisLineAlignment = AxisLineAlignment.RIGHT;

	protected boolean drawPhysicalUnit = true;
	protected String physicalUnit = "";

	// internal
	protected CopyOnWriteArrayList<Entry<Double, String>> markerPositionsWithLabels = new CopyOnWriteArrayList<Entry<Double, String>>();

	/**
	 * classical constructor for a non-flipped axis
	 * 
	 * @param start start
	 * @param end   end
	 */
	public AxisNumericalPainter(T start, T end) {
		this(start, end, false);
	}

	public AxisNumericalPainter(T start, T end, boolean flipAxisValues) {
		this.minValue = start;
		this.maxValue = end;

		positionEncodingFunction = new PositionEncodingFunction(this.minValue, this.maxValue, 0.0, 0.0, flipAxisValues);
		positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		// this was added, due to the many cases where it had to adapted explicitly
		// this.setBackgroundPaint(null);
		// moved up to AxisPainter
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		drawAxis(g2);

		if (drawPhysicalUnit)
			drawPhysU(g2);
	}

	protected abstract void drawAxis(Graphics2D g2);

	protected abstract void drawPhysU(Graphics2D g2);

	/**
	 * accepts new screen coordinates for the axis and the value mapping
	 * 
	 * @param minPixel min pix
	 * @param maxPixel max pix
	 */
	protected final void setAxisWorldCoordinates(double minPixel, double maxPixel) {
		setAxisWorldCoordinatesAndCalculateMarkers(minPixel, maxPixel,
				Math.max(2, Math.abs(maxPixel - minPixel) / (double) markerDistanceInPixels));
	}

	/**
	 * accepts new screen coordinates for the axis. Sets the position mapping and
	 * adds markers to the axis
	 * 
	 * @param minPixel    min pix
	 * @param maxPixel    max pix
	 * @param markerCount markers
	 */
	protected final void setAxisWorldCoordinatesAndCalculateMarkers(double minPixel, double maxPixel,
			double markerCount) {

		positionEncodingFunction.setMinPixel(minPixel);
		positionEncodingFunction.setMaxPixel(maxPixel);

		calculateMarkerPositions(minPixel, maxPixel, markerCount);
	}

	protected void calculateMarkerPositions(double minPixel, double maxPixel, double markerCount) {
		// axis markers
		markerPositionsWithLabels = new CopyOnWriteArrayList<Entry<Double, String>>();

		// internal variables
		double valueInterval = Math.abs(maxValue.doubleValue() - minValue.doubleValue());

		if (!isLogarithmicScale()) {
			double quantization = AxisCartTools.suggestMeaningfulValueIntervalLinear(valueInterval / markerCount);

			// The uniform-step walk below stops as soon as the next regularly-spaced
			// candidate would land on or past maxValue. When the chosen "nice"
			// quantization does not evenly divide the value range (e.g.
			// quantization=5 on a range up to ~6.7 yields ticks at {0, 5} only, 1.7 --
			// over a third of a full step -- left uncovered at the top), prefer a
			// finer quantization that reaches closer to the true maximum instead,
			// e.g. quantization=2 covering {0, 2, 4, 6}. Bounded retry (not an open
			// search) so this can't run away or overcrowd a small panel with ticks;
			// stops as soon as the wasted top gap is acceptable or the "nice number"
			// chooser stops offering anything finer.
			for (int attempt = 1; attempt <= 3; attempt++) {
				double wastedFraction = wastedTopFraction(quantization, valueInterval);
				if (wastedFraction <= 0.3)
					break;
				double finer = AxisCartTools.suggestMeaningfulValueIntervalLinear(valueInterval / (markerCount + attempt));
				if (finer >= quantization)
					break; // no finer option available -- stop trying
				quantization = finer;
			}

			double startValue = minValue.doubleValue();

			double d = startValue;
			double mod = d % quantization;
			startValue = d - mod;

			if (startValue < minValue.doubleValue())
				startValue += quantization;

			// suggestMeaningfulValueString() picks its decimal precision purely from
			// this "maxValue" magnitude (e.g. anything >50 gets whole-number
			// formatting), with no idea how fine the ticks it's about to label
			// actually are. When quantization is small relative to maxValue's own
			// tier (e.g. maxValue=50.5 -- just over the >50 whole-number cutoff --
			// but quantization=0.5), adjacent ticks round to the identical label
			// text, and the redundant-label removal below then collapses them to
			// one -- which leaves drawAxisBetweenAxeMarkersOnly with a single
			// marker and nothing to draw a line between. Clamping down to whichever
			// of the two is smaller only ever pushes toward finer precision, never
			// toward a coarser one or an inappropriate unit suffix (those only
			// trigger on much larger values than any sane quantization*100 here), so
			// it's a safe correction rather than a behavior change for the normal
			// case where quantization is already proportionate to maxValue.
			double formattingScale = Math.min(Math.abs(maxValue.doubleValue()), Math.abs(quantization) * 100);

			double pixValue = positionEncodingFunction.apply(startValue);
			addMarkerPosition(pixValue, startValue, formattingScale);

			// iterate...
			double loop = quantization;
			while (!Double.isNaN(loop) && !Double.isInfinite(loop)
					&& new BigDecimal(loop).doubleValue() <= new BigDecimal(valueInterval).doubleValue()
					&& new BigDecimal(startValue + loop).doubleValue() < maxValue.doubleValue()) {
				pixValue = positionEncodingFunction.apply(startValue + loop);
				addMarkerPosition(pixValue, startValue + loop, formattingScale);
				loop += quantization;
			}
		} else {
			// logarithmicScale
			List<Double> meaningfulValuesLogarithmic = AxisCartTools.suggestMeaningfulValueIntervalLogarithmic(
					minValue.doubleValue(), maxValue.doubleValue(), (int) markerCount);
			if (meaningfulValuesLogarithmic != null)
				for (Double value : meaningfulValuesLogarithmic) {
					Double pixValue = positionEncodingFunction.apply(value);
					addMarkerPosition(pixValue, value, maxValue.doubleValue());
				}
		}

		// post-processing: remove identical (redundant) labels.
		// This particularly makes sense for discrete (Integer) Axis painters
		Entry<Double, String> lastEntry = null;
		for (int i = 0; i < markerPositionsWithLabels.size(); i++) {
			Entry<Double, String> entry = markerPositionsWithLabels.get(i);
			if (lastEntry != null && entry.getValue().equals(lastEntry.getValue())) {
				markerPositionsWithLabels.remove(i);
				i--;
			} else
				lastEntry = entry;
		}
	}

	private void addMarkerPosition(double pixValue, double value, double maxValue) {
		markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(pixValue,
				AxisCartTools.suggestMeaningfulValueString(value, maxValue)));
	}

	/**
	 * Simulates the uniform-step tick walk for a candidate quantization (without
	 * adding any markers) and returns how much of the top of the value range
	 * would be left without a tick, expressed as a fraction of one quantization
	 * step. Used by {@link #calculateMarkerPositions(double, double, double)} to
	 * decide whether a finer "nice" quantization would cover the range better
	 * than the one initially suggested.
	 */
	private double wastedTopFraction(double quantization, double valueInterval) {
		double startValue = minValue.doubleValue();
		double mod = startValue % quantization;
		startValue = startValue - mod;
		if (startValue < minValue.doubleValue())
			startValue += quantization;

		double lastTickValue = startValue;
		double loop = quantization;
		while (!Double.isNaN(loop) && !Double.isInfinite(loop)
				&& new BigDecimal(loop).doubleValue() <= new BigDecimal(valueInterval).doubleValue()
				&& new BigDecimal(startValue + loop).doubleValue() < maxValue.doubleValue()) {
			lastTickValue = startValue + loop;
			loop += quantization;
		}

		return (maxValue.doubleValue() - lastTickValue) / quantization;
	}

	@Override
	public ChartPainter getTooltip(Point p) {

		if (!isToolTipping())
			return null;

		Number worldX = positionEncodingFunction.inverseMapping(p.getX());

		String text = formatTooltipValue(worldX);
		StringPainter stringPainter = new StringPainter(text);

		// Sized off this axis's own configured font, not the enclosing rectangle's
		// height -- an x-axis strip can be a handful of pixels or the whole panel's
		// height depending on how it's embedded, and scaling the tooltip font off
		// that produced wildly oversized tooltips in the latter case.
		int fontSize = Math.max(11, getFont().getSize());
		stringPainter.setFontSize(fontSize);
		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(getFontColor());

		// Measured with the SAME font size the tooltip actually renders at --
		// previously measured with getFont()'s own (unrelated) size while the
		// tooltip rendered at a different, rectangle-derived size, so the box
		// width and the text it was sized for didn't correspond to one another.
		Font tooltipFont = getFont().deriveFont((float) fontSize);
		int width = Toolkit.getDefaultToolkit().getFontMetrics(tooltipFont).stringWidth(text);
		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, width + 16, fontSize * 1.8);

		stringPainter.setRectangle(rect);

		return stringPainter;
	}

	/**
	 * Formats the world value under the cursor for this axis's tooltip.
	 *
	 * <p>
	 * Default: plain numeric, rounded to 4 decimals. Override when a subclass's
	 * axis labels use a different notation (e.g. calendar dates, elapsed
	 * durations) so the tooltip stays consistent with what the axis itself
	 * draws, instead of showing a raw number a viewer has to decode by hand.
	 *
	 * @param worldValue the world-space value under the cursor
	 * @return the tooltip text
	 */
	protected String formatTooltipValue(Number worldValue) {
		return String.format("%.2f", MathFunctions.round(worldValue.doubleValue(), 4));
	}

	public abstract double getAxisAlignmentCoordinate();

	@Override
	public IPositionEncodingFunction getPositionEncodingFunction() {
		return this.positionEncodingFunction;
	}

	public List<Entry<Double, String>> getMarkerPositionsWithLabels() {
		return markerPositionsWithLabels;
	}

	public boolean isDrawPhysicalUnit() {
		return drawPhysicalUnit;
	}

	public void setDrawPhysicalUnit(boolean drawPhysicalUnit) {
		this.drawPhysicalUnit = drawPhysicalUnit;
	}

	public String getPhysicalUnit() {
		return physicalUnit;
	}

	public void setPhysicalUnit(String physicalUnit) {
		this.physicalUnit = physicalUnit;
	}

	public int getMarkerDistanceInPixels() {
		return markerDistanceInPixels;
	}

	public void setMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.markerDistanceInPixels = markerDistanceInPixels;
		setRectangle(rectangle);
	}

	public T getMinValue() {
		return minValue;
	}

	public void setMinValue(T minValue) {
		this.minValue = minValue;
		markerPositionsWithLabels = null;

		this.positionEncodingFunction.setMinWorldValue(minValue);
	}

	public T getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(T maxValue) {
		this.maxValue = maxValue;
		markerPositionsWithLabels = null;

		this.positionEncodingFunction.setMaxWorldValue(maxValue);
	}

	public boolean isDrawAxisBetweenAxeMarkersOnly() {
		return drawAxisBetweenAxeMarkersOnly;
	}

	public void setDrawAxisBetweenAxeMarkersOnly(boolean drawAxisBetweenAxeMarkersOnly) {
		this.drawAxisBetweenAxeMarkersOnly = drawAxisBetweenAxeMarkersOnly;
	}

	@Override
	public boolean isLogarithmicScale() {
		return positionEncodingFunction.isLogarithmicScale();
	}

	@Override
	public void setLogarithmicScale(boolean logarithmicScale) {
		this.positionEncodingFunction.setLogarithmicScale(logarithmicScale);
	}

	public AxisLineAlignment getAxisLineAlignment() {
		return axisLineAlignment;
	}

	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		this.axisLineAlignment = axisLineAlignment;
	}

	public boolean isDrawLabels() {
		return drawLabels;
	}

	public void setDrawLabels(boolean drawLabels) {
		this.drawLabels = drawLabels;
	}

	public int getTickCount() {
		return tickCount;
	}

	public void setTickCount(int tickCount) {
		this.tickCount = tickCount;
	}

	public boolean isFlipAxisValues() {
		return positionEncodingFunction.isFlipAxisValues();
	}

	public void setFlipAxisValues(boolean flipAxisValues) {
		positionEncodingFunction.setFlipAxisValues(flipAxisValues);
	}

}
