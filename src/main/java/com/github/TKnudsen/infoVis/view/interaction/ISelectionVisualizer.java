package com.github.TKnudsen.infoVis.view.interaction;

import java.util.function.Function;

/**
 * @version 1.02
 * @since 2016
 */
public interface ISelectionVisualizer<T> {

	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction);
}
