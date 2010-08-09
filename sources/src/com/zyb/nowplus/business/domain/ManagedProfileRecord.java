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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.zyb.nowplus.data.storage.DataRecord;
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

import de.enough.polish.io.Serializer;

/**
 * A managed profile stored in a data store.
 */
public class ManagedProfileRecord implements DataRecord 
{
	public static final int TYPE_MY_PROFILE = 1;
	public static final int TYPE_CONTACT_PROFILE = 2;
	
	private int type;
	private boolean syncToNab;
	private long sabId;
	private long cabId;
	private long userId;
	private int nowPlusMember;
	private int nowPlusPresence;
	private String profileImageURL;
	private String nickname;
	private long nicknameCri;
	private String firstName;
	private String middleNames;
	private String lastName;
	private long nameCri;
	private int yearOfBirth;
	private int monthOfBirth;
	private int dayOfBirth;
	private long dateOfBirthCri;
	private long[] groupIds;
	private long groupsCri;
	private Identity[] identities;
	private Identity url;
	private Address[] addresses;
	private String title;
	private long titleCri;
	private String department;
	private String organisation;
	private long organisationCri;
	private Note[] notes;
	private String status;
	private String statusSourceNetworkId;
	private long statusCri;
	
	public ManagedProfileRecord()
	{
		groupIds = new long[0];
	}

	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
	
	public long getId()
	{
		return cabId;
	}

	public void setSyncToNab(boolean syncToNab)
	{
		this.syncToNab = syncToNab;
	}
	
	public boolean syncToNab()
	{
		return syncToNab;
	}
	
	public void setSabId(long sabId) 
	{
		this.sabId = sabId;
	}

	public long getSabId() 
	{
		return sabId;
	}
	
	public void setCabId(long cabId)
	{
		this.cabId = cabId;
	}

	public long getCabId() 
	{
		return cabId;
	}

	public void setUserId(long userId) 
	{
		this.userId = userId;
	}

	public long getUserId() 
	{
		return userId;
	}

	public void setNowPlusMember(int nowPlusMember) 
	{
		this.nowPlusMember = nowPlusMember;
	}
	
	public int getNowPlusMember() 
	{
		return nowPlusMember;
	}
	
	public void setNowPlusPresence(int nowPlusPresence) 
	{
		this.nowPlusPresence = nowPlusPresence;
	}
	
	public int getNowPlusPresence() 
	{
		return nowPlusPresence;
	}
	
	public void setProfileImageURL(String url)
	{
		this.profileImageURL = url;
	}
	
	public String getProfileImageURL() 
	{
		return profileImageURL;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public String getNickname() 
	{
		return nickname;
	}

	public void setNicknameCri(long nicknameCri)
	{
		this.nicknameCri = nicknameCri;
	}
	
	public long getNicknameCri()
	{
		return nicknameCri;
	}
	
	public void setFirstName(String firstName) 
	{
		this.firstName = firstName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setMiddleNames(String middleNames)
	{
		this.middleNames = middleNames;
	}
	
	public String getMiddleNames()
	{
		return middleNames;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getLastName() 
	{
		return lastName;
	}

	public void setNameCri(long nameCri)
	{
		this.nameCri = nameCri;
	}
	
	public long getNameCri()
	{
		return nameCri;
	}
		
	public void setYearOfBirth(int yearOfBirth)
	{
		this.yearOfBirth = yearOfBirth;
	}
	
	public int getYearOfBirth()
	{
		return yearOfBirth;
	}
	
	public void setMonthOfBirth(int monthOfBirth)
	{
		this.monthOfBirth = monthOfBirth;
	}
	
	public int getMonthOfBirth()
	{
		return monthOfBirth;
	}
	
	public void setDayOfBirth(int dayOfBirth)
	{
		this.dayOfBirth = dayOfBirth;
	}
	
	public int getDayOfBirth()
	{
		return dayOfBirth;
	}
	
	public void setDateOfBirthCri(long dateOfBirthCri)
	{
		this.dateOfBirthCri = dateOfBirthCri;
	}
	
	public long getDateOfBirthCri()
	{
		return dateOfBirthCri;
	}
	
	public void setGroups(long[] groupIds) 
	{
		this.groupIds = groupIds;
	}

	public long[] getGroups()
	{
		return groupIds;
	}
		
	public void setGroupsCri(long groupsCri) 
	{
		this.groupsCri = groupsCri;
	}

	public long getGroupsCri()
	{
		return groupsCri;
	}

	public void setIdentities(Identity[] identities)
	{
		this.identities = identities;
	}
	
	public Identity[] getIdentities()
	{
		return identities;
	}
	
	public void setUrl(Identity url)
	{
		this.url = url;
	}
	
	public Identity getUrl()
	{
		return url;
	}
	
	public void setAddresses(Address[] addresses) {
		this.addresses = addresses;
	}

	public Address[] getAddresses() {
		return addresses;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitleCri(long titleCri)
	{
		this.titleCri = titleCri;
	}
	
	public long getTitleCri()
	{
		return titleCri;
	}
	
	public void setDepartment(String department) 
	{
		this.department = department;
	}

	public String getDepartment()
	{
		return department;
	}
	
	public void setOrganisation(String organisation)
	{
		this.organisation = organisation;
	}

	public String getOrganisation() 
	{
		return organisation;
	}
	
	public void setOrganisationCri(long organisationCri)
	{
		this.organisationCri = organisationCri;
	}
	
	public long getOrganisationCri()
	{
		return organisationCri;
	}

	public void setNotes(Note[] notes)
	{
		this.notes = notes;
	}

	public Note[] getNotes()
	{
		return notes;
	}
	
	public void setStatus(String status) 
	{
		this.status = status;
	}
	
	public String getStatus() 
	{
		return status;
	}
	
	public void setStatusSourceNetworkId(String statusSourceNetworkId) 
	{
		this.statusSourceNetworkId = statusSourceNetworkId;
	}
	
	public String getStatusSourceNetworkId() 
	{
		return statusSourceNetworkId;
	}	
	
	public void setStatusCri(long statusCri)
	{
		this.statusCri = statusCri;
	}
	
	public long getStatusCri()
	{
		return statusCri;
	}
	
	public void release()
	{
		nickname = null;
		nicknameCri = 0;
		profileImageURL = null;
		yearOfBirth = 0;
		monthOfBirth = 0;
		dayOfBirth = 0;
		dateOfBirthCri = 0;
		identities = null;
		url = null;
		addresses = null;
		title = null;
		titleCri = 0;
		organisation = null;
		department = null;
		organisationCri = 0;
		notes = null;
//		status = null;
//		statusSourceNetworkId = null;
//		statusCri = 0;
	}
	
	public void read(DataInputStream in) throws IOException
	{
		type = in.readInt();
		syncToNab = in.readBoolean();
		sabId = in.readLong();
		cabId = in.readLong();
		userId = in.readLong();
		nowPlusMember = in.readInt();
		nowPlusPresence = in.readInt();
		profileImageURL = (String) Serializer.deserialize(in);
		nickname = (String) Serializer.deserialize(in);
		nicknameCri = in.readLong();
		firstName = (String) Serializer.deserialize(in);
		middleNames = (String) Serializer.deserialize(in);
		lastName = (String) Serializer.deserialize(in);
		nameCri = in.readLong();
		yearOfBirth = in.readInt();
		monthOfBirth = in.readInt();
		dayOfBirth = in.readInt();
		dateOfBirthCri = in.readLong();
		groupIds = (long[]) Serializer.deserialize(in);
		groupsCri = in.readLong();
		identities = ArrayUtils.deserializeIdentityArray(in);
		url = (Identity) Serializer.deserialize(in);
		addresses = ArrayUtils.deserializeAddressArray(in);
		title = (String) Serializer.deserialize(in);
		titleCri = in.readLong();
		department = (String) Serializer.deserialize(in);
		organisation = (String) Serializer.deserialize(in);
		organisationCri = in.readLong();
		notes = ArrayUtils.deserializeNoteArray(in);
		status = (String) Serializer.deserialize(in);
		statusSourceNetworkId = (String) Serializer.deserialize(in);
		statusCri = in.readLong();
	}
	
	public void write(DataOutputStream out) throws IOException 
	{
		out.writeInt(type);
		out.writeBoolean(syncToNab);
		out.writeLong(sabId);
		out.writeLong(cabId);
		out.writeLong(userId);
		out.writeInt(nowPlusMember);
		out.writeInt(nowPlusPresence);
		Serializer.serialize(profileImageURL, out);
		Serializer.serialize(nickname, out);
		out.writeLong(nicknameCri);
		Serializer.serialize(firstName, out);
		Serializer.serialize(middleNames, out);
		Serializer.serialize(lastName, out);
		out.writeLong(nameCri);
		out.writeInt(yearOfBirth);
		out.writeInt(monthOfBirth);
		out.writeInt(dayOfBirth);
		out.writeLong(dateOfBirthCri);
		Serializer.serialize(groupIds, out);
		out.writeLong(groupsCri);
		ArrayUtils.serializeArray(identities, out);
		Serializer.serialize(url, out);
		ArrayUtils.serializeArray(addresses, out);
		Serializer.serialize(title, out);
		out.writeLong(titleCri);
		Serializer.serialize(department, out);
		Serializer.serialize(organisation, out);
		out.writeLong(organisationCri);
		ArrayUtils.serializeArray(notes, out);
		Serializer.serialize(status, out);
		Serializer.serialize(statusSourceNetworkId, out);
		out.writeLong(statusCri);
	}

	public boolean equals(Object o)
	{
		ManagedProfileRecord that = (ManagedProfileRecord) o;
		return (this.type == that.type)
		&& (this.syncToNab == that.syncToNab)
		&& (this.sabId == that.sabId)
		&& (this.cabId == that.cabId)
		&& (this.userId == that.userId)
		&& (this.nowPlusMember == that.nowPlusMember)
		&& (this.nowPlusPresence == that.nowPlusPresence)
		&& HashUtil.equals(this.profileImageURL, that.profileImageURL)
		&& HashUtil.equals(this.nickname, that.nickname)
		&& (this.nicknameCri == that.nicknameCri)
		&& HashUtil.equals(this.firstName, that.firstName)
		&& HashUtil.equals(this.middleNames, that.middleNames)
		&& HashUtil.equals(this.lastName, that.lastName)
		&& (this.nameCri == that.nameCri)
		&& (this.yearOfBirth == that.yearOfBirth)
		&& (this.monthOfBirth == that.monthOfBirth)
		&& (this.dayOfBirth == that.dayOfBirth)
		&& (this.dateOfBirthCri == that.dateOfBirthCri)
		&& HashUtil.equals(this.title, that.title)
		&& (this.titleCri == that.titleCri)
		&& HashUtil.equals(this.department, that.department)
		&& HashUtil.equals(this.organisation, that.organisation)
		&& (this.organisationCri == that.organisationCri)
		&& HashUtil.equals(this.status, that.status)
		&& HashUtil.equals(this.statusSourceNetworkId, that.statusSourceNetworkId)
		&& (this.statusCri == that.statusCri);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ContactRecord[type=" + type
		+ ",syncToNab=" + syncToNab
		+ ",sabId=" + sabId
		+ ",cabId=" + cabId
		+ ",userId=" + userId
		+ ",nowPlusMember=" + nowPlusMember
		+ ",nowPlusPresence=" + nowPlusPresence
		+ ",profileImageURL=" + profileImageURL
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
		+ ",title=" + title
		+ ",titleCri=" + titleCri
		+ ",department=" + department
		+ ",organisation=" + organisation
		+ ",organisationCri=" + organisationCri
		+ ",status=" + status
		+ ",statusSourceNetworkId=" + statusSourceNetworkId
		+ ",statusCri=" + statusCri
		+ "]";
	}	
	//#enddebug
}
