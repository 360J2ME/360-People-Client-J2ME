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
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.ProfileForm;
import com.zyb.util.event.Event;

/**
 * Controller for events associated to the profile page
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ProfileController extends ContextController
{
	private ProfileForm ppf;
	
	public ProfileController(Model model, Controller controller,
			ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.PROFILE;
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
				case Event.Profile.OPEN: 
					//Profile view invoked on contact
					if (data instanceof ManagedProfile) {
						if(data instanceof ContactProfile) {
							ContactProfile c = (ContactProfile)data;

							if (!c.isDeleted()) {
								showProfile(c);
							}
						}
						else if(data instanceof MyProfile) {
							showProfile((MyProfile)data);
						}
					}
					break;

				case Event.Profile.LAUNCH_CHANNEL: 
					if (data instanceof Channel) {
						try {
							getModel().launch((Channel)data);
						}
						catch (LaunchException le) {
							//#debug error
							System.out.println("Error launching channel connection: " + le);
							
							//action requires app to close down
							if(LaunchException.TYPE_LAUNCH_POSTPONED == le.getType()) {
								getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
							}
						}
					}				
					break;

					/*
				case Event.Profile.CONTACTS_INIT: 
					//init friendsoffriends searching
					getModel().searchFriendsOfSelectedProfile();
					break;					
				case Event.Profile.CONTACTS_REFRESH: 
					//notify view
					getController().notifyEvent(context, event);
					break;
				case Event.Profile.SEARCH: 
					//possibly support search
					break;
				case Event.Profile.SEARCH_FINISHED: 
					//possibly support search					
					break;
					*/

				case Event.Profile.DELETE_DETAIL:
					if (null != ppf) {
						ppf.handleEvent(context, event, data);
					}
					break;

				default: 
					break;	
			}	
		}
	}
	
	private void showProfile(ManagedProfile profile)
	{
		if (profile instanceof ContactProfile) {
			model.selectContact((ContactProfile)profile);
		}

		//#style profilepage_form
		ppf = new ProfileForm(getModel(),getController(), profile);
		
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, ppf);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ProfileController[]";
	}	
    //#enddebug
}
