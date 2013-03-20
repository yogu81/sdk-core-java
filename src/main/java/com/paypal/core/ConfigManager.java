package com.paypal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.paypal.sdk.util.ResourceLoader;

/**
 * <code>ConfigManager</code> loads the configuration file and hands out
 * appropriate parameters to application
 * 
 */
public final class ConfigManager {

	/**
	 * Singleton instance variable
	 */
	private static ConfigManager conf;

	/**
	 * Underlying property implementation
	 */
	private Properties properties;

	/**
	 * Initialized notifier
	 */
	private boolean propertyLoaded = false;

	/**
	 * Map View of internal {@link Properties}
	 */
	private Map<String, String> mapView = null;

	/**
	 * Map View of internal Default {@link Properties}
	 */
	private static Map<String, String> defaultMapView = null;

	/**
	 * Default {@link Properties}
	 */
	private static final Properties DEFAULT_PROPERTIES;

	// Initialize DEFAULT_PROPERTIES
	static {
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.put(Constants.HTTP_CONNECTION_TIMEOUT, "5000");
		DEFAULT_PROPERTIES.put(Constants.HTTP_CONNECTION_RETRY, "2");
		DEFAULT_PROPERTIES.put(Constants.HTTP_CONNECTION_READ_TIMEOUT, "30000");
		DEFAULT_PROPERTIES.put(Constants.HTTP_CONNECTION_MAX_CONNECTION, "100");
		DEFAULT_PROPERTIES.put(Constants.DEVICE_IP_ADDRESS, "127.0.0.1");
		DEFAULT_PROPERTIES.put(Constants.GOOGLE_APP_ENGINE, "false");
	}

	/**
	 * Private constructor
	 */
	private ConfigManager() {
		ResourceLoader resourceLoader = new ResourceLoader(
				Constants.DEFAULT_CONFIGURATION_FILE);
		try {
			InputStream inputStream = resourceLoader.getInputStream();
			properties = new Properties();
			properties.load(inputStream);
			setPropertyLoaded(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Singleton accessor method
	 * 
	 * @return ConfigManager object
	 */
	public static ConfigManager getInstance() {
		synchronized (ConfigManager.class) {
			if (conf == null) {
				conf = new ConfigManager();
			}
		}
		return conf;
	}

	/**
	 * Returns the Default {@link Properties} of System Configuration
	 * 
	 * @return Default {@link Properties}
	 */
	public static final Properties getDefaultProperties() {
		return DEFAULT_PROPERTIES;
	}

	/**
	 * Returns a {@link Map} view of Default {@link Properties}
	 * 
	 * @return {@link Map} view of Default {@link Properties}
	 */
	public static Map<String, String> getDefaultSDKMap() {
		if (defaultMapView == null) {
			defaultMapView = new HashMap<String, String>();
			for (Object object : DEFAULT_PROPERTIES.keySet()) {
				defaultMapView.put(object.toString().trim(), DEFAULT_PROPERTIES
						.getProperty(object.toString()).trim());
			}
		}
		return defaultMapView;
	}
	
	/**
	 * Combines some {@link Properties} with Default {@link Properties}
	 * 
	 * @param receivedProperties
	 *            Properties used to combine with Default {@link Properties}
	 * 
	 * @return Combined {@link Properties}
	 */
	public static Properties combineDefaultProperties(
			Properties receivedProperties) {
		Properties combinedProperties = new Properties(getDefaultProperties());
		if ((receivedProperties != null) && (receivedProperties.size() > 0)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
			try {
				receivedProperties.store(bos, null);
				combinedProperties.load(new ByteArrayInputStream(bos
						.toByteArray()));
			} catch (IOException e) {
				// TODO return defaultProperties
			}
		}
		return combinedProperties;
	}

	/**
	 * Loads the internal properties with the passed {@link InputStream}
	 * 
	 * @deprecated
	 * @param is
	 *            InputStream
	 * 
	 * @throws IOException
	 */
	public void load(InputStream is) throws IOException {
		properties = new Properties();
		properties.load(is);
		if (!propertyLoaded) {
			setPropertyLoaded(true);
		}
	}

	/**
	 * Initializes the internal properties with the passed {@link Properties}
	 * instance
	 * 
	 * @deprecated
	 * @param properties
	 *            Properties instance
	 * 
	 */
	public void load(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException(
					"Initialization properties cannot be null");
		}
		this.properties = properties;
		if (!propertyLoaded) {
			setPropertyLoaded(true);
		}
	}

	/**
	 * Constructs a {@link Map} object from the underlying {@link Properties}
	 * 
	 * @return {@link Map}
	 */
	public Map<String, String> getConfigurationMap() {
		if (mapView == null) {
			mapView = new HashMap<String, String>();
			if (properties != null) {
				for (Object object : properties.keySet()) {
					mapView.put(object.toString().trim(), properties
							.getProperty(object.toString()).trim());
				}
			}
		}
		return mapView;
	}

	/**
	 * Returns a value for the corresponding key
	 * 
	 * @deprecated
	 * 
	 * @param key
	 *            String key
	 * @return String value
	 */
	public String getValue(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Mimics the call to {@link Properties}.getProperty(key, defaultValue)
	 * 
	 * @deprecated
	 * 
	 * @param key
	 *            String key to search in properties file
	 * @param defaultValue
	 *            Default value to be sent in case of a miss
	 * @return String value corresponding to the key or default value
	 */
	public String getValueWithDefault(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Gets all the values in the particular category in configuration (eg:
	 * acct)
	 * 
	 * @deprecated
	 * 
	 * @param category
	 * @return Map
	 */
	public Map<String, String> getValuesByCategory(String category) {
		String key = Constants.EMPTY_STRING;
		HashMap<String, String> map = new HashMap<String, String>();
		for (Object obj : properties.keySet()) {
			key = (String) obj;
			if (key.contains(category)) {
				map.put(key, properties.getProperty(key));
			}
		}
		return map;
	}

	/**
	 * Returns the key prefixes for all configured accounts
	 * 
	 * @deprecated
	 * 
	 * @return {@link Set} of Accounts
	 */

	public Set<String> getNumOfAcct() {
		String key = Constants.EMPTY_STRING;
		Set<String> set = new HashSet<String>();
		for (Object obj : properties.keySet()) {
			key = (String) obj;
			if (key.contains("acct")) {
				int pos = key.indexOf('.');
				String acct = key.substring(0, pos);
				set.add(acct);
			}
		}
		return set;

	}

	/**
	 * @deprecated
	 * @return
	 */
	public boolean isPropertyLoaded() {
		return propertyLoaded;
	}

	private void setPropertyLoaded(boolean propertyLoaded) {
		this.propertyLoaded = propertyLoaded;
	}

}
