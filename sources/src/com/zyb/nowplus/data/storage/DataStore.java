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

/**
 * A data store persists {@link com.zyb.nowplus.data.storage.DataRecord data
 * records}.
 */
public interface DataStore 
{
	/**
	 * Opens the data store.
	 * @param name Name of the data store.
	 * @param createIfNecessary If true, a new data store will be created if
	 * an existing data store can't be opened.
	 */
	public void open(String name, boolean createIfNecessary) throws StorageException;
	
	/**
	 * Closes the data store.
	 */
	public void close();
	
	/**
	 * Gets the number of data records in the data store.
	 */
	public int getNumberOfRecords();
	
	/** 
	 * Checks if a record with the given id exists in the data store.
	 */
	public boolean hasRecord(long id);
	
	/**
	 * Gets the short record (see {@link com.zyb.nowplus.data.storage.DataRecord#release()})
	 * at the given index.
	 */
	public Object getShortRecord(int recordIndex);
	
	/**
	 * Gets a full record (see {@link com.zyb.nowplus.data.storage.DataRecord#release()}).
	 * @param id Identifier of the record.
	 */
	public Object getFullRecord(long id);
	
	/**
	 * Inserts a record.
	 */
	public void insert(DataRecord record) throws StorageException;
	
	/**
	 * Updates a record.
	 */
	public void update(DataRecord record) throws StorageException;
	
	/**
	 * Deletes a record.
	 * @param id Identifier of the record to be deleted.
	 */
	public void delete(long id) throws StorageException;
	
	/**
	 * Checks if the recordstore has enough space.
	 */
	public boolean isFillingUp();
}
