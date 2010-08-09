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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import com.zyb.nowplus.data.protocol.hessian.MicroHessianInput;

/**
 *
 * <p>The PushEngine class derives from the Singleton-pattern meaning that one instance
 * that is created is used by all objects using the PushEngine.</p>
 * 
 * <p>The single instance of the PushEngine is retrieved by calling the 
 * {@link #getInstance() getInstance()}-method.</p>
 * 
 * <p>{@link #stopInstance() stopInstance()} deallocates memory and resources used by the PushEngine and 
 * should be called by the object that instantiated the PushEngine.</p>
 * 
 * <p>It is recommended to put the startInstance()- in the startApp()-method and the stopInstance()- in
 * the destroyApp()-method of the MIDlet.</p>
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 * 
 */
public final class PushEngine implements MessageListener {
	private static final String KEY_R = "r",
								KEY_E = "e";
	
	private SmsWorkerThread smsWorkerThread;
	private MessageConnection connection;
	private CommunicationManagerImpl commsManager;
	
	/**
	 * 
	 * Constructor. Used for singleton pattern.
	 * 
	 * @param commsManager The communication manager to 
	 * call back to.
	 * 
	 */
	public PushEngine(CommunicationManagerImpl commsManager) {
		this.commsManager = commsManager;
	}
		
	/**
	 * 
	 * Starts listening on a specific SMS port.
	 * 
	 * @param smsPort The port to listen on for SMS.
	 * 
	 */
	public void startListening(int smsPort) {		
		try {
			//#debug info
			System.out.println("SMS listening on port " + smsPort);
			
			connection = (MessageConnection) Connector.open("sms://:" + smsPort);
			connection.setMessageListener(this);
		} catch (Exception e) {
			//#debug error
			System.out.println("Could not open message connection." +e.toString());
		}
	}
		
	/**
	 * 
	 * Stops the message listener.
	 * 
	 */
	public void stopListening() {
		if (null != connection) {
			try {
				connection.setMessageListener(null);
				connection.close();
				
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not stop message connection." +e.toString());
			}
		//#if polish.blackberry
			finally
			{
				connection=null;
			}
		//#endif
		}
	}
	
	/**
	 * 
	 * Called when a message comes in.
	 * 
	 */
	public void notifyIncomingMessage(MessageConnection msgConn) {
		if (connection == msgConn) {
			smsWorkerThread = null;
			
			smsWorkerThread = new SmsWorkerThread();
			smsWorkerThread.start();
		}
	}
	
	
	
	
	
	/**
	 * 
	 * Works off an SMS and calls back the CommunicationManager's
	 * activation code received.
	 * 
	 * @author Rudy Norff (rudy.norff@vodafone.com)
	 *
	 */
	public class SmsWorkerThread extends Thread {
		
		/**
		 * 
		 * Decodes the payload of the activation SMS that comes in. The payload
		 * consists of a Map containing another Map (for the key "e") with the 
		 * payload for the key "r".
		 * 
		 * @paramm payload The payload that was received via SMS.
		 * 
		 * @return The payload containing the activation code as a String.
		 * 
		 */
		private String decodePayload(byte[] payload) {
			InputStream is = new ByteArrayInputStream(payload);
			MicroHessianInput mhi = new MicroHessianInput(is, commsManager, null, -1, true,
										((byte) 0),((byte) 0));
			
			String activationCode = null;
			try {
				Hashtable htOuter = mhi.readHash();
			
				if (null != htOuter) {
					//#debug info
					System.out.println("SMS: " + htOuter.toString());
					
					if (htOuter.containsKey(KEY_E)) {
						Hashtable htInner = (Hashtable) htOuter.get(KEY_E);
						
						if (null != htInner) {
							if (htInner.containsKey(KEY_R)) {
								activationCode = (String) htInner.get(KEY_R);
							}
						}
					}
				}
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not get activation code.");
			}
			
			return activationCode;
		}
		
		/**
		 * 
		 * Called when a message came in. Decodes the message and calls back
		 * the communication manager.
		 * 
		 */
		public void run() {
			try {
				try {
					Message message = connection.receive();
					byte[] payload = ((BinaryMessage)message).getPayloadData();
					
					//#debug info
					System.out.println("ACTIVATION CODE IS: " + payload);
					
					if (null != payload) {
						String actCode = decodePayload(payload);
					
						if (null != actCode) {
							commsManager.activationCodeReceived(actCode);
						}
					}
					
					payload = null; message = null;
				} catch (Exception e) {
					//#debug error
					System.out.println("Receiving SMS failed " + e);
				}
			} catch(Throwable t) {
				//#debug error
				System.out.println("Caught an error:"+t.getMessage());
			}
		}
	}
}
