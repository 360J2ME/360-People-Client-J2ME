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
package com.zyb.util;

/**
 * An index leaf, that is a collection of (partial key,element) pairs.
 * @author Mark
 */
class IndexLeaf extends Index
{
	private String[] keys;
	private Object[] elements;
	
	public IndexLeaf()
	{
		keys = new String[0];
		elements = new Object[0];
	}
	
	public Object get(String fullKey, int i)
	{
		String key = (i < fullKey.length()) ? fullKey.substring(i) : "";
		
		Object element = null;
		for (int j = 0; (j < keys.length) && (element == null); j++)
		{
			if (key.equals(keys[j]))
			{
				element = elements[j];
			}
		}
		return element;
	}
	
	public Index set(String fullKey, int i, Object element)
	{
		String key = (i < fullKey.length()) ? fullKey.substring(i) : "";
		
		for (int j = 0; j < keys.length; j++)
		{
			if (key.equals(keys[j]))
			{
				elements[j] = element;
				return this;
			}
		}
		
		if (keys.length < MAX_SIZE)
		{
			keys = ArrayUtils.trimArray(keys, keys.length, key);
			elements = ArrayUtils.trimArray(elements, elements.length, element);
			return this;
		}
		
		Index index = new IndexNode();
		for (int j = 0; j < keys.length; j++)
		{
			index = index.set(keys[j], 0, elements[j]);
		}
		index = index.set(key, 0, element);
		return index;
	}
	
	//#mdebug error
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		if (keys.length > 0)
		{
			sb.append(keys[0]);
			sb.append('=');
			sb.append(elements[0]);
			for (int j = 1; j < keys.length; j++)
			{
				sb.append(',');
				sb.append(keys[j]);
				sb.append('=');
				sb.append(elements[j]);
			}			
		}
		sb.append(")");
		return sb.toString();
	}	
	//#enddebug
}
