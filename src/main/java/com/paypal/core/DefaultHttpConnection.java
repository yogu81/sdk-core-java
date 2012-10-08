package com.paypal.core;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import com.paypal.exception.SSLConfigurationException;

/**
 * Wrapper class used for HttpsURLConnection
 * 
 */
public class DefaultHttpConnection extends HttpConnection {

	/**
	 * Secure Socket Layer context
	 */
	private SSLContext sslContext;

	@Override
	public void setupClientSSL(String certPath, String certKey)
			throws SSLConfigurationException {
		try {
			if (isDefaultSSL()) {
				// Get Default SSL Context
				this.sslContext = SSLUtil.getSSLContext(null);
			} else {
				this.sslContext = SSLUtil.setupClientSSL(certPath, certKey);
			}
		} catch (Exception e) {
			throw new SSLConfigurationException(e.getMessage(), e);
		}
	}

	@Override
	public void createAndconfigureHttpConnection(
			HttpConfiguration clientConfiguration) throws IOException {
		this.config = clientConfiguration;
		URL url = new URL(this.config.getEndPointUrl());
		Proxy proxy = null;
		String proxyHost = this.config.getProxyHost();
		int proxyPort = this.config.getProxyPort();
		if ((proxyHost != null) && (proxyPort > 0)) {
			SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
			proxy = new Proxy(Proxy.Type.HTTP, addr);
		}
		if (proxy != null) {
			this.connection = (HttpsURLConnection) url.openConnection(proxy);
		} else {
			this.connection = (HttpsURLConnection) url
					.openConnection(Proxy.NO_PROXY);
		}
		if (this.connection instanceof HttpsURLConnection) {
			((HttpsURLConnection) this.connection)
					.setSSLSocketFactory(this.sslContext.getSocketFactory());
			if (isDefaultSSL()) {
				((HttpsURLConnection) this.connection).setHostnameVerifier(hv);
			}
		}
		if (this.config.getProxyUserName() != null
				&& this.config.getProxyPassword() != null) {
			final String username = this.config.getProxyUserName();
			final String password = this.config.getProxyPassword();
			Authenticator authenticator = new DefaultPasswordAuthenticator(
					username, password);
			Authenticator.setDefault(authenticator);
		}

		System.setProperty("http.maxConnections",
				String.valueOf(this.config.getMaxHttpConnection()));
		System.setProperty("sun.net.http.errorstream.enableBuffering", "true");
		this.connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		this.connection.setDoInput(true);
		this.connection.setDoOutput(true);
		this.connection.setRequestMethod("POST");
		this.connection.setConnectTimeout(this.config.getConnectionTimeout());
		this.connection.setReadTimeout(this.config.getReadTimeout());
	}

	/**
	 * Class used to relax host name verification.
	 */
	private HostnameVerifier hv = new DefaultHostNameVerifier();

	/**
	 * Private class used for relaxing all host name verification
	 * 
	 * @author kjayakumar
	 * 
	 */
	private static class DefaultHostNameVerifier implements HostnameVerifier {
		public boolean verify(String urlHostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * Private class for password based authentication
	 * 
	 * @author kjayakumar
	 * 
	 */
	private static class DefaultPasswordAuthenticator extends Authenticator {

		/**
		 * Username
		 */
		private String userName;

		/**
		 * Password
		 */
		private String password;

		public DefaultPasswordAuthenticator(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return (new PasswordAuthentication(userName, password.toCharArray()));
		}
	}

}