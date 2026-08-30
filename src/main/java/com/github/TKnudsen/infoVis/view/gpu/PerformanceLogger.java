package com.github.TKnudsen.infoVis.view.gpu;

import java.util.logging.Logger;

/**
 * <p>
 * Performance logger for tracking rendering metrics across all painters.
 * Monitors frame times, FPS, draw calls, and vertex counts to help identify
 * performance bottlenecks and compare CPU vs GPU rendering performance.
 * 
 * Usage example:
 * 
 * <pre>
 * PerformanceLogger logger = new PerformanceLogger("ScatterPlot");
 * logger.enable();
 * 
 * // In rendering loop:
 * logger.logFrameStart();
 * // ... rendering code ...
 * logger.logFrameEnd(vertexCount, drawCalls, "GPU");
 * 
 * // Periodic statistics:
 * logger.logStatistics();
 * </pre>
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class PerformanceLogger {

	private static final Logger LOGGER = Logger.getLogger(PerformanceLogger.class.getName());

	// ==================== CONFIGURATION ====================

	/** How often to automatically log statistics (in frames) */
	private int logInterval = 60;

	/** Name/identifier for this logger (e.g., "ScatterPlot", "BarChart") */
	private final String name;

	// ==================== STATE ====================

	private boolean enabled = false;

	// ==================== TIMING ====================

	private long lastFrameStartTime;
	private long frameCount;
	private long totalRenderTime;
	private long minRenderTime = Long.MAX_VALUE;
	private long maxRenderTime = 0;

	// ==================== METRICS ====================

	private int lastVertexCount;
	private int lastDrawCalls;
	private String lastRenderMode;

	// Accumulated metrics over the logging interval
	private long totalVertices;
	private long totalDrawCalls;

	// ==================== CONSTRUCTOR ====================

	/**
	 * Create a performance logger with a default name.
	 */
	public PerformanceLogger() {
		this("Painter");
	}

	/**
	 * Create a performance logger with a specific name.
	 * 
	 * @param name identifier for this logger (e.g., "ScatterPlot",
	 *             "ParallelCoordinates")
	 */
	public PerformanceLogger(String name) {
		this.name = name != null ? name : "Painter";
	}

	// ==================== ENABLE/DISABLE ====================

	/**
	 * Enable performance logging and reset all statistics.
	 */
	public void enable() {
		this.enabled = true;
		reset();
		LOGGER.info(String.format("[%s] Performance logging enabled", name));
	}

	/**
	 * Disable performance logging.
	 */
	public void disable() {
		if (enabled) {
			logStatistics(); // Log final stats before disabling
		}
		this.enabled = false;
		LOGGER.info(String.format("[%s] Performance logging disabled", name));
	}

	/**
	 * Check if performance logging is currently enabled.
	 * 
	 * @return true if logging is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	// ==================== RESET ====================

	/**
	 * Reset all accumulated statistics.
	 */
	public void reset() {
		frameCount = 0;
		totalRenderTime = 0;
		minRenderTime = Long.MAX_VALUE;
		maxRenderTime = 0;
		totalVertices = 0;
		totalDrawCalls = 0;

		lastVertexCount = 0;
		lastDrawCalls = 0;
		lastRenderMode = "UNKNOWN";
	}

	// ==================== FRAME LOGGING ====================

	/**
	 * Mark the start of a frame. Call this at the beginning of your draw() method.
	 */
	public void logFrameStart() {
		if (!enabled)
			return;
		lastFrameStartTime = System.nanoTime();
	}

	/**
	 * Mark the end of a frame with rendering metrics. Call this at the end of your
	 * draw() method.
	 * 
	 * @param vertexCount number of vertices/points rendered this frame
	 * @param drawCalls   number of draw calls made this frame
	 * @param renderMode  rendering mode used (e.g., "CPU", "GPU", "AUTO")
	 */
	public void logFrameEnd(int vertexCount, int drawCalls, String renderMode) {
		if (!enabled)
			return;

		long frameTime = System.nanoTime() - lastFrameStartTime;
		frameCount++;
		totalRenderTime += frameTime;
		minRenderTime = Math.min(minRenderTime, frameTime);
		maxRenderTime = Math.max(maxRenderTime, frameTime);

		lastVertexCount = vertexCount;
		lastDrawCalls = drawCalls;
		lastRenderMode = renderMode != null ? renderMode : "UNKNOWN";

		totalVertices += vertexCount;
		totalDrawCalls += drawCalls;

		// Auto-log at intervals
		if (frameCount % logInterval == 0) {
			logStatistics();
		}
	}

	/**
	 * Convenience overload for enum-based render modes.
	 */
	public void logFrameEnd(int vertexCount, int drawCalls, Enum<?> renderMode) {
		logFrameEnd(vertexCount, drawCalls, renderMode != null ? renderMode.name() : "UNKNOWN");
	}

	// ==================== STATISTICS ====================

	/**
	 * Log current performance statistics. This is called automatically every
	 * logInterval frames when enabled.
	 */
	public void logStatistics() {
		if (!enabled || frameCount == 0)
			return;

		double avgMs = (totalRenderTime / (double) frameCount) / 1_000_000.0;
		double minMs = minRenderTime / 1_000_000.0;
		double maxMs = maxRenderTime / 1_000_000.0;
		double fps = 1000.0 / avgMs;

		long avgVerticesPerFrame = totalVertices / frameCount;
		long avgDrawCallsPerFrame = totalDrawCalls / frameCount;

		String message = String.format(
				"[%s/%s] Frames: %d | Avg: %.2fms (%.1f FPS) | Min: %.2fms | Max: %.2fms | "
						+ "Vertices/frame: %d | DrawCalls/frame: %d | Last: %d vertices, %d calls",
				name, lastRenderMode, frameCount, avgMs, fps, minMs, maxMs, avgVerticesPerFrame, avgDrawCallsPerFrame,
				lastVertexCount, lastDrawCalls);

		LOGGER.info(message);
	}

	/**
	 * Get a detailed statistics report as a formatted string.
	 * 
	 * @return multi-line statistics report
	 */
	public String getStatisticsReport() {
		if (frameCount == 0) {
			return String.format("[%s] No frames recorded yet", name);
		}

		double avgMs = (totalRenderTime / (double) frameCount) / 1_000_000.0;
		double minMs = minRenderTime / 1_000_000.0;
		double maxMs = maxRenderTime / 1_000_000.0;
		double fps = 1000.0 / avgMs;

		long avgVerticesPerFrame = totalVertices / frameCount;
		long avgDrawCallsPerFrame = totalDrawCalls / frameCount;

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("=== Performance Statistics: %s ===\n", name));
		sb.append(String.format("Render Mode: %s\n", lastRenderMode));
		sb.append(String.format("Frames Recorded: %d\n", frameCount));
		sb.append(String.format("Average Frame Time: %.2fms (%.1f FPS)\n", avgMs, fps));
		sb.append(String.format("Min Frame Time: %.2fms (%.1f FPS)\n", minMs, 1000.0 / minMs));
		sb.append(String.format("Max Frame Time: %.2fms (%.1f FPS)\n", maxMs, 1000.0 / maxMs));
		sb.append(String.format("Total Vertices Rendered: %d\n", totalVertices));
		sb.append(String.format("Avg Vertices/Frame: %d\n", avgVerticesPerFrame));
		sb.append(String.format("Total Draw Calls: %d\n", totalDrawCalls));
		sb.append(String.format("Avg Draw Calls/Frame: %d\n", avgDrawCallsPerFrame));
		sb.append("================================");

		return sb.toString();
	}

	// ==================== GETTERS ====================

	/**
	 * Get the name/identifier of this logger.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the number of frames recorded since last reset.
	 */
	public long getFrameCount() {
		return frameCount;
	}

	/**
	 * Get average frame time in milliseconds.
	 */
	public double getAverageFrameTimeMs() {
		if (frameCount == 0)
			return 0.0;
		return (totalRenderTime / (double) frameCount) / 1_000_000.0;
	}

	/**
	 * Get average frames per second.
	 */
	public double getAverageFPS() {
		double avgMs = getAverageFrameTimeMs();
		return avgMs > 0 ? 1000.0 / avgMs : 0.0;
	}

	/**
	 * Get minimum frame time in milliseconds.
	 */
	public double getMinFrameTimeMs() {
		if (minRenderTime == Long.MAX_VALUE)
			return 0.0;
		return minRenderTime / 1_000_000.0;
	}

	/**
	 * Get maximum frame time in milliseconds.
	 */
	public double getMaxFrameTimeMs() {
		if (maxRenderTime == 0)
			return 0.0;
		return maxRenderTime / 1_000_000.0;
	}

	/**
	 * Get average number of vertices rendered per frame.
	 */
	public long getAverageVerticesPerFrame() {
		if (frameCount == 0)
			return 0;
		return totalVertices / frameCount;
	}

	/**
	 * Get average number of draw calls per frame.
	 */
	public long getAverageDrawCallsPerFrame() {
		if (frameCount == 0)
			return 0;
		return totalDrawCalls / frameCount;
	}

	/**
	 * Get the last recorded render mode.
	 */
	public String getLastRenderMode() {
		return lastRenderMode;
	}

	/**
	 * Get the last recorded vertex count.
	 */
	public int getLastVertexCount() {
		return lastVertexCount;
	}

	/**
	 * Get the last recorded draw call count.
	 */
	public int getLastDrawCalls() {
		return lastDrawCalls;
	}

	// ==================== CONFIGURATION ====================

	/**
	 * Set how often to automatically log statistics (in frames). Default is 60
	 * frames.
	 * 
	 * @param interval number of frames between automatic logs (must be > 0)
	 */
	public void setLogInterval(int interval) {
		if (interval <= 0) {
			throw new IllegalArgumentException("Log interval must be positive");
		}
		this.logInterval = interval;
	}

	/**
	 * Get the current log interval.
	 */
	public int getLogInterval() {
		return logInterval;
	}

	// ==================== COMPARISON ====================

	/**
	 * Compare performance with another logger and log the results. Useful for A/B
	 * testing CPU vs GPU rendering.
	 * 
	 * @param other another performance logger to compare with
	 */
	public void compareWith(PerformanceLogger other) {
		if (other == null)
			return;

		if (this.frameCount == 0 || other.frameCount == 0) {
			LOGGER.warning("Cannot compare - one or both loggers have no data");
			return;
		}

		double thisAvg = this.getAverageFrameTimeMs();
		double otherAvg = other.getAverageFrameTimeMs();
		double speedup = otherAvg / thisAvg;

		String message = String.format(
				"Performance Comparison: %s (%.2fms, %.1f FPS) vs %s (%.2fms, %.1f FPS) | " + "Speedup: %.2fx %s",
				this.name, thisAvg, this.getAverageFPS(), other.name, otherAvg, other.getAverageFPS(), speedup,
				speedup > 1.0 ? "faster" : "slower");

		LOGGER.info(message);
	}

	// ==================== UTILITY ====================

	@Override
	public String toString() {
		return String.format("PerformanceLogger[%s, enabled=%s, frames=%d, avgFPS=%.1f]", name, enabled, frameCount,
				getAverageFPS());
	}
}