package de.schoenebaum.budgetbook.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;

@Tag("error-hint")
@JsModule("./src/error-hint.ts")
public class ErrorHint extends LitTemplate {

	private static final long serialVersionUID = 6074682664878559107L;

	public ErrorHint() {
		
	}
	
	public ErrorHint(String message) {
		
	}
	
	public void setMessage(String message) {
		getElement().setProperty("message", message);
	}
}
