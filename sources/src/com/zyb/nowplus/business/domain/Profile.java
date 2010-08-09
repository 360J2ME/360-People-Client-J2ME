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
package com.zyb.nowplus.business.domain;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Image;

import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.util.ArrayUtils;
import com.zyb.util.Collator;
import com.zyb.util.HashUtil;

import de.enough.polish.ui.StyleSheet;

/**
 * The details of a Now+ user or contact of a Now+ user.
 */
public abstract class Profile implements ProfileSummary
{
	public static ProfileManager manager;
	
	// NOTE: the methods with a name ending on 0 are the unsafe,
	// internal versions of the methods with the same name minus the 0.

	/**
	 * This is the profile of the user of the application.
	 */
	public static final int NOWPLUS_ME = 1;
	
	/**
	 * This is the profile of a Now+ user who is connected to the user of the application.
	 */
	public static final int NOWPLUS_CONNECTED_MEMBER = 2;
	
	/**
	 * This is the profile of a Now+ user who is not connected to the user of the application.
	 */
	public static final int NOWPLUS_MEMBER = 3;
	
	/**
	 * This is the profile of a contact of the user of the application who is not a Now+ user.
	 */
	public static final int NOWPLUS_CONTACT = 4;
	
	public static Image DEFAULT_PROFILE_IMAGE;
	
	static 
	{
		try {
			DEFAULT_PROFILE_IMAGE =
				//#if polish.classes.ImageLoader:defined
				StyleSheet.getImage("/contact_avatar_default_40x40.png", null, false);
				//#else
				Image.createImage("/contact_avatar_default_40x40.png");
				//#endif
		} catch (IOException e) {
			// TODO
		}
	}
	
	// The start of session (SOS) is the time in ms when the application 
	// is started.
	// The Change Request Id (CRI) contains the id of the add/edit request
	// that is send to the server to change an attribute. 
	// * When an attribute is changed, the CRI is set to -1. At commit the
	// profile is added to the change log.
	// * When an add/edit request is send to the server for the profiles in
	// the change log, the atttributes with CRI == 0 (not changed) 
	// or CRI > SOS (already send) are skipped while filling ContactChanges.
	// The CRI of the other attributes, which was CRI == -1 (changed) or 
	// 0 < CRI < SOS (send in a previous session, never confirmed) is set
	// to SOS + the id of the server request. 
	// * When a successfull response to an add/edit request is received, 
	// CRI is reset to 0 and the profile is removed from the changelog.
	
	protected long userId;
	protected int nowPlusMember;
	protected int nowPlusPresence;
	protected ImageRef profileImage;
	protected String nickname;
	protected long nicknameCri;
	protected String firstName;
	protected String middleNames;
	protected String lastName;
	protected long nameCri;
	protected int yearOfBirth;
	protected int monthOfBirth;
	protected int dayOfBirth;
	protected long dateOfBirthCri;
	protected Identity[] identities;
	protected Identity url;
	protected Address[] addresses;
	protected String title;
	protected long titleCri;
	protected String department;
	protected String organisation;
	protected long organisationCri;
	protected Note[] notes;
	protected String status;
	protected long statusCri;
	protected ExternalNetwork statusSource;

	protected int[] sortName;
	
	public Profile()
	{
		profileImage = new ImageRef(DEFAULT_PROFILE_IMAGE);		
		
		identities = new Identity[8];
		addresses = new Address[3];
		notes = new Note[3];
	}

	public long getUserId() 
	{
		return userId;
	}

	public int getNowPlusMember() 
	{
		return nowPlusMember;
	}

	public int getNowPlusPresence() 
	{
		return nowPlusPresence;
	}

	protected void setProfileImageUrl0(String url) 
	{
		profileImage.setUrl(url);
	}
	
	public ImageRef getProfileImage() 
	{
		return profileImage;
	}	
	
	public String getNickname() 
	{
		return checkForNull(nickname, "");
	}
	
	public String getFirstName() 
	{
		return checkForNull(firstName, "");
	}

	public String getMiddleNames() 
	{
		return checkForNull(middleNames, "");
	}
	
	public String getLastName()
	{
		return checkForNull(lastName, "");
	}
	
	public String getFullName()
	{
		return ContactList.currentOrder.compileFullName(firstName, middleNames, lastName);
	}

	public String getUserVisibleName()
	{
		String name = getFullName();

		if (name == null || name.length() == 0) {
			Channel channel = getPrimaryEmailChannel();

			if (channel != null && !(this instanceof MyProfile)) {
				name = channel.getName();
			}
		}

		return name;
	}
	
	protected void resetSortName()
	{
		sortName = null;
	}
	
	public int[] getSortName()
	{
		if (sortName == null)
		{
			sortName = Collator.getInstance().compileSortName(getFullName());
		}
		return sortName;
	}
	
	public String getInformalName()
	{
		if (firstName != null)
		{
			return firstName;
		}
		if (middleNames != null)
		{
			return middleNames;
		}
		return lastName;
	}
	
	public Date getDateOfBirth() 
	{
		if ((yearOfBirth == 0) && (monthOfBirth == 0) && (dayOfBirth == 0))
		{
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, yearOfBirth);
		calendar.set(Calendar.MONTH, monthOfBirth - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfBirth);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	protected void addIdentity0(Identity identity)
	{
		if (identity.getType() == Identity.TYPE_URL)
		{
			throw new RuntimeException("Can't add " + identity + " to identities.");
		}
		// For chat identities, we need to make sure they're not
		// already in the identities array. This is done in order to fix
		// duplicate identities.
		else if (identity.getType() == Identity.TYPE_IM_ACCOUNT) {
			Identity temp = null;

			for (int j = 0; j < identities.length; j++) {
				temp = identities[j];
				
				if (temp != null) {
					if (temp.getType() == identity.getType()
						&& temp.getNetworkId().equals(identity.getNetworkId())
						&& temp.getName().equals(identity.getName())) {
						// We have added this identity already, we should not add it again
						return;
					}
				}
			}
		}
		
		identity.setProfile(this);
		
		boolean added = false;
		
		int i = 0;
		for (; i < identities.length; i++)
		{
			if (identities[i] == null)
			{
				if (!added)
				{
					identities[i] = identity;
					added = true;
				}
			}
			else
			if ((identities[i].getType() == identity.getType()) && identities[i].isPreferred() && identity.isPreferred())
			{
				identities[i].setPreferred(false);
				identities[i].setCri(-1);
			}
		}
		if (!added)
		{
			identities = Identity.extendArray(identities);
			identities[i] = identity;
		}
	}	

	protected void updateIdentity0(Identity identity)
	{
		if (identity.getType() == Identity.TYPE_URL)
		{
			throw new RuntimeException("Can't update " + identity + " in identities.");
		}
		identity.setProfile(this);
		
		for (int i = 0; i < identities.length; i++)
		{
			if (identities[i] != null) 
			{
				if (identities[i].getSabDetailId() == identity.getSabDetailId())
				{
					identities[i] = identity;
				}
				else
				if ((identities[i].getType() == identity.getType()) && identities[i].isPreferred() && identity.isPreferred())
				{
					identities[i].setPreferred(false);
					identities[i].setCri(-1);
				}
			}
		}
	}
	
	protected void updateIdentity0(Identity oldIdentity, Identity identity)
	{
		if (identity.getType() == Identity.TYPE_URL)
		{
			throw new RuntimeException("Can't update " + identity + " in identities.");
		}
		identity.setProfile(this);
		
		for (int i = 0; i < identities.length; i++)
		{
			if (identities[i] != null) 
			{
				if (identities[i].equals(oldIdentity))
				{
					identities[i] = identity;
				}
				else
				if ((identities[i].getType() == identity.getType()) && identities[i].isPreferred() && identity.isPreferred())
				{
					identities[i].setPreferred(false);
					identities[i].setCri(-1);
				}
			}
		}
	}	
		
	protected void removeIdentity0(Identity identity)
	{
		// For IM identities, we need to make sure that we
		// remove the identities by using the values of their fields,
		// not the references to their corresponding objects.
		if ( identity.getType() == Identity.TYPE_IM_ACCOUNT ) {
			Identity temp = null;

			for (int j = 0; j <identities.length; j++) {
				temp = identities[j];
				
				if (temp != null) {
					if (temp.getType() == identity.getType()
						&& temp.getNetworkId().equals(identity.getNetworkId())
						&& temp.getName().equals(identity.getName())) {		
							identities[j] = null;
						}
				}
			}
		}
		else
		{
			for (int i = 0; i < identities.length; i++) {
				if ((identities[i] != null) && (identities[i].equals(identity))) {
					identities[i] = null;
				}
			}		
		}
	}
	
	/**
	 * Gets all identities of the contact of a given type.
	 */
	public Identity[] getIdentities(int type) 
	{
		Identity[] selection = new Identity[identities.length];
		int len = 0;
		for (int i = 0; i < identities.length; i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && (candidate.getType() == type) && !candidate.isEmpty())
			{
				selection[len++] = candidate;
			}
		}
		return Identity.trimArray(selection, len);
	}
	
	public Identity[] getLoggedInWebaccountsWithPostCap()
	{
		Identity[] selection = new Identity[identities.length];
		int len = 0;
		for (int i = 0; i < identities.length; i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && !candidate.isEmpty() && candidate.isLoggedIn() && candidate.hasCap("post_own_status"))
			{
				selection[len++] = candidate;
			}
		}
		return Identity.trimArray(selection, len);		
	}
	
	protected Identity[] getIdentities(boolean includeEmpty)
	{
		Identity[] selection = new Identity[identities.length];
		int len = 0;
		for (int i = 0; i < identities.length; i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && (includeEmpty || !candidate.isEmpty()))
			{
				selection[len++] = candidate;
			}
		}
		return Identity.trimArray(selection, len);
	}
	
	/**
	 * Gets the identity of a contact of the given type and 
	 * native phonebook subtypes, as a bitmask.
	 */
	public Identity getIdentity(int type, int nabSubtypes)
	{
		Identity selection = null;
		for (int i = 0; (i < identities.length) && (selection == null); i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) 
					&& (candidate.getType() == type) && (candidate.getNabSubtypes() == nabSubtypes)
					&& !candidate.isEmpty())
			{
				selection = candidate;
			}
		}
		return selection;
	}
	
	private Identity getIdentity(long sabDetailId)
	{
		Identity selection = null;
		for (int i = 0; (i < identities.length) && (selection == null); i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && (candidate.getSabDetailId() == sabDetailId))
			{
				selection = candidate;
			}
		}
		return selection;
	}
	
	public Channel getPrimaryCallChannel()
	{
		return getPrimaryChannel(Identity.TYPE_PHONE, Channel.TYPE_CALL);
	}
	
	public Channel getPrimarySMSChannel()
	{
		return getPrimaryChannel(Identity.TYPE_PHONE, Channel.TYPE_SMS);
	}
	
	public Channel getPrimaryMMSChannel()
	{
		return getPrimaryChannel(Identity.TYPE_PHONE, Channel.TYPE_MMS);
	}
	
	public abstract Channel getPrimaryChatChannel();
	
	public Channel getPrimaryEmailChannel()
	{
		return getPrimaryChannel(Identity.TYPE_EMAIL, Channel.TYPE_EMAIL);
	}
	
	protected Channel getPrimaryChannel(int identityType, int channelType)
	{
		Channel selection = null;
		for (int i = 0; i < identities.length; i++)
		{
			Identity identity = identities[i];
			if ((identity != null) && (identity.getType() == identityType) && !identity.isEmpty() 
					&& ((selection == null) || identity.isPreferred()))
			{
				selection = identity.getChannel(channelType);
			}
		}
		return selection;
	}
	
	/**
	 * Sets the presences on my profile from the client.
	 */
	public void setMyPresences(String[] networkIds, int[] presences)
	{
		String networkId = null;
		for (int i = 0; i < networkIds.length; i++)
		{
			if (networkIds[i] == null)
			{
				networkId = ExternalNetwork.VODAFONE_360;
			}
			else
			{
				networkId = networkIds[i];
			}
			
			//#debug info
			System.out.println("Set presence " + getFullName() + " " + networkId + " " + presences[i]);
			
			for (int j = 0; j < identities.length; j++) 
			{
				Identity candidate = identities[j];
				if ((candidate != null) && HashUtil.equals(candidate.getNetworkId(), networkId))
				{
					candidate.setPresence(presences[i]);
					break;
				}
			}
		}
	}
	
	/**
	 * Sets the presences on my profile or a contacts profile.
	 */
	public void setPresences(String[] networkIds, int[] presences)
	{
		String networkId = null;
		
		for (int i = 0; i < networkIds.length; i++)
		{
			if ("mobile".equals(networkIds[i]) || "pc".equals(networkIds[i]))
			{
				networkId = ExternalNetwork.VODAFONE_360;

				this.nowPlusPresence = presences[i];
			}
			else
			{
				networkId = networkIds[i];
			}
			
			//#debug info
			System.out.println("Set (im) presence " + getFullName() + " " + networkId + " " + presences[i]);
			
			for (int j = 0; j < identities.length; j++) 
			{
				Identity candidate = identities[j];
				
				if ((candidate != null) && HashUtil.equals(candidate.getNetworkId(), networkId)
						&& (candidate.getType() == Identity.TYPE_IM_ACCOUNT))
				{
					candidate.setPresence(presences[i]);
					
					//#debug debug
					System.out.println(getFullName() + " : set presence for " + candidate.getNetworkId() + ":" + presences[i]);
				}
			}
		}
		
		if (nowPlusMember != NOWPLUS_ME)
		{
			int presence = Channel.PRESENCE_OFFLINE;
			for (int i = 0; (i < identities.length); i++)
			{
				Identity candidate = identities[i];
				if ((candidate != null) && (candidate.getType() == Identity.TYPE_IM_ACCOUNT)
						&& (candidate.getPresence() > presence ))
				{
					presence = candidate.getPresence();
				}
			}
			
			//#debug debug
			System.out.println(getFullName() + " : got nowplus presence : " + presence);

			this.nowPlusPresence = presence;
		} 
	}
	
	public Channel setConversationId(String networkId, String name, String oldConversationId, String newConversationId)
	{
		Channel channel = getChatChannel(networkId, name, oldConversationId);		
		if (channel != null)
		{
			channel.setConversationId(newConversationId);
		}
		return channel;
	}
	
	public Channel addMessage(String networkId, String name, String conversationId, Message message)
	{
		Channel channel = getChatChannel(networkId, name, conversationId);		
		if (channel != null)
		{
			channel.addMessage(message);	
		}
		return channel;
	}
	
	private Channel getChatChannel(String networkId, String name, String conversationId)
	{
		Channel channel = null;
		// try the conversation id
		if (conversationId != null)
		{
			channel = getOngoingChat(conversationId);
		}
		// if not found, try by network/name
		if (channel == null)
		{
			if (networkId == null) 
			{
				channel = getChatChannel(ExternalNetwork.VODAFONE_360, Identity.NOWPLUS_ACCOUNT_LABEL);
			}
			else
			{
				channel = getChatChannel(networkId, name);
			}
		}
		return channel;
	}
	
	public Channel getChatChannel(String networkId, String name)
	{
		Channel selection = null;
		
		for (int i = 0; (i < identities.length) && (selection == null); i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && (candidate.getType() == Identity.TYPE_IM_ACCOUNT)
					&& HashUtil.equals(candidate.getNetworkId(), networkId)
					&& HashUtil.equals(candidate.getName(), name))
			{
				selection = candidate.getChannel(Channel.TYPE_CHAT);
			}
		}
		return selection;		
	}
	
	public Channel[] getOngoingChats()
	{
		Channel[] selection = new Channel[identities.length];
		int len = 0;
		for (int i = 0; i < identities.length; i++) 
		{
			Identity identity = identities[i];
			if ((identity != null) && (identity.getType() == Identity.TYPE_IM_ACCOUNT))
			{
				Channel candidate = identity.getChannel(Channel.TYPE_CHAT);
				if ((candidate != null) && candidate.isOpen())
				{
					selection[len++] = candidate;
				}
			}
		}
		return Channel.trimArray(selection, len);
	}

	private Channel getOngoingChat(String conversationId)
	{
		Channel selection = null;
		for (int i = 0; (i < identities.length) && (selection == null); i++) 
		{
			Identity identity = identities[i];
			if ((identity != null) && (identity.getType() == Identity.TYPE_IM_ACCOUNT))
			{
				Channel candidate = identity.getChannel(Channel.TYPE_CHAT);
				if ((candidate != null) && conversationId.equals(candidate.getConversationId()))
				{
					selection = candidate;
				}
			}
		}
		return selection;
	}

	public String getEmailOrMSISDN()
	{
		Channel channel = getPrimaryEmailChannel();
		if (channel == null)
		{
			channel = getPrimaryCallChannel();
		}
		return (channel == null) ? null : channel.getName();
	}
	
	public Identity getUrl()
	{
		return ((url == null) || url.isEmpty()) ? null : url;
	}
	
	protected void addAddress0(Address address)
	{
		int i = 0;
		for (;i < addresses.length; i++)
		{
			if (addresses[i] == null)
			{
				addresses[i] = address;
				return;
			}
		}
		addresses = Address.extendArray(addresses);
		addresses[i] = address;
	}
	
	protected void updateAddress0(Address oldAddress, Address address)
	{
		for (int i = 0; i < addresses.length; i++)
		{
			if ((addresses[i] != null) && (addresses[i].equals(oldAddress)))
			{
				addresses[i] = address;
			}
		}		
	}
	
	protected void removeAddress0(Address address)
	{
		for (int i = 0; i < addresses.length; i++)
		{
			if ((addresses[i] != null) && (addresses[i].equals(address)))
			{
				addresses[i] = null;
			}
		}
	}
	
	/**
	 * Gets all addresses of the contact.
	 */
	public Address[] getAddresses() 
	{
		return getAddresses(false);
	}
	
	protected Address[] getAddresses(boolean includeEmpty)
	{
		Address[] selection = new Address[addresses.length];
		int len = 0;
		for (int i = 0; i < addresses.length; i++)
		{
			Address candidate = addresses[i];
			if ((candidate != null) && (includeEmpty || !candidate.isEmpty()))
			{
				selection[len++] = candidate;
			}
		}
		return Address.trimArray(selection, len);
	}	
	
	/**
	 * Gets the address of a contact with the given 
	 * native phonebook subtypes, as a bitmask.
	 */
	public Address getAddress(int nabSubtypes)
	{
		Address selection = null;
		for (int i = 0; (i < addresses.length) && (selection == null); i++)
		{
			Address candidate = addresses[i];
			if ((candidate != null) && (candidate.getNabSubtypes() == nabSubtypes))
			{
				selection = candidate;
			}
		}
		return selection;
	}
	
	protected Address getAddress(long sabDetailId)
	{
		Address selection = null;
		for (int i = 0; (i < addresses.length) && (selection == null); i++)
		{
			Address candidate = addresses[i];
			if ((candidate != null) && (candidate.getSabDetailId() == sabDetailId))
			{
				selection = candidate;
			}
		}
		return selection;
	}

	public String[] getWorkDetails() 
	{
		if ((title == null) && (department == null) && (organisation == null))
		{
			return new String[0];
		}
		return new String[] {(organisation == null) ? "" : organisation, 
				(department == null) ? "" : department,
				(title == null) ? "" : title};
	}

	protected void addNote0(Note note)
	{
		int i = 0;
		for (;i < notes.length; i++)
		{
			if (notes[i] == null)
			{
				notes[i] = note;
				return;
			}
		}
		notes = Note.extendArray(notes);
		notes[i] = note;
	}
	
	protected Note[] getNotes(boolean includeEmpty)
	{
		Note[] selection = new Note[notes.length];
		int len = 0;
		for (int i = 0; i < notes.length; i++)
		{
			Note candidate = notes[i];
			if ((candidate != null) && (includeEmpty || !candidate.isEmpty()))
			{
				selection[len++] = candidate;
			}
		}
		return Note.trimArray(selection, len);		
	}
	
	protected Note getNote(long sabDetailId)
	{
		Note selection = null;
		for (int i = 0; (i < notes.length) && (selection == null); i++)
		{
			Note candidate = notes[i];
			if ((candidate != null) && (candidate.getSabDetailId() == sabDetailId))
			{
				selection = candidate;
			}
		}
		return selection;
	}
	
	public Note getNote() 
	{
		// The server has more than one note per contact, but
		// the client can only handle one note. Here and in 
		// ManagedProfile.setNote() we try to work around this.
		int index = -1;
		for (int i = 0; i < notes.length; i++)
		{
			Note candidate = notes[i];
			if ((candidate != null) && !candidate.isEmpty() && ((index == -1) || (notes[index].getSabDetailId() > candidate.getSabDetailId())))
			{
				index = i; 
			}
		}
		return (index == -1) ? null : notes[index];
	}

	public String getStatus() 
	{
		return status;
	}	
	
	public ExternalNetwork getStatusSource()
	{
		return statusSource;
	}
	
	protected void updateDetail(ContactDetail[] details, ContactDetail detail)
	{
		for (int i = 0; i < details.length; i++)
		{
			if ((details[i] != null) && (details[i].getSabDetailId() == detail.getSabDetailId()))
			{
				details[i] = detail;
				break;
			}
		}
	}
	
	protected void removeDetail(ContactDetail[] details, long sabDetailId)
	{
		for (int i = 0; i < details.length; i++)
		{
			if ((details[i] != null) && (details[i].getSabDetailId() == sabDetailId))
			{
				details[i] = null;
				break;
			}
		}		
	}
	
	protected void removeAllDetails(ContactDetail[] details)
	{
		for (int i = 0; i < details.length; i++)
		{
			details[i] = null;
		}		
	}
	
	protected void fillFromServiceObject0(ContactChanges serviceObject)
	{
		if (serviceObject.isAvatarDeleted())
		{
			setProfileImageUrl0(null);
		}
		else
		{
			if (serviceObject.getAvatarURL() == null)
			{
				// no change
			}
			else
			{
				if (nowPlusMember == NOWPLUS_ME)
				{
					//#debug debug
					System.out.println("received avatar url " + serviceObject.getAvatarURL());
				}
				setProfileImageUrl0(serviceObject.getAvatarURL());
			}
		}
		
		if (serviceObject.isNicknameDeleted())
		{
			nickname = null;
			nicknameCri = 0;
		}
		else
		{
			if (serviceObject.getNickname() == null)
			{
				// no change
			}
			else
			{
				nickname = serviceObject.getNickname();
				nicknameCri = 0;
			}
		}
		
		if (serviceObject.isNameDeleted())
		{
			firstName = null;
			middleNames = null;
			lastName = null;
			resetSortName();
			nameCri = 0;
		}
		else
		{
			if ((serviceObject.getFirstName() == null) 
					&& (serviceObject.getMiddleNames() == null) 
					&& (serviceObject.getLastName() == null))
			{
				// no change
			}
			else
			{
				firstName = serviceObject.getFirstName();
				middleNames = serviceObject.getMiddleNames();
				lastName = serviceObject.getLastName();
				resetSortName();
				nameCri = 0;
			}
		}
		
		if (serviceObject.isDateOfBirthDeleted())
		{
			yearOfBirth = 0;
			monthOfBirth = 0;
			dayOfBirth = 0;
			dateOfBirthCri = 0;
		}
		else
		{
			if ((serviceObject.getYearOfBirth() == 0) 
				&& (serviceObject.getMonthOfBirth() == 0)
				&& (serviceObject.getDayOfBirth() == 0))
			{
				// no change
			}
			else
			{
				yearOfBirth = serviceObject.getYearOfBirth();
				monthOfBirth = serviceObject.getMonthOfBirth();
				dayOfBirth = serviceObject.getDayOfBirth();
				dateOfBirthCri = 0;
			}
		}
		
		long[] phoneNumberDetailIds = serviceObject.getPhoneDetailIDs();
		boolean[] phoneNumbersDeleted = serviceObject.isPhoneDeleted();
		String[] phoneNumberTypes = serviceObject.getPhoneTypes();
		String[] phoneNumbers = serviceObject.getPhones();
		boolean[] phonePrefs = serviceObject.getPhonePrefs();
		
		if ((phoneNumberDetailIds != null) && (phoneNumbersDeleted != null)
				&& (phoneNumberTypes != null) && (phoneNumbers != null) && (phonePrefs != null))
		{
			for (int i = 0; i < phoneNumberDetailIds.length; i++)
			{
				fillPhoneNumberFromServiceObject(phoneNumberDetailIds[i], 
						phoneNumbersDeleted[i], phoneNumberTypes[i], phoneNumbers[i], phonePrefs[i]);							
			}
		}
		
		long[] emailDetailIds = serviceObject.getEmailDetailIDs();
		boolean[] emailsDeleted = serviceObject.isEmailDeleted();
		String[] emailTypes = serviceObject.getEmailTypes();
		String[] emails = serviceObject.getEmails();
		boolean[] emailPrefs = serviceObject.getEmailPrefs();
		
		if ((emailDetailIds != null) && (emailsDeleted != null) 
				&& (emailTypes != null) && (emails != null) && (emailPrefs != null))
		{
			for (int i = 0; i < emailDetailIds.length; i++)
			{
				fillEmailFromServiceObject(emailDetailIds[i],
						emailsDeleted[i], emailTypes[i], emails[i], emailPrefs[i]);
			}
		}
		
		if (nowPlusMember != NOWPLUS_ME)
		{
			long[] imAccountDetailIds = serviceObject.getImAddressDetailIDs();
			boolean[] imAccountsDeleted = serviceObject.isImAddressDeleted();
			String[] imAccountNetworks = serviceObject.getImAddressServices();
			String[] imAccountNames = serviceObject.getImAddresses();
			if ((imAccountDetailIds != null) && (imAccountsDeleted != null) 
					&& (imAccountNetworks != null) && (imAccountNames != null))
			{
				for (int i = 0; i < imAccountDetailIds.length; i++)
				{
					fillImAccountFromServiceObject(imAccountDetailIds[i], imAccountsDeleted[i], 
							imAccountNetworks[i], imAccountNames[i], false);
				}
			}
			
			long[] snAccountDetailIds = serviceObject.getSocialNetworkDetailIDs();
			boolean[] snAccountsDeleted = serviceObject.isSocialNetworkDeleted();
			String[] snAccountNetworks = serviceObject.getSocialNetworkNames();
			String[] snAccountNames = serviceObject.getSocialNetworkNames();  // sic!
			String[] snAccountUrls = serviceObject.getSocialNetworkAccountNames();  // sic!
			if ((snAccountDetailIds != null) && (snAccountsDeleted != null) 
					&& (snAccountNetworks != null) && (snAccountNames != null) && (snAccountUrls != null))
			{
				for (int i = 0; i < snAccountDetailIds.length; i++) 
				{
					fillSnAccountFromServiceObject(snAccountDetailIds[i], snAccountsDeleted[i],
							snAccountNetworks[i], snAccountNames[i], snAccountUrls[i]);
				}
			}	
		}
		
		if (serviceObject.isUrlDeleted())
		{
			url = null;
		}
		else
		{
			if (serviceObject.getUrl() == null)
			{
				// no change
			}
			else
			{
				url = Identity.createUrl(serviceObject.getUrl());
			}
		}
		
		long[] addressDetailIds = serviceObject.getAddressDetailIDs();
		boolean[] addressesDeleted = serviceObject.isAddressDeleted();
		String[] addressTypes = serviceObject.getAddressTypes();
		String[][] addrs = serviceObject.getAddresses();
		
		if ((addressDetailIds != null) && (addressesDeleted != null)
			&& (addressTypes != null) && (addrs != null))
		{
			for (int i = 0; i < addressDetailIds.length; i++) 
			{
				fillAddressFromServiceObject(addressDetailIds[i], addressesDeleted[i],
					addressTypes[i], addrs[i]);
			}
		}
		
		if (serviceObject.isTitleDeleted())
		{
			title = null;
			titleCri = 0;
		}
		else
		{
			if (serviceObject.getTitle() == null)
			{
				// no changes
			}
			else
			{
				title = serviceObject.getTitle();
				titleCri = 0;
			}
		}
		
		if (serviceObject.isOrganizationDeleted())
		{
			organisation = null;
			department = null;
			organisationCri = 0;
		}
		else
		{
			if ((serviceObject.getDepartment() == null) && (serviceObject.getOrganization() == null))
			{
				// no changes
			}
			else
			{
				organisation = serviceObject.getOrganization();
				department = serviceObject.getDepartment(); 
				organisationCri = 0;
			}
		}		
		
		long[] noteDetailIds = serviceObject.getNotesDetailIDs();
		boolean[] notesDeleted = serviceObject.isNotesDeleted();
		String[] ns = serviceObject.getNotes();
		
		if ((noteDetailIds != null) && (notesDeleted != null)
			&& (ns != null))
		{
			for (int i = 0; i < noteDetailIds.length; i++) 
			{
				fillNoteFromServiceObject(noteDetailIds[i], notesDeleted[i],
					ns[i]);
			}
		}
		
		if (serviceObject.isStatusTextDeleted())
		{
			status = null;
			statusSource = null;
			statusCri = 0;
		}
		else
		{
			if ((serviceObject.getStatusText() == null) 
					&& (serviceObject.getStatusTextNetwork() == null))
			{
				// no change
			}
			else
			{
				status = serviceObject.getStatusText();
				statusSource = ExternalNetwork.manager.findNetworkById(serviceObject.getStatusTextNetwork());
				statusCri = 0;
			}
		}
	}
	
	protected void fillPhoneNumberFromServiceObject(long sabDetailId, boolean deleted, String subtypeLabel, String phoneNumber, boolean preferred)
	{
		if (deleted)
		{
			removeDetail(identities, sabDetailId);
		}
		else
		{
			Identity oldPhone = getIdentity(sabDetailId);
			if (oldPhone == null)
			{
				addIdentity0(Identity.createPhoneNumber(Identity.toSubtype(subtypeLabel), phoneNumber, preferred, sabDetailId));
			}
			else
			{
				updateIdentity0(Identity.createPhoneNumber(Identity.toSubtype(subtypeLabel), oldPhone.getNabSubtypes(), phoneNumber, preferred, sabDetailId));
			}
		}
	}
	
	private void fillEmailFromServiceObject(long sabDetailId, boolean deleted, String subtypeLabel, String email, boolean preferred)
	{
		if (deleted)
		{
			removeDetail(identities, sabDetailId);
		}
		else
		{
			try 
			{
				Identity oldEmail = getIdentity(sabDetailId);
				if (oldEmail == null)
				{
					Identity newAccount = Identity.createEmail(Identity.toSubtype(subtypeLabel), email, preferred, sabDetailId);
					addIdentity0(newAccount);
					//#if activate.embedded.360email
					Email.manager.index(this, newAccount);
					//#endif
				}
				else
				{
					updateIdentity0(Identity.createEmail(Identity.toSubtype(subtypeLabel), oldEmail.getNabSubtypes(), email, preferred, sabDetailId));
				}
			}
			catch (InvalidValueException e)
			{
				//#debug error
				System.out.println("Received invalid email: " + email);
			}
		}
	}	
	
	protected void fillImAccountFromServiceObject(long sabDetailId, boolean deleted, String networkId, String name, boolean preferred)
	{
		if (deleted)
		{
			removeDetail(identities, sabDetailId);
		}
		else
		{
			ExternalNetwork network = ExternalNetwork.manager.findNetworkById(networkId);

			Identity oldAccount = getIdentity(sabDetailId);
			if (oldAccount == null)
			{
				Identity newAccount = Identity.createImAccount(network, name, preferred, sabDetailId);
				addIdentity0(newAccount);
				ExternalNetwork.manager.index(this, newAccount);
			}
			else
			{
				updateIdentity0(Identity.createImAccount(network, name, preferred, sabDetailId));
			}
		}
	}
	
	private void fillSnAccountFromServiceObject(long sabDetailId, boolean deleted, String networkId, String name, String profileUrl)
	{
		if (deleted)
		{
			removeDetail(identities, sabDetailId);
		}
		else
		{
			ExternalNetwork network = ExternalNetwork.manager.findNetworkById(networkId);

			Identity oldAccount = getIdentity(sabDetailId);
			if (oldAccount == null)
			{
				Identity newAccount = Identity.createSnAccount(network, name, profileUrl, sabDetailId);
				addIdentity0(newAccount);
			}
			else
			{
				updateIdentity0(Identity.createSnAccount(network, name, profileUrl, sabDetailId));
			}
		}
	}	
	
	private void fillAddressFromServiceObject(long sabDetailId, boolean deleted, String typeLabel, String[] addressDetails)
	{	
		if (deleted)
		{
			removeDetail(addresses, sabDetailId);
		}
		else
		{
			if (addressDetails.length != 7)
			{
				//#debug error
				System.out.println("Received invalid address: " + ArrayUtils.toString(addressDetails));
				
				return;
			}
			
			Address oldAddress = getAddress(sabDetailId);
			if (oldAddress == null)
			{
				addAddress0(Address.createAddress(Address.toType(typeLabel), 
						addressDetails[0],addressDetails[1], addressDetails[2], addressDetails[3], addressDetails[5], addressDetails[4], addressDetails[6], sabDetailId));
			}
			else
			{
				updateDetail(addresses, Address.createAddress(Address.toType(typeLabel), oldAddress.getNabSubtypes(),
						addressDetails[0],addressDetails[1], addressDetails[2], addressDetails[3], addressDetails[5], addressDetails[4], addressDetails[6], sabDetailId));
			}
		}
	}
	
	private void fillNoteFromServiceObject(long sabDetailId, boolean deleted, String content)
	{	
		if (deleted)
		{
			removeDetail(notes, sabDetailId);
		}
		else
		{	
			Note oldNote = getNote(sabDetailId);
			if (oldNote == null)
			{
				addNote0(new Note(content, sabDetailId));
			}
			else
			{
				updateDetail(notes, new Note(content, sabDetailId));
			}
		}
	}	
	
	/**
	 * Fills the given service object with the changes in this profile.
	 */
	public boolean fill(ContactChanges serviceObject, long startOfSession)
	{
		int changes = 10;
		
		if ((nicknameCri == 0) || (nicknameCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if (nickname == null)
			{
				serviceObject.setNicknameDeleted();
			}
			else
			{
				serviceObject.setNickname(nickname);
			}
		}
		
		if ((nameCri == 0) || (nameCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if ((firstName == null) && (middleNames == null) && (lastName == null))
			{
				serviceObject.setNameDeleted();
			}
			else
			{
				serviceObject.setName(firstName, middleNames, lastName);
			}
		}

		if ((dateOfBirthCri == 0) || (dateOfBirthCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if ((yearOfBirth == 0) && (monthOfBirth == 0) && (dayOfBirth == 0))
			{
				serviceObject.setDateOfBirthDeleted();
			}
			else
			{
				serviceObject.setDateOfBirth(yearOfBirth, monthOfBirth, dayOfBirth);
			}
		}

		boolean changed = false;
		for (int i = 0; i < identities.length; i++)
		{
			Identity candidate = identities[i];
			if (candidate != null)
			{
				if ((candidate.getCri() == 0) || (candidate.getCri() > startOfSession))
				{
					// no change, or change already sent
				}
				else
				{
					if (candidate.getType() == Identity.TYPE_PHONE)
					{
						fillPhoneNumber(serviceObject, candidate, startOfSession);
						changed = true;
					}
					else
					if (candidate.getType() == Identity.TYPE_EMAIL)
					{
						fillEmail(serviceObject, candidate, startOfSession);
						changed = true;
					}
				}
			}
		}
		if (!changed)
		{
			changes--;
		}
		
		if ((url == null) || (url.getCri() == 0) || (url.getCri() > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if (url.isEmpty())
			{
				serviceObject.setUrlDeleted();
			}
			else
			{
				serviceObject.setUrl(url.getUrl(), null);
			}
		}
		
		changed = false;
		for (int i = 0; i < addresses.length; i++)
		{
			Address candidate = addresses[i];
			if (candidate != null) 
			{
				if ((candidate.getCri() == 0) || (candidate.getCri() > startOfSession))
				{
					// no change, or change already sent
				}
				else
				{
					fillAddress(serviceObject, candidate, startOfSession);
					changed = true;
				}
			}
		}
		if (!changed)
		{
			changes--;
		}
		
		if ((titleCri == 0) || (titleCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if (title == null)
			{
				serviceObject.setTitleDeleted();
			}
			else
			{
				serviceObject.setTitle(title);
			}
		}	
		
		if ((organisationCri == 0) || (organisationCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if ((department == null) && (organisation == null))
			{
				serviceObject.setOrganizationDeleted();
			}
			else
			{
				serviceObject.setOrganization(organisation, department);
			}
		}	
		
		changed = false;
		for (int i = 0; i < notes.length; i++)
		{
			Note candidate = notes[i];
			if (candidate != null) 
			{
				if ((candidate.getCri() == 0) || (candidate.getCri() > startOfSession))
				{
					// no change, or change already sent
				}
				else
				{
					fillNote(serviceObject, candidate, startOfSession);
					changed = true;
				}
			}
		}
		if (!changed)
		{
			changes--;
		}	
		
		if ((statusCri == 0) || (statusCri > startOfSession))
		{
			// no change, or change already sent
			changes--;
		}
		else
		{
			if (status == null)
			{
				serviceObject.setStatusTextDeleted();
			}
			else
			{
				serviceObject.setStatusText(status);
			}
		}
		return (changes > 0);
	}
	
	private void fillPhoneNumber(ContactChanges serviceObject, Identity identity, long startOfSession)
	{
		long index;
		if (identity.isEmpty())
		{
			index = serviceObject.deleteVcardPhoneForContact(identity.getSabDetailId());
		}
		else
		{
			if (identity.getSabDetailId() == 0)
			{
				index = serviceObject.addVcardPhoneToContact(identity.getName(), Identity.toSubtypeLabel(identity.getSubtype()), identity.isPreferred());							
			}
			else
			{
				index = serviceObject.updateVcardPhoneForContact(identity.getSabDetailId(), identity.getName(), Identity.toSubtypeLabel(identity.getSubtype()), identity.isPreferred());
			}	
		}		
		identity.setCri(index + 1);
	}
	
	private void fillEmail(ContactChanges serviceObject, Identity identity, long startOfSession)
	{
		long index;
		if (identity.isEmpty())
		{
			index = serviceObject.deleteVcardEmailForContact(identity.getSabDetailId());
		}
		else
		{
			if (identity.getSabDetailId() == 0)
			{
				index = serviceObject.addVcardEmailToContact(identity.getName(), Identity.toSubtypeLabel(identity.getSubtype()), identity.isPreferred());							
			}
			else
			{
				index = serviceObject.updateVcardEmailForContact(identity.getSabDetailId(), identity.getName(), Identity.toSubtypeLabel(identity.getSubtype()), identity.isPreferred());
			}	
		}		
		identity.setCri(index + 1);
	}
	
	private void fillAddress(ContactChanges serviceObject, Address address, long startOfSession)
	{
		long index;
		if (address.isEmpty())
		{
			index = serviceObject.deleteVcardAddressForContact(address.getSabDetailId());
		}
		else
		{
			if (address.getSabDetailId() == 0)
			{
				index = serviceObject.addVcardAddressToContact(address.getStreet2(), address.getStreet1(), address.getTown(), address.getRegion(), address.getPostcode(), address.getCountry(), Address.toTypeLabel(address.getType()));							
			}
			else
			{
				index = serviceObject.updateVcardAddressForContact(address.getSabDetailId(), address.getStreet2(), address.getStreet1(), address.getTown(), address.getRegion(), address.getPostcode(), address.getCountry(), Address.toTypeLabel(address.getType()));
			}	
		}
		address.setCri(index + 1);
	}
	
	private void fillNote(ContactChanges serviceObject, Note note, long startOfSession)
	{
		long index;
		if (note.isEmpty())
		{
			index = serviceObject.deleteVcardNotesForContact(note.getSabDetailId());
		}
		else
		{
			if (note.getSabDetailId() == 0)
			{
				index = serviceObject.addVcardNotesToContact(note.getContent());							
			}
			else
			{
				index = serviceObject.updateVcardNotesForContact(note.getSabDetailId(), note.getContent());
			}	
		}		
		note.setCri(index + 1);
	}	
	
	public abstract void load(boolean immediately);
	
	public abstract void unload();

	/**
	 * Checks if a string is loaded yet, and if not, returns a default string.
	 */
	protected String checkForNull(String value, String defaultValue)
	{
		return (value == null) ? defaultValue : value;
	}
	
	/**
	 * Checks if an integer is loaded yet, and if not, returns a default integer.
	 */
	protected int checkForZero(int value, int defaultValue)
	{
		return (value == 0) ? defaultValue : value;
	}

	public boolean equals(Object obj) {
		Profile profile = (Profile)obj;
		return this.userId == profile.getUserId();
	}	

	
}
