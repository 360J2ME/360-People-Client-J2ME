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
import com.zyb.nowplus.business.domain.orders.FirstLastOrder;
import com.zyb.nowplus.business.domain.orders.LastFirstOrder;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.SettingsForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.Locale;

/**
 * The application controller
 * @author Jens Vesti
 *
 */
public class SettingsController extends ContextController implements CommandListener
{
	private SettingsForm settingsForm;

	private NotificationForm syncNotification;
	
    protected final Command hide = new Command(Locale.get("polish.command.hide"), Command.ITEM, 0);
    
	public SettingsController(Model model, Controller controller, ExtendedScreenHistory history ) {
		super(model, controller, history);
	}

	public void commandAction(Command c, Displayable d) 
	{
		if(hide == c)
		{
			synchronized (history)
			{
				if(syncNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, syncNotification);
					syncNotification = null;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.SETTINGS;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) {
		//#debug debug
		System.out.println("context:"+context+"/event:"+event);
	
		if(context == getContext())
		{
			Command cmdOk = null;
			Command cmdCancel = null;
			Event confirmEvent = null;
			ConfirmationForm cf = null;
			
			switch (event) {
			case Event.Settings.OPEN:
				
				//#style base_form
				settingsForm = new SettingsForm(getModel(), getController());
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, settingsForm);
				break;
				
			case Event.Settings.CLEAR_STORAGE:
				
				//envoke confirmation
				cmdOk = new Command(Locale.get("polish.command.delete"), Command.SCREEN, 0);
				cmdCancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				//event to fire upon confirmation
				confirmEvent = new Event(getContext(),Event.Settings.CLEAR_STORAGE_CONFIRM_WARNING,null);
				//#style notification_form_delete
				cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.settings.clearstorage.warning.title"),
						Locale.get("nowplus.client.java.settings.clearstorage.warning.text"),
						cmdOk, cmdCancel,
						confirmEvent);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				break;
				
			case Event.Settings.CLEAR_STORAGE_CONFIRM_WARNING:
				
				//envoke confirmation
				cmdOk = new Command(Locale.get("nowplus.client.java.settings.clearstorage.confirm.yes"), Command.SCREEN, 0);
				cmdCancel = new Command(Locale.get("nowplus.client.java.settings.clearstorage.confirm.no"), Command.CANCEL, 0);
				//event to fire upon confirmation
				confirmEvent = new Event(Event.Context.APP,Event.App.MARK_FOR_FLUSH_AND_EXIT,null);
				//#style notification_form_base
				cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.settings.clearstorage.confirm.title"),
						Locale.get("nowplus.client.java.settings.clearstorage.confirm.text"),
						cmdOk, cmdCancel,
						confirmEvent);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				break;
				
			case Event.Settings.SYNC:
				
				//#style notification_form_progress_startup
				syncNotification = new NotificationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.settings.sync.progress"),
						null,
						hide,
						2000, //2 sec 
						true,
						ProgressIndicatorItem.PROGRESS_INFINITE
						);
				syncNotification.setCommandListener(this);
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncNotification);
				
				//start sync
				getModel().sync();
				
				break;
				
			case Event.Settings.ROAMING:
				
				if(null != data && data instanceof Boolean)
				{
					//set roaming settings
					getModel().setRoamingDataConnectionAllowed(((Boolean)data).booleanValue());
				}
				break;
				
			case Event.Settings.ORDER:
				
				if(null != data && data instanceof Integer)
				{
					if(((Integer)data).intValue() == 0)
						getModel().setContactsOrder(new FirstLastOrder());
					else
					if(((Integer)data).intValue() == 1)
						getModel().setContactsOrder(new LastFirstOrder());
				}
				break;
				
			case Event.Settings.RESET_COUNTER:
				
				getModel().resetDataCounter();
				break;
				
			case Event.Settings.RESET_COUNTER_CONFIRM:
				
				//envoke confirmation
				cmdOk = new Command(Locale.get("nowplus.client.java.settings.clearstorage.confirm.yes"), Command.SCREEN, 0);
				cmdCancel = new Command(Locale.get("nowplus.client.java.settings.clearstorage.confirm.no"), Command.CANCEL, 0);
				//event to fire upon confirmation
				confirmEvent = new Event(Event.Context.SETTINGS,Event.Settings.RESET_COUNTER,null);
				//#style notification_form_base
				cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.settings.reset.confirm.title"),
						Locale.get("nowplus.client.java.settings.reset.confirm.text"),
						cmdOk, cmdCancel,
						confirmEvent);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, cf);
				break;
			}
		}
	}
	
	//#mdebug error
	public String toString()
	{
		return "SettingsController[]";
	}
    //#enddebug
}
