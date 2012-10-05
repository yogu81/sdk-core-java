package com.paypal.core;

import java.util.Map;

import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.core.credential.ICredential;

/**
 * <code>APICallPreHandler</code> defines a high level abstraction for call
 * specific operations. The calls may be divided as per formats as SOAP or NVP.
 * 
 * @author kjayakumar
 * 
 */
public interface APICallPreHandler {

	/**
	 * Returns headers for HTTP call
	 * 
	 * @return Map of headers with name and value
	 * @throws InvalidCredentialException
	 * @throws MissingCredentialException
	 * @throws OAuthException
	 */
	public Map<String, String> getHeader() throws InvalidCredentialException,
			MissingCredentialException, OAuthException;

	/**
	 * Returns the payload for the API call. The implementation should take care
	 * in formatting the payload appropriately
	 * 
	 * @return Payload as String
	 */
	public String getPayLoad();

	/**
	 * Returns the endpoint for the API call
	 * 
	 * @return Endpoint
	 */
	public String getEndpoint();

	/**
	 * Returns {@link ICredential} configured for the api call
	 * 
	 * @return ICredential object
	 */
	public ICredential getCredential();

}
