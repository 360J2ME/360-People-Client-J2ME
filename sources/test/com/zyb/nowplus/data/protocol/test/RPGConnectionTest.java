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
import com.zyb.nowplus.data.protocol.transport.RPGConnection;

/**
 * User: ftite
 */

public class RPGConnectionTest extends TestCase implements AuthenticationListener {
//    private static final String USER = "florin.tite";
//    private static final String PASSWORD = "v0daf0n31";

    MockRPGConnection rpgConnection;


    public void setUp()
    {
    }

    /**
     * Test constructor using OUT_IN_BOUND connection mode
     */
    public void test_constructor_connection_mode_out_in_bound() {
        rpgConnection = new MockRPGConnection(null, null, this, RPGConnection.OUT_IN_BOUND);
        assertNotNull(rpgConnection);
    }

    /**
     * Test constructor using null auth listener and invalid connection mode
     */
    public void test_constructor_invalid_params() {
        rpgConnection = new MockRPGConnection(null, null, null, (byte)1111);
        assertNotNull(rpgConnection);
    }

    /**
     * Test constructor using OUTBOUND connection mode
     */
    public void test_constructor_connection_mode_other() {
        rpgConnection = new MockRPGConnection(null, null, this, RPGConnection.OUTBOUND);
        assertNotNull(rpgConnection);
    }

    /**
     * Negative test - username and passwod are null, auth should fail 
     */
    /*public void test_authenticate_null_credentials() {
        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
        assertFalse(rpgConnection.mockAuthenticate(null, null));
    }*/

    /**
     * Negative test - CommunicationManagerImpl instance null
     * should be ok, we only test the auth logic and CommunicationManagerImpl obj should not be involved 
     */
//    public void test_authenticate_null_CommunicationManagerImpl() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.reset();
//        assertTrue(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - the response contains fault data tag
     * auth should fail, readObject() method should return null
     */
//    public void test_authenticate_response_with_fault_data_tag() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.responseWithFaultCodeFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing userId data from the response
     */
//    public void test_authenticate_response_with_no_userId_data() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noUserIdAttrErrorFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing sessionSecret data from the response
     */
//    public void test_authenticate_response_with_no_sessionSecret_data() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noSessionSecretAttrErrorFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing sessionId data from the response
     */
//    public void test_authenticate_response_with_no_sessionId_data() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noSessionIdAttrErrorFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing startReply tag from the response
     */
//    public void test_authenticate_response_with_no_startReply_tag() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noStartReplyTagFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing completeReply tag from the response
     */
//    public void test_authenticate_response_with_no_completeReply_tag() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noCompleteReplyTagFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - missing "session" attribute from the response
     */
//    public void test_authenticate_response_with_no_session_attribute() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.noSessionAttrErrorFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - invalid data associated with the "session" attribute in the response
     */
//    public void test_authenticate_invalid_response_data() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.invalidResponseDataFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Negative test - response is null, no tags or data in it
     */
//    public void test_authenticate_null_response() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        rpgConnection.nullResponseDataFlag = true;
//        assertFalse(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    /**
     * Positive test - a fake response is used to indicate a success auth
     */
//    public void test_authenticate_success() {
//        rpgConnection = new MockRPGConnection(this, RPGConnection.OUTBOUND);
//        CommunicationManagerImpl.getInstance(this, null);
//        assertTrue(rpgConnection.mockAuthenticate(USER, PASSWORD));
//    }

    public void tearDown()
	{
        rpgConnection = null;
        RequestQueue.reset();
    }

    public void authenticationSucceeded() {
    }

    public void authenticationFailed(int authCode) {
    }

	public void userDisallowedConnection() {
		// TODO Auto-generated method stub
		
	}

	public void registrationFailed(int errorCode) {
		// TODO Auto-generated method stub
		
	}

	public void registrationSucceeded(long userID) {
		// TODO Auto-generated method stub
		
	}
}
