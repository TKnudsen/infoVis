package com.github.TKnudsen.infoVis.view.ui.themes;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;

/**
 * <p>
 * Central coordinator for application-wide theme management with
 * comprehensive enterprise features.
 * </p>
 *
 * <p>
 * <b>Architecture:</b>
 * </p>
 * <ul>
 * <li><b>Singleton:</b> Global access point for theme configuration</li>
 * <li><b>Observer:</b> Subscription-based listener pattern with
 * auto-cleanup</li>
 * <li><b>EDT-Safe:</b> All theme changes and notifications on EDT</li>
 * <li><b>Performance:</b> Token reuse, adaptive sizing cool-down,
 * de-bouncing</li>
 * <li><b>Integration:</b> Optional NimbusUITools synchronization</li>
 * </ul>
 *
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 * <li>Subscription pattern for automatic listener cleanup</li>
 * <li>Type-safe theme presets via enum</li>
 * <li>De-bounced adaptive sizing with configurable delay</li>
 * <li>Font size controls with bounds checking</li>
 * <li>Configurable NimbusUITools integration</li>
 * <li>Comprehensive diagnostics and logging</li>
 * <li>Thread-safe with proper EDT handling</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * <ul>
 * <li>All theme mutations occur on EDT</li>
 * <li>Can be called from any thread (auto-dispatches to EDT)</li>
 * <li>Volatile currentTheme for safe reads</li>
 * <li>Thread-safe listener list (CopyOnWriteArrayList)</li>
 * </ul>
 *
 * <p>
 * <b>Example Usage:</b>
 * </p>
 *
 * <pre>
 * ThemeManager tm = ThemeManager.getInstance();
 *
 * // Set theme with preset
 * tm.setThemePreset(ThemePreset.DARK, true);
 *
 * // Subscribe to changes with auto-cleanup
 * try (Subscription sub = tm.addThemeChangeListener(this::onThemeChange)) {
 * 	// Listener active
 * } // Automatically unregistered
 *
 * // Setup debounced resize
 * frame.addComponentListener(tm.createAdaptiveResizeListener(frame, 120));
 * </pre>
 *
 * @version 1.0
 * @since 2026
 */
public final class VisualizationThemeManager {

	private static final Logger LOGGER = Logger.getLogger(VisualizationThemeManager.class.getName());

	// ==================== CONSTANTS ====================

	/**
	 * Default font size used for NimbusUITools scale factor calculation.
	 */
	private static final double NIMBUS_DEFAULT_FONT_SIZE = 12.0;

	/**
	 * Cool-down period for adaptive sizing updates (milliseconds).
	 */
	private static final long ADAPTIVE_SIZING_COOLDOWN_MS = 100;

	// ==================== SINGLETON ====================

	private static volatile VisualizationThemeManager instance;

	/**
	 * Gets the singleton instance with double-checked locking.
	 * 
	 * @return ThemeManager instance (never null)
	 */
	public static VisualizationThemeManager getInstance() {
		if (instance == null) {
			synchronized (VisualizationThemeManager.class) {
				if (instance == null) {
					instance = new VisualizationThemeManager();
				}
			}
		}
		return instance;
	}

	// ==================== THEME PRESETS ====================

	/**
	 * Predefined theme configurations for easy switching.
	 * 
	 * <p>
	 * All presets use default metrics (12pt font, 3.0 offset). Use
	 * {@link #setThemePreset(ThemePreset, boolean)} with preserveMetrics=true to
	 * keep current sizing.
	 * </p>
	 */
	public enum ThemePreset {
		/** Nimbus theme derived from NimbusUITools current settings */
		NIMBUS_UI_TOOLS {
			@Override
			public VisualizationTheme createTheme() {
				return VisualizationThemes.fromNimbusUITools();
			}
		},

		/** Light Nimbus theme (dark text on light background) */
		NIMBUS_LIGHT {
			@Override
			public VisualizationTheme createTheme() {
				return VisualizationThemes.nimbusLight();
			}
		},

		/** Classic white theme */
		CLASSIC {
			@Override
			public VisualizationTheme createTheme() {
				return VisualizationThemes.classic();
			}
		},

		/** Dark theme (light text on dark background) */
		DARK {
			@Override
			public VisualizationTheme createTheme() {
				return VisualizationThemes.dark();
			}
		};

		/**
		 * Creates the theme with default metrics.
		 * 
		 * @return Theme instance
		 */
		public abstract VisualizationTheme createTheme();
	}

	// ==================== FIELDS ====================

	// Current theme (volatile for cross-thread visibility)
	private volatile VisualizationTheme currentTheme;

	// Theme change listeners (thread-safe list)
	private final CopyOnWriteArrayList<ThemeChangeListener> listeners = new CopyOnWriteArrayList<>();

	// Configuration
	private volatile boolean nimbusScalingEnabled = true;

	// Adaptive sizing state
	private volatile long lastAdaptiveSizingTime = 0;

	// ==================== CONSTRUCTOR ====================

	private VisualizationThemeManager() {
		// Default to Nimbus UI Tools theme
		this.currentTheme = VisualizationThemes.fromNimbusUITools();
		LOGGER.info("ThemeManager initialized with default theme: " + currentTheme);
	}

	// ==================== LISTENER INTERFACE ====================

	/**
	 * Listener interface for theme changes.
	 * 
	 * <p>
	 * <b>Thread Safety:</b> Always invoked on EDT.
	 * </p>
	 */
	@FunctionalInterface
	public interface ThemeChangeListener {
		/**
		 * Called when theme changes.
		 * 
		 * @param oldTheme Previous theme
		 * @param newTheme New theme
		 */
		void themeChanged(VisualizationTheme oldTheme, VisualizationTheme newTheme);
	}

	/**
	 * Subscription handle for automatic listener cleanup.
	 * 
	 * <p>
	 * Use try-with-resources or explicitly call {@link #close()} to unregister:
	 * </p>
	 * 
	 * <pre>
	 * try (Subscription sub = themeManager.addThemeChangeListener(this::onThemeChange)) {
	 * 	// Listener active here
	 * } // Automatically unregistered
	 * </pre>
	 */
	@FunctionalInterface
	public interface Subscription extends AutoCloseable {
		/**
		 * Unregisters the listener. Safe to call multiple times.
		 */
		@Override
		void close();
	}

	// ==================== THEME ACCESS ====================

	/**
	 * Gets the current theme.
	 * 
	 * <p>
	 * <b>Thread Safety:</b> Safe to call from any thread.
	 * </p>
	 * 
	 * @return Current visualization theme (never null)
	 */
	public VisualizationTheme getTheme() {
		return currentTheme;
	}

	/**
	 * Sets a new theme and notifies all listeners.
	 * 
	 * <p>
	 * <b>Thread Safety:</b> Can be called from any thread. If not on EDT,
	 * automatically dispatches to EDT.
	 * </p>
	 * 
	 * <p>
	 * <b>Deduplication:</b> If new theme equals current theme, no action is taken.
	 * </p>
	 * 
	 * <p>
	 * <b>NimbusUITools:</b> If enabled, automatically syncs scale factor.
	 * </p>
	 * 
	 * @param theme New theme
	 * @throws NullPointerException if theme is null
	 */
	public void setTheme(VisualizationTheme theme) {
		Objects.requireNonNull(theme, "theme cannot be null");

		if (SwingUtilities.isEventDispatchThread()) {
			setThemeOnEDT(theme);
		} else {
			SwingUtilities.invokeLater(() -> setThemeOnEDT(theme));
		}
	}

	/**
	 * Internal theme setting (must be called on EDT).
	 */
	private void setThemeOnEDT(VisualizationTheme theme) {
		VisualizationTheme oldTheme = this.currentTheme;

		// Suppress redundant updates
		if (oldTheme.equals(theme)) {
			LOGGER.fine("Theme unchanged, skipping update");
			return;
		}

		this.currentTheme = theme;

		LOGGER.info(String.format("Theme changed: %s -> %s", oldTheme, theme));

		// Sync with NimbusUITools if enabled
		if (nimbusScalingEnabled) {
			syncWithNimbusUITools(theme);
		}

		// Notify all listeners
		notifyListeners(oldTheme, theme);
	}

	/**
	 * Sets a predefined theme using the preset enum.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * </p>
	 * 
	 * <pre>
	 * // Switch to dark theme with default sizing
	 * themeManager.setThemePreset(ThemePreset.DARK, false);
	 * 
	 * // Switch to light theme, keep current font size and offset
	 * themeManager.setThemePreset(ThemePreset.NIMBUS_LIGHT, true);
	 * </pre>
	 * 
	 * @param preset          Theme preset to apply
	 * @param preserveMetrics If true, keeps current font size and offset
	 * @throws NullPointerException if preset is null
	 */
	public void setThemePreset(ThemePreset preset, boolean preserveMetrics) {
		Objects.requireNonNull(preset, "preset cannot be null");

		VisualizationTheme newTheme = preset.createTheme();

		if (preserveMetrics) {
			// Preserve current dynamic metrics
			newTheme = newTheme.withBaseFontSize(currentTheme.getBaseFontSize())
					.withGlobalOffset(currentTheme.getGlobalOffset());
		}

		setTheme(newTheme);
	}

	/**
	 * Sets theme by preset name (case-insensitive).
	 * 
	 * @param presetName      Name of preset ("nimbus_light", "dark", etc.)
	 * @param preserveMetrics If true, keeps current font size and offset
	 * @return true if preset was found and applied
	 */
	public boolean setThemeByName(String presetName, boolean preserveMetrics) {
		Objects.requireNonNull(presetName, "preset name cannot be null");

		try {
			ThemePreset preset = ThemePreset.valueOf(presetName.toUpperCase().replace(' ', '_').replace('-', '_'));
			setThemePreset(preset, preserveMetrics);
			return true;
		} catch (IllegalArgumentException e) {
			LOGGER.warning("Unknown theme preset: " + presetName);
			return false;
		}
	}

	// ==================== ADAPTIVE SIZING ====================

	/**
	 * Updates theme with adaptive sizing based on viewport dimensions.
	 * 
	 * <p>
	 * <b>Performance:</b> Includes cooldown to throttle rapid resize events
	 * (100ms). Only updates if metrics actually change. Reuses tokens for optimal
	 * performance.
	 * </p>
	 * 
	 * <p>
	 * <b>Thread Safety:</b> Can be called from any thread.
	 * </p>
	 * 
	 * @param viewportWidth  Viewport width in pixels (must be > 0)
	 * @param viewportHeight Viewport height in pixels (must be > 0)
	 */
	public void updateAdaptiveSizing(int viewportWidth, int viewportHeight) {
		// Validate dimensions
		if (viewportWidth <= 0 || viewportHeight <= 0) {
			LOGGER.fine("Invalid viewport dimensions, skipping adaptive sizing");
			return;
		}

		// Cooldown check to prevent excessive updates
		long now = System.currentTimeMillis();
		if ((now - lastAdaptiveSizingTime) < ADAPTIVE_SIZING_COOLDOWN_MS) {
			return; // Throttle
		}
		lastAdaptiveSizingTime = now;

		// Create adapted theme (reuses tokens)
		VisualizationTheme adaptedTheme = currentTheme.withAdaptiveSizing(viewportWidth, viewportHeight);

		// Only update if theme actually changed
		if (!adaptedTheme.equals(currentTheme)) {
			setTheme(adaptedTheme);
		}
	}

	/**
	 * Creates a debounced component listener for adaptive sizing.
	 * 
	 * <p>
	 * <b>Usage Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * // With 120ms debounce
	 * ComponentAdapter listener = themeManager.createAdaptiveResizeListener(frame, 120);
	 * frame.addComponentListener(listener);
	 * 
	 * // No debounce (immediate updates)
	 * ComponentAdapter listener = themeManager.createAdaptiveResizeListener(frame, 0);
	 * frame.addComponentListener(listener);
	 * </pre>
	 * 
	 * @param component  Component providing width/height
	 * @param debounceMs Debounce interval in milliseconds (0 = no debounce)
	 * @return Component listener that updates adaptive sizing
	 * @throws NullPointerException     if component is null
	 * @throws IllegalArgumentException if debounceMs is negative
	 */
	public ComponentAdapter createAdaptiveResizeListener(Component component, int debounceMs) {
		Objects.requireNonNull(component, "component cannot be null");
		if (debounceMs < 0) {
			throw new IllegalArgumentException("debounceMs must be >= 0");
		}

		// No de-bounce: simple listener
		if (debounceMs == 0) {
			return new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					updateAdaptiveSizing(component.getWidth(), component.getHeight());
				}
			};
		}

		// De-bounced listener using Swing Timer
		Timer debounceTimer = new Timer(debounceMs, e -> {
			updateAdaptiveSizing(component.getWidth(), component.getHeight());
		});
		debounceTimer.setRepeats(false);

		return new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				debounceTimer.restart();
			}
		};
	}

	// ==================== FONT SIZE CONTROLS ====================

	/**
	 * Sets explicit base font size.
	 * 
	 * @param fontSize Font size in points (must be within theme's range)
	 * @throws IllegalArgumentException if size is out of range
	 */
	public void setBaseFontSize(int fontSize) {
		VisualizationTheme.Metrics metrics = currentTheme.metrics();

		if (fontSize < metrics.minFontSize() || fontSize > metrics.maxFontSize()) {
			throw new IllegalArgumentException(String.format("Font size %d out of range [%d, %d]", fontSize,
					metrics.minFontSize(), metrics.maxFontSize()));
		}

		VisualizationTheme newTheme = currentTheme.withBaseFontSize(fontSize);
		if (!newTheme.equals(currentTheme)) {
			setTheme(newTheme);
		}
	}

	/**
	 * Gets current base font size.
	 * 
	 * @return Font size in points
	 */
	public int getBaseFontSize() {
		return currentTheme.getBaseFontSize();
	}

	/**
	 * Increases font size by one point (within range).
	 * 
	 * @return true if size was increased, false if already at maximum
	 */
	public boolean increaseFontSize() {
		VisualizationTheme.Metrics metrics = currentTheme.metrics();
		int current = metrics.baseFontSize();
		int max = metrics.maxFontSize();

		if (current < max) {
			setBaseFontSize(current + 1);
			return true;
		}
		return false;
	}

	/**
	 * Decreases font size by one point (within range).
	 * 
	 * @return true if size was decreased, false if already at minimum
	 */
	public boolean decreaseFontSize() {
		VisualizationTheme.Metrics metrics = currentTheme.metrics();
		int current = metrics.baseFontSize();
		int min = metrics.minFontSize();

		if (current > min) {
			setBaseFontSize(current - 1);
			return true;
		}
		return false;
	}

	// ==================== NIMBUS INTEGRATION ====================

	/**
	 * Gets whether NimbusUITools synchronization is enabled.
	 * 
	 * @return true if sync is enabled
	 */
	public boolean isNimbusScalingEnabled() {
		return nimbusScalingEnabled;
	}

	/**
	 * Sets whether NimbusUITools synchronization is enabled.
	 * 
	 * <p>
	 * When enabled, theme changes automatically update NimbusUITools scale factor
	 * based on current font size.
	 * </p>
	 * 
	 * @param enabled true to enable sync
	 */
	public void setNimbusScalingEnabled(boolean enabled) {
		boolean oldValue = this.nimbusScalingEnabled;
		this.nimbusScalingEnabled = enabled;

		if (oldValue != enabled) {
			LOGGER.info(String.format("NimbusUITools scaling %s", enabled ? "enabled" : "disabled"));

			// If enabling, sync immediately
			if (enabled) {
				syncWithNimbusUITools(currentTheme);
			}
		}
	}

	/**
	 * Synchronizes theme font size with NimbusUITools scale factor.
	 * 
	 * <p>
	 * Scale factor = currentFontSize / defaultFontSize (12pt)
	 * </p>
	 */
	private void syncWithNimbusUITools(VisualizationTheme theme) {
		try {
			double currentFontSize = theme.getBaseFontSize();
			double scaleFactor = currentFontSize / NIMBUS_DEFAULT_FONT_SIZE;

			double currentNimbusScale = NimbusUITools.getScaleFactor();

			if (Math.abs(scaleFactor - currentNimbusScale) > 0.01) {
				LOGGER.fine(
						String.format("Syncing NimbusUITools scale: %.2f -> %.2f", currentNimbusScale, scaleFactor));
				NimbusUITools.setScaleFactor(scaleFactor);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to sync with NimbusUITools", e);
		}
	}

	// ==================== LISTENER MANAGEMENT ====================

	/**
	 * Registers a theme change listener with subscription handle.
	 * 
	 * <p>
	 * <b>Recommended Pattern:</b>
	 * </p>
	 * 
	 * <pre>
	 * // In constructor
	 * this.themeSubscription = themeManager.addThemeChangeListener(this::onThemeChange);
	 * 
	 * // In dispose()
	 * themeSubscription.close();
	 * 
	 * // Or with try-with-resources
	 * try (Subscription sub = themeManager.addThemeChangeListener(this::onThemeChange)) {
	 * 	// Listener automatically unregistered
	 * }
	 * </pre>
	 * 
	 * @param listener Listener to add
	 * @return Subscription handle for cleanup
	 * @throws NullPointerException if listener is null
	 */
	public Subscription addThemeChangeListener(ThemeChangeListener listener) {
		Objects.requireNonNull(listener, "listener cannot be null");

		if (!listeners.contains(listener)) {
			listeners.add(listener);
			LOGGER.fine("Theme listener registered: " + listener.getClass().getName());
		} else {
			LOGGER.fine("Theme listener already registered: " + listener.getClass().getName());
		}

		return () -> removeThemeChangeListener(listener);
	}

	/**
	 * Removes a theme change listener.
	 * 
	 * @param listener Listener to remove
	 * @return true if listener was removed
	 */
	public boolean removeThemeChangeListener(ThemeChangeListener listener) {
		if (listener == null) {
			return false;
		}

		boolean removed = listeners.remove(listener);
		if (removed) {
			LOGGER.fine("Theme listener removed: " + listener.getClass().getName());
		}
		return removed;
	}

	/**
	 * Gets the number of registered listeners.
	 * 
	 * @return Listener count
	 */
	public int getListenerCount() {
		return listeners.size();
	}

	/**
	 * Notifies all listeners of theme change (must be on EDT).
	 */
	private void notifyListeners(VisualizationTheme oldTheme, VisualizationTheme newTheme) {
		if (listeners.isEmpty()) {
			return;
		}

		int notified = 0;
		int errors = 0;

		for (ThemeChangeListener listener : listeners) {
			try {
				listener.themeChanged(oldTheme, newTheme);
				notified++;
			} catch (Exception e) {
				errors++;
				LOGGER.log(Level.SEVERE, "Error notifying listener: " + listener.getClass().getName(), e);
			}
		}

		LOGGER.fine(String.format("Theme change notification: %d notified, %d errors", notified, errors));
	}

	// ==================== UTILITY METHODS ====================

	/**
	 * Resets to default theme (Nimbus UI Tools).
	 */
	public void resetToDefault() {
		LOGGER.info("Resetting to default theme");
		setTheme(VisualizationThemes.fromNimbusUITools());
	}

	/**
	 * Gets detailed summary of current theme.
	 * 
	 * @return Human-readable theme description
	 */
	public String getThemeSummary() {
		return currentTheme.toDetailedString();
	}

	/**
	 * Gets manager state for debugging.
	 * 
	 * @return Manager state information
	 */
	public String getManagerState() {
		return String.format(
				"ThemeManager State:\n" + "  Current Theme: %s\n" + "  Listeners: %d registered\n"
						+ "  Nimbus Scaling: %s\n" + "  Last Resize: %d ms ago\n" + "  NimbusUITools Scale: %.2f",
				currentTheme, listeners.size(), nimbusScalingEnabled ? "enabled" : "disabled",
				System.currentTimeMillis() - lastAdaptiveSizingTime, NimbusUITools.getScaleFactor());
	}

	/**
	 * Logs comprehensive diagnostics to logger.
	 */
	public void logDiagnostics() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n============================================================\n");
		sb.append("THEME MANAGER DIAGNOSTICS\n");
		sb.append("============================================================\n");
		sb.append(String.format("Current Theme: %s%n", currentTheme));
		sb.append(String.format("Listeners: %d registered%n", listeners.size()));
		sb.append(String.format("Nimbus Scaling: %s%n", nimbusScalingEnabled ? "enabled" : "disabled"));

		if (!listeners.isEmpty()) {
			sb.append("------------------------------------------------------------\n");
			sb.append("REGISTERED LISTENERS\n");
			for (ThemeChangeListener listener : listeners) {
				sb.append(String.format("  - %s%n", listener.getClass().getName()));
			}
		}

		sb.append("------------------------------------------------------------\n");
		sb.append("THEME DETAILS\n");
		sb.append(String.format("%s%n", currentTheme.tokens()));
		sb.append(String.format("%s%n", currentTheme.metrics()));

		if (nimbusScalingEnabled) {
			sb.append("------------------------------------------------------------\n");
			sb.append("NIMBUS INTEGRATION\n");
			sb.append(String.format("Scale Factor: %.2f%n", NimbusUITools.getScaleFactor()));
			sb.append(String.format("Font Size: %d pt%n", currentTheme.getBaseFontSize()));
		}

		sb.append("============================================================\n");

		LOGGER.info(sb.toString());
	}

	// ==================== TESTING SUPPORT ====================

	/**
	 * Resets singleton instance (for testing only).
	 * 
	 * <p>
	 * <b>WARNING:</b> Only use in unit tests.
	 * </p>
	 */
	static void resetInstanceForTesting() {
		synchronized (VisualizationThemeManager.class) {
			instance = null;
		}
	}

	/**
	 * Clears all listeners (for testing/cleanup).
	 */
	public void clearListeners() {
		int count = listeners.size();
		listeners.clear();
		LOGGER.info(String.format("Cleared %d listeners", count));
	}
}