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

import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.InvalidValueException;
import com.zyb.nowplus.business.domain.ManagedProfileRecord;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.data.storage.DataRecord;
import com.zyb.nowplus.data.storage.DataStore;

import de.enough.polish.util.IntHashMap;

/**
 * Temporary data store.
 * Note: the objectIds used in the TmpDataStore should fit in int.
 */
public class TmpDataStore implements DataStore
{
	private IntHashMap records;
	
	public void open(String name, boolean createIfNecessary) 
	{
		records = new IntHashMap();
		
		ExternalNetwork googleNetwork = new ExternalNetwork("", "google", "GoogleTalk", new String[] {"chat"});
		ExternalNetwork icqNetwork = new ExternalNetwork("", "icq", "ICQ", new String[] {"chat"});
		ExternalNetwork facebookNetwork = new ExternalNetwork("", "facebook.com", "Facebook", new String[0]);
		ManagedProfileRecord record = new ManagedProfileRecord();
		record.setType(ManagedProfileRecord.TYPE_MY_PROFILE);
		record.setCabId(201);
		record.setFirstName("Louis");
		record.setLastName("Couperus");
		record.setStatus("Pondering on things gone by");
		record.setStatusSourceNetworkId("facebook.com");
		record.setDayOfBirth(20);
		record.setMonthOfBirth(3);
		record.setYearOfBirth(1977);
		record.setAddresses(new Address[]{Address.createAddress(Address.TYPE_HOME, null,"street", null, "town", "postcode", "region", "country"),Address.createAddress(Address.TYPE_WORK,null, "street", null, "town", "postcode", "region", "country")});
		record.setDepartment("department");
		insert(record);
		
		for (int i = 1; i <= 1000; i++)
		{
			record = new ManagedProfileRecord();
			record.setType(ManagedProfileRecord.TYPE_CONTACT_PROFILE);
			record.setCabId(i);
			record.setLastName("Contact " + i);
			if(i%3==0)
			{
				record.setStatus("Status "+ i );
				record.setStatusSourceNetworkId("google");
			}
			
			switch (i)
			{
				case 1:
				record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
				break;
				
				case 2:
				record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
				record.setGroups(null); // TODO new long[] {1, 2001});
				break;
				
				case 3:
				record.setGroups(null); // TODO new long[] {1, 2000});
				record.setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
				break;	
				
				case 4:
				record.setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
				record.setGroups(null); // TODO new long[] {2, 2001});
				break;
				
				case 5:
				record.setGroups(null); // TODO new long[] {2, 2000});
				break;				
				
				case 6:
				record.setGroups(null); // TODO new long[] {3, 2001});
				break;				
				
				case 7:
				record.setGroups(null); // TODO new long[] {3, 2000});
				break;	
				
				case 11:
				record.setFirstName("Anders");
				record.setLastName("Andersen");
				break;	

				case 12:
				record.setFirstName("Anders");
				record.setLastName("Bjornsen");
				break;	

				case 13:
				record.setFirstName("Bjorn");
				record.setLastName("Andersen");
				break;	

				case 14:
				record.setFirstName("Bjorn");
				record.setLastName("Bjornsen");
				break;					
				
				case 101:
				record.setUserId(10001);
				record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
				Identity[] identities = new Identity[6];
				identities[0] = Identity.createPhoneNumber(Identity.SUBTYPE_HOME, "00441487711278", true);
				identities[1] = Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "00447595514712", false);
				try
				{
				identities[2] = Identity.createEmail(Identity.SUBTYPE_WORK, "mark.h@zyb.com", false);
				}
				catch (InvalidValueException e)
				{
				}
				identities[3] = Identity.createImAccount(googleNetwork, "mark.hoogenboom@googlemail.com", false, 0);
				identities[4] = Identity.createImAccount(icqNetwork, "mark_hoogenboom", true, 0);
				identities[5] = Identity.createSnAccount(facebookNetwork, "mark.hoogenboom", "http://m.facebook.com", 0);
				record.setIdentities(identities);
				record.setUrl(Identity.createUrl("http://zyb.com/mhoogenboom"));
				record.setGroups(null); // TODO new long[] {3});
				Note[] notes = {new Note("Line 1/nLine 2/nLine 3/nLine 44444444444444444444444444444444444444444444444444444444444", -1)};
				record.setNotes(notes);
				record.setNowPlusMember(ContactProfile.NOWPLUS_CONNECTED_MEMBER);
				Address[] addresses2 = new Address[1];
				addresses2[0] = Address.createAddress(Address.TYPE_HOME,null, "Stefansgade 12345, 4.th", null, "Copenhagen North", "2200", "Sealand", "Denmark");
				record.setAddresses(addresses2);
				record.setOrganisation("Muffins Incorporated");
				record.setTitle("Chocolate Gangster");
				record.setStatus("Telling the world I'm drinking a cup of tea.");
				record.setStatusSourceNetworkId("facebook.com");
				break;
				
				case 102:
				record.setUserId(10002);
				record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
				identities = new Identity[6];
				identities[0] = Identity.createPhoneNumber(Identity.SUBTYPE_HOME, "00441487711278535345345345345", true);
				identities[1] = Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "004475955147123543543543534534", false);
				try
				{
				identities[2] = Identity.createEmail(Identity.SUBTYPE_WORK, "mark.mark.mark.mark.mark.mark.mark.h@zyb.com", false);
				}
				catch (InvalidValueException e)
				{
				}
				identities[3] = Identity.createImAccount(googleNetwork, "mark.hoogenboom@googlemail.com", false, 0);
				identities[4] = Identity.createImAccount(icqNetwork, "mark_hoogenboom", true, 0);
				identities[5] = Identity.createSnAccount(facebookNetwork, "mark.hoogenboom", "http://m.facebook.com", 0);
				record.setIdentities(identities);
				record.setUrl(Identity.createUrl("http://zyb.com/mhoogenboom"));
				record.setStatus("Lorem ipsum dolor sit amet consectetuer nec vitae accumsan Curabitur lacinia. Enim Curabitur In sagittis eleifend a Proin Vestibulum");
				record.setGroups(null); // TODO new long[] {1});
				Note[] notes2 = {new Note("Line 1 asdsa  asdas  asdas asd/nLine 233333333333333 33333/nLine 3/nLine 44444444444444444444444444444444444444444444444444444444444", -1)};
				record.setNotes(notes2);
				record.setNowPlusMember(ContactProfile.NOWPLUS_CONTACT);
				Address[] addresses = new Address[1];
				addresses[0] = Address.createAddress(Address.TYPE_HOME,null, "Weizenkampstr. 666", null, "Bremen", "28199", "Neustadt", "Germany");
				record.setAddresses(addresses);
				record.setStatusSourceNetworkId("facebook.com");
				break;
				
				case 103:
					record.setLastName("b contact");
					identities = new Identity[1];
					identities[0] = Identity.createPhoneNumber(Identity.SUBTYPE_MOBILE, "004475955147123543543543534534", false);
					record.setIdentities(identities);
					break;

				case 104:
					record.setLastName(record.getLastName()+" 1 2");
					record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
					break;

				case 105:
					record.setLastName(record.getLastName()+" 1 2 3 4");
					record.setNowPlusPresence(Channel.PRESENCE_INVISIBLE);
					break;
				
				case 106:
					record.setLastName(record.getLastName()+" 1 2 3 4 5 6");
					record.setNowPlusPresence(Channel.PRESENCE_OFFLINE);
					break;

				case 107:
					record.setLastName(record.getLastName()+" 1 2 3 4 5 6 7 8");
					record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
					break;

				case 108:
					record.setLastName(record.getLastName()+" 1 2 3 4 5 6 7 8 9 0");
					record.setStatus("Lorem ipsum dolor sit amet consectetuer nec vitae accumsan Curabitur lacinia. Enim Curabitur In sagittis eleifend a Proin Vestibulum");
					record.setStatusSourceNetworkId("nowplus");
					record.setNowPlusPresence(Channel.PRESENCE_OFFLINE);
					break;
				
				case 109:
					record.setLastName(record.getLastName()+" 1 2 3 4 5 6 7 8 9 0 1 2");
					record.setNowPlusPresence(Channel.PRESENCE_ONLINE);
					break;
				
				case 110:
					record.setLastName(record.getLastName()+" 1 2 3 4 5 6 7 8 9 0 1 2 3 4 ");
					record.setNowPlusPresence(Channel.PRESENCE_OFFLINE);
					break;
			}
			insert(record);
		}
	}

	public void close() 
	{
		records = null;
	}

	public void delete()
	{
		records = null;
	}
	
	public int getNumberOfRecords() 
	{
		return records.size();
	}

	public boolean hasRecord(long objectId)
	{
		return records.containsKey((int) objectId);
	}
	
	public Object getShortRecord(int recordIndex) 
	{
		return records.values()[recordIndex];
	}

	public Object getFullRecord(long objectId) 
	{
		return records.get((int) objectId);
	}

	public void insert(DataRecord object) 
	{
		update(object);
	}

	public void update(DataRecord object) 
	{
		records.put((int) object.getId(), object);
	}
	
	public void delete(long recordId) 
	{
		records.remove((int) recordId);
	}
	
	public boolean isFillingUp() 
	{
		return false;
	}
	
	//#mdebug error
	public String toString()
	{
		return "MockDataStore["
			+ "]";
	}
	//#enddebug

}
