package com.paypal.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.core.nvp.PlatformAPICallPreHandler;
import com.paypal.core.soap.MerchantAPICallPreHandler;
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

	Properties props;

	Map<String, String> cMap;

	Map<String, String> map = new HashMap<String, String>();

	@BeforeClass
	public void beforeClass() throws NumberFormatException,
			SSLConfigurationException, FileNotFoundException, IOException {
		props = new Properties();
		props.load(this.getClass()
				.getResourceAsStream("/sdk_config.properties"));
		cMap = SDKUtil.constructMap(props);
		service = new APIService(cMap);
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class, priority = 0)
	public void getEndPointTest(ConfigManager conf) {
		Assert.assertEquals(UnitTestConstants.API_ENDPOINT,
				service.getEndPoint());

	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class, priority = 1)
	public void makeRequestUsingForNVPSignatureCredentialTest(ConfigManager conf)
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		cMap.put("service.EndPoint", "https://svcs.sandbox.paypal.com/");
		service = new APIService(cMap);
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				UnitTestConstants.API_USER_NAME, null, null, null, null, null,
				cMap);
		String response = service.makeRequestUsing(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
	}

	@Test(dataProvider = "configParamsForSoap", dataProviderClass = DataProviderClass.class, priority = 3)
	public void makeRequestUsingForSOAPSignatureCredentialTest(
			ConfigManager conf) throws InvalidCredentialException,
			MissingCredentialException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		cMap.put("service.EndPoint", "https://api-3t.sandbox.paypal.com/2.0");
		service = new APIService(cMap);
		String payload = "<ns:GetBalanceReq><ns:GetBalanceRequest><ebl:Version>94.0</ebl:Version></ns:GetBalanceRequest></ns:GetBalanceReq>";
		DefaultSOAPAPICallHandler apiCallHandler = new DefaultSOAPAPICallHandler(
				payload, null, null, cMap);
		APICallPreHandler handler = new MerchantAPICallPreHandler(
				apiCallHandler, UnitTestConstants.API_USER_NAME, null, null,
				null, null, null, cMap);
		String response = service.makeRequestUsing(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response
				.contains("<Ack xmlns=\"urn:ebay:apis:eBLBaseComponents\">Success</Ack>"));
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class, priority = 2)
	public void makeRequestUsingForNVPCertificateCredentialTest(
			ConfigManager conf) throws InvalidCredentialException,
			MissingCredentialException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		service = new APIService(cMap);
		cMap.put("service.EndPoint", "https://svcs.sandbox.paypal.com/");
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				"certuser_biz_api1.paypal.com", null, null, null, null, null,
				cMap);
		String response = service.makeRequestUsing(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
	}

	@Test(dataProvider = "configParamsForProxy", dataProviderClass = DataProviderClass.class, priority = 4)
	public void proxyTest(ConfigManager conf) throws IOException {
		Assert.assertEquals(service.getEndPoint(),
				"https://api-3t.sandbox.paypal.com/2.0");
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
