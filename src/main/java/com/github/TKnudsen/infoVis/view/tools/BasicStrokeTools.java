package com.github.TKnudsen.infoVis.view.tools;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.01
 * @since 2024
 */
public class BasicStrokeTools {

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
