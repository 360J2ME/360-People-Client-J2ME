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


package com.zyb.util.test;

import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.PrioritizedReference;
import com.zyb.util.UsedMemory;

public class PrioritizedReferencesTest extends TestCase
{
	private static final int MEM_OBJ_SIZE = 40000;
	
	public class MemoryTestObject implements UsedMemory{
		public int memSize = MEM_OBJ_SIZE;
		public int getUsedMemory()
		{
			return memSize;
		}
	}
	
	public void setUp()
	{
		PrioritizedReference.releaseResources();
		PrioritizedReference.setMaxMemoryAllowed(MEM_OBJ_SIZE*10);
	}
	
	/*
	 * Purpose is to test if objects are created and stored internally correct
	 * */
	public void testObjectCreation() throws Exception
	{
		MemoryTestObject tmpMemObj = new MemoryTestObject();
		PrioritizedReference tmpObj = new PrioritizedReference(tmpMemObj);
		Assert.assertEquals(true, tmpObj.get() == tmpMemObj);
	}

	/*
	 * Purpose is to test if multiple objects are created and stored internally correct
	 * and object references are cleared when creating more than one what is allowed for maximum
	 * */
	public void testObjectFlushAtCreationTime() throws Exception
	{
		int SIZE = 15;

		MemoryTestObject tmpMemObjs[] = new MemoryTestObject[SIZE];
		PrioritizedReference tmpObjs[] = new PrioritizedReference[SIZE];

		for(int i=0; i<SIZE; i++)
		{
			tmpMemObjs[i] = new MemoryTestObject();
			tmpObjs[i] = new PrioritizedReference(tmpMemObjs[i]);
		}

		//First 5 are flushed to give room for the last 10
		for(int i=0; i<5; i++)
			Assert.assertEquals(true, null == tmpObjs[i].get());

		for(int i=5; i<SIZE; i++)
			Assert.assertEquals(true, tmpMemObjs[i] == tmpObjs[i].get());
	}
	
	
	/*
	 * Purpose is to test if objects are cleared correctly when memory size of the internal objects change
	 * */
	public void testObjectFlushWhenMemoryChanges() throws Exception
	{
		int SIZE = 9;

		MemoryTestObject tmpMemObjs[] = new MemoryTestObject[SIZE];
		PrioritizedReference tmpObjs[] = new PrioritizedReference[SIZE];

		for(int i=0; i<SIZE; i++)
		{
			tmpMemObjs[i] = new MemoryTestObject();
			tmpObjs[i] = new PrioritizedReference(tmpMemObjs[i]);
		}

		//Test everything is correct
		for(int i=0; i<SIZE; i++)
			Assert.assertEquals(true, tmpMemObjs[i] == tmpObjs[i].get());

		tmpMemObjs[0].memSize = tmpMemObjs[0].memSize*2+1;//Last in the list
		Assert.assertEquals(true, tmpMemObjs[0] == tmpObjs[0].get());

		//#1 was the least recently accesses
		Assert.assertEquals(true, null == tmpObjs[1].get());

		//But not the rest
		tmpMemObjs[1] = null;
		for(int i=0; i<SIZE-1; i++)
			Assert.assertEquals(true, tmpMemObjs[i] == tmpObjs[i].get());
	}
	
	public void tearDown()
	{
		PrioritizedReference.releaseResources();
	}
}
