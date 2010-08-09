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
package com.zyb.nowplus.business.test;

import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.storage.test.MockDataStore;
import com.zyb.nowplus.data.storage.test.MockKeyValueStore;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.MockMIDlet;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class WebaccountsTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[4];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin1", "skype", "Skype", null);
		EXTERNAL_NETWORKS[1] = new ExternalNetwork("plugin1", "microsoft", "MSN", null);
		EXTERNAL_NETWORKS[2] = new ExternalNetwork("plugin2", "facebook.com", "Facebook", null);
		EXTERNAL_NETWORKS[3] = new ExternalNetwork(null, ExternalNetwork.VODAFONE_360, ExternalNetwork.VODAFONE_360_LABEL, null);
	}
	
	private static final Group[] GROUPS = new Group[3];
	static
	{
		GROUPS[0] = new Group(1, Group.TYPE_STANDARD, "Family");
		GROUPS[1] = new Group(2, Group.TYPE_CUSTOM, "My Group");
		GROUPS[2] = new Group(3, Group.TYPE_SOCIAL_NETWORK, "Facebook");
	}
	
	private MockMIDlet context;
	private MockSyncManager syncManager;
	private MockKeyValueStore settingsStore;
	private MockDataStore contactsStore;
	private MockSinkAndSource contentSinkAndSource;
	private MockCommunicationManager protocol;
	private MockEmailCommunicationManager emailProtocol;
	
	private Model model;

	public void setUp()
	{
		index = 0;
		
		EventDispatcher eventDispatcher = new RunnableEventDispatcher();
		
		syncManager = new MockSyncManager(eventDispatcher, 20);
		settingsStore = new MockKeyValueStore();		
		contactsStore = new MockDataStore();
		contentSinkAndSource = new MockSinkAndSource(20);
		protocol = new MockCommunicationManager(20);
		emailProtocol = new MockEmailCommunicationManager();
		
		context = new MockMIDlet(settingsStore, contactsStore, new MockDataStore(), new MockDataStore(), 
				syncManager, contentSinkAndSource, contentSinkAndSource, protocol, emailProtocol, eventDispatcher, 20);
	   
		model = context.getModel();
	    model.attach(this);
	}
	
	private void setUpCurrentInstallation(boolean facebookLoggedIn) throws Exception
	{
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, true);
		settingsStore.setStringValue(Settings.KEY_IMSI, "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, false);
		settingsStore.setIntValue(Settings.KEY_CONTACTS_ORDER, Order.TYPE_FIRST_LAST);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 1);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 0);

		ManagedProfileRecord[] records = new ManagedProfileRecord[1];
		
		records[0] = new ManagedProfileRecord();
		records[0].setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		records[0].setCabId(1);
		records[0].setSabId(70001);
		records[0].setUserId(80007);
		records[0].setNowPlusMember(ContactProfile.NOWPLUS_ME);
		records[0].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		records[0].setProfileImageURL("image/profile/80007");
		records[0].setNickname("michael.bloomberg");
		records[0].setFirstName("Michael");
		records[0].setMiddleNames("R.");
		records[0].setLastName("Bloomberg");
		records[0].setYearOfBirth(1942);
		records[0].setMonthOfBirth(2);
		records[0].setDayOfBirth(14);
		
		Identity imAccount = null;
		Identity snAccount = null;
		records[0].setIdentities(new Identity[] {
			Identity.createPhoneNumber(Identity.SUBTYPE_HOME, "+31000000001", false, 800021),
			Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "+31000000002", false, 800022),
			Identity.createEmail(Identity.SUBTYPE_HOME, "michael1@nyc.com", false, 800031),
			Identity.createEmail(Identity.SUBTYPE_WORK, "michael2@nyc.com", false, 800032),
			Identity.createImAccount(EXTERNAL_NETWORKS[3], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID),
			imAccount = Identity.createImAccount(EXTERNAL_NETWORKS[1], "contact.me@example.com", false, 0),
			snAccount = Identity.createSnAccount(EXTERNAL_NETWORKS[2], "contact.me", "http://www.facebook.com/contact.me", 0)
		});
		imAccount.setPresence(Channel.PRESENCE_ONLINE);
		if (facebookLoggedIn)
		{
			snAccount.setPresence(Channel.PRESENCE_ONLINE);
		}
		records[0].setUrl(Identity.createUrl("http://www1.ibm.com/"));
		records[0].setAddresses(new Address[] {
				Address.createAddress(Address.toType("home"), "Straat 1", null, "Plaats", "1234AB", "Regio", "Nederland", 800051),
				Address.createAddress(Address.toType("work"), "1 Street", null, "City", "NY 12345", "New York", "United States", 800052)
			});
		records[0].setGroups(new long[] {});
		records[0].setTitle("Mayor");
		records[0].setDepartment("Council");
		records[0].setOrganisation("New York");
		records[0].setNotes(new Note[] {new Note("Appointed in 2001.", -1)});
		records[0].setStatus("Is in charge");	
		records[0].setStatusSourceNetworkId("facebook");
			
		contactsStore.initialise(records);
	}	
	
	public void testAddSocialNetworksNotAvailable() throws Exception
	{		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// user logs in
		model.login("user", "password2", true);	
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		// receive networks
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("pluginid", "plugin1");
				ht.put("network", "skype");
				ht.put("name", "Skype");
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
			}
			protocol.mockItemsReceived(1, serviceObjects, ServiceObject.AVAILABLE_IDENTITY);
		}
		
		// receive groups
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(1));
				ht.put("name", "Family");
				ht.put("isreadonly", Boolean.FALSE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			protocol.mockItemsReceived(2, serviceObjects, ServiceObject.GROUPS);
		}
		
		// sync
		syncManager.mockSyncFinished();
		
		// start up finished
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.SYNC, Event.Sync.START_IMPORT, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTERS_UPDATED, null),
                new Event(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(0)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(100)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, new Integer(0))
		}, getEvents());
		
		Assert.assertFalse(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));
		
		// user exits
		model.exit(false);
	}
	
	public void testAddSocialNetworksAtFirstStartUpNo() throws Exception
	{		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// user logs in
		model.login("user", "password2", true);	
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		// receive networks
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("pluginid", "plugin1");
				ht.put("network", "skype");
				ht.put("name", "Skype");
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
			}
			protocol.mockItemsReceived(1, serviceObjects, ServiceObject.AVAILABLE_IDENTITY);
		}
		
		// receive groups
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(1));
				ht.put("name", "Family");
				ht.put("isreadonly", Boolean.FALSE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			protocol.mockItemsReceived(2, serviceObjects, ServiceObject.GROUPS);
		}
		
		// receive my profile
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Bloomberg;Michael;R.");
			detailList.addElement(ht);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70001));
		ht.put("userid", new Long(80007));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(33);
		
		protocol.mockItemsReceived(0, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
				
		// sync
		syncManager.mockSyncFinished();
		
		// event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
                new Event(Event.Context.SYNC, Event.Sync.START_IMPORT, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTERS_UPDATED, null),
                new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
                new Event(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED, null),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(0)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(100)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, new Integer(0))
		}, getEvents());
		
		index = 0;
		
		// user skips
		model.skipAddingSocialNetworkAccounts();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
				
		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED)); // we don't ask again
		
		// go into Me | Webaccounts
		model.finishAddingSocialNetworkAccounts();
		
		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));
		
		// user exits
		model.exit(false);
	}
	
	public void testAddSocialNetworksAtFirstStartUpYes() throws Exception
	{		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// user logs in
		model.login("user", "password2", true);	
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		// receive networks
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("pluginid", "plugin1");
				ht.put("network", "skype");
				ht.put("name", "Skype");
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
			}
			protocol.mockItemsReceived(1, serviceObjects, ServiceObject.AVAILABLE_IDENTITY);
		}
		
		// receive groups
		{
			ServiceObject[] serviceObjects = new ServiceObject[1];
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(1));
				ht.put("name", "Family");
				ht.put("isreadonly", Boolean.FALSE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			protocol.mockItemsReceived(2, serviceObjects, ServiceObject.GROUPS);
		}
		
		// receive my profile
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Bloomberg;Michael;R.");
			detailList.addElement(ht);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70001));
		ht.put("userid", new Long(80007));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(33);
		
		protocol.mockItemsReceived(0, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
				
		// sync
		syncManager.mockSyncFinished();
		
		// event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
                new Event(Event.Context.SYNC, Event.Sync.START_IMPORT, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTERS_UPDATED, null),
                new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
                new Event(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED, null),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(0)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_SEND, new Integer(100)),
				new Event(Event.Context.SYNC, Event.Sync.CONTACTS_RECEIVED, new Integer(0))				
		}, getEvents());
		
		index = 0;
		
		// user skips
		model.finishAddingSocialNetworkAccounts();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());

		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));
		
		// user exits
		model.exit(false);
	}	
	
	public void testAddSocialNetworksAtNextStartUpNo() throws Exception
	{		
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED, null)
		}, getEvents());
		
		index = 0;
		
		// user skips
		model.skipAddingSocialNetworkAccounts();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
				
		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED)); // we don't ask again
		
		// go into Me | Webaccounts
		model.finishAddingSocialNetworkAccounts();
		
		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));
		
		// user exits
		model.exit(false);
	}
	
	public void testAddSocialNetworksAtNextStartUpYes() throws Exception
	{		
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED, null)
		}, getEvents());
		
		index = 0;
		
		// user skips
		model.finishAddingSocialNetworkAccounts();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());

		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));
		
		// user exits
		model.exit(false);
	}
	
	public void testAddSocialNetworksDone() throws Exception
	{		
		setUpCurrentInstallation(false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// start up finished
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		Assert.assertTrue(settingsStore.getBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED));

		// user exits
		model.exit(false);
	}
	
// Restore the following tests when add/remove/login/logout use the Identities API proper.
	
//	public void testAddNetworkAccount() throws Exception
//	{
//		setUpCurrentInstallation(false);
//		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
//		
//		// application started
//		context.mockApplicationStarted();
//		
//		// no upgrade available
//		protocol.mockUpgradeInfoReceived(null);
//
//		protocol.resetEvents();
//		
//		// webaccounts are shown
//		MyProfile me = model.getMe();
//		me.load(true);
//		
//		ExternalNetwork[] externalNetworks = model.getAvailableSocialNetworks();
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertNull(me.getAccount(externalNetworks[0]));
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[1]));
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[2]));
//		
//		// add social network account
//		model.addSocialNetworkAccount(EXTERNAL_NETWORKS[0], "ik_ke", "password", true);
//		
//		Thread.sleep(100);
//
//		// validate identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(8, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.ADD_IDENTITY_RESULT);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_TIMED_OUT, null)
//		}, getEvents());
//		
//		// try again
//		model.addSocialNetworkAccount(EXTERNAL_NETWORKS[0], "ik_ke", "password", true);
//		
//		Thread.sleep(100);
//		
//		// validate identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// request causes temporary error
//		protocol.mockErrorReceived(9, ResponseListener.REQUEST_FAILED_TEMP_ERROR, ServiceObject.ADD_IDENTITY_RESULT);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null)
//		}, getEvents());
//		
//		// try again
//		model.addSocialNetworkAccount(EXTERNAL_NETWORKS[0], "ik_ke", "password", true);
//		
//		Thread.sleep(100);
//
//		// validate identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES)
//		}, protocol.getEvents());	
//
//		// request failed
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(false)};
//
//			protocol.mockItemsReceived(10, serviceObjects, ServiceObject.ADD_IDENTITY_RESULT);
//		}		
//
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null)
//		}, getEvents());
//
//		// try again
//		model.addSocialNetworkAccount(EXTERNAL_NETWORKS[0], "ik_ke", "password", true);
//		
//		Thread.sleep(100);
//
//		// validate identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.VALIDATE + ":" + ServerRequest.IDENTITIES)
//		}, protocol.getEvents());	
//
//		// request succeeded
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(true)};
//
//			protocol.mockItemsReceived(11, serviceObjects, ServiceObject.ADD_IDENTITY_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_SUCCEEDED, null)
//		}, getEvents());
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertTrue(externalNetworks[0].addingToMyProfile());
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[1]));
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[2]));
//		
//		model.exit(false);		
//	}	
	
//	public void testLoginNetworkAccount() throws Exception
//	{
//		setUpCurrentInstallation(false);
//		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
//		
//		// application started
//		context.mockApplicationStarted();
//		
//		// no upgrade available
//		protocol.mockUpgradeInfoReceived(null);
//
//		protocol.resetEvents();
//		
//		// webaccounts are shown
//		MyProfile me = model.getMe();
//		me.load(true);
//		
//		ExternalNetwork[] externalNetworks = model.getAvailableSocialNetworks();
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertNull(me.getAccount(externalNetworks[0]));
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[1]));
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		
//		Identity webaccount = me.getAccount(externalNetworks[2]);
//		Assert.assertNotNull(webaccount);
//		Assert.assertFalse(webaccount.isOnline());
//		
//		// login social network
//		model.loginSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(8, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null)
//		}, getEvents());
//
//		Assert.assertFalse(webaccount.isOnline());
//	
//		// try again
//		model.loginSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msn");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(9, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// login requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable")
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(10, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null)
//		}, getEvents());
//
//		Assert.assertFalse(webaccount.isOnline());
//		
//		// try again
//		model.loginSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msn");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(11, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// login requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable")
//		}, protocol.getEvents());	
//		
//		// request causes temporary error
//		protocol.mockErrorReceived(12, ResponseListener.REQUEST_FAILED_TEMP_ERROR, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null)
//		}, getEvents());
//
//		Assert.assertFalse(webaccount.isOnline());
//		
//		// try again
//		model.loginSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msn");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(13, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// login requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable")
//		}, protocol.getEvents());	
//		
//		// request failed
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(false)};
//
//			protocol.mockItemsReceived(14, serviceObjects, ServiceObject.SET_IDENTITY_STATUS_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null)
//		}, getEvents());
//
//		Assert.assertFalse(webaccount.isOnline());
//		
//		// try again
//		model.loginSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(15, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// login requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:enable")
//		}, protocol.getEvents());	
//		
//		// request succeeded
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(true)};
//
//			protocol.mockItemsReceived(16, serviceObjects, ServiceObject.SET_IDENTITY_STATUS_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_SUCCEEDED, null)
//		}, getEvents());
//		
//		Assert.assertTrue(webaccount.isOnline());
//		
//		model.exit(false);
//	}
	
//	public void testLogoutNetworkAccount() throws Exception
//	{
//		setUpCurrentInstallation(true);
//		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
//		
//		// application started
//		context.mockApplicationStarted();
//		
//		// no upgrade available
//		protocol.mockUpgradeInfoReceived(null);
//
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Vector caps = new Vector();
//				{
//					Hashtable ht = new Hashtable();
//					ht.put("capabilityid", "chat");
//					caps.addElement(ht);
//				}
//				
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				ht.put("identitycapabilitylist", caps);
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(6, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		protocol.resetEvents();
//		
//		// webaccounts are shown
//		MyProfile me = model.getMe();
//		me.load(true);
//		
//		ExternalNetwork[] externalNetworks = model.getAvailableSocialNetworks();
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertNull(me.getAccount(externalNetworks[0]));
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[2]));
//		
//		Identity webaccount = me.getAccount(externalNetworks[2]);
//		Assert.assertNotNull(webaccount);
//		Assert.assertTrue("Webaccount is not online", webaccount.isOnline());
//		
//		// logout social network
//		model.logoutSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(8, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null)
//		}, getEvents());
//
//		Assert.assertTrue("Webaccount is not online", webaccount.isOnline());
//	
//		// try again
//		model.logoutSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(9, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// logout requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable")
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(10, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null)
//		}, getEvents());
//
//		Assert.assertTrue("Webaccount is not online", webaccount.isOnline());
//		
//		// try again
//		model.logoutSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(11, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// logout requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable")
//		}, protocol.getEvents());	
//		
//		// request causes temporary error
//		protocol.mockErrorReceived(12, ResponseListener.REQUEST_FAILED_TEMP_ERROR, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null)
//		}, getEvents());
//
//		Assert.assertTrue("Webaccount is not online 3", webaccount.isOnline());		
//		
//		// try again
//		model.logoutSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(13, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// logout requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable")
//		}, protocol.getEvents());	
//		
//		// request failed
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(false)};
//
//			protocol.mockItemsReceived(14, serviceObjects, ServiceObject.SET_IDENTITY_STATUS_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null)
//		}, getEvents());
//
//		Assert.assertTrue("Webaccount is not online 4", webaccount.isOnline());
//		
//		// try again
//		model.logoutSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(15, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// logout requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.SET + ":" + ServerRequest.IDENTITIES + ":plugin2:facebook.com:0002:disable")
//		}, protocol.getEvents());	
//		
//		// request succeeded
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(true)};
//
//			protocol.mockItemsReceived(16, serviceObjects, ServiceObject.SET_IDENTITY_STATUS_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_SUCCEEDED, null)
//		}, getEvents());
//		
//		Assert.assertFalse("Webaccount is online", webaccount.isOnline());
//		
//		model.exit(false);
//	}	
	
//	public void testRemoveNetworkAccount() throws Exception 
//	{
//		setUpCurrentInstallation(false);
//		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
//		
//		// application started
//		context.mockApplicationStarted();
//		
//		// no upgrade available
//		protocol.mockUpgradeInfoReceived(null);
//
//		protocol.resetEvents();
//		
//		// webaccounts are shown
//		MyProfile me = model.getMe();
//		me.load(true);
//		
//		ExternalNetwork[] externalNetworks = model.getAvailableSocialNetworks();
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertNull(me.getAccount(externalNetworks[0]));
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[2]));
//
//		Identity webaccount = me.getAccount(externalNetworks[1]);
//		Assert.assertNotNull(webaccount);
//		
//		// remove social network account
//		model.removeSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(8, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null)
//		}, getEvents());
//	
//		// try again
//		model.removeSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(9, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// remove requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null")
//		}, protocol.getEvents());	
//		
//		// request times out
//		protocol.mockErrorReceived(10, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null)
//		}, getEvents());
//
//		// try again
//		model.removeSocialNetworkAccount(webaccount);
//		
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());	
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(11, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// remove requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null")
//		}, protocol.getEvents());	
//		
//		// request causes temporary error
//		protocol.mockErrorReceived(12, ResponseListener.REQUEST_FAILED_TEMP_ERROR, ServiceObject.MY_IDENTITY);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null)
//		}, getEvents());
//		
//		// try again
//		model.removeSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(13, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// remove requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null")
//		}, protocol.getEvents());
//		
//		// request failed
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(false)};
//
//			protocol.mockItemsReceived(14, serviceObjects, ServiceObject.DELETE_IDENTITY_RESULT);
//			
//			Thread.sleep(100);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null)
//		}, getEvents());
//
//		// try again
//		model.removeSocialNetworkAccount(webaccount);
//
//		Thread.sleep(100);
//		
//		// my identities requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
//		}, protocol.getEvents());
//		
//		// receive my identities
//		{
//			ServiceObject[] serviceObjects = new ServiceObject[2];
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin1");
//				ht.put("network", "msnmessenger");
//				ht.put("identityid", "0001");
//				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			{
//				Hashtable ht = new Hashtable();
//				ht.put("pluginid", "plugin2");
//				ht.put("network", "facebook");
//				ht.put("identityid", "0002");
//				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
//			}
//			protocol.mockItemsReceived(15, serviceObjects, ServiceObject.MY_IDENTITY);
//		}
//		
//		// remove requested
//		Assert.assertEquals("Protocol", new Event[]	{
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null"),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.DELETE + ":" + ServerRequest.IDENTITIES + ":plugin1:microsoft:0001:null")
//		}, protocol.getEvents());
//		
//		// request succeeded
//		{
//			ServiceObject[] serviceObjects = {new com.zyb.nowplus.data.protocol.types.Identity(true)};
//
//			protocol.mockItemsReceived(16, serviceObjects, ServiceObject.DELETE_IDENTITY_RESULT);
//		}		
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
//				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_SUCCEEDED, null)
//		}, getEvents());
//		
//		Assert.assertEquals(3, externalNetworks.length);
//		Assert.assertEquals("skype", externalNetworks[0].getNetworkId());
//		Assert.assertNull(me.getAccount(externalNetworks[0]));
//		Assert.assertEquals("microsoft", externalNetworks[1].getNetworkId());
//		Assert.assertTrue(externalNetworks[1].removingFromMyProfile());
//		Assert.assertEquals("facebook.com", externalNetworks[2].getNetworkId());
//		Assert.assertNotNull(me.getAccount(externalNetworks[2]));
//		
//		model.exit(false);		
//	}

	public void tearDown()
	{		
		model.detach(this);
		
		settingsStore = null;
		contactsStore = null;
		syncManager = null;
		contentSinkAndSource = null;
		protocol = null;
		emailProtocol = null;
		
		context = null;
		model = null;
	}
	
	private Event[] events = new Event[20];
	private int index;
	
	public byte getContext() 
	{
		return 0;
	}

	public void handleEvent(byte context, int id, Object data)
	{
		if (index < events.length)
		{
			events[index++] = new Event(context, id, data);
		}
		else
		{
			Assert.fail("Received more than " + events.length + " events");
		}
	}
	
	public Event[] getEvents()
	{
		return Event.trimArray(events, index);
	}	
}
