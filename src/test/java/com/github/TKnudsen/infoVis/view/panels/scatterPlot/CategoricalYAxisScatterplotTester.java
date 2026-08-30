package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.CategoricalYAxisScatterplotPanel;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

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
 * Minimal demo for {@link CategoricalYAxisScatterplotPanel}: two synthetic
 * classes plotted against a numerical x axis and a categorical y axis.
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.00
 */
public class CategoricalYAxisScatterplotTester {

    public static void main(String[] args) {
        // create data
        List<Entry<Double, String>> points = new ArrayList<>();
        int count = 20;

        for (int i = 0; i < count; i++)
            points.add(new AbstractMap.SimpleEntry<>(Math.random() * 30000000, "Class1"));

        for (int i = 0; i < count; i++)
            points.add(new AbstractMap.SimpleEntry<>(Math.random() * 30000000, "Class2"));

        Function<Entry<Double, String>, Paint> colorMapping = entry -> "Class1".equals(entry.getValue())
                ? Color.BLUE
                : Color.RED;

        CategoricalYAxisScatterplotPanel<Entry<Double, String>> panel = new CategoricalYAxisScatterplotPanel<>(points,
                colorMapping, Entry::getKey, Entry::getValue);

        // SELECTION MODEL
        SelectionModel<Entry<Double, String>> selectionModel = SelectionModels.create();

        // SELECTION HANDLER (click + rectangle, left mouse button)
        SelectionHandler<Entry<Double, String>> selectionHandler = new SelectionHandler<>(selectionModel);
        selectionHandler.attachTo(panel);
        selectionHandler.setClickSelection(panel);
        selectionHandler.setRectangleSelection(panel);

        panel.addChartPainter(new ChartPainter() {
            @Override
            public void draw(Graphics2D g2) {
                selectionHandler.draw(g2);
            }
        });

        panel.setSelectedFunction(t -> selectionHandler.getSelectionModel().isSelected(t));

        // LASSO SELECTION (RIGHT MOUSE BUTTON)
        LassoSelectionHandler<Entry<Double, String>> lassoSelectionHandler = new LassoSelectionHandler<>(
                selectionModel, MouseButton.RIGHT);
        lassoSelectionHandler.attachTo(panel);
        lassoSelectionHandler.setShapeSelection(panel);

        panel.addChartPainter(new ChartPainter() {
            @Override
            public void draw(Graphics2D g2) {
                lassoSelectionHandler.draw(g2);
            }
        });

        selectionModel.addSelectionListener(new LoggingSelectionListener<>());

        // ZOOM (mouse wheel to zoom, double-click to reset) -- no pan: it would
        // collide with rectangle selection's left-mouse drag
        ScatterPlots.addZoomInteraction(panel);

        SVGFrameTools.dropSVGFrame(panel, "CategoricalYAxisScatterplotPainter Test Frame", 500, 500);
    }
}
