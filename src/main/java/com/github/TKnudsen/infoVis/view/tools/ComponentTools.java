package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * This class provides helpers to compute the drawable area of a component. The
 * drawable area excludes borders and padding defined by insets.
 * </p>
 *
 * @version 2.03
 * @since 2018
 */
public final class ComponentTools {

	/**
	 * Prevent instantiation.
	 */
	private ComponentTools() {
		throw new AssertionError("Utility class must not be instantiated.");
	}

	/**
	 * Returns the drawable rectangle of a component.
	 *
	 * <p>
	 * The returned rectangle is in the local coordinate system of the component.
	 * Its origin starts after the insets. Its width and height exclude the inset
	 * space.
	 * </p>
	 *
	 * @param component the component; may be null
	 * @return the drawable rectangle, or null if the component is null or the
	 *         computed size is negative
	 */
	public static Rectangle2D getDrawableRectangle(Component component) {
		if (component == null) {
			return null;
		}

		Insets insets = getComponentInsets(component);

		double x = insets.left;
		double y = insets.top;
		double width = component.getWidth() - insets.left - insets.right;
		double height = component.getHeight() - insets.top - insets.bottom;

		if (width < 0 || height < 0) {
			return null;
		}

		return new Rectangle2D.Double(x, y, width, height);
	}

	/**
	 * Returns the insets of a component.
	 *
	 * @param component the component; must not be null
	 * @return the insets of the component, or zero insets if unavailable
	 */
	private static Insets getComponentInsets(Component component) {
		if (component instanceof Container) {
			Insets insets = ((Container) component).getInsets();
			if (insets != null) {
				return insets;
			}
		}

		return new Insets(0, 0, 0, 0);
	}

	/**
	 * Backward-compatible alias for {@link #getDrawableRectangle(Component)}.
	 *
	 * @param component the component
	 * @return the drawable rectangle, or null if the component is null or invalid
	 * @deprecated Use {@link #getDrawableRectangle(Component)}.
	 */
	@Deprecated
	public static Rectangle2D getCompontentDrawableRectangle(Component component) {
		return getDrawableRectangle(component);
	}

	/**
	 * Backward-compatible alias for {@link #getDrawableRectangle(Component)}.
	 *
	 * @param component the component
	 * @return the drawable rectangle, or null if the component is null or invalid
	 * @deprecated Use {@link #getDrawableRectangle(Component)}.
	 */
	@Deprecated
	public static Rectangle2D getBoundsRectangle(Component component) {
		return getDrawableRectangle(component);
	}
}