/**
 * 
 */
package com.paypal.core;

import java.util.HashMap;
import java.util.Map;

import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.sdk.util.OAuthSignature;

/**
 * @author lvairamani
 * 
 */
public class AuthenticationService {
	private Map<String, String> headers = new HashMap<String, String>();
	ICredential apiCred = null;
	CredentialManager cred = null;
	private String authString = Constants.EMPTY_STRING;
	ConfigManager config = null;

	/**
	 * @param apiUsername
	 * @param connection
	 * @param accessToken
	 * @param tokenSecret
	 * @param config
	 * @return
	 * @throws SSLConfigurationException
	 * @throws InvalidCredentialException
	 * @throws MissingCredentialException
	 * @throws OAuthException
	 */
	public Map<String, String> getPayPalHeaders(String apiUsername,
			HttpConnection connection, String accessToken, String tokenSecret,
			HttpConfiguration httpConfiguration)
			throws SSLConfigurationException, InvalidCredentialException,
			MissingCredentialException, OAuthException {
		cred = CredentialManager.getInstance();
		apiCred = cred.getCredentialObject(apiUsername);
		config = ConfigManager.getInstance();
		/* Add headers required for service authentication */
		if ((Constants.EMPTY_STRING != accessToken && accessToken != null)
				&& (Constants.EMPTY_STRING != tokenSecret && tokenSecret != null)) {
			authString = generateAuthString(apiCred, accessToken, tokenSecret,
					httpConfiguration.getEndPointUrl());
			headers.put("X-PAYPAL-AUTHORIZATION", authString);
			//headers.put("CLIENT-AUTH", "No cert");
			connection.setDefaultSSL(true);
			connection.setupClientSSL(null, null,
					httpConfiguration.isTrustAll());
		} else if (apiCred instanceof SignatureCredential) {
			headers.put("X-PAYPAL-SECURITY-USERID",
					((SignatureCredential) apiCred).getUserName());
			headers.put("X-PAYPAL-SECURITY-PASSWORD",
					((SignatureCredential) apiCred).getPassword());
			headers.put("X-PAYPAL-SECURITY-SIGNATURE",
					((SignatureCredential) apiCred).getSignature());
			connection.setDefaultSSL(true);
			connection.setupClientSSL(null, null,
					httpConfiguration.isTrustAll());
		} else if (apiCred instanceof CertificateCredential) {
			connection.setDefaultSSL(false);
			headers.put("X-PAYPAL-SECURITY-USERID",
					((CertificateCredential) apiCred).getUserName());
			headers.put("X-PAYPAL-SECURITY-PASSWORD",
					((CertificateCredential) apiCred).getPassword());
			connection.setupClientSSL(
					((CertificateCredential) apiCred).getCertificatePath(),
					((CertificateCredential) apiCred).getCertificateKey(),
					httpConfiguration.isTrustAll());
		}

		/* Add other headers */
		headers.put("X-PAYPAL-APPLICATION-ID", apiCred.getApplicationId());
		headers.put("X-PAYPAL-REQUEST-DATA-FORMAT",
				config.getValue("service.Binding"));
		headers.put("X-PAYPAL-RESPONSE-DATA-FORMAT",
				config.getValue("service.Binding"));
		if (httpConfiguration.getEndPointUrl().contains("sandbox")) {
			headers.put("X-PAYPAL-SANDBOX-EMAIL-ADDRESS",
					Constants.SANDBOX_EMAIL_ADDRESS);
		}

		return headers;

	}

	private String generateAuthString(ICredential apiCred, String accessToken,
			String tokenSecret, String endPoint) throws OAuthException {

		authString = OAuthSignature.getFullAuthString(apiCred.getUserName(),
				apiCred.getPassword(), accessToken, tokenSecret,
				OAuthSignature.HTTPMethod.POST, endPoint, null);
		return authString;
	}
}
