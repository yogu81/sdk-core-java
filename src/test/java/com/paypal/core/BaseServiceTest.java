package com.paypal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.paypal.core.nvp.PlatformAPICallPreHandler;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

public class BaseServiceTest extends BaseService {
	BaseService service;
	String incorrectFilePath = "src/test/resources/config.properties";
	String correctFilePath = "src/test/resources/sdk_config.properties";

	@Test
	public void setAndGetLastRequestTest() {
		this.setLastRequest("lastRequest");
		Assert.assertEquals("lastRequest", this.getLastRequest());
	}

	@Test
	public void setAndGetLastResponseTest() {
		this.setLastResponse("lastResponse");
		Assert.assertEquals("lastResponse", this.getLastResponse());
	}

	@Test
	public void initConfigTestUsingFilePathTest() throws IOException {
		BaseService.initConfig(correctFilePath);
	}

	@Test
	public void initConfigTestUsingFileTest() throws IOException {
		File file = new File(correctFilePath);
		BaseService.initConfig(file);
	}

	@Test
	public void initConfigTestUsingInputStreamTest() throws IOException {
		InputStream is = new FileInputStream(new File(correctFilePath));
		BaseService.initConfig(is);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingFilePathForExceptionTest() throws Exception {
		BaseService.initConfig(incorrectFilePath);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingFileForExceptionTest() throws Exception {
		File file = new File(incorrectFilePath);
		BaseService.initConfig(file);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingInputStreamForExceptionTest()
			throws Exception {
		InputStream is = new FileInputStream(new File(incorrectFilePath));
		BaseService.initConfig(is);
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void callTest(ConfigManager conf) throws InvalidCredentialException,
			MissingCredentialException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				UnitTestConstants.API_USER_NAME, null, null);
		String response = this.call(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
	}

}
