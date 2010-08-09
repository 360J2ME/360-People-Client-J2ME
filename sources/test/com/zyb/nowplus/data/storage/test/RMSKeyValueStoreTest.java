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
package com.zyb.nowplus.data.storage.test;

import com.zyb.nowplus.data.storage.RMSKeyValueStore;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;

public class RMSKeyValueStoreTest extends TestCase
{
	private RMSKeyValueStore store;
	
	public void setUp()
	{
		store = new RMSKeyValueStore();
		
		try 
		{
			store.open("rmskeyvaluestoretest", true);
		} 
		catch (StorageException e) 
		{
			System.out.println("Failed open \"rmskeyvaluestoretest\"");
		}	
		store.delete();		
	}
	
	public void testSetAndGet()
	{
		try
		{
			store.open("rmskeyvaluestoretest", true);

			Assert.assertEquals(0, store.getLongValue(1));
			Assert.assertEquals(0, store.getIntValue(2));
			Assert.assertEquals(false, store.getBooleanValue(3));
			Assert.assertNull(store.getStringValue(4));
			Assert.assertNull(store.getStringArrayValue(5));
			
			store.setStringValue(4, "four");
			
			Assert.assertEquals(0, store.getLongValue(1));
			Assert.assertEquals(0, store.getIntValue(2));
			Assert.assertEquals(false, store.getBooleanValue(3));			
			Assert.assertEquals("four", store.getStringValue(4));
			Assert.assertNull(store.getStringArrayValue(5));

			store.setLongValue(1, 111);
			store.setIntValue(2, 222);
			store.setBooleanValue(3, true);
			store.setStringArrayValue(5, new String[] {"five", "vijf"});
			
			Assert.assertEquals(111, store.getLongValue(1));
			Assert.assertEquals(222, store.getIntValue(2));
			Assert.assertEquals(true, store.getBooleanValue(3));
			Assert.assertEquals("four", store.getStringValue(4));			
			Assert.assertEquals(new String[] {"five", "vijf"}, store.getStringArrayValue(5));		

			store.setLongValue(1, 0);
			store.setIntValue(2, 0);
			store.setBooleanValue(3, false);
			store.setStringValue(4, null);
			store.setStringArrayValue(5, null);
			
			Assert.assertEquals(0, store.getLongValue(1));
			Assert.assertEquals(0, store.getIntValue(2));
			Assert.assertEquals(false, store.getBooleanValue(3));
			Assert.assertNull(store.getStringValue(4));
			Assert.assertNull(store.getStringArrayValue(5));

			store.close();
		}
		catch (StorageException e)
		{
			Assert.fail("Exception: " + e);
		}
	}
	
	public void tearDown()
	{
		store = null;
	}
}
