package com.paypal.ipn;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.paypal.core.ConfigManager;
import com.paypal.core.ConnectionManager;
import com.paypal.core.Constants;
import com.paypal.core.HttpConfiguration;
import com.paypal.core.HttpConnection;
import com.paypal.core.LoggingManager;

public class IPNMessage {

	private static final long serialVersionUID = -7187275404183441828L;
	private static final String ENCODING = "windows-1252";
	
	private Map<String, String> ipnMap = new HashMap<String, String>();
	private ConfigManager config = ConfigManager.getInstance();
	private HttpConfiguration httpConfiguration = null;
	private String ipnEndpoint = Constants.EMPTY_STRING;
	private boolean isIpnVerified = false;
	private StringBuffer payload;
	/**
	 * Populates HttpConfiguration with connection specifics parameters
	 */
	{
		httpConfiguration = new HttpConfiguration();
		config = ConfigManager.getInstance();
		ipnEndpoint = config.getValue(Constants.IPN_ENDPOINT);
		httpConfiguration.setEndPointUrl(ipnEndpoint);
		httpConfiguration.setConnectionTimeout(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_TIMEOUT)));
		httpConfiguration.setMaxRetry(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_RETRY)));
		httpConfiguration.setReadTimeout(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_READ_TIMEOUT)));
		httpConfiguration.setMaxHttpConnection(Integer.parseInt(config
				.getValue(Constants.HTTP_CONNECTION_MAX_CONNECTION)));
	}

	/**
	 * @param ipnMap  representing IPN name/value pair
	 */
	public IPNMessage(Map<String, String[]> ipnMap) {
		payload = new StringBuffer("cmd=_notify-validate");
		if (ipnMap != null) {
			for (Map.Entry<String, String[]> entry : ipnMap.entrySet()) {
				String name = entry.getKey();
				String[] value = entry.getValue();
				try{
					this.ipnMap.put(URLDecoder.decode(name,ENCODING), URLDecoder.decode(value[0], ENCODING));
					payload.append("&").append(name).append("=").append(URLEncoder.encode(value[0], ENCODING));
				}catch(Exception e){
					LoggingManager.debug(IPNMessage.class, e.getMessage());
				}
			}
		}

	}

	/**
	 * @param HttpServletrequest received from PayPal IPN call back.
	 */
	public IPNMessage(HttpServletRequest request) {
		this(request.getParameterMap());
	}

	/**
	 * This method post back ipn payload to PayPal system for verification
	 */
	public boolean validate() {
		Map<String, String> headerMap = new HashMap<String, String>();
		URL url = null;
		String res = Constants.EMPTY_STRING;
		HttpConnection connection = ConnectionManager.getInstance().getConnection();
		
		try {
			
			connection.createAndconfigureHttpConnection(httpConfiguration);
			url = new URL(this.ipnEndpoint);
			headerMap.put("Host", url.getHost());
			res = Constants.EMPTY_STRING;
			if (!this.isIpnVerified) {
				res = connection.execute(null, payload.toString(), headerMap);
			}

		} catch (Exception e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		}

		// check notification validation
		if (res.equals("VERIFIED")) {
			isIpnVerified = true;
		}

		return isIpnVerified;
	}

	/**
	 * @return Map of IPN name/value parameters
	 */
	public Map<String, String> getIpnMap() {
		return ipnMap;
	}

	/**
	 * @param ipnName
	 * @return IPN value for corresponding IpnName
	 */
	public String getIpnValue(String ipnName) {

		return this.ipnMap.get(ipnName);

	}

	/**
	 * @return Transaction Type (eg: express_checkout, cart, web_accept)
	 */
	public String getTransactionType() {
		return this.ipnMap.containsKey("txn_type") ? this.ipnMap
				.get("txn_type") : this.ipnMap.get("transaction_type");
	}

}
