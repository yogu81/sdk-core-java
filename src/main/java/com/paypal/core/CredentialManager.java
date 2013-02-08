package com.paypal.core;

import java.util.Map;
import java.util.Map.Entry;

import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.ICredential;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.credential.SubjectAuthorization;
import com.paypal.core.credential.ThirdPartyAuthorization;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;

/**
 * <code>CredentialManager</code> acts as a factory for loading
 * {@link ICredential} credential configured in application properties
 * 
 */
public final class CredentialManager {

	/**
	 * Singleton instance
	 */
	private static CredentialManager instance;

	// Private Constructor
	private CredentialManager() {

	}

	/**
	 * Singleton accessor method
	 * 
	 * @return Singleton instance of {@link CredentialManager}
	 */
	public static CredentialManager getInstance() {
		synchronized (CredentialManager.class) {
			if (instance == null) {
				instance = new CredentialManager();
			}
		}
		return instance;
	}

	public ICredential getCredentialObject(String userId)
			throws MissingCredentialException, InvalidCredentialException {
		ICredential credential = null;
		ConfigManager conf = ConfigManager.getInstance();
		if (conf.getNumOfAcct().size() == 0) {
			throw new MissingCredentialException(
					"No API accounts have been configured in application properties");
		}
		String prefix = Constants.ACCOUNT_PREFIX;
		Map<String, String> credMap = conf.getValuesByCategory(prefix);
		if (userId != null && userId.trim().length() != 0) {
			for (Entry<String, String> entry : credMap.entrySet()) {
				if (entry.getKey().endsWith(
						Constants.CREDENTIAL_USERNAME_SUFFIX)) {
					if (entry.getValue().equalsIgnoreCase(userId)) {
						String acctKey = entry.getKey().substring(0,
								entry.getKey().indexOf('.'));
						credential = returnCredential(credMap, acctKey);
					}

				}
			}
			if (credential == null) {
				throw new MissingCredentialException(
						"Account for the username does not exists in the properties file");
			}
		} else {
			int index = 1;
			String userName = (String) credMap.get(prefix + index
					+ Constants.CREDENTIAL_USERNAME_SUFFIX);
			if (userName != null && userName.trim().length() != 0) {
				credential = returnCredential(credMap, prefix + index);
			} else {
				throw new MissingCredentialException(
						"Associate valid account for index 1");
			}
		}
		return credential;
	}

	private ICredential returnCredential(Map<String, String> credMap,
			String acctKey) throws InvalidCredentialException {
		ICredential credential = null;
		String userName = (String) credMap.get(acctKey
				+ Constants.CREDENTIAL_USERNAME_SUFFIX);
		String password = (String) credMap.get(acctKey
				+ Constants.CREDENTIAL_PASSWORD_SUFFIX);
		String appId = (String) credMap.get(acctKey
				+ Constants.CREDENTIAL_APPLICATIONID_SUFFIX);
		String subject = (String) credMap.get(acctKey
				+ Constants.CREDENTIAL_SUBJECT_SUFFIX);
		if (credMap.get(acctKey + Constants.CREDENTIAL_SIGNATURE_SUFFIX) != null) {
			String signature = (String) credMap.get(acctKey
					+ Constants.CREDENTIAL_SIGNATURE_SUFFIX);
			credential = new SignatureCredential(userName, password, signature);
			((SignatureCredential) credential).setApplicationId(appId);
			if (subject != null && subject.trim().length() > 0) {
				ThirdPartyAuthorization thirdPartyAuthorization = new SubjectAuthorization(
						subject);
				((SignatureCredential) credential)
						.setThirdPartyAuthorization(thirdPartyAuthorization);
			}
		} else if (credMap.get(acctKey + Constants.CREDENTIAL_CERTPATH_SUFFIX) != null) {
			String certPath = (String) credMap.get(acctKey
					+ Constants.CREDENTIAL_CERTPATH_SUFFIX);
			String certKey = (String) credMap.get(acctKey
					+ Constants.CREDENTIAL_CERTKEY_SUFFIX);
			credential = new CertificateCredential(userName, password,
					certPath, certKey);
			((CertificateCredential) credential).setApplicationId(appId);
			if (subject != null && subject.trim().length() > 0) {
				ThirdPartyAuthorization thirdPartyAuthorization = new SubjectAuthorization(
						subject);
				((CertificateCredential) credential)
						.setThirdPartyAuthorization(thirdPartyAuthorization);
			}
		} else {
			throw new InvalidCredentialException(
					"The account does not have a valid credential type(signature/certificate)");
		}
		return credential;

	}
}
