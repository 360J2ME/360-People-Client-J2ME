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

import com.zyb.nowplus.data.protocol.types.ContactChanges;
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

/**
 * Represents the profile of the user of the application.
 */
public class MyProfile extends ManagedProfile
{
	public boolean syncToNab()
	{
		return false;
	}
	
	public void setStatus(String status)
	{
		checkLock();
		if (status == null)
		{
			this.status = null;
			this.statusSource = null;
			this.statusCri = -1;
		}
		else
		{
			this.status = status;
			this.statusSource = ExternalNetwork.manager.findNetworkById(ExternalNetwork.VODAFONE_360);
			this.statusCri = -1;
		}
	}
		
	/**
	 * Gets an identity of type SOCIAL_NETWORK and IM_ACCOUNT with
	 * the given network.
	 */
	public Identity getAccount(ExternalNetwork network)
	{
		return getAccount(network.getNetworkId());
	}
	
	public Identity getAccount(String networkId)
	{
		Identity selection = null;
		for (int i = 0; (selection == null) && (i < identities.length); i++)
		{
			Identity candidate = identities[i];
			if ((candidate != null) && !candidate.isEmpty() && candidate.isWebAccount()
					&& HashUtil.equals(candidate.getNetworkId(), networkId))
			{
				selection = candidate;
			}
		}
		return selection;
	}
	
	public Channel getPrimaryChatChannel()
	{
		return null; // my profile doesn't have a primary chat channel
	}
	
	public void fill(ManagedProfileRecord record) 
	{
		record.setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		
		super.fill(record);
	}
	
	protected void fillFromServiceObject0(ContactChanges serviceObject)
	{		
		if (serviceObject.getUserId() != -1)
		{
			userId = serviceObject.getUserId();
		}
		
		if (serviceObject.getContactId() != -1)
		{
			sabId = serviceObject.getContactId();
		}
		
		nowPlusMember = NOWPLUS_ME;
		
		super.fillFromServiceObject0(serviceObject);
	}
	
	/**
	 * Sets webaccounts (social network and im accounts).
	 */
	public void setWebaccounts(Identity[] newIdentities)
	{
		load(true);
		try
		{
			lock();
			setWebaccounts0(newIdentities);
			commit(true, false, false);
		}
		catch (LockException e)
		{
			//#debug error
			System.out.println("Can't update webaccounts because profile is being updated." + e);
		}
		unload();	
	}
	
	private void setWebaccounts0(Identity[] newIdentities)
	{
		// remove the old webaccounts
		for (int i = 0; i < identities.length; i++)
		{
			if ((identities[i] != null) && identities[i].isWebAccount())
			{
				identities[i] = null;
			}
		}
		// add the new webaccounts
		for (int i = 0; i < newIdentities.length; i++)
		{
			addIdentity0(newIdentities[i]);
		}
	}
	
	//#mdebug error
	public String toString() 
	{
		return "MyProfile[cabId=" + cabId
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
}
