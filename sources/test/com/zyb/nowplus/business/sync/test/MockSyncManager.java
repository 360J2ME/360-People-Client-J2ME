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
package com.zyb.nowplus.business.sync.test;

import com.zyb.nowplus.business.event.test.MockEventDispatcher;
import com.zyb.nowplus.business.sync.Sync;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;

public class MockSyncManager extends MockEventDispatcher implements Sync
{
	public static final int SYNC = 501;
	public static final int CANCEL_SYNC = 502;
	
	private EventDispatcher dispatcher;
	
	public MockSyncManager(EventDispatcher dispatcher, int numberOfEvents)
	{
		super(numberOfEvents);
		
		this.dispatcher = dispatcher;
	}

	public void start()
	{
		
	}
	
	public void stop()
	{
		
	}
	
	public void sync(int syncMode, int resolution) 
	{
		notifyEvent(Event.Context.TEST, SYNC, syncMode + ":" + resolution);
	}
	
	public void cancelSync()
	{
		notifyEvent(Event.Context.TEST, CANCEL_SYNC, null);
	}

    public boolean remap(Object contact)
    {
    	return false;
    }
    
    public boolean isInteractingWithSource()
    {
    	return false;
    }
    
	public String toString()
	{
		return "MockSyncManager["
			+ "]";
	}

    // for testing
	public void mockSyncFinished() throws Exception
	{
		dispatcher.notifyEvent(Event.Context.SYNC, Event.Sync.SUCCESSFULL);
		
		// give the model some time
		Thread.sleep(100);
	}
}
