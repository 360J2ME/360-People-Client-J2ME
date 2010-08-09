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
package com.zyb.nowplus.business.domain;

import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.data.storage.DataStore;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.ArrayUtils;

import de.enough.polish.util.Comparator;

/**
 * A list of activities.
 */
public class ActivityList 
{
	/**
	 * Sorts activities from new to old.
	 */
	private static final Comparator COMPARATOR = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			Activity a1 = (Activity) o1;
			Activity a2 = (Activity) o2;
			///#mdebug debug
			//System.out.println(new Long(a1.getTime()).toString()+"::second::"+new Long( a2.getTime()).toString()
			//			+"::diference::"+new Long((a2.getTime() - a1.getTime())).toString()
			//			+"::diference in int::"+new Integer((int)(a2.getTime() - a1.getTime())).toString());
			///#enddebug
			if (a2.getTime()>a1.getTime())
				return 1;
			else
				return 0;
			//return (int) (a2.getTime() - a1.getTime()); //results in an incorect ordering of sections
		}
	};
	
//#if polish.blackberry
	public static final int MAX_NUMBER_OF_ACTIVITIES = 200; //because bb device has more memory compared with normal j2me device
//#else
	//# public static final int MAX_NUMBER_OF_ACTIVITIES = 50;
//#endif
	
	private Activity[] activities;
	private int activitiesLen;

	private final DataStore store;
	
	public ActivityList(DataStore store)
	{
		activities = new Activity[MAX_NUMBER_OF_ACTIVITIES];
		activitiesLen = 0;
		
		this.store = store;
	}
	
	public void load(String storeName) throws StorageException
	{
		store.open(storeName, true);

		int numberOfRecords = store.getNumberOfRecords();
		for (int i = 0; i < numberOfRecords; i++)
		{
			if (activitiesLen == activities.length)
			{
				activities = Activity.extendArray(activities);
			}
			Activity activity = (Activity) store.getShortRecord(i);
			
			if ((activity.getInvolvedContacts().length > 0) && (activity.getInvolvedContacts()[0] == null))
			{	
				//#debug info
				System.out.println("Activity " + activity + " not loaded because contact not found.");
			}
			else 
			{
				activities[activitiesLen++] = activity;
			}
		}
		
		refresh();
	}
	
	public void close() 
	{
		store.close();
	}
	
	/**
	 * Gets a range of activities.
	 */
	public synchronized ListSelection getActivities(int from, int to)
	{
		if (to > activitiesLen - 1)
		{
			to = activitiesLen - 1;
		}
		return new ListSelection(activities, from, to, activitiesLen);
	}	
	
	/**
	 * Adds an activity to the list. Overwrites the activity with the same id if present.
	 */
	public synchronized void addActivity(Activity activity)
	{
		int i = getActivityIndex(activity.getId());
		
		if ((i == -1) && (activity.getType() == Activity.TYPE_MESSAGE_IM_CONVERSATION))
		{
			// don't add an activity of this type if there is already an activity with
			// the same conversation id
			/*if (getActivityIndexByConversationId(activity.getConversationId()) != -1) {*/
			if(this.getActivityIndexBy(activity.getTimeLineChannel(), activity.getSourceName())!=NO_MATCHED_VALUE_INDEX){
				return;
			}
		}

		try 
		{
			if (i == -1)
			{
				if (activitiesLen == activities.length)
					activities = Activity.extendArray(activities);

				i = activitiesLen++;
				
				store.insert(activity);
			}
			else 
			{
				store.update(activity);
			}
		}
		catch (StorageException e) 
		{
			//#debug error
			System.out.println("Failed to persist activity " + activity);
		}
		activities[i] = activity;

		// remove the locally added activity for my profile status update
		if ((activity.getId() != Activity.LOCAL_ID) 
				&& (activity.getType() == Activity.TYPE_CONTACT_SENT_STATUS_UPDATE))
		{
			i = 0;
			for (int j = 0; j < activitiesLen; j++)
			{
				if ((activities[j].getId() == Activity.LOCAL_ID)   
					&& (activities[j].getType() == Activity.TYPE_CONTACT_SENT_STATUS_UPDATE))
				{
					// local activity of the same type: remove
					try 
					{
						store.delete(activities[j].getId());
					}
					catch (StorageException e) 
					{
						//#debug error
						System.out.println("Failed to delete overwritten activity " + activity);
					}
				}
				else
				{
					activities[i++] = activities[j];
				}
			}
			activitiesLen = i;
		}
	}
	
	public synchronized Activity updateActivity(Channel channel, String sourceName,long time, String description)
	{
		Activity matchedActivity=null;
		
		int i = getActivityIndexBy(channel,sourceName);
		
		if (i != NO_MATCHED_VALUE_INDEX) 
		{
			activities[i].setTime(time);
			activities[i].setDescription(description);
			
			matchedActivity=activities[i];//kept it for further access e.g. updating the item in timeline tab
			
			try 
			{
				store.update(activities[i]);
			}
			catch (StorageException e) 
			{
				//#debug error
				System.out.println("Failed to update activity " + activities[i]);				
			}
		}
		
		return matchedActivity;
	}
	
	/**
	 * Refreshes the list.
	 */
	public synchronized void refresh()
	{
		ArrayUtils.shellSort(activities, activitiesLen, COMPARATOR);
		while (activitiesLen > MAX_NUMBER_OF_ACTIVITIES)
		{
			activitiesLen--;
			
			// remove obsolete activities
			try 
			{
				store.delete(activities[activitiesLen].getId());
			}
			catch (StorageException e) 
			{
				//#debug error
				System.out.println("Failed to remove obsolete activity " + activities[activitiesLen]);
			}
			activities[activitiesLen] = null;
		}
	}
	
	private static final int NO_MATCHED_VALUE_INDEX=-1;
	private int getActivityIndexBy(Channel channel,String sourceName)
	{
		if(channel==null||activities==null)
			return NO_MATCHED_VALUE_INDEX;//default not found return value -1
		
		//checking (NetworkId,SourceName) equals 
		//SourceName is name of chatting buddy
		for (int i = 0; i < activities.length; i++)
			if (activities[i] != null
					&& activities[i].getSource() != null
					&& activities[i].getSource().getNetworkId().equals(
							channel.getNetworkId())
					&& activities[i].getSourceName().equals(sourceName))
				return i;
		
		return NO_MATCHED_VALUE_INDEX;
	}
	
	private int getActivityIndex(long id)
	{
		int index = -1;
		if (id != Activity.LOCAL_ID)
		{
			for (int i = 0; (i < activitiesLen) && (index == -1); i++)
			{
				if (activities[i].getId() == id)
				{
					index = i;
				}
			}
		}
		return index;			
	}
	/*
	private int getActivityIndexByConversationId(String conversationId)
	{
		int index = -1;
		for (int i = 0; (i < activitiesLen) && (index == -1); i++)
		{
			if (HashUtil.equals(activities[i].getConversationId(), conversationId))
			{
				index = i;
			}
		}
		return index;
	}
	*/
	//#mdebug error
	public String toString()
	{
		return "ActivityList[activities=" + ArrayUtils.toString(activities)
			+ "]";
	}
	//#enddebug
}
