package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

/**
 * allows search and auto-complete operations.
 * 
 * masks all events from the original JCombobox class. Only lets ActionEvents
 * pass which fulfill the constraints defined in the constructor.
 * 
 * Supports action events by using the enter key.
 *
 * @param <E> e
 */
public class AutoCompleteComboBox<E> extends JComboBox<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8694399146338674035L;

	private AutoTextFieldEditor autoTextFieldEditor;
	private boolean isFired;

	private List<E> items;
	private KeyEvent lastKeyEvent;
	private boolean ignoreArrowKeyActions;
	private boolean ignoreEnterKeyActions;

	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

	public AutoCompleteComboBox(List<E> items, boolean ignoreArrowKeyActions, boolean ignoreEnterKeyActions) {
		super();

		isFired = false;
		autoTextFieldEditor = new AutoTextFieldEditor(items);
		setEditable(true);
		setModel(new DefaultComboBoxModel(items.toArray()) {

			protected void fireContentsChanged(Object obj, int i, int j) {
				if (!isFired)
					super.fireContentsChanged(obj, i, j);
			}

		});
		setEditor(autoTextFieldEditor);

		this.items = items;
		this.ignoreArrowKeyActions = ignoreArrowKeyActions;
		this.ignoreEnterKeyActions = ignoreEnterKeyActions;

		initialize();
	}

	private void initialize() {
		getEditor().selectAll();

		// essential
		setDataList(items);

		super.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JComboBox) {
					if (lastKeyEvent != null)
						if (lastKeyEvent.getKeyCode() == KeyEvent.VK_UP
								|| lastKeyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
							if (!ignoreArrowKeyActions)
								fireActionEvent(e);

							return;
						}

					fireActionEvent(e);
				}
			}
		});

		getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent event) {
				lastKeyEvent = event;

				if (!ignoreEnterKeyActions)
					if (lastKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
						if (((JTextComponent) ((JComboBox) ((Component) event.getSource()).getParent()).getEditor()
								.getEditorComponent()).getText().isEmpty())
							System.out.println("please dont make me blank");
						else {
							fireActionEvent(new ActionEvent(event.getSource(), ActionEvent.ACTION_PERFORMED,
									getActionCommand()));
						}
					}
			}
		});
	}

	private final void fireActionEvent(ActionEvent e) {
		for (ActionListener actionListener : actionListeners)
			actionListener.actionPerformed(e);
	}

	public void removeActionListener(ActionListener actionListener) {
		actionListeners.remove(actionListener);
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListeners.remove(actionListener);
		this.actionListeners.add(actionListener);
	}

	private class AutoTextFieldEditor extends BasicComboBoxEditor {

		private AutoTextField<E> getAutoTextFieldEditor() {
			return (AutoTextField<E>) editor;
		}

		AutoTextFieldEditor(List<E> list) {
			editor = new AutoTextField<E>(list, AutoCompleteComboBox.this);
		}
	}

	public boolean isCaseSensitive() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
	}

	public void setCaseSensitive(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
	}

	public boolean isStrict() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
	}

	public void setStrict(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
	}

	public List<E> getDataList() {
		return autoTextFieldEditor.getAutoTextFieldEditor().getDataList();
	}

	public void setDataList(List<E> list) {
		autoTextFieldEditor.getAutoTextFieldEditor().setDataList(list);
		setModel(new DefaultComboBoxModel(list.toArray()));
	}

	void setSelectedValue(Object obj) {
		if (isFired) {
			return;
		} else {
			isFired = true;
			setSelectedItem(obj);
			fireItemStateChanged(new ItemEvent(this, 701, selectedItemReminder, 1));
			isFired = false;
			return;
		}
	}

	protected void fireActionEvent() {
		if (!isFired)
			super.fireActionEvent();
	}

}
