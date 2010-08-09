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
package com.zyb.nowplus.business;

import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ContactList;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.GroupManager;
import com.zyb.nowplus.business.domain.InvalidValueException;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.business.domain.ProfileManager;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.business.domain.filters.GroupFilter;
import com.zyb.nowplus.business.domain.filters.NullFilter;
import com.zyb.nowplus.business.domain.filters.PresenceFilter;
import com.zyb.nowplus.business.sync.Sync;
import com.zyb.nowplus.business.sync.SyncManager;
//#= import com.zyb.nowplus.business.sync.SyncEngine;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.types.BulkUpdateContactsResult;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.storage.DataStore;
import com.zyb.nowplus.data.storage.RMSDataStore5;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.ArrayUtils;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

//leave out 'phonebook' filter for devices not supporting continuous sync
//#if unittesting || polish.pim.sync.always:defined
import com.zyb.nowplus.business.domain.filters.NativePhoneBookFilter;
//#endif

/**
 * The contact manager manages contacts. Contacts are persisted as
 * {@link com.zyb.nowplus.data.storage.DataRecord data records} in a
 * {@link com.zyb.nowplus.data.storage.DataStore data store}, cached in a
 * {@link com.zyb.nowplus.business.domain.ContactList contact list} and
 * requested/received from the {@link com.zyb.nowplus.data.protocol.CommunicationManager
 * communication manager}.
 */
class ContactManager implements GroupManager, ProfileManager, EventListener
{
	public static final String STORE = "contacts";

	/*
	 * BulkUpdateContacts updates 25 contacts 
	 * while GetContactsChanges gets a max. of 25 contacts.
	 * https://wiki.zyb.com/index.php?title=Client_Protocol_Implementation_Comparison
	 */
	private static final int MAX_NUMBER_OF_CONTACT_CHANGES_PER_RESPONSE = 25;//60;

	private static final int MAX_NUMBER_OF_CONTACT_CHANGES_PER_REQUEST = 25;//30;

	/**
	 * If the number of contacts received from the server is above this
	 * threshold, no individual events will be fired, but instead one
	 * event will be fired after all contacts have been added or updated,
	 * without checking if the contact list is affected.
	 */
	private static final int THRESHOLD_INDIVIDUAL_EVENTS = 20;

	/**
	 * expiry time limited into 60 mins
	 */
	private static final long CHANGES_SYNC_EXPIRY = 60 * 60 * 1000;//10 * 60 * 1000;

	private final ServiceBroker services;
	private final Sync syncManager;

	private Group[] groups;
	private int groupsRequestId;

	private MyProfile me;
	private long meChangesSynced;
	private int meRequestId;

	private final ContactList contacts;
	private long contactChangesSynced;
	private int sendContactChangesRequestId;
	private int receiveContactChangesRequestId;
	private boolean fireContactChangesSendEvent;
	private boolean fireContactChangesReceivedEvent;
	private boolean newContactsReceived;

	private final DataStore store;
	
	private Filter[] filters;

	/**
	 * The most recently requested page of contact changes.
	 */
	private int contactChangesPage;

	/**
	 * Indicates if the contact manager has started. If it has, the user
	 * shouldn't change.
	 */
	private boolean running;

	/**
	 * Constructs a contact manager.
	 */
	public ContactManager(ServiceBroker services, EventDispatcher dispatcher)
	{
		this(services, 
			new RMSDataStore5(
				//#if polish.loadContactsOnDemand
				true
				//#else
				//#= false
				//#endif
				),
			new SyncManager(services, dispatcher), dispatcher);
	}

	/**
	 * Constructs a contact manager with the given dependencies.
	 */
	public ContactManager(ServiceBroker services, DataStore store, Sync syncManager, EventDispatcher dispatcher)
	{
		//#debug debug
		System.out.println("Constructing contact manager.");

		this.services = services;
		this.syncManager = syncManager;
		this.contacts = new ContactList(dispatcher);
		this.store = store;

		dispatcher.attach(this);
	}

	/**
	 * Gets the user of the application.
	 */
	public MyProfile getMe()
	{
		return me;
	}

	public void setMyStatus(String status) throws LockException
	{
		if (me != null) {
			try {
				me.load(true);
				me.lock();
				me.setStatus(status);
				me.commit();
			}
			finally {
				me.unload();
			}
		}
	}

	/**
	 * Gets the list of all contacts.
	 */
	public ContactList getContactList()
	{
		return contacts;
	}

	public Profile getProfileByUserId(long userId)
	{
		Profile profile = null;

		if (userId != 0) {
			if ((me != null) && (me.getUserId() == userId)) {
				profile = me;
			}
			if (profile == null) {
				profile = contacts.getContactByUserId(userId);
			}
		}

		return profile;
	}
	
	public ManagedProfile getProfileBySabId(long sabId)
	{
		ManagedProfile profile = null;

		if (sabId != 0)
		{
			if ((me != null) && (me.getSabId() == sabId))
			{
				profile = me;
			}
			if (profile == null)
			{
				profile = contacts.getContactBySabId(sabId);
			}
		}

		return profile;
	}	
	
	public ManagedProfile getProfileByCabId(long cabId)
	{
		ManagedProfile profile = null;

		if (cabId != 0)
		{
			if ((me != null) && (me.getCabId() == cabId))
			{
				profile = me;
			}
			if (profile == null)
			{
				profile = contacts.getContact(cabId);
			}
		}

		return profile;
	}
	
	/**
	 * Starts the contact manager.
	 */
	public void start() throws StorageException
	{
		if (!running) {
			//#debug info
			System.out.println("Starting contact manager.");

			this.groups = services.getSettings().getGroups();

			//RefreshTask task = services.scheduleEvents(Event.Context.CONTACTS, Event.Contacts.REFRESHING);

			store.open(STORE, true);

			ContactProfile[] profiles = new ContactProfile[store.getNumberOfRecords()];

			for (int i = 0; i < profiles.length; i++) {
				ManagedProfileRecord record = (ManagedProfileRecord) store.getShortRecord(i);

				if (record == null) {
					//#debug error
					System.out.println("Corrupted record in slot " + i);
				}
				else {
					if (record.getType() == ManagedProfileRecord.TYPE_MY_PROFILE) {
						me = new MyProfile();
						me.fillFromSimple(record);
							
						//#if !polish.loadContactsOnDemand
						me.load(true);
						//#endif
					}
					else if (record.getType() == ManagedProfileRecord.TYPE_CONTACT_PROFILE) {
						ContactProfile profile = new ContactProfile();
						profiles[i] = profile;

						profile.fillFromSimple(record);
						profile.load(true);
						services.getChatManager().index(profile);

						//#if activate.embedded.360email
							services.getEmailManager().index(profile);
						//#endif
							
						//#if polish.loadContactsOnDemand
							profile.unload();
						//#endif

						//#debug debug
						System.out.println("Loaded " + profile.getCabId() + ":" + profile.getSabId());
					}
				}
			}

			contacts.initialise(services.getSettings().getContactsOrder(), profiles);
			contacts.invalidateAllOnlinePresences();
			
			//task.cancel();

			running = true;

			services.fireEvent(Event.Context.MODEL, Event.Model.GROUPS_CHANGED, null);
			services.fireEvent(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null);

			if (syncManager != null) {
				syncManager.start();
			}
			
			if (services.getSettings().firstLogin()) {
				fireContactChangesSendEvent = true;
				fireContactChangesReceivedEvent = true;
			}
		}
	}

	/**
	 * Indicates the manager is running.
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * Stops the contacts manager.
	 */
	public void stop()
	{
		if (running)
		{
			running = false;
			
			store.close();
			
			if (syncManager != null)
			{
				syncManager.stop();
			}

			//#debug info
			System.out.println("Contact manager stopped.");
		}
	}

	/**
	 * Starts a sync with the native address book.
	 */
	public void importFromNab(boolean firstImport)
	{
		//#debug info
		System.out.println("Start nab-cab import");
		
		if (running)
		{
			if (syncManager == null)
			{
				if (firstImport)
				{
					services.fireEvent(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null);
				}
			}
			else
			{
				if (firstImport)
				{
                    //#debug debug
                    System.out.println("Dispatching Event.Sync.START_IMPORT");
                    services.fireEvent(Event.Context.SYNC, Event.Sync.START_IMPORT, null);

                    //#if polish.pim.sync.firstrun:defined
                    //#foreach firstRunSync in polish.pim.sync.firstrun
                    //#= syncManager.sync(SyncEngine.${firstRunSync}, SyncEngine.CONFLICT_ORIGINATOR_WINS);
                    //#next firstRunSync

					//#debug info
					//#= System.out.println("Waiting for sync...");
                    //#else
                    services.fireEvent(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null);
                    //#endif
				}
				else
				{
                    //#if polish.pim.sync.always:defined
                    //#foreach alwaysSyncRun in polish.pim.sync.always
                    //#= syncManager.sync(SyncEngine.${alwaysSyncRun}, SyncEngine.CONFLICT_RECIPIENT_WINS);
                    //#next alwaysSyncRun
                    //#endif

				}
			}
		}
	}

	/**
     * Cancels the sync with the native addressbook
     */
	public void cancelImportFromNab()
	{
		if (running)
		{
			if (syncManager != null)
			{
				syncManager.cancelSync();
			}
		}
	}

	public boolean hasGroups()
	{
		return (groups != null);
	}

	public Group[] getAvailableGroups()
	{
		Group[] selection = new Group[groups.length];
		int len = 0;

		for (int i = 0; i < groups.length; i++)
		{
			Group candidate = groups[i];
			if ((candidate != null) && (candidate.getType() != Group.TYPE_SOCIAL_NETWORK))
			{
				selection[len++] = candidate;
			}
		}

		return Group.trimArray(selection, len);
	}

	public Group findGroupById(long groupId)
	{
		Group selection = null;

		for (int i = 0; (selection == null) && (i < groups.length); i++)
		{
			Group candidate = groups[i];
			if ((candidate != null) && (candidate.getGroupId() == groupId))
			{
				selection = candidate;
			}
		}
		if (selection == null)
		{
			selection = new Group(groupId, Group.TYPE_SOCIAL_NETWORK, Long.toString(groupId));
			requestGroups(); // hack till group change events are received 
		}
		return selection;
	}

	void requestGroups()
	{
		if (groupsRequestId == 0)
		{
			groupsRequestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.GROUPS, 
					null, null, ServerRequest.MEDIUM_PRIORITY, 2 * 60);

			//#debug info
			System.out.println("Request " + groupsRequestId + ": get groups");
		}
		else
		{
			//#debug info
			System.out.println("Groups already requested.");
		}
	}

	public void groupsReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (requestId != groupsRequestId)
			{
				//#debug info
				System.out.println("Received response to stale request " + requestId + ", ignored");
				return;
			}

			Group[] newGroups = new Group[serviceObjects.length];

			for (int i = 0; i < serviceObjects.length; i++)
			{
				newGroups[i] = createGroupFromServiceObject(serviceObjects[i]);
			}

			if (groups == null)
			{
				groups = newGroups;
				services.groupsReceived();
			}
			else
			{
				groups = newGroups;
			}

			services.getSettings().setGroups(groups);
			groupsRequestId = 0;
			filters = null;
			services.fireEvent(Event.Context.CONTACTS, Event.Contacts.FILTERS_UPDATED, null);
		}
	}

	public boolean errorReceived(int requestId, byte errorCode)
	{
		if (requestId == sendContactChangesRequestId) 
		{
			//#debug info
			System.out.println("Request " + requestId + " to send contact changes failed.");
			
			if (fireContactChangesSendEvent) 
			{
			//#if activate.progress.debugging==false
				fireContactChangesSendEvent = false;//disable reason:Add 'Cancal' cmd into Notification instead of force notification disappeared
			//#endif
				
				services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_NOT_SEND, null);
			}
			return true;
		}
		else
		if (requestId == receiveContactChangesRequestId) 
		{
			//#debug info
			System.out.println("Request " + requestId + " to receive contact changes failed.");
			
			if (fireContactChangesReceivedEvent) 
			{
			//#if activate.progress.debugging==false
				fireContactChangesReceivedEvent = false;//disable reason:Add 'Cancal' cmd into Notification instead of force notification disappeared
			//#endif
				
				services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_NOT_RECEIVED, null);
			}
			return true;
		}
		else		
		{
			return false;
		}
	}
	
	public boolean timeOutReceived(int requestId)
	{
		if (requestId == groupsRequestId)
		{
			//#debug info
			System.out.println("Request " + requestId + " for groups timed out, requested again.");

			groupsRequestId = 0;
			
			requestGroups();
			return true;
		}
		else
		if ((requestId == sendContactChangesRequestId) || (requestId == receiveContactChangesRequestId)) 
		{
			return errorReceived(requestId, (byte) 0);
		}
		else
		{
			return false;
		}
	}

	private Group createGroupFromServiceObject(ServiceObject serviceObject)
	{
		com.zyb.nowplus.data.protocol.types.Group group = (com.zyb.nowplus.data.protocol.types.Group) serviceObject;

		String networkId = ExternalNetwork.getStandardId(group.getGroupNetwork());

		if (group.isSystemGroup()) {
			return null; // for content, not relevant
		}
		else if (group.isReadOnly()) {
			return new Group(group.getGroupID(), Group.TYPE_SOCIAL_NETWORK, group.getGroupName(), networkId);
		}
		else if (true) { // TODO: figure out how to identifiy standard groups
			return new Group(group.getGroupID(), Group.TYPE_CUSTOM, group.getGroupName(), networkId);
		}
		else {
			return new Group(group.getGroupID(), Group.TYPE_STANDARD, group.getGroupName(), networkId);
		}
	}

	private void initFilters()
	{
		Filter[] tmpFilters = new Filter[4 + (groups == null ? 0 : groups.length)];
		int len = 0;

		tmpFilters[len++] = new NullFilter();
		tmpFilters[len++] = new PresenceFilter();
		
		// leave out 'connected' filter as it doesn't add much value atm
		// tmpFilters[len++] = new ConnectedFilter();
		
		//leave out 'phonebook' filter for devices not supporting continuous sync
		//#if unittesting || polish.pim.sync.always:defined
		tmpFilters[len++] = new NativePhoneBookFilter();
		//#endif

		if (groups != null)
		{
			for (int i = 0; i < groups.length; i++)
			{
				Group candidate = groups[i];
				if ((candidate != null) && (candidate.getType() == Group.TYPE_STANDARD))
				{
					tmpFilters[len++] = new GroupFilter(Filter.TYPE_STANDARD_GROUP, candidate);
				}
			}
			for (int i = 0; i < groups.length; i++)
			{
				Group candidate = groups[i];
				if ((candidate != null) && (candidate.getType() == Group.TYPE_CUSTOM))
				{
					tmpFilters[len++] = new GroupFilter(Filter.TYPE_CUSTOM_GROUP, candidate);
				}
			}
			for (int i = 0; i < groups.length; i++)
			{
				Group candidate = groups[i];
				if ((candidate != null) && (candidate.getType() == Group.TYPE_SOCIAL_NETWORK))
				{
					tmpFilters[len++] = new GroupFilter(Filter.TYPE_SOCIAL_NETWORK_GROUP, candidate);
				}
			}
		}
		filters = Filter.trimArray(tmpFilters, len);
	}

	/**
	 * Gets the available contact filters.
	 */
	public Filter[] getAvailableFilters()
	{
		if (filters == null)
		{
			initFilters();
		}
		return filters;
	}

	/**
	 * Creates a new contact. This contact is not in the list until it
	 * is committed.
	 */
	public ContactProfile createContact()
	{
		ContactProfile contact = new ContactProfile();
		//#if !polish.loadContactsOnDemand
		contact.load(true);
		//#endif
		
		contact.setSyncToNab(true);
		return contact;
	}

	/**
	 * Invites a contact who is not a Now+ member to Now+.
	 */
	public void invite(ContactProfile contact) throws InvalidValueException
	{
		if (running)
		{
			if (contact.getNowPlusMember() != ContactProfile.NOWPLUS_CONTACT)
			{
				throw new InvalidValueException(InvalidValueException.TYPE_CONTACT_ALREADY_NOWPLUS_MEMBER);
			}

			String emailOrMSISDN = contact.getEmailOrMSISDN();
			if (emailOrMSISDN == null)
			{
				throw new InvalidValueException(InvalidValueException.TYPE_CONTACT_WITHOUT_EMAIL_OR_PHONENUMBER);
			}

			Hashtable params = new Hashtable();
			params.put("emailormsisdn", emailOrMSISDN);

			int requestId = services.getProtocol().sendRequest(ServerRequest.INVITE, ServerRequest.CONTACTS,
					null, params, ServerRequest.MEDIUM_PRIORITY);

			//#debug debug
			System.out.println("Request " + requestId + ": invite " + emailOrMSISDN + " to Now+.");
		}
	}

	/**
	 * Connect to a contact who is a Now+ member.
	 */
	public void connect(ContactProfile contact) throws InvalidValueException
	{
		if (running)
		{
			if (contact.getNowPlusMember() != ContactProfile.NOWPLUS_MEMBER)
			{
				throw new InvalidValueException(InvalidValueException.TYPE_CONTACT_ALREADY_CONNECTED);
			}

			Hashtable params = new Hashtable();
			params.put("userid", new Integer((int) contact.getUserId()));

			int requestId = services.getProtocol().sendRequest(ServerRequest.ADD, ServerRequest.BUDDY,
					null, params, ServerRequest.MEDIUM_PRIORITY);

			//#debug debug
			System.out.println("Request " + requestId + ": connect with " + contact.getUserId());
		}
	}

	public void commit(ManagedProfile profile, boolean local, boolean ignoreList, boolean silent)
	{
		if (running)
		{
			//#debug debug
			System.out.println("Commit " + profile);

			if (profile instanceof MyProfile)
			{
				commit((MyProfile) profile, local, silent);
			}
			else
			if (profile instanceof ContactProfile)
			{
				commit((ContactProfile) profile, local, ignoreList, silent);
			}
		}
	}

	private void commit(MyProfile profile, boolean local, boolean silent)
	{
		if (profile.getCabId() == 0)
		{
			profile.setCabId(services.getSettings().generateNextCabId());

			if (!local)
			{
				services.getSettings().addToChangeLog(profile.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
			}
			storeNewProfile(profile);

			if (!silent)
			{
				services.fireEvent(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(profile.getCabId()) );
			}
		}
		else
		{
			if (!local)
			{
				services.getSettings().addToChangeLog(profile.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
			}
			storeChangedProfile(profile);

			if (!silent)
			{
				services.fireEvent(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(profile.getCabId()) );
			}
		}
	}

    private void commit(ContactProfile contact, boolean local, boolean ignoreList, boolean silent)
    {
		if ((syncManager != null) && contact.syncToNab())
		{
			syncManager.remap(contact);
		}
		
		if (contact.getCabId() == 0)
		{
			contact.setCabId(services.getSettings().generateNextCabId());

			if (!local)
			{
				services.getSettings().addToChangeLog(contact.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
			}
			storeNewProfile(contact);

			// an add may always affect the list
			contacts.addContact(contact, silent);
		}
		else
		{
			if (!local)
			{
				services.getSettings().addToChangeLog(contact.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
			}

			ContactProfile oldContact = new ContactProfile();

			if (!ignoreList)
			{
				ManagedProfileRecord record = (ManagedProfileRecord) store.getFullRecord(contact.getCabId());
				if (record == null)
				{
					//#debug error
					System.out.println("Can't load old details for profile " + contact.getCabId() + ": record doesn't exist anymore.");
				}
				else
				{
					oldContact.fillFromSimple(record);
				}
			}
			
			storeChangedProfile(contact);

			if (!ignoreList)
			{
				contacts.updateContact(oldContact, contact, silent);
			}
		}
	}

	public void finishedEditing(ManagedProfile profile)
	{
		if (running)
		{
			if (profile.syncToNab())
			{
				services.fireEvent(Event.Context.SYNC, Event.Sync.CAB_CHANGED, new Long(profile.getCabId()));
			}
		}
	}

	public void revert(ManagedProfile contact)
	{
		if (running)
		{
			if (contact.getCabId() == 0)
			{
				// just ignore
			}
			else
			{
				ManagedProfileRecord record = (ManagedProfileRecord) store.getFullRecord(contact.getCabId());
				if (record == null)
				{
					//#debug error
					System.out.println("Can't revert profile " + contact.getCabId() + ": record doesn't exist anymore.");
				}
				else
				{
					contact.fillFromSimple(record);
					contact.fillFromComplex(record);
				}
			}
		}
	}

	/**
	 * Deletes a contact.
	 */
	public void delete(ContactProfile contact, boolean local)
	{
		if (running)
		{
			if (contact.getCabId() == 0)
			{
				// not saved yet, just ignore
			}
			else
			{
				if (!local)
				{
					services.getSettings().addToChangeLog(contact.getSabId(), Settings.CHANGELOG_DELETE);
				}
				
				deleteProfile(contact);
	
				contacts.removeContact(contact.getCabId(), false);
				
				if (contact.syncToNab())
				{
					services.fireEvent(Event.Context.SYNC, Event.Sync.CAB_CHANGED, new Long(contact.getCabId()));
				}
			}
		}
	}

	public byte getContext()
	{
		return Event.Context.MODEL;
	}

	public void handleEvent(byte context, int id, Object data)
	{
		if (context == Event.Context.MODEL)
		{
			//#debug debug
			System.out.println("Contact manager received event " + id);

			if (id == Event.Model.LOAD_PROFILE)
			{
				executeRequest((ManagedProfile) data);
			}
			else
			if (id == Event.Model.ME_CHANGED_IN_CAB)
			{
				sendMeChanges(data == Boolean.TRUE);
			}
			else
			if (id == Event.Model.ME_CHANGED_IN_SAB)
			{
				requestMeChanges();
			}
			else
			if (id == Event.Model.CONTACTS_CHANGED_IN_CAB)
			{
				sendContactChanges(data == Boolean.TRUE);
			}
			else
			if (id == Event.Model.CONTACTS_CHANGED_IN_SAB)
			{
				requestContactChanges();
			}
			else
			if (id == Event.Model.GROUPS_CHANGED)
			{
				requestGroups();
			}
		}
	}

	public void request(ManagedProfile profile)
	{
		services.fireEvent(Event.Context.MODEL, Event.Model.LOAD_PROFILE, profile);
	}

	public void cancelRequest(ManagedProfile profile)
	{
		try
		{
			services.cancelEvent(Event.Context.MODEL, Event.Model.LOAD_PROFILE, profile);
		}
		catch (InterruptedException e)
		{
			//#debug error
			System.out.println("Unexpected exception " + e);
		}
	}

	public void executeRequest(ManagedProfile profile)
	{
		synchronized (profile.getLoadLock())  // load lock is weaker than profile lock, so should be acquired first
		{
			synchronized (profile)
			{
				if (profile.isLoading())
				{
					if (profile.getCabId() == 0)
					{
						profile.fillInitial();
					}
					else
					{
						ManagedProfileRecord record = (ManagedProfileRecord) store.getFullRecord(profile.getCabId());
						profile.fillFromComplex(record);
					}
				}
			}
		}
	}

	private void sendMeChanges(boolean forced)
	{
		if (running && services.isConnectionUp() && (forced || (meChangesSynced + CHANGES_SYNC_EXPIRY < System.currentTimeMillis())))
		{
			meChangesSynced = System.currentTimeMillis();

			sendMeChanges();
		}
		else
		{
			//#debug info
			System.out.println("Network is down or sync in progess... (" + meChangesSynced + ")");
		}
	}

	private void sendMeChanges()
	{
		int requestId = 0;
		
		if ((me != null) && services.getSettings().inChangeLog(me.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE))
		{
			long startOfSession = services.getStartOfSession();

			me.load(true);
			try
			{
				me.lock();

				ContactChanges serviceObject = new ContactChanges();
				if (me.fill(serviceObject, startOfSession))
				{
					requestId = services.getProtocol().sendRequest(ServerRequest.SET, ServerRequest.ME,
						new ServiceObject[] {serviceObject}, null, ServerRequest.MEDIUM_PRIORITY, 2 * 60);

					//#debug info
					System.out.println("Request " + requestId + ": send me changes");

					me.setCris(startOfSession, requestId, 0);
				}
				else
				{
					me.revert();

					// no changes to send
					services.getSettings().removeFromChangeLog(me.getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
				}
				me.unload();
			}
			catch (LockException e)
			{
				//#debug error
				System.out.println("Can't lock the profile, try next time" + e);

				me.unload();
			}
		}
		
		if (requestId == 0) 
		{
			meChangesSynced = 0;
			
			services.fireEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_SAB, null);
		}
	}

	/**
	 * Handles the results of an update of me.
	 */
	public void meChangesResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (serviceObjects.length == 1)
			{
				BulkUpdateContactsResult serviceObject = (BulkUpdateContactsResult) serviceObjects[0];

//				if (serviceObject.didErrorOccurUpdatingContact(0))
//				{
//					//#debug error
//					System.out.println("Received response to request " + requestId + ": error, try again next time");
//				}
//				else
//				{
					meChangesResultReceived(requestId, serviceObject.getDetailIDsForMe());
//				}
					
				meChangesSynced = 0;
				
				// done, check with server
				services.fireEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_SAB, null);
			}
			else
			{
				//#debug error
				System.out.println("Update me result contains " + serviceObjects.length + " changes, ignored.");
			}
		}
	}

	private void meChangesResultReceived(int requestId, long[] detailIds)
	{
		me.load(true);
		try
		{
			me.lock();
			me.resetCris(services.getStartOfSession(), requestId, detailIds);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Received response to request " + requestId + ": can't lock the profile, pretend we never received response." + e);
		}
		me.unload();
	}

	private void requestMeChanges()
	{
		if (running && services.isConnectionUp() && (meChangesSynced + CHANGES_SYNC_EXPIRY < System.currentTimeMillis()))
		{
			meChangesSynced = System.currentTimeMillis();
			
			long fromRevision = services.getSettings().getCurrentRevMe();

			Hashtable params = new Hashtable();
			params.put("fromrevision", new Long(fromRevision));

			meRequestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.MY_CHANGES, 
					null, params, ServerRequest.MEDIUM_PRIORITY, 40);

			//#debug info
			System.out.println("Request " + meRequestId + ": get me changes from " + fromRevision);
		}
		else
		{
			//#debug info
			System.out.println("Network is down or sync in progess... (" + meChangesSynced + ")");
		}
	}

	/**
	 * Handles me changes received from the server.
	 */
	public void meChangesReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (serviceObjects.length == 1)
			{
				ContactChanges c = (ContactChanges) serviceObjects[0];

				boolean newMe = false;

				if (me == null)
				{
					me = new MyProfile();
					//#if !polish.loadContactsOnDemand
					me.load(true);
					//#endif
					
					newMe = true;
				}
				me.fillFromServiceObject(c, false);

				//#debug info
				System.out.println("My profile updated to revision " + c.getCurrentServerRevision());

				services.getSettings().setCurrentRevisionMe(c.getCurrentServerRevision());

				services.getChatManager().meChangesReceived(newMe);
				
				meChangesSynced = 0;
				
				services.fireEvent(Event.Context.MODEL, Event.Model.MY_IDENTITIES_CHANGED_IN_SAB, null);
			}
			else
			{
				//#debug error
				System.out.println("My profile changes contains " + serviceObjects.length + " changes, ignored.");
			}
		}
	}

	private void sendContactChanges(boolean forced)
	{
		if (running && services.isConnectionUp() && (forced || (contactChangesSynced + CHANGES_SYNC_EXPIRY < System.currentTimeMillis())))
		{
			contactChangesSynced = System.currentTimeMillis();

			sendContactChanges();
		}
		else
		{
			//#debug info
			System.out.println("Network is down or sync in progess... (" + contactChangesSynced + ")");
		}
	}

	private void sendContactChanges()
	{
		if (fireContactChangesSendEvent) 
		{
			services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(services.getSettings().getChangeLogProgress()));
		}
		
		Object[] changeLog = services.getSettings().getChangeLog(MAX_NUMBER_OF_CONTACT_CHANGES_PER_REQUEST);
		long[] ids = (long[]) changeLog[0];
		int[] types = (int[]) changeLog[1];
		
		if (ids.length > 0)
		{
			long startOfSession = services.getStartOfSession();

			// get the contacts (exluding my profile)
			ContactProfile[] profiles = contacts.getContacts(ids, types);
			int[] profileSlots = new int[profiles.length];
			int profilesLen = 0;

			ServiceObject[] profileChanges = new ServiceObject[profiles.length];
			int profileChangesLen = 0;

			long[] deletionSabIds = new long[profiles.length];
			int deletionsLen = 0;
			int deletionsRequestId = 0;
			
			int[] groupChangeIndices = new int[profiles.length];
			int[] groupChangeRequestIds = new int[profiles.length];
			int groupChangesLen = 0;

			for (int i = 0; i < profiles.length; i++)
			{
				if (types[i] == Settings.CHANGELOG_ADD_OR_UPDATE)
				{
					if (profiles[i] != null)
					{
						profiles[i].load(true);
						try
						{
							profiles[i].lock();
	
							boolean changed = false;

							ContactChanges serviceObject = new ContactChanges();
							if (profiles[i].fill(serviceObject, startOfSession))
							{
								//#debug info
								System.out.println("Send changes in contact " + profiles[i].getFullName() + " (" + profiles[i].getCabId() + ":" + profiles[i].getSabId() + ") in slot " + profileChangesLen);
	
								profileSlots[profilesLen] = profileChangesLen;
								profileChanges[profileChangesLen++] = serviceObject;
								changed = true;
							}

							if (profiles[i].hasGroupChanges(startOfSession))
							{
								if (profiles[i].getSabId() <= 0) // new contact
								{
									// can't send the group changes yet
									services.getSettings().addToChangeLog(profiles[i].getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
								}
								else
								{
									groupChangeIndices[groupChangesLen++] = profilesLen;
									changed = true;
								}
							}
						
							if (changed)
							{
								profiles[profilesLen++] = profiles[i];
							}
							else
							{
								profiles[i].revert();
								profiles[i].unload();
	
								//#debug debug
								System.out.println("No contact changes to send.");

								// no changes to send
								services.getSettings().removeFromChangeLog(profiles[i].getCabId(), Settings.CHANGELOG_ADD_OR_UPDATE);
							}
						}
						catch (LockException e)
						{
							//#debug error
							System.out.println("Can't lock the profile, try next time" + e);
	
							profiles[i].unload();
						}
					}
					else
					{
						if ((me != null) && (me.getCabId() == ids[i]))
						{
							// ignore, this is my profile
						}
						else
						{
							services.getSettings().removeFromChangeLog(ids[i], types[i]);
						}
					}
				}
				else
				{
					deletionSabIds[deletionsLen++] = ids[i];
				}
			}

			if (profileChangesLen > 0)
			{
				sendContactChangesRequestId = services.getProtocol().sendRequest(ServerRequest.BULK_UPDATE, ServerRequest.CONTACTS,
					ArrayUtils.trimArray(profileChanges, profileChangesLen), null, ServerRequest.MEDIUM_PRIORITY, 2 * 60);

				//#debug info
				System.out.println("Request " + sendContactChangesRequestId + ": send contact changes");
			}
			
			if (groupChangesLen > 0)
			{
				for (int i = 0; i < groupChangesLen; i++)
				{
					int index = groupChangeIndices[i];
					
					if(profiles[index].isGroupAdded()){
	                	com.zyb.nowplus.data.protocol.types.Group groupAdded =
							new com.zyb.nowplus.data.protocol.types.Group(profiles[index].getAddedGroupIds(), new long[] {profiles[index].getSabId()});
	                	groupChangeRequestIds[i] = services.getProtocol().sendRequest(ServerRequest.ADD, ServerRequest.CONTACT_GROUP_RELATIONS,
								new ServiceObject[] {groupAdded}, null, ServerRequest.MEDIUM_PRIORITY);
	                }
					
					 //#debug info
					System.out.println("Request " + groupChangeRequestIds[i] + ": add groups");
					
					if(profiles[index].isGroupDeleted()){
	                	com.zyb.nowplus.data.protocol.types.Group group =
							new com.zyb.nowplus.data.protocol.types.Group(profiles[index].getGroupIds(), new long[] {profiles[index].getSabId()});
	                	groupChangeRequestIds[i] = services.getProtocol().sendRequest(ServerRequest.SET, ServerRequest.CONTACT_GROUP_RELATIONS,
								new ServiceObject[] {group}, null, ServerRequest.MEDIUM_PRIORITY);
	                }
					//#debug info
					System.out.println("Request " + groupChangeRequestIds[i] + ": set groups");
				}
			}
			else
			{
				contactChangesSynced = 0;
			
				// send next batch of changes
				services.fireEvent(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_CAB, null);
			}
			
			if (deletionsLen > 0)
			{
  				Vector contactIdList = new Vector();
				for (int j = 0; j < deletionsLen; j++)
				{
					contactIdList.addElement(new Long(deletionSabIds[j]));					
				}

				Hashtable params = new Hashtable();
				params.put("contactidlist", contactIdList);

				deletionsRequestId = services.getProtocol().sendRequest(ServerRequest.DELETE, ServerRequest.CONTACTS,
						null, params, ServerRequest.MEDIUM_PRIORITY);
				
				//#debug info
				System.out.println("Request " + deletionsRequestId + ": delete contacts");
			}
			
			int j = 0;
			for (int i = 0; i < profilesLen; i++)
			{
				if (profiles[i].getSabId() <= 0) // new contact
				{
					// set a temporary sabId, so we can find this profile back when the server confirms the change
					profiles[i].setSabId(-(sendContactChangesRequestId * MAX_NUMBER_OF_CONTACT_CHANGES_PER_REQUEST + profileSlots[i]));
				}

				int groupChangeRequestId = 0;
				if (i == groupChangeIndices[j])
				{
					groupChangeRequestId = groupChangeRequestIds[j];
					j++;
				}
				profiles[i].setCris(startOfSession, sendContactChangesRequestId, groupChangeRequestId);
				profiles[i].unload();
			}
		}
		else 
		{
			contactChangesSynced = 0;
			
			if (fireContactChangesSendEvent) 
			{
				// this may be the second 100% event 
				services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(100));
			}
			
			fireContactChangesSendEvent = false;
			
			// done, check with server
			services.fireEvent(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_SAB, null);			
		}
	}

	/**
	 * Handles the results of an update of contacts.
	 */
	public void contactChangesResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (serviceObjects.length == 1)
			{
				BulkUpdateContactsResult serviceObject = (BulkUpdateContactsResult) serviceObjects[0];

				int numberOfChanges = serviceObject.getContactIDs().length;
				for (int i = 0; i < numberOfChanges; i++)
				{
					if (serviceObject.didErrorOccurUpdatingContact(i))
					{
						//#debug error
						System.out.println("Result " + i + " in response to request " + requestId + ": error, try again next time");
					}
					else
					{
						//#debug info
						System.out.println("Change in slot " + i + " has been confirmed.");

						contactChangesResultReceived(requestId, i, serviceObject.getContactIDs()[i], serviceObject.getDetailIDsForContactID(i));
					}
				}

				contactChangesSynced = 0;
				
				// send next batch of changes
				services.fireEvent(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_CAB, null);
			}
			else
			{
				//#debug error
				System.out.println("Update contacts result contains " + serviceObjects.length + " changes, ignored.");
			}
		}
	}

	private void contactChangesResultReceived(int requestId, int i, long sabId, long[] detailIds)
	{
		ContactProfile addedProfile = contacts.getContactBySabId(-(requestId * MAX_NUMBER_OF_CONTACT_CHANGES_PER_REQUEST + i));
		
		ContactProfile profile = contacts.getContactBySabId(sabId);
		if (profile == null) 
		{
			if (addedProfile == null)
			{
				//#debug error
				System.out.println("Result " + i + " in response to request " + requestId + ": can't find new contact or contact with sabId=" + sabId);
			}
			else
			{
				contactChangesResultReceived(addedProfile, requestId, i, sabId, detailIds);
			}
		}
		else
		{
			if (addedProfile == null)
			{
				contactChangesResultReceived(profile, requestId, i, sabId, detailIds);
			}
			else
			{
				//#debug error
				System.out.println("Result " + i + " in response to request " + requestId + ": received a sabId for an existing contact, delete new contact");
				
				if (!profile.syncToNab() && addedProfile.syncToNab())
				{
					//#debug info
					System.out.println("Oops - profile in nab is going to be deleted because of deduplication.");
				}
				
				delete(addedProfile, true);
			}
		}
	}
	
	private void contactChangesResultReceived(ContactProfile profile, int requestId, int i, long sabId, long[] detailIds)
	{
		profile.load(true);
		try
		{
			profile.lock();

			profile.setSabId(sabId);
			profile.resetCris(services.getStartOfSession(), requestId, detailIds);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Result " + i + " in response to request " + requestId + ": can't lock the profile, pretend we never received response." + e);
		}
		profile.unload();
	}

	public void contactGroupChangesResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (serviceObjects.length == 1)
			{
				com.zyb.nowplus.data.protocol.types.Group serviceObject = (com.zyb.nowplus.data.protocol.types.Group) serviceObjects[0];
			
				int numberOfChanges = serviceObject.getContactIDs().length;
				for (int i = 0; i < numberOfChanges; i++)
				{
					ContactProfile profile = contacts.getContactBySabId(serviceObject.getContactIDs()[i]);
					if (profile != null)
					{
						profile.resetGroupCri(services.getStartOfSession(), requestId);
					}
				}
			}
			else
			{
				//#debug error
				System.out.println("Update groups in contacts result contains " + serviceObjects.length + " changes, ignored.");
			}
		}		
	}
	
	private void requestContactChanges()
	{
		if (running && services.isConnectionUp() && (contactChangesSynced + CHANGES_SYNC_EXPIRY < System.currentTimeMillis()))
		{
			contactChangesSynced = System.currentTimeMillis();

			contactChangesPage = services.getSettings().getNextContactPage();
			requestContactChanges(-1);
		}
		else
		{
			//#debug info
			System.out.println("Network is down or sync in progess... (" + contactChangesSynced + ")");
		}
	}

	private void requestContactChanges(long toRevision)
	{
		if (fireContactChangesReceivedEvent && (toRevision == -1)) 
		{
			services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, new Integer(0));
		}
		
		long fromRevision = services.getSettings().getCurrentRevContacts();

		Hashtable params = new Hashtable();
		params.put("pagenumber", new Integer(contactChangesPage));
		params.put("maxpagesize", new Long(MAX_NUMBER_OF_CONTACT_CHANGES_PER_RESPONSE));
		params.put("fromrevision", new Long(fromRevision));
		params.put("torevision", new Long(toRevision));
		params.put("getownchanges", Boolean.FALSE);

		receiveContactChangesRequestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.CONTACTS_CHANGES, 
				null, params, ServerRequest.MEDIUM_PRIORITY, 40);

		//#debug info
		System.out.println("Request " + receiveContactChangesRequestId + ": get contact changes from " + fromRevision + " to " + toRevision + " (page " + contactChangesPage + ")");
		
		//#mdebug debug
			if(fireContactChangesReceivedEvent)//display what is requesting
				services.fireEvent(Event.Context.SYNC, Event.Sync.UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_SEND,
						"Request " + receiveContactChangesRequestId + ": get contact changes from " + fromRevision + " to " + toRevision + " (page " + contactChangesPage + ")");
		//#enddebug
	}
	
	
	boolean isDuplicate(ContactProfile profile)
	{
		Vector cache = this.contacts.getCache();

		for (int index = 0; index < cache.size(); index++) {
			ContactProfile profileToCheck = (ContactProfile)cache.elementAt(index);

			if (profileToCheck.getCabId() != profile.getCabId()
				&& profileToCheck.getSabId() != profile.getSabId()) {
				if(profileToCheck.getChecksum() == profile.getChecksum()) {
					//#debug error
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! FOUND DUPLICATE : " + profileToCheck + "/" + profile);
					return true;  
				}
			}
		}

		return false;
	}

	/**
	 * Handles contact changes received from the server.
	 */
	public void contactChangesReceived(int requestId, ServiceObject[] serviceObjects)
	{
		int currentItemOnContactPage = services.getSettings().getNextItemOnContactPage();
		
		if (running)
		{
			if (serviceObjects.length == 0)
			{
				// no changes
				contactChangesSynced = 0;
				
				if (fireContactChangesReceivedEvent) 
				{
					services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, 
							new Integer(100));
				}
				
				fireContactChangesReceivedEvent = false;
			}
			else
			{
				int counter = 0;
				if (serviceObjects.length > THRESHOLD_INDIVIDUAL_EVENTS)
				{
					counter = THRESHOLD_INDIVIDUAL_EVENTS;
				}
				
				ContactChanges c = null;

				for (int i = currentItemOnContactPage; i < serviceObjects.length; i++)
				{
					c = (ContactChanges) serviceObjects[i];

					serviceObjects[i] = null;

					if (isDeleted(c.getContactId()))
					{
						//#debug error
						System.out.println("Received contact changes for deleted contact " + c.getContactId());
					}
					else
					{
						ContactProfile contact = contacts.getContactBySabId(c.getContactId());
						if (c.isContactDeleted())
						{
							if (contact != null)
							{
								//#debug debug
								System.out.println("Received delete for contact " + contact.getCabId() + ":" + c.getContactId());
								
								delete(contact, true);
							}
						}
						else
						{
							if (contact == null)
							{
								//#debug debug
								System.out.println("Received add for contact ?:" + c.getContactId());
								
								newContactsReceived = true;
	
								contact = new ContactProfile();
								//#if !polish.loadContactsOnDemand
								contact.load(true);
								//#endif
							}
							else
							{
								//#debug debug
								System.out.println("Received update for contact " + contact.getCabId() + ":" + c.getContactId());
							}
							contact.fillFromServiceObject(c, (counter > 0));
							
							// Check of this contact is empty or a duplicate.
							if (contact.isEmpty() || isDuplicate(contact)) {
								delete(contact, true);
								continue;
							}
							
							if (contact.syncToNab())
							{
								services.fireEvent(Event.Context.SYNC, Event.Sync.CAB_CHANGED, new Long(contact.getCabId()));
							}
							
							if (counter > 0)
							{
								if (--counter == 0)
								{
									services.fireEvent(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null);
									
									if (serviceObjects.length - i > THRESHOLD_INDIVIDUAL_EVENTS)
									{
										counter = THRESHOLD_INDIVIDUAL_EVENTS;
									}
								}
							}
						}
					}
				}
				

				currentItemOnContactPage++;
				services.getSettings().setNextItemOnContactPage(currentItemOnContactPage);
			
				contactChangesPage++;
				
				services.getSettings().setNextContactPage(contactChangesPage);
				currentItemOnContactPage = 0;
				services.getSettings().setNextItemOnContactPage(currentItemOnContactPage);
				
				if (fireContactChangesReceivedEvent) 
				{
					services.fireEvent(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, 
							new Integer(100 * contactChangesPage / c.getNumberOfPages()));
					
				//#if activate.progress.debugging
					//update text of percentage 
					services.fireEvent(Event.Context.SYNC, Event.Sync.UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_SEND,
							new Integer(100 * contactChangesPage / c.getNumberOfPages())+"%");
				//#endif
				}
				
				if (contactChangesPage < c.getNumberOfPages())
				{
					requestContactChanges(c.getServerRevisionAnchor());
					
				//#if activate.progress.debugging
					if (fireContactChangesReceivedEvent) 
						services.fireEvent(Event.Context.SYNC, Event.Sync.UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_RECEIVED,""+c.getNickname());
				//#endif
				}
				else
				{
					//#debug info
					System.out.println("Contacts updated to revision " + c.getServerRevisionAnchor());

					services.getSettings().setCurrentRevisionContacts(c.getServerRevisionAnchor());
					services.getSettings().setNextContactPage(0);
					services.getSettings().setNextItemOnContactPage(0);

					contactChangesSynced = 0;
					fireContactChangesReceivedEvent = false;

					if (newContactsReceived)
					{
						newContactsReceived = false;

						services.getChatManager().newContactsReceived();
						//#if !polish.remove_status_tab
						services.getActivityManager().newContactsReceived();
						//#endif
					}
				}
			}
		}
	}

	private void storeNewProfile(ManagedProfile profile)
	{
		if (running)
		{
			ManagedProfileRecord record = new ManagedProfileRecord();
			profile.fill(record);
			try
			{
				store.insert(record);
			}
			catch (StorageException e)
			{
				//#debug error
				System.out.println("Failed to insert profile " + profile.getCabId() + e);
			}
			record = null;
			
			if (store.isFillingUp())
			{
				// TODO: Check this is being used somewhere else.
				services.fireEvent(Event.Context.APP, Event.App.CONTACTS_FILLING_UP, null);
			}
		}
	}

	private void storeChangedProfile(ManagedProfile profile)
	{
		if (running)
		{
			ManagedProfileRecord record = new ManagedProfileRecord();
			profile.fill(record);
			try
			{
				store.update(record);
			}
			catch (StorageException e)
			{
				//#debug error
				System.out.println("Failed to update contact " + profile.getCabId() + e);
			}
			record = null;
		}
	}

	private void deleteProfile(ManagedProfile profile)
	{
		if (running)
		{
			services.getSettings().addToDeletedContacts(profile.getSabId());
			
			try
			{
				store.delete(profile.getCabId());
			}
			catch (StorageException e)
			{
				//#debug error
				System.out.println("Failed to delete profile " + profile.getCabId() + e);
			}
		}
	}
	
	public boolean isDeleted(long sabId)
	{
		return services.getSettings().inDeletedContacts(sabId);
	}

	public boolean isInteractingWithNab()
	{
		return (syncManager != null) && syncManager.isInteractingWithSource();
	}
	
	public boolean isInteractingWithSab()
	{
		return !((contactChangesSynced == 0) && services.getSettings().isChangeLogEmpty());
	}
	
	//#mdebug error
	public String toString()
	{
		return "ContactManager[me=" + me
			+ ",contacts=" + contacts
			+ ",store=" + store
			+ ",syncManager=" + syncManager
			+ ",running=" + running
			+ "]";
	}
	//#enddebug
}
