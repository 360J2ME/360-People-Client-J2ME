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

public class BulkUpdateContactsResult implements ServiceObject {
	private static final String	KEY_SERVER_REV_BEFORE = "serverrevisionbefore",
								KEY_SERVER_REV_AFTER = "serverrevisionafter",
								KEY_CONTACT_LIST = "contactlist",
								KEY_PROFILE = "userprofile",
								KEY_DETAIL_LIST = "detaillist",
								KEY_CONTACT_ID = "contactid",
								KEY_DETAIL_ID = "detailid",
								KEY_DELETED = "deleted",
								KEY_VAL = "val",
								KEY_ALT = "alt",
								KEY_BYTES = "bytes";
	
	public static final long ERROR_OCCURRED = -1;
	public static final long SINGULAR = -2;
	private static final long DETAIL_DELETED = -1;
	
	private long serverRevisionBefore, serverRevisionAfter;
	private long[] contactIDs;
	private boolean[] isContactDeleted;
	private boolean[] didErrorOccurUpdatingContact;
	private long[][] detailIDs;
	private boolean[][] isDetailDeleted;
	
	/**
	 * 
	 * Parses the response either carried out by a SetMe-request or by
	 * a BulkUpdateContacts-request. The response consists of a contactID-
	 * list and a detailID-list per contactID.
	 * 
	 * @param ht The hashtable to parse from.
	 * @param isMeUpdate If true the Hashtable contains a response to
	 */
	public BulkUpdateContactsResult(Hashtable ht, boolean isMeUpdate) {
		if (null == ht) {
			return;
		}
		
		if (ht.containsKey(KEY_SERVER_REV_AFTER)) {
			try {
				serverRevisionAfter = 
						((Long) ht.get(KEY_SERVER_REV_AFTER)).longValue();
				serverRevisionBefore = 
					((Long) ht.get(KEY_SERVER_REV_BEFORE)).longValue();
			} catch (Exception e) {
				//#debug info
				System.out.println("Could not cast server rev after.");
			}
		}
		
		if (isMeUpdate) {
			//#debug info
			System.out.println("Parsing me update.");
			parseMeUpdate(ht);
		} else {
			//#debug info
			System.out.println("Parsing contacts update.");
			parseContactsUpdate(ht);
		}
	}
	
	private void parseMeUpdate(Hashtable ht) {
		isContactDeleted = new boolean[1];
		isContactDeleted[0] = false;
		
		Hashtable htProfile = null;
		detailIDs = new long[1][];
		isDetailDeleted = new boolean[1][];
		try {
			htProfile = (Hashtable) ht.get(KEY_PROFILE);
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast userprofile Hashtable.");
		}
		
		if (null != htProfile) {
			Vector detailList = null;
			try {
				detailList = 
						(Vector) htProfile.get(KEY_DETAIL_LIST);
			} catch (Exception e) {
				//#debug info
				System.out.println("Could not cast detail list.");
			}
			
			if (null != detailList) {
				detailIDs[0] = new long[detailList.size()];
				isDetailDeleted[0] = new boolean[detailList.size()];
				Hashtable cDetail = null;
				for (int j = 0; j < detailList.size(); j++) {
					try {
						cDetail = (Hashtable) detailList.elementAt(j);
					} catch (Exception e) {
						//#debug info
						System.out.println("Could not cast contact detail.");
					}
					
					if (null != cDetail) {
						try {
							// we have a detail id, no singular field
							if (cDetail.containsKey(KEY_DETAIL_ID)) {
								long dID =
									((Long) cDetail.get(KEY_DETAIL_ID)).longValue();
								
								// an error occured manipulating the detail id
								if (dID < 0) {
									detailIDs[0][j] = ERROR_OCCURRED;
								} else {
									detailIDs[0][j] = dID;
								}
							} else {	// singular field
								detailIDs[0][j] = SINGULAR;
							}
							
							if ((!cDetail.containsKey(KEY_VAL)) &&
									(!cDetail.containsKey(KEY_ALT)) &&
									(!cDetail.containsKey(KEY_BYTES))) {
								isDetailDeleted[0][j] = true;
							} else {
								isDetailDeleted[0][j] = false;
							}
						} catch (Exception e) {
							//#debug error
							System.out.println("Could not get detail id. " + e);
						}
					} else {	// no contactdetail something went wrong
						detailIDs[0][j] = ERROR_OCCURRED;
					}
					
					cDetail = null;
				}
				detailList = null;
			}
		}
		htProfile = null;
	}
	
	private void parseContactsUpdate(Hashtable ht) {		
		Vector v = null;
		try {
			v = (Vector) ht.get(KEY_CONTACT_LIST);
		} catch (Exception e) {
			//#debug info
			System.out.println("Could not cast contact list.");
		}

		if (null == v) {
			//#debug info
			System.out.println("Contact list is null.");
			return;
		}
		
		contactIDs = new long[v.size()];
		isContactDeleted = new boolean[v.size()];
		isDetailDeleted = new boolean[v.size()][];
		didErrorOccurUpdatingContact = new boolean[v.size()];
		detailIDs = new long[v.size()][];
		Hashtable htContact = null;
		for (int i = 0; i < v.size(); i++) {
			try {
				//#debug debug
				System.out.println("Contact hashtable found.");
				htContact = (Hashtable) v.elementAt(i);
			} catch (Exception e) {
				//#debug info
				System.out.println("Could not cast contact at " + i + ".");
			}
			
			if (null != htContact) {
				try {
					contactIDs[i] = 
						((Long) htContact.get(KEY_CONTACT_ID)).longValue();
					
					if (contactIDs[i] != ERROR_OCCURRED) {
						didErrorOccurUpdatingContact[i] = false;
					} else {
						didErrorOccurUpdatingContact[i] = true;
					}
					
					//#debug debug
					System.out.println("Contact id: " + contactIDs[i]);
				} catch (Exception e) {
					//#debug info
					System.out.println("Could not cast contact id.");
				}
				
				try {
					isContactDeleted[i] = ((Boolean) 
									ht.get(KEY_DELETED)).booleanValue();
				} catch (Exception e) {
					//#debug info
					System.out.println("Could not cast id deleted.");
				}
				
				Vector detailList = null;
				try {
					detailList = 
							(Vector) htContact.get(KEY_DETAIL_LIST);
					//#debug debug
					System.out.println("Detail vector found.");
				} catch (Exception e) {
					//#debug info
					System.out.println("Could not cast detail vector.");
				}
				
				if (null != detailList) {
					detailIDs[i] = new long[detailList.size()];
					isDetailDeleted[i] = new boolean[detailList.size()];
					Hashtable cDetail = null;
					for (int j = 0; j < detailList.size(); j++) {
						try {
							cDetail = (Hashtable) detailList.elementAt(j);
						} catch (Exception e) {
							//#debug info
							System.out.println("Could not cast contact detail.");
						}
						
						if (null != cDetail) {
							try {
								detailIDs[i][j] = 
									((Long) cDetail.get(KEY_DETAIL_ID)).longValue();
								if (detailIDs[0][j] == DETAIL_DELETED) {
									isDetailDeleted[i][j] = true;
								} else {
									isDetailDeleted[i][j] = false;
								}
							} catch (Exception e) {
								//#debug info
								System.out.println("Could not get detail id.");
							}
						}
						
						cDetail = null;
					}
					detailList = null;
				}
			}
			htContact = null;
		}
	}
	
	public long getServerRevisionBefore() {
		return serverRevisionBefore;
	}
	
	public long getServerRevisionAfter() {
		return serverRevisionAfter;
	}

	public long[] getContactIDs() {
		return contactIDs;
	}
	
	/**
	 * 
	 * Returns true if the contact was deleted or
	 * false if he was not. The method also returns 
	 * false if the index is out of bounds or the
	 * isContactDeleted-array is null.
	 * 
	 * @param index The index of the contact to check
	 * for.
	 * 
	 * @return True if the found contact exists, false
	 * otherwise.
	 */
	public boolean isContactDeleted(int index) {
		if ((null == isContactDeleted) || 
			(index > isContactDeleted.length) ||
			(index < 0)) {
			return false;
		}
		
		return isContactDeleted[index];
	}

	public long[] getDetailIDsForContactID(int index) {
		if ((null == detailIDs) || 
			(index > detailIDs.length)) {
			return null;
		}
		
		return detailIDs[index];
	}
	
	public long[] getDetailIDsForMe() {
		if ((null == detailIDs) || 
			(detailIDs.length < 1)) {
			return null;
		}
		
		return detailIDs[0];
	}
	
	/**
	 * 
	 * Returns whether there were errors carrying out the update
	 * on the contact at the given index. If the
	 * 
	 * @return
	 */
	public boolean didErrorOccurUpdatingContact(int i) {
		return didErrorOccurUpdatingContact[i];
	}

	public Hashtable toHashtable() {
		return null;
	}

}
