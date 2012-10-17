package com.paypal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.core.nvp.NVPAPICallPreHandler;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.SSLConfigurationException;

public class APIServiceTest {
	APIService service;
	HttpConnection connection;

	Map<String, String> map = new HashMap<String, String>();

	@BeforeClass
	public void beforeClass() throws NumberFormatException,
			SSLConfigurationException, FileNotFoundException, IOException {
		ConfigManager.getInstance().load(
				this.getClass().getResourceAsStream("/sdk_config.properties"));
		service = new APIService();
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void getEndPointTest(ConfigManager conf) {
		Assert.assertEquals(UnitTestConstants.API_ENDPOINT,
				service.getEndPoint());
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
