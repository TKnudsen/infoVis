# infoVis

Information Visualization library for the visual-interactive analysis of data - with a selection of visual encodings, chart types and interaction designs.

## Architecture

The library is organized under `com.github.TKnudsen.infoVis`, split roughly into a rendering layer and a data/window layer:

- **`view/painters/`** - stateless, `Graphics2D`-based renderers for individual chart types (scatterplot, barchart, boxplot, piechart, donutchart, parallelCoordinates, trajectory, radial, number, string, grid, axis, ...) plus shared drawing primitives.
- **`view/panels/`** - interactive Swing `JPanel` wrappers around the painters above, adding selection, zoom/pan, tooltips and axis handling on top of the pure rendering logic. Mostly one subpackage per chart type, mirroring `view/painters/`; a few panel-level distinctions don't have a painter-side counterpart, e.g. `barchart` (categorical bar charts) and `histogram` (numeric distributions) are separate panel packages sharing the same `painters/barchart` rendering classes, with their common panel infrastructure factored into `bins`.
- **`view/visualChannels/`** - encoding functions that map data to visual variables: `color/` (including the `colormaps/` collection - quantitative, qualitative and bivariate color maps), `position/` and `size/`.
- **`view/interaction/`** - selection models, mouse/lasso selection handlers, sliders and other interactive controls used by the panels, including `labeling/` - a human-in-the-loop workflow for mapping raw category strings to a controlled alphabet (with persistence and optional ChatGPT-assisted suggestions).
- **`view/viewTransformation/`** - dimensionality reduction and coordinate transformation pipelines feeding position-encoded views (e.g. scatterplots).
- **`view/table/`**, **`view/gpu/`** - table-based views, and GPU-accelerated (JOGL) variants of selected panels for large data volumes.
- **`view/ui/`**, **`view/tools/`**, **`view/chartLayouts/`**, **`view/frames/`** - theming, shared utilities, multi-chart layout helpers, and standalone frame/window utilities (including SVG export).
- **`view/views/`** - higher-level, composite views built from multiple panels.
- **`data/`** - supporting data model classes.

## Installation

Maven:

```xml
<dependency>
    <groupId>com.github.tknudsen</groupId>
    <artifactId>info-vis</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick start

Building a scatterplot panel with 200 random points, colored by a quantitative colormap:

```java
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RainbowColorMap;

public class QuickStart {
    public static void main(String[] args) {
        List<Double[]> points = new ArrayList<>();
        List<Paint> colors = new ArrayList<>();

        AbstractColorMap1D colorMap = (AbstractColorMap1D) RainbowColorMap.getInstance();

        for (int i = 0; i < 200; i++) {
            double x = Math.random();
            double y = Math.random();
            points.add(new Double[] { x, y });
            colors.add(colorMap.getColor((float) x));
        }

        // ScatterPlot is a JPanel; ScatterPlots also wires up click/rectangle/lasso selection
        ScatterPlot<Double[]> scatterPlot = ScatterPlots.createForDoubles(points, colors);

        JFrame frame = new JFrame("infoVis ScatterPlot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scatterPlot);
        frame.setSize(600, 600);
        frame.setVisible(true);
    }
}
```

`ScatterPlots.createForDoubles(...)` is the simplest of several factory methods in `ScatterPlots` - `create(...)` exposes the general form (custom position/color mapping functions, an explicit `SelectionModel`), and `createForDoublesGPU(...)`/`createGPU(...)` build the JOGL-backed GPU variant for larger point counts.
