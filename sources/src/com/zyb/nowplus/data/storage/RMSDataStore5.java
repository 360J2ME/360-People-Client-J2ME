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

import com.zyb.util.ArrayUtils;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;

import de.enough.polish.io.Serializer;

public class RMSDataStore5 implements DataStore
{
	// This data store uses multiple record stores ('stores'). The records in 
	// a record store ('collections') are divided in a number of slots ('elements') 
	// Each slot contains a piece of data ('record').
	
	/**
	 * Maximum number of underlying record stores.
	 */
	//#if polish.blackberry
		//#= private static final int MAX_NUMBER_OF_STORES = 32;
	//#else
		private static final int MAX_NUMBER_OF_STORES = 12;
	//#endif

	
	/**
	 * The maximum size of a record in the underlying record stores.
	 */
	private static final int MAX_COLLECTION_SIZE = 8192;

	private static final int INITIAL_NUMBER_OF_RECORDS = 128;
	private static final int INITIAL_NUMBER_OF_COLLECTIONS = INITIAL_NUMBER_OF_RECORDS * 1024 / MAX_COLLECTION_SIZE;
	
	private String name;

	// the stores
	private RecordStore[] stores;
	private int numberOfStores;
	
	// maps a collection to a record store and a record number
	private int[] collectionStoreIndices;
	private int[] collectionRecordNumbers;
	private int[] collectionSizes;
	private int numberOfCollections;
	
	// maps a record to a collection and an element for the simple data
	// and (if fullRecords=true) a collection and an element for the complex data 
	private long[] recordIds;
	private int[] shortRecordCollectionIndices;
	private int[] shortRecordElementIndices;
	private final boolean fullRecords;
	private int[] fullRecordCollectionIndices;
	private int[] fullRecordElementIndices;
	private int numberOfRecords;
		
	// cache
	private int cachedCollectionIndex;
	private byte[] cachedCollection;
	private int cachedCollectionSize;
	private byte[] newCachedCollection; // used to change the cache
	private int newCachedCollectionSize;
	
	// buffer to reuse
	private byte[] inputBuffer;
	private int inputBufferLen;
	
	private ByteArrayOutputStream outputBuffer;
	
	private int unsavedChanges;
	private static final int MAX_UNSAVED_CHANGES = 12;
	
	private int unflushedChanges;
	private static final int MAX_UNFLUSHED_CHANGES = 48 / MAX_UNSAVED_CHANGES;
	
	/**
	 * Constructs an RMS data store.
	 * @param fullRecords If true, the store stores records in a short and
	 * full format (see DataRecord). 
	 */
	public RMSDataStore5(boolean fullRecords)
	{
		cachedCollectionIndex = -1;
		cachedCollection = new byte[MAX_COLLECTION_SIZE];
		cachedCollectionSize = 0;
		newCachedCollection = new byte[MAX_COLLECTION_SIZE];
		
		inputBuffer = new byte[MAX_COLLECTION_SIZE];
		inputBufferLen = 0;
		
		outputBuffer = new ByteArrayOutputStream(MAX_COLLECTION_SIZE);	
		
		this.fullRecords = fullRecords;
	}
	
	public synchronized void open(String name, boolean createIfNecessary) throws StorageException
	{
		//#debug rms5
		System.out.println("Opening data store " + name);
		
		if (stores != null)
		{
			throw new StorageException("Data store is already open");
		}
		try 
		{
			this.name = name;
			
			// open the first store to read the index
			stores = new RecordStore[MAX_NUMBER_OF_STORES];
			stores[0] = RecordStore.openRecordStore(name + 0, createIfNecessary);
					
			//#debug rms5
			System.out.println("Opened rms " + name + 0);
	
			readIndex();
		
			// open the other stores
			for (int i = 1; i < numberOfStores; i++)
			{
				stores[i] = RecordStore.openRecordStore(name + i, false);
				
				//#debug rms5
				System.out.println("Opened rms " + name + i);
			}
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
		if (stores == null)
		{
			return;
		}

		//#debug rms5
		System.out.println("Closing data store " + name);
		
		if (unsavedChanges > 0)
		{
			try
			{
				writeIndex(true); // to get rid of unsaved changes
			}
			catch (RecordStoreException e)
			{
				//#debug debug
				System.out.println("Failed to write index" + e);			
			}
			catch (IOException e)
			{
				//#debug debug
				System.out.println("Failed to serialize index" + e);
			}
		}
	
		close0();
	}
	
	private void close0()
	{	
		// close all stores
		for (int i = 0; i < numberOfStores; i++)
		{
			try 
			{
				//#debug info
				System.out.println("before");
				stores[i].closeRecordStore();
				
				//#debug info
				System.out.println("Closed rms " + name + i);
			} 
			catch (RecordStoreException e) 
			{
				//#debug error
				System.out.println("Failed to close rms " + name + i + e);
			}
		}

		stores = null;
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
		//#debug rms5
		System.out.println("Get short record " + recordIndex);
		
		Object record = null;

		try 
		{
			getBuffer(shortRecordCollectionIndices[recordIndex], shortRecordElementIndices[recordIndex]);
		    record = deserializeBuffer();
		} 
		catch (RecordStoreException e) 
		{
			//#debug error
			System.out.println("Failed to read record  " + recordIndex + e);
		}
		catch (IOException e)
		{
			//#debug error
			System.out.println("Failed to deserialize record " + recordIndex + e);			
		}

		return record;
	}
	
	public synchronized Object getFullRecord(long id) 
	{
		//#debug rms5
		System.out.println("Get full record " + id);
		
		int recordIndex = getRecordIndex(id);

		if (recordIndex == -1)
		{
			return null;
		}

		Object record = null;

		try 
		{
			if (fullRecords) 
			{
				getBuffer(fullRecordCollectionIndices[recordIndex], fullRecordElementIndices[recordIndex]);
			}
			else
			{
				getBuffer(shortRecordCollectionIndices[recordIndex], shortRecordElementIndices[recordIndex]);
			}

			record = deserializeBuffer();
		} 
		catch (RecordStoreException e) 
		{
			//#debug error
			System.out.println("Failed to read record " + recordIndex + e);
		}
		catch (IOException e)
		{
			//#debug error
			System.out.println("Failed to deserialize record " + recordIndex + e);			
		}

		return record;
	}
	
	public synchronized void insert(DataRecord record) throws StorageException 
	{
		//#debug rms5
		System.out.println("Insert record " + record.getId());
		
		try 
		{
			int recordIndex = getNewRecordIndex(record.getId());

			if (recordIndex == -1)
			{
				throw new StorageException("Existing id " + record.getId());
			}

			recordIds[recordIndex] = record.getId();
			
			if (fullRecords) 
			{
				// add full record
				serializeBuffer(record);
				addBuffer(recordIndex, fullRecordCollectionIndices, fullRecordElementIndices);
	
				record.release();
			}
			
			// add short record
			serializeBuffer(record); 
			addBuffer(recordIndex, shortRecordCollectionIndices, shortRecordElementIndices);
			
			// update index
			writeIndex(false);
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
		//#debug rms5
		System.out.println("Update record " + record.getId());
		
		int recordIndex = getRecordIndex(record.getId());

		if (recordIndex == -1)
		{
			throw new StorageException("Unknown id " + record.getId());
		}

		try 
		{
			if (fullRecords) 
			{
				// update full record
				serializeBuffer(record); 
				setBuffer(recordIndex, fullRecordCollectionIndices, fullRecordElementIndices);
				
				record.release();
				
				// TODO: do we need a return here?
			}
			
			// update short record
			serializeBuffer(record);
			setBuffer(recordIndex, shortRecordCollectionIndices, shortRecordElementIndices);
			
			// update index
			writeIndex(false);
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
		//#debug rms5
		System.out.println("Delete record " + id);
		
		int recordIndex = getRecordIndex(id);
		if (recordIndex == -1)
		{
			throw new StorageException("Unknown id " + id);
		}
		try
		{		
			// delete short record
			delete(recordIndex, shortRecordCollectionIndices, shortRecordElementIndices);

			if (fullRecords) 
			{
				// delete full record
				delete(recordIndex, fullRecordCollectionIndices, fullRecordElementIndices);
			}	
			
			// update index
			numberOfRecords--;

			for (int i = recordIndex; i < numberOfRecords; i++)
			{
				recordIds[i] = recordIds[i + 1];
				shortRecordCollectionIndices[i] = shortRecordCollectionIndices[i + 1];
				shortRecordElementIndices[i] = shortRecordElementIndices[i + 1];

				if (fullRecords)
				{
					fullRecordCollectionIndices[i] = fullRecordCollectionIndices[i + 1];
					fullRecordElementIndices[i] = fullRecordElementIndices[i + 1];
				}
			}

			writeIndex(false);
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

	private void readIndex() throws RecordStoreException, IOException
	{
		if (stores[0].getNumRecords() == 0) {
			// initialise the index
			numberOfStores = 1;
			
			collectionStoreIndices = new int[INITIAL_NUMBER_OF_COLLECTIONS];
			collectionRecordNumbers = new int[INITIAL_NUMBER_OF_COLLECTIONS];
			collectionSizes = new int[INITIAL_NUMBER_OF_COLLECTIONS];
			numberOfCollections = 0;
						
			recordIds = new long[INITIAL_NUMBER_OF_RECORDS];
			shortRecordCollectionIndices = new int[INITIAL_NUMBER_OF_RECORDS];
			shortRecordElementIndices = new int[INITIAL_NUMBER_OF_RECORDS];
			if (fullRecords) 
			{
				fullRecordCollectionIndices = new int[INITIAL_NUMBER_OF_RECORDS];
				fullRecordElementIndices = new int[INITIAL_NUMBER_OF_RECORDS];
			}
			numberOfRecords = 0;

			//#debug rms5
			System.out.println("Initialised index");
			
			writeIndex(true);
		}
		else {
			//#debug performancemonitor
			PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSREAD);

			// load the index
			if (inputBuffer.length < stores[0].getRecordSize(1)) {
				inputBuffer = stores[0].getRecord(1);
				
				//#debug rms5
				System.out.println("Expanded input buffer to fit index");
			}
			else {
				stores[0].getRecord(1, inputBuffer, 0);
			}

			//#debug performancemonitor
			PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSREAD);
			
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(inputBuffer));
			
			numberOfStores = in.readInt();
			
			collectionStoreIndices = (int[]) Serializer.deserialize(in);
			collectionRecordNumbers = (int[]) Serializer.deserialize(in);
			collectionSizes = new int[collectionRecordNumbers.length];
			numberOfCollections = in.readInt();
			
			recordIds = (long[]) Serializer.deserialize(in);
			shortRecordCollectionIndices = (int[]) Serializer.deserialize(in);
			shortRecordElementIndices = (int[]) Serializer.deserialize(in);

			if (fullRecords) 
			{
				fullRecordCollectionIndices = (int[]) Serializer.deserialize(in);
				fullRecordElementIndices = (int[]) Serializer.deserialize(in);
			}

			numberOfRecords = in.readInt();
			
			//#debug rms5
			System.out.println("Loaded index");
		}		
	}
	
	private void writeIndex(boolean compulsory) throws RecordStoreException, IOException
	{
		if (compulsory || (++unsavedChanges >= MAX_UNSAVED_CHANGES))
		{
			outputBuffer.reset();
			
			DataOutputStream out = new DataOutputStream(outputBuffer);
			
			out.writeInt(numberOfStores);
			
			Serializer.serialize(collectionStoreIndices, out);
			Serializer.serialize(collectionRecordNumbers, out);
			out.writeInt(numberOfCollections);
			
			Serializer.serialize(recordIds, out);
			Serializer.serialize(shortRecordCollectionIndices, out);
			Serializer.serialize(shortRecordElementIndices, out);

			if (fullRecords) 
			{
				Serializer.serialize(fullRecordCollectionIndices, out);
				Serializer.serialize(fullRecordElementIndices, out);
			}

			out.writeInt(numberOfRecords);	
			
			byte[] data = outputBuffer.toByteArray();
			
			//#debug performancemonitor
			PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSWRITE);

			if (stores[0].getNumRecords() == 0)
			{
				stores[0].addRecord(data, 0, data.length);
			}
			else
			{
				stores[0].setRecord(1, data, 0, data.length);
			}

			//#debug performancemonitor
			PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSWRITE);
			
			//#debug rms5
			System.out.println("Stored index");		
			
			unsavedChanges = 0;
			
			if (++unflushedChanges >= MAX_UNFLUSHED_CHANGES)
			{
				//#debug rms5
				System.out.println("Flush changes to speed up close");
				
				try
				{
					close0();
					open(name, false);
				}
				catch (StorageException e)
				{
					//#debug error
					System.out.println("Flush failed" + e);
				}
				
				unflushedChanges = 0;
			}
		}
	}	
	
	private int getRecordIndex(long recordId)
	{		
		//#debug rms5
		System.out.println("Get index for " + recordId);
		
		// binary search to find index for given record id
		int min = 0;
		int mid = 0;
		int max = numberOfRecords - 1;

		while (max - min > 7)
		{
			mid = (min + max) / 2;

			if (recordIds[mid] == recordId)
			{
				return mid;
			}
			else
			{
				if (recordIds[mid] > recordId)
				{
					max = mid - 1;
				}
				else
				{
					min = mid + 1;
				}
			}
		}
		
		for (mid = min; mid <= max; mid++)
		{
			if (recordIds[mid] == recordId)
			{
				return mid;
			}
		}	
		
		return -1;		
	}
	
	private int getNewRecordIndex(long recordId)
	{	
		//#debug rms5
		System.out.println("Get new index for " + recordId);
		
		// binary search to find where to add record id
		int min = 0;
		int mid = 0;
		int max = numberOfRecords - 1;

		while (max - min > 7)
		{
			mid = (min + max) / 2;
			if (recordIds[mid] == recordId)
			{
				return -1;
			}
			else
			{
				if (recordIds[mid] > recordId)
				{
					max = mid - 1;
				}
				else
				{
					min = mid + 1;
				}
			}
		}
		
		for (mid = min; (mid <= max) && (recordIds[mid] < recordId); mid++);
		
		if ((mid <= max) && (recordIds[mid] == recordId))
		{
			return -1;
		}
		
		if (numberOfRecords == recordIds.length)
		{
			// expand record arrays
			recordIds = ArrayUtils.extendArray(recordIds);
			shortRecordCollectionIndices = ArrayUtils.extendArray(shortRecordCollectionIndices);
			shortRecordElementIndices = ArrayUtils.extendArray(shortRecordElementIndices);

			if (fullRecords) 
			{
				fullRecordCollectionIndices = ArrayUtils.extendArray(fullRecordCollectionIndices);
				fullRecordElementIndices = ArrayUtils.extendArray(fullRecordElementIndices);	
			}
			
			//#debug rms5
			System.out.println("Expanded record arrays to " + recordIds.length + " elements");
		}
		
		// make room to insert new record
		for (int i = numberOfRecords; i > mid; i--)
		{
			recordIds[i] = recordIds[i - 1];
			shortRecordCollectionIndices[i] = shortRecordCollectionIndices[i - 1];
			shortRecordElementIndices[i] = shortRecordElementIndices[i - 1];

			if (fullRecords) 
			{
				fullRecordCollectionIndices[i] = fullRecordCollectionIndices[i - 1];
				fullRecordElementIndices[i] = fullRecordElementIndices[i - 1];
			}
		}

		numberOfRecords++;
		return mid;		
	}			
	
	/**
	 * Fill buffer with the given element of the given collection.
	 */
	private void getBuffer(int collectionIndex, int elementIndex) throws RecordStoreException
	{	
		//#debug rms5
		System.out.println("Get data from element " + elementIndex + " in collection " + collectionIndex);
		
		// determine the start and length of the required element
		cacheCollection(collectionIndex);
		
		int start = gotoStart(elementIndex);
		inputBufferLen = (cachedCollection[start] & 0xff) << 8 | (cachedCollection[start + 1] & 0xff);
		
		// fill the buffer
		System.arraycopy(cachedCollection, start + 2, inputBuffer, 0, inputBufferLen);
	}	
	
	/**
	 * Add the contents of the buffer to the store.
	 */
	private void addBuffer(int recordIndex, int[] collectionIndices, int[] elementIndices) throws RecordStoreException
	{	
		// find collection with space		
		int storeIndex = -1;
		int collectionIndex = -1;
		
		// try to use the fact that while iterating through the collections,
		// the storeIndex won't change too often
		checkedStoreIndex = -1;
		checkedHasSpace = false;
		
		if (numberOfCollections == 0)
		{
			// store 0 is the only store that is created without any collections
			if (storeHasSpace(0, outputBuffer.size()))
			{
				storeIndex = 0;
			}
		}
		else
		{		
			for (int i = numberOfCollections - 1; (i >= 0) && (collectionIndex == -1); i--)
			{
				if (storeHasSpace(collectionStoreIndices[i], 2 + outputBuffer.size()))
				{
					storeIndex = collectionStoreIndices[i];
										
					if (collectionHasSpace(i, 2 + outputBuffer.size()))
					{	
						collectionIndex = i;
					}
				}
			}
		}
		
		if (storeIndex == -1)
		{
			// add a store
			stores[numberOfStores] = RecordStore.openRecordStore(name + numberOfStores, true);
			
			storeIndex = numberOfStores++;
			
			//#debug rms5
			System.out.println("Added store " + storeIndex);
		}
		
		if (collectionIndex == -1)
		{
			// add a collection
			if (numberOfCollections == collectionRecordNumbers.length)
			{
				collectionStoreIndices = ArrayUtils.extendArray(collectionStoreIndices);
				collectionRecordNumbers = ArrayUtils.extendArray(collectionRecordNumbers);
				collectionSizes = ArrayUtils.extendArray(collectionSizes);
			}			

			collectionStoreIndices[numberOfCollections] = storeIndex;
			collectionRecordNumbers[numberOfCollections] = -1;
			collectionSizes[numberOfCollections] = 0;

			collectionIndex = numberOfCollections++;
			
			cachedCollectionIndex = collectionIndex;
			cachedCollectionSize = 0;
			
			//#debug rms5
			System.out.println("Added collection " + collectionIndex);
		}
		else
		{
			cacheCollection(collectionIndex);
		}

		// find empty element in cached collection
		int elementIndex = 0;
		int start = 0;

		while ((start < cachedCollectionSize) && ((cachedCollection[start] != 0) || (cachedCollection[start + 1] != 0)))
		{
			elementIndex++;
			start += 2 + ((cachedCollection[start] & 0xff) << 8 | (cachedCollection[start + 1] & 0xff));
		}
		
		//#debug rms5
		System.out.println("Add data to element " + elementIndex + " in collection " + collectionIndex);
		
		collectionIndices[recordIndex] = collectionIndex;
		elementIndices[recordIndex] = elementIndex;
	
		// change cache
		changeCache(start, 0, outputBuffer.size());
	}	
	
	/**
	 * Set the contents of the buffer in the given record.
	 */
	private void setBuffer(int recordIndex, int[] collectionIndices, int[] elementIndices) throws RecordStoreException
	{		
		int collectionIndex = collectionIndices[recordIndex];
		
		//#debug rms5
		System.out.println("Set data in element " + elementIndices[recordIndex] + " in collection " + collectionIndex);

		// determine the start of the element
		cacheCollection(collectionIndex);
		
		int start = gotoStart(elementIndices[recordIndex]);
		int len = (cachedCollection[start] & 0xff) << 8 | (cachedCollection[start + 1] & 0xff);
		
		if (collectionHasSpace(collectionIndex, outputBuffer.size() - len))
		{
			// change cache
			changeCache(start, len, outputBuffer.size());		
		}
		else
		{
			// remove this element
			changeCache(start, len, 0);				
			
			// and add a new one
			addBuffer(recordIndex, collectionIndices, elementIndices);
		}
	}
	
	private void delete(int recordIndex, int[] collectionIndices, int[] elementIndices) throws RecordStoreException
	{
		int collectionIndex = collectionIndices[recordIndex];
		
		//#debug rms5
		System.out.println("Delete data from element " + elementIndices[collectionIndex] + " in collection " + collectionIndex);
		
		// determine start of element
		cacheCollection(collectionIndex);
		
		int start = gotoStart(elementIndices[recordIndex]);
		int len = (cachedCollection[start] & 0xff) << 8 | (cachedCollection[start + 1] & 0xff);

		// change cache
		changeCache(start, len, 0);
	}
	
	private int checkedStoreIndex;
	private boolean checkedHasSpace;
	
	private boolean storeHasSpace(int storeIndex, long len)
	{
		if (checkedStoreIndex != storeIndex)
		{
			checkedStoreIndex = storeIndex;
			checkedHasSpace = false;

			try
			{
				checkedHasSpace = stores[storeIndex].getSize() + len <= stores[storeIndex].getSizeAvailable();
			}
			catch (RecordStoreException e)
			{
				//#debug error
				System.out.println("Failed to check size of store" + e);
			}
			
			//#debug rms5
			System.out.println("Checked that store " + storeIndex + (checkedHasSpace ? " has" : " has no") + " space left");
		}
		return checkedHasSpace;
	}
	
	private boolean collectionHasSpace(int collectionIndex, long len) throws RecordStoreException
	{
		if (collectionSizes[collectionIndex] == 0)
		{
			// cache collection to set size
			cacheCollection(collectionIndex);
			collectionSizes[collectionIndex] = cachedCollectionSize;
		}
		
		return (collectionSizes[collectionIndex] + len <= MAX_COLLECTION_SIZE);
	}
	
	private void cacheCollection(int collectionIndex) throws RecordStoreException
	{
		if (collectionIndex == cachedCollectionIndex)
		{
			// use cached record
		}
		else
		{
			//#debug rms5
			System.out.println("Cache collection " + collectionIndex);
		
			int storeIndex = collectionStoreIndices[collectionIndex];
			int recordNumber = collectionRecordNumbers[collectionIndex];
			
			cachedCollectionIndex = collectionIndex;
			
			//#debug performancemonitor
			PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSREAD);
			
			cachedCollectionSize = stores[storeIndex].getRecord(recordNumber, cachedCollection, 0);
			
			//#debug performancemonitor
			PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSREAD);
		}
	}
	
	private int gotoStart(int elementIndex) 
	{
		int start = 0;

		for (int i = 0; i < elementIndex; i++)
		{
			start += 2 + ((cachedCollection[start] & 0xff) << 8 | (cachedCollection[start + 1] & 0xff));

			if (start > cachedCollectionSize)
			{
				//#debug error
				System.out.println("Collection has length " + cachedCollectionSize + ", but element " + i + " starts at " + start);
			
				break;
			}
		}

		//#debug rms5
		System.out.println("Element " + elementIndex + " starts at position " + start);
		
		return start;
	}

	private void changeCache(int start, int len, int newLen) throws RecordStoreException
	{	
		// copy part before
		if (start > 0)
		{
			//#debug rms5
			System.out.println("Copy cache position " + 0 + " to " + (start - 1));
			
			System.arraycopy(cachedCollection, 0, newCachedCollection, 0, start);
		}
		
		// set data
		
		//#debug rms5
		System.out.println("Copy cache position " + start + " to " + (start + 2 + newLen - 1));
		
		newCachedCollection[start] = (byte) (newLen / 256);
		newCachedCollection[start + 1] = (byte) (newLen % 256);

		if (newLen > 0)
		{
			System.arraycopy(outputBuffer.toByteArray(), 0, newCachedCollection, start + 2, newLen);
		}
		
		// copy part after
		if (start == cachedCollectionSize)
		{
			newCachedCollectionSize = cachedCollectionSize + 2 + newLen;
		}
		else
		{
			newCachedCollectionSize = cachedCollectionSize - len + newLen;
			
			//#debug rms5
			System.out.println("Copy cache position " + (start + 2 + newLen) + " to " + (newCachedCollectionSize - 1));
			
			System.arraycopy(cachedCollection, start + 2 + len, newCachedCollection, start + 2 + newLen, cachedCollectionSize - (start + 2 + len));
		}
		
		// swap old and new cache
		byte[] collection = cachedCollection;
		int size = cachedCollectionSize;
		
		cachedCollection = newCachedCollection;
		cachedCollectionSize = newCachedCollectionSize; 
		
		newCachedCollection = collection;
		newCachedCollectionSize = size;
		
		// flush cache
		int storeIndex = collectionStoreIndices[cachedCollectionIndex];
		
		//#debug performancemonitor
		PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSWRITE);
		
		if (collectionRecordNumbers[cachedCollectionIndex] == -1)
		{
			collectionRecordNumbers[cachedCollectionIndex] = stores[storeIndex].addRecord(cachedCollection, 0, cachedCollectionSize);
		}
		else
		{
			stores[storeIndex].setRecord(collectionRecordNumbers[cachedCollectionIndex], cachedCollection, 0, cachedCollectionSize);
		}
		
		//#debug performancemonitor
		PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSWRITE);
		
		collectionSizes[cachedCollectionIndex] = cachedCollectionSize;
		
		//#debug rms5
		System.out.println("Flushed cache to record " + collectionRecordNumbers[cachedCollectionIndex] + " in store " + storeIndex);		
	}
	
	private void serializeBuffer(Object object) throws IOException
	{
		outputBuffer.reset();
		
		Serializer.serialize(object, new DataOutputStream(outputBuffer));
	}
	
	private Object deserializeBuffer() throws IOException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(inputBuffer);
		
		return Serializer.deserialize(new DataInputStream(in));
	}	

	public boolean isFillingUp()
	{
		return numberOfStores == MAX_NUMBER_OF_STORES;
	}
	
	//#mdebug error
	public String toString()
	{
		return "RMSDataStore[ids=" + recordIds 
			+ ",numberOfRecords=" + numberOfRecords
			+ "]";
	}
	//#enddebug
}
