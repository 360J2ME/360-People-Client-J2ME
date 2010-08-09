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

import javax.microedition.lcdui.Graphics;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.DateHelper;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * This class implements the layout of the 'edit contact' & 'create new contact'.
 * <p>
 * Used for editing of all profile types.
 * <p>
 * ManagedProfile methods that can be called WITOHOUT using load/unload paradigm:
 * getNowPlusMember()
 * getNowPlusPresence()
 * get*Name()
 * getGroups()
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class EditProfileForm extends BaseFramedForm
{
	/**
	 * Delete attribute key. Used to store object reference used when deleting contact details
	 * by press of c-key. 
	 */
	private static final String DELETE_KEY = "DK";
	
	/**
	 * Type attribute key. Used to store store object reference and distinguish between 'add' & 'edit' items.  
	 */
	private static final String TYPE_KEY = "TK";
	
	/**
	 * Commands
	 */
	private static final Command cmdEdit = new Command(Locale.get("nowplus.client.java.edit.contact.command.edit"),Command.ITEM,0);
	private static final Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"),Command.SCREEN,0);
	private static final Command cmdNew = new Command(Locale.get("nowplus.client.java.edit.contact.command.new"),Command.ITEM,0);

	//Descoped for now
	//private static final Command cmdTakePic = new Command(Locale.get("nowplus.client.java.edit.contact.command.takepicture"),Command.ITEM,0);

	/**
	 * Edit profile view details containers
	 */
	protected Container nameContainer;
	protected Container phonesContainer;
	protected Container emailsContainer;
	protected Container addressContainer;
	protected Container groupsContainer;
	protected Container urlsContainer;
	protected Container birthdayContainer;
	protected Container notesContainer;
	protected Container workContainer;

	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;

	/**
	 * Flag for defining if the form is 'new contact'. True means new contact, false means
	 * that a contact is being edited
	 */
	protected boolean isNew;

	/**
	 * Contact to edit
	 */
	protected ManagedProfile contact;
	
	protected ContactSummarizedItem csi;

	/**
	 * Cashed indexes, used to retain focus when going to and from edit detail screens
	 */
	private int lastIndex=0, lastNestedIndex=0;
	private boolean beenShown = true;

	/**
	 *
	 * @param model
	 * @param controller
	 * @param view
	 * @param contact
	 */
	public EditProfileForm(Model model, Controller controller, ManagedProfile contact, boolean isNew) {
		this(model, controller, contact, isNew, null);
	}

	/**
	 *
	 * @param model
	 * @param controller
	 * @param view
	 * @param contact
	 * @param style
	 */
	public EditProfileForm(Model model, Controller controller, ManagedProfile contact, boolean isNew, Style style) {
		super(model, controller, null, style);

		this.isNew = isNew;
		this.contact = contact;
		//#if polish.blackberry.isTouchBuild == true
		// do nothing.
		//#else
		//append profile item
//		this.csi = (ContactSummarizedItem) getCustomProfileItem(this.contact);
		this.csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_NONE, null);
		this.csi.setAppearanceMode(Item.PLAIN);
		this.append(this.csi);
		//#endif
		//append contact data to form if any
		this.updateContact(this.contact);

		//#if  not polish.blackberry
			addCommand(cmdOptions);
			addCommand(cmdBack);
		//#endif
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseProfileForm#getContext()
	 */
	public byte getContext()
	{
		return Event.Context.EDIT_PROFILE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#createDetailEntry(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected StringItem createDetailEntry(String label, String text)
	{
		//#style .editpage_content_item
		StringItem si = new StringItem(label, text);
		si.setAppearanceMode(Item.INTERACTIVE);
		
		//#if polish.blackberry
			si.setDefaultCommand(cmdEdit);
		//#endif
		
		si.addCommand(cmdEdit);
		
		si.setItemCommandListener(this);

		return si;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#createNestedDetailsContainer()
	 */
	protected Container createNestedDetailsContainer()
	{
		//#style .editpage_content_container_nested
		Container c = new Container(false);
		c.setAppearanceMode(Item.INTERACTIVE);
		
		//#if polish.blackberry
			c.setDefaultCommand(cmdEdit);
		//#endif
		
		c.addCommand(cmdEdit);
		
		c.setItemCommandListener(this);

		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#createNestedDetailEntry(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected StringItem createNestedDetailEntry(String label, String text)
	{
		//#style .editpage_content_item_nested
		StringItem si = new StringItem(label, text);
		si.setAppearanceMode(Item.PLAIN);
		return si;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#updateContact(com.zyb.nowplus.business.domain.ContactProfile)
	 */
	public void updateContact(final ManagedProfile c)
	{
		//#debug debug
		System.out.println("updateContact()");

		if(null != c)
		{
			this.contact = c;
			
			this.contact.load(true);

			//prevent drawing while updating form
			synchronized (getPaintLock())
			{
				//try to store focus settings
				if(beenShown && null != this.container)
				{
					synchronized (this.container)
					{
						try
						{
							//store indexes
							lastIndex = this.container.getFocusedIndex();
							if(this.container.getFocusedItem() != null && this.container.getFocusedItem() instanceof Container)
								lastNestedIndex = ((Container)this.container.getFocusedItem()).getFocusedIndex();
							beenShown = false;

							//#mdebug debug
							System.out.println("STORE");
							System.out.println("current item focus: "+this.getCurrentItem());
							System.out.println("lastIndex: "+lastIndex);
							System.out.println("lastNestedIndex: "+lastNestedIndex);
							//#enddebug
						}
						catch (Exception e)
						{
							//#debug error
							System.out.println("error storing edit screen focus:" + e);
						}
					}
				}

				this.container.clear();
				
				if(null !=  csi)
				{
					csi.setContact(this.contact);
				}
				
				this.nameContainer = updateProfileContentFromStringArray(Locale.get("nowplus.client.java.profilepage.content.name"),new String[]{this.contact.getFullName()},this.nameContainer, Event.EditProfile.EDIT_NAME);
				this.nameContainer.getStyle().border = null; /* reuse profile page style, but without border */

				//only display other data fields if name has been set
				if( null != this.nameContainer && this.nameContainer.size() > 0 )
				{
					this.phonesContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.callorsms"), phonesContainer, Identity.TYPE_PHONE, Event.EditProfile.EDIT_PHONE);
					this.phonesContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					this.emailsContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.email"), emailsContainer, Identity.TYPE_EMAIL, Event.EditProfile.EDIT_EMAIL);
					this.emailsContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					this.addressContainer = updateProfileContentFromAddresses(Locale.get("nowplus.client.java.profilepage.content.address"), this.contact.getAddresses(), addressContainer, Event.EditProfile.EDIT_ADRESS );
					this.addressContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					//Only add group editing option if not own profile
					if(this.contact.getNowPlusMember() != ContactProfile.NOWPLUS_ME && this.contact instanceof ContactProfile)
					{
						this.groupsContainer = updateProfileContentFromGroups(Locale.get("nowplus.client.java.profilepage.content.groupes"),  ((ContactProfile)this.contact).getGroups(), groupsContainer, Event.EditProfile.EDIT_GROUP);
						this.groupsContainer.getStyle().border = null; /* reuse profile page style, but without border */
					}
					
					this.urlsContainer = updateProfileContentFromIdentities(Locale.get("nowplus.client.java.profilepage.content.url"), urlsContainer, Identity.TYPE_URL, Event.EditProfile.EDIT_URL);
					this.urlsContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					this.birthdayContainer = updateProfileContentFromBirthday(Locale.get("nowplus.client.java.profilepage.content.birthday"), this.contact.getDateOfBirth(),  birthdayContainer, Event.EditProfile.EDIT_BIRTHDAY );
					this.birthdayContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					Note note = this.contact.getNote();
					
					this.notesContainer = updateProfileContentFromStringArray(Locale.get("nowplus.client.java.profilepage.content.notes"),new String[] {(note == null) ? "" : note.getContent()},notesContainer, Event.EditProfile.EDIT_NOTE);
					this.notesContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					this.workContainer = updateProfileContentFromWorkInfo(Locale.get("nowplus.client.java.profilepage.content.work"),this.contact.getWorkDetails(),workContainer, Event.EditProfile.EDIT_WORK);
					this.workContainer.getStyle().border = null; /* reuse profile page style, but without border */
					
					//add items for new entries
					addCreatorItems(false);
				}
				else
				{
					addCreatorItems(true);
				}

				//remove containers that do not contribute anything
				removeEmptyDetailContainers();

				//try to re-store focus settings
				if(!beenShown && null != this.container)
				{
					synchronized (this.container)
					{
						try
						{
							//#mdebug debug
							System.out.println("RE-STORE");
							System.out.println("current item focus: "+this.getCurrentItem());
							System.out.println("lastIndex: "+lastIndex);
							System.out.println("lastNestedIndex: "+lastNestedIndex);
							//#enddebug

							//restore indexes
							this.container.focusChild(lastIndex);
							if(this.container.getFocusedItem() != null && this.container.getFocusedItem() instanceof Container)
								((Container)this.container.getFocusedItem()).focusChild(lastNestedIndex);
							beenShown = true;
						}
						catch (Exception e)
						{
							//#debug error
							System.out.println("error re-storing edit screen focuse:" + e);
						}
					}
				}

				//call after remove
				requestInit();
			}
			
			this.contact.unload();
		}
	}

	public void paint(Graphics g)
	{
		try
		{
			super.paint(g);
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("Paint failed" + e);
		}
	}


	protected void calculateContentArea(int x, int y, int width, int height)
	{
		try
		{
			super.calculateContentArea(x, y, width, height);
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("Failed to calculate content area" + e);
		}
	}

	/**
	 * Creates and inserts interactive Items for adding new entries to a contact profile
	 */
	private void addCreatorItems(boolean isNameOnly)
	{
		StringItem si;

		//if in 'new contact' mode, add new name entry
		if( null != this.nameContainer && this.nameContainer.size() == 0 )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.name"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_NAME,null));
			this.nameContainer.add(si);
		}
		
		if(isNameOnly)
			return;

		si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.phone"));
		si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_PHONE,null));
		this.phonesContainer.add(si);

		si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.email"));
		si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_EMAIL,null));
		this.emailsContainer.add(si);

		/*
		Adding IM not supported according to UE
		si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.im"), new Event(getContext(),Event.EditContact.NEW_IM,null));
		this.imsContainer.add(si);
		*/

		si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.address"));
		si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_ADRESS,null));
		this.addressContainer.add(si);

		//only one birthday entry
		if(null != this.birthdayContainer && this.birthdayContainer.size() == 0 )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.birthday"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_BIRTHDAY,null));
			this.birthdayContainer.add(si);
		}
		
		//only one URL entry
		if(null != this.urlsContainer && this.urlsContainer.size() == 0 )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.url"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_URL,null));
			this.urlsContainer.add(si);
		}

		//only one note entry
		if(null != this.notesContainer && this.notesContainer.size() == 0 )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.note"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_NOTE,null));
			this.notesContainer.add(si);
		}

		//Only add group creation option if no groups are selected already and not own profile
		if( (null != this.groupsContainer && this.groupsContainer.size() == 0) &&
				(this.contact.getNowPlusMember() != ContactProfile.NOWPLUS_ME && this.contact instanceof ContactProfile) )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.group"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_GROUP,null));
			this.groupsContainer.add(si);
		}

		//only one work entry
		if(null != this.workContainer && this.workContainer.size() == 0 )
		{
			si = createCreatorEntry(null, Locale.get("nowplus.client.java.edit.contact.add") + " " + Locale.get("nowplus.client.java.edit.contact.work"));
			si.setAttribute(TYPE_KEY, new Event(getContext(),Event.EditProfile.NEW_WORK,null));
			this.workContainer.add(si);
		}
	}

	/**
	 * Helper method for addCreatorItems(). Created the common 'creator' item
	 * used to make new contact detail entries.
	 *
	 * @param label
	 * @param text
	 * @param reference
	 * @return
	 */
	private StringItem createCreatorEntry(String label, String text)
	{
		//#style .editpage_content_item
		StringItem si = new StringItem(label, text);
		si.setAppearanceMode(Item.INTERACTIVE);
		
		//#if polish.blackberry
			si.setDefaultCommand(cmdNew);
	    //#else
			si.addCommand(cmdNew);
		//#endif
		
		si.setItemCommandListener(this);

		return si;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#createItemFromIdentity(com.zyb.nowplus.business.domain.Identity, int)
	 */
	protected Item createItemFromIdentity(Identity identity, int type)
	{
		Item item = null;

		if(type == Identity.TYPE_PHONE)
			item = createDetailEntry(subTypeToString(identity.getSubtype()),identity.getName());
		else
		if(type == Identity.TYPE_EMAIL)
			item = createDetailEntry(subTypeToString(identity.getSubtype()),identity.getName());
		else
		if(type == Identity.TYPE_URL)
			item = createDetailEntry(null,identity.getUrl());
		/*
		else
		if(type == Identity.TYPE_SOCIAL_NETWORK)
			item = createDetailEntry(null,identity.getName(),new Event(getContext(),Event.EditContact.EDIT_NETWORK,identity));
		*/

		return item;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d)
	{
		if(c == cmdOptions)
		{
			//make sure that 'delete' option is only added if current item is NOT a creator item
			Event event = null;
			if( checkCurrentItemsDeletableState() )
				event = new Event(Event.Context.EDIT_PROFILE, Event.EditProfile.DELETE_DETAIL_CONTEXTUAL, null);
			else
				event = null;

			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, new Object[]{this.contact, event, null != event ? getCurrentDetailString() : null});
		}
		else
		if(c == cmdClearKey)
		{
			if( checkCurrentItemsDeletableState() )
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
						new Event(Event.Context.EDIT_PROFILE, Event.EditProfile.DELETE_DETAIL,null));
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
			}
		}
		else
		if(c == cmdBack)
		{
			//#debug debug
			System.out.println("Going back");
			
			if(null != this.contact && !this.contact.getFullName().equalsIgnoreCase(""))
			{
				//notify model that editing of this contact is complete
				getModel().finishedEditing(this.contact);
				
				//back to checkpoint
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
			}
			else
			{
				//envoke missing name confirmation screen 
				Command yes = new Command(Locale.get("nowplus.client.java.edit.contact.missing.name.confirm.yes"), Command.SCREEN, 0);
				Command no = new Command(Locale.get("nowplus.client.java.edit.contact.missing.name.confirm.no"), Command.BACK, 0);
				
				//#style notification_form_base
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(), 
						Locale.get("nowplus.client.java.edit.contact.invalid.name.title"),
						Locale.get("nowplus.client.java.edit.contact.invalid.name.text"),
						yes, no,
						new Event(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT, null)
						);
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
			}
		}
		else
			super.commandAction(c, d);
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.ProfilePageDetailsTab#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item)
	{
		//#debug debug
		System.out.println(c+"/"+item);

		/*
		 * Descoped for now
		if(c == cmdTakePic)
		{
			getController().notifyEvent(Event.Context.GETIMAGE, Event.GetImages.TAKE_IMAGE_WITH_CAMERA, this.contact);
		}
		else
		 */
		if( (c == cmdEdit || c == cmdNew) && null != item && (item instanceof StringItem || item instanceof Container))
		{
			Event ev = null;
			if(null != item.getAttribute(TYPE_KEY))
				ev = (Event)item.getAttribute(TYPE_KEY);

			if(null != ev)
			{
				switch( ev.getId() )
				{
					case Event.EditProfile.EDIT_NAME:
					case Event.EditProfile.NEW_NAME:

						//Update contact name data
						String[] name = new String[]
						                           {
								contact.getFirstName(),
								contact.getMiddleNames(),
								contact.getLastName()
						                           };

						ev.setData(name);

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_PHONE:
					case Event.EditProfile.NEW_PHONE:

						//do nothing, event already has identity reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_EMAIL:
					case Event.EditProfile.NEW_EMAIL:

						//do nothing, event already has identity reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_ADRESS:
					case Event.EditProfile.NEW_ADRESS:

						//do nothing, event already has identity reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_GROUP:
					case Event.EditProfile.NEW_GROUP:

						//do nothing, event already has String[] reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_URL:
					case Event.EditProfile.NEW_URL:

						//do nothing, event already has identity reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_BIRTHDAY:
					case Event.EditProfile.NEW_BIRTHDAY:

						//do nothing, event already has identity reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_NOTE:
					case Event.EditProfile.NEW_NOTE:

						//set note reference
						this.contact.load(true);
						Note note = this.contact.getNote();
						ev.setData((note == null) ? "" : note.getContent());
						this.contact.unload();
						
						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					case Event.EditProfile.EDIT_WORK:
					case Event.EditProfile.NEW_WORK:

						//do nothing, event already has work String[] reference

						//notify controller
						getController().notifyEvent(getContext(),ev.getId(), ev);
						break;

					default:
						break;
				}
			}
		}
		else
		if(c == cmdBack)
		{
			//#debug debug
			System.out.println("Going back");

			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);
		}
		else
			super.commandAction(c, item);
	}
	
	private String getCurrentDetailString()
	{
		Item itm = this.getCurrentItem();
		
		if(itm == nameContainer)
		{
			StringItem si = (StringItem)nameContainer.getFocusedItem();
			return si.getText();
		}
		else
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
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(final byte context, final int event, final Object data)
	{

		//#debug debug
		System.out.println(context+"/"+event+"/"+data);

		if(context == getContext())
		{
			if(event == Event.EditProfile.SAVE || event == Event.EditProfile.DELETE_DETAIL || event == Event.EditProfile.DELETE_DETAIL_CONTEXTUAL)
			{
				if(event == Event.EditProfile.SAVE)
				{
					if(null != data && data instanceof Event)
					{
						//set update event handle for update thread
						final Event updateEvent = (Event) data;

						if( updateEvent.getId() == Event.EditProfile.EDIT_BIRTHDAY || updateEvent.getId() == Event.EditProfile.NEW_BIRTHDAY)
						{
							if(updateEvent.getData() instanceof Date)
							{
								//verify new date
								Date newBday = (Date)updateEvent.getData();
								Calendar now = DateHelper.getCalendar(System.currentTimeMillis());
								Calendar dateOfBirth = Calendar.getInstance();
								dateOfBirth.setTime(newBday);

								//bday must not be later than today
								if (DateHelper.isFuture(now, dateOfBirth)) {
									Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);

									//envoke invalid bday notification
									//#style notification_form_base
									NotificationForm nf = new NotificationForm(
											getModel(), getController(),
											Locale.get("nowplus.client.java.signup.error.headline"),
											Locale.get("nowplus.client.java.edit.contact.bdinvalid.text"),
											cmdOk,
											-1
									);

									//set next
									getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
									return;
								}
							}
						}
					}
				}

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
							
							//set next
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
							//#if false
							long startTime = System.currentTimeMillis();
							//#endif
							
							try
							{
								//load contact
								contact.load(true);
								
								//try to lock
								contact.lock();
								
								if(event == Event.EditProfile.SAVE)
								{
									if(null != data && data instanceof Event)
									{
										//set update event handle for update thread
										final Event updateEvent = (Event) data;

										switch( updateEvent.getId() )
										{
										case Event.EditProfile.EDIT_NAME:
										case Event.EditProfile.NEW_NAME:
											if(updateEvent.getData() instanceof String[])
											{
												//Update contact name data
												String[] name = (String[])updateEvent.getData();
												EditProfileForm.this.contact.setName(name[0], name[1], name[2]);
											}
											break;

										case Event.EditProfile.EDIT_PHONE:
										case Event.EditProfile.NEW_PHONE:
											if(updateEvent.getData() instanceof Identity[])
											{
												//get reference to new and old identity
												Identity[] phones = (Identity[]) updateEvent.getData();

												if (phones[0] == null)
												{
													contact.addIdentity(phones[1]);
												}
												else
												{
													contact.updateIdentity(phones[0], phones[1]);
												}
											}
											break;

										case Event.EditProfile.EDIT_EMAIL:
										case Event.EditProfile.NEW_EMAIL:
											if(updateEvent.getData() instanceof Identity[])
											{
												//get reference to new and old identity
												Identity[] emails = (Identity[]) updateEvent.getData();

												if (emails[0] == null)
												{
													contact.addIdentity(emails[1]);
												}
												else
												{
													contact.updateIdentity(emails[0], emails[1]);
												}
											}
											break;

										case Event.EditProfile.EDIT_ADRESS:
										case Event.EditProfile.NEW_ADRESS:
											if(updateEvent.getData() instanceof Address[])
											{
												//get reference to new and old Address
												Address[] addresses = (Address[]) updateEvent.getData();

												if (addresses[0] == null)
												{
													contact.addAddress(addresses[1]);
												}
												else
												{
													contact.updateAddress(addresses[0], addresses[1]);
												}
											}
											break;
										case Event.EditProfile.EDIT_GROUP:
										case Event.EditProfile.NEW_GROUP:
											if(updateEvent.getData() instanceof Group[] && contact instanceof ContactProfile)
											{
												//get reference to new and old Address
												Group[] groups = (Group[])updateEvent.getData();

												((ContactProfile)contact).setGroups(groups);
											}
											break;

										case Event.EditProfile.EDIT_URL:
										case Event.EditProfile.NEW_URL:
											if(updateEvent.getData() instanceof Identity[])
											{
												//get reference to new and old identity
												Identity[] urls = (Identity[]) updateEvent.getData();

												//set new
												contact.setUrl(urls[1]);
											}
											break;

										case Event.EditProfile.EDIT_BIRTHDAY:
										case Event.EditProfile.NEW_BIRTHDAY:
											if(updateEvent.getData() instanceof Date)
											{
												//set new date
												Date newBday = (Date)updateEvent.getData();

												//replace old identity
												contact.setDateOfBirth(newBday);
											}
											break;

										case Event.EditProfile.EDIT_NOTE:
										case Event.EditProfile.NEW_NOTE:
											if(updateEvent.getData() instanceof String[])
											{
												String[] note = (String[])updateEvent.getData();
												
												//set new note
												contact.setNote( note[0] );
											}
											break;

										case Event.EditProfile.EDIT_WORK:
										case Event.EditProfile.NEW_WORK:
											if(updateEvent.getData() instanceof String[])
											{
												//Update contact name data
												String[] work = (String[])updateEvent.getData();
												contact.setWorkDetails(work);
											}
											break;
										default:
											break;
										}
										
										//commit changes
										contact.commit();

										//unload
										contact.unload();

										//remove update notification
										getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
										
										//go back from edit detail page
										getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT, nf);
									}
								}
								else
								if(event == Event.EditProfile.DELETE_DETAIL || event == Event.EditProfile.DELETE_DETAIL_CONTEXTUAL)
								{
									Item current = getCurrentItem();

									if(current == nameContainer)
									{
										EditProfileForm.this.contact.setName(null, null, null);
									}
									else
									if(current == phonesContainer)
									{
										Identity iden = (Identity)phonesContainer.getFocusedItem().getAttribute(DELETE_KEY);
										contact.removeIdentity(iden);
									}
									else
									if(current == emailsContainer)
									{
										Identity iden = (Identity)emailsContainer.getFocusedItem().getAttribute(DELETE_KEY);
										contact.removeIdentity(iden);
									}
									else
									if(current == addressContainer)
									{
										Address adress = (Address)addressContainer.getFocusedItem().getAttribute(DELETE_KEY);
										contact.removeAddress(adress);
									}
									else
									if(current == urlsContainer)
									{
										contact.setUrl(null);
									}
									else
									if(current == birthdayContainer)
									{
										contact.setDateOfBirth(null);
									}
									else
									if(current == notesContainer)
									{
										contact.setNote(null);
									}
									else
									if(current == workContainer)
									{
										contact.setWorkDetails(null);
									}
									else
									if(current == groupsContainer)
									{
										((ContactProfile)contact).setGroups(new Group[0]);
									}
									
									//commit changes
									contact.commit();

									//unload
									contact.unload();

									//remove update notification
									getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
								}
								//#if false
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
							}
							catch (LockException le)
							{
								//#debug error
								System.out.println("Error while obtaining lock on profile object: "+le.getMessage());

								//ensure contact is unloaded
								contact.unload();
								
								//remove update notification
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
								
								Command cmdOk = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
								
								//envoke update failed notification
								//#style notification_form_base
								nf = new NotificationForm(
										getModel(), getController(),
										Locale.get("nowplus.client.java.edit.contact.update.failed.notify.title"),
										Locale.get("nowplus.client.java.edit.contact.update.failed.notify.text"),
										cmdOk,
										-1
								);
								
								//set next
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
							}
						}
						catch (Throwable t)
						{
							//#debug error
							System.out.println("General error while saving changes to 'Me View': " + t);
							
							//ensure contact is unloaded
							contact.unload();
							
							//remove update notification
							if(null != nf)
								getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
							
						}
					}
				}.start();
			}
		}
		else
		if( context == Event.Context.CONTACTS)
		{
			if(event == Event.Contacts.UPDATE ||
					event == Event.Contacts.ADD
					)
			{
				//is contact id same?
				if(null != contact && null != data && data instanceof Long && ((Long)data).longValue() == contact.getCabId())
				{
					//add existing contact data to form if any
					this.updateContact(this.contact);
				}
			}
		}
        else
            super.handleEvent(context, event, data);

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
	protected Container updateProfileContentFromIdentities(String label, Container container, final int type, int event)
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
			if (this.contact.getUrl() == null)
			{
				identities = new Identity[0];
			}
			else
			{
				identities = new Identity[]{ this.contact.getUrl() };
			}
		}
		else
		{
			identities = this.contact.getIdentities(type);
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
					itm.setAttribute(TYPE_KEY,  new Event(getContext(), event, iden) );
					container.add(itm);
				}
			}
		}

		return container;
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
		return "";
	}

	/**
	 * Adds detail entries to the profile form based on arrays of strings.
	 *
	 * @param label is label of the container
	 * @param texts are each text string we want to display
	 * @param container is the container each string will belong to
	 * @return same as argument container or a newly created
	 */
	protected Container updateProfileContentFromStringArray(final String label, final String[] texts, Container container, int event)
	{
		//Create container if it doesn't exist
		if(container == null)
		{
			String eventString = null;
			if(Event.EditProfile.EDIT_NOTE == event)
			{
				eventString = "note";
			}
			else
			if(Event.EditProfile.EDIT_NAME == event)
			{
				eventString = "name";
			}
			container = UiFactory.createProfilePageDetailContainer(eventString);
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
			for(int i=0; i<texts.length; i++)
			{
				if(null != texts[i] && !texts[i].toString().trim().equalsIgnoreCase(""))
				{
					Item tmp = createDetailEntry(null,texts[i]);
					tmp.setAttribute(TYPE_KEY,  new Event(getContext(), event, texts[i]));
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
	protected Container updateProfileContentFromBirthday(final String label, final Date bday, Container container, int event)
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
			si.setAttribute(TYPE_KEY,  new Event(getContext(), event, bday));
			
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
	protected Container updateProfileContentFromWorkInfo(final String label, final String[] work, Container container, int event)
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
			{
				ac.setAttribute(TYPE_KEY, new Event(getContext(), event, work) );				
				container.add(ac);
			}
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
	protected Container updateProfileContentFromAddresses(final String label, final Address[] addresses, Container container, int event)
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
				if(null != address.getStreet() && !address.getStreet().toString().trim().equalsIgnoreCase(""))
				{
					addressDetail = createNestedDetailEntry((!haveAddedType?type:null), address.getStreet());
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
					ac.setAttribute(TYPE_KEY,  new Event(getContext(), event, address));
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
	protected Container updateProfileContentFromGroups(final String label, final Group[] groups, Container container, int event)
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
			{
				ac.setAttribute(TYPE_KEY,  new Event(getContext(), event, groups));
				container.add(ac);
			}
		}

		return container;
	}

	protected Item createBottomItem()
	{
		return null;
	}

	protected String createCssSelector()
	{
		return null;
	}

	protected Item[] createStaticItems()
	{
		if(null == this.titleitem)
		{
			if(!this.isNew)
				this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.edit.contact.title"),getModel() );
			else
				this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.new.contact.title"),getModel() );
		}
		
		return new Item[]{this.titleitem};
	}

	protected Item createTopItem()
	{
		Item result;
	
		//#if polish.blackberry.isTouchBuild == true
			this.csi = UiFactory.createUserProfileItem(this.contact, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_NONE, null);
			this.csi.setAppearanceMode(Item.PLAIN);
			result = this.csi;
		//#else	
			result = null;
		//#endif
		
		return result;
	}
	
	/**
	 * Checks if the currently selected item is in fact deletable
	 * 
	 * @return
	 */
	private boolean checkCurrentItemsDeletableState()
	{
		Container c = (Container)this.getCurrentItem();
		Item nestItm = null;
		
		//is current null aka is contact details empty?
		if(null == c)
			return false;
		
		//Special case, name is not deletable if own profile
		if(c == nameContainer && this.contact.getNowPlusMember() == ContactProfile.NOWPLUS_ME)
			return false;
		
		if(c == phonesContainer ||
				c == emailsContainer ||
				c == addressContainer ||
				c == groupsContainer || 
				c == birthdayContainer||
				c == notesContainer||
				c == urlsContainer ||
				c == workContainer)
			nestItm = c.getFocusedItem();
		
		if(null != nestItm)
		{
			if(nestItm.containsCommand(cmdEdit))
				return true;
			else
			if(nestItm.containsCommand(cmdNew))
				return false;
			else 
				return false;
		}
		else
			return false;
	}
	
	public void itemStateChanged(Item item) {
		if(item instanceof TextField){
			this.container.scroll(0, item, true);
		}
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
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			getCommandListener().commandAction(cmdBack, this);
		else if (Keypad.key(keyCode) == Keypad.KEY_MENU)
			getCommandListener().commandAction(cmdOptions, this);
		else if (Keypad.key(keyCode) == Keypad.KEY_BACKSPACE) 
			commandAction(cmdClearKey, this);
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
