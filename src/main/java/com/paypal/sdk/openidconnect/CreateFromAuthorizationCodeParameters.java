package com.paypal.sdk.openidconnect;

import java.util.HashMap;
import java.util.Map;

public class CreateFromAuthorizationCodeParameters {
	
	/**
	 * Code
	 */
	private static final String CODE = "code";

	/**
	 * Redirect URI
	 */
	private static final String REDIRECTURI = "redirect_uri";
	
	// Map backing QueryParameters intended to processed
	// by SDK library 'RESTUtil'
	private Map<String, String> containerMap;

	public CreateFromAuthorizationCodeParameters() {
		containerMap = new HashMap<String, String>();
	}

	/**
	 * @return the containerMap
	 */
	Map<String, String> getContainerMap() {
		return containerMap;
	}
	
	/**
	 * Set the code
	 * 
	 * @param code
	 */
	public void setCode(String code) {
		containerMap.put(CODE, code);
	}

	/**
	 * Set the redirect URI
	 * 
	 * @param redirectURI
	 */
	public void setRedirectURI(String redirectURI) {
		containerMap.put(REDIRECTURI, redirectURI);
	}
	
}
