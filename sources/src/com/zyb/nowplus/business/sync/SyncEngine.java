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

/**
 * Please update description
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public interface SyncEngine {

    static final int SYNCMODE_UPDATE_BOTH = 0;
    static final int TWO_WAY = SYNCMODE_UPDATE_BOTH;
    static final int SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY = 1;
    static final int IMPORT = SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY;
    static final int SYNCMODE_UPDATE_FROM_RECIPIENT_ONLY = 2;
    static final int EXPORT = SYNCMODE_UPDATE_FROM_RECIPIENT_ONLY;
    static final int SYNCMODE_EXTERNAL = 3;
    static final int EXTERNAL = SYNCMODE_EXTERNAL;

    static final int CONFLICT_ORIGINATOR_WINS = 1;
    static final int CONFLICT_RECIPIENT_WINS = 2;
    static final int CONFLICT_USER_PROMPTS = 3;

    int getSection();
    /**
     * Performs a sync
     */
    void sync() ;

    boolean init();

    void uninit();

    /**
     * Cancels current sync and releases all resources this Engine is using
     * Note: Saves the Sync Progress
     */
    void cancelSync();

    /**
     * @param mode SyncEngine.SYNCMODE_? code representing the sync direction; ConflictResolution is ignored
     *             on a SYNCMODE_FROM_? sync mode
     */
    void setMode(int mode);

    int getMode();

    void setResolution(int resolution);

    int getResolution();

    boolean isInteractingWithSource();
}
