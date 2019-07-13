package com.github.TKnudsen.infoVis.view.interaction;

import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Provides the ability to encode selected elements.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface ISelectionVisualizer<T> {

	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction);
}
