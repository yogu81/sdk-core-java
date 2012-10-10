package com.paypal.core;

import java.util.Map;

import com.paypal.core.credential.ICredential;
import com.paypal.sdk.exceptions.OAuthException;

/**
 * <code>APICallPreHandler</code> defines a high level abstraction for call
 * specific operations. The calls may be divided as per formats as SOAP or NVP.
 * 
 */
public interface APICallPreHandler {

	/**
	 * Returns headers for HTTP call
	 * 
	 * @return Map of headers with name and value
	 * @throws OAuthException
	 */
	Map<String, String> getHeaderMap() throws OAuthException;

	/**
	 * Returns the payload for the API call. The implementation should take care
	 * in formatting the payload appropriately
	 * 
	 * @return Payload as String
	 */
	String getPayLoad();

	/**
	 * Returns the endpoint for the API call
	 * 
	 * @return Endpoint
	 */
	String getEndPoint();

	/**
	 * Returns {@link ICredential} configured for the api call
	 * 
	 * @return ICredential object
	 */
	ICredential getCredential();

}
