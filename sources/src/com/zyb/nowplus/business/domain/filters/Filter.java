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
 * Describes a selection of contacts.
 */
public abstract class Filter
{
	// When updating filter types, be sure to remove unused type support in class FilterContainer and also remove unused styles and asset resources
	
	public static final int TYPE_ALL = 1;
	public static final int TYPE_ONLINE = 2;
	public static final int TYPE_CONNECTED = 3;
	public static final int TYPE_NATIVE_PHONEBOOK = 4;
	public static final int TYPE_STANDARD_GROUP = 5;
	public static final int TYPE_CUSTOM_GROUP = 6;
	public static final int TYPE_SOCIAL_NETWORK_GROUP = 7;
	public static final int TYPE_NAME = 8;
	
	private final int type;
	
	public Filter(int type)
	{
		this.type = type;
	}
	
	/**
	 * Gets the type of the filter.
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Gets the name of the filter.
	 */
	public abstract String getName();
	
	/**
	 * Gets an identifier for the icon to be shown on this filter.
	 */
	public abstract String getIconId();
	
	/**
	 * Returns true if the given object is in the selection.
	 */
	public abstract boolean accepts(ContactProfile contact);
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Filter[] trimArray(Filter[] src, int len)
	{
		Filter[] dst = new Filter[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
}
