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
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
//#if polish.blackberry && add.switch.application.form
import com.zyb.nowplus.presentation.view.forms.SwitchApplicationsListForm;
//#endif
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;
import com.zyb.util.ATagHandler;
//#= import com.zyb.util.TextUtilities;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Form;
import de.enough.polish.util.Locale;

//#if polish.device.requires.polish.browser
import com.zyb.nowplus.presentation.view.browser.xPolishBrowser;
import com.zyb.nowplus.presentation.view.browser.BrowserListener;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
//#endif

import java.lang.String;

/**
 * The application controller
 *
 * @author Andre Schmidt
 */
public class AppController
	extends ContextController
	implements CommandListener
{
	private static final Command cmdConnectionDownOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
	private static final Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
	private static final Command cmdExit = new Command(Locale.get("nowplus.client.java.command.exit"), Command.CANCEL, 0);
	private static final Command cmdRoamingYes = new Command(Locale.get("nowplus.client.java.command.roaming.yes"), Command.SCREEN, 0);
	private static final Command cmdUpdateYes = new Command(Locale.get("nowplus.client.java.updateapplication.confirm.yes"), Command.SCREEN, 0);
	private static final Command cmdBrowserBack = new Command(Locale.get("polish.command.back"), Command.BACK, 9);

    private NotificationForm connDownNotif;
    private ConfirmationForm exitAppConfirmNotification;
    private NotificationForm serviceUnavailableNotification;

	public AppController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext()
	{
		return Event.Context.APP;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data)
	{
		//#debug debug
		System.out.println("context/event:"+context+"/"+event);

		if (context == getContext()) {
			switch (event) {
				case Event.App.SERVICE_UNAVAILABLE:
					if (serviceUnavailableNotification == null) {
	                    //#style notification_form_base
	                    serviceUnavailableNotification = new NotificationForm(
	                    		model, controller,
								Locale.get("nowplus.client.java.app.serviceunavailable.title"),
								Locale.get("nowplus.client.java.app.serviceunavailable.text"),
	                            cmdOk,
	                            -1);
	                    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, serviceUnavailableNotification);
					}
					break;

				case Event.App.CONTACTS_CORRUPTED:
					//invoke confirmation
					Command cmdOkExit = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);

					//event to fire upon confirmation
					Event confirmEventCorrupted = new Event(Event.Context.APP,Event.App.MARK_FOR_FLUSH_AND_EXIT,null);

					//#style notification_form_base
					ConfirmationForm cfe = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.app.criticalerror.title"),
							Locale.get("nowplus.client.java.app.criticalerror.text"),
							cmdOkExit, null,
							confirmEventCorrupted);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cfe);
					break;

				case Event.App.MARK_FOR_FLUSH_AND_EXIT:
					showExitProgress();
					//Setting it to true will flush RMS
					getModel().exit(true);
					break;
		
				case Event.App.EXIT:
					//if this notification is showing, not try to popup new one for same purpose
					if (exitAppConfirmNotification != null
						&& history.currentGlobalAndLocalDisplayableContainer().disp == exitAppConfirmNotification) {
						return;
					}

					//reused existed object of Notification created 
					if (exitAppConfirmNotification == null) {
						//invoke confirmation
						Command cmdOk = new Command(Locale.get("nowplus.client.java.exit.confirm.yes"), Command.SCREEN, 0);
						Command cmdCancel = new Command(Locale.get("nowplus.client.java.exit.confirm.no"), Command.CANCEL, 0);
							
						//event to fire upon confirmation
						Event confirmEvent = new Event(Event.Context.APP,Event.App.CONFIRMED_EXIT,null);
							
						//#style notification_form_base
						exitAppConfirmNotification = new ConfirmationForm(
								getModel(), getController(),
								Locale.get("nowplus.client.java.exit.confirm.text"),
								Locale.get("nowplus.client.java.exit.confirm.headline"),
								cmdOk, cmdCancel,
								confirmEvent);
					}
					 
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, exitAppConfirmNotification);
					break;

				case Event.App.CONFIRMED_EXIT:
					showExitProgress();
					getModel().exit(false);
					break;

				case Event.App.OPTIONAL_UPDATE_RECEIVED:
				case Event.App.MANDATORY_UPDATE_RECEIVED:
					//#style notification_form_base
					ConfirmationForm cuf = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.updateapplication.headline"),
							Locale.get("nowplus.client.java.updateapplication.text"),
							cmdUpdateYes, cmdExit,
							null);
					cuf.setCommandListener(this);//Override who should take care of the commands
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cuf);
					break;

				case Event.App.CONNECTION_ROAMING:
					//#style notification_form_base
					ConfirmationForm confRoaming = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.roamingwarning.title"),
							Locale.get("nowplus.client.java.roamingwarning.text"),
							cmdRoamingYes, cmdExit,
							null);
					confRoaming.setCommandListener(this);//Override who should take care of the commands
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, confRoaming);
					break;

				case Event.App.CONNECTION_UP:
                    // Hide connection down notification
                    if (connDownNotif != null && history.currentGlobalAndLocalDisplayableContainer().disp == connDownNotif) {
                        getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, connDownNotif);
                    }

                    // Hide service unavailable notification
                    if (serviceUnavailableNotification != null && history.currentGlobalAndLocalDisplayableContainer().disp == serviceUnavailableNotification) {
                        getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, serviceUnavailableNotification);
                    }

                    connDownNotif = null;
                    serviceUnavailableNotification = null;
                    break;

				case Event.App.CONNECTION_DOWN:
                	if (connDownNotif != null) {
                		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, connDownNotif);
                		connDownNotif = null;
                	}
                		
                	Command cmdConnDownCmd = cmdOk;
                	//special case, return to login screen before showing 'connection lost' notification, fixes 0016643
                	if(!getModel().isAuthenticated() ) {
                		getController().notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.NO_SERVICE, null);
                	}
                	if(!getModel().isApplicationReady()){
                		//getController().notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.NO_SERVICE, null);
                		cmdConnDownCmd = cmdConnectionDownOk;
                	}
                	//#style notification_form_base
                	connDownNotif = new NotificationForm(
                			model, controller,
                			Locale.get("nowplus.client.java.networkdown.title"),
                			Locale.get("nowplus.client.java.networkdown.text"),
                			cmdConnDownCmd,
                			-1);
                	if(cmdConnDownCmd == cmdConnectionDownOk) {
                		connDownNotif.setCommandListener(this);
                	}
                	getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, connDownNotif);
                    break;

                case Event.App.UPDATE_APPLICATION:
					//#debug debug
					System.out.println("Doing update");

					try {
						getModel().upgradeApplication();
						//force a quick exit for Huawei to avoid browser crash
						//#if (polish.identifier==Huawei/V735-ZYB)
							//#= getModel().exit(false);
						//#endif
					} 
					catch (LaunchException le) {
						//#debug error
						System.out.println("Could not launch URL" + le);
						
						//action requires app to close down
						if (LaunchException.TYPE_LAUNCH_POSTPONED == le.getType()) {
							getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
						}
					}
					break;
					
				case Event.App.SHOW_HELP:
					Identity ident = null;
					//#= ident = Identity.createUrl("http://${external.webpages.domain}/"+TextUtilities.getCurrentLanguageForLinks()+"/web/mobile/help",(Locale.get( "nowplus.client.java.contextual.menu.command.help")));

					//#ifdef polish.device.requires.polish.browser
						getController().notifyEvent(Event.Context.APP, Event.App.BROWSE_POLISH, ident);
					//#else
						getController().notifyEvent(Event.Context.APP, Event.App.BROWSE, ident);
					//#endif
					
					break;
				
				//#ifdef polish.device.requires.polish.browser
				case Event.App.BROWSE_POLISH:
					//#style htmlform
					Form form = new Form(null);
					form.setTitle(new TitleBarItem(((Identity)data).getName(),model));
					xPolishBrowser htmlBrowser = null;

					if (htmlBrowser == null) {
						// style htmlBrowser
						htmlBrowser = new xPolishBrowser();
						//taghandler removing <a> tags
						new ATagHandler(htmlBrowser.getTagHandler("a")).register(htmlBrowser);
						new ATagHandler(htmlBrowser.getTagHandler("title")).register2(htmlBrowser);
						//Custom listener controlling NotificationForm
						htmlBrowser.setBrowserListener(new BrowserListener(model, getController(),form));
						form.append( htmlBrowser );
					}
					else {
						htmlBrowser.clear();
						htmlBrowser.releaseResources();
					}

					htmlBrowser.go( ((Identity)data).getUrl() );
					form.addCommand( cmdBrowserBack );
					form.setCommandListener(this);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, form);
					break;
				//#endif
					
				case Event.App.BROWSE:
					//invoke confirmation
					Command cmdOkBrowse = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
					Command cmdCancelBrowse = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);

					//event to fire upon confirmation
					Event confirmEventBrowse = new Event(Event.Context.APP,Event.App.CONFIRMED_BROWSE,data);

					//#style notification_form_base
					ConfirmationForm cfBrowse = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.browse.confirm.headline"),
							Locale.get("nowplus.client.java.browse.confirm.text"),
							cmdOkBrowse, cmdCancelBrowse,
							confirmEventBrowse);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cfBrowse);
					break;
					
				case Event.App.CONFIRMED_BROWSE:
					try {
						if(data != null)
							if(data instanceof Identity)
								getModel().launch(((Identity)data).getChannel(Channel.TYPE_BROWSE));
							else
							if(data instanceof Channel)
								getModel().launch((Channel)data);
					} 
					catch (LaunchException le) {
						//#debug error
						System.out.println("Could not launch URL" + le);
						
						//action requires app to close down
						if(LaunchException.TYPE_LAUNCH_POSTPONED == le.getType())
							getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
					}
					break;
					
			//#if polish.blackberry && add.switch.application.form
				case Event.App.SHOW_LIST_SWITCH_APPS:
					//#style contextualmenu_base_form
					SwitchApplicationsListForm switchAppsContextualmenu = new SwitchApplicationsListForm(getController());
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, switchAppsContextualmenu);
					break;
			//#endif
			}
		}
	}
	
	/**
	 * 
	 */
	private void showExitProgress()
	{
		//#style notification_form_progress
		NotificationForm exitNotification = new NotificationForm(
				model, controller, 
				Locale.get("nowplus.client.java.exit.notification.headline"),
				Locale.get("nowplus.client.java.exit.notification.text"),
				null,
				0, true, ProgressIndicatorItem.PROGRESS_INFINITE
				);
		//Should no be possible to cancel this process, hence remove commands
		exitNotification.removeAllCommands();
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, exitNotification);
	}

	//#mdebug error
	public String toString()
	{
		return "AppController[]";
	}
    //#enddebug

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d)
	{
		if (cmdConnectionDownOk == c) {
			//getController().notifyEvent(Event.Context.AUTHENTICATION,  Event.Authentication.FIRST_LOGIN, null);
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, d);
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT, null);
		}
		if (cmdUpdateYes == c) {
			getController().notifyEvent(Event.Context.APP, Event.App.UPDATE_APPLICATION, null);
		}
		else if (cmdRoamingYes == c) {
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, d);
			getModel().acceptRoaming();
		}
		else if (cmdExit == c) {
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT, null);
		}
		//#if polish.device.requires.polish.browser
		else if (cmdBrowserBack == c) {
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, d);
		}
		//#endif
	}
}
