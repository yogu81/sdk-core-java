package com.paypal.ipn;

import java.io.IOException;
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
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidResponseDataException;

public class IPNMessage {
	
	private static final long serialVersionUID = -7187275404183441828L;
	private Map<String,String> ipnMap =  new HashMap<String,String>();
	private ConfigManager config = ConfigManager.getInstance();
	private HttpConfiguration httpConfiguration = null;
	private String ipnEndpoint = Constants.EMPTY_STRING;
	private boolean isIpnVerified = false;
	
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
	 * @param ipnParamValueMap representing IPN name/value pair
	 * @throws IOException
	 */
	public IPNMessage(Map<String,String> ipnParamValueMap) throws IOException{
		StringBuffer payload = new StringBuffer("cmd=_notify-validate");
		for(Map.Entry<String,String> entry : ipnParamValueMap.entrySet()){
			String name = entry.getKey();
			String value = entry.getValue();
			this.ipnMap.put(name, value);
			payload.append("&").append(name).append("=")
			.append(URLEncoder.encode(value));
		}
		processRequest(payload.toString());
	}
	
	/**
	 * @param HttpServletrequest from PayPal IPN call back.
	 * @throws IOException
	 */
	public IPNMessage(HttpServletRequest request)throws IOException{
		/* Read post from PayPal system and add 'cmd' */
		Map<String,String[]> map = request.getParameterMap();
		StringBuffer payload = new StringBuffer("cmd=_notify-validate");
		for(Map.Entry<String,String[]> entry : map.entrySet()){
			String name = entry.getKey();
			String[] value = entry.getValue();
			this.ipnMap.put(name, value[0]);
			payload.append("&").append(name).append("=")
			.append(URLEncoder.encode(value[0]));
		}
		processRequest(payload.toString());
	}
	
	/**
	 * This method post back ipn payload to PayPal system for verification
	 * @param payload
	 * @throws IOException
	 */
	private void processRequest(String payload) throws IOException{
		
		HttpConnection connection = ConnectionManager.getInstance().getConnection();
		connection.createAndconfigureHttpConnection(httpConfiguration);

		Map<String,String> headerMap = new HashMap<String,String>();
		if(ipnEndpoint.indexOf("sandbox")>0){
			headerMap.put("Host", "ipnpb.sandbox.paypal.com");
		}else{
			headerMap.put("Host", "ipnpb.paypal.com");
		}
		
		String res = Constants.EMPTY_STRING;
		
		try {
			
			res = connection.execute(null, payload, headerMap);
			
		} catch (InvalidResponseDataException e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		} catch (HttpErrorException e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		} catch (ClientActionRequiredException e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		} catch (IOException e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		} catch (InterruptedException e) {
			LoggingManager.debug(IPNMessage.class, e.getMessage());
		}
		
		// check notification validation
		if (res.equals("VERIFIED")) {
			isIpnVerified = true;
		} 
		
	}
	
	
	/**
	 * @return Map of IPN  name/value parameters 
	 */
	public Map<String, String> getIpnParamValueMap() {
		return ipnMap;
	}
	
	/**
	 * @param ipnName
	 * @return IPN value for corresponding IpnName
	 */
	public String getIpnParamValue(String ipnName){
		
		return this.ipnMap.get(ipnName)!=null? this.ipnMap.get(ipnName) : null;
	
	}
	
	
	/**
	 * @return IPN verification result in boolean
	 */
	public boolean isIpnVerified() {
		return isIpnVerified;
	}

	/**
	 * @return Transaction Type (eg: express_checkout, cart, web_accept)
	 */
	public String getTransactionType(){
		return this.ipnMap.get("txn_type") != null? this.ipnMap.get("txn_type") : 
			(this.ipnMap.get("transaction_type")!=null? this.ipnMap.get("transaction_type"):null);
	}
	
}
