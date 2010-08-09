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
package com.zyb.nowplus.business.event.test;

import java.util.Timer;

import com.zyb.nowplus.business.event.EventDispatcherTask;
import com.zyb.nowplus.test.Assert;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class MockEventDispatcher extends EventDispatcher
{
	public static final int ATTACH_LISTENER = 401;
	public static final int DETACH_LISTENER = 402;

	private Event[] events;
	private int index;

	private final Timer timer;

	public MockEventDispatcher(int numberOfEvents)
	{
		events = new Event[numberOfEvents];
		index = 0;
		
		timer = new Timer();			
	}

	// implementation of EventDispatcher
	
	public void attach(EventListener listener)
	{
		notifyEvent(Event.Context.TEST, ATTACH_LISTENER, null);
	}
	
	public Event getLatestEvent(byte context)
	{
		return null;
	}
	
	public void detach(EventListener listener)
	{
		notifyEvent(Event.Context.TEST, DETACH_LISTENER, null);
	}
	
	public void notifyEvent(byte context, int id, Object data) 
	{
		if (context != Event.Context.MODEL)
		{
			if (index < events.length)
			{
				events[index++] = new Event(context, id, data);
			}
			else
			{
				Assert.fail(this + " received more than " + events.length + " events");
			}
		}
	}
	
	public void cancelEvent(byte context, int id, Object data)
	{
		if (context != Event.Context.MODEL)
		{
			if (index < events.length)
			{
				events[index++] = null;
			}
			else
			{
				Assert.fail(this + " received more than " + events.length + " events");
			}
		}
	}
	
	public EventDispatcherTask scheduleEvents(byte context, int id, long delay)
	{
		EventDispatcherTask task = new EventDispatcherTask(this, context, id);
		timer.schedule(task, delay);
		return task;
	}
	
	public EventDispatcherTask scheduleEvents(byte context, int id, long delay, long period)
	{
		EventDispatcherTask task = new EventDispatcherTask(this, context, id);
		timer.schedule(task, delay, period);
		return task;
	}	
	
	public void waitForFinish()
	{
	}
	
	// for testing
	
	public Event[] getEvents()
	{
		return Event.trimArray(events, index);
	}
	
	public void resetEvents()
	{
		index = 0;
	}
	
	public String toString()
	{
		return "MockEventDispatcher["
			+ "]";
	}	
}
