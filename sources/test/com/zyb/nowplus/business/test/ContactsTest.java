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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.ServiceBroker;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.business.domain.filters.NameFilter;
import com.zyb.nowplus.business.domain.filters.NullFilter;
import com.zyb.nowplus.business.domain.orders.FirstLastOrder;
import com.zyb.nowplus.business.domain.orders.LastFirstOrder;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.storage.test.MockDataStore;
import com.zyb.nowplus.data.storage.test.MockKeyValueStore;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.MockMIDlet;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.ArrayUtils;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class ContactsTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[2];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin2", "facebook.com", "Facebook", null);
		EXTERNAL_NETWORKS[1] = new ExternalNetwork(null, ExternalNetwork.VODAFONE_360, ExternalNetwork.VODAFONE_360_LABEL, null);
	}
	
	private static final Group[] GROUPS = new Group[4];
	static
	{
		GROUPS[0] = new Group(1, Group.TYPE_STANDARD, "Family");
		GROUPS[1] = new Group(2, Group.TYPE_CUSTOM, "My Group");
		GROUPS[2] = new Group(3, Group.TYPE_SOCIAL_NETWORK, "Facebook");
		GROUPS[3] = new Group(4, Group.TYPE_CUSTOM, "Double Filtering Group");
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

	private void setUpAndStart() throws Exception
	{	
		// set up settings
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, true);
		settingsStore.setStringValue(Settings.KEY_IMSI, "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		settingsStore.setIntValue(Settings.KEY_CONTACTS_ORDER, Order.TYPE_FIRST_LAST);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 9);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 44);

		// set up contacts
		ManagedProfileRecord[] records = new ManagedProfileRecord[9];
		
		records[0] = new ManagedProfileRecord();
		records[0].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[0].setCabId(1);
		records[0].setSabId(70001);
		records[0].setUserId(0);
		records[0].setNowPlusMember(ContactProfile.NOWPLUS_CONTACT);
		records[0].setNowPlusPresence(0);
		records[0].setFirstName("anders");
		records[0].setMiddleNames("");
		records[0].setLastName("Bjornsen");
		records[0].setGroups(null);
		records[0].setIdentities(null);
		
		records[1] = new ManagedProfileRecord();
		records[1].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[1].setCabId(2);
		records[1].setSabId(70002);
		records[1].setUserId(80002);
		records[1].setNowPlusMember(ContactProfile.NOWPLUS_MEMBER);
		records[1].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		records[1].setFirstName("Bjorn");
		records[1].setMiddleNames("");
		records[1].setLastName("Carlsen");
		records[1].setGroups(null);
		records[1].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});
		
		records[2] = new ManagedProfileRecord();
		records[2].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[2].setCabId(3);
		records[2].setSabId(70003);
		records[2].setUserId(80003);
		records[2].setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
		records[2].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		records[2].setFirstName("Carl");
		records[2].setMiddleNames("");
		records[2].setLastName("andersen");
		records[2].setGroups(null);
		records[2].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});
		
		records[3] = new ManagedProfileRecord();
		records[3].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[3].setSyncToNab(true);
		records[3].setCabId(4);
		records[3].setSabId(70004);
		records[3].setUserId(80004);
		records[3].setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
		records[3].setNowPlusPresence(Channel.PRESENCE_OFFLINE);
		records[3].setFirstName("Anders");
		records[3].setMiddleNames("");
		records[3].setLastName("Carlsen");
		records[3].setGroups(new long[] {4});
		records[3].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});;
		
		records[4] = new ManagedProfileRecord();
		records[4].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[4].setSyncToNab(true);
		records[4].setCabId(5);
		records[4].setSabId(70005);
		records[4].setUserId(80005);
		records[4].setNowPlusMember(ContactProfile.NOWPLUS_MEMBER);
		records[4].setNowPlusPresence(Channel.PRESENCE_INVISIBLE);
		records[4].setFirstName("bjorn");
		records[4].setMiddleNames("");
		records[4].setLastName("Andersen");
		records[4].setGroups(new long[] {1, 4});
		records[4].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});
		
		records[5] = new ManagedProfileRecord();
		records[5].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[5].setCabId(6);
		records[5].setSabId(70006);
		records[5].setUserId(80006);
		records[5].setNowPlusMember(ContactProfile.NOWPLUS_MEMBER);
		records[5].setNowPlusPresence(Channel.PRESENCE_OFFLINE);
		records[5].setFirstName("Carl");
		records[5].setMiddleNames("");
		records[5].setLastName("Bjornsen");
		records[5].setGroups(new long[] {1, 2});
		records[5].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});
		
		records[6] = new ManagedProfileRecord();
		records[6].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[6].setCabId(7);
		records[6].setSabId(70007);
		records[6].setUserId(80007);
		records[6].setNowPlusMember(ContactProfile.NOWPLUS_MEMBER);
		records[6].setNowPlusPresence(Channel.PRESENCE_OFFLINE);
		records[6].setFirstName("Anders");
		records[6].setMiddleNames("");
		records[6].setLastName("andersen");
		records[6].setGroups(new long[] {2, 3});
		records[6].setIdentities(new Identity[] {
				Identity.createImAccount(EXTERNAL_NETWORKS[1], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID),
				Identity.createSnAccount(EXTERNAL_NETWORKS[0], "anders.andersen", "http://www.facebook.com/anders.andersen", 0)	
		});
		
		records[7] = new ManagedProfileRecord();
		records[7].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[7].setCabId(8);
		records[7].setSabId(70008);
		records[7].setUserId(0);
		records[7].setNowPlusMember(ContactProfile.NOWPLUS_CONTACT);
		records[7].setNowPlusPresence(0);
		records[7].setFirstName("Bjorn");
		records[7].setMiddleNames("");
		records[7].setLastName("Bjornsen");
		records[7].setGroups(new long[] {3, 4});
		records[7].setIdentities(new Identity[] {
				Identity.createSnAccount(EXTERNAL_NETWORKS[0], "bjorn.bjornsen", "http://www.facebook.com/bjorn.bjornsen", 0)	
		});
		
		records[8] = new ManagedProfileRecord();
		records[8].setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		records[8].setCabId(9);
		records[8].setSabId(70009);
		records[8].setUserId(80009);
		records[8].setNowPlusMember(ContactProfile.NOWPLUS_ME);
		records[8].setNowPlusPresence(0);
		records[8].setFirstName("Me");
		records[8].setMiddleNames("");
		records[8].setLastName("");	
		
		contactsStore.initialise(records);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
	}
	
	public void testWriteReadGroups() throws Exception
	{
		setUpAndStart();
		
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArrayUtils.serializeArray(null, new DataOutputStream(baos));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			Group[] readGroups = Group.deserializeGroupArray(new DataInputStream(bais));
			
			Assert.assertNull(readGroups);
		}
		
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArrayUtils.serializeArray(GROUPS, new DataOutputStream(baos));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			Group[] readGroups = Group.deserializeGroupArray(new DataInputStream(bais));
			
			Assert.assertEquals(GROUPS, readGroups);
		}
		
		model.exit(false);
	}
	
	public void testWriteReadManagedProfileRecord() throws Exception
	{
		setUpAndStart();

		ManagedProfileRecord record = new ManagedProfileRecord();
		record.setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		record.setSyncToNab(true);
		record.setSabId(70001);
		record.setCabId(1);
		record.setUserId(80001);
		record.setNowPlusMember(ContactProfile.NOWPLUS_MEMBER);
		record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
		record.setProfileImageURL("image/profile/80001");
		record.setNickname("first.last");
		record.setNicknameCri(-1);
		record.setFirstName("first");
		record.setMiddleNames("middles");
		record.setLastName("last");
		record.setNameCri(-2);
		record.setYearOfBirth(1933);
		record.setMonthOfBirth(3);
		record.setDayOfBirth(3);
		record.setDateOfBirthCri(-3);
		record.setGroups(new long[] {1, 2, 3});
		record.setGroupsCri(-4);
		record.setTitle("VP");
		record.setTitleCri(-5);
		record.setDepartment("Cupboard");
		record.setOrganisation("Cups-r-us");
		record.setOrganisationCri(-6);
		record.setNotes(new Note[] {new Note("Aap", -1)});
		record.setStatus("Brushing");
		record.setStatusSourceNetworkId("nowplus");
		record.setStatusCri(-7);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.write(new DataOutputStream(baos));
	
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ManagedProfileRecord readRecord = new ManagedProfileRecord();
		readRecord.read(new DataInputStream(bais));
		
		Assert.assertEquals(record, readRecord);
		
		record.release();
		
		Assert.assertEquals(ManagedProfileRecord.TYPE_CONTACT_PROFILE, record.getType());
		Assert.assertEquals(true, record.syncToNab());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(80001, record.getUserId());
		Assert.assertEquals(ContactProfile.NOWPLUS_MEMBER, record.getNowPlusMember());
		Assert.assertEquals(Channel.PRESENCE_ONLINE, record.getNowPlusPresence());
		Assert.assertNull(record.getProfileImageURL());
		Assert.assertNull(record.getNickname());
		Assert.assertEquals(0, record.getNicknameCri());
		Assert.assertEquals("first", record.getFirstName());
		Assert.assertEquals("middles", record.getMiddleNames());
		Assert.assertEquals("last", record.getLastName());
		Assert.assertEquals(-2, record.getNameCri());
		Assert.assertEquals(0, record.getYearOfBirth());
		Assert.assertEquals(0, record.getMonthOfBirth());
		Assert.assertEquals(0, record.getDayOfBirth());
		Assert.assertEquals(0, record.getDateOfBirthCri());
		Assert.assertEquals(new long[] {1, 2, 3}, record.getGroups());
		Assert.assertEquals(-4, record.getGroupsCri());
		Assert.assertNull(record.getTitle());
		Assert.assertEquals(0, record.getTitleCri());
		Assert.assertNull(record.getDepartment());
		Assert.assertNull(record.getOrganisation());
		Assert.assertEquals(0, record.getOrganisationCri());
//		Assert.assertNull(record.getNotes());
//		Assert.assertNull(record.getStatus());
//		Assert.assertNull(record.getStatusSourceNetworkId());
//		Assert.assertEquals(0, record.getStatusCri());
	}
	
	public void testSelection() throws Exception
	{
		setUpAndStart();

		ListSelection selection = null;
		Object[] contacts = null;

		// no selection
		selection = model.getContacts(null, 5);
		Assert.assertEquals(0, selection.getStart());
		Assert.assertEquals(4, selection.getEnd());		
		Assert.assertEquals(8, selection.getTotal());
		
		contacts = selection.getEntries();

		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("anders Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[3]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[4]).getFullName());
		
		// selection is cut at the beginning
		selection = model.getContacts(model.getContact(1), 5);
		Assert.assertEquals(0, selection.getStart());
		Assert.assertEquals(3, selection.getEnd());		
		Assert.assertEquals(8, selection.getTotal());
		
		contacts = selection.getEntries();
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("anders Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[3]).getFullName());
		
		// selection is complete
		selection = model.getContacts(model.getContact(8), 5);
		Assert.assertEquals(2, selection.getStart());
		Assert.assertEquals(6, selection.getEnd());		
		Assert.assertEquals(8, selection.getTotal());		

		contacts = selection.getEntries();
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("Bjorn Carlsen ", ((ContactProfile) contacts[3]).getFullName());
		Assert.assertEquals("Carl andersen ", ((ContactProfile) contacts[4]).getFullName());
		
		// selection is cut at the end
		selection = model.getContacts(model.getContact(3), 5);
		Assert.assertEquals(4, selection.getStart());
		Assert.assertEquals(7, selection.getEnd());		
		Assert.assertEquals(8, selection.getTotal());		

		contacts = selection.getEntries();
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Bjorn Carlsen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Carl andersen ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[3]).getFullName());
		
		model.exit(false);
	}		
	
	public void testFilters() throws Exception
	{
		setUpAndStart();
		
		// filter bar is shown		
		Filter[] filters = model.getContactFilters();
		
		Assert.assertEquals(7, filters.length);
		
		Object[] contacts = null;
		
		// all
		Assert.assertEquals("All", filters[0].getName());		

		model.setContactsFilter(filters[0]);
		Thread.sleep(100);
		
		try
		{
			filters[0].accepts(null);
			Assert.fail("NullFilter.accepts() should throw exception");
		}
		catch (RuntimeException e)
		{
			Assert.assertEquals("Don't use this method!", e.getMessage());
		}

		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(8, contacts.length);
		
		// online
		Assert.assertEquals("Online", filters[1].getName());
			
		model.setContactsFilter(filters[1]);
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());
		
		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Bjorn Carlsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl andersen ", ((ContactProfile) contacts[1]).getFullName());
		
//		// connected
//		Assert.assertEquals("Connected", filters[2].getName());
//
//		model.setContactsFilter(filters[2]);
//		Thread.sleep(100);
//		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.START, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.APP, Event.App.READY, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
//				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null)
//		}, getEvents());
//		
//		contacts = model.getContacts(null, 20).getEntries();
//		
//		Assert.assertEquals(2, contacts.length);
//		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[0]).getFullName());
//		Assert.assertEquals("Carl andersen ", ((ContactProfile) contacts[1]).getFullName());
		
		// standard group
		Assert.assertEquals("Family", filters[3].getName());
		
		model.setContactsFilter(filters[3]);
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());

		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		// native phonebook
		Assert.assertEquals("Phonebook", filters[2].getName());

		model.setContactsFilter(filters[2]);
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());

		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[1]).getFullName());
		
		// custom group
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());
	
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// social network group filter
		Assert.assertEquals("Facebook", filters[6].getName());
		
		model.setContactsFilter(filters[6]);
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());

		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// no change in filter: no refresh
		model.setContactsFilter(filters[6]);
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(9)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.FILTER_CHANGED, null)
		}, getEvents());		
		
		model.exit(false);
	}

	public void testTextFilter() throws Exception
	{
		setUpAndStart();

		Object[] contacts = null;
		
		NameFilter filter = new NameFilter("Anders");
		NullFilter filter0 = new NullFilter();
		
		Assert.assertEquals("", filter.getName());
		Assert.assertFalse(filter.equals(filter0));
		Assert.assertFalse(filter0.equals(filter));
		
		// search for contacts starting with 'A'
		model.setTextFilter("A");
		
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(3, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("anders Bjornsen ", ((ContactProfile) contacts[1]).getFullName());		
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[2]).getFullName());
		
		// search for contacts starting with 'b'
		model.setTextFilter("b");
		
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(3, contacts.length);
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[1]).getFullName());		
		Assert.assertEquals("Bjorn Carlsen ", ((ContactProfile) contacts[2]).getFullName());
		
		model.exit(false);
	}
	
	public void testTextSearch() throws Exception
	{
		setUpAndStart();
		
		// go to contacts starting with 'B'
		Assert.assertEquals("First, Last", model.getContactsOrder().getName());
		Assert.assertEquals("bjorn Andersen ", model.getFirstContact("B").getFullName());
		
		// swap first/last order
		model.setContactsOrder(new LastFirstOrder());
		
		// go to contacts starting with 'carlsen'
		Assert.assertEquals("Last, First", model.getContactsOrder().getName());
		Assert.assertEquals("Carlsen Anders ", model.getFirstContact("carlsen").getFullName());
		
		// swap first/last order
		model.setContactsOrder(new FirstLastOrder());
		
		// go to contacts starting after 'Anderz'
		Assert.assertEquals("First, Last", model.getContactsOrder().getName());
		Assert.assertEquals("bjorn Andersen ", model.getFirstContact("Anderz").getFullName());
		
		model.exit(false);
	}
	
	public void testOrders() throws Exception
	{
		setUpAndStart();

		Object[] contacts = null;
		
		// initial order
		Assert.assertEquals("First, Last", model.getContactsOrder().getName());
		Assert.assertEquals("Settings", Order.TYPE_FIRST_LAST, settingsStore.getIntValue(Settings.KEY_CONTACTS_ORDER));
		
		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("anders Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[3]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[4]).getFullName());
		Assert.assertEquals("Bjorn Carlsen ", ((ContactProfile) contacts[5]).getFullName());
		Assert.assertEquals("Carl andersen ", ((ContactProfile) contacts[6]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[7]).getFullName());
		
		// change order
		model.setContactsOrder(new LastFirstOrder());
		
		Assert.assertEquals("Last, First", model.getContactsOrder().getName());
		Assert.assertEquals("Settings", Order.TYPE_LAST_FIRST, settingsStore.getIntValue(Settings.KEY_CONTACTS_ORDER));
		
		contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals("andersen Anders ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Andersen bjorn ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("andersen Carl ", ((ContactProfile) contacts[2]).getFullName());
		Assert.assertEquals("Bjornsen anders ", ((ContactProfile) contacts[3]).getFullName());
		Assert.assertEquals("Bjornsen Bjorn ", ((ContactProfile) contacts[4]).getFullName());
		Assert.assertEquals("Bjornsen Carl ", ((ContactProfile) contacts[5]).getFullName());
		Assert.assertEquals("Carlsen Anders ", ((ContactProfile) contacts[6]).getFullName());
		Assert.assertEquals("Carlsen Bjorn ", ((ContactProfile) contacts[7]).getFullName());
		
		// change order back
		model.setContactsOrder(new FirstLastOrder());
		
		Assert.assertEquals("First, Last", model.getContactsOrder().getName());
		Assert.assertEquals("Settings", Order.TYPE_FIRST_LAST, settingsStore.getIntValue(Settings.KEY_CONTACTS_ORDER));
		
		model.exit(false);
	}
	
	public void testDoubleFiltering() throws Exception
	{
		setUpAndStart();

		Object[] contacts = null;
		
		// secondary filter accepts none
		model.setContactsFilter(model.getContactFilters()[5]);
		
		model.setTextFilter("Carl");
		
		contacts = model.getContacts(null, 20).getEntries();
		Assert.assertEquals(0, contacts.length);
	
		// secondary filter accepts some
		model.setTextFilter("Bjorn");
	
		contacts = model.getContacts(null, 20).getEntries();
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// secondary filter accepts all
		model.setTextFilter("");
	
		contacts = model.getContacts(null, 20).getEntries();
		Assert.assertEquals(3, contacts.length);
		Assert.assertEquals("Anders Carlsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("bjorn Andersen ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Bjorn Bjornsen ", ((ContactProfile) contacts[2]).getFullName());
		
		model.exit(false);
	}	
	
	public void testAddInsideList() throws Exception
	{
		setUpAndStart();
		
		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);
		
		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// add contact
		ContactProfile profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "B");
		profile.setGroups(new Group[] {GROUPS[1]});
		profile.commit();
		profile.unload();

		// ui notified
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.CONTACTS, Event.Contacts.ADD_TO_LIST, null)
		}, getEvents());
		
		// new contact in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(3, contacts.length);	
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Anders B ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[2]).getFullName());
		
		// new contact persisted
		Assert.assertEquals(10, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));

		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(10);
		
		Assert.assertEquals(0, record.getSabId());
		Assert.assertEquals("Anders", record.getFirstName());
		Assert.assertEquals("B", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {2}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// new contact in change log
		Assert.assertEquals(new long[] {10}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));
		
		model.exit(false);
	}	
	
	public void testAddOutsideList() throws Exception
	{
		setUpAndStart();
		
		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);
		
		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100); 

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// add contact
		ContactProfile profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "B");
		profile.setGroups(new Group[] {GROUPS[3]});
		profile.commit();
		profile.unload();

		// ui not notified
		Assert.assertEquals("Listener", new Event[] {
		}, getEvents());
		
		// new contact not in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);	
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// new contact persisted
		Assert.assertEquals(10, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));

		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(10);
		
		Assert.assertEquals(0, record.getSabId());
		Assert.assertEquals("Anders", record.getFirstName());
		Assert.assertEquals("B", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {4}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// new contact in change log
		Assert.assertEquals(new long[] {10}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));
		
		model.exit(false);
	}	
	
	public void testUpdateInsideList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		ContactProfile contact = (ContactProfile) contacts[0];
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", contact.getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//update contact
		contact.load(true);
		contact.lock();
		contact.setName("Carl", "", "C");
		contact.setGroups(new Group[] {GROUPS[1], GROUPS[3]});
		contact.commit();
		contact.unload();
		
		// ui notified
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE_IN_LIST, null)
		}, getEvents());
		
		// changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);	
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl C ", ((ContactProfile) contacts[1]).getFullName());
		
		// changes persisted
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(7);
		
		Assert.assertEquals(70007, record.getSabId());
		Assert.assertEquals("Carl", record.getFirstName());
		Assert.assertEquals("C", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {3, 2, 4}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// updated contact in change log
		Assert.assertEquals(new long[] {7}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));

		model.exit(false);
	}	
	
	public void testUpdateOutOfList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		ContactProfile contact = (ContactProfile) contacts[0];
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", contact.getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//update contact
		contact.load(true);
		contact.lock();
		contact.setName("Carl", "", "C");
		contact.setGroups(new Group[] {GROUPS[3]});
		contact.commit();
		contact.unload();
		
		Object data = getEvents()[0].getData();
		
		// ui notified
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.CONTACTS, Event.Contacts.REMOVE_FROM_LIST, data)
		}, getEvents());
		
		// changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(1, contacts.length);	
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[0]).getFullName());
		
		// changes persisted
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(7);
		
		Assert.assertEquals(70007, record.getSabId());
		Assert.assertEquals("Carl", record.getFirstName());
		Assert.assertEquals("C", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {3, 4}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// updated contact in change log
		Assert.assertEquals(new long[] {7}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));
		
		model.exit(false);
	}	
	
	public void testUpdateIntoList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//update contact
		ContactProfile contact = model.getContact(2);
		
		contact.load(true);
		contact.lock();
		contact.setName("Bjorn", "", "C");
		contact.setGroups(new Group[] {GROUPS[1]});
		contact.commit();
		contact.unload();
				
		// ui notified
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.CONTACTS, Event.Contacts.ADD_TO_LIST, null)
		}, getEvents());
		
		// changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(3, contacts.length);	
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Bjorn C ", ((ContactProfile) contacts[1]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[2]).getFullName());
		
		// changes persisted
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(2);
		
		Assert.assertEquals(70002, record.getSabId());
		Assert.assertEquals("Bjorn", record.getFirstName());
		Assert.assertEquals("C", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {2}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// updated contact in change log
		Assert.assertEquals(new long[] {2}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));
		
		model.exit(false);
	}	
	
	public void testUpdateOutsideList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;

		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//update contact
		ContactProfile contact = model.getContact(2);
		
		contact.load(true);
		contact.lock();
		contact.setName("Bjorn", "", "C");
		contact.setGroups(new Group[] {GROUPS[3]});
		contact.commit();
		contact.unload();

		// ui not notified
		Assert.assertEquals("Listener", new Event[] {
		}, getEvents());
		
		// no changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);	
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// changes persisted
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(2);
		
		Assert.assertEquals(70002, record.getSabId());
		Assert.assertEquals("Bjorn", record.getFirstName());
		Assert.assertEquals("C", record.getLastName());
		Assert.assertEquals(-1, record.getNameCri());
		Assert.assertEquals(new long[] {4}, record.getGroups());
		Assert.assertEquals(-1, record.getGroupsCri());
		
		// updated contact in change log
		Assert.assertEquals(new long[] {2}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_ADD_OR_UPDATE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));		
		
		model.exit(false);
	}	
	
	public void testDeleteOutOfList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;
		
		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();

		ContactProfile contact = (ContactProfile) contacts[0];
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", contact.getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//delete contact
		model.delete(contact);

		Object data = getEvents()[0].getData();
		
		// ui notified
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.CONTACTS, Event.Contacts.REMOVE_FROM_LIST, data)
		}, getEvents());
		
		// changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(1, contacts.length);	
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[0]).getFullName());
		
		// contact deleted from storage
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(7);
		
		Assert.assertNull(record);
		
		// deletion in change log
		Assert.assertEquals(new long[] {70007}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_DELETE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));

		model.exit(false);
	}

	public void testDeleteOutsideList() throws Exception
	{
		setUpAndStart();

		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);

		// navigate to 'My Group'
		Filter[] filters = model.getContactFilters();
	
		Assert.assertEquals("My Group", filters[4].getName());
		
		model.setContactsFilter(filters[4]);
		Thread.sleep(100);

		index = 0;
		
		// check list
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());

		//delete contact
		model.delete(model.getContact(2));
		
		// ui not notified
		Assert.assertEquals("Listener", new Event[] {
		}, getEvents());
		
		// no changes in list
		contacts = model.getContacts(null, 20).getEntries();

		Assert.assertEquals(2, contacts.length);
		Assert.assertEquals("Anders andersen ", ((ContactProfile) contacts[0]).getFullName());
		Assert.assertEquals("Carl Bjornsen ", ((ContactProfile) contacts[1]).getFullName());
		
		// contact deleted from storage
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(2);
		
		Assert.assertNull(record);
		
		// deletion in change log
		Assert.assertEquals(new long[] {70002}, settingsStore.getLongArrayValue(Settings.KEY_CHANGELOG_IDS));
		Assert.assertEquals(new int[] {Settings.CHANGELOG_DELETE}, settingsStore.getIntArrayValue(Settings.KEY_CHANGELOG_TYPES));

		model.exit(false);
	}
	
	public void testSendChanges() throws Exception
	{
		setUpAndStart();

		// receive changes
		{
		Vector detailList = new Vector();	

		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(3, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		protocol.mockItemsReceived(5, new ServiceObject[] {}, ServiceObject.CONTACT_CHANGES);
		}
		
		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);
		
		ContactProfile profile = null;
		
		//update name
		profile = model.getContact(5);
		profile.load(true);
		profile.lock();
		profile.setName("Bjorn", "", "A");
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		// add contact with groups
		profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "B");
		profile.setGroups(new Group[] {GROUPS[1]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		//delete contact
		model.delete(model.getContact(6));
		Thread.sleep(100);

		// update groups
		profile = model.getContact(7);
		profile.load(true);
		profile.lock();
		profile.setGroups(new Group[] {GROUPS[3]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		//delete contact
		model.delete(model.getContact(3));
		Thread.sleep(100);

		// add contact without groups
		profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "A");
		profile.commit();
		profile.unload();
		Thread.sleep(100);

		// update name and groups
		profile = model.getContact(2);
		profile.load(true);
		profile.lock();
		profile.setName("Bjorn", "", "C");
		profile.setGroups(new Group[] {GROUPS[1], GROUPS[3]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);

		// send changes to server
		protocol.mockNetworkUp();
		
		protocol.resetEvents();
		
		model.sync();
		Thread.sleep(200);
		
		Object changes = protocol.getEvents()[0].getData();
		Object groups1 = protocol.getEvents()[1].getData();
		Object groups2 = protocol.getEvents()[2].getData();
		Object deletions = protocol.getEvents()[3].getData();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.UPDATE_CONTACTS, changes),
				new Event(Event.Context.TEST, MockCommunicationManager.SET_CONTACT_GROUP_RELATIONS, groups1),
				new Event(Event.Context.TEST, MockCommunicationManager.SET_CONTACT_GROUP_RELATIONS, groups2),
				new Event(Event.Context.TEST, MockCommunicationManager.DELETE_CONTACTS, deletions),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES)
		}, protocol.getEvents());
		
		{
			Hashtable objHt = ((ServiceObject[]) changes)[0].toHashtable();
	
			Assert.assertEquals(new Long(70005), objHt.get("contactid"));
			
			Vector detailList = (Vector) objHt.get("detaillist");
			{
				Hashtable ht = (Hashtable) detailList.elementAt(0);
				Assert.assertEquals("vcard.name", ht.get("key"));
				Assert.assertEquals("A;Bjorn;;", ht.get("val"));
			}		
		}
		{
			Hashtable objHt = ((ServiceObject[]) changes)[1].toHashtable();
	
			Assert.assertNull(objHt.get("contactid"));

			Vector detailList = (Vector) objHt.get("detaillist");
			{
				Hashtable ht = (Hashtable) detailList.elementAt(0);
				Assert.assertEquals("vcard.name", ht.get("key"));
				Assert.assertEquals("B;Anders;;", ht.get("val"));
			}		
		}
		{
			Hashtable objHt = ((ServiceObject[]) changes)[2].toHashtable();
			
			Assert.assertNull(objHt.get("contactid"));
			
			Vector detailList = (Vector) objHt.get("detaillist");
			{
				Hashtable ht = (Hashtable) detailList.elementAt(0);
				Assert.assertEquals("vcard.name", ht.get("key"));
				Assert.assertEquals("A;Anders;;", ht.get("val"));
			}		
		}	
		{
			Hashtable objHt = ((ServiceObject[]) changes)[3].toHashtable();
	
			Assert.assertEquals(new Long(70002), objHt.get("contactid"));

			Vector detailList = (Vector) objHt.get("detaillist");
			{
				Hashtable ht = (Hashtable) detailList.elementAt(0);
				Assert.assertEquals("vcard.name", ht.get("key"));
				Assert.assertEquals("C;Bjorn;;", ht.get("val"));
			}		
		}	
		
		{
			Hashtable objHt = ((ServiceObject[]) groups1)[0].toHashtable();
			
			Vector contactIdList = (Vector) objHt.get("contactidlist");
			Vector groupList = (Vector) objHt.get("grouplist");

			Assert.assertEquals(new Long(70007), contactIdList.elementAt(0));

			Assert.assertEquals(new Long(3), ((Hashtable) groupList.elementAt(0)).get("id"));
			Assert.assertEquals(new Long(4), ((Hashtable) groupList.elementAt(1)).get("id"));
		}

		{
			Hashtable objHt = ((ServiceObject[]) groups2)[0].toHashtable();
			
			Vector contactIdList = (Vector) objHt.get("contactidlist");
			Vector groupList = (Vector) objHt.get("grouplist");
			
			Assert.assertEquals(new Long(70002), contactIdList.elementAt(0));
			
			Assert.assertEquals(new Long(2), ((Hashtable) groupList.elementAt(0)).get("id"));			
			Assert.assertEquals(new Long(4), ((Hashtable) groupList.elementAt(1)).get("id"));
		}	
		
		{
			Hashtable objHt = (Hashtable) deletions;
			
			Vector contactIdList = (Vector) objHt.get("contactidlist");
			
			Assert.assertEquals(new Long(70006), contactIdList.elementAt(0));
			Assert.assertEquals(new Long(70003), contactIdList.elementAt(1));
		}
		
		long startOfSession = ((ServiceBroker) model).getStartOfSession();
		
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(5);
		Assert.assertEquals("cri of record 5", startOfSession + 8 * 20, record.getNameCri());
	
		record = (ManagedProfileRecord) contactsStore.getFullRecord(7);
		Assert.assertEquals("cri of record 7", startOfSession + 9 * 20, record.getGroupsCri());
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(2);
		Assert.assertEquals(startOfSession + 8 * 20, record.getNameCri());
		Assert.assertEquals("cri of record 2", startOfSession + 10 * 20, record.getGroupsCri());
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(10);
		Assert.assertEquals(-(8 * 25 + 1), record.getSabId());
		Assert.assertEquals("cri of record 9", startOfSession + 8 * 20, record.getNameCri());
		Assert.assertEquals(-1, record.getGroupsCri());  // not sent yet
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(11);
		Assert.assertEquals(-(8 * 25 + 2), record.getSabId());
		Assert.assertEquals("cri of record 10", startOfSession + 8 * 20, record.getNameCri());
		
		model.exit(false);
	}

	public void testReceiveChangeResults() throws Exception
	{
		setUpAndStart();

		// receive changes
		{
		Vector detailList = new Vector();	

		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(3, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		protocol.mockItemsReceived(5, new ServiceObject[] {}, ServiceObject.CONTACT_CHANGES);
		}
		
		// make sure the sync is not started
		protocol.mockNetworkDown((byte) 0);
		
		ContactProfile profile = null;
		
		//update name
		profile = model.getContact(5);
		profile.load(true);
		profile.lock();
		profile.setName("Bjorn", "", "A");
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		// add contact with groups
		profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "B");
		profile.setGroups(new Group[] {GROUPS[1]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		//delete contact
		model.delete(model.getContact(6));
		Thread.sleep(100);

		// update groups
		profile = model.getContact(7);
		profile.load(true);
		profile.lock();
		profile.setGroups(new Group[] {GROUPS[3]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);
		
		//delete contact
		model.delete(model.getContact(3));
		Thread.sleep(100);

		// add contact without groups
		profile = model.createContact();
		profile.load(true);
		profile.lock();
		profile.setName("Anders", "", "A");
		profile.commit();
		profile.unload();
		Thread.sleep(100);

		// update name and groups
		profile = model.getContact(2);
		profile.load(true);
		profile.lock();
		profile.setName("Bjorn", "", "C");
		profile.setGroups(new Group[] {GROUPS[1], GROUPS[3]});
		profile.commit();
		profile.unload();
		Thread.sleep(100);

		// send changes to server
		protocol.mockNetworkUp();
		
		protocol.resetEvents();
		
		model.sync();
		Thread.sleep(200);

		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(10);
		Assert.assertEquals("cri of record 9", -(8 * 25 + 1), record.getSabId());
		Assert.assertEquals(-1, record.getGroupsCri());  // not sent yet
				
		Vector contactList = new Vector();
		
		{
			Hashtable htContact = new Hashtable();
			htContact.put("contactid", new Long(70005));
			contactList.addElement(htContact);
		}
		{
			Hashtable htContact = new Hashtable();
			htContact.put("contactid", new Long(70010));
			contactList.addElement(htContact);
		}		
		{
			Hashtable htContact = new Hashtable();
			htContact.put("contactid", new Long(70011));
			contactList.addElement(htContact);
		}		
		{
			Hashtable htContact = new Hashtable();
			htContact.put("contactid", new Long(70002));
			contactList.addElement(htContact);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("contactlist", contactList);
		
		com.zyb.nowplus.data.protocol.types.BulkUpdateContactsResult serviceObject = new com.zyb.nowplus.data.protocol.types.BulkUpdateContactsResult(ht, false);
		
		protocol.mockItemsReceived(8, new ServiceObject[] {serviceObject}, ServiceObject.BULK_UPDATE_CONTACTS_RESULT);

		// sabIds are set, cris are reset
		record = (ManagedProfileRecord) contactsStore.getFullRecord(5);
		Assert.assertEquals(0, record.getNameCri());
	
		record = (ManagedProfileRecord) contactsStore.getFullRecord(7);
//		Assert.assertEquals(0, record.getGroupsCri());
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(2);
		Assert.assertEquals(0, record.getNameCri());
//		Assert.assertEquals(0, record.getGroupsCri());
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(10);
		Assert.assertEquals(70010, record.getSabId());
		Assert.assertEquals(0, record.getNameCri());
		
		record = (ManagedProfileRecord) contactsStore.getFullRecord(11);
		Assert.assertEquals(70011, record.getSabId());
		Assert.assertEquals(0, record.getNameCri());
		
		model.exit(false);
	}
	
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

	private Event[] events = new Event[40];
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
