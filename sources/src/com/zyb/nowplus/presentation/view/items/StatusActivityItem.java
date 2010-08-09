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
import com.zyb.nowplus.business.domain.ImageRef;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.util.DateHelper;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;


public class StatusActivityItem extends ActivityItem{

	Container contentContainer;
	
	CachedImageItem avatarItem;
	
	Container innerContainer;
	
	StringItem nameItem;
	
	StringItem timeItem;
	
	StringItem textItem;
	
	IconItem serviceItem;
	
	public StatusActivityItem(Activity activity) 
	{
		//#style activity
		super(activity, false);
	}
	
	protected void layoutItem()
	{
		//#style activity_content
		this.contentContainer = new Container(false);
		
		//#style activity_status_avatar
		this.avatarItem = new CachedImageItem();
		
		// create inner
		
		//#style activity_info
		this.innerContainer = new Container(false);
		
		//#style activity_name
		this.nameItem = new StringItem(null,null);
		//#style activity_status_time
		this.timeItem = new StringItem(null,null);
		//#style activity_status_text
		this.textItem = new StringItem(null,null);
		
		this.innerContainer.add(this.nameItem);
		this.innerContainer.add(this.timeItem);
		this.innerContainer.add(this.textItem);
		
		// create service
		this.serviceItem = new IconItem(null,null);
		this.serviceItem.setAppearanceMode(Item.PLAIN);
		
		// layout avatar, inner, service 
		
		this.contentContainer.add(this.avatarItem);
		this.contentContainer.add(this.innerContainer);
		this.contentContainer.add(this.serviceItem);
		
		add(this.contentContainer);
	}

	protected void updateItem(String contactName, ImageRef contactAvatar,
			String text, String networkId, Activity activity) {
		this.avatarItem.setImageRef(contactAvatar);
		
		this.nameItem.setText(contactName);
		
		long updateTime=activity.getTime();
		
		String time = DateHelper.getActivitySection(updateTime, false); 
		time  += ", " + DateHelper.getTime(updateTime);
		
		this.timeItem.setText(time);
		
		this.textItem.setText(text);
		
		//#style activity_status_service
		Style serviceStyle = UiFactory.createNetworkStyle(networkId, false);
		this.serviceItem.setStyle(serviceStyle);
	}
}
