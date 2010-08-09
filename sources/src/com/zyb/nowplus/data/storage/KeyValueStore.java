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

import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;

/**
 * A key-value store persists key-value pairs.
 */
public interface KeyValueStore 
{
	/**
	 * Opens the key-value store.
	 * @param name Name of the key-value store.
	 * @param createIfNecessary If true, a new key-value store will be created if
	 * an existing key-value store can't be opened.
	 */
	public void open(String name, boolean createIfNecessary) throws StorageException;
	
	/**
	 * Closes the key-value store.
	 */
	public void close();

	/**
	 * Sets a long value for the given key.
	 */
	public void setLongValue(int key, long value);
	
	/**
	 * Gets the long value for the given key.
	 */
	public long getLongValue(int key);
	
	/**
	 * Sets a long array value for the given key.
	 */
	public void setLongArrayValue(int key, long[] value, int len);
	
	/**
	 * Gets the long array value for the given key.
	 */
	public long[] getLongArrayValue(int key);
	
	/**
	 * Sets an int value for the given key.
	 */
	public void setIntValue(int key, int value);
	
	/**
	 * Gets the int value for the given key.
	 */
	public int getIntValue(int key);
	
	/**
	 * Sets a int array value for the given key.
	 */
	public void setIntArrayValue(int key, int[] value, int len);
	
	/**
	 * Gets the int array value for the given key.
	 */
	public int[] getIntArrayValue(int key);
	
	/**
	 * Sets a boolean value for the given key.
	 */
	public void setBooleanValue(int key, boolean value);
	
	/**
	 * Gets the boolean value for the given key.
	 */
	public boolean getBooleanValue(int key);	
	
	/**
	 * Sets a string value for the given key.
	 */
	public void setStringValue(int key, String value);
	
	/**
	 * Gets the string value for the given key.
	 */
	public String getStringValue(int key);

	/**
	 * Sets a string array value for the given key.
	 */
	public void setStringArrayValue(int key, String[] value);
	
	/**
	 * Gets the string array value for the given key.
	 */
	public String[] getStringArrayValue(int key);
	
	/**
	 * Sets a group array value for the given key.
	 */
	public void setGroupArrayValue(int key, Group[] value);
	
	/**
	 * Gets the group array value for the given key.
	 */
	public Group[] getGroupArrayValue(int key);	
	
	/**
	 * Sets an external network array value for the given key.
	 */
	public void setExternalNetworkArrayValue(int key, ExternalNetwork[] value);
	
	/**
	 * Gets the external network array value for the given key.
	 */
	public ExternalNetwork[] getExternalNetworkArrayValue(int key);	
}
