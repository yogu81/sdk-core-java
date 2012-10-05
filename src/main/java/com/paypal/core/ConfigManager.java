package com.paypal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashMap;

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
	 * Holder for property keys
	 */
	private Enumeration<Object> em;

	/**
	 * Underlying property implementation
	 */
	private Properties properties;

	/**
	 * Initialized notifier
	 */
	private boolean propertyLoaded = false;

	// Private constructor
	private ConfigManager() {
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
	 * Overloaded method to load the configuration file.
	 * 
	 * @param is
	 *            InputStream
	 * 
	 * @throws IOException
	 */
	public void load(InputStream is) throws IOException {
		properties = new Properties();
		properties.load(is);
		setPropertyLoaded(true);
	}

	/**
	 * Load the keys from properties
	 * 
	 * @return Enumeration of keys from configuration property file.
	 */
	private Enumeration<Object> loadKeys() {
		em = properties.keys();
		return em;
	}

	/**
	 * Returns a value for the corresponding key
	 * 
	 * @param key
	 *            String key
	 * @return String value
	 */
	public String getValue(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Gets all the values in the particular category in configuration (eg:
	 * acct)
	 * 
	 * @param category
	 * @return Map
	 */
	public Map<String, String> getValuesByCategory(String category) {
		loadKeys();
		String key = Constants.EMPTY_STRING;
		HashMap<String, String> map = new HashMap<String, String>();
		while (em.hasMoreElements()) {
			key = (String) em.nextElement();
			if (key.contains(category)) {
				map.put(key, properties.getProperty(key));
			}
		}
		return map;
	}

	/**
	 * Returns acct(Account) value corresponding to the API userId passed in
	 * 
	 * @param userName
	 * @return String
	 */
	public String getPrefix(String userName) {
		String acct = Constants.EMPTY_STRING;
		String key = Constants.EMPTY_STRING;
		loadKeys();
		while (em.hasMoreElements()) {
			key = (String) em.nextElement();
			if (userName.equalsIgnoreCase(properties.getProperty(key))) {
				int pos = key.indexOf('.');
				acct = key.substring(0, pos);
			}
		}
		return acct;
	}

	/**
	 * Returns the key prefixes for all configured accounts
	 * 
	 * @return Set
	 */

	public Set<String> getNumOfAcct() {
		loadKeys();
		String key = Constants.EMPTY_STRING;
		Set<String> set = new HashSet<String>();
		while (em.hasMoreElements()) {
			key = (String) em.nextElement();
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
