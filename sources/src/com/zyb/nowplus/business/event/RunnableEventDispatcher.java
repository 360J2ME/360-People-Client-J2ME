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
package com.zyb.nowplus.business.event;

import java.util.Enumeration;
import java.util.Timer;
import java.util.Vector;

import com.zyb.util.Queue;
import com.zyb.util.SafeRunnable;
import com.zyb.util.SafeThread;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

/**
 * Dispatches events on its own thread.
 */
public class RunnableEventDispatcher extends EventDispatcher implements SafeRunnable
{
	private SafeThread thread;
	
	private final Queue eventQueue;
	
	private final Vector listeners;
	
	private final Timer timer;
	
	/**
	 * Constructs an event dispatcher.
	 */
	public RunnableEventDispatcher()
	{
		//#debug debug
		System.out.println("Constructing event dispatcher.");
		
		thread = new SafeThread(this);
		
		eventQueue = new Queue();
		
		listeners = new Vector();
		
		timer = new Timer();		
	}
	
	public void init()
	{
		//#debug debug
		System.out.println("Starting event dispatcher thread.");
	}
	
	public void work()
	{
		try 
		{
			Event event = (Event) eventQueue.pop();
		
			if (event == null) 
			{
				// if a queue() and cancel() squeeze between the wait() and notify(), the pop() may return null
			}
			else
			{
				//#debug debug
				System.out.println("Dispatch " + event);
				
				if (event.getContext() == Event.Context.MODEL)
				{
						Enumeration elements = listeners.elements();
						while(elements.hasMoreElements())
						{
							EventListener listener = (EventListener) elements.nextElement();
							try 
							{
								if (listener.getContext() == Event.Context.MODEL)
								{
									listener.handleEvent(event.getContext(), event.getId(), event.getData());
								}
							}
							catch (Exception e)
							{
								//#debug error
								System.out.println("Exception in " + listener + " while handling " + event + "." + e);
							}
						}
				}
				else
				{				
					Enumeration elements = listeners.elements();
					while(elements.hasMoreElements())
					{
						EventListener listener = (EventListener) elements.nextElement();
						{
							try
							{
								listener.handleEvent(event.getContext(), event.getId(), event.getData());
							}
							catch (Exception e)
							{
								//#debug error
								System.out.println("Exception in " + listener + " while handling " + event + "." + e);
							}
						}	
					}
					
					if (event.getContext() == Event.Context.APP && event.getId() == Event.App.STOP)
					{
						thread.stop();
					}
				}
			}
		}
		catch (InterruptedException e)
		{
			thread.stop();
		}
	}
			
	public void cleanUp()
	{
	}
	
	public void releaseMemory()
	{
		// TODO one of the listeners caused an oom, have to free some memory
	}
	
	public void notifyEvent(byte context, int id, Object data) 
	{
		if (context == Event.Context.APP &&
			id == Event.App.START)
		{			
			//#debug info
			System.out.println("Starting model dispatcher thread");
			
			thread.start("model");
		}
		
		Event event = new Event(context, id, data);
		
		//#debug debug
		System.out.println("Queue " + event);
		
		eventQueue.push(event);
	}
	
	public void cancelEvent(byte context, int id, Object data) throws InterruptedException
	{
		Event event = new Event(context, id, data);
		
		//#debug debug
		System.out.println("Cancel " + event);
		
		eventQueue.cancel(event);		
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
	
	public void attach(EventListener listener) 
	{
		if (!listeners.contains(listener))
		{
			this.listeners.addElement(listener);
		}
		//#mdebug debug
		dumpAttachedObjects("after attach");
		//#enddebug
	}

	public void detach(EventListener listener) 
	{
		this.listeners.removeElement(listener);
		//#mdebug debug
		dumpAttachedObjects("after detach");
		//#enddebug
	}
	
	/**
	 * @param string
	 */
	//#mdebug debug
	private void dumpAttachedObjects(String string) {
			//#debug debug
			System.out.println(string+":");
			Object[] list = new Object[this.listeners.size()]; 
			this.listeners.copyInto(list);
			//#debug debug
			System.out.println("List length:"+list.length);
			for(int i=0; i<list.length; i++)
				//#debug debug
				System.out.println(i+":"+list[i]);
	}
	//#enddebug
	
	public void waitForFinish()
	{
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			//#debug error
			System.out.println("Wait for finish interrupted." + e);
		}
	}
	
	//#mdebug error
	public String toString()
	{
		return "RunnableEventDispatcher[queue=" + eventQueue
			+ "]";
	}
	//#enddebug
}
