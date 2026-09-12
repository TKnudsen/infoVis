package com.github.TKnudsen.infoVis.view.panels.aggregation;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.infoVis.view.painters.aggregation.AggregateSignificanceBarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.buttons.CloseButtonPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;

/**
 * An interactive {@link AggregateSignificanceBarChartPainter} panel: clicking
 * a bar sets/adds to the given {@link SelectionModel} (control-click adds to
 * the current selection instead of replacing it), and a close button in the
 * corner removes the panel from its parent -- useful for a dashboard of
 * dismissible per-attribute charts.
 *
 * @param <O> the element type held in each bucket
 * @since 2013
 */
public class AggregateSignificanceBarChartPainterPanel<O> extends InfoVisChartPanel implements SelectionListener<O> {

	private static final long serialVersionUID = 1489952956772182728L;

	private static final int HEADLINE_HEIGHT = 12;

	private final AggregateSignificanceBarChartPainter<O> painter;
	private final SelectionModel<O> selectionModel;
	private final CloseButtonPainter closeButtonPainter;

	public AggregateSignificanceBarChartPainterPanel(List<List<O>> elementMapping, List<String> labels, String headline,
			SelectionModel<O> selectionModel) {
		this(new AggregateSignificanceBarChartPainter<>(elementMapping, labels, headline), selectionModel);
	}

	private AggregateSignificanceBarChartPainterPanel(AggregateSignificanceBarChartPainter<O> painter, SelectionModel<O> selectionModel) {
		super(painter);

		this.painter = painter;
		this.selectionModel = selectionModel;

		painter.setBackgroundPaint(null);
		painter.setFontColor(Color.MAGENTA);
		painter.setOffset(3.0);
		painter.setColor(Color.LIGHT_GRAY);
		painter.setFill(true);
		painter.setVerticalOrientation(false);
		painter.setInvertLegendAlignment(true);

		closeButtonPainter = new CloseButtonPainter(Color.GRAY, Color.GRAY);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (painter.getRectangle() == null || !painter.getRectangle().contains(e.getPoint()))
					return;

				if (closeButtonPainter.getRectangle() != null && closeButtonPainter.getRectangle().contains(e.getPoint())) {
					Container container = getParent();
					if (container != null) {
						container.remove(AggregateSignificanceBarChartPainterPanel.this);
						container.revalidate();
						container.repaint();
					}
					return;
				}

				List<O> clickedEntities = painter.getClickedEntities(e);
				if (clickedEntities == null)
					selectionModel.clear();
				else if (e.isControlDown())
					selectionModel.addToSelection(clickedEntities);
				else
					selectionModel.setSelection(clickedEntities);
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		painter.updateSelectedBarRenderer();
		painter.drawLegend((Graphics2D) g);

		closeButtonPainter.setRectangle(new Rectangle2D.Double(getWidth() - HEADLINE_HEIGHT, 0, HEADLINE_HEIGHT - 1, HEADLINE_HEIGHT));
		closeButtonPainter.draw((Graphics2D) g);
	}

	/**
	 * Sets the color coding of every bar with respect to the significance
	 * values (e.g. blueish or reddish).
	 */
	public void setSignificanceColors(List<Color> colors) {
		painter.setColorsForSelection(colors);
	}

	public Map<O, Boolean> getSelectedStatus() {
		return painter.getSelectedStatus();
	}

	public void setSelectedStatus(Map<O, Boolean> selectedStatus) {
		painter.setSelectedStatus(selectedStatus);

		painter.updateSelectedBarRenderer();
		revalidate();
		repaint();
	}

	public void setSelectedStatus(Set<O> selectedStatus) {
		painter.setSelectedStatus(selectedStatus);

		painter.updateSelectedBarRenderer();
		revalidate();
		repaint();
	}

	public void setOffset(double offset) {
		painter.setOffset(offset);
	}

	public void setFontColor(Color color) {
		painter.setFontColor(color);
	}

	public void setInvertLegendAlignment(boolean invert) {
		painter.setInvertLegendAlignment(invert);
	}

	@Override
	public void selectionChanged(SelectionEvent<O> selectionEvent) {
		setSelectedStatus(selectionEvent.getSelectionModel().getSelection());
	}
}
