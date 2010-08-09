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
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;

import com.zyb.nowplus.presentation.view.items.ActivityItem;

import com.zyb.util.DateHelper;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;

public abstract class ActivityListProvider extends ExtendedListProvider{
	
	StringItem activityRequestItem;
	
	int received = 0;
	
	public ActivityListProvider(Container container, Screen screen, ItemCommandListener commandListener, int requestSize, Command cmdRequest) {
		super(container, screen, commandListener);
		
		String requestSizeText = "" + requestSize;
		String buttonText = Locale.get("nowplus.client.java.statuspage.request",requestSizeText);
		
		//#style activitylist_request_button
		this.activityRequestItem = new StringItem(null,buttonText);
		
		//#if polish.blackberry
			this.activityRequestItem.setDefaultCommand(cmdRequest);
		//#else
			this.activityRequestItem.addCommand(cmdRequest);
		//#endif
			
		this.activityRequestItem.setItemCommandListener(commandListener);
	}
	
	Item createItem(Object data, ItemCommandListener commandListener) {
		
		if(!(data instanceof Activity))
			throw new NullPointerException("invalid method paramter input.");
		
		Activity activity = (Activity)data;
		
		ActivityItem activityItem =null;
		
		if(activity != null){
			activityItem = createActivityItem(activity);
		}
		
		setActivityCommand(activityItem);
		activityItem.setItemCommandListener(commandListener);
		return activityItem;
	}
	
	public abstract ActivityItem createActivityItem(Activity activity);

	void updateItem(Item item, Object data) {
		
		if(!(data instanceof Activity))
			throw new NullPointerException("invalid method paramter input.");
		
		ActivityItem activityItem = (ActivityItem)item;
		Activity activity = (Activity)data;
		activityItem.setActivity(activity);
		setActivityCommand(activityItem);
	}
	
	public synchronized void apply(Object collection, Object[] list, int count, Object scope, boolean doFocus, boolean request) {
		
		if(request) {
			scope = list[this.received];
		}
		
		super.apply(collection, list, count, scope, doFocus);
		
		this.received = list.length;
	}
	
	protected void setActivityCommand(ActivityItem activityItem) {
		//fix for bug 0006292 && 7126
		Object profile = activityItem.getProfile();
		if(null != profile && (profile instanceof ManagedProfile && !((ManagedProfile)profile).isDeleted()) )
		{
			//#if polish.blackberry
				activityItem.setDefaultCommand(BasePeopleForm.cmdOpen);
			//#else
				activityItem.addCommand(BasePeopleForm.cmdOpen);
			//#endif
		} else {
			//#if polish.blackberry
				activityItem.setDefaultCommand(null);
			//#else
				activityItem.removeCommand(BasePeopleForm.cmdOpen);
			//#endif
		}
	}
		
	Object getData(Item item) {
		if(item instanceof ActivityItem) {
			ActivityItem activityItem = (ActivityItem)item;
			return activityItem.getActivity();
		} else {
			return null;
		}
	}

	protected Item getHeader(Object collection) {
		return null;
	}

	protected String getSection(Object data) {
		String section = null;

		//#if !polish.blackberry
		Activity activity = (Activity)data;
		section = DateHelper.getActivitySection(activity.getTime(), true);
		//#endif
		
		return section;
	}

	protected Item getTail(Object collection) {
		ListSelection selection = (ListSelection)collection;
		if(selection.getEntries().length < selection.getTotal()) {
			return this.activityRequestItem;
		} else {
			return null;
		}
	}

}
