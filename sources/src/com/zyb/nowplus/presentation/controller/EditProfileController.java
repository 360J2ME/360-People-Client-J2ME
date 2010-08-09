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
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.EditDetailForm;
import com.zyb.nowplus.presentation.view.forms.EditProfileForm;
import com.zyb.util.event.Event;

public class EditProfileController extends ContextController
{
	private EditProfileForm ecf;
	
	/**
	 * 
	 * @param model
	 * @param view
	 * @param controller
	 */
	public EditProfileController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#getContext()
	 */
	public byte getContext() 
	{
		return Event.Context.EDIT_PROFILE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		//#debug debug
		System.out.println(context+"/"+event);
		
		if(context == getContext())
		{
			switch(event)
			{		
					case Event.EditProfile.OPEN:
						if(null != data && data instanceof ManagedProfile)
							showEditContact((ManagedProfile)data);
						else
							showEditContact(null);
						break;
						
					case Event.EditProfile.SAVE:
						
						//#if polish.blackberry
						
						if(ecf != null)
						{
							try
							{
								ecf.handleEvent(context, event, data);	
							}
						    catch(Exception e)
						    {
								//#debug error
								System.out.println(this + " exception:" + e);
						    }
						}
						
						//#else
						
						//pass to edit form
						if(ecf != null)
							ecf.handleEvent(context, event, data);
						
						//#endif
						
						break;
						
					case Event.EditProfile.EDIT_NAME:
					case Event.EditProfile.EDIT_PHONE:
					case Event.EditProfile.EDIT_EMAIL:
					case Event.EditProfile.EDIT_ADRESS:
					case Event.EditProfile.EDIT_GROUP:
					case Event.EditProfile.EDIT_URL:
					case Event.EditProfile.EDIT_BIRTHDAY:
					case Event.EditProfile.EDIT_NOTE:
					case Event.EditProfile.EDIT_WORK:
						
					case Event.EditProfile.NEW_NAME:
					case Event.EditProfile.NEW_PHONE:
					case Event.EditProfile.NEW_EMAIL:
					case Event.EditProfile.NEW_ADRESS:
					case Event.EditProfile.NEW_GROUP:
					case Event.EditProfile.NEW_URL:
					case Event.EditProfile.NEW_BIRTHDAY:
					case Event.EditProfile.NEW_NOTE:
					case Event.EditProfile.NEW_WORK:
						if(null != data && data instanceof Event)
							showEditDetail((Event)data);
						break;
						
					case Event.EditProfile.DELETE_DETAIL_CONTEXTUAL:						
						
						//back to checkpoint from contextual
						history.back(true, true); //direct call to screen stack, do not use Controller
						
						//NO BREAK!!!
						
					case Event.EditProfile.DELETE_DETAIL:
						
						if(ecf != null)
							ecf.handleEvent(context, event, data);
						
						break;
				default: 
					break;
			}
		}
	}
	
	private void showEditContact(ManagedProfile contact)
	{
		if(null != contact)
		{
			//#style base_form
			ecf = new EditProfileForm(model, controller, contact, false);
		}
		else
		{
			//create new contact
			ContactProfile c = getModel().createContact();
			
			//#style base_form
			ecf = new EditProfileForm(model, controller, c, true);
		}
		
		//#if polish.blackberry
		if(ecf!=null)
		{
			try
			{
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, ecf);
			}
			catch(Exception e)
			{
				//#debug error
				System.out.println(this+" exception:" + e);
			}
		}
		//#else
		if(null != ecf)
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, ecf);
		//#endif
	}
	
	private void showEditDetail(Event event)
	{
		//#style editdetail_form
		EditDetailForm edf = new EditDetailForm(model, controller, event);
		
		//#if polish.blackberry
		if(edf!=null)
		{
			try
			{
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, edf);	
			}
			catch(Exception e)
			{
				//#debug error
				System.out.println(this+" exception:" + e);
			}
		}
		//#else
		if(null != edf)
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, edf);	
		//#endif
	}
	
	//#mdebug error
	public String toString()
	{
		return "EditContactController[]";
	}
    //#enddebug

}
