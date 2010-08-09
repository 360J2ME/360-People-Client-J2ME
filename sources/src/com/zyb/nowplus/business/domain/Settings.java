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

import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.data.storage.KeyValueStore;
import com.zyb.nowplus.data.storage.RMSKeyValueStore;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

import de.enough.polish.content.ContentLoader;

/**
 * Represents user and system settings. 
 */
public class Settings
{
	public static final String STORE = "settings";
	
	public static final String STORAGE_VERSION = "1.0.1.3832";
	
	public static final boolean WAIT_FOR_ACTIVATION_SMS =
	//#if polish.wait_for_activation_sms	
		true
	//#else
	//#= false
	//#endif
	;
	
	// TODO: replace by proper associations
	public static ContentLoader contentLoader;

	public static final int KEY_STORAGE_VERSION = 1;
	public static final int KEY_USER_NAME = 2;
	public static final int KEY_STAY_LOGGED_IN = 3;
	public static final int KEY_IMSI = 4;
	public static final int KEY_REAUTHENTICATE = 5;
	public static final int KEY_SN_ACCOUNT_ADDED = 6;
	public static final int KEY_CONTACTS_ORDER = 7;
	public static final int KEY_ROAMING_ALLOWED = 8;
	public static final int KEY_DATA_COUNTER = 9;
	public static final int KEY_EXTERNAL_NETWORKS = 10;
	public static final int KEY_GROUPS = 11;
	public static final int KEY_LATEST_CAB_ID = 12;
	public static final int KEY_CURRENT_REV_ME = 13;
	public static final int KEY_CURRENT_REV_CONTACTS = 14;
	public static final int KEY_CHANGELOG_IDS = 15;
	public static final int KEY_CHANGELOG_TYPES = 16;
	public static final int KEY_DELETED_SABIDS = 17;
	public static final int KEY_NEXT_CONTACT_PAGE = 18;
	public static final int KEY_NEXT_ITEM_ON_CONTENT_PAGE = 19;
	
	public static final int CHANGELOG_ADD_OR_UPDATE = 1;
	public static final int CHANGELOG_DELETE = 2;
	
	private final KeyValueStore store;
	
	private long startOfSession;
	private boolean upgraded;
	private boolean firstLogin;
	private String storageVersion;
	private String userName;
	private boolean stayLoggedIn;
	private boolean socialNetworkAccountAdded;
	private int contactsOrder;
	private boolean roamingAllowed;
	private boolean roamingAccepted;
	private boolean connectionUp;
	private int dataCounter;
	private long latestCabId;
	private long currentRevMe;
	private long currentRevContacts;
	private long[] changeLogIds;
	private int[] changeLogTypes;
	private int changeLogIndex;
	private int changeLogLen;
	private int changeLogCount;
	private long[] deletedSabIds;
	private int deletedSabIdsLen;
	private boolean reauthenticate;
	
	private int nextContactPage;
	private int nextItemOnContactPage;

	public void setNextContactPage(int page)
	{
		this.nextContactPage = page;
		store.setIntValue(KEY_NEXT_CONTACT_PAGE, page);		
	}
	
	public int getNextContactPage()
	{
		return nextContactPage;
	}
	
	public void setNextItemOnContactPage(int item)
	{
		this.nextItemOnContactPage = item;
		store.setIntValue(KEY_NEXT_ITEM_ON_CONTENT_PAGE, item);		
	}
	
	public int getNextItemOnContactPage()
	{
		return nextItemOnContactPage;
	}
	
	/**
	 * Constructs settings.
	 */
	public Settings()
	{
		this(new RMSKeyValueStore());
	}
	
	/**
	 * Constructs settings with the given dependencies.
	 */
	public Settings(KeyValueStore store)
	{
		//#debug debug
		System.out.println("Constructing settings");
		
		this.store = store;
		this.connectionUp = true; // assume the network is up
	}
	
	/**
	 * Initialises the settings.
	 */
	public synchronized void init()
	{
		upgraded = false;
		
		load();

		if (!HashUtil.equals(storageVersion, STORAGE_VERSION))
		{
			if (storageVersion == null)
			{
				// initial settings
				stayLoggedIn = true;
				contactsOrder = Order.TYPE_FIRST_LAST;
			}
			else
			{
				//#debug info
				System.out.println("Upgraded from " + storageVersion + " to " + STORAGE_VERSION);
				
				upgraded = true;
			}
			
			storageVersion = STORAGE_VERSION;
			store();
		}
	
		startOfSession = System.currentTimeMillis();
		
		firstLogin = (userName == null);
	}
	
	/**
	 * Gets the version of the application.
	 */
	public String getAppVersion()
	{
		String appVersion = "";
		// #if build.number:defined
		//#= appVersion = "${build.number}";
		// #endif
		return appVersion;
	}
	
	/**
	 * Gets the time in ms the current session started.
	 */
	public long getStartOfSession()
	{
		return startOfSession;
	}
	
	/**
	 * Indicates if a first login is required.
	 */
	public boolean firstLogin()
	{
		return firstLogin;
	}

	/**
	 * Indicates if the user should authenticate again
	 */
	public boolean reauthenticate()
	{
		//#debug debug
		System.out.println("reauthenticate:"+reauthenticate);
		
		return reauthenticate;
	}

	public void setReauthenticate(boolean reauthenticate) 
	{
		this.reauthenticate = reauthenticate;

		//#debug debug
		System.out.println("setting reauthenticate:"+reauthenticate);
		
		store.setBooleanValue(Settings.KEY_REAUTHENTICATE, reauthenticate);
	}
	
	/**
	 * Indicates if persisted data needs to be wiped because the format has changed.
	 */
	public boolean isUpgraded()
	{
		return upgraded;
	}
	
	public void setUserDetails(String userName) 
	{
		this.userName = userName;
		store.setStringValue(KEY_USER_NAME, userName);
	}

	public void setStayLoggedIn(boolean stayLoggedIn)
	{
		this.stayLoggedIn = stayLoggedIn;
		store.setBooleanValue(KEY_STAY_LOGGED_IN, stayLoggedIn);
	}
	
	public String getUserName() 
	{
		return userName;
	}
	
	public boolean stayLoggedIn()
	{
		return stayLoggedIn;
	}
		
	public void setSocialNetworkAccountAdded()
	{
		this.socialNetworkAccountAdded = true;
		store.setBooleanValue(KEY_SN_ACCOUNT_ADDED, socialNetworkAccountAdded);
	}
	
	public boolean hasAddedSocialNetworkAccount()
	{
		return socialNetworkAccountAdded;
	}
	
	public void setContactsOrder(int type)
	{
		this.contactsOrder = type;
		store.setIntValue(KEY_CONTACTS_ORDER, type);
	}
	
	public int getContactsOrder()
	{
		return contactsOrder;
	}
	
	public void setRoamingAllowed(boolean roamingAllowed)
	{
		this.roamingAllowed = roamingAllowed;
		store.setBooleanValue(KEY_ROAMING_ALLOWED, roamingAllowed);
	}
	
	public boolean isRoamingAllowed()
	{
		return roamingAllowed;
	}
	
	public void setRoamingAccepted()
	{
		roamingAccepted = true;
	}
	
	public boolean isRoamingAccepted()
	{
		return roamingAccepted;
	}
	
	public void setConnectionUp(boolean connectionUp)
	{
		this.connectionUp = connectionUp;
	}
	
	public boolean isConnectionUp()
	{
		return connectionUp;
	}
	
	public void resetDataCounter()
	{
		this.dataCounter = 0;
		store.setIntValue(KEY_DATA_COUNTER, dataCounter);
	}
	
	public void addDataCounter(int data)
	{
		this.dataCounter += data;
		store.setIntValue(KEY_DATA_COUNTER, dataCounter);
	}
	
	public int getDataCounter()
	{
		return dataCounter;
	}
	
	public void setExternalNetworks(ExternalNetwork[] networks)
	{
		store.setExternalNetworkArrayValue(KEY_EXTERNAL_NETWORKS, networks);
	}
	
	public ExternalNetwork[] getExternalNetworks()
	{
		return store.getExternalNetworkArrayValue(KEY_EXTERNAL_NETWORKS);
	}
	
	public void setGroups(Group[] groups)
	{
		store.setGroupArrayValue(KEY_GROUPS, groups);
	}
	
	public Group[] getGroups()
	{
		return store.getGroupArrayValue(KEY_GROUPS);
	}
	
	/**
	 * Generates the next unique id for the client address book.
	 */
	public synchronized long generateNextCabId()
	{
		store.setLongValue(KEY_LATEST_CAB_ID, ++latestCabId);
		return latestCabId;
	}
	
	public void setCurrentRevisionMe(long revision)
	{
		if (this.currentRevMe != revision)
		{
			this.currentRevMe = revision;
			store.setLongValue(KEY_CURRENT_REV_ME, currentRevMe);
		}
	}
	
	public long getCurrentRevMe()
	{
		return currentRevMe;
	}
	
	public void setCurrentRevisionContacts(long revision)
	{
		if (this.currentRevContacts != revision)
		{
			this.currentRevContacts = revision;
			store.setLongValue(KEY_CURRENT_REV_CONTACTS, currentRevContacts);
		}
	}
	
	public long getCurrentRevContacts()
	{
		return currentRevContacts;
	}
	
	/**
	 * Adds a profile to the change log.
	 */
	public synchronized void addToChangeLog(long id, int type)
	{
		addToChangeLog(id, type, true);
	}
	
	private void addToChangeLog(long id, int type, boolean save)
	{
		boolean found = false;
		for (int i = changeLogIndex; (i < changeLogLen) && !found; i++)
		{
			found = ((changeLogIds[i] == id) && (changeLogTypes[i] == type));
		}
		if (!found)
		{
			if (changeLogIds.length == changeLogLen)
			{
				changeLogIds = ArrayUtils.extendArray(changeLogIds);
				changeLogTypes = ArrayUtils.extendArray(changeLogTypes);
			}
			changeLogIds[changeLogLen] = id;
			changeLogTypes[changeLogLen] = type;
			changeLogLen++;
			
			if (save)
			{
				store.setLongArrayValue(KEY_CHANGELOG_IDS, changeLogIds, changeLogLen);
				store.setIntArrayValue(KEY_CHANGELOG_TYPES, changeLogTypes, changeLogLen);
			}
		}
	}
	
	/**
	 * Checks if there are any changes in the change log.
	 */
	public synchronized boolean isChangeLogEmpty()
	{
		return (changeLogIndex == changeLogLen);
	}
	
	/**
	 * Returns a progress indication.
	 * The percentage of changes in the changelog that is 
	 * processed. 
	 */
	public synchronized int getChangeLogProgress() 
	{
		int total = changeLogCount + changeLogLen - changeLogIndex;
		return (total == 0) ? 0 : 100 * changeLogCount / total;
	}
	
	/**
	 * Checks if cabId is in change log.
	 */
	public synchronized boolean inChangeLog(long id, int type)
	{
		boolean found = false;
		for (int i = 0; (i < changeLogLen) && !found; i++)
		{
			found = ((changeLogIds[i] == id) && (changeLogTypes[i] == type));
		}
		return found;
	}
	
	/**
	 * Gets the next 'number' elements of the change log.
	 */
	public synchronized Object[] getChangeLog(int number)
	{
		if (changeLogIndex + number > changeLogLen)
		{
			number = changeLogLen - changeLogIndex;
		}
		changeLogCount += number;
		
		long[] ids = new long[number];
		System.arraycopy(changeLogIds, changeLogIndex, ids, 0, number);
		int[] types = new int[number];
		System.arraycopy(changeLogTypes, changeLogIndex, types, 0, number);
		changeLogIndex += number;
		return new Object[] {ids, types};
	}
	
	/**
	 * Removes a profile from the changelog.
	 */
	public synchronized void removeFromChangeLog(long id, int type)
	{
		boolean found = false;
		for(int i = 0; (i < changeLogLen); i++)
		{
			if (found)
			{
				if (changeLogIndex == i)
				{
					changeLogIndex = i - 1;
				}
				changeLogIds[i - 1] = changeLogIds[i];
				changeLogTypes[i - 1] = changeLogTypes[i];
			}
			else
			{
				found = ((changeLogIds[i] == id) && (changeLogTypes[i] == type));
			}
		}
		if (found)
		{
			if (changeLogIndex == changeLogLen)
			{
				changeLogIndex--;
			}
			changeLogLen--;
			
			store.setLongArrayValue(KEY_CHANGELOG_IDS, changeLogIds, changeLogLen);
			store.setIntArrayValue(KEY_CHANGELOG_TYPES, changeLogTypes, changeLogLen);
		}
	}

	public void addToDeletedContacts(long sabId)
	{
		if (sabId > 0)
		{
			if (deletedSabIds.length == deletedSabIdsLen)
			{
				deletedSabIds = ArrayUtils.extendArray(deletedSabIds);
			}
			deletedSabIds[deletedSabIdsLen++] = sabId;	

			store.setLongArrayValue(KEY_DELETED_SABIDS, deletedSabIds, deletedSabIdsLen);
		}
	}
	
	public boolean inDeletedContacts(long sabId)
	{
		boolean found = false;
		for (int j = 0; !found && (j < deletedSabIdsLen); j++)
		{
			found = deletedSabIds[j] == sabId;
		}
		return found;
	}

	private void load()
	{
		changeLogIds = new long[16];
		changeLogTypes = new int[16];
		changeLogIndex = 0;
		changeLogLen = 0;
		changeLogCount = 0;

		deletedSabIds = new long[16];
		
		try
		{
			store.open(STORE, true);
			storageVersion = store.getStringValue(KEY_STORAGE_VERSION);
			userName = store.getStringValue(KEY_USER_NAME);
			stayLoggedIn = store.getBooleanValue(KEY_STAY_LOGGED_IN);
			store.getStringValue(KEY_IMSI);
			reauthenticate = store.getBooleanValue(KEY_REAUTHENTICATE);
			socialNetworkAccountAdded = store.getBooleanValue(KEY_SN_ACCOUNT_ADDED);
			contactsOrder = store.getIntValue(KEY_CONTACTS_ORDER);
			roamingAllowed = store.getBooleanValue(KEY_ROAMING_ALLOWED);
			dataCounter = store.getIntValue(KEY_DATA_COUNTER);
			latestCabId = store.getLongValue(KEY_LATEST_CAB_ID);
			currentRevMe = store.getLongValue(KEY_CURRENT_REV_ME);
			currentRevContacts = store.getLongValue(KEY_CURRENT_REV_CONTACTS);
			nextItemOnContactPage = store.getIntValue(KEY_NEXT_ITEM_ON_CONTENT_PAGE);
			nextContactPage = store.getIntValue(KEY_NEXT_CONTACT_PAGE);									
					
			long[] ids = store.getLongArrayValue(KEY_CHANGELOG_IDS);
			int[] types = store.getIntArrayValue(KEY_CHANGELOG_TYPES);
			if ((ids != null) && (types != null))
			{
				for (int i = 0; i < ids.length; i++)
				{
					addToChangeLog(ids[i], types[i], false);
				}
			}
		
			ids = store.getLongArrayValue(KEY_DELETED_SABIDS);
			if (ids != null)
			{
				for (int i = 0; i < ids.length; i++)
				{
					addToDeletedContacts(ids[i]);
				}
			}
			//#debug debug
			System.out.println("Loaded settings " + this);
		}
		catch (StorageException e)
		{
			upgraded = true;
			
			//#debug error
			System.out.println("Failed to load settings." + e);
		}
	}
	
	private void store()
	{
		//#debug debug
		System.out.println("Store settings " + this);

		store.setStringValue(KEY_STORAGE_VERSION, storageVersion);
		store.setStringValue(KEY_USER_NAME, userName);
		store.setBooleanValue(KEY_STAY_LOGGED_IN, stayLoggedIn);
		store.setStringValue(KEY_IMSI, null);
		store.setBooleanValue(KEY_REAUTHENTICATE, reauthenticate);
		store.setBooleanValue(KEY_SN_ACCOUNT_ADDED, socialNetworkAccountAdded);
		store.setIntValue(KEY_CONTACTS_ORDER, contactsOrder);
		store.setBooleanValue(KEY_ROAMING_ALLOWED, roamingAllowed);
		store.setIntValue(KEY_DATA_COUNTER, dataCounter);
		store.setLongValue(KEY_LATEST_CAB_ID, latestCabId);
		store.setLongValue(KEY_CURRENT_REV_ME, currentRevMe);
		store.setLongValue(KEY_CURRENT_REV_CONTACTS, currentRevContacts);
		store.setLongArrayValue(KEY_CHANGELOG_IDS, changeLogIds, changeLogLen);
		store.setIntArrayValue(KEY_CHANGELOG_TYPES, changeLogTypes, changeLogLen);
		store.setLongArrayValue(KEY_DELETED_SABIDS, deletedSabIds, deletedSabIdsLen);
	}
	
	public void flush()
	{
		store.close();
		
		//#debug info
		System.out.println("Settings flushed.");
	}
	
	//#mdebug error
	public String toString()
	{
		return "Settings[storageVersion=" + storageVersion
			+ ",userName=" + userName
			+ ",stayLoggedIn=" + stayLoggedIn
			+ ",socialNetworkAccountAdded=" + socialNetworkAccountAdded
			+ ",contactsOrder=" + contactsOrder
			+ ",roamingAllowed=" + roamingAllowed
			+ ",roamingAccepted=" + roamingAccepted
			+ ",dataCounter=" + dataCounter
			+ ",latestCabId=" + latestCabId
			+ ",currentRevMe=" + currentRevMe
			+ ",currentRevContacts=" + currentRevContacts
			+ ",changeLogIds=" + ArrayUtils.toString(changeLogIds)
			+ ",changeLogTypes=" + ArrayUtils.toString(changeLogTypes)
			+ ",reauthenticate=" + reauthenticate
			+ "]";
	}
	//#enddebug
}
