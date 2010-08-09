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
package com.zyb.nowplus.business.sync.storage;

import com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException;

import java.io.IOException;

/**
 * This interface represents the abstraction layer for any random CRUD-able Data Storage
 * ( Such as jsr75-PIM Contacts )
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public interface RecipientDataStorageConnector {

    /**
     * Opens and initializes the data storage this Connector is operating on
     * <p/>
     *
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    void open() throws IOException;


    /**
     * Closes this instance and releases all used resources
     * <p/>
     *
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    void close() throws IOException;


    /**
     * @return an array containing all syncable item ids available items in this storage
     */
    long[] allIds();

    /**
     * @param uid target item uid
     * @return content revision, or -1 if the item is not existing
     */
    long getRevision(long uid);

    String getShortDescription(long uid);

    /**
     * Reserves a permanent UID for the next new item.
     * From this call on, the item identified by UID must be accessible for CRUD
     * <p/>
     *
     * @return the new item UID
     * @throws com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException
     *                             if this data storage reached it's max size limit
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    long create(Object source) throws SyncDataStorageFullException, IOException;

    /**
     * Reads the target item and returns it's data container
     * <p/>
     *
     * @param uid target item unique identifier
     * @return target item data container
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    Object get(long uid) throws IOException;

    /**
     * Overwrites the item identified by uid from the source input data.
     *
     * @param uid    target item uid
     * @param source input data container
     * @throws IOException when outstanding IO issues are encountered
     */
    void update(long uid, Object source) throws IOException;

    /**
     * Deletes the target Item from this Data Storage
     * <p/>
     *
     * @param uid target item unique identifier
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    void delete(long uid) throws IOException;

}
