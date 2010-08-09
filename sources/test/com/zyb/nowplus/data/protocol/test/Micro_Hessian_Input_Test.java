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

import java.io.InputStream;

import com.sonyericsson.junit.framework.TestCase;
import com.zyb.nowplus.data.protocol.hessian.End;
import com.zyb.nowplus.data.protocol.hessian.MicroHessianInput;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Update;
import com.zyb.nowplus.test.Assert;

public class Micro_Hessian_Input_Test extends TestCase implements ResponseListener {
	ServiceObject[] servObjs;
	
	public void setUp()
	{	
		servObjs = null;
	}
	
	public void testStart()
	{	
		InputStream is = null;
		try {
			is = this.getClass().getResourceAsStream("max.hessian1");
		} catch (Exception e) {
			Assert.fail("Could not get max.hessian1");
		}
		
		if (null != is) {
			MicroHessianInput mhi = new MicroHessianInput(is, null, this,
															0, false,
															ServerRequest.GET,
															ServerRequest.CONTACTS);
			
			Assert.assertNotNull(mhi);
			
			if (null != mhi) {
				try {
					mhi.startReply();
					while (!(mhi.readObject() instanceof End)) {}
					
					Assert.assertNotNull(servObjs);
					
					Assert.assertEquals(servObjs.length, 62);
				} catch (Exception e) {
					Assert.fail("Parsing max.hessian1 failed.");
				}
			}
		}
		
		try {
			is.close();
		} catch (Exception e) {
		}
	
		
		
		
		try {
			is = this.getClass().getResourceAsStream("susi.hessian1");
		} catch (Exception e) {
			Assert.fail("Could not get max.hessian1");
		}
		
		if (null != is) {
			MicroHessianInput mhi = new MicroHessianInput(is, null, this,
															0, false,
															ServerRequest.GET,
															ServerRequest.CONTACTS);
			
			Assert.assertNotNull(mhi);
			
			if (null != mhi) {
				try {
					mhi.startReply();
					while (!(mhi.readObject() instanceof End)) {}
					
					Assert.assertNotNull(servObjs);
					
					Assert.assertEquals(servObjs.length, 201);
				} catch (Exception e) {
					Assert.fail("Parsing max.hessian1 failed.");
				}
			}
		}
		
		try {
			is.close();
		} catch (Exception e) {
		}
		
		
		try {
			is = this.getClass().getResourceAsStream("testuser.hessian1");
		} catch (Exception e) {
			Assert.fail("Could not get max.hessian1");
		}
		
		if (null != is) {
			MicroHessianInput mhi = new MicroHessianInput(is, null, this,
															0, false,
															ServerRequest.GET,
															ServerRequest.CONTACTS);
			
			Assert.assertNotNull(mhi);
			
			if (null != mhi) {
				try {
					mhi.startReply();
					while (!(mhi.readObject() instanceof End)) {}
					
					Assert.assertNotNull(servObjs);
					
					Assert.assertEquals(servObjs.length, 48);
				} catch (Exception e) {
					Assert.fail("Parsing max.hessian1 failed.");
				}
			}
		}
		
		try {
			is.close();
		} catch (Exception e) {
		}
	}
	
	public void tearDown()
	{

	}

	public void errorReceived(int requestID, byte errorCode) {
		// TODO Auto-generated method stub
		
	}

	public void itemsReceived(int requestID, ServiceObject[] serviceObjects,
			byte type) {
		servObjs = serviceObjects;
	}

	public void itemsReceived(int requestID, byte[] data, byte itemType) {
		// TODO Auto-generated method stub
		
	}

	public void instantMessageReceived(String conversationID, String fromUserID,
			String[] toUserIDs, String message) {
	}

	public void presenceChangeReceived(int requestID, Presence presence) {
		// TODO Auto-generated method stub
		
	}

	public void networkErrorReceived(byte errorCode) {
		// TODO Auto-generated method stub
		
	}

	public void pushReceived(APIEvent apiEvt) {
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
