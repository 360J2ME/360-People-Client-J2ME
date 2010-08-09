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

import com.zyb.nowplus.test.TestCase;
import com.zyb.nowplus.data.protocol.*;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.Update;

/**
 * User: ftite
 */

public class CommunicationManagerImplTest extends TestCase implements AuthenticationListener, ResponseListener {

    private static final String CONVERSATION_ID = "test";
    private static final String URL = "http://test.com";
    private static final String USER = "florin.tite";
    private static final String PASSWORD = "v0daf0n31";

    private int authCode;

    CommunicationManager connManager;
    MockHttpRPGConnection mockHttpRPGConnectionOut;

    
    public void setUp()
    {
        resetMsg();
        mockHttpRPGConnectionOut = new MockHttpRPGConnection(null, null, this, RPGConnection.OUTBOUND);
        //single connection for in/out
        connManager = new CommunicationManagerImpl(null, this, mockHttpRPGConnectionOut, mockHttpRPGConnectionOut);
    }

    private void resetMsg() {
        authCode = 0;
    }

    /**
     * Test constructor - fail if MockHttpRPGConnection is not used
     */
    public void test_constructor_single_mock_connection_mode_null_session_values() {
        connManager = new CommunicationManagerImpl(null, this,
                                                     null, mockHttpRPGConnectionOut);
        connManager.startConnections();
        uninit();
        assertFalse( mockHttpRPGConnectionOut.startFlag);
    }

    /**
     * Test constructor - fail if MockHttpRPGConnection is not used
     */
    public void test_constructor_single_mock_connection_mode_session_values_not_null() {
        RPGConnection.sessionID = "1";
        RPGConnection.sessionSecret = "2";
        RPGConnection.userID = "3";
        connManager = new CommunicationManagerImpl(null, this,
                                                     null, mockHttpRPGConnectionOut);
        connManager.startConnections();
        uninit();
        assertTrue("The provided connection object was not used: ", mockHttpRPGConnectionOut.startFlag);
    }

    /**
     * Negative test - start connection with null user/pass and no sored session values
     * authenticationFailed method should be invoked and authMsg should be equal to AUTH_ERR 
     */
    public void test_startConnections_with_null_credentials_no_session_values() {
        connManager = null;
        connManager = new CommunicationManagerImpl(null, this,
                                                     mockHttpRPGConnectionOut, mockHttpRPGConnectionOut);
        connManager.startConnections();
        uninit();
        assertEquals("Expected failure: ", AuthenticationListener.AUTH_FAILED_UNKNOWN, authCode);

    }

    /**
     * Positive test - start connection with valid credentials
     * auth msg should not contain any error msg 
     */
    public void test_startConnections_with_credentials() {
        RPGConnection.sessionID = "1";
        RPGConnection.sessionSecret = "2";
        RPGConnection.userID = "3";
        connManager.startConnections();
        uninit();
        assertEquals("Expected success: ", 0, authCode);
    }

    /**
     * Negative test - reauthenticate null params, expected IllegalArgumentException  
     */
    public void test_reauthenticate_null(){
            connManager.authenticate(null, null, false, mockHttpRPGConnectionOut);
            uninit();
            assertTrue(true);
    }

    /**
     * Positive test - valid session values and credentials
     * authMsg should nor contain error msg 
     */
    public void test_reauthenticate_not_null(){
        try {
            RPGConnection.sessionID = "1";
            RPGConnection.sessionSecret = "2";
            RPGConnection.userID = "3";
            connManager.authenticate(USER, PASSWORD, false, mockHttpRPGConnectionOut);
            uninit();
            assertEquals("Expected success: ", 0, authCode);
        } catch (IllegalArgumentException iae) {
            uninit();
            assertTrue("Unexpected IllegalArgumentException.",false);
        }
    }

    /**
     * Test send request, output should not be null 
     */
    public void test_sendRequest(){
        int res = connManager.sendRequest(ServerRequest.GET,ServerRequest.USER_PROFILES, null, null,
                                            ServerRequest.MEDIUM_PRIORITY);
        assertTrue(res > 0);
    }

    /**
     * Test send sendRequest with timeout in Secs
     */
    public void test_sendRequest_timeoutInSecs(){
        int res = connManager.sendRequest(ServerRequest.GET,ServerRequest.USER_PROFILES, null, null,
                                            ServerRequest.MEDIUM_PRIORITY, 1);
        assertTrue(res > 0);
    }

    /**
     * Test send  Create Conversation Request
     */
    public void test_sendCreateConversationRequest(){
        int res = connManager.sendCreateConversationRequest(null, "1");
        assertTrue(res > 0);
    }

    /**
     * Test send stop conversation request 
     */
    public void test_sendStopConversationRequest(){
        int stopRes = connManager.sendStopConversationRequest(null, "1", CONVERSATION_ID);
        assertTrue(stopRes > 0);
    }

    /**
     * Test send chat message
     */
    public void test_sendChatMessage(){
        int chatRes = connManager.sendChatMessage(null, "1", CONVERSATION_ID, "test");
        assertTrue(chatRes > 0);
    }

    /**
     * Test getting the presence information of all users on the RPG.
     */
    public void test_getPresence() {
        int res = connManager.getPresences();
        assertTrue(res > 0);
    }

    /**
     * Test load binary data to request queue 
     */
    public void test_loadBinary(){
        int res = connManager.loadBinary(URL);
        assertTrue(res > 0);
    }

    /**
     * Test load Binaries to request queue
     */
    public void test_loadBinaries(){
        String[] urls = new String[]{"test1", "test2"};
        assertNotNull(connManager.loadBinaries(urls));
    }

    /**
     * Test - cancel a created request, should be removed from the request queue  
     */
    public void test_cancelRequest_created(){
        int res = connManager.sendCreateConversationRequest(null, "1");
        assertTrue(connManager.cancelRequest(res));
    }

    /**
     * Negative test - cancel a request that was not created 
     */
    public void test_cancelRequest_not_created(){
        assertFalse(connManager.cancelRequest(-1));
    }

    private void uninit() {
        RPGConnection.sessionID = null;
        RPGConnection.sessionSecret = null;
        RPGConnection.userID = null;
    }

    public void tearDown()
	{
        uninit();
        RequestQueue.reset();
        mockHttpRPGConnectionOut = null;
        connManager = null;
    }

    public void authenticationSucceeded() {
        resetMsg();
    }

    public void authenticationFailed(int authCode) {
        this.authCode = authCode;
    }

    public void instantMessageReceived(String conversationID, String fromUserID, String[] toUserIDs, String message) {
        //do nothing
    }

    public void presenceChangeReceived(int requestID, Presence presence) {
        //do nothing
    }

    public void itemsReceived(int requestID, ServiceObject[] serviceObjects, byte type) {
        //do nothing
    }

    public void itemsReceived(int requestID, byte[] data, byte itemType) {
        //do nothing
    }

    public void errorReceived(int requestID, byte errorCode) {
        //do nothing
    }

	public void networkErrorReceived(byte errorCode) {
		// TODO Auto-generated method stub
		
	}

	public void userDisallowedConnection() {
		// TODO Auto-generated method stub
		
	}

	public void pushReceived(APIEvent apiEvt) {
		// TODO Auto-generated method stub
		
	}

	public void registrationFailed(int errorCode) {
		// TODO Auto-generated method stub
		
	}

	public void registrationSucceeded(long userID) {
		// TODO Auto-generated method stub
		
	}

	public void clientIsUpToDate() {
		// TODO Auto-generated method stub
		
	}

	public void clientUpdateAvailable(Update update) {
		// TODO Auto-generated method stub
		
	}

	public void clientInitialized() {
		// TODO Auto-generated method stub

	}

	public void dataTransmitted(int dataCounter) {
		// TODO Auto-generated method stub
		
	}

	public boolean isBusy() {
		// TODO Auto-generated method stub
		return false;
	}

	public void msisdnReceived(String msisdn) {
		// TODO Auto-generated method stub
		
	}
}
