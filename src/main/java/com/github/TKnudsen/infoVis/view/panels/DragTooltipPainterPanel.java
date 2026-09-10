package com.github.TKnudsen.infoVis.view.panels;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;

/**
 * A {@link ClippingPainterPanel} for a tooltip-capable painter that also
 * offers its hovered/clicked element as a drag-and-drop text export
 * (left mouse button) and as a callback to a right-click consumer -- both
 * driven purely by the wrapped painter's {@link ITooltip#getTooltip(Point)},
 * so any {@link ChartPainter} implementing {@link ITooltip} (e.g.
 * {@link ScatterPlotPainter}) works here without further adaptation.
 *
 * @param <T> the wrapped painter's concrete type
 *
 * @version 1.0
 * @since 2026
 */
public class DragTooltipPainterPanel<T extends ChartPainter & ITooltip> extends ClippingPainterPanel<T>
		implements Transferable {

	private static final long serialVersionUID = 1L;

	protected volatile Point lastPos = new Point(0, 0);
	private JLabel tooltipLabel;

	private final Consumer<String> rightClickConsumer;

	public DragTooltipPainterPanel(T painter, Consumer<String> rightClickConsumer, TransferHandler transferHandler) {
		super(painter);

		this.rightClickConsumer = rightClickConsumer;

		this.setTransferHandler(transferHandler);

		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					TransferHandler handler = DragTooltipPainterPanel.this.getTransferHandler();
					if (handler != null) {
						handler.exportAsDrag(DragTooltipPainterPanel.this, e, TransferHandler.COPY);
						lastPos = e.getPoint();
					}
				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (rightClickConsumer != null) {
						String text = tooltipText(e.getPoint());
						if (text != null)
							rightClickConsumer.accept(text);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				JLabel label = tooltipLabel;
				if (label != null) {
					String text = tooltipText(e.getPoint());
					if (text != null && !text.isEmpty()) {
						label.setText(text);
						if (label.getParent() != null) {
							label.getParent().revalidate();
							label.getParent().repaint();
						}
					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.stringFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor != null && flavor.equals(getTransferDataFlavors()[0]);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor != null && flavor.equals(getTransferDataFlavors()[0])) {
			String text = tooltipText(lastPos);
			return text != null ? text : "";
		}
		return "";
	}

	/**
	 * @return the wrapped painter's tooltip text at {@code p}, or null if there
	 *         is none or the painter's {@link ITooltip#getTooltip(Point)} does
	 *         not return a {@link StringPainter} (e.g. a custom tooltip
	 *         renderer) -- drag export and the right-click callback only make
	 *         sense for plain text tooltips.
	 */
	private String tooltipText(Point p) {
		ChartPainter tooltip = painter.getTooltip(p);
		if (!(tooltip instanceof StringPainter))
			return null;

		String data = ((StringPainter) tooltip).getData();
		return data != null ? data : "";
	}

	public JLabel getTooltipLabel() {
		return tooltipLabel;
	}

	public void setTooltipLabel(JLabel tooltipLabel) {
		this.tooltipLabel = tooltipLabel;
	}

}
