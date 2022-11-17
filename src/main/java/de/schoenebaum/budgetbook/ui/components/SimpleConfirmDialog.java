package de.schoenebaum.budgetbook.ui.components;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class SimpleConfirmDialog extends ConfirmDialog {

	private static final long serialVersionUID = 4807748376725995322L;

	public SimpleConfirmDialog(String header, String text, ComponentEventListener<ConfirmEvent> confirmEventListener,
			ComponentEventListener<RejectEvent> rejectEventListener) {
		setHeader(header);
		setText(text);

		setRejectable(true);
		setRejectText("Cancel");
		if (rejectEventListener != null) {
			addRejectListener(rejectEventListener);
		}

		setConfirmText("Ok");
		if (confirmEventListener != null) {
			addConfirmListener(confirmEventListener);
		}
	}

	public SimpleConfirmDialog(String header, String text, ComponentEventListener<ConfirmEvent> confirmEventListener) {
		this(header, text, confirmEventListener, null);
	}

	public static SimpleConfirmDialog createSimpleConfirmDialog(String header, String text,
			ComponentEventListener<ConfirmEvent> confirmEventListener) {
		SimpleConfirmDialog dialog = new SimpleConfirmDialog(header, text, confirmEventListener);

		return dialog;
	}

}
