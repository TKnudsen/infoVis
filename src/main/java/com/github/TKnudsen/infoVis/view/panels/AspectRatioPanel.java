package com.github.TKnudsen.infoVis.view.panels;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Constrains a single nested component to a fixed {@code xDim:yDim} aspect
 * ratio, centering it within whatever bounds this panel is actually given by
 * its own container/layout manager. The ratio is specified as a pair of
 * dimensions (e.g. {@code 16, 9} or {@code 1, 1} for square) rather than a
 * single pre-divided ratio value, so the intent stays readable at the call
 * site.
 *
 * <p>
 * This panel does not lay out its nested component directly -- it uses a
 * single-cell {@link GridLayout} so the nested component always fills whatever
 * bounds {@link #setBounds(int, int, int, int)} computes for this panel itself.
 * That override is where the actual constraint happens: it shrinks the larger
 * dimension so the result matches the target ratio, then centers the shrunk
 * rectangle within the space this panel was allocated. A container that never
 * calls {@code setBounds} on its children directly (uncommon for standard Swing
 * layout managers, but possible with custom ones) will not trigger the
 * constraint.
 * </p>
 *
 * <p>
 * Only one component is ever meaningfully displayed at a time -- adding a
 * second via {@link JPanel#add} without going through
 * {@link #setNestedComponent(JComponent)} first will not remove the first.
 * </p>
 *
 * 
 * @since 2020
 * @version 1.1
 */
public class AspectRatioPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final int xDim;
	private final int yDim;

	/**
	 * Creates the panel with a target ratio but no nested component yet; add one
	 * later via {@link #setNestedComponent(JComponent)}.
	 *
	 * @param xDim width component of the target ratio, e.g. 16 for 16:9
	 * @param yDim height component of the target ratio, e.g. 9 for 16:9
	 */
	public AspectRatioPanel(int xDim, int yDim) {
		this.setLayout(new GridLayout(1, 1));

		this.xDim = xDim;
		this.yDim = yDim;
	}

	/**
	 * @param xDim            width component of the target ratio, e.g. 16 for 16:9
	 * @param yDim            height component of the target ratio, e.g. 9 for 16:9
	 * @param nestedComponent the component to constrain
	 */
	public AspectRatioPanel(int xDim, int yDim, JComponent nestedComponent) {
		this(xDim, yDim);

		this.add(nestedComponent);
	}

	/**
	 * Replaces the currently nested component (if any) with
	 * {@code nestedComponent}. The target ratio set at construction time is
	 * unaffected.
	 */
	public void setNestedComponent(JComponent nestedComponent) {
		this.removeAll();

		this.add(nestedComponent);

		repaint();
	}

	/**
	 * Shrinks whichever of {@code width}/{@code height} is oversized relative to
	 * {@code xDim:yDim} so the result matches the target ratio exactly, then
	 * centers that rectangle within the {@code (x, y, width, height)} bounds this
	 * panel was allocated by its container.
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (width == 0 || height == 0) {
			super.setBounds(x, y, width, height);
			return;
		}

		double xQ = width / (double) xDim;
		double yQ = height / (double) yDim;

		int w = width;
		int h = height;

		int x0 = x;
		int y0 = y;

		// skip if ratios are super similar (sub-pixel, etc)
		if (Math.abs(xQ - yQ) > 0.001) {
			if (xQ > yQ) {
				// all y space will be used, x reduced
				w = (int) (yQ * xDim);
				x0 += ((width - w) * 0.5);
			}

			if (yQ > xQ) {
				// all x space will be used, y reduced
				h = (int) (xQ * yDim);
				y0 += (int) ((height - h) * 0.5);
			}
		}

		super.setBounds(x0, y0, w, h);
	}

}
