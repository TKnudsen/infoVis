package com.github.TKnudsen.infoVis.view.visualChannels;

/**
 * <p>
 * Guarantees that an implementing painter/panel can mitigate overplotting
 * (visual clutter from densely overlapping data points).
 * </p>
 *
 * <p>
 * Overplotting mitigation is the feature; dynamic alpha adjustment is (for
 * now) its one implementation mechanism -- which is why these methods are
 * named after alpha adjustment rather than the more general feature name. See
 * {@code com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools}
 * for the shared alpha-computation logic used by every implementor.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public interface IOverplottingMitigation {

	public boolean isAlphaAdjustment();

	public void setAlphaAdjustment(boolean alphaAdjustment);
}
