/*
 * www.javagl.de - SOM - Self-Organizing Maps
 *
 * Copyright (c) 2013-2015 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
//TODO remove this class. it is borrowed from somewhere else.
package com.github.TKnudsen.infoVis.view.interaction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * A connection between two {@link SelectionModel} instances.<br>
 * <br>
 * It will update the selection in each {@link SelectionModel} based on the
 * other, using functions that map between the selections. Depending on the
 * {@link ConnectionMode} that is {@link #setConnectionMode(ConnectionMode)
 * set}, it may forward the selection from the first to the second model, vice
 * versa, or in both directions.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @version 1.02
 * @param <A> The type of the elements in the first {@link SelectionModel}
 * @param <B> The type of the elements in the second {@link SelectionModel}
 */
public final class SelectionModelConnector<A, B> {
	/**
	 * The possible modes for the connection between the {@link SelectionModel}
	 * instances
	 */
	public enum ConnectionMode {
		/**
		 * No connection between the {@link SelectionModel} instances
		 */
		NONE,

		/**
		 * Forward the selections from {@link SelectionModel} A to
		 * {@link SelectionModel} B
		 */
		A_TO_B,

		/**
		 * Forward the selections from {@link SelectionModel} B to
		 * {@link SelectionModel} A
		 */
		B_TO_A,

		/**
		 * Forward the selections from {@link SelectionModel} A to
		 * {@link SelectionModel} B, and vice versa
		 */
		BIDIRECTIONAL
	}

	/**
	 * The first {@link SelectionModel}
	 */
	private SelectionModel<A> selectionModelA;

	/**
	 * The second {@link SelectionModel}
	 */
	private SelectionModel<B> selectionModelB;

	/**
	 * The {@link ConnectionMode}
	 */
	private ConnectionMode connectionMode;

	/**
	 * Whether an update from the first {@link SelectionModel} to the second
	 * {@link SelectionModel} is currently in progress
	 */
	private boolean currentlyUpdatingAtoB = false;

	/**
	 * Whether an update from the second {@link SelectionModel} to the first
	 * {@link SelectionModel} is currently in progress
	 */
	private boolean currentlyUpdatingBtoA = false;

	/**
	 * The function that maps a selection from the first {@link SelectionModel} to a
	 * selection for the second {@link SelectionModel}
	 */
	private final Function<? super Set<A>, ? extends Set<B>> mappingAtoB;

	/**
	 * The function that maps a selection from the second {@link SelectionModel} to
	 * a selection for the first {@link SelectionModel}
	 */
	private final Function<? super Set<B>, ? extends Set<A>> mappingBtoA;

	/**
	 * The {@link SelectionListener} for the first {@link SelectionModel}
	 */
	private final SelectionListener<A> selectionListenerA = new SelectionListener<A>() {
		@Override
		public void selectionChanged(SelectionEvent<A> selectionEvent) {
			if (connectionMode == ConnectionMode.A_TO_B || connectionMode == ConnectionMode.BIDIRECTIONAL) {
				updateAtoB();
			}
		}
	};

	/**
	 * The {@link SelectionListener} for the second {@link SelectionModel}
	 */
	private final SelectionListener<B> selectionListenerB = new SelectionListener<B>() {
		@Override
		public void selectionChanged(SelectionEvent<B> selectionEvent) {
			if (connectionMode == ConnectionMode.B_TO_A || connectionMode == ConnectionMode.BIDIRECTIONAL) {
				updateBtoA();
			}
		}
	};

	/**
	 * Create a new connector for the given {@link SelectionModel} instances. <br>
	 * <br>
	 * The {@link #setConnectionMode(ConnectionMode) connection mode} will be
	 * {@link ConnectionMode#BIDIRECTIONAL}.<br>
	 * <br>
	 * If one of the given functions returns <code>null</code> for an argument, then
	 * this will be treated as the empty set.
	 * 
	 * @param mappingAtoB The mapping from selections in the first
	 *                    {@link SelectionModel} to selections in the second
	 *                    {@link SelectionModel}
	 * @param mappingBtoA The mapping from selections in the second
	 *                    {@link SelectionModel} to selections in the first
	 *                    {@link SelectionModel}
	 * @throws NullPointerException If any of the given functions is
	 *                              <code>null</code>
	 */
	public SelectionModelConnector(Function<? super Set<A>, ? extends Set<B>> mappingAtoB,
			Function<? super Set<B>, ? extends Set<A>> mappingBtoA) {
		this.mappingAtoB = Objects.requireNonNull(mappingAtoB, "The mappingAtoB may not be null");
		this.mappingBtoA = Objects.requireNonNull(mappingBtoA, "The mappingBtoA may not be null");
		this.connectionMode = ConnectionMode.BIDIRECTIONAL;
	}

	/**
	 * Connect the given {@link SelectionModel} instances. These may not be
	 * <code>null</code>.<br>
	 * <br>
	 * This is equivalent to calling
	 * {@link #connect(SelectionModel, SelectionModel, ConnectionMode)
	 * connect(selectionModelA, selectionModelB, ConnectionMode.BIDIRECTIONAL)}
	 * 
	 * @param selectionModelA The first {@link SelectionModel}
	 * @param selectionModelB The second {@link SelectionModel}
	 * @throws NullPointerException If any of the given models is <code>null</code>
	 */
	public void connect(SelectionModel<A> selectionModelA, SelectionModel<B> selectionModelB) {
		connect(selectionModelA, selectionModelB, ConnectionMode.BIDIRECTIONAL);
	}

	/**
	 * Connect the given {@link SelectionModel} instances. These may not be
	 * <code>null</code>.
	 * 
	 * @param selectionModelA The first {@link SelectionModel}
	 * @param selectionModelB The second {@link SelectionModel}
	 * @param connectionMode  The {@link ConnectionMode}. If this is
	 *                        <code>null</code>, then {@link ConnectionMode#NONE}
	 *                        will be used.
	 * @throws NullPointerException If any of the given models is <code>null</code>
	 */
	public void connect(SelectionModel<A> selectionModelA, SelectionModel<B> selectionModelB,
			ConnectionMode connectionMode) {
		disconnect();
		this.selectionModelA = Objects.requireNonNull(selectionModelA, "The selectionModelA may not be null");
		this.selectionModelB = Objects.requireNonNull(selectionModelB, "The selectionModelB may not be null");
		selectionModelA.addSelectionListener(selectionListenerA);
		selectionModelB.addSelectionListener(selectionListenerB);
	}

	/**
	 * Disconnect the selection models that have previously been passed to
	 * {@link #connect(SelectionModel, SelectionModel)}
	 */
	public void disconnect() {
		if (selectionModelA != null) {
			selectionModelA.removeSelectionListener(selectionListenerA);
			selectionModelA = null;
		}
		if (selectionModelB != null) {
			selectionModelB.removeSelectionListener(selectionListenerB);
			selectionModelB = null;
		}
	}

	/**
	 * Set the {@link ConnectionMode} of this connector
	 * 
	 * @param connectionMode The {@link ConnectionMode}
	 */
	public void setConnectionMode(ConnectionMode connectionMode) {
		this.connectionMode = Objects.requireNonNull(connectionMode, "The connectionMode may not be null");
	}

	/**
	 * Update the selection from the first {@link SelectionModel} to the second
	 * {@link SelectionModel}, if an update in the opposite direction is not
	 * currently in progress.
	 */
	private void updateAtoB() {
		if (currentlyUpdatingBtoA) {
			return;
		}
		currentlyUpdatingAtoB = true;
		Set<A> selectionA = selectionModelA.getSelection();
		Set<B> selectionB = mappingAtoB.apply(selectionA);
		if (selectionB == null) {
			selectionB = Collections.emptySet();
		}
		selectionModelB.setSelection(selectionB);
		currentlyUpdatingAtoB = false;
	}

	/**
	 * Update the selection from the second {@link SelectionModel} to the first
	 * {@link SelectionModel}, if an update in the opposite direction is not
	 * currently in progress.
	 */
	private void updateBtoA() {
		if (currentlyUpdatingAtoB) {
			return;
		}
		currentlyUpdatingBtoA = true;
		Set<B> selectionB = selectionModelB.getSelection();
		Set<A> selectionA = mappingBtoA.apply(selectionB);
		if (selectionA == null) {
			selectionA = Collections.emptySet();
		}
		selectionModelA.setSelection(selectionA);
		currentlyUpdatingBtoA = false;
	}

}