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

import com.zyb.nowplus.business.LaunchException;
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.protocol.types.ChatObject;
import com.zyb.nowplus.data.protocol.types.Presence;
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

public class CommunicationTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[4];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin1", "skype", "Skype", new String[] {"chat"});
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
		protocol = new MockCommunicationManager(20, new byte[] {ServiceObject.MY_IDENTITY}, new int[] {ServerRequest.GET}, new int[] {ServerRequest.MY_IDENTITIES});
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
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 7);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 44);

		// set up contacts
		ManagedProfileRecord[] records = new ManagedProfileRecord[2];

		records[0] = new ManagedProfileRecord();
		records[0].setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		records[0].setCabId(2);
		records[0].setSabId(70002);
		records[0].setUserId(80002);
		records[0].setNowPlusMember(ContactProfile.NOWPLUS_ME);
		records[0].setNowPlusPresence(Channel.PRESENCE_OFFLINE);
		records[0].setFirstName("Ik");
		records[0].setMiddleNames("");
		records[0].setLastName("Ke");
		
		{
			Identity nowPlusAccount = Identity.createImAccount(EXTERNAL_NETWORKS[3], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID);
			nowPlusAccount.getChannel(Channel.TYPE_CHAT).setPresence(Channel.PRESENCE_OFFLINE);
						
			Identity msnAccount = Identity.createImAccount(EXTERNAL_NETWORKS[1], "ik.ke@example.com", false, 80001);
			msnAccount.getChannel(Channel.TYPE_CHAT).setPresence(Channel.PRESENCE_OFFLINE);
			
			Identity facebookAccount = Identity.createSnAccount(EXTERNAL_NETWORKS[2], "ik.ke", "http://www.facebook.com/ik.ke", 80002);
			facebookAccount.getChannel(Channel.TYPE_BROWSE).setPresence(Channel.PRESENCE_OFFLINE);
			
			records[0].setIdentities(new Identity[] {
				nowPlusAccount,
				msnAccount,
				facebookAccount
			});
		}
		
		records[1] = new ManagedProfileRecord();
		records[1].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[1].setCabId(1);
		records[1].setSabId(70001);
		records[1].setUserId(80001);
		records[1].setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
		records[1].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		records[1].setFirstName("Contact");
		records[1].setMiddleNames("");
		records[1].setLastName("Me");
	
		{
			Identity nowPlusAccount = Identity.createImAccount(EXTERNAL_NETWORKS[3], Identity.NOWPLUS_ACCOUNT_LABEL, true, Identity.NOWPLUS_ACCOUNT_SABDETAILID);
			nowPlusAccount.getChannel(Channel.TYPE_CHAT).setPresence(Channel.PRESENCE_ONLINE);
			
			Identity skypeAccount = Identity.createImAccount(EXTERNAL_NETWORKS[0], "contact_me", false, 0);
			skypeAccount.getChannel(Channel.TYPE_CHAT).setPresence(Channel.PRESENCE_ONLINE);
			
			records[1].setIdentities(new Identity[] {
				Identity.createPhoneNumber(Identity.SUBTYPE_HOME, "+44000000001", false),
				Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "+44000000002", true),
				Identity.createEmail(Identity.SUBTYPE_WORK, "contact.me@example.com", false),
				nowPlusAccount,
				skypeAccount,
				Identity.createImAccount(EXTERNAL_NETWORKS[1], "contact.me@example.com", false, 0),
				Identity.createSnAccount(EXTERNAL_NETWORKS[2], "contact.me", "http://www.facebook.com/contact.me", 0)
			});
		}
		
		contactsStore.initialise(records);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
	}
	
	public void testWriteReadExternalNetworks() throws Exception
	{
		setUpAndStart();
		
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArrayUtils.serializeArray(null, new DataOutputStream(baos));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ExternalNetwork[] readExternalNetworks = ExternalNetwork.deserializeExternalNetworkArray(new DataInputStream(bais));
			
			Assert.assertNull(readExternalNetworks);
		}
		
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArrayUtils.serializeArray(EXTERNAL_NETWORKS, new DataOutputStream(baos));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ExternalNetwork[] readExternalNetworks = ExternalNetwork.deserializeExternalNetworkArray(new DataInputStream(bais));
			
			Assert.assertEquals(EXTERNAL_NETWORKS, readExternalNetworks);
		}
		
		model.exit(false);
	}
	
	public void testWriteReadIdentity() throws Exception
	{
		setUpAndStart();

		// phone number
		Identity identity = Identity.createPhoneNumber(Identity.SUBTYPE_HOME, 123, "+44123456789", true, 456);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		identity.write(new DataOutputStream(baos));
	
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Identity readIdentity = new Identity();
		readIdentity.read(new DataInputStream(bais));
		
		Assert.assertEquals(identity, readIdentity);
		
		// email
		identity = Identity.createEmail(Identity.SUBTYPE_WORK, 123, "x@example.com", true, 456);
		
		baos = new ByteArrayOutputStream();
		identity.write(new DataOutputStream(baos));
	
		bais = new ByteArrayInputStream(baos.toByteArray());
		readIdentity = new Identity();
		readIdentity.read(new DataInputStream(bais));
		
		Assert.assertEquals(identity, readIdentity);
		
		// url
		identity = Identity.createUrl("http://www.wikipedia.org/");
		baos = new ByteArrayOutputStream();
		identity.write(new DataOutputStream(baos));
	
		bais = new ByteArrayInputStream(baos.toByteArray());
		readIdentity = new Identity();
		readIdentity.read(new DataInputStream(bais));
		
		Assert.assertEquals(identity, readIdentity);
	
		// im
		identity = Identity.createImAccount(EXTERNAL_NETWORKS[1], "contact_me@example.com", true, 456);
		identity.getChannel(Channel.TYPE_CHAT).addMessage(new Message("Read?", 123456L, true));
		identity.getChannel(Channel.TYPE_CHAT).addMessage(new Message("Write!", 654321L, false));
		baos = new ByteArrayOutputStream();
		identity.write(new DataOutputStream(baos));
	
		bais = new ByteArrayInputStream(baos.toByteArray());
		readIdentity = new Identity();
		readIdentity.read(new DataInputStream(bais));
		
		Assert.assertEquals(identity, readIdentity);
		Assert.assertEquals(identity.getChannel(Channel.TYPE_CHAT), readIdentity.getChannel(Channel.TYPE_CHAT));
		Assert.assertEquals(identity.getChannel(Channel.TYPE_CHAT).getMessages(), readIdentity.getChannel(Channel.TYPE_CHAT).getMessages());
		
		// social network
		identity = Identity.createSnAccount(EXTERNAL_NETWORKS[2], "contact_me@example.com", "http://www.facebook.com/x", 456);
		baos = new ByteArrayOutputStream();
		identity.write(new DataOutputStream(baos));
	
		bais = new ByteArrayInputStream(baos.toByteArray());
		readIdentity = new Identity();
		readIdentity.read(new DataInputStream(bais));
		
		Assert.assertEquals(identity, readIdentity);	
		
		model.exit(false);
	}
	
	public void testNonChatChannels() throws Exception
	{	
		setUpAndStart();

		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);

		Channel channel = null;
		
		// call		
		channel = contact.getPrimaryCallChannel();
		Assert.assertEquals(Channel.TYPE_CALL, channel.getType());
		Assert.assertEquals("+44000000002", channel.getName());

		model.launch(channel);
		
		// sms		
		channel = contact.getPrimarySMSChannel();
		Assert.assertEquals(Channel.TYPE_SMS, channel.getType());
		Assert.assertEquals("+44000000002", channel.getName());
		
		model.launch(channel);
		
		// mms
		channel = contact.getPrimaryMMSChannel();
		Assert.assertEquals(Channel.TYPE_MMS, channel.getType());
		Assert.assertEquals("+44000000002", channel.getName());

		model.launch(channel);
		
		// email
		channel = contact.getPrimaryEmailChannel();
		Assert.assertEquals(Channel.TYPE_EMAIL, channel.getType());
		Assert.assertEquals("contact.me@example.com", channel.getName());
		
		model.launch(channel);
		
		Assert.assertEquals("Context", new Event[] {
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "tel:+44000000002"),
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "smsto:+44000000002"),
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "mmsto:+44000000002"),
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "mailto:contact.me@example.com")
		}, context.getEvents());	
		
		model.exit(false);
	}

	public void testBrowseChannels() throws Exception
	{
		setUpAndStart();

		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);

		// open sns profile page		
		Identity[] snsIds = contact.getIdentities(Identity.TYPE_SN_ACCOUNT);
		Assert.assertEquals(Identity.TYPE_SN_ACCOUNT, snsIds[0].getType());
		Assert.assertEquals("contact.me", snsIds[0].getName());
		
		model.launch(snsIds[0].getChannel(Channel.TYPE_BROWSE));
		
		// open url (attached to contact)
		Identity[] urls = new Identity[] {
				Identity.createUrl("http://www.google.com"),
				Identity.createUrl("http://www.google.com/postpone"),
				Identity.createUrl("http://www.google.com/fail")
		};
		Assert.assertEquals("www.google.com", urls[0].getName());
		
		model.launch(urls[0].getChannel(Channel.TYPE_BROWSE));
		
		try
		{
			model.launch(urls[1].getChannel(Channel.TYPE_BROWSE));
			
			Assert.fail("Expected launch exception");
		}
		catch (LaunchException e)
		{
			Assert.assertEquals(LaunchException.TYPE_LAUNCH_POSTPONED, e.getType());
		}	
		try
		{
			model.launch(urls[2].getChannel(Channel.TYPE_BROWSE));
			
			Assert.fail("Expected launch exception");
		}
		catch (LaunchException e)
		{
			Assert.assertEquals(LaunchException.TYPE_LAUNCH_FAILED, e.getType());
		}

		// open url
		model.launch("http://www.wikipedia.org");
		
		Assert.assertEquals("Context", new Event[] { 
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "http://www.facebook.com/contact.me"),
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "http://www.google.com"),
				new Event(Event.Context.TEST, MockMIDlet.EVENT_PLATFORM_REQUEST, "http://www.wikipedia.org")
		}, context.getEvents());
		
		model.exit(false);
	}

	public void testInitialAvailability() throws Exception
	{
		setUpAndStart();
		
		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);
		{
		Identity[] imIds = contact.getIdentities(Identity.TYPE_IM_ACCOUNT);
		
		Assert.assertEquals(Channel.PRESENCE_ONLINE, contact.getNowPlusPresence());

		Assert.assertEquals("nowplus", imIds[0].getNetworkId());
		Assert.assertEquals(Identity.NOWPLUS_ACCOUNT_LABEL, imIds[0].getName());
		Assert.assertEquals(Channel.PRESENCE_ONLINE, imIds[0].getChannel(Channel.TYPE_CHAT).getPresence());

		Assert.assertEquals("skype", imIds[1].getNetworkId());
		Assert.assertEquals("contact_me", imIds[1].getName());
		Assert.assertEquals(Channel.PRESENCE_ONLINE, imIds[1].getChannel(Channel.TYPE_CHAT).getPresence());

		Assert.assertEquals("microsoft", imIds[2].getNetworkId());
		Assert.assertEquals("contact.me@example.com", imIds[2].getName());
		Assert.assertEquals(0, imIds[2].getChannel(Channel.TYPE_CHAT).getPresence());
		}
		
		model.exit(false);
	}

	public void testChangedAvailability() throws Exception
	{
		setUpAndStart();

		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
				
		// change via Now+ user id
		{
			Hashtable presenceTable0 = new Hashtable();
			presenceTable0.put("pc", "invisible");
			
			Hashtable presenceTable1 = new Hashtable();
			presenceTable1.put("pc", "offline");
			presenceTable1.put("skype", "online");
			
			Hashtable ht = new Hashtable();
			ht.put("80002", presenceTable0);
			ht.put("80001", presenceTable1);

			Presence presences = new Presence(ht);
			
			protocol.mockPresenceChangeReceived(1, presences);
		}
		
		MyProfile me = model.getMe();
				
		Assert.assertEquals(Channel.PRESENCE_INVISIBLE, me.getNowPlusPresence());
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);
		{
			Identity[] imIds = contact.getIdentities(Identity.TYPE_IM_ACCOUNT);
			
			Assert.assertEquals(Channel.PRESENCE_ONLINE, contact.getNowPlusPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[3].getNetworkId(), imIds[0].getNetworkId());
			Assert.assertEquals(Identity.NOWPLUS_ACCOUNT_LABEL, imIds[0].getName());
			Assert.assertEquals(Channel.PRESENCE_OFFLINE, imIds[0].getChannel(Channel.TYPE_CHAT).getPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[0].getNetworkId(), imIds[1].getNetworkId());
			Assert.assertEquals("contact_me", imIds[1].getName());
			Assert.assertEquals(Channel.PRESENCE_ONLINE, imIds[1].getChannel(Channel.TYPE_CHAT).getPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[1].getNetworkId(), imIds[2].getNetworkId());
			Assert.assertEquals("contact.me@example.com", imIds[2].getName());
			Assert.assertEquals(0, imIds[2].getChannel(Channel.TYPE_CHAT).getPresence());
		}
		contact.unload();

		// change via third party chat id
		{
			Hashtable presenceTable = new Hashtable();
			presenceTable.put("microsoft", "invisible");
			
			Hashtable ht = new Hashtable();
			ht.put("microsoft::contact.me@example.com", presenceTable);
			
			Presence presences = new Presence(ht);
			
			protocol.mockPresenceChangeReceived(2, presences);
		}
		
		contact.load(true);
		{
			Identity[] imIds = contact.getIdentities(Identity.TYPE_IM_ACCOUNT);
			
			Assert.assertEquals(Channel.PRESENCE_ONLINE, contact.getNowPlusPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[3].getNetworkId(), imIds[0].getNetworkId());
			Assert.assertEquals(Identity.NOWPLUS_ACCOUNT_LABEL, imIds[0].getName());
			Assert.assertEquals(Channel.PRESENCE_OFFLINE, imIds[0].getChannel(Channel.TYPE_CHAT).getPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[0].getNetworkId(), imIds[1].getNetworkId());
			Assert.assertEquals("contact_me", imIds[1].getName());
			Assert.assertEquals(Channel.PRESENCE_ONLINE, imIds[1].getChannel(Channel.TYPE_CHAT).getPresence());
	
			Assert.assertEquals(EXTERNAL_NETWORKS[1].getNetworkId(), imIds[2].getNetworkId());
			Assert.assertEquals("contact.me@example.com", imIds[2].getName());
			Assert.assertEquals(Channel.PRESENCE_INVISIBLE, imIds[2].getChannel(Channel.TYPE_CHAT).getPresence());
		}
		contact.unload();

		model.exit(false);
	}

	public void testNowPlusChat() throws Exception
	{
		setUpAndStart();

		protocol.resetEvents();
		
		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();

		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);
		
		// open channel
		Channel channel = contact.getPrimaryChatChannel();

		model.launch(channel);
		
		Assert.assertEquals(Identity.NOWPLUS_ACCOUNT_LABEL, channel.getName());
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals(new Channel[] {channel}, contact.getOngoingChats());
		
		Assert.assertEquals("Protocol", new Event[] {
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "null:80001")
		}, protocol.getEvents());

		// try to open open channel
		try
		{
			model.launch(channel);
			
			Assert.fail("Expected launch exception");
		}
		catch (LaunchException e)
		{
			Assert.assertEquals(LaunchException.TYPE_CHANNEL_ALREADY_OPEN, e.getType());
		}

		// receive conversation id		
		{
			Vector tos = new Vector();
			tos.addElement("80001");
			
			Hashtable ht = new Hashtable();
			ht.put("tos", tos);
			ht.put("conversation", "30303");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(3, new ServiceObject[] {serviceObject}, ServiceObject.START_CHAT_CONVERSATION);
		}
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals("30303", channel.getConversationId());
				
		// send chat messages		
		Message message1 = new Message("Hoi!", 1240000000001L, true);
		model.sendChatMessage(channel, message1);
		
		Assert.assertEquals("Protocol", new Event[]	{
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "null:80001"),
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_CHAT_MESSAGE, "null:80001:30303:Hoi!")
		}, protocol.getEvents());

		Message message2 = new Message("Hi!", 1240000000002L, true);
		model.sendChatMessage(channel, message2);

		Assert.assertEquals(new Message[] {
			new Message("Hoi!", 1240000000001L, true),
			new Message("Hi!", 1240000000002L, true)
		}, channel.getMessages());
		
		// receive chat messages
		{
			Hashtable ht = new Hashtable();
			ht.put("from", "80001");
			ht.put("conversation", "30303");
			ht.put("body", "Hiya!");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(4, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
		}
		Object data6 = getEvents()[6].getData();
		Object data8 = getEvents()[8].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(2)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null),		
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data6),
				new Event(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data8)
		}, getEvents());
		
		Assert.assertEquals("30303", channel.getConversationId());

		Message[] messages = channel.getMessages();
		
		long received = messages[2].getTime();
		
		Assert.assertEquals(new Message[] {
			new Message("Hoi!", 1240000000001L, true),
			new Message("Hi!", 1240000000002L, true),
			new Message("Hiya!", received, false)
		}, messages);	
		
		Assert.assertEquals(true, messages[1].isFromMe());
		Assert.assertEquals(false, messages[2].isFromMe());

		// receive invalid message, not passed on
		{
			Hashtable ht = new Hashtable();
			ht.put("from", "8ooo1");
			ht.put("conversation", "30303");
			ht.put("body", "Yo!");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(5, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
		}

		data6 = getEvents()[6].getData();
		data8 = getEvents()[8].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(2)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null),		
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data6),
				new Event(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data8)
		}, getEvents());
		
		// close channel
		model.close(channel);
		
		Assert.assertEquals(false, channel.isOpen());
		Assert.assertEquals(new Channel[0], contact.getOngoingChats());

		Assert.assertEquals("Protocol", new Event[]	{
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "null:80001"),
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_CHAT_MESSAGE, "null:80001:30303:Hoi!"),
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_CHAT_MESSAGE, "null:80001:30303:Hi!"),
			new Event(Event.Context.TEST, MockCommunicationManager.STOP_CONVERSATION, "null:80001:30303")
		}, protocol.getEvents());
	
		model.exit(false);
	}
	
	public void testNowPlusChatReceived() throws Exception
	{
		setUpAndStart();
		
		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);

		// receive chat messages
		{
			Hashtable ht = new Hashtable();
			ht.put("from", "80001");
			ht.put("conversation", "50505");
			ht.put("body", "Hallo!");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(5, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
			
			Thread.sleep(100);
		}
		
		Event[] events = getEvents();
		
		Channel channel = (Channel) events[6].getData();
		
		Object data7 = events[7].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(2)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null),
				new Event(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data7)
		}, events);

		Assert.assertEquals(contact, channel.getProfile());
		Assert.assertEquals(Identity.NOWPLUS_ACCOUNT_LABEL, channel.getName());
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals("50505", channel.getConversationId());
		
		Message[] messages = channel.getMessages();
		
		long received = messages[0].getTime();
		
		Assert.assertEquals(new Message[] {
			new Message("Hallo!", received, false)
		}, messages);			
		
		model.exit(false);
	}
	
	public void testThirdPartyChat() throws Exception
	{
		setUpAndStart();

		protocol.resetEvents();
		
		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);

		Identity[] imIds = contact.getIdentities(Identity.TYPE_IM_ACCOUNT);
		
//		// open offline channel
//		Channel channel = imIds[2].getChannel(Channel.TYPE_CHAT);
//		
//		try
//		{
//			model.launch(channel);
//			
//			Assert.fail("Expected launch exception");
//		}
//		catch (LaunchException e)
//		{
//			Assert.assertEquals(LaunchException.TYPE_CHANNEL_NOT_ONLINE, e.getType());
//		}
		
		// open channel
		Channel channel = imIds[1].getChannel(Channel.TYPE_CHAT);
		
		model.launch(channel);
		
		Assert.assertEquals("contact_me", channel.getName());
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals(new Channel[] {channel}, contact.getOngoingChats());

		Assert.assertEquals("Protocol", new Event[] {
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "skype:contact_me")
		}, protocol.getEvents());
		
		// try to send message before channel is established (no longer an exception)
//		try
//		{
//			Message msg = new Message("Are you there yet?", 1240000000001L, true);
//			model.sendChatMessage(channel, msg);
//			
//			Assert.fail("Expected launch exception");
//		}
//		catch (LaunchException e)
//		{
//			Assert.assertEquals(LaunchException.TYPE_LAUNCH_FAILED, e.getType());
//		}	
		
		// receive conversation id		
		{
			Vector tos = new Vector();
			tos.addElement("skype::contact_me");
			
			Hashtable ht = new Hashtable();
			ht.put("tos", tos);
			ht.put("conversation", "40404");
			
			ChatObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(3, new ServiceObject[] {serviceObject}, ServiceObject.START_CHAT_CONVERSATION);
			
			Thread.sleep(100);
		}	
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals("40404", channel.getConversationId());
		
		// send chat messages		
		Message message1 = new Message("Hoi!", 1240000000001L, true);
		model.sendChatMessage(channel, message1);
		
		Assert.assertEquals("Protocol", new Event[] {
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "skype:contact_me"),
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_CHAT_MESSAGE, "skype:contact_me:40404:Hoi!"),
		}, protocol.getEvents());	
		
		// receive chat messages
		{
			Hashtable ht = new Hashtable();
			ht.put("from", "skype::contact_me");
			ht.put("conversation", "40404");
			ht.put("body", "Hiya!");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(5, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
			
			Thread.sleep(100);
		}
		
		Object data7 = getEvents()[7].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(2)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null),
				new Event(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data7)
		}, getEvents());
			
		Assert.assertEquals("40404", channel.getConversationId());
			
		Message[] messages = channel.getMessages();
		
		long received = messages[1].getTime();
		
		Assert.assertEquals(new Message[] {
			new Message("Hoi!", 1240000000001L, true),
			new Message("Hiya!", received, false)
		}, messages);		
		
		// try to send message when channel gone offline (no longer an exception)
//		channel.setPresence(Channel.PRESENCE_OFFLINE);
//		
//		try
//		{
//			Message msg = new Message("Are you there?", 1240000000001L, true);
//			model.sendChatMessage(channel, msg);
//			
//			Assert.fail("Expected launch exception");
//		}
//		catch (LaunchException e)
//		{
//			Assert.assertEquals(LaunchException.TYPE_CHANNEL_NOT_ONLINE, e.getType());
//		}	
		
		// close channel
		model.close(channel);
		
		Assert.assertEquals(false, channel.isOpen());
		Assert.assertEquals(new Channel[0], contact.getOngoingChats());

		Assert.assertEquals("Protocol", new Event[] {
			new Event(Event.Context.TEST, MockCommunicationManager.START_CONVERSATION, "skype:contact_me"),
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_CHAT_MESSAGE, "skype:contact_me:40404:Hoi!"),
			new Event(Event.Context.TEST, MockCommunicationManager.STOP_CONVERSATION, "skype:contact_me:40404")
		}, protocol.getEvents());
		
		model.exit(false);
	}

	public void testThirdyPartyChatReceived() throws Exception
	{
		setUpAndStart();
		
		// contacts list is shown
		Object[] contacts = model.getContacts(null, 20).getEntries();
		
		ContactProfile contact = (ContactProfile) contacts[0];
		contact.load(true);
		
		// receive chat messages
		{
			Hashtable ht = new Hashtable();
			ht.put("from", "skype::contact_me");
			ht.put("conversation", "60606");
			ht.put("body", "Hello!");
			
			ServiceObject serviceObject = new ChatObject(ht);
			
			protocol.mockItemsReceived(6, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
			
			Thread.sleep(100);
		}
		
		Event[] events = getEvents();
		
		Channel channel = (Channel) events[6].getData();
		
		Object data7 = events[7].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.UPDATE, new Long(2)),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null),
				new Event(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel),
				new Event(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, data7)
		}, events);

		Assert.assertEquals(contact, channel.getProfile());
		Assert.assertEquals("contact_me", channel.getName());
		Assert.assertEquals(true, channel.isOpen());
		Assert.assertEquals("60606", channel.getConversationId());
		
		Message[] messages = channel.getMessages();
		
		long received = messages[0].getTime();
		
		Assert.assertEquals(new Message[] {
			new Message("Hello!", received, false)
		}, messages);	
		
		model.exit(false);
	}
	
	public void testMoreThan18MessagesReceived() throws Exception
	{
//		setUpAndStart();
//		
//		// contacts list is shown
//		Object[] contacts = model.getContacts(null, 20).getEntries();
//		
//		ContactProfile contact = (ContactProfile) contacts[0];
//		contact.load(true);
//		
//		// receive chat messages
//		for (int i = 0; i < 20; i++)
//		{
//			Hashtable ht = new Hashtable();
//			ht.put("from", "skype::contact_me");
//			ht.put("conversation", "60606");
//			ht.put("body", "Message " + (i+1));
//			
//			ServiceObject serviceObject = new ChatObject(ht);
//			
//			protocol.mockItemsReceived(6 + i, new ServiceObject[] {serviceObject}, ServiceObject.CHAT_MESSAGE);
//			
//			Thread.sleep(100);
//		}
//
//		Channel channel = (Channel) getEvents()[4].getData();
//		
//		Message[] messages = channel.getMessages();
//
//		Assert.assertEquals(18, messages.length);
//		Assert.assertEquals("Message 3", messages[0].getText());
//		Assert.assertEquals("Message 20", messages[17].getText());
//		
//		model.exit(false);
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
