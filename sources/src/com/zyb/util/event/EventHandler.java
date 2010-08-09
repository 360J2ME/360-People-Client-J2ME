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
package com.zyb.util.event;

/**
 * An abstract class providing methods 
 * to handle or dispatch events in an implemented way
 * @author Andre Schmidt
 */
public abstract class EventHandler 
{
	/**
	 * Notifies this EventHandler
	 * @param context the context
	 * @param id the event identifier
	 */
	public void notifyEvent(byte context, int id) 
	{
		notifyEvent(context,id,null);
	}
	
	/**
	 * Handles or dispatches events
	 * @param context the context
	 * @param id the event identifier
	 * @param data the data
	 */
	public abstract void notifyEvent(byte context, int id, Object data);
	
	/**
	 * Notifies this EventHandler
	 * @param event the event
	 */
	public void notify(Event event) 
	{
		notifyEvent(event.getContext(),event.getId(),event.getData());
	}
}
