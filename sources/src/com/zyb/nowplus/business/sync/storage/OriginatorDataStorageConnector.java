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
 * This interface represents the abstraction layer for any real-time CRUD-able Data Storage
 * ( Such as jsr75-PIM Contacts )
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public interface OriginatorDataStorageConnector {

    /**
     * @return true if this Connector contains more elements
     */
    boolean hasMoreElements();

    /**
     * Advances the iteration to the next element
     */
    void nextElement();

    /**
     * Returns the iteration to the first element.
     */
    void reset()  throws IOException;

    /**
     * Loads the element with the given id.
     */
    void loadElement(String elementId);
    
    /**
     * @return current element unique identifier
     */
    String getElementId();

    /**
     * @return current element data container
     */
    Object getElementData();

    /**
     * @return current element revision
     */
    long getElementRevision();

    String getElementShortDescription();

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
     * Creates a temporary new item and reserves the permanent UID.
     * This new item is set as the current iteration of the Connector.
     * <p/>
     *
     * @return new item id
     * @throws com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException
     *                             if this data storage reached it's max size limit
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    String create(Object input) throws SyncDataStorageFullException, IOException;


    /**
     * Overwrites the current item with the input data
     *
     * @param input input data container
     * @throws IOException if unable to interpret the data or unable to commit due to I/O issues.
     */
    void update(Object input) throws IOException;

    /**
     * Deletes the current Item from this Data Storage
     * <p/>
     *
     * @throws java.io.IOException when outstanding IO issues are encountered
     */
    void delete() throws IOException;

    boolean isInteracting();
}
