package com.paypal.core.soap;

import com.paypal.core.credential.ThirdPartyAuthorization;

/**
 * SubjectAuthorization encapsulates third party subject authorization.
 * Subject Authorization caters to SOAP APIs only
 * @author kjayakumar
 *
 */
public class SubjectAuthorization implements ThirdPartyAuthorization {

	/**
	 * Subject information
	 */
	private String subject;

	/**
	 * @param subject
	 */
	public SubjectAuthorization(String subject) {
		super();
		if (subject == null || subject.length() == 0) {
			throw new IllegalArgumentException("Subject is null or empty in SubjectAuthorization");
		}
		this.subject = subject;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

}
