package com.paypal.core.soap;

import com.paypal.core.AuthenticationStrategy;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.SubjectAuthorization;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.core.credential.TokenAuthorization;
import com.paypal.exception.InvalidCredentialException;

public class SignatureSOAPHeaderAuthStrategy implements
		AuthenticationStrategy<String, SignatureCredential> {

	private ThirdPartyAuthorization thirdPartyAuthorization;

	public SignatureSOAPHeaderAuthStrategy() {
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

	public String realize(SignatureCredential credential) {
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

	private String tokenAuthPayLoad() {
		String payLoad = null;
		StringBuilder soapMsg = new StringBuilder();
		soapMsg.append("<soapenv:Header>");
		soapMsg.append("<urn:RequesterCredentials/>");
		soapMsg.append("</soapenv:Header>");
		return payLoad;
	}

	private String authPayLoad(SignatureCredential credential,
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
		soapMsg.append("<ebl:Signature>" + credential.getSignature()
				+ "</ebl:Signature>");
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
