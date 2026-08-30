package com.github.TKnudsen.infoVis.view.ui.themes;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import com.github.TKnudsen.infoVis.view.painters.texture.HatchingUtils;

/**
 * <p>
 * Immutable theme with explicit Tokens/Metrics separation. Tokens define
 * visual identity (cacheable/shareable). Metrics adapt to viewport (recreated
 * on resize).
 * 
 * <b>Performance:</b> Token instances are reused during adaptive sizing. Only
 * Metrics objects are recreated on viewport changes.
 * 
 * <b>Thread Safety:</b> Fully immutable and thread-safe.
 * </p>
 *
 * @version 1.02 (Enhanced Hatching)
 */
public final class VisualizationTheme {

	private static final Logger LOGGER = Logger.getLogger(VisualizationTheme.class.getName());

	// ==================== INNER CLASSES ====================

	/**
	 * Static design tokens (colors, patterns, typography family). Rarely change and
	 * can be cached/shared across theme instances.
	 */
	public static final class Tokens {
		private final Color backgroundColor;
		private final Color borderColor;
		private final Color fontColor;
		private final Color neutralColor;
		private final Color binColor;
		private final Color highlightColor;

		private final double[] hatchingPattern;
		private final Color[] hatchingColors;
		private final boolean darkTheme;

		private Tokens(Builder b) {
			this.backgroundColor = b.backgroundColor;
			this.borderColor = b.borderColor;
			this.fontColor = b.fontColor;
			this.neutralColor = b.neutralColor;
			this.binColor = b.binColor;
			this.highlightColor = b.highlightColor;
			this.hatchingPattern = b.hatchingPattern.clone();
			this.hatchingColors = b.hatchingColors.clone();
			this.darkTheme = b.darkTheme;
		}

		public Color backgroundColor() {
			return backgroundColor;
		}

		public Color borderColor() {
			return borderColor;
		}

		public Color fontColor() {
			return fontColor;
		}

		public Color neutralColor() {
			return neutralColor;
		}

		public Color binColor() {
			return binColor;
		}

		public Color highlightColor() {
			return highlightColor;
		}

		public double[] hatchingPattern() {
			return hatchingPattern.clone();
		}

		public Color[] hatchingColors() {
			return hatchingColors.clone();
		}

		public boolean isDarkTheme() {
			return darkTheme;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Tokens))
				return false;
			Tokens other = (Tokens) obj;
			return Objects.equals(backgroundColor, other.backgroundColor)
					&& Objects.equals(borderColor, other.borderColor) && Objects.equals(fontColor, other.fontColor)
					&& Objects.equals(neutralColor, other.neutralColor) && Objects.equals(binColor, other.binColor)
					&& Objects.equals(highlightColor, other.highlightColor)
					&& Arrays.equals(hatchingPattern, other.hatchingPattern)
					&& Arrays.equals(hatchingColors, other.hatchingColors);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(backgroundColor, borderColor, fontColor, neutralColor, binColor, highlightColor);
			result = 31 * result + Arrays.hashCode(hatchingPattern);
			result = 31 * result + Arrays.hashCode(hatchingColors);
			return result;
		}

		@Override
		public String toString() {
			return String.format("Tokens{bg=%s, border=%s, font=%s, hatching=%d colors}", backgroundColor, borderColor,
					fontColor, hatchingColors.length);
		}

	}

	/**
	 * Dynamic metrics (font sizing, offsets, spacing). Adapt to viewport size and
	 * are recreated on resize events.
	 */
	public static final class Metrics {
		private final String fontFamily;
		private final int baseFontSize;
		private final int minFontSize;
		private final int maxFontSize;
		private final Font baseFont;

		private final double globalOffset;
		private final double minGlobalOffset;
		private final double maxGlobalOffset;

		private Metrics(Builder b) {
			this.fontFamily = b.fontFamily;
			this.baseFontSize = b.baseFontSize;
			this.minFontSize = b.minFontSize;
			this.maxFontSize = b.maxFontSize;
			this.globalOffset = b.globalOffset;
			this.minGlobalOffset = b.minGlobalOffset;
			this.maxGlobalOffset = b.maxGlobalOffset;
			this.baseFont = new Font(fontFamily, Font.PLAIN, baseFontSize);
		}

		public String fontFamily() {
			return fontFamily;
		}

		public int baseFontSize() {
			return baseFontSize;
		}

		public int minFontSize() {
			return minFontSize;
		}

		public int maxFontSize() {
			return maxFontSize;
		}

		public Font baseFont() {
			return baseFont;
		}

		public Font font(int style, int size) {
			return new Font(fontFamily, style, size);
		}

		public double globalOffset() {
			return globalOffset;
		}

		public double minGlobalOffset() {
			return minGlobalOffset;
		}

		public double maxGlobalOffset() {
			return maxGlobalOffset;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Metrics))
				return false;
			Metrics other = (Metrics) obj;
			return Objects.equals(fontFamily, other.fontFamily) && baseFontSize == other.baseFontSize
					&& minFontSize == other.minFontSize && maxFontSize == other.maxFontSize
					&& Double.compare(globalOffset, other.globalOffset) == 0
					&& Double.compare(minGlobalOffset, other.minGlobalOffset) == 0
					&& Double.compare(maxGlobalOffset, other.maxGlobalOffset) == 0;
		}

		@Override
		public int hashCode() {
			return Objects.hash(fontFamily, baseFontSize, minFontSize, maxFontSize, globalOffset, minGlobalOffset,
					maxGlobalOffset);
		}

		@Override
		public String toString() {
			return String.format("Metrics{font=%s %dpt (%d-%d), offset=%.2f (%.2f-%.2f)}", fontFamily, baseFontSize,
					minFontSize, maxFontSize, globalOffset, minGlobalOffset, maxGlobalOffset);
		}
	}

	// ==================== FIELDS ====================

	private final Tokens tokens;
	private final Metrics metrics;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Internal constructor from Builder (creates new Tokens and Metrics).
	 */
	private VisualizationTheme(Builder b) {
		this.tokens = new Tokens(b);
		this.metrics = new Metrics(b);
		LOGGER.fine(() -> "VisualizationTheme created: " + this);
	}

	/**
	 * Internal constructor for token reuse (performance optimization). Used by
	 * withAdaptiveSizing() to reuse tokens instance.
	 */
	private VisualizationTheme(Tokens tokens, Metrics metrics) {
		this.tokens = Objects.requireNonNull(tokens, "tokens required");
		this.metrics = Objects.requireNonNull(metrics, "metrics required");
	}

	// ==================== BUILDER ====================

	public static final class Builder {
		// Required
		private final Color backgroundColor;
		private final Color borderColor;
		private final Color fontColor;

		// Optional tokens
		private Color neutralColor;
		private Color binColor;
		private Color highlightColor = new Color(170, 170, 170);
		private double[] hatchingPattern = new double[] { 3, 3 };
		private Color[] hatchingColors;
		private boolean darkTheme;

		// Optional metrics
		private String fontFamily = "Tahoma";
		private int baseFontSize = 12;
		private int minFontSize = 9;
		private int maxFontSize = 16;
		private double globalOffset = 3.0;
		private double minGlobalOffset = 2.0;
		private double maxGlobalOffset = 10.0;

		public Builder(Color backgroundColor, Color borderColor, Color fontColor) {
			this.backgroundColor = Objects.requireNonNull(backgroundColor, "backgroundColor required");
			this.borderColor = Objects.requireNonNull(borderColor, "borderColor required");
			this.fontColor = Objects.requireNonNull(fontColor, "fontColor required");
		}

		// ========== Token Setters ==========

		public Builder neutralColor(Color color) {
			this.neutralColor = Objects.requireNonNull(color);
			return this;
		}

		public Builder binColor(Color color) {
			this.binColor = Objects.requireNonNull(color);
			return this;
		}

		public Builder highlightColor(Color color) {
			this.highlightColor = Objects.requireNonNull(color);
			return this;
		}

		public Builder hatchingPattern(double[] pattern) {
			Objects.requireNonNull(pattern);
			if (pattern.length == 0) {
				throw new IllegalArgumentException("hatchingPattern must not be empty");
			}
			this.hatchingPattern = pattern.clone();
			return this;
		}

		public Builder hatchingColors(Color[] colors) {
			Objects.requireNonNull(colors);
			if (colors.length < 2) {
				throw new IllegalArgumentException("hatchingColors must have at least 2 colors");
			}
			this.hatchingColors = colors.clone();
			return this;
		}

		public Builder darkTheme(boolean darkTheme) {
			this.darkTheme = darkTheme;
			return this;
		}

		// ========== Metric Setters ==========

		public Builder fontFamily(String family) {
			this.fontFamily = Objects.requireNonNull(family);
			return this;
		}

		public Builder baseFontSize(int size) {
			if (size <= 0)
				throw new IllegalArgumentException("baseFontSize must be > 0");
			this.baseFontSize = size;
			return this;
		}

		public Builder fontSizeRange(int min, int max) {
			if (min <= 0)
				throw new IllegalArgumentException("minFontSize must be > 0");
			if (max < min)
				throw new IllegalArgumentException("maxFontSize must be >= minFontSize");
			this.minFontSize = min;
			this.maxFontSize = max;
			return this;
		}

		public Builder font(String family, int size) {
			return fontFamily(family).baseFontSize(size);
		}

		public Builder globalOffset(double offset) {
			this.globalOffset = offset;
			return this;
		}

		public Builder offsetRange(double min, double max) {
			if (min < 0.0)
				throw new IllegalArgumentException("minGlobalOffset must be >= 0");
			if (max < min)
				throw new IllegalArgumentException("maxGlobalOffset must be >= minGlobalOffset");
			this.minGlobalOffset = min;
			this.maxGlobalOffset = max;
			return this;
		}

		// ========== Build ==========

		public VisualizationTheme build() {
			applyDefaults();
			validate();
			return new VisualizationTheme(this);
		}

		private void applyDefaults() {
			if (neutralColor == null) {
				neutralColor = new Color(64, 64, 64);
			}
			if (binColor == null) {
				binColor = new Color(100, 100, 100);
			}
			if (hatchingPattern == null || hatchingPattern.length == 0) {
				hatchingPattern = HatchingUtils.ensureHatchingPattern(hatchingPattern);
			}
			if (hatchingColors == null) {
				hatchingColors = HatchingUtils.generateAdaptiveHatchingColors(backgroundColor, borderColor, fontColor,
						2);
			}
		}

		private void validate() {
			if (baseFontSize < minFontSize || baseFontSize > maxFontSize) {
				throw new IllegalArgumentException(String.format("baseFontSize=%d not within range [%d, %d]",
						baseFontSize, minFontSize, maxFontSize));
			}
			if (globalOffset < minGlobalOffset || globalOffset > maxGlobalOffset) {
				throw new IllegalArgumentException(String.format("globalOffset=%.3f not within range [%.3f, %.3f]",
						globalOffset, minGlobalOffset, maxGlobalOffset));
			}
		}
	}

	// ==================== ADAPTIVE METHODS (WITH TOKEN REUSE) ====================

	public VisualizationTheme withAdaptiveSizing(int viewportWidth, int viewportHeight) {
		int newFontSize = calculateAdaptiveFontSize(viewportHeight);
		double newOffset = calculateAdaptiveOffset(viewportWidth, viewportHeight);

		if (newFontSize == metrics.baseFontSize && Math.abs(newOffset - metrics.globalOffset) < 0.01) {
			return this; // No change
		}

		Builder b = toBuilder().baseFontSize(newFontSize).globalOffset(newOffset);

		Metrics newMetrics = new Metrics(b);
		return new VisualizationTheme(this.tokens, newMetrics); // TOKEN REUSE
	}

	public VisualizationTheme withBaseFontSize(int size) {
		if (size == metrics.baseFontSize) {
			return this;
		}
		Builder b = toBuilder().baseFontSize(size);
		Metrics newMetrics = new Metrics(b);
		return new VisualizationTheme(this.tokens, newMetrics); // TOKEN REUSE
	}

	public VisualizationTheme withGlobalOffset(double offset) {
		if (Double.compare(offset, metrics.globalOffset) == 0) {
			return this;
		}
		Builder b = toBuilder().globalOffset(offset);
		Metrics newMetrics = new Metrics(b);
		return new VisualizationTheme(this.tokens, newMetrics); // TOKEN REUSE
	}

	public Builder toBuilder() {
		return new Builder(tokens.backgroundColor, tokens.borderColor, tokens.fontColor)
				.neutralColor(tokens.neutralColor).binColor(tokens.binColor).highlightColor(tokens.highlightColor)
				.hatchingPattern(tokens.hatchingPattern()).hatchingColors(tokens.hatchingColors())
				.font(metrics.fontFamily, metrics.baseFontSize).fontSizeRange(metrics.minFontSize, metrics.maxFontSize)
				.globalOffset(metrics.globalOffset).offsetRange(metrics.minGlobalOffset, metrics.maxGlobalOffset);
	}

	// ==================== CALCULATION HELPERS ====================

	private int calculateAdaptiveFontSize(int viewportHeight) {
		int size = (int) (viewportHeight * 0.01);
		return Math.max(metrics.minFontSize, Math.min(metrics.maxFontSize, size));
	}

	private double calculateAdaptiveOffset(int viewportWidth, int viewportHeight) {
		double minDimension = Math.min(viewportWidth, viewportHeight);
		double offset = minDimension * 0.0066;
		return Math.max(metrics.minGlobalOffset, Math.min(metrics.maxGlobalOffset, offset));
	}

	// ==================== ACCESSORS ====================

	public Tokens tokens() {
		return tokens;
	}

	public Metrics metrics() {
		return metrics;
	}

	public Color getBackgroundColor() {
		return tokens.backgroundColor();
	}

	public Color getBorderColor() {
		return tokens.borderColor();
	}

	public Color getFontColor() {
		return tokens.fontColor();
	}

	public Color getNeutralColor() {
		return tokens.neutralColor();
	}

	public Color getBinColor() {
		return tokens.binColor();
	}

	public Color getHighlightColor() {
		return tokens.highlightColor();
	}

	public double[] getHatchingPattern() {
		return tokens.hatchingPattern();
	}

	public Color[] getHatchingColors() {
		return tokens.hatchingColors();
	}

	public boolean isDarkTheme() {
		return tokens.isDarkTheme();
	}

	public Font getBaseFont() {
		return metrics.baseFont();
	}

	public Font getFont(int style, int size) {
		return metrics.font(style, size);
	}

	public String getFontFamily() {
		return metrics.fontFamily();
	}

	public int getBaseFontSize() {
		return metrics.baseFontSize();
	}

	public double getGlobalOffset() {
		return metrics.globalOffset();
	}

	// ==================== OBJECT METHODS ====================

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof VisualizationTheme))
			return false;
		VisualizationTheme other = (VisualizationTheme) obj;
		return Objects.equals(tokens, other.tokens) && Objects.equals(metrics, other.metrics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokens, metrics);
	}

	@Override
	public String toString() {
		return String.format("VisualizationTheme{tokens=%s, metrics=%s}", tokens, metrics);
	}

	public String toDetailedString() {
		return String.format("VisualizationTheme Details:\n  %s\n  %s", tokens, metrics);
	}
}