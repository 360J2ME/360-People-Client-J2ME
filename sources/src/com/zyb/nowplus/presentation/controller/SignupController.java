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
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.SignupForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.DateHelper;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.Locale;

/**
 * The signup controller
 * @author Jens Vesti
 *
 *
 * Prepared textkeys:
 * nowplus.client.java.signup.importsn.confirmation.title
 * nowplus.client.java.signup.importsn.confirmation.text
 *
 * nowplus.client.java.signup.waitforsms.notification.title
 * nowplus.client.java.signup.waitforsms.notification.text
 *
 */
public class SignupController extends ContextController implements CommandListener
{
	private SignupForm signupForm;
	private NotificationForm checkingData;

	protected Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param history
	 */
	public SignupController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext()
	{
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		//#debug debug
		System.out.println("context:"+context+"/event:"+event+"/data:"+data);

		if(context == Event.Context.AUTHENTICATION)
		{
			switch(event)
			{
					case Event.Authentication.FIRST_LOGIN:
                    	showWelcome();
                   	break;
					case Event.Authentication.SIGNUP_SUCCEEDED:
						if(checkingData != null)
							getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, checkingData);
						
						showCongratulations();
                   	break;
					case Event.Authentication.SIGNUP_FAILED:
						
						if(checkingData != null)
							getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, checkingData);

						//display error message received from server
						
						//#style notification_form_base
						NotificationForm nf = new NotificationForm(
								model, controller, 
								Locale.get("nowplus.client.java.signup.signupfailed.title"),
								(String) data,
								ok,
								0
								);
							getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
                   	break;

					case Event.Authentication.SIGNUP_FAILED_WRONG_MSISDN:
						if(checkingData != null)
							getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, checkingData);
						
						//display error message received from server
						
						//#style notification_form_base
						NotificationForm nfWrongNumber = new NotificationForm(
								model, controller, 
								Locale.get("nowplus.client.java.signup.signupfailedwrongnumber.title"),
								Locale.get("nowplus.client.java.signup.signupfailedwrongnumber.text"),
								ok,
								0
								);
							getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nfWrongNumber);
							
						showRerequestPhoneNumber();
                   	break;

			}
		}
	}
	
	/**
	 * 
	 */
	private void showRerequestPhoneNumber() {
		signupForm.setState(SignupForm.REREQUEST);
		
	}

	private void showWelcome() {
		
		if(signupForm == null)
		{
			//#style signup_form
			signupForm = new SignupForm(model,controller,null);
			signupForm.setVisibleCommandListener(this);
			getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, signupForm);

		}
		else
			signupForm.setState(SignupForm.WELCOME);
	}

	private void showEnterData() {
		signupForm.setState(SignupForm.ENTER_DATA);
	}

	//#mdebug error
	public String toString()
	{
		return "SignupController[]";
	}
    //#enddebug

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable d) 
	{
		//#debug debug
		System.out.println("CMD:"+cmd.getLabel());
		
		if (cmd == SignupForm.CMD_LOGIN)
		{
			//#if polish.blackberry
				new Thread()
				{
					public void run()
					{
						getController().handleEvent(Event.Context.AUTHENTICATION, Event.Authentication.SHOW_FIRST_LOGIN, null);
						getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, signupForm);
						signupForm = null;
					}
				}.start();
			//#else
				getController().handleEvent(Event.Context.AUTHENTICATION, Event.Authentication.SHOW_FIRST_LOGIN, null);
				getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, signupForm);
				signupForm = null;
			//#endif
		}
		else if (cmd == SignupForm.CMD_SIGNUP)
		{
			showEnterData();
		}
		else if (cmd == SignupForm.CMD_CONTINUE_CONGRATULATIONS)
		{
			if (signupForm != null)
			{
				getController().handleEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, signupForm);
				getModel().finishSignUp(signupForm.getRememberMe());
			}
		}
		else if (cmd == SignupForm.CMD_CONTINUE_ENTER_DATA)
		{
			if (validateEnteredData())
			{

				//#style notification_form_progress
				checkingData = new NotificationForm(
						model, controller, 
						Locale.get( "nowplus.client.java.signup.checkingdata.progress.headline" ),
						Locale.get( "nowplus.client.java.signup.checkingdata.progress.text" ),
						null,
						-1, true,  ProgressIndicatorItem.PROGRESS_INFINITE
						);
				checkingData.setCommandListener(this);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, checkingData);
				getModel().signup(signupForm.getUsername(), signupForm.getPassword(), signupForm.getPhoneNumber(),signupForm.getEmail(), signupForm.getDoB(), signupForm.agreesToTC());
			}
		}
		else if (cmd == SignupForm.CMD_CONTINUE_REENTER_DATA)
		{
			if (validatePhonenumber())
			{
				//#style notification_form_progress
				checkingData = new NotificationForm(
						model, controller, 
						Locale.get( "nowplus.client.java.signup.checkingdata.progress.headline" ),
						Locale.get( "nowplus.client.java.signup.checkingdata.progress.text" ),
						null,
						-1, true,  ProgressIndicatorItem.PROGRESS_INFINITE
						);
				checkingData.setCommandListener(this);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, checkingData);
				getModel().rerequestConfirmationSMS(signupForm.getPhoneNumber());
			}
		}
		else if (cmd == SignupForm.CMD_CANCEL)
		{
			showWelcome();
		}
		else if (cmd == SignupForm.CMD_EXIT)
		{
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT, null);
		}
		else if (cmd == SignupForm.CMD_READTC || cmd == SignupForm.CMD_READPRIVACY)
		{
			Identity ident = null;

			if (cmd == SignupForm.CMD_READPRIVACY) {
				//#= ident = Identity.createUrl("http://${external.webpages.domain}/" + TextUtilities.getCurrentLanguageForLinks() + "/web/mobile/privacy", Locale.get("nowplus.client.java.signup.enterdata.privacy.text"));
			}
			else {
				//#= ident = Identity.createUrl("http://${external.webpages.domain}/" + TextUtilities.getCurrentLanguageForLinks() + "/web/mobile/terms", Locale.get("nowplus.client.java.signup.enterdata.termsandconditions.text"));
			}

			//#debug debug
			System.out.println("launching browser with url:" + ident.getUrl());
			
			//#ifdef polish.device.requires.polish.browser
				getController().notifyEvent(Event.Context.APP, Event.App.BROWSE_POLISH, ident);
			//#else
				getController().notifyEvent(Event.Context.APP, Event.App.BROWSE, ident);
			//#endif
		}
		else if (cmd == NotificationForm.cmdCancel)
		{
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, checkingData);
			getModel().cancelSignUp();
		}
	}

	/**
	 * 
	 */
	private void showCongratulations()
	{
		signupForm.setState(SignupForm.CONGRATULATIONS);
	}

	/**
	 * @return
	 */
	private boolean validateEnteredData()
	{
		if (signupForm != null)
		{
			String username = signupForm.getUsername(); 
			String password = signupForm.getPassword(); 

			if (username == null || username.length()<6 || username.length()>50)
			{
				signupForm.focusField(SignupForm.USERNAME_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.usernamelength.headline" ), Locale.get( "nowplus.client.java.signup.error.usernamelength" ));
				return false;
			}

			if (username != null)
			{
				String invalidChars = TextUtilities.invalidChars("._abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-", username);

				if (invalidChars.length()>0)
				{
					signupForm.focusField(SignupForm.USERNAME_FIELD);
					showError(Locale.get( "nowplus.client.java.signup.error.usernamechars.headline"), Locale.get( "nowplus.client.java.signup.error.usernamechars" )+" "+invalidChars);
					return false;
				}
			}
			
			if (password == null || password.length() < 6 || password.length() > 25)
			{
				signupForm.focusField(SignupForm.PASSWORD_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.passwordlength.headline" ), Locale.get( "nowplus.client.java.signup.error.passwordlength" ));
				return false;
			}

			if (password != null)
			{
				String invalidChars = TextUtilities.invalidChars("#.?/-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", password);

				if (invalidChars.length() > 0)
				{
					signupForm.focusField(SignupForm.PASSWORD_FIELD);
					showError(Locale.get( "nowplus.client.java.signup.error.passwordchars.headline" ), Locale.get( "nowplus.client.java.signup.error.passwordchars" )+" "+invalidChars);
					return false;
				}
			}
			
			//check dob is correct

			if (signupForm.getDoB() == null)
			{
				signupForm.focusField(SignupForm.DOB_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.dobnotprovided.headline" ), Locale.get( "nowplus.client.java.signup.error.dobnotprovided" ));
				return false;
			}

			if (!DateHelper.ageAcceptable(signupForm.getDoB(), null, 14))
			{
				signupForm.focusField(SignupForm.DOB_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.mustbe14.headline" ), Locale.get( "nowplus.client.java.signup.error.mustbe14" ));
				return false;
			}

			if (!validatePhonenumber())
				return false;
			
			if (!validateEmail())
			{
				signupForm.focusField(SignupForm.EMAIL_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.email.headline" ),Locale.get( "nowplus.client.java.signup.error.email.invalid" ));
				return false;
			}

			if (!signupForm.agreesToTC())
			{
				signupForm.focusField(SignupForm.TOC_FIELD);
				showError(Locale.get( "nowplus.client.java.signup.error.mustaccepttc.headline" ), Locale.get( "nowplus.client.java.signup.error.mustaccepttc" ));
				return false;
			}

			return true;
		}
		
		return false;
	}

	
	private boolean validatePhonenumber() 
	{
		if(signupForm.getPhoneNumber() == null || signupForm.getPhoneNumber().trim().length()==0 || (TextUtilities.getCurrentHeaderPrefix() != null && signupForm.getPhoneNumber().trim().equals(TextUtilities.getCurrentHeaderPrefix())))
		{
			signupForm.focusField(SignupForm.PHONENUMBER_FIELD);
			showError(Locale.get( "nowplus.client.java.signup.error.mustenterphonenumber.headline" ), Locale.get( "nowplus.client.java.signup.error.mustenterphonenumber" ));
			return false;
		}

		if(signupForm.getPhoneNumber() != null && !signupForm.getPhoneNumber().trim().startsWith("+"))
		{
			signupForm.focusField(SignupForm.PHONENUMBER_FIELD);
			showError(Locale.get( "nowplus.client.java.signup.error.mustbeinternational.headline" ), Locale.get( "nowplus.client.java.signup.error.mustbeinternational" ));
			return false;
		}

		return true;
	}
	
	private boolean validateEmail()
	{
		return  TextUtilities.isValidEmail(signupForm.getEmail());
	}

	/**
	 * @param string
	 */
	private void showError(String header, String txt)
	{
		//#debug debug
		System.out.println("Trying to show error:"+txt);
		
		//#style signup_notification
		NotificationForm nf = new NotificationForm(
				getModel(), getController(), 
				header == null ? Locale.get( "nowplus.client.java.signup.error.headline" ):header,
				txt,
				ok,
				-1
				);
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
	}
}
