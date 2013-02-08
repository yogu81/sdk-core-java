package com.paypal.core;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SDKUtilTest {

	@Test
	public void escapeInvalidXmlChars() {
		Assert.assertEquals(
				SDKUtil.escapeInvalidXmlChars("<ns:GetBalanceReq>&\";</ns:GetBalanceReq>"),
				"&lt;ns:GetBalanceReq&gt;&amp;&quot;;&lt;/ns:GetBalanceReq&gt;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlChars("&"), "&amp;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlChars("&amp;<"),
				"&amp;&lt;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlChars("&lt;&"), "&lt;&amp;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlChars("abc\"xyz"),
				"abc&quot;xyz");
	}

	@Test
	public void escapeInvalidXmlCharsRegexString() {
		Assert.assertEquals(
				SDKUtil.escapeInvalidXmlCharsRegex("<ns:GetBalanceReq>&\";</ns:GetBalanceReq>"),
				"&lt;ns:GetBalanceReq&gt;&amp;&quot;;&lt;/ns:GetBalanceReq&gt;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlCharsRegex("&"), "&amp;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlCharsRegex("&amp;<"),
				"&amp;&lt;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlCharsRegex("&lt;&"),
				"&lt;&amp;");
		Assert.assertEquals(SDKUtil.escapeInvalidXmlCharsRegex("abc\"xyz"),
				"abc&quot;xyz");
	}
	
	@Test
	public void testFormatURIPathForNull() {
		String nullString = SDKUtil.formatURIPath(null, null);
		Assert.assertNull(nullString);
	}
	
	@Test
	public void testFormatURIPathNoPattern() {
		String pattern = "/a/b/c";
		String uriPath = SDKUtil.formatURIPath(pattern, null);
		Assert.assertEquals(uriPath, pattern);
	}
	
	@Test
	public void testFormatURIPathNoQS() {
		String pattern = "/a/b/{0}";
		Object[] parameters = new Object[] {"replace"};
		String uriPath = SDKUtil.formatURIPath(pattern, parameters);
		Assert.assertEquals(uriPath, "/a/b/replace");
	}
	
	@Test
	public void testFormatURIPath() {
		String pattern = "/a/b/{0}?name={1}";
		Object[] parameters = new Object[] {"replace", "nameValue"};
		String uriPath = SDKUtil.formatURIPath(pattern, parameters);
		Assert.assertEquals(uriPath, "/a/b/replace?name=nameValue");
	}
	
	@Test
	public void testFormatURIPathWithNull() {
		String pattern = "/a/b/{0}?name={1}&age={2}";
		Object[] parameters = new Object[] {"replace", "nameValue", null};
		String uriPath = SDKUtil.formatURIPath(pattern, parameters);
		Assert.assertEquals(uriPath, "/a/b/replace?name=nameValue");
	}
	
	@Test
	public void testFormatURIPathWithEmpty() {
		String pattern = "/a/b/{0}?name={1}&age=";
		Object[] parameters = new Object[] {"replace", "nameValue", null};
		String uriPath = SDKUtil.formatURIPath(pattern, parameters);
		Assert.assertEquals(uriPath, "/a/b/replace?name=nameValue");
	}
	
	@Test
	public void testFormatURIPathTwoQS() {
		String pattern = "/a/b/{0}?name={1}&age={2}";
		Object[] parameters = new Object[] {"replace", "nameValue", "1"};
		String uriPath = SDKUtil.formatURIPath(pattern, parameters);
		Assert.assertEquals(uriPath, "/a/b/replace?name=nameValue&age=1");
	}

}
