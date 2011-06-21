package test.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.core.ConfigManager;
import com.paypal.core.ConnectionManager;
import test.UnitTestConstants;
import com.paypal.core.HttpConfiguration;
import com.paypal.core.HttpConnection;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.SSLConfigurationException;

public class HttpConnectionTest {
	HttpConnection connection;
	HttpConfiguration httpConfiguration;
	String payload = "requestEnvelope.detailLevel=ReturnAll&requestEnvelope.errorLanguage=en_US&invoice.merchantEmail=jb-us-seller1@paypal.com&invoice.payerEmail=jbui-us-personal1@paypal.com&invoice.items[0].name=product1&invoice.items[0].quantity=10.0&invoice.items[0].unitPrice=1.2&invoice.currencyCode=USD&invoice.paymentTerms=DueOnReceipt";

	@BeforeClass
	public void beforeClass() throws MalformedURLException, IOException {
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
		httpConfiguration = new HttpConfiguration();

	}

	/*@Test(dependsOnMethods = "setupClientSSLTest", dataProvider = "headers", dataProviderClass = DataProviderClass.class)
	public void executeTest(Map<String, String> map)
			throws SocketTimeoutException, ConnectException,
			InvalidResponseDataException, HttpErrorException, SocketException,
			IOException, InterruptedException, ClientActionRequiredException {
		httpConfiguration.setEndPointUrl(UnitTestConstants.API_ENDPOINT
				+ "Invoice/CreateInvoice");
		String url = httpConfiguration.getEndPointUrl();
		connection.CreateAndconfigureHttpConnection(httpConfiguration);

		String response = connection.execute(url, payload, map);
		Assert.assertNotNull(response);
		assert (response.contains("Success"));
		assert (response.contains("invoiceID"));
		assert (response.contains("invoiceNumber"));
	}*/

	@Test(dataProvider = "configParams", dataProviderClass = DataProviderClass.class)
	public void setupClientSSLTest(ConfigManager conf)
			throws SSLConfigurationException {
		connection.setupClientSSL(null, null, true);
	}

	@Test(expectedExceptions = MalformedURLException.class)
	public void checkMalformedURLExceptionTest() throws Exception {
		httpConfiguration.setEndPointUrl("ww.paypal.in");
		connection.CreateAndconfigureHttpConnection(httpConfiguration);
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void readMethodTest() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, IOException {
		Method readMethod = HttpConnection.class.getDeclaredMethod("read",
				BufferedReader.class);
		readMethod.setAccessible(true);
		BufferedReader br = null;
		readMethod.invoke(connection, br);
	}

	@Test(expectedExceptions = UnknownHostException.class, dependsOnMethods = "setupClientSSLTest", dataProvider = "headers", dataProviderClass = DataProviderClass.class)
	public void checkHttpErrorExceptionTest(Map<String, String> map)
			throws Exception {
		httpConfiguration
				.setEndPointUrl("https://xyz.paypal.com/Invoice/CreateInvoice");
		connection.CreateAndconfigureHttpConnection(httpConfiguration);
		connection.execute(httpConfiguration.getEndPointUrl(), payload, map);
	}

	@AfterClass
	public void afterClass() {
		connection = null;
		httpConfiguration = null;
	}

}
