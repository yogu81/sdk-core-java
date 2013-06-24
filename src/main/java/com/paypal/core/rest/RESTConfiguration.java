package com.paypal.core.rest;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.paypal.core.APICallPreHandler;
import com.paypal.core.Constants;
import com.paypal.core.SDKUtil;
import com.paypal.core.codec.binary.Base64;
import com.paypal.core.credential.ICredential;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.sdk.util.UserAgentHeader;

/**
 * RESTConfiguration helps {@link PayPalResource} with state dependent utility
 * methods
 */
public class RESTConfiguration implements APICallPreHandler {

	/**
	 * Configuration Map used for dynamic configuration
	 */
	private Map<String, String> configurationMap = null;

	/**
	 * Base URL for the service
	 */
	private URL url;

	/**
	 * Authorization token
	 */
	private String authorizationToken;

	/**
	 * Resource URI as defined in the WSDL
	 */
	private String resourcePath;

	/**
	 * Request Id
	 */
	private String requestId;

	/**
	 * Custom headers Map
	 */
	private Map<String, String> headersMap;

	/**
	 * Request Payload
	 */
	private String payLoad;

	/**
	 * Constructor using configurations dynamically
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 */
	public RESTConfiguration(Map<String, String> configurationMap) {
		this.configurationMap = SDKUtil.combineDefaultMap(configurationMap);
	}

	/**
	 * Constructor using a Map of headers for forming custom headers
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 * @param headersMap
	 *            Headers Map
	 */
	public RESTConfiguration(Map<String, String> configurationMap,
			Map<String, String> headersMap) {
		this(configurationMap);
		this.headersMap = (headersMap == null) ? Collections
				.<String, String> emptyMap() : headersMap;
	}

	/**
	 * @param authorizationToken
	 *            the authorizationToken to set
	 */
	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}

	/**
	 * @param resourcePath
	 *            the resourcePath to set
	 */
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @param payLoad
	 *            the payLoad to set
	 */
	public void setPayLoad(String payLoad) {
		this.payLoad = payLoad;
	}

	/**
	 * Returns HTTP headers as a {@link Map}
	 * 
	 * @return {@link Map} of Http headers
	 */
	public Map<String, String> getHeaderMap() {
		return getProcessedHeaderMap();
	}

	/**
	 * Returns the base URL configured in application resources
	 * 
	 * @return Base {@link URL}
	 * @throws MalformedURLException
	 */
	public URL getBaseURL() throws MalformedURLException {
		if (url == null) {
			String urlString = this.configurationMap.get(Constants.ENDPOINT);
			if (urlString == null || urlString.length() <= 0) {
				String mode = this.configurationMap.get(Constants.MODE);
				if (Constants.SANDBOX.equalsIgnoreCase(mode)) {
					urlString = Constants.REST_SANDBOX_ENDPOINT;
				} else if (Constants.LIVE.equalsIgnoreCase(mode)) {
					urlString = Constants.REST_LIVE_ENDPOINT;
				} else {
					throw new MalformedURLException(
							"service.EndPoint not set (OR) mode not configured to sandbox/live ");
				}
			}
			if (!urlString.endsWith("/")) {
				urlString += "/";
			}
			url = new URL(urlString);
		}
		return url;
	}

	/**
	 * @param urlString
	 *            the url to set
	 */
	public void setUrl(String urlString) throws MalformedURLException {
		if (urlString != null && urlString.length() > 0) {
			String uString = urlString.endsWith("/") ? urlString : urlString
					+ "/";
			this.url = new URL(uString);
		} else {
			this.url = getBaseURL();
		}
	}

	/**
	 * Returns User-Agent header
	 * 
	 * @return
	 */
	protected Map<String, String> formUserAgentHeader() {
		UserAgentHeader userAgentHeader = new UserAgentHeader(
				PayPalResource.SDK_ID, PayPalResource.SDK_VERSION);
		return userAgentHeader.getHeader();
	}

	/*
	 * Return Client ID from configuration Map
	 */
	private String getClientID() {
		return this.configurationMap.get(Constants.CLIENT_ID);
	}

	/*
	 * Returns Client Secret from configuration Map
	 */
	private String getClientSecret() {
		return this.configurationMap.get(Constants.CLIENT_SECRET);
	}

	/*
	 * Encodes Client ID and Client Secret in Base 64
	 */
	private String encodeToBase64(String clientID, String clientSecret)
			throws UnsupportedEncodingException {
		String base64ClientID = generateBase64String(clientID + ":"
				+ clientSecret);
		return base64ClientID;
	}

	/*
	 * Generate a Base64 encoded String from clientID & clientSecret
	 */
	private String generateBase64String(String clientID)
			throws UnsupportedEncodingException {
		String base64ClientID = null;
		byte[] encoded = null;
		encoded = Base64.encodeBase64(clientID.getBytes("UTF-8"));
		base64ClientID = new String(encoded, "UTF-8");
		return base64ClientID;
	}

	public String getPayLoad() {
		return getProcessedPayLoad();
	}

	public String getEndPoint() {
		return getProcessedEndPoint();
	}

	public ICredential getCredential() {
		return null;
	}

	public void validate() throws ClientActionRequiredException {
		// TODO
	}

	protected String getProcessedEndPoint() {
		String endPoint = null;
		try {
			endPoint = getBaseURL().toURI().resolve(resourcePath).toString();
		} catch (MalformedURLException e) {
			//
		} catch (URISyntaxException e) {
			//
		}
		return endPoint;
	}

	protected Map<String, String> getProcessedHeaderMap() {
		Map<String, String> headers = new HashMap<String, String>();
		if (authorizationToken != null
				&& authorizationToken.trim().length() > 0) {
			headers.put(Constants.AUTHORIZATION_HEADER, authorizationToken);
		} else if (getClientID() != null && getClientID().trim().length() > 0
				&& getClientSecret() != null
				&& getClientSecret().trim().length() > 0) {
			try {
				headers.put(Constants.AUTHORIZATION_HEADER, "Basic "
						+ encodeToBase64(getClientID(), getClientSecret()));
			} catch (UnsupportedEncodingException e) {
				// TODO
			}
		}
		if (requestId != null && requestId.length() > 0) {
			headers.put(Constants.PAYPAL_REQUEST_ID_HEADER, requestId);
		}
		headers.putAll(formUserAgentHeader());
		
		// Add any custom headers
		if (headersMap != null && headersMap.size() > 0) {
			headers.putAll(headersMap);
		}
		return headers;
	}

	protected String getProcessedPayLoad() {
		return payLoad;
	}

}
