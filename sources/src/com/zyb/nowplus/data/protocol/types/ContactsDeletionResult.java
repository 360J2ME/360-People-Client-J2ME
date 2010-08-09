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

public class ContactsDeletionResult implements ServiceObject {
	private static final String KEY_CONTACT_ID_LIST = "contactidlist";
	private long[] contactIDs;	// the deleted contact IDs
	
	
	public ContactsDeletionResult(Hashtable ht) {
		if (null == ht) {
			return;
		}
		
		//#debug info
		System.out.println("Contact Del Result " + ht.toString());
		
		try {
			Vector v = (Vector) ht.get(KEY_CONTACT_ID_LIST);
			
			if (null != v) {
				contactIDs = new long[v.size()];
				
				for (int i = 0; i < v.size(); i++) {
					contactIDs[i] = ((Long) 
									v.elementAt(i)).longValue();
				}
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Could not parse vector." + e);
		}
	}
	
	public ContactsDeletionResult(int contactID) {
		//#debug info
		System.out.println("Contact Del Result");
		
		contactIDs = new long[1];
		contactIDs[0] = contactID;
	}
	
	public long[] getContactIDs() {
		return contactIDs;
	}

	public Hashtable toHashtable() {
		return null;
	}

}
