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
package com.zyb.util.test;

import com.zyb.nowplus.business.event.test.MockEventDispatcher;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.SafeRunnable;
import com.zyb.util.SafeThread;
import com.zyb.util.event.Event;

public class SafeThreadTest extends TestCase
{
	private static final int EVENT_INIT = 1;
	private static final int EVENT_WORK = 2;
	private static final int EVENT_CLEAN_UP = 3;
	private static final int EVENT_RELEASE_MEMORY = 4;
	
	private MockEventDispatcher dispatcher;
	private SafeThread thread;
	
	public void setUp()
	{
		dispatcher = new MockEventDispatcher(5);
	}

	public void testStartAndStop()
	{
		// create a test runnable
		SafeRunnable runnable = new SafeRunnable()
		{
			private int step;
			
			public void init() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_INIT);
			}
			
			public void work() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_WORK);
				
				step++;
				switch (step)
				{				
					case 1 :
					thread.stop();
					break;
				}
			}
			
			public void cleanUp() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_CLEAN_UP);
			}

			public void releaseMemory() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_RELEASE_MEMORY);
			}				
		};
		
		// run the runnable in a safe thread
		thread = new SafeThread(runnable);
		thread.start("test");
		try
		{
			thread.join();
		} 
		catch (InterruptedException e)
		{
		}
		
		// check the events that occurred
		Event[] expected = 
		{
				new Event(Event.Context.TEST, EVENT_INIT, null),
				new Event(Event.Context.TEST, EVENT_WORK, null),
				new Event(Event.Context.TEST, EVENT_CLEAN_UP, null)
		};
		Assert.assertEquals(expected, dispatcher.getEvents());
	}
	
	// if this test fails, check that the heap size = 2MB
	public void testOOMInWork()
	{
		// create a test runnable
		SafeRunnable runnable = new SafeRunnable()
		{
			private int step;
			private byte[] memory; // used to cause an OutOfMemoryError
			
			public void init() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_INIT);
			}
			
			public void work() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_WORK);
				
				step++;
				switch (step)
				{
					case 1 :
					memory = new byte[2 * 1024 * 1024];
					break;
					
					case 2 :
					thread.stop();
					break;
				}
			}
			
			public void cleanUp() 
			{
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_CLEAN_UP);
			}
	
			public void releaseMemory() 
			{
				memory = null;
				System.gc();
				
				dispatcher.notifyEvent(Event.Context.TEST, EVENT_RELEASE_MEMORY);
			}				
		};
	
		// run the runnable in a safe thread
		thread = new SafeThread(runnable);
		thread.start("test");
		try
		{
			thread.join();
		} 
		catch (InterruptedException e)
		{
		}
	
		// check the events that occurred
		Event[] expected = 
		{
				new Event(Event.Context.TEST, EVENT_INIT, null),
				new Event(Event.Context.TEST, EVENT_WORK, null),
				new Event(Event.Context.TEST, EVENT_RELEASE_MEMORY, null),
				new Event(Event.Context.TEST, EVENT_WORK, null),		
				new Event(Event.Context.TEST, EVENT_CLEAN_UP, null)
		};
		Assert.assertEquals(expected, dispatcher.getEvents());	
	}
	
	public void tearDown()
	{
		dispatcher = null;
	}
}
