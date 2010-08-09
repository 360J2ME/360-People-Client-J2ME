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

import java.util.Calendar;
import java.util.Date;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandItem;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Debug;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
import com.zyb.nowplus.presentation.BlackBerryOptionsHandler;
//#endif

/**
 * The 'Details' tab of the Profile Pages.
 * <p>
 * Implements layout of the core details associated to a contact.
 * <p>
 * ManagedProfile methods that can be called WITOHOUT using load/unload paradigm:
 * getNowPlusMember()
 * getNowPlusPresence()
 * get*Name()
 * getGroups()
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 * @author Jens Vesti, jens@zyb.com
 */
public class ProfileForm extends BaseFramedForm
//#if polish.blackberry
implements BlackBerryOptionsHandler
//#endif
{
	/**
	 * Call key attribute key. Used to store object reference when launching platform requests by
	 * press of green call key.
	 */
	private static final String CALL_KEY = "CK";
	
	/**
	 * Generel attribute key. Used to store object reference used when pressing FIRE key.  
	 */
	private static final String KEY = "K";
	
	/**
	 * Delete attribute key. Used to store object reference used when deleting contact details
	 * by press of c-key. 
	 */
	private static final String DELETE_KEY = "DK";
	
	/**
	 * Profile view details containers
	 */
	protected Container phonesContainer;
	protected Container imsContainer;
	protected Container emailsContainer;
	protected Container addressContainer;
	protected Container groupsContainer;
	protected Container urlsContainer;
	protected Container birthdayContainer;
	protected Container networksContainer;
	protected Container notesContainer;
	protected Container workContainer;

	//#if use-connect-invite == true
	protected Container inviteConnectContainer;
	private StringItem connectItem,inviteItem;
	//#endif

	/**
	 * Commands
	 */
	public static final Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
	private static final Command cmdSelect = new Command(Locale.get("polish.command.select"),Command.ITEM,0);
	private static final Command cmdOpenUrl = new Command(Locale.get("nowplus.client.java.profilepage.content.command.open"),Command.ITEM,0);
	private static final Command cmdCall = new Command(Locale.get("nowplus.client.java.profilepage.content.command.call"),Command.OK,0);
	private static final Command cmdSMS = new Command(Locale.get("nowplus.client.java.profilepage.content.command.sms"),Command.OK,0);
	private static final Command cmdMMS = new Command(Locale.get("nowplus.client.java.profilepage.content.command.mms"),Command.OK,0);
	private static final Command cmdChat = new Command(Locale.get("nowplus.client.java.profilepage.content.command.chat"),Command.ITEM,0);
	private static final Command cmdWrite = new Command(Locale.get("nowplus.client.java.profilepage.content.command.write"),Command.ITEM,0);
	private static final Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);

	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	
	/**
	 * Contact id item 
	 */
	protected ContactSummarizedItem csi;
	
	/**
	 * Reference to profile object to be displayed. 
	 */
	protected ManagedProfile profile;
	
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param profile
	 */
	public ProfileForm(Model model, Controller controller, ManagedProfile profile)
	{
		this(model, controller, profile, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param c
	 * @param style
	 */
	public ProfileForm(Model model, Controller controller, ManagedProfile profile, Style style)
	{
		super(model, controller, null, style);
		
		this.profile = profile;
		
		//add existing contact data to form if any
		this.updateContact(this.profile);
		
		//#if not polish.blackberry
			addCommand(cmdOptions);
			addCommand(cmdBack);
		//#endif
		
		this.setCommandListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#getContext()
	 */
	public byte getContext()
	{
		return Event.Context.PROFILE;
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
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createStaticItems()
	 */
	protected Item[] createStaticItems() 
	{
		if(null != this.profile)
		{
			if(null == this.titleitem)
			{
				if(profile.getNowPlusMember() == Profile.NOWPLUS_ME)
					this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.peoplepage.meview.myprofile"),getModel() );
				else
					this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.profilepage.tab.details"),getModel() );
				
				this.titleitem.setAppearanceMode(Item.PLAIN);
			}
			
			if(null == csi)
			{
				this.csi = UiFactory.createUserProfileItem(this.profile, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ONLINE_GRAY, null);
				
				this.csi.setAppearanceMode(Item.PLAIN);
			}
		}
		
		return new Item[]
		                {
				this.titleitem,
				this.csi
				};
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseProfileForm#commandAction(de.enough.polish.ui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable disp)
	{	
		//#debug debug
		System.out.println("commandAction(disp):"+c+"/"+disp);

		if(c == cmdOptions)
		{
			//if a CommandItem sub menu is open, close it instead of going back
			Item itm = getCurrentItem();
			if(null != itm && itm == phonesContainer)
			{
				Item item = ((Container)itm).getFocusedItem();
				if(null != item && item instanceof CommandItem && ((CommandItem)item).isOpen())
				{
					((CommandItem)item).open(false);
					return;
				}
			}
			
			//launch contextual menu
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, this.profile);
		}
		else
		if(cmdSMS==c || cmdCall==c || cmdMMS==c)
		{
			//#debug debug
			System.out.println("cmdSMS|cmdCall|cmdMMS");
			
			//Fecthing current item which would have triggered one of these commands
			Item currentItem = getCurrentItem();
			if(currentItem instanceof Container)
				currentItem = ((Container)currentItem).getFocusedItem();
			
			if(cmdSMS == c){
				//#if polish.device.supports.nativesms == false
				//#debug debug
				System.out.println("SMS CALLED NATIVE==FALSE");
				//profile.load(true);
				getController().notifyEvent(Event.Context.SMS_EDITOR, Event.SmsEditor.OPEN, ((Identity)currentItem.getAttribute(KEY)).getChannel(Channel.TYPE_SMS));
				//#else
				//#debug debug
				System.out.println("SMS CALLED NATIVE==TRUE");
				getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, ((Identity)currentItem.getAttribute(KEY)).getChannel(Channel.TYPE_SMS));
				//#endif
			}
			else
			if(cmdMMS == c)
				getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, ((Identity)currentItem.getAttribute(KEY)).getChannel(Channel.TYPE_MMS));
			else
			if(cmdCall == c)
				getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, ((Identity)currentItem.getAttribute(KEY)).getChannel(Channel.TYPE_CALL));
				
			((CommandItem)currentItem).open(false);
			
			//#debug debug
			System.out.println("currentItem:"+currentItem);
		}
		else
		if(c == cmdGreenCallKey)
		{
			Item currentItem = getCurrentItem();
			if(currentItem != null && currentItem instanceof Container)
				currentItem = ((Container)currentItem).getFocusedItem();
			
			//#debug debug
			System.out.println("getCurrentItem():"+currentItem);
			//#debug debug
			System.out.println("getCurrentItem().getAttribute(CALLKEY):"+currentItem.getAttribute(CALL_KEY));

			if(null != currentItem && currentItem.getAttribute(CALL_KEY) != null)
			{
				//#debug debug
				System.out.println("Call key command:"+currentItem.getAttribute(CALL_KEY));				
				getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, currentItem.getAttribute(CALL_KEY));
			}
			else
			{
				//#debug debug
				System.out.println("Call key command primary");		
				
				this.profile.load(true);
				getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, this.profile.getPrimaryCallChannel());
				this.profile.unload();
			}
		}
		else
		if(c == cmdClearKey)
		{
			Item itm = getCurrentItem();
			
			if(null != itm && itm != networksContainer && itm != imsContainer)
			{
				//envoke confirmation
				Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
				Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				
				String currentDetail = getCurrentDetailString();
				
				//#style notification_form_delete
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.detail.title"),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.detail.text", currentDetail),
						ok, cancel,
						new Event(getContext(), Event.Profile.DELETE_DETAIL, null));
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
			}
		}
		else
		if(c == cmdBack)
		{
			//if a CommandItem sub menu is open, close it instead of going back
			Item itm = getCurrentItem();
			if(null != itm && itm == phonesContainer)
			{
				Item item = ((Container)itm).getFocusedItem();
				if(null != item && item instanceof CommandItem && ((CommandItem)item).isOpen())
				{
					((CommandItem)item).open(false);
					//#if polish.blackberry
					phonesContainer.requestInit();
					//#endif
					return;
				}
			}
			
			//notify model that possible editing of this contact is complete
			//Nessesary in case details has been deleted with c-key
			// Change: continous sync doesn't handle deleted attributes
			// getModel().finishedEditing(this.contact);
			
			//back to checkpoint
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
		}		
		else
			super.commandAction(c, disp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item)
	{
		//#debug debug
		System.out.println("commandAction(item):"+c+"/"+item);

		if(cmdCall==c || cmdWrite==c)
		{
			Object obj = item.getAttribute(KEY);
			getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, obj);
			return;
		}
		else
		if(cmdOpenUrl==c)
		{
			Object obj = item.getAttribute(KEY);
			getController().notifyEvent(Event.Context.APP, Event.App.BROWSE, obj);
			return;
		}
		else		
		if(cmdChat==c)
		{
			Object obj = item.getAttribute(KEY);
			getController().notifyEvent(Event.Context.CHAT, Event.Chat.OPEN, obj);
			return;
		}
		//#if use-connect-invite == true
		else
			if(cmdSelect==c)
			{
				if(item == this.inviteItem)
					getController().notifyEvent(Event.Context.CONTACTS,Event.Contacts.INVITE,this.profile);
				else
				if(item == this.connectItem)
					getController().notifyEvent(Event.Context.CONTACTS,Event.Contacts.CONNECT,this.profile);

				return;
			}
		//#endif
		else
			super.commandAction(c, item);
	}
	
	/**
	 * Common creator method used for all profile detail entries.
	 * 
	 * @param label
	 * @param text
	 * @param reference
	 * @param style
	 * @return
	 */
	protected StringItem createDetailEntry(String label, String text)
	{
		//#style .profilepage_content_item
		StringItem si = new StringItem(label, text);
		si.setAppearanceMode(Item.INTERACTIVE);
		si.setItemCommandListener(this);
		
		return si;
	}
	
	protected Item createNetworkDetailEntry(String networkId, String label, String text)
	{
		return createNetworkDetailEntry(false, -1, networkId, label, text, null);
	}
	
	protected Item createNetworkDetailEntry(String networkId, String label, String text, Style style)
	{
		return createNetworkDetailEntry(false, -1, networkId, label, text, style);
	}

	protected Item createNetworkDetailEntry(int presence, String networkId, String label, String text)
	{
		return createNetworkDetailEntry(true, presence, networkId, label, text, null);
	}
	
	protected Item createNetworkDetailEntry(int presence, String networkId, String label, String text, Style style)
	{
		return createNetworkDetailEntry(true, presence, networkId, label, text, style);
	}
	
	protected Item createNetworkDetailEntry(boolean considerPresence, int presence, String networkId, String label, String text, Style style)
	{
		Container cont = null;
		IconItem presenceIcon = null;

		if(considerPresence)
		{			
			//only display online or invisible (grey) states
			if(Channel.PRESENCE_ONLINE == presence ||Channel.PRESENCE_IDLE == presence)
			{
				//#style .profilepage_content_plain_icon_item
				presenceIcon= UiFactory.createPresenceIcon(presence);
			}
			else
			/*
			if(Channel.PRESENCE_IDLE == presence ||
					Channel.PRESENCE_INVISIBLE == presence ||
					Channel.PRESENCE_OFFLINE == presence
			)
			*/
			{
				//#style .profilepage_content_plain_icon_item
				presenceIcon = UiFactory.createPresenceIcon(Channel.PRESENCE_INVISIBLE);
			}
		}
		
		//is item chat item or sn item?
		if(null == presenceIcon)
			//#style .profilepage_content_sn_item_container
			cont = new Container(false);
		else
			//#style .profilepage_content_chat_item_container
			cont = new Container(false);
		
		if(presenceIcon != null)
		{
			presenceIcon.setAppearanceMode(Item.PLAIN);
			cont.add(presenceIcon);
		}
		
		IconItem networkIcon = UiFactory.createNetworkIcon(networkId, false);
		
		if(networkIcon != null)
		{
			networkIcon.setAppearanceMode(Item.PLAIN);
			cont.add(networkIcon);
		}
		else
			//#style .profilepage_content_plain_icon_item
			cont.add(" ");

		StringItem si = new StringItem(label, text, style);
		cont.add(si);

		cont.setAppearanceMode(Item.INTERACTIVE);
		cont.setItemCommandListener(this);
		
		return cont;
	}

	/**
	 * Common creator method used for all 'nested' profile detail entries.
	 * 
	 * @param label
	 * @param text
	 * @param reference
	 * @param style
	 * @return
	 */
	protected Container createNestedDetailsContainer()
	{
		//#style .profilepage_content_container_nested
		Container c = new Container(false);
		c.setAppearanceMode(Item.INTERACTIVE);
		
		return c;
	}
	
	/**
	 * Common creator method used for all 'nested' profile detail entries.
	 * 
	 * @param label
	 * @param text
	 * @param reference
	 * @param style
	 * @return
	 */
	protected StringItem createNestedDetailEntry(String label, String text)
	{
		//#style .profilepage_content_item_nested
		StringItem si = new StringItem(label, text);
		si.setAppearanceMode(Item.PLAIN);
		return si;
	}	
	
	/**
	 * Updates the contents of the profile view form based on the passed ContactProfile
	 * instance. The method is called during construction.
	 * <p>
	 * Old entries will be removed.
	 * 
	 * @param profile is the contact we wish to update this form with
	 */
	public void updateContact(final ManagedProfile profile) 
	{
		//#debug debug
		System.out.println("updateContact("+profile+")");
		
		if(null != profile)
		{
			this.profile = profile;
			
			this.profile.load(true);
			
			synchronized(getPaintLock())
			{
				//#if use-connect-invite == true
				//add invite item if needed
				if(profile instanceof ContactProfile)
					updateInviteConnect();
				//#endif
				
				//update ContactSummarizedItem if any
				if(null!=this.csi)
					this.csi.setContact(this.profile);
				
				phonesContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.callorsms"), phonesContainer, Identity.TYPE_PHONE);
				
				//only chat ims for contact profiles
				if(profile instanceof ContactProfile)
				{
					imsContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.im"), imsContainer, Identity.TYPE_IM_ACCOUNT);
				}
				
				emailsContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.email"), emailsContainer, Identity.TYPE_EMAIL);
				
				networksContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.socialnetwork"), networksContainer, Identity.TYPE_SN_ACCOUNT);
				
				addressContainer = updateProfileContentFromAddresses(Locale.get("nowplus.client.java.profilepage.content.address"), this.profile.getAddresses(), addressContainer);
				
				//only groups for contact profiles
				if(profile instanceof ContactProfile)
				{
					groupsContainer = updateProfileContentFromGroups(Locale.get("nowplus.client.java.profilepage.content.groupes"),((ContactProfile)this.profile).getGroups(),groupsContainer);
				}
				
				urlsContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.url"), urlsContainer, Identity.TYPE_URL);
				
				birthdayContainer = updateProfileContentFromBirthday(Locale.get("nowplus.client.java.profilepage.content.birthday"), this.profile.getDateOfBirth(), birthdayContainer);

				Note note = this.profile.getNote();
				
				notesContainer = updateProfileContentFromNotes(Locale.get("nowplus.client.java.profilepage.content.notes"),new String[] {(note == null) ? "" : note.getContent()},notesContainer);
				
				workContainer = updateProfileContentFromWorkInfo(Locale.get("nowplus.client.java.profilepage.content.work"),this.profile.getWorkDetails(),workContainer);
				
				//remove containers that do not contribute anything
				removeEmptyDetailContainers();
			}
			
			this.profile.unload();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#init(int, int)
	 */
	protected void init(int width, int height)
	{
		super.init(width, height);
		
		//make sure first item in list, besides invite/conntect, is focused

		if(this.container.size()>0)
			this.focus(0);

		//#if use-connect-invite == true
		if(this.getCurrentItem() == inviteConnectContainer && this.container.size()>1)
			this.focus(this.getCurrentIndex()+1);
		//#endif
	}
	
	/**
	 * Removes detail entry containers that have no content.
	 */
	protected void removeEmptyDetailContainers()
	{
		for(int i = this.container.size();--i>=0;)
			if( ((Container)this.container.get(i)).size() == 0)
				this.container.remove(i);
	}

	/**
	 * Adds detail entries to the profile form based on presence of Identity members.
	 * 
	 * @param label
	 * @param container
	 * @param type
	 * @return
	 */
	protected Container updateProfileContentFromIdentities(String label, Container container, int type) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			String typeString = null;
			switch (type)
			{
				case Identity.TYPE_PHONE:
				typeString = "phone";
				break;
				case Identity.TYPE_EMAIL:
				typeString = "email";
				break;
				case Identity.TYPE_URL:
				typeString = "url";
				case Identity.TYPE_IM_ACCOUNT:
				typeString = "im";
				case Identity.TYPE_SN_ACCOUNT:
				typeString = "sn";
				break;
			}
			container = UiFactory.createProfilePageDetailContainer(typeString);
			container.setLabel(label);
			this.append(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		//special handling for url entries
		Identity[] identities = null;
		if(type == Identity.TYPE_URL)
		{
			if (this.profile.getUrl() == null)
			{
				identities = new Identity[0];
			}
			else
			{
				identities = new Identity[]{ this.profile.getUrl() };
			}
		}
		else
		{
			identities = this.profile.getIdentities(type);
		}
		Identity iden;
		if(null != identities && identities.length > 0)
		{
			for(int i=0; i<identities.length; i++)
			{
				iden = identities[i];
				Item itm = createItemFromIdentity(iden,type);
				if(null !=itm)
				{
					itm.setAttribute(DELETE_KEY, iden);
					container.add(itm);
				}
			}
		}
		
		return container;
	}
	
	/**
	 * Helper method for updateProfileContentFromIdentities(). Creates identity detail members
	 * according to type.
	 * 
	 * @param identity
	 * @param type
	 * @return
	 */
	protected Item createItemFromIdentity(Identity identity, int type) 
	{
		Item item = null;
		boolean isPrimary = false;
		boolean overwriteStyle = true;
		
		if (null != identity) {
			if(type == Identity.TYPE_PHONE)
			{
				isPrimary = identity.isPreferred();
				
				//#style profilepage_content_item
				CommandItem parent = UiFactory.createCascadeItem(identity.getName(), new Command(Locale.get("polish.command.select"),Command.ITEM,0), this.container, this, null);
				
				parent.setLabel(subTypeToString(identity.getSubtype()));
				parent.setAttribute(KEY, identity);

				if (identity.getProfile().getNowPlusMember() != Profile.NOWPLUS_ME) {
					parent.setAppearanceMode(Item.INTERACTIVE);
					
					//#style profilepage_content_cascade_item
					UiFactory.createCascadeItem(null, cmdCall, parent, this, null);
					
					//#style profilepage_content_cascade_item
					UiFactory.createCascadeItem(null, cmdSMS, parent, this, null);
					
					//#if not polish.device.supports.nativemms:defined || polish.device.supports.nativemms == true
						//#style profilepage_content_cascade_item
						UiFactory.createCascadeItem(null, cmdMMS, parent, this, null);
					//#endif
				}

				item = parent;
			}
			else
			if(type == Identity.TYPE_EMAIL)
			{
				isPrimary = identity.isPreferred();
				
				item = createDetailEntry(subTypeToString(identity.getSubtype()),identity.getName());
				//#style profilepage_content_item_mail
				UiAccess.setStyle(item);
				item.setAttribute(KEY, identity.getChannel(Channel.TYPE_EMAIL));
				//#if not polish.device.supports.nativeemail == false
				item.setAttribute(CALL_KEY, identity.getChannel(Channel.TYPE_EMAIL));
				//#endif
				
				//#if not polish.blackberry
					//#if polish.device.supports.nativeemail == false
					//#debug info
					System.out.println("Device does not support native SMS editor. 'Write' command removed");
					//#else
					item.addCommand(cmdWrite);
					//#endif
				//#endif
				
				//#if polish.blackberry
				item.setDefaultCommand(cmdWrite);
				/*
				//#style .profilepage_content_sn_item_container
				Container cont = new Container(false);
				cont.add(item);
				item = cont;
				*/
				//#endif
			}
			else
			if(type == Identity.TYPE_IM_ACCOUNT)
			{
				isPrimary = identity.isPreferred();
				
				overwriteStyle = false;
				
				int presence = identity.getPresence();
				String networkId = identity.getNetworkId();
				
				//returning null if there is no chat information.
				if(identity.getName()!=null && identity.getName().equals("NoIMAddress")){
					return item;
				}
				
				if(isPrimary)
					//#style .profilepage_content_chat_item_preferred
					item = createNetworkDetailEntry(presence, networkId, null, identity.getName());
				else
					//#style .profilepage_content_chat_item
					item = createNetworkDetailEntry(presence, networkId, null, identity.getName());
				
				item.setAttribute(KEY, identity.getChannel(Channel.TYPE_CHAT));
				item.setAttribute(CALL_KEY, identity.getChannel(Channel.TYPE_CHAT));
				
				
				//#if polish.blackberry
					if(profile.getNowPlusMember() == Profile.NOWPLUS_ME)
						item.setDefaultCommand(cmdFake);
					else
						item.setDefaultCommand(cmdChat);
				//#else
					if(profile.getNowPlusMember() == Profile.NOWPLUS_ME)
						item.addCommand(cmdFake);
					else
						item.addCommand(cmdChat);
				//#endif
			}
			else
			if(type == Identity.TYPE_URL)
			{
				item = createDetailEntry(null,identity.getName());
				item.setAttribute(KEY, identity.getChannel(Channel.TYPE_BROWSE));
				item.setAttribute(CALL_KEY, identity.getChannel(Channel.TYPE_BROWSE));
				
				//#if polish.blackberry
					item.setDefaultCommand(cmdOpenUrl);
				//#else
					//#if not (polish.device.requires.nonativebrowser:defined && polish.device.requires.nonativebrowser==true)
						item.addCommand(cmdOpenUrl);
					//#endif
				//#endif
			}
			else
			if(type == Identity.TYPE_SN_ACCOUNT)
			{
				overwriteStyle = false;
				
				//#style .profilepage_content_chat_item
				item = createNetworkDetailEntry(identity.getNetworkId(), null,identity.getName());
				item.setAttribute(KEY, identity.getChannel(Channel.TYPE_BROWSE));
				
				// Disabled due to PBLA-997. PO decided to don't open a website at all for this item.
				//#if polish.blackberry
					item.setDefaultCommand(cmdFake);
				//#else
					//#if not (polish.device.requires.nonativebrowser:defined && polish.device.requires.nonativebrowser==true)
						item.addCommand(cmdFake);
					//#endif
				//#endif
			}
		}

		if(overwriteStyle && null != item)
		{
			//Following sets style depending on preferred or not
			if(isPrimary)
			{
				//#style .profilepage_content_item_preferred
				item.setStyle();
			}
			else
			{
				//#style .profilepage_content_item
				item.setStyle();
			}
		}
		
		return item;
	}
	
	/**
	 * Helper method for updateProfileContentFromIdentities().
	 * 
	 * @param subtype from the model which is translated into a string
	 * @return
	 */
	protected String subTypeToString(int subtype) 
	{
		switch(subtype)
		{
			case Identity.SUBTYPE_HOME: return Locale.get("nowplus.client.java.profilepage.subtype.home");
			case Identity.SUBTYPE_MOBILE: return Locale.get("nowplus.client.java.profilepage.subtype.mobile");
			case Identity.SUBTYPE_WORK: return Locale.get("nowplus.client.java.profilepage.subtype.work");
			case Identity.SUBTYPE_FAX: return Locale.get("nowplus.client.java.profilepage.subtype.fax");
			case Identity.SUBTYPE_OTHER: return Locale.get("nowplus.client.java.profilepage.subtype.other");
		}
		return " ";
	}

	//#if use-connect-invite == true
	/**
	 * Append and display 'invite item'
	 */
	protected void updateInviteConnect() 
	{
		boolean addInviteConnect = false;
		boolean removeInviteConnect = true;
		
		//only add invite possibility if not already a member
		if( this.profile.getNowPlusMember() == ContactProfile.NOWPLUS_CONTACT && this.profile.getEmailOrMSISDN() != null)
		{
			if(inviteConnectContainer == null)
			{
				//#style .profilepage_invite_container
				inviteConnectContainer = new Container(true);
				addInviteConnect = true;
			}
			else
			{
				inviteConnectContainer.clear();
			}

			//#style .profilepage_invite_labeltext
			inviteItem = new StringItem(null,Locale.get("nowplus.client.java.profilepage.invite.item.label"));
			inviteItem.setAppearanceMode(Item.INTERACTIVE);
			
			//#if polish.blackberry
				inviteItem.setDefaultCommand(cmdSelect);
			//#else
				inviteItem.addCommand(cmdSelect);
			//#endif
				
			inviteItem.setItemCommandListener(this);
			inviteConnectContainer.add(inviteItem);

			//#style .profilepage_invite_icon
			final StringItem iconItem = new StringItem(null,null);
			inviteConnectContainer.add(iconItem);

			//#style .profilepage_invite_text
			final StringItem textItem = new StringItem(null,Locale.get("nowplus.client.java.profilepage.invite.item.text"));
			inviteConnectContainer.add(textItem);
			
			removeInviteConnect = false;
		}
		else
		//only add connect possibility if not already connected
		if( this.profile.getNowPlusMember() == ContactProfile.NOWPLUS_MEMBER )
		{
			if(inviteConnectContainer == null)
			{
				//#style .profilepage_invite_container
				inviteConnectContainer = new Container(true);
				addInviteConnect = true;
			}
			else
			{
				inviteConnectContainer.clear();
			}

			//#style .profilepage_invite_labeltext
			connectItem = new StringItem(null,Locale.get("nowplus.client.java.profilepage.connect.item"));
			
			connectItem.setAppearanceMode(Item.INTERACTIVE);
//			connectItem.setDefaultCommand(cmdSelect);
			
			//#if polish.blackberry
				connectItem.setDefaultCommand(cmdSelect);
			//#else
				connectItem.addCommand(cmdSelect);
			//#endif
			
			connectItem.setItemCommandListener(this);
			inviteConnectContainer.add(connectItem);

			removeInviteConnect = false;
		}

		if(inviteConnectContainer != null)
		{
			if(addInviteConnect)
				this.append(inviteConnectContainer);

			if(removeInviteConnect)
			{
				this.remove(inviteConnectContainer);
				inviteConnectContainer = null;
			}
		}
	}
	//#endif
	
	/**
	 * Adds detail entries to the profile form based on arrays of strings.
	 * 
	 * @param label is label of the container
	 * @param texts are each text string we want to display
	 * @param container is the container each string will belong to
	 * @return same as argument container or a newly created 
	 */
	protected Container updateProfileContentFromNotes(final String label, final String[] texts, Container container) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			container = UiFactory.createProfilePageDetailContainer("note");
			container.setLabel(label);
			this.append(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		if(null != texts && texts.length > 0)
		{
			Item tmp;
			for(int i=0; i<texts.length; i++)
			{
				if(null != texts[i] && !texts[i].toString().trim().equalsIgnoreCase(""))
				{
					tmp = createDetailEntry(null,texts[i]);
					container.add(tmp);
				}
			}
		}
		
		return container;
	}

	/**
	 * Adds detail entries to the profile form based on contact birthday Data object.
	 * 
	 * @param label
	 * @param bday
	 * @param container
	 * @param event
	 * @return
	 */
	protected Container updateProfileContentFromBirthday(final String label, final Date bday, Container container) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			container = UiFactory.createProfilePageDetailContainer("birthday");
			container.setLabel(label);
			this.append(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		if(null != bday)
		{
			//format date string. E.g. "11 March 2009"
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bday);
            String dateOfBirth = calendar.get(Calendar.DAY_OF_MONTH)
                                 + " "
                                 + TextUtilities.getMonthFullName(calendar.get(Calendar.MONTH) + 1, "")
                                 + " "
                                 + calendar.get(Calendar.YEAR);

			StringItem si = createDetailEntry( null,dateOfBirth);
			
			container.add(si);
		}
		
		return container;
	}
	
	/**
	 * Adds detail entries to the profile form based on contact work information.
	 * 
	 * @param label
	 * @param work
	 * @param container
	 * @param event
	 * @return
	 */
	protected Container updateProfileContentFromWorkInfo(final String label, final String[] work, Container container) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			container = UiFactory.createProfilePageDetailContainer("work");
			container.setLabel(label);
			this.add(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		if(null != work && work.length > 0)
		{
			Container ac = createNestedDetailsContainer();
			
			StringItem workDetail;
			
			//add work details if present
			if(null != work[0] && !work[0].toString().trim().equalsIgnoreCase(""))
			{
				workDetail = createNestedDetailEntry( Locale.get("nowplus.client.java.profilepage.content.work.company"),work[0]);
				ac.add(workDetail);
			}
			if(null != work[1] && !work[1].toString().trim().equalsIgnoreCase(""))
			{
				workDetail = createNestedDetailEntry( Locale.get("nowplus.client.java.profilepage.content.work.department"),work[1]);
				ac.add(workDetail);
			}
			if(null != work[2] && !work[2].toString().trim().equalsIgnoreCase(""))
			{
				workDetail = createNestedDetailEntry( Locale.get("nowplus.client.java.profilepage.content.work.title"),work[2]);
				ac.add(workDetail);
			}
			
			if(ac.size()>0)
				container.add(ac);
		}
		
		return container;
	}
	
	/**
	 * Adds detail entries to the profile form based on contact addresses.
	 * 
	 * @param label
	 * @param addresses
	 * @param container
	 * @return
	 */
	protected Container updateProfileContentFromAddresses(final String label, final Address[] addresses, Container container) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			container = UiFactory.createProfilePageDetailContainer("adress");
			container.setLabel(label);
			this.append(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		if(null != addresses && addresses.length > 0)
		{
			for(int i=0; i<addresses.length; i++)
			{
				Address address = addresses[i];
				StringItem addressDetail;
				
				Container ac = createNestedDetailsContainer();
				
				String type = getAdressTypeString(address);
				boolean haveAddedType = false;

				//add address details if present, only add adress type once for first present
				if(null != address.getPOBox() && !address.getPOBox().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:" "), address.getPOBox() );
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				if(null != address.getStreet() && !address.getStreet().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null), address.getStreet() );
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				if(null != address.getRegion() && !address.getRegion().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null),address.getRegion());
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				if(null != address.getTown() && !address.getTown().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null),address.getTown());
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				if(null != address.getPostcode() && !address.getPostcode().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null),address.getPostcode());
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				if(null != address.getCountry() && !address.getCountry().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null),address.getCountry());
					ac.add(addressDetail);
					
					haveAddedType = true;
				}
				
				if(ac.size()>0)
				{
					ac.setAttribute(DELETE_KEY, address);
					container.add(ac);
				}
			}
		}
		
		return container;
	}
	
	/**
	 * Helper method for updateProfileContentFromAddresses().
	 * Returns the string associated with a address type.
	 * 
	 * @param ad
	 * @return
	 */
	protected String getAdressTypeString(Address ad)
	{
		switch (ad.getType()) {
		case Address.TYPE_HOME:
			return Locale.get("nowplus.client.java.profilepage.subtype.home");
		case Address.TYPE_WORK:
			return Locale.get("nowplus.client.java.profilepage.subtype.work");
		case Address.TYPE_OTHER:
			return Locale.get("nowplus.client.java.profilepage.subtype.other");
		default:
			return " ";
		}
	}
	
	/**
	 * Adds detail entries to the profile form based on contact addresses.
	 * 
	 * @param label
	 * @param addresses
	 * @param container
	 * @return
	 */
	protected Container updateProfileContentFromGroups(final String label, final Group[] groups, Container container) 
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			container = UiFactory.createProfilePageDetailContainer("groups");
			container.setLabel(label);
			this.append(container);
		}
		else
		{
			container.clear();
			//make sure that container is appended
			this.remove(container);
			this.append(container);
		}
		
		if(null != groups && groups.length > 0)
		{
			Container ac = createNestedDetailsContainer();
			
			Item itm;
			for(int i=0; i<groups.length; i++)
			{
				itm = createNestedDetailEntry(null, groups[i].getName());
				ac.add(itm);
			}
			
			if(ac.size()>0)
				container.add(ac);
		}
		
		return container;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, final int event, Object data)
	{
		//#debug debug
		System.out.println(context+"/"+event+"/"+data);

		if( context == getContext() )
		{
			if(event == Event.Profile.DELETE_DETAIL)
			{
				//commit changes
				new Thread()
				{
					public void run()
					{
						NotificationForm nf=null;
						try
						{
							//invoke contact update notification
							
														
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

							try
							{
								profile.load(true);
								profile.lock();
								
								Item current = getCurrentItem();

								if(current == phonesContainer)
								{
									Identity iden = (Identity)phonesContainer.getFocusedItem().getAttribute(DELETE_KEY);
									profile.removeIdentity(iden);
								}
								else
								if(current == emailsContainer)
								{
									Identity iden = (Identity)emailsContainer.getFocusedItem().getAttribute(DELETE_KEY);
									profile.removeIdentity(iden);
								}
								else
								if(current == addressContainer)
								{
									Address adress = (Address)addressContainer.getFocusedItem().getAttribute(DELETE_KEY);
									profile.removeAddress(adress);
								}
								else
								if(current == groupsContainer)
								{
									if(profile instanceof ContactProfile)
									{
										((ContactProfile)profile).setGroups(null);
									}
								}
								else
								if(current == urlsContainer)
								{
									profile.setUrl(null);
								}
								else
								if(current == birthdayContainer)
								{
									profile.setDateOfBirth(null);
								}
								else
								if(current == notesContainer)
								{
									profile.setNote(null);
								}
								else
								if(current == workContainer)
								{
									profile.setWorkDetails(null);
								}

								profile.commit();
								profile.unload();

								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
							}
							catch (LockException le)
							{
								//#debug error
								System.out.println("Error while obtaining lock on profile object: "+le.getMessage());
								
								profile.unload();
								
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
								
								//invoke update failed notification
								//#style notification_form_base
								nf = new NotificationForm(
										getModel(), getController(),
										Locale.get("nowplus.client.java.edit.contact.delete.failed.notify.title"),
										Locale.get("nowplus.client.java.edit.contact.delete.failed.notify.text"),
										cmdOk,
										-1
								);
								
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
							}

						}
						catch (Throwable t)
						{
							//#debug error
							System.out.println("General error while saving profile changes:" + t);
							
							profile.unload();
							//remove update notification
							if(null != nf)
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
						}
					}
				}.start();
			}
		}
		else
		if( context == Event.Context.CONTACTS )
		{
			if(event == Event.Contacts.UPDATE)
			{
				if(null != profile && null != data && data instanceof Long && ((Long)data).longValue() == profile.getCabId())
				{
					//add existing contact data to form if any
					this.updateContact(this.profile);
					
					//update profile item
					super.handleEvent(context, event, data);
				}
			}
		}
		else
			super.handleEvent(context, event, data);
	}
	
	private String getCurrentDetailString()
	{
		Item itm = this.getCurrentItem();
		
		if(itm == phonesContainer)
		{
			StringItem si = (StringItem)phonesContainer.getFocusedItem();
			return si.getText();
		}
		else
		if(itm == emailsContainer)
		{
			StringItem si = (StringItem)emailsContainer.getFocusedItem();
			return si.getText();
		}
		else
		if(itm == addressContainer)
		{
			Container c = (Container)addressContainer.getFocusedItem();
			String s = "";
			for(int i = 0; i < c.size();++i)
			{
				s += ((StringItem)c.get(i)).getText()+", ";
			}
			if(c.size() > 0)
				s = s.substring(0, s.length()-2);
			return s;
		}
		else
		if(itm == groupsContainer)
		{
			Container c = (Container)groupsContainer.getFocusedItem();
			String s = "";
			for(int i = 0; i < c.size();++i)
			{
				s += ((StringItem)c.get(i)).getText()+", ";
			}
			if(c.size() > 0)
				s = s.substring(0, s.length()-2);
			return s;
		}
		else
		if(itm == urlsContainer)
		{
			StringItem si = (StringItem)urlsContainer.getFocusedItem();
			return si.getText();
		}
		else
		if(itm == birthdayContainer)
		{
			StringItem si = (StringItem)birthdayContainer.getFocusedItem();
			return si.getText();
		}
		else
		if(itm == notesContainer)
		{
			StringItem si = (StringItem)notesContainer.getFocusedItem();
			return si.getText();
		}	
		else
		if(itm == workContainer)
		{
			Container c = (Container)workContainer.getFocusedItem();
			String s = "";
			for(int i = 0; i < c.size();++i)
			{
				s += ((StringItem)c.get(i)).getText()+", ";
			}
			if(c.size() > 0)
				s = s.substring(0, s.length()-2);
			return s;
		}
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#focusByAction(int, de.enough.polish.ui.Container)
	 */
	public void focusByAction(int gameAction, Container container)
	{
		switch(gameAction)
		{
			case LEFT:
			case RIGHT:
			case DOWN :
				for (int i=0; i<container.size(); i++) {
					Item item = container.get(i);
					if (item.appearanceMode != Item.PLAIN) {
						container.focusChild(i);
						if(item instanceof Container) {
							focusByAction(gameAction, (Container)item);
						} 
						break;
					}
				}
				break;
			case UP : 
				for (int i=container.size(); --i >= 0; ) {
					Item item = container.get(i);
					if (item.appearanceMode != Item.PLAIN) {
						container.focusChild(i);
						if(item instanceof Container) {
							focusByAction(gameAction, (Container)item);
						} 
						break;
					}
				}
				break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		if( !isKeyPadKey(keyCode, gameAction) )
		{
			if( gameAction == Canvas.LEFT || gameAction == Canvas.RIGHT)
				return true;
			else
				//is keyCode keypad?
				return super.handleKeyPressed(keyCode, gameAction);
		}
		
		return false;
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
		
		return super.handleKeyRepeated(keyCode, gameAction);
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() {
		commandAction(cmdOptions, this);
		return true;
	}
	
	protected boolean handleKeyReleased(int keyCode, int gameAction){
		if(Keypad.key(keyCode) == Keypad.KEY_SEND){
			//check for profile number
			Profile profile = this.profile;
			profile.load(true);

			// Don't allow calling ourself.
			if (profile.getNowPlusMember() == Profile.NOWPLUS_ME) {
				return true;
			}

			Channel callChannel = profile.getPrimaryCallChannel();
        	profile.unload();
        	//Identity[] idents = profile.getIdentities(Identity.TYPE_PHONE);
        	//if(callChannel == null && idents.length > 0){
        	//	callChannel = idents[0].getChannel(Channel.TYPE_CALL);
        	//}
        	//#debug info
        	System.out.println("Got profile " + profile.getFullName() + " with primary call number " + callChannel);
        	if(callChannel != null){
        		getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, callChannel);
        		return true;
        	}
		}
		//FIXME remove for fixing bug 0011275: [Bold] Unable so send an SMS
		/*
		if(gameAction == Canvas.FIRE){
			Item item = getCurrentItem();
			if(item != null){
				while(item instanceof Container && item != null){
					item = ((Container) item).getFocusedItem();
				}
				if(item != null && item.getDefaultCommand() == null){
					return true;
				}
			}
		}
		*/
		return super.handleKeyReleased(keyCode, gameAction);
	}
	//#endif
	
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
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			this.getCommandListener().commandAction(cmdBack,this);
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
