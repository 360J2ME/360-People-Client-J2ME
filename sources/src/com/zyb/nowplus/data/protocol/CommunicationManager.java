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

import java.util.Hashtable;

import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;
import com.zyb.nowplus.data.protocol.types.ServiceObject;

/**
 * Manages the asynchronous connection to the Jibe Backend by handling all connections.
 * Allows creating, encoding and sending requests (e.g. setting contacts, getting activities) 
 * and retrieving responses as well as decoding them.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public interface CommunicationManager {
	
	/**
	 * 
	 * Starts the connections. Based on the handset capabilities it will either start
	 * only 1 connection or 2 connections.
	 * 
	 */
	public void startConnections();
	
	/**
	 * 
	 * Stops the connections. Based on the handset capabilities it will either stop
	 * only 1 connection or 2 connections.
	 * 
	 */
	public void stopConnections(boolean finalStop);
	
	/**
	 * 
	 * Reauthenticates with the backend to allow further calls. It should not
	 * be called generally. It should only be called the first time the client
	 * ever connects to the network OR when the AuthenticationRequestListener is called
	 * back.
	 * 
	 * @param username The username to pass on to the connection.
	 * @param password The password to pass on to the connection.
	 * @param doStoreSession If true the session will be stored, if false it
	 * will not.
	 * @param authConnection Optional, should be passed for unit tests only. 
	 * Connection used by the communication manager.
	 * 
	 * @throws IllegalArgumentException If username and/or password are null.
	 * 
	 */
	public void authenticate(String username, String password, 
								boolean doStoreSession, 
								HttpRPGConnection authConnection) 
			throws IllegalArgumentException;
	
	/**
	 * 
	 * Used to sign up (register) a user for the Now+ service. The method is called
	 * synchronously via a HTTPS connection.
	 * 
	 * @param username The username to register.
	 * @param password The password for the username.
	 * @param fullName The fullName, this is optional.
	 * @param birthdate The birthdate in the format YYYY-MM-DD.
	 * @param msisdn The MSISDN of the user.
	 * @param acceptedTermsAndConditions True if the user accepted the T&Cs.
	 * @param countryCode The country code, e.g. de for Germany.
	 * @param timezone The timezone of the client, e.g. Europe/Berlin.
	 * @param language The language of the client, e.g. en-GB or de-DE.
	 * @param mobileOperatorID The mobile operator ID as per Zyb XML definition.
	 * @param mobileModelID The mobile model ID as per Zyb XML definition.
	 * @param subscribedToNewsletter True if the user wants to have the newsletter.
	 * @param signupConnection Optional, should be passed for unit tests only. 
	 * Connection used by the communication manager.
	 * 
	 */
	public void registerUser(String username, String password, String fullName, 
								String birthdate, String msisdn, 
								boolean acceptedTermsAndConditions, 
								String countryCode, String userEmailAddr, String timezone, 
								String language, int mobileOperatorID,
								int mobileModelID, boolean subscribedToNewsletter,
								HttpRPGConnection signupConnection);
	
	
	/**
	 * 
	 * Needed if the activation SMS after the registerUser never arrived and the
	 * activationTimerExpired() method was called. This method is triggered by
	 * the user after the first activation attempt went wrong.
	 * 
	 * @param username
	 *            The username of the registered user.
	 * @param msisdn
	 *            The MSISDN of the user to send the SMS to.
	 * 
	 */
	public void rerequestActivationCodeForUser(String username, String msisdn);
		
	/**
	 * 
	 * Checks for new client updates on the server.
	 * 
	 * @param versionNumber The version number of the current client.
	 * @param updateConnection The mockup connection for unit tests.
	 * 
	 */
	public void checkForUpdates(String versionNumber, HttpRPGConnection updateConnection);
	
    /**
	 * Called back when client is initialized.
	 */
	public void clientInitialized();

	/**
	 * 
	 * Autodetects the best connection for the MIDlet. If TCP can be used it 
	 * will be set in the RMS. If not HTTP will be used. This method only checks
	 * once and persists its data later on for non-Vofafone customers. For Vodafone
	 * customers TCP will always be used.
	 * 
	 * @param supportsTcp True if the MIDlet supports Tcp, false otherwise.
	 * 
	 */
	public void autodetectConnection(boolean supportsTcp);
	
	public void registerListeners(AuthenticationListener authListener,
			 NetworkListener netListener, ResponseListener responseListener);
	
	/**
	 * 
	 * Requests the MSISDN of the user. This can be passed on if the user is a
	 * Vodafone customer and he accesses via a supported gateway.
	 * 
	 * @param conn An RPGConnection that can be used for unit testing.
	 * 
	 */
	public void requestMsisdn(HttpRPGConnection conn);
	
	
	/**
	 * 
	 * Adds a new request to the queue which is implemented as a pushback queue
	 * (@link{RequestQueue RequestQueue}). Requests are used to
	 * set, get and/or delete contacts, identities, activities and/or messages.
	 * 
	 * @param verb The verb. Verbs are defined inside the 
	 * {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}-class 
	 * and define the action, whether it is getting, setting or deleting of items (nouns)
	 * @param noun The item-type to get. E.g. an activity or a contact. Nouns are defined
	 * inside {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}-class.
	 * @param items The items to be used in a set- or delete-request. Can be null if it
	 * is a get-request.
	 * @param parameters Parameters are passed to filter certain requests. Parameters passed
	 * are defined in the developer documentation. The parameter that must not be passed, 
	 * however, is the auth-parameter as it is generated automatically. Parameters can be 
	 * null if no filters and other optional parameters are required for a method.
	 * @param priority The priority of the request. There are 3 priorities defined in
	 * {ConnectionRequest ConnectionRequest}.
	 * 
	 * @return An integer marking the unique id of the ConnectionRequest or -1 if there was 
	 * an error.
	 * 
	 */
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, 
								Hashtable parameters, byte priority);
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, 
			Hashtable parameters, byte priority, boolean fireAndForget);
	
	/**
	 * 
	 * Adds a new request to the queue which is implemented as a pushback queue
	 * ({@link com.zyb.nowplus.data.protocol.request.RequestQueue RequestQueue}). Requests are used to
	 * set, get and/or delete contacts, identities, activities and/or messages.
	 * 
	 * @param verb The verb. Verbs are defined inside the 
	 * {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}-class and define the action, whether 
	 * it is getting, setting or deleting of items (nouns)
	 * @param noun The item-type to get. E.g. an activity or a contact. Nouns are defined
	 * inside {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}.
	 * @param items The items to be used in a set- or delete-request. Can be null if it
	 * is a get-request.
	 * @param parameters Parameters are passed to filter certain requests. Parameters passed
	 * are defined in the developer documentation. The parameter that must not be passed, 
	 * however, is the auth-parameter as it is generated automatically. Parameters can be 
	 * null if no filters and other optional parameters are required for a method.
	 * @param priority The priority of the request. There are 3 priorities defined in
	 * {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}.
	 * @param timeoutInSecs The amount of time in seconds when the request will timeout.
	 * 
	 * @return An integer marking the unique id of the ConnectionRequest or -1 if there was 
	 * an error.
	 * 
	 */
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, 
								Hashtable parameters, byte priority, int timeoutInSecs);
	
	/**
	 * 
	 * Sends multiple requests in one batch. This method is identical to sendRequest
	 * but uses multidimensional arrays for multiple requests. The call
	 * ServerRequest.VALIDATE / ServerRequest.IDENTITIES MUST NOT be carried out
	 * using this method!!!
	 * 
	 */
	public int[] sendMultipleRequests(byte[] verbs, byte[] nouns, 
			ServiceObject[][] items, Hashtable[] params, byte[] priorities);
	
	/**
	 * <p>Sends a request to create a conversation. The asynchronous callback
	 * will send the conversation ID back that was created on the server-
	 * side.</p>
	 * <p>The request is followed by an asynchronous callback to the ResponseListener's
	 * itemsReceived()-method where 1 ChatObject is passed back. The ChatObject contains
	 * the created conversationID which can be retrieved using getConversationID().</p>
	 * 
	 * @param identities The list of user IDs (as Strings) or
	 * chat identities.
	 * 
	 * @return The request ID. Needs to be matched with the callback
	 * to resolve the conversation ID later on.
	 */
	public int sendCreateConversationRequest(String network, String name);
	
	/**
	 * 
	 * <p>Sends a request to stop a conversation. The asynchronous callback
	 * will send the conversation ID back to make sure that
	 * the conversation was closed.</p>
	 * <p>The request is a fire and forget request meaning that nothing will be 
	 * returned except for the successful HTTP 200 status code.</p>
	 * 
	 * @param conversationID The conversation ID to stop with.
	 * 
	 * @return The request ID. Can be ignored in most cases.
	 * 
	 */
	public int sendStopConversationRequest(String network, String name, String conversationID);
	
	/**
	 * <p>Sends a chat message to the people defined in the user ids via the RPG.</p>
	 * 
	 * <p>The request is a fire and forget request meaning that nothing will be 
	 * returned except for the successful HTTP 200 status code.</p>
	 * 
	 * @param conversationID The ID of the conversation. Needs to be created by
	 * calling createConversation().
	 * @param identities The user ids (as Strings) or chat identities to send the 
	 * message to.
	 * @param body The message text to send.
	 * 
	 * @return The request ID for the message. Can most likely be ignored.
	 */
	public int sendChatMessage(String network, String name, String conversationID, String body);
	
	/**
	 * Gets the presence information of all users on the RPG.
	 * 
	 * @return The request ID of the operation.
	 */
	public int getPresences();
	
	/**
	 * 
	 * Adds a new binary request to the queue which is implemented as a pushback queue
	 * (@link{RequestQueue RequestQueue}). Binaries include thumbnails,
	 * audio, video, etc.
	 * 
	 * @param url The URL to load the binary from.
	 * 
	 * @return An integer marking the unique id of the ConnectionRequest or -1 if there was 
	 * an error.
	 * 
	 */
	public int loadBinary(String url);
	
	/**
	 * Sends one request for multiple
	 * 
	 * @param urls The URLs to load the binaries from.
	 * 
	 * @return Multiple request IDs for each binary one.
	 */
	public int[] loadBinaries(String[] urls);

	/**
	 * 
	 * Removes a request from the connection request queue and returns true if
	 * successful.
	 * 
	 * @param requestID The ID for the request to be removed.
	 * 
	 * @return True if the request was able to be removed, false otherwise.
	 * 
	 */
	public boolean cancelRequest(int requestID);
	
	/**
	 * 
	 * Returns true if the client is in roaming mode or 
	 * false if the client is in the home network. This method
	 * contains vendor dependent (Sony Ericsson) method calls!
	 * 
	 * @return True if the client is roaming, false otherwise.
	 * 
	 */
	public boolean isRoaming();
}
