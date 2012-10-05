package com.paypal.core.soap;

import com.paypal.core.AuthenticationStrategy;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.core.credential.TokenAuthorization;
import com.paypal.exception.InvalidCredentialException;

public class SignatureSOAPHeaderAuthStrategy implements
		AuthenticationStrategy<String, SignatureCredential> {

	private final String rawPayLoad;

	private ThirdPartyAuthorization thirdPartyAuthorization;

	public SignatureSOAPHeaderAuthStrategy(String rawPayLoad) {
		this.rawPayLoad = rawPayLoad;
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
		if (thirdPartyAuthorization != null
				&& thirdPartyAuthorization instanceof TokenAuthorization) {
			payLoad = tokenAuthPayLoad();
		} else if (thirdPartyAuthorization != null
				&& thirdPartyAuthorization instanceof SubjectAuthorization) {
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
		soapMsg.append(getSoapEnvelopeStart());
		soapMsg.append("<soapenv:Header>");
		soapMsg.append("<urn:RequesterCredentials/>");
		soapMsg.append("</soapenv:Header>");
		soapMsg.append(getSoapBody());
		soapMsg.append(getSoapEnvelopeEnd());
		return payLoad;
	}

	private String authPayLoad(SignatureCredential credential,
			SubjectAuthorization subjectAuth) {
		String payLoad = null;
		StringBuilder soapMsg = new StringBuilder();
		soapMsg.append(getSoapEnvelopeStart());
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
		soapMsg.append(getSoapBody());
		soapMsg.append(getSoapEnvelopeEnd());
		return payLoad;
	}

	private String getSoapEnvelopeStart() {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:ebay:api:PayPalAPI\" xmlns:ebl=\"urn:ebay:apis:eBLBaseComponents\" xmlns:cc=\"urn:ebay:apis:CoreComponentTypes\" xmlns:ed=\"urn:ebay:apis:EnhancedDataTypes\">";
	}

	private String getSoapEnvelopeEnd() {
		return "</soapenv:Envelope>";
	}

	private String getSoapBody() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<soapenv:Body>");
		stringBuilder.append(rawPayLoad);
		stringBuilder.append("</soapenv:Body>");
		return stringBuilder.toString();
	}

}
