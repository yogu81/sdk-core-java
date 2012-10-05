package com.paypal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.ICredential;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.soap.SubjectAuthorization;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;

/**
 * <code>CredentialManager</code> acts as a factory for loading
 * {@link ICredential} credential configured in application properties
 * 
 * @author kjayakumar
 * 
 */
public final class CredentialManager {

	/**
	 * Singleton instance
	 */
	private static CredentialManager instance;

	/**
	 * Credential map
	 */
	private Map<String, ICredential> credentialMap;

	/**
	 * Default account name
	 */
	private String defaultAcctName = null;

	// Private Constructor
	private CredentialManager() throws MissingCredentialException {
		credentialMap = new HashMap<String, ICredential>();
		this.initCredential();
	}

	/**
	 * Singleton accessor method
	 * 
	 * @return
	 * @throws MissingCredentialException
	 */
	public static CredentialManager getInstance()
			throws MissingCredentialException {
		synchronized (CredentialManager.class) {
			if (instance == null) {
				instance = new CredentialManager();
			}
		}
		return instance;
	}

	/**
	 * Loads credentials of multiple accounts from property file.
	 * 
	 * @throws MissingCredentialException
	 */
	private void initCredential() throws MissingCredentialException {
		ConfigManager conf = ConfigManager.getInstance();
		int suffix = 1;
		String prefix = Constants.ACCCOUT_PREFIX;
		Map<String, String> credMap = conf.getValuesByCategory(prefix);
		Set<String> acctSet = conf.getNumOfAcct();
		if (acctSet.size() == 0) {
			throw new MissingCredentialException(
					"No valid API accounts have been configured");
		}
		while (suffix <= acctSet.size()) {
			String userName = (String) credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_USERNAME_SUFFIX);
			String password = (String) credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_PASSWORD_SUFFIX);
			String appId = (String) credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_APPLICACTIONID_SUFFIX);
			String subject = (String) credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_SUBJECT_SUFFIX);
			if (credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_SIGNATURE_SUFFIX) != null) {
				String signature = (String) credMap.get(prefix + suffix
						+ Constants.CREDENTIAL_SIGNATURE_SUFFIX);
				SignatureCredential credential = new SignatureCredential(
						userName, password, appId, signature);
				if (subject != null && subject.length() > 0) {
					ThirdPartyAuthorization thirdPartyAuthorization = new SubjectAuthorization(
							subject);
					credential
							.setThirdPartyAuthorization(thirdPartyAuthorization);
				}
				credentialMap.put(userName, credential);
			} else if (credMap.get(prefix + suffix
					+ Constants.CREDENTIAL_CERTPATH_SUFFIX) != null) {
				String certPath = (String) credMap.get(prefix + suffix
						+ Constants.CREDENTIAL_CERTPATH_SUFFIX);
				String certKey = (String) credMap.get(prefix + suffix
						+ Constants.CREDENTIAL_CERTKEY_SUFFIX);
				CertificateCredential credential = new CertificateCredential(
						userName, password, appId, certPath, certKey);
				if (subject != null && subject.length() > 0) {
					ThirdPartyAuthorization thirdPartyAuthorization = new SubjectAuthorization(
							subject);
					credential
							.setThirdPartyAuthorization(thirdPartyAuthorization);
				}
				credentialMap.put(userName, credential);
			}
			if (defaultAcctName == null) {
				defaultAcctName = (String) credMap.get(prefix + suffix
						+ Constants.CREDENTIAL_USERNAME_SUFFIX);
			}
			suffix++;
		}

	}

	/**
	 * Obtains credential object based on userId
	 * 
	 * @param userId
	 *            Username string
	 * @return {@link ICredential} object
	 * @throws InvalidCredentialException
	 */
	public ICredential getCredentialObject(String userId)
			throws InvalidCredentialException {
		ICredential credObj = null;
		if (userId == null) {
			credObj = (ICredential) credentialMap.get(defaultAcctName);
		} else if (credentialMap.containsKey(userId)) {
			credObj = (ICredential) credentialMap.get(userId);
		}
		if (credObj == null) {
			throw new InvalidCredentialException("Invalid userId" + userId);
		}
		return credObj;

	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
