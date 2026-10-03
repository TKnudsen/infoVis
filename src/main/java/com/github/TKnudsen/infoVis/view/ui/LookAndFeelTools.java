package com.github.TKnudsen.infoVis.view.ui;

import java.awt.Color;

import javax.swing.UIManager;

/**
 * <p>
 * Utility methods for querying the currently active look-and-feel's defaults
 * -- as opposed to {@code DisplayTools}, which renders graphical primitives,
 * this class only reads L&amp;F state.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public final class LookAndFeelTools {

	private LookAndFeelTools() {
	}

	/**
	 * Resolves a look-and-feel-defined color for {@code key}, so a component's
	 * default colors match whatever L&amp;F is active when queried (e.g. a dark
	 * Nimbus theme) instead of being frozen at a fixed literal -- the same way a
	 * {@link javax.swing.JLabel}'s foreground would. Falls back to
	 * {@code fallback} when {@code key} isn't defined by the current L&amp;F, or
	 * no L&amp;F has been installed yet (e.g. a headless context).
	 *
	 * <p>
	 * This is a one-time snapshot, not a live binding: a value read before a
	 * later {@code UIManager.setLookAndFeel(...)} call does not update itself.
	 * Callers that cache the result (e.g. at construction time) need to
	 * re-query (or let the caller rebuild) after switching L&amp;F if they need
	 * to pick up the new theme.
	 * </p>
	 *
	 * @param key      a {@code UIManager} defaults key, e.g.
	 *                 {@code "Label.foreground"}
	 * @param fallback used when the key resolves to {@code null}
	 * @return the resolved color
	 */
	public static Color uiColor(String key, Color fallback) {
		Color c = UIManager.getColor(key);
		return c != null ? c : fallback;
	}
}
