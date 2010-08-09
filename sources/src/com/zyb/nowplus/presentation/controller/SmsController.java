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
//#condition polish.device.supports.nativesms == false
/**
 * 
 */
package com.zyb.nowplus.presentation.controller;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.BaseTabForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.forms.SmsForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.Locale;

/**
 * Controller class for sending sms when there is no native sms in platformRequest
 * It handles commands from SmsForm and uses WMA to send sms-es. 
 * 
 * @author marek.defecinski@mobica.com
 *
 */
public class SmsController extends ContextController implements CommandListener
{
	public SmsController(Model model, Controller controller,
			ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}

	public byte getContext() {
		return Event.Context.SMS_EDITOR;
	}
		
	public void handleEvent(byte context, int event, Object data) {
		//#debug debug
		System.out.println("handleEvent" + context + " " + event + " " + data);
		
		// When you choose sms number
		try {
			if (context == getContext()) {
				switch(event) {
				case Event.SmsEditor.OPEN:
				if (data instanceof Channel) {
					//#style sms_form
					SmsForm smsForm = new SmsForm(getModel(), getController(),
							null, (Channel) data);
					smsForm.setCommandListener(this);

					getController().notifyEvent(Event.Context.NAVIGATION,
							Event.Navigation.NEXT_CHECKPOINT, smsForm);
				}
				// When you want send sms in primary number.
				else if (data instanceof ManagedProfile) {
					//#style sms_form
					SmsForm smsForm = new SmsForm(getModel(), getController(),
							(ManagedProfile) data);
					smsForm.setCommandListener(this);
					getController().notifyEvent(Event.Context.NAVIGATION,
							Event.Navigation.NEXT_CHECKPOINT, smsForm);
				}
				break;
				case Event.SmsEditor.SEND_OK:
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, data);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
				break;
				case Event.SmsEditor.SEND_FAILED:
					//#style notification_form_base
			    	NotificationForm errorNotification = new NotificationForm(model, controller,  Locale.get("nowplus.client.java.sms.error.notification.headline"),
						    Locale.get("nowplus.client.java.sms.error.notification.message"), BaseTabForm.cmdBack, 0, true);

					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, data);
			    	getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, errorNotification);
			    	break;
				case Event.SmsEditor.SEND_STARTED:
					controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, data);
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			//#debug error
			System.out.println("IllegalArgumentException " + e.getMessage());
		}
	}

	
	public void commandAction(Command command, Displayable displayable) 
	{
		//#debug debug
		System.out.println("command:"+command.getLabel());
		if (displayable instanceof SmsForm){
			if(command == SmsForm.cmdOptions){
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, ((SmsForm)displayable).getProfile());
			}else 
			if(command == SmsForm.cmdSubmit) {
				final String number = ((SmsForm)displayable).getNumber();
				final String message = ((SmsForm)displayable).getMessage();
				if (message==null || message.length()==0){
					return;
				}

				sendSms(TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, number), message);
			} else if(command == SmsForm.cmdBack) {
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
			}
		}
	}
	
	public void sendSms(final String number, final String message) {
		//#style notification_form_progress
		NotificationForm sendingNotification = new NotificationForm(model, controller,  Locale.get("nowplus.client.java.sms.notification.headline"),
			    null, null, 0, false, ProgressIndicatorItem.PROGRESS_INFINITE);
		
		getModel().sendSms(number, message, sendingNotification);
	}
}
