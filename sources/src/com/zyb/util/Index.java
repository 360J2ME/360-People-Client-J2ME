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
 * An index to provides quick access to an element by a key.
 */
public abstract class Index 
{
	/**
	 * The maximum size of a leaf index.
	 */
	public static int MAX_SIZE = 12;
	
	/**
	 * Creates an empty index.
	 */
	public static Index create()
	{
		return new IndexLeaf();
	}
	
	/**
	 * Sets the element with the given key.
	 * @return The changed index. This may not be the same
	 * as the index this method is called on.
	 */
	public Index set(String key, Object element)
	{
		return set(key, 0, element);
	}
	
	protected abstract Index set(String key, int i, Object element);

	/**
	 * Gets the element with the given key.
	 */	
	public Object get(String key)
	{
		return get(key, 0);
	}
	
	protected abstract Object get(String key, int i);	
	
	/**
	 * Creates a new array of (len + 1) elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1]. Element len will be filled with srcL.
	 */
	public static Index[] trimArray(Index[] src, int len, Index srcL)
	{
		Index[] dst = new Index[len + 1];
		System.arraycopy(src, 0, dst, 0, len);
		dst[len] = srcL;
		return dst;
	}	
}
