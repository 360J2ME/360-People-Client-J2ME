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
package com.zyb.nowplus.business.sync;

public interface Sync {

    public static final long FULL_SPEED = 0;
    public static final long HALF_SPEED = 250;
    public static final long LAZY_SPEED = 1000;

    /**
     * Start a sync
     * Note: init sync engine with OCDS/RCDS connectos and start sync thread
     */
    void start();

    /**
     * Sync stop
     * Note: Cancel current sync and releases queued syncs
     */
    void stop();

    /**
     * Queues a sync.
     *
     * SYNCMODE_EXTERNAL is not queueable as it is continuous. It can not
     * stack one after the other, but it can stack combined with other sync modes.
     * If the sync is running the continuous sync and a new sync is invoked, the
     * continuous sync will be forcefully completed ( cancelled ).
     * In other words, the external sync ( continuous sync ) is only
     * running while the queue is empty.
     * @param mode SyncEngine.SYNCMODE_ mode
     * @param resolution SyncEngine.CONFLICT_ resolution
     */
    void sync(int mode, int resolution);

    /**
     * Cancels the current sync ( if any ).
     */
    void cancelSync();

    /**
     * Remap CAB fields to NAB fields for a ContactProfile item
     * Note: Supported fields are TEL, EMAIL, ADDRESS
     * @param input ContactProfile
     * @return true if all fields were remapped successfully
     */
    boolean remap(Object input);

    /**
     * Indicates if the sync manager is interacting with the source.
     */
    public boolean isInteractingWithSource();
}
