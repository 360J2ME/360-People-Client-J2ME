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
package com.zyb.nowplus.data.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.util.ArrayUtils;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;

import de.enough.polish.io.Serializer;

public class RMSKeyValueStore implements KeyValueStore
{	
	private RecordStore recordStore;
	private String name;
	private int access;
	
	public synchronized void open(String name, boolean createIfNecessary) throws StorageException
	{
		if (recordStore != null)
		{
			close();
		}
		try 
		{
			this.name = name;
			recordStore = RecordStore.openRecordStore(name, createIfNecessary);
			access = 0;
		} 
		catch (RecordStoreException e) 
		{
			throw new StorageException("Failed to open data store " + name, e);
		} 
	}
	
	public synchronized void close()
	{
		if (recordStore != null)
		{
			try 
			{
				recordStore.closeRecordStore();
			} 
			catch (RecordStoreException e) 
			{
				//#debug error
				System.out.println("Failed to close data store" + e);
			}
			recordStore = null;
		}
	}
			
	public synchronized void delete()
	{
		try 
		{
			close();
			RecordStore.deleteRecordStore(name);
		}
		catch (RecordStoreException e)
		{
			//#debug error
			System.out.println("Failed to delete data store" + e);
		}
	}

	public void setLongValue(int key, long value) 
	{
		if (value == 0)
		{
			setByteValue(key, null);
		}
		else
		{
			byte[] bytes = new byte[8];
			bytes[0] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[1] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[2] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[3] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[4] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[5] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[6] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[7] = (byte) (value & 0x000000ff);
			setByteValue(key, bytes);
		}
	}
	
	public long getLongValue(int key)
	{
		int value = 0;

		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			value = (bytes[3] & 0x000000ff);
			value <<= 8;
			value |= (bytes[2] & 0x000000ff);				
			value <<= 8;
			value |= (bytes[1] & 0x000000ff);
			value <<= 8;
			value |= (bytes[0] & 0x000000ff);
		}
		return value;
	}
	
	public void setLongArrayValue(int key, long[] value, int len) 
	{
		if ((value == null) || (len == 0))
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ArrayUtils.serializeArray(value, len, new DataOutputStream(out)); 
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize long array value");
			}
		}
	}	
	
	public long[] getLongArrayValue(int key)
	{
		long[] value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = ArrayUtils.deserializeLongArray(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize long array value");
			}
		}
		return value;
	}
	
	public void setIntValue(int key, int value) 
	{
		if (value == 0)
		{
			setByteValue(key, null);
		}
		else
		{
			byte[] bytes = new byte[4];
			bytes[0] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[1] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[2] = (byte) (value & 0x000000ff);
			value >>= 8;
			bytes[3] = (byte) (value & 0x000000ff);
			setByteValue(key, bytes);
		}
	}

	public int getIntValue(int key)
	{
		int value = 0;

		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			value = (bytes[3] & 0x000000ff);
			value <<= 8;
			value |= (bytes[2] & 0x000000ff);				
			value <<= 8;
			value |= (bytes[1] & 0x000000ff);
			value <<= 8;
			value |= (bytes[0] & 0x000000ff);
		}
		return value;
	}
	
	public void setIntArrayValue(int key, int[] value, int len) 
	{
		if ((value == null) || (len == 0))
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ArrayUtils.serializeArray(value, len, new DataOutputStream(out)); 
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize int array value");
			}
		}
	}	
	
	public int[] getIntArrayValue(int key)
	{
		int[] value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = ArrayUtils.deserializeIntArray(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize int array value");
			}
		}
		return value;
	}
	
	public void setBooleanValue(int key, boolean value)
	{
		if (value)
		{
			setByteValue(key, new byte[] {1});
		}
		else
		{
			setByteValue(key, new byte[] {0});
		}
	}
	
	public boolean getBooleanValue(int key)
	{
		boolean value = false;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			value = (bytes[0] == 1);
		}
		return value;
	}
	
	public void setStringValue(int key, String value) 
	{
		if (value == null)
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Serializer.serialize(value, new DataOutputStream(out));
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize string value" + e);
			}
		}
	}
	
	public String getStringValue(int key)
	{
		String value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = (String) Serializer.deserialize(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize string value" + e);
			}
		}
		return value;
	}

	public void setStringArrayValue(int key, String[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ArrayUtils.serializeArray(value, new DataOutputStream(out)); 
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize string array value");
			}
		}
	}
	
	public String[] getStringArrayValue(int key)
	{
		String[] value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = ArrayUtils.deserializeStringArray(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize string array value");
			}
		}
		return value;
	}
	
	public void setGroupArrayValue(int key, Group[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ArrayUtils.serializeArray(value, new DataOutputStream(out)); 
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize group array value");
			}
		}
	}
	
	public Group[] getGroupArrayValue(int key)
	{
		Group[] value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = Group.deserializeGroupArray(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize group array value");
			}
		}
		return value;
	}
	
	public void setExternalNetworkArrayValue(int key, ExternalNetwork[] value) 
	{
		if ((value == null) || (value.length == 0))
		{
			setByteValue(key, null);
		}
		else
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ArrayUtils.serializeArray(value, new DataOutputStream(out)); 
				setByteValue(key, out.toByteArray());
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to serialize external network array value");
			}
		}
	}
	
	public ExternalNetwork[] getExternalNetworkArrayValue(int key)
	{
		ExternalNetwork[] value = null;
		
		byte[] bytes = getByteValue(key);
		if (bytes != null)
		{
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);		
				value = ExternalNetwork.deserializeExternalNetworkArray(new DataInputStream(in));
			}
			catch (IOException e)
			{
				//#debug error
				System.out.println("Failed to deserialize external network array value");
			}
		}
		return value;
	}
	
	private synchronized byte[] getByteValue(int key)
	{
		byte[] value = null;
		try 
		{
			if (recordStore.getNumRecords() >= key)
			{
				value = recordStore.getRecord(key);
			}
		}
		catch (RecordStoreException e) 
		{
			//#debug error
			System.out.println("Failed to get record for key " + key + e);
		}
		return value;		
	}
	
	private synchronized void setByteValue(int key, byte[] value)
	{
		if (value == null)
		{
			value = new byte[0];
		}
		try 
		{
			//#debug performancemonitor
			PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSWRITE);
			while (recordStore.getNumRecords() < key - 1)
			{
				recordStore.addRecord(new byte[0], 0, 0);
			}
			if (recordStore.getNumRecords() == key - 1)
			{
				recordStore.addRecord(value, 0, value.length);
			}
			else
			{
				recordStore.setRecord(key, value, 0, value.length);
			}
			//#debug performancemonitor
			PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSWRITE);
		}
		catch (RecordStoreException e)
		{
			//#debug error
			System.out.println("Failed to set record for key " + key + e);
		}
		if (++access > 40)
		{
			try
			{
				open(name, false);
			}		
			catch (StorageException e)
			{
				//#debug error
				System.out.println("Failed to reopen" + e);				
			}
		}
	}

	//#mdebug error
	public String toString()
	{
		return "RMSKeyValueStore[recordStore=" + recordStore
			+ "]";
	}
	//#enddebug
}
