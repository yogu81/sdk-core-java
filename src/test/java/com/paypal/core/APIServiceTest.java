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

	@Test(priority = 0)
	public void getEndPointTest() {
		Assert.assertEquals(UnitTestConstants.API_ENDPOINT,
				service.getEndPoint());
	}

	@Test(priority = 1)
	public void makeRequestUsingForNVPSignatureCredentialTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		cMap.remove("service.EndPoint");
		cMap.put("mode", "sandbox");
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

	@Test(priority = 3)
	public void makeRequestUsingForSOAPSignatureCredentialTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
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

	@Test(priority = 2)
	public void makeRequestUsingForNVPCertificateCredentialTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		cMap.put("service.EndPoint", "https://svcs.sandbox.paypal.com/");
		service = new APIService(cMap);
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				"certuser_biz_api1.paypal.com", null, null, null, null, null,
				cMap);
		String response = service.makeRequestUsing(handler);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.contains("responseEnvelope.ack=Success"));
	}

	@Test(priority = 4)
	public void proxyTest() throws IOException {
		Assert.assertEquals(service.getEndPoint(),
				"https://api-3t.sandbox.paypal.com/2.0");
	}

	@Test(priority = 5, expectedExceptions = {ClientActionRequiredException.class})
	public void modeTest() throws InvalidCredentialException, MissingCredentialException, InvalidResponseDataException, HttpErrorException, ClientActionRequiredException, OAuthException, SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getCertificateConfiguration();
		initMap.remove("mode");
		initMap = SDKUtil.combineDefaultMap(initMap);
		service = new APIService(initMap);
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				"certuser_biz_api1.paypal.com", null, null, null, null, null,
				initMap);
		service.makeRequestUsing(handler);
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
