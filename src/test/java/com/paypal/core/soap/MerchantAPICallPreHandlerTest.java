package com.paypal.core.soap;

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.paypal.core.ConfigManager;
import com.paypal.core.Constants;
import com.paypal.core.CredentialManager;
import com.paypal.core.DefaultSOAPAPICallHandler;
import com.paypal.core.credential.ICredential;

public class MerchantAPICallPreHandlerTest {

//	private DefaultSOAPAPICallHandler defaultSoaphandler;
//	private CredentialManager credentialmgr;
//
//	@BeforeClass
//	public void setUp() {
//		defaultSoaphandler = new DefaultSOAPAPICallHandler(
//				"<Request>test</Request>", null, null, null);
//		credentialmgr = new CredentialManager(null);
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void getHeaderMapSignatureTest(ConfigManager configMgr)
//			throws Exception {
//
//		ICredential signatureCredential = credentialmgr
//				.getCredentialObject("jb-us-seller_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, signatureCredential, null);
//		Map<String, String> headers = soapHandler.getHeaderMap();
//		Assert.assertNotNull(headers);
//		assert (headers.size() > 0);
//		Assert.assertEquals("jb-us-seller_api1.paypal.com",
//				headers.get(Constants.PAYPAL_SECURITY_USERID_HEADER));
//		Assert.assertEquals("WX4WTU3S8MY44S7F",
//				headers.get(Constants.PAYPAL_SECURITY_PASSWORD_HEADER));
//		Assert.assertEquals(
//				"AFcWxV21C7fd0v3bYYYRCpSSRl31A7yDhhsPUU2XhtMoZXsWHFxu-RWy",
//				headers.get(Constants.PAYPAL_SECURITY_SIGNATURE_HEADER));
//		Assert.assertEquals(Constants.PAYLOAD_FORMAT_SOAP,
//				headers.get(Constants.PAYPAL_REQUEST_DATA_FORMAT_HEADER));
//		Assert.assertEquals(Constants.PAYLOAD_FORMAT_SOAP,
//				headers.get(Constants.PAYPAL_RESPONSE_DATA_FORMAT_HEADER));
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void getHeaderMapCertificateTest(ConfigManager conf)
//			throws Exception {
//
//		ICredential certificateCredential = credentialmgr
//				.getCredentialObject("certuser_biz_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, certificateCredential, null);
//		Map<String, String> headers = soapHandler.getHeaderMap();
//		Assert.assertNotNull(headers);
//		assert (headers.size() > 0);
//		Assert.assertEquals("certuser_biz_api1.paypal.com",
//				headers.get(Constants.PAYPAL_SECURITY_USERID_HEADER));
//		Assert.assertEquals("D6JNKKULHN3G5B8A",
//				headers.get(Constants.PAYPAL_SECURITY_PASSWORD_HEADER));
//		Assert.assertEquals(Constants.PAYLOAD_FORMAT_SOAP,
//				headers.get(Constants.PAYPAL_REQUEST_DATA_FORMAT_HEADER));
//		Assert.assertEquals(Constants.PAYLOAD_FORMAT_SOAP,
//				headers.get(Constants.PAYPAL_RESPONSE_DATA_FORMAT_HEADER));
//		Assert.assertEquals(
//				soapHandler.getSdkName() + "-" + soapHandler.getSdkVersion(),
//				headers.get(Constants.PAYPAL_REQUEST_SOURCE_HEADER));
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void getPayLoadForSignature(ConfigManager conf) throws Exception {
//		ICredential signatureCredential = credentialmgr
//				.getCredentialObject("jb-us-seller_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, signatureCredential, null);
//		String payload = soapHandler.getPayLoad();
//
//		Document dom = loadXMLFromString(payload);
//		Element docEle = dom.getDocumentElement();
//		NodeList header = docEle.getElementsByTagName("soapenv:Header");
//		NodeList requestCredential = ((Element) header.item(0))
//				.getElementsByTagName("ns:RequesterCredentials");
//		NodeList credential = ((Element) requestCredential.item(0))
//				.getElementsByTagName("ebl:Credentials");
//		NodeList user = ((Element) credential.item(0))
//				.getElementsByTagName("ebl:Username");
//		NodeList psw = ((Element) credential.item(0))
//				.getElementsByTagName("ebl:Password");
//		NodeList sign = ((Element) credential.item(0))
//				.getElementsByTagName("ebl:Signature");
//
//		String username = user.item(0).getTextContent();
//		String password = psw.item(0).getTextContent();
//		String signature = sign.item(0).getTextContent();
//
//		Assert.assertEquals("jb-us-seller_api1.paypal.com", username);
//		Assert.assertEquals("WX4WTU3S8MY44S7F", password);
//		Assert.assertEquals(
//				"AFcWxV21C7fd0v3bYYYRCpSSRl31A7yDhhsPUU2XhtMoZXsWHFxu-RWy",
//				signature);
//
//		NodeList requestBody = docEle.getElementsByTagName("Request");
//		Node bodyContent = requestBody.item(0);
//		String bodyText = bodyContent.getTextContent();
//		Assert.assertEquals("test", bodyText);
//
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void getPayLoadForCertificate(ConfigManager conf) throws Exception {
//		ICredential certificateCredential = credentialmgr
//				.getCredentialObject("certuser_biz_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, certificateCredential, null);
//		String payload = soapHandler.getPayLoad();
//
//		Document dom = loadXMLFromString(payload);
//		Element docEle = dom.getDocumentElement();
//		NodeList header = docEle.getElementsByTagName("soapenv:Header");
//		NodeList requestCredential = ((Element) header.item(0))
//				.getElementsByTagName("ns:RequesterCredentials");
//		NodeList credential = ((Element) requestCredential.item(0))
//				.getElementsByTagName("ebl:Credentials");
//		NodeList user = ((Element) credential.item(0))
//				.getElementsByTagName("ebl:Username");
//		NodeList psw = ((Element) credential.item(0))
//				.getElementsByTagName("ebl:Password");
//
//		String username = user.item(0).getTextContent();
//		String password = psw.item(0).getTextContent();
//
//		Assert.assertEquals("certuser_biz_api1.paypal.com", username);
//		Assert.assertEquals("D6JNKKULHN3G5B8A", password);
//
//		NodeList requestBody = docEle.getElementsByTagName("Request");
//		Node bodyContent = requestBody.item(0);
//		String bodyText = bodyContent.getTextContent();
//		Assert.assertEquals("test", bodyText);
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void setGetSDKNameTest(ConfigManager conf) throws Exception {
//		ICredential certificateCredential = credentialmgr
//				.getCredentialObject("certuser_biz_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, certificateCredential, "testsdk", "1.0.0",
//				"testsdkPortName", null);
//		Assert.assertEquals("testsdk", soapHandler.getSdkName());
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void setGetSDKVersionTest(ConfigManager conf) throws Exception {
//		ICredential certificateCredential = credentialmgr
//				.getCredentialObject("certuser_biz_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, certificateCredential, "testsdk", "1.0.0",
//				"testsdkPortName", null);
//		Assert.assertEquals("1.0.0", soapHandler.getSdkVersion());
//	}
//
//	@Test(dataProvider = "configParamsForSoap", dataProviderClass = com.paypal.core.DataProviderClass.class)
//	public void getEndPointTest(ConfigManager conf) throws Exception {
//		ICredential certificateCredential = credentialmgr
//				.getCredentialObject("certuser_biz_api1.paypal.com");
//		MerchantAPICallPreHandler soapHandler = new MerchantAPICallPreHandler(
//				defaultSoaphandler, certificateCredential, null);
//		String endpoint = soapHandler.getEndPoint();
//		Assert.assertEquals("https://api-3t.sandbox.paypal.com/2.0", endpoint);
//	}
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void MerchantAPICallPreHandlerConstructorTest() {
//		new MerchantAPICallPreHandler(defaultSoaphandler, null, null);
//	}
//
//	private Document loadXMLFromString(String xml) throws Exception {
//		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
//		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
//				.newDocumentBuilder();
//		return builder.parse(stream);
//	}
//
//	@AfterClass
//	public void tearDown() {
//		credentialmgr = null;
//		defaultSoaphandler = null;
//	}

}
