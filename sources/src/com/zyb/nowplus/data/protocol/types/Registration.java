/*******************************************************************************
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 *  include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 ******************************************************************************/
package com.zyb.nowplus.data.protocol.types;

import java.util.Hashtable;

import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;

/**
 * 
 * Takes care of sending out a new registration for a user, constructing
 * the object that holds the activation code and for constructing the 
 * confirmation request payload to confirm the registration process by
 * passing the activation SMS' payload to the backend.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public class Registration implements ServiceObject {
	private static final int UNKNOWN = -1;
	private static final String	KEY_USERNAME = "username",
								KEY_PASSWORD = "password",
								KEY_FULL_NAME = "fullname",
								KEY_DATE = "birthdate",
								KEY_ACCEPTED_T_A_CS = "acceptedtandc",
								KEY_SUBSCRIBE_NEWSLETTER = "subscribetonewsletter",
								KEY_MOBILE_MODEL_ID = "mobilemodelid",
								KEY_MOBILE_OPERATOR_ID = "mobileoperatorid",
								KEY_COUNTRY_CODE = "countrycode",
								KEY_USER_EMAIL_ADDR_CODE ="email",
								KEY_MSISDN = "msisdn",
								KEY_TIMEZONE = "timezone",
								KEY_LANGUAGE = "language",
								KEY_CONFIRMATION_SMS = "sendconfirmationsms",
								KEY_TIMESTAMP = "timestamp",
								
								KEY_VALUE = "value",
								KEY_FLAGS = "flags",
								KEY_CODE = "code";	
	
	
	// variables for the signupUser method
	private String username, password, fullName, date, msisdn,
			countryCode,userEmailAddr, timezone, language;
	private boolean acceptedTermsAndConditions, subscribedToNewsletter;
	private int mobileModelID, mobileOperatorID;
	
	//variables for the requestActivationCode method.
	// the variable username is also used for this method.
	private String value;
	private String activationCode;
	private int flags;
	
	/**
	 * 
	 * The constructor for registering a new user on the Now+ service.
	 * 
	 * @param username The username of the user.
	 * @param password The password for the user.
	 * @param fullName The full name. Optional, pass null 
	 * if not wanted.
	 * @param date The date of birth in the format YYYY-DD-MMTHH:MM:SS
	 * @param msisdn The msisdn to send the activation code to.
	 * @param acceptedTermsAndConditions True if the user accepted 
	 * the terms and conditions.
	 * @param countryCode The country code, e.g. "de".
	 * @param timezone The timezone, e.g. "Europe/Berlin"
	 * @param language The language, e.g. "de-DE"
	 * @param mobileOperatorID The mobile operator ID, TBC.
	 * @param mobileModelID The mobile model ID, TBC.
	 * @param subscribedToNewsletter True if the user wants to have the
	 * newsletter.
	 * 
	 */
	public Registration(String username, 
						String password, 
						String fullName, 
						String date, 
						String msisdn, 
						boolean acceptedTermsAndConditions, 
						String countryCode,
						String userEmailAddr,  
						String timezone, 
						String language, 
						int mobileOperatorID,
						int mobileModelID, 
						boolean subscribedToNewsletter) 
	{
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.date = date;
		this.msisdn = msisdn;
		this.acceptedTermsAndConditions = acceptedTermsAndConditions;
		this.subscribedToNewsletter = subscribedToNewsletter;
		this.mobileModelID = mobileModelID;
		this.mobileOperatorID = mobileOperatorID;
		this.countryCode = countryCode;
		this.userEmailAddr=userEmailAddr;
		this.timezone = timezone;
		this.language = language;
		this.flags = 8;
	}
	
	/**
	 * 
	 * Constructor for rerequesting the activation SMS.
	 * 
	 * @param username The username to rerequest for.
	 * @param msisdn The MSISDN to request the SMS for.
	 * 
	 */
	public Registration(String username, String msisdn) {
		this.username = username;
		this.value = msisdn;
		this.flags = 8;
		this.mobileModelID = UNKNOWN;
		this.mobileOperatorID = UNKNOWN;
	}
	
	/**
	 * 
	 * Constructor for sending the activation payload to the
	 * backend to complete a mobile registration.
	 * 
	 * @param activationCode The payload of the SMS to 
	 * confirm to the backend.
	 * 
	 */
	public Registration(String activationCode) {
		this.activationCode = activationCode;
		this.flags = 8;
	}
	
	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		if (flags == 8) {
			ht.put(KEY_FLAGS, new Integer(flags));
		}
		if (null != activationCode) {
			ht.put(KEY_CODE, activationCode);
		}
		if (null != value) {
			ht.put(KEY_VALUE, value);
		}
		if (null != username) {
			ht.put(KEY_USERNAME, username);
		}
		if ((null != password) && (null != username)) {
			//#debug debug
			System.out.println("Creating encrypted Pass with uID " + username);
			long ts = ((long) System.currentTimeMillis() / 1000);
			byte[] cryptedPass = Toolkit.getEncryptedPassword(
											ts, username, password);
			if (null != cryptedPass) {
				ht.put(KEY_PASSWORD, cryptedPass);
				ht.put(KEY_TIMESTAMP, new Long(ts));
			} else {
				//#debug error
				System.out.println("Encrypted password was null!!");
			}
			cryptedPass = null;
		}
		if (null != fullName) {
			ht.put(KEY_FULL_NAME, fullName);
		}
		if (null != date) {
			ht.put(KEY_DATE, date);
		}
		if (null != msisdn) {
			ht.put(KEY_MSISDN, msisdn);
			
			ht.put(KEY_ACCEPTED_T_A_CS, 
					new Boolean(acceptedTermsAndConditions));
			ht.put(KEY_SUBSCRIBE_NEWSLETTER, 
					new Boolean(subscribedToNewsletter));
			ht.put(KEY_CONFIRMATION_SMS, 
					new Boolean(true));
		}
		if (UNKNOWN != mobileModelID) {
			ht.put(KEY_MOBILE_MODEL_ID, new Integer(mobileModelID));
		}
		if (UNKNOWN != mobileOperatorID) {
			ht.put(KEY_MOBILE_OPERATOR_ID, 
								new Integer(mobileOperatorID));
		}
		if (null != countryCode) {
			ht.put(KEY_COUNTRY_CODE, countryCode);
		}
		if (null != userEmailAddr) {
			ht.put(KEY_USER_EMAIL_ADDR_CODE, userEmailAddr);
		}
		if (null != timezone) {
			ht.put(KEY_TIMEZONE, timezone);
		}
		if (null != language) {
			ht.put(KEY_LANGUAGE, language);
		}	
		
		return ht;
	}

}
