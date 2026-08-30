package com.github.TKnudsen.infoVis.view.interaction.controls.slider;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * Abstract base class for panels controlling numerical values or parameters
 * via sliders.
 * 
 * Provides common functionality for slider-based parameter controls including:
 * <ul>
 * <li>Slider creation and configuration</li>
 * <li>Value change detection and notification</li>
 * <li>Value scaling and validation</li>
 * <li>Programmatic value setting</li>
 * </ul>
 * 
 * Subclasses must implement:
 * <ul>
 * <li>{@link #getTitle()} - The panel's title</li>
 * <li>{@link #getMinSliderValue()} - Slider minimum (typically 0)</li>
 * <li>{@link #getMaxSliderValue()} - Slider maximum (typically 100)</li>
 * <li>{@link #getDefaultSliderValue()} - Initial slider value</li>
 * <li>{@link #sliderValueToParameter(int)} - Convert slider int to parameter
 * double</li>
 * <li>{@link #parameterToSliderValue(double)} - Convert parameter double to
 * slider int</li>
 * </ul>
 * </p>
 *
 * @version 1.01
 * @since 2026
 */
public abstract class SliderControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** The slider control */
	protected JSlider slider;

	/** Consumer callback invoked when parameter value changes */
	private final Consumer<Double> parameterConsumer;

	/** Last parameter value to detect changes */
	private double lastParameterValue;

	/**
	 * Creates a new parameter control panel.
	 * 
	 * @param parameterConsumer callback invoked when the parameter value changes
	 * @throws IllegalArgumentException if parameterConsumer is null
	 */
	public SliderControlPanel(Consumer<Double> parameterConsumer) {
		super(new BorderLayout());

		if (parameterConsumer == null) {
			throw new IllegalArgumentException("parameterConsumer cannot be null");
		}

		this.parameterConsumer = parameterConsumer;
		this.lastParameterValue = sliderValueToParameter(getDefaultSliderValue());

		initialize();
	}

	/**
	 * Initializes the panel UI components.
	 */
	private void initialize() {
		// Set title border
		TitledBorder titledBorder = new TitledBorder(getTitle());
		setBorder(titledBorder);

		// Create and add control panel
		JPanel controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates the control panel containing the slider.
	 * 
	 * @return the control panel
	 */
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new GridLayout(1, 0));

		// Create slider with subclass-defined parameters
		slider = new JSlider(getMinSliderValue(), getMaxSliderValue(), getDefaultSliderValue());

		// Add change listener
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				handleSliderChange();
			}
		});

		controlPanel.add(slider);
		return controlPanel;
	}

	/**
	 * Handles slider value changes and notifies consumer if value actually changed.
	 */
	private void handleSliderChange() {
		double newValue = sliderValueToParameter(slider.getValue());

		// Only notify if value actually changed (avoid redundant updates)
		if (lastParameterValue != newValue) {
			parameterConsumer.accept(newValue);
			lastParameterValue = newValue;
		}
	}

	// ==================== PUBLIC API ====================

	/**
	 * Sets the slider orientation.
	 * 
	 * @param orientation {@link JSlider#HORIZONTAL} or {@link JSlider#VERTICAL}
	 */
	public void setOrientation(int orientation) {
		if (slider != null) {
			slider.setOrientation(orientation);
		}
	}

	/**
	 * Sets a new parameter value programmatically. The value will be validated and
	 * clamped to the valid range.
	 * 
	 * @param parameterValue the new parameter value
	 */
	public void setParameterValue(double parameterValue) {
		double validatedValue = validateParameterValue(parameterValue);
		int sliderValue = parameterToSliderValue(validatedValue);

		if (slider != null) {
			slider.setValue(sliderValue);
		}
	}

	/**
	 * Gets the current parameter value.
	 * 
	 * @return the current parameter value
	 */
	public double getParameterValue() {
		if (slider != null) {
			return sliderValueToParameter(slider.getValue());
		}
		return lastParameterValue;
	}

	/**
	 * Validates and clamps a parameter value to the valid range.
	 * 
	 * @param value the value to validate
	 * @return the validated value
	 */
	protected double validateParameterValue(double value) {
		double min = sliderValueToParameter(getMinSliderValue());
		double max = sliderValueToParameter(getMaxSliderValue());
		return Math.max(min, Math.min(max, value));
	}

	// ================ ABSTRACT METHODS (SUBCLASS MUST IMPLEMENT) ================

	/**
	 * Returns the title displayed on the panel border.
	 * 
	 * @return the panel title
	 */
	protected abstract String getTitle();

	/**
	 * Returns the minimum value for the slider (typically 0).
	 * 
	 * @return the minimum slider value
	 */
	protected abstract int getMinSliderValue();

	/**
	 * Returns the maximum value for the slider (typically 100).
	 * 
	 * @return the maximum slider value
	 */
	protected abstract int getMaxSliderValue();

	/**
	 * Returns the default initial value for the slider.
	 * 
	 * @return the default slider value
	 */
	protected abstract int getDefaultSliderValue();

	/**
	 * Converts a slider integer value to the parameter's double value.
	 * 
	 * <p>
	 * Example: For a parameter range of 0.0-2.0 with slider range 0-100, this might
	 * return: sliderValue / 50.0
	 * </p>
	 * 
	 * @param sliderValue the slider value (0-100)
	 * @return the parameter value
	 */
	protected abstract double sliderValueToParameter(int sliderValue);

	/**
	 * Converts a parameter double value to a slider integer value.
	 * 
	 * <p>
	 * Example: For a parameter range of 0.0-2.0 with slider range 0-100, this might
	 * return: (int)(parameterValue * 50)
	 * </p>
	 * 
	 * @param parameterValue the parameter value
	 * @return the slider value (0-100)
	 */
	protected abstract int parameterToSliderValue(double parameterValue);
}