package com.paypal.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.DataProvider;

public class DataProviderClass {
	static ConfigManager conf;

	@DataProvider(name = "configParams")
	public static Object[][] configParams() throws FileNotFoundException,
			IOException {
			conf = ConfigManager.getInstance();
			InputStream in = DataProviderClass.class
					.getResourceAsStream("/sdk_config.properties");
			conf.load(in);
		return new Object[][] { new Object[] { conf } };

	}

	@DataProvider(name = "configParamsForSoap")
	public static Object[][] configParamsForSoap()
			throws FileNotFoundException, IOException {
			conf = ConfigManager.getInstance();
			InputStream in = DataProviderClass.class
					.getResourceAsStream("/sdk_config_soap.properties");
			conf.load(in);
		return new Object[][] { new Object[] { conf } };

	}
	// @DataProvider(name = "headers")
	// public static Object[][] getPayPalHeaders()
	// throws InvalidCredentialException, IOException,
	// MissingCredentialException, SSLConfigurationException,
	// OAuthException {
	// AuthenticationService auth = new AuthenticationService();
	// ConnectionManager connectionMgr = ConnectionManager.getInstance();
	// HttpConnection connection = connectionMgr.getConnection();
	// HttpConfiguration httpConfiguration = new HttpConfiguration();
	// httpConfiguration.setEndPointUrl(UnitTestConstants.API_ENDPOINT);
	// Map<String, String> map = auth.getPayPalHeaders(
	// UnitTestConstants.API_USER_NAME, connection, null, null,
	// httpConfiguration);
	// return new Object[][] { new Object[] { map } };
	//
	// }
}
