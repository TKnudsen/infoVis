package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics.AxisSizeCharacteristics;
import com.github.TKnudsen.infoVis.view.visualChannels.position.FixedPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * Abstract base class for layouts that distribute multiple axes across a single
 * dimension with configurable spacing.
 * 
 * Subclasses implement the specific orientation (horizontal Y-axes or vertical
 * X-axes) by providing the axis center calculation and rectangle matrix
 * creation logic.
 * </p>
 *
 * @version 1.01
 * @since 2025
 */
public abstract class MultiAxisChartRectangleLayout extends ChartRectangleLayout {

	protected static final double DEFAULT_AXIS_POSITION = 1.0;

	protected List<AxisSizeCharacteristics> axisSizeCharacteristics = new ArrayList<AxisSizeCharacteristics>();

	protected FixedPositionEncodingFunction axesPositionEncodingFunction;

	protected int spacingBetweenAxes = 0;
	protected Double relativeSpaceBetweenAxes = null;

	public MultiAxisChartRectangleLayout() {
		this(0);
	}

	public MultiAxisChartRectangleLayout(int axes) {
		if (axes < 0)
			throw new IllegalArgumentException(
					"MultiAxisChartRectangleLayout: Number of axes cannot be negative: " + axes);

		for (int i = 0; i < axes; i++)
			addAxis();

		refreshAxesPositionEncodingFunction();
	}

	public MultiAxisChartRectangleLayout(List<AxisSizeCharacteristics> axisSizeCharacteristics) {
		if (axisSizeCharacteristics == null)
			throw new IllegalArgumentException("MultiAxisChartRectangleLayout: axisSizeCharacteristics cannot be null");

		this.axisSizeCharacteristics = new ArrayList<>(axisSizeCharacteristics);

		refreshAxesPositionEncodingFunction();
	}

	/**
	 * Extracts the center position from an axis rectangle. Subclasses implement
	 * this to return getCenterX() for horizontal layouts or getCenterY() for
	 * vertical layouts.
	 * 
	 * @param axisRectangle the axis rectangle
	 * @return the center position along the layout dimension
	 */
	protected abstract double getAxisCenterPosition(Rectangle2D axisRectangle);

	/**
	 * Calculates the spacing to use based on the rectangle dimensions. Subclasses
	 * implement this to use getWidth() for horizontal layouts or getHeight() for
	 * vertical layouts.
	 * 
	 * @param rectangle the bounding rectangle
	 * @return the dimension to use for relative spacing calculation
	 */
	protected abstract double getSpacingDimension(Rectangle2D rectangle);

	/**
	 * Creates the rectangle matrix for axis layout. Subclasses implement this to
	 * create Nx1 matrices for horizontal layouts or 1xN matrices for vertical
	 * layouts.
	 * 
	 * @param bounds          the bounding rectangle
	 * @param axisCount       the number of axes
	 * @param spacingInPixels the spacing between axes
	 * @return the rectangle matrix, or null if creation failed
	 */
	protected abstract Rectangle2D[][] createAxisRectangleMatrix(Rectangle2D bounds, int axisCount,
			int spacingInPixels);

	/**
	 * Gets the name of the axis type for error messages (e.g., "Y-axis" or
	 * "X-axis").
	 * 
	 * @return the axis type name
	 */
	protected abstract String getAxisTypeName();

	/**
	 * Refreshes the position encoding function based on current axis rectangles.
	 */
	protected void refreshAxesPositionEncodingFunction() {
		if (axesPositionEncodingFunction == null && !axisSizeCharacteristics.isEmpty()) {
			Map<Number, Double> mapping = new LinkedHashMap<Number, Double>();
			for (int i = 0; i < axisSizeCharacteristics.size(); i++)
				if (axisSizeCharacteristics.get(i).getAxisRectangle() != null)
					mapping.put(i, getAxisCenterPosition(axisSizeCharacteristics.get(i).getAxisRectangle()));
				else
					mapping.put(i, DEFAULT_AXIS_POSITION);

			if (!mapping.isEmpty())
				axesPositionEncodingFunction = new FixedPositionEncodingFunction(mapping);
		} else if (axesPositionEncodingFunction != null) {
			for (int i = 0; i < axisSizeCharacteristics.size(); i++)
				if (axisSizeCharacteristics.get(i).getAxisRectangle() != null)
					axesPositionEncodingFunction.addFixedPosition(i,
							getAxisCenterPosition(axisSizeCharacteristics.get(i).getAxisRectangle()));
				else
					axesPositionEncodingFunction.addFixedPosition(i, DEFAULT_AXIS_POSITION);
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null || axisSizeCharacteristics.isEmpty())
			return;

		int spacingBetweenAxes = this.spacingBetweenAxes;
		if (relativeSpaceBetweenAxes != null && !Double.isNaN(relativeSpaceBetweenAxes))
			spacingBetweenAxes = (int) (getSpacingDimension(rectangle) * relativeSpaceBetweenAxes);

		Rectangle2D[][] rectangleMatrix = createAxisRectangleMatrix(chartRectangle, axisSizeCharacteristics.size(),
				spacingBetweenAxes);

		if (rectangleMatrix == null)
			return;

		assignRectanglesToAxes(rectangleMatrix);

		refreshAxesPositionEncodingFunction();
	}

	/**
	 * Assigns rectangles from the matrix to the axis characteristics. Subclasses
	 * can override to handle different matrix orientations.
	 * 
	 * @param rectangleMatrix the matrix of rectangles
	 */
	protected void assignRectanglesToAxes(Rectangle2D[][] rectangleMatrix) {
		for (int i = 0; i < rectangleMatrix.length && i < axisSizeCharacteristics.size(); i++)
			axisSizeCharacteristics.get(i).setAxisRectangle(rectangleMatrix[i][0]);
	}

	/**
	 * Gets the rectangle bounds for the axis at the specified index.
	 * 
	 * @param axisIndex the zero-based index of the axis
	 * @return the rectangle defining the axis bounds
	 * @throws IndexOutOfBoundsException if index is invalid
	 */
	public Rectangle2D getAxisRectangle(int axisIndex) {
		return axisSizeCharacteristics.get(axisIndex).getAxisRectangle();
	}

	/**
	 * Gets the rectangles for all charts.
	 * 
	 * @return list of axis rectangles
	 */
	public List<Rectangle2D> getYAxisRectangles() {
		List<Rectangle2D> rectangles = new ArrayList<>();
		for (int i = 0; i < getAxisCount(); i++)
			rectangles.add(getAxisRectangle(i));
		return rectangles;
	}

	/**
	 * Adds a new axis to the layout with default characteristics. The layout will
	 * be recalculated on the next setRectangle call.
	 * <p>
	 * Note: Each axis consumes memory and rendering resources. Consider the
	 * performance implications when adding many axes.
	 */
	public void addAxis() {
		axisSizeCharacteristics.add(new AxisSizeCharacteristics());

		refreshAxesPositionEncodingFunction();
	}

	/**
	 * Gets the number of axes in this layout.
	 * 
	 * @return the number of axes
	 */
	public int getAxisCount() {
		return axisSizeCharacteristics.size();
	}

	/**
	 * Applies an operation to all axis characteristics.
	 * 
	 * @throws IllegalArgumentException if no axes have been added
	 */
	protected void forEachAxis(Consumer<AxisSizeCharacteristics> action) {
		if (axisSizeCharacteristics.isEmpty())
			throw new IllegalArgumentException(getClass().getSimpleName() + ": add axes first");

		for (AxisSizeCharacteristics characteristics : axisSizeCharacteristics)
			action.accept(characteristics);
	}

	/**
	 * Gets a property from the first axis, or default value if no axes exist.
	 */
	protected <T> T getAxisProperty(Function<AxisSizeCharacteristics, T> getter, T defaultValue) {
		if (!axisSizeCharacteristics.isEmpty())
			return getter.apply(axisSizeCharacteristics.get(0));
		return defaultValue;
	}

	/**
	 * Gets the absolute spacing between axes in pixels.
	 * 
	 * @return spacing in pixels, or calculated from relative spacing if set
	 */
	public int getSpacingBetweenAxes() {
		return spacingBetweenAxes;
	}

	/**
	 * Sets the absolute spacing between axes in pixels. This is overridden if
	 * relative spacing is set.
	 * 
	 * @param spacingBetweenAxes spacing in pixels (must be non-negative)
	 * @throws IllegalArgumentException if spacing is negative
	 */
	public void setSpacingBetweenAxes(int spacingBetweenAxes) {
		if (spacingBetweenAxes < 0)
			throw new IllegalArgumentException(
					"MultiAxisChartRectangleLayout.setSpacingBetweenAxes: Spacing cannot be negative: "
							+ spacingBetweenAxes);

		this.spacingBetweenAxes = spacingBetweenAxes;

		setRectangle(rectangle);
	}

	/**
	 * Gets the legend offset for axes (space reserved for axis labels).
	 * 
	 * @return the legend offset, or default if no axes exist
	 */
	public double getAxisLegendOffset() {
		return getAxisProperty(AxisSizeCharacteristics::getAxisLegendOffset,
				new AxisSizeCharacteristics().getAxisLegendOffset());
	}

	/**
	 * Sets the legend offset for all axes (space reserved for axis labels).
	 * 
	 * @param axisLegendOffset the legend offset in pixels
	 * @throws IllegalArgumentException if no axes have been added
	 */
	public void setAxisLegendOffset(double axisLegendOffset) {
		forEachAxis(characteristics -> characteristics.setAxisLegendOffset(axisLegendOffset));

		setRectangle(rectangle);
	}

	/**
	 * Checks if axes should be drawn.
	 * 
	 * @return true if axes are drawn, or default if no axes exist
	 */
	public boolean isDrawAxis() {
		return getAxisProperty(AxisSizeCharacteristics::isDrawAxis, new AxisSizeCharacteristics().isDrawAxis());
	}

	/**
	 * Sets whether axes should be drawn.
	 * 
	 * @param drawAxis true to draw axes, false to hide them
	 * @throws IllegalArgumentException if no axes have been added
	 */
	public void setDrawAxis(boolean drawAxis) {
		forEachAxis(characteristics -> characteristics.setDrawAxis(drawAxis));

		setRectangle(rectangle);
	}

	/**
	 * Gets the minimum size required to draw an axis.
	 * 
	 * @return the minimum size in pixels, or default if no axes exist
	 */
	public double getMinSizeToDrawAxis() {
		return getAxisProperty(AxisSizeCharacteristics::getMinSizeToDrawAxis,
				new AxisSizeCharacteristics().getMinSizeToDrawAxis());
	}

	/**
	 * Sets the minimum size required to draw an axis. Axes smaller than this will
	 * not be rendered.
	 * 
	 * @param minSizeToDrawAxis the minimum size in pixels
	 * @throws IllegalArgumentException if no axes have been added
	 */
	public void setMinSizeToDrawAxis(double minSizeToDrawAxis) {
		forEachAxis(characteristics -> characteristics.setMinSizeToDrawAxis(minSizeToDrawAxis));

		setRectangle(rectangle);
	}

	/**
	 * Gets the position encoding function that maps axis indices to their
	 * positions.
	 * 
	 * @return the position encoding function
	 */
	public IPositionEncodingFunction getAxesPositionEncodingFunction() {
		if (axesPositionEncodingFunction == null)
			refreshAxesPositionEncodingFunction();

		return axesPositionEncodingFunction;
	}

	/**
	 * Gets the relative spacing between axes as a fraction of total dimension.
	 * 
	 * @return relative spacing (0.0-1.0), or null if using absolute spacing
	 */
	public Double getRelativeSpaceBetweenAxes() {
		return relativeSpaceBetweenAxes;
	}

	/**
	 * Sets the relative spacing between axes as a fraction of total dimension. When
	 * set, this overrides absolute spacing.
	 * 
	 * @param relativeSpaceBetweenAxes spacing as fraction (0.0-1.0), or null for
	 *                                 absolute spacing
	 * @throws IllegalArgumentException if value is outside valid range
	 */
	public void setRelativeSpaceBetweenAxes(Double relativeSpaceBetweenAxes) {
		if (relativeSpaceBetweenAxes != null && (relativeSpaceBetweenAxes < 0.0 || relativeSpaceBetweenAxes > 1.0))
			throw new IllegalArgumentException(
					"MultiAxisChartRectangleLayout.setRelativeSpaceBetweenAxes: Relative spacing must be between 0.0 and 1.0: "
							+ relativeSpaceBetweenAxes);

		this.relativeSpaceBetweenAxes = relativeSpaceBetweenAxes;

		setRectangle(rectangle);
	}

}