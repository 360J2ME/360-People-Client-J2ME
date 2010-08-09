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
//#condition not polish.signupdisable
/**
 * 
 */
package com.zyb.nowplus.presentation.view.forms;

import java.util.Date;

import javax.microedition.lcdui.Graphics;

//#if polish.blackberry
//import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Keypad;
import com.zyb.util.event.Event;
//#endif

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.DeviceControl;
import de.enough.polish.util.Locale;

/**
 * @author Jens Vesti
 *
 */
public class SignupForm extends BaseFramedForm
{
	public static final byte WELCOME = 0;
	public static final byte ENTER_DATA = 1;
	public static final byte CONGRATULATIONS = 2;
	public static final byte REREQUEST = 3;
	private byte state = WELCOME;

	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;

	private CommandListener commandListener;
	
	//Enter details items
	private TextField username;
	private TextField password;
	private DateField dob;
	private TextField phonenumber;
	private TextField emailTextField;
	
	private ChoiceItem agreeToTC;
	private ChoiceItem rememberMe;
	public static final byte USERNAME_FIELD = 0;
	public static final byte PASSWORD_FIELD = 1;
	public static final byte DOB_FIELD = 2;
	public static final byte PHONENUMBER_FIELD = 3;
	public static final byte TOC_FIELD = 4;
	public static final byte EMAIL_FIELD=TOC_FIELD+1;
	
	
	public static Command CMD_EXIT = new Command(Locale.get("nowplus.client.java.signup.command.exit"), Command.EXIT, 1);
	public static Command CMD_CANCEL = new Command(Locale.get("nowplus.client.java.signup.command.cancel"), Command.CANCEL, 1);
	public static Command CMD_LOGIN = new Command(Locale.get("nowplus.client.java.signup.command.select"), Command.ITEM, 1);
	public static Command CMD_SIGNUP = new Command(Locale.get("nowplus.client.java.signup.command.select"), Command.ITEM, 1);
	public static Command CMD_CONTINUE_ENTER_DATA = new Command(Locale.get("nowplus.client.java.signup.command.continue"), Command.SCREEN, 1);
	public static Command CMD_CONTINUE_REENTER_DATA = new Command(Locale.get("nowplus.client.java.signup.command.continue"), Command.SCREEN, 1);
	public static Command CMD_CONTINUE_CONGRATULATIONS = new Command(Locale.get("nowplus.client.java.signup.command.continue"), Command.SCREEN, 1);
	public static Command CMD_READTC = new Command(Locale.get("nowplus.client.java.signup.command.read"), Command.ITEM, 1);
	public static Command CMD_READPRIVACY = new Command(Locale.get("nowplus.client.java.signup.command.read"), Command.ITEM, 1);
	
	//#if polish.blackberry
	private Container buttonContainer;
	private StringItem continueButton;//for cmds of CMD_CONTINUE_ENTER_DATA or CMD_CONTINUE_REENTER_DATA or CMD_CONTINUE_CONGRATULATIONS
	//#endif
	
	
	/**
	 * @param model
	 * @param controller
	 * @param title
	 * @param style
	 */
	public SignupForm(Model model, Controller controller, String title, Style style) 
	{
		super(model, controller, title, style);
		
		//#if polish.blackberry
		if(buttonContainer==null)
		{
			//#style blackberry_command_buttons_container
			buttonContainer = new Container(false);
			buttonContainer.setAppearanceMode(Item.PLAIN);
		}
		//#endif
	}

	/**
	 * @param model
	 * @param controller
	 * @param title
	 */
	public SignupForm(Model model, Controller controller, String title) 
	{
		this(model, controller, title, null);
		
		//#if polish.blackberry
		if(buttonContainer==null)
		{
			//#style blackberry_command_buttons_container
			buttonContainer = new Container(true);
			buttonContainer.setAppearanceMode(Item.PLAIN);
		}
		//#endif
	}
	
	private static Container splashImageContainer;
	
	public void setState(byte state)
	{
		if(this.state != state)
		{
			this.state = state;
			initState();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#create()
	 */
	protected void create() 
	{	
	    synchronized (this) 
        { 
                    if(!this.isContentCreated) 
                    { 
                            super.create(); 
                             
                            initState(); //call state init as part of delayed construction routine 
                    } 
          } 


	}
	
	/**
	 * Re-initiates form contents
	 */
	protected void initState()
	{
		//fix for PBLA-795
		//closing the Virtual keyboard for new content is the same form
		//#if polish.blackberry.isTouchBuild == true
			DeviceControl.hideSoftKeyboard();
		//#endif
	
		
		//lock form during reinit
		synchronized (this)
		{
			this.deleteAll();
			
			this.removeAllCommands();
			
			//#if polish.blackberry
	        if(buttonContainer!=null)
	      	  	buttonContainer.clear();
			//#endif
			
			//ensure scrollbar visibility 
			this.scrollBarVisible = true;	
			
			//Add title
			if(state != WELCOME)
			{
				if(this.titleitem == null)
				{
					this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.signup.title"),getModel() );
					this.setTitle(this.titleitem);
				}
			}
			
			if(state == WELCOME)
			{
				//set scrollbar invisible for landing page 
				this.scrollBarVisible = false;
				
				Item item;
				
				//Remove title
				if(this.titleitem != null)
				{
					this.titleitem = null;
					this.setTitle(this.titleitem);
				}
				
				//#style .signup_splash_image_container
				 splashImageContainer = new Container(false);
				splashImageContainer.setAppearanceMode(Item.PLAIN);
				
				//#style .signup_splash_image_text_container
				Container c2 = new Container(false);
				c2.setAppearanceMode(Item.PLAIN);
				
				c2.add
				(
						//#style .signup_splash_image_text1
						new StringItem(null, Locale.get("nowplus.client.java.landingpage.tagline1") )
				);
				c2.add
				(
						//#style .signup_splash_image_text2
						new StringItem(null, Locale.get("nowplus.client.java.landingpage.tagline2") )
				);
				
				splashImageContainer.add(c2);
				this.append(splashImageContainer);
				
				//#style signup_button_item
				item = UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.signup.welcome.button.signup"), CMD_SIGNUP, this, null);
				this.append(Graphics.BOTTOM, item);
				this.setActiveFrame(Graphics.BOTTOM);
				this.focus(item);
				//#style signup_button_item
				item = UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.signup.welcome.button.login"), CMD_LOGIN, this, null);
				this.append(Graphics.BOTTOM, item);
				
				//#if !polish.blackberry
				this.addCommand(CMD_EXIT);
				//#endif
				
				requestInit();
			}
			else
			if(state == ENTER_DATA)
			{
				this.setActiveFrame(-1, true);		
				
				
				//#if polish.blackberry.isTouchBuild == false
				//#style .signup_headline
				UiFactory.createStringItem(null, Locale.get("nowplus.client.java.signup.enterdata.headline"), null, null, this);
				//#endif
				
				this.username = UiFactory.createTextField(Locale.get("nowplus.client.java.signup.enterdata.choseusername"), null, 255, TextField.NON_PREDICTIVE | TextField.INITIAL_CAPS_WORD , this);
				
				//fix for PBLA-816 Touch device: predictive text while disabling the surepress
				//#if polish.blackberry
				this.username.setNoComplexInput(true);
				//#endif
				
				UiAccess.setTextfieldHelp(this.username, Locale.get( "nowplus.client.java.loginform.username.choseusername.hint" ));
				
	            //#if using.native.textfield:defined
					this.username.setTitle(Locale.get( "nowplus.client.java.loginform.username.choseusername.hint" ));
					//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(this.username);
					//#endif
				//#endif
				this.focus(this.username);
				
				//#if polish.blackberry
				this.password = UiFactory.createTextField(Locale.get("nowplus.client.java.signup.enterdata.chosepassword"), null, 255, TextField.NON_PREDICTIVE | TextField.INITIAL_CAPS_NEVER | TextField.PASSWORD, this);				
				//#else
				this.password = UiFactory.createTextField(Locale.get("nowplus.client.java.signup.enterdata.chosepassword"), null, 255, TextField.INITIAL_CAPS_NEVER | TextField.PASSWORD , this);
				//#endif
				
				UiAccess.setTextfieldHelp(this.password, Locale.get( "nowplus.client.java.loginform.password.chosepassword.hint" ));
				
	            //#if using.native.textfield:defined
				this.password.setTitle(Locale.get( "nowplus.client.java.loginform.password.chosepassword.hint" ));
					//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(this.password);
					//#elif polish.device.textField.requires.initialCapsNeverFix:defined
						UiFactory.fixS40NativeTextField(this.password);
					//#endif
				//#endif
				
				this.dob = UiFactory.createDateField(Locale.get( "nowplus.client.java.signup.enterdata.dob" ), null, DateField.DATE, this);
				
				this.phonenumber = UiFactory.createTextField(Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber" ), TextUtilities.getCurrentHeaderPrefix(), 255, TextField.PHONENUMBER, this);
				UiAccess.setTextfieldHelp(this.phonenumber, Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber.hint" ));
	            //#if using.native.textfield:defined
				this.phonenumber.setTitle(Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber.hint" ));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(this.phonenumber);
					//#endif
				//#endif
					
					String textEmail=null;
					
					//#if polish.blackberry
						net.rim.blackberry.api.mail.Session mailSession = null;
						net.rim.blackberry.api.mail.ServiceConfiguration config = null;
		
						try
						{
							// If the various service book entries are not yet present on
							// the handheld, this call returns null.
							mailSession = net.rim.blackberry.api.mail.Session.getDefaultInstance();
							
							if (mailSession != null)
								config = mailSession.getServiceConfiguration();
							
							// Email address used by the phone
							if (config != null)
								textEmail = config.getEmailAddress();
						}
						catch(Exception e)
						{
							//#debug error
							System.out.println("Failed to get email address used by the phone:" + e);
						}
						
					//#endif
					
					/* previously had the type TextField.EMAILADDR which cause more
					 * problems than solving it 
					 * see http://mantis.next-vodafone.com/mantis/view.php?id=4679
					 */
						emailTextField= UiFactory.createTextField(Locale.get("nowplus.client.java.signup.enterdata.emailfield.title"),textEmail, 256, TextField.ANY | TextField.INITIAL_CAPS_NEVER,this);
						
						emailTextField.setHelpText(Locale.get("nowplus.client.java.signup.enterdata.emailfield.hint"));
					
					//#if polish.blackberry
						//#style ui_factory_textfield_helpfont
					emailTextField.setHelpStyle();
					//#endif
						
			        //#if using.native.textfield:defined
					//# emailTextField.setTitle(Locale.get(100));
						//#if (${lowercase(polish.vendor)}==samsung)
						//# UiFactory.fixSamsungNativeTextField(emailTextField);
						//#endif
					//#endif
				
				this.agreeToTC = UiFactory.createChoiceRadioItem(Locale.get( "nowplus.client.java.signup.enterdata.termsandconditions.checkbox.label" ), ChoiceGroup.MULTIPLE);
				ChoiceItem[] cis = {this.agreeToTC};
				
				UiFactory.createChoiceGroup(null, ChoiceGroup.MULTIPLE, cis, 0, false, null, this);
				
				//#style .signup_tc_link
				UiFactory.createStringItem(null, Locale.get( "nowplus.client.java.signup.enterdata.termsandconditions.text" ), CMD_READTC, this, this);
				
				//#style .signup_tc_link
				UiFactory.createStringItem(null, Locale.get( "nowplus.client.java.signup.enterdata.privacy.text" ), CMD_READPRIVACY, this, this);
				
				//#if polish.blackberry
					removeCommand(CMD_CONTINUE_ENTER_DATA);
					if(continueButton!=null)
						buttonContainer.remove(continueButton);
					removeItemCommands(buttonContainer);
					
					//#style ui_factory_button_item
					continueButton= UiFactory.createButtonItem(null, CMD_CONTINUE_ENTER_DATA.getLabel(), (de.enough.polish.ui.Command) CMD_CONTINUE_ENTER_DATA, null, null);
					buttonContainer.add(continueButton);
					
					append(buttonContainer);
				
				//#else
					addCommand(CMD_CONTINUE_ENTER_DATA);
					addCommand(CMD_CANCEL);
				//#endif
			}
			else
			if(state == REREQUEST)
			{
				this.setActiveFrame(-1, true);		
				
				String currentNumber = null;
				
				if(this.phonenumber != null)
					currentNumber = this.phonenumber.getText();
				this.phonenumber = UiFactory.createTextField(Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber" ), TextUtilities.getCurrentHeaderPrefix(), 255, TextField.PHONENUMBER, this);
				if(currentNumber != null)
					this.phonenumber.setText(currentNumber);
				UiAccess.setTextfieldHelp(this.phonenumber, Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber.hint" ));
	            //#if using.native.textfield:defined
				this.phonenumber.setTitle(Locale.get( "nowplus.client.java.signup.enterdata.entermobilenumber.hint" ));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(this.phonenumber);
					//#endif
				//#endif
				this.focus(phonenumber);
				
				//#if polish.blackberry
					removeCommand(CMD_CONTINUE_REENTER_DATA);
					if(continueButton!=null)
						buttonContainer.remove(continueButton);
					removeItemCommands(buttonContainer);
					
					//#style ui_factory_button_item
					continueButton= UiFactory.createButtonItem(null, CMD_CONTINUE_REENTER_DATA.getLabel(), (de.enough.polish.ui.Command) CMD_CONTINUE_REENTER_DATA, null, null);
					buttonContainer.add(continueButton);
					
					append(buttonContainer);
				
				//#else
					addCommand(CMD_CONTINUE_REENTER_DATA);
					addCommand(CMD_CANCEL);
				//#endif
			}
			else
			if(state == CONGRATULATIONS)
			{
				this.setActiveFrame(-1, true);		
				
				//#style .signup_headline
				Item item = UiFactory.createStringItem(null,  Locale.get( "nowplus.client.java.signup.congratulations.headline" ), null, null, null);
				item.setAppearanceMode(Item.PLAIN);
				
				this.append(Graphics.TOP, item);
				
				this.rememberMe = UiFactory.createChoiceRadioItem(Locale.get( "nowplus.client.java.signup.congratulations.rememberme" ), ChoiceGroup.MULTIPLE);
				ChoiceItem[] cis = {this.rememberMe};
				
				UiFactory.createChoiceGroup(Locale.get( "nowplus.client.java.signup.congratulations.youarenow" ), ChoiceGroup.MULTIPLE, cis, 0, true, null, this);
				
				//#if polish.blackberry
					removeCommand(CMD_CONTINUE_CONGRATULATIONS);
					if(continueButton!=null)
						buttonContainer.remove(continueButton);
					removeItemCommands(buttonContainer);
					
					//#style ui_factory_button_item
					continueButton= UiFactory.createButtonItem(null, CMD_CONTINUE_CONGRATULATIONS.getLabel(), (de.enough.polish.ui.Command) CMD_CONTINUE_CONGRATULATIONS, null, null);
					buttonContainer.add(continueButton);
					
					append(buttonContainer);
					
					this.focus(continueButton);
				//#else
					addCommand(CMD_CONTINUE_CONGRATULATIONS);
					addCommand(CMD_EXIT);
				//#endif
			}
			
			requestInit();
		}
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#getContext()
	 */
	public byte getContext() 
	{
		return -1;
	}
	
	public void commandAction(Command c, Item itm) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(null != commandListener)
			commandListener.commandAction(c, this);
		else
			super.commandAction(c, itm);
	}

	
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(null != commandListener)
			commandListener.commandAction(c, this);
		else
			super.commandAction(c, d);
	}

	/**
	 * @param signupController
	 */
	public void setVisibleCommandListener(CommandListener commandListener) {
		this.commandListener = commandListener;
	}
	
	public synchronized void focusField(final byte field)
	{
		this.setActiveFrame(-1, true);		
		
		switch(field)
		{
			case USERNAME_FIELD: if(username != null) this.focus(username); break;
			case PASSWORD_FIELD: if(password != null) this.focus(password);break;
			case DOB_FIELD: if(dob != null) this.focus(dob);break;
			case PHONENUMBER_FIELD: if(phonenumber != null) this.focus(phonenumber); break;
			case TOC_FIELD: if(agreeToTC != null) this.focus(agreeToTC); break;
			case EMAIL_FIELD: if(emailTextField != null) this.focus(emailTextField); break;
		}
	}
	
	public synchronized String getUsername()
	{
		return username==null?null:username.getText();
	}

	public synchronized String getPassword()
	{
		return password==null?null:password.getString();
	}

	public synchronized Date getDoB()
	{
		return dob==null?null:dob.getDate();
	}

	public synchronized String getPhoneNumber()
	{
		return phonenumber==null?null:phonenumber.getText();
	}
	
	public synchronized String getEmail()
	{
		return emailTextField==null?null:emailTextField.getText();
	}

	public synchronized boolean agreesToTC()
	{
		return agreeToTC==null?false:agreeToTC.isSelected;
	}

	/**
	 * 
	 */
	public boolean getRememberMe() {
		return rememberMe==null?false:rememberMe.isSelected;
	}	
	

	public void showNotify()
	{
		super.showNotify();

		//#if !polish.blackberry
			//#if not using.native.textfield:defined
				TextUtilities.loadTextFieldCharacterset("la");//Defaulting to latin characters as we only allow latin characters in input fields 
			//#endif	
		//#endif
	}	
	

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
	 */
	protected Item createBottomItem() 
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createStaticItems()
	 */
	protected Item[] createStaticItems() 
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createTopItem()
	 */
	protected Item createTopItem()
	{
		return null;
	}
	
	public void releaseResources() 
	{
		super.releaseResources();
		
		//clean up automatically cashed landing page image
		StyleSheet.releaseResources();
	}

	//#if polish.blackberry
	//overrides
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need
	 * to ensure that super.keyPressed( int ) is called!
	 * 
	 * @param keyCode The code of the pressed key
	 */
	public void keyPressed(int keyCode) 
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE&&(state == WELCOME||state == CONGRATULATIONS))
			getController().notifyEvent(Event.Context.APP, Event.App.MARK_FOR_FLUSH_AND_EXIT);
		else if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE&& (state == ENTER_DATA||state == REREQUEST))
			this.getCommandListener().commandAction(CMD_CANCEL,this);
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
