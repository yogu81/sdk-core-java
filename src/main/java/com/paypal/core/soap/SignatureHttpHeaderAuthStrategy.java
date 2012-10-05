package com.paypal.core.soap;

import java.util.HashMap;
import java.util.Map;

import com.paypal.core.AuthenticationStrategy;
import com.paypal.core.Constants;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.TokenAuthorization;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.sdk.util.OAuthSignature;

/**
 * <code>CertificateHttpHeaderAuthStrategy</code> is an implementation of
 * {@link AuthenticationStrategy} which acts on {@link SignatureCredential} and
 * retrieves them as HTTP headers
 * 
 * @author kjayakumar
 * 
 */
public class SignatureHttpHeaderAuthStrategy implements
		AuthenticationStrategy<Map<String, String>, SignatureCredential> {

	private String endPointUrl;

	public SignatureHttpHeaderAuthStrategy(String endPointUrl) {
		super();
		this.endPointUrl = endPointUrl;
	}

	public Map<String, String> realize(SignatureCredential credential)
			throws OAuthException {
		Map<String, String> headers = null;
		if (credential.getThirdPartyAuthorization() != null
				&& credential.getThirdPartyAuthorization() instanceof TokenAuthorization) {
			headers = processTokenAuthorization(credential,
					(TokenAuthorization) credential
							.getThirdPartyAuthorization());
		} else {
			// Subject Authorization is handled by soap headers
			// Http headers remain the same
			headers = new HashMap<String, String>();
			headers.put(Constants.PAYPAL_SECURITY_USERID_HEADER,
					credential.getUserName());
			headers.put(Constants.PAYPAL_SECURITY_PASSWORD_HEADER,
					credential.getPassword());
			headers.put(Constants.PAYPAL_SECURITY_SIGNATURE_HEADER,
					credential.getSignature());
		}
		return headers;
	}

	/**
	 * Processing for {@link TokenAuthorization} under
	 * {@link SignatureCredential}
	 * 
	 * @param sigCred
	 *            {@link SignatureCredential} instance
	 * @param tokenAuth
	 *            {@link TokenAuthorization} instance
	 * @return Map of HTTP headers
	 * @throws OAuthException
	 */
	private Map<String, String> processTokenAuthorization(
			SignatureCredential sigCred, TokenAuthorization tokenAuth)
			throws OAuthException {
		Map<String, String> headers = new HashMap<String, String>();
		String authString = OAuthSignature.getFullAuthString(
				sigCred.getUserName(), sigCred.getPassword(),
				tokenAuth.getAccessToken(), tokenAuth.getTokenSecret(),
				OAuthSignature.HTTPMethod.POST, endPointUrl, null);
		headers.put(Constants.PAYPAL_AUTHORIZATION_MERCHANT, authString);
		return headers;
	}

}
