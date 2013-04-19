package com.paypal.sdk.openidconnect;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import com.paypal.sdk.openidconnect.JSONFormatter;
import com.paypal.sdk.openidconnect.PayPalRESTException;
import com.paypal.sdk.openidconnect.PayPalResource;
import java.util.Map;
import java.util.HashMap;
import com.paypal.sdk.openidconnect.CreateFromAuthorizationCodeParameters;
import com.paypal.sdk.openidconnect.CreateFromRefreshTokenParameters;
import com.paypal.sdk.openidconnect.RESTUtil;
import com.paypal.sdk.openidconnect.HttpMethod;
import java.io.IOException;
import java.net.URISyntaxException;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidResponseDataException;

public class Tokeninfo {

	/**
	 * OPTIONAL, if identical to the scope requested by the client; otherwise,
	 * REQUIRED.
	 */
	private String scope;

	/**
	 * The access token issued by the authorization server.
	 */
	private String accessToken;

	/**
	 * The refresh token, which can be used to obtain new access tokens using
	 * the same authorization grant as described in OAuth2.0 RFC6749 in Section
	 * 6.
	 */
	private String refreshToken;

	/**
	 * The type of the token issued as described in OAuth2.0 RFC6749 (Section
	 * 7.1). Value is case insensitive.
	 */
	private String tokenType;

	/**
	 * The lifetime in seconds of the access token.
	 */
	private Integer expiresIn;

	/**
	 * Returns the last request sent to the Service
	 * 
	 * @return Last request sent to the server
	 */
	public static String getLastRequest() {
		return PayPalResource.getLastRequest();
	}

	/**
	 * Returns the last response returned by the Service
	 * 
	 * @return Last response got from the Service
	 */
	public static String getLastResponse() {
		return PayPalResource.getLastResponse();
	}

	/**
	 * Initialize using InputStream(of a Properties file)
	 * 
	 * @param is
	 *            InputStream
	 * @throws PayPalRESTException
	 */
	public static void initConfig(InputStream is) throws PayPalRESTException {
		PayPalResource.initConfig(is);
	}

	/**
	 * Initialize using a File(Properties file)
	 * 
	 * @param file
	 *            File object of a properties entity
	 * @throws PayPalRESTException
	 */
	public static void initConfig(File file) throws PayPalRESTException {
		PayPalResource.initConfig(file);
	}

	/**
	 * Initialize using Properties
	 * 
	 * @param properties
	 *            Properties object
	 */
	public static void initConfig(Properties properties) {
		PayPalResource.initConfig(properties);
	}

	/**
	 * Default Constructor
	 */
	public Tokeninfo() {
	}

	/**
	 * Parameterized Constructor
	 */
	public Tokeninfo(String accessToken, String tokenType, Integer expiresIn) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
	}

	/**
	 * Setter for scope
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * Getter for scope
	 */
	public String getScope() {
		return this.scope;
	}

	/**
	 * Setter for accessToken
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Getter for accessToken
	 */
	public String getAccessToken() {
		return this.accessToken;
	}

	/**
	 * Setter for refreshToken
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * Getter for refreshToken
	 */
	public String getRefreshToken() {
		return this.refreshToken;
	}

	/**
	 * Setter for tokenType
	 */
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	/**
	 * Getter for tokenType
	 */
	public String getTokenType() {
		return this.tokenType;
	}

	/**
	 * Setter for expiresIn
	 */
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}

	/**
	 * Getter for expiresIn
	 */
	public Integer getExpiresIn() {
		return this.expiresIn;
	}

	/**
	 * Creates an Access Token from an Authorization Code.
	 * 
	 * @param createFromAuthorizationCodeParameters
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Tokeninfo createFromAuthorizationCode(
			CreateFromAuthorizationCodeParameters createFromAuthorizationCodeParameters)
			throws PayPalRESTException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			URISyntaxException, IOException, InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&code={1}&redirect_uri={2}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(createFromAuthorizationCodeParameters
				.getContainerMap());
		paramsMap.put("grant_type", "authorization_code");
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(null, HttpMethod.POST,
				resourcePath, headersMap, payLoad, Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Authorization Code.
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 * @param createFromAuthorizationCodeParameters
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Tokeninfo createFromAuthorizationCode(
			Map<String, String> configurationMap,
			CreateFromAuthorizationCodeParameters createFromAuthorizationCodeParameters)
			throws PayPalRESTException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			URISyntaxException, IOException, InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&code={1}&redirect_uri={2}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(createFromAuthorizationCodeParameters
				.getContainerMap());
		paramsMap.put("grant_type", "authorization_code");
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(configurationMap,
				HttpMethod.POST, resourcePath, headersMap, payLoad,
				Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Authorization Code.
	 * 
	 * @param containerMap
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Tokeninfo createFromAuthorizationCode(
			Map<String, String> containerMap) throws PayPalRESTException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, URISyntaxException, IOException,
			InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&code={1}&redirect_uri={2}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(containerMap);
		paramsMap.put("grant_type", "authorization_code");
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(null, HttpMethod.POST,
				resourcePath, headersMap, payLoad, Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Authorization Code.
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 * @param containerMap
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Tokeninfo createFromAuthorizationCode(
			Map<String, String> configurationMap,
			Map<String, String> containerMap) throws PayPalRESTException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, URISyntaxException, IOException,
			InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&code={1}&redirect_uri={2}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(containerMap);
		paramsMap.put("grant_type", "authorization_code");
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(configurationMap,
				HttpMethod.POST, resourcePath, headersMap, payLoad,
				Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Refresh Token.
	 * 
	 * @param createFromRefreshTokenParameters
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Tokeninfo createFromRefreshToken(
			CreateFromRefreshTokenParameters createFromRefreshTokenParameters)
			throws PayPalRESTException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			URISyntaxException, IOException, InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&refresh_token={1}&scope={2}&client_id={3}&client_secret={4}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(createFromRefreshTokenParameters.getContainerMap());
		paramsMap.put("grant_type", "refresh_token");
		paramsMap.put("refresh_token", getRefreshToken());
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(null, HttpMethod.POST,
				resourcePath, headersMap, payLoad, Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Refresh Token.
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 * @param createFromRefreshTokenParameters
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Tokeninfo createFromRefreshToken(
			Map<String, String> configurationMap,
			CreateFromRefreshTokenParameters createFromRefreshTokenParameters)
			throws PayPalRESTException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			URISyntaxException, IOException, InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&refresh_token={1}&scope={2}&client_id={3}&client_secret={4}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(createFromRefreshTokenParameters.getContainerMap());
		paramsMap.put("grant_type", "refresh_token");
		paramsMap.put("refresh_token", getRefreshToken());
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(configurationMap,
				HttpMethod.POST, resourcePath, headersMap, payLoad,
				Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Refresh Token.
	 * 
	 * @param containerMap
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Tokeninfo createFromRefreshToken(Map<String, String> containerMap)
			throws PayPalRESTException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			URISyntaxException, IOException, InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&refresh_token={1}&scope={2}&client_id={3}&client_secret={4}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(containerMap);
		paramsMap.put("grant_type", "refresh_token");
		paramsMap.put("refresh_token", getRefreshToken());
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(null, HttpMethod.POST,
				resourcePath, headersMap, payLoad, Tokeninfo.class);
	}

	/**
	 * Creates an Access Token from an Refresh Token.
	 * 
	 * @param configurationMap
	 *            Map used for dynamic configuration
	 * @param containerMap
	 *            Query parameters used for API call
	 * @return Tokeninfo
	 * @throws PayPalRESTException
	 * @throws InvalidResponseDataException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Tokeninfo createFromRefreshToken(
			Map<String, String> configurationMap,
			Map<String, String> containerMap) throws PayPalRESTException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, URISyntaxException, IOException,
			InterruptedException {
		String pattern = "v1/identity/openidconnect/tokenservice ?grant_type={0}&refresh_token={1}&scope={2}&client_id={3}&client_secret={4}";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.putAll(containerMap);
		paramsMap.put("grant_type", "refresh_token");
		paramsMap.put("refresh_token", getRefreshToken());
		Object[] parameters = new Object[] { paramsMap };
		String resourcePath = RESTUtil.formatURIPath(pattern, parameters);
		String payLoad = resourcePath.substring(resourcePath.indexOf('?') + 1);
		resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		return PayPalResource.configureAndExecute(configurationMap,
				HttpMethod.POST, resourcePath, headersMap, payLoad,
				Tokeninfo.class);
	}

	/**
	 * Returns a JSON string corresponding to object state
	 * 
	 * @return JSON representation
	 */
	public String toJSON() {
		return JSONFormatter.toJSON(this);
	}

	@Override
	public String toString() {
		return toJSON();
	}
}