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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;

import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;

public class TcpResponseReader extends Thread {
	private static final short DELIMITER = (short) 0xFFFF;
	
	private DataInputStream dis;
	private boolean isConnectionRunning;
	private ResponseDecoder respDecoder;
	private TcpRPGConnection tcpConn;
	
	public TcpResponseReader(DataInputStream dis, ResponseDecoder respDecoder, RequestQueue requestQueue, 
											TcpRPGConnection tcpConn) {
		isConnectionRunning = true;
		this.dis = dis;
		this.tcpConn = tcpConn;
		
		this.respDecoder = respDecoder;
	}
	
	public void run() {
		while (isConnectionRunning) {
			try {
				readResponses();
				
				if (tcpConn.isNetworkDown()) {
					tcpConn.setNetworkState(true);
				}
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
			} catch (Exception e2) {}
		}
	}
	
	/**
	 * 
	 * Reads the responses from the TCP input stream and
	 * adds them to the queue.
	 * 
	 * @throws Exception Thrown if reading from the input stream
	 * fails.
	 * 
	 */
	protected void readResponses() throws Exception {		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		boolean didErrorOccur = false;
		int offset = 0;
		
		
		short delim = dis.readShort();
		if (delim != DELIMITER) {
			//#debug error
			System.out.println("Error occured reading delimter!");
			return;
		}
		byte msgType = dis.readByte();
		long other = dis.readLong();
		int payloadSize = dis.readInt();
		byte compression = dis.readByte();
		dos.writeShort(delim);
		dos.writeByte(msgType);
		dos.writeLong(other);
		dos.writeInt(payloadSize);
		dos.writeByte(compression);
					
		// read the payload
		while ((offset < payloadSize) &&
				(!didErrorOccur)) {
			try {
				dos.write(dis.read());				
			} catch (Exception e) {}
			offset++;
		}
		
		dos.flush();
		byte[] response = baos.toByteArray();
		dos.close();
		dos = null;
		baos = null;
		
		if ((null != response) && (response.length >= 16) &&
			(!didErrorOccur)) {
			//#debug info
			System.out.println("Added TCP response with length " + response.length + " to decoder.");
			//#debug info
			Toolkit.printHessian(new ByteArrayInputStream(response));
			respDecoder.addResponseForDecoding(response);
		}
	}
	
	/**
	 * 
	 * Stops the connection.
	 * 
	 */
	public void stopConnection() {
		try {
			dis.close();
		} catch (Exception e) {}
		dis = null;
		
		isConnectionRunning = false;
	}
}
