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
package com.zyb.nowplus.data.protocol.types;

import java.util.Hashtable;
import java.util.Vector;

public class Activity implements ServiceObject {
	public static final int T_TIMELINE = 0, 
							T_STATUS = 1,
							T_UNKNOWN = -1;
	
	private static final String KEY_ACTIVITY_ID= "activityid",	
								KEY_FLAG_LIST ="flaglist",
								KEY_FLAG = "flag",
								KEY_TITLE = "title",
								KEY_DESCRIPTION = "description",
								KEY_CONTACT_LIST = "contactlist",
								KEY_CONTACT_ID = "contactid",
								KEY_CONTACT_NAME = "name",
								KEY_AVATAR_URL = "avatarurl",
								KEY_URL = "url",
								KEY_CREATED = "created",
								KEY_UPDATED = "updated",
								KEY_TIME = "time",
								KEY_DELETED = "deleted",
								
								KEY_TYPE = "type",
								KEY_NETWORK = "network",
								KEY_STORE = "store",
								KEY_MORE_INFO = "moreinfo";

	private long activityID;
	private String[] activityTypes;
	private String type;	// TODO see and implement type http://devapi.next-vodafone.com/devel/api/activities/getactivities#activities_activity
	private String title;
	private String description;
	private long[] contactIDs;
	private String[] contactNames, contactAvatarUrls;
	private String url;
	private long dateCreated, dateUpdated, timeOfActivity;
	private boolean deleted;	
	
	private String network;
	
	public Activity(Hashtable ht) {
		if (null == ht) {
			return;
		}
		
		try {
			activityID = (((Long) ht.get(KEY_ACTIVITY_ID)).longValue());
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		Vector v = null;
		try {
			v = ((Vector) ht.get(KEY_FLAG_LIST));
			
			// handle case where there is no list but only 1 item
			if (null == v) {
				String flag = (String) ht.get(KEY_FLAG);
				if (null != flag) {
					v = new Vector();
					v.addElement(flag);
				}
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		if (null != v) {
			activityTypes = new String[v.size()];
			
			for (int i = 0; i < v.size(); i++) {
				try {
					activityTypes[i] = (String) v.elementAt(i);
				} catch (Exception e2) {
					//#debug error
					System.out.println("Could not cast activity type." + e2);
				}
			}
			v = null;
		}
		
		try {
			type = (String) ht.get(KEY_TYPE);
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		try {
			title = (String) ht.get(KEY_TITLE);
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
				
		try {
			description = (String) ht.get(KEY_DESCRIPTION);
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}

		try {
			v = ((Vector) ht.get(KEY_CONTACT_LIST));
			
			// handle case where there is no list but only 1 item
			if (null == v) {
				Hashtable htContact = (Hashtable) ht.get(KEY_CONTACT_LIST);
				if (null != htContact) {
					v = new Vector();
					v.addElement(htContact);
				}
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		if (null != v) {
			contactIDs = new long[v.size()];
			contactNames = new String[v.size()];
			contactAvatarUrls = new String[v.size()];
			
			for (int i = 0; i < v.size(); i++) {
				try {
					if (null != v.elementAt(i)) {
						Hashtable htCntct = (Hashtable) v.elementAt(i);
						
						if (null != htCntct) {
							Long id = (Long) htCntct.get(KEY_CONTACT_ID);
							contactIDs[i] = (id == null) ? 0 : id.longValue();

							contactNames[i] = (String) htCntct.get(KEY_CONTACT_NAME);

							contactAvatarUrls[i] = (String) htCntct.get(KEY_AVATAR_URL);
						}
					}
				} catch (Exception e2) {
					//#debug error
					System.out.println("Could not cast involved contact details." + e2);
				}
			}
			v = null;	
		}
		
		try {
			url = (String) ht.get(KEY_URL);
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}		
		
		try {
			dateCreated = ((Long) ht.get(KEY_CREATED)).longValue();
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		try {
			dateUpdated = ((Long) ht.get(KEY_UPDATED)).longValue();
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		try {
			timeOfActivity = ((Long) ht.get(KEY_TIME)).longValue();
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}

		try {
			deleted = ((Boolean) ht.get(KEY_DELETED)).booleanValue();
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		


		if (ht.containsKey(KEY_MORE_INFO)) {
			Hashtable htMoreInfo = ((Hashtable) ht.get(KEY_MORE_INFO));
			
			if ((null != htMoreInfo) && (htMoreInfo.containsKey(KEY_NETWORK))) {
				try {
					network = (String) htMoreInfo.get(KEY_NETWORK);
				} catch (Exception e) {
					//#debug error
					System.out.println("Exception " + e);
				}
			}
		}

		if(network == null)//In case network wasn't set and store is
		{
			try {
				network = (String) ht.get(KEY_STORE);
				//#debug debug
				System.out.println("network: " + network);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
		}
	}
		
	public Hashtable toHashtable() {
		return null;
	}

	public long getActivityID() {
		return activityID;
	}

	public String[] getActivityTypes() {
		return activityTypes;
	}
	
	public String getType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}	
	
	public long[] getInvolvedContactIDs() {
		return contactIDs;
	}
	
	public String[] getInvolvedContactNames() {
		return contactNames;
	}
	
	public String[] getInvolvedContactAvatarUrls() {
		return contactAvatarUrls;
	}
	
	public String getUrl() {
		return url;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public long getDateUpdated() {
		return dateUpdated;
	}

	public long getTimeOfActivity() {
		return timeOfActivity;
	}

	public boolean isDeleted() {
		return deleted;
	}
	

	public String getNetwork() {
		return network;
	}

	
	//#mdebug error
	public String toString() {

		return "Activity[activityID=" + activityID
		+ ",type=" + type
		+ ",title=" + title
		+ ",description=" + description
		+ ",url=" + url
		+ ",dateCreated=" + dateCreated
		+ ",dateUpdated=" + dateUpdated
		+ ",timeOfActivity=" + timeOfActivity
		+ ",isDeleted=" + deleted
		+ ",network=" + network
		+ "]";
	}
	//#enddebug
}
