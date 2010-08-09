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
import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.protocol.types.APIEvent;
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

public class ActivitiesTest extends TestCase implements EventListener
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
	
	private static final String[] TYPES = {
	 "call_dialed",
	 "call_received",
	 "call_missed",
	 "contact_sent_status_update",
	 "contact_received_status_update",
	 "contact_joined",
	 "contact_friend_invitation_sent",
	 "contact_friend_invitation_received",
	 "contact_new_friends",
	 "contact_wall_post_sent",
	 "contact_wall_post_received",
	 "contact_profile_email_updated",
	 "contact_profile_phone_updated",
	 "contact_profile_address_updated",
	 "contact_profile_picture_updated",
	 "store_application_purchased",
	 "sn_added",
	 "sn_added_by_friend",
	 "sn_wall_post_sent",
	 "sn_wall_post_received",
	 "sn_message_sent",
	 "sn_message_received",
	 "sn_photos_posted",
	 "sn_videos_posted",
	 "sn_status_sent",
	 "sn_status_received",
	 "sn_contact_profile_email_updated",
	 "sn_contact_profile_phone_updated",
	 "sn_contact_profile_address_updated",
	 "message_sms_sent",
	 "message_sms_received",
	 "message_mms_sent",
	 "message_mms_received",
	 "message_email_sent",
	 "message_email_received",
	 "share_album_sent",
	 "share_album_received",
	 "share_photo_sent",
	 "share_photo_received",
	 "share_video_sent",
	 "share_video_received",
	 "share_photo_comment_sent",
	 "share_photo_comment_received",
	 "share_photo_multiple_sent",
	 "share_photo_multiple_received",
	 "share_video_multiple_sent",
	 "share_video_multiple_received",
	 "location_sent",
	 "location_received",
	 "location_shared_placemark_created",
	 "location_shared_placemark_received",
	 "location_placemark_created",
	 "location_placemark_received",
	 "music_purchased_song",
	 "music_purchased_album",
	 "music_downloaded_song",
	 "music_downloaded_album",
	 "music_downloaded_playlist",
	 "music_rated_song",
	 "music_rated_album",
	 "music_recommendation_sent_track",
	 "music_recommendation_sent_album",
	 "music_recommendation_sent_playlist",
	 "music_recommendation_sent_track_anon",
	 "music_recommendation_sent_album_anon",
	 "music_recommendation_sent_playlist_anon",
	 "music_recommendation_received_track",
	 "music_recommendation_received_album",
	 "music_recommendation_received_playlist",
	 "music_recommendation_received_track_anon",
	 "music_recommendation_received_album_anon"
	 };
	
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
		
		context = new MockMIDlet(settingsStore, contactsStore, new MockDataStore(), new MockDataStore(), syncManager, 
				contentSinkAndSource, contentSinkAndSource, protocol, emailProtocol, eventDispatcher, 20);
	   
		model = context.getModel();
	    model.attach(this);
	}
	
	private void setUpAndStart() throws Exception
	{	
		// setup settings
		settingsStore.setStringValue(Settings.KEY_STORAGE_VERSION, "1.0.1.3832");
		settingsStore.setStringValue(Settings.KEY_USER_NAME, "user");
		settingsStore.setBooleanValue(Settings.KEY_STAY_LOGGED_IN, true);
		settingsStore.setStringValue(Settings.KEY_IMSI, "mysim");
		settingsStore.setBooleanValue(Settings.KEY_REAUTHENTICATE, false);
		settingsStore.setBooleanValue(Settings.KEY_SN_ACCOUNT_ADDED, true);
		settingsStore.setExternalNetworkArrayValue(Settings.KEY_EXTERNAL_NETWORKS, EXTERNAL_NETWORKS);
		settingsStore.setGroupArrayValue(Settings.KEY_GROUPS, GROUPS);
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 7);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 33);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 44);

		// setup contacts 
		ManagedProfileRecord[] records = new ManagedProfileRecord[1];
		
		records[0] = new ManagedProfileRecord();
		records[0].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		records[0].setCabId(1);
		records[0].setSabId(70001);
		records[0].setUserId(80001);
		records[0].setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
		records[0].setNowPlusPresence(Channel.PRESENCE_ONLINE);
		records[0].setFirstName("Busy");
		records[0].setMiddleNames("");
		records[0].setLastName("Friend");
	
		contactsStore.initialise(records);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
	}
	/*
	public void testReceiveActivities() throws Exception
	{
		setUpAndStart();
		
		protocol.resetEvents();
		
		// request timed out
		protocol.mockErrorReceived(ResponseListener.REQUEST_TIMED_OUT, ServiceObject.ACTIVITY);

		Assert.assertEquals("Protocol", new Event[]	{
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
			}, protocol.getEvents());
		
		// receive activities
		ServiceObject[] serviceObjects = new ServiceObject[3];
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			Hashtable moreinfo = new Hashtable();
			moreinfo.put("network", "facebook");
			
			ht.put("activityid", new Long(301));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Cup of tea");
			ht.put("description", "Having a nice cuppa.");
			ht.put("time", new Long(123456));
			//ht.put("store", "facebook");
			ht.put("moreinfo", moreinfo);
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			
			ht.put("activityid", new Long(302));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Feet");
			ht.put("description", "Putting my feet up.");
			ht.put("time", new Long(1234));
			
			serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			flags.addElement("status");
			
			ht.put("activityid", new Long(303));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Pipe");
			ht.put("description", "Lighting a pipe.");
			ht.put("time", new Long(12345));
			
			serviceObjects[2] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
		
		// display friends stream
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.LIFE_DRIVE_CHANGED, new Integer(2)),
				new Event(Event.Context.ACTIVITY, Event.Activities.FRIENDS_STREAM_CHANGED, new Integer(2))
		}, getEvents());
		
		ListSelection ls = model.getFriendsStream(0, 20);
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(301, activity.getId());
			Assert.assertEquals(Activity.TYPE_CONTACT_RECEIVED_STATUS_UPDATE, activity.getType());
			Assert.assertEquals(EXTERNAL_NETWORKS[1], activity.getSource());
			Assert.assertEquals("Cup of tea", activity.getTitle());
			Assert.assertEquals("Having a nice cuppa.", activity.getDescription());
			Assert.assertEquals(123456000, activity.getTime());
		}
		{
			Activity activity = (Activity) ls.getEntries()[1];
			Assert.assertEquals(303, activity.getId());
		}

		Assert.assertEquals(2, model.getLifeDrive(0, 20).getTotal());

		// receive response to first request
		serviceObjects = new ServiceObject[1];
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(304));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Hello");
			ht.put("description", "Don't ignore me!");
			ht.put("time", new Long(123));
			
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
		
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.LIFE_DRIVE_CHANGED, new Integer(2)),
				new Event(Event.Context.ACTIVITY, Event.Activities.FRIENDS_STREAM_CHANGED, new Integer(2))
		}, getEvents());
		
		Assert.assertEquals(2, model.getLifeDrive(0, 20).getTotal());
		Assert.assertEquals(2, model.getFriendsStream(0, 20).getTotal());
		
		model.exit(false);		
	}
	*/
	public void testReceiveMoreThan50Activities() throws Exception
	{
		setUpAndStart();
		
		// receive activities
		ServiceObject[] serviceObjects = new ServiceObject[71];
		for (int i = 0; i < serviceObjects.length; i++)
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(i));
			ht.put("flaglist", flags);
			ht.put("type", TYPES[i]);
			ht.put("title", "Title");
			ht.put("description", "Description");
			ht.put("time", new Long(i));
			
			serviceObjects[i] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
				
		// only 50 most recent are returned (message_email_received is discarded)
		ListSelection ls = model.getFriendsStream(0, 60);
		
		Assert.assertEquals(50, ls.getTotal());
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(70, activity.getId());
		}
		{
			Activity activity = (Activity) ls.getEntries()[49];
			Assert.assertEquals(20, activity.getId());
		}
		
		model.exit(false);
	}
	
	public void testReceiveActivitiesWithUnknownContacts() throws Exception
	{
		setUpAndStart();
		
		protocol.resetEvents();
		
		// receive activities
		ServiceObject[] serviceObjects = new ServiceObject[2];
		{	
			Vector flags = new Vector();
			flags.addElement("status");
			
			Hashtable contact = new Hashtable();
			contact.put("contactid", new Long(70001));
			
			Vector contactList = new Vector();
			contactList.addElement(contact);
	
			Hashtable ht = new Hashtable();
			ht.put("activityid", new Long(301));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Hi!");
			ht.put("description", "You know me");
			ht.put("time", new Long(123456));
			ht.put("contactlist", contactList);
			
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Vector flags = new Vector();
			flags.addElement("status");
			
			Hashtable contact = new Hashtable();
			contact.put("contactid", new Long(70002));
			
			Vector contactList = new Vector();
			contactList.addElement(contact);
	
			Hashtable ht = new Hashtable();
			ht.put("activityid", new Long(302));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Hi!");
			ht.put("description", "You don't know me");
			ht.put("time", new Long(123456));
			ht.put("contactlist", contactList);
			
			serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
				
		// only one is displayed
		ListSelection ls = model.getFriendsStream(0, 60);
		
		Assert.assertEquals(1, ls.getTotal());
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(301, activity.getId());
		}
		
		// receive new contacts
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.nickname");
			ht.put("val", "lazy.friend");
			detailList.addElement(ht);
		}
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Friend;Lazy;");
			detailList.addElement(ht);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70002));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(34);
		
		protocol.mockItemsReceived(1, new ServiceObject[] {serviceObject}, ServiceObject.CONTACT_CHANGES);
				
		// check activities are requested again
		Assert.assertEquals("Protocol", new Event[]	{
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());
		
		model.exit(false);
	}
	
	public void testReceiveUpdatedFriendStream() throws Exception
	{
		setUpAndStart();
		
		protocol.resetEvents();
		
		// receive activities
		ServiceObject[] serviceObjects = new ServiceObject[2];
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(301));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "1");
			ht.put("description", "No Change.");
			ht.put("time", new Long(12003));
			
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(302));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "2a");
			ht.put("description", "Before change.");
			ht.put("time", new Long(12002));
			
			serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
		
		// receive notification
		protocol.mockPushReceived(new APIEvent(APIEvent.STATUS_CHANGE));
				
		Assert.assertEquals("Protocol", new Event[]	{
			new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());
		
		// receive new and updated activities
		serviceObjects = new ServiceObject[2];
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(302));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "2b");
			ht.put("description", "After change.");
			ht.put("time", new Long(12002));
			
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("status");
			
			ht.put("activityid", new Long(303));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "3");
			ht.put("description", "Added.");
			ht.put("time", new Long(12001));
			
			serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);		
		
		// display life drive
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.STATUS_STREAM_CHANGED, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.STATUS_STREAM_CHANGED, null)
		}, getEvents());
		
		ListSelection ls = model.getFriendsStream(0, 20);
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(301, activity.getId());
		}
		{
			Activity activity = (Activity) ls.getEntries()[1];
			Assert.assertEquals(302, activity.getId());
			Assert.assertEquals("2b", activity.getTitle());
			Assert.assertEquals("After change.", activity.getDescription());
		}
		{
			Activity activity = (Activity) ls.getEntries()[2];
			Assert.assertEquals(303, activity.getId());
		}
		
		model.exit(false);				
	}
	/*
	public void testReceiveUpdatedLifeDrive() throws Exception
	{
		setUpAndStart();
		
		protocol.resetEvents();
		
		// receive push (necessary, because client doesn't request life drive at start up anymore)
		protocol.mockPushReceived(new APIEvent(APIEvent.TIMELINE_CHANGE));
		
		// activities requested
		Assert.assertEquals("Protocol", new Event[]	{
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
		}, protocol.getEvents());	
		
		// request timed out
		protocol.mockErrorReceived(ResponseListener.REQUEST_TIMED_OUT, ServiceObject.ACTIVITY);
		
		Assert.assertEquals("Protocol", new Event[]	{
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES),
				new Event(Event.Context.TEST, MockCommunicationManager.SEND_REQUEST, ServerRequest.GET + ":" + ServerRequest.ACTIVITIES)
			}, protocol.getEvents());
		
		// receive activities
		ServiceObject[] serviceObjects = new ServiceObject[4];
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			
			Hashtable contact = new Hashtable();
			contact.put("contactid", new Long(70001));
			
			Vector contactList = new Vector();
			contactList.addElement(contact);
			
			ht.put("activityid", new Long(301));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Cup of tea");
			ht.put("description", "Having a nice cuppa.");
			ht.put("time", new Long(123456));
			ht.put("contactlist", contactList);
			
			serviceObjects[0] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			
			ht.put("activityid", new Long(302));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Feet");
			ht.put("description", "Putting my feet up.");
			ht.put("time", new Long(1234));
			
			serviceObjects[1] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			
			ht.put("activityid", new Long(303));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "Pipe");
			ht.put("description", "Lighting a pipe.");
			ht.put("time", new Long(12345));
			
			serviceObjects[2] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		{	
			Hashtable ht = new Hashtable();
			
			Vector flags = new Vector();
			flags.addElement("timeline");
			
			ht.put("activityid", new Long(304));
			ht.put("flaglist", flags);
			ht.put("type", "contact_received_status_update");
			ht.put("title", "No description");
			ht.put("description", " ");
			ht.put("time", new Long(123456));
			
			serviceObjects[3] = new com.zyb.nowplus.data.protocol.types.Activity(ht);
		}
		protocol.mockItemsReceived(serviceObjects, ServiceObject.ACTIVITY);
		
		// display life drive
		Assert.assertEquals("Listener", new Event[] {
				new Event(Event.Context.APP, Event.App.START, null),
				new Event(Event.Context.CONTACTS, Event.Contacts.REFRESH_LIST, null),
				new Event(Event.Context.APP, Event.App.READY, null),
				new Event(Event.Context.ACTIVITY, Event.Activities.LIFE_DRIVE_CHANGED, new Integer(3)),
		}, getEvents());
		
		ListSelection ls = model.getLifeDrive(0, 20);
		Assert.assertEquals(3, ls.getTotal());
		{
			Activity activity = (Activity) ls.getEntries()[0];
			Assert.assertEquals(301, activity.getId());
			Assert.assertEquals(Activity.TYPE_CONTACT_RECEIVED_STATUS_UPDATE, activity.getType());
			Assert.assertEquals(model.getContacts(null, 20).getEntries(), activity.getInvolvedContacts());
			Assert.assertEquals("Cup of tea", activity.getTitle());
			Assert.assertEquals("Having a nice cuppa.", activity.getDescription());
			Assert.assertEquals(123456000, activity.getTime());
		}
		{
			Activity activity = (Activity) ls.getEntries()[1];
			Assert.assertEquals(303, activity.getId());
		}
		{
			Activity activity = (Activity) ls.getEntries()[2];
			Assert.assertEquals(302, activity.getId());
		}

		Assert.assertEquals(0, model.getFriendsStream(0, 20).getTotal());
		
		model.exit(false);
	}
	*/
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
