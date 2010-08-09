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

import de.enough.polish.util.ArrayList;

/**
 * @author Jens Vesti
 *
 * PrioritizedReference is similar to CLDCs SoftReference and WeakReference but flushes 
 * objects based on when they were used the last time and how much memory all of the object consume.
 * Objects using this class must implement <CODE>UsedMemory</CODE>
 */
public class PrioritizedReference
{

	//TODO set MAX_MEMORY as a preprocess variable
	//#todo set MAX_MEMORY as a preprocess variable

	private static int maxMemoryAllowed = 500000; 

	public static int getMaxMemoryAllowed() {
		return maxMemoryAllowed;
	}

	public static void setMaxMemoryAllowed(int maxMemoryAllowed) {
		PrioritizedReference.maxMemoryAllowed = maxMemoryAllowed;
	}

	private UsedMemory reference;
	
	private static final ArrayList allReferences = new ArrayList();
	
	/**
	 * Constructor 
	 * @param reference is the object we which to "soft reference"  
	 */
	public PrioritizedReference(final UsedMemory reference)
	{
		this.reference = reference;
		synchronized(allReferences)
		{
			allReferences.add(this);
			cleanup();
		}
	}
	
	/**
	 * @return the object we registered when creating the <CODE>PrioritizedObject</CODE> object 
	 */
	public Object get()
	{
		synchronized(allReferences)
		{
			allReferences.remove(this);
			allReferences.add(this);
			cleanup();
			return this.reference;
		}
	}

	/**
	 * Cleans up and nullifies references if needed until we are below memory threshold 
	 */
	public void cleanup()
	{
		synchronized(allReferences)
		{
			while(exceedsMaxMemory())
			{
				((PrioritizedReference)allReferences.get(0)).reference = null;
				allReferences.remove(0);
			}
		}
	}
	
	/**
	 * @return true if memory consumed by objects registered exceed a predefined level 
	 */
	private boolean exceedsMaxMemory() {
		final int size = allReferences.size();
		int memoryUsed = 0;
		for(int i=0; i<size; i++)
		{
			Object obj = allReferences.get(i);
			if(obj != null)
			{
				if(((PrioritizedReference)obj).reference != null)
				{
					memoryUsed += ((PrioritizedReference)obj).reference.getUsedMemory();
				}
			}
		}
		
		if(memoryUsed>maxMemoryAllowed)
			return true;
		
		return false;
	}

	/**
	 * Clears the list so the gc can reclaim memory
	 */
	public static void releaseResources() 
	{
		synchronized(allReferences)
		{
			allReferences.clear();
		}
	}

	//#mdebug error
	public String toString() 
	{
		return "PrioritizedReference[reference=" + reference + "]";
	}
	//#enddebug

}
