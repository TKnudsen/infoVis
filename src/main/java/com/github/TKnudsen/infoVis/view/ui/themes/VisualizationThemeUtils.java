package com.github.TKnudsen.infoVis.view.ui.themes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * Utility class for applying {@link VisualizationTheme} to Swing component
 * trees, especially useful for third-party or legacy components that don't
 * expose explicit theme setters. Also works for {@link ChartPainter} and its
 * inheriting classes.
 * </p>
 *
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 * <li>Conservative vs Aggressive modes for different use cases</li>
 * <li>Respects UIResource to preserve Look-and-Feel defaults</li>
 * <li>Stack-based iterative traversal for performance</li>
 * <li>Component collection and caching support</li>
 * <li>Type-safe generic methods</li>
 * <li>Comprehensive Swing component coverage</li>
 * <li>EDT thread safety warnings</li>
 * </ul>
 *
 * <p>
 * <b>Usage Modes:</b>
 * </p>
 * <ul>
 * <li><b>Conservative (default):</b> Only themes components with UIResource
 * properties or null values. Respects opaque flag. Preserves custom
 * styling.</li>
 * <li><b>Aggressive:</b> Themes all components regardless of current values.
 * Use for third-party components or complete theme enforcement.</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * </p>
 *
 * <pre>
 * // Conservative mode (preserves custom styling)
 * ThemeUtils.applyThemeRecursively(myPanel, theme);
 *
 * // Aggressive mode (complete coverage, e.g., for third-party components)
 * ThemeUtils.applyThemeRecursively(myPanel, theme, true);
 * </pre>
 *
 * <p>
 * <b>Thread Safety:</b> Should be executed on the Event Dispatch Thread (EDT).
 * Warnings will be logged if called from other threads.
 * </p>
 *
 * <p>
 * <b>Performance Tips:</b>
 * </p>
 * <ul>
 * <li>Cache component collections for repeated theme applications</li>
 * <li>Use aggressive mode sparingly, only for third-party components</li>
 * </ul>
 *
 * @version 1.02
 * @since 2026
 */
public final class VisualizationThemeUtils {

	private static final Logger LOGGER = Logger.getLogger(VisualizationThemeUtils.class.getName());

	private VisualizationThemeUtils() {
		throw new AssertionError("ThemeUtils is a utility class and should not be instantiated");
	}

	// ==================== MAIN API ====================

	public static <T extends ChartPainter> int applyThemeToPainters(Collection<T> painters, VisualizationTheme theme) {
		return applyThemeToPainters(painters, theme, false);
	}

	public static <T extends ChartPainter> int applyThemeToPainters(Collection<T> painters, VisualizationTheme theme,
			boolean aggressive) {
		Objects.requireNonNull(theme, "theme must not be null");

		if (painters == null || painters.size() == 0)
			return 0;

		int count = 0;
		for (ChartPainter painter : painters)
			if (painter != null && applyThemeToPainter(painter, theme, aggressive))
				count++;

		return count;
	}

	public static <T extends ChartPainter> boolean applyThemeToPainter(T painter, VisualizationTheme theme) {
		return applyThemeToPainter(painter, theme, false);
	}

	public static <T extends ChartPainter> boolean applyThemeToPainter(T painter, VisualizationTheme theme,
			boolean aggressive) {
		Objects.requireNonNull(painter, "painter must not be null");
		Objects.requireNonNull(theme, "theme must not be null");

		try {
			// 1:1 mappings Theme -> Painter

			// backgroundColor -> backgroundPaint
			if (aggressive || painter.getBackgroundPaint() == null) {
				painter.setBackgroundPaint(theme.getBackgroundColor());
			}

			// borderColor -> borderPaint
			if (aggressive || painter.getBorderPaint() == null) {
				painter.setBorderPaint(theme.getBorderColor());
			}

			// baseFont -> font
			if (aggressive || painter.getFont() == null) {
				painter.setFont(theme.getBaseFont());
			}

			// fontColor (== Swing foreground) -> fontColor
			if (aggressive || painter.getFontColor() == null) {
				painter.setFontColor(theme.getFontColor());
			}

			// neutralColor -> paint (default paint, linking, etc.)
			if (aggressive || painter.getPaint() == null) {
				painter.setPaint((Paint) theme.getNeutralColor());
			}

			return true;
		} catch (Exception e) {
			LOGGER.log(Level.FINEST, "Error theming painter " + painter.getClass().getName() + ": " + e.getMessage(),
					e);
			return false;
		}
	}

	/**
	 * Applies theme recursively to the component subtree (conservative mode).
	 * 
	 * <p>
	 * Conservative mode only themes components with UIResource properties or null
	 * values, respects the opaque flag, and preserves custom styling.
	 * </p>
	 * 
	 * @param root  The root container
	 * @param theme The theme to apply
	 * @return Number of components successfully themed
	 * @throws NullPointerException if root or theme is null
	 */
	public static int applyThemeRecursively(Container root, VisualizationTheme theme) {
		return applyThemeRecursively(root, theme, false);
	}

	/**
	 * Applies theme recursively to the component subtree.
	 * 
	 * <p>
	 * <b>Conservative mode (aggressive=false):</b>
	 * </p>
	 * <ul>
	 * <li>Only overwrites UIResource or null properties</li>
	 * <li>Respects isOpaque() for backgrounds</li>
	 * <li>Preserves custom borders and styling</li>
	 * <li>Minimal selection color changes</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Aggressive mode (aggressive=true):</b>
	 * </p>
	 * <ul>
	 * <li>Overwrites all properties</li>
	 * <li>Forces theme on all components</li>
	 * <li>Updates selection colors</li>
	 * <li>Themes scroll bars and L&F-dependent elements</li>
	 * </ul>
	 * 
	 * @param root       The root container
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 * @return Number of components successfully themed
	 * @throws NullPointerException if root or theme is null
	 */
	public static int applyThemeRecursively(Container root, VisualizationTheme theme, boolean aggressive) {
		Objects.requireNonNull(root, "Root container cannot be null");
		Objects.requireNonNull(theme, "Theme cannot be null");

		warnIfNotOnEDT();

		int themedCount = 0;
		long startTime = System.nanoTime();

		try {
			// Stack-based iterative traversal (more efficient than recursion)
			Deque<Component> stack = new ArrayDeque<>();
			stack.push(root);

			while (!stack.isEmpty()) {
				Component c = stack.pop();

				if (applyThemeToSingleComponent(c, theme, aggressive)) {
					themedCount++;
				}

				// Push children onto stack (reverse order for natural traversal)
				if (c instanceof Container) {
					Container container = (Container) c;
					Component[] children = container.getComponents();
					for (int i = children.length - 1; i >= 0; i--) {
						if (children[i] != null) {
							stack.push(children[i]);
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error applying theme recursively: " + e.getMessage(), e);
		}

		// Performance logging
		long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format("Theme applied to %d components in %d ms (%s mode)", themedCount, elapsedMs,
					aggressive ? "aggressive" : "conservative"));
		}

		return themedCount;
	}

	/**
	 * Applies theme only to the given component (no recursion, conservative mode).
	 * 
	 * @param component The component to theme
	 * @param theme     The theme to apply
	 * @return true if successful
	 * @throws NullPointerException if component or theme is null
	 */
	public static boolean applyThemeToComponentOnly(Component component, VisualizationTheme theme) {
		return applyThemeToComponentOnly(component, theme, false);
	}

	/**
	 * Applies theme only to the given component with mode selection.
	 * 
	 * @param component  The component to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 * @return true if successful
	 * @throws NullPointerException if component or theme is null
	 */
	public static boolean applyThemeToComponentOnly(Component component, VisualizationTheme theme, boolean aggressive) {
		Objects.requireNonNull(component, "Component cannot be null");
		Objects.requireNonNull(theme, "Theme cannot be null");

		warnIfNotOnEDT();

		return applyThemeToSingleComponent(component, theme, aggressive);
	}

	// ==================== BATCH APPLICATION ====================

	/**
	 * Applies theme to a list of cached components (conservative mode).
	 * 
	 * <p>
	 * Useful when you've pre-collected components for performance optimization.
	 * </p>
	 * 
	 * <p>
	 * <b>Performance Pattern:</b>
	 * </p>
	 * 
	 * <pre>
	 * // Cache once during initialization
	 * List&lt;AbstractButton&gt; buttons = ThemeUtils.collectAllButtons(panel);
	 * 
	 * // Fast application on theme changes
	 * ThemeUtils.applyThemeToCachedComponents(buttons, newTheme);
	 * </pre>
	 * 
	 * @param components List of components to theme
	 * @param theme      The theme to apply
	 * @return Number of components successfully themed
	 * @throws NullPointerException if components or theme is null
	 */
	public static int applyThemeToCachedComponents(List<? extends Component> components, VisualizationTheme theme) {
		return applyThemeToCachedComponents(components, theme, false);
	}

	/**
	 * Applies theme to a list of cached components with mode selection.
	 * 
	 * @param components List of components to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 * @return Number of components successfully themed
	 * @throws NullPointerException if components or theme is null
	 */
	public static int applyThemeToCachedComponents(List<? extends Component> components, VisualizationTheme theme,
			boolean aggressive) {
		Objects.requireNonNull(components, "Components list cannot be null");
		Objects.requireNonNull(theme, "Theme cannot be null");

		warnIfNotOnEDT();

		int count = 0;
		for (Component component : components) {
			if (component != null && applyThemeToSingleComponent(component, theme, aggressive)) {
				count++;
			}
		}

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format("Theme applied to %d cached components (%s mode)", count,
					aggressive ? "aggressive" : "conservative"));
		}

		return count;
	}

	// ==================== COLLECTION HELPERS ====================

	/**
	 * Collects all components of a specific type from the component tree.
	 * 
	 * <p>
	 * <b>Performance Tip:</b> Cache the result and use
	 * {@link #applyThemeToCachedComponents} for repeated theme applications.
	 * </p>
	 * 
	 * @param <T>  The component type
	 * @param root The root container
	 * @param type The class of components to collect
	 * @return List of matching components (never null)
	 * @throws NullPointerException if root or type is null
	 */
	public static <T extends Component> List<T> collectComponents(Container root, Class<T> type) {
		Objects.requireNonNull(root, "Root container cannot be null");
		Objects.requireNonNull(type, "Type cannot be null");

		List<T> result = new ArrayList<>();
		Deque<Component> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			Component c = stack.pop();

			if (type.isInstance(c)) {
				result.add(type.cast(c));
			}

			if (c instanceof Container) {
				Container container = (Container) c;
				Component[] children = container.getComponents();
				for (int i = children.length - 1; i >= 0; i--) {
					if (children[i] != null) {
						stack.push(children[i]);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Collects all buttons (JButton, JToggleButton, etc.) from the component tree.
	 * 
	 * @param root The root container
	 * @return List of all buttons (never null)
	 */
	public static List<AbstractButton> collectAllButtons(Container root) {
		return collectComponents(root, AbstractButton.class);
	}

	/**
	 * Collects all text components from the component tree.
	 * 
	 * @param root The root container
	 * @return List of all text components (never null)
	 */
	public static List<JTextComponent> collectAllTextComponents(Container root) {
		return collectComponents(root, JTextComponent.class);
	}

	/**
	 * Collects all labels from the component tree.
	 * 
	 * @param root The root container
	 * @return List of all labels (never null)
	 */
	public static List<JLabel> collectAllLabels(Container root) {
		return collectComponents(root, JLabel.class);
	}

	// ==================== BORDER UTILITIES ====================

	/**
	 * Creates a themed titled border.
	 * 
	 * @param title The border title
	 * @param theme The theme to use
	 * @return Themed titled border
	 * @throws NullPointerException if theme is null
	 */
	public static TitledBorder createThemedTitledBorder(String title, VisualizationTheme theme) {
		Objects.requireNonNull(theme, "Theme cannot be null");

		return BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(1, theme.getBorderColor(),
						theme.getBorderColor().brighter().brighter()),
				title, TitledBorder.LEFT, TitledBorder.TOP, theme.getBaseFont(), theme.getFontColor());
	}

	/**
	 * Creates a themed line border.
	 * 
	 * @param theme The theme to use
	 * @return Themed line border
	 * @throws NullPointerException if theme is null
	 */
	public static Border createThemedLineBorder(VisualizationTheme theme) {
		Objects.requireNonNull(theme, "Theme cannot be null");
		return BorderFactory.createLineBorder(theme.getBorderColor());
	}

	/**
	 * Creates a themed compound border (line border + padding).
	 * 
	 * @param theme   The theme to use
	 * @param padding Padding in pixels
	 * @return Themed compound border
	 * @throws NullPointerException if theme is null
	 */
	public static Border createThemedCompoundBorder(VisualizationTheme theme, int padding) {
		Objects.requireNonNull(theme, "Theme cannot be null");

		return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(theme.getBorderColor()),
				BorderFactory.createEmptyBorder(padding, padding, padding, padding));
	}

	public static Border createThemedEmptyBorder(int top, int left, int bottom, int right) {
		return BorderFactory.createEmptyBorder(top, left, bottom, right);
	}

	public static Border createThemedMatteBorder(VisualizationTheme theme, int top, int left, int bottom, int right) {
		Objects.requireNonNull(theme, "Theme cannot be null");
		return BorderFactory.createMatteBorder(top, left, bottom, right, theme.getBorderColor());
	}

	public static Border createThemedEtchedBorder(VisualizationTheme theme) {
		Objects.requireNonNull(theme, "Theme cannot be null");
		return BorderFactory.createEtchedBorder(1, theme.getBorderColor(),
				theme.getBorderColor().brighter().brighter());
	}

	public static Border createThemedFocusBorder(VisualizationTheme theme, int thickness) {
		Objects.requireNonNull(theme, "Theme cannot be null");
		return BorderFactory.createLineBorder(theme.getHighlightColor(), thickness);
	}

	public static Border compound(Border outer, Border inner) {
		return BorderFactory.createCompoundBorder(outer, inner);
	}

	// ==================== INTERNAL: APPLY THEMING ====================

	/**
	 * Applies theme to a single component based on its type.
	 */
	private static boolean applyThemeToSingleComponent(Component c, VisualizationTheme theme, boolean aggressive) {
		if (c == null) {
			return false;
		}

		try {
			// Base theming (font, foreground, background)
			applyBasicTheme(c, theme, aggressive);

			// Border adoption (JComponent only)
			if (c instanceof JComponent) {
				//adoptTitledBorderIfPresent((JComponent) c, theme, aggressive);
				adoptBorderIfPresent((JComponent) c, theme, aggressive);
			}

			// Type-specific theming (best-effort, L&F-dependent)
			if (c instanceof JTextComponent) {
				adoptTextComponent((JTextComponent) c, theme, aggressive);
			} else if (c instanceof JScrollPane) {
				adoptScrollPane((JScrollPane) c, theme, aggressive);
			} else if (c instanceof JTable) {
				adoptTable((JTable) c, theme, aggressive);
			} else if (c instanceof JTree) {
				adoptTree((JTree) c, theme, aggressive);
			} else if (c instanceof JList) {
				adoptList((JList<?>) c, theme, aggressive);
			} else if (c instanceof JSpinner) {
				adoptSpinner((JSpinner) c, theme, aggressive);
			} else if (c instanceof JTabbedPane) {
				adoptTabbedPane((JTabbedPane) c, theme, aggressive);
			} else if (c instanceof JSplitPane) {
				adoptSplitPane((JSplitPane) c, theme, aggressive);
			} else if (c instanceof JToolBar) {
				adoptToolBar((JToolBar) c, theme, aggressive);
			} else if (c instanceof JProgressBar) {
				adoptProgressBar((JProgressBar) c, theme, aggressive);
			} else if (c instanceof JMenuBar) {
				adoptMenuBar((JMenuBar) c, theme, aggressive);
			} else if (c instanceof JMenu) {
				adoptMenu((JMenu) c, theme, aggressive);
			} else if (c instanceof JMenuItem) {
				adoptMenuItem((JMenuItem) c, theme, aggressive);
			} else if (c instanceof JPopupMenu) {
				adoptPopupMenu((JPopupMenu) c, theme, aggressive);
			} else if (c instanceof JPanel) {
				adoptPanel((JPanel) c, theme, aggressive);
			}
			// Note: JComboBox, JSlider, AbstractButton, JLabel handled by base theming

			return true;

		} catch (Exception e) {
			LOGGER.log(Level.FINEST, "Error theming component " + c.getClass().getName() + ": " + e.getMessage(), e);
			return false;
		}
	}

	// ==================== BASIC THEME ====================

	/**
	 * Applies basic theme properties in optimal order: font to foreground to
	 * background.
	 * 
	 * <p>
	 * Setting font first ensures proper rendering of foreground/background.
	 * </p>
	 */
	private static void applyBasicTheme(Component c, VisualizationTheme theme, boolean aggressive) {
		// Font first (affects rendering of other properties)
		if (shouldOverrideFont(c, aggressive)) {
			c.setFont(theme.getBaseFont());
		}

		// Foreground second
		if (shouldOverrideForeground(c, aggressive)) {
			c.setForeground(theme.getFontColor());
		}

		// Background last (with opaque check in conservative mode)
		if (shouldOverrideBackground(c, aggressive)) {
			// Conservative: avoid forcing backgrounds on non-opaque components
			if (aggressive || !(c instanceof JComponent) || ((JComponent) c).isOpaque()) {
				c.setBackground(theme.getBackgroundColor());
			}
		}
	}

	/**
	 * Determines if background should be overridden.
	 */
	private static boolean shouldOverrideBackground(Component c, boolean aggressive) {
		if (aggressive) {
			return true;
		}
		Color bg = c.getBackground();
		return (bg == null) || (bg instanceof UIResource);
	}

	/**
	 * Determines if foreground should be overridden.
	 */
	private static boolean shouldOverrideForeground(Component c, boolean aggressive) {
		if (aggressive) {
			return true;
		}
		Color fg = c.getForeground();
		return (fg == null) || (fg instanceof UIResource);
	}

	/**
	 * Determines if font should be overridden.
	 */
	private static boolean shouldOverrideFont(Component c, boolean aggressive) {
		if (aggressive) {
			return true;
		}
		Font font = c.getFont();
		return (font == null) || (font instanceof UIResource);
	}

	// ==================== BORDER ADOPTION ====================

	/**
	 * Updates titled border with theme styling if present.
	 * 
	 * @deprecated to specific. I have generalized this functionality.
	 */
	private static void adoptTitledBorderIfPresent(JComponent jc, VisualizationTheme theme, boolean aggressive) {
		Border border = jc.getBorder();
		if (!(border instanceof TitledBorder)) {
			return;
		}

		TitledBorder titledBorder = (TitledBorder) border;

		// Conservative: preserve custom borders
		if (!aggressive) {
			Font titleFont = titledBorder.getTitleFont();
			if (titleFont != null && !(titleFont instanceof UIResource)) {
				return;
			}
		}

		String title = titledBorder.getTitle();
		jc.setBorder(createThemedTitledBorder(title, theme));
	}

	// ==================== BORDER ADOPTION ====================

	/**
	 * Adopts theme styling for component borders based on border type.
	 * 
	 * <p>
	 * Handles multiple border types intelligently:
	 * </p>
	 * <ul>
	 * <li><b>TitledBorder:</b> Updates title font and color while preserving title
	 * text</li>
	 * <li><b>LineBorder:</b> Updates color to theme border color</li>
	 * <li><b>MatteBorder:</b> Updates color to theme border color</li>
	 * <li><b>EtchedBorder:</b> Updates highlight and shadow colors</li>
	 * <li><b>BevelBorder:</b> Updates highlight and shadow colors</li>
	 * <li><b>CompoundBorder:</b> Recursively themes inner and outer borders</li>
	 * <li><b>EmptyBorder:</b> Preserved (no visual styling)</li>
	 * <li><b>Custom borders:</b> Preserved unless aggressive mode</li>
	 * </ul>
	 * 
	 * <p>
	 * In conservative mode, only themes borders that are UIResource or null. In
	 * aggressive mode, themes all border types (except custom borders which are
	 * preserved).
	 * </p>
	 * 
	 * @param jc         The component whose border to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, themes all borders; if false, only UIResource/null
	 */
	private static void adoptBorderIfPresent(JComponent jc, VisualizationTheme theme, boolean aggressive) {
		Border border = jc.getBorder();

		// No border to theme
		if (border == null) {
			return;
		}

		// Conservative mode: only theme UIResource borders
		if (!aggressive && !(border instanceof UIResource)) {
			// Exception: TitledBorder is often worth theming even in conservative mode
			// if it uses UIResource font
			if (border instanceof TitledBorder) {
				TitledBorder titledBorder = (TitledBorder) border;
				Font titleFont = titledBorder.getTitleFont();
				if (titleFont == null || titleFont instanceof UIResource) {
					jc.setBorder(themedBorder(border, theme));
				}
			}
			return;
		}

		// Aggressive mode or UIResource border: theme it
		Border themedBorder = themedBorder(border, theme);
		if (themedBorder != null) {
			jc.setBorder(themedBorder);
		}
	}

	/**
	 * Creates a themed version of the given border.
	 * 
	 * <p>
	 * Returns a new border with theme colors applied, or null if the border type
	 * cannot be themed (custom border implementations).
	 * </p>
	 * 
	 * @param border The original border
	 * @param theme  The theme to apply
	 * @return Themed border, or null if border cannot be themed
	 */
	private static Border themedBorder(Border border, VisualizationTheme theme) {
		if (border == null) {
			return null;
		}

		// TitledBorder: Preserve title, update styling
		if (border instanceof TitledBorder) {
			TitledBorder tb = (TitledBorder) border;

			// Theme the inner border if it exists
			Border innerBorder = tb.getBorder();
			Border themedInner = (innerBorder != null) ? themedBorder(innerBorder, theme)
					: BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, theme.getBorderColor(),
							theme.getBorderColor().brighter().brighter());

			return BorderFactory.createTitledBorder(themedInner, tb.getTitle(), tb.getTitleJustification(),
					tb.getTitlePosition(), theme.getBaseFont(), theme.getFontColor());
		}

		// LineBorder: Simple color update
		if (border instanceof LineBorder) {
			LineBorder lb = (LineBorder) border;
			int thickness = lb.getThickness();
			boolean roundedCorners = lb.getRoundedCorners();
			return BorderFactory.createLineBorder(theme.getBorderColor(), thickness, roundedCorners);
		}

		// MatteBorder: Update color while preserving insets
		if (border instanceof MatteBorder) {
			MatteBorder mb = (MatteBorder) border;
			Insets insets = mb.getBorderInsets();

			// Check if it uses a tile icon (don't theme those)
			if (mb.getTileIcon() != null) {
				return null; // Preserve icon-based matte borders
			}

			return BorderFactory.createMatteBorder(insets.top, insets.left, insets.bottom, insets.right,
					theme.getBorderColor());
		}

		// EtchedBorder: Update highlight and shadow
		if (border instanceof EtchedBorder) {
			EtchedBorder eb = (EtchedBorder) border;
			int etchType = eb.getEtchType();
			Color highlight = theme.getBorderColor().brighter().brighter();
			Color shadow = theme.getBorderColor().darker();
			return BorderFactory.createEtchedBorder(etchType, highlight, shadow);
		}

		// BevelBorder: Update highlight and shadow
		if (border instanceof BevelBorder) {
			BevelBorder bb = (BevelBorder) border;
			int bevelType = bb.getBevelType();
			Color highlight = theme.getBorderColor().brighter();
			Color shadow = theme.getBorderColor().darker();
			return BorderFactory.createBevelBorder(bevelType, highlight, shadow);
		}

		// CompoundBorder: Recursively theme both borders
		if (border instanceof CompoundBorder) {
			CompoundBorder cb = (CompoundBorder) border;
			Border themedOuter = themedBorder(cb.getOutsideBorder(), theme);
			Border themedInner = themedBorder(cb.getInsideBorder(), theme);

			// If we couldn't theme either, preserve original
			if (themedOuter == null && themedInner == null) {
				return null;
			}

			// Use themed versions where available, otherwise use original
			Border outer = (themedOuter != null) ? themedOuter : cb.getOutsideBorder();
			Border inner = (themedInner != null) ? themedInner : cb.getInsideBorder();

			return BorderFactory.createCompoundBorder(outer, inner);
		}

		// EmptyBorder: Preserve (no visual styling needed)
		if (border instanceof EmptyBorder) {
			return border; // Keep as-is
		}
		
		//TODO
//		if (border instanceof MenuBarBorder) {
//			return border; // Keep as-is
//		}

		// Unknown/custom border type: don't theme
		return null;
	}

	// ==================== COMPONENT-SPECIFIC ADOPTION ====================

	/**
	 * Themes text components (caret, selection).
	 */
	private static void adoptTextComponent(JTextComponent tc, VisualizationTheme theme, boolean aggressive) {
		if (shouldOverrideForeground(tc, aggressive)) {
			tc.setCaretColor(theme.getFontColor());
		}

		// JTextComponent uses different method names
		if (aggressive) {
			tc.setSelectionColor(theme.getHighlightColor()); // Background of selection
			tc.setSelectedTextColor(theme.getBackgroundColor()); // Foreground of selection
		}
	}

	/**
	 * Themes spinners (editor component).
	 */
	private static void adoptSpinner(JSpinner spinner, VisualizationTheme theme, boolean aggressive) {
		Component editor = spinner.getEditor();
		if (editor != null) {
			applyBasicTheme(editor, theme, aggressive);
		}
	}

	/**
	 * Themes scroll panes and their contents.
	 */
	private static void adoptScrollPane(JScrollPane sp, VisualizationTheme theme, boolean aggressive) {
		// Viewport
		JViewport vp = sp.getViewport();
		if (vp != null && shouldOverrideBackground(vp, aggressive)) {
			vp.setBackground(theme.getBackgroundColor());
		}

		// Column header
		JViewport header = sp.getColumnHeader();
		if (header != null && shouldOverrideBackground(header, aggressive)) {
			header.setBackground(theme.getBackgroundColor());
		}

		// Corner component
		Component corner = sp.getCorner(JScrollPane.UPPER_RIGHT_CORNER);
		if (corner != null) {
			applyBasicTheme(corner, theme, aggressive);
		}

		// View component (only in aggressive mode; conservative relies on traversal)
		if (aggressive && vp != null) {
			Component view = vp.getView();
			if (view != null) {
				applyBasicTheme(view, theme, aggressive);
			}
		}

		// Scrollbars (aggressive only)
		if (aggressive) {
			JScrollBar vsb = sp.getVerticalScrollBar();
			if (vsb != null) {
				applyBasicTheme(vsb, theme, true);
			}

			JScrollBar hsb = sp.getHorizontalScrollBar();
			if (hsb != null) {
				applyBasicTheme(hsb, theme, true);
			}
		}

		// Border
		if (aggressive || sp.getBorder() == null || sp.getBorder() instanceof UIResource) {
			sp.setBorder(createThemedLineBorder(theme));
		}
	}

	/**
	 * Themes tables (grid, selection, header).
	 */
	private static void adoptTable(JTable table, VisualizationTheme theme, boolean aggressive) {
		// Grid color is safe even in conservative mode
		table.setGridColor(theme.getBorderColor());

		// Selection colors (aggressive only)
		if (aggressive) {
			table.setSelectionBackground(theme.getHighlightColor());
			table.setSelectionForeground(theme.getBackgroundColor());
		}

		// Table header
		JTableHeader header = table.getTableHeader();
		if (header != null) {
			applyBasicTheme(header, theme, aggressive);
		}
	}

	/**
	 * Themes trees.
	 * 
	 * <p>
	 * <b>Important:</b> JTree does not support direct selection color methods like
	 * {@code setSelectionBackground()} or {@code setSelectionForeground()}.
	 * Selection appearance is entirely controlled by the TreeCellRenderer.
	 * </p>
	 * 
	 * <p>
	 * This method only applies base theming (background, foreground, font) to the
	 * tree component itself. To fully theme tree selection, a custom renderer would
	 * be required, which is beyond the scope of this utility.
	 * </p>
	 * 
	 * @param tree       The tree to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, applies base theme more forcefully
	 */
	private static void adoptTree(JTree tree, VisualizationTheme theme, boolean aggressive) {
		// JTree has NO setSelectionBackground() or setSelectionForeground()
		// Base theming (background, foreground, font) is already applied
		// Selection theming requires custom TreeCellRenderer (too invasive)

		// Nothing additional to do here - base theming is sufficient
	}

	/**
	 * Themes lists (selection colors).
	 */
	private static void adoptList(JList<?> list, VisualizationTheme theme, boolean aggressive) {
		if (aggressive) {
			list.setSelectionBackground(theme.getHighlightColor());
			list.setSelectionForeground(theme.getBackgroundColor());
		}
	}

	/**
	 * Themes tabbed panes (L&F-heavy, minimal in conservative mode).
	 */
	private static void adoptTabbedPane(JTabbedPane tabs, VisualizationTheme theme, boolean aggressive) {
		if (aggressive) {
			tabs.setBackground(theme.getBackgroundColor());
			tabs.setForeground(theme.getFontColor());
			tabs.setFont(theme.getBaseFont());
		}
	}

	/**
	 * Themes split panes (dividers are UI delegate painted).
	 */
	private static void adoptSplitPane(JSplitPane split, VisualizationTheme theme, boolean aggressive) {
		if (aggressive) {
			split.setBackground(theme.getBackgroundColor());
		}
	}

	/**
	 * Themes toolbars.
	 */
	private static void adoptToolBar(JToolBar toolBar, VisualizationTheme theme, boolean aggressive) {
		// Toolbars often want background alignment
		if (shouldOverrideBackground(toolBar, aggressive)) {
			toolBar.setBackground(theme.getBackgroundColor());
		}
		if (shouldOverrideForeground(toolBar, aggressive)) {
			toolBar.setForeground(theme.getFontColor());
		}
		if (shouldOverrideFont(toolBar, aggressive)) {
			toolBar.setFont(theme.getBaseFont());
		}
	}

	/**
	 * Themes progress bars.
	 */
	private static void adoptProgressBar(JProgressBar bar, VisualizationTheme theme, boolean aggressive) {
		if (aggressive) {
			bar.setForeground(theme.getHighlightColor());
			bar.setBackground(theme.getBackgroundColor());
		}
	}

	/**
	 * Themes menu bars with comprehensive look and feel support.
	 * 
	 * <p>
	 * Applies theme to the menu bar itself and recursively themes all contained
	 * menus and menu items. In aggressive mode, updates selection colors and
	 * borders.
	 * </p>
	 * 
	 * @param menuBar    The menu bar to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, applies complete theming including selection
	 *                   colors
	 */
	private static void adoptMenuBar(JMenuBar menuBar, VisualizationTheme theme, boolean aggressive) {
		// Menu bar background and foreground
		if (shouldOverrideBackground(menuBar, aggressive)) {
			menuBar.setBackground(theme.getBackgroundColor());
		}
		if (shouldOverrideForeground(menuBar, aggressive)) {
			menuBar.setForeground(theme.getFontColor());
		}
		if (shouldOverrideFont(menuBar, aggressive)) {
			menuBar.setFont(theme.getBaseFont());
		}

		// Border (aggressive mode or UIResource)
		if (aggressive || menuBar.getBorder() == null || menuBar.getBorder() instanceof UIResource) {
			menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.getBorderColor()));
		}

		// Theme all menus in the menu bar
		int menuCount = menuBar.getMenuCount();
		for (int i = 0; i < menuCount; i++) {
			JMenu menu = menuBar.getMenu(i);
			if (menu != null) {
				adoptMenu(menu, theme, aggressive);
			}
		}
	}

	/**
	 * Themes menus with comprehensive styling.
	 * 
	 * <p>
	 * Applies background, foreground, font, and selection colors. In aggressive
	 * mode, forces theme on all properties.
	 * </p>
	 * 
	 * @param menu       The menu to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 */
	private static void adoptMenu(JMenu menu, VisualizationTheme theme, boolean aggressive) {
		// Base properties
		if (shouldOverrideBackground(menu, aggressive)) {
			menu.setBackground(theme.getBackgroundColor());
		}
		if (shouldOverrideForeground(menu, aggressive)) {
			menu.setForeground(theme.getFontColor());
		}
		if (shouldOverrideFont(menu, aggressive)) {
			menu.setFont(theme.getBaseFont());
		}

		// Selection colors (aggressive mode)
		if (aggressive) {
//			menu.setSelectionForeground(theme.getBackgroundColor());
//			menu.setSelectionBackground(theme.getHighlightColor());
		}

		// Disable roll-over effect border in aggressive mode for cleaner look
		if (aggressive) {
			menu.setBorderPainted(false);
		}

		for (int i = 0; i < menu.getMenuComponents().length; i++)
			if (menu.getItem(i) instanceof JMenuItem)
				adoptMenuItem(menu.getItem(i), theme, aggressive);
	}

	/**
	 * Themes menu items with comprehensive styling.
	 * 
	 * <p>
	 * Handles regular menu items, checkboxes, and radio buttons. Updates selection
	 * colors, accelerator text color, and proper foreground/background.
	 * </p>
	 * 
	 * @param menuItem   The menu item to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 */
	private static void adoptMenuItem(JMenuItem menuItem, VisualizationTheme theme, boolean aggressive) {
		// Base properties
		if (shouldOverrideBackground(menuItem, aggressive)) {
			menuItem.setBackground(theme.getBackgroundColor());
		}
		if (shouldOverrideForeground(menuItem, aggressive)) {
			menuItem.setForeground(theme.getFontColor());
		}
		if (shouldOverrideFont(menuItem, aggressive)) {
			menuItem.setFont(theme.getBaseFont());
		}

		// Selection colors (aggressive mode)
		if (aggressive) {
//			menuItem.setSelectionForeground(theme.getBackgroundColor());
//			menuItem.setSelectionBackground(theme.getHighlightColor());

			// Accelerator text color (e.g., "Ctrl+S")
//			menuItem.setAcceleratorForeground(theme.getFontColor());
//			menuItem.setAcceleratorSelectionForeground(theme.getBackgroundColor());

			// Disable border for cleaner look
			menuItem.setBorderPainted(false);
		}
	}

	/**
	 * Themes popup menus with comprehensive styling.
	 * 
	 * <p>
	 * Updates background, border, and ensures consistency with the overall theme.
	 * Popup menus are the containers that appear when menus are clicked.
	 * </p>
	 * 
	 * @param popup      The popup menu to theme
	 * @param theme      The theme to apply
	 * @param aggressive If true, overwrites all properties
	 */
	private static void adoptPopupMenu(JPopupMenu popup, VisualizationTheme theme, boolean aggressive) {
		// Background
		if (aggressive || shouldOverrideBackground(popup, aggressive)) {
			popup.setBackground(theme.getBackgroundColor());
		}

		// Foreground
		if (shouldOverrideForeground(popup, aggressive)) {
			popup.setForeground(theme.getFontColor());
		}

		// Font
		if (shouldOverrideFont(popup, aggressive)) {
			popup.setFont(theme.getBaseFont());
		}

		// Border for visual separation
		if (aggressive || popup.getBorder() == null || popup.getBorder() instanceof UIResource) {
			popup.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 1),
							BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		}
	}

	/**
	 * Themes panels.
	 */
	private static void adoptPanel(JPanel panel, VisualizationTheme theme, boolean aggressive) {
		// Base theming is sufficient
	}

	// ==================== UTILITIES ====================

	/**
	 * Warns if theme application is not on EDT.
	 */
	private static void warnIfNotOnEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("ThemeUtils should be executed on the EDT (current thread: "
						+ Thread.currentThread().getName() + ")");
			}
		}
	}
}
