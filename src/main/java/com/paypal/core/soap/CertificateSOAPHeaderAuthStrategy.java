package com.paypal.core.soap;

import com.paypal.core.AuthenticationStrategy;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.SubjectAuthorization;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.core.credential.TokenAuthorization;

/**
 * <code>CertificateSOAPHeaderAuthStrategy</code> is an implementation of
 * {@link AuthenticationStrategy} which acts on {@link CertificateCredential}
 * and retrieves them as SOAP headers
 * 
 */
public class CertificateSOAPHeaderAuthStrategy implements
		AuthenticationStrategy<String, CertificateCredential> {

	/**
	 * Instance of ThirdPartyAuthorization
	 */
	private ThirdPartyAuthorization thirdPartyAuthorization;

	public CertificateSOAPHeaderAuthStrategy() {
	}

	/**
	 * @return the thirdPartyAuthorization
	 */
	public ThirdPartyAuthorization getThirdPartyAuthorization() {
		return thirdPartyAuthorization;
	}

	/**
	 * @param thirdPartyAuthorization
	 *            the thirdPartyAuthorization to set
	 */
	public void setThirdPartyAuthorization(
			ThirdPartyAuthorization thirdPartyAuthorization) {
		this.thirdPartyAuthorization = thirdPartyAuthorization;
	}

	public String realize(CertificateCredential credential) {
		String payLoad = null;
		if (thirdPartyAuthorization instanceof TokenAuthorization) {
			payLoad = tokenAuthPayLoad();
		} else if (thirdPartyAuthorization instanceof SubjectAuthorization) {
			authPayLoad(credential,
					(SubjectAuthorization) thirdPartyAuthorization);
		} else {
			authPayLoad(credential, null);
		}
		return payLoad;
	}

	/**
	 * Returns a empty soap header String, token authorization does not bear a
	 * credential part
	 * 
	 * @return
	 */
	private String tokenAuthPayLoad() {
		String payLoad = null;
		StringBuilder soapMsg = new StringBuilder();
		soapMsg.append("<soapenv:Header>");
		soapMsg.append("<urn:RequesterCredentials/>");
		soapMsg.append("</soapenv:Header>");
		return payLoad;
	}

	private String authPayLoad(CertificateCredential credential,
			SubjectAuthorization subjectAuth) {
		String payLoad = null;
		StringBuilder soapMsg = new StringBuilder();
		soapMsg.append("<soapenv:Header>");
		soapMsg.append("<urn:RequesterCredentials>");
		soapMsg.append("<ebl:Credentials>");
		soapMsg.append("<ebl:Username>" + credential.getUserName()
				+ "</ebl:Username>");
		soapMsg.append("<ebl:Password>" + credential.getPassword()
				+ "</ebl:Password>");

		// Append subject credential if available
		if (subjectAuth != null) {
			soapMsg.append("<ebl:Subject>" + subjectAuth.getSubject()
					+ "</ebl:Subject>");
		}
		soapMsg.append("</ebl:Credentials>");
		soapMsg.append("</urn:RequesterCredentials>");
		soapMsg.append("</soapenv:Header>");
		return payLoad;
	}

}
