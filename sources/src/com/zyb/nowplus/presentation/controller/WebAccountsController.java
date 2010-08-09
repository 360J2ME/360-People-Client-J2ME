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
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.forms.EditWebAccountForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.WebAccountsForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.Locale;

public class WebAccountsController extends ContextController implements CommandListener
{
	private WebAccountsForm webAccountsForm;
	
	private EditWebAccountForm editWebAccountForm;

	private NotificationForm progressNotification;
	//#if (${lowercase(polish.vendor)}==nokia) || (${lowercase(polish.vendor)}==samsung) 
	//#define tmp.formShownTwice
	private ConfirmationForm confirmationForm;
    //#endif
	
	protected final Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
    protected final Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
    protected final Command hide = new Command(Locale.get("polish.command.hide"), Command.ITEM, 0);
    protected final Command hide2 = new Command(Locale.get("polish.command.hide"), Command.ITEM, 0);

	
	public WebAccountsController(Model model, Controller controller, ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}
	
	public void commandAction(Command c, Displayable d) 
	{
		if(hide == c)
		{
			synchronized (history)
			{
				if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}
				
				//TODO: cancel add/signup when model supports this
				
				/*
				 * return to webaccounts when user forcefully removes progress indicator
				 */
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
			}
		}
		/*
		else
		if(hide2)
		{
			if(this.progressNotification != null)
			{
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
				this.progressNotification = null;
			}
			
			//TODO: cancel remove/login/logout when model supports this
		}
		*/
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext()
	{
		return Event.Context.WEB_ACCOUNTS;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data)
	{
		//#debug debug
		System.out.println("context:"+context+"/event:"+event);
	
		if(context == getContext())
		{
			switch(event){
			case Event.WebAccounts.NO_WEB_ACCOUNT_ADDED:
				//#ifdef tmp.formShownTwice
				if(confirmationForm == null)
				//#endif
				{
					Event confirmEvent = new Event(Event.Context.WEB_ACCOUNTS,Event.WebAccounts.OPEN,null);
	                Event cancelEvent = new Event(Event.Context.WEB_ACCOUNTS,Event.WebAccounts.SKIP, null);

	                //#style notification_form_base
	                ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(),
	                    Locale.get("nowplus.client.java.webaccounts.popup.importnow.title"),
	                    Locale.get("nowplus.client.java.webaccounts.popup.importnow.text"),
						ok, cancel,
						confirmEvent,cancelEvent);

	                //#if polish.blackberry
	                	cf.setShownCancelCmdButton(true);
	                //#endif
	                
	                //#ifdef tmp.formShownTwice
	                	confirmationForm = cf;
	                //#endif
                
	                getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				}
				break;

			case Event.WebAccounts.OPEN:
			{
				//#debug debug
				System.out.println("Event.WebAccounts.OPEN");

			    
				//refreshing the ui (update function in webaccount form) doesn't work it never calls 
				//command action in web account forms second time if you open the web form.
				
				//#style base_form
				this.webAccountsForm = new WebAccountsForm(getModel(), getController());
				
				//#debug debug
				System.out.println("show webAccountsForm");

				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, this.webAccountsForm);
			}
			break;

			case Event.WebAccounts.BACK:
			{
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK);
				getModel().finishAddingSocialNetworkAccounts();
			}
			break;

			case Event.WebAccounts.SKIP:
			{
				getModel().skipAddingSocialNetworkAccounts();
			}
			break;

			case Event.WebAccounts.EDIT:
			{
				if(null != data && data instanceof Object[])
				{
					Object[] objs = (Object[])data;
					
					ExternalNetwork enw = null;
					Identity iden = null;
					
					if(null != objs[0])
						enw = (ExternalNetwork)objs[0];
					if(null != objs[1])
						iden = (Identity)objs[1];
					
					//#style base_form
					this.editWebAccountForm = new EditWebAccountForm(getModel(), getController(), enw, iden);
					
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, this.editWebAccountForm);
				}
			}
			break;
			case Event.WebAccounts.SAVE:
			{
				if (!validateEnteredData())
                    return;
				
                //#style notification_form_progress
				this.progressNotification = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.saving.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.saving.text"),
						hide,
						-1, 
						true,  
						ProgressIndicatorItem.PROGRESS_INFINITE
						);
				this.progressNotification.setCommandListener(this);
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, this.progressNotification);
				
				getModel().addSocialNetworkAccount(
						this.editWebAccountForm.getNetwork(), 
						this.editWebAccountForm.getUsername(), 
						this.editWebAccountForm.getPassword(), 
						true
						);
			}
			break;

			case Event.WebAccounts.LOGOUT:
			{
				//#style notification_form_progress
				this.progressNotification = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.loggingout.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.loggingout.text"),
						hide2,
						-1, true,  ProgressIndicatorItem.PROGRESS_INFINITE
					);
				/*
				this.progressNotification.setCommandListener(this);
				*/
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, this.progressNotification);
				
				getModel().logoutSocialNetworkAccount((Identity)data);
			}
			break;

			case Event.WebAccounts.LOGIN:
			{
				//#style notification_form_progress
				this.progressNotification = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.saving.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.saving.text"),
						hide2,
						-1, true,  ProgressIndicatorItem.PROGRESS_INFINITE
					);
				/*
				this.progressNotification.setCommandListener(this);
				*/
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, this.progressNotification);
				
				getModel().loginSocialNetworkAccount((Identity)data);
			}
			break;

			case Event.WebAccounts.REMOVE:
			{
				//#style notification_form_progress
				this.progressNotification = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.progress.deletesn.title"),
						null,
						hide2,
						-1, true,  ProgressIndicatorItem.PROGRESS_INFINITE
					);
				/*
				this.progressNotification.setCommandListener(this);
				*/
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, this.progressNotification);
				getModel().removeSocialNetworkAccount((Identity)data);
			}
			break;

			case Event.WebAccounts.ADD_FAILED:
			case Event.WebAccounts.ADD_TIMED_OUT:
			{
				if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

				//#style notification_form_base
				NotificationForm nf = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.savingfailed.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.savingfailed.text"),
						ok,
						-1
						);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
			}
			break;

			case Event.WebAccounts.ADD_SUCCEEDED:
			{
				if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT, null);

				//#style notification_form_base
				NotificationForm nf = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.savingsuccess.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.savingsuccess.text"),
						ok,
						-1
						);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
			}
			break;

			case Event.WebAccounts.LOGIN_FAILED:
			case Event.WebAccounts.LOGIN_TIMED_OUT:
			{
				if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

				//#style notification_form_base
				NotificationForm nf = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.loginfailed.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.loginfailed.text"),
						ok,
						-1
						);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
			}
            break;

			case Event.WebAccounts.REMOVE_FAILED:
			case Event.WebAccounts.REMOVE_TIMED_OUT:
            {
                if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

                //#style notification_form_base
                NotificationForm nf = new NotificationForm(
                        model, controller,
                        Locale.get("nowplus.client.java.webaccounts.popup.removefailed.title"),
                        Locale.get("nowplus.client.java.webaccounts.popup.removefailed.text"),
                        ok,
                        -1
                        );
                getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
            }
            break;

			case Event.WebAccounts.LOGOUT_FAILED:
			case Event.WebAccounts.LOGOUT_TIMED_OUT:
            {
                if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

                //#style notification_form_base
                NotificationForm nf = new NotificationForm(
                        model, controller,
                        Locale.get("nowplus.client.java.webaccounts.popup.logoutfailed.title"),
                        Locale.get("nowplus.client.java.webaccounts.popup.logoutfailed.text"),
                        ok,
                        -1
                        );
                getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
            }
            break;

			case Event.WebAccounts.LOGIN_SUCCEEDED:
			{
				if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

				//disable below code for fixing bug of JIRA Story#206:[After logging to web account, the user is navigated out of web accounts page]
				//Also see this page line#284
				/*
				//go back if currently in EditWebAccountForm
				DisplayableContainer dispCon = history.currentGlobalAndLocalDisplayableContainer();
				if( null != dispCon && !(dispCon.disp instanceof WebAccountsForm) )
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT, null);
				 */

				//#style notification_form_base
				NotificationForm nf = new NotificationForm(
						model, controller, 
						Locale.get("nowplus.client.java.webaccounts.popup.loginsucceeded.title"),
						Locale.get("nowplus.client.java.webaccounts.popup.loginsucceeded.text"),
						ok,
						-1
						);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
			}
            break;

			case Event.WebAccounts.REMOVE_SUCCEEDED:
            {
                if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

                //back to webaccounts form if in contextual menu
                Object disp;
				while( null != history.currentLocalDisplayableContainer() && 
						null != (disp = history.currentLocalDisplayableContainer().disp) && 
						!(disp instanceof WebAccountsForm)
						)
					history.backSilent(); //direct call to screenstack, do not use Controller
				
                //#style notification_form_base
                NotificationForm nf = new NotificationForm(
                        model, controller,
                        Locale.get("nowplus.client.java.webaccounts.popup.removesucceeded.title"),
                        Locale.get("nowplus.client.java.webaccounts.popup.removesucceeded.text"),
                        ok,
                        -1
                        );
                getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
            }
            break;

			case Event.WebAccounts.LOGOUT_SUCCEEDED:
            {
                if(this.progressNotification != null)
				{
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, progressNotification);
					this.progressNotification = null;
				}

                //#style notification_form_base
                NotificationForm nf = new NotificationForm(
                        model, controller,
                        Locale.get("nowplus.client.java.webaccounts.popup.logoutsucceeded.title"),
                        Locale.get("nowplus.client.java.webaccounts.popup.logoutsucceeded.text"),
                        ok,
                        -1
                        );
                getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
            }
            break;
			}
		}
	}

	/**
	 * @return
	 */
	private boolean validateEnteredData()
	{
		if(this.editWebAccountForm != null)
		{
			if(this.editWebAccountForm.getUsername() == null || "".equals(this.editWebAccountForm.getUsername().trim()))
			{
				showError(Locale.get("nowplus.client.java.login.loginfailed.text"));
				return false;
			}

			if(this.editWebAccountForm.getPassword() == null || "".equals(this.editWebAccountForm.getPassword().trim()))
			{
				showError(Locale.get("nowplus.client.java.login.loginfailed.text"));
				return false;
			}
        }
         return true;
     }

    /**
	 * @param txt error message
	 */
	private void showError(String txt) {

		//#debug debug
		System.out.println("Trying to show error:"+txt);

		//#style signup_notification
		NotificationForm nf = new NotificationForm(
				getModel(), getController(),
				Locale.get( "nowplus.client.java.signup.error.headline" ),
				txt,
				ok,
				-1
				);
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, nf);
	}

	//#mdebug error
	public String toString()
	{
		return "WebAccountsController[]";
	}
    //#enddebug
}
