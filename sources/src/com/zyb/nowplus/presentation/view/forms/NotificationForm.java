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

import java.util.Timer;
import java.util.TimerTask;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import com.zyb.nowplus.presentation.UiFactory;
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * Implement traits common to all notification forms.
 * <p>
 * Will automatically disappear if a timeout > 0 is set.
 * 
 * //TODO: Use Polish Alert instead and overwrite and use Alter.DISMISS_COMMAND in ConfirmationForm
 * to obtain same functionality as now. Will make this class unnecessary
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class NotificationForm extends BaseForm
{
	/**
	 * Default command common to all notification screens
	 */
	public final static Command  cmdCancel;
	
	//#if polish.blackberry
	protected Container buttonContainer;
	private boolean isHasDefaultCmds;
	protected StringItem okButton;
	
	//command for forcing to exit app 
	private StringItem cmdForceExitButton;
	protected final static Command cmdExit = new Command(Locale.get( "nowplus.client.java.command.exit"),Command.EXIT,10);
	//#endif
	
	static
	{
		cmdCancel = new Command(Locale.get("polish.command.cancel"), Command.BACK, 0);
	}
	
	/**
	 * Strings Items to be displayed by notification
	 */
	protected String title, text;
	
	protected Container body;
	
	/**
	 * Strings Items to be displayed by notification
	 */
	protected StringItem titleItem, textItem;
	
	/**
	 * Icon for notification headline
	 */
	protected IconItem icon;
	
	/**
	 * custom styles for elements
	 */
	protected Style bodyStyle=null, headlineStyle=null, titleStyle=null, textStyle=null, iconStyle=null;
	
	/**
	 * Time manager
	 */
	protected Timer timer;

	protected ProgressIndicatorItem progress;
	
	/**
	 * Container for headline
	 */
	protected Container headline;
	
	/**
	 * Notification command
	 */
	protected Command ok;

	
	/**
	 * Flag to check if the form has been dismissed. 
	 * Used by the controller to determine if it should be displayed
	 */
	protected boolean isDismissed = false;

	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut) 
	{
		this(model, controller, title, text, ok, timeOut, true, null);
	}

	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut, Style style) 
	{
		this(model, controller, title, text,  ok, timeOut, true, style); 
	}
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param title
	 * @param text
	 * @param timeOut
	 */
	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut, boolean dismissAble) 
	{
		this(model, controller, title, text, ok, timeOut, dismissAble, null);
	}

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param title
	 * @param text
	 * @param timeOut
	 * @param style
	 */
	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut, boolean dismissAble, Style style) 
	{
		super(model, controller, null, style);
				
		initStyle(style);
		
		this.text = text;
		
		this.title = title;
		
		if(null == ok)
		{
			if (dismissAble) 
			{
				//#if polish.blackberry
				
					isHasDefaultCmds=true;
					okButton=null;
					
				//#else
					
					this.addCommand(cmdCancel); // add default
				
				//#endif
			}
			//#if polish.blackberry
			else
			{
				isHasDefaultCmds=false;
				okButton=null;
				
				//#style blackberry_command_button_base
				cmdForceExitButton= UiFactory.createButtonItem(null, cmdExit.getLabel(), (de.enough.polish.ui.Command) cmdExit, null, null  );
			}
			//#endif
		}
		else 
		{
			//#if polish.blackberry

				this.ok = ok;
				isHasDefaultCmds=true;
				//#style blackberry_command_button_base
				okButton = UiFactory.createButtonItem(null, this.ok.getLabel(), (de.enough.polish.ui.Command) this.ok, null, null );
				
			//#else
				
				this.addCommand(this.ok = ok);
				
			//#endif
		}
		
		//Init timer if timeout > 0
		if(timeOut > 0)
		{
			this.timer = new Timer();
			TimerTask tt = new TimerTask()
			{
				public void run()
				{
					commandAction(cmdCancel, NotificationForm.this);
				}
			};
			this.timer.schedule(tt, timeOut);    
		}
	}

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param title
	 * @param text
	 * @param timeOut
	 * @param progressType is the type of progress to append to the notification. Choice is between ProgressIndicatorItem.PROGRESS_INFINITE and PROGRESS_INCREMENTAL
	 */
	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut, boolean dismissAble, byte progressType) 
	{
		this(model, controller, title, text, ok, timeOut, dismissAble, progressType, null);
	}

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param title
	 * @param text
	 * @param timeOut
	 * @param progressType is the type of progress to append to the notification. Choice is between ProgressIndicatorItem.PROGRESS_INFINITE and PROGRESS_INCREMENTAL
	 * @param style
	 */
	public NotificationForm(Model model, Controller controller, String title, String text, Command  ok, int timeOut, boolean dismissAble, byte progressType, Style style) 
	{
		this(model, controller, title, text, ok, timeOut, dismissAble, style);
		
		//#style .progress_infinite
		progress = new ProgressIndicatorItem(progressType);
	}	
	
	/**
	 * @see setProgress(int percentage, int smoothOverSeconds)
	 */
	public void setProgress(int percentage)
	{
		setProgress(percentage, 0);
	}

	/**
	 * @param percentage the percentage the progressbar should be set to
	 * @smoothOverSeconds the seconds over which the progress bar should increment to give an indication of something happening
	 */
	public void setProgress(int percentage, int smoothOverSeconds)
	{
		if(progress != null)
		{
			progress.setPercentage(percentage, smoothOverSeconds);
			this.setLastInteractionTime(System.currentTimeMillis());//A hack to ensure the progress bar keeps animating
		}
	}

	protected void createContent()
	{		
		// disabling scrolling
		Container container = UiAccess.getScreenContainer(this);
		container.setScrollHeight(-1);
		
		//init body
		this.body = new Container(false, this.bodyStyle);
		
		//init wrappers
		this.headline = new Container(false, headlineStyle);
		
		//init content
		this.titleItem = new StringItem(null, title, titleStyle);
		this.textItem = new StringItem(null, text, textStyle);
		this.icon = new IconItem(null, null, iconStyle);

		//fill headline
		this.headline.add(this.icon);
		this.headline.add(this.titleItem);
		
		//append to form
		this.body.add(this.headline);
		this.body.add(this.textItem);
		
		if (progress != null) 
		{
			this.body.add(this.progress);
			this.progress.setAppearanceMode(Item.PLAIN);
		}
		
		this.body.setAppearanceMode(Item.PLAIN);
		this.headline.setAppearanceMode(Item.PLAIN);
		this.icon.setAppearanceMode(Item.PLAIN);
		this.titleItem.setAppearanceMode(Item.PLAIN);
		this.textItem.setAppearanceMode(Item.PLAIN);
		
		createButton(this.body);
		
		append(this.body);
	}
	
	public void createButton(Container body) {
		//#if polish.blackberry
		
		if(isHasDefaultCmds&&(okButton!=null)) {
			//#style blackberry_command_buttons_container
			this.buttonContainer = new Container(false);
			this.buttonContainer.setAppearanceMode(Item.PLAIN);
			
			this.buttonContainer.add(okButton);
			this.body.add(buttonContainer);
		}
		else if(cmdForceExitButton!=null) {
			//#style blackberry_command_buttons_container
			this.buttonContainer = new Container(false);
			this.buttonContainer.setAppearanceMode(Item.PLAIN);
			
			this.buttonContainer.add(cmdForceExitButton);
			this.body.add(buttonContainer);
		}
		else {
			this.buttonContainer=null;
			return;
		}
		
		//#endif
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable d) 
	{
		if(cmd == cmdCancel || cmd == this.ok)
		{
			cancelTimer();
			
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, this);
			
			return;
		}
		//#if polish.blackberry
		else if(cmd==cmdExit)
		{
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT, this);
			
		   return;
		}
		//#endif
		
//		super.commandAction(cmd, d);
	}
	
	/**
	 * Cancels timer if set
	 */
	private void cancelTimer()
	{
		if(null != this.timer)
		{
			this.timer.cancel();
			this.timer = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#releaseResources()
	 */
	public void releaseResources()
	{
		//ensure that timer is cancled if command 'cancel' is never called
		cancelTimer();
		
		super.releaseResources();
	}
	
	public void destroy() {
		// don't destroy the NotificationForm because it disturbs the screen-change-animation
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#getContext()
	 */
	public byte getContext() {
		return Event.Context.CONFIRM;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#setStyle(de.enough.polish.ui.Style)
	 */
	protected void initStyle(Style style)
	{
		super.setStyle(style);
		
		//#ifdef polish.css.body-style
		if(style.getObjectProperty("body-style") != null)
		{
			this.bodyStyle = (Style)style.getObjectProperty("body-style");
		}
		else
		//#endif
			this.bodyStyle = style;

		//#ifdef polish.css.headline-style
		if(style.getObjectProperty("headline-style") != null)
		{
			this.headlineStyle = (Style)style.getObjectProperty("headline-style");
		}
		else
		//#endif
			this.headlineStyle = style;

		//#ifdef polish.css.title-style
		if(style.getObjectProperty("title-style") != null)
			this.titleStyle = (Style)style.getObjectProperty("title-style");
		else
		//#endif
			this.titleStyle = style;
		
		//#ifdef polish.css.text-style
		if(style.getObjectProperty("text-style") != null)
			this.textStyle = (Style)style.getObjectProperty("text-style");
		else
		//#endif
			this.textStyle = style;
		
		//#ifdef polish.css.icon-style
		if(style.getObjectProperty("icon-style") != null)
			this.iconStyle = (Style)style.getObjectProperty("icon-style");
		else
		//#endif
			this.iconStyle = style;		
	}
	
	public void setTitle(Item item) {
		if(null != item && item instanceof StringItem)
			this.titleItem = (StringItem)item;
	}
	
	public void setText(Item item) {
		if(null != item && item instanceof StringItem)
			this.textItem = (StringItem)item;
	}

	public void setText(String txt) {
		if(null != this.textItem)
			this.textItem.setText(txt);
	}

	
	public void setIcon(Item item) {
		if(null != item && item instanceof IconItem)
			this.icon = (IconItem)item;
	}
	
	/**
	 * @return the isDismissed flag
	 */
	public boolean isDismissed() {
		return isDismissed;
	}

	/**
	 * @param isDismissed the isDismissed value to set
	 */
	public void setDismissed(boolean isDismissed) {
		//#debug debug
		System.out.println("NotificationForm dismissed: " + isDismissed);
		this.isDismissed = isDismissed;
	}
	
	protected boolean handleKeyReleased(int keyCode, int gameAction) 
	{
		//surpress clear key to avoid cancel command
		
		//#ifdef polish.key.clearkey:defined
		//#= if(keyCode == ${polish.key.clearkey})
		//# return true;
		//#= else
		//#endif
		
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	//#if polish.fix_uncaught_startup_exception
	public void create()
	{
		synchronized (this)
		{
			if(!this.isContentCreated)
			{
				createContent();
				
				this.isContentCreated = true;
			}
		}
	}
	//#endif
	
	//#if polish.blackberry
	/*
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode)
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
		{
			if(isHasDefaultCmds ||this.ok!=null)
				commandAction(cmdCancel, this);
			else
				commandAction(cmdExit,this);
		}
		else
			super.keyPressed(keyCode);
	}
	//#endif
	
	//#mdebug error
	public String toString()
	{
		return "NotificationForm[]";
	}
	//#enddebug	
}
