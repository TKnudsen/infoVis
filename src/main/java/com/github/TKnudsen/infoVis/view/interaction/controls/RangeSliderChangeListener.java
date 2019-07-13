package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.jidesoft.swing.RangeSlider;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class RangeSliderChangeListener implements ChangeListener, MouseInputListener, ISelfDescription {

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println(this.getName() + ": mouseClicked(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println(this.getName() + ": mousePressed(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println(this.getName() + ": mouseReleased(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		System.out.println(this.getName() + ": mouseEntered(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		System.out.println(this.getName() + ": mouseExited(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println(this.getName() + ": mouseDragged(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		System.out.println(this.getName() + ": mouseMoved(), Event: " + e);

		printModel(e.getSource());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		System.out.println(this.getName() + ": stateChanged(), Event: " + e);

		printModel(e.getSource());
	}

	private void printModel(Object source) {
		if (source instanceof RangeSlider) {
			RangeSlider rangeSlider = (RangeSlider) source;
			BoundedRangeModel model = rangeSlider.getModel();
			System.out.println("Model: " + model);
		}
	}

	@Override
	public String getName() {
		return "RangeSliderChangeListener";
	}

	@Override
	public String getDescription() {
		return getName();
	}

}
