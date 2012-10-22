package com.paypal.core.soap;

import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.TokenAuthorization;

public class SignatureHttpHeaderAuthStrategyTest {

	@Test
	public void processTokenAuthorizationTest() throws Exception{
		SignatureHttpHeaderAuthStrategy signatureHttpHeaderAuthStrategy = new SignatureHttpHeaderAuthStrategy("https://svcs.sandbox.paypal.com/");
		TokenAuthorization tokenAuthorization = new TokenAuthorization("accessToken","tokenSecret");
		SignatureCredential signatureCredential = new SignatureCredential("testusername","testpassword","testsignature");
		Map header = signatureHttpHeaderAuthStrategy.processTokenAuthorization(signatureCredential, tokenAuthorization);
		String authHeader = (String)header.get("X-PP-AUTHORIZATION");
		String[] headers=authHeader.split(",");
		
		Assert.assertEquals("token=accessToken", headers[0]);
	}
}
