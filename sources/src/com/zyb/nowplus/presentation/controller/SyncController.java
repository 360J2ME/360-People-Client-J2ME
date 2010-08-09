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
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Display;
import de.enough.polish.util.DeviceControl;
import de.enough.polish.util.Locale;

/**
 * The sync controller
 * @author Jens Vesti
 *
 */
public class SyncController extends ContextController 
{
    private static final int DEFAULT_PROGRESS_TIME_SMOOTH = 120;//This can and should be adjusted to reflect the average time it takes for a batch of contacts to be sent to the server, or received 

    private static final Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
    private static final Command cmdCancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
    private static final Command cmdExit = new Command(Locale.get("nowplus.client.java.command.exit"), Command.EXIT, 0);
    
    //private static final Event confirmEvent = new Event(Event.Context.NAVIGATION,Event.Navigation.BACK_CHECKPOINT,null);
    private static final Event cancelImportEvent = new Event(Event.Context.SYNC, Event.Sync.CANCEL_IMPORT, null);
    
    private ConfirmationForm syncNotification;
    private ConfirmationForm syncConfirmation;
    private Thread forceActiveScreen;
    private volatile boolean isActiveScreenForced = false;
	private ConfirmationForm sabCabSyncNotification;
	private long lastContactsSyncEventReceived;

    /**
	 * 
	 * @param model
	 * @param controller
	 * @param history
	 */
	public SyncController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.SYNC;
	}

	/* 
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		//#debug debug
		System.out.println("context: " + context + ", event:" + event);

		if(context == getContext())
		{
			switch(event)
			{
		//#if activate.progress.debugging
			case Event.Sync.UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_SEND:
				if (data instanceof String)
					setSyncNotiSentDescription((String) data);
				break;
			
			case Event.Sync.UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_RECEIVED:
				if (data instanceof String)
					setSyncNotiReceivedDescription((String) data);
				break;
		//#endif
				
			case Event.Sync.CONTACTS_SEND:
			case Event.Sync.CONTACTS_RECEIVED:
			
				try
				{
					//We want to show smooth progress bar, so we capture the round trip time for a batch of contacts to be sent/received. This gives a more accurate and realistic feeling of progress.
					int timeDiffSinceLastEvent = lastContactsSyncEventReceived == 0 ? DEFAULT_PROGRESS_TIME_SMOOTH : (int)(System.currentTimeMillis() - lastContactsSyncEventReceived);
					lastContactsSyncEventReceived = System.currentTimeMillis();
					
					//Fetching and setting the percentage/progress of current event for use in progress indicator
					int progress = 0;

					if (data instanceof Integer)
						progress = ((Integer) data).intValue();
					
					if (sabCabSyncNotification == null && progress != 100)
					{
						Event confirmEvent = new Event(Event.Context.APP,Event.App.CONFIRMED_EXIT,null);
						//#style notification_form_progress_startup
						sabCabSyncNotification = new ConfirmationForm(
			                    model, controller,
			                    Locale.get("nowplus.client.java.cabsabsync.popup.title"),
			                    (event==Event.Sync.CONTACTS_SEND?Locale.get("nowplus.client.java.cabsabsync.popup.sending"):Locale.get("nowplus.client.java.cabsabsync.popup.receiving")),
			                    cmdExit,
			                    null,
			                    confirmEvent,
			                    null,
			                    ProgressIndicatorItem.PROGRESS_INCREMENTAL
			                    );
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, sabCabSyncNotification);
					}

					//If it is not null we need to update it
					if (sabCabSyncNotification != null)
					{
						//If progress is 100% we are done and can dismiss the progress indicator
						if (progress == 100)
						{
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, sabCabSyncNotification);
							sabCabSyncNotification = null;
							lastContactsSyncEventReceived = 0;
						}
						else
						{
							sabCabSyncNotification.setProgress(progress, timeDiffSinceLastEvent / 1000);
							
							//Text may have changed, so we set it again
							sabCabSyncNotification.setText(event == Event.Sync.CONTACTS_SEND ? Locale.get("nowplus.client.java.cabsabsync.popup.sending")
																							 : Locale.get("nowplus.client.java.cabsabsync.popup.receiving"));
							
						//#if activate.progress.debugging
							//displaying both sending and receiving info.
							sabCabSyncNotification.setText(Locale.get("nowplus.client.java.cabsabsync.popup.sending") + "\t"
									+ getSyncNotiSentDescription() + "\t\n"
									+ Locale.get("nowplus.client.java.cabsabsync.popup.receiving") + " \t\n"
									+ getSyncNotiReceivedDescription() + "\n"+progress+"%");
							
							clearTexts();//always use real-time info. for sabCabSync Notification
						//#endif
						}
					}
				}
				catch (Exception e) {
					//#debug error
					System.out.println(this + " thrown exception: " + e.toString());
				}
					
				break;			

			//#if activate.progress.debugging
			//#	case Event.Sync.CONTACTS_NOT_SEND:
			//#		if(sabCabSyncNotification != null)
			//#					{
			//#						setSyncNotiSentDescription(Locale.get("nowplus.client.java.cabsabsync.popup.sending.failed"));
			//#						sabCabSyncNotification.setCancelCmdAndEvent(cancel,new Event(Event.Context.SYNC, Event.Sync.DISMISS_CAB_SAB_SYNC_NOTIFICATION,null));
			//#						sabCabSyncNotification.repaint();
			//#					}
			//#					break;
			//#				case Event.Sync.CONTACTS_NOT_RECEIVED:
			//#					if(sabCabSyncNotification != null)
			//#					{
			//#						setSyncNotiReceivedDescription(Locale.get("nowplus.client.java.cabsabsync.popup.receiving.failed"));
			//#						sabCabSyncNotification.setCancelCmdAndEvent(cancel,new Event(Event.Context.SYNC, Event.Sync.DISMISS_CAB_SAB_SYNC_NOTIFICATION,null));
			//#						sabCabSyncNotification.repaint();
			//#					}
			//#else
				case Event.Sync.CONTACTS_NOT_SEND:
				case Event.Sync.CONTACTS_NOT_RECEIVED:
					if(sabCabSyncNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, sabCabSyncNotification);
						sabCabSyncNotification = null;
						lastContactsSyncEventReceived = 0;
					}

					//#style notification_form_base
					sabCabSyncNotification = new ConfirmationForm(
		                    model, controller,
		                    Locale.get("nowplus.client.java.cabsabsync.popup.title"),
		                    (event==Event.Sync.CONTACTS_NOT_SEND?Locale.get("nowplus.client.java.cabsabsync.popup.sending.failed"):Locale.get("nowplus.client.java.cabsabsync.popup.receiving.failed")),
		                    cmdOk,
		                    null,
		                    null		                    
		                    );
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, sabCabSyncNotification);
             //#endif
					break;			
					
				case Event.Sync.START:
				case Event.Sync.RESUME:
				case Event.Sync.SYNCING:
					if(syncNotification != null)
						if(data instanceof String)
							syncNotification.setText((String)data);
					
				//#if polish.vendor.sony-ericsson
					startForceActiveScreen(Model.FEEDBACK_NAB_INTERACTION | Model.FEEDBACK_SAB_INTERACTION);
				//#endif
					
					break;

				case Event.Sync.OUT_ADD:
                    if(syncNotification != null && data != null && data instanceof String)
                    {
                		String suffix = ((String)data).trim();
                 		syncNotification.setText(Locale.get("nowplus.client.java.sync.popup.text", suffix));
                    }
                    break;

                case Event.Sync.START_IMPORT:
                    //#style notification_form_progress_startup
                    syncNotification = new ConfirmationForm(
                            model, controller,
                            Locale.get("nowplus.client.java.sync.popup.title"),
                            " ",
                            null,
                            cmdCancel,
                            null,
                            cancelImportEvent,
                            ProgressIndicatorItem.PROGRESS_INFINITE
                            );
                    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncNotification);
                    
					if(data instanceof String)
						syncNotification.setText((String)data);
				
				//#if polish.vendor.sony-ericsson
					startForceActiveScreen(Model.FEEDBACK_NAB_INTERACTION | Model.FEEDBACK_SAB_INTERACTION);
				//#endif
					
					break;

                case Event.Sync.CANCEL_IMPORT:
                    model.cancelImportFromNab();
                    break;

				case Event.Sync.CANCELLING:
                    if(syncNotification != null)
                    {
                        syncNotification.setText(Locale.get("nowplus.client.java.sync.cancel.popup.text"));
                    }
                    break;

				case Event.Sync.SUCCESSFULL:
					if(syncNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, syncNotification);
						syncNotification = null;
					}
					break;
					
                case Event.Sync.CANCELLED:
				case Event.Sync.FAILED:
				case Event.Sync.SUSPEND:
					if(syncNotification != null)
					{
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, syncNotification);
						syncNotification = null;
					}
					break;

				case Event.Sync.USER_DISALLOWED_OPEN:
                    //#style notification_form_base
                    syncConfirmation = new ConfirmationForm(
                       model, getController(),
                       Locale.get("nowplus.client.java.sync.user.disallowed.open.title"),
                       Locale.get("nowplus.client.java.sync.user.disallowed.open.text"),
                       cmdOk, null,
                       null);
                    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncConfirmation);
					break;

				case Event.Sync.USER_DISALLOWED_CONTENT_READ:
                    //#style notification_form_base
                    syncConfirmation = new ConfirmationForm(
                       model, getController(),
                       Locale.get("nowplus.client.java.sync.user.disallowed.read.title"),
                       Locale.get("nowplus.client.java.sync.user.disallowed.read.text"),
                       cmdOk, null,
                       null);
                    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncConfirmation);
					break;

				case Event.Sync.USER_DISALLOWED_CONTENT_WRITE:
                    //#style notification_form_base
                    syncConfirmation = new ConfirmationForm(
                       model, getController(),
                       Locale.get("nowplus.client.java.sync.user.disallowed.write.title"),
                       Locale.get("nowplus.client.java.sync.user.disallowed.write.text"),
                       cmdOk, null,
                       null);
                    getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncConfirmation);
					break;

				case Event.Sync.USER_DISALLOWED_ALLOCATE_NEW:
					//#style notification_form_base
					syncConfirmation = new ConfirmationForm(
							model, getController(),
							Locale.get("nowplus.client.java.sync.user.disallowed.create.title"),
							Locale.get("nowplus.client.java.sync.user.disallowed.create.text"),
							cmdOk, null,
							null);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncConfirmation);
					break;

				case Event.Sync.USER_DISALLOWED_CONTENT_DELETE:
					//#style notification_form_base
					syncConfirmation = new ConfirmationForm(
							model, getController(),
							Locale.get("nowplus.client.java.sync.user.disallowed.delete.title"),
							Locale.get("nowplus.client.java.sync.user.disallowed.delete.text"),
							cmdOk, null,
							null);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, syncConfirmation);
					break;
			}
		}
	}
	

	
	/**
	 * Activate seperately running thread that forces backlight/active mode and
	 * supresses sleep/idle mode.
	 * <p>
	 * Bitmask options:
	 * Model.FEEDBACK_NAB_INTERACTION
	 * Model.FEEDBACK_SAB_INTERACTION
	 * 
	 * @param syncBitMask 
	 */
//#if polish.vendor.sony-ericsson
	private synchronized void startForceActiveScreen(final int syncBitMask)
	{
		if(!isActiveScreenForced)
		{
			isActiveScreenForced = true;
			forceActiveScreen = new Thread()
			{
				public void run() 
				{
					while((model.getFeedback() & syncBitMask) != 0)
					{
						try
						{
							//#debug debug
							System.out.println("Forcing Active Screen");
							
							//hack to keep screen active
							Display.getInstance().setCurrent(Display.getInstance().getCurrent());
							DeviceControl.lightOn();
							
							Thread.sleep(1000);
						}
						catch (Exception e)
						{
						}
					}
					
//					DeviceControl.lightOff(); //no need, light shuts off after 10 sec
					isActiveScreenForced = false;
				}
			};
			forceActiveScreen.start();
		}
	}
//#endif
	
	//#mdebug error
	public String toString()
	{
		return "SyncController[]";
	}
    //#enddebug

	
	//text displayed as content of sabCabSyncNotification when app sending requests of updating contacts
	private String sabCabSync_CONTACTS_SEND_Text="";
	
	//text displayed as content of sabCabSyncNotification when app receiving responses of updated contacts
	private String sabCabSync_CONTACTS_RECEIVED_Text="";
	
	private String getSyncNotiSentDescription()
	{
		if(sabCabSync_CONTACTS_SEND_Text==null)
			sabCabSync_CONTACTS_SEND_Text="";
			
		return sabCabSync_CONTACTS_SEND_Text;
	}
	
	private String getSyncNotiReceivedDescription()
	{
		if(sabCabSync_CONTACTS_RECEIVED_Text==null)
			sabCabSync_CONTACTS_RECEIVED_Text="";
		
		return sabCabSync_CONTACTS_RECEIVED_Text;
	}
	
	public void setSyncNotiSentDescription(String text)
	{
		sabCabSync_CONTACTS_SEND_Text=(text!=null)?text:"";
		
		if(sabCabSyncNotification!=null)
			sabCabSyncNotification.repaint();
	}
	
	public void setSyncNotiReceivedDescription(String text)
	{
		sabCabSync_CONTACTS_RECEIVED_Text=(text!=null)?text:"";
		
		if(sabCabSyncNotification!=null)
			sabCabSyncNotification.repaint();
	}
	
	private void clearTexts()
	{
		setSyncNotiSentDescription("");
		setSyncNotiReceivedDescription("");
	}
}
