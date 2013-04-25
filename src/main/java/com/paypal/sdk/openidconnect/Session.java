package com.paypal.sdk.openidconnect;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paypal.core.ConfigManager;
import com.paypal.core.Constants;
import com.paypal.core.SDKUtil;
import com.paypal.core.rest.APIContext;

public class Session {

	/**
	 * Returns the PayPal URL to which the user must be redirected to start the
	 * authentication / authorization process.
	 * 
	 * @param redirectURI
	 *            Uri on merchant website to where the user must be redirected
	 *            to post paypal login
	 * 
	 * @param scope
	 *            The access privilges that you are requesting for from the
	 *            user. Pass empty array for all scopes. See
	 *            https://developer.paypal
	 *            .com/webapps/developer/docs/classic/loginwithpaypal
	 *            /ht_OpenIDConnect/#parameters for more information
	 * 
	 * @param apiContext
	 *            {@link APIContext} to be used for the call.
	 * @return Redirect URL
	 */
	public static String getRedirectURL(String redirectURI, List<String> scope,
			APIContext apiContext) {
		String redirectURL = null;
		Map<String, String> configurationMap = null;
		try {
			if (apiContext.getConfigurationMap() == null) {
				configurationMap = SDKUtil.combineDefaultMap(ConfigManager
						.getInstance().getConfigurationMap());
			} else {
				configurationMap = SDKUtil.combineDefaultMap(apiContext
						.getConfigurationMap());
			}
			String baseURL = configurationMap
					.get(Constants.OPENID_REDIRECT_URI);
			if (baseURL == null || baseURL.trim().length() <= 0) {
				baseURL = Constants.OPENID_REDIRECT_URI_CONSTANT;
			}
			if (scope == null || scope.size() <= 0) {
				scope = new ArrayList<String>();
				scope.add("openid");
				scope.add("profile");
				scope.add("address");
				scope.add("email");
				scope.add("phone");
				scope.add("https://uri.paypal.com/services/paypalattributes");
			}
			if (!scope.contains("openid")) {
				scope.add("openid");
			}
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder
					.append("client_id=")
					.append(URLEncoder.encode(
							configurationMap.get("clientId") != null ? configurationMap
									.get("clientId") : "", "UTF-8"))
					.append("&response_type=").append("code").append("&scope=");
			StringBuilder scpBuilder = new StringBuilder();
			for (String str : scope) {
				scpBuilder.append(str).append(" ");

			}
			stringBuilder.append(URLEncoder.encode(scpBuilder.toString(),
					"UTF-8"));
			stringBuilder.append("&redirect_uri=").append(
					URLEncoder.encode(redirectURI, "UTF-8"));
			redirectURL = baseURL + "/v1/authorize?" + stringBuilder.toString();
		} catch (UnsupportedEncodingException exe) {
			// Ignore
		}
		return redirectURL;
	}

	/**
	 * Returns the URL to which the user must be redirected to logout from the
	 * OpenID provider (i.e. PayPal)
	 * 
	 * @param redirectURI
	 *            URI on merchant website to where the user must be redirected
	 *            to post logout
	 * @param idToken
	 *            id_token from the TokenInfo object
	 * @param apiContext
	 *            {@link APIContext} to be used for the call.
	 * @return Logout URL
	 */
	public static String getLogoutUrl(String redirectURI, String idToken,
			APIContext apiContext) {
		String logoutURL = null;
		Map<String, String> configurationMap = null;
		try {
			if (apiContext.getConfigurationMap() == null) {
				configurationMap = SDKUtil.combineDefaultMap(ConfigManager
						.getInstance().getConfigurationMap());
			} else {
				configurationMap = SDKUtil.combineDefaultMap(apiContext
						.getConfigurationMap());
			}
			String baseURL = configurationMap
					.get(Constants.OPENID_REDIRECT_URI);
			if (baseURL == null || baseURL.trim().length() <= 0) {
				baseURL = Constants.OPENID_REDIRECT_URI_CONSTANT;
			}
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("id_token=")
					.append(URLEncoder.encode(idToken, "UTF-8"))
					.append("&redirect_uri=")
					.append(URLEncoder.encode(redirectURI, "UTF-8"))
					.append("&logout=true");
			logoutURL = baseURL + "/v1/endsession?" + stringBuilder.toString();
		} catch (UnsupportedEncodingException exe) {
			// Ignore
		}
		return logoutURL;
	}

}
