package test.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.testng.annotations.DataProvider;

import test.UnitTestConstants;

import com.paypal.core.APIService;
import com.paypal.core.ConfigManager;
import com.paypal.core.ConnectionManager;
import com.paypal.core.CredentialManager;
import com.paypal.core.HttpConnection;
import com.paypal.core.ICredential;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;

public class DataProviderClass {
	static ConfigManager conf;
	static File file = new File(UnitTestConstants.FILE_PATH);

	@DataProvider(name = "configParams")
	public static Object[][] configParams() throws FileNotFoundException,
			IOException {
		conf = ConfigManager.getInstance();

		FileInputStream in = new FileInputStream(file);
		conf.load(in);
		return new Object[][] { new Object[] { conf } };

	}

	@DataProvider(name = "headers")
	public static Object[][] getPayPalHeaders() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InvalidCredentialException, IOException,
			MissingCredentialException, SSLConfigurationException {

		APIService service = new APIService("AdaptivePayments");
		Method getPayPalHeaders = APIService.class.getDeclaredMethod(
				"getPayPalHeaders", ICredential.class, HttpConnection.class);
		getPayPalHeaders.setAccessible(true);
		CredentialManager credMgr = CredentialManager.getInstance();
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		HttpConnection connection = connectionMgr.getConnection();
		ICredential apiCredential = credMgr
				.getCredentialObject(UnitTestConstants.API_USER_NAME);
		Map<String, String> map = (Map) getPayPalHeaders.invoke(service,
				apiCredential, connection);
		return new Object[][] { new Object[] { map } };

	}

}
