package com.github.TKnudsen.infoVis.view.gpu;

/**
 * <p>
 * Selects between CPU-based Java2D rendering, GPU-accelerated OpenGL
 * rendering, or automatic selection based on data size and availability.
 * </p>
 *
 * @version 1.0
 */
public enum RenderMode {
	/** Traditional CPU-based rendering using Java2D Graphics2D */
	CPU,
	/** GPU-accelerated rendering using OpenGL */
	GPU,
	/** Automatic selection based on data size and availability */
	AUTO
}