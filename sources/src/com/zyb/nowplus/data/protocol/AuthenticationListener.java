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
package com.zyb.nowplus.data.protocol;

/**
 * 
 * Notifies the listener as soon as an authentication with the backend 
 * succeeded or failed.
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public interface AuthenticationListener {	
	public static final int	UNKNOWN = 1,
							ACTIVATION_TIMED_OUT = 2,
							
							USERNAME_MISSING = 3,
							USERNAME_BLACKLISTED = 4,
							USERNAME_FORBIDDEN = 5,
							USERNAME_IN_USE = 6,
							FULL_NAME_MISSING = 7,
							PASSWORD_MISSING = 8,
							PASSWORD_INVALID = 9,
							ACCEPT_TC_MISSING = 10,
							D_O_B_INVALID = 11,
							EMAIL_MISSING = 12,
							EMAIL_INVALID = 13,
							COUNTRY_INVALID = 14,
							MSISDN_MISSING = 15,
							MSISDN_INVALID = 16,
							TIMEZONE_MISSING = 17,
							TIMEZONE_INVALID = 18,
							MOBILE_OPERATOR_INVALID = 19,
							MOBILE_MODEL_INVALID = 20,
							LANGUAGE_INVALID = 21,
							IP_ADDRESS_MISSING = 22,
							INTERNAL_ERROR = 23;
	public void registrationFailed(int errorCode);
	public void registrationSucceeded(long userID);
		
	public void authenticationSucceeded();
	
	public static final int AUTH_FAILED_UNKNOWN = 1,
							AUTH_INVALID_CREDENTIALS = 2,
							AUTH_NEW_PASSWORD = 3;
							
	public void authenticationFailed(int authCode);
	
	public void userDisallowedConnection();
}
