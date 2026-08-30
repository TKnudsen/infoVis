package com.github.TKnudsen.infoVis.view.painters.texture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * <p>
 * Utility class for generating hatching patterns, hatching colors, and
 * texture-related helpers. Intended to keep {@code VisualizationTheme}
 * manageable by extracting hatching-specific logic.
 * 
 * Notes:
 * <ul>
 * <li>Pure functions: deterministic and side-effect free.</li>
 * <li>Java 8 compatible.</li>
 * <li>Does not cache; caching belongs in painter classes.</li>
 * </ul>
 * </p>
 *
 */
public final class HatchingUtils {

	private HatchingUtils() {
		throw new AssertionError("Utility class");
	}

	// ============================================================
	// Validation / normalization helpers
	// ============================================================

	/**
	 * Validates that colors and widths form a legal hatching definition.
	 *
	 * @throws IllegalArgumentException if invalid
	 */
	public static void validateHatchingDefinition(Color[] colors, double[] widths) {
		if (colors == null || colors.length == 0) {
			throw new IllegalArgumentException("colors must not be null/empty");
		}
		if (widths == null || widths.length == 0) {
			throw new IllegalArgumentException("widths must not be null/empty");
		}
		if (colors.length != widths.length) {
			throw new IllegalArgumentException("colors.length must equal widths.length");
		}
		for (int i = 0; i < widths.length; i++) {
			if (!(widths[i] > 0.0)) {
				throw new IllegalArgumentException("All widths must be > 0. width[" + i + "]=" + widths[i]);
			}
		}
	}

	public static Color[] copyColors(Color[] colors) {
		return colors == null ? null : colors.clone();
	}

	public static double[] copyWidths(double[] widths) {
		return widths == null ? null : widths.clone();
	}

	public static double sum(double[] values) {
		if (values == null) {
			return 0.0;
		}
		double s = 0.0;
		for (double v : values) {
			s += v;
		}
		return s;
	}

	// ============================================================
	// Hashing (for painter-side caching)
	// ============================================================

	/**
	 * Computes a stable cache hash for a hatching tile definition.
	 *
	 * <p>
	 * Intended usage: pass in an already-normalized angleDeg so equivalent angles
	 * hash identically.
	 * </p>
	 */
	public static int computeHatchingHash(Color[] colors, double[] widths, float normalizedAngleDeg,
			Color tileBackground, int targetTilePixels) {

		Objects.requireNonNull(colors, "colors required");
		Objects.requireNonNull(widths, "widths required");

		int result = 17;

		result = 31 * result + Arrays.hashCode(colors);
		result = 31 * result + Arrays.hashCode(widths);

		// Angle: use bits for stability (incl. -0/+0)
		result = 31 * result + Float.floatToIntBits(normalizedAngleDeg);

		// Optional background
		result = 31 * result + (tileBackground == null ? 0 : tileBackground.getRGB());

		// Tile size hint influences produced tile
		result = 31 * result + targetTilePixels;

		return result;
	}

	// ============================================================
	// TexturePaint helpers
	// ============================================================

	/**
	 * Creates a {@link TexturePaint} for the provided tile image with a standard
	 * (0,0,width,height) anchor.
	 */
	public static TexturePaint createTexturePaint(BufferedImage tileImage) {
		Objects.requireNonNull(tileImage, "tileImage required");

		if (tileImage.getWidth() <= 0 || tileImage.getHeight() <= 0) {
			throw new IllegalArgumentException("tileImage must have positive width/height");
		}

		Rectangle2D anchor = new Rectangle2D.Double(0, 0, tileImage.getWidth(), tileImage.getHeight());
		return new TexturePaint(tileImage, anchor);
	}

	/**
	 * Fills a rectangle in a phase-aligned way: the paint pattern origin is aligned
	 * to the rectangle's (x,y) (so adjacent fills line up).
	 *
	 * <p>
	 * This avoids the common "pattern shifts between shapes" artifact when using
	 * {@link TexturePaint}.
	 * </p>
	 */
	public static void fillPhaseAligned(Graphics2D g2, Rectangle2D rect, TexturePaint paint) {
		Objects.requireNonNull(g2, "g2 required");
		Objects.requireNonNull(rect, "rect required");
		Objects.requireNonNull(paint, "paint required");

		AffineTransform oldTx = g2.getTransform();
		try {
			g2.translate(rect.getX(), rect.getY());
			g2.setPaint(paint);
			g2.fill(new Rectangle2D.Double(0, 0, rect.getWidth(), rect.getHeight()));
		} finally {
			g2.setTransform(oldTx);
		}
	}

	// ============================================================
	// Angle normalization
	// ============================================================

	/**
	 * Normalizes an angle in degrees for hatching:
	 * <ul>
	 * <li>maps to (-90..90] using symmetry (because 180-degree repetition is
	 * identical)</li>
	 * <li>snaps very small values to 0 and ~90 to 90 to avoid numeric issues</li>
	 * </ul>
	 */
	public static float normalizeAngleDegrees(float angleDeg) {
		float a = angleDeg % 360f;
		if (a > 180f) {
			a -= 360f;
		}
		if (a < -180f) {
			a += 360f;
		}

		// Keep in (-90..90] by symmetry
		if (a > 90f) {
			a -= 180f;
		}
		if (a < -90f) {
			a += 180f;
		}

		// Snap
		if (Math.abs(a) < 0.0001f) {
			return 0f;
		}
		if (Math.abs(Math.abs(a) - 90f) < 0.0001f) {
			return 90f;
		}
		return a;
	}

	public static boolean isHorizontal(float normalizedAngleDeg) {
		return Math.abs(normalizedAngleDeg) < 0.0001f;
	}

	public static boolean isVertical(float normalizedAngleDeg) {
		return Math.abs(Math.abs(normalizedAngleDeg) - 90f) < 0.0001f;
	}

	// ============================================================
	// Adaptive hatching colors (to be called by VisualizationTheme.Builder)
	// ============================================================

	/**
	 * Generates adaptive hatching colors that blend well with the theme.
	 *
	 * <p>
	 * This is a default generator. It does NOT assume a background "color slot"
	 * inside the hatching; it only returns stripe colors.
	 * </p>
	 *
	 * @param background Background color (for brightness decision)
	 * @param border     Border color (used as blend partner in light themes)
	 * @param font       Font color (currently unused but kept for future contrast
	 *                   policy)
	 * @param n          number of colors to generate (>=2)
	 */
	public static Color[] generateAdaptiveHatchingColors(Color background, Color border, Color font, int n) {
		Objects.requireNonNull(background, "background required");
		Objects.requireNonNull(border, "border required");
		Objects.requireNonNull(font, "font required");

		if (n < 2) {
			throw new IllegalArgumentException("n must be >= 2");
		}

		// Decide light/dark based on WCAG luminance
		boolean isLightTheme = ColorTools.calculateLuminance(background) > 0.5;

		final Color blendTarget;
		final double tMin;
		final double tMax;

		if (isLightTheme) {
			// subtle greys close to background toward border
			blendTarget = border;
			tMin = 0.12;
			tMax = 0.45;
		} else {
			// dark theme: blend toward a light gray for visibility
			blendTarget = new Color(200, 200, 200);
			tMin = 0.20;
			tMax = 0.55;
		}

		Color[] out = new Color[n];
		if (n == 2) {
			out[0] = ColorTools.mergeColors(background, blendTarget, tMin);
			out[1] = ColorTools.mergeColors(background, blendTarget, tMax);
			return out;
		}

		// Evenly sample [tMin..tMax]
		double step = (tMax - tMin) / (n - 1);
		for (int i = 0; i < n; i++) {
			double t = tMin + i * step;
			out[i] = ColorTools.mergeColors(background, blendTarget, t);
		}
		return out;
	}

	/**
	 * Convenience: returns a safe default if user provides null/empty.
	 */
	public static Color[] ensureHatchingColors(Color[] colors, Color background, Color border, Color font, int n) {
		if (colors != null && colors.length >= 2) {
			return colors.clone();
		}
		return generateAdaptiveHatchingColors(background, border, font, n);
	}

	/**
	 * Convenience: returns a safe default if user provides null/empty.
	 */
	public static double[] ensureHatchingPattern(double[] pattern) {
		if (pattern != null && pattern.length > 0) {
			return pattern.clone();
		}
		return new double[] { 3, 2 };
	}

	// ============================================================
	// TexturePaint + BufferedImage creation (convenience)
	// ============================================================

	/**
	 * Convenience factory: creates a tile image using the stroke-based generator
	 * and returns a {@link TexturePaint}.
	 *
	 * <p>
	 * If you want "original working" behavior for diagonal hatching, prefer
	 * {@link #createTexture(Color[], double[], float, int)}.
	 * </p>
	 */
	public static TexturePaint createHatchingTexturePaint(Color[] colors, double[] widths, float angleDeg,
			Color background, int targetTilePixels) {

		Objects.requireNonNull(colors, "colors required");
		Objects.requireNonNull(widths, "widths required");

		BufferedImage tile = createHatchingTileStrokeBased(colors, widths, angleDeg, background, targetTilePixels);
		return createTexturePaint(tile);
	}

	// ============================================================
	// ORIGINAL TEXTURE CREATION (kept compatible)
	// ============================================================

	/**
	 * Creates a hatching texture tile using the original (working) line-by-line
	 * algorithm.
	 *
	 * <p>
	 * IMPORTANT: This method intentionally uses {@link BufferedImage#TYPE_INT_RGB}
	 * to preserve original behavior. The painter is expected to draw tiles such
	 * that no unpainted background remains visible. If you observe black pixels, it
	 * usually means some pixels of the tile were never painted.
	 * </p>
	 */
	public static BufferedImage createTexture(Color[] colors, double[] colorwidths, float angle, int sizeMultiple) {

		if (colorwidths == null || colorwidths.length == 0) {
			throw new IllegalArgumentException("colorwidths must not be null/empty");
		}
		if (colors == null || colors.length == 0) {
			throw new IllegalArgumentException("colors must not be null/empty");
		}
		if (colors.length != colorwidths.length) {
			throw new IllegalArgumentException("colors.length must equal colorwidths.length");
		}

		// --- compute base period width ---
		int w = 0;
		for (int i = 0; i < colorwidths.length; i++) {
			w += (int) Math.round(colorwidths[i]);
		}
		w = Math.max(1, w);

		// normalize a bit for stability
		double rad = (angle * Math.PI) / 180.0;
		double tan = Math.tan(rad);

		// detect degenerate angles
		final double EPS = 1e-6;
		boolean horizontal = Math.abs(tan) < EPS; // angle ~ 0 degrees
		boolean vertical = Math.abs(Math.cos(rad)) < EPS; // angle ~ 90 degrees (cos ~ 0)

		// --- texture dimensions ---
		final int tileW;
		final int tileH;

		if (horizontal) {
			tileW = w * sizeMultiple;
			tileH = w * sizeMultiple;
		} else if (vertical) {
			tileW = w * sizeMultiple;
			tileH = w * sizeMultiple;
		} else {
			int h = (int) Math.max(1, Math.abs(MathFunctions.round(Math.tan(rad) * w, 0)));
			tileW = w * sizeMultiple;
			tileH = h * sizeMultiple;
		}

		Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, tileW, tileH);

		BufferedImage bufferedImage = new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();

		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// ------------------------------------------------------------
			// CASE 1: Horizontal stripes (angle ~ 0)
			// ------------------------------------------------------------
			if (horizontal) {
				double y = 0.0;
				int idx = 0;
				while (y < rectangle.getMaxY()) {
					Color col = colors[idx % colors.length];
					double bandH = colorwidths[idx % colorwidths.length] * sizeMultiple;

					g2.setColor(col);
					g2.fill(new Rectangle2D.Double(0, y, rectangle.getWidth(), bandH));

					y += bandH;
					idx++;
				}
				return bufferedImage;
			}

			// ------------------------------------------------------------
			// CASE 2: Vertical stripes (angle ~ 90)
			// ------------------------------------------------------------
			if (vertical) {
				double x = 0.0;
				int idx = 0;
				while (x < rectangle.getMaxX()) {
					Color col = colors[idx % colors.length];
					double bandW = colorwidths[idx % colorwidths.length] * sizeMultiple;

					g2.setColor(col);
					g2.fill(new Rectangle2D.Double(x, 0, bandW, rectangle.getHeight()));

					x += bandW;
					idx++;
				}
				return bufferedImage;
			}

			// ------------------------------------------------------------
			// CASE 3: Diagonal stripes (original line stepping)
			// ------------------------------------------------------------

			double deltaX = rectangle.getHeight() / tan;
			double xStart = rectangle.getMinX() - Math.abs(deltaX);

			if (!Double.isFinite(deltaX)) {
				return bufferedImage;
			}
			if (!Double.isFinite(xStart)) {
				return bufferedImage;
			}

			double yStart = rectangle.getMinY();
			double yEnd = rectangle.getMaxY();
			if (angle > 0) {
				yStart = rectangle.getMaxY();
				yEnd = rectangle.getMinY();
			}

			double innerPixelMultiple = 4.0;
			double step = 1.0 / innerPixelMultiple;

			while (xStart < rectangle.getMaxX()) {
				for (int i = 0; i < colors.length; i++) {
					if (i > colorwidths.length - 1) {
						continue;
					}

					g2.setColor(colors[i]);

					for (double j = 0; j < colorwidths[i]; j += step) {

						if (xStart > rectangle.getMaxX()) {
							break;
						}

						double startXLocal = xStart;
						double endXLocal = xStart + Math.abs(deltaX);
						double startYLocal = yStart;
						double endYLocal = yEnd;

						// cut left overlap
						if (startXLocal < rectangle.getMinX()) {
							double overlap = rectangle.getMinX() - startXLocal;
							double haa = (overlap / deltaX) * rectangle.getHeight();
							startYLocal -= haa;
							startXLocal = rectangle.getMinX();
						}

						// cut right overlap
						if (endXLocal > rectangle.getMaxX()) {
							double overlap = endXLocal - rectangle.getMaxX();
							double haa = (overlap / deltaX) * rectangle.getHeight();
							endYLocal += haa;
							endXLocal = rectangle.getMaxX();
						}

						g2.draw(new Line2D.Double(startXLocal, startYLocal, endXLocal, endYLocal));

						xStart += step;
					}
				}
			}

			return bufferedImage;

		} finally {
			g2.dispose();
		}
	}

	// ============================================================
	// Alternative tile generation (stroke-based diagonal)
	// ============================================================

	/**
	 * Creates a tile image for hatching using a stroke-thickness approach for
	 * diagonal stripes.
	 *
	 * <p>
	 * Uses TYPE_INT_ARGB. If background is null, tile is transparent.
	 * </p>
	 */
	public static BufferedImage createHatchingTileStrokeBased(Color[] colors, double[] widths, float angleDeg,
			Color background, int targetTilePixels) {

		Objects.requireNonNull(colors, "colors required");
		Objects.requireNonNull(widths, "widths required");
		validateHatchingDefinition(colors, widths);

		float a = normalizeAngleDegrees(angleDeg);

		final boolean vertical = isVertical(a);
		final boolean horizontal = isHorizontal(a);

		int period = Math.max(1, (int) Math.round(MathFunctions.getSum(widths, true)));

		final int tileW;
		final int tileH;

		if (vertical) {
			tileW = period;
			tileH = Math.max(16, period);
		} else if (horizontal) {
			tileH = period;
			tileW = Math.max(16, period);
		} else {
			int min = MathFunctions.clamp(period * 4, 32, 512);
			int t = MathFunctions.clamp(targetTilePixels, min, 512);
			tileW = t;
			tileH = t;
		}

		BufferedImage img = new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (background != null) {
				g2.setColor(background);
				g2.fillRect(0, 0, tileW, tileH);
			}

			if (vertical) {
				paintVerticalBands(g2, colors, widths, tileW, tileH);
			} else if (horizontal) {
				paintHorizontalBands(g2, colors, widths, tileW, tileH);
			} else {
				paintDiagonalBandsWithStrokes(g2, colors, widths, a, tileW, tileH);
			}
		} finally {
			g2.dispose();
		}
		return img;
	}

	private static void paintVerticalBands(Graphics2D g2, Color[] colors, double[] widths, int w, int h) {
		double x = 0.0;
		for (int i = 0; i < colors.length; i++) {
			g2.setColor(colors[i]);
			double stripeW = widths[i];
			g2.fill(new Rectangle2D.Double(x, 0, stripeW, h));
			x += stripeW;
		}
	}

	private static void paintHorizontalBands(Graphics2D g2, Color[] colors, double[] widths, int w, int h) {
		double y = 0.0;
		for (int i = 0; i < colors.length; i++) {
			g2.setColor(colors[i]);
			double stripeH = widths[i];
			g2.fill(new Rectangle2D.Double(0, y, w, stripeH));
			y += stripeH;
		}
	}

	private static void paintDiagonalBandsWithStrokes(Graphics2D g2, Color[] colors, double[] widths, float angleDeg,
			int tileW, int tileH) {

		double rad = Math.toRadians(angleDeg);
		double dx = Math.cos(rad);
		double dy = Math.sin(rad);

		double nx = -dy;
		double ny = dx;

		double L = Math.hypot(tileW, tileH) * 2.0;
		double period = MathFunctions.getSum(widths, true);
		double maxDist = Math.hypot(tileW, tileH) + period;

		Stroke oldStroke = g2.getStroke();
		try {
			double offset = -maxDist;

			while (offset < maxDist) {
				for (int i = 0; i < colors.length; i++) {
					double bandWidth = widths[i];

					g2.setColor(colors[i]);
					float strokeW = (float) Math.max(1.0, bandWidth);
					g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

					double cx = tileW / 2.0 + nx * offset;
					double cy = tileH / 2.0 + ny * offset;

					g2.draw(new Line2D.Double(cx - dx * L, cy - dy * L, cx + dx * L, cy + dy * L));

					offset += bandWidth;
				}
			}
		} finally {
			g2.setStroke(oldStroke);
		}
	}

}