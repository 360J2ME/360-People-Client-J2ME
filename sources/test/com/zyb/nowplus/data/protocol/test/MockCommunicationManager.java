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
package com.zyb.nowplus.data.protocol.test;

import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.event.test.MockEventDispatcher;
import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManager;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Update;
import com.zyb.util.event.Event;

public class MockCommunicationManager extends MockEventDispatcher implements CommunicationManager 
{
	public static final int CHECK_UPGRADE = 300;
	public static final int START_CONNECTIONS = 301;
	public static final int STOP_CONNECTIONS = 302;
	public static final int REGISTER_USER = 303;
	public static final int REREQUEST_ACTIVATION_CODE = 304;
	public static final int AUTHENTICATE = 305;
	public static final int SEND_REQUEST = 306;
	public static final int SET_ME = 307;
	public static final int UPDATE_CONTACTS = 308;
	public static final int SET_CONTACT_GROUP_RELATIONS = 309;
	public static final int DELETE_CONTACTS = 310;
	public static final int START_CONVERSATION = 311;
	public static final int STOP_CONVERSATION = 312;
	public static final int SEND_CHAT_MESSAGE = 313;
	
	private AuthenticationListener authListener;
	private ResponseListener responseListener;
	private NetworkListener networkListener;
	private int requestId;

	private int[] dynamicRequestId;
	private byte[] dynamicType;
	private int[] dynamicVerb;
	private int[] dynamicNoun;
	
	public MockCommunicationManager(int numberOfEvents)
	{
		this(numberOfEvents, new byte[0], new int[0], new int[0]);
	}
	
	public MockCommunicationManager(int numberOfEvents, byte[] dynamicType, int[] dynamicVerb, int[] dynamicNoun) 
	{
		super(numberOfEvents);
		
		this.dynamicRequestId = new int[dynamicType.length];
		this.dynamicType = dynamicType;
		this.dynamicVerb = dynamicVerb;
		this.dynamicNoun = dynamicNoun;
	}
	
	// implementation of CommunicationManager
	
	public void registerListeners(AuthenticationListener authListener,
			NetworkListener netListener, ResponseListener responseListener) {
		this.authListener = authListener;
		this.networkListener = netListener;
		this.responseListener = responseListener;		
	}

	public void startConnections()
	{
		notifyEvent(Event.Context.TEST, START_CONNECTIONS, null);
	}

	public void stopConnections(boolean finalStop) 
	{
		notifyEvent(Event.Context.TEST, STOP_CONNECTIONS, null);
	}
	
	public void checkForUpdates(String version, HttpRPGConnection conn)
	{
		notifyEvent(Event.Context.TEST, CHECK_UPGRADE, version);
	}

	public void clientInitialized()
	{
		// TODO Auto-generated method stub
	}

	public boolean isRoaming() 
	{
		// TODO
		return false;
	}

	public void registerUser(String username, String password, String fullName,
			String birthdate, String msisdn,
			boolean acceptedTermsAndConditions, String countryCode,String userEmailAddr,
			String timezone, String language, int mobileOperatorID,
			int mobileModelID, boolean subscribedToNewsletter,
			HttpRPGConnection signupConnection)
	{
		notifyEvent(Event.Context.TEST, REGISTER_USER, username + ":" + password + ":" + fullName 
				+ ":" + birthdate + ":" + msisdn 
				+ ":" + acceptedTermsAndConditions + ":" + countryCode
				+ ":" + userEmailAddr
				+ ":" + timezone + ":" + language + ":" + mobileOperatorID
				+ ":" + mobileModelID + ":" + subscribedToNewsletter);
	}
	
	public void rerequestActivationCodeForUser(String username, String msisdn)
	{
		notifyEvent(Event.Context.TEST, REREQUEST_ACTIVATION_CODE, username + ":" + msisdn);
	}
	
	public void authenticate(String username, String password, boolean doStoreSession, HttpRPGConnection conn) throws IllegalArgumentException 
	{
		notifyEvent(Event.Context.TEST, AUTHENTICATE, username + ":" + password);
	}	

	public int sendRequest(byte verb, byte noun, ServiceObject[] items, Hashtable filters, byte priority) 
	{
		return sendRequest(verb, noun, items, filters, priority, 0);
	}

	public int sendRequest(byte verb, byte noun, ServiceObject[] items,	Hashtable filters, byte priority, boolean fireAndForget) {
		return sendRequest(verb, noun, items, filters, priority, 0);
	}

	public int sendRequest(byte verb, byte noun, ServiceObject[] items, Hashtable filters, byte priority, int timeOut) 
	{
		requestId++;

		for (int i = 0; i < dynamicRequestId.length; i++)
		{
			if ((verb == dynamicVerb[i]) && (noun == dynamicNoun[i]))
			{
				dynamicRequestId[i] = requestId;
				break;
			}
		}

		String request = verb + ":" + noun; 
		if ((verb == ServerRequest.SET) && (noun == ServerRequest.ME))
		{
			notifyEvent(Event.Context.TEST, SET_ME, items);
			return requestId;
		}
		else
		if ((verb == ServerRequest.SET) && (noun == ServerRequest.CONTACT_GROUP_RELATIONS))
		{
			notifyEvent(Event.Context.TEST, SET_CONTACT_GROUP_RELATIONS, items);
			return requestId;
		}
		else
		if ((verb == ServerRequest.DELETE) && (noun == ServerRequest.CONTACTS))
		{
			notifyEvent(Event.Context.TEST, DELETE_CONTACTS, filters);
			return requestId;
		}
		else		
		if ((verb == ServerRequest.BULK_UPDATE) && (noun == ServerRequest.CONTACTS))
		{
			notifyEvent(Event.Context.TEST, UPDATE_CONTACTS, items);
			return requestId;
		}
		else			
		if (((verb == ServerRequest.SET) || (verb == ServerRequest.DELETE)) && (noun == ServerRequest.IDENTITIES))
		{
			com.zyb.nowplus.data.protocol.types.Identity identity = (com.zyb.nowplus.data.protocol.types.Identity) items[0]; 
			request += ":" + identity.getPluginid() + ":" + identity.getNetwork() + ":" + identity.getIdentityID() + ":" + identity.getStatus(); 
		}
		else			
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.FRIENDS_OF_FRIENDS))
		{
			Vector userIds = (Vector) ((Hashtable) filters).get("useridlist");
			request += ":" + userIds.elementAt(0);
		}
		notifyEvent(Event.Context.TEST, SEND_REQUEST, request);
		return requestId;
	}
	
	public int[] sendMultipleRequests(byte[] verbs, byte[] nouns, 
			ServiceObject[][] items, Hashtable[] params, byte[] priorities) {
		return new int[] {55, 33, 22};
	}
	
	public int sendCreateConversationRequest(String network, String name) 
	{
		notifyEvent(Event.Context.TEST, START_CONVERSATION, network + ":" + name);
		return ++requestId;
	}
	
	public int sendChatMessage(String network, String name, String conversationId, String body) 
	{
		notifyEvent(Event.Context.TEST, SEND_CHAT_MESSAGE, network + ":" + name + ":" + conversationId + ":" + body);
		return ++requestId;
	}	

	public int sendStopConversationRequest(String network, String name, String conversationId) 
	{
		notifyEvent(Event.Context.TEST, STOP_CONVERSATION, network + ":" + name + ":" + conversationId);
		return ++requestId;
	}	

	public int loadBinary(String url)
	{
		return 0;
	}
	
	public int[] loadBinaries(String[] urls)
	{
		return new int[0];
	}
	
	public boolean cancelRequest(int requestId) 
	{
		return false;
	}

	public int getPresences() 
	{
		return 0;
	}
	
	public void autodetectConnection(boolean supportsTcp)
	{
		networkListener.autodetectConnectionFinished();
	}
	
	// for testing
	
	public void mockUpgradeInfoReceived(Update upgradeInfo) throws Exception
	{
		if (upgradeInfo == null)
		{
			responseListener.clientIsUpToDate();
		}
		else
		{
			responseListener.clientUpdateAvailable(upgradeInfo);
		}
		
		// give the model some time
		Thread.sleep(100);		
	}
	
	public void mockRegistrationSucceeded() throws Exception
	{
		authListener.registrationSucceeded(0);
		
		// give the model some time
		Thread.sleep(100);	
	}
	
	public void mockRegistrationFailed(int code) throws Exception
	{
		authListener.registrationFailed(code);
		
		// give the model some time
		Thread.sleep(100);	
	}
	
	public void mockAuthenticationSucceeded() throws Exception
	{
		authListener.authenticationSucceeded();
		
		// give the model some time
		Thread.sleep(100);	
	}
	
	public void mockAuthenticationFailed(int code) throws Exception
	{
		authListener.authenticationFailed(code);
		
		// give the model some time
		Thread.sleep(100);	
	}
	
	public void mockUserDisallowedConnection() throws Exception
	{
		authListener.userDisallowedConnection();
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockItemsReceived(int reqId, ServiceObject[] serviceObjects, byte type) throws Exception
	{
		responseListener.itemsReceived(reqId, serviceObjects, type);
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockItemsReceived(ServiceObject[] serviceObjects, byte type) throws Exception
	{
		for (int i = 0; i < dynamicRequestId.length; i++)
		{
			if (type == dynamicType[i])
			{
				mockItemsReceived(dynamicRequestId[i], serviceObjects, type);
				break;
			}
		}
	}
	
	public void mockPushReceived(APIEvent apiEvt) throws Exception
	{
		responseListener.pushReceived(apiEvt);
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockPresenceChangeReceived(int reqId, Presence presences)
	{
		responseListener.presenceChangeReceived(reqId, presences);
	}
	
	
	public void mockErrorReceived(byte errorCode, byte type) throws Exception
	{
		for (int i = 0; i < dynamicRequestId.length; i++)
		{
			if (type == dynamicType[i])
			{
				mockErrorReceived(dynamicRequestId[i], errorCode, type);
				break;
			}
		}
	}
	
	public void mockErrorReceived(int reqId, byte errorCode, byte type) throws Exception
	{
		responseListener.errorReceived(reqId, errorCode);
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockNetworkUp() throws Exception
	{
		networkListener.networkUp();
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockNetworkDown(byte networkCode) throws Exception
	{
		networkListener.networkDown();
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockRoaming() throws Exception
	{
		networkListener.roamingActive();
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockTraffic(int size) throws Exception
	{
		responseListener.dataTransmitted(size);
		
		// give the model some time
		Thread.sleep(100);
	}
	
	public String toString()
	{
		return "MockCommunicationManager["
			+ "]";
	}
	
	public void requestMsisdn(HttpRPGConnection conn) {
		// TODO Auto-generated method stub
		
	}
}
