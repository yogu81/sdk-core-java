package com.paypal.core;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SDKUtilTest {

	@Test
	public void escapeInvalidXmlChars() {
		Assert.assertEquals(
				"&lt;ns:GetBalanceReq&gt;&amp;&quot;;&lt;/ns:GetBalanceReq&gt;",
				SDKUtil.escapeInvalidXmlChars("<ns:GetBalanceReq>&\";</ns:GetBalanceReq>"));
	}

	@Test
	public void escapeInvalidXmlCharsRegexString() {
		Assert.assertEquals(
				"&lt;ns:GetBalanceReq&gt;&amp;&quot;;&lt;/ns:GetBalanceReq&gt;",
				SDKUtil.escapeInvalidXmlCharsRegex("<ns:GetBalanceReq>&\";</ns:GetBalanceReq>"));
	}

}
