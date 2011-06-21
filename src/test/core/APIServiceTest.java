package test.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import test.UnitTestConstants;

import com.paypal.core.APIService;
import com.paypal.core.ConfigManager;
import com.paypal.core.ConnectionManager;
import com.paypal.core.CredentialManager;
import com.paypal.core.HttpConnection;
import com.paypal.core.ICredential;
import com.paypal.core.SignatureCredential;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;

public class APIServiceTest {
	APIService service;
	HttpConnection connection;

	Map<String, String> map = new HashMap<String, String>();

	@BeforeClass
	public void beforeClass() throws NumberFormatException,
			SSLConfigurationException {
		service = new APIService("Invoice");
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void getPayPalHeadersTest(ConfigManager conf)
			throws SSLConfigurationException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, InvalidCredentialException, IOException,
			MissingCredentialException {
		Method method = APIService.class.getDeclaredMethod("getPayPalHeaders",
				ICredential.class, HttpConnection.class);
		method.setAccessible(true);
		CredentialManager credMgr = CredentialManager.getInstance();
		ICredential apiCredential = credMgr
				.getCredentialObject(UnitTestConstants.API_USER_NAME);
		map = (Map<String, String>) method.invoke(service, apiCredential,
				connection);
		Assert.assertEquals(map.get("X-PAYPAL-SECURITY-USERID"),
				UnitTestConstants.API_USER_NAME);
		Assert.assertEquals(map.get("X-PAYPAL-SECURITY-PASSWORD"),
				UnitTestConstants.API_PASSWORD);
		if (apiCredential instanceof SignatureCredential) {
			Assert.assertEquals(map.get("X-PAYPAL-SECURITY-SIGNATURE"),
					UnitTestConstants.API_SIGNATURE);
		}
		Assert.assertEquals(map.get("X-PAYPAL-APPLICATION-ID"),
				UnitTestConstants.APP_ID);
		Assert.assertEquals(map.get("X-PAYPAL-REQUEST-DATA-FORMAT"), "NV");
		Assert.assertEquals(map.get("X-PAYPAL-RESPONSE-DATA-FORMAT"), "NV");
		if (UnitTestConstants.API_ENDPOINT.contains("sandbox")) {
			Assert.assertEquals(map.get("X-PAYPAL-SANDBOX-EMAIL-ADDRESS"),
					"Platform.sdk.seller@gmail.com");
		}
		Assert.assertEquals(map.get("X-PAYPAL-DEVICE-IPADDRESS"), "127.0.0.1");
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class, expectedExceptions = InvalidCredentialException.class)
	public void getInvalidCredentialExceptionTest(ConfigManager conf)
			throws Exception {
		Method method = APIService.class.getDeclaredMethod("getPayPalHeaders",
				ICredential.class, HttpConnection.class);
		method.setAccessible(true);
		CredentialManager credMgr = CredentialManager.getInstance();
		ICredential apiCredential = credMgr
				.getCredentialObject("invalid@gmail.com");
		map = (Map<String, String>) method.invoke(service, apiCredential,
				connection);

	}

	/*@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void makeRequestTest(ConfigManager conf)
			throws SSLConfigurationException, InvalidCredentialException,
			IOException, HttpErrorException, InvalidResponseDataException,
			ClientActionRequiredException, MissingCredentialException,
			InterruptedException {
		String reqStr = "requestEnvelope.detailLevel=ReturnAll&requestEnvelope.errorLanguage=en_US&invoice.merchantEmail=jb-us-seller1@paypal.com&invoice.payerEmail=jbui-us-personal1@paypal.com&invoice.items[0].name=product1&invoice.items[0].quantity=10.0&invoice.items[0].unitPrice=1.2&invoice.currencyCode=USD&invoice.paymentTerms=DueOnReceipt";
		String response = service.makeRequest("CreateInvoice", reqStr,
				UnitTestConstants.API_USER_NAME);
		Assert.assertNotNull(response);
		assert (response.contains("Success"));
		assert (response.contains("invoiceID"));
		assert (response.contains("invoiceNumber"));
	}*/

	@Test
	public void getServiceNameTest() {
		Assert.assertEquals("Invoice", service.getServiceName());
	}

	@Test
	public void getEndPointTest() {
		Assert.assertEquals(UnitTestConstants.API_ENDPOINT,
				service.getEndPoint());
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
