package com.github.TKnudsen.infoVis.view.table;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ComponentTools;

/**
 * <p>
 * Panel that resizes and draws one or more {@link ChartPainter}s to fill its
 * bounds on each repaint, used for table cell renderers whose size is only
 * known once the panel is added to the table.
 * </p>
 *
 * @version 1.0
 */
public class MyTableCellPainterPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7740906403396885134L;

	List<ChartPainter> painters = new ArrayList<>();

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// fill background
		Rectangle2D rect = ComponentTools.getDrawableRectangle(this);
		if (rect != null) {
			g.setColor(getBackground());
			g.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
		}

		// painter(s)
		for (ChartPainter painter : painters) {
			if (painter != null) {
				painter.setRectangle(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
				painter.draw((Graphics2D) g);
			}
		}
	}

	public void addPainter(ChartPainter painter) {
		this.painters.add(painter);
	}
}
