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

import java.util.Random;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.content.test.MockSinkAndSource;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.orders.LastFirstOrder;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.test.MockSyncManager;
import com.zyb.nowplus.data.email.test.MockEmailCommunicationManager;
import com.zyb.nowplus.data.protocol.test.MockCommunicationManager;
import com.zyb.nowplus.data.storage.test.MockDataStore;
import com.zyb.nowplus.data.storage.test.MockKeyValueStore;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.MockMIDlet;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.Collator;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

public class SortPerfTest extends TestCase implements EventListener
{
	private static final ExternalNetwork[] EXTERNAL_NETWORKS = new ExternalNetwork[2];
	static
	{
		EXTERNAL_NETWORKS[0] = new ExternalNetwork("plugin2", "facebook", "Facebook", null);
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

	private long start;
	
	private static Random RANDOM = new Random();
	
	public void setUp()
	{
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
		settingsStore.setLongValue(Settings.KEY_LATEST_CAB_ID, 500);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_ME, 1);
		settingsStore.setLongValue(Settings.KEY_CURRENT_REV_CONTACTS, 1);

		Collator c = Collator.getInstance();
		c.loadAlphabet("en");
		
		// set up contacts
		ManagedProfileRecord[] records = new ManagedProfileRecord[500];
		
		for (int i = 0; i < records.length; i++)
		{
			records[i] = new ManagedProfileRecord();
			records[i].setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
			records[i].setCabId(1 + i);
			records[i].setNowPlusMember(ContactProfile.NOWPLUS_CONTACT);
			records[i].setFirstName(c.compileRandomName(RANDOM));
			records[i].setMiddleNames(c.compileRandomName(RANDOM));
			records[i].setLastName(c.compileRandomName(RANDOM));
		}
		
		contactsStore.initialise(records);
		
		// application started
		context.mockApplicationStarted();
		
		// no upgrade available
		protocol.mockUpgradeInfoReceived(null);
	}

	public void testPerformance() throws Exception
	{
		setUpAndStart();
		
		System.gc();
		
		Thread.sleep(300);
		
		start = System.currentTimeMillis();
	
		model.setContactsOrder(new LastFirstOrder());

		long time = System.currentTimeMillis() - start;
		Assert.assertTrue("setContactsOrder returned in " + time + "ms", time < 300);

		Thread.sleep(300);
		
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
	
	public byte getContext() 
	{
		return 0;
	}

	public void handleEvent(byte context, int id, Object data)
	{
		if ((start != 0) && (context == Event.Context.CONTACTS) && (id == Event.Contacts.REFRESH_LIST))
		{
			long time = System.currentTimeMillis() - start;
			Assert.assertTrue("REFRESH_LIST fired in " + time + "ms", time < 250);
		}
	}
}
