package com.paypal.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.paypal.core.credential.ICredential;

/**
 * <code>DefaultSOAPAPICallHandler</code> acts a basic SOAP
 * {@link APICallPreHandler}. The interface method returns defaults for HTTP
 * headers which is an empty {@link Map}. SOAP:HEADERS to be added should be
 * included in headerString parameter and namespace to be added to SOAP:ENVELOPE
 * should added in namespace parameter.
 * 
 */
public class DefaultSOAPAPICallHandler implements APICallPreHandler {

	/**
	 * SOAP Envelope Message Formatter String start
	 */
	private static final String SOAP_ENV_START = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" {0}>";

	/**
	 * SOAP Envelope Message Formatter String end
	 */
	private static final String SOAP_ENV_END = "</soapenv:Envelope>";

	/**
	 * SOAP Header Message Formatter String start
	 */
	private static final String SOAP_HEAD_START = "<soapenv:Header>{1}";

	/**
	 * SOAP Header Message Formatter String end
	 */
	private static final String SOAP_HEAD_END = "</soapenv:Header>";

	/**
	 * SOAP Body Message Formatter String start
	 */
	private static final String SOAP_BODY_START = "<soapenv:Body>{2}";

	/**
	 * SOAP Body Message Formatter String end
	 */
	private static final String SOAP_BODY_END = "</soapenv:Body>";

	/**
	 * Raw payload from stubs
	 */
	private String rawPayLoad;

	/**
	 * Header element as String
	 */
	private String headerString;

	/**
	 * Namespace attributes as String
	 */
	private String namespaces;

	/**
	 * @return the headerString
	 */
	public String getHeaderString() {
		return headerString;
	}

	/**
	 * @param headerString
	 *            the headerString to set
	 */
	public void setHeaderString(String headerString) {
		this.headerString = headerString;
	}

	/**
	 * @return the namespaces
	 */
	public String getNamespaces() {
		return namespaces;
	}

	/**
	 * @param namespaces
	 *            the namespaces to set
	 */
	public void setNamespaces(String namespaces) {
		this.namespaces = namespaces;
	}

	/**
	 * DefaultSOAPAPICallHandler acts as the base SOAPAPICallHandler.
	 * 
	 * @param rawPayLoad
	 *            Raw SOAP payload that goes into SOAP:BODY
	 * @param namespaces
	 *            Namespace attributes that should be appended to SOAP:ENVELOPE,
	 *            this argument can take any valid String value, empty String or
	 *            NULL. If the value is NULL {0} value is sandwiched between
	 *            SOAP:HEADER element that can be used for decorating purpose
	 * @param headerString
	 *            SOAP header String that should be appended to SOAP:HEADER,
	 *            this argument can take any valid String value, empty String or
	 *            NULL. If the value is NULL {1} value is sandwiched between
	 *            SOAP:HEADER element that can be used for decorating purpose
	 */
	public DefaultSOAPAPICallHandler(String rawPayLoad, String namespaces,
			String headerString) {
		super();
		this.rawPayLoad = rawPayLoad;
		this.namespaces = namespaces;
		this.headerString = headerString;
	}

	public Map<String, String> getHeaderMap() {
		return new HashMap<String, String>();
	}

	public String getPayLoad() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getSoapEnvelopeStart());
		stringBuilder.append(getSoapHeaderStart());
		stringBuilder.append(getSoapHeaderEnd());
		stringBuilder.append(getSoapBodyStart());
		stringBuilder.append(getSoapBodyEnd());
		stringBuilder.append(getSoapEnvelopeEnd());
		return stringBuilder.toString();
	}

	public String getEndPoint() {
		return ConfigManager.getInstance().getValue("service.EndPoint");
	}

	public ICredential getCredential() {
		return null;
	}

	private String getSoapEnvelopeStart() {
		String envelope = null;
		if (namespaces != null) {
			envelope = MessageFormat.format(SOAP_ENV_START,
					new Object[] { namespaces });
		} else {
			envelope = SOAP_ENV_START;
		}
		return envelope;
	}

	private String getSoapEnvelopeEnd() {
		return SOAP_ENV_END;
	}

	private String getSoapHeaderStart() {
		String header = null;
		if (headerString != null) {
			header = MessageFormat.format(SOAP_HEAD_START, new Object[] { null,
					headerString });
		} else {
			header = SOAP_HEAD_START;
		}
		return header;
	}

	private String getSoapHeaderEnd() {
		return SOAP_HEAD_END;
	}

	private String getSoapBodyStart() {
		String body = null;
		if (rawPayLoad != null) {
			body = MessageFormat.format(SOAP_BODY_START, new Object[] { null,
					null, rawPayLoad });
		} else {
			body = SOAP_BODY_START;
		}
		return body;
	}

	private String getSoapBodyEnd() {
		return SOAP_BODY_END;
	}

	// TODO remove
	public static void main(String[] args) {
		DefaultSOAPAPICallHandler handler = new DefaultSOAPAPICallHandler(
				"Payload", 
				"xmlns:urn=\"urn:ebay:api:PayPalAPI\"", "<my>test</my>");
		System.out.println(handler.getPayLoad());
	}

}
