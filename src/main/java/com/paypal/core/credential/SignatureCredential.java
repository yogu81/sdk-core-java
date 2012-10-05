package com.paypal.core.credential;

/**
 * <code>SignatureCredential</code> encapsulates signature credential information 
 * used by service authentication systems
 *
 */
public class SignatureCredential implements ICredential {

	/**
	 * Username
	 */
	private String userName;

	/**
	 * Password
	 */
	private String password;

	/**
	 * Application ID
	 */
	private String applicationId;

	/**
	 * Signature
	 */
	private String signature;

	/**
	 * {@link ThirdPartyAuthorization} instance
	 */
	private ThirdPartyAuthorization thirdPartyAuthorization;

	public SignatureCredential(String userName, String password,
			String applicationId, String signature) {
		super();
		this.userName = userName;
		this.password = password;
		this.applicationId = applicationId;
		this.signature = signature;
	}

	/**
	 * @param userName
	 * @param password
	 * @param applicationId
	 * @param signature
	 * @param thirdPartyAuthorization
	 */
	public SignatureCredential(String userName, String password,
			String applicationId, String signature,
			ThirdPartyAuthorization thirdPartyAuthorization) {
		this(userName, password, applicationId, signature);
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
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}

}
