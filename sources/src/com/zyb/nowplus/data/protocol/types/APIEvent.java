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

public class APIEvent implements ServiceObject {
	private static final String KEY_PAYLOAD = "payload",
								KEY_TYPE = "type";
								// KEY_USERID = "userid"; // we have a user id of ourself, do we need it?
	private static final String TYPE_PRESENCE = "pp",
								TYPE_START_CONV = "c1",	
								TYPE_CHAT_MESSAGE = "cm",
								
								TYPE_PROFILE_CHANGE = "pc",
								TYPE_CONTACTS_CHANGE = "cc",
								TYPE_TIMELINE_CHANGE = "atl",
								TYPE_STATUS_CHANGE = "ast",
								TYPE_GROUP_CHANGE = "gc",
								TYPE_IDENTITY_CHANGE = "ic";
	public static final int UNKNOWN = 0,
							PRESENCE = 1,
							START_CONVERSATION = 2,
							CHAT_MESSAGE = 3,
							
							PROFILE_CHANGE = 4,
							CONTACTS_CHANGE = 5,
							TIMELINE_CHANGE = 6,
							STATUS_CHANGE = 7,
							IDENTITY_CHANGE = 8,
							GROUP_CHANGE = 9;
	private int apiEventType;
	private ServiceObject so;

	public APIEvent() {
	}
	
	public APIEvent(int apiEventType) {
		this.apiEventType = apiEventType;
	}
	
	public void parseAPIEvent(Hashtable ht) {
		if (null == ht) {
			return;
		}
		
		apiEventType = UNKNOWN;
		
		String type = null;
		Object payload = null;
		
		//#debug info
		System.out.println("Parsing API Evt: " + ht.toString());
		
		try {
			type = ((String) ht.get(KEY_TYPE));
			payload = ht.get(KEY_PAYLOAD);
				
			//#debug info
			System.out.println("API Evt Payload is not null.");
			
			if (TYPE_PRESENCE.equals(type)) {
				apiEventType = PRESENCE;
				
				if (null != payload) {
					Hashtable htUsers = (Hashtable) payload;
					so = new Presence(htUsers);
				}
			} else if ((TYPE_CHAT_MESSAGE.equals(type)) ||
						(TYPE_START_CONV.equals(type))) {
				if (TYPE_CHAT_MESSAGE.equals(type)) {
					apiEventType = CHAT_MESSAGE;
				} else {
					apiEventType = START_CONVERSATION;
				}
				
				//#debug info
				System.out.println("Chat message or start conv.");
				
				if (null != payload) {
					Hashtable htChat = (Hashtable) payload;
					if (null != htChat) {
						//#debug info
						System.out.println("Payload hashtable not null.");
						
						so = new ChatObject(htChat);
					}
				}
			} else if (TYPE_CONTACTS_CHANGE.equals(type)) {
				//#debug info
				System.out.println("Contacts Changes");
				apiEventType = CONTACTS_CHANGE;
			} else if (TYPE_PROFILE_CHANGE.equals(type)) {
				//#debug info
				System.out.println("Profile Changes");
				apiEventType = PROFILE_CHANGE;
			} else if (TYPE_STATUS_CHANGE.equals(type)) {
				//#debug info
				System.out.println("Friendstream Changes");
				apiEventType = STATUS_CHANGE;
			} else if (TYPE_TIMELINE_CHANGE.equals(type)) {
				//#debug info
				System.out.println("TIMELINE Changes");
				apiEventType = TIMELINE_CHANGE;
			} else if (TYPE_GROUP_CHANGE.equals(type)) {
				//#debug info
				System.out.println("Group changes");
				apiEventType = GROUP_CHANGE;
			} else if (TYPE_IDENTITY_CHANGE.equals(type)) {
				//#debug info
				System.out.println("Idenitity Changes");
				apiEventType = IDENTITY_CHANGE;
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Failed parsing API Event." + e);
		}
	}
	
	public int getType() {
		return apiEventType;
	}
	
	public ServiceObject getServiceObject() {
		return so;
	}
	
	/**
	 * Left empty. API Events are just pushed. 
	 */
	public Hashtable toHashtable() {
		return null;
	}

}
