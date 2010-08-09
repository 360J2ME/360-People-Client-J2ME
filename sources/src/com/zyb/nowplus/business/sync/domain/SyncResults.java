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


public class SyncResults {


    public int out_add;
    public int out_mod;
    public int out_del;
    public int in_add;
    public int in_mod;
    public int in_del;
    public int merge;
    public int deletes;


    public SyncResults() {
    }

    /**
     * Reset the sync results
     */
    public void reset() {
        out_add = 0;
        out_mod = 0;
        out_del = 0;
        in_add = 0;
        in_mod = 0;
        in_del = 0;
        merge = 0;
        deletes = 0;
    }

	//#mdebug error
    public String toString() {
        return new StringBuffer("[SyncResults::").
                append("\nOUT_ADD:").append(out_add).
                append("\nOUT_MOD:").append(out_mod).
                append("\nOUT_DEL:").append(out_del).
                append("\nIN_ADD :").append(in_add).
                append("\nIN_MOD :").append(in_mod).
                append("\nIN_DEL :").append(in_del).
                append("\nMERGE  :").append(merge).
                append("\nDELETES:").append(deletes).
                append("]").toString();
    }
	//#enddebug

}
