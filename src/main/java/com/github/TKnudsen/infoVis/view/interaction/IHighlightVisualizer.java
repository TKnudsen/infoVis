package com.github.TKnudsen.infoVis.view.interaction;

import java.util.function.Function;

/**
 * Counterpart to {@link ISelectionVisualizer}: reports, per element, whether
 * it is currently hover-highlighted rather than persistently selected.
 *
 * @version 1.00
 * @since 2026
 */
public interface IHighlightVisualizer<T> {

	public void setHighlightedFunction(Function<? super T, Boolean> highlightedFunction);
}
