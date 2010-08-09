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

import de.enough.polish.io.Serializer;

public class RMSDataStore implements DataStore
{
	private static final int INITIAL_SIZE = 100;
		
	private static final int SIZE_WARNING_LIMIT = 80; // %
	
	private RecordStore recordStore;
	private String name;
	private int access;
	
	private long[] recordIds;
	private int[] shortRecordNumbers;
	private int[] fullRecordNumbers;
	private int numberOfRecords;
	
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
			readIndex();
			access = 0;
		} 
		catch (RecordStoreException e) 
		{
			throw new StorageException("Failed to open data store " + name, e);
		} 
		catch (IOException e) 
		{
			throw new StorageException("Failed to deserialize index", e);
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
	
	public int getNumberOfRecords()
	{
		return numberOfRecords;
	}
	
	public boolean hasRecord(long id)
	{
		return getRecordIndex(id) != -1;
	}
	
	public synchronized Object getShortRecord(int recordIndex)
	{
		Object record = null;
		try 
		{
			byte[] data = recordStore.getRecord(shortRecordNumbers[recordIndex]);
			record = deserialize(data);
		} 
		catch (RecordStoreException e) 
		{
			//#debug error
			System.out.println("Failed to read record at slot " + shortRecordNumbers[recordIndex] + e);
		}
		catch (IOException e)
		{
			//#debug error
			System.out.println("Failed to deserialize record at slot " + shortRecordNumbers[recordIndex] + e);			
		}
		return record;
	}
	
	public synchronized Object getFullRecord(long id) 
	{
		int recordIndex = getRecordIndex(id);
		if (recordIndex == -1)
		{
			return null;
		}
		Object record = null;
		try 
		{
			byte[] data = recordStore.getRecord(fullRecordNumbers[recordIndex]);
			record = deserialize(data);
		} 
		catch (RecordStoreException e) 
		{
			//#debug error
			System.out.println("Failed to read record " + id + " at slot " + fullRecordNumbers[recordIndex] + e);
		}
		catch (IOException e)
		{
			//#debug error
			System.out.println("Failed to deserialize record " + id + e);			
		}
		return record;
	}
	
	public synchronized void insert(DataRecord record) throws StorageException 
	{
		if (numberOfRecords == recordIds.length)
		{
			long[] ids2 = new long[recordIds.length * 3 / 2 + 1];
			System.arraycopy(recordIds, 0, ids2, 0, recordIds.length);
			recordIds = ids2;
			int[] indices2 = new int[shortRecordNumbers.length * 3 / 2 + 1];
			System.arraycopy(shortRecordNumbers, 0, indices2, 0, shortRecordNumbers.length);
			shortRecordNumbers = indices2;
			indices2 = new int[fullRecordNumbers.length * 3 / 2 + 1];
			System.arraycopy(fullRecordNumbers, 0, indices2, 0, fullRecordNumbers.length);
			fullRecordNumbers = indices2;			
		}
		try 
		{
			recordIds[numberOfRecords] = record.getId();
			
			// add full record
			byte[] data = serialize(record);
			fullRecordNumbers[numberOfRecords] = recordStore.addRecord(data, 0, data.length);
			
			// add short record
			record.release();
				
			data = serialize(record); 
			shortRecordNumbers[numberOfRecords] = recordStore.addRecord(data, 0, data.length);			
			
			// update index
			numberOfRecords++;
			writeIndex();			
			
			flush();
		}
		catch (RecordStoreException e)
		{
			throw new StorageException("Failed to insert " + record, e);
		}
		catch (IOException e) 
		{
			throw new StorageException("Failed to serialize index or " + record, e);
		}
	}

	public synchronized void update(DataRecord record) throws StorageException
	{
		int recordIndex = getRecordIndex(record.getId());
		if (recordIndex == -1)
		{
			throw new StorageException("Unknown id " + record.getId());
		}
		try 
		{
			// update full record
			byte[] data = serialize(record); 
			recordStore.setRecord(fullRecordNumbers[recordIndex], data, 0, data.length);
			
			// update short record
			record.release();
			
			data = serialize(record);
			recordStore.setRecord(shortRecordNumbers[recordIndex], data, 0, data.length);			
			
			flush();
		}
		catch (RecordStoreException e)
		{
			throw new StorageException("Failed to update " + record, e);
		}
		catch (IOException e) 
		{
			throw new StorageException("Failed to serialize index or " + record, e);
		}		
	}
	
	public synchronized void delete(long id) throws StorageException
	{
		int recordIndex = getRecordIndex(id);
		if (recordIndex == -1)
		{
			throw new StorageException("Unknown id " + id);
		}
		try
		{		
			// delete full record
			recordStore.deleteRecord(fullRecordNumbers[recordIndex]);
			
			// delete short record
			recordStore.deleteRecord(shortRecordNumbers[recordIndex]);

			numberOfRecords--;
			if (recordIndex < numberOfRecords)
			{
				recordIds[recordIndex] = recordIds[numberOfRecords];
				fullRecordNumbers[recordIndex] = fullRecordNumbers[numberOfRecords];
				shortRecordNumbers[recordIndex] = shortRecordNumbers[numberOfRecords];
			}
			writeIndex();
			
			flush();
		}
		catch (RecordStoreException e)
		{
			throw new StorageException("Failed to delete record " + id, e);
		}
		catch (IOException e) 
		{
			throw new StorageException("Failed to serialize index", e);
		}	
	}
	
	private int getRecordIndex(long recordId)
	{
		for (int i = 0; i < numberOfRecords; i++)
		{
			if (recordIds[i] == recordId)
			{
				return i;
			}
		}
		return -1;
	}
	
	private byte[] serialize(Object object) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Serializer.serialize(object, new DataOutputStream(out));
		
		return out.toByteArray();
	}
	
	private Object deserialize(byte[] data) throws IOException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		
		return Serializer.deserialize(new DataInputStream(in));
	}
	
	private void readIndex() throws RecordStoreException, IOException
	{
		if (recordStore.getNumRecords() == 0)
		{
			recordIds = new long[INITIAL_SIZE];
			shortRecordNumbers = new int[INITIAL_SIZE];
			fullRecordNumbers = new int[INITIAL_SIZE];
			numberOfRecords = 0;
			writeIndex();
		}
		else 
		{
			byte[] data = recordStore.getRecord(1);
			
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			
			recordIds = (long[]) Serializer.deserialize(in);
			shortRecordNumbers = (int[]) Serializer.deserialize(in);
			fullRecordNumbers = (int[]) Serializer.deserialize(in);
			numberOfRecords = in.readInt();
		}		
	}
	
	private void writeIndex() throws RecordStoreException, IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		DataOutputStream out = new DataOutputStream(baos);
		
		Serializer.serialize(recordIds, out);
		Serializer.serialize(shortRecordNumbers, out);
		Serializer.serialize(fullRecordNumbers, out);
		out.writeInt(numberOfRecords);	
		
		byte[] data = baos.toByteArray();
		
		if (recordStore.getNumRecords() == 0)
		{
			recordStore.addRecord(data, 0, data.length);
		}
		else
		{
			recordStore.setRecord(1, data, 0, data.length);
		}
	}
	
	private void flush()
	{
		// close and open after 40 insert/update/delete's
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
	
	public boolean isFillingUp()
	{
		boolean fillingUp = false;
		try
		{
			fillingUp = (recordStore.getSize() * 100 > recordStore.getSizeAvailable() * SIZE_WARNING_LIMIT);
		}
		catch (RecordStoreException e)
		{
			//#debug error
			System.out.println("Failed to check record size" + e);
		}
		return fillingUp;
	}
	
	//#mdebug error
	public String toString()
	{
		return "RMSDataStore[ids=" + recordIds 
			+ ",numberOfRecords=" + numberOfRecords
			+ ",recordStore=" + recordStore
			+ "]";
	}
	//#enddebug
}
