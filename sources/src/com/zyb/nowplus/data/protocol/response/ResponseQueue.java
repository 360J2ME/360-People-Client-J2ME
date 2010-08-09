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
package com.zyb.nowplus.data.protocol.response;

import java.util.Vector;

/**
 * A queue where responses from the backend are stored and worked off by
 * the ResponseDecoder-class.
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public class ResponseQueue {
	private long queueSize;
	private Vector store;
	private ResponseDecoder decoder;
	
	
	public ResponseQueue(ResponseDecoder decoder) {
		store = new Vector(8, 4);
		queueSize = 0;
		this.decoder = decoder;
	}
	
	/**
	 * Adds a response to the queue and returns true if the operation was successful.
	 * 
	 * 
	 * @param responseData The response from the server in bytes.
	 * 
	 * @return True if the responseData was added successfully, false otherwise.
	 */
	public boolean addResponse(byte[] responseData) {
		if (null == responseData) {
			return false;
		}
		
		queueSize += responseData.length;		
		store.addElement(responseData);

		decoder.notifyOfResponse();
		
		return true;
	}
	
	
	
	
	/**
	 * Returns the next available response in bytes.
	 * 
	 * @return The next available response
	 */
	public byte[] getNextResponse() {
		byte[] data = null;
		
		if (store.size() > 0) {
			try {
				data = (byte[]) store.elementAt(0);
				store.removeElementAt(0);
				
				if (null != data) {
					queueSize -= data.length;
				}
			} catch(Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
		}
		
		return data;
	}
	
	
	
	
	/**
	 * Gets the current size of the response stack in bytes.
	 * 
	 * @return A long resembling the size in bytes of all responses.
	 */
	public long getSize() {
		return queueSize;
	}
	
	
	
	/**
	 * Returns number of elements in queue.
	 * 
	 * @return The number of elements in the queue.
	 */
	public int getNumberOfElementsInQueue() {
		if (null != store) {
			return store.size();
		}
		
		return -1;
	}


    /**
     * Used only for unit testing
     */
    public static void reset() {
    }
}
