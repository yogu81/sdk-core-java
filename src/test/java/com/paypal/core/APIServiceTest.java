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

import com.paypal.core.credential.ICredential;
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

	@Test(priority = 5, expectedExceptions = { ClientActionRequiredException.class })
	public void modeTest() throws InvalidCredentialException,
			MissingCredentialException, InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
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

	@Test(priority = 6)
	public void defaultPlatformSandboxEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getCertificateConfiguration();
		initMap = SDKUtil.combineDefaultMap(initMap);
		service = new APIService(initMap);
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				"certuser_biz_api1.paypal.com", null, null, null, null, null,
				initMap);
		Assert.assertEquals(handler.getEndPoint(),
				"https://svcs.sandbox.paypal.com/AdaptivePayments/ConvertCurrency");
	}

	@Test(priority = 7)
	public void defaultPlatformLiveEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getCertificateConfiguration();
		initMap = SDKUtil.combineDefaultMap(initMap);
		initMap.put("mode", "live");
		service = new APIService(initMap);
		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
		APICallPreHandler handler = new PlatformAPICallPreHandler(payload,
				"AdaptivePayments", "ConvertCurrency",
				"certuser_biz_api1.paypal.com", null, null, null, null, null,
				initMap);
		Assert.assertEquals(handler.getEndPoint(),
				"https://svcs.paypal.com/AdaptivePayments/ConvertCurrency");
	}

	@Test(priority = 8)
	public void defaultMerchantCertificateSandboxEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getCertificateConfiguration();
		CredentialManager credentialmgr = new CredentialManager(initMap);
		DefaultSOAPAPICallHandler defaultSoaphandler = new DefaultSOAPAPICallHandler(
				"<Request>test</Request>", null, null, initMap);
		ICredential certificateCredential = credentialmgr
				.getCredentialObject("certuser_biz_api1.paypal.com");
		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
				defaultSoaphandler, certificateCredential, "testsdk", "1.0.0",
				"testsdkPortName", initMap);
		String endpoint = soapHandler.getEndPoint();
		Assert.assertEquals("https://api.sandbox.paypal.com/2.0", endpoint);
	}
	
	@Test(priority = 8)
	public void defaultMerchantSignatureSandboxEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getSignatureConfiguration();
		CredentialManager credentialmgr = new CredentialManager(initMap);
		DefaultSOAPAPICallHandler defaultSoaphandler = new DefaultSOAPAPICallHandler(
				"<Request>test</Request>", null, null, initMap);
		ICredential signatureCredential = credentialmgr
				.getCredentialObject("jb-us-seller_api1.paypal.com");
		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
				defaultSoaphandler, signatureCredential, "testsdk", "1.0.0",
				"testsdkPortName", initMap);
		String endpoint = soapHandler.getEndPoint();
		Assert.assertEquals("https://api-3t.sandbox.paypal.com/2.0", endpoint);
	}
	
	@Test(priority = 8)
	public void defaultMerchantCertificateLiveEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getCertificateConfiguration();
		initMap.put("mode", "live");
		CredentialManager credentialmgr = new CredentialManager(initMap);
		DefaultSOAPAPICallHandler defaultSoaphandler = new DefaultSOAPAPICallHandler(
				"<Request>test</Request>", null, null, initMap);
		ICredential certificateCredential = credentialmgr
				.getCredentialObject("certuser_biz_api1.paypal.com");
		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
				defaultSoaphandler, certificateCredential, "testsdk", "1.0.0",
				"testsdkPortName", initMap);
		String endpoint = soapHandler.getEndPoint();
		Assert.assertEquals("https://api.paypal.com/2.0", endpoint);
	}
	
	@Test(priority = 8)
	public void defaultMerchantSignatureLiveEndpointTest()
			throws InvalidCredentialException, MissingCredentialException,
			InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, OAuthException,
			SSLConfigurationException, IOException, InterruptedException {
		Map<String, String> initMap = DataProviderClass
				.getSignatureConfiguration();
		initMap.put("mode", "live");
		CredentialManager credentialmgr = new CredentialManager(initMap);
		DefaultSOAPAPICallHandler defaultSoaphandler = new DefaultSOAPAPICallHandler(
				"<Request>test</Request>", null, null, initMap);
		ICredential signatureCredential = credentialmgr
				.getCredentialObject("jb-us-seller_api1.paypal.com");
		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
				defaultSoaphandler, signatureCredential, "testsdk", "1.0.0",
				"testsdkPortName", initMap);
		String endpoint = soapHandler.getEndPoint();
		Assert.assertEquals("https://api-3t.paypal.com/2.0", endpoint);
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
