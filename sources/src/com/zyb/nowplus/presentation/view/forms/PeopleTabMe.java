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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.InputModeTextFieldItem;
import com.zyb.nowplus.presentation.view.items.TouchStringItemButton;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Locale;
import de.enough.polish.ui.StyleSheet;

//#ifdef testversion:defined
import de.enough.polish.util.Debug;
//#endif

//#if polish.blackberry
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * The 'Me' tab of the PeoplePages
 * 
 * TODO Use of Model setter methods does not belong here!!! /Jens 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class PeopleTabMe extends BasePeopleForm
{
	/**
	 * Default command common to all people page tab screens
	 */
	public final static Command cmdMyProfile = new Command(Locale.get("nowplus.client.java.profilepage.content.command.open"), Command.ITEM, 0);
	public final static Command cmdWebAccounts = new Command(Locale.get("nowplus.client.java.profilepage.content.command.open"), Command.ITEM, 0);
	public final static Command cmdSettings = new Command(Locale.get("nowplus.client.java.profilepage.content.command.open"), Command.ITEM, 0);
	public final static Command cmdSet = new Command(Locale.get("nowplus.client.java.peoplepage.meview.command.set"), Command.ITEM, 0);
	
	//#if polish.blackberry.isTouchBuild == true
	private Command meTabCmd;
	//#endif
	
//#if polish.blackberry
	
	//Index of items added in Container[textFieldAndButtonContainer] for Status textfield 
	private final int INDEX_TEXTFILED_STATUS=0;
	
	//Index of items added in Container[textFieldAndButtonContainer] for update command button[updateButton]
	private final int INDEX_UPDATE_BUTTON=1;
	
	private Container  textFieldAndButtonContainer;
	private StringItem updateButton;
//#endif
	
	/**
	 * Own profile object
	 */
	protected MyProfile me;
	
	/**
	 * Item representation of profile
	 */
	protected ContactSummarizedItem csi;
	
	/**
	 * Handle to the status text field
	 */
	protected TextField status;
	protected StringItem statusInfo;
	
	/**
	 * Flag to set to focus the status text field.
	 */
	protected boolean statusFieldSet = false;
	
	/**
	 * Handle to the availability choice group
	 */
	protected ChoiceGroup availability;	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public PeopleTabMe(Model model, Controller controller)
	{
		this(model, controller, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param style
	 */
	public PeopleTabMe(Model model, Controller controller, Style style)
	{
		super(model, controller, Event.Context.CONTACTS, PEOPLEPAGE_ME_TAB_PRIORITY, style);
		
		//enable scrolling cycling //TODO fixed in Super class of this or from j2mepolish or property of custom device
		de.enough.polish.ui.UiAccess.setCycling(this, true);
		de.enough.polish.ui.UiAccess.setCycling(container, true);
		
		//#debug startup
		System.out.println("Constructing Me Tab");
		
		String iconImageName = null;
		
		//#if polish.blackberry
			 iconImageName = Locale.get("nowplus.client.java.peoplepage.tab.me");//"Me";
		//#endif
		
		//Fake usage of style to avoid removal during preprocessing
		//#style peoplepage_navbar_item_me_active
		this.tabItem = new IconItem(iconImageName, null);
		
		//#style peoplepage_navbar_item_me
		this.tabItem.setStyle();
		
		this.tabItem.setDefaultCommand(cmdFake);	 
		
		//#if polish.blackberry.isTouchBuild == true
			meTabCmd = new Command("meTab", Item.BUTTON, 0);
			this.tabItem.setDefaultCommand(meTabCmd);
			this.tabItem.setItemCommandListener(this);
			super.setCommandListener(this);
		//#endif
			
		setTabItem(this.tabItem);
		
		this.tabTitle = Locale.get("nowplus.client.java.peoplepage.tab.me");
		
		setTabTitle(this.tabTitle);
		//set to avoid the status text field focused at the begining.
		this.statusFieldSet = false;
		
		//try to load
		loadMe();
		
		//add settings button if loading failed
		if(null == this.me)
			UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.peoplepage.meview.settings"), cmdSettings, this, this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
	 */
	protected Item createBottomItem()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector() 
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createTopItem()
	 */
	protected Item createTopItem() 
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#getPriority()
	 */
	public byte getPriority() 
	{
		return PEOPLEPAGE_ME_TAB_PRIORITY;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if( context == Event.Context.CONTACTS)
		{
			if(event == Event.Contacts.UPDATE)
			{
				//initiate loading of own profile if not done already
				loadMe();
				
				//is contact id same?
				if(null != this.me && null != data && data instanceof Long && ((Long)data).longValue() == this.me.getCabId())
				{
					//load contact
					this.me.load(true);
					
					//update contact summarized item
					if(null != csi)
						csi.setContact(this.me);
					
					//update additional statusfield string telling which SNs a status will go out to
					String s = getSocialNetworkStatus();
					if(null != statusInfo )
						statusInfo.setText(s);
					
					//update availability
					int currentAvalibility = 0;
					
					switch (this.me.getNowPlusPresence())
					{
					case Channel.PRESENCE_ONLINE:
						currentAvalibility = 0;
						break;
					case Channel.PRESENCE_INVISIBLE:
						currentAvalibility = 1;
						break;
						/*
					//Descoped for for June 2009 release
					case Channel.PRESENCE_OFFLINE:
					currentAvalibility = 2;
					break;
						 */
					default:
						currentAvalibility = 0;
						break;
					}
					
					if(null != this.availability && this.availability.getSelectedIndex() != currentAvalibility)
						this.availability.setSelectedIndex(currentAvalibility, true);
					
					//unload
					this.me.unload();
				}
			}
		}
        else
            super.handleEvent(context, event, data);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(c == cmdOptions)
		{
			//launch contextual menu
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, this.me);
		}
		else
			super.commandAction(c, d);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item)
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		//#if polish.blackberry.isTouchBuild == true
		if(c == meTabCmd)
		{
			//special tab switch handling for BB touch devices
			  super.switchTabByIndex(this.tabIndex);    
              return;
		}
		else
		//#endif	
		if(c == cmdMyProfile)
		{
			getController().notifyEvent(Event.Context.PROFILE, Event.Profile.OPEN, this.me);
		}
		else
		if(c == cmdWebAccounts)
		{
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.OPEN);
		}
		else
		if(c == cmdSettings)
		{
			getController().notifyEvent(Event.Context.SETTINGS, Event.Settings.OPEN);
		}
		else
		if(c == cmdSet)
		{
			//is status textfield empty
			if(!this.status.getString().trim().equals(""))
			{
				saveProfile();
			}
		}		
		else
			super.commandAction(c, item);
	}
	
	public void itemStateChanged(Item item) 
	{
		if(item == availability)
		{
			int currentAvalibility = 0;
			
			switch (this.me.getNowPlusPresence())
			{
			case Channel.PRESENCE_ONLINE:
				currentAvalibility = 0;
				break;
			case Channel.PRESENCE_INVISIBLE:
				currentAvalibility = 1;
				break;
				/*
			//Descoped for for June 2009 release
			case Channel.PRESENCE_OFFLINE:
			currentAvalibility = 2;
			break;
				 */
			default:
				currentAvalibility = 0;
				break;
			}
			
			//has availability changed and should therefore be updated?
			if(null != this.availability && this.availability.getSelectedIndex() != currentAvalibility)
			{
				//save availability
				saveAvailability();
			}
		}
		else if(item instanceof TextField){
			this.container.scroll(0, item, true);
		}
		else {
			super.itemStateChanged(item);
		}
	}
	
	
	public void saveAvailability()
	{
		//First we check if it makes sense to do an update at all. If availability is what it is already set to we just ignore it - bug 14278
		final int index = availability.getSelectedIndex();
		if ((index == 0 && this.me.getNowPlusPresence() != Channel.PRESENCE_ONLINE) || 
				(index == 1 && this.me.getNowPlusPresence() != Channel.PRESENCE_INVISIBLE))
		{
			//commit changes
			new Thread()
			{
				public void run() 
				{
					NotificationForm nf=null;
					try
					{
						//envoke contact update notification 
						
						
						//#if polish.blackberry
						//#style notification_form_base
						nf = new NotificationForm(
								getModel(), getController(),
								Locale.get("nowplus.client.java.edit.contact.update.notify.title"),
								Locale.get("nowplus.client.java.edit.contact.update.notify.text"),
								new Command(Locale.get("polish.command.ok"), Command.OK, 0),
								Integer.MAX_VALUE
						);
						
						//#else
						//#style notification_form_base
						nf = new NotificationForm(
								getModel(), getController(),
								Locale.get("nowplus.client.java.edit.contact.update.notify.title"),
								Locale.get("nowplus.client.java.edit.contact.update.notify.text"),
								null,
								10
						);
						//#endif
						
						nf.removeAllCommands();
						
						//set next
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
						//#if polish.blackberry
						long startTime = System.currentTimeMillis();
						//#endif
						
						//set new availability
						if (index == 0)
						{
							model.setMyNowPlusPresence(Channel.PRESENCE_ONLINE);	
						}
						else
							if (index == 1)
							{
								model.setMyNowPlusPresence(Channel.PRESENCE_INVISIBLE);
							}
						/*
					//Descoped for for June 2009 release
					else
					if (index == 2)
					{
						model.setMyNowPlusPresence(Channel.PRESENCE_OFFLINE);
					}
						 */
						//#if polish.blackberry
						long endTime = System.currentTimeMillis();
						long runTime = endTime - startTime;
						if(runTime < 2000){
							try{
								Thread.sleep(2000 - runTime);
							}catch (Exception e) {
								//ignore
							}
						}
						//#endif
						//remove update notification
						if(nf.isShown()){
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
						}
						
					}
					catch (Throwable t)
					{
						//#debug error
						System.out.println("General error while saving changes to 'Me View': " + t);
						//remove update notification
						if(null != nf)
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
						
					}
				}
			}.start();
		}		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void saveProfile() 
	{
		//commit changes
		new Thread()
		{
			public void run() 
			{
				try
				{
					//invoke contact update notification 
					NotificationForm nf;

				//#if polish.blackberry
				
					//#style notification_form_base
					nf = new NotificationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.edit.contact.update.notify.title"),
							Locale.get("nowplus.client.java.edit.contact.update.notify.text"),
							new Command(Locale.get("nowplus.client.java.edit.contact.missing.name.notify.ok"), Command.OK, 0),
							Integer.MAX_VALUE
					);
		
				//#else
				
					//#style notification_form_base
					nf = new NotificationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.edit.contact.update.notify.title"),
							Locale.get("nowplus.client.java.edit.contact.update.notify.text"),
								null,
							Integer.MAX_VALUE
							);
					
				//#endif
					
					nf.removeAllCommands();
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);

					//#if polish.blackberry
					long startTime = System.currentTimeMillis();
					//#endif
					
					try
					{		
						model.setMyStatus(status.getString());

                        //clear status field value and add helptext, mingle task: #850 
                        status.setString("");
                        status.setHelpText(Locale.get("nowplus.client.java.peoplepage.meview.status.helptext"));
                        
        				//#if polish.blackberry
	    					//#style ui_factory_textfield_helpfont
                        	status.setHelpStyle();
    					//#endif
    					
                        //#if using.native.textfield:defined
                        status.setTitle(Locale.get("nowplus.client.java.peoplepage.meview.status.helptext"));
                        //#endif
                      //#if polish.blackberry
    					long endTime = System.currentTimeMillis();
    					long runTime = endTime - startTime;

    					if(runTime < 2000){
    						try{
    							Thread.sleep(2000 - runTime);
    						}
    						catch (Exception e) {
    							//ignore
    						}
    					}
    					//#endif

						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
						model.sync();//Force data to the server right away
					}
					catch (LockException le) {
						//#debug error
						System.out.println("Error while obtaining lock on profile object: "+le.getMessage());
						
						//remove update notification
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
						
						Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
						
						//envoke update failed notification
						//#style notification_form_base
						nf = new NotificationForm(
								getModel(), getController(),
								Locale.get("nowplus.client.java.peoplepage.meview.status.failed.notify.title"),
								Locale.get("nowplus.client.java.peoplepage.meview.status.failed.notify.text"),
								cmdOk,
								-1
						);
						
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
					}
				}
				catch (Throwable t)
				{
					//#debug error
					System.out.println("General error while saving changes to 'Me View': " + t);
				}
			}
		}.start();
	}
	
	/**
	 * Load own profile and populates form with elements of me view
	 */
	protected void loadMe()
	{
		if(null == me && null != (this.me = getModel().getMe()))
		{
			//clear content frame
			this.deleteAll(-1);
			
			//add profile item
			csi = UiFactory.createUserProfileItem(this.me, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_GRAY, this);
			csi.setAppearanceMode(Item.PLAIN);

			//#if polish.blackberry
				//#style peoplepage_me_status_textfield
				status = UiFactory.createTextField("",null, 1024, TextField.INITIAL_CAPS_WORD | TextField.ANY, null);
				status.setHelpText(Locale.get("nowplus.client.java.peoplepage.meview.status.helptext"));
				//#style peoplepage_me_status_helptextfont
				status.setHelpStyle();

				//#style peoplepage_me_textfieldbutton_container
				textFieldAndButtonContainer = new Container(false);
				textFieldAndButtonContainer.setAppearanceMode(Item.PLAIN);
				
				//#style peoplepage_me_button_submit
				StringItem button = UiFactory.createButtonItem(null ,Locale.get("nowplus.client.java.peoplepage.meview.submit.command.update") , (de.enough.polish.ui.Command) cmdSet, this, null);	  				
				
				//#if polish.blackberry.isTouchBuild == true
					
					TouchStringItemButton updateButton = new TouchStringItemButton(button.getLabel(), button.getText(), button.getStyle());	
		            updateButton.setDefaultCommand(cmdSet);
		            updateButton.setItemCommandListener(this);
					//#style peoplepage_me_button_submit_press
					updateButton.setFocusedStyle();
				//#else
					//#style peoplepage_me_button_submit
					//# updateButton = UiFactory.createButtonItem(null ,Locale.get("nowplus.client.java.peoplepage.meview.submit.command.update") , (de.enough.polish.ui.Command) cmdSet, this, null);;
				//#endif
					
				StringItem lableText = new StringItem(null, Locale.get("nowplus.client.java.peoplepage.meview.status.label"));
				
				
				//textFieldAndButtonContainer.add(lableText);
				
				textFieldAndButtonContainer.add(INDEX_TEXTFILED_STATUS,status);
				textFieldAndButtonContainer.add(INDEX_UPDATE_BUTTON,updateButton);
				
				//#style peoplepage_me_textfieldbutton_container_text
				append(lableText);
				append(textFieldAndButtonContainer);
					
				//status = statusInputModeTextFieldItem;
				
			//#else
			
				//add status field
				//String currentStatus = Locale.get("nowplus.client.java.peoplepage.meview.status.label");
				status = UiFactory.createTextField(Locale.get("nowplus.client.java.peoplepage.meview.status.label"), "", 1024, TextField.INITIAL_CAPS_SENTENCE|TextField.ANY, this);
	
	            //set helptext (if we clear the textfield a default English text will appear)
	            status.setHelpText(Locale.get("nowplus.client.java.peoplepage.meview.status.helptext"));
	            
				//#style ui_factory_textfield_helpfont
	            status.setHelpStyle();
	            
	            //#if using.native.textfield:defined
                status.setTitle(Locale.get("nowplus.client.java.peoplepage.meview.status.helptext"));
	            //Allow prediction in status native text box
                status.setConstraints(status.getConstraints() &~ TextField.NON_PREDICTIVE);    
                //Allow initial caption
                status.setConstraints(status.getConstraints() &~ TextField.INITIAL_CAPS_NEVER);    
                	//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(status);
					//#endif
                //#endif
					
		    //#endif
            
		//#if polish.blackberry.isTouchBuild == false
			status.setDefaultCommand(cmdSet);
		//#else
			status.addCommand(cmdSet);
		//#endif
			
           status.setItemCommandListener(this);
            
            //update additional statusfield string telling which SNs a status will go out to
            String s = getSocialNetworkStatus();
            
            //#style .peoplepage_me_status_textfield_status
            statusInfo = UiFactory.createStringItem(null, s, null, null, null);
            
            statusInfo.setAppearanceMode(Item.PLAIN);
            
            this.add(statusInfo);

			//add availability 
			int selectedIndex = 0;
			
			switch (this.me.getNowPlusPresence())
			{
				case Channel.PRESENCE_ONLINE:
					selectedIndex = 0;
					break;
				case Channel.PRESENCE_INVISIBLE:
					selectedIndex = 1;
					break;
				//Descoped for for June 2009 release
				/*	
				case Channel.PRESENCE_OFFLINE:
					selectedIndex = 2;
					break;
				*/
				default:
					selectedIndex = 0;
					break;
			}
			
			//#style peoplepage_me_availability_radio_item
			ChoiceItem choiceAvailable = UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.peoplepage.meview.availability.available"), ChoiceGroup.EXCLUSIVE);
			//#style peoplepage_me_availability_radio_item
			ChoiceItem choiceInvisible = UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.peoplepage.meview.availability.invisible"), ChoiceGroup.EXCLUSIVE); 
			
			ChoiceItem[] cis = {
					choiceAvailable,
					choiceInvisible
					//Descoped for for June 2009 release
					/*,UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.peoplepage.meview.availability.offline"), ChoiceGroup.EXCLUSIVE)*/
			};
			
			this.availability = UiFactory.createChoiceGroup(Locale.get("nowplus.client.java.peoplepage.meview.availability.label"), ChoiceGroup.EXCLUSIVE, cis, selectedIndex, true, this, this);
			
			//add my profile button
			UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.peoplepage.meview.myprofile"), cmdMyProfile, this, this);
			
			//add web accounts button
			UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.peoplepage.meview.webaccounts"), cmdWebAccounts, this, this);
			
			//add settings button
			UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.peoplepage.meview.settings"), cmdSettings, this, this);
		}
		

		// setting container focus only when the user switched the tab.
		//to fix the issue jira #1556
		if(wasTabSwitch){
			this.container.focusChild(-1);
		}
		
		//caused UI defects e.g. all tab focus TextField of Me tab
		//always focus status textfield when entering 'me tab'
		//focused only returning from input-mode for nokia devices.
		if(null != status && statusFieldSet){
			UiAccess.focus(this, status);
			statusFieldSet = false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#showNotify()
	 */
	public void showNotify()
	{
		//#debug debug
		System.out.println("showNotify()");
		
		//#if polish.blackberry
			 Object bbLock = Application.getEventLock();
			 synchronized (bbLock) {
        //#endif	
        
		/* Load own profile and me view elements.
		 * Nessasary since the model might no be ready when loadMe()
		 * is called during construction.
		 */
		loadMe();
		
		//Call super MUST be last to retain navBar focus state
		super.showNotify();
		
		//#if polish.blackberry
			 }
		//#endif
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#switchTabItemStyle()
	 */
	protected void switchTabItemStyle() 
	{
		if( isNavBarUnfocused() )
		{
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_me_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_me_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_me_active
				tabItem.setStyle();
			}
		}
		else if ( isNavBarFocused() )
		{
			
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_me_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_me_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_me
				tabItem.setStyle();
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{	
		statusFieldSet = false;
		if(this.status != null && (this.getCurrentItem() instanceof InputModeTextFieldItem && ((InputModeTextFieldItem)this.getCurrentItem()).getTextField() == this.status))
		{
			//#if using.native.textfield:defined 
			if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5 && status.size()>0)
			{
				return false;
			}
			//#endif 
			//mobica>
			
			if(UiAccess.handleKeyPressed(this.status, keyCode, gameAction)){
				// set to focus the status field text
				statusFieldSet = true;
				return true;
			}
		}
		setFromContacts(false);
		return super.handleKeyPressed(keyCode, gameAction);
	}

	protected boolean handleKeyRepeated(int keyCode, int gameAction) 
	{
		//#ifdef testversion:defined
		if(keyCode == KEY_POUND)
		{
			Debug.showLog(StyleSheet.display);
			return true;
		}
		//#endif
		
		if(this.status != null && (this.getCurrentItem() instanceof InputModeTextFieldItem && ((InputModeTextFieldItem)this.getCurrentItem()).getTextField() == this.status))
		{
			if(UiAccess.handleKeyRepeated(this.status, keyCode, gameAction))
				return true;
		}
		
		return super.handleKeyPressed(keyCode, gameAction);
	}

	/**
	 * Constructs string under status textfield telling the user what SNs a status
	 * message will be sent out to.
	 * 
	 * Example: "Posts to FaceBook, Twitter too"
	 */
	private String getSocialNetworkStatus()
	{
		StringBuffer sb = null;
		
		if(null == this.me)
			return null;
		
		synchronized (getPaintLock())
		{
			this.me.load(true);
			Identity[] identities = this.me.getLoggedInWebaccountsWithPostCap();
			this.me.unload();
			
			if(null == identities || identities.length == 0)
				return null;
			
			sb = new StringBuffer();
			
			for(int i=0; i<identities.length; i++)
			{
				if(sb.length()>0)
					sb.append(", ");
			
				sb.append(identities[i].getNetwork().getName());
			}
		}
		
		if(null != sb && sb.length() > 0)
		{
			String parameter = sb.toString().trim();
			return Locale.get("nowplus.client.java.peoplepage.meview.sn.status", parameter );
		}
		else
			return null;
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() 
	{
		commandAction(cmdOptions, this);
		
		return true;
	}
	
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
		int gameActionCustom= getGameAction(keyCode);
		
		boolean isFocusedContainerRecorded=false;
		
		if(textFieldAndButtonContainer!=null)//important for avoiding nullPointer exception thrown
			isFocusedContainerRecorded=textFieldAndButtonContainer.isFocused;
		
		if (Keypad.key(keyCode) == Keypad.KEY_ENTER&&getCurrentItem()==status)
			commandAction(cmdSet, status);
		else if((textFieldAndButtonContainer!=null)&&//also for avoiding nullPointer exception thrown
				(gameActionCustom == Canvas.LEFT || gameActionCustom == Canvas.RIGHT)&&textFieldAndButtonContainer.isFocused)
		{
			if(updateButton != null && updateButton.isFocused)
				textFieldAndButtonContainer.focusChild(INDEX_TEXTFILED_STATUS);
			else
				textFieldAndButtonContainer.focusChild(INDEX_UPDATE_BUTTON);
				
			repaint();
			return;
		}
		else
			super.keyPressed(keyCode);
		
		//after accepting key pressed actions, now if container of Submit button and textField is focused, always focus on TextField
		if(!isFocusedContainerRecorded && textFieldAndButtonContainer != null && textFieldAndButtonContainer.isFocused)
			textFieldAndButtonContainer.focusChild(INDEX_TEXTFILED_STATUS);
	}
	//#endif
	
	//#mdebug error
	public String toString()
	{
		return "PeopleTabMe[]";
	}
	//#enddebug
}
