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
package com.zyb.nowplus.test;

import javax.microedition.io.ConnectionNotFoundException;

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.ServiceBroker;
import com.zyb.nowplus.business.content.Sink;
import com.zyb.nowplus.business.content.Source;
import com.zyb.nowplus.business.event.test.MockEventDispatcher;
import com.zyb.nowplus.business.sync.Sync;
import com.zyb.nowplus.data.email.EmailCommunicationManager;
import com.zyb.nowplus.data.protocol.CommunicationManager;
import com.zyb.nowplus.data.storage.test.MockDataStore;
import com.zyb.nowplus.data.storage.test.MockKeyValueStore;
import com.zyb.util.Collator;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;

import de.enough.polish.ui.Displayable;

public class MockMIDlet extends MockEventDispatcher implements MIDletContext
{
	public static final int EVENT_PLATFORM_REQUEST = 101;
	public static final int EVENT_DESTROYED = 102;
	public static final int EVENT_RMS_DELETED = 103;
	
	private final ServiceBroker services;
	
	private final MockKeyValueStore settingsStore;
	private final MockDataStore contactsStore;
	private final MockDataStore lifedriveStore;
	private final MockDataStore friendstreamStore;
	
	public MockMIDlet(MockKeyValueStore settingsStore, MockDataStore contactsStore, 
			MockDataStore lifeDriveStore, MockDataStore friendstreamStore, Sync syncManager, 
			Sink contentSink, Source contentSource, 
			CommunicationManager protocol, EmailCommunicationManager emailProtocol, 
			EventDispatcher dispatcher, int numberOfEvents)
	{
		super(numberOfEvents);
		
		TextUtilities.loadLanguageTextKeys(TextUtilities.getCurrentLanguageOnly());
		
		services = new ServiceBroker(this, settingsStore, contactsStore, lifeDriveStore, 
				friendstreamStore, syncManager,
				contentSink, contentSource, protocol, dispatcher);	
		
		this.contactsStore = contactsStore;
		this.lifedriveStore = lifeDriveStore;
		this.friendstreamStore = friendstreamStore;
		this.settingsStore = settingsStore;
	}
	
    // implementation of MIDletContext
	
	public void setCurrent(Displayable disp)
	{
	}
	
	public void setCurrent(javax.microedition.lcdui.Displayable disp) 
	{
	}

	public String getMsisdn()
	{
		return "mynumber";
	}
	
	public String getCurrentLanguage()
	{
		return "en-US";
	}
	
	public boolean platformRequest(String url)	throws ConnectionNotFoundException 
	{
		if (url.endsWith("fail"))
		{
			throw new ConnectionNotFoundException();
		}
		if (url.endsWith("postpone"))
		{
			return true;
		}
		notifyEvent(Event.Context.TEST, EVENT_PLATFORM_REQUEST, url);
		return false;
	}
	
	public void exit(boolean deleteStorageBefore) 
	{
		services.applicationStopped(false);
		notifyEvent(Event.Context.TEST, EVENT_DESTROYED);
	}
	
	public boolean checkFiles(String[] files)
	{
		return true;
	}
	
	public void deleteRecordStores(String[] selection)
	{
		settingsStore.wipe();
		contactsStore.wipe();
		lifedriveStore.wipe();
		friendstreamStore.wipe();
		notifyEvent(Event.Context.TEST, EVENT_RMS_DELETED);
	}
	
	// for testing
	
	public Model getModel()
	{
		return services;
	}
	
	public void mockApplicationStarted() throws Exception
	{
		Collator.getInstance().loadAlphabet(getCurrentLanguage());
		
		services.applicationStarted();
		services.clientInitialized();

		// give the model some time
		Thread.sleep(100);
	}
	
	public void mockApplicationPaused()
	{
		services.applicationPaused();
	}	
	
	public String toString()
	{
		return "MockMIDlet["
			+ "]";
	}

	public Displayable getCurrent()
	{
		return null;
	}

	public boolean supportsTcp() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getDeviceId() {
		// TODO Auto-generated method stub
		return null;
	}
}
