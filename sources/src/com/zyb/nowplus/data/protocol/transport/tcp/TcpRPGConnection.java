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
package com.zyb.nowplus.data.protocol.transport.tcp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;

/**
 *
 * @author Rudy Norff (rudy.norff@vodafone.com)
 * 
 */
public class TcpRPGConnection extends RPGConnection {
	public static final byte CONN_NOT_FOUND = 1, 
							 END_OF_FILE = 2,
							 INPUT_OUTPUT = 2,
							 SECURITY_EXCEPTION = 4;
	
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private RequestQueue requestQueue;
	private TcpHeartbeatSender heartbeatSender;
	private TcpResponseReader responseReader;
	private boolean didCriticalErrorOccur;
	
	public TcpRPGConnection(String name, CommunicationManagerImpl cmMgr, ResponseDecoder responseDecoder, AuthenticationListener authListener, 
							NetworkListener netListener,
							RequestQueue requestQueue,
							ResponseListener respListener) {
		super(name, cmMgr, responseDecoder, authListener, netListener, respListener);
		
		connState = STATE_RUN_CONNECTION;
		
		this.requestQueue = requestQueue;
		requestQueue.registerRequestListener(this);
	}
	
	
	public void run() {
		SocketConnection socketConn = null;
		
		while (connState == STATE_RUN_CONNECTION) {
			didCriticalErrorOccur = false;
			try {
				//#debug info
				System.out.println("(Re-)Initializing socket and opening input/output streams...");
				socketConn = (SocketConnection) Connector.open(RPG_SOCKET_URL);
				dis = socketConn.openDataInputStream();
				dos = socketConn.openDataOutputStream();
				
				if ((null == dis) || (null == dos) || (null == socketConn) || (null == requestQueue)) {
					//#debug error
					System.out.println("Input-, Outputstream or SocketConn was null!");
					return;
				}
				
				// send initial heartbeat
				heartbeatSender = new TcpHeartbeatSender(dos, this);		
				heartbeatSender.start();
				responseReader = new TcpResponseReader(dis, responseDecoder, requestQueue, this);			
				responseReader.start();
				
				//#debug debug
				System.out.println("Starting basic request handling...");
				while (!didCriticalErrorOccur) {			
					try { // response handling
						while (requestQueue.hasNextRequest()) {
							//#debug info
							System.out.println("Has next request.");
							sendAsynchronousRequests(
										requestQueue.getNextRequests());
							if (cmMgr.isNetworkDown()) {
								setNetworkState(true);
							}
						}
						
						if (!didCriticalErrorOccur) {
							synchronized (this) {
								wait();
							}
						}
					} catch (SecurityException se) {
			        	//#debug error
						System.out.println("Error: Calling user disallowed connection!");
						notifyOfError(TcpRPGConnection.SECURITY_EXCEPTION);
			        } catch (EOFException eof) {
			        	//#debug error
						System.out.println("Error: End of file exception");
						notifyOfError(TcpRPGConnection.END_OF_FILE);
			        } catch (ConnectionNotFoundException cnf) {
			        	//#debug error
						System.out.println("Error: Connection not found!");
						notifyOfError(TcpRPGConnection.CONN_NOT_FOUND);
			        } catch (IOException ioe) {
			        	//#debug error
						System.out.println("Error: Connection not found!");
						notifyOfError(TcpRPGConnection.INPUT_OUTPUT);
			        } catch (Exception e) {
			            //#debug error
			            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Error sending heartbeat <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			            //#debug error
			            e.printStackTrace();
			        }
									
					try {
						Thread.sleep(200);
					} catch (Exception e) {}
				}
			} catch (SecurityException se) {
	        	//#debug error
				System.out.println("Error: Calling user disallowed connection!");
				notifyOfError(TcpRPGConnection.SECURITY_EXCEPTION);
	        } catch (EOFException eof) {
	        	//#debug error
				System.out.println("Error: End of file exception");
				notifyOfError(TcpRPGConnection.END_OF_FILE);
	        } catch (ConnectionNotFoundException cnf) {
	        	//#debug error
				System.out.println("Error: Connection not found!");
				notifyOfError(TcpRPGConnection.CONN_NOT_FOUND);
	        } catch (IOException ioe) {
	        	//#debug error
				System.out.println("Error: Connection not found!");
				notifyOfError(TcpRPGConnection.INPUT_OUTPUT);
	        } catch (Exception e) {
	            //#debug error
	            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Error sending heartbeat <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	            //#debug error
	            e.printStackTrace();
	        } finally {
				if (null != heartbeatSender) {
					heartbeatSender.stopConnection();
					heartbeatSender = null;
				}
				if (null != responseReader) {
					responseReader.stopConnection();
					responseReader = null;
				}
				try {
					if (null != socketConn) {
						socketConn.close();
					}
				} catch (Exception e) {}
				socketConn = null;
			}
			
			if (didCriticalErrorOccur) {
				try {
					Thread.sleep(10000);
				} catch (Exception e) {}
			} else {
				try {
					Thread.sleep(100);
				} catch (Exception e) {}
			}
		}
	}
	
	/**
	 * 
	 * Sends out the passes requests in one go to the server.
	 * 
	 * @param batchRequests The requests to send out.
	 * 
	 * @return True if sending succeeded, false otherwise.
	 * 
	 */
	private boolean sendAsynchronousRequests(ServerRequest[] batchRequests) throws Exception {
		if (null == batchRequests) {
			//#debug info
			System.out.println("Requests were null. Not sending requests.");
			return false;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		//#debug debug
		System.out.println("Sending next requests...");

		for (int i = 0; i < batchRequests.length; i++) {
			if (null == batchRequests[i]) {
				continue;
			}
			
			batchRequests[i].writeToRPGDataStructure(dos, true);
			if (batchRequests[i].isFireAndForgetRequest()) {
                //#debug debug
                System.out.println("Removing FAFR: " + batchRequests[i].getRequestID());
                requestQueue.removeRequest(batchRequests[i].getRequestID()); 	
            }
			try {
				baos.flush();
				dos.write(baos.toByteArray());
				dos.flush();
			} catch (IOException ioe) {
				//#debug error
				ioe.printStackTrace();
				
				throw new IOException();
			} finally {
				try {
					baos.close();
				} catch (Exception e2) {}
				baos = null;
			}
		}
			
		return true;
	}	
	
	public void notifyOfNewItemInQueue() {
		//#debug info
		System.out.println("Notified of new item in request queue!");
		
		synchronized (this) {
			notify();
		}
	}
	
	/**
	 * 
	 * Notifies the connection that an error occurred.
	 * 
	 * @param errorCode The error code defined in 
	 * TcpRPGConnection.
	 * 
	 */
	public void notifyOfError(byte errorCode) {
		//#debug info
		System.out.println("Notified of error in TCP socket!");
		
		didCriticalErrorOccur = true;
		
		if (!cmMgr.isNetworkDown()) {
			if (netListener != null) {
				netListener.networkDown();
			}
			setNetworkState(false);
		}
		
		
		
		synchronized (this) {
			notify();
		}
	}
	
	public void setNetworkState(boolean isNetworkUp) {
		cmMgr.setNetworkState(isNetworkUp);
		
		if (isNetworkUp) {
			netListener.networkUp();
		}
	}
	
	public boolean isNetworkDown() {
		return cmMgr.isNetworkDown();
	}
	
	public void stopConnection(boolean finalStop) {
		super.stopConnection(finalStop);
		
		if (null != responseReader) {
			responseReader.stopConnection();
		}
		if (null != heartbeatSender) {
			heartbeatSender.stopConnection();
		}
		
		try {
			Thread.sleep(1000);
		} catch (Exception e) {}
		
		heartbeatSender = null;
		responseReader = null;
	}
}
