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

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.business.domain.Settings;
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
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class MyProfileTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[3];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin1", "msn", "MSN", null);
		EXTERNAL_NETWORKS[1] = new ExternalNetwork("plugin2", "facebook.com", "Facebook", null);
		EXTERNAL_NETWORKS[2] = new ExternalNetwork(null, ExternalNetwork.VODAFONE_360, ExternalNetwork.VODAFONE_360_LABEL, null);
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
	
	private void setUpInstallationWithoutMyProfile()
	{
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, true);
		settingsStore.setStringValue(Settings.KEY_IMSI, "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		settingsStore.setIntValue(Settings.KEY_CONTACTS_ORDER, Order.TYPE_FIRST_LAST);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 0);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 0);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 0);
	}
	
	private void setUpCurrentInstallation(boolean hasAvatar, boolean hasNickname, boolean hasName, 
			boolean hasDateOfBirth, boolean hasUrl, boolean hasWorkDetails, boolean hasNote, boolean hasStatus) throws Exception
	{	
		setUpInstallationWithoutMyProfile();
		
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 1);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);

		ManagedProfileRecord[] records = new ManagedProfileRecord[1];
		
		records[0] = new ManagedProfileRecord();
		records[0].setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		records[0].setCabId(1);
		records[0].setSabId(70001);
		records[0].setUserId(80007);
		records[0].setNowPlusMember(ContactProfile.NOWPLUS_ME);
		records[0].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		if (hasAvatar)
		{
			records[0].setProfileImageURL("image/profile/80007");
		}
		if (hasNickname)
		{
			records[0].setNickname("michael.bloomberg");
		}
		if (hasName)
		{
			records[0].setFirstName("Michael");
			records[0].setMiddleNames("R.");
			records[0].setLastName("Bloomberg");
		}
		if (hasDateOfBirth)
		{
			records[0].setYearOfBirth(1942);
			records[0].setMonthOfBirth(2);
			records[0].setDayOfBirth(14);
		}
		records[0].setIdentities(new Identity[] {
			Identity.createPhoneNumber(Identity.SUBTYPE_HOME, "+31000000001", false, 800021),
			Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "+31000000002", false, 800022),
			Identity.createEmail(Identity.SUBTYPE_HOME, "michael1@nyc.com", false, 800031),
			Identity.createEmail(Identity.SUBTYPE_WORK, "michael2@nyc.com", false, 800032),
			Identity.createImAccount(EXTERNAL_NETWORKS[2], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID)
		});
		if (hasUrl)
		{
			records[0].setUrl(Identity.createUrl("http://www1.ibm.com/"));
		}
		records[0].setAddresses(new Address[] {
				Address.createAddress(Address.toType("home"), "Straat 1", null, "Plaats", "1234AB", "Regio", "Nederland", 800051),
				Address.createAddress(Address.toType("work"), "1 Street", null, "City", "NY 12345", "New York", "United States", 800052)
			});
		records[0].setGroups(new long[] {});
		if (hasWorkDetails)
		{
			records[0].setTitle("Mayor");
			records[0].setDepartment("Council");
			records[0].setOrganisation("New York");
		}
		if (hasNote)
		{
			records[0].setNotes(new Note[] {new Note("Appointed in 2001.", 800061)});
		}
		if (hasStatus)
		{
			records[0].setStatus("Is in charge");	
			records[0].setStatusSourceNetworkId("facebook");
		}
		contactsStore.initialise(records);
	}	
	
	public void testReceiveMyProfile() throws Exception
	{
		setUpInstallationWithoutMyProfile();
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// start up finished
		Assert.assertEquals("Listener", new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		// receive new profile
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "photo");
			ht.put("val", "image/profile/80007");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.nickname");
			ht.put("val", "michael.bloomberg");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Bloomberg;Michael;R.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.date");
			ht.put("val", "1942-02-14");
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.phone");
			ht.put("detailid", new Long(800021));
			ht.put("val", "+31000000001");
			ht.put("type", "home");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.phone");
			ht.put("detailid", new Long(800022));
			ht.put("val", "+31000000002");
			ht.put("type", "cell");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.email");
			ht.put("detailid", new Long(800031));
			ht.put("val", "michael1@nyc.com");
			ht.put("type", "home");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.email");
			ht.put("detailid", new Long(800032));
			ht.put("val", "michael2@nyc.com");
			ht.put("type", "work");
			detailList.addElement(ht);
		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.url");
			ht.put("detailid", new Long(800041));
			ht.put("val", "http://www1.ibm.com/");
			detailList.addElement(ht);
		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.address");
			ht.put("detailid", new Long(800051));
			ht.put("val", ";Straat;1;Plaats;Regio;1234AB;Nederland");
			ht.put("type", "home");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.address");
			ht.put("detailid", new Long(800052));
			ht.put("val", ";;1 Street;City;New York;NY 12345;United States");
			ht.put("type", "work");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.title");
			ht.put("val", "Mayor");
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.org");
			ht.put("val", "New York;Council");
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.note");
			ht.put("detailid", new Long(800061));
			ht.put("val", "Appointed in 2001.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "presence.text");
			ht.put("val", "Is in charge");
			ht.put("type", "facebook");
			detailList.addElement(ht);
		}		
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70001));
		ht.put("userid", new Long(80007));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(33);

		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", me.getProfileImage().getUrl());
		Assert.assertEquals("michael.bloomberg", me.getNickname());
		Assert.assertEquals("Michael", me.getFirstName());
		Assert.assertEquals("R.", me.getMiddleNames());
		Assert.assertEquals("Bloomberg", me.getLastName());
		Assert.assertEquals("Michael R. Bloomberg ", me.getFullName());
		Assert.assertEquals(new Date(-879811200000L), me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800031, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[0].getName());
			Assert.assertEquals(800032, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[1].getName());
		}	
		// url
		{
			Identity url = me.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www1.ibm.com/", url.getName());
			Assert.assertEquals("http://www1.ibm.com/", url.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals(new String[] {"New York", "Council", "Mayor"}, me.getWorkDetails());
		Assert.assertEquals(new Note("Appointed in 2001.", 800061), me.getNote());
		Assert.assertEquals("Is in charge", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[1], me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(33, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", record.getProfileImageURL());
		Assert.assertEquals("michael.bloomberg", record.getNickname());
		Assert.assertEquals("Michael", record.getFirstName());
		Assert.assertEquals("R.", record.getMiddleNames());
		Assert.assertEquals("Bloomberg", record.getLastName());
		Assert.assertEquals(1942, record.getYearOfBirth());
		Assert.assertEquals(2, record.getMonthOfBirth());
		Assert.assertEquals(14, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());		
		}
		// emails
		{
			Assert.assertEquals(800031, ids[2].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[2].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[2].getName());
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[3].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[3].getName());
		}	
		// url
		{
			Identity url = record.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www1.ibm.com/", url.getName());
			Assert.assertEquals("http://www1.ibm.com/", url.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals("Mayor", record.getTitle());
		Assert.assertEquals("Council", record.getDepartment());
		Assert.assertEquals("New York", record.getOrganisation());
		Assert.assertEquals(new Note[] {new Note("Appointed in 2001.", 800061)}, record.getNotes());
		Assert.assertEquals("Is in charge", record.getStatus());
		Assert.assertEquals("facebook.com", record.getStatusSourceNetworkId());	
		
		// notified ui of change
		Assert.assertEquals("Listener", new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),  // this is the actual update
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))   // this sets the availability to online
		}, getEvents());
		
		// user exits
		model.exit(false);
	}
	
	public void testReceiveChangedMyProfile1() throws Exception
	{	
		setUpCurrentInstallation(true, false, true, true, true, true, false, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// receive changes
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.nickname");
			ht.put("val", "peter.boomberg");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Boomberg;Peter;S.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.date");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.phone");
			ht.put("detailid", new Long(800021));
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.phone");
			ht.put("detailid", new Long(800022));
			ht.put("val", "+31000000022");
			ht.put("type", "home");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.phone");
			ht.put("detailid", new Long(800023));
			ht.put("val", "+31000000003");
			ht.put("type", "work");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.url");
			ht.put("detailid", new Long(800041));
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}			
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.note");
			ht.put("detailid", new Long(800061));
			ht.put("val", "Appointed in 2002.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "presence.text");
			ht.put("val", "Is charging");
			detailList.addElement(ht);
		}	

		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);

		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", me.getProfileImage().getUrl());
		Assert.assertEquals("peter.boomberg", me.getNickname());
		Assert.assertEquals("Peter", me.getFirstName());
		Assert.assertEquals("S.", me.getMiddleNames());
		Assert.assertEquals("Boomberg", me.getLastName());
		Assert.assertEquals("Peter S. Boomberg ", me.getFullName());
		Assert.assertNull(me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[1].getSubtype());
			Assert.assertEquals("+31000000022", ids[1].getName());
			Assert.assertEquals(800023, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[0].getSubtype());
			Assert.assertEquals("+31000000003", ids[0].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800031, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[0].getName());
			Assert.assertEquals(800032, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[1].getName());
		}	
		// url
		{
			Assert.assertNull(me.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals(new String[] {"New York", "Council", "Mayor"}, me.getWorkDetails());
		Assert.assertEquals(new Note("Appointed in 2002.", 800061), me.getNote());
		Assert.assertEquals("Is charging", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[2], me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(34, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", record.getProfileImageURL());
		Assert.assertEquals("peter.boomberg", record.getNickname());
		Assert.assertEquals("Peter", record.getFirstName());
		Assert.assertEquals("S.", record.getMiddleNames());
		Assert.assertEquals("Boomberg", record.getLastName());
		Assert.assertEquals(0, record.getYearOfBirth());
		Assert.assertEquals(0, record.getMonthOfBirth());
		Assert.assertEquals(0, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[1].getSubtype());
			Assert.assertEquals("+31000000022", ids[1].getName());	
			Assert.assertEquals(800023, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[0].getSubtype());
			Assert.assertEquals("+31000000003", ids[0].getName());
		}
		// emails
		{
			Assert.assertEquals(800031, ids[2].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[2].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[2].getName());
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[3].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[3].getName());
		}	
		// url
		{
			Assert.assertNull(record.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals("Mayor", record.getTitle());
		Assert.assertEquals("Council", record.getDepartment());
		Assert.assertEquals("New York", record.getOrganisation());
		Assert.assertEquals(new Note[] {new Note("Appointed in 2002.", 800061)}, record.getNotes());
		Assert.assertEquals("Is charging", record.getStatus());
		Assert.assertEquals("nowplus", record.getStatusSourceNetworkId());		

		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))
		}, getEvents());
		
		// user exits
		model.exit(false);
	}
	
	public void testReceiveChangedMyProfile2() throws Exception
	{	
		setUpCurrentInstallation(false, true, true, true, true, false, true, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// receive changes
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "photo");
			ht.put("val", "image/avatar/80007");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.nickname");
			ht.put("val", "peter.boomberg");
			detailList.addElement(ht);
		}
//		{  can't have empty name!
//			Hashtable ht = new Hashtable();
//			ht.put("key", "vcard.name");
//			ht.put("deleted", Boolean.TRUE);
//			detailList.addElement(ht);
//		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.email");
			ht.put("detailid", new Long(800031));
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.email");
			ht.put("detailid", new Long(800032));
			ht.put("val", "peter2@nyc.com");
			ht.put("type", "home");
			detailList.addElement(ht);
		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.email");
			ht.put("detailid", new Long(800033));
			ht.put("val", "michael3@nyc.com");
			ht.put("type", "work");
			detailList.addElement(ht);
		}	
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.title");
			ht.put("val", "Big Boss");
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.org");
			ht.put("val", "NY;C");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.note");
			ht.put("detailid", new Long(800061));
			ht.put("val", "Appointed in 2002.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "presence.text");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}			

		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals("image/avatar/80007", me.getProfileImage().getUrl());
		Assert.assertEquals("peter.boomberg", me.getNickname());
//		Assert.assertEquals("", me.getFirstName());
//		Assert.assertEquals("", me.getMiddleNames());
//		Assert.assertEquals("", me.getLastName());
//		Assert.assertEquals("", me.getFullName());
		Assert.assertEquals(new Date(-879811200000L), me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800032, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[1].getSubtype());
			Assert.assertEquals("peter2@nyc.com", ids[1].getName());
			Assert.assertEquals(800033, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[0].getSubtype());
			Assert.assertEquals("michael3@nyc.com", ids[0].getName());
		}	
		// url
		{
			Identity url = me.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www1.ibm.com/", url.getName());
			Assert.assertEquals("http://www1.ibm.com/", url.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals(new String[] {"NY", "C", "Big Boss"}, me.getWorkDetails());
		Assert.assertEquals(new Note("Appointed in 2002.", 800061), me.getNote());
		Assert.assertNull(me.getStatus());
		Assert.assertNull(me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(34, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals("image/avatar/80007", record.getProfileImageURL());
		Assert.assertEquals("peter.boomberg", record.getNickname());
//		Assert.assertNull(record.getFirstName());
//		Assert.assertNull(record.getMiddleNames());
//		Assert.assertNull(record.getLastName());
		Assert.assertEquals(1942, record.getYearOfBirth());
		Assert.assertEquals(2, record.getMonthOfBirth());
		Assert.assertEquals(14, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());		
		}
		// emails
		{
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[3].getSubtype());
			Assert.assertEquals("peter2@nyc.com", ids[3].getName());
			Assert.assertEquals(800033, ids[2].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[2].getSubtype());
			Assert.assertEquals("michael3@nyc.com", ids[2].getName());
		}	
		// url
		{
			Identity url = record.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www1.ibm.com/", url.getName());
			Assert.assertEquals("http://www1.ibm.com/", url.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals("Big Boss", record.getTitle());
		Assert.assertEquals("C", record.getDepartment());
		Assert.assertEquals("NY", record.getOrganisation());
		Assert.assertEquals(new Note[] {new Note("Appointed in 2002.", 800061)}, record.getNotes());
		Assert.assertNull(record.getStatus());
		Assert.assertNull(record.getStatusSourceNetworkId());		
		
		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))
		}, getEvents());
		
		// user exits
		model.exit(false);
	}
	
	public void testReceiveChangedMyProfile3() throws Exception
	{	
		setUpCurrentInstallation(true, true, true, false, false, true, true, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// receive changes
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "photo");
			ht.put("val", "image/avatar/80007");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.nickname");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.date");
			ht.put("val", "1942-03-14");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.url");
			ht.put("detailid", new Long(800041));
			ht.put("val", "http://www2.ibm.com/");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.address");
			ht.put("detailid", new Long(800051));
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.address");
			ht.put("detailid", new Long(800052));
			ht.put("val", ";2 Street;;Village;Texas;TX 12345;USA");
			ht.put("type", "home");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.address");
			ht.put("detailid", new Long(800053));
			ht.put("val", ";;Place du Carrousel;Paris;Ile de France;75001;France");
			ht.put("type", "work");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.title");
			ht.put("val", "Big Boss");
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.org");
			ht.put("val", "NY;C");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.note");
			ht.put("detailid", new Long(800061));
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals("image/avatar/80007", me.getProfileImage().getUrl());
		Assert.assertEquals("", me.getNickname());
		Assert.assertEquals("Michael", me.getFirstName());
		Assert.assertEquals("R.", me.getMiddleNames());
		Assert.assertEquals("Bloomberg", me.getLastName());
		Assert.assertEquals("Michael R. Bloomberg ", me.getFullName());
		Assert.assertEquals(new Date(-877392000000L), me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800031, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[0].getName());
			Assert.assertEquals(800032, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[1].getName());
		}	
		// url
		{
			Identity url = me.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[1].getType());
			Assert.assertEquals("2 Street ", addresses[1].getStreet());
			Assert.assertEquals("Village", addresses[1].getTown());
			Assert.assertEquals("TX 12345", addresses[1].getPostcode());
			Assert.assertEquals("Texas", addresses[1].getRegion());
			Assert.assertEquals("USA", addresses[1].getCountry());
			Assert.assertEquals(Address.TYPE_WORK, addresses[0].getType());
			Assert.assertEquals("Place du Carrousel ", addresses[0].getStreet());
			Assert.assertEquals("Paris", addresses[0].getTown());
			Assert.assertEquals("75001", addresses[0].getPostcode());
			Assert.assertEquals("Ile de France", addresses[0].getRegion());
			Assert.assertEquals("France", addresses[0].getCountry());
		}
		Assert.assertEquals(new String[] {"NY", "C", "Big Boss"}, me.getWorkDetails());
		Assert.assertNull(me.getNote());
		Assert.assertEquals("Is in charge", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[1], me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(34, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals("image/avatar/80007", record.getProfileImageURL());
		Assert.assertNull(record.getNickname());
		Assert.assertEquals("Michael", record.getFirstName());
		Assert.assertEquals("R.", record.getMiddleNames());
		Assert.assertEquals("Bloomberg", record.getLastName());
		Assert.assertEquals(1942, record.getYearOfBirth());
		Assert.assertEquals(3, record.getMonthOfBirth());
		Assert.assertEquals(14, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());		
		}
		// emails
		{
			Assert.assertEquals(800031, ids[2].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[2].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[2].getName());
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[3].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[3].getName());
		}	
		// url
		{
			Identity url = record.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[1].getType());
			Assert.assertEquals("2 Street ", addresses[1].getStreet());
			Assert.assertEquals("Village", addresses[1].getTown());
			Assert.assertEquals("TX 12345", addresses[1].getPostcode());
			Assert.assertEquals("Texas", addresses[1].getRegion());
			Assert.assertEquals("USA", addresses[1].getCountry());
			Assert.assertEquals(800053, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[0].getType());
			Assert.assertEquals("Place du Carrousel ", addresses[0].getStreet());
			Assert.assertEquals("Paris", addresses[0].getTown());
			Assert.assertEquals("75001", addresses[0].getPostcode());
			Assert.assertEquals("Ile de France", addresses[0].getRegion());
			Assert.assertEquals("France", addresses[0].getCountry());
		}
		Assert.assertEquals("Big Boss", record.getTitle());
		Assert.assertEquals("C", record.getDepartment());
		Assert.assertEquals("NY", record.getOrganisation());
		Assert.assertEquals(new Note[0], record.getNotes());
		Assert.assertEquals("Is in charge", record.getStatus());
		Assert.assertEquals("facebook.com", record.getStatusSourceNetworkId());		
		
		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))
		}, getEvents());
		
		// user exits
		model.exit(false);
	}	

	public void testReceiveChangedMyProfile4() throws Exception
	{	
		setUpCurrentInstallation(true, true, true, true, true, true, true, false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// receive changes
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "photo");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Boomberg;Peter;S.");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.date");
			ht.put("val", "1942-03-14");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.url");
			ht.put("detailid", new Long(800041));
			ht.put("val", "http://www2.ibm.com/");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.title");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}		
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.org");
			ht.put("deleted", Boolean.TRUE);
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "presence.text");
			ht.put("val", "Is charging");
			detailList.addElement(ht);
		}	

		Hashtable ht = new Hashtable();
		ht.put("detaillist", detailList);	
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals(null, me.getProfileImage().getUrl());
		Assert.assertEquals("michael.bloomberg", me.getNickname());
		Assert.assertEquals("Peter", me.getFirstName());
		Assert.assertEquals("S.", me.getMiddleNames());
		Assert.assertEquals("Boomberg", me.getLastName());
		Assert.assertEquals("Peter S. Boomberg ", me.getFullName());
		Assert.assertEquals(new Date(-877392000000L), me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800031, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[0].getName());
			Assert.assertEquals(800032, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[1].getName());
		}	
		// url
		{
			Identity url = me.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals(new String[0], me.getWorkDetails());
		Assert.assertEquals(new Note("Appointed in 2001.", 800061), me.getNote());
		Assert.assertEquals("Is charging", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[2], me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(34, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals(null, record.getProfileImageURL());
		Assert.assertEquals("michael.bloomberg", record.getNickname());
		Assert.assertEquals("Peter", record.getFirstName());
		Assert.assertEquals("S.", record.getMiddleNames());
		Assert.assertEquals("Boomberg", record.getLastName());
		Assert.assertEquals(1942, record.getYearOfBirth());
		Assert.assertEquals(3, record.getMonthOfBirth());
		Assert.assertEquals(14, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000001", ids[0].getName());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_MOBILE, ids[1].getSubtype());
			Assert.assertEquals("+31000000002", ids[1].getName());		
		}
		// emails
		{
			Assert.assertEquals(800031, ids[2].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[2].getSubtype());
			Assert.assertEquals("michael1@nyc.com", ids[2].getName());
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[3].getSubtype());
			Assert.assertEquals("michael2@nyc.com", ids[3].getName());
		}	
		// url
		{
			Identity url = record.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("Straat 1 ", addresses[0].getStreet());
			Assert.assertEquals("Plaats", addresses[0].getTown());
			Assert.assertEquals("1234AB", addresses[0].getPostcode());
			Assert.assertEquals("Regio", addresses[0].getRegion());
			Assert.assertEquals("Nederland", addresses[0].getCountry());
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("1 Street ", addresses[1].getStreet());
			Assert.assertEquals("City", addresses[1].getTown());
			Assert.assertEquals("NY 12345", addresses[1].getPostcode());
			Assert.assertEquals("New York", addresses[1].getRegion());
			Assert.assertEquals("United States", addresses[1].getCountry());
		}
		Assert.assertEquals(null, record.getTitle());
		Assert.assertEquals(null, record.getDepartment());
		Assert.assertEquals(null, record.getOrganisation());
		Assert.assertEquals(new Note[] {new Note("Appointed in 2001.", 800061)}, record.getNotes());
		Assert.assertEquals("Is charging", record.getStatus());
		Assert.assertEquals("nowplus", record.getStatusSourceNetworkId());		
		
		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.APP, Event.App.START, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
			new Event(Event.Context.APP, Event.App.READY, null),
			new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))
		}, getEvents());
		
		// user exits
		model.exit(false);
	}

	public void testChangeMyProfile() throws Exception
	{	
		setUpCurrentInstallation(true, true, true, true, true, true, true, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		index = 0;
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		me.lock();
		me.setNickname("peter.boomberg");
		me.setName("Peter", "S.", "Boomberg");
		me.setDateOfBirth(new Date(-877392000000L));
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			me.removeIdentity(ids[0]);
			me.updateIdentity(ids[1], Identity.createPhoneNumber(Identity.SUBTYPE_HOME, ids[1].getNabSubtypes(), "+31000000022", false, ids[1].getSabDetailId()));
			me.addIdentity(Identity.createPhoneNumber(Identity.SUBTYPE_WORK, "+31000000003", false));
		}
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			me.removeIdentity(ids[0]);
			me.updateIdentity(ids[1], Identity.createEmail(Identity.SUBTYPE_HOME, ids[1].getNabSubtypes(), "peter2@nyc.com", false, ids[1].getSabDetailId()));
			me.addIdentity(Identity.createEmail(Identity.SUBTYPE_WORK, "michael3@nyc.com", false));
		}
		{
			Address[] addrs = me.getAddresses();
			me.removeAddress(addrs[0]);
			me.updateAddress(addrs[1], Address.createAddress(Address.TYPE_HOME, addrs[1].getNabSubtypes(), "2 Street", null, "Village", "TX 12345", "Texas", "USA", addrs[1].getSabDetailId()));
			me.addAddress(Address.createAddress(Address.TYPE_WORK, "Place du Carrousel", null, "Paris", "75001", "Ile de France", "France"));
		}
		me.setUrl(Identity.createUrl("http://www2.ibm.com/"));
		me.setWorkDetails(new String[] {"NY", "C", "Big Boss"});
		me.setNote("Appointed in 2002.");
		me.commit();
		
		model.finishedEditing(me);
		
		Thread.sleep(100);
		
		// my profile changed is fired, but cab changed for sync is not fired
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1))
		}, getEvents());
		
		Assert.assertEquals(1, me.getCabId());
		Assert.assertEquals(70001, me.getSabId());
		Assert.assertEquals(80007, me.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, me.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", me.getProfileImage().getUrl());
		Assert.assertEquals("peter.boomberg", me.getNickname());
		Assert.assertEquals("Peter", me.getFirstName());
		Assert.assertEquals("S.", me.getMiddleNames());
		Assert.assertEquals("Boomberg", me.getLastName());
		Assert.assertEquals("Peter S. Boomberg ", me.getFullName());
		Assert.assertEquals(new Date(-877392000000L), me.getDateOfBirth());
		// phone numbers
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_PHONE);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800022, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("+31000000022", ids[0].getName());
			Assert.assertEquals(0, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("+31000000003", ids[1].getName());
		}
		// emails
		{
			Identity[] ids = me.getIdentities(Identity.TYPE_EMAIL);
			Assert.assertEquals(2, ids.length);
			Assert.assertEquals(800032, ids[0].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[0].getSubtype());
			Assert.assertEquals("peter2@nyc.com", ids[0].getName());
			Assert.assertEquals(0, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[1].getSubtype());
			Assert.assertEquals("michael3@nyc.com", ids[1].getName());
		}
		// url
		{
			Identity url = me.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}	
		// addresses
		{
			Address[] addresses = me.getAddresses();
			Assert.assertEquals(2, addresses.length);
			Assert.assertEquals(800052, addresses[0].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[0].getType());
			Assert.assertEquals("2 Street ", addresses[0].getStreet());
			Assert.assertEquals("Village", addresses[0].getTown());
			Assert.assertEquals("TX 12345", addresses[0].getPostcode());
			Assert.assertEquals("Texas", addresses[0].getRegion());
			Assert.assertEquals("USA", addresses[0].getCountry());
			Assert.assertEquals(0, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[1].getType());
			Assert.assertEquals("Place du Carrousel ", addresses[1].getStreet());
			Assert.assertEquals("Paris", addresses[1].getTown());
			Assert.assertEquals("75001", addresses[1].getPostcode());
			Assert.assertEquals("Ile de France", addresses[1].getRegion());
			Assert.assertEquals("France", addresses[1].getCountry());
		}
		Assert.assertEquals(new String[] {"NY", "C", "Big Boss"}, me.getWorkDetails());
		Assert.assertEquals(new Note("Appointed in 2002.", 800061), me.getNote());
		Assert.assertEquals("Is in charge", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[1], me.getStatusSource());
		
		// latest cab id and current rev number
		Assert.assertEquals(1, settingsStore.getLongValue(Settings.KEY_LATEST_CAB_ID));
		Assert.assertEquals(33, settingsStore.getLongValue(Settings.KEY_CURRENT_REV_ME));
		
		// data store
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals(ManagedProfileRecord.TYPE_MY_PROFILE, record.getType());
		Assert.assertEquals(1, record.getCabId());
		Assert.assertEquals(70001, record.getSabId());
		Assert.assertEquals(80007, record.getUserId());
		Assert.assertEquals(MyProfile.NOWPLUS_ME, record.getNowPlusMember());
		Assert.assertEquals("image/profile/80007", record.getProfileImageURL());
		Assert.assertEquals("peter.boomberg", record.getNickname());
		Assert.assertEquals("Peter", record.getFirstName());
		Assert.assertEquals("S.", record.getMiddleNames());
		Assert.assertEquals("Boomberg", record.getLastName());
		Assert.assertEquals(1942, record.getYearOfBirth());
		Assert.assertEquals(3, record.getMonthOfBirth());
		Assert.assertEquals(14, record.getDayOfBirth());
		
		Identity[] ids = record.getIdentities();
		// phone numbers
		{
			Assert.assertEquals(800021, ids[0].getSabDetailId());
			Assert.assertTrue(ids[0].isEmpty());
			Assert.assertEquals(800022, ids[1].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[1].getSubtype());
			Assert.assertEquals("+31000000022", ids[1].getName());		
			Assert.assertEquals(0, ids[5].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[5].getSubtype());
			Assert.assertEquals("+31000000003", ids[5].getName());
		}
		// emails
		{
			Assert.assertEquals(800031, ids[2].getSabDetailId());
			Assert.assertTrue(ids[2].isEmpty());
			Assert.assertEquals(800032, ids[3].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_HOME, ids[3].getSubtype());
			Assert.assertEquals("peter2@nyc.com", ids[3].getName());
			Assert.assertEquals(0, ids[6].getSabDetailId());
			Assert.assertEquals(Identity.SUBTYPE_WORK, ids[6].getSubtype());
			Assert.assertEquals("michael3@nyc.com", ids[6].getName());
		}	
		// url
		{
			Identity url = record.getUrl();
			Assert.assertEquals(0, url.getSabDetailId());
			Assert.assertEquals("www2.ibm.com/", url.getName());
			Assert.assertEquals("http://www2.ibm.com/", url.getUrl());
		}
		// addresses
		{
			Address[] addresses = record.getAddresses();
			Assert.assertEquals(800051, addresses[0].getSabDetailId());
			Assert.assertTrue(addresses[0].isEmpty());
			
			Assert.assertEquals(800052, addresses[1].getSabDetailId());
			Assert.assertEquals(Address.TYPE_HOME, addresses[1].getType());
			Assert.assertEquals("2 Street ", addresses[1].getStreet());
			Assert.assertEquals("Village", addresses[1].getTown());
			Assert.assertEquals("TX 12345", addresses[1].getPostcode());
			Assert.assertEquals("Texas", addresses[1].getRegion());
			Assert.assertEquals("USA", addresses[1].getCountry());
			
			Assert.assertEquals(0, addresses[2].getSabDetailId());
			Assert.assertEquals(Address.TYPE_WORK, addresses[2].getType());
			Assert.assertEquals("Place du Carrousel ", addresses[2].getStreet());
			Assert.assertEquals("Paris", addresses[2].getTown());
			Assert.assertEquals("75001", addresses[2].getPostcode());
			Assert.assertEquals("Ile de France", addresses[2].getRegion());
			Assert.assertEquals("France", addresses[2].getCountry());
		}
		Assert.assertEquals("Big Boss", record.getTitle());
		Assert.assertEquals("C", record.getDepartment());
		Assert.assertEquals("NY", record.getOrganisation());
		Assert.assertEquals(new Note[] {new Note("Appointed in 2002.", 800061)}, record.getNotes());
		Assert.assertEquals("Is in charge", record.getStatus());
		Assert.assertEquals("facebook.com", record.getStatusSourceNetworkId());
		
		protocol.resetEvents();
		
		// sync
		model.sync();
		
		Thread.sleep(200);
		
		Object data = protocol.getEvents()[0].getData();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.SET_ME, data),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES)
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
		}, protocol.getEvents());
		
//		ServiceObject obj = ((ServiceObject[]) data)[0];
//		
//		Hashtable objHt = obj.toHashtable();
//
//		Vector detailList = (Vector) objHt.get("detaillist");
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(0);
//			Assert.assertEquals("vcard.nickname", ht.get("key"));
//			Assert.assertEquals("peter.boomberg", ht.get("val"));
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(1);
//			Assert.assertEquals("vcard.name", ht.get("key"));
//			Assert.assertEquals("Boomberg;Peter;S.;", ht.get("val"));
//		}		
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(2);
//			Assert.assertEquals("vcard.date", ht.get("key"));
//			Assert.assertEquals("1942-03-14", ht.get("val"));
//		}			
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(3);
//			Assert.assertEquals("vcard.phone", ht.get("key"));
//			Assert.assertEquals(new Long(800021), ht.get("detailid"));
//			// Assert.assertEquals(Boolean.TRUE, ht.get("deleted")); 
//		}	
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(4);
//			Assert.assertEquals("vcard.phone", ht.get("key"));
//			Assert.assertEquals(new Long(800022), ht.get("detailid"));
//			Assert.assertEquals("+31000000022", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(7);
//			Assert.assertEquals("vcard.phone", ht.get("key"));
//			Assert.assertEquals("+31000000003", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(5);
//			Assert.assertEquals("vcard.email", ht.get("key"));
//			Assert.assertEquals(new Long(800031), ht.get("detailid"));
//			// Assert.assertEquals(Boolean.TRUE, ht.get("deleted")); 
//		}	
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(6);
//			Assert.assertEquals("vcard.email", ht.get("key"));
//			Assert.assertEquals(new Long(800032), ht.get("detailid"));
//			Assert.assertEquals("peter2@nyc.com", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(8);
//			Assert.assertEquals("vcard.email", ht.get("key"));
//			Assert.assertEquals("michael3@nyc.com", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(9);
//			Assert.assertEquals("vcard.url", ht.get("key"));
//			Assert.assertEquals("http://www2.ibm.com/", ht.get("val"));
//		}	
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(10);
//			Assert.assertEquals("vcard.address", ht.get("key"));
//			Assert.assertEquals(new Long(800051), ht.get("detailid"));
//			// Assert.assertEquals(Boolean.TRUE, ht.get("deleted")); 
//		}	
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(11);
//			Assert.assertEquals("vcard.address", ht.get("key"));
//			Assert.assertEquals(new Long(800052), ht.get("detailid"));
//			Assert.assertEquals(";;2 Street;Village;Texas;TX 12345;USA", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(12);
//			Assert.assertEquals("vcard.address", ht.get("key"));
//			Assert.assertEquals(";;Place du Carrousel;Paris;Ile de France;75001;France", ht.get("val")); 
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(13);
//			Assert.assertEquals("vcard.title", ht.get("key"));
//			Assert.assertEquals("Big Boss", ht.get("val"));
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(14);
//			Assert.assertEquals("vcard.org", ht.get("key"));
//			Assert.assertEquals("NY;C", ht.get("val"));
//		}
//		{
//			Hashtable ht = (Hashtable) detailList.elementAt(15);
//			Assert.assertEquals("vcard.note", ht.get("key"));
//			Assert.assertEquals("Appointed in 2002.", ht.get("val"));
//		}
		
		// user exits
		model.exit(false);
	}
	
	public void testChangeMyStatus() throws Exception
	{	
		setUpCurrentInstallation(true, true, true, true, true, true, true, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals("Is in charge", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[1], me.getStatusSource());
		
		// change status
		index = 0;
		protocol.resetEvents();
		
		model.setMyStatus("Is charging");
		
		model.sync();
		
		Thread.sleep(100);
		
		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
			new Event(Event.Context.ACTIVITY, Event.Activities.STATUS_STREAM_CHANGED, null)
		}, getEvents());		
		
		Assert.assertEquals("Is charging", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[2], me.getStatusSource());		
		
		ListSelection ls = model.getFriendsStream(0, 20);
		Assert.assertEquals(1, ls.getTotal());
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(Activity.LOCAL_ID, activity.getId());
			Assert.assertEquals(Activity.TYPE_CONTACT_SENT_STATUS_UPDATE, activity.getType());
			Assert.assertEquals("", activity.getTitle());
			Assert.assertEquals("Is charging", activity.getDescription());
		}
		
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertEquals("Is charging", record.getStatus());
		Assert.assertEquals("nowplus", record.getStatusSourceNetworkId());
		
		// sent to server
		Object data = protocol.getEvents()[0].getData();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.SET_ME, data),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES)
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
		}, protocol.getEvents());
		
		ServiceObject obj = ((ServiceObject[]) data)[0];
		
		Hashtable objHt = obj.toHashtable();

		Vector detailList = (Vector) objHt.get("detaillist");
		{
			Hashtable ht = (Hashtable) detailList.elementAt(0);
			Assert.assertEquals("presence.text", ht.get("key"));
			Assert.assertEquals("Is charging", ht.get("val"));
			Assert.assertNull(ht.get("type"));
		}
		
		// user exits
		model.exit(false);
	}
	
	public void testEmptyMyStatus() throws Exception
	{	
		setUpCurrentInstallation(true, true, true, true, true, true, true, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// my profile attributes
		MyProfile me = model.getMe();
		me.load(true);
		
		Assert.assertEquals("Is in charge", me.getStatus());
		Assert.assertEquals(EXTERNAL_NETWORKS[1], me.getStatusSource());
		
		// change status
		index = 0;
		protocol.resetEvents();
		
		model.setMyStatus(null);
		
		model.sync();
		
		Thread.sleep(100);
		
		// notified ui
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(1)),
		}, getEvents());		
		
		Assert.assertNull(me.getStatus());
		Assert.assertNull(me.getStatusSource());		
		
		ListSelection ls = model.getFriendsStream(0, 20);
		Assert.assertEquals(0, ls.getTotal());
		
		ManagedProfileRecord record = (ManagedProfileRecord) contactsStore.getFullRecord(1);
		Assert.assertNull(record.getStatus());
		Assert.assertNull(record.getStatusSourceNetworkId());
		
		// sent to server
		Object data = protocol.getEvents()[0].getData();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.SET_ME, data),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES)
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
//				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_IDENTITIES)
		}, protocol.getEvents());
		
		ServiceObject obj = ((ServiceObject[]) data)[0];
		
		Hashtable objHt = obj.toHashtable();

		Vector detailList = (Vector) objHt.get("detaillist");
		{
			Hashtable ht = (Hashtable) detailList.elementAt(0);
			Assert.assertEquals("presence.text", ht.get("key"));
			Assert.assertNull(ht.get("val"));
		}
		
		// user exits
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
