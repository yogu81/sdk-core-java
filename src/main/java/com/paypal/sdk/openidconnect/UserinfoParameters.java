package com.paypal.sdk.openidconnect;

import java.util.HashMap;
import java.util.Map;

public class UserinfoParameters {

	/**
	 * Schema
	 */
	private static final String SCHEMA = "schema";
	
	/**
	 * Access Token
	 */
	private static final String ACCESSTOKEN = "access_token";

	// Map backing QueryParameters intended to processed
	// by SDK library 'RESTUtil'
	private Map<String, String> containerMap;

	public UserinfoParameters() {
		containerMap = new HashMap<String, String>();
		containerMap.put(SCHEMA, "openid");
	}

	/**
	 * @return the containerMap
	 */
	public Map<String, String> getContainerMap() {
		return containerMap;
	}

	/**
	 * Set the schema
	 * 
	 * @param schema
	 */
	public void setSchema(String schema) {
		containerMap.put(SCHEMA, schema);
	}
	
	/**
	 * Set the accessToken
	 * 
	 * @param accessToken
	 */
	public void setAccessToken(String accessToken) {
		containerMap.put(ACCESSTOKEN, accessToken);
	}

}
