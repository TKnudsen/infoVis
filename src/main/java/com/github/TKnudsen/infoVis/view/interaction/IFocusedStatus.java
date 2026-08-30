package com.github.TKnudsen.infoVis.view.interaction;

/**
 * <p>
 * Marks a painter or component as capable of carrying a focused/not-focused
 * state, e.g. for hover-driven highlighting.
 * </p>
 *
 * @version 1.0
 * @since 2016
 */
public interface IFocusedStatus {

	boolean isFocused();

	void setFocused(boolean focused);
}
