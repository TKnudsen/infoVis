package com.github.TKnudsen.infoVis.view.panels.legend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.LegendPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Wraps an arbitrary {@link ChartPainter} with a {@link LegendPainter}
 * overlay drawn on top of the whole panel -- for a chart whose own axes don't
 * already occupy the space a legendPainter would need.
 *
 * @param <P> the wrapped chart painter's type
 * @since 2013
 */
public class LegendaryPainterPanel<P extends ChartPainter> extends InfoVisChartPanel {

	private static final long serialVersionUID = 1L;

	private final P painter;
	private final LegendPainter legendPainter;

	public LegendaryPainterPanel(P painter, List<Entry<String, Paint>> legendEntries) {
		super(painter);

		this.painter = painter;
		legendPainter = new LegendPainter();
		legendPainter.setData(legendEntries);
	}

	public LegendaryPainterPanel(P painter, List<String> legendaryEntries, List<Color> legendaryColors) {
		super(painter);

		this.painter = painter;

		List<Entry<String, Paint>> entries = new ArrayList<>();
		for (int i = 0; i < legendaryEntries.size(); i++) {
			Color c = Color.BLACK;
			if (legendaryColors != null && legendaryColors.size() > i)
				c = legendaryColors.get(i);
			entries.add(new SimpleEntry<String, Paint>(legendaryEntries.get(i), c));
		}

		legendPainter = new LegendPainter();
		legendPainter.setData(entries);
	}

	public P getPainter() {
		return painter;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		legendPainter.setRectangle(new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
		legendPainter.draw((Graphics2D) g);
	}
}
