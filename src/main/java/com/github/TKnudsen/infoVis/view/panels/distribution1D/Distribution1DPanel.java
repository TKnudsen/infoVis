package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DPainter;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

public interface Distribution1DPanel<T> extends IColorEncoding<T>, IRectangleSelection<T> {

	public Distribution1DPainter<T> getDistribution1DPainter();

	default void clearSpecialValues() {
		getDistribution1DPainter().clearSpecialValues();
	}

	default void addSpecialValue(T worldValue, ShapeAttributes shapeAttributes) {
		getDistribution1DPainter().addSpecialValue(worldValue, shapeAttributes);
	}

	default IPositionEncodingFunction getPositionEncodingFunction() {
		return getDistribution1DPainter().getPositionEncodingFunction();
	}

	default void setPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.getDistribution1DPainter().setPositionEncodingFunction(xPositionEncodingFunction);
	}

	default Function<? super T, ? extends Paint> getColorEncodingFunction() {
		return getDistribution1DPainter().getColorEncodingFunction();
	}

	@Override
	default void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.getDistribution1DPainter().setColorEncodingFunction(colorEncodingFunction);
	}

	@Override
	default List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getDistribution1DPainter().getElementsInRectangle(rectangle);
	}

	default boolean isToolTipping() {
		return getDistribution1DPainter().isToolTipping();
	}

	default void setToolTipping(boolean toolTipping) {
		this.getDistribution1DPainter().setToolTipping(toolTipping);
	}

	default void setTriangleSize(double triangleSize) {
		this.getDistribution1DPainter().setSizeOfTriangle(triangleSize);
	}

	default void setShowTrianglesForSelection(boolean showTrianglesForSelection) {
		this.getDistribution1DPainter().setShowTrianglesForSelection(showTrianglesForSelection);
	}

	default float getAlpha() {
		return getDistribution1DPainter().getAlpha();
	}

	default void setAlpha(float alpha) {
		this.getDistribution1DPainter().setAlpha(alpha);
	}

	default boolean isDynamicAlpha() {
		return getDistribution1DPainter().isDynamicAlpha();
	}

	default void setDynamicAlpha(boolean dynamicAlpha) {
		this.getDistribution1DPainter().setDynamicAlpha(dynamicAlpha);
	}

	default boolean isAlphaOnSpecialValues() {
		return getDistribution1DPainter().isAlphaOnSpecialValues();
	}

	default void setAlphaOnSpecialValues(boolean alphaOnSpecialValues) {
		this.getDistribution1DPainter().setAlphaOnSpecialValues(alphaOnSpecialValues);
	}
}
