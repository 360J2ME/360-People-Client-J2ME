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
//#condition polish.device.supports.nativesms == false
package com.zyb.nowplus.presentation.view.items;

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;

import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * Extension of ContactSummarized item to allow sms in place of status. 
 * 
 * It uses ugly workaround to switch status text to phone number
 * 
 * @see ContactSummarizedItem
 * @author marek.defecinski@mobica.com
 */
public class ContactSummarizedSMSInsteadStatusItem extends ContactSummarizedItem
{
	public ContactSummarizedSMSInsteadStatusItem(Channel choosenChannel, byte netWorkIconMode, byte presenceMode) 
	{
		this(choosenChannel, netWorkIconMode, presenceMode, null);
	}
	
	public ContactSummarizedSMSInsteadStatusItem(Channel choosenChannel, byte netWorkIconMode, byte presenceMode, final Style style) 
	{
		//First set primary sms-number 
		super(choosenChannel.getProfile(), netWorkIconMode, presenceMode , true, style);
		
		//then set choosen sms number if isn't null 
		if (choosenChannel.getName()!=null)
			status.setText(choosenChannel.getName());
	}

	/**
	 * Return dummy icon
	 */
	protected IconItem getNetworkIcon()
	{
		return dummyIcon;
	}
	
	/**
	 * Overriden method to switch status text to phone number.
	 */
	protected StringItem getStatus()
	{
		StringItem result;
		
		String phoneNumber = null;
		if (this.contact==null)
		{
			return dummyString;
		}
		
		this.contact.load(true);
		
		Channel smsChannel = this.contact.getPrimarySMSChannel();
		if(smsChannel != null && smsChannel.getName() != null)
			phoneNumber = smsChannel.getName();
		
		if(null != phoneNumber)
		{
			if(null == this.status)
			{
				this.status = new StringItem(null, phoneNumber, statusStyle);
				this.status.setAppearanceMode(Item.PLAIN);	
			}
			else
				this.status.setText(phoneNumber);
			
			result = this.status;
		}
		else
			result = dummyString;
		
		this.contact.unload();
		
		return result;
	}
	
	/**
	 * Return dummy icon
	 */
	/*protected*/public IconItem getPresenceIcon()
	{
		return dummyIcon;
	}
}	
