package com.paypal.core.credential;

/**
 * TokenAuthorization encapsulates third party token authorization.
 *
 */
public class TokenAuthorization implements ThirdPartyAuthorization {

	/**
	 * Access token
	 */
	private String accessToken;

	/**
	 * Token secret
	 */
	private String tokenSecret;

	/**
	 * @param accessToken
	 * @param tokenSecret
	 */
	public TokenAuthorization(String accessToken, String tokenSecret) {
		super();
		this.accessToken = accessToken;
		this.tokenSecret = tokenSecret;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @return the tokenSecret
	 */
	public String getTokenSecret() {
		return tokenSecret;
	}

}
