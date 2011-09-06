package test.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

public class APIServiceTest {
	APIService service;
	HttpConnection connection;

	Map<String, String> map = new HashMap<String, String>();

	@BeforeClass
	public void beforeClass() throws NumberFormatException,
			SSLConfigurationException, FileNotFoundException, IOException {
		ConfigManager.getInstance().load(
				new FileInputStream(new File(UnitTestConstants.FILE_PATH)));
		service = new APIService("Service");
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
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

	

	@Test
	public void getServiceNameTest() {
		Assert.assertEquals("Service", service.getServiceName());
	}

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void getEndPointTest(ConfigManager conf) {

		Assert.assertEquals(UnitTestConstants.API_ENDPOINT,
				service.getEndPoint());
	}

	@AfterClass
	public void afterClass() {
		service = null;
		connection = null;
	}

}
