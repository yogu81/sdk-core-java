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
	}

	@Test
	public void getCredential() {
		Assert.assertEquals(defaultHandler.getCredential(), null);
	}

	@Test(dataProvider = "configParamsForSoap", dataProviderClass = DataProviderClass.class)
	public void getEndPoint(ConfigManager conf) {
		Assert.assertEquals(defaultHandler.getEndPoint(),
				"https://api-3t.sandbox.paypal.com/2.0");

	}

	@Test
	public void getHeaderMap() {
		Assert.assertEquals(defaultHandler.getHeaderMap().getClass(),
				HashMap.class);
	}

	@Test
	public void getHeaderString() {
		Assert.assertEquals(defaultHandler.getHeaderString(), "");
	}

	@Test
	public void getNamespaces() {
		Assert.assertEquals(defaultHandler.getNamespaces(), "");
	}

	@Test
	public void getPayLoad() {
		Assert.assertEquals(defaultHandler.getPayLoad(), "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ><soapenv:Header></soapenv:Header><soapenv:Body>requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP</soapenv:Body></soapenv:Envelope>");
	}

	@Test
	public void getSoapBodyEnd() {

	}

	@Test
	public void getSoapBodyStart() {

	}

	@Test
	public void getSoapEnvelopeEnd() {

	}

	@Test
	public void getSoapEnvelopeStart() {

	}

	@Test
	public void getSoapHeaderEnd() {

	}

	@Test
	public void getSoapHeaderStart() {

	}

	@Test
	public void setHeaderString() {
		defaultHandler.setHeaderString("headerString");
		Assert.assertEquals(defaultHandler.getHeaderString(), "headerString");
	}

	@Test
	public void setNamespaces() {
		defaultHandler.setNamespaces("namespaces");
		Assert.assertEquals(defaultHandler.getNamespaces(), "namespaces");
	}
}
