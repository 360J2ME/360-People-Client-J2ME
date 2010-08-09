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
import java.util.Vector;

/**
 * Used to start a conversation, to create a new chat message
 * or to parse a retrieved chat message.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public class ChatObject implements ServiceObject {
	private static final String  KEY_CONVERSATION = "conversation",
								KEY_TOS = "tos",
								KEY_FROM = "from",
								KEY_USERS = "users",
								KEY_BODY = "body";
	private static final byte T_SEND_MSG = 0,
								T_CREATE_CONV = 1,
								T_STOP_CONV = 2;
	
	private String conversationID;
	private String fromNetwork;
	private String fromName;
	private String[] networks;
	private String[] names;
	private String body;
	private byte chatObjectType;
	
	
	
	
	/**
	 * Constructor used for sending a chat message.
	 * 
	 * @param conversationID The conversation ID to send the chat
	 * message under.
	 * @param networks The networks to send the message over, 
	 * e.g. google.
	 * @param names The names to send the message to, 
	 * e.g. user id or an identifier name like rudy@gtalk.com.
	 * @param body The body contains all the text to send.
	 * 
	 */
	public ChatObject(String conversationID, String[] networks, 
								String[] names, String body) {
		chatObjectType = T_SEND_MSG;	// we are sending a message
		
		this.conversationID = conversationID;
		this.networks = networks;
		this.names = names;
		this.body = body;
	}

	/**
	 * 
	 * Constructor that creates a conversation.
	 * 
	 * @param networks The networks to start the conversation
	 * on.
	 * @param names The names to start the conversation
	 * for. Either user ID or the network identifier.
	 * 
	 */
	public ChatObject(String[] networks, String[] names) {
		chatObjectType = T_CREATE_CONV;	// start conversation
		
		this.networks = networks;
		this.names = names;
	}
	
	/**
	 * 
	 * Constructs a chat object to stop the conversation.
	 * 
	 * @param conversationID The ID to stop the conversation for.
	 */
	public ChatObject(String conversationID) {
		chatObjectType = T_STOP_CONV;	// stop conversation
		
		this.conversationID = conversationID;
	}
	
	/**
	 * 
	 * Constructs a chat object resembling an incoming chat message.
	 * 
	 * @param htPush The Hashtable resembling the incoming chat message.
	 * 
	 */
	public ChatObject(Hashtable htPush) {
		try {
			conversationID = ((String) htPush.get(KEY_CONVERSATION));
			
			//#debug info
			System.out.println("Conversation ID: " + conversationID);
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast conv ID.");
		}
		
		try {
			String from = (String) htPush.get(KEY_FROM);
			int i = from.indexOf(':');
			if (i < 0) {	// it's a now+ network chat
				//#debug info
				System.out.println("Now+ Chat with " + from);
				fromNetwork = null;
				fromName = from;
			} else {
				//#debug info
				System.out.println("3rd Party Chat with " + from);
				fromNetwork = from.substring(0, i);
				fromName = from.substring(i + 2);
			}
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast from.");
		}
		
		try {
			Vector v = ((Vector) htPush.get(KEY_TOS));
			
			if (null == v) {
				v = ((Vector) htPush.get(KEY_USERS));		
			}
			
			
			names = new String[v.size()];
			networks = new String[v.size()];
			
			for (int i = 0; i < v.size(); i++) {
				String temp = (String) v.elementAt(i);
							
				// split up the string into network and name
				if (null != temp) {
					int charPos = temp.indexOf(':');
					
					if ((charPos != -1) && 
							(temp.charAt(charPos + 1)) == ':') {
						names[i] = temp.substring(charPos + 2);
						networks[i] = temp.substring(0, charPos);
						
						//#debug info
						System.out.println("Parsing User " + names[i] + " for network " + networks[i]);
					} else {
						names[i] = temp;
					}
					
					temp = null;
				}
			}
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast tos.");
		}
	
		try {
			body = ((String) htPush.get(KEY_BODY));
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast conv ID.");
		}
	}
	
	/**
	 * 
	 * Creates a Hashtable to either create a conversation or to
	 * send out a chat message.
	 * 
	 */
	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		// if we are creating a conversation or sending a message
		// we need user ids
		if ((chatObjectType == T_CREATE_CONV) ||
				(chatObjectType == T_SEND_MSG)) {
			Vector v = new Vector();
			
			// if we have user ids we convert to a vector
			// and store them in the hashtable
			if ((null != networks) && (null != names) &&
					(networks.length == names.length)) {				
				for (int i = 0; i < names.length; i++) {
					if ((null != networks[i]) && (null != names[i])) {
						v.addElement(networks[i] + "::" + names[i]);
					} else if (null != names[i]) {
						v.addElement(names[i]);
					}
				}
				
				ht.put(KEY_TOS, v);
			} else if (null != names) {
				for (int i = 0; i < names.length; i++) {
					v.addElement(names[i]);
				}
				
				ht.put(KEY_TOS, v);
			}
		}
		
		// we only need body and attach if we are sending a message
		if (chatObjectType == T_SEND_MSG) {
			// put in body
			if (null != body) {
				ht.put(KEY_BODY, body);
			}
		}
		
		// if we are stopping a conversation or sending a message
		// we need a conversation id
		if ((chatObjectType == T_SEND_MSG) ||
				(chatObjectType == T_STOP_CONV)) {
			ht.put(KEY_CONVERSATION, conversationID);
		}
		
		return ht;
	}
	
	public String getFromNetwork() {
		return fromNetwork;
	}
	
	public String getFromName() {
		return fromName;
	}
	
	public String[] getNetworks() {
		return networks;
	}
	
	public String[] getNames() {
		return names;
	}
	
	
	
	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}

	/**
	 * 
	 * Returns a body of an incoming chat message.
	 * 
	 * @return The body of an incoming chat message.
	 */
	public String getBody() {
		return body;
	}
}
