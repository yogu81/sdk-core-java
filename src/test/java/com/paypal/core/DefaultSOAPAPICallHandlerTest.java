package com.paypal.core;

import java.util.HashMap;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultSOAPAPICallHandlerTest {

	DefaultSOAPAPICallHandler defaultHandler;

	@BeforeClass
	public void beforeClass() {

		defaultHandler = new DefaultSOAPAPICallHandler(
				"requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP",
				"", "");
	}

	@AfterClass
	public void afterClass() {
		defaultHandler = null;
	}

	@Test(dataProvider = "configParamsForSoap", dataProviderClass = DataProviderClass.class)
	public void getEndPointTest(ConfigManager conf) {
		Assert.assertEquals("https://api-3t.sandbox.paypal.com/2.0",
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
		defaultHandler = new DefaultSOAPAPICallHandler("", "", "");
		Assert.assertEquals("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ><soapenv:Header></soapenv:Header><soapenv:Body></soapenv:Body></soapenv:Envelope>", defaultHandler.getPayLoad());
	}
}
