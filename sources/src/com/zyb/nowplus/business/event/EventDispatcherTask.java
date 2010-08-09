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

import java.util.TimerTask;

import com.zyb.util.event.EventDispatcher;

public class EventDispatcherTask extends TimerTask
{
	private final EventDispatcher dispatcher;
	private final byte context;
	private final int id;
	private boolean fired;
	
	public EventDispatcherTask(EventDispatcher dispatcher, byte context, int id)
	{
		this.dispatcher = dispatcher;
		this.context = context;
		this.id = id;
	}
	
	public void run()
	{
		fired = true;		
		dispatcher.notifyEvent(context, id);
	}
	
	public boolean hasFired()
	{
		return fired;
	}
}
