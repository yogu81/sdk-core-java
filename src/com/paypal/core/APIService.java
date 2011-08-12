package com.paypal.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.sdk.exceptions.OAuthException;

/**
 * Wrapper class for api calls
 * 
 */
public class APIService {

	private String serviceName;
	private String endPoint;
	private ConfigManager config = null;
	private HttpConfiguration httpConfiguration = new HttpConfiguration();
	private Map<String, String> headers = new HashMap<String, String>();

	/**
	 * Set all the http related parameters from the config file
	 * 
	 * @param serviceName
	 * @throws SSLConfigurationException
	 * @throws NumberFormatException
	 */
	public APIService(String serviceName) throws NumberFormatException {
		this.serviceName = serviceName;
		config = ConfigManager.getInstance();
		endPoint = config.getValue("service.EndPoint");
		httpConfiguration.setTrustAll(Boolean.parseBoolean(config
				.getValue("http.TrustAllConnection")));

		try {
			if (Boolean.parseBoolean(config.getValue("http.UseProxy"))) {
				httpConfiguration.setProxyPort(Integer.parseInt(config
						.getValue("http.ProxyPort")));
				httpConfiguration.setProxyHost(config
						.getValue("http.ProxyHost"));
				httpConfiguration.setProxyUserName(config
						.getValue("http.ProxyUserName"));
				httpConfiguration.setProxyPassword(config
						.getValue("http.ProxyPassword"));
			}
			httpConfiguration.setConnectionTimeout(Integer.parseInt(config
					.getValue("http.ConnectionTimeOut")));
			httpConfiguration.setMaxRetry(Integer.parseInt(config
					.getValue("http.Retry")));
			httpConfiguration.setReadTimeout(Integer.parseInt(config
					.getValue("http.ReadTimeOut")));
			httpConfiguration.setMaxHttpConnection(Integer.parseInt(config
					.getValue("http.MaxConnection")));
		} catch (NumberFormatException nfe) {
			LoggingManager.debug(APIService.class, nfe.getMessage());
			throw nfe;
		}
	}

	/**
	 * makes a request to specified end point.
	 * 
	 * @param apiMethod
	 *            (WSDL API operation name that user wants to call)
	 * @param payload
	 *            (request parameters)
	 * @param apiUsername
	 *            (PayPal account)
	 * @param tokenSecret
	 * @param accessToken
	 * @return response String
	 * @throws HttpErrorException
	 * @throws InterruptedException
	 * @throws InvalidResponseDataException
	 * @throws ClientActionRequiredException
	 * @throws MissingCredentialException
	 * @throws SSLConfigurationException
	 * @throws InvalidCredentialException
	 * @throws IOException
	 * @throws OAuthException
	 */
	public String makeRequest(String apiMethod, String payload,
			String apiUsername, String accessToken, String tokenSecret)
			throws HttpErrorException, InterruptedException,
			InvalidResponseDataException, ClientActionRequiredException,
			MissingCredentialException, SSLConfigurationException,
			InvalidCredentialException, IOException, OAuthException {

		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		HttpConnection connection = connectionMgr.getConnection();
		String url = endPoint + serviceName + '/' + apiMethod;
		httpConfiguration.setEndPointUrl(url);
		AuthenticationService auth = new AuthenticationService();
		headers = auth.getPayPalHeaders(apiUsername, connection, accessToken,
				tokenSecret, httpConfiguration);
		/*
		 * connection.setDefaultSSL(true); connection.setupClientSSL(null, null,
		 * httpConfiguration.isTrustAll());
		 */
		try {
			connection.CreateAndconfigureHttpConnection(httpConfiguration);
		} catch (MalformedURLException me) {
			LoggingManager.severe(APIService.class, me.getMessage());
			throw me;
		} catch (IOException ioe) {
			LoggingManager.severe(APIService.class, ioe.getMessage());
			throw ioe;
		}

		LoggingManager.info(APIService.class, payload);
		String response = Constants.EMPTY_STRING;
		try {
			response = connection.execute(url, payload, headers);
		} catch (ClientActionRequiredException car) {
			LoggingManager.severe(APIService.class, car.getMessage());
			throw car;
		} catch (InterruptedException ie) {
			LoggingManager.severe(APIService.class, ie.getMessage());
			throw ie;
		} catch (InvalidResponseDataException inv) {
			LoggingManager.severe(APIService.class, inv.getMessage());
			throw inv;
		} catch (IOException ioe) {
			LoggingManager.severe(APIService.class, ioe.getMessage());
			throw ioe;
		} catch (HttpErrorException htr) {
			LoggingManager.severe(APIService.class, htr.getMessage());
			throw htr;
		}

		LoggingManager.info(APIService.class, response);
		return response;

	}

	/**
	 * get a map containing paypal headers and set SSLContext if it is a
	 * certificate authentication.
	 * 
	 * @param apiCred
	 * @param connection
	 * @return Map
	 * @throws SSLConfigurationException
	 */
	private Map<String, String> getPayPalHeaders(ICredential apiCred,
			HttpConnection connection) throws SSLConfigurationException {
		/* Add headers required for service authentication */
		if (apiCred instanceof SignatureCredential) {
			headers.put("X-PAYPAL-SECURITY-USERID",
					((SignatureCredential) apiCred).getUserName());
			headers.put("X-PAYPAL-SECURITY-PASSWORD",
					((SignatureCredential) apiCred).getPassword());
			headers.put("X-PAYPAL-SECURITY-SIGNATURE",
					((SignatureCredential) apiCred).getSignature());
			connection.setDefaultSSL(true);
			connection.setupClientSSL(null, null,
					this.httpConfiguration.isTrustAll());
		} else if (apiCred instanceof CertificateCredential) {
			connection.setDefaultSSL(false);
			headers.put("X-PAYPAL-SECURITY-USERID",
					((CertificateCredential) apiCred).getUserName());
			headers.put("X-PAYPAL-SECURITY-PASSWORD",
					((CertificateCredential) apiCred).getPassword());
			connection.setupClientSSL(
					((CertificateCredential) apiCred).getCertificatePath(),
					((CertificateCredential) apiCred).getCertificateKey(),
					this.httpConfiguration.isTrustAll());
		}

		/* Add other headers */
		headers.put("X-PAYPAL-APPLICATION-ID", apiCred.getApplicationId());
		headers.put("X-PAYPAL-REQUEST-DATA-FORMAT",
				config.getValue("service.Binding"));
		headers.put("X-PAYPAL-RESPONSE-DATA-FORMAT",
				config.getValue("service.Binding"));
		if (endPoint.contains("sandbox")) {
			headers.put("X-PAYPAL-SANDBOX-EMAIL-ADDRESS",
					"Platform.sdk.seller@gmail.com");
		}
		headers.put("X-PAYPAL-DEVICE-IPADDRESS", "127.0.0.1");
		return headers;

	}

	public String getServiceName() {
		return serviceName;
	}

	public String getEndPoint() {
		return endPoint;
	}

}
