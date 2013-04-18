package com.paypal.sdk.openidconnect;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import com.paypal.sdk.openidconnect.JSONFormatter;
import com.paypal.sdk.openidconnect.PayPalRESTException;
import com.paypal.sdk.openidconnect.PayPalResource;

public class Error {

	/**
	 * A single ASCII error code from the following enum.
	 */
	private String error;
	
	/**
	 * A resource ID that indicates the starting resource in the returned results.
	 */
	private String errorDescription;
	
	/**
	 * A URI identifying a human-readable web page with information about the error, used to provide the client developer with additional information about the error.
	 */
	private String errorUri;
	

	/**
	 * Returns the last request sent to the Service
	 * 
	 * @return Last request sent to the server
	 */
	public static String getLastRequest() {
		return PayPalResource.getLastRequest();
	}

	/**
	 * Returns the last response returned by the Service
	 * 
	 * @return Last response got from the Service
	 */
	public static String getLastResponse() {
		return PayPalResource.getLastResponse();
	}

	/**
	 * Initialize using InputStream(of a Properties file)
	 * 
	 * @param is
	 *            InputStream
	 * @throws PayPalRESTException
	 */
	public static void initConfig(InputStream is) throws PayPalRESTException {
		PayPalResource.initConfig(is);
	}

	/**
	 * Initialize using a File(Properties file)
	 * 
	 * @param file
	 *            File object of a properties entity
	 * @throws PayPalRESTException
	 */
	public static void initConfig(File file) throws PayPalRESTException {
		PayPalResource.initConfig(file);
	}

	/**
	 * Initialize using Properties
	 * 
	 * @param properties
	 *            Properties object
	 */
	public static void initConfig(Properties properties) {
		PayPalResource.initConfig(properties);
	}

	/**
	 * Default Constructor
	 */
	public Error() {
	}

	/**
	 * Parameterized Constructor
	 */
	public Error(String error) {
		this.error = error;
	}
	
	/**
	 * Setter for error
	 */
	public void setError(String error) {
		this.error = error;
 	}
 	
 	/**
	 * Getter for error
	 */
	public String getError() {
		return this.error;
	}
	
	/**
	 * Setter for errorDescription
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
 	}
 	
 	/**
	 * Getter for errorDescription
	 */
	public String getErrorDescription() {
		return this.errorDescription;
	}
	
	/**
	 * Setter for errorUri
	 */
	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
 	}
 	
 	/**
	 * Getter for errorUri
	 */
	public String getErrorUri() {
		return this.errorUri;
	}
	
	/**
	 * Returns a JSON string corresponding to object state
	 * 
	 * @return JSON representation
	 */
	public String toJSON() {
		return JSONFormatter.toJSON(this);
	}

	@Override
	public String toString() {
		return toJSON();
	}
}