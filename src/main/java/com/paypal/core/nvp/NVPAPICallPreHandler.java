package com.paypal.core.nvp;

import java.util.HashMap;
import java.util.Map;

import com.paypal.core.APICallPreHandler;
import com.paypal.core.ConfigManager;
import com.paypal.core.Constants;
import com.paypal.core.CredentialManager;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.ICredential;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.TokenAuthorization;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.sdk.exceptions.OAuthException;

/**
 * <code>NVPAPICallPreHandler</code> is an implementation of
 * {@link APICallPreHandler} for NVP based API service
 * 
 * @author kjayakumar
 * 
 */
public class NVPAPICallPreHandler implements APICallPreHandler {

	/**
	 * Service Name
	 */
	private final String serviceName;

	/**
	 * Raw payload from stubs
	 */
	private final String rawPayLoad;

	/**
	 * API method
	 */
	private final String method;

	/**
	 * API Username for authentication
	 */
	private String apiUserName;

	/**
	 * {@link ICredential} for authentication
	 */
	private ICredential credential;

	/**
	 * Access token if any for authorization
	 */
	private String accessToken;

	/**
	 * TokenSecret if any for authorization
	 */
	private String tokenSecret;

	// Private Constructor
	private NVPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method) {
		super();
		this.serviceName = serviceName;
		this.rawPayLoad = rawPayLoad;
		this.method = method;
	}

	/**
	 * NVPAPICallPreHandler
	 * 
	 * @param serviceName
	 *            Service Name
	 * @param rawPayLoad
	 *            Payload
	 * @param method
	 *            API method
	 * @param apiUserName
	 *            API Username
	 * @throws MissingCredentialException
	 * @throws InvalidCredentialException
	 */
	public NVPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method, String apiUserName)
			throws InvalidCredentialException, MissingCredentialException {
		this(serviceName, rawPayLoad, method);
		this.apiUserName = apiUserName;
		initCredential();
	}

	/**
	 * NVPAPICallPreHandler
	 * 
	 * @param serviceName
	 *            Service Name
	 * @param rawPayLoad
	 *            Payload
	 * @param method
	 *            API method
	 * @param credential
	 *            {@link ICredential} instance
	 */
	public NVPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method, ICredential credential) {
		this(serviceName, rawPayLoad, method);
		if (credential == null) {
			throw new IllegalArgumentException(
					"Credential is null in NVPAPICallPreHandler");
		}
		this.credential = credential;
	}

	public Map<String, String> getHeader() throws InvalidCredentialException,
			MissingCredentialException, OAuthException {
		Map<String, String> headerMap = new HashMap<String, String>();
		if (credential == null) {
			credential = getCredentials();
		}
		if (credential instanceof SignatureCredential) {
			SignatureHttpHeaderAuthStrategy signatureHttpHeaderAuthStrategy = new SignatureHttpHeaderAuthStrategy(
					getEndpoint());
			headerMap = signatureHttpHeaderAuthStrategy
					.realize((SignatureCredential) credential);
		} else if (credential instanceof CertificateCredential) {
			CertificateHttpHeaderAuthStrategy certificateHttpHeaderAuthStrategy = new CertificateHttpHeaderAuthStrategy(
					getEndpoint());
			headerMap = certificateHttpHeaderAuthStrategy
					.realize((CertificateCredential) credential);
		}
		headerMap.putAll(getDefaultHttpHeadersNVP());
		return headerMap;
	}

	public String getPayLoad() {
		// No formating necessary for NVP return the raw payload
		return rawPayLoad;
	}

	public String getEndpoint() {
		return ConfigManager.getInstance().getValue(Constants.END_POINT);
	}

	public ICredential getCredential() {
		return credential;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	private ICredential getCredentials() throws InvalidCredentialException,
			MissingCredentialException {
		ICredential credential = null;
		CredentialManager credentialManager = CredentialManager.getInstance();
		credential = credentialManager.getCredentialObject(apiUserName);
		if (accessToken != null && !accessToken.isEmpty()) {
			ThirdPartyAuthorization tokenAuth = new TokenAuthorization(
					accessToken, tokenSecret);
			if (credential instanceof SignatureCredential) {
				SignatureCredential sigCred = (SignatureCredential) credential;
				sigCred.setThirdPartyAuthorization(tokenAuth);
			} else if (credential instanceof CertificateCredential) {
				CertificateCredential certCred = (CertificateCredential) credential;
				certCred.setThirdPartyAuthorization(tokenAuth);
			}
		}
		return credential;
	}

	private Map<String, String> getDefaultHttpHeadersNVP() {
		Map<String, String> returnMap = new HashMap<String, String>();
		returnMap.put(Constants.PAYPAL_APPLICATION_ID, getApplicationId());
		returnMap.put(Constants.PAYPAL_REQUEST_DATA_FORMAT_HEADER, Constants.NVP);
		returnMap.put(Constants.PAYPAL_RESPONSE_DATA_FORMAT_HEADER, Constants.NVP);
		// returnMap.put("X-PAYPAL-DEVICE-IPADDRESS",
		// httpConfiguration.getIpAddress());
		//TODO revisit
		returnMap.put("X-PAYPAL-REQUEST-SOURCE", Constants.SDK_NAME + "-"
				+ Constants.SDK_VERSION);
		return returnMap;
	}

	private String getApplicationId() {
		String applicationId = null;
		if (credential instanceof CertificateCredential) {
			applicationId = ((CertificateCredential) credential)
					.getApplicationId();
		} else if (credential instanceof SignatureCredential) {
			applicationId = ((SignatureCredential) credential)
					.getApplicationId();
		}
		return applicationId;
	}

	private void initCredential() throws InvalidCredentialException,
			MissingCredentialException {
		if (credential == null) {
			credential = getCredentials();
		}
	}

}
