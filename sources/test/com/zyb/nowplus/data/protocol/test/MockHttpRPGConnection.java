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

import com.zyb.nowplus.data.protocol.*;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;

/**
 * User: ftite
 */

public class MockHttpRPGConnection extends HttpRPGConnection {
    private boolean requestStatus = true;
    public boolean startFlag = false;

    
    public MockHttpRPGConnection(CommunicationManagerImpl cmMgr, ResponseDecoder responseDecoder, AuthenticationListener authListener, byte connectionMode) {
        super("mock", cmMgr, responseDecoder, authListener, null, null, connectionMode);
    }

    public void startConnection() {
        startFlag = true;
        super.startConnection();
    }

    public void stopConnection(boolean finalStop) {
        super.stopConnection(finalStop);
    }

    public boolean isRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(boolean requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void run() {
        try {
            if (null != authListener) {
                if ((null != sessionID) && (null != sessionSecret) && (null != userID)) {
                    authListener.authenticationSucceeded();
                } else if (username != null && !username.equals("") && password != null && !password.equals("")) {
                    authListener.authenticationSucceeded();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean mockAuthenticate(String username, String password) {
        boolean flag = false;
        try {
             flag = super.authenticate(username, password);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
      return flag;
    }
}
