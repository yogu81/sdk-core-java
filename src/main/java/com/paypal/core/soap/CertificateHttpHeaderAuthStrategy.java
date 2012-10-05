package com.paypal.core.soap;

import java.util.HashMap;
import java.util.Map;

import com.paypal.core.AuthenticationStrategy;
import com.paypal.core.Constants;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.TokenAuthorization;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.sdk.util.OAuthSignature;

/**
 * <code>CertificateHttpHeaderAuthStrategy</code> is an implementation of
 * {@link AuthenticationStrategy} which acts on {@link CertificateCredential}
 * and retrieves them as HTTP headers
 * 
 * @author kjayakumar
 * 
 */
public class CertificateHttpHeaderAuthStrategy implements
		AuthenticationStrategy<Map<String, String>, CertificateCredential> {

	/**
	 * Endpoint url
	 */
	private String endPointUrl;

	/**
	 * CertificateHttpHeaderAuthStrategy
	 * 
	 * @param endPointUrl
	 */
	public CertificateHttpHeaderAuthStrategy(String endPointUrl) {
		this.endPointUrl = endPointUrl;
	}

	public Map<String, String> realize(CertificateCredential credential)
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
		}
		return headers;
	}

	/**
	 * Processing for {@link TokenAuthorization} under
	 * {@link CertificateCredential}
	 * 
	 * @param credential
	 *            {@link CertificateCredential} instance
	 * @param tokenAuth
	 *            {@link TokenAuthorization} instance
	 * @return Map of HTTP headers
	 * @throws OAuthException
	 */
	private Map<String, String> processTokenAuthorization(
			CertificateCredential credential, TokenAuthorization tokenAuth)
			throws OAuthException {
		Map<String, String> headers = new HashMap<String, String>();
		String authString = OAuthSignature.getFullAuthString(
				credential.getUserName(), credential.getPassword(),
				tokenAuth.getAccessToken(), tokenAuth.getTokenSecret(),
				OAuthSignature.HTTPMethod.POST, endPointUrl, null);
		headers.put(Constants.PAYPAL_AUTHORIZATION_MERCHANT, authString);
		return headers;
	}

}
