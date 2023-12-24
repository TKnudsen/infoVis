package com.github.TKnudsen.infoVis.view.tools;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

public class GridTools {

	/**
	 * heuristics to guess grid sizes x and y, aiming for a quadratic grid layout
	 * 
	 * @param <T>
	 * @param items
	 * @return
	 */
	public static <T> T[][] toGridQuadratic(Collection<T> items, Class<? extends T> cls) {

		int x = (int) Math.ceil(Math.sqrt(items.size()));
		int y = (int) Math.floor(Math.sqrt(items.size()));
		if (Math.sqrt(items.size()) - (int) Math.floor(Math.sqrt(items.size())) > 0.5)
			y++;

		x = Math.max(x, 1);
		y = Math.max(y, 1);

		@SuppressWarnings("unchecked")
		T[][] grid = (T[][]) Array.newInstance(cls, x, y);

		int x_ = 0;
		int y_ = 0;
		Iterator<T> iterator = items.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			grid[x_][y_++] = iterator.next();
			i++;
			if (i % x == 0)
				x_++;
			y_ = y_ % y;
		}

		return grid;
	}
}
