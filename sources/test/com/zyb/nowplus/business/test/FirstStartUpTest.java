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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.AuthenticationListener;
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

public class FirstStartUpTest extends TestCase implements EventListener
{
	private static final String CURRENT_VERSION = "1.0.1.0";

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
	
	public void testUserDisallowedUpgradeCheck() throws Exception
	{			
		// application started
		context.mockApplicationStarted();

		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION)
		}, protocol.getEvents());
		
		// user disallows connection
		protocol.mockUserDisallowedConnection();
		
		// 'user disallowed connection' event fired
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.USER_DISALLOWED_CONNECTION, null)
		}, getEvents());
		
		// user can only exit
		model.exit(false);		
	}
	
	public void testUpgradeAvailable() throws Exception
	{
		// application started
		context.mockApplicationStarted();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION)
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
		
		Object data = getEvents()[2].getData();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.APP, Event.App.MANDATORY_UPDATE_RECEIVED, data)
		}, getEvents());
		
		Assert.assertEquals("Info", new String[] {"update.title", "update.message", "update.version", "update.url"}, (String[]) data);
		
		// user exits
		model.exit(false);
	}
	
	public void testSignUp() throws Exception
	{
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
		}, getEvents());
		
		// user chooses sign up
		model.signup("user", "password", "phonenumber","email1@test.com", new Date(87909628535L), true);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.REGISTER_USER, 
						"user:password:null:1972-10-14T00:00:00:phonenumber:true:null:email1@test.com:null:en-US:0:0:false")
		}, protocol.getEvents());
		
		// sms not arrived
		protocol.mockRegistrationFailed(AuthenticationListener.ACTIVATION_TIMED_OUT);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED_WRONG_MSISDN, null)
		}, getEvents());
		
		// user corrects phonenumber
		model.rerequestConfirmationSMS("phonenumber2");
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.REGISTER_USER, 
						"user:password:null:1972-10-14T00:00:00:phonenumber:true:null:email1@test.com:null:en-US:0:0:false"),
				new Event(Event.Context.TEST, MockCommunicationManager.REREQUEST_ACTIVATION_CODE, "user:phonenumber2")
		}, protocol.getEvents());
	
		// registration failed
		protocol.mockRegistrationFailed(AuthenticationListener.USERNAME_IN_USE);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED_WRONG_MSISDN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED, "Username is already taken")
		}, getEvents());	
		
		// user tries again
		model.signup("user2", "password", "phonenumber2","email2@test.com",new Date(87909628535L), true);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.REGISTER_USER, 
						"user:password:null:1972-10-14T00:00:00:phonenumber:true:null:email1@test.com:null:en-US:0:0:false"),
				new Event(Event.Context.TEST, MockCommunicationManager.REREQUEST_ACTIVATION_CODE, "user:phonenumber2"),
				new Event(Event.Context.TEST, MockCommunicationManager.REGISTER_USER, 
				"user2:password:null:1972-10-14T00:00:00:phonenumber2:true:null:email2@test.com:null:en-US:0:0:false")
		}, protocol.getEvents());
		
		// registration succeeds
		protocol.mockRegistrationSucceeded();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED_WRONG_MSISDN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED, "Username is already taken"),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_SUCCEEDED, null),
		}, getEvents());
		
		// user exits
		model.exit(false);
	}

	public void testSignIn() throws Exception
	{
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);

		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
		}, getEvents());
		
		// user chooses sign in
		Assert.assertNull(model.getUserName());
		Assert.assertEquals(true, model.stayLoggedIn());
		
		model.login("user", "password", false);

		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
		}, protocol.getEvents());
		
		// wrong password
		protocol.mockAuthenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);

		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_FAILED, null)
		}, getEvents());		
		
		// user tries again
		Assert.assertEquals("user", model.getUserName());
		Assert.assertEquals(false, model.stayLoggedIn());
		
		model.login("user", "password2", true);

		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password2")
		}, protocol.getEvents());		
		
		// correct password
		protocol.mockAuthenticationSucceeded();
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password2"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES)
		}, protocol.getEvents());
		
		// request times out
		protocol.mockErrorReceived(1, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.AVAILABLE_IDENTITY);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password2"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
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
			protocol.mockItemsReceived(2, serviceObjects, ServiceObject.AVAILABLE_IDENTITY);
		}
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password2"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS)
		}, protocol.getEvents());
		
		// request times out
		protocol.mockErrorReceived(3, ResponseListener.REQUEST_TIMED_OUT, ServiceObject.GROUPS);
		
		Assert.assertEquals("Protocol", new Event[] {
				new Event(Event.Context.TEST, MockCommunicationManager.CHECK_UPGRADE, CURRENT_VERSION),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password"),
				new Event(Event.Context.TEST, MockCommunicationManager.AUTHENTICATE, "user:password2"),
				new Event(Event.Context.TEST, MockCommunicationManager.START_CONNECTIONS, null),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.IDENTITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.GROUPS)
		}, protocol.getEvents());
		
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
			protocol.mockItemsReceived(4, serviceObjects, ServiceObject.GROUPS);
		}
		
		// sync
		Assert.assertEquals(new Event[] {
				new Event(Event.Context.TEST, MockSyncManager.SYNC, "1:1"),	
//				new Event(Event.Context.TEST, MockSyncManager.SYNC, "3:1")
		},  syncManager.getEvents());
		
		syncManager.mockSyncFinished();
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN, null),
				new Event(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_FAILED, null),
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
		
		Assert.assertEquals("Settings", "user", settingsStore.getStringValue(Settings.KEY_USER_NAME));
		Assert.assertEquals("Settings", true, settingsStore.getBooleanValue(Settings.KEY_STAY_LOGGED_IN));
		
		// user exits
		model.exit(false);
	}
	
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
