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

import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.data.storage.KeyValueStore;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.ArrayUtils;

import de.enough.polish.util.IntHashMap;

public class MockKeyValueStore implements KeyValueStore
{
	private static final String NULL = "$null$";
	
	private boolean open;
	private boolean faulty;
	
	private final IntHashMap records;
	
	public MockKeyValueStore()
	{
		records = new IntHashMap();
	}
	
	// implementation of KeyValueStore
	
	public void open(String name, boolean createIfNecessary) throws StorageException 
	{
		if (faulty)
		{
			throw new StorageException("Faulty Storage");
		}
		open = true;
	}

	public void close() 
	{
		open = false;
	}

	public void setLongValue(int key, long value) 
	{
		records.put(key, new Long(value));
	}

	public long getLongValue(int key) 
	{
		Object value = records.get(key);
		return (value == null) ? 0 : ((Long) value).longValue();
	}
	
	public void setLongArrayValue(int key, long[] value, int len) 
	{
		if ((value == null) || (len == 0))
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, ArrayUtils.trimArray(value, len));
		}
	}
	
	public long[] getLongArrayValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (long[]) value;
	}		
	
	public void setIntValue(int key, int value) 
	{
		records.put(key, new Integer(value));
	}

	public int getIntValue(int key) 
	{
		Object value = records.get(key);
		return (value == null) ? 0 : ((Integer) value).intValue();
	}

	
	public void setIntArrayValue(int key, int[] value, int len) 
	{
		if ((value == null) || (len == 0))
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, ArrayUtils.trimArray(value, len));
		}
	}
	
	public int[] getIntArrayValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (int[]) value;
	}	
	
	public void setBooleanValue(int key, boolean value)
	{
		records.put(key, new Boolean(value));
	}
	
	public boolean getBooleanValue(int key)
	{
		Object value = records.get(key);
		return (value == null) ? false : ((Boolean) value).booleanValue();
	}
	
	public void setStringValue(int key, String value) 
	{
		if (value == null)
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, value);
		}
	}
	
	public String getStringValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (String) value;
	}
	
	public void setStringArrayValue(int key, String[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, value);
		}
	}
	
	public String[] getStringArrayValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (String[]) value;
	}	
	
	public void setGroupArrayValue(int key, Group[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, value);
		}
	}
	
	public Group[] getGroupArrayValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (Group[]) value;
	}	
	
	public void setExternalNetworkArrayValue(int key, ExternalNetwork[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			records.put(key, NULL);
		}
		else
		{
			records.put(key, value);
		}
	}
	
	public ExternalNetwork[] getExternalNetworkArrayValue(int key) 
	{
		Object value = records.get(key);
		return ((value == null) || NULL.equals(value)) ? null : (ExternalNetwork[]) value;
	}
	
	// for testing
	
	public void setFaulty()
	{
		this.faulty = true;
	}	
	
	public boolean isOpen()
	{
		return open;
	}
	
	public void wipe()
	{
		records.clear();
	}
	
	public String toString()
	{
		return "MockKeyValueStore["
			+ "]";
	}	
}
