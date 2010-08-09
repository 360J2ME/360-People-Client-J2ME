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

/**
 * Describes a selection of contacts by name.
 */
public class NameFilter extends Filter
{
	private final String text;
	
	public NameFilter(String text)
	{
		super(TYPE_NAME);
		this.text = text.trim().toLowerCase();
	}
	
	public String getName()
	{
		return "";
	}
	
	public String getIconId()
	{
		return null;
	}
	
	public boolean accepts(ContactProfile contact)
	{
		return contact.getFullName().toLowerCase().startsWith(text);
	}	
	
	public boolean equals(Object o)
	{
		if (o instanceof NameFilter)
		{
			return ((NameFilter) o).text.equals(this.text);
		}
		else
		{
			return false;
		}
	}	
	
	//#mdebug error
	public String toString()
	{
		return "NameFilter[text=" + text	
			+ "]";
	}
	//#enddebug

}
