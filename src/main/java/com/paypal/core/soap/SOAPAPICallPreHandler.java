package com.paypal.core.soap;

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
 * <code>SOAPAPICallPreHandler</code> is an implementation of
 * {@link APICallPreHandler} for SOAP based API service
 * 
 * @author kjayakumar
 * 
 */
public class SOAPAPICallPreHandler implements APICallPreHandler {

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
	private SOAPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method) {
		super();
		this.serviceName = serviceName;
		this.rawPayLoad = rawPayLoad;
		this.method = method;
	}

	public SOAPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method, String apiUserName)
			throws InvalidCredentialException, MissingCredentialException {
		this(serviceName, rawPayLoad, method);
		this.apiUserName = apiUserName;
		initCredential();
	}

	public SOAPAPICallPreHandler(String serviceName, String rawPayLoad,
			String method, ICredential credential) {
		this(serviceName, rawPayLoad, method);
		if (credential == null) {
			throw new IllegalArgumentException(
					"Credential is null in SOAPAPICallPreHandler");
		}
		this.credential = credential;
	}

	public Map<String, String> getHeader() throws OAuthException {
		Map<String, String> headerMap = new HashMap<String, String>();
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
		headerMap.putAll(getDefaultHttpHeadersSOAP());
		return headerMap;
	}

	public String getPayLoad() {
		
		// This method appends SOAP Headers to payload
		// if the credentials mandate soap headers
		String payLoad = null;
		String headerPart = null;
		if (credential instanceof SignatureCredential) {
			SignatureCredential sigCredential = (SignatureCredential) credential;
			SignatureSOAPHeaderAuthStrategy signatureSoapHeaderAuthStrategy = new SignatureSOAPHeaderAuthStrategy();
			signatureSoapHeaderAuthStrategy
					.setThirdPartyAuthorization(sigCredential
							.getThirdPartyAuthorization());
			headerPart = signatureSoapHeaderAuthStrategy.realize(sigCredential);
		} else if (credential instanceof CertificateCredential) {
			CertificateCredential certCredential = (CertificateCredential) credential;
			CertificateSOAPHeaderAuthStrategy certificateSoapHeaderAuthStrategy = new CertificateSOAPHeaderAuthStrategy();
			certificateSoapHeaderAuthStrategy
					.setThirdPartyAuthorization(certCredential
							.getThirdPartyAuthorization());
			headerPart = certificateSoapHeaderAuthStrategy.realize(certCredential);

		}
		payLoad = getPayLoadUsingSOAPHeader(headerPart);
		return payLoad;
	}

	public String getEndpoint() {
		return ConfigManager.getInstance().getValue("service.EndPoint")
				+ serviceName + '/' + method;
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

	/**
	 * Returns a credential as configured in the application configuration
	 * 
	 * @return ICredential
	 * @throws InvalidCredentialException
	 * @throws MissingCredentialException
	 */
	private ICredential getCredentials() throws InvalidCredentialException,
			MissingCredentialException {
		ICredential credential = null;
		CredentialManager credentialManager = CredentialManager.getInstance();
		credential = credentialManager.getCredentialObject(apiUserName);
		if (accessToken != null && !accessToken.isEmpty()) {

			// Set third party authorization to token
			// if token is sent as part of request call
			ThirdPartyAuthorization tokenAuth = new TokenAuthorization(
					accessToken, tokenSecret);
			if (credential instanceof SignatureCredential) {
				SignatureCredential sigCred = (SignatureCredential) credential;
				sigCred.setThirdPartyAuthorization(tokenAuth);
			} else if (credential instanceof CertificateCredential) {
				CertificateCredential certCred = (CertificateCredential) credential;
				certCred.setThirdPartyAuthorization(tokenAuth);
			}
		} else {
			// TODO subject authorization set in credential
		}
		return credential;
	}

	/**
	 * Returns default HTTP headers used in SOAP call
	 * @return Map of HTTP headers
	 */
	private Map<String, String> getDefaultHttpHeadersSOAP() {
		Map<String, String> returnMap = new HashMap<String, String>();
		returnMap.put(Constants.PAYPAL_REQUEST_DATA_FORMAT_HEADER, "SOAP");
		returnMap.put(Constants.PAYPAL_RESPONSE_DATA_FORMAT_HEADER, "SOAP");
		// returnMap.put("X-PAYPAL-DEVICE-IPADDRESS",
		// httpConfiguration.getIpAddress());
		// TODO revisit
		returnMap.put("X-PAYPAL-REQUEST-SOURCE", Constants.SDK_NAME + "-"
				+ Constants.SDK_VERSION);
		return returnMap;
	}

	private void initCredential() throws InvalidCredentialException,
			MissingCredentialException {
		if (credential == null) {
			credential = getCredentials();
		}
	}

	private String getSoapEnvelopeStart() {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:ebay:api:PayPalAPI\" xmlns:ebl=\"urn:ebay:apis:eBLBaseComponents\" xmlns:cc=\"urn:ebay:apis:CoreComponentTypes\" xmlns:ed=\"urn:ebay:apis:EnhancedDataTypes\">";
	}

	private String getSoapEnvelopeEnd() {
		return "</soapenv:Envelope>";
	}

	private String getSoapBody() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<soapenv:Body>");
		stringBuilder.append(rawPayLoad);
		stringBuilder.append("</soapenv:Body>");
		return stringBuilder.toString();
	}
	
	private String getPayLoadUsingSOAPHeader(String headerPart) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getSoapEnvelopeStart());
		stringBuilder.append(headerPart);
		stringBuilder.append(getSoapBody());
		stringBuilder.append(getSoapEnvelopeEnd());
		return stringBuilder.toString();
	}

}
