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

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;

/**
 * Implementation of ListProvider for contacts
 * 
 * @author Andre Schmidt
 * 
 */
public class VirtualContactProvider extends VirtualListProvider {
	
	Model model;
	
	Controller controller;
	
	ListSelection selection;
	
	ItemCommandListener itemCommandListener;
	
	public VirtualContactProvider(Model model, Controller controller, Container container, Screen screen, ItemCommandListener commandListener, int bufferSize) {
		super(container,screen,commandListener,bufferSize);
		this.itemCommandListener = commandListener;
		this.model = model;
		this.controller = controller;
	}
	
	protected ListSelection select(Object data, int range)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;
			
			ListSelection previous = this.selection;
			
			this.selection = this.model.getContacts(contact, range);
			
			// first load the new entries, then unload the old entries, so the contacts
			// that are still in the selection will not be released and read from
			// storage
			if (this.selection != null)
			{
				for (int i = 0; i < this.selection.getEntries().length; i++)
				{
					ContactProfile entry = (ContactProfile) this.selection.getEntries()[i];
					
					entry.load(false);
				}			
			}
			
			if (previous != null)
			{
				for (int i = 0; i < previous.getEntries().length; i++)
				{
					ContactProfile entry = (ContactProfile) previous.getEntries()[i];
					
					entry.unload();
				}
			}
			
			return this.selection;
			
		}
		catch(Exception e)
		{
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#select(com.zyb.nowplus.presentation.view.providers.VirtualListRange)
	 */
	protected ListSelection select(VirtualListRange range)
	{
		try
		{
			return this.model.getContacts(range.getStart(), range.getEnd());
			
		}
		catch(Exception e)
		{
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#total()
	 */
	protected int total()
	{
		try
		{
			return this.model.getContactsSize();
		}
		catch(Exception e)
		{
			System.out.println(this+"thrown exception:"+e.toString());
		}
		return 0;
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.BaseListProvider#createItem(java.lang.Object, de.enough.polish.ui.ItemCommandListener)
	 */
	Item createItem(Object data, ItemCommandListener commandListener)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;
	
			//#style contactlist_contact
			ContactSummarizedItem contactItem = new ContactSummarizedItem(contact, 
					ContactSummarizedItem.NETWORK_ICON_MODE_ALL, 
					ContactSummarizedItem.PRESENCE_MODE_ONLINE_ONLY,true);
			
			contactItem.getAvatar().setRequest(false);
			contactItem.setItemCommandListener(this.itemCommandListener);
			
			//#if polish.blackberry
				contactItem.setDefaultCommand(BasePeopleForm.cmdOpen);
			//#else
				contactItem.addCommand(BasePeopleForm.cmdOpen);
			//#endif
			
			return contactItem;
		}
		catch(Exception e)
		{
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.BaseListProvider#getData(de.enough.polish.ui.Item)
	 */
	Object getData(Item item)
	{
		try
		{
			ContactSummarizedItem contactItem = (ContactSummarizedItem) item;

			return contactItem.getContact();

		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
		
		return null;
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.BaseListProvider#updateItem(de.enough.polish.ui.Item, java.lang.Object)
	 */
	void updateItem(Item item, Object data)
	{
		try
		{
			ContactProfile contact = (ContactProfile) data;

			ContactSummarizedItem contactItem = (ContactSummarizedItem) item;

			contactItem.setContact(contact);

			contactItem.getAvatar().setRequest(false);
		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
	}
	
	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#search(java.lang.String)
	 */
	public void search(String search)
	{
	
		try
		{
			ContactProfile profile = this.model.getFirstContact(search);

			update(profile, true);

		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#filter(com.zyb.nowplus.presentation.view.items.FilterContainer)
	 */
	public void filter(FilterContainer filter)
	{
		try
		{

			this.controller.notifyEvent(Event.Context.CONTACTS,
					Event.Contacts.FILTER, filter);

		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
	}
	
	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#getSampleData()
	 */
	protected Object getSampleData()
	{
		try
		{
			return this.model.createContact();
		}
		catch(Exception e)
		{
			System.out.println(this+"thrown exception:"+e.toString());
		}
		
		return null;
	
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#notify(de.enough.polish.ui.Item, boolean)
	 */
	protected void notify(Item item, boolean active)
	{
		try
		{
			ContactSummarizedItem contactItem = (ContactSummarizedItem) item;

			ImageRef ref = contactItem.getAvatar().getImageRef();

			if (active)
				ref.load();
			else
				ref.cancel();

		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
	}

	/* 
	 * @see com.zyb.nowplus.presentation.view.providers.VirtualListProvider#reset()
	 */
	public void reset()
	{
		try
		{
			this.view.reset();
			update(this.model.getFirstContact(), false);
		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
	}
	
}
