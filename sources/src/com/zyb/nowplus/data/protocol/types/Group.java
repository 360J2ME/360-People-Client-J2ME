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

public class Group implements ServiceObject {
	public static final long UNKNOWN_GROUP_ID = -1;
	private static final String KEY_GROUP_ID = "id",
								KEY_GROUP_NAME = "name",
								KEY_READ_ONLY = "isreadonly",
								KEY_SYSTEM_GROUP = "issystemgroup",
								KEY_CONTACT_ID_LIST = "contactidlist",
								KEY_NETWORK = "network",
								KEY_GROUP_LIST = "grouplist";
	
	private long[] groupIDs, contactIDs;
	private String[] groupNames;
	private String[] groupNetworks;
	private boolean isReadOnly, isSystemGroup;

	public Group() {}
	
	/**
	 * 
	 * Parses one group as a result coming from the GetGroups
	 * response.
	 * 
	 * @param group The group as a hashtable coming from 
	 * the backend.
	 * 
	 */
	public Group(Hashtable group) {
		isReadOnly = false;
		isSystemGroup = false;
		
		if (null == group) {
			return;
		}
		
		groupIDs = new long[1];
		groupNames = new String[1];
		groupNetworks = new String[1];
		
		
		if (group.containsKey(KEY_GROUP_ID)) {
			try {
				groupIDs[0] = ((Long) group.get(KEY_GROUP_ID)).longValue();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing group id");
			}
		}
		
		if (group.containsKey(KEY_GROUP_NAME)) {
			try {
				groupNames[0] = (String) group.get(KEY_GROUP_NAME);
				
				//#debug debug
				System.out.println("Added group " + groupNames[0]);
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing group name");
			}
		}
		
		if (group.containsKey(KEY_READ_ONLY)) {
			try {
				isReadOnly = 
					((Boolean) group.get(KEY_READ_ONLY)).booleanValue();
			} catch (Exception e) {
				//#debug info
				System.out.println("Failed parsing read only");
			}
		}
		
		if (group.containsKey(KEY_SYSTEM_GROUP)) {
			try {
				isSystemGroup = 
					((Boolean) group.get(KEY_SYSTEM_GROUP)).booleanValue();
				//#debug debug
				System.out.println("isSystemGroup: " + isSystemGroup);
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing system group");
			}
		}

		if (group.containsKey(KEY_NETWORK)) {
			try {
				groupNetworks[0] = (String) group.get(KEY_NETWORK);
				//#debug debug
				System.out.println("Added group with network " + groupNetworks[0]);
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing group network");
			}
		}

		
		
	}
	
	/**
	 * 
	 * Constructor for creating a SetContactGroupRelations request.
	 * 
	 * @param groupIDs The list of group IDs to match against 
	 * contacts. Must not be null. Must be as long as groupNames 
	 * or it will not be set.
	 * @param contactIDs The list of contact IDs to match against 
	 * groups. Must not be null. Must be as long as groupIDs or 
	 * it will not be set.
	 * 
	 */
	public Group(long[] groupIDs, long[] contactIDs) {
				
		this.groupIDs = groupIDs;
		this.contactIDs = contactIDs;
	}	
	
	
	/**
	 * 
	 * Creates the hashtable for the SetContactGroupRelations request.
	 * 
	 */
	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		if ((null != groupIDs) && (null != contactIDs)) {
			Vector contactIDList = new Vector(contactIDs.length);
			
			for (int i = 0; i < contactIDs.length; i++) {
				contactIDList.addElement(new Long(contactIDs[i]));
			}
			ht.put(KEY_CONTACT_ID_LIST, contactIDList);
			
			Vector groupIDList = new Vector(groupIDs.length);
			
			for (int i = 0; i < groupIDs.length; i++) {
				Hashtable htGroup = new Hashtable();
				htGroup.put(KEY_GROUP_ID, new Long(groupIDs[i]));
				groupIDList.addElement(htGroup);
			}
			ht.put(KEY_GROUP_LIST, groupIDList);
		}
		
		return ht;
	}

	
	public long getGroupID() {
		if ((null == groupIDs) || (groupIDs.length < 1)) {
			return UNKNOWN_GROUP_ID;
		}
		
		return groupIDs[0];
	}

	public String getGroupName() {
		if ((null == groupNames) || (groupNames.length < 1)) {
			return "";
		}
		
		return groupNames[0];
	}

	public String getGroupNetwork() {
		if ((null == groupNetworks) || (groupNetworks.length < 1)) {
			return "";
		}
		
		//Default to group name
		if(groupNetworks[0] == null || groupNetworks[0].length() == 0)
			return getGroupName();
			
		return groupNetworks[0];
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}	
	
	public boolean isSystemGroup() {
		return isSystemGroup;
	}	
	
	public long[] getContactIDs()
	{
		return contactIDs;
	}
}
