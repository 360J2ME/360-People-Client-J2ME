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
package com.zyb.nowplus.data.protocol.request;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

//#if !polish.blackberry
//# import org.bouncycastle.crypto.prng.RandomGenerator;
//#endif

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.hessian.MicroHessianOutput;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.types.ServiceObject;

/**
 * 
 * Represents a connection request object which encapsulates all information
 * that the ConnectionManager needs to put it on the ConnectionRequestStack.
 * 
 * Once an object is on the ConnectionRequestStack it stays there until the next
 * GenericConnections is free to grab it and work it off.
 * 
 * The following table gives an overview on how requests are passed via the
 * ServerRequest object, e.g. for which request which ServiceObject-types are
 * passed and which ServiceObjects come back and in what quantity.
 * 
 * The table is in the format
 * Verb; Noun; Passed ServiceObject type; Quantity of passed ServiceObjects; 
 * Received ServiceObject type; Quantity of received ServiceObjects
 * 
 * SET; ME; ContactChanges; 1; BulkUpdateContactsResult; 1
 * BULK_UPDATE; CONTACT; ContactChanges; n; BulkUpdateContactsResult; 1
 * DELETE; IDENTITIES; Identity; 1; Identity; 1
 * VALIDATE; IDENTITIES; Identity; 1; Identity; 1
 * SET; IDENTITY_STATUS; Identity; 1; Identity; 1
 * GET; GROUPS; null; 0; Group; n
 * SET; CONTACT_GROUP_RELATIONS; Group; 1; Group; 1
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 * 
 */
public class ServerRequest {
	public static final int RPG_HEADER_DLIMIT1_OFFSET = 0,
							RPG_HEADER_DLIMIT2_OFFSET = 1,
							RPG_HEADER_LENGTH = 16,
							RPG_HEADER_PLSIZE_OFFSET = 11,
							RPG_HEADER_COMP_OFFSET = 15;
	
	
	// describes what kind of request we have
	public static final byte RPG_MSG_POLL = 0,
							 RPG_MSG_REQUEST = 1,
							 RPG_MSG_RESPONSE = 2,
							 RPG_MSG_PUSH = 3,
							 RPG_INTERNAL_MSG_REQUEST = 4,
							 RPG_CONTACTS_REQUEST = 5,
							 RPG_INTERNAL_MSG_RESPONSE = 6,
							 RPG_CHAT_SET_PRESENCE_REQUEST = 7,
							 RPG_CHAT_SEND_MSG_REQUEST = 8,
							 RPG_CREATE_CONVERSATION_REQUEST = 9,
							 RPG_STOP_CONVERSATION_REQUEST = 10,
							 RPG_CHAT_GET_PRESENCE_REQUEST = 11,
							 RPG_CHAT_GET_PRESENCE_RESPONSE = 12,
							 RPG_TCP_HEARTBEAT = 100,
							 RPG_TCP_SEND_TEST_REQUEST = 101,
							 RPG_TCP_SEND_TEST_RESPONSE = 102,
							 UNKNOWN = -1;

	// the prioritization of the request. The higher the priority, the quicker
	// it gets picked
	public static final byte 	HIGH_PRIORITY = 2,
								MEDIUM_PRIORITY = 1,
								LOW_PRIORITY = 0;

	// PLEASE SEE BELOWS METHODS TO FIND OUT THE VERB/NOUN GRAMMAR TO USE
	// these are the categories/nouns getting and setting:
	public static final byte ACTIVITIES = 1,
							 IDENTITIES = 2,
							 MESSAGES = 3,
							 CHAT_MESSAGES = 4,
							 PRESENCE = 5,
							 CONVERSATION = 6,
							 CONTACTS = 7,
							 CONTACT_DETAILS = 8,
							 IMAGES = 9,
							 USER_PROFILES = 10,
							 FRIENDS_OF_FRIENDS = 11,
							 BUDDY = 12,
							 ME = 13,
							 CONTACTS_CHANGES = 14,
							 MY_CHANGES = 15,
							 MY_IDENTITIES = 16,
							 IDENTITY_STATUS = 17,
							 GROUPS = 18,
							 CONTACT_GROUP_RELATIONS = 19,
							 UPDATE_AVAILABLE = 20,
							 TCP_TEST_PACKAGE = 21;
	// these are the verbs for the categories mentioned above:
	public static final byte GET = 1,
							 SET = 2,
							 SEND = 3,
							 DELETE = 4,
							 SEARCH = 5,
							 ADD = 6,
							 INVITE = 7,
							 START = 8,
							 STOP = 9,
							 UPDATE = 10,
							 BULK_UPDATE = 11,
							 VALIDATE = 12;

	// these are the nouns
	public static final String	AUTHENTICATE = "auth/getsessionbycredentialscrypted",
								SIGNUP = "auth/signupusercrypted",
								GET_NETWORK_INFO = "auth/getnetworkinfo",
								REQUEST_ACTIVATION_CODE = "auth/requestactivationcode",
								ACTIVATE_USER = "auth/activate",
								IS_UPDATE_AVAILABLE = "auth/isupdateavailable",
								GET_CONTACTS_CHANGES = "contacts/getcontactschanges",
								GET_MY_CHANGES = "contacts/getmychanges",
								SET_ME = "contacts/setme",
								BULK_UPDATE_CONTACTS = "contacts/bulkupdatecontacts",
								ADD_CONTACT = "contacts/addcontact",
								DELETE_CONTACTS = "contacts/deletecontacts",
								DELETE_CONTACT_DETAILS = "contacts/deletecontactdetails",
								UPDATE_CONTACT_DETAILS = "contacts/updatecontact",
								SEARCH_USERPROFILES = "contacts/searchuserprofiles",
								GET_USERPROFILES = "contacts/getuserprofiles",
								INVITE_CONTACTS = "contacts/invitenewuser",
								ADD_BUDDY = "contacts/addbuddy",
								GET_MESSAGES = "messages/getmessages",
								SET_MESSAGES = "messages/setmessages",
								DELETE_MESSAGES = "messages/deletemessages",
								GET_ACTIVITIES = "activities/getactivities",
								SET_ACTIVITIES = "activities/setactivities",
								DELETE_ACTIVITIES = "activities/deleteactivities",
								GET_FRIENDSOFFRIENDS = "contacts/getfriendsoffriends",
								GET_IDENTITIES = "identities/getavailableidentities",
								SET_IDENTITY_STATUS = "identities/setidentitystatus",
								GET_MY_IDENTITIES = "identities/getmyidentities",
								VALIDATE_IDENTITY = "identities/validateidentitycredentialscrypted",
								DELETE_IDENTITY = "identities/deleteidentity",
								ADD_CONTACT_GROUP_RELATIONS = "groupprivacy/addcontactgrouprelations",
								SET_CONTACT_GROUP_RELATIONS = "groupprivacy/setcontactgrouprelations",
								GET_GROUPS = "groupprivacy/getgroups";
	
	private static final String KEY_CONTACTLIST = "contactlist";

	public static final int SERIALISED_SIZE = 256;

	private boolean isActive; // set true when a GenericConnection grabs it,
	// false if still idle in queue
	private int requestID; // unique id for this object
	private byte priority, verb, noun;
	private ServiceObject[] setterObjects;
	private Hashtable parameters;
	private String url; // thumbnail url to load
	private boolean isFireAndForgetRequest;
	private boolean needsSynchronousConnection;

	public ServerRequest(int requestID, byte priority, byte verb, byte noun,
			ServiceObject[] setterObjects, Hashtable parameters,
			boolean isFireAndForgetRequest) {
		this.isActive = false;
		this.requestID = requestID;
		this.priority = priority;
		this.verb = verb;
		this.noun = noun;
		this.setterObjects = setterObjects;
		this.parameters = parameters;
		this.isFireAndForgetRequest = isFireAndForgetRequest;
		
		// some methods like validateIdentities need synchronous connections
		this.needsSynchronousConnection = getNeedsSynchronousConnection(verb, noun);
	}

	public ServerRequest(int requestID, byte priority, String url, byte verb,
			byte noun, ServiceObject[] setterObjects, Hashtable parameters) {
		this.isActive = false;
		this.requestID = requestID;
		this.priority = priority;
		this.url = url;		
		this.verb = verb;
		this.noun = noun;
		this.setterObjects = setterObjects;
		this.parameters = parameters;
		
		// some methods like validateIdentities need synchronous connections
		this.needsSynchronousConnection = getNeedsSynchronousConnection(verb, noun);
	}

	/**
	 * 
	 * Converts the connection request to the representation needed for talking
	 * to the RPG and writes it into the passed ByteArrayOutputStream.
	 * This includes hessianating the payload, setting the header fields, etc.
	 * 
	 * @param os The OutputStream to write the data to.
	 * @param writeRPGHeader True if the RPG header shoule be written. Applies
	 * for all asynchronous calls.
	 * 
	 */
	public void writeToRPGDataStructure(OutputStream os, 
										boolean writeRPGHeader) {
		if (null == os) {
			return;
		}
		
		String methodName = getMethodName(verb, noun);
		long ts = ((long) System.currentTimeMillis() / 1000);
		boolean needsAuth = true;	// indicates that we need authentication
		
		if (null != methodName) {
			switch (noun) {
			case ACTIVITIES:
				switch (verb) {
				case GET:
					// TODO implement
					break;
				case SET:
					// TODO implement
					break;
				case DELETE:
					// TODO implement
					break;
				}
				break;
			case CONVERSATION:
				switch (verb) {
					case START:
					case STOP:
						if (null != setterObjects) {
							parameters = null;

							if (null != setterObjects[0]) {
								parameters = setterObjects[0].toHashtable();
							}
						}
						break;
				}
				break;
			case CHAT_MESSAGES:
				switch (verb) {
					case SEND:
					case SET:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case CONTACT_GROUP_RELATIONS:
				if ((null != setterObjects) && (null != setterObjects[0])) {
					parameters = null;
					parameters = setterObjects[0].toHashtable();
				}
				break;
			case PRESENCE:
				switch (verb) {
					case SET:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case IDENTITIES:
				switch (verb) {
					case VALIDATE:
					case DELETE:
					case SET:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case IDENTITY_STATUS:
				switch (verb) {
					case SET:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case CONTACTS:
				switch (verb) {
					case SET:
					case ADD:
					case UPDATE:
					case DELETE:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
					case BULK_UPDATE:
						parameters = null;
						parameters = new Hashtable();
						Vector contacts = new Vector();
						
						if (null != setterObjects) {
							for (int i = 0; i < setterObjects.length; i++) {
								if (null != setterObjects[i]) {
									contacts.addElement(setterObjects[i].toHashtable());
								}
							}
						}
						parameters.put(KEY_CONTACTLIST, contacts);
						break;
				}
				break;
			case CONTACT_DETAILS:
				switch (verb) {
					case UPDATE:
					case DELETE:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case ME:
				switch (verb) {
					case SET:
						if ((null != setterObjects) && (null != setterObjects[0])) {
							parameters = null;
							parameters = setterObjects[0].toHashtable();
						}
						break;
				}
				break;
			case FRIENDS_OF_FRIENDS:
				break;
			case IMAGES:
				parameters.put("method", "GET");
				parameters.put("url", ((null != url) ? url : ""));
				break;
			case CONTACTS_CHANGES:
				break;
			case TCP_TEST_PACKAGE:
				needsAuth = false;
				break;
			}
		}
		
		if (needsAuth) {
			if (null == parameters) {
				parameters = new Hashtable();
			}
	
			parameters.put("timestamp", new Long(ts));
			parameters.put("auth", getRequestAuth(methodName, parameters,
					RPGConnection.sessionSecret, RPGConnection.sessionID,
					MIDletContext.APP_KEY_ID,
					MIDletContext.APP_KEY_SECRET));
		}	
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALISED_SIZE);
		MicroHessianOutput mho = new MicroHessianOutput(baos);

		try {
			// in the tcp send test request we must not have the methodname
			if (getMessageType() != RPG_TCP_SEND_TEST_REQUEST) {
				if (null != methodName) {
					mho.startCall(methodName);
				}
				else {
					mho.startCall("");
				}

				if (null != parameters) {
					mho.writeHashtable(parameters);
					parameters.clear();
					parameters = null;
				}

				mho.completeCall();
			}

			baos.flush();
			baos.close();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed writing hessian.");
		}

		byte[] payload = null;

		if (null != baos) {
			payload = baos.toByteArray();
		}
		
		if (null != payload) {
			DataOutputStream dos = new DataOutputStream(os);
			
			try {
				// write header if we are in async (RPG) mode
				if (writeRPGHeader) {
					dos.write((byte) 0xFF);
					dos.write((byte) 0xFF);
					dos.write(getMessageType());
					dos.writeInt(requestID);
					dos.writeInt(0);
					dos.writeInt(payload.length);
					dos.writeByte(0);
				}
					
				// write payload
				dos.write(payload, 0, payload.length);

				//#debug info
				System.out.println("Request Payload Length: " + payload.length);

				//#debug info
				Toolkit.printHessian(new java.io.ByteArrayInputStream(payload));
			}
			catch (IOException io) {
				// #debug error
				System.out.println("Failed writing Comet Representation.");
			}
			finally {
				payload = null;
			}
		}
		
		//#debug info
		System.out.println("--------------------------------------------");
		//#debug info
		System.out.println("Request ID: " + requestID);
		//#debug info
		System.out.println("Request Method: " + methodName);
		//#debug info
		System.out.println("Request Type: " + getMessageType());
		//#debug info
		System.out.println("--------------------------------------------");	
	}
	
	private boolean getNeedsSynchronousConnection(byte verb, byte noun)
	{
		switch (verb) {
			// add methods that should require a synchronous connection here!
		}
		
		return false;
	}

	private String getMethodName(byte verb, byte noun)
	{
		// find out method to start the hessian call with
		switch (noun) {
			case ACTIVITIES:
				switch (verb) {
				case GET:
					return GET_ACTIVITIES;
				case SET:
					return SET_ACTIVITIES;
				case DELETE:
					return DELETE_ACTIVITIES;
				}
				break;
			case ME:
				switch (verb) {
					case SET:
						return SET_ME;
				}
				break;
			case BUDDY:
				switch (verb) {
				case ADD:
					return ADD_BUDDY;
				}
				break;
			case MESSAGES:
				switch (verb) {
				case GET:
					return GET_MESSAGES;
				case SET:
				case SEND:
					return SET_MESSAGES;
				case DELETE:
					return DELETE_MESSAGES;
				}
				break;
			case CHAT_MESSAGES:
				return "";
			case PRESENCE:
				return "";
			case CONTACTS:
				switch (verb) {
				case GET:
					return GET_CONTACTS_CHANGES;
				case SET:
				case ADD:
					return ADD_CONTACT;
				case DELETE:
					return DELETE_CONTACTS;
				case INVITE:
					return INVITE_CONTACTS;
				case BULK_UPDATE:
					return BULK_UPDATE_CONTACTS;
				}
				break;
			case CONTACT_DETAILS:
				switch (verb) {
				case UPDATE:
					return UPDATE_CONTACT_DETAILS;
				case DELETE:
					return DELETE_CONTACT_DETAILS;
				}
				break;
			case CONTACTS_CHANGES:
				return GET_CONTACTS_CHANGES;
			case MY_CHANGES:
				return GET_MY_CHANGES;
			case FRIENDS_OF_FRIENDS:
				return GET_FRIENDSOFFRIENDS;
			case IMAGES:
				break;
			case USER_PROFILES:
				switch (verb) {
					case GET:
						return GET_USERPROFILES;
					case SEARCH:
						return SEARCH_USERPROFILES;
					}
				break;
			case IDENTITIES:
				switch (verb) {
					case GET:
						return GET_IDENTITIES;
					case VALIDATE:
						return VALIDATE_IDENTITY;
					case DELETE:
						return DELETE_IDENTITY;
					case SET:
						return SET_IDENTITY_STATUS;
				}
				break;
			case MY_IDENTITIES:
				switch (verb) {
					case GET:
						return GET_MY_IDENTITIES;
				}
				break;
			case IDENTITY_STATUS:
				switch (verb) {
					case SET:
						return SET_IDENTITY_STATUS;
				}
				break;
			case GROUPS:
				switch (verb) {
					case GET:
						return GET_GROUPS;
				}
				break;
			case CONTACT_GROUP_RELATIONS:
				switch (verb) {
					case ADD:
						return ADD_CONTACT_GROUP_RELATIONS;	
					case SET:
						return SET_CONTACT_GROUP_RELATIONS;
				}
				break;
		}

		return "";
	}

	/**
	 * Returns the message type of the ServerRequest.
	 * 
	 * @return The message type.
	 */
	public byte getMessageType() {
		switch (noun) {
			case IMAGES:
				return RPG_MSG_REQUEST;
			case CHAT_MESSAGES:
				return RPG_CHAT_SEND_MSG_REQUEST;
			case PRESENCE:
				switch (verb) {
					case SET:
						return RPG_CHAT_SET_PRESENCE_REQUEST;
					case GET:
						return RPG_CHAT_GET_PRESENCE_REQUEST;
				}
			case CONVERSATION:
				switch (verb) {
					case START:
						return RPG_CREATE_CONVERSATION_REQUEST;
					case STOP:
						return RPG_STOP_CONVERSATION_REQUEST;
				}
			case TCP_TEST_PACKAGE:
				switch (verb) {
					case SEND:
						return RPG_TCP_SEND_TEST_REQUEST;
				}
			default:
				return RPG_INTERNAL_MSG_REQUEST;
		}
	}

	/**
	 * 
	 * Gets the default payload values for the request so the backend knows which
	 * client it is talking to.
	 * 
	 * @param hRequest Optionally, a hashtable with prepopulated data.
	 * @param method The method to request with.
	 * @param isAuthRequest True if it is an authentication request.
	 * @param username The username of the user (optional for auth).
	 * @param password The password of the user (optional for auth).
	 * @param userID The user id of the user if available.
	 * 
	 * @return The byte-array representing the passed data in the 
	 * request.
	 * 
	 */
	public static final byte[] getRequestPayload(Hashtable hRequest,
						String method, boolean isAuthRequest,
						String username, String password,
						String userID)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALISED_SIZE);
		getRequestPayload(hRequest, method, isAuthRequest, username, password, userID, baos);
		return baos.toByteArray();
	}
	
	public static final void getRequestPayload(Hashtable hRequest,
								String method, boolean isAuthRequest,
								String username, String password,
								String userID, OutputStream os) {
		//#debug info
		System.out.println("Auth for method: " + method + " with Username/Password: " + username + "/" + password);

		if (null == hRequest) {
			hRequest = new Hashtable();
		}
		if (null == method) {
			method = "";
		}

		long ts = ((long) System.currentTimeMillis() / 1000);

		if (isAuthRequest) {
			if (null != RPGConnection.appInstance) {
				Hashtable hMore = new Hashtable();
				hMore.put("appinstance", getAppInstance());
				hRequest.put("more", hMore);
			}
			if (null != username) {
				hRequest.put("username", username);
				if (null != password) {
					hRequest.put("password", Toolkit.getEncryptedPassword(ts, username, password));
				}
			}
		}
		
		// for TCP sockets
		if (null != userID) {
			try {
				Long uid = new Long(Long.parseLong(userID));
				hRequest.put("userid", uid);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not add user id!");
			}
		}
		
		hRequest.put("timestamp", new Long(ts));
		hRequest.put("auth", getRequestAuth(method, hRequest,
				RPGConnection.sessionSecret, RPGConnection.sessionID, 
				MIDletContext.APP_KEY_ID,
				MIDletContext.APP_KEY_SECRET));

		MicroHessianOutput out = new MicroHessianOutput(os);
		try {
			out.startCall(method);
			out.writeHashtable(hRequest);
			out.completeCall();
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}

		out = null;
		hRequest.clear();
		hRequest = null;
	}
	
	/**
	 * 
	 * Generates the authentication parameter needed for every request to the 
	 * backend.
	 * 
	 * 
	 * @param method The method to invoke on the backend, e.g. contacts/getcontacts.
	 * @param parameters The parameters sent in the request.
	 * @param userSessionSecret The user session secret.
	 * @param userSessionID The user session ID.
	 * @param appKeyID The application ID.
	 * @param appKeySecret The application secret.
	 * 
	 * @return A string containing the authentication parameter.
	 * 
	 */
	public static String getRequestAuth(String method, Object parameters, String userSessionSecret,
					String userSessionID, String appKeyID, String appKeySecret) {
		if ((appKeyID == null) || (appKeyID.length() == 0)) {
			return null;
		}
		
		if ((appKeySecret == null) || (appKeySecret.length() == 0)) {
			return null;
		}
		
		int authLevel = 0;
		
		StringBuffer sbAuth = new StringBuffer();
		sbAuth.append(appKeyID.toLowerCase());
		
		if ((authLevel == 0) && (userSessionID != null) && (userSessionID.length() > 0) && 
				(userSessionSecret != null) && (userSessionSecret.length() > 0)) {
			sbAuth.append("::");
			sbAuth.append(userSessionID.toLowerCase());
		}
		
		String timestamp = "" + ((long) System.currentTimeMillis() / 1000);
		sbAuth.append("::");
		sbAuth.append(timestamp);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			os.write(appKeySecret.toLowerCase().getBytes("utf-8"));
		
			if ( (authLevel == 0) && (userSessionID != null)
					&& (userSessionID.length() > 0) && (userSessionSecret != null)
					&& (userSessionSecret.length() > 0)) {
				os.write('&');
				os.write(userSessionSecret.toLowerCase().getBytes("utf-8"));
			}
			
			os.write('&');
			os.write(method.toLowerCase().getBytes("utf-8"));
			
			if (parameters != null) {
				if (parameters instanceof Hashtable) {
					((Hashtable) parameters).put("auth", sbAuth.toString());
				} else if ((parameters instanceof Vector)
						&& (((Vector) parameters).size() > 0)) {
					((Vector) parameters).setElementAt(sbAuth.toString(), 0);
				} else if ((parameters instanceof Object[])
						&& (((Object[]) parameters).length > 0)) {
					((Object[]) parameters)[0] = sbAuth.toString();
				}
				
				os.write('&');
				encodeSignature(parameters, os);
			}
		} catch (IOException e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		String hexSig = Toolkit.MD5(os.toByteArray());
		
		sbAuth.append("::");
		sbAuth.append(hexSig);

		return sbAuth.toString();
	}
	
	/**
	 * Encodes an object into a string, for the signature.
	 * 
	 * @param o The object to encode to a string.
	 * @param oos The output stream to write the object to.
	 * 
	 * @return the string representation of the object
	 */
	private static byte[] encodeSignature(Object o, OutputStream oos)
			throws IOException {
		
		OutputStream os;
		
		if (oos != null) {
			os = oos;
		} else {
			os = (OutputStream) new ByteArrayOutputStream();
		}
			
		if (o == null) {
			return null;
		}
		
		if (o instanceof Object[]) {
			Object[] oo = (Object[]) o;
			
			for (int i = 0; i < oo.length; i++) {
				if (i != 0) {
					os.write('&');
				}
				
				encodeSignature(oo[i], os);
			}
		} else if (o instanceof Vector) {
			Vector v = (Vector) o;
			
			for (int i = 0; i < v.size(); i++) {
				if (i != 0) {
					os.write('&');
				}
				
				encodeSignature(v.elementAt(i), os);
			}
		} else if (o instanceof Hashtable) {
			Hashtable h = (Hashtable) o;
			
			String[] keys = Toolkit.sort(h);
			
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				
				if (i != 0) {
					os.write('&');
				}
				
				encodeSignature(key, os);
				os.write('=');
				Object val = h.get(key);
				encodeSignature(val, os);
			}
		} else if (o instanceof byte[]) {
			os.write(Toolkit.MD5((byte[]) o).getBytes());
		} else {
			os.write(o.toString().getBytes("utf-8"));
		}
		if (os == null) {
			return null;
		} else {
			return ((ByteArrayOutputStream) os).toByteArray();
		}
	}

	/**
	 * 
	 * Helps construct an RPG header.
	 * 
	 * @param existingByteArray The existing byte array. This will be copied to a
	 * 16 byte larger array that contains the header.
	 * @param messageType The message type, e.g. internal message request.
	 * 
	 * @return The header + the payload in a copied array.
	 * 
	 */
	public static void getRPGHeader(final byte messageType, byte[] requestData) {
		
		byte[] payloadSize = Toolkit.intToSignedBytes(requestData.length - RPG_HEADER_LENGTH);
		
		requestData[2] = messageType; // type = rpg message polling
		requestData[RPG_HEADER_PLSIZE_OFFSET] = payloadSize[0];
		requestData[RPG_HEADER_PLSIZE_OFFSET + 1] = payloadSize[1];
		requestData[RPG_HEADER_PLSIZE_OFFSET + 2] = payloadSize[2];
		requestData[RPG_HEADER_PLSIZE_OFFSET + 3] = payloadSize[3];
		requestData[RPG_HEADER_COMP_OFFSET] = 0;                // no gzip compression
	}
	
	public static byte[] EMPTY_RPG_HEADER = {(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
    /**
     * 
     * Gets a unique application value to use in the appInstance so
     * the RPG can distinguish between different clients.
     * 
     * @param appName The application name to generate the hash for.
     * 
     * @return A unique id of the client at runtime.
     * 
     */
    public static String getAppInstance() {
    	Random rand = new Random(System.currentTimeMillis());
    	long randLong = rand.nextLong();
    	rand = null;
    	
    	return Toolkit.MD5(MIDletContext.APP_NAME 
    									+ "::" + randLong);
    }
	
  //#mdebug error
    public String toString() {
    	return requestID + ": " + getMethodName(verb, noun);
    }
	//#enddebug
    
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Hashtable getFilters() {
		return parameters;
	}

	public void setFilters(Hashtable filters) {
		this.parameters = filters;
	}

	public byte getNoun() {
		return noun;
	}

	public void setNoun(byte noun) {
		this.noun = noun;
	}

	public byte getPriority() {
		return priority;
	}

	public void setPriority(byte priority) {
		this.priority = priority;
	}

	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public ServiceObject[] getSetterObjects() {
		return setterObjects;
	}

	public void setSetterObjects(ServiceObject[] setterObjects) {
		this.setterObjects = setterObjects;
	}

	public byte getVerb() {
		return verb;
	}

	public void setVerb(byte verb) {
		this.verb = verb;
	}

	public boolean isFireAndForgetRequest() {
		return isFireAndForgetRequest;
	}

	public void setFireAndForgetRequest(boolean isFireAndForgetRequest) {
		this.isFireAndForgetRequest = isFireAndForgetRequest;
	}
	
	public boolean getNeedsSynchronousConnection() {
		return needsSynchronousConnection;
	}
}
