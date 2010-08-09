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

import com.zyb.nowplus.business.domain.Profile;
import com.zyb.util.Collator;

import de.enough.polish.util.Comparator;

/**
 * Describes an ordering of contacts.
 */
public abstract class Order implements Comparator 
{
	public static final int TYPE_FIRST_LAST = 1;
	public static final int TYPE_LAST_FIRST = 2;
	
	protected Collator collator;
	
	public Order()
	{
		this.collator = Collator.getInstance();
	}
	
	/**
	 * Gets the type of the order.
	 */
	public abstract int getType();

	/**
	 * Gets the name of the order.
	 */
	public abstract String getName();

	/**
	 * Compiles the full name consistent with the order.
	 */
	public abstract String compileFullName(String firstName, String middleNames, String lastName);
	
    /**
     * Compares the Profiles
     */
    public int compare(Object o1, Object o2)
    {       	
    	int[] name1 = null;
    	if (o1 instanceof Profile)
    	{
    		Profile profile = (Profile) o1;
    		
    		name1 = profile.getSortName();
    	}

       	int[] name2 = null;
    	if (o2 instanceof Profile)
    	{
    		Profile profile = (Profile) o2;
    		
    		name2 = profile.getSortName();
    	}
    	else
    	if (o2 instanceof int[])
    	{
    		name2 = (int[]) o2;
    	}
 
    	return compareNames(name1, name2);
    }
    
    /**
     * Compares the names
     */
	private int compareNames(int[] name1, int[] name2)
	{
		int c = 0;
		if (name1 == null)
		{
			if (name2 != null)
			{
				c = -1;
			}
		}
		else
		{
			if (name2 == null)
			{
				c = 1;
			}
			else
			{
				for (int i = 0; (c == 0) && (i < name1.length) && (i < name2.length); i++)
				{
					c = name1[i] - name2[i];
				}
				if (c == 0)
				{
					c = name1.length - name2.length;
				}
			}
		}

        return c;
	}
}
