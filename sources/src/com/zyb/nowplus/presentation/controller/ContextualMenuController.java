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

import com.zyb.nowplus.business.LaunchException;
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.BaseTabForm;
import com.zyb.nowplus.presentation.view.forms.ContextForm;
import com.zyb.nowplus.presentation.view.forms.ContextualMenuForm;
import com.zyb.nowplus.presentation.view.forms.ProfileForm;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Displayable;

/**
 * Controller for events associated to the contextual menu
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ContextualMenuController extends ContextController
{
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param history
	 */
	public ContextualMenuController(Model model, Controller controller, ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.CONTEXTUAL_MENU;
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if(context == getContext())
		{
			switch (event) 
			{
				case Event.ContextualMenu.OPEN:
					showContextualMenu(data);
					break;
					
				case Event.ContextualMenu.VIEW_PROFILE:
					{
						//back to last checkpoint
						getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_CHECKPOINT,null);
							
						getController().notifyEvent(Event.Context.PROFILE, Event.Profile.OPEN, data);
					}
					break;	
					
					
//				case Event.ContextualMenu.INVITE:
//					
//					//back to last checkpoint
//					getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_CHECKPOINT,null);
//					
//					getController().notifyEvent(Event.Context.CONTACTS,Event.Contacts.INVITE,null);
//					
//					break;	
					
//				case Event.ContextualMenu.CONNECT:
//					
//					//back to last checkpoint
//					getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_CHECKPOINT,null);
//					
//					getController().notifyEvent(Event.Context.CONTACTS,Event.Contacts.CONNECT,null);
//					
//					break;	
					
				case Event.ContextualMenu.DELETE_CONTACT:
					
					if(null != data && data instanceof ContactProfile)
					{
						getModel().delete((ContactProfile)data);
						
						//dismiss confirmation
						getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_TO_LATEST_PEOPLESPAGE,null);
					}
					
					break;
					
				case Event.ContextualMenu.SETTINGS:
					
					//Transforming and passing on event 
					getController().handleEvent(Event.Context.SETTINGS, Event.Settings.OPEN, null);
					
					break;
		
				case Event.ContextualMenu.CHANNEL_CHAT:
					
					if(null != data && data instanceof Channel)
					{
						//init chat form
						getController().notifyEvent(Event.Context.CHAT, Event.Chat.OPEN, data);
					}
				
					break;	
					
				case Event.ContextualMenu.CHANNEL_CALL:
				case Event.ContextualMenu.CHANNEL_SMS:
				case Event.ContextualMenu.CHANNEL_EMAIL:
					
					if(null != data && data instanceof Channel)
					{
						try 
						{
							getModel().launch((Channel)data);
						}
						catch (LaunchException le)
						{
							//#debug error
							System.out.println("Error launching channel connection: " + le);
							
							//action requires app to close down
							if(LaunchException.TYPE_LAUNCH_POSTPONED == le.getType())
								getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
						}
					}
					
					break;	
					
				case Event.ContextualMenu.CREATE_TEXT:
					
					//create dummy identity
					Identity iden = Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "", false);
					iden.getChannel(Channel.TYPE_SMS);
					
					//launch native
					try 
					{
						getModel().launch(iden.getChannel(Channel.TYPE_SMS));
					}
					catch (LaunchException le)
					{
						//#debug error
						System.out.println("Error launching channel connection: " + le);
						
						//action requires app to close down
						if(LaunchException.TYPE_LAUNCH_POSTPONED == le.getType())
							getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
					}
					
					break;
					
				case Event.ContextualMenu.ADD_NEW_CONTACT:
					
					//passing null to edit form, generates new user
					getController().notifyEvent(Event.Context.EDIT_PROFILE, Event.EditProfile.OPEN, null);
					
					break;
					
				case Event.ContextualMenu.DELETE_SN:
					
					//remove sn account
					getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE, data);
					
					break;		
					
				case Event.ContextualMenu.PROFILE_FROM_CHAT:
					
					//search for ProfilePage instance in screen history
					if( history.isInStack(ProfileForm.class) )
					{
						//Closing chat sessions
						getController().notifyEvent(Event.Context.CHAT, Event.Chat.CLOSE);
					}
					else
					{
						//Closing chat sessions
						getController().notifyEvent(Event.Context.CHAT, Event.Chat.CLOSE);
						
						//back to contact list
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_TO_LATEST_PEOPLESPAGE);
						
						//launch profile view
						getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.VIEW_PROFILE, data);
					}
					
					break;						
					
					//#if polish.blackberry && add.switch.application.form
				case Event.ContextualMenu.OPEN_FORM_SWITCH_APPS:
						getController().notifyEvent(Event.Context.APP,Event.App.SHOW_LIST_SWITCH_APPS);
					break;
					//#endif
					
				//#if activate.embedded.360email
				case Event.ContextualMenu.NEW_EMAIL:
					
					break;
				case Event.ContextualMenu.GOTO_EMAIL_FOLDERS:
					
					break;
				//#endif
					
				default:
					break;
			}			
		}
	}

	private void showContextualMenu(Object obj)
	{
		ContextualMenuForm omf = null;
		
		Object form;
		if( (form = history.currentGlobalAndLocalDisplayableContainer().disp) instanceof ContextForm)
		{
			ProfileSummary ps = null;
			Object[] objs = null;
			
			ContextForm contextForm = (ContextForm)form;
			//launch menu variant based on launchpad
			switch(contextForm.getContext())
			{
				case Event.Context.CHAT:
					
					if(obj instanceof Object[])
						objs = (Object[]) obj;
					
					if(null != objs && objs.length >= 1 && objs[0] instanceof ProfileSummary)
						ps = (ProfileSummary)objs[0];
					
					/*
					 * Changing inputmode for chat descoped for R1
					Integer inputFieldMode;
					if(null != objs && objs.length >= 1 && null != objs[1] && objs[1] instanceof Integer)
						inputFieldMode = (Integer)objs[1];
					*/
					
					//#style contextualmenu_base_form
					omf = new ContextualMenuForm(getModel(),getController(), ps, contextForm.getContext(), null, (Displayable) contextForm);
					
					break;
					
				case Event.Context.CONTACTS:
				case Event.Context.PROFILE:
				case Event.Context.ACTIVITY:
				case Event.Context.TIMELINE:
				case Event.Context.EMAIL:
				//#if polish.device.supports.nativesms == false
				case Event.Context.SMS_EDITOR:
				//#endif
					
					if(obj instanceof ProfileSummary)
						ps = (ProfileSummary)obj;
						
					//#style contextualmenu_base_form
					omf = new ContextualMenuForm(getModel(),getController(), ps, contextForm.getContext(), null, (Displayable) contextForm);
					
					break;
					
//				case Event.Context.NOWPLUS_SEARCH:
//					
//					if(obj instanceof UserRef)
//						ur = (UserRef)obj;
//					
//					//#style contextualmenu_base_form
//					omf = new ContextualMenuForm(getModel(),getController(), ur, contextForm.getContext(), null, (Displayable) contextForm);
//										
//					break;	
					
				case Event.Context.EDIT_PROFILE:
					
					if(obj instanceof Object[])
						objs = (Object[])obj;

					if(objs.length == 3)
					{
						if(null != objs[0] && objs[0] instanceof ProfileSummary)
							ps = (ProfileSummary)objs[0];
						Event event = (Event)objs[1];
						String suffix = (String)objs[2];
						
						//#style contextualmenu_base_form
						omf = new ContextualMenuForm(getModel(),getController(), ps, contextForm.getContext(), new Object[]{suffix,event}, (Displayable) contextForm);
					}
					
					break;		
					
				case Event.Context.WEB_ACCOUNTS:
					
					if(obj instanceof Object[])
						objs = (Object[])obj;

					Identity iden = null;

					if(objs.length == 2)
					{
						if(objs[0] instanceof ProfileSummary)
							ps = (ProfileSummary)objs[0];

						iden = (Identity)objs[1];
					}
					
					//#style contextualmenu_base_form
					omf = new ContextualMenuForm(getModel(),getController(), ps, contextForm.getContext(), new Object[]{iden}, (Displayable) contextForm);
					
					break;
					
				default:
					//#debug error
					System.out.println("Contextual menu for context: "+((BaseTabForm)history.currentGlobalAndLocalDisplayableContainer().disp).getContext()+" not found");
					break;
			}
		}
		
		if(null != omf)
			//NOTE: No checkpoint!
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, omf);
	}	

	
	//#mdebug error
	public String toString()
	{
		return "ContextualMenuController[]";
	}
    //#enddebug

}
