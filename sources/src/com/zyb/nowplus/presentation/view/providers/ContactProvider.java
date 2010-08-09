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
package com.zyb.nowplus.presentation.view.providers;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ImageRef;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.FilterContainer;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;

/**
 * Implementation of ListProvider for contacts
 * 
 * @author Andre Schmidt
 * 
 */
public class ContactProvider extends ListProvider {
	
	public ContactProvider(Model model, Controller controller, Container container, BasePeopleForm form, int range, int step,
			int interval) {
		super(model, controller, container, form, range, step, interval);
	}

	protected ListSelection select(Object data, int range)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;
			
			ListSelection ls = getModel().getContacts(contact, this.range);
			
			// first load the new entries, then unload the old entries, so the
			// contacts
			// that are still in the selection will not be released and read from
			// storage
			if (ls != null)
			{
				for (int i = 0; i < ls.getEntries().length; i++)
				{
					ContactProfile entry = (ContactProfile) ls.getEntries()[i];
					
					entry.load(false);
				}
			}
			
			if (selection != null)
			{
				for (int i = 0; i < selection.getEntries().length; i++)
				{
					ContactProfile entry = (ContactProfile) selection.getEntries()[i];
					
					entry.unload();
				}
			}
			
			return ls;
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}

	public void search(String search)
	{
		try
		{
			ContactProfile profile = getModel().getFirstContact(search);
			
			update(profile, true);
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}

	public void filter(FilterContainer filter)
	{
		try
		{
			getController().notifyEvent(Event.Context.CONTACTS,
					Event.Contacts.FILTER, filter);
		
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}

	protected Item createItem(Object data)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;
	
			//#style contactlist_contact
			ContactSummarizedItem contactItem = new ContactSummarizedItem(contact, 
					ContactSummarizedItem.NETWORK_ICON_MODE_ALL, 
					ContactSummarizedItem.PRESENCE_MODE_ONLINE_ONLY,true);
			
			contactItem.getAvatar().setRequest(false);
			
			//#if polish.blackberry
				contactItem.setDefaultCommand(BasePeopleForm.cmdOpen);
			//#else
				contactItem.addCommand(BasePeopleForm.cmdOpen);
			//#endif
				
			return contactItem;
			
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}

	protected void updateItem(Item item, Object data)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;
			ContactSummarizedItem contactItem = (ContactSummarizedItem) item;
	
			contactItem.setContact(contact);
			contactItem.getAvatar().setRequest(false);
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}
	
	public void processItemCommand(Command cmd, Object data)
	{
		try
		{
			if (cmd == BasePeopleForm.cmdOpen)
				getController().notifyEvent(Event.Context.PROFILE,Event.Profile.OPEN, data);
			
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}

	protected Object getSampleData()
	{
		try
		{
			return getModel().createContact();
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}
	
	public void notify(Item item, boolean active)
	{
	  try
	  {
			ContactSummarizedItem contactItem = (ContactSummarizedItem) item;
	
			ImageRef ref = contactItem.getAvatar().getImageRef();
	
			if (active)
			{
				// contactItem.setContact(contactItem.getContact(), true);
				ref.load();
			}
			else
				ref.cancel();
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}
}
