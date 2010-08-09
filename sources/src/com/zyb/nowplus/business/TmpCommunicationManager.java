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
package com.zyb.nowplus.business;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManager;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.nowplus.data.protocol.types.Identity;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.util.DateHelper;

/**
 * Temporary communication manager.
 */
public class TmpCommunicationManager implements CommunicationManager
{
	private final Timer timer;
	
	private AuthenticationListener authListener;
	private ResponseListener responseListener;
	private NetworkListener networkListener;
	
	private int requestId;
	
	public TmpCommunicationManager()
	{
		timer = new Timer();
	}
	
	public void startConnections()
	{
	}

	public void stopConnections(boolean finalStop) 
	{
	}
			
	public void registerListeners(AuthenticationListener authListener,
			 NetworkListener netListener, ResponseListener responseListener)

	{
		this.authListener = authListener;
		this.networkListener = netListener;
		this.responseListener = responseListener;
	}
	
	public void authenticate(String username, String password, boolean doStoreSession, HttpRPGConnection conn) throws IllegalArgumentException 
	{
		if (authListener != null)
		{
			authListener.authenticationSucceeded();
		}
	}

	public void checkForUpdates(String versionNumber, HttpRPGConnection updateConnection)
	{
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				checkForUpdates0(true);
			}
		}, 300);		
	}
	
	private void checkForUpdates0(boolean uptodate)
	{
		if (uptodate)
		{
			responseListener.clientIsUpToDate();
		}
		else
		{
			Hashtable ht = new Hashtable();
			ht.put("version", "newversion");
			ht.put("title", "title");
			ht.put("mesage", "message");
			ht.put("url", "url");
			ht.put("force", Boolean.TRUE);
			
			com.zyb.nowplus.data.protocol.types.Update updateInfo = new com.zyb.nowplus.data.protocol.types.Update(ht) ;
			
			responseListener.clientUpdateAvailable(updateInfo);
		}
	}

	public void clientIsUpToDate()
	{
		responseListener.clientIsUpToDate();
	}

	public void clientInitialized()
	{
	}
	
	public boolean isRoaming() 
	{
		return false;
	}

	public void registerUser(final String username, String password, String fullName,
			String birthdate, String msisdn,
			boolean acceptedTermsAndConditions, String countryCode,
			String timezone, String userEmailAddr, String language, int mobileOperatorID,
			int mobileModelID, boolean subscribedToNewsletter,
			HttpRPGConnection conn) 
	{
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				registerUser0(username);
			}
		}, 300);
	}

	private void registerUser0(String userName)
	{
		if (responseListener != null)
		{
			if ("user.fail".equals(userName))
			{
				authListener.registrationFailed(500);
			}
			else
			{
				authListener.registrationSucceeded(123456);
			}
		}
	}
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items,
			Hashtable parameters, byte priority) {
		return sendRequest(verb, noun, items, parameters, priority, false);
	}
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, Hashtable filters, byte priority, boolean fireAndForget) 
	{
		final int reqId = ++requestId;
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.IDENTITIES))
		{
			timer.schedule(new TimerTask()
			{
				public void run()
				{
					getIdentities0(reqId);
				}
			}, 300);
		}
		else
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.GROUPS))
		{
			timer.schedule(new TimerTask()
			{
				public void run()
				{
					getGroups0(reqId);
				}
			}, 300);
		}
		else
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.MY_CHANGES))
		{
			timer.schedule(new TimerTask()
			{
				public void run()
				{	
					getMyChanges0(reqId);
				}
			}, 300);			
		}
		else
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.CONTACTS_CHANGES))
		{
			timer.schedule(new TimerTask()
			{
				public void run()
				{	
					getContactChanges0(reqId);
				}
			}, 300);			
		}
		else
		if ((verb == ServerRequest.GET) && (noun == ServerRequest.ACTIVITIES))
		{
			timer.schedule(new TimerTask()
			{
				public void run()
				{	
					getActivities0(reqId);
				}
			}, 300);
		}		
		return reqId;
	}

	private void getIdentities0(int reqId)
	{
		Identity i0 = new Identity(false);
		i0.setNetwork("facebook.com");
		
		Identity i1 = new Identity(false);
		i1.setNetwork("winlive");
		
		notifyItemsReceived(reqId, new ServiceObject[] {i0, i1}, ServiceObject.AVAILABLE_IDENTITY);
	}
	
	private void getGroups0(int reqId)
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
		
		notifyItemsReceived(reqId, serviceObjects, ServiceObject.GROUPS);
	}
	
	private void getMyChanges0(int reqId)
	{
		Vector detailList = new Vector();
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
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70001));
		ht.put("userid", new Long(80007));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(33);		
		
		notifyItemsReceived(reqId, new ServiceObject[] {serviceObject}, ServiceObject.MY_CHANGES);
	}
	
	private void getContactChanges0(int reqId)
	{
		Vector detailList = new Vector();
		{
			Hashtable ht = new Hashtable();
			ht.put("key", "vcard.name");
			ht.put("val", "Lastname;Firstname;Middlenames");
			detailList.addElement(ht);
		}
		
		Hashtable ht = new Hashtable();
		ht.put("contactid", new Long(70002));
		ht.put("userid", new Long(80002));
		ht.put("detaillist", detailList);
		
		ContactChanges serviceObject = new ContactChanges(ht);
		serviceObject.setCurrentServerRevision(33);		
		
		System.out.println("notifyItemsReceived ");
		
		notifyItemsReceived(reqId, new ServiceObject[] {serviceObject}, ServiceObject.CONTACT_CHANGES);
	}
	
	private void getActivities0(int reqId)
	{
		long id = 301;
		long time = System.currentTimeMillis();;
		
		ServiceObject[] serviceObjects = new ServiceObject[50];
		for (int i = 0; i < serviceObjects.length; i++) 
		{
			Vector flagList = new Vector();
			flagList.addElement("status");

			Hashtable contact = new Hashtable();
			contact.put("contactid", new Long(70001));

			Vector contactList = new Vector();
			contactList.addElement(contact);

			Hashtable ht = new Hashtable();
			ht.put("activityid", new Long(id));
			ht.put("type", "contact_received_status_update");
			ht.put("flaglist", flagList);
			ht.put("contactlist", contactList);
			ht.put("time", new Long(time));

			switch(i % 3)
			{
				case 0 : 
					ht.put("title", "Cup of tea");
					ht.put("description", "" + id);					
				case 1 : 
					ht.put("title", "Feet");
					ht.put("description", "" + id);
				case 2 : 
					ht.put("title", "Pipe");
					ht.put("description", "" + id);
			}
			
			serviceObjects[i] = new com.zyb.nowplus.data.protocol.types.Activity(ht);

			time -= DateHelper.DAY / 2;
			id++;
		}
		
		notifyItemsReceived(reqId, serviceObjects, ServiceObject.ACTIVITY);	
	}

	public int sendRequest(byte verb, byte noun, ServiceObject[] items, Hashtable filters, byte priority, int timeOut) 
	{
		return sendRequest(verb, noun, items, filters, priority);
	}	
	
	public int[] sendMultipleRequests(byte[] verbs, byte[] nouns, 
			ServiceObject[][] items, Hashtable[] filters, byte[] priorities) 
	{
		int[] reqIds = new int[verbs.length];
		for (int i = 0; i < verbs.length; i++)
		{
			reqIds[i] = sendRequest(verbs[i], nouns[i], items[i], filters[i], priorities[i]);
		}
		return reqIds;
	}
	
	public int sendCreateConversationRequest(String network, String name) 
	{
		return 0;
	}
	
	public int sendStopConversationRequest(String network, String name, String conversationID) 
	{
		return 0;
	}

	public int sendChatMessage(String network, String name, String conversationId, String body) 
	{
		return 0;
	}
	
	public int setPresence(int[] presences, String[] networks, String[] names) 
	{
		return 0;
	}
	
	public int getPresences() 
	{
		return 0;
	}
	
	public int loadBinary(String url) 
	{
		return 0;
	}
	
	public int[] loadBinaries(String[] urls) 
	{
		return new int[0];
	}
	
	public boolean cancelRequest(int requestID) 
	{
		return false;
	}
	
	private void notifyItemsReceived(int requestId, ServiceObject[] data, byte itemType)
	{
		if (responseListener != null)
		{
			responseListener.itemsReceived(requestId, data, itemType);
		}
	}

	public void rerequestActivationCodeForUser(String username, String msisdn)
	{

	}

	public void requestMsisdn(HttpRPGConnection conn) 
	{

	}

	public void autodetectConnection(boolean supportsTcp) 
	{
		networkListener.autodetectConnectionFinished();
	}
}
