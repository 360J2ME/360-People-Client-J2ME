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
package com.zyb.nowplus.business.domain;

import com.zyb.nowplus.business.sync.util.CRC32;
import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.util.ArrayUtils;

/**
 * Represents the profile of a contact of the user.
 */
public class ContactProfile extends ManagedProfile
{	
	private static final int CHECKSUM_SALT = 37;
	
	public static final int CHECKSUM_UNDEFINED = Integer.MAX_VALUE;
	
	protected boolean syncToNab;
	
	protected Group[] groups;
	protected Group[] groupsDeleted;
	protected Group[] groupsAdded;
	protected long groupsCri;
	protected boolean isGroupAdded;
	protected boolean isGroupDeleted;
	
	protected int checksum = CHECKSUM_UNDEFINED;
	
	public int getChecksum() {
		if(this.checksum == CHECKSUM_UNDEFINED) {
			int checksum = CHECKSUM_SALT;
			
			String cName = getFullName();
	
			if (cName != null && !"".equals(cName)) {
				checksum = CRC32.update(cName, checksum);
			}
			
			Channel channel = getPrimaryEmailChannel();
	
			if (channel != null) {
				checksum = CRC32.update(channel.getName(), checksum);
			}
			
			String[] workDetails = getWorkDetails();
			if (workDetails != null && workDetails.length > 0 && workDetails[0] != null && !"".equals(workDetails[0])) {
				checksum = CRC32.update(workDetails[0], checksum);
			}
			
			channel = getPrimaryCallChannel();
			
			if (channel != null) {
				checksum = CRC32.update(channel.getName(), checksum);
			}
			
			this.checksum = checksum;
		} 
		
		return this.checksum;
	}
	
	public boolean isEmpty() {
		return getChecksum() == CHECKSUM_SALT;
	}
	public ContactProfile() 
	{
		groups = new Group[0];
	}
	
	public void setSyncToNab(boolean syncToNab)
	{
		this.syncToNab = syncToNab;
	}
	
	public boolean syncToNab()
	{
		return syncToNab;
	}
	
	public void setIdentity(Identity identity)
	{
		Identity oldIdentity = getIdentity(identity.getType(), identity.getNabSubtypes());
		if (oldIdentity == null)
		{
			addIdentity(identity);
		}
		else
		{
			identity.setSabDetailId(oldIdentity.getSabDetailId());
			updateIdentity(oldIdentity, identity);
		}
	}
	
	public void removeIdentity(int type, int nabSubtypes)
	{
		removeIdentity(getIdentity(type, nabSubtypes));
	}
	
	public void setAddress(Address address)
	{
		Address oldAddress = getAddress(address.getNabSubtypes());
		if (oldAddress == null)
		{
			addAddress(address);
		}
		else
		{
			address.setSabDetailId(oldAddress.getSabDetailId());
			updateAddress(oldAddress, address);
		}
	}
	
	public void removeAddress(int nabSubtypes)
	{
		removeAddress(getAddress(nabSubtypes));
	}	
	
	/**
	 * Sets the custom and standard groups this contact is in.
	 */
	public void setGroups(Group[] groups)
	{
		checkLock();
		isGroupAdded = false;
		isGroupDeleted = false;
		
		if (groups == null)
		{
			groups = new Group[0];
		}

		Group[] tmpGroups = new Group[this.groups.length + groups.length];
		int len = 0;
		//to fill the deleted group.
		boolean flag = false;
		for (int i = 0; i < this.groups.length; i++)
		{
			if (this.groups[i].getType() == Group.TYPE_SOCIAL_NETWORK)
			{
				tmpGroups[len++] = this.groups[i];
			}else{
				//check if the group is deleted, if it is then add in the array.
				flag = false; 
				for(int j=0;j<groups.length;j++){
					 if(groups[j].equals(this.groups[i])){
						 flag = true; break;
					 }
				 }
				if(!flag){
					//tmpGroupsDeleted[k++] =this.groups[i];
					isGroupDeleted = true;
				}
			}
		}

		int j = 0;
		//to fill added groups
		Group[] tmpGroupsAdded = new Group[groups.length];
		
		for (int i = 0; i < groups.length; i++)
		{
			if (groups[i].getType() != Group.TYPE_SOCIAL_NETWORK)
			{
				tmpGroups[len++] = groups[i];
				if(!isGroupPresent(groups[i])){
					tmpGroupsAdded[j++] = groups[i];
				}
			}
		}
		if(j!=0){
			this.groupsAdded = Group.trimArray(tmpGroupsAdded, j);
			isGroupAdded = true;
		}
		this.groups = Group.trimArray(tmpGroups, len);
		this.groupsCri = -1;
	}
	
	private  boolean isGroupPresent(Group group){
		for (int i = 0; i < this.groups.length; i++)
		{
			if (this.groups[i].equals(group))
			{
				return true;
			}
		}
		return false;
	}
	
	
	private void setGroups(long[] groupIds)
	{
		// these are all ways of the communicating the contact is not in any groups (anymore)
		if ((groupIds == null) || (groupIds.length == 0) || ((groupIds.length == 1) && (groupIds[0] == -1)))
		{
			groups = new Group[0];
		}
		else
		{
			groups = new Group[groupIds.length];
			for (int i = 0; i < groups.length; i++)
			{
				groups[i] = Group.manager.findGroupById(groupIds[i]);
			}
		}
	}
	
	/**
	 * Gets the standard and custom groups this contact is in.
	 */
	public Group[] getGroups()
	{
		Group[] selection = new Group[groups.length];
		int len = 0;
		for (int i = 0; i < groups.length; i++)
		{
			Group candidate = groups[i];
			if (candidate.getType() != Group.TYPE_SOCIAL_NETWORK)
			{
				selection[len++] = candidate;
			}
		}
		return Group.trimArray(selection, len);
	}
	
	/**
	 * Checks if this contact is in the given group.
	 */
	public boolean inGroup(Group group)
	{
		boolean found = false;
		for (int i = 0; (i < groups.length) && !found; i++)
		{
			found = (groups[i].equals(group));
		}
		return found;
	}
	
	public boolean hasGroupChanges(long startOfSession)
	{
		return !((groupsCri == 0) || (groupsCri > startOfSession));
	}
	
	public long[] getGroupIds()
	{
		if (groups == null)
		{
			return new long[0];
		}
		else
		{
			long[] groupIds = new long[groups.length];
			for (int i = 0; i < groupIds.length; i++)
			{
				groupIds[i] = groups[i].getGroupId();
			}
			return groupIds;
		}
	}
	
	public long[] getAddedGroupIds()
	{
		if (this.groupsAdded == null)
		{
			return new long[0];
		}
		else
		{
			long[] groupIds = new long[this.groupsAdded.length];
			for (int i = 0; i < groupIds.length; i++)
			{
				groupIds[i] = this.groupsAdded[i].getGroupId();
			}
			return groupIds;
		}
	}
		
	public void setCris(long startOfSession, int contactChangesRequestId, int groupChangesRequestId)
	{
		checkLock();
		if (sabId <= 0)
		{
			// no group changes sent
		}
		else
		{
			long cri = startOfSession + groupChangesRequestId * MAX_NUMBER_OF_MULTIPLE_VALUES_PER_FIELD_PER_REQUEST;
			if ((groupsCri == 0) || (groupsCri > startOfSession))
			{
				// no change, or already sent
			}
			else
			{
				groupsCri = cri;
			}	
		}
		super.setCris(startOfSession, contactChangesRequestId, groupChangesRequestId);
	}
	
	public void resetGroupCri(long startOfSession, int requestId)
	{
		requestId *= MAX_NUMBER_OF_MULTIPLE_VALUES_PER_FIELD_PER_REQUEST;
		
		checkLock();
		long cri = startOfSession + requestId;
		
		if (groupsCri == cri)
		{
			groupsCri = 0;
		}
		commit(true, true, true);
	}
	
	public Channel getPrimaryChatChannel()
	{
		return getPrimaryChannel(Identity.TYPE_IM_ACCOUNT, Channel.TYPE_CHAT);
	}

	public void fillFromSimple(ManagedProfileRecord record) 
	{
		syncToNab = record.syncToNab();
		
		setGroups(record.getGroups());
		groupsCri = record.getGroupsCri();
		
		super.fillFromSimple(record);
	}

	public void fill(ManagedProfileRecord record) 
	{
		record.setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
		
		record.setSyncToNab(syncToNab);
		
		if (groups.length == 0)
		{
			record.setGroups(null);
		}
		else
		{
			record.setGroups(getGroupIds());
		}
		record.setGroupsCri(groupsCri);
		
		super.fill(record);
	}

	protected void fillFromServiceObject0(ContactChanges serviceObject)
	{
		//#debug syncdetails
		System.out.println("Overwrite CAB with SAB");
		
		//#debug syncdetails
		System.out.println("Before: " + this);
		
		sabId = serviceObject.getContactId();
		
		if (nowPlusMember == 0)
		{
			if (serviceObject.getUserId() == -1)
			{
				userId = 0;
				nowPlusMember = NOWPLUS_CONTACT;
			}
			else
			{
				userId = serviceObject.getUserId();
				nowPlusMember = NOWPLUS_MEMBER;
			}
		}
		if ((nowPlusMember == NOWPLUS_MEMBER) && (serviceObject.getFriendshipState() == ContactChanges.CONNECTED))
		{
			nowPlusMember = NOWPLUS_CONNECTED_MEMBER;
			fillImAccountFromServiceObject(Identity.NOWPLUS_ACCOUNT_SABDETAILID, false, ExternalNetwork.VODAFONE_360, Identity.NOWPLUS_ACCOUNT_LABEL, true);
		}
		if ((nowPlusMember == NOWPLUS_CONNECTED_MEMBER) && (serviceObject.getFriendshipState() == ContactChanges.DISCONNECTED))
		{
			nowPlusMember = NOWPLUS_MEMBER;
			fillImAccountFromServiceObject(Identity.NOWPLUS_ACCOUNT_SABDETAILID, true, ExternalNetwork.VODAFONE_360, Identity.NOWPLUS_ACCOUNT_LABEL, true);
		}

		long[] groupIds = serviceObject.getGroupIDs();
		if (groupIds == null)
		{
			// no change
		}
		else
		{
			setGroups(groupIds);
			groupsCri = 0;
		}
		
		super.fillFromServiceObject0(serviceObject);
		
		// previous hack
//		if (getPrimaryCallChannel() != null)
//		{
//			syncToNab = true;
//		}

		char charSyncToPhone = serviceObject.getSyncToPhone();
		if(charSyncToPhone==ContactChanges.C_UNCHANGED)
		{
			// no change 
		}
		else
		{
			syncToNab = (charSyncToPhone == ContactChanges.C_TRUE);
		}
		
		//#debug syncdetails
		System.out.println("After : " + this);
	}
	
	public boolean fill(ContactChanges serviceObject, long startOfSession)
	{
		if (sabId <= 0) // new contact
		{
			serviceObject.setContactId(ContactChanges.UNKNOWN_DETAIL_ID);
		}
		else
		{
			serviceObject.setContactId(sabId);
		}
		return super.fill(serviceObject, startOfSession);
	}

	/**
	 * Fills the given array with this contact's groups, starting from len.
	 * @return the new value of len.
	 */
	public int fillGroups(long[] sabIds, long[] groupIds, int len, long startOfSession)
	{
		if ((groupsCri == 0) || (groupsCri > startOfSession))
		{
			// no change, or already sent
		}
		else
		{
			if (groups.length == 0)
			{
				sabIds[len] = sabId;
				groupIds[len] = 0;
				len++;
			}
			else
			{
				for (int i = 0; i < groups.length; i++)
				{
					sabIds[len] = sabId;
					groupIds[len] = groups[i].getGroupId();
					len++;
				}
			}
		}
		return len;
	}
	
	//#mdebug error
	public String toString() 
	{
		return "ContactProfile[syncToNab=" + syncToNab
			+ ",cabId=" + cabId
			+ ",sabId=" + sabId
			+ ",userId=" + userId
			+ ",nowPlusMember=" + nowPlusMember
			+ ",nowPlusPresence=" + nowPlusPresence
			+ ",profileImage=" + profileImage
			+ ",nickname=" + nickname
			+ ",nicknameCri=" + nicknameCri
			+ ",firstName=" + firstName
			+ ",middleNames=" + middleNames
			+ ",lastName=" + lastName
			+ ",nameCri=" + nameCri
			+ ",yearOfBirth=" + yearOfBirth
			+ ",monthOfBirth=" + monthOfBirth
			+ ",dayOfBirth=" + dayOfBirth
			+ ",dateOfBirthCri=" + dateOfBirthCri
			+ ",identities=" + ArrayUtils.toString(identities)
			+ ",addresses=" + ArrayUtils.toString(addresses)			
			+ ",title=" + title
			+ ",titleCri=" + titleCri
			+ ",department=" + department
			+ ",organisation=" + organisation
			+ ",organisationCri=" + organisationCri
			+ ",status=" + status
			+ ",statusCri=" + statusCri
			+ "]";
	}
	//#enddebug
	
	/**
	 * Extends an array.
	 */
	public static ContactProfile[] extendArray(ContactProfile[] src)
	{
		ContactProfile[] dst = new ContactProfile[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}
	

	public boolean isGroupAdded(){
		return isGroupAdded;
	}
	
	public boolean isGroupDeleted(){
		return isGroupDeleted;
	}
}
