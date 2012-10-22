package com.paypal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.testng.annotations.Test;

public class BaseServiceTest {
	BaseService service;
	String filePath = "src/test/resources/config.properties";

	// @BeforeClass
	// public void beforeClass() {
	// service = new BaseService("Service", "1.6.0");
	// }
	//
	// @AfterClass
	// public void afterClass() {
	// service = null;
	// }
	//
	// @Test
	// public void getServiceNameTest() {
	// Assert.assertEquals("Service", service.getServiceName());
	// }
	//
	// @Test
	// public void getVersionTest() {
	// Assert.assertEquals("1.6.0", service.getVersion());
	// }

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingFilePath() throws Exception {
		BaseService.initConfig(filePath);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingFile() throws Exception {
		File file = new File(filePath);
		BaseService.initConfig(file);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void initConfigTestUsingInputStream() throws Exception {
		InputStream is = new FileInputStream(new File(filePath));
		BaseService.initConfig(is);
	}

//	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
//	public void callTest(ConfigManager conf) throws InvalidCredentialException,
//			MissingCredentialException, NoSuchMethodException,
//			SecurityException, IllegalAccessException,
//			IllegalArgumentException, InvocationTargetException {
//		String payload = "requestEnvelope.errorLanguage=en_US&baseAmountList.currency(0).code=USD&baseAmountList.currency(0).amount=2.0&convertToCurrencyList.currencyCode(0)=GBP";
//		APICallPreHandler handler = new NVPAPICallPreHandler(payload,
//				"AdaptivePayments", "ConvertCurrency",
//				UnitTestConstants.API_USER_NAME, null, null);
//		String response = "";
//		BaseService service;
//		Class<?> ser = service.getClass();
//		Method method = ser.getDeclaredMethod("call",
//				APICallPreHandler.class);
//		method.setAccessible(true);
//		response = (String) method.invoke(service, handler);
//
//	}
}
