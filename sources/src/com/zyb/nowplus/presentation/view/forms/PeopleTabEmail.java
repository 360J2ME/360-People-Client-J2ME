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

package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.EmailListItem;
import com.zyb.nowplus.presentation.view.items.NavBarContainer;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * The 'Email' tab of the PeoplePages
 * 
 * TODO: Add TestKeys to translation system, currently hardcoded to EN language
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class PeopleTabEmail extends BasePeopleForm
{
	private Command cmdRefresh;
	
	private Item refreshItem;
	
	//#if polish.blackberry.isTouchBuild == true
	private Command emailTabCmd;
	//#endif
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public PeopleTabEmail(Model model, Controller controller)
	{
		this(model, controller, null);
	}

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param style
	 */
	public PeopleTabEmail(Model model, Controller controller, Style style)
	{
		super(model, controller, Event.Context.CONTACTS, PEOPLEPAGE_EMAIL_TAB_PRIORITY, style);
		
		//#debug debug
		System.out.println("Constructing Email Tab");
		
		setCommandListener(this);
		
		String iconImageName = null;
		
		//#if polish.blackberry
//			 iconImageName = Locale.get("nowplus.client.java.peoplepage.tab.email");
			iconImageName = "Email";
		//#endif
		
		//Fake usage of style to avoid removal during preprocessing
		//#style peoplepage_navbar_item_email_active_defocused
		this.tabItem = new IconItem(iconImageName, null);
		
		//#style peoplepage_navbar_item_email
		this.tabItem.setStyle();
		
		this.tabItem.setDefaultCommand(cmdFake);	 
		
		//#if polish.blackberry.isTouchBuild == true
		emailTabCmd = new Command("emailTab", Item.BUTTON, 0);
			this.tabItem.setDefaultCommand(emailTabCmd);
			this.tabItem.setItemCommandListener(this);
			super.setCommandListener(this);
		//#endif
		
		setTabItem(this.tabItem);
		
		this.tabTitle = "Email";
		
		setTabTitle(this.tabTitle);
		
		//Load emails at first construct
//		loadEmails();
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#getPriority()
	 */
	public byte getPriority() 
	{
		return PEOPLEPAGE_EMAIL_TAB_PRIORITY;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector() 
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#switchTabItemStyle()
	 */
	protected void switchTabItemStyle()
	{
		//#if polish.blackberry
		
		if( (navBar.getStyle() == navBarUnfocusedStyle || navBar.getStyle() == navBarActiveUnfocusedStyle) )
		{
			if(NavBarContainer.nextFocus != tabIndex)
			{
				//#style .peoplepage_navbar_item_email_active_defocused
				tabItem.setStyle();
			}
			else
			{
				//#style .peoplepage_navbar_item_email_active_defocusedfocused
				tabItem.setStyle();
			}
		}
		else if (navBar.getStyle() == navBarFocusedStyle)
		{
			if(NavBarContainer.nextFocus != tabIndex)
			{
				//#style .peoplepage_navbar_item_email
				tabItem.setStyle();
			}
			else
			{
				//#style .peoplepage_navbar_item_emailfocused
				tabItem.setStyle();
			}
		}
		
		//#else
		
		if( (navBar.getStyle() == navBarUnfocusedStyle || navBar.getStyle() == navBarActiveUnfocusedStyle) )
		{
			if(!tabItem.isFocused)
			{
				//#style .peoplepage_navbar_item_email_active_defocused
				tabItem.setStyle();
			}
			else
			{
				//#style .peoplepage_navbar_item_email_active_defocusedfocused
				tabItem.setStyle();
			}
		}
		else if (navBar.getStyle() == navBarFocusedStyle)
		{
			if(!tabItem.isFocused)
			{
				//#style .peoplepage_navbar_item_email
				tabItem.setStyle();
			}
			else
			{
				//#style .peoplepage_navbar_item_emailfocused
				tabItem.setStyle();
			}
		}
		
		//#endif
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
	 */
	protected Item createBottomItem() 
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createTopItem()
	 */
	protected Item createTopItem() 
	{
		this.cmdRefresh = new Command("Refresh",Command.OK, 0);
		
		//#style emailtab_request_button
		//TODO: Add textkey
		this.refreshItem = new StringItem(null,"Refresh");
		this.refreshItem.setAppearanceMode(Item.INTERACTIVE);
		this.refreshItem.addCommand(cmdRefresh);
		this.refreshItem.setItemCommandListener(this);
		
		return refreshItem;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(c == cmdOptions)
		{
			//TODO: Match email ID sender with contact and launch contextual menu based on outcomde
			
			//launch contextual menu
			if(null != getCurrentItem() && getCurrentItem() instanceof EmailListItem)
			{
				EmailListItem eli = (EmailListItem) this.getCurrentItem();
				
				//use first person in froms list for profile item in Contextual menu
				ProfileSummary first = null;
				if(null != eli.getEmailSource() && null != eli.getEmailSource().getFroms() && eli.getEmailSource().getFroms().length > 0)
				{
					first = eli.getEmailSource().getFroms()[0];
				}
				
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, first);
			}
			else
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, model.getMe());
		}
		else
			super.commandAction(c, d);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item)
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if (c == cmdRefresh)
		{
			getModel().refreshFolder();
		}
		else
		if(c == BasePeopleForm.cmdOpen)
		{
			if(null != item)
			{
				if(null != item && item instanceof EmailListItem)
				{
					EmailListItem eli = (EmailListItem)item;
					eli.setBeenRead(true);
					int id = eli.getEmailSource().getId();
					
					//pass email to controller
					getController().notifyEvent(Event.Context.EMAIL, Event.Email.OPEN_EMAIL, new Integer(id));	
				}
			}
		}
		//#if polish.blackberry.isTouchBuild == true
		else
        if(c == emailTabCmd)
        {
        	//special tab switch handling for BB touch devices
            super.switchTabByIndex(this.tabIndex);    
            return;
        } 
        //#endif
		else
			super.commandAction(c, item);
	}
	
	//#if activate.embedded.360email
	/**
	 * 
	 */
	protected void loadEmails()
	{			
		Email[] emails = model.getAvailableMessages2();
		if (emails == null)
		{
			// TODO: inform user email not available (yet)
		}
		else
		if (emails.length == 0)
		{
			//inform user the inbox is empty
			
			this.container.clear();
			
			//#style .activitylist_noresults
			setStyle();
			
			this.container.add(
					//TODO: Add TK
					//#style activitylist_noresults
					new StringItem(null, "No emails in this inbox at this time. Try again later.")
					);
		}
		else
		{
			for (int i = 0; (i < emails.length) || (i < this.container.size()); i++)
			{
				if (i < this.container.size())
				{
					EmailListItem item = (EmailListItem) this.get(i);
					if (i < emails.length)
					{
						item.setEmailContents(emails[i]);
					}
					else
					{
						this.remove(item);
					}
				}
				else
				{
					Item item = createEmailListItem(emails[i]);
					this.add(item);
				}
			}
			
			//#style .emailtab_form
			setStyle();				
		}
		
		/* TEST EMAIL INJECTION */
		
//		Item email = createEmailListItem("name1", "subject1");
//		this.add(email);
//		
//		email = createEmailListItem("loong nnnnaaaaaammmmmmeee", "long subjecccccccctttttttttt");
//		this.add(email);
//		
//		email = createEmailListItem("name2", "subject2");
//		this.add(email);
//		
//		email = createEmailListItem("name3", "subject3");
//		this.add(email);
//		
//		email = createEmailListItem("name4", "subject4");
//		this.add(email);
//		
//		email = createEmailListItem("name5", "subject5");
//		this.add(email);
//		
//		email = createEmailListItem("name6", "subject6");
//		this.add(email);
//		
//		email = createEmailListItem("name7", "subject7");
//		this.add(email);
		
		/* END TEST EMAIL INJECTION */
	}
	
	/**
	 * 
	 * @param name
	 * @param subject
	 * @return
	 */
	private Item createEmailListItem(final Email email)
	{
		//TODO: pass contact reference
		Item emailitem = new EmailListItem(email);
		emailitem.addCommand(BasePeopleForm.cmdOpen);
		emailitem.setItemCommandListener(this);
		return emailitem;
	}
	//#endif
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if( context == Event.Context.EMAIL )
		{
			switch(event)
			{
				case Event.Email.UPDATE_EMAIL:
					loadEmails();
					break;
			}
		}
        else
            super.handleEvent(context, event, data);
	}

	//#if polish.blackberry
	public boolean handleShowOptions() {
		// TODO Auto-generated method stub
		return false;
	}
	//#endif
}
