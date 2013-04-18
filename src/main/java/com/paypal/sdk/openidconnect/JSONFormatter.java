package com.paypal.sdk.openidconnect;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSONFormatter converts objects to JSON representation and vice-versa
 * 
 * @author kjayakumar
 * 
 */
public class JSONFormatter {

	/**
	 * Gson
	 */
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();

	/**
	 * Converts a Raw Type to JSON String
	 * 
	 * @param <T>
	 *            Type to be converted
	 * @param t
	 *            Object of the type
	 * @return JSON representation
	 */
	public static <T> String toJSON(T t) {
		return GSON.toJson(t);
	}

	/**
	 * Converts a JSON String to object representation
	 * 
	 * @param <T>
	 *            Type to be converted
	 * @param responseString
	 *            JSON representation
	 * @param clazz
	 *            Target class
	 * @return Object of the target type
	 */
	public static <T> T fromJSON(String responseString, Class<T> clazz) {
		T t = null;
		t = GSON.fromJson(responseString, clazz);
		return t;
	}

}
