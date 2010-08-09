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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.storage.test.MockDataStore;
import com.zyb.nowplus.data.storage.test.MockKeyValueStore;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.MockMIDlet;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class NetworkTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[3];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin1", "skype", "Skype", null);
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
		protocol = new MockCommunicationManager(20, new byte[] {ServiceObject.ACTIVITY}, new int[] {ServerRequest.GET}, new int[] {ServerRequest.ACTIVITIES});
		emailProtocol = new MockEmailCommunicationManager();
		
		context = new MockMIDlet(settingsStore, contactsStore, new MockDataStore(), new MockDataStore(), 
				syncManager, contentSinkAndSource, contentSinkAndSource, protocol, emailProtocol, eventDispatcher, 20);
	   
		model = context.getModel();
	    model.attach(this);
	}
	
	private void setUpCurrentInstallation(boolean roamingAllowed) throws Exception
	{	
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, true);
		settingsStore.setStringValue(Settings.KEY_IMSI, "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		settingsStore.setBooleanValue(Settings.KEY_ROAMING_ALLOWED, roamingAllowed);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 7);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 44);
	}
	
	public void testRoamingAllowed() throws Exception
	{
		setUpCurrentInstallation(true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		// network goes roaming, no warning
		protocol.mockRoaming();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		model.exit(false);
	}
	
	public void testRoamingWarning() throws Exception
	{
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		// network goes roaming, warning
		protocol.mockRoaming();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_ROAMING, null)
		}, getEvents());
		
		model.exit(false);
	}
	
	public void testRoamingChange() throws Exception
	{
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		Assert.assertEquals("Settings", false, settingsStore.getBooleanValue(Settings.KEY_ROAMING_ALLOWED));
		
		// roaming allowed
		model.setRoamingDataConnectionAllowed(true);
		
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_ROAMING_ALLOWED));
		
		// network goes roaming, no warning
		protocol.mockRoaming();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null)
		}, getEvents());
		
		model.exit(false);
	}
	
	public void testOneRoamingWarning() throws Exception 
	{
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		protocol.mockUpgradeInfoReceived(null);
		
		// network goes roaming, warning
		protocol.mockRoaming();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_ROAMING, null)
		}, getEvents());
		
		// network goes roaming again, no second warning
		protocol.mockRoaming();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_ROAMING, null)
		}, getEvents());
		
		model.exit(false);
	}
	
	public void testNetworkUpAndDown() throws Exception
	{
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		protocol.mockUpgradeInfoReceived(null);
		
		// network goes down, warning
		protocol.mockNetworkDown((byte) 0);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_DOWN, null)
		}, getEvents());
			
		// network goes down, ignored
		protocol.mockNetworkDown((byte) 0);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_DOWN, null)
		}, getEvents());
		
		// network comes up, warning
		protocol.mockNetworkUp();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_DOWN, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_UP, null)
		}, getEvents());		
		
		// network comes up, ignored
		protocol.mockNetworkUp();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_DOWN, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_UP, null)
		}, getEvents());
		
		// random error
		protocol.mockErrorReceived(77, ResponseListener.REQUEST_FAILED_NOT_IMPLEMENTED, (byte) 0);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_DOWN, null),
				new Event(Event.Context.APP, Event.App.CONNECTION_UP, null)
		}, getEvents());
	}
	
	public void testDataCounter() throws Exception
	{
		setUpCurrentInstallation(false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		index = 0;
		
		// counter = 0
		Assert.assertEquals(0, model.getDataCounter());
		
		// some traffic
		protocol.mockTraffic(1024);
		
//		Assert.assertEquals("Listener", new Event[] {
//				new Event(Event.Context.APP, Event.App.TRAFFIC, null)
//		}, getEvents());
		
		Assert.assertEquals(1024, model.getDataCounter());
		
		// some more traffic
		protocol.mockTraffic(512);
		
		Assert.assertEquals(1536, model.getDataCounter());
		
		// reset counter
		model.resetDataCounter();
		
		Assert.assertEquals(0, model.getDataCounter());
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
