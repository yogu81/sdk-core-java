package com.paypal.sdk.openidconnect;

/**
 * PayPalException handles all exceptions related to REST services
 */
public class PayPalRESTException extends Exception {

	public PayPalRESTException(String message) {
		super(message);
	}

	public PayPalRESTException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public PayPalRESTException(Throwable throwable) {
		super(throwable);
	}

}
