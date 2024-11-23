package com.github.TKnudsen.infoVis.view.tools;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Little helper that avoids millions of BasicStroke object creations.
 * 
 * <p>
 * Copyright: (c) 2024 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BasicStrokes {

	private static Map<Integer, BasicStroke> basicStrokes = new HashMap<>();

	public static BasicStroke get(float width) {
		return get(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	}

	public static BasicStroke get(float width, int cap, int join) {
		int index = (int) (cap + 10 * join + 10000 * width);
		if (!basicStrokes.containsKey(index))
			basicStrokes.put(index, new BasicStroke(width, cap, join));

		return basicStrokes.get(index);
	}

}
