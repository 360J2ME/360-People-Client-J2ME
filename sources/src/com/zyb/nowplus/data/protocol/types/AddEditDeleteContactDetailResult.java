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
package com.zyb.nowplus.data.protocol.types;

import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * <p>This class is used to retrieve the results of the Contacts/AddContact, 
 * Contacts/DeleteContactDetails Contacts/UpdateContact calls on the 
 * backend.</p>
 * <p>It works by giving back the detail IDs of the added, deleted or updated
 * details.</p>
 * <p>The methods getServerRevisionBefore() and getServerRevisionAfter() inform
 * about the current version of the contact and the version number before the 
 * update was done.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public class AddEditDeleteContactDetailResult implements ServiceObject {
	public static final long UNKNOWN_DETAIL = -1;
	
	private long contactID;
	private long serverRevisionBefore, serverRevisionAfter;
	private long[] detailIDs;
	
	/**
	 * 
	 * Called by the hessian parser in order to finalize the parsing of the 
	 * object.
	 * 
	 * @param contactID The contact ID of the contact edited, added or deleted.
	 * @param detailList The list of details altered in the operation.
	 */
	public AddEditDeleteContactDetailResult(long contactID, Vector detailList) {	
		this.contactID = contactID;
		
		detailIDs = new long[detailList.size()];
		
		for (int i = 0; i < detailList.size(); i++) {
			try {
				Hashtable ht = (Hashtable) detailList.elementAt(i);
				detailIDs[i] = ((Long)ht.get(ContactChanges.K_DETAIL_ID)).longValue();
				ht = null;
			} catch (Exception e) {
				detailIDs[i] = UNKNOWN_DETAIL;
			}
		}
	}	
	
	public long getServerRevisionBefore() {
		return serverRevisionBefore;
	}

	public long getServerRevisionAfter() {
		return serverRevisionAfter;
	}

	public long getContactID() {
		return contactID;
	}

	public long[] getDetailIDs() {
		return detailIDs;
	}

	public Hashtable toHashtable() {
		return null;
	}

}
