package com.paypal.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultSOAPAPICallHandlerTest {

	DefaultSOAPAPICallHandler defaultHandler;

	@BeforeClass
	public void beforeClass() throws IOException {
		Properties props;
		props = new Properties();
		props.load(this.getClass()
				.getResourceAsStream("/sdk_config.properties"));
		Map<String, String> cMap = SDKUtil.constructMap(props);
		defaultHandler = new DefaultSOAPAPICallHandler(
				"requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP",
				"", "", cMap);
	}

	@AfterClass
	public void afterClass() {
		defaultHandler = null;
	}

	@Test
	public void getEndPointTest() {
		Assert.assertEquals("https://svcs.sandbox.paypal.com/",
				defaultHandler.getEndPoint());
	}

	@Test
	public void getCredentialTest() {
		Assert.assertEquals(defaultHandler.getCredential(), null);
	}

	@Test
	public void getHeaderMapTest() {
		Assert.assertEquals(defaultHandler.getHeaderMap().getClass(),
				HashMap.class);
	}

	@Test
	public void getHeaderStringTest() {
		Assert.assertEquals(defaultHandler.getHeaderString(), "");
	}

	@Test
	public void getNamespacesTest() {
		Assert.assertEquals(defaultHandler.getNamespaces(), "");
	}

	@Test
	public void getPayLoadTest() {
		Assert.assertEquals(
				defaultHandler.getPayLoad(),
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ><soapenv:Header></soapenv:Header><soapenv:Body>requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP</soapenv:Body></soapenv:Envelope>");
	}

	@Test
	public void setHeaderStringTest() {
		defaultHandler.setHeaderString("headerString");
		Assert.assertEquals(defaultHandler.getHeaderString(), "headerString");
	}

	@Test
	public void setNamespacesTest() {
		defaultHandler.setNamespaces("namespaces");
		Assert.assertEquals(defaultHandler.getNamespaces(), "namespaces");
	}

	@Test
	public void getPayloadForEmptyRawPayloadTest() {
		defaultHandler = new DefaultSOAPAPICallHandler("", "", "", null);
		Assert.assertEquals("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ><soapenv:Header></soapenv:Header><soapenv:Body></soapenv:Body></soapenv:Envelope>", defaultHandler.getPayLoad());
	}
}
