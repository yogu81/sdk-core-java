package com.paypal.core.credential;

/**
 * <code>CertificateCredential</code> encapsulates certificate credential information
 * used by service authentication systems.
 * @author kjayakumar
 *
 */
public class CertificateCredential implements ICredential {

	/**
	 * Username credential
	 */
	private String userName;

	/**
	 * Password credential
	 */
	private String password;

	/**
	 * Application Id (Used by Platform APIs)
	 */
	private String applicationId;

	/**
	 * Certificate path
	 */
	private String certificatePath;

	/**
	 * Certificate key
	 */
	private String certificateKey;

	/**
	 * {@link ThirdPartyAuthorization} instance
	 */
	private ThirdPartyAuthorization thirdPartyAuthorization;

	/**
	 * Certificate Credential
	 * @param userName
	 * @param password
	 * @param applicationId
	 * @param certificatePath
	 * @param certificateKey
	 */
	public CertificateCredential(String userName, String password,
			String applicationId, String certificatePath, String certificateKey) {
		super();
		this.userName = userName;
		this.password = password;
		this.applicationId = applicationId;
		this.certificatePath = certificatePath;
		this.certificateKey = certificateKey;
	}

	/**
	 * Certificate Credential
	 * @param userName
	 * @param password
	 * @param applicationId
	 * @param certificatePath
	 * @param certificateKey
	 * @param thirdPartyAuthorization
	 */
	public CertificateCredential(String userName, String password,
			String applicationId, String certificatePath,
			String certificateKey,
			ThirdPartyAuthorization thirdPartyAuthorization) {
		this(userName, password, applicationId, certificatePath, certificateKey);
		this.thirdPartyAuthorization = thirdPartyAuthorization;
	}

	/**
	 * @return the applicationId
	 */
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * @param applicationId the applicationId to set
	 */
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * @return the thirdPartyAuthorization
	 */
	public ThirdPartyAuthorization getThirdPartyAuthorization() {
		return thirdPartyAuthorization;
	}

	/**
	 * Sets any instance of {@link ThirdPartyAuthorization}.
	 * @param thirdPartyAuthorization the thirdPartyAuthorization to set
	 */
	public void setThirdPartyAuthorization(
			ThirdPartyAuthorization thirdPartyAuthorization) {
		this.thirdPartyAuthorization = thirdPartyAuthorization;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the certificatePath
	 */
	public String getCertificatePath() {
		return certificatePath;
	}

	/**
	 * @return the certificateKey
	 */
	public String getCertificateKey() {
		return certificateKey;
	}

}
