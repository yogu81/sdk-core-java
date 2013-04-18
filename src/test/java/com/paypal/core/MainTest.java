package com.paypal.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.sdk.openidconnect.CreateFromAuthorizationCodeParameters;
import com.paypal.sdk.openidconnect.CreateFromRefreshTokenParameters;
import com.paypal.sdk.openidconnect.PayPalRESTException;
import com.paypal.sdk.openidconnect.Tokeninfo;
import com.paypal.sdk.openidconnect.Userinfo;
import com.paypal.sdk.openidconnect.UserinfoParameters;

public class MainTest {

	private Tokeninfo info;

//	@Test
//	public void test() throws InvalidResponseDataException, HttpErrorException,
//			ClientActionRequiredException, PayPalRESTException,
//			URISyntaxException, IOException, InterruptedException {
//		CreateFromAuthorizationCodeParameters param = new CreateFromAuthorizationCodeParameters();
//		param.setCode("XhfdH25uvYUGgWfI6zgJk6Xzu9bfCiKjcMdSOPLFyZOhRrk51Sb-8A-aQKS185Hqj-yAfKHvTrooqYPA9V6vggPfjJ78HQzemhII92OOyjJKKVvV");
//		info = Tokeninfo.createFromAuthorizationCode(param);
//		System.out.println("AT: " + info.getAccessToken());
//		System.out.println("RT: " + info.getRefreshToken());
//		String enc = URLEncoder.encode(info.getRefreshToken(), "UTF-8");
//		System.out.println("ENC: " + enc);
//		info.setRefreshToken(enc);
//	}
//
//	@Test(dependsOnMethods = { "test" })
//	public void test2() throws InvalidResponseDataException,
//			HttpErrorException, ClientActionRequiredException,
//			PayPalRESTException, URISyntaxException, IOException,
//			InterruptedException {
//		CreateFromRefreshTokenParameters param = new CreateFromRefreshTokenParameters();
//		info = info.createFromRefreshToken(param);
//		System.out.println(info.getAccessToken());
//		System.out.println(info.getRefreshToken());
//	}

	@Test
	public void test() throws InvalidResponseDataException, HttpErrorException,
			ClientActionRequiredException, PayPalRESTException,
			URISyntaxException, IOException, InterruptedException {
		Map<String, String> configurationMap = new HashMap<String, String>();
		configurationMap.put("clientId", "ProxyRP-01");
		configurationMap.put("clientSecret", "A8VERY8SECRET8VALUE0");
		configurationMap.put("service.EndPoint", "http://biggmax.com/");
		configurationMap.put("http.ConnectionTimeOut", "5000");
		configurationMap.put("http.Retry", "2");
		configurationMap.put("http.ReadTimeOut", "30000");
		configurationMap.put("http.MaxConnection", "100");
		configurationMap.put("http.IPAddress", "127.0.0.1");
		CreateFromAuthorizationCodeParameters param = new CreateFromAuthorizationCodeParameters();
		param.setCode("sUUzOEucaN6NMWsDTPqT7tq9P8kwrdYwvarcpLnh4n7nhE_QkiOsFrYJc59nJbl97UnhgUcG9s-BTxiWyJxJ4tMYKMCV2WwaXKcoos7hSGpu4HdG");
		info = Tokeninfo.createFromAuthorizationCode(configurationMap, param);
		System.out.println("AT: " + info.getAccessToken());
		System.out.println("RT: " + info.getRefreshToken());
		String enc = URLEncoder.encode(info.getRefreshToken(), "UTF-8");
		System.out.println("RT: " + enc);
		info.setRefreshToken(enc);
	}

	@Test(dependsOnMethods = { "test" })
	public void test2() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			PayPalRESTException, URISyntaxException, IOException,
			InterruptedException {
		Map<String, String> configurationMap = new HashMap<String, String>();
		configurationMap.put("clientId", "ProxyRP-01");
		configurationMap.put("clientSecret", "A8VERY8SECRET8VALUE0");
		configurationMap.put("service.EndPoint", "http://biggmax.com/");
		configurationMap.put("http.ConnectionTimeOut", "5000");
		configurationMap.put("http.Retry", "2");
		configurationMap.put("http.ReadTimeOut", "30000");
		configurationMap.put("http.MaxConnection", "100");
		configurationMap.put("http.IPAddress", "127.0.0.1");
		CreateFromRefreshTokenParameters param = new CreateFromRefreshTokenParameters();
		info = info.createFromRefreshToken(configurationMap, param);
		System.out.println(info.getAccessToken());
		System.out.println(info.getRefreshToken());
	}

	@Test(dependsOnMethods = { "test2" })
	public void test3() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException,
			PayPalRESTException, URISyntaxException, IOException,
			InterruptedException {
		Map<String, String> configurationMap = new HashMap<String, String>();
		configurationMap.put("clientId", "ProxyRP-01");
		configurationMap.put("clientSecret", "A8VERY8SECRET8VALUE0");
		configurationMap.put("service.EndPoint", "http://biggmax.com/");
		configurationMap.put("http.ConnectionTimeOut", "5000");
		configurationMap.put("http.Retry", "2");
		configurationMap.put("http.ReadTimeOut", "30000");
		configurationMap.put("http.MaxConnection", "100");
		configurationMap.put("http.IPAddress", "127.0.0.1");
		UserinfoParameters param = new UserinfoParameters();
		param.setAccessToken(info.getAccessToken());
		Userinfo userInfo = Userinfo.userinfo(param);
		System.out.println("Email: " + userInfo.getEmail());
		System.out.println("Account Type: " + userInfo.getAccountType());
		System.out.println("Name: " + userInfo.getGivenName());
	}

}
