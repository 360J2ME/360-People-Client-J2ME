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
package com.zyb.nowplus.business.domain.orders;

import de.enough.polish.util.Locale;

/**
 * Describes an ordering of contacts by last name, first name.
 */
public class LastFirstOrder extends Order 
{
	public int getType()
	{
		return TYPE_LAST_FIRST;
	}

	public String getName()
	{			
		return Locale.get("nowplus.client.java.settings.sortcontacts.lastfirst");		
	}

	public String compileFullName(String firstName, String middleNames, String lastName)
	{
		return collator.compileFullName(lastName, middleNames, firstName);
	}

	public boolean equals(Object o)
	{
		return (o instanceof LastFirstOrder);
	}
	
	//#mdebug error
	public String toString()
	{
		return "LastFirstOrder["
			+ "]";
	}
	//#enddebug
}
