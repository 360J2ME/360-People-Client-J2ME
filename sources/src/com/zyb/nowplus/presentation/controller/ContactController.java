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
package com.zyb.nowplus.presentation.controller;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.InvalidValueException;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;
import com.zyb.nowplus.presentation.view.forms.BaseTabForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.PeopleTabContacts;
//#if activate.embedded.360email
import com.zyb.nowplus.presentation.view.forms.PeopleTabEmail;
//#endif
import com.zyb.nowplus.presentation.view.forms.PeopleTabMe;
//#if !polish.remove_status_tab
import com.zyb.nowplus.presentation.view.forms.PeopleTabStatus;
//#endif
//#if activate.timeline.tab
import com.zyb.nowplus.presentation.view.forms.PeopleTabTimeline;
//#endif
import com.zyb.nowplus.presentation.view.items.FilterContainer;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.util.Locale;

/**
 * The contact controller
 * @author Andre Schmidt
 *
 */
public class ContactController extends ContextController
{
	PeopleTabContacts tabContacts;
	//#if !polish.remove_status_tab
	PeopleTabStatus tabStatus;
	//#endif
	PeopleTabMe tabMe;
	//#if activate.timeline.tab
	PeopleTabTimeline tabTimeline;
	//#endif
	//#if activate.embedded.360email
	PeopleTabEmail tabEmail;
	//#endif
	
	private NotificationForm refreshingContactsListNotification;
	private NotificationForm notreadyNotification;

	public ContactController(Model model, Controller controller,
			ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.CONTACTS;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) {

		
		//#debug debug
		System.out.println(context+"/"+event);
		
		if(context == getContext())
		{
			switch (event) {
//				case Event.Contacts.OPEN:
//					showPeopleTab();
//					break;
				case Event.Contacts.OPEN_FILTER:
					this.tabContacts.requestFocusFilter();
					break;
					
				case Event.Contacts.FILTER:
					if(null != data && data instanceof FilterContainer)
					{
						FilterContainer filter = (FilterContainer)data;
						// TODO: only show Model.getContactFilters()
						
						//notify model of filter change
						getModel().setContactsFilter(filter.getSelectedFilter());
					}
					break;
					
				case Event.Contacts.REFRESH_LIST:
					if(refreshingContactsListNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS,refreshingContactsListNotification);
						refreshingContactsListNotification = null;
					}
					break;
					
				case Event.Contacts.REFRESHING_LIST:
					if(refreshingContactsListNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS,refreshingContactsListNotification);
						refreshingContactsListNotification = null;
					}
				
					Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);

					//#style notification_form_base
					refreshingContactsListNotification = new NotificationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.peoplepage.notification.pleasewait"),
							null,
							ok,
							0, true, ProgressIndicatorItem.PROGRESS_INFINITE);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT,refreshingContactsListNotification); //NOT NEXT_GLOBAL
					break;
					
				//#if use-connect-invite == true
				case Event.Contacts.INVITE:
					
					if(null != data && data instanceof ContactProfile)
					{
						try
						{
							getModel().invite((ContactProfile)data);
						}
						catch (InvalidValueException e)
						{
						}
					}
					showInviteAcknowledgementNotification();
					
					break;
					
				case Event.Contacts.CONNECT:
					
					if(null != data && data instanceof ContactProfile)
					{
						try
						{
							getModel().connect((ContactProfile)data);
						}
						catch (InvalidValueException e)
						{
						}
					}
					showConnectAcknowledgementNotification();
					break;
				//#endif
					
				case Event.Contacts.DELETE_CONTACT:
					
					if(null != data && data instanceof ContactProfile)
					{
						getModel().delete((ContactProfile)data);
						
						//dismiss confirmation
						getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_TO_LATEST_PEOPLESPAGE,null);
					}
					
					break;
			}
		}
		else
		if(context == Event.Context.APP)
		{
			switch (event) 
			{
				case Event.App.START:
					
					//#debug debug
					System.out.println("Display loading progress bar");
					
					//#style notification_form_progress_startup
					notreadyNotification = new NotificationForm(
							model, controller, 
							Locale.get("nowplus.client.java.application.popup.title"),
							null,
							null,
							0, false, ProgressIndicatorItem.PROGRESS_INFINITE
							);
					
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, notreadyNotification); //NOT NEXT_GLOBAL
					
					break;
	
				case Event.App.READY:
					
					//remove loading progress bar
					if(notreadyNotification != null)
					{
						//#debug debug
						System.out.println("Hide loading progress bar");
						
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, notreadyNotification);
						notreadyNotification = null;
					}
	
					//launch UI
					showPeopleTab();
					
					//#debug debug
					System.out.println("Start up finished.");
					break;
					
				case Event.App.SERVICE_UNAVAILABLE:
				case Event.App.CONNECTION_DOWN:
					
					if(notreadyNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, notreadyNotification);
						notreadyNotification = null;
					}
					
					break;
			}
		}
	}
	
	private void showPeopleTab()
	{
		boolean isFirstInit = false;
		
		if(null == BaseTabForm.getTab(Event.Context.CONTACTS, BasePeopleForm.PEOPLEPAGE_CONTACTS_TAB_PRIORITY))
		{
			//#style contactlist
			this.tabContacts = new PeopleTabContacts(getModel(),getController());
			
			//#if activate.timeline.tab
			//#style base_form
			this.tabTimeline = new PeopleTabTimeline(getModel(),getController());
			//#endif
			
			//#if !polish.remove_status_tab
			//#style base_form
			this.tabStatus = new PeopleTabStatus(getModel(),getController());
			//#endif
			
			//#style base_form
			this.tabMe = new PeopleTabMe(getModel(),getController());
			
			//#if activate.embedded.360email
			//#style base_form
			this.tabEmail = new PeopleTabEmail(getModel(),getController());
			//#endif
			
				
			isFirstInit = true; 
		}
		
		//explicit calls to showNotify to avoid 'flickring' when starting up, bug 4448
		if(isFirstInit)
		{
			//#if !polish.remove_status_tab
			if(null != this.tabStatus)
			{
				this.tabStatus.showNotify();
				this.tabStatus.hideNotify(); //make sure to deregister listener after foced construct
			}
			//#endif
			
			if(null != this.tabMe)
			{
				this.tabMe.showNotify();
				this.tabMe.hideNotify(); //make sure to deregister listener after foced construct
			}
			//#if activate.timeline.tab
			if(null != this.tabTimeline)
			{
				this.tabTimeline.showNotify();
				this.tabTimeline.hideNotify(); //make sure to deregister listener after foced construct
			}
			//#endif
			//#if activate.embedded.360email
			if(null != this.tabEmail)
			{
				this.tabEmail.showNotify();
				this.tabEmail.hideNotify(); //make sure to deregister listener after foced construct
			}
			//#endif
			if(null != this.tabContacts)
				this.tabContacts.showNotify();
		}
		
		//getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, BaseTabForm.getTab(Event.Context.CONTACTS, BasePeopleForm.PEOPLEPAGE_CONTACTS_TAB_PRIORITY));
		history.next(this.tabContacts, null, true); //direct call, needs to override anything and everything in Controller event cue
		
	}

	public void showInviteAcknowledgementNotification()
	{
		//envoke notification screen 
		Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
		//#style notification_form_base
		NotificationForm nf = new NotificationForm(
				getModel(), getController(), 
				Locale.get("nowplus.client.java.contextual.menu.notify.invite.title"),
				Locale.get("nowplus.client.java.contextual.menu.notify.invite.text"),
				ok,
				3000
				);
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);	
	}
	
	public void showConnectAcknowledgementNotification()
	{
		//envoke notification screen 
		Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
		//#style notification_form_base
		NotificationForm nf = new NotificationForm(
				getModel(), getController(), 
				Locale.get("nowplus.client.java.contextual.menu.notify.connect.title"),
				Locale.get("nowplus.client.java.contextual.menu.notify.connect.text"),
				ok,
				3000
				);
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
	}

	//#mdebug error
	public String toString()
	{
		return "ContactController[]";
	}	
    //#enddebug

}
