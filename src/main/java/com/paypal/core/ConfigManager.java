package com.paypal.core;

import java.io.IOException;
import java.io.InputStream;
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
	 * Loads the internal properties with the passed {@link InputStream}
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
	public Map<String, String> getConf() {
		return SDKUtil.constructMap(properties);
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

	public boolean isPropertyLoaded() {
		return propertyLoaded;
	}

	private void setPropertyLoaded(boolean propertyLoaded) {
		this.propertyLoaded = propertyLoaded;
	}

}
