package com.paypal.core;

import java.net.MalformedURLException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.exception.SSLConfigurationException;

public class DefaultHttpConnectionTest {

	DefaultHttpConnection defaultHttpConnection;
	HttpConfiguration httpConfiguration;

	@BeforeClass
	public void beforeClass() {
		defaultHttpConnection = new DefaultHttpConnection();
		httpConfiguration = new HttpConfiguration();
	}

	@AfterClass
	public void afterClass() {
		defaultHttpConnection = null;
		httpConfiguration = null;
	}

	@Test(expectedExceptions = MalformedURLException.class)
	public void checkMalformedURLExceptionTest() throws Exception {
		httpConfiguration.setEndPointUrl("ww.paypal.in");
		defaultHttpConnection
				.createAndconfigureHttpConnection(httpConfiguration);
	}

	@Test(expectedExceptions = SSLConfigurationException.class)
	public void checkSSLConfigurationExceptionTest()
			throws SSLConfigurationException {
		defaultHttpConnection.setupClientSSL("certPath", "certKey");
	}

}
