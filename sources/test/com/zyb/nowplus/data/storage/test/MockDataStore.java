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

import com.zyb.nowplus.data.storage.DataRecord;
import com.zyb.nowplus.data.storage.DataStore;

import de.enough.polish.util.IntHashMap;

public class MockDataStore implements DataStore
{
	private boolean open;
	
	private final IntHashMap records;
	
	public MockDataStore()
	{
		records = new IntHashMap();
	}
	
	// implementation of DataStore
	
	public void open(String name, boolean createIfNecessary) 
	{
		open = true;
	}

	public void close() 
	{
		open = false;
	}
	
	public int getNumberOfRecords() 
	{
		return records.size();
	}

	public boolean hasRecord(long objectId)
	{
		return records.containsKey((int) objectId);
	}
	
	public Object getShortRecord(int recordIndex) 
	{
		return records.values()[recordIndex];
	}

	public Object getFullRecord(long objectId) 
	{
		return records.get((int) objectId);
	}
	
	public void insert(DataRecord object) 
	{
		update(object);
	}

	public void update(DataRecord object) 
	{
		records.put((int) object.getId(), object);
	}
	
	public void delete(long recordId) 
	{
		records.remove((int) recordId);
	}

	// for testing
	
	public void initialise(DataRecord[] records)
	{
		for (int i = 0; i < records.length; i++)
		{
			insert(records[i]);
		}
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public void wipe()
	{
		records.clear();
	}
	
	public boolean isFillingUp()
	{
		return false;
	}
	
	public String toString()
	{
		return "MockDataStore["
			+ "]";
	}
}
