package com.paypal.sdk.openidconnect;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paypal.core.ConfigManager;

public class Authorization {

	/**
	 * Get the PayPal URL to which the user must be redirected to start the
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
	 * @param configurationMap
	 *            dynamic configuration map
	 * @return Redirect URL
	 * @throws UnsupportedEncodingException
	 */
	public static String getRedirectURL(String redirectURI, List<String> scope,
			Map<String, String> configurationMap)
			throws UnsupportedEncodingException {
		String redirectURL = null;
		if (configurationMap == null) {
			configurationMap = ConfigManager.getInstance()
					.getConfigurationMap();
		}
		String baseURL = configurationMap.get("openid.RedirectUri");
		if (baseURL == null || baseURL.trim().length() <= 0) {
			baseURL = "...";
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
		stringBuilder.append(baseURL).append("/v1/authorize?")
				.append("client_id=").append(configurationMap.get("clientId"))
				.append("&response_type").append("code").append("&scope=");
		for (int i = 0; i < scope.size(); i++) {
			String s = scope.get(0);
			stringBuilder.append(s);
			if (i != (scope.size() - 1)) {
				stringBuilder.append(" ");
			}
		}
		stringBuilder.append("&redirect_uri").append(redirectURI);
		redirectURL = URLEncoder.encode(stringBuilder.toString(), "UTF-8");
		return redirectURL;
	}

}
