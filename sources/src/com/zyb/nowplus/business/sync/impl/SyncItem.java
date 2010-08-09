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
package com.zyb.nowplus.business.sync.impl;

import com.zyb.nowplus.business.sync.domain.conflicts.SyncPairDescriptor;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Syncable item data embed.
 * <p/>
 * 'Contains' a Factory instead of a public constructor.
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class SyncItem implements SyncPairDescriptor {

//    public static final int CHANGE_IN_ADD = 1;
//    public static final int CHANGE_OUT_ADD = -1;
//    public static final int CHANGE_IN_MOD = 2;
//    public static final int CHANGE_OUT_MOD = -2;
//    public static final int CHANGE_IN_DEL = 3;
//    public static final int CHANGE_OUT_DEL = -3;
//    public static final int CHANGE_MRG = 4;
//    public static final int CHANGE_DELETED = 5;

    public static final int SERIALISED_SIZE = 31;
    
	/* Native UID */
    protected String originatorUID = null;
    /* (ZYB) Client UID */
    protected long recipientUID = -1;

    protected int storageUID = -1;


    /* Native Revision*/
    protected long originatorRevision = -1;
    /* (ZYB) Client Revision */
    protected long recipientRevision = -1;

    /* Native Sync Revision*/
    protected long originatorSyncRevision = -1;
    /* (ZYB) Client Sync Revision */
    protected long recipientSyncRevision = -1;
    
    private String targetDesc = null;
    private String sourceDesc = null;


    /**
     * SyncItem constructor
     */
    public SyncItem() {
    }

    /**
     * Get Native UID
     * @return UID
     */
    public String getOriginatorUID() {
        return originatorUID;
    }

    /**
     * Set Native UID
     * @param anUID UID
     */
    public void setOriginatorUID(String anUID) {
        originatorUID = anUID;
    }

    /**
     * Get (ZYB) Client UID
     * @return
     */
    public long getRecipientUID() {
        return recipientUID;
    }

    /**
     * Set (ZYB) Client UID
     * @param anUID
     */
    public void setRecipientUID(long anUID) {
        recipientUID = anUID;
    }

    /**
     * Set Originator revision for the current SyncItem
     * @param oRevision revision
     */
    public void setOriginatorRevision(long oRevision) {
        originatorRevision = oRevision;
    }

    /**
     * Set Recipient revision for the current SyncItem
     * @param rRevision revision
     */
    public void setRecipientRevision(long rRevision) {
        recipientRevision = rRevision;
    }

    /**
     * Clear originator and recipient revision/uid for a SyncItem
     */
    public void destroy() {
        originatorUID = targetDesc = sourceDesc = null;
        recipientUID = -1;
        originatorRevision = originatorSyncRevision = recipientRevision = recipientSyncRevision = storageUID = -1;
    }

    /**
     * Serialize SyncItem revision/UID
     * @param output DataOutputStream
     * @throws IOException
     */
    public void serialize(DataOutputStream output) throws IOException {
        output.writeUTF(originatorUID);
        output.writeLong(recipientUID);
        output.writeLong(originatorSyncRevision);
        output.writeLong(recipientSyncRevision);
    }

    /**
     * Reset revisions
      */
    public void reset() {
        originatorRevision = -1;
        recipientRevision = -1;
    }

    /**
     * Update sync revisions for synced items
     */
    public void synced() {
        originatorSyncRevision = originatorRevision;
        recipientSyncRevision = recipientRevision;
    }

    /**
     * Set sync revisions
     * @param oSRev originator Sync Revision
     * @param rSRev recipient Sync Revision
     */
    public void setSyncRevisions(long oSRev, long rSRev) {
        originatorSyncRevision = oSRev;
        recipientSyncRevision = rSRev;
    }

    //#mdebug error
    public String toString() {
        return "[SyncItem:" +
                "\nOriginatorUID:" + originatorUID +
                "\nRecipientUID:" + recipientUID +
                "\noRev=" + originatorRevision + ";oSyncRev=" + originatorSyncRevision +
                "\nrRev=" + recipientRevision + ";rSyncRev=" + recipientSyncRevision +
                "\nstIdx:" + storageUID +
                "]";
    }
    //#enddebug

    public String getSourceDescription() {
        return sourceDesc;
    }

    public String getTargetDescription() {
        return targetDesc;
    }

    public void setSourceDesc(String sourceDesc) {
        this.sourceDesc = sourceDesc;
    }

    public void setTargetDesc(String targetDesc) {
        this.targetDesc = targetDesc;
    }
}

class OriginatorUIDSeeker {
    private static OriginatorUIDSeeker instance = new OriginatorUIDSeeker();

    private String originatorUID;

    private OriginatorUIDSeeker() {
    }

    public static OriginatorUIDSeeker getInstance() {
        return instance;
    }

    public OriginatorUIDSeeker seekerFor(String uid) {
        originatorUID = uid;
        return this;
    }

    public boolean equals(Object anObject) {
        return anObject instanceof SyncItem && this.originatorUID != null && this.originatorUID.equals(((SyncItem) anObject).getOriginatorUID());
    }
}

class RecipientUIDSeeker {
    private static RecipientUIDSeeker instance = new RecipientUIDSeeker();

    private long recipientUID;

    private RecipientUIDSeeker() {
    }

    public static RecipientUIDSeeker getInstance() {
        return instance;
    }

    public RecipientUIDSeeker seekerFor(long uid) {
        recipientUID = uid;
        return this;
    }

    public boolean equals(Object anObject) {
        return anObject instanceof SyncItem && this.recipientUID != -1 && this.recipientUID == ((SyncItem) anObject).getRecipientUID();
    }
}


