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
//#condition activate.embedded.360email

package com.zyb.nowplus.presentation.view.items;

import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.business.domain.ProfileSummary;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * Simple representation of email showing name and email subject only
 *  
 * @author Anders
 */
public class EmailListItem extends Container
{
	private Email emailSource;
	
	private IconItem mailIcon;
	private static final int MAILICON_INDEX = 0;
	
	private Container innerRight;
	
	private StringItem fromsItm;
	private StringItem subjectItm;
	
	/**
	 * 
	 * @param name
	 * @param subject
	 * @param contact
	 */
	public EmailListItem(Email email) 
	{
		//#style emailtab_email_item_container
		super(false);
	
		if(null != email)
		{
			this.setAppearanceMode(Item.INTERACTIVE);
			
			this.emailSource = email;
			
//			this.mailIcon = getMailIcon(this.emailSource.getReadState());
			this.mailIcon = getMailIcon(false);
			this.add(this.mailIcon);
			
			//#style emailtab_email_item_inner_right_container
			this.innerRight = new Container(false);
			this.add(this.innerRight);
			
			//TODO: Add TKs
			
			//#style emailtab_email_item_name
			this.fromsItm = new StringItem(null, getFromsString(email.getFroms()));
			this.innerRight.add(this.fromsItm);
			
			//#style emailtab_email_item_subject
			this.subjectItm = new StringItem(null, email.getSubject());
			this.innerRight.add(this.subjectItm);
		}
	}
	
	/**
	 * Update contents of email item
	 * 
	 * @param name
	 * @param subject
	 * @param id
	 */
	public void setEmailContents(Email email) 
	{
		if(null != email)
		{
//			setBeenRead(email.markedRead());
			
			String newFroms = getFromsString(email.getFroms());
			if(null != this.fromsItm && !this.fromsItm.getText().equalsIgnoreCase(newFroms))
				this.fromsItm.setText(newFroms);
			
			if(null != this.subjectItm && !this.subjectItm.getText().equalsIgnoreCase(email.getSubject()))
				this.subjectItm.setText(email.getSubject());
		}
	}

	private IconItem getMailIcon(boolean beenRead)
	{
		final int ICONIMAGE_KEY = 6;
		final int FOCUSSTYLE_KEY = 1;
		
		Style focused = new Style(
				null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
				null, null, 
				null, null 
		);
		Style unfocused = new Style(
				null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
				null, null, 
				null, null 
		);
		
		if(beenRead)
		{
			focused.addAttribute(ICONIMAGE_KEY, "/emailtab_emailicon_opened_28x26_focused.png" );
			unfocused.addAttribute(ICONIMAGE_KEY, "/emailtab_emailicon_opened_28x26_unfocused.png" );
			unfocused.addAttribute(FOCUSSTYLE_KEY, focused);
		}
		else
		{
			focused.addAttribute(ICONIMAGE_KEY, "/emailtab_emailicon_unopened_28x26_focused.png" );
			unfocused.addAttribute(ICONIMAGE_KEY, "/emailtab_emailicon_unopened_28x26_unfocused.png" );
			unfocused.addAttribute(FOCUSSTYLE_KEY, focused);
		}
		
		return new IconItem(null, null, unfocused);
	}
	
	public void setBeenRead(boolean beenRead)
	{
//		if(this.emailSource.getReadState() != beenRead)
//		{
//			this.beenRead = beenRead;
			this.set(MAILICON_INDEX, getMailIcon(beenRead));
//		}
	}

	public Email getEmailSource()
	{
		return this.emailSource;
	}
	
	private String getFromsString(ProfileSummary[] froms)
	{
		if(null != froms)
		{
			StringBuffer sb = new StringBuffer();
			
			for(int i = 0; i < froms.length; ++i)
			{
				if(i>0)
					sb.append(";");
				sb.append(froms[i].getFullName());
			}
			return sb.toString();
		}
		else
			return null;
	}
}
