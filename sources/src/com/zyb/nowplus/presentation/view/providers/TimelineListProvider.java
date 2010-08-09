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
//#condition !polish.remove_status_tab
package com.zyb.nowplus.presentation.view.providers;

import com.zyb.nowplus.business.domain.Activity;

import com.zyb.nowplus.presentation.view.items.ActivityItem;
import com.zyb.nowplus.presentation.view.items.TimelineActivityItem;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;

public class TimelineListProvider extends ActivityListProvider {
	
	public TimelineListProvider(Container container, Screen screen,
			ItemCommandListener commandListener, int requestSize,
			Command cmdRequest) {
		super(container, screen, commandListener, requestSize, cmdRequest);
	}

	public ActivityItem createActivityItem(Activity activity) {
		return new TimelineActivityItem(activity);
	}
	
	//update the activity with correspondent activityItem in UI 
	public synchronized void updateTimeLineActivity(Object data)
	{

		if (!(data instanceof Activity))
			throw new NullPointerException("invalid method paramter input.");

		Activity activity = (Activity) data;

		//this.buffer is ArrayList cached the listed objects of activityItem
		Object[] items = this.buffer.getInternalArray();

		ActivityItem activityItem;

		if (items != null)
			for (int i = 0; i < items.length; i++)
			{
				if (items[i] instanceof TimelineActivityItem)
				{
					activityItem = ((TimelineActivityItem) items[i]);

					if(activityItem.getActivity().equals(activity))
					{
						updateItem(activityItem, activity);
						
						return;
					}
				}
				else
					continue;
			}
	}
}
