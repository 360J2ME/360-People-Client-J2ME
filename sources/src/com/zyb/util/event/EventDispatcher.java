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

import com.zyb.nowplus.business.event.EventDispatcherTask;

/**
 * Dispatches events to listeners
 * @author Andre Schmidt
 */
public abstract class EventDispatcher extends EventHandler 
{
	/**
	 * Cancels an event if possible.
	 */
	public abstract void cancelEvent(byte context, int id, Object data) throws InterruptedException;
	
	/**
	 * Schedules an event to be fired after the given delay.
	 */
	public abstract EventDispatcherTask scheduleEvents(byte context, int id, long delay);
	
	/**
	 * Schedules events to be fired repeatedly, starting after the given delay. 
	 */
	public abstract EventDispatcherTask scheduleEvents(byte context, int id, long delay, long period);
	
	/**
	 * Attaches a listener to this dispatcher
	 * @param listener the listener
	 */
	public abstract void attach(EventListener listener);
	
	/**
	 * Detaches a listener from this dispatcher
	 * @param listener the listener
	 */
	public abstract void detach(EventListener listener);
	
	/**
	 * Waits for the dispatcher to finish.
	 */
	public abstract void waitForFinish();
}

