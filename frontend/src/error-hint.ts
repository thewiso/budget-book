import { html, css, LitElement, property } from 'lit-element';
import '@vaadin/button';
import '@vaadin/password-field';

class ErrorHint extends LitElement {

	@property()
	message = "An error occured"

	render() {
		return html`
			<div class="error-hint">
				<vaadin-icon icon="lumo:error" class="error-icon"></vaadin-icon>
				<label class="error-text">${this.message}</label>
			</div>
    	`;
	}

	static get styles() {
		return css`
		:host {
			display: block;
		}
				
		.error-hint {
			padding: 10px;
			padding-right: 20px;
			background-color: var(--lumo-error-color-10pct);
			display: flex;
			flex-direction: row;
			align-items: start;
			color: var(--lumo-error-text-color);
		}
		
		.error-text {
			font-size: var(--lumo-font-size-m);
			font-weight: bold;
			margin-left: 5px;
		}
		
		.error-icon {
			margin-top: 2px;
		}
		`
	}
}

customElements.define('error-hint', ErrorHint);