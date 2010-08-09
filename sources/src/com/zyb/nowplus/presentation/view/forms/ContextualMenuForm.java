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

import com.zyb.nowplus.business.domain.Identity;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.NavBarContainer;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandItem;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * Contextual menu aka Options menu class. Implements features common to all such menu variants.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ContextualMenuForm extends BaseForm
{	
	/**
	 * Default command common to all contextual menu types
	 */
	public final static Command cmdOptions,cmdOpenFilter,cmdAddNewContact, cmdCreateText, cmdEditContact, cmdDeleteContact, cmdDeleteDetail, cmdDeleteSN, cmdHelp, cmdExit, cmdCall, cmdSms, cmdIm, cmdEmail, cmdProfile;
	
	//#if activate.embedded.360email
	public final static Command cmdNewEmail, cmdGotoFolders, cmdDeleteEmail, cmdForward, cmdReply ; 
	//#endif
	
	//#ifdef testversion:defined
	public final static Command cmdClearRMS;
	//#endif
	
	//#if polish.blackberry.isTouchBuild == true
	public final static Command  cmdShowSearchbar;
	//#endif
	
	static
	{
		cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
		
		cmdAddNewContact = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdCreateText = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdOpenFilter = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		
		cmdHelp = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdExit = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		
		cmdEditContact = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdDeleteContact = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdDeleteDetail = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdDeleteSN = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		
		cmdCall = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdSms = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdIm = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdEmail = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdProfile = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		
		//#if activate.embedded.360email
		//TODO: to be substituted by textkeys
		cmdNewEmail = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdGotoFolders = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdDeleteEmail = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		cmdForward = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdReply = new Command(Locale.get("polish.command.select"), Command.OK, 0); 
		//#endif
		
		//#ifdef testversion:defined
		cmdClearRMS = new Command(Locale.get("polish.command.select"), Command.OK, 10);
		//#endif
		
		//#if polish.blackberry.isTouchBuild == true
		cmdShowSearchbar = new Command("Searchbar", Command.OK, 0);
		//#endif
	}
	
	//#if polish.blackberry && add.switch.application.form
	public final static Command cmdSwitchApps = new Command(Locale.get("nowplus.client.java.contextual.menu.command.switch.application"), Command.OK, 0);
	//#endif
	
	protected Container navBar;
	
	protected ContactSummarizedItem csi;
	
	/**
	 * Contact reference
	 * <p>
	 * Using a ProfileSummary type enables support for
	 * both Profile and UserRef instances.
	 */
	protected ProfileSummary contact;
	
	protected StringItem infoBar;
	
	protected Item callIcon, smsIcon, imIcon, emailIcon, profileIcon;
	
	private Item focusedNavBarItem;
	
	private Style unfocusInfoBar, focusInfoBar;
	
	private int contentContext;
	
	private Object[] objs;
	
	private Displayable parent;
	
	private boolean beenShown = false;
	
	public ContextualMenuForm(Model model, Controller controller, ProfileSummary contact, int contentContext, Object[] objs, Displayable parent) 
	{
		this(model, controller, contact, contentContext, objs, parent, null);
	}

	public ContextualMenuForm(Model model, Controller controller, ProfileSummary contact, int contentContext, Object[] objs, Displayable parent, Style style) 
	{
		super(model, controller, null, style);
		
		this.contact = contact;
		
		this.contentContext = contentContext;
		
		this.objs = objs;
		
		this.parent = parent;
		
		//#if not polish.blackberry
			this.addCommand(cmdBack);
		//#endif
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#createContent()
	 */
	protected void createContent() 
	{
		//deside if carousel should be created based on context
		if(this.contentContext != Event.Context.EDIT_PROFILE && this.contentContext != Event.Context.WEB_ACCOUNTS)
		{
			//create carousel
			if(contact != null)
			{	
				if(this.contact instanceof MyProfile)
					//#style contextualmenu_profile_item
					this.csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_GRAY, this);
				else
					//#style contextualmenu_profile_item
					this.csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_ONLY, this);
				this.csi.setAppearanceMode(Item.PLAIN);
				
				//Add navbar if contact is Profile and hence hold necessary data
				if(this.contact instanceof Profile)
				{
					Profile profile = (Profile) this.contact;

					//ensure contact is loaded
					profile.load(true);
					
					//#style contextualmenu_infobar
					infoBar = new StringItem(null, "" );
					infoBar.setAppearanceMode(Item.PLAIN);
					
					unfocusInfoBar = infoBar.getStyle();
					focusInfoBar = infoBar.getFocusedStyle();
					
					boolean focusFirstItem = true;
					
					//#if polish.blackberry
						focusFirstItem = false;
					//#endif
					
					//#style contextualmenu_navbar
					navBar = new NavBarContainer(focusFirstItem);
					navBar.allowCycling = true;
					
					if(null != profile.getPrimaryCallChannel() && profile.getNowPlusMember() != Profile.NOWPLUS_ME)
					{
						String callString = null;
						
						//#if polish.blackberry.isTouchBuild == true
						
							callString = Locale.get("nowplus.client.java.profilepage.content.command.call");//"call";
					
							String phoneNum = TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, profile.getPrimaryCallChannel().getName());
					
							//#style contextualmenu_navbar_item_call_BB
							callIcon = new IconItem(callString +": "+phoneNum, null);
						
						//#else
						
							//#style contextualmenu_navbar_item_call
							callIcon = new IconItem(callString, null);
						
						//#endif
						
						//#if polish.blackberry
							callIcon.setDefaultCommand(cmdCall);
						//#else
							callIcon.addCommand(cmdCall);
						//#endif
						
						callIcon.setItemCommandListener(this);
						
						//#if polish.blackberry.isTouchBuild == true
							this.append(callIcon);
						//#else
							navBar.add(callIcon);
						//#endif
						
					}
					
					if(null != profile.getPrimarySMSChannel() && profile.getNowPlusMember() != Profile.NOWPLUS_ME)
					{
						String smsString = null;
						
						//#if polish.blackberry.isTouchBuild == true
						
							smsString = Locale.get("nowplus.client.java.profilepage.content.command.call");//"call";
						
							String phoneNum = TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, profile.getPrimaryCallChannel().getName());
						
							//#style contextualmenu_navbar_item_sms_BB
							smsIcon = new IconItem(smsString +": "+phoneNum, null);
							
						//#else
							
							//#style contextualmenu_navbar_item_sms
							smsIcon = new IconItem(smsString, null);
							
						//#endif
							
						//#if polish.blackberry
							smsIcon.setDefaultCommand(cmdSms);
						//#else
							smsIcon.addCommand(cmdSms);
						//#endif
							
						smsIcon.setItemCommandListener(this);
						
						//#if polish.blackberry.isTouchBuild == true
							this.append(smsIcon);
						//#else
							navBar.add(smsIcon);
						//#endif
					}
					
					if(null != profile.getPrimaryChatChannel())
					{
						String chatString = null;
						
						//#if polish.blackberry.isTouchBuild == true
						
							chatString = Locale.get("nowplus.client.java.chat.thread.title");//"chat";
						
							String chatID = profile.getPrimaryChatChannel().getName();
						
							//#style contextualmenu_navbar_item_IM_BB
							imIcon = new IconItem(chatString +": "+chatID, null);
							
						//#else
							
							//#style contextualmenu_navbar_item_IM
							imIcon = new IconItem(chatString, null);
							
						//#endif
						
						//#if polish.blackberry
							imIcon.setDefaultCommand(cmdIm);
						//#else
							imIcon.addCommand(cmdIm);
						//#endif
						
						imIcon.setItemCommandListener(this);
						
						//#if polish.blackberry.isTouchBuild == true
							this.append(imIcon);
						//#else
							navBar.add(imIcon);
						//#endif
					}
					//#if polish.device.supports.nativeemail == false
					//#debug
					System.out.println("Device does not have native email creator. Removing \"email\" icon");
					//#else
					if(null != profile.getPrimaryEmailChannel())
					{
						String emailString = null;
						
						//#if polish.blackberry.isTouchBuild == true
						
							emailString = Locale.get("nowplus.client.java.edit.contact.email");//"email";
					
							String emailID = profile.getPrimaryEmailChannel().getName();
						
							//#style contextualmenu_navbar_item_email_BB
							emailIcon = new IconItem(emailString +": "+emailID, null);
							
						//#else
							
							//#style contextualmenu_navbar_item_email
							emailIcon = new IconItem(emailString, null);
							
						//#endif
							
						//#if polish.blackberry
							emailIcon.setDefaultCommand(cmdEmail);
						//#else
							emailIcon.addCommand(cmdEmail);
						//#endif
							
						emailIcon.setItemCommandListener(this);
						
						//#if polish.blackberry.isTouchBuild == true
							this.append(emailIcon);
						//#else
							navBar.add(emailIcon);
						//#endif
					}
					//#endif
					
					
					String profileString = null;
					
					//#if polish.blackberry.isTouchBuild == true
				
						profileString = Locale.get("nowplus.client.java.profilepage.tab.details");// "profile";
					
						//#style contextualmenu_navbar_item_profile_BB
						profileIcon = new IconItem(profileString, null);
						
					//#else
						
						//#style contextualmenu_navbar_item_profile
						profileIcon = new IconItem(profileString, null);
						
					//#endif
					
					//#if polish.blackberry
						profileIcon.setDefaultCommand(cmdProfile);
					//#else
						profileIcon.addCommand(cmdProfile);
					//#endif
					
					profileIcon.setItemCommandListener(this);
					
					//#if polish.blackberry.isTouchBuild == true
					    if(this.contentContext != Event.Context.PROFILE)
					    	this.append(profileIcon);
					//#else
					    if(this.contentContext != Event.Context.PROFILE)					    
					    	navBar.add(profileIcon);
					//#endif
					
					//only include menubar if contact has valid channels
					if(navBar.size() > 0)
					{
						//#if polish.blackberry.isTouchBuild == false
							this.append(infoBar);
							this.append(navBar);

							//initial default focus on navbar
							this.focus(navBar);
							
							//init state of infobar according to navbar's state
							if(navBar.isFocused && infoBar.getStyle() != focusInfoBar)
								infoBar.setStyle(focusInfoBar);
							if(!navBar.isFocused && infoBar.getStyle() != unfocusInfoBar)
								infoBar.setStyle(unfocusInfoBar);
							
							updateInfoItem();
						//#endif
					}
					
					//ensure contact is unloaded
					profile.unload();
				}
			}
		}
		
		createContextContent(this.contentContext);
		
		//#if polish.blackberry.isTouchBuild == true
//		initContentForTouchDevice();
		//#endif
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
	
	/**
	 * Creates Form content based on the given context
	 * 
	 * @param contentContext
	 */
	private void createContextContent(int contentContext)
	{
		CommandItem ci = null;
		
		switch (contentContext)
		{
		case Event.Context.CHAT:
			
			if(null != this.csi)
				csi.setNetworkIconMode(ContactSummarizedItem.NETWORK_ICON_MODE_ALL);
			
			//#if not polish.blackberry
			//<mobica
			//#if not polish.device.supports.nativesms:defined || polish.device.supports.nativesms == true
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.create.text"),
					cmdCreateText,
					this.container,
					this,
					this
					);
			//#endif
			//mobica>
			//#endif
			
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);				
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);	
			
			//remove undesired icons from navbar
			if(null != imIcon)
				this.navBar.remove(imIcon);
			
			break;
			
		case Event.Context.EDIT_PROFILE:
			
			if(null != this.contact)
			{
				//#style contextualmenu_profile_item
				csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_ONLY, this);
				csi.setAppearanceMode(Item.PLAIN);
			}

			String detailSuffix = null;
			Event event = null;
			
			if(null != objs && objs.length == 2)
			{
				detailSuffix = (String)objs[0];
				detailSuffix = null != detailSuffix ? detailSuffix : "";
				event = (Event)objs[1];
			}
			
			if(	null != event && event.getId() == Event.EditProfile.DELETE_DETAIL_CONTEXTUAL)
			{	
				//#style contextualmenu_seperator_action_item_base
				ci = UiFactory.createCascadeItem(
						Locale.get("polish.command.delete")+" "+detailSuffix,
						cmdDeleteDetail,
						this.container,
						this,
						this
						);
				this.focus(ci);
			}
				
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);	
			
			if(!(this.getCurrentItem() instanceof CommandItem))
				this.focus(ci);
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);	
			break;
			
		case Event.Context.CONTACTS:
			
			if(null != parent)
			{
				if(parent instanceof PeopleTabContacts)
				{
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.add.new.contact"),
							cmdAddNewContact,
							this.container,
							this,
							this
							);
					
					//#if polish.blackberry
					if(contact instanceof ContactProfile)
					{
						//#style contextualmenu_seperator_action_item_base
						ci = UiFactory.createCascadeItem(
								Locale.get("nowplus.client.java.contextual.menu.command.delete") + " " +this.contact.getFullName(),
								cmdDeleteContact,
								this.container,
								this,
								this
						);
					}
					//#endif
					
					//<mobica
					//#if not polish.device.supports.nativesms:defined || polish.device.supports.nativesms == true
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.create.text"),
							cmdCreateText,
							this.container,
							this,
							this
							);
					//#endif
					//mobica>
					
					
					//#if polish.blackberry.isTouchBuild == true
					
					//#else
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.open.filter"),
							cmdOpenFilter,
							this.container,
							this,
							this
							);
					//#endif
					
					//#if polish.blackberry.isTouchBuild == true
						//#style contextualmenu_navbar_item_profile_BB
						ci = UiFactory.createCascadeItem(
								"Search",
								cmdShowSearchbar,
								this.container,
								this,
								this
								);
					//#endif
					
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.help"),
							cmdHelp,
							this.container,
							this,
							this
							);

					
				   //#if polish.blackberry && add.switch.application.form
						//#style contextualmenu_action_item_base
						ci = UiFactory.createCascadeItem(
								cmdSwitchApps.getLabel(),
								cmdSwitchApps,
								this.container,
								this,
								this
								);
				   //#else
					//#style contextualmenu_last_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.exit"),
							cmdExit,
							this.container,
							this,
							this
							);	
					//#endif
				}
				//#if !polish.remove_status_tab
				else
				if(parent instanceof PeopleTabStatus
						//#if activate.timeline.tab
						|| parent instanceof PeopleTabTimeline
						//#endif
						//#if activate.embedded.360email
						|| parent instanceof PeopleTabEmail
						//#endif
				)
				{
					
					//<mobica
					//#if not polish.device.supports.nativesms:defined || polish.device.supports.nativesms == true
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.create.text"),
							cmdCreateText,
							this.container,
							this,
							this
							);
					//#endif
					//mobica>
					
					
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.help"),
							cmdHelp,
							this.container,
							this,
							this
							);
					
					//#style contextualmenu_last_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.exit"),
							cmdExit,
							this.container,
							this,
							this
							);
				}
				//#endif
				else
				if(parent instanceof PeopleTabMe)
				{
					if(contact != null)
					{	
						//#style contextualmenu_seperator_action_item_base
						ci = UiFactory.createCascadeItem(
								Locale.get("nowplus.client.java.contextual.menu.command.edit.profile"),
								cmdEditContact,
								this.container,
								this,
								this
								);
					}
					
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.add.new.contact"),
							cmdAddNewContact,
							this.container,
							this,
							this
							);
					
					
					//<mobica
					//#if not polish.device.supports.nativesms:defined || polish.device.supports.nativesms == true
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.create.text"),
							cmdCreateText,
							this.container,
							this,
							this
							);
					//#endif
					//mobica>
					
					//#style contextualmenu_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.help"),
							cmdHelp,
							this.container,
							this,
							this
							);
					
					//#style contextualmenu_last_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.exit"),
							cmdExit,
							this.container,
							this,
							this
							);	
					
					//remove undesired icons from navbar
					if(null != profileIcon)
						this.navBar.remove(profileIcon);
					
					//remove undesired icons from navbar
					if(contact instanceof MyProfile)
					{
						//remove chat option if own profile
						if(null != imIcon)
							this.navBar.remove(imIcon);
					}
				}
			}
			
			break;
			
		case Event.Context.PROFILE:
			
			if(contact != null)
			{	
				//#style contextualmenu_action_item_base
				ci = UiFactory.createCascadeItem(
						Locale.get("nowplus.client.java.contextual.menu.command.edit.profile"),
						cmdEditContact,
						this.container,
						this,
						this
						);
				
				if(contact instanceof ContactProfile)
				{
					//#style contextualmenu_seperator_action_item_base
					ci = UiFactory.createCascadeItem(
							Locale.get("nowplus.client.java.contextual.menu.command.delete") + " " +this.contact.getFullName(),
							cmdDeleteContact,
							this.container,
							this,
							this
					);
				}
			}
			
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.add.new.contact"),
					cmdAddNewContact,
					this.container,
					this,
					this
					);
			
			
			//<mobica
			//#if not polish.device.supports.nativesms:defined || polish.device.supports.nativesms == true
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.create.text"),
					cmdCreateText,
					this.container,
					this,
					this
					);
			//#endif
			//mobica>
			
			
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);				
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);	
			
			//remove undesired icons from navbar
			if(null != profileIcon)
				this.navBar.remove(profileIcon);
						
			break;
			
		case Event.Context.WEB_ACCOUNTS:

			if(null != this.contact)
			{
				//#style contextualmenu_profile_item
				csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_GRAY, this);
				csi.setAppearanceMode(Item.PLAIN);
			}

			Identity iden = null;
			
			if(null != objs && objs.length == 1)
			{
				iden = (Identity)objs[0];
			}
			
			if(iden != null)
			{	
				//#style contextualmenu_seperator_action_item_base
				ci = UiFactory.createCascadeItem(
						Locale.get("nowplus.client.java.contextual.menu.command.delete") + " " + iden.getName(),
						cmdDeleteSN,
						this.container,
						this,
						this
						);
			}
			
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);				
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);	
			
			break;
			
		//#if activate.embedded.360email			
		case Event.Context.EMAIL:

			if(parent instanceof ShowEmailForm)
			{
				//#style contextualmenu_action_item_base
				ci = UiFactory.createCascadeItem(
						"Reply",
						cmdReply,
						this.container,
						this,
						this
						);	
				
				//#style contextualmenu_action_item_base
				ci = UiFactory.createCascadeItem(
						"Forward",
						cmdForward,
						this.container,
						this,
						this
						);
				
				//#style contextualmenu_action_item_base
				ci = UiFactory.createCascadeItem(
						"Delete",
						cmdDeleteEmail,
						this.container,
						this,
						this
						);	
			}
			else
			if(parent instanceof NewEmailForm)
			{
				
			}	
			
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);				
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);
			
			if(null != this.contact && !(this.contact instanceof MyProfile))
			{
				//remove profile option unless own profile
				if(null != profileIcon)
					this.navBar.remove(profileIcon);
			}
			
			break;
		//#endif
			
		//#if polish.device.supports.nativesms == false
		case Event.Context.SMS_EDITOR:

			//remove smsIcon option if own profile
			if(null != smsIcon)
				this.navBar.remove(smsIcon);
			//#style contextualmenu_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.help"),
					cmdHelp,
					this.container,
					this,
					this
					);				
			
			//#style contextualmenu_last_action_item_base
			ci = UiFactory.createCascadeItem(
					Locale.get("nowplus.client.java.contextual.menu.command.exit"),
					cmdExit,
					this.container,
					this,
					this
					);	
			
			break;
		//#endif
			
		default:
			break;
		}
		
		//check if entire navbar should be removed
		if(null != this.navBar && this.navBar.size() == 0)
		{
			if(null != infoBar)
				this.container.remove(infoBar);
			if(null != navBar)
				this.container.remove(navBar);
		}
	}
	
	private void updateInfoItem()
	{
		if(null != navBar && focusedNavBarItem != navBar.getFocusedItem())
		{
			Channel ch;
			focusedNavBarItem = navBar.getFocusedItem();
			
			//ensure contact is loaded
			if(this.contact instanceof Profile)
				((Profile)this.contact).load(true);
			
			if(focusedNavBarItem == callIcon)
			{
				ch = ((Profile)this.contact).getPrimaryCallChannel();
				if(null != ch)
					infoBar.setText(ch.getName());	
			}
			else
			if(focusedNavBarItem == smsIcon)
			{
				ch = ((Profile)this.contact).getPrimarySMSChannel();
				if(null != ch)
					infoBar.setText(ch.getName());					
			}
			else
			if(focusedNavBarItem == imIcon)
			{
				ch = ((Profile)this.contact).getPrimaryChatChannel();
				if(null != ch)
					infoBar.setText(ch.getName());	
			}
			else
			if(focusedNavBarItem == emailIcon)
			{
				ch = ((Profile)this.contact).getPrimaryEmailChannel();
				if(null != ch)
					infoBar.setText(ch.getName());	
			}	
			if(focusedNavBarItem == profileIcon)
			{
				infoBar.setText(Locale.get("nowplus.client.java.contextual.menu.infobar.profile"));	
			}
			
			//ensure contact is loaded
			if(this.contact instanceof Profile)
				((Profile)this.contact).unload();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		boolean result = super.handleKeyPressed(keyCode, gameAction);
		
		//update visual state of infobar according to navbar focus state
		if(null != navBar && null != infoBar)
		{
			if(navBar.isFocused && infoBar.getStyle() != focusInfoBar)
				infoBar.setStyle(focusInfoBar);
			if(!navBar.isFocused && infoBar.getStyle() != unfocusInfoBar)
				infoBar.setStyle(unfocusInfoBar);
		}
		
		//handle navbar state and actions
		if( null != navBar && navBar == getCurrentItem() )
			updateInfoItem();
		
		return result;
	}
		
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#handleCommand(de.enough.polish.ui.Command)
	 */
	public void commandAction(Command cmd, Displayable disp) 
	{
		//#ifdef testversion:defined
		if(cmd == cmdClearRMS)
		{
			getController().notifyEvent(Event.Context.APP, Event.App.MARK_FOR_FLUSH_AND_EXIT);
			return;
		}
		else
		//#endif
			
		if(cmd == cmdAddNewContact)
		{
			//passing null to edit form, generates new user
			getController().notifyEvent(getContext(), Event.ContextualMenu.ADD_NEW_CONTACT, null);
		}
		else
		if(cmd == cmdCreateText)
		{
			getController().notifyEvent(getContext(), Event.ContextualMenu.CREATE_TEXT, null);
		}
		else
		if(cmd == cmdHelp)
		{
			//#ifdef polish.device.requires.polish.browser
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
			//#endif
			getController().notifyEvent(Event.Context.APP, Event.App.SHOW_HELP, null);
		}
		else
		if(cmd == cmdExit)
		{
			getController().notifyEvent(Event.Context.APP, Event.App.EXIT);
		}
		else
		if(cmd == cmdBack)
		{
			//go back to last checkpoint
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
		}
		else
			//#if polish.blackberry && add.switch.application.form
			if(cmd == cmdSwitchApps)
				getController().notifyEvent(Event.Context.APP, Event.App.SHOW_LIST_SWITCH_APPS, null);
			else
			//#endif
		if(handleContextCommand(cmd, this.contentContext))
		{
			return;
		}
		else
			//#if polish.blackberry.isTouchBuild == true
			if(cmd == cmdShowSearchbar){
				//go back to last checkpoint
				
				if(parent instanceof PeopleTabContacts){
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
					
					((PeopleTabContacts)parent).showLocalSearchField();
					
					
				}
			}
			else
			//#endif
		super.commandAction(cmd, disp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command cmd, Item item) 
	{
		//launch channel communication
		Channel ch;
		if(cmd == cmdCall)
		{
			if(this.contact instanceof Profile)
				((Profile)this.contact).load(true);
			
			ch = this.contact.getPrimaryCallChannel();
			
			if(this.contact instanceof Profile)
				((Profile)this.contact).unload();
			
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.CHANNEL_CALL, ch);
		}
		else
		if(cmd == cmdSms)
		{
			//#if polish.device.supports.nativesms == false
			getController().notifyEvent(Event.Context.SMS_EDITOR, Event.SmsEditor.OPEN, this.contact);
			//#else
			if(this.contact instanceof Profile)
				((Profile)this.contact).load(true);			
			
			ch = ((Profile)this.contact).getPrimarySMSChannel();
			
			if(this.contact instanceof Profile)
				((Profile)this.contact).unload();
			
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.CHANNEL_SMS, ch);
			//#endif
		}
		else
		if(cmd == cmdIm)
		{
			if(this.contact instanceof Profile)
				((Profile)this.contact).load(true);
			
			ch = ((Profile)this.contact).getPrimaryChatChannel();
			
			if(this.contact instanceof Profile)
				((Profile)this.contact).unload();
			
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.CHANNEL_CHAT, ch);
		}
		else
		if(cmd == cmdEmail)
		{
			if(this.contact instanceof Profile)
				((Profile)this.contact).load(true);
			
			ch = ((Profile)this.contact).getPrimaryEmailChannel();
			
			if(this.contact instanceof Profile)
				((Profile)this.contact).unload();
			
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.CHANNEL_EMAIL, ch);
		}	
		else
		if(cmd == cmdProfile)
		{
			if(Event.Context.CHAT != this.contentContext)
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.VIEW_PROFILE, contact);
			else
				//Special handling to avoid circular profile->chat->profile page traversal
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.PROFILE_FROM_CHAT, this.contact);
		}
		else
		if(handleContextCommand(cmd, this.contentContext))
		{
			return;
		}
		else
			super.commandAction(cmd, item);
	}
	
	/**
	 * Handles Commands specific to the given context.
	 * 
	 * @param cmd
	 * @param context
	 * @return true if Command was handled, false otherwise
	 */
	private boolean handleContextCommand(Command cmd, int context)
	{
		boolean result = false;
		
		switch (context)
		{
		case Event.Context.EDIT_PROFILE:
			
			if(cmd == cmdDeleteDetail)
			{
				//envoke confirmation
				Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
				Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				
				String detailSuffix = null;
				Event event = null;
				
				if(null != objs && objs.length == 2)
				{
					detailSuffix = (String)objs[0];
					detailSuffix = null != detailSuffix ? detailSuffix : "";
					event = (Event)objs[1];
				}
				
				//#style notification_form_delete
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(), 
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.detail.title"),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.detail.text", detailSuffix),
						ok, cancel,
						event);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				
				result = true;
			}
			
			break;
			
		case Event.Context.CONTACTS:
			
			if(cmd == cmdEditContact)
			{
				getController().notifyEvent(Event.Context.EDIT_PROFILE, Event.EditProfile.OPEN, this.contact);
				
				result = true;
			}
			else 
			if(cmd == cmdProfile)
			{
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.VIEW_PROFILE, this.contact);
				
				result = true;
			}
			else
			if(cmd == cmdOpenFilter)
			{
				getController().notifyEvent(Event.Context.CONTACTS, Event.Contacts.OPEN_FILTER);
				
				//back to last checkpoint
				getController().notifyEvent(Event.Context.NAVIGATION,Event.Navigation.BACK_CHECKPOINT,null);
				
				result = true;
			}
			
			//#if activate.embedded.360email
			
			else
			if(cmd == cmdNewEmail)
			{
				//back to last checkpoint
				getController().notifyEvent(Event.Context.EMAIL,Event.Email.COMPOSE_EMAIL,null);
			}
			else
			if(cmd == cmdGotoFolders)
			{
				
			}
			else
			if(cmd == cmdReply)
			{
				
			}
			else
			if(cmd == cmdForward)
			{
				
			}
			else
			if(cmd == cmdDeleteEmail)
			{
				
			}
						
			//#endif
			
			//#if polish.blackberry
			else
				if(cmd == cmdDeleteContact)
				{
					//envoke confirmation
					Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
					Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
					//event to fire upon confirmation
					Event event = new Event(getContext(),Event.ContextualMenu.DELETE_CONTACT,this.contact);
					
					 String formattedName = this.contact.getFullName();
					 
					//#style notification_form_delete
					ConfirmationForm cf = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.title"),
							Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.text",formattedName),
							ok, cancel,
							event);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
					
					result = true;
				}
			//#endif
			break;
			
		case Event.Context.PROFILE:
			
			if(cmd == cmdEditContact)
			{
				getController().notifyEvent(Event.Context.EDIT_PROFILE, Event.EditProfile.OPEN, this.contact);
				
				result = true;
			}
			else
			if(cmd == cmdDeleteContact)
			{
				//envoke confirmation
				Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
				Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				//event to fire upon confirmation
				Event event = new Event(getContext(),Event.ContextualMenu.DELETE_CONTACT,this.contact);
				
				 String formattedName = this.contact.getFullName();
				 
				//#style notification_form_delete
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.title"),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.text",formattedName),
						ok, cancel,
						event);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				
				result = true;
			}
			
			break;
			
		case Event.Context.WEB_ACCOUNTS:
			
			if(cmd == cmdDeleteSN)
			{
				//envoke confirmation
				Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
				Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				
				Identity iden = null;
				
				if(null != objs && objs.length == 1)
					iden = (Identity)objs[0];
				
				//event to fire upon confirmation
				Event event = new Event(getContext(), Event.ContextualMenu.DELETE_SN, iden);
				
				String name;
				if(null != iden)
					name = iden.getName();
				else
					name="";
				
				//#style notification_form_delete
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.sn.title"),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.sn.text1",name),
						ok, cancel,
						event);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				
				result = true;
			}
			
			break;

		default:
			break;
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#getContext()
	 */
	public byte getContext() 
	{
		return Event.Context.CONTEXTUAL_MENU;
	}
	
	//#if polish.blackberry.isTouchBuild == true
// Commented to fix bug 0014525...scrolling of contextual menu in Storm build. Need to remove this if not required.
//	Container touchContainer;
//	public void initContentForTouchDevice(){
//		 for(int i = 0; i < getRootContainer().getItems().length; i++){
//             if((getRootContainer().getItems()[i]).equals(this.csi)){
//                  getRootContainer().remove(i);
//                  
//               }
//          }
//        this.setTitle(this.csi); 
//        
//		 
//	}
//	
	//#endif

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseForm#showNotify()
	 */
	public void showNotify() 
	{
		// Allow screenchangeanimation to close the ContextualMenuForm for BB
		// fix for: 0014390 
		//#if !polish.blackberry
		if(beenShown)
			UiAccess.enableScreenChangeAnimation(this,false);
		else
			beenShown = true;
		//#endif
		super.showNotify();
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
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode) 
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			commandAction(cmdBack, this); 
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
