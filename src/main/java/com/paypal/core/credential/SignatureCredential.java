package com.paypal.core.credential;

/**
 * <code>SignatureCredential</code> encapsulates signature credential
 * information used by service authentication systems
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

	/**
	 * Signature Credential
	 * 
	 * @param userName
	 *            UserName
	 * @param password
	 *            Password
	 * @param applicationId
	 *            Application ID used for PLATFORM APIs, it will be ignored in
	 *            case of MERCHANT APIs
	 * @param signature
	 *            Signature
	 */
	public SignatureCredential(String userName, String password,
			String applicationId, String signature) {
		super();
		if (userName == null || userName.trim().length() == 0
				|| password == null || password.trim().length() == 0
				|| signature == null || signature.trim().length() == 0) {
			throw new IllegalArgumentException(
					"SignatureCredential arguments cannot be empty or null");
		}
		this.userName = userName;
		this.password = password;
		this.applicationId = applicationId;
		this.signature = signature;
	}

	/**
	 * Signature Credential
	 * 
	 * @param userName
	 *            UserName
	 * @param password
	 *            Password
	 * @param applicationId
	 *            Application ID used for PLATFORM APIs
	 * @param signature
	 *            Signature
	 * @param thirdPartyAuthorization
	 *            {@link ThirdPartyAuthorization} instance
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
	 * @param applicationId
	 *            the applicationId to set, ignored for MERCHANT APIs
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
	 * Sets any instance of {@link ThirdPartyAuthorization}. Can be an Instance
	 * of {@link TokenAuthorization} or {@link SubjectAuthorization}.
	 * {@link SubjectAuthorization} is used in MERCHANT APIs
	 * 
	 * @param thirdPartyAuthorization
	 *            the thirdPartyAuthorization to set
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
