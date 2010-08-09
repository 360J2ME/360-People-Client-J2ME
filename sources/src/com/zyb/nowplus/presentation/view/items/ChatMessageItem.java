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
package com.zyb.nowplus.presentation.view.items;

import java.util.Date;

import javax.microedition.lcdui.Image;

import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.util.TextUtilities;

//#if polish.blackberry
//# import com.zyb.util.DateHelper;
//#endif

import de.enough.polish.ui.Container;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * Simple item for displaying a chat message.
 * <p>
 * A message can be of two types, either from one self or from another contact.
 * Graphics will change automatically based on type.
 * <p>
 * Newly received messages can be marked as new using setJustRecieved()
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ChatMessageItem extends Container
{
	/**
	 * the different message types
	 */
	public static final byte IDENTITY_SELF = 1<<0;
	public static final byte IDENTITY_OTHER = 1<<1;
	
	private Message msg;
	
	private Container nameWrap;
	private StringItem name, dateOrTime;
	private IconItem arrow, networkIcon;
	private StringItem content;
	
	private int type;
	
	private boolean justRecieved = false;
	
	/**
	 * 
	 * @param networkId 
	 * @param label
	 * @param txt
	 * @param type
	 */
	public ChatMessageItem(String name, Message msg, String networkId) 
	{
		super(false);
		
		constructContent(name, msg, networkId);
	}
	
	private void constructContent(String name, Message msg, String networkId)
	{
		this.msg = msg;
		
		this.type = (msg.isFromMe()?IDENTITY_SELF:IDENTITY_OTHER);
		
		/*
		 * construct new item according to type
		 */
		try 
		{
			this.clear();
			
			if(this.type == IDENTITY_SELF)
			{
				//#style chat_message_item_self_wrap_container
				this.setStyle();
				
				//inset fake item, to fill first cell
				this.add(new StringItem(null,null));
				
				//#style chat_message_item_self_namedate_wrap_container
				this.nameWrap = new Container(false);

				//#style chat_message_item_name
				this.networkIcon = UiFactory.createNetworkIcon(networkId, false);
				if(null != this.networkIcon)
				{
					this.networkIcon.setAppearanceMode(Item.PLAIN);
					nameWrap.add(this.networkIcon);
				}

				//#style chat_message_item_name
				this.name = new StringItem(null,name);
				this.name.setAppearanceMode(Item.INTERACTIVE);
				nameWrap.add(this.name);
				
				//#style chat_message_item_date
				this.dateOrTime = new StringItem(null," "+getTimeOrDate(this.msg.getTime()));
				nameWrap.add(this.dateOrTime);
				
				this.add(nameWrap);
				
				//#style chat_message_item_self_arrow
				this.arrow = new IconItem((String)null,(String)null,(Image)null);
				this.arrow.setAppearanceMode(Item.PLAIN);
				this.add(arrow);
				
				//#style chat_message_item_self_content
				content = new StringItem(null,msg.getText());
				this.add(content);
			}
			else
			if(this.type == IDENTITY_OTHER)
			{
				//#style chat_message_item_other_wrap_container
				this.setStyle();
				
				//#style chat_message_item_other_namedate_wrap_container
				this.nameWrap = new Container(false);

				//#style chat_message_item_name
				this.networkIcon = UiFactory.createNetworkIcon(networkId, false);
				if(null != this.networkIcon)
				{
					this.networkIcon.setAppearanceMode(Item.PLAIN);
					nameWrap.add(this.networkIcon);
				}

				//#style chat_message_item_name
				this.name = new StringItem(null,name);
				nameWrap.add(this.name);
				
				//#style chat_message_item_date
				this.dateOrTime = new StringItem(null," "+getTimeOrDate(this.msg.getTime()));
				nameWrap.add(this.dateOrTime);
				
				this.add(nameWrap);
				
				//inset fake item, to fill second cell
				this.add(new StringItem(null,null));
				
				//#style chat_message_item_other_content
				content = new StringItem(null,msg.getText());
				this.add(content);
				
				//#style chat_message_item_other_arrow
				this.arrow = new IconItem((String)null,(String)null,(Image)null);
				this.add(arrow);
			}
		}
		catch (Exception e) 
		{
			//#debug error
			System.out.println("Error finding style for chat message construction: "+e.getMessage());
		}
	}
	
	/**
	 * Sets 'just received' style of content StringItem label
	 * 
	 * @param isNew
	 */
	public void setJustRecieved(boolean isNew)
	{
		if(justRecieved != isNew)
		{
			try 
			{
				if(justRecieved = isNew)
				{
					//#style chat_message_item_name_new
					name.setStyle();
				}
				else
				{
					//#style chat_message_item_name
					name.setStyle();
				}
			}
			catch (Exception e) 
			{
				//#debug error
				System.out.println("Error finding style for 'new message': "+e.getMessage());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#defocus(de.enough.polish.ui.Style)
	 */
	public void defocus(Style originalStyle)
	{
		//reset 'new' state
		if(justRecieved)
			setJustRecieved(false);
		super.defocus(originalStyle);
	}
	
	/**
	 * return message reference of this Item
	 */
	public Message getMessage() 
	{
		return msg;
	}
	
	/**
	 * return message reference of this Item
	 */
	public long getTimeStamp() 
	{
		return msg.getTime();
	}
	
	/**
	 * Updates content, label and type of this chat item
	 */
	public boolean updateMessage(String name, Message msg, String networkId) 
	{
		if(null != name && null != msg)
		{
			constructContent(name, msg, networkId);
			return true;
		}
		
		return false;
	}
	
	private String getTimeOrDate(final long time)
	{
		long timeNow = System.currentTimeMillis();
		
		int aDay = 100 * 60 * 60 * 24;
		int aWeek = aDay * 7;
		
		if( time / aDay == timeNow / aDay )
		{
			//is today
			//#if polish.blackberry
			//#= return DateHelper.getTime(time);
			//#else
			return TextUtilities.getTime(time);
			//#endif
		}
		else
		if( time / aWeek == timeNow / aWeek )
		{
			//is same week
			//#if polish.blackberry
			//#= return TextUtilities.getDayShortName( 0, TextUtilities.getDay(time) ) + " " + DateHelper.getTime(time);
			//#else
			return TextUtilities.getDayShortName( 0, TextUtilities.getDay(time) ) + " " + TextUtilities.getTime(time);
			//#endif
			
		}	
		else
		{
			//older than one week, just use date
			DateField df = new DateField(null, DateField.DATE);
			df.setDate(new Date(time));
			return df.getText();
		}
	}
}
