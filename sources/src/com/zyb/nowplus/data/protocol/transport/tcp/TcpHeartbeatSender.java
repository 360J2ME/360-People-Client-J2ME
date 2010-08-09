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
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;

import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;

public class TcpHeartbeatSender extends Thread {
	private static final long HEARTBEAT_INTERVAL = 28 * 60 * 1000;	// every 28 minutes
	
	private boolean isConnectionRunning;
	private DataOutputStream dos;
	private TcpRPGConnection tcpConn;
	
	public TcpHeartbeatSender(DataOutputStream dos, TcpRPGConnection tcpConn) {
		this.dos = dos;
		this.tcpConn = tcpConn;
		
		isConnectionRunning = true;
	}

	public void run() {
		while (isConnectionRunning) {
			try {
				sendHeartbeat();
				
				if (tcpConn.isNetworkDown()) {
					tcpConn.setNetworkState(true);
				}
				Thread.sleep(HEARTBEAT_INTERVAL - 100);
			} catch (SecurityException se) {
	        	//#debug error
				System.out.println("Error: Calling user disallowed connection!");
				tcpConn.notifyOfError(TcpRPGConnection.SECURITY_EXCEPTION);
	        } catch (EOFException eof) {
	        	//#debug error
				System.out.println("Error: End of file exception");
				tcpConn.notifyOfError(TcpRPGConnection.END_OF_FILE);
	        } catch (ConnectionNotFoundException cnf) {
	        	//#debug error
				System.out.println("Error: Connection not found!");
				tcpConn.notifyOfError(TcpRPGConnection.CONN_NOT_FOUND);
	        } catch (IOException ioe) {
	        	//#debug error
				System.out.println("Error: Connection not found!");
				tcpConn.notifyOfError(TcpRPGConnection.INPUT_OUTPUT);
	        } catch (Exception e) {
	            //#debug error
	            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Error sending heartbeat <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	            //#debug error
	            e.printStackTrace();
	        }
			
			try {
				Thread.sleep(100);	// safety sleep
			} catch (Exception e) {}
		}
	}
	
	
	/**
	 * 
	 * Sends a heartbeat to the server to keep the connection alive
	 * and updates the timestamp to know when to send the next 
	 * heartbeat if no requests were sent in the meantime.
	 * 
	 * @throws Exception Thrown if the request could not be sent.
	 * 
	 */
	public void sendHeartbeat() throws Exception {
		// send heartbeat if we have not done anything for a longer time
		//#debug info
		System.out.println("Sending heartbeat...");
		
        Hashtable ht = new Hashtable();
        ht.put("mode", "active");
        ht.put("batchsize", new Integer(20480));
		
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        baos.write(ServerRequest.EMPTY_RPG_HEADER);
        
        ServerRequest.getRequestPayload(ht, "", false, null, null, RPGConnection.userID, baos);
        
        byte[] fullPoll = baos.toByteArray();
        ServerRequest.getRPGHeader(ServerRequest.RPG_TCP_HEARTBEAT, fullPoll);
        
		dos.write(fullPoll);
		dos.flush();
	}
	

	/**
	 * 
	 * Stops the conneciton.
	 * 
	 */
	public void stopConnection() {
		try {
			dos.close();
		} catch (Exception e) {}
		dos = null;
		
		isConnectionRunning = false;
	}
}
