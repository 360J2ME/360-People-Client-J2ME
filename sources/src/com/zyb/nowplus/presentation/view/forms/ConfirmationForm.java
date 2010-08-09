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
package com.zyb.nowplus.presentation.view.forms;

import javax.microedition.lcdui.Item;

import com.zyb.nowplus.business.Model;

//#if polish.blackberry
import com.zyb.nowplus.presentation.UiFactory;
//#endif

import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * Implement traits common to all confirmations forms.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ConfirmationForm extends NotificationForm
{
	/**
	 * The event that should be kicked if confirmed or canceled
	 */
	protected Event okEvent, cancelEvent;
	
	/**
	 * Confirmation command
	 */
	protected Command cancel;
	
	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent) 
	{
		this(model, controller, title, text, ok, cancel, okEvent, null, null);
	}

	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent, Style style) 
	{
		this(model, controller, title, text, ok, cancel, okEvent, null, style);
	}
	
	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent, Event cancelEvent) 
	{
		this(model, controller, title, text, ok, cancel, okEvent, cancelEvent, null);
	}
	
	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent, Event cancelEvent, Style style) 
	{
		super(model, controller, title, text, null, -1, style);
		
		this.okEvent = okEvent;
		this.cancelEvent = cancelEvent;
		
		this.ok = ok;
		this.cancel = cancel;

		this.removeAllCommands();
		
		//#if not polish.blackberry
			if(null != ok)
				this.addCommand(ok);
			if(null != cancel)
				this.addCommand(cancel);
		//#endif
		
	}
	
	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent, Event cancelEvent, byte progressType)
	{
		this(model, controller, title, text, ok, cancel, okEvent, cancelEvent, progressType, null);
	}

	public ConfirmationForm(Model model, Controller controller, String title, String text, Command  ok, Command  cancel, Event okEvent, Event cancelEvent, byte progressType, Style style)
	{
		super(model, controller, title, text, null, -1, true, progressType,
				style);

		this.okEvent = okEvent;
		this.cancelEvent = cancelEvent;

		this.ok = ok;
		this.cancel = cancel;

		this.removeAllCommands();

		//#if not polish.blackberry
		if (null != ok)
			this.addCommand(ok);
		if (null != cancel)
			this.addCommand(cancel);
		//#endif
	}
	
	/**
	 * update missing/existed cancel cmd and event, e.g. add new command for dismissing notification when error met 
	 */
	public void setCancelCmdAndEvent(Command _cancelCmd, Event _cancelEvent)
	{
		cancelEvent = _cancelEvent;
		cancel = _cancelCmd;
		
		//#if polish.blackberry
			if(cancelEvent!=null)//show cmd button in notification for accepting user input event
				setShownCancelCmdButton(true);
		//#else
			if(cancelEvent!=null&&this.cancel!=null)
			{
				removeCommand(cancel);
				addCommand(cancel);
			}
		//#endif
			
	}
	    
    //#if polish.blackberry
	private boolean isShownCancelCmdButton;
	public void setShownCancelCmdButton(boolean _isShownCancelCmdButton) 
	{
		isShownCancelCmdButton=_isShownCancelCmdButton;
	}
	
	public void createButton(Container body) {
		if(ok==null&&cancel==null) {
			return;
		}
		
		//#style blackberry_command_buttons_container
		this.buttonContainer = new Container(false);
		this.buttonContainer.setAppearanceMode(Item.PLAIN);
		
		if(ok !=null)
		{
			//#style blackberry_command_button_base
			okButton = UiFactory.createButtonItem(null, ok.getLabel(), (de.enough.polish.ui.Command) ok, null, null);
			buttonContainer.add(okButton);
			
			removeCommand(ok);
		}
		
		if(isShownCancelCmdButton && cancel !=null)
		{
			//#style blackberry_command_button_base
			StringItem cancelButton = UiFactory.createButtonItem(null, cancel.getLabel(), (de.enough.polish.ui.Command) cancel, null, null);
			buttonContainer.add(cancelButton);
			
			removeCommand(cancel);
		}
		
		body.add(this.buttonContainer);
	}
    //#endif 
	
	/*
	 * @see com.zyb.nowplus.presentation.view.forms.NotificationForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable d)
	{
		if(cmd == ok)
		{
			//pass ok event to controller
			if(null != okEvent)
				getController().notify(okEvent);
			
			//dismiss this notification //TODO debug and clear bug #0016958: Chat crashes device
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, this);
		}
		else
		if(cmd == cancel)
		{
			//pass cancel event to controller
			if(null != cancelEvent)
				getController().notify(cancelEvent);
			
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, this);
		}
		else
			super.commandAction(cmd, d);
	}
	
	//#if polish.blackberry
	/*
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode)
	{
		if (net.rim.device.api.ui.Keypad.key(keyCode) == net.rim.device.api.ui.Keypad.KEY_ESCAPE)
		{
			if(cancel!=null&& getCommandListener()!=null)//avoiding null exception thrown caused crash bug for blackberry
           	 getCommandListener().commandAction(cancel, this);
		}
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
