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


public interface ServiceObject {
	public static byte CONTACT = 1,
						ACTIVITY = 2,
						MESSAGE = 3,
						IMAGE = 4,
						FRIENDS_OF_FRIEND_LIST = 5,
						USER_PROFILE = 6,
						SEARCH_USER_PROFILE_RESULT = 7, 
						CONTACT_DELETION = 8,
						ADD_BUDDY_RESULT = 9,
						INVITE_NEW_USER_RESULT = 10,
						ADD_CONTACT_RESULT = 11,
						START_CHAT_CONVERSATION = 12,
						STOP_CHAT_CONVERSATION = 13,
						CHAT_MESSAGE = 14,
						TERMS_AND_CONDITIONS = 15,
						CONTACT_CHANGES = 16,
						MY_CHANGES = 17,
						DELETE_CONTACT_DETAILS_RESULT = 18,
						UPDATE_CONTACT_DETAILS_RESULT = 19,
						BULK_UPDATE_CONTACTS_RESULT = 20,
						MY_IDENTITY = 21,
						AVAILABLE_IDENTITY = 22,
						ADD_IDENTITY_RESULT = 23,
						SET_IDENTITY_STATUS_RESULT = 24,
						DELETE_IDENTITY_RESULT = 25,
						SET_ME_RESULT = 26,
						GROUPS = 27,
						SET_CONTACT_GROUP_RELATIONS_RESULT = 28,
						TCP_TEST_PACKET = 29;
	
	public Hashtable toHashtable();
}
