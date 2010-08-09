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
 * An index node, that is a collection of (partial key,index) pairs.
 * @author Mark
 */
class IndexNode extends Index
{	
	private char[] keys;
	private Index[] indices;
	
	public IndexNode()
	{
		keys = new char[0];
		indices = new Index[0];
	}
	
	public Object get(String fullKey, int i)
	{
		char key = (i < fullKey.length()) ? fullKey.charAt(i) : ' ';

		Index index = null;
		for (int j = 0; (j < keys.length) && (index == null); j++)
		{
			if (key == keys[j])
			{
				index = indices[j];
			}
		}
		return (index == null) ? null : index.get(fullKey, i + 1);
	}
	
	public Index set(String fullKey, int i, Object element)
	{
		char key = (i < fullKey.length()) ? fullKey.charAt(i) : ' ';
		
		for (int j = 0; j < keys.length; j++)
		{
			if (key == keys[j])
			{
				indices[j] = indices[j].set(fullKey, i + 1, element);
				return this;
			}
		}
		
		Index index = new IndexLeaf();
		index = index.set(fullKey, i + 1, element);
		
		keys = ArrayUtils.trimArray(keys, keys.length, key);
		indices = Index.trimArray(indices, indices.length, index);
		return this;
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
			sb.append(indices[0]);
			for (int j = 1; j < keys.length; j++)
			{
				sb.append(',');
				sb.append(keys[j]);
				sb.append('=');
				sb.append(indices[j]);
			}
		}
		sb.append(")");
		return sb.toString();
	}
	//#enddebug

}
