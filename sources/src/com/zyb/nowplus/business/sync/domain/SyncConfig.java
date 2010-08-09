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
package com.zyb.nowplus.business.sync.domain;

import com.zyb.nowplus.business.sync.SyncManager;
import com.zyb.nowplus.business.sync.domain.conflicts.SyncConflictPrompter;
import com.zyb.nowplus.business.sync.impl.LightSyncEngine;

public class SyncConfig {
    private int section;
    private int mode;
    private int resolution;

    /**
     * Sync Config constructor
     * @param section  sync section
     * @param mode  sync mode representing the sync direction
     * @param resolution conflict resolutions used for two-way sync
     */
    public SyncConfig(int section, int mode, int resolution) {
        this.section = section;
        this.mode = mode;
        this.resolution = resolution;

    }

    public SyncConflictPrompter getConflictPrompter() {
        return null;
    }

    /**
     * Get the sync section
     * e.g. CONTACTS
     * @return section
     */
    public int getSection() {
        return section;
    }

    /**
     * Get the sync mode representing the sync direction
     * e.g. from originator/recipient/external...
     * @return sync mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get the conflict resolutions used for two-way sync
     * @return resolutions
     */
    public int getResolution() {
        return resolution;
    }
    //#mdebug debug
    public String toString() {
        return new StringBuffer("[SyncConfig").
                append(section == SyncManager.CONTACTS ? "CONTACTS" : "Unknown").
                append(" - ").
                append(LightSyncEngine.resolveSync(mode, resolution)).
                append("]").toString();
    }
    //#enddebug
}
