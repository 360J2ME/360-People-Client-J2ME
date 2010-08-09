/*
 * Copyright (c) 2001-2006 Caucho Technology, Inc.  All rights reserved.
 *
 * The Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Caucho Technology (http://www.caucho.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Hessian", "Resin", and "Caucho" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@caucho.com.
 *
 * 5. Products derived from this software may not be called "Resin"
 *    nor may "Resin" appear in their names without prior written
 *    permission of Caucho Technology.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Scott Ferguson
 */

/*
 * Some changes:
 * Copyright 2007 Vodafone Group Services GmbH
 * Copyright 2007 Bruno Rodrigues <bruno.rodrigues@vodafone.com>
 * Licensed under the same licence as described above.
 */

/*
 * Further changes:
 * Copyright 2008 Vodafone Group Services GmbH
 * Copyright 2008 Rudy Norff <rudy.norff@vodafone.com>
 * Licensed under the same licence as described above.
 */

package com.zyb.nowplus.data.protocol.hessian;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.Activity;
import com.zyb.nowplus.data.protocol.types.AddBuddyResult;
import com.zyb.nowplus.data.protocol.types.AddEditDeleteContactDetailResult;
import com.zyb.nowplus.data.protocol.types.BulkUpdateContactsResult;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.nowplus.data.protocol.types.ContactsDeletionResult;
import com.zyb.nowplus.data.protocol.types.Group;
import com.zyb.nowplus.data.protocol.types.Identity;
import com.zyb.nowplus.data.protocol.types.InviteNewUserResult;
import com.zyb.nowplus.data.protocol.types.ServiceObject;


/**
 * Input stream for Hessian requests, compatible with microedition java. It only
 * uses classes and types available to J2ME.
 * 
 * <p>
 * MicroHessianInput does not depend on any classes other than in J2ME, so it
 * can be extracted independently into a smaller package.
 * 
 * <p>
 * MicroHessianInput is unbuffered, so any client needs to provide its own
 * buffering.
 * 
 * <pre>
 * InputStream is = ...; // from http connection
 * MicroHessianInput in = new MicroHessianInput(is);
 * String value;
 * in.startReply();         // read reply header
 * value = in.readString(); // read string value
 * in.completeReply();      // read reply footer
 * </pre>
 */	
public class MicroHessianInput {
	public static final int MAX_OBJECTS_TO_READ = 10000;
	
	private static final boolean DEBUG = false;	
	
	// service types to identify what service object we want to parse
	//public static final byte CONTACT = 0, MESSAGE = 1, ACTIVITY = 2;

	// vf api error codes
	// TODO add remaining
	private static final String ERR_INTERNAL = "INTERNAL_ERROR",
								ERR_TEMP = "TEMP_ERROR",
								ERR_NOT_IMPL = "TEMP_ERROR",
								ERR_INVALID_REQ = "INVALID_REQUEST",
								ERR_INVALID_PARAM = "INVALID_PARAMETER",
								ERR_AUTH_1 = "INVALID_AUTHENTICATION",
								ERR_AUTH_2 = "AUTH_INVALID_CREDENTIALS",
								ERR_AUTH_USER_NOT_FOUND = "AUTH_USER_NOT_FOUND",
								ERR_SESSION = "INVALID_SESSION",
								
								ERR_USERNAME_MISSING = "USERNAMEMISSING",
								ERR_USERNAME_BLACKLISTED = "USERNAMEBLACKLISTED",
								ERR_USERNAME_FORBIDDEN = "USERNAMEFORBIDDEN",
								ERR_USERNAME_IN_USE = "USERNAMEINUSE",
								ERR_FULL_NAME_MISSING = "FULLNAMEMISSING",
								ERR_PASSWORD_MISSING = "PASSWORDMISSING",
								ERR_PASSWORD_INVALID = "PASSWORDINVALID",
								ERR_ACCEPT_TC_MISSING = "ACCEPTTCMISSING",
								ERR_DATE_OF_BIRTH_INVALID = "DATEOFBIRTHINVALID",
								ERR_EMAIL_MISSING = "EMAILMISSING",
								ERR_EMAIL_INVALID = "EMAILINVALID",
								ERR_COUNTRY_INVALID = "COUNTRYINVALID",
								ERR_MSISDN_MISSING = "MSISDNMISSING",
								ERR_MSISDN_INVALID = "MSISDNINVALID",
								ERR_TIME_ZONE_MISSING = "TIMEZONEMISSING",
								ERR_TIME_ZONE_INVALID = "TIMEZONEINVALID",
								ERR_MOBILE_OPERATOR_INVALID = "MOBILEOPERATORINVALID",
								ERR_MOBILE_MODEL_INVALID = "MOBILEMODELINVALID",
								ERR_LANGUAGE_INVALID = "LANGUAGEINVALID",
								ERR_IP_ADDRESS_MISSING = "IPADDRESSMISSING";
	
	// hessian key strings to identify service objects to create
	public static final String CONTACT_STR = "contact",
								DETAILLIST_STR = "detaillist",
								CONTACT_ID_STR = "contactid",
								ACTIVITY_STR = "activity", 
								MESSAGE_STR = "message",
								FOF_STR = "userfof",
								USER_PROFILE_STR = "userprofile",
								USER_PROFILE_CHANGES_STR = "userprofilechanges",
								RESULT_STR = "result",
								SHORT_REQUEST_ID_STR = "shortrequestid",
								CONVERSATION_STR = "conversation",
								PM_TYPE_STR = "pmtype",
								SERVER_REVISION_ANCHOR = "serverrevisionanchor",
								CURRENT_SERVER_REVISION = "currentserverrevision",
								SERVER_REV_BEFORE_STR = "serverrevisionbefore",
								SERVER_REV_AFTER_STR = "serverrevisionafter",
								NUMBER_OF_PAGES = "numpages",
								AVAILABLE_IDENTITY_STR = "availableidentity",
								IDENTITY_STR = "identity",
								STATUS_STR = "status",
								ITEM_STR = "item";
	
	public static final String LIST_STR = "list";
	
	// we call back every xx items when we have more than xx items
	//private static final int MAX_CALLBACK_OBJECTS = 2000;

	
	// the type of service object we are looking for
	private byte searchedType;	// e.g. CONTACT, MESSAGE, etc.
	private int requestID;		// the ID of the request, managed by the comms manager
	private final CommunicationManagerImpl cmMgr;
	private final ResponseListener  serviceReqListener;	// the listener for the service
	private ServiceObject[] serviceObjects;
	//private String searchedKey;
	private boolean hasParsedServiceObject;
	
	private byte verb, noun;
	
	private ByteArrayOutputStream bos = null;
	protected InputStream is;
	
	private int byteCounter;
	private int payloadSize; 

	/**
	 * 
	 * Creates a new Hessian input stream, initialized with an underlying input
	 * stream.
	 * 
	 * @param is The underlying input stream.
	 * 
	 */
	public MicroHessianInput(InputStream is, 
			CommunicationManagerImpl cmMgr,
			ResponseListener serviceReqListener,
									int requestID, boolean parseRegularObject,
									byte verb, byte noun) {
		// if true we are really going to parse real hash tables, if not
		// hashtables are only parsed until we find a contact-, message- or
		// activity list and not written to memory
		hasParsedServiceObject = parseRegularObject;
		this.verb = verb;
		this.noun = noun;
		
		byteCounter = 0;
		
		this.cmMgr = cmMgr;	
		this.serviceReqListener = serviceReqListener;
		this.requestID = requestID;
		
		init(is);
	}

	/**
	 * Initialize the hessian stream with the underlying input stream.
	 */
	public void init(InputStream is) {
		this.is = is;
		
		if (DEBUG) {
			bos = new ByteArrayOutputStream();
		}
	}

	private int isread() throws IOException {
		int tag = is.read();
		byteCounter++;
		
		return tag;
	}
	
	public void setPayloadSize(int payloadSize) {
		//#debug info
		System.out.println("Payload length set to " + payloadSize);
		
		byteCounter = 0;
		this.payloadSize = payloadSize;
	}

	/**
	 * Starts reading a method call
	 * 
	 * <pre>
	 * c MAJ MIN 
	 * </pre>
	 */
	public void startCall() throws IOException {
		startCall(-1);
	}

	private void startCall(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'c')
			throw expect("hessian call", tag);
		/*int major =*/isread();
		/*int minor =*/isread();
	}

	/**
	 * Reads a method
	 * 
	 * <pre>
	 * m b16 b8 name 
	 * </pre>
	 */
	public String readMethod() throws IOException {
		return readMethod(-1);
	}

	private String readMethod(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'm')
			throw expect("method", tag);
		int b16 = isread();
		int b8 = isread();
		int len = (b16 << 8) + b8;
		return readStringImpl(len);
	}

	/**
	 * Starts reading the reply
	 * 
	 * <p>
	 * A successful completion will have a single value:
	 * 
	 * <pre>
	 * r x02 x00
	 * </pre>
	 */
	public void startReply() throws IOException {
		startReply(-1);
	}

	private void startReply(int tag) throws IOException {
		searchedType = 0;
		
		if (tag == -1)
			tag = isread();
		if (tag != 'r')
			throw expect("hessian reply", tag);

		isread();	// major
		isread();	// minor
	}

	/**
	 * Completes reading the method call
	 * 
	 * <p>
	 * A successful completion will have a single value:
	 * 
	 * <pre>
	 * z
	 * </pre>
	 */
	public void completeCall() throws IOException {
		completeCall(-1);
	}

	private void completeCall(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'z')
			throw expect("hessian end of call", tag);
	}

	/**
	 * Completes reading the reply
	 * 
	 * <p>
	 * A successful completion will have a single value:
	 * 
	 * <pre>
	 * z
	 * </pre>
	 */
	public void completeReply() throws IOException {
		completeReply(-1);
	}

	private void completeReply(int tag) throws IOException {
// System.out.println("TAG " + tag);
		
		
		if (tag == -1)
			tag = isread();
		if (tag != 'z')
			throw expect("hessian end of reply", tag);
	}

	/**
	 * Reads a null
	 * 
	 * <pre>
	 * N
	 * </pre>
	 */
	public Object readNull() throws IOException {
		return readNull(-1);
	}

	private Object readNull(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'N')
			throw expect("null", tag);
		return null;
	}

	/**
	 * Reads a boolean
	 * 
	 * <pre>
	 * T
	 * F
	 * </pre>
	 */
	public boolean readBoolean() throws IOException {
		return readBoolean(-1);
	}

	private boolean readBoolean(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		switch (tag) {
		case 'T':
			return true;
		case 'F':
			return false;
		default:
			throw expect("boolean", tag);
		}
	}

	/**
	 * Reads an integer
	 * 
	 * <pre>
	 * I b32 b24 b16 b8
	 * </pre>
	 */
	public int readInt() throws IOException {
		return readInt(-1);
	}

	private int readInt(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'I')
			throw expect("integer", tag);
		int b32 = isread();
		int b24 = isread();
		int b16 = isread();
		int b8 = isread();
		return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
	}

	/**
	 * Reads a long
	 * 
	 * <pre>
	 * L b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	public long readLong() throws IOException {
		return readLong(-1);
	}

	private long readLong(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'L')
			throw expect("long", tag);
		long b64 = isread();
		long b56 = isread();
		long b48 = isread();
		long b40 = isread();
		long b32 = isread();
		long b24 = isread();
		long b16 = isread();
		long b8 = isread();
		return ((b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32)
				+ (b32 << 24) + (b24 << 16) + (b16 << 8) + b8);
	}

	/**
	 * Reads a double
	 * 
	 * <pre>
	 * D b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre> / public double readDouble() throws IOException { return
	 * readDouble( -1 ); } private double readDouble( int tag ) throws
	 * IOException { if( tag == -1 ) tag = isread(); long l = 0; l = isread()<<56;
	 * l += (isread()<<48); l += (isread()<<40); l += (isread()<<32); l =
	 * isread()<<24; l += (isread()<<16); l += (isread()<<8); l +=
	 * isread(); return Double.longBitsToDouble(l); }
	 */

	/**
	 * Reads a date.
	 * 
	 * <pre>
	 * T b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	public Date readUTCDate() throws IOException {
		return readUTCDate(-1);
	}

	private Date readUTCDate(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag != 'd')
			throw expect("date", tag);
		long b64 = isread();
		long b56 = isread();
		long b48 = isread();
		long b40 = isread();
		long b32 = isread();
		long b24 = isread();
		long b16 = isread();
		long b8 = isread();
		return new Date((b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32)
				+ (b32 << 24) + (b24 << 16) + (b16 << 8) + b8);
	}

	/**
	 * Reads a string
	 * 
	 * <pre>
	 * S b16 b8 string value
	 * </pre>
	 */
	public String readString() throws IOException {
		return readString(-1);
	}

	private String readString(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag == 'N')
			return null;
		if (tag != 'S')
			throw expect("string", tag);
		int b16 = isread();
		int b8 = isread();
		int len = (b16 << 8) + b8;
		return readStringImpl(len);
	}

	/**
	 * Reads a byte array
	 * 
	 * <pre>
	 * B b16 b8 data value
	 * </pre>
	 */
	public byte[] readBytes() throws IOException {
		return readBytes(-1);
	}

	private byte[] readBytes(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
		if (tag == 'N')
			return null;
		if (tag != 'B' && tag != 'b')
			throw expect("bytes", tag);
		int b16 = isread();
		int b8 = isread();
		int len = (b16 << 8) + b8;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < len; i++)
			bos.write(isread());
		if (tag == 'b') {
			bos.write(readBytes());
		}
		//if( callbackbytes != null ) { callback.HessianObject( result ); return null; }
		return bos.toByteArray();
	}


	/**
	 * Reads a hash
	 * 
	 * <pre>
	 * B b16 b8 data value
	 * </pre>
	 */
	public Object parseHash() throws IOException {
		return parseHash(-1);
	}
	

	// match this with the real readHash
	private Object parseHash(int tag) throws IOException {		
		//#debug info
		System.out.println("Parsing Request Hash");

		if (tag == -1) {
			tag = isread();

			//#debug debug
			System.out.println("First hash byte " + ((char)tag));
		}
		
		if (tag == 'f') {
			parseFault(tag);
			
			while (byteCounter < payloadSize) {
				tag = isread();
				
				//#debug info
				System.out.println("Read unfinished byte: " + ((char) tag) + " (" + tag + ")");
			}
			return null;
		} else if (tag != 'M') {
			throw expect("hash", tag);
		}

		//#debug debug
		System.out.println("Parsing special objects");
		hasParsedServiceObject = true;
		Object key = null;

		if ((verb == ServerRequest.START) && (noun == ServerRequest.CONVERSATION)) {
			searchedType = ServiceObject.START_CHAT_CONVERSATION;
			serviceObjects = new ServiceObject[1];
				
			try {
				key = readFirstObject(tag);
				//#debug info
				System.out.println("Read first start conv object");
				
				if ((null != key) && ((key instanceof Hashtable))) {
					Hashtable ht = (Hashtable) key;
					
					//#debug info
					System.out.println("object is a hashtable");
					
					if (null != ht) {
						APIEvent apiEvt = new APIEvent();
						apiEvt.parseAPIEvent(ht);
						serviceObjects[0] = apiEvt.getServiceObject();
						
						//#debug info
						System.out.println("CALLING BACK MODEL WITH TYPE: " + searchedType + " WITH " + ((null != serviceObjects) ? ("" + serviceObjects.length) : "null") + " NUMBER OF ITEMS.");
						serviceReqListener.itemsReceived(requestID, serviceObjects, 
								searchedType);
						cleanServiceObjects(serviceObjects);
					}
				}
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast start conv hashtable." + e);
				
				serviceReqListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
			
			while (byteCounter < payloadSize) {
				tag = isread();
			}
			return null;
		} else if ((verb == ServerRequest.DELETE) && (noun == ServerRequest.CONTACTS)) {
			serviceObjects = new ServiceObject[1];
			
			//#debug info
			System.out.println("Found deleted contact(s)");
			
			searchedType = ServiceObject.CONTACT_DELETION;
			
			try {
				key = readFirstObject(tag);

				if ((null != key) && ((key instanceof Hashtable))) {
					Hashtable ht = (Hashtable) key;
					if (null != ht) {
						//#debug debug
						System.out.println("Hashtable is not null.");

						serviceObjects[0] = new ContactsDeletionResult(ht);
						serviceReqListener.itemsReceived(requestID, serviceObjects, 
								searchedType);
						cleanServiceObjects(serviceObjects);
					}
				}
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast contacts deletion result hashtable." + e);
				
				serviceReqListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
			
			while (byteCounter < payloadSize) {
				tag = isread();
			}
			return null;
		} else if (((verb == ServerRequest.SET) && (noun == ServerRequest.ME)) ||
					((verb == ServerRequest.BULK_UPDATE) && (noun == ServerRequest.CONTACTS))) {
			if (noun == ServerRequest.ME) {
				searchedType = ServiceObject.SET_ME_RESULT;
			} else {
				searchedType = ServiceObject.BULK_UPDATE_CONTACTS_RESULT;
			}
			serviceObjects = new ServiceObject[1];
				
			try {
				key = readFirstObject(tag);

				if ((null != key) && ((key instanceof Hashtable))) {
					Hashtable ht = (Hashtable) key;
					if (null != ht) {
						//#debug debug
						System.out.println("Hashtable is not null.");
						serviceObjects[0] = new BulkUpdateContactsResult(ht,
							((searchedType == ServiceObject.BULK_UPDATE_CONTACTS_RESULT) 
																? false : true));

						serviceReqListener.itemsReceived(requestID, serviceObjects, 
								searchedType);
						cleanServiceObjects(serviceObjects);
					}
				}
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast bulk update result hashtable." + e);
				
				serviceReqListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
			
			while (byteCounter < payloadSize) {
				tag = isread();
			}
			return null;
		}

		//#debug debug
		System.out.println("Parsing Standard Objects");

		long currentServerRevision = -1;
		long serverRevisionAnchor = -1;
		int numberOfPages = -1;
		
		// read every key and val in the hashtable and try 
		// to find the key for the object we are looking for...
		for (int i = 0; i < MAX_OBJECTS_TO_READ; i++) {
			// let's see if it's not a push message
			if (!(key instanceof End)) {
				//#debug info
				System.out.println("Reading next key in parser.");

				key = _readObject();
			} 
			
			if (key instanceof End) {
				//#debug info
				System.out.println("FOUND END");
				
				// if we have contacts- or my-changes, we fill the revision fields
				if (((searchedType == ServiceObject.CONTACT_CHANGES) ||
						(searchedType == ServiceObject.MY_CHANGES) ||
						((searchedType == ServiceObject.CONTACT) &&
								(verb == ServerRequest.BULK_UPDATE))) &&
						(null != serviceObjects)) {
					for (int j = 0; j < serviceObjects.length; j++) {
						if (null != serviceObjects[j]) {
							((ContactChanges) serviceObjects[j]).
									setServerRevisionAnchor(serverRevisionAnchor);
							((ContactChanges) serviceObjects[j]).
							setCurrentServerRevision(currentServerRevision);
							((ContactChanges) serviceObjects[j]).
							setNumberOfPages(numberOfPages);
						}
					}
				}
				
				if (null == serviceObjects) {
					serviceObjects = new ServiceObject[0];
				}
				
				// searchedType was not set for some reason, e.g. empty list...
				if (searchedType == 0) {
					searchedType = getSearchedType();
				}
				
				//#debug info
				System.out.println("CALLING BACK MODEL WITH TYPE: " + searchedType + " WITH " + ((null != serviceObjects) ? ("" + serviceObjects.length) : "null") + " NUMBER OF ITEMS.");
				serviceReqListener.itemsReceived(requestID, 
						serviceObjects, searchedType);
				cleanServiceObjects(serviceObjects);
				break;
			}

			if (key instanceof Type) {
				//#debug debug
				System.out.println("Type found.");

				key = _readObject();
			}

			if (key instanceof End) {
				//#debug info
				System.out.println("FOUND END 2");
				
				// if we have contacts- or my-changes, we fill the revision fields
				if (((searchedType == ServiceObject.CONTACT_CHANGES) ||
						(searchedType == ServiceObject.MY_CHANGES) ||
						((searchedType == ServiceObject.CONTACT) &&
								(verb == ServerRequest.BULK_UPDATE))) &&
						(null != serviceObjects)) {
					for (int j = 0; j < serviceObjects.length; j++) {
						if (null != serviceObjects[j]) {
							((ContactChanges) serviceObjects[j]).
									setServerRevisionAnchor(serverRevisionAnchor);
							((ContactChanges) serviceObjects[j]).
							setCurrentServerRevision(currentServerRevision);
							((ContactChanges) serviceObjects[j]).
							setNumberOfPages(numberOfPages);
						}
					}
				}
				
				if (null == serviceObjects) {
					serviceObjects = new ServiceObject[0];
				}
				
				// searcgedType was not set for some reason, e.g. empty list...
				if (searchedType == 0) {
					searchedType = getSearchedType();
				}
				
				//#debug info
				System.out.println("CALLING BACK MODEL WITH TYPE: " + searchedType + " WITH " + ((null != serviceObjects) ? ("" + serviceObjects.length) : "null") + " NUMBER OF ITEMS.");
				serviceReqListener.itemsReceived(requestID, 
						serviceObjects, searchedType);
				cleanServiceObjects(serviceObjects);
				break;
			}
		
			// check if we have a key that we are looking for. e.g. contactlist
			// if we were looking for contacts
			if ((null != key) && (key instanceof String)) {
				String keyStr = (String) key;
				
				//#debug info
				System.out.println("Found String " + keyStr);
			
				// addbuddy and invitecontacts are special cases where no hashtable
				// is wrapped around the results...
				if ((verb == ServerRequest.ADD) && (noun == ServerRequest.BUDDY)) {
					searchedType = ServiceObject.ADD_BUDDY_RESULT;
					
					if (keyStr.equals(CONTACT_ID_STR)) {
						key = _readObject();
						
						try {
							long contactID = ((Long) key).longValue();
							
							serviceObjects = new ServiceObject[1];
							serviceObjects[0] = new AddBuddyResult(contactID);
							
							// XXX change if needed
							/*serviceReqListener.itemsReceived(requestID, serviceObjects, 
									searchedType);*/
						} catch (Exception e) {
							//#debug error
							System.out.println("Could not cast contact id.");
						}
					}
				} else if (((verb == ServerRequest.ADD) && (noun == ServerRequest.CONTACTS)) 
						|| ((verb == ServerRequest.DELETE) && (noun == ServerRequest.CONTACT_DETAILS)) 
						|| ((verb == ServerRequest.UPDATE) && (noun == ServerRequest.CONTACT_DETAILS))) {
					switch (verb) {
						case ServerRequest.ADD:
							searchedType = ServiceObject.ADD_CONTACT_RESULT;
							break;
						case ServerRequest.DELETE:
							searchedType = ServiceObject.DELETE_CONTACT_DETAILS_RESULT;
							break;
						case ServerRequest.UPDATE:
							searchedType = ServiceObject.UPDATE_CONTACT_DETAILS_RESULT;
							break;
					}
					
					long contactID = -1;
					Vector detailList = null;
					
					while (!(key instanceof End)) {
						if (null != keyStr) {							
							if (keyStr.equals(CONTACT_ID_STR)) {
								if (!((key =_readObject()) instanceof End)) {									
									try {
										contactID = ((Long) key).longValue();
									} catch (Exception e) {
										//#debug error
										System.out.println("Could not cast contact id.");
									}
								}
							} else if (keyStr.equals(DETAILLIST_STR)) {
								if (!((key =_readObject()) instanceof End)) {
									
									try {
										detailList = (Vector) key;
									} catch (Exception e) {
										//#debug error
										System.out.println("Could not cast detail list.");
									}
								}
							}
						}
						
						keyStr = null;
						
						try {
							key = _readObject();
							keyStr = (String) key;
						} catch (Exception e) {
							//#debug error
							System.out.println("Key is not a String.");
						}
					}
					
					if (contactID != -1) {
						serviceObjects = new ServiceObject[1];
						serviceObjects[0] = new AddEditDeleteContactDetailResult(contactID, detailList);
						
						// XXX change if needed
						/*serviceReqListener.itemsReceived(requestID, serviceObjects, 
															searchedType);*/
					}
				} else if ((verb == ServerRequest.INVITE) && (noun == ServerRequest.CONTACTS)) {
					searchedType = ServiceObject.INVITE_NEW_USER_RESULT;
					long shortRequestID = -1;
					long contactID = -1;
					
					while (!(key instanceof End)) {
						if (null != keyStr) {							
							if (keyStr.equals(CONTACT_ID_STR)) {
								if (!((key =_readObject()) instanceof End)) {									
									try {
										contactID = ((Long) key).longValue();
									} catch (Exception e) {
										//#debug error
										System.out.println("Could not cast contact id.");
									}
								}
							} else if (keyStr.equals(SHORT_REQUEST_ID_STR)) {
								if (!((key =_readObject()) instanceof End)) {
									
									try {
										shortRequestID = ((Integer) key).longValue();
									} catch (Exception e) {
										//#debug error
										System.out.println("Could not cast contact id.");
									}
								}
							}
						}
						
						keyStr = null;
						
						try {
							key = _readObject();
							keyStr = (String) key;
						} catch (Exception e) {
							//#debug error
							System.out.println("Key is not a String.");
						}
					}
					
					// only if we have parsed succesfully
					if (shortRequestID != -1) {
						serviceObjects = new ServiceObject[1];
						serviceObjects[0] = new InviteNewUserResult(contactID, shortRequestID);
					}
				} else if ((verb == ServerRequest.SET) && (noun == ServerRequest.CONTACT_GROUP_RELATIONS)) {
					searchedType = ServiceObject.SET_CONTACT_GROUP_RELATIONS_RESULT;
					
					while (!(key instanceof End)) {
						if (null != keyStr) {							
							if (keyStr.equals(ITEM_STR + LIST_STR)) {
								if (!((key =_readObject()) instanceof End)) {									
									// we avoid parsing this object, too complex
									serviceObjects = new ServiceObject[1];
									serviceObjects[0] = new Group();
								}
							}
						}
						
						keyStr = null;
						
						try {
							key = _readObject();
							keyStr = (String) key;
						} catch (Exception e) {
							//#debug error
							System.out.println("Exception " + e);
						}
					}
				} else {
					if (keyStr.equals(USER_PROFILE_CHANGES_STR)) {
						if (noun == ServerRequest.MY_CHANGES) {
							//#debug info
							System.out.println("Found my changes.");
							
							searchedType = ServiceObject.MY_CHANGES;
							serviceObjects = parseServiceObjects(-1, true);
						}
					} else if ((keyStr.equals(USER_PROFILE_STR)) || (keyStr.equals(USER_PROFILE_STR + LIST_STR))) {
						//#debug info
						System.out.println("Found user profile(s)");
						
						searchedType = ServiceObject.USER_PROFILE;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(USER_PROFILE_STR)) || (keyStr.equals(USER_PROFILE_STR + LIST_STR))) {
						//#debug info
						System.out.println("Found user profile(s)");
						
						searchedType = ServiceObject.USER_PROFILE;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(ACTIVITY_STR)) || (keyStr.equals(ACTIVITY_STR + LIST_STR))) {
						searchedType = ServiceObject.ACTIVITY;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(AVAILABLE_IDENTITY_STR)) || (keyStr.equals(AVAILABLE_IDENTITY_STR + LIST_STR))) {
						searchedType = ServiceObject.AVAILABLE_IDENTITY;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(IDENTITY_STR + LIST_STR)) || (keyStr.equals(IDENTITY_STR))) {
						searchedType = ServiceObject.MY_IDENTITY;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);						
					} else if ((keyStr.equals(MESSAGE_STR)) || (keyStr.equals(MESSAGE_STR + LIST_STR))) {
						searchedType = ServiceObject.MESSAGE;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(FOF_STR)) || (keyStr.equals(FOF_STR + LIST_STR))) {
						searchedType = ServiceObject.FRIENDS_OF_FRIEND_LIST;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(FOF_STR)) || (keyStr.equals(FOF_STR + LIST_STR))) {
						searchedType = ServiceObject.FRIENDS_OF_FRIEND_LIST;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(CONTACT_STR)) || (keyStr.equals(CONTACT_STR + LIST_STR))) {
						searchedType = ServiceObject.CONTACT_CHANGES;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(ITEM_STR)) || (keyStr.equals(ITEM_STR + LIST_STR))) {
						searchedType = ServiceObject.GROUPS;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if ((keyStr.equals(RESULT_STR)) || (keyStr.equals(RESULT_STR + LIST_STR))) {
						searchedType = ServiceObject.SEARCH_USER_PROFILE_RESULT;
						serviceObjects = parseServiceObjects(-1, 
								(keyStr.endsWith(LIST_STR)) ? false: true);
					} else if (keyStr.equals(STATUS_STR)) {
						if (verb == ServerRequest.VALIDATE) {
							searchedType = ServiceObject.ADD_IDENTITY_RESULT;
						} else if (verb == ServerRequest.DELETE) {
							searchedType = ServiceObject.DELETE_IDENTITY_RESULT;
						} else if ((verb == ServerRequest.SET) && 
								((noun == ServerRequest.IDENTITY_STATUS) ||
								 (noun == ServerRequest.IDENTITIES)) ) {
							searchedType = ServiceObject.SET_IDENTITY_STATUS_RESULT;
						}
						boolean identityChanged = false;
						try {
							identityChanged = readBoolean();
						} catch (Exception e) {
							//#debug error
							System.out.println("Exception " + e);
						}
						
						serviceObjects = new ServiceObject[1];
						serviceObjects[0] = new Identity(identityChanged);
					} else if (SERVER_REVISION_ANCHOR.equals(keyStr)) {
						serverRevisionAnchor = readLong();
						//#debug info
						System.out.println("... Found Anchor " + serverRevisionAnchor);
					} else if (CURRENT_SERVER_REVISION.equals(keyStr)) {
						//#debug info
						System.out.println("... Found current server rev. " + currentServerRevision);
						currentServerRevision = readLong();
					} else if (NUMBER_OF_PAGES.equals(keyStr)) {
						//#debug info
						System.out.println("... Found numpages " + numberOfPages);
						numberOfPages = readInt();
					}
					
					keyStr = null;
				}
			}
		}
		
		while (byteCounter < payloadSize) {
			tag = isread();
		}
		return null; // too many items, return what was read
	}
	
	
	private byte getSearchedType() {
		byte type = 0;
		
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.IDENTITIES)) {
			type = ServiceObject.AVAILABLE_IDENTITY;
		} else if ((verb == ServerRequest.GET) && (noun == ServerRequest.MY_IDENTITIES)) {
			type = ServiceObject.MY_IDENTITY;
		} else if ((verb == ServerRequest.GET) && (noun == ServerRequest.MY_CHANGES)) {
			type = ServiceObject.MY_CHANGES;
		} else if ((verb == ServerRequest.GET) && (noun == ServerRequest.CONTACTS_CHANGES)) {
			type = ServiceObject.CONTACT_CHANGES;
		}
		// TODO continue this list
		
		return type;
	}

	private void parseFault(int tag) {
		try {
			if (tag == -1) {
				tag = isread();
			}
			
			// the response is a fault
			if (tag == 'f') {
				readString();
				String code = readString();
				
				//#debug error
				System.out.println("FAULT IN HESSIAN: " + code);

				if ((ServerRequest.GET == verb) && 
						(ServerRequest.UPDATE_AVAILABLE == noun)) {
					//#debug info
					System.out.println("Client is UP TO DATE!!!");
					if (null != serviceReqListener) {
						serviceReqListener.clientIsUpToDate();
					}
					return;
				}

				if (null != code) {
					// special update check error case
					if (code.equals(ERR_INTERNAL)) {
						readString();
						String message = readString();

						//#debug error
						System.out.println("message: " + message);

						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_INTERNAL_ERROR);
						}
						else {
							cmMgr.authenticationFailed(AuthenticationListener.AUTH_FAILED_UNKNOWN);
						}
					} else if (code.equals(ERR_TEMP)) {
						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_TEMP_ERROR);
						}
					} else if (code.equals(ERR_NOT_IMPL)) {
						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_NOT_IMPLEMENTED);
						}
					} else if (code.equals(ERR_INVALID_REQ)) {
						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_INVALID_REQUEST);
						}
					} else if (code.equals(ERR_INVALID_PARAM)) {
						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_INVALID_PARAMETER);
						}
					} else if (code.equals(ERR_USERNAME_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.USERNAME_MISSING);
					} else if (code.equals(ERR_USERNAME_BLACKLISTED)) {
						cmMgr.registrationFailed(AuthenticationListener.USERNAME_BLACKLISTED);
					} else if (code.equals(ERR_USERNAME_FORBIDDEN)) {
						cmMgr.registrationFailed(AuthenticationListener.USERNAME_FORBIDDEN);
					} else if (code.equals(ERR_USERNAME_IN_USE)) {
						cmMgr.registrationFailed(AuthenticationListener.USERNAME_IN_USE);
					} else if (code.equals(ERR_FULL_NAME_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.FULL_NAME_MISSING);
					} else if (code.equals(ERR_PASSWORD_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.PASSWORD_MISSING);
					} else if (code.equals(ERR_PASSWORD_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.PASSWORD_INVALID);
					} else if (code.equals(ERR_ACCEPT_TC_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.ACCEPT_TC_MISSING);
					} else if (code.equals(ERR_DATE_OF_BIRTH_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.D_O_B_INVALID);
					} else if (code.equals(ERR_EMAIL_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.EMAIL_MISSING);
					} else if (code.equals(ERR_EMAIL_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.EMAIL_INVALID);
					} else if (code.equals(ERR_COUNTRY_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.COUNTRY_INVALID);
					} else if (code.equals(ERR_MSISDN_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.MSISDN_MISSING);
					} else if (code.equals(ERR_MSISDN_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.MSISDN_INVALID);
					} else if (code.equals(ERR_TIME_ZONE_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.TIMEZONE_MISSING);
					} else if (code.equals(ERR_TIME_ZONE_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.TIMEZONE_INVALID);
					} else if (code.equals(ERR_MOBILE_OPERATOR_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.MOBILE_OPERATOR_INVALID);
					} else if (code.equals(ERR_MSISDN_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.MSISDN_MISSING);
					} else if (code.equals(ERR_MOBILE_MODEL_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.MOBILE_MODEL_INVALID);
					} else if (code.equals(ERR_LANGUAGE_INVALID)) {
						cmMgr.registrationFailed(AuthenticationListener.LANGUAGE_INVALID);
					} else if (code.equals(ERR_IP_ADDRESS_MISSING)) {
						cmMgr.registrationFailed(AuthenticationListener.IP_ADDRESS_MISSING);
					} else if (code.equals(ERR_SESSION)) {
						cmMgr.authenticationFailed(AuthenticationListener.AUTH_NEW_PASSWORD);
					} else if (code.equals(ERR_AUTH_1)) {
						cmMgr.authenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
					} else if (code.equals(ERR_AUTH_2)) {	
						cmMgr.authenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
					} else if (code.equals(ERR_AUTH_USER_NOT_FOUND)) {	
						cmMgr.authenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
					} else {
						//#debug info
						System.out.println("Reporting other error");
						
						if (null != serviceReqListener) {
							serviceReqListener.errorReceived(requestID,
								ResponseListener.REQUEST_FAILED_UNKNOWN);
						}
					}
					
					code = null;
				}					
			} // end fault response
		} catch (Exception e) {
			//#debug error
			System.out.println("Something went wrong doing the fault callback. " + e);
			
			serviceReqListener.errorReceived(requestID, 
								ResponseListener.REQUEST_PARSING_FAILED);
		}
	}

	private ServiceObject[] parseServiceObjects(int tag, boolean isSingleItem) throws IOException {
		hasParsedServiceObject = true;
		
		if (tag == -1) {
			tag = isread();	// read the first 
		}
		
		if ((isSingleItem && ((tag != 'M') && (tag != 'I'))) || 
			((!isSingleItem) && (tag != 'V'))) {
			// the I is needed for a del contacts result with one item
			//#debug info
			System.out.println("Exiting because tag is " + ((char)tag));
			
			throw expect("either M for single ServiceObject or V for list of ServiceObjects", tag); 
		}
		
		// attention: let's check whether we have a list or just a single contact
		// single contact
		if(tag == 'M') {
			serviceObjects = new ServiceObject[1];
			
			try {
				Hashtable ht = readHash(tag);
				if (null != ht) {
					
					switch(searchedType) {
						/*case ServiceObject.CONTACT:
						case ServiceObject.USER_PROFILE:
							//#debug info
							System.out.println("New contact being parsed.");
							serviceObjects[0] = new Contact(ht);
							break;*/
						case ServiceObject.CONTACT_CHANGES:
							//#debug info
							System.out.println("New contact changes being parsed.");
							serviceObjects[0] = new ContactChanges(ht);
							break;
						// myChanges never appears inside a vector, only in a map
						case ServiceObject.MY_CHANGES:
							//#debug info
							System.out.println("New my changes being parsed.");
							serviceObjects[0] = new ContactChanges(ht);
							break;
						case ServiceObject.CONTACT_DELETION:
							serviceObjects[0] = new ContactsDeletionResult(ht);
							break;
						case ServiceObject.ACTIVITY:
							serviceObjects[0] = new Activity(ht);
							break;
						case ServiceObject.MY_IDENTITY:
						case ServiceObject.AVAILABLE_IDENTITY:
							serviceObjects[0] = new Identity(ht);
							break;
						case ServiceObject.GROUPS:
							serviceObjects[0] = new Group(ht);
							break;
						case ServiceObject.MESSAGE:
							break;
					}
						/*serviceReqListener.itemsReceived(requestID, serviceObjects, 
															searchedType);*/
					ht.clear();
					ht = null;
					
					hasParsedServiceObject = false;
					return serviceObjects;
				}
			} catch (Exception e) {
				serviceObjects[0] = null;
			}
		} else if(tag == 'I') {
			serviceObjects = new ServiceObject[1];
			
			try {
				int idInt = readInt(tag);

				switch(searchedType) {
					case ServiceObject.CONTACT_DELETION:
						serviceObjects[0] = new ContactsDeletionResult(idInt);
						break;
				}
			} catch (Exception e) {
				serviceObjects[0] = null;
			}
		} else if(tag == 'V') {
			int len = 0;

			tag = isread();	// read the next byte, look for t, l, or a M
			
			if (tag == 't') {	// it's a type
				int i = 0;

				i += (isread() << 8);
				i += isread();
				
				// we can ignore the type
				for(int j = 0; j < i; j++) {
					isread();
				}
				
				tag = isread();	// get next byte, should be 'l'
			}
			
			if (tag == 'l') {	// read length
				len = 0;
				
				len += (isread() << 24);
				len += (isread() << 16);				
				len += (isread() << 8);
				len += isread();
				
				tag = isread();	// get next byte, should be first contact 'M'
			}


			if (len == -1) {
				return null;
			} else {
				serviceObjects = new ServiceObject[len];
			}
			
			// read every entry
			for (int i = 0; i < serviceObjects.length; i++) {
				if (tag == 'M') {
					try {
						Hashtable ht = readHash(tag);
						
						if (null != ht) {
							switch(searchedType) {
								/*case ServiceObject.CONTACT:
								case ServiceObject.USER_PROFILE:
									serviceObjects[i] = new Contact(ht);			
									break;*/
								case ServiceObject.CONTACT_CHANGES:
									serviceObjects[i] = new ContactChanges(ht);			
									break;
								case ServiceObject.ACTIVITY:
									serviceObjects[i] = new Activity(ht);
									break;
								case ServiceObject.GROUPS:
									serviceObjects[i] = new Group(ht);
									break;
								case ServiceObject.MESSAGE:
									break;
								case ServiceObject.MY_IDENTITY:
								case ServiceObject.AVAILABLE_IDENTITY:
									serviceObjects[i] = new Identity(ht);
									break;
							}
						
							ht.clear();
							ht = null;
						}
					} catch (Exception e) {
						serviceObjects[i] = null;
					}
				} else if (tag == 'z') {	// end reached
					if (len != MAX_OBJECTS_TO_READ) {	// it's not variable length
						// it should not end before all items are passed
						throw expect("array item " + i + " of " + len, tag);
					} else {
						break;	// variable size, just exit
					}
				}
				
				tag = isread();
			}
			
			if (null == serviceObjects) {
				serviceObjects = new ServiceObject[1];
			}
			
			// call back with all contacts, messages, etc.
			/*if(null != serviceReqListener) {
				serviceReqListener.itemsReceived(requestID, serviceObjects, 
													searchedType);
			}*/
			hasParsedServiceObject = false;
			return serviceObjects;
		}
		
		return null;
	}
	
	private void cleanServiceObjects(ServiceObject[] serviceObjects) {
		if (null == serviceObjects) {
			return;
		}
		
		for (int i = 0; i < serviceObjects.length; i++) {
			serviceObjects[i] = null;
		}
		
		serviceObjects = null;
	}

	/**
	 * Reads a hash
	 * 
	 * <pre>
	 * B b16 b8 data value
	 * </pre>
	 */
	public Hashtable readHash() throws IOException {
		return readHash(-1);
	}

	private Hashtable readHash(int tag) throws IOException {
		if (tag == -1)
			tag = isread();
			
		// check if we have a map
		if (tag != 'M')
			throw expect("hash", tag);
		
			
		Hashtable r = new Hashtable();
		
		
		Object o = _readObject();
		
		if (o instanceof End)
			return r;
		if (o instanceof Type)
			o = _readObject();
		if (o instanceof End)
			return r;
		
		
		Object val = _readObject(null);
		
		if (val == null) {
// System.out.println("!!!!!!!!!NULL VALUE FOUND");
			val = new Null();
		}
		
		r.put(o, val);
		
		for (int i = 0; i < 65535; i++) {
			o = _readObject();
			if (o instanceof End) {
				return r;
			}
				
				
			val = _readObject(null);
			if (val == null) {
				val = new Null();
// System.out.println("!!!!!!!!!NULL VALUE FOUND2");
			} else {
				r.put(o, val);
			}
		}

// System.out.println("--------- ENDING HASH " + tag);		
		
		return r; // too many items, return what was read
	}



	/**
	 * Reads an array
	 * 
	 * <pre>
	 * B b16 b8 data value
	 * </pre>
	 */
	public Vector readArray() throws IOException {
		return readArray(-1);
	}

	public Vector readArray(String isList) throws IOException {
		return readArray(-1, isList);
	}

	private Vector readArray(int tag) throws IOException {
		return readArray(tag, null);
	}

	private Vector readArray( int tag, String isList ) throws IOException {
		if( tag == -1 )
			tag = isread();
			
		if( tag != 'V' )
			throw expect( "array", tag );
			
		int len=0;
		
		Vector v = new Vector();
		Object o = _readObject();
		
		if( o instanceof End )
			return v;
			
		if( o instanceof Type )
			o = _readObject();
			
		if( o instanceof Len ) {
			len = ((Len)o).len;
			o = _readObject();
		}
		
		if( o instanceof End )
			return v;
			
		for( int i=0; i<len; i++ ) {
			if( o instanceof End )
				throw expect("array item "+i+" of "+len, tag);
				
		    v.addElement(o);
			o = _readObject();
		}
		return v;
	}

	/**
	 * Reads an arbitrary object the input stream.
	 */
	public Object readObject() throws HessianException, IOException {
		
		return readFirstObject(-1);
	}

	public Object readObject(Class expectedClass) throws HessianException,
			IOException {
		return readFirstObject(-1, expectedClass);
	}

	private Object readFirstObject(int tag) throws HessianException,
			IOException {
		return readFirstObject(tag, null);
	}

	private Object readFirstObject(int tag, Class expectedClass)
			throws HessianException, IOException {

		if (tag == -1) {
			tag = isread();
		}
		
		// the response is a fault
		if (tag == 'f') {
			parseFault(tag);
			return null;
		} // end fault response
		
		return _readObject(tag, expectedClass);
	}

	private Object _readObject() throws IOException {
		return _readObject(-1, null, null);
	}

	private Object _readObject(String isList) throws IOException {
		return _readObject(-1, null, isList);
	}

	/*
	private Object _readObject( Class expectedClass ) throws IOException {
		return _readObject( -1, expectedClass );
	}
	private Object _readObject( int tag ) throws IOException {
		return _readObject( tag, null );
	}
	 */
	private Object _readObject(int tag, Class expectedClass) throws IOException {
		return _readObject(tag, expectedClass, null);
	}

	private Object _readObject(int tag, Class expectedClass, String isList)
			throws IOException {
		if (tag == -1) {
			tag = isread();
		}

		switch (tag) {
			case 't': // object type, skip and return type
				isread();
				isread();
				return new Type();
			case 'l':	// length
				int i = 0;
				
				i += (isread() << 24);
				i += (isread() << 16);
				i += (isread() << 8);
				i += isread();
				
				
				return new Len(i);
			case 'z':	// end
				
				return new End();
			case 'N':	// null
				return readNull(tag);
			case 'M':
				if (hasParsedServiceObject) {
					return readHash(tag);
				} else {
					return parseHash(tag);	
				}
			case 'V':
				return readArray(tag, isList);
			case 'T':
				return new Boolean(readBoolean(tag));
			case 'F':
				return new Boolean(readBoolean(tag));
			case 'I':
				return new Integer(readInt(tag));
			case 'L':
				Long l = new Long(readLong(tag));
				
				return l;
			case 'd':
				return readUTCDate(tag);
			case 'S':
				return readString(tag);
			case 'X':
				return readString(tag);
			case 'B':
				return readBytes(tag);
			case 'b':
				return readBytes(tag);
			case '\r':
			case '\n':
				return _readObject(-1, expectedClass, isList);
			case 'f':
				parseFault(tag);
				return null;
			default:
				throw expect("known code", tag);
		}
		

	}
	

	/**
	 * Reads a string from the underlying stream.
	 */
	protected String readStringImpl(int length) throws IOException {
		
		StringBuffer sb = new StringBuffer();
		
        int ch, ch1, ch2, v;
		byte[] b = null;
        
        for( int i = 0; i < length; i++ ) {
            ch = this.isread();
            if( ch < 0x80 )
                sb.append( (char) ch );
            else if( ( ch & 0xe0 ) == 0xc0 ) {
                ch1 = this.isread();
                v = ( ( ch & 0x1f ) << 6 ) + ( ch1 & 0x3f );
                sb.append( (char) v );
            }
            else if( ( ch & 0xf0 ) == 0xe0 ) {
                ch1 = this.isread();
                ch2 = this.isread();
                v = ( ( ch & 0x0f ) << 12 ) + ( ( ch1 & 0x3f ) << 6 ) + ( ch2 & 0x3f );
                sb.append( (char) v );
            }
            else if( ( ch & 0xff ) >= 0xf0 && ( ch & 0xff ) <= 0xf4 ) { // UTF-4
            	if(null == b)
            		b = new byte[ 4 ];
                b[0] = (byte) ch;
                b[1] = (byte) this.isread();
                b[2] = (byte) this.isread();
                b[3] = (byte) this.isread();
                sb.append( new String( b, "utf-8" ) );
                i++;
            }
            else
               throw new IOException( "bad utf-8 encoding at " + i + "/" + length );
        }

        return sb.toString();
	}


	protected IOException expect(String expect, int ch) {
		String debug = "";
		
		if (DEBUG) {
			debug = " " + new String(bos.toByteArray());
		}
		
		
		//#debug error
		System.out.println("Hessian Parsing failed!!!");
		
		//#debug error
		Toolkit.printHessian(is);
		
		if (ch < 0) {
			return protocolException("expected " + expect
					+ " at end of content." + debug);
		} else {
			return protocolException("expected " + expect + " at " + (char) ch
					+ " (" + Integer.toHexString((char) ch) + ")" + debug);
		}
	}


	protected IOException protocolException(String message) {
		return new IOException(message);
	}
	
	public void printDebug() {
		// System.out.println(new String(bos.toByteArray()));
	}
}