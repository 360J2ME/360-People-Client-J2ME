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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
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

public class NextStartUpTest extends TestCase implements EventListener
{
	private static final String CURRENT_VERSION = "1.0.1.0";

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
		GROUPS[0] = new Group(1, Group.TYPE_CUSTOM, "Family");
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
	
	private void setUpCurrentInstallation(boolean newSim, boolean autoLogin) throws Exception
	{	
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, autoLogin);
		settingsStore.setStringValue(Settings.KEY_IMSI, newSim ? "oldsim" : "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
	}
	
	public void testSoftwareUpgraded() throws Exception
	{
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.0.0");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		
		// application started
		context.mockApplicationStarted();
		
		Assert.assertEquals(new Event[] {
				new Event(Event.Context.TEST, MockMIDlet.EVENT_RMS_DELETED, null)
		}, context.getEvents());
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		Assert.assertEquals(new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null)
		}, getEvents());
	}
	
	public void testUserDisallowedUpgradeCheck() throws Exception
	{		
		setUpCurrentInstallation(false, true);
		
		// application started
		context.mockApplicationStarted();

		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES)				
		}, protocol.getEvents());
		
		// user disallows connection
		protocol.mockUserDisallowedConnection();
		
		Thread.sleep(500);
		
		// 'user disallowed connection' event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.USER_DISALLOWED_CONNECTION, null)
		}, getEvents());
		
		// user can only exit
		model.exit(false);		
	}
	
	public void testUpgradeAvailable() throws Exception
	{
		setUpCurrentInstallation(false, true);
		
		// application started
		context.mockApplicationStarted();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES)				
		}, protocol.getEvents());
		
		// upgrade info received
		Hashtable ht = new Hashtable();
		ht.put("title", "update.title");
		ht.put("message", "update.message");
		ht.put("version", "update.version");
		ht.put("url", "update.url");
		ht.put("force", Boolean.TRUE);
		
		com.zyb.nowplus.data.protocol.types.Update update = new com.zyb.nowplus.data.protocol.types.Update(ht); 
		
		protocol.mockUpgradeInfoReceived(update);
		
		Object data = getEvents()[4].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.APP, Event.App.MANDATORY_UPDATE_RECEIVED, data)
		}, getEvents());
		
		Assert.assertEquals("Info", new String[] {"update.title", "update.message", "update.version", "update.url"}, (String[]) data);
		
		// user exits
		model.exit(false);
	}
	
	public void testNextLoginFailed() throws Exception
	{
		setUpCurrentInstallation(false, false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
				
		Assert.assertEquals(new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null)
		}, getEvents());
		
		// user logs in
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(false, model.stayLoggedIn());
		
		model.login("user", "wrongpassword", true);
				
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:wrongpassword")
		}, protocol.getEvents());
		
		// wrong password
		protocol.mockAuthenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_FAILED, null)
		}, getEvents());
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", false, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));

		// user exits
		model.exit(false);
	}
	
	public void testNextLoginSucceeded() throws Exception
	{
		setUpCurrentInstallation(false, false);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		Assert.assertEquals(new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
		}, getEvents());
		
		// user logs in
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(false, model.stayLoggedIn());
		
		model.login("user", "password", true);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
		}, protocol.getEvents());
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));
	
		// user exits
		model.exit(false);
	}	
	
	public void testAutoLogin() throws Exception
	{
		setUpCurrentInstallation(false, true);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
		}, getEvents());
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.MY_CHANGES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.CONTACTS_CHANGES),
		}, protocol.getEvents());

		model.exit(false);
	}
			
	/*
	public void testNextLoginWithNewUsername() throws Exception
	{
		setUpCurrentInstallation(false, false, true);
		
		// application started
		context.mockApplicationStarted();
		protocol.mockUpgradeInfoReceived(null);

		Assert.assertEquals(new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
		}, getEvents());
		
		// user chooses log in
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(false, model.stayLoggedIn());
		
		model.login("newuser", "newpassword", true);
		
		Thread.sleep(100);

		Assert.assertEquals(new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.CONFIRM_USER_NAME_CHANGE, null),
		}, getEvents());
		
		// user confirms new user name
		Assert.assertEquals("user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals(false, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));

		Assert.assertEquals("newuser", model.getUserName());
		Assert.assertEquals(false, model.stayLoggedIn());

		model.login("newuser", "newpassword", true);
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "newuser:newpassword"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES)
		}, protocol.getEvents());
		
		// receive networks
		{
			ServiceObject[] serviceObjects = new ServiceObject[2];
			{
				Hashtable ht = new Hashtable();
				ht.put("pluginid", "plugin1");
				ht.put("network", "skype");
				ht.put("name", "Skype");
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
			}
			{
				Hashtable ht = new Hashtable();
				ht.put("pluginid", "plugin2");
				ht.put("network", "facebook");
				ht.put("name", "Facebook");
				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Identity(ht);
			}
			protocol.mockItemsReceived(1, serviceObjects, ServiceObject.AVAILABLE_IDENTITY);
		}
		
		// receive groups
		{
			ServiceObject[] serviceObjects = new ServiceObject[3];
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(1));
				ht.put("name", "Family");
				ht.put("isreadonly", Boolean.FALSE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(2));
				ht.put("name", "My Group");
				ht.put("isreadonly", Boolean.FALSE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			{
				Hashtable ht = new Hashtable();
				ht.put("id", new Long(3));
				ht.put("name", "Facebook");
				ht.put("isreadonly", Boolean.TRUE);
				ht.put("issystemgroup", Boolean.FALSE);
				serviceObjects[2] = new com.zyb.nowplus.data.protocol.types.Group(ht);
			}
			protocol.mockItemsReceived(2, serviceObjects, ServiceObject.GROUPS);
		}
		
		Thread.sleep(100);
		
		// sync
		Assert.assertEquals(new Event[] {
			new Event(Event.Context.TEST, MockSyncManager.SYNC, "1:1"),	
			new Event(Event.Context.TEST, MockSyncManager.SYNC, "3:2")
		},  syncManager.getEvents());
		
		syncManager.mockSyncFinished();
		
		Thread.sleep(100);
		
		// add social networks
		model.finishAddingSocialNetworkAccounts();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Context", new Event[] {
				new Event(Event.Context.TEST, MockMIDlet.EVENT_RMS_DELETED, null)
		}, context.getEvents());
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.CONFIRM_USER_NAME_CHANGE, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.SYNC, Event.Sync.SUCCESSFULL, null),
				new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED, null),
				new Event(Event.Context.APP, Event.App.READY, null)
		}, getEvents());
		
		Assert.assertEquals("Settings", "newuser", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));
		Assert.assertEquals("Settings", EXTERNAL_NETWORKS, settingsStore.getExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS));
		Assert.assertEquals("Settings", GROUPS, settingsStore.getGroupArrayValue(Settings.KEY_GROUPS));
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "newuser:newpassword"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());

		model.exit(false);
	}
	*/
	
	/*
	public void testReauthenticationRequired() throws Exception
	{
		setUpCurrentInstallation(false, true, true);

		// application started
		context.mockApplicationStarted();
		protocol.mockUpgradeInfoReceived(null);
		
		// authentication failed
		protocol.mockAuthenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED, null)
		}, getEvents());
		
		// user logs in again
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(true, model.stayLoggedIn());

		model.login("user", "password", false);
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		Thread.sleep(100);
		
		Assert.assertEquals("Context", new Event[0], context.getEvents());
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED, null),
				new Event(Event.Context.APP, Event.App.READY, null)
		}, getEvents());
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", false, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());

		model.exit(false);
	}
	*/
	
	/*
	public void testReauthenticationWithNewUsername() throws Exception
	{
		setUpCurrentInstallation(false, true, true);

		// application started
		context.mockApplicationStarted();
		protocol.mockUpgradeInfoReceived(null);

		// authentication failed
		protocol.mockAuthenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
		
		Thread.sleep(100);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED, null)
		}, getEvents());
		
		// user logs in again
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(true, model.stayLoggedIn());

		model.login("newuser", "newpassword", false);
				
		Thread.sleep(100);
		
		Assert.assertEquals("Context", new Event[0], context.getEvents());
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.USER_NAME_CHANGE_NOT_ALLOWED, null)
		}, getEvents());
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		Assert.assertEquals("Settings", "mysim", settingsStore.getStringValue(Settings.KEY_IMSI));
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());
		
		model.exit(false);		
	}
	*/
	
	public void tearDown()
	{		
		model.detach(this);
		
		syncManager = null;
		settingsStore = null;
		contactsStore = null;
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
