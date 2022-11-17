import { html, css, LitElement, query } from 'lit-element';
import '@vaadin/button';
import '@vaadin/password-field';
//import "error-hint.js"

class PasswordLoginForm extends LitElement {

	@query('#loginForm')
	loginForm!: HTMLFormElement;
	
	@query('#submitLoginButton')
	submitLoginButton!: HTMLButtonElement;

	render() {
		return html`
			<form id="loginForm" method="POST" action="login" class="login-body">
				${this.renderErrorMessage()}
				<h2 class="login-title">Log in</h2>
				<vaadin-password-field name="password" required label="Password" autocomplete="current-password"></vaadin-password-field>
				<vaadin-button @click="${this.submit}" theme="primary" class="login-button" id="submitLoginButton">Log in</vaadin-button>
			</form>
    	`;
	}
	
	renderErrorMessage(){
		if(this.isError){
			return html`
				<error-hint message="Incorrect password"></error-hint>
			`;	
		}
		return undefined;
	}

	submit() {
		this.submitLoginButton.disabled = true;
		console.log("submit");
		this.loginForm.submit();
	}
	
	get isError(): boolean{
		const urlSearchParams = new URLSearchParams(window.location.search);
		const params = Object.fromEntries(urlSearchParams.entries());
		return params.error !== undefined;
	}

	static get styles() {
		return css`
		:host {
			display: block;
		}
		
		.login-body {
			padding: 25px;
			display: flex;
			flex-direction: column;
		}
		
		.login-title {
			margin-top: 10px;
			margin-bottom: 0;
		}
		
		.login-button {
			margin-top: 20px;
		}
		
		.error-message {
			padding: 10px;
			background-color: var(--lumo-error-color-10pct);
			display: flex;
			flex-direction: row;
			align-items: center;
			color: var(--lumo-error-text-color);
		}
		
		.error-text {
			font-size: var(--lumo-font-size-m);
			font-weight: bold;
			margin-left: 5px;
		}
		
		`
	}
}

customElements.define('password-login-form', PasswordLoginForm);