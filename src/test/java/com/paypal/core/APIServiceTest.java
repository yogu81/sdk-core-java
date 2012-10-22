package com.paypal.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.core.nvp.PlatformAPICallPreHandler;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

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

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void makeRequestUsingForNVPSignatureCredentialTest(ConfigManager conf)
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				UnitTestConstants.API_USER_NAME, null, null);
		String response = service.makeRequestUsing(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
	}

//	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
//	public void makeRequestUsingForSOAPSignatureCredentialTest(
//			ConfigManager conf) throws InvalidCredentialException,
//			MissingCredentialException, InvalidResponseDataException,
//			HttpErrorException, ClientActionRequiredException, OAuthException,
//			SSLConfigurationException, IOException, InterruptedException {
//		DefaultSOAPAPICallHandler apiCallHandler = new DefaultSOAPAPICallHandler(
//				"", null, null);
//		APICallPreHandler handler = new SOAPAPICallPreHandler(apiCallHandler,
//				UnitTestConstants.API_USER_NAME, null, null);
//		String response = service.makeRequestUsing(handler);
//		Assert.assertNotNull(response);
//		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
//	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
