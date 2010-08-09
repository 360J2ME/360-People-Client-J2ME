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
package com.zyb.nowplus.presentation.view.items;

import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.ImageRef;
import com.zyb.nowplus.business.domain.Profile;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Style;

public abstract class ActivityItem extends Container {
	
	Activity activity;
	
	public ActivityItem(Activity activity, boolean focusFirstElement){
		this(activity, focusFirstElement, null);
	}
	
	public ActivityItem(Activity activity, boolean focusFirstElement, Style style) {
		super(focusFirstElement, style);
		clear();
		this.activity = activity;
		layoutItem();
		setActivity(activity);
	}

	protected abstract void layoutItem();
	
	protected abstract void updateItem(String contactName, ImageRef contactAvatar, String text, String networkId, Activity activity);
	
	public void setActivity(Activity activity)
	{
		this.activity = activity;
		
		Profile[] profiles = activity.getInvolvedContacts();
		
		String name = "";
		ImageRef avatar = null;
		String text = "";
		String networkId = null;
		
		Profile[] involvedContacts = activity.getInvolvedContacts();

		if (involvedContacts != null
			&& involvedContacts.length > 0
			&& involvedContacts[0] != null) {
			name = involvedContacts[0].getUserVisibleName();
		}
		
		text = activity.getDescription();
		
		if(	profiles != null && 
			profiles.length > 0 &&
			profiles[0] != null)
		{
			Profile profile = profiles[0];
			
			avatar = profile.getProfileImage();
			
			ExternalNetwork network = activity.getSource();
			networkId = network.getNetworkId();
			networkId = ExternalNetwork.getStandardId(networkId);
		}
		
		updateItem(name, avatar, text, networkId, activity);
	}
	
	public Activity getActivity()
	{
		return this.activity;
	}
	
	public Object getProfile()
	{
		Profile[] involved = this.activity.getInvolvedContacts();
		
		if(involved != null && involved.length > 0)
		{
			return involved[0];
		}
		
		return null;
	}	
}
