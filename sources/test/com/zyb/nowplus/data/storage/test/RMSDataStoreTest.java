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

import java.util.Date;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.nowplus.data.storage.RMSDataStore;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;

public class RMSDataStoreTest extends TestCase
{
	private RMSDataStore store;
	
	public void setUp()
	{
		try 
		{
			RecordStore.deleteRecordStore("rmsdatastoretest");
		} 
		catch (RecordStoreException e1) 
		{
			System.out.println("Failed to delete \"rmsdatastoretest\"");
		}
		
		store = new RMSDataStore();

		try 
		{
			store.open("rmsdatastoretest", true);
		} 
		catch (StorageException e) 
		{
			System.out.println("Failed to open \"rmsdatastoretest\"");
		}	
	}
	
	public void testInsert()
	{
		try
		{
			// open store, insert record 10001 and close
			store.open("rmsdatastoretest", true);
			
			Assert.assertEquals(0, store.getNumberOfRecords());
			
			store.insert(new TestRecord(10001));
			Assert.assertEquals(1, store.getNumberOfRecords());	

			store.close();
			
			// open store, check short record 10001, check full record 10001 and close
			store.open("rmsdatastoretest", false);
			
			TestRecord record = (TestRecord) store.getShortRecord(0);
			Assert.assertEquals(10001, record.getId());
			
			Assert.assertEquals(37, record.getAttribute1());
			Assert.assertEquals(true, record.getAttribute2());
			Assert.assertEquals("Test", record.getAttribute3());
			Assert.assertEquals(new Date(1000000), record.getAttribute4());

			Assert.assertEquals(0, record.getComplexAttribute1());
			Assert.assertEquals(false, record.getComplexAttribute2());
			Assert.assertNull(record.getComplexAttribute3());
			Assert.assertNull(record.getComplexAttribute4());			

			record = (TestRecord) store.getFullRecord(10001);
			
			Assert.assertEquals(10001, record.getId());
			
			Assert.assertEquals(37, record.getAttribute1());
			Assert.assertEquals(true, record.getAttribute2());
			Assert.assertEquals("Test", record.getAttribute3());
			Assert.assertEquals(new Date(1000000), record.getAttribute4());

			Assert.assertEquals(42, record.getComplexAttribute1());
			Assert.assertEquals(true, record.getComplexAttribute2());
			Assert.assertEquals("Complex Test", record.getComplexAttribute3());
			Assert.assertEquals(new Date(2000000), record.getComplexAttribute4());	

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
