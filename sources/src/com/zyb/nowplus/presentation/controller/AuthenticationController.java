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
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.LoginForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.Locale;

/**
 * The application controller
 * @author Jens Vesti
 *
 */
public class AuthenticationController extends ContextController implements CommandListener
{
	private LoginForm loginForm;

	private NotificationForm loginNotification;
	
	protected EventListener currentListener;
	protected Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);

	private boolean firstLogin = false;

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param history
	 */
	public AuthenticationController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.AUTHENTICATION;
	}

	private void showLogin(boolean firstLogin)
	{
		//#debug debug
		System.out.println("showLogin");
		
		//getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginForm);
		
		if(loginForm == null)
		{
			//#style base_form
			loginForm = new LoginForm(model,controller,Locale.get("nowplus.client.java.login.title"),firstLogin);
			loginForm.setCommandListener(this);
		}
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, loginForm);
		
		//#if polish.blackberry
		loginForm.focus(loginForm.getUserNameTextField());
		//#endif
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		//#debug debug
		System.out.println("context:"+context+"/event:"+event+"/data:"+data);

		/*
		 * TODO Put this in a notification when init times out 
		 * nowplus.client.java.application.initialisation.failed.title
		 * nowplus.client.java.application.initialisation.failed.text
		 * */
		
		if(context == getContext())
		{
			switch(event)
			{
				case Event.Authentication.SAVE:
					//#debug debug
					System.out.println("calling login");

					//#debug debug
					System.out.println("((Boolean)((Object[])data)[2]).booleanValue():"+((Boolean)((Object[])data)[2]).booleanValue());
					
					//Object[] must be in the "username,password,stay_logged_in" order
					getModel().login(((String)((Object[])data)[0]), ((String)((Object[])data)[1]), ((Boolean)((Object[])data)[2]).booleanValue());
					break;
					
                case Event.Authentication.SHOW_FIRST_LOGIN:
                		firstLogin = true;

                		// Explicitely fall-through.

                case Event.Authentication.LOGIN:
						//#if bypass-login-form && username:defined && password:defined
							//#message Bypassing login form.
							//#= model.login("${username}", "${password}", true);
						//#else	
							//#if polish.blackberry
					        	new Thread()
					        	{
					        		public void run()
					        		{
					        			showLogin(firstLogin);
					        		}
					        	}.start();
							//#else
								showLogin(firstLogin);
							//#endif
						//#endif
						break;

				case Event.Authentication.CONFIRM_USER_NAME_CHANGE:
				case Event.Authentication.USER_NAME_CHANGE_NOT_ALLOWED:
                case Event.Authentication.USER_DISALLOWED_CONNECTION:
	    				//event to fire upon confirmation
                        Event confirmEvent = new Event(Event.Context.APP,Event.App.CONFIRMED_EXIT,null);
                        //#style notification_form_base
                        ConfirmationForm cf = new ConfirmationForm(
							getModel(), getController(),
                        Locale.get("nowplus.client.java.login.connection.disallowed.title"),
                        Locale.get("nowplus.client.java.login.connection.disallowed.text"),
							ok, null,
							confirmEvent);
					    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
					    break;

                case Event.Authentication.NO_SERVICE:
						if (loginNotification != null) {
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);
							loginNotification = null;
						}

						break;

				case Event.Authentication.AUTHENTICATION_FAILED:
					
						//#if polish.blackberry
							new Thread()
							{
								public void run()
								{
									showLogin(false);
								}
							}.start();
						//#else
							showLogin(false);
						//#endif

						if(loginNotification != null)
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);

						//#style notification_form_base
						loginNotification = new NotificationForm(
								model, controller,
								Locale.get("nowplus.client.java.login.loginfailed.title"),
								Locale.get("nowplus.client.java.login.reauthenticate.text"),
								ok,
								0
								);
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, loginNotification);
						setCallback(loginNotification);

						//#if polish.blackberry
						if(loginForm!=null&&loginForm.getUserNameTextField()!=null)
							loginForm.focus(loginForm.getUserNameTextField(),true);
						//#endif
						break;

				case Event.Authentication.LOGIN_FAILED:
                	
                	    //#if polish.blackberry
                	        new Thread()
                	        {
                	        	public void run()
                	        	{
                	        		showLogin(false);
                	        	}
                	        }.start();
                		//#else
							showLogin(false);
						//#endif

						if(loginNotification != null)
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);
						
						//#style notification_form_base
						loginNotification = new NotificationForm(
								model, controller, 
								Locale.get("nowplus.client.java.login.loginfailed.title"),
								Locale.get("nowplus.client.java.login.loginfailed.text"),
								ok,
								0
								);
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, loginNotification);
						setCallback(loginNotification);
						
						//#if polish.blackberry
						if(loginForm!=null&&loginForm.getUserNameTextField()!=null)
							loginForm.focus(loginForm.getUserNameTextField(),true);
						//#endif

						break;

                case Event.Authentication.LOGIN_SUCCEEDED:
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);
						loginNotification = null;
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginForm);
						loginForm = null;
						this.currentListener = null;
						break;

				case Event.Authentication.AUTHENTICATION_FAILED_NEW_PASSWORD:
	                    Event confirmExitEvent = new Event(Event.Context.APP,Event.App.CONFIRMED_EXIT,null);

	                    //#style notification_form_base
	                    ConfirmationForm cef = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.newpasswordrestart.title"),
							Locale.get("nowplus.client.java.newpasswordrestart.text"),
							ok, null,
							confirmExitEvent);
					    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cef);
						break;
			}
		}
	}
	
	//#mdebug error
	public String toString()
	{
		return "AuthenticationController[]";
	}
    //#enddebug


	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable d) 
	{
		if(cmd == LoginForm.cmdSave)
		{
			getController().notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.SAVE, new Object[]{loginForm.getUsername(),loginForm.getPassword(),new Boolean(loginForm.isRememberMeSet())});
		
			if(loginNotification != null)
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);

			//#style .notification_form_progress
			loginNotification = new NotificationForm(
					model, controller, 
					Locale.get("nowplus.client.java.login.notification.loggingin"),
					null,
					null,
					0, true,  ProgressIndicatorItem.PROGRESS_INFINITE
					);
			loginNotification.setCommandListener(this);
			
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, loginNotification);
			setCallback(loginNotification);
		}
		else
		if(cmd == NotificationForm.cmdCancel)
		{
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginNotification);
			getModel().cancelLogin();
		}
		else
		if(cmd == LoginForm.cmdCancel)
		{
			getController().notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null);
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, loginForm);
		}
		else
		if(cmd == LoginForm.cmdExit)
		{
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT, null);
		}
	}	
	
	/**
	 * If the specified Displayable implements EventCallback
	 * set it as the current EventCallback
	 * 
	 * @param disp the Displayable
	 */
	public void setCallback(Displayable disp)
	{
		if(disp instanceof EventListener)
		{
			this.currentListener = (EventListener)disp;
		}
	}
}
