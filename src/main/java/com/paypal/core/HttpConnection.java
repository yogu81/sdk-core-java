package com.paypal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.SSLConfigurationException;

/**
 * Base HttpConnection class
 * 
 */
public abstract class HttpConnection {

	/**
	 * Subclasses must override the default value of true to use strict hostname
	 * verification
	 */
	protected boolean defaultSSL;

	/**
	 * Subclasses must set the http configuration in the
	 * createAndconfigureHttpConnection() method.
	 */
	protected HttpConfiguration config;

	/**
	 * Subclasses must create and set the connection in the
	 * createAndconfigureHttpConnection() method.
	 */
	protected HttpURLConnection connection;

	public HttpConnection() {
		defaultSSL = true;
	}

	/**
	 * Executes HTTP request
	 * 
	 * @param url
	 * @param payload
	 * @param headers
	 * @return String response
	 * @throws InvalidResponseDataException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws HttpErrorException
	 * @throws ClientActionRequiredException
	 */
	public String execute(String url, String payload,
			Map<String, String> headers) throws InvalidResponseDataException,
			IOException, InterruptedException, HttpErrorException,
			ClientActionRequiredException {

		String successResponse = Constants.EMPTY_STRING;
		String errorResponse = Constants.EMPTY_STRING;
		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		connection.setRequestProperty("Content-Length", ""
				+ payload.trim().length());
		if (headers != null) {
			setHttpHeaders(headers);
		}
		try {
			
			// Changed retry logic from for loop to do while (1-29-2013)
			int retry = 0;
			do {
				try {
					if ("POST".equalsIgnoreCase(connection.getRequestMethod())) {
						writer = new OutputStreamWriter(
								this.connection.getOutputStream(),
								Charset.forName(Constants.ENCODING_FORMAT));
						writer.write(payload);
						writer.flush();
					}
					int responsecode = connection.getResponseCode();
					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream(),
							Constants.ENCODING_FORMAT));
					if (responsecode >= 200 && responsecode < 300) {
						successResponse = read(reader);
						LoggingManager.debug(HttpConnection.class,
								"Response : " + successResponse);
						if (successResponse.trim().length() <= 0) {
							throw new InvalidResponseDataException(
									successResponse);
						}
						break;
					} else {
						successResponse = read(reader);
						throw new ClientActionRequiredException(
								"Response Code : " + responsecode
										+ " with response : " + successResponse);
					}
				} catch (IOException e) {
					int responsecode = connection.getResponseCode();
					if (connection.getErrorStream() != null) {
						reader = new BufferedReader(new InputStreamReader(
								connection.getErrorStream(),
								Constants.ENCODING_FORMAT));
						errorResponse = read(reader);
						LoggingManager.severe(HttpConnection.class,
								"Error code : " + responsecode
										+ " with response : " + errorResponse);
					} else if (connection.getInputStream() != null) {
						
						// Added logic to read from InputStream 
						// HTTPError code 500 are sent (?) in InputStream
						//(1-29-2013)
						reader = new BufferedReader(new InputStreamReader(
								connection.getInputStream(),
								Constants.ENCODING_FORMAT));
						errorResponse = read(reader);
						LoggingManager.severe(HttpConnection.class,
								"Error code : " + responsecode
										+ " with response : " + errorResponse);
					}
					
					// Throw HttpErrorException for HTTPError code 500
					// (1-29-2013)
					if (responsecode <= 500) {
						throw new HttpErrorException("Error code : "
								+ responsecode + " with response : "
								+ errorResponse);
					}
				}
				
				// Retry logic
				retry ++;
				if (retry > 0) {
					LoggingManager.debug(HttpConnection.class, " Retry  No : "
							+ retry + "...");
					Thread.sleep(this.config.getRetryDelay());
				}
			} while (retry < this.config.getMaxRetry());
			if (successResponse.trim().length() <= 0) {
				throw new HttpErrorException(
						"retry fails..  check log for more information");
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} finally {
				reader = null;
				writer = null;
			}
		}
		return successResponse;
	}

	/**
	 * Set ssl parameters for client authentication
	 * 
	 * @param certPath
	 * @param certKey
	 * @throws SSLConfigurationException
	 */
	public abstract void setupClientSSL(String certPath, String certKey)
			throws SSLConfigurationException;

	/**
	 * create and configure HttpsURLConnection object
	 * 
	 * @param clientConfiguration
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public abstract void createAndconfigureHttpConnection(
			HttpConfiguration clientConfiguration) throws IOException;

	protected String read(BufferedReader reader) throws IOException {
		String inputLine = Constants.EMPTY_STRING;
		StringBuilder response = new StringBuilder();
		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		return response.toString();
	}

	/**
	 * Set headers for HttpsURLConnection object
	 * 
	 * @param headers
	 */
	protected void setHttpHeaders(Map<String, String> headers) {
		Iterator<Map.Entry<String, String>> itr = headers.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> pairs = itr.next();
			String key = pairs.getKey();
			String value = pairs.getValue();
			this.connection.addRequestProperty(key, value);
		}
	}

}