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

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.hessian.MicroHessianOutput;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;

import java.util.Hashtable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * User: ftite
 */

public class MockRPGConnection extends RPGConnection {

    public static String mockSessionId = "123";
    public static String mockSessionsecret = "secret";
    public static long mockUserid = 2;

    public boolean nullResponseDataFlag = false;
    public boolean noStartReplyTagFlag = false;
    public boolean noCompleteReplyTagFlag = false;
    public boolean invalidResponseDataFlag = false;

    public boolean noSessionAttrErrorFlag = false;
    public boolean noSessionIdAttrErrorFlag = false;
    public boolean noSessionSecretAttrErrorFlag = false;
    public boolean noUserIdAttrErrorFlag = false;
    public boolean responseWithFaultCodeFlag = false;
    
    protected MockRPGConnection(CommunicationManagerImpl cmMgr, ResponseDecoder responseDecoder, AuthenticationListener authListener, byte connectionMode) {
        super("mock", cmMgr, responseDecoder, authListener, null, null);
    }
    
	public byte[] invokeRPGRequest(byte[] requestData, int requestType,
			int[] requestIDs) {
        Hashtable hContent = null;
        Hashtable hResp = null;

        if(requestType == REQUESTTYPE_AUTH){
            //test null response data
            if (!nullResponseDataFlag) {
                hResp = new Hashtable();
                hContent = new Hashtable();

                //test missing sessionid
                if (!noSessionIdAttrErrorFlag)
                    hContent.put("sessionid", mockSessionId);
                //test missing sessionsecret
                if (!noSessionSecretAttrErrorFlag)
                    hContent.put("sessionsecret", mockSessionsecret);
                //test missing userid
                if (!noUserIdAttrErrorFlag)
                    hContent.put("userid", new Long(mockUserid));
                //test missing session data
                if (!noSessionAttrErrorFlag)
                    hResp.put("session", hContent);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                MicroHessianOutput out = new MicroHessianOutput(os);

                try {
                    //test response with no startReply tag 
                    if (!noStartReplyTagFlag)
                        out.startReply();
                    //test response with invalid data content
                    if (invalidResponseDataFlag)
                        out.writeBytes("error".getBytes());
                    else if (responseWithFaultCodeFlag) {
                        out.writeFault(null, null, null);
                    }
                    else {
                          //test response with no data for the "session" attribute 
                          if (!noSessionAttrErrorFlag)
                              out.writeHashtable(hResp);
                    }
                    //test response with no completeReply tag
                    if (!noCompleteReplyTagFlag)
                        out.completeReply();

                    os.flush();
                    os.close();
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    Toolkit.printHessian(is);
                    return os.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
          }
        }
        return new byte[0];
	}

	protected boolean sendAsynchronousRequests(ServerRequest[] batchRequests) {
		// TODO Auto-generated method stub
		return false;
	}

	public void notifyOfNewItemInQueue() {
		// TODO Auto-generated method stub
		
	}

}
