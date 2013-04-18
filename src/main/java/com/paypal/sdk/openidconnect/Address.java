package com.paypal.sdk.openidconnect;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import com.paypal.sdk.openidconnect.JSONFormatter;
import com.paypal.sdk.openidconnect.PayPalRESTException;
import com.paypal.sdk.openidconnect.PayPalResource;

public class Address {

	/**
	 * Full street address component, which may include house number, street name.
	 */
	private String streetAddress;
	
	/**
	 * City or locality component.
	 */
	private String locality;
	
	/**
	 * State, province, prefecture or region component.
	 */
	private String region;
	
	/**
	 * Zip code or postal code component.
	 */
	private String postalCode;
	
	/**
	 * Country name component.
	 */
	private String country;
	

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
	public Address() {
	}

	/**
	 * Setter for streetAddress
	 */
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
 	}
 	
 	/**
	 * Getter for streetAddress
	 */
	public String getStreetAddress() {
		return this.streetAddress;
	}
	
	/**
	 * Setter for locality
	 */
	public void setLocality(String locality) {
		this.locality = locality;
 	}
 	
 	/**
	 * Getter for locality
	 */
	public String getLocality() {
		return this.locality;
	}
	
	/**
	 * Setter for region
	 */
	public void setRegion(String region) {
		this.region = region;
 	}
 	
 	/**
	 * Getter for region
	 */
	public String getRegion() {
		return this.region;
	}
	
	/**
	 * Setter for postalCode
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
 	}
 	
 	/**
	 * Getter for postalCode
	 */
	public String getPostalCode() {
		return this.postalCode;
	}
	
	/**
	 * Setter for country
	 */
	public void setCountry(String country) {
		this.country = country;
 	}
 	
 	/**
	 * Getter for country
	 */
	public String getCountry() {
		return this.country;
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