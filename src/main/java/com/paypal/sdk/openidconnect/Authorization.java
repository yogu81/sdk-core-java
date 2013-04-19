package com.paypal.sdk.openidconnect;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paypal.core.ConfigManager;
import com.paypal.core.Constants;

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
	 */
	public static String getRedirectURL(String redirectURI, List<String> scope,
			Map<String, String> configurationMap) {
		String redirectURL = null;
		try {
			if (configurationMap == null) {
				configurationMap = ConfigManager.getInstance()
						.getConfigurationMap();
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
			for (int i = 0; i < scope.size(); i++) {
				String s = scope.get(i);
				scpBuilder.append(s);
				if (i != (scope.size() - 1)) {
					scpBuilder.append(" ");
				}
			}
			stringBuilder.append(URLEncoder.encode(scpBuilder.toString(),
					"UTF-8"));
			stringBuilder.append("&redirect_uri").append(
					URLEncoder.encode(redirectURI, "UTF-8"));
			redirectURL = baseURL + "?" + stringBuilder.toString();
		} catch (UnsupportedEncodingException exe) {

		}
		return redirectURL;
	}

	public static void main(String[] args) {
		Map<String, String> m = new HashMap<String, String>();
		m.put("openid.RedirectUri",
				"https://www.paypal.com/webapps/auth/protocol/openidconnect/v1/authorize");
		m.put("clientId", "ANdfsalkoiarT");
		List<String> l = new ArrayList<String>();
		l.add("openid");
		l.add("profile");
		System.out.println(getRedirectURL("http://google.com", l, m));
	}

}
