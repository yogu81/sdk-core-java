package com.paypal.core;

import com.paypal.core.credential.ICredential;

/**
 * A Strategy pattern to retrieve {@link ICredential} as any conceivable
 * datatype as required by the application
 * 
 * @author kjayakumar
 * 
 * @param <T>
 *            Return data type
 * @param <E>
 *            Operated {@link ICredential}
 */
public interface AuthenticationStrategy<T, E extends ICredential> {

	/**
	 * Realizes {@link ICredential} as any type as chosen by the implementation
	 * 
	 * @param e
	 *            {@link ICredential} instance
	 * @return
	 * @throws Exception
	 */
	T realize(E e) throws Exception;

}
