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


import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.types.ServiceObject;

/**
 * Resembles a simple queue which allows the ConnectionManager to
 * push requests (ServerRequest objects) on it. The queue basically serves 
 * the ConnectionRequests on a first in first out basis, but adds a minor tweak 
 * to it.
 * 
 * The application logic can give three priorities to a 
 * {@link com.zyb.nowplus.data.protocol.request.ServerRequest ServerRequest}. A ServerRequest of high 
 * priority is always served before a medium or low priority one. The same goes
 * for a medium prioritized one over a weak prioritized ServerRequest.
 * 
 * The RequestQueue uses a singleton-pattern as it is always needed.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 * 
 */
public class RequestQueue {
	private static final int MAX_BATCHED_REQUESTS = 15;
	
	private static RequestQueue queue;
	private Vector store;
	private int idCounter;
	
	private RPGConnection connection;
	
	
	
	protected RequestQueue() {
		store = new Vector(8, 4);
		idCounter = 0;
	}
	
	
	
	
	/**
	 * Gets a singleton instance of the RequestQueue.
	 * 
	 * @return A singleton instance of the RequestQueue.
	 */
	public static synchronized RequestQueue getInstance() {
		if(null == queue) {
			queue = new RequestQueue();
		}
		
		return queue;
	}
	
	
	
	
	/**
	 * 
	 * Adds a new ServerRequest object to the queue.
	 * 
	 * @param priority The priority of the request. The values are defined 
	 * in @link{ServerRequest ServerRequest}.
	 * @param verb The action to take place. E.g. GET or DELETE. 
	 * The values are defined in @link{ServerRequest ServerRequest}.
	 * @param noun The object the action is to be taken on. E.g. ACTIVITY or 
	 * CONTACT. The values are defined in @link{ServerRequest ServerRequest}.
	 * @param items If a SET action is carried out objects need to be passed to be set. Can
	 * be null if the action is GET.
	 * @param filters If objects are to be deleted or retrieved additional filters can
	 * be applied. For the possible keys and values of filters see the online documentation
	 * of the @link{http://developer.next-vodafone.com/docs API}.
	 * @param isFireAndForgetRequest Tells the Request that it is a fire and forget request
	 * which does not expect any response.
	 * 
	 * @return An int resembling the unique id of the request or -1 if there was an error.
	 */
	public synchronized int addRequest(byte priority, byte verb, byte noun, 
										ServiceObject[] items, Hashtable filters,
										boolean isFireAndForgetRequest) {		
		idCounter++;	// increment the unique identifier
		store.addElement(new ServerRequest(idCounter, priority, verb, 
												noun, items, filters,
												isFireAndForgetRequest));
		
		if (null != connection) {
			connection.notifyOfNewItemInQueue();
		}
		
		return idCounter;
	}
	
	/**
	 * Adds multiple new ServerRequest objects to the queue.
	 * 
	 * @param priorities The priorities of the requests. The values are defined 
	 * in @link{ServerRequest ServerRequest}.
	 * @param verbs The actions to take place. E.g. GET or DELETE. 
	 * The values are defined in @link{ServerRequest ServerRequest}.
	 * @param nouns The objects the actions are to be taken on. E.g. ACTIVITY or 
	 * CONTACT. The values are defined in @link{ServerRequest ServerRequest}.
	 * @param items If a SET action is carried out objects need to be passed to be set. Can
	 * be null if the action is GET.
	 * @param filters If objects are to be deleted or retrieved additional filters can
	 * be applied. For the possible keys and values of filters see the online documentation
	 * of the @link{http://developer.next-vodafone.com/docs API}.
	 * @param isFireAndForgetRequest Tells the requests that it is a fire and forget request
	 * which does not expect any response.
	 * 
	 * @return An int-array resembling the unique ids of the requests or -1 if 
	 * there was an error. If the priorities length was not the same as the one
	 * of verbs and nouns the result will be an int-array with length 1 containing
	 * a -1.
	 * 
	 */
	public synchronized int[] addRequests(byte[] priorities, byte[] verbs, byte[] nouns, 
										ServiceObject[][] items, Hashtable[] filters,
										boolean isFireAndForgetRequest) {		
		if ((null == priorities) || (null == verbs) || (null == nouns) ||
				(priorities.length != verbs.length) || (verbs.length != nouns.length)) { 
			return new int[] { -1 };
		}
		
		int[] ids = new int[priorities.length];
		
		for (int i = 0; i < priorities.length; i++) {
			ServiceObject[] itemArray = null;
			if ((null != items) && (items.length > i)) {}
					
					
			Hashtable filter = null;
			
			store.addElement(new ServerRequest(++idCounter, priorities[i], verbs[i], 
												nouns[i], itemArray, filter, 
												isFireAndForgetRequest));
			ids[i] = idCounter;
		}
		
		if (null != connection) {
			connection.notifyOfNewItemInQueue();
		}
		
		return ids;
	}
	
	/**
	 * Adds a new ServerRequest object to the queue specifically for thumbnail requests.
	 * 
	 * @param priority The priority of the request. The values are defined 
	 * in @link{ServerRequest ServerRequest}.
	 * @param url The URL the thumbnail should be loaded from.
	 * 
	 * @return An int resembling the unique id of the request or -1 if there was an error.
	 */
	public synchronized int addRequest(byte priority, String url) {		
		idCounter++;	// increment the unique identifier
		
		store.addElement(new ServerRequest(idCounter, priority, url,
											ServerRequest.GET, 
											ServerRequest.IMAGES,
											null, new Hashtable()));
		
		if (null != connection) {
			connection.notifyOfNewItemInQueue();
		}
		
		return idCounter;
	}	
	
	/**
	 * Gets the next ServerRequest-items on the queue. By next the FIFO principle
	 * is meant, but with the tweak of returning higher prioritized items first. Up
	 * to MAX_BATCHED_REQUEST items are retrieved for doing batch requests.
	 * 
	 * @return The next ServerRequest-items on the queue or null if all 
	 * ServerRequest-items have been worked off.
	 */
	public synchronized ServerRequest[] getNextRequests()
	{
		ServerRequest tempRequest = null;
		ServerRequest[] requests = null;
		
		int highPriority = 0;
		int mediumPriority = 0;
		int lowPriority = 0;
		int inactive = 0;

		for (int i=0; i<store.size(); i++) {
			if (null != (tempRequest = (ServerRequest) store.elementAt(i))) {
				if (!tempRequest.isActive()) {
					switch (tempRequest.getPriority()) {
						case ServerRequest.HIGH_PRIORITY:
							highPriority++;
							break;

						case ServerRequest.MEDIUM_PRIORITY:
							mediumPriority++;
							break;

						case ServerRequest.LOW_PRIORITY:
							lowPriority++;
							break;
					}
					
					inactive++;
				}
			}
		}
		
		if (0 != inactive) {
			if (inactive > MAX_BATCHED_REQUESTS) {
				requests = new ServerRequest[MAX_BATCHED_REQUESTS];
			}
			else {
				requests = new ServerRequest[inactive];
			}
			
			// add requests ordered by priority to queue
			int counterAdded = 0;
			counterAdded = populateRequests(counterAdded, requests, ServerRequest.HIGH_PRIORITY);
			counterAdded = populateRequests(counterAdded, requests, ServerRequest.MEDIUM_PRIORITY);
			populateRequests(counterAdded, requests, ServerRequest.LOW_PRIORITY);
	
			//#debug info
			System.out.println("Returning " + requests.length + " batch requests for sending!");
		}
			
		return requests;
	}
	
	/**
	 * 
	 * Adds requests from the queue to the array which is then used to
	 * send out the requests in a batch.
	 * 
	 * @param counterAdded The offset in the requests array to start with.
	 * @param requests The requests-array to be used to fire off the request
	 * in a batch. The requests found based on the priority will be added
	 * to this array.
	 * @param priority The priority to look for. Either HIGH_PRIORITY,
	 * LOW_PRIORITY or MEDIUM_PRIORITY (found in ServerRequest).
	 * 
	 * @return The new offset in the requests array after adding the 
	 * prioritized items.
	 * 
	 */
	private int populateRequests(int counterAdded, 
					ServerRequest[] requests, final byte priority) {
		if ((null == requests) || (counterAdded > requests.length) ||
				(counterAdded < 0)) {
			return -1;
		}
		
		ServerRequest tempRequest = null;
		
		// add all items for the passed priority
		for (int i = 0; i < store.size(); i++) {
			if (counterAdded >= requests.length) {
				break;
			}
			
			if (null != (tempRequest = (ServerRequest) store.elementAt(i))) {
				if ((!tempRequest.isActive()) && 
					(tempRequest.getPriority() == priority)) {
					tempRequest.setActive(true);
					requests[counterAdded] = tempRequest;
					counterAdded++;
				}	
			}
		}
		
		return counterAdded;
	}
	
	
	
	
	/**
	 * Gets a ServerRequest from the Stack and returns it. The Request is not
	 * removed from the stack.
	 * 
	 * @param id The unique id of the ServerRequest.
	 * 
	 * @return The server request or null if it could not be found.
	 * 
	 */
	public synchronized ServerRequest getRequest(int id) {
		ServerRequest tempRequest = null;
		
		for(int i=0; i<store.size(); i++) {
			try {
				tempRequest = (ServerRequest) store.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast to ServerRequest.");
			}
				
			if( (null != tempRequest) && (tempRequest.getRequestID() == id) ) {
				return tempRequest;
			}

			tempRequest = null;
		}
		
		return null;	// could not be found.
	}
	
	
	/**
	 * Returns a boolean indicating whether there is an inactive request in the queue.
	 * 
	 * @return True if there is a free request available at the moment, false
	 * otherwise.
	 */
	public synchronized boolean hasNextRequest() {
		ServerRequest tempRequest = null;
		boolean hasNextRequest = false;
		
		// go through queue and find an item based on highest available priority
		for (int i=0; i<store.size(); i++) {
			try {
				tempRequest = (ServerRequest) store.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast to ServerRequest.");
			}
			
			// request should not be null and not active
			if ((null != tempRequest) && (!tempRequest.isActive()) ) {
				hasNextRequest = true;
				
				//#debug info
				System.out.println("Has next request " + tempRequest.getRequestID() + "!!! " + tempRequest.getMessageType());
				break;
			}
		}
		
		return hasNextRequest;
	}
	
	
	
	/**
	 * Returns a boolean indicating whether there is an inactive request in the queue.
	 * 
	 * @param requestID The requestID to look for.
	 * 
	 * @return True if the request was found, false otherwise.
	 */
	public synchronized boolean hasRequest(int requestID) {
		ServerRequest tempRequest = null;
		boolean hasRequest = false;
		
		// go through queue and find an item based on highest available priority
		for (int i=0; i<store.size(); i++) {
			try {
				tempRequest = (ServerRequest) store.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast to ServerRequest.");
			}
			
			// request should not be null and not active
			if ((null != tempRequest) && (tempRequest.getRequestID() == requestID) ) {
				hasRequest = true;
				break;
			}
		}
		
		return hasRequest;
	}
	
	/**
	 * Returns the message type for a request which is found via
	 * its request ID.
	 * 
	 * @param id The ID of the request.
	 * 
	 * @return The message type of the @link{ServerRequest ServerRequest}
	 * or the default message
	 * request if it could not be found.
	 * 
	 */
	public byte getMessageTypeForRequest(int id) {
		ServerRequest tempRequest = null;
		
		for(int i=0; i<store.size(); i++) {			
			try {
				tempRequest = (ServerRequest) store.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast to ServerRequest.");
			}
			
			//#debug info
			System.out.println("Searched: " + id + " Found: " + tempRequest.getRequestID());
			
			if( (null != tempRequest) && (tempRequest.getRequestID() == id) ) {
				return tempRequest.getMessageType();
			}
		}
		
		return ServerRequest.UNKNOWN;
	}
	
	
	
	
	
	/**
	 * Removes a ServerRequest from the Stack. E.g. after the 
	 * RPGConnection has successfully worked off a ServerRequest,
	 * it must remove the request from the queue by calling this method!
	 * 
	 * @param id The unique id of the ServerRequest.
	 * 
	 * @return True if connection request was removed, false otherwise.
	 */
	public synchronized boolean removeRequest(int id)
	{
		if (id == -1) {
			return false;
		}

		ServerRequest tempRequest = null;
		boolean wasRemoved = false;
		
		for(int i=0; i<store.size(); i++) {
			try {
				tempRequest = (ServerRequest) store.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast to ServerRequest.");
			}
			
			if( (null != tempRequest) && (tempRequest.getRequestID() == id) ) {
				store.removeElementAt(i);
				wasRemoved = true;
				break;
			}

			tempRequest = null;
		}
		
		//#debug info
		System.out.println("Removed ID: " + id + " from request queue. Size is now " + store.size() + " " + store.toString());
		
		return wasRemoved;
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
        queue = null;
    }
    
	/**
	 * 
	 * Registers a connection that is called back when a request
	 * is added.
	 * 
	 * @param connection The connection to call back when a request 
	 * is added to the queue.
	 * 
	 */
	public synchronized void registerRequestListener(RPGConnection connection) {
		this.connection = connection;
	}
}
