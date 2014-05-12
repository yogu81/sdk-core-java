package com.paypal.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.paypal.core.APIService;
import com.paypal.core.LoggingManager;

public class LoggingManagerTest {

	@Test
	public void getLoggerTest() throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method method = LoggingManager.class.getDeclaredMethod("getLogger",
				Class.class);
		method.setAccessible(true);
		Object obj= method.invoke(LoggingManager.class,
				APIService.class);
		Assert.assertNotNull(obj);
		Assert.assertTrue(Logger.class.isAssignableFrom(obj.getClass()));
		Assert.assertEquals("com.paypal.core.APIService", ((Logger)obj).getName());
	}
}
