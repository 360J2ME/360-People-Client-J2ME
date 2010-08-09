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
//#condition !polish.remove_status_tab
package com.zyb.nowplus.business;

import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.ActivityList;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.storage.DataStore;
import com.zyb.nowplus.data.storage.RMSDataStore5;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.Collator;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

/**
 * Manages the activities in the lifedrive and friends stream.
 */
class ActivityManager implements EventListener
{
	public static final String TIMELINE_STORE = "lifedrive";
	public static final String STATUSSTREAM_STORE = "friendstream";
	
	private static final String TYPE_TIMELINE = "timeline"; 
	private static final String TYPE_STATUSSTREAM = "status"; 
	
	private Object startLock = new Object();
	private boolean activityManagerStarted;

	private final ServiceBroker services;

	private final ActivityList timeline;
	private int timelineRequestId;
	
	private final ActivityList statusStream;
	private int statusStreamRequestId;
	
	/**
	 * This indicates activities were received with unknown contact ids. If
	 * new contacts are received, we request the activities.
	 */
	private boolean contactsNotFound;
	
	/**
	 * Creates an activity manager.
	 */
	public ActivityManager(ServiceBroker services, EventDispatcher dispatcher)
	{
		this(services, new RMSDataStore5(false), new RMSDataStore5(false), dispatcher);
	}

	/**
	 * Creates an activity manager with the given dependencies.
	 */
	public ActivityManager(ServiceBroker services, DataStore lifeDriveStore, DataStore friendsStreamStore, EventDispatcher dispatcher)
	{
		//#debug debug
		System.out.println("Constructing activity manager.");
				
		this.services = services;
		
		timeline = new ActivityList(lifeDriveStore);
		statusStream = new ActivityList(friendsStreamStore);
		
		dispatcher.attach(this);
	}	
	
	public void start() throws StorageException
	{
		//#debug info
		System.out.println("Starting activity manager.");

		synchronized (startLock) {
			if (activityManagerStarted) {
				return;
			}
			
			activityManagerStarted = true;
		}
		
		timeline.load(TIMELINE_STORE);
		services.fireEvent(Event.Context.MODEL, Event.Model.TIMELINE_CHANGED, null);
		
		statusStream.load(STATUSSTREAM_STORE);
		services.fireEvent(Event.Context.MODEL, Event.Model.STATUS_STREAM_CHANGED, null);			
	}
	
	public void stop() 
	{
		timeline.close();
		
		statusStream.close();
		
		//#debug info
		System.out.println("Activity manager stopped.");
	}
	
	public void addStatusActivity(ManagedProfile profile, String description, String networkId)
	{
		if (!Collator.isEmpty(description))
		{
			ManagedProfile[] profiles = {profile};
			
			long time = System.currentTimeMillis();
			
			ExternalNetwork source = services.getChatManager().findNetworkById(networkId);
			
			Activity activity = new Activity(Activity.LOCAL_ID, Activity.TYPE_CONTACT_SENT_STATUS_UPDATE,  
					"", description, profiles, null, time, source);
			
			statusStream.addActivity(activity);
			statusStream.refresh();
			services.fireEvent(Event.Context.ACTIVITY, Event.Activities.STATUS_STREAM_CHANGED, null);
		}
	}
	
	public void addTimelineActivity(ManagedProfile profile, String description, String networkId, String name, String conversationId, Channel channel)
	{
		long time = System.currentTimeMillis();
		
		ExternalNetwork source = services.getChatManager().findNetworkById(networkId);
		
		Activity activity = new Activity(System.currentTimeMillis(), 
				"", description, profile, null, time, source, name, conversationId,channel);
		
		timeline.addActivity(activity);			
		timeline.refresh();			
		services.fireEvent(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null);
	}
	
	public Activity updateTimelineActivity(Channel channel,String sourceName, long time, String description)
	{		
		Activity activity=timeline.updateActivity(channel,sourceName, time, description);
		
		if(activity==null)//no matched object found based on 'conversationId'
			return null;
		
		timeline.refresh();		
		
		services.fireEvent(Event.Context.ACTIVITY, Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE, /*content for updating ActivityItem of UI*/activity);
		
		return activity;
	}
	
	public byte getContext()
	{
		return Event.Context.MODEL;
	}
	
	public synchronized void handleEvent(byte context, int id, Object data)
	{
		if (context == Event.Context.MODEL)
		{
			if (id == Event.Model.TIMELINE_CHANGED)
			{
				// lifeDriveRequestId = requestActivities(TYPE_LIFEDRIVE); not in scope yet
				
				//#debug info
				System.out.println("Request " + timelineRequestId + ": request life drive");
			}
			else
			if (id == Event.Model.STATUS_STREAM_CHANGED)
			{
				statusStreamRequestId = requestActivities(TYPE_STATUSSTREAM);

				//#debug info
				System.out.println("Request " + statusStreamRequestId + ": request friend stream");			
			}
		}
		else if (context == Event.Context.ACTIVITY && (id == Event.Activities.OUTGOING_SMS || id == Event.Activities.OUTGOING_PHONECALL) )
		{
			Activity activity = (Activity) data;
			timeline.addActivity(activity);
			timeline.refresh();
			services.fireEvent(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null);
		}
	}
	
	private int requestActivities(String filter)
	{
		Vector lids = new Vector();
		lids.addElement("0-" + ActivityList.MAX_NUMBER_OF_ACTIVITIES);
		
		Vector v = new Vector();
		v.addElement("true");
		
		Vector type = new Vector();
		type.addElement("contact_sent_status_update");
		type.addElement("contact_received_status_update");
		type.addElement("contact_profile_email_updated");
		type.addElement("contact_profile_phone_updated");
		type.addElement("contact_profile_address_updated");
		type.addElement("sn_added");
		type.addElement("sn_added_by_friend");
		type.addElement("sn_photos_posted");
		type.addElement("sn_videos_posted");
		type.addElement("sn_status_received");
		type.addElement("sn_contact_profile_email_updated");
		type.addElement("sn_contact_profile_phone_updated");
		type.addElement("sn_contact_profile_address_updated");
		type.addElement("music_recommendation_received_track");
		type.addElement("music_recommendation_received_album");
		type.addElement("contact_joined");
		type.addElement("contact_new_friends");
		type.addElement("message_email_received");
		
		Vector sort = new Vector();
		sort.addElement("updated?rev");
		
		Hashtable filters = new Hashtable();
		filters.put("lids",lids);
		filters.put("f.type", type);
		filters.put("sort", sort);
		filters.put(filter, v);

		Hashtable params = new Hashtable();
		params.put("filterlist", filters);

		return services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.ACTIVITIES, null, 
			params, ServerRequest.MEDIUM_PRIORITY);
	}
		
	public synchronized void activitiesReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (requestId == timelineRequestId)
		{
			timelineRequestId = 0;
		}
		else
		if (requestId == statusStreamRequestId)
		{
			statusStreamRequestId = 0;
		}
		else
		{
			//#debug info 
			System.out.println("Received response to stale request " + requestId + ", ignored");	
			return;
		}
		
		int timelineNew = 0;
		int statusStreamNew = 0;
		
		//#if activate.embedded.360email
		boolean messageEmailReceived = false;
		//#endif
		
		for (int i = 0; i < serviceObjects.length; i++)
		{
			com.zyb.nowplus.data.protocol.types.Activity serviceObject = 
				(com.zyb.nowplus.data.protocol.types.Activity) serviceObjects[i];
		
			if (serviceObject.isDeleted())
			{
				//#debug info
				System.out.println("Activitiy " + serviceObject.getActivityID() + " discarded because deleted.");
				continue;
			}
			
			int type = Activity.toType(serviceObject.getType());
			if (type == Activity.TYPE_MESSAGE_EMAIL_RECEIVED)
			{
				//#if activate.embedded.360email
				messageEmailReceived = true;
				//#endif
				
				//#debug info
				System.out.println("Activity " + serviceObject.getActivityID() + " discarded because type=email.");
				continue;
			}
			
			if (Collator.isEmpty(serviceObject.getDescription()))
			{
				//#debug info
				System.out.println("Activity " + serviceObject.getActivityID() + " discarded because description is empty.");
				continue;
			}

			ManagedProfile[] profiles = getProfilesBySabId(serviceObject.getInvolvedContactIDs());
			if ((profiles.length > 0) && (profiles[0] == null))
			{
				contactsNotFound = true;
				
				//#debug info
				System.out.println("Activity " + serviceObject.getActivityID() + " discarded because contact not found.");
				continue;
			}
						
			long time = serviceObject.getTimeOfActivity() * 1000;
			if (time == 0) {
				time = serviceObject.getDateUpdated() * 1000;
			}
			if (time == 0) {
				time = serviceObject.getDateCreated() * 1000;
			} 
			
			ExternalNetwork source = services.getChatManager().findNetworkById(serviceObject.getNetwork());
				
			Activity activity = new Activity(serviceObject.getActivityID(), type,  
					serviceObject.getTitle(), serviceObject.getDescription(), profiles, serviceObject.getUrl(), 
					time, source);
				
			String[] activityTypes = serviceObject.getActivityTypes();
			for (int j = 0; j < activityTypes.length; j++)
			{
				if (TYPE_TIMELINE.equals(activityTypes[j]))
				{
					timeline.addActivity(activity);
					timelineNew++;
				}
				else
				if (TYPE_STATUSSTREAM.equals(activityTypes[j]))
				{
					statusStream.addActivity(activity);
					statusStreamNew++;
				}
				else
				{
					//#debug info
					System.out.println("Unknown activity type " + activityTypes[j]);
				}
			}
		}
		if (timelineNew > 0)
		{
			timeline.refresh();
			services.fireEvent(Event.Context.ACTIVITY, Event.Activities.TIMELINE_CHANGED, null);
		}
		if (statusStreamNew > 0)
		{
			statusStream.refresh();
			services.fireEvent(Event.Context.ACTIVITY, Event.Activities.STATUS_STREAM_CHANGED, null);
		}
		
		//#if activate.embedded.360email
		if (messageEmailReceived)
		{
			services.getEmailManager().newEmailReceived();
		}
		//#endif
	}
	
	/**
	 * Gets a number of profiles by server address book id.
	 */
	private ManagedProfile[] getProfilesBySabId(long[] sabIds)
	{
		if (sabIds == null)
		{
			return new ManagedProfile[0];
		}
		// TODO this should be optimised
		ManagedProfile[] profiles = new ManagedProfile[sabIds.length];
		for (int i = 0; i < profiles.length; i++)
		{
			profiles[i] = Profile.manager.getProfileBySabId(sabIds[i]);
		}
		return profiles;		
	}
	
	public boolean errorReceived(int requestId, byte errorCode)
	{
		return false;
	}
	
	public synchronized boolean timeOutReceived(int requestId)
	{
		boolean handled = false;
		if (requestId == timelineRequestId)
		{
			timelineRequestId = 0;
			services.fireEvent(Event.Context.MODEL, Event.Model.TIMELINE_CHANGED, null);
						
			//#debug info
			System.out.println("Request " + requestId + " for timeline timed out, request again.");
			handled = true;
		}
		else
		if (requestId == statusStreamRequestId)
		{
			statusStreamRequestId = 0;			
			services.fireEvent(Event.Context.MODEL, Event.Model.STATUS_STREAM_CHANGED, null);

			//#debug info
			System.out.println("Request " + requestId + " for status stream timed out, request again.");
			handled = true;
		}
		return handled;
	}
	
	public void newContactsReceived()
	{
		if (contactsNotFound)
		{
			contactsNotFound = false;
			
			//#debug info
			System.out.println("Request activities again, because new contacts have arrived.");
			
			services.fireEvent(Event.Context.MODEL, Event.Model.TIMELINE_CHANGED, null);
			services.fireEvent(Event.Context.MODEL, Event.Model.STATUS_STREAM_CHANGED, null);
		}
	}
	
	public ListSelection getTimeline(int from, int to)
	{
		return timeline.getActivities(from, to);
	}
	
	public ListSelection getStatusStream(int from, int to)
	{
		return statusStream.getActivities(from, to);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ActivityManager[timeline=" + timeline
		 + ",timelineRequestId=" + timelineRequestId
		 + ",statusStream=" + statusStream
		 + ",statusStreamRequestId=" + statusStreamRequestId
		 + "]";
	}
	//#enddebug
}
