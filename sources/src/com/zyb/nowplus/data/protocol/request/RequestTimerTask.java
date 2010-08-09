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

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.response.ResponseListener;

import java.util.TimerTask;

public class RequestTimerTask extends TimerTask
{
	private int requestID;
	private ResponseListener responseListener;
	private CommunicationManagerImpl commsMgr;
	private boolean isCasualRequest;
	
	public RequestTimerTask(int requestID, ResponseListener responseListener)
	{
		this.requestID = requestID;
		this.responseListener = responseListener;
		this.isCasualRequest = true;
	}
	
	public RequestTimerTask(CommunicationManagerImpl commsMgr)
	{
		this.commsMgr = commsMgr;
		this.isCasualRequest = false;
	}
	
	public void run()
	{
		// normal request with timeout
		if (isCasualRequest) {
			// remove request from queue
			RequestQueue reqQueue = RequestQueue.getInstance();
			
			// if we still have the request as the response decoder has not
			// decoded it, we timed out and clean it up for the response
			// decoder :)
			if (reqQueue.hasRequest(requestID)) {
				reqQueue.removeRequest(requestID);
				
				if (null != responseListener) {
					//#debug info
					System.out.println("TimerTask killing request " + requestID);

					responseListener.errorReceived(requestID, ResponseListener.REQUEST_TIMED_OUT);
				}
			}
		}
		else { // registration request waiting for act. SMS
			if (null != commsMgr) {
				//#debug info
				System.out.println("TimerTask killing registraton.");
				
				commsMgr.registrationFailed(AuthenticationListener.ACTIVATION_TIMED_OUT);
			}
		}
	}
}
