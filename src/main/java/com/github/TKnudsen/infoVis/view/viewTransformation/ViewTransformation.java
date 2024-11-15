package com.github.TKnudsen.infoVis.view.viewTransformation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.DimensionalityReductionPipeline;

public abstract class ViewTransformation<T> {

	public enum DimRed {
		PCA, MDS, tSNE
	};

	private DimRed dimRed = DimRed.PCA;

	private final Function<Float, Color> colorMapping;

	private final BiFunction<Float, Float, Color> colorMapping2D;

	protected DimensionalityReductionPipeline<T> dimensionalityReductionPipeline;

	/**
	 * some 2D color maps are expensive to compute
	 */
	protected Map<T, Color> colorLookup = new HashMap<T, Color>();

	public ViewTransformation(Function<Float, Color> colorMapping, BiFunction<Float, Float, Color> colorMapping2D) {
		this.colorMapping = colorMapping;
		this.colorMapping2D = colorMapping2D;
	}

	public Double mapToXPosition(T primaryKey) {
		if (getDimensionalityReductionPipeline() != null)
			return getDimensionalityReductionPipeline().getLowDimRelativeWorldCoordinates(primaryKey, 0);

		return 0.0;
	}

	public Double mapToYPosition(T primaryKey) {
		if (getDimensionalityReductionPipeline() != null)
			return getDimensionalityReductionPipeline().getLowDimRelativeWorldCoordinates(primaryKey, 1);

		return 0.0;
	}

	public abstract Color mapToColor(T scoringObject);

	public DimensionalityReductionPipeline<T> getDimensionalityReductionPipeline() {
		if (dimensionalityReductionPipeline == null)
			refreshDimensionalityReductionPipeline();

		return dimensionalityReductionPipeline;
	}

	protected abstract void refreshDimensionalityReductionPipeline();

	protected Color colorMapping1D(float value) {
		if (colorMapping != null)
			return colorMapping.apply(value);

		return Color.GRAY;
	}

	protected Color colorMapping2D(float x, float y) {
		if (colorMapping2D == null)
			return Color.DARK_GRAY;

		return colorMapping2D.apply(x, y);
	}

	public void resetViewTransformation() {
		dimensionalityReductionPipeline = null;
		colorLookup.clear();
	}

	public DimRed getDimRedMethod() {
		return this.dimRed;
	}

	public void setDimRedMethod(DimRed dimRed) {
		this.dimRed = dimRed;

		resetViewTransformation();
	}
}
