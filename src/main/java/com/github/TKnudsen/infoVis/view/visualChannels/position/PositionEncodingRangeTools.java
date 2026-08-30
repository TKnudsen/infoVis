package com.github.TKnudsen.infoVis.view.visualChannels.position;

/**
 * <p>
 * InfoVis
 * </p>
 *
 * <p>
 * Copyright: (c) 2018-2026 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 *
 * <p>
 * Pure world-value-range arithmetic shared by every {@code IZooming}/
 * {@code IPanning} implementation: shrinking/growing or shifting a
 * [min, max] window, clamped to a global data range. Deliberately independent
 * of {@link IPositionEncodingFunction} -- callers use a function only to read
 * the world value under the cursor (via
 * {@link IPositionEncodingFunction#inverseMapping(Double)}), then apply the
 * range returned here through their own axis-painter setters (e.g.
 * {@code setXAxisMinValue}/{@code setXAxisMaxValue}), never by writing the
 * function directly -- axis painters cache tick/marker positions separately
 * from the function and only recompute them through those setters.
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.00
 */
public final class PositionEncodingRangeTools {

    private static final double ZOOM_STEP = 0.15;

    private PositionEncodingRangeTools() {
    }

    /**
     * Shrinks/grows {@code [currentMin, currentMax]} by one {@code zoomCount}
     * notch (15% of the current span per notch), clamped to
     * {@code [globalMin, globalMax]}, and re-centers the result so
     * {@code worldValueUnderCursor} stays at the same relative position within
     * the window it held before the zoom -- i.e. the point under the cursor
     * does not visually drift.
     *
     * @return the new {@code [min, max]}, or {@code null} if the change would
     *         have no effect (already at the global bounds, or the requested
     *         step rounds to zero)
     */
    public static double[] computeZoomedRange(double currentMin, double currentMax, double globalMin,
            double globalMax, double worldValueUnderCursor, int zoomCount) {
        double interval = Math.abs(currentMax - currentMin);
        if (interval <= 0 || !Double.isFinite(interval))
            return null;

        double delta = interval * (zoomCount * ZOOM_STEP);
        if (delta == 0)
            return null;

        double newMin = currentMin + delta;
        double newMax = currentMax - delta;

        newMin = Math.max(newMin, globalMin);
        newMax = Math.min(newMax, globalMax);

        if (newMin >= newMax)
            return null;

        // re-center: worldValueUnderCursor keeps the same relative position within
        // [newMin, newMax] that it held within [currentMin, currentMax]
        double relative = (worldValueUnderCursor - currentMin) / interval;
        double newWorldValueUnderCursor = newMin + (newMax - newMin) * relative;
        double recenter = worldValueUnderCursor - newWorldValueUnderCursor;

        newMin += recenter;
        newMax += recenter;

        newMin = Math.max(newMin, globalMin);
        newMax = Math.min(newMax, globalMax);

        if (newMin >= newMax)
            return null;

        return new double[] { newMin, newMax };
    }

    /**
     * Shifts {@code [currentMin, currentMax]} by {@code worldDelta} (the world
     * value change corresponding to the drag distance, positive when the view
     * should reveal smaller world values), clamping each bound independently to
     * {@code [globalMin, globalMax]} -- matching how far a drag can push the
     * window against the data's edge.
     *
     * @return the new {@code [min, max]}, or {@code null} if {@code worldDelta}
     *         is zero
     */
    public static double[] computePannedRange(double currentMin, double currentMax, double globalMin,
            double globalMax, double worldDelta) {
        if (worldDelta == 0)
            return null;

        double newMin = currentMin - worldDelta;
        double newMax = currentMax - worldDelta;

        newMin = Math.max(newMin, globalMin);
        newMax = Math.min(newMax, globalMax);

        if (newMin >= newMax)
            return null;

        return new double[] { newMin, newMax };
    }
}
