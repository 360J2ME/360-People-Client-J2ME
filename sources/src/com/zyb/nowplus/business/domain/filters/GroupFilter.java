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
package com.zyb.nowplus.business.domain.filters;

import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.util.HashUtil;

import de.enough.polish.util.Locale;

/**
 * Describes a selection of contacts by group.
 */
public class GroupFilter extends Filter
{
	private final Group group;
	
	public GroupFilter(int type, Group group)
	{
		super(type);
		this.group = group;
	}
	
	public String getName()
	{
		//local strings for standard
		if(group.getName().equals("Family"))
			return Locale.get("nowplus.client.java.peoplepage.people.filter.family");
		else if(group.getName().equals("Friends"))
			return Locale.get("nowplus.client.java.peoplepage.people.filter.friends");
		else if(group.getName().equals("Work"))
			return Locale.get("nowplus.client.java.peoplepage.people.filter.colleages");
		
		return group.getName();
	}

	public String getIconId()
	{
		return group.getNetworkId();
	}

	public boolean accepts(ContactProfile contact)
	{
		return contact.inGroup(group);
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof GroupFilter)
		{
			GroupFilter that = (GroupFilter) o;
			return HashUtil.equals(this.group, that.group);
		}
		else
		{
			return false;
		}
	}
	
	//#mdebug error
	public String toString()
	{
		return "GroupFilter[type=" + getType()
		    + ",group=" + group
			+ "]";
	}	
	//#enddebug

}
