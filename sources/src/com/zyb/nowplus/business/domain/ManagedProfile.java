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

import java.util.Calendar;
import java.util.Date;

import com.zyb.nowplus.data.protocol.types.BulkUpdateContactsResult;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.util.Collator;
import com.zyb.util.HashUtil;

/**
 * Represents a profile that's managed on the client, that 
 * is the profile of the user of the application or the profile
 * of a contact of the user.
 */
public abstract class ManagedProfile extends Profile
{	
	protected static final int MAX_NUMBER_OF_MULTIPLE_VALUES_PER_FIELD_PER_REQUEST = 20;
	
	private static final int CONTAINER_STATUS_NO_COMPLEX_DATA = 1;
	private static final int CONTAINER_STATUS_COMPLEX_DATA_LOADING = 2;
	private static final int CONTAINER_STATUS_COMPLEX_DATA_LOADED = 3;

	// The container functionality should be encapsulated in a different 
	// (super)class, but for efficiency reasons and since ManagedProfile
	// is the only container, it is included in this class.
	private int containerStatus;
	private int containerRequests;
	private Thread containerOwner;
	
	// guards container requests and container status 
	private Object loadLock = new Object();
	
	/**
	 * The id of the profile in the client address book.
	 */
	protected long cabId;
	
	/**
	 * The id of the profile in the server address book.
	 * If a contact profile is created, it has id = 0. When it
	 * is send to the server, it is set to the inverse of
	 * the id of the ADD request. When the ADD response
	 * is received, it is set to the id the server uses.
	 * My profile is never created on the client, it's always received
	 * from the server.
	 */
	protected long sabId;
	
	public ManagedProfile() 
	{
		this.containerStatus = CONTAINER_STATUS_NO_COMPLEX_DATA;
	}
	
	/**
	 * Indicates if the profile is sync'd to the native phonebook.
	 */
	public abstract boolean syncToNab();
	
	public void setCabId(long cabId)
	{
		this.cabId = cabId;
	}
	
	public long getCabId()
	{
		return cabId;
	}
		
	public void setSabId(long sabId)
	{
		this.sabId = sabId;
	}
	
	public long getSabId()
	{
		return sabId;
	}
	
	public void setNickname(String nickname)
	{
		checkLock();
		if (!HashUtil.equals(this.nickname, nickname))
		{
			this.nickname = nickname;
			this.nicknameCri = -1;
		}
	}
	
	public void setName(String firstName, String middleNames, String lastName) throws InvalidValueException
	{
		checkLock();
		if (!HashUtil.equals(this.firstName, firstName))
		{
			this.firstName = firstName;
			this.nameCri = -1;
		}
		if (!HashUtil.equals(this.middleNames, middleNames))
		{
			this.middleNames = middleNames;
			this.nameCri = -1;
		}
		if (!HashUtil.equals(this.lastName, lastName))
		{
			this.lastName = lastName;
			this.nameCri = -1;
		}
		resetSortName();
		if (Collator.isEmpty(firstName) && Collator.isEmpty(middleNames) && Collator.isEmpty(lastName))
		{
			throw new InvalidValueException(InvalidValueException.TYPE_CONTACT_WITHOUT_NAME);
		}
	}
	
	public void setDateOfBirth(Date dateOfBirth) 
	{
		checkLock();
		if (dateOfBirth == null)
		{
			setDateOfBirth(0, 0, 0);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateOfBirth);
			setDateOfBirth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		}
	}
	
	private void setDateOfBirth(int year, int month, int day)
	{
		if ((yearOfBirth != year) || (monthOfBirth != month) || (dayOfBirth != day))
		{
			this.yearOfBirth = year;
			this.monthOfBirth = month;
			this.dayOfBirth = day;
			this.dateOfBirthCri = -1;
		}
	}
	
	public void addIdentity(Identity identity)
	{
		checkLock();
		if ((identity != null) && !identity.isEmpty())
		{
			addIdentity0(identity);
			identity.setCri(-1);
		}
	}
	
	public void updateIdentity(Identity oldIdentity, Identity identity)
	{
		checkLock();
		if (identity != null)
		{
			if (identity.getSabDetailId() == 0)
			{
				updateIdentity0(oldIdentity, identity);
			}
			else
			{
				updateIdentity0(identity);
			}
			identity.setCri(-1);
		}
	}
	
	public void removeIdentity(Identity identity)
	{
		checkLock();
		if (identity != null)
		{
			if (identity.getSabDetailId() == 0)
			{
				removeIdentity0(identity);
			}
			else
			{
				Identity emptyIdentity = Identity.createEmptyIdentity(identity.getType(), identity.getNabSubtypes(), identity.getSabDetailId());
				updateIdentity0(emptyIdentity);
				emptyIdentity.setCri(-1);
			}
		}
	}
	
	public void setUrl(Identity identity)
	{
		checkLock();
		if (!HashUtil.equals(url, identity))
		{
			if (identity == null)
			{
				url = Identity.createEmptyIdentity(url.getType(), url.getNabSubtypes(), 0);
			}
			else
			{
				url = identity;
			}
			url.setCri(-1);
		}
	}
	
	public void setMyPresences(String[] networkIds, int[] presences)
	{
		load(true);
		try
		{
			lock();
			super.setMyPresences(networkIds, presences);
			commit(true, false, false);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't update social network presences because contact is being updated." + e);
		}
		unload();		
	}
	
	public void setPresences(String[] networks, int[] presences)
	{
		load(true);
		try
		{
			lock();
			super.setPresences(networks, presences);
			commit(true, false, false);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't update chat network presences because contact is being updated." + e);
		}
		unload();
	}
	
	public Channel setConversationId(String network, String name, String oldConversationId, String newConversationId)
	{
		Channel channel = null;
		load(true);
		try
		{
			lock();
			channel = super.setConversationId(network, name, oldConversationId, newConversationId);
			commit(true, true, true);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't update conversation id because contact is being updated." + e);
		}
		unload();
		return channel;
	}
	
	public Channel addMessage(String network, String name, String conversationId, Message message)
	{
		Channel channel = null;
		load(true);
		try
		{
			lock();
			channel = super.addMessage(network, name, conversationId, message);
			commit(true, true, true);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't add message because contact is being updated." + e);
		}
		unload();
		return channel;
	}
	
	public void addAddress(Address address)
	{
		checkLock();
		if ((address != null) && !address.isEmpty())
		{
			addAddress0(address);
			address.setCri(-1);
		}
	}

	public void updateAddress(Address oldAddress, Address address)
	{
		checkLock();
		if (address != null)
		{
			if (address.getSabDetailId() == 0)
			{
				updateAddress0(oldAddress, address);
			}
			else
			{
				updateDetail(addresses, address);
			}
			address.setCri(-1);
		}
	}
	
	public void removeAddress(Address address)
	{
		checkLock();
		if (address != null)
		{
			if (address.getSabDetailId() == 0)
			{
				removeAddress0(address);
			}
			else
			{
				Address emptyAddress = Address.createEmptyAddress(address.getNabSubtypes(), address.getSabDetailId());
				updateDetail(addresses, emptyAddress);
				emptyAddress.setCri(-1);
			}
		}
	}
	
	public void setWorkDetails(String[] workDetails) 
	{
		checkLock();
		if (workDetails == null)
		{
			setWorkDetails(null, null, null);
		}
		else
		{
			setWorkDetails(workDetails[2], workDetails[1], workDetails[0]);
		}
	}
	
	private void setWorkDetails(String title, String department, String organisation)
	{
		if (!HashUtil.equals(this.title, title))
		{
			this.title = title;
			this.titleCri = -1;
		}
		if (!HashUtil.equals(this.department, department))
		{
			this.department = department;
			this.organisationCri = -1; // because department and organisation are one field
		}
		if (!HashUtil.equals(this.organisation, organisation))
		{
			this.organisation = organisation;
			this.organisationCri = -1;
		}
	}
	
	public void setNote(String content)
	{
		checkLock();
		
		// The server has more than one note per contact, but
		// the client can only handle one note. Here and in 
		// Profile.getNote() we try to work around this.
		Note oldNote = getNote();
		if (oldNote == null)
		{
			if (Collator.isEmpty(content))
			{
				// ignore
			}
			else
			{
				Note note = new Note(content, 0);
				addNote0(note);
				note.setCri(-1);
			}
		}
		else
		{
			if (Collator.isEmpty(content))
			{
				Note note = new Note(null, oldNote.getSabDetailId());
				updateDetail(notes, note);
				note.setCri(-1);
			}
			else
			{
				Note note = new Note(content, oldNote.getSabDetailId());
				updateDetail(notes, note);
				note.setCri(-1);
			}
		}
	}
	
	/**
	 * Sets the change request ids for changed attributes.
	 */
	public void setCris(long startOfSession, int contactChangesRequestId, int groupChangesRequestId)
	{
		contactChangesRequestId *= MAX_NUMBER_OF_MULTIPLE_VALUES_PER_FIELD_PER_REQUEST;
		
		checkLock();
		long cri = startOfSession + contactChangesRequestId;
		if ((nicknameCri == 0) || (nicknameCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			nicknameCri = cri;
		}
		if ((nameCri == 0) || (nameCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			nameCri = cri;
		}
		if ((dateOfBirthCri == 0) || (dateOfBirthCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			dateOfBirthCri = cri;
		}
		setCris(identities, startOfSession, cri);
		if ((url == null) || (url.getCri() == 0) || (url.getCri() > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			url.setCri(cri);
		}
		setCris(addresses, startOfSession, cri);
		if ((titleCri == 0) || (titleCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			titleCri = cri;
		}		
		if ((organisationCri == 0) || (organisationCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			organisationCri = cri;
		}
		setCris(notes, startOfSession, cri);
		if ((statusCri == 0) || (statusCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			statusCri = cri;
		}
		commit(true, true, true);
	}

	private void setCris(ContactDetail[] details, long startOfSession, long cri)
	{
		for (int i = 0; i < details.length; i++)
		{
			if (details[i] != null)
			{
				if ((details[i].getCri() == 0) || (details[i].getCri() > startOfSession))
				{
					// no change, or already sent
				}
				else
				{
					details[i].setCri(cri + details[i].getCri());
				}
			}
		}
	}
	
	public void resetCris(long startOfSession, int requestId, long[] detailIds)
	{
		requestId *= MAX_NUMBER_OF_MULTIPLE_VALUES_PER_FIELD_PER_REQUEST;
		
		checkLock();
		long cri = startOfSession + requestId;
		if (nicknameCri == cri)
		{
			nicknameCri = 0;
		}
		if (nameCri == cri)
		{
			nameCri = 0;
		}
		if (dateOfBirthCri == cri)
		{
			dateOfBirthCri = 0;
		}
		resetCris(identities, cri, detailIds);
		
		if ((url != null) && (url.getCri() == cri))
		{
			if (url.isEmpty())
			{
				url = null;
			}
			else
			{
				url.setCri(0);
			}
		}
		resetCris(addresses, cri, detailIds);
		
		if (titleCri == cri)
		{
			titleCri = 0;
		}
		if (organisationCri == cri)
		{
			organisationCri = 0;
		}
		resetCris(notes, cri, detailIds);
		
		if (statusCri == cri)
		{
			statusCri = 0;
		}
		commit(true, true, true);
	}
	
	private void resetCris(ContactDetail[] details, long cri, long[] detailIds)
	{
		for (int i = 0; i < details.length; i++)
		{
			if ((details[i] != null) && (1 <= details[i].getCri() - cri) && (details[i].getCri() - cri <= detailIds.length)) 
			{
				int j = (int) (details[i].getCri() - cri) - 1;
				if (detailIds[j] == BulkUpdateContactsResult.ERROR_OCCURRED)
				{
					//#debug error
					System.out.println("Change in " + details[i] + " failed on server: try again");
				}
				else
				{
					 if (details[i].isEmpty())
					 {
						 details[i] = null;
					 }
					 else
					 {
						 details[i].setSabDetailId(detailIds[j]);
						 details[i].setCri(0);
					 }
				}
			}
		}		
	}
	
	public void fillInitial()
	{
		containerStatus = CONTAINER_STATUS_COMPLEX_DATA_LOADED;
	}
	
	/**
	 * Fills the simple data with the simple data of the given record.
	 */
	public void fillFromSimple(ManagedProfileRecord record) 
	{
		cabId = record.getCabId();
		sabId = record.getSabId();
		userId = record.getUserId();
		nowPlusMember = record.getNowPlusMember();
		nowPlusPresence = record.getNowPlusPresence();
		firstName = record.getFirstName();
		middleNames = record.getMiddleNames();
		lastName = record.getLastName();
		nameCri = record.getNameCri();
		
		// temporarily include status in simple data till loading in contacts list is revised
		if (record.getStatus() == null) {
			status = null;
			statusSource = null;
		}
		else {
			status = record.getStatus();
			statusSource = ExternalNetwork.manager.findNetworkById(record.getStatusSourceNetworkId());
		}

		statusCri = record.getStatusCri();
	}

	/**
	 * Fills the complex data with the complex data of the given record.
	 */
	public void fillFromComplex(ManagedProfileRecord record) 
	{
		if (record == null)
		{
			// This can happen if the profile has been deleted from the
			// store, but there is still a reference to it (in Activity 
			// for example.
		}
		else
		{
			setProfileImageUrl0(record.getProfileImageURL());
			nickname = record.getNickname();
			nicknameCri = record.getNicknameCri();
			yearOfBirth = record.getYearOfBirth();
			monthOfBirth = record.getMonthOfBirth();
			dayOfBirth = record.getDayOfBirth();
			dateOfBirthCri = record.getDateOfBirthCri();
			
			removeAllDetails(identities);
			Identity[] ids = record.getIdentities();

			if (ids != null) {
				for (int i = 0; i < ids.length; i++) {
					addIdentity0(ids[i]);
				}
			}
			
			url = record.getUrl();
			
			removeAllDetails(addresses);
			Address[] addrs = record.getAddresses();

			if (addrs != null)
			{
				for (int i = 0; i < addrs.length; i++)
				{
					addAddress0(addrs[i]);
				}
			}
			
			title = record.getTitle();
			titleCri = record.getTitleCri();
			department = record.getDepartment();
			organisation = record.getOrganisation();
			organisationCri = record.getOrganisationCri();

			removeAllDetails(notes);
			Note[] ns = record.getNotes();

			if (ns != null)
			{
				for (int i = 0; i < ns.length; i++)
				{
					addNote0(ns[i]);
				}
			}
			
//			if (record.getStatus() == null)   temporarily include status in simple data till loading in contacts list is revised
//			{
//				status = null;
//				statusSource = null;
//			}
//			else
//			{
//				status = record.getStatus();
//				statusSource = ExternalNetwork.manager.findNetworkById(record.getStatusSourceNetworkId());
//			}
//			statusCri = record.getStatusCri();
		}
		
		containerStatus = CONTAINER_STATUS_COMPLEX_DATA_LOADED;
	}

	/**
	 * Fills the given record with this contact's data.
	 */
	public void fill(ManagedProfileRecord record) 
	{
		record.setCabId(cabId);
		record.setSabId(sabId);
		record.setUserId(userId);
		record.setNowPlusMember(nowPlusMember);
		record.setNowPlusPresence(nowPlusPresence);
		record.setProfileImageURL(profileImage.getUrl());
		record.setNickname(nickname);
		record.setNicknameCri(nicknameCri);
		record.setFirstName(firstName);
		record.setMiddleNames(middleNames);
		record.setLastName(lastName);
		record.setNameCri(nameCri);
		record.setYearOfBirth(yearOfBirth);
		record.setMonthOfBirth(monthOfBirth);
		record.setDayOfBirth(dayOfBirth);
		record.setDateOfBirthCri(dateOfBirthCri);
		record.setIdentities(getIdentities(true));
		record.setUrl(url);
		record.setAddresses(getAddresses(true));
		record.setTitle(title);
		record.setTitleCri(titleCri);
		record.setDepartment(department);
		record.setOrganisation(organisation);
		record.setOrganisationCri(organisationCri);
		record.setNotes(getNotes(true));
		
		if (status == null)
		{
			record.setStatus(null);
			record.setStatusSourceNetworkId(null);
		}
		else
		{
			record.setStatus(status);
			record.setStatusSourceNetworkId(statusSource.getNetworkId());
		}

		record.setStatusCri(statusCri);
	}
	
	/**
	 * Applies the changes in the given service object to this profile.
	 */
	public void fillFromServiceObject(ContactChanges contact, boolean silent)
	{
		load(true);

		try
		{
			lock();
			fillFromServiceObject0(contact);
			commit(true, false, silent);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't merge because profile is being updated.");
		}

		unload();
	}
	
	/**
	 * Loads complex data in the container.
	 */
	public void load(boolean immediately)
	{
		synchronized (loadLock)
		{
			containerRequests++;

			if (containerStatus == CONTAINER_STATUS_NO_COMPLEX_DATA)
			{
				containerStatus = CONTAINER_STATUS_COMPLEX_DATA_LOADING;
				if (immediately)
				{
					Profile.manager.executeRequest(this);
				}
				else
				{
					Profile.manager.request(this);
				}
			}
			else if (containerStatus == CONTAINER_STATUS_COMPLEX_DATA_LOADING)
			{
				if (immediately)
				{
					Profile.manager.cancelRequest(this);
					Profile.manager.executeRequest(this);
				}
			}
		}
	}

	/**
	 * Locks the container for state changes.
	 */
	public synchronized void lock() throws LockException
	{	
		if (containerStatus != CONTAINER_STATUS_COMPLEX_DATA_LOADED) 
		{
			throw new RuntimeException("Container needs to be loaded before it can be locked.");	
		}

		if (containerOwner != null)
		{
			try {
				wait(3 * 1000); // wait 3 secs for container to be unlocked
			}
			catch (InterruptedException e) {
			}
		}

		if (containerOwner != null)
		{
			// container still locked, give up 
			throw new LockException("Container is locked by " + containerOwner);
		}

		containerOwner = Thread.currentThread();
	}
		
	protected void checkLock()
	{
		if (containerOwner == Thread.currentThread()) {
			// ok
		}
		else {
			throw new RuntimeException("Current thread doesn't own lock: please lock before update, commit or revert.");
		}
	}
	
	/**
	 * Commits state changes.
	 */
	public void commit()
	{
		commit(false, false, false);
	}
	
	protected synchronized void commit(boolean local, boolean ignoreList, boolean silent)
	{
		checkLock();
		Profile.manager.commit(this, local, ignoreList, silent);
		containerOwner = null;
		notify();
	}
	
	/**
	 * Reverts state changes.
	 */
	public synchronized void revert()
	{
		checkLock();
		Profile.manager.revert(this);
		containerOwner = null;
		notify();
	}
	
	/**
	 * Unloads complex data from the container.
	 * @return true if the container required unloading.
	 */
	public void unload()
	{
		synchronized (loadLock)
		{
			if (containerOwner == Thread.currentThread())
			{
				throw new RuntimeException("Current thread owns update lock: please commit or revert before unload.");
			}
			if (containerRequests == 0)
			{
				Exception e = new RuntimeException("Can't unload profile that's not loaded.");
				
				//#debug error
				System.out.println("Can't unload profile that's not loaded." + e);
				return;
				
				// throw e;
			}
			containerRequests--;
			if (containerRequests == 0)
			{
				if (containerStatus == CONTAINER_STATUS_COMPLEX_DATA_LOADING)
				{
					Profile.manager.cancelRequest(this);
				}
				else
				{
					release();
				}
				containerStatus = CONTAINER_STATUS_NO_COMPLEX_DATA;
			}
		}
	}
	
	/**
	 * Indicates if the container is loading.
	 */
	public boolean isLoading()
	{
		return (containerStatus == CONTAINER_STATUS_COMPLEX_DATA_LOADING);
	}
	
	public Object getLoadLock()
	{
		return loadLock;
	}
		
	/**
	 * Releases references to complex data.
	 */	
	protected synchronized void release() 
	{
		nickname = null;
		nicknameCri = 0;
		yearOfBirth = 0;
		monthOfBirth = 0;
		dayOfBirth = 0;
		dateOfBirthCri = 0;
		removeAllDetails(identities);
		url = null;
		removeAllDetails(addresses);
		title = null;
		titleCri = 0;
		organisation = null;
		department = null;
		organisationCri = 0;
		removeAllDetails(notes);
//		status = null;                temporarily include status in simple data till loading in contacts list is revised
//		statusSource = null;
//		statusCri = 0;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof ManagedProfile)
		{
			ManagedProfile that = (ManagedProfile) o;
			return this.cabId == that.cabId;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isDeleted()
	{
		return manager.isDeleted(sabId);
	}
	
}
