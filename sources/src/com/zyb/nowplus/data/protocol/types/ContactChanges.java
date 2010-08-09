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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.zyb.util.TextUtilities;

public class ContactChanges implements ServiceObject 
{
	// Order of phone/email entries.
	private static final Integer TYPE_INTEGER_PREFERRED = new Integer(0);
	private static final Integer TYPE_INTEGER_STANDARD = new Integer(50);
	
	// friendship status indicator
	public static final int UNCHANGED = 0, DISCONNECTED = 1, CONNECTED = 2;	
	
	// identifies the key keyword in a contactdetail
	public static final String K_DETAIL_VALUE = "val",
								K_DETAIL_KEY = "key",
								K_DELETED = "deleted",
								K_PHOTO_URL = "bytesurl",
								K_TYPE = "type",
								K_ORDER = "order",
								K_DETAIL = "detail",
								K_DETAIL_LIST = "detaillist",
								K_DETAIL_ID = "detailid",
								K_CONTACT_ID = "contactid",
								K_USER_ID = "userid",
								K_FRIEND = "friend",
								K_DETAIL_ALT = "alt",
								K_DETAIL_BYTES = "bytes",
								K_DETAIL_MIME = "bytesmime",
								K_DETAIL_TYPE = "type",								
								K_GROUP_ID_LIST = "groupidlist",
								K_SOURCES1 = "sourcelist",
								K_SOURCES2 = "sources";
	

	// define keys that are in the hashtable
	public static final String 	KEY_CONTACT_ID = "contactid",
								KEY_DELETED = "deleted", 
	
								KEY_NAME = "vcard.name", 
								KEY_NICKNAME = "vcard.nickname", 
								KEY_IMADDRESS = "vcard.imaddress",
								KEY_EMAIL = "vcard.email", 
								KEY_PHONE = "vcard.phone", 
								KEY_DATE = "vcard.date", 
								KEY_PHOTO = "photo", 
								KEY_ADDRESS = "vcard.address", 
								KEY_SOCIAL_NETWORK = "vcard.internetaddress", 
								KEY_URL = "vcard.url",
								KEY_PRESENCE = "presence.text",
								KEY_ORG = "vcard.org",
								KEY_TITLE = "vcard.title",
								KEY_NOTE = "vcard.note",
								KEY_SYNC_TO_PHONE = "synctophone";

	public static final int ERROR_DURING_ADD = -1;
	public static final long UNKNOWN_DETAIL_ID = -1;
	
	public static final int ADDRESS_PARTS_LENGTH = 7;
	
	public static final char C_TRUE = 'T',
							 C_FALSE = 'F',
							 C_UNCHANGED = 0;
	
	public static final Integer K_ORDER_PREF = new Integer(0);
					
	private long contactID;
	private boolean contactDeleted;
	
	private char syncToPhone;
	
	private long userID;	
		
	private int friendshipState;

	private String firstName;
	private String middleNames;
	private String lastName;
	private boolean nameDeleted;
	
	private String nickname;
	private boolean nicknameDeleted;
	
	private String avatarURL;
	private boolean wasAvatarDeleted;
	
	private int yearOfBirth;
	private int monthOfBirth;
	private int dayOfBirth;
	private boolean dateOfBirthDeleted;
	
	private long[] groupIDs;
	
	private long[] phoneDetailIDs;
	private String[] phones;
	private String[] phoneTypes;
	private boolean[] phonePrefs;
	private boolean[] phoneDeleted;

	private long[] emailDetailIDs;
	private String[] emails;
	private String[] emailTypes;
	private boolean[] emailPrefs;
	private boolean[] emailDeleted;
	
	private long[] addressDetailIDs;
	private String[][] addresses;
	private String[] addressTypes;
	private boolean[] addressDeleted;

	private long[]	 imAddressDetailIDs;
	private String[] imAddressServices;
	private String[] imAddresses;
	private boolean[] imAddressDeleted;
	
	private long[] socialNetworkDetailIDs;
	private String[] socialNetworkNames;
	private String[] socialNetworkAccountNames;
	private String[] socialNetworkAccountUrls;
	private boolean[] socialNetworkDeleted;

	private String url;
	private String urlType;
	private boolean urlDeleted;
	
	private String title;
	private boolean titleDeleted;
	
	private String department;
	private String organization;
	private boolean organizationDeleted;

	private long[] notesDetailIDs;
	private String[] notes;
	private boolean[] notesDeleted;
	
	private String statusText;
	private String statusTextNetwork;
	private boolean wasStatusTextDeleted;
	
	private long serverRevisionAnchor,
	 currentServerRevision;
	private int numberOfPages;
	
	private Vector store;

	public ContactChanges() {
		store = new Vector();
	}
	
	public ContactChanges(long contactID) {
		contactDeleted = false;
		this.contactID = contactID;
		store = new Vector();
	}
	
	public ContactChanges(Hashtable ht) {
		contactDeleted = false;
		contactID = UNKNOWN_DETAIL_ID;
		userID = UNKNOWN_DETAIL_ID;	// marks whether a user is a now+ user
		friendshipState = UNCHANGED;
		
		if (null != ht) {
		
			Vector contactDetails = null;
			
			if (ht.containsKey(K_CONTACT_ID)) {
				try {
					contactID = ((Long) ht.get(K_CONTACT_ID)).longValue();
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast contact id into long.");
				}
			}
			
			if (ht.containsKey(K_DELETED)) {
				contactDeleted = true;
			}
			
			if (ht.containsKey(K_USER_ID)) {
				try {
					userID = ((Long) ht.get(K_USER_ID)).longValue();
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast user id into long.");
				}
			}
			if (userID == 0) {
				userID = UNKNOWN_DETAIL_ID;
			}

			if (ht.containsKey(K_FRIEND)) {
				try {
					boolean isFriend = ((Boolean) ht.get(K_FRIEND)).booleanValue();
					
					if (isFriend) {
						friendshipState = CONNECTED;
					} else {
						friendshipState = DISCONNECTED;
					}
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not fetch isConnected.");
				}
			}
			
			Vector groupIDList = null;
			if (ht.containsKey(K_GROUP_ID_LIST)) {
				try {
					groupIDList = (Vector) ht.get(K_GROUP_ID_LIST);
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast into Vector.");
				}
			}
			
			if (null != groupIDList) {
				groupIDs = new long[groupIDList.size()];
				
				for (int i = 0; i < groupIDList.size(); i++) {
					try {
						groupIDs[i] = 
							((Long) groupIDList.elementAt(i)).longValue();	
					} catch (Exception e) {
						//#debug debug
						System.out.println("Could not cast group id at " + i);
					}
				}
			}
			groupIDList = null;
			
			syncToPhone = C_UNCHANGED;
		    if (ht.containsKey(KEY_SYNC_TO_PHONE)) {
				try {
					boolean doSyncToPhone = ((Boolean) ht.get(KEY_SYNC_TO_PHONE)).booleanValue();
					
					if (doSyncToPhone) {
						syncToPhone = C_TRUE;
					} else {
						syncToPhone = C_FALSE;
					}
				} catch (Exception e) {
					//#debug error
					System.out.println("Exception " + e);
				}
		    }
		    
			if (ht.containsKey(K_DETAIL_LIST)) {
				try {
					contactDetails = (Vector) ht.get(K_DETAIL_LIST);
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast into Vector.");
				}
			} else if (ht.containsKey(K_DETAIL)) {	// single detail
				contactDetails = new Vector(1);
				
				try {
					Hashtable contactDetail = (Hashtable) ht.get(K_DETAIL);
					contactDetails.addElement(contactDetail);
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast into Hashtable.");
				}
			}
			
			if (null != contactDetails) {
				// finds out amount of details of the same type 
				initWithContactDetails(contactDetails);
				
				// fills the arrays with contactDetails
				populateContactDetails(contactDetails);
			}
			contactDetails = null;
		}
	}
	
	
	private void initWithContactDetails(final Vector contactDetails) {
		if (null == contactDetails) {
			return;
		}
		
		Hashtable htContactDetail = null;
		
		// counters we need to init the contactdetail arrays
		int counterIMAddress = 0;
		int counterEmail = 0;
		int counterPhone = 0;
		int counterAddress = 0;
		int counterInternetAddress = 0;
		int counterNote = 0;
		
		// iterate through vector of contact details and prepare arrays...					
		for (int i = 0; i < contactDetails.size(); i++) {
			try {
				htContactDetail = (Hashtable) contactDetails.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast into Hashtable.");
			}
				
			if (null != htContactDetail) {
				if (htContactDetail.containsKey(K_DETAIL_KEY)) {
					String detailValue = (String)htContactDetail.get(K_DETAIL_KEY);
					
					// now we take a look what kind of detail we have
					// and increment the counters
					if (null != detailValue) {
						if (detailValue.equals(KEY_IMADDRESS)) {
							counterIMAddress++;
						} else if (detailValue.equals(KEY_EMAIL)) {
							counterEmail++;
						} else if (detailValue.equals(KEY_PHONE)) {
								counterPhone++;
						} else if (detailValue.equals(KEY_ADDRESS)) {
							counterAddress++;
						} else if (detailValue.equals(KEY_SOCIAL_NETWORK)) {
							counterInternetAddress++;
						} else if (detailValue.equals(KEY_NOTE)) {
							counterNote++;
						}
					}
				}
			}
		}
		
		// initialize array-sizes
		if (0 != counterIMAddress) {
			imAddresses = new String[counterIMAddress];
			imAddressDetailIDs = new long[counterIMAddress];
			imAddressServices = new String[counterIMAddress];
			imAddressDeleted = new boolean[counterIMAddress];
		}
		if (0 != counterEmail) {
			emails = new String[counterEmail];
			emailTypes = new String[counterEmail];
			emailDetailIDs = new long[counterEmail];
			emailPrefs = new boolean[counterEmail];
			emailDeleted = new boolean[counterEmail];
		}
		if (0 != counterPhone) {
			phones = new String[counterPhone];
			phoneTypes = new String[counterPhone];
			phoneDetailIDs = new long[counterPhone];
			phonePrefs = new boolean[counterPhone];
			phoneDeleted = new boolean[counterPhone];
		}
		if (0 != counterAddress) {
			addresses = new String[counterAddress][5];
			addressTypes = new String[counterAddress];
			addressDetailIDs = new long[counterAddress];
			addressDeleted = new boolean[counterAddress];
		}
		if (0 != counterInternetAddress) {
			socialNetworkAccountNames = new String[counterInternetAddress];
			socialNetworkNames = new String[counterInternetAddress];
			socialNetworkDetailIDs = new long[counterInternetAddress];
			socialNetworkDeleted = new boolean[counterInternetAddress];
		}
		if (0 != counterNote) {
			notes = new String[counterNote];
			notesDetailIDs = new long[counterNote];
			notesDeleted = new boolean[counterNote];
		}
	}		

	private void populateContactDetails(final Vector contactDetails) {
		if (null == contactDetails) {
			return;
		}
		
		Hashtable htContactDetail = null;
		
		nameDeleted = false;
		wasAvatarDeleted = false;
		dateOfBirthDeleted = false;
		nameDeleted = false;
		wasStatusTextDeleted = false;
		
		// we need these to keep the index
		int indexIMAddress = 0;
		int indexEmail = 0;
		int indexPhone = 0;
		int indexAddress = 0;
		int indexInternetAddress = 0;
		int indexNotes = 0;
		
		//#debug contactoutput
		System.out.println("--------------New contact + " + currentServerRevision + " " + serverRevisionAnchor + " + ----------------");
		
		
		// iterate through contactDetails again and poputlate arrays now					
		for (int i = 0; i < contactDetails.size(); i++) {
			try {
				htContactDetail = (Hashtable) contactDetails.elementAt(i);
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast into Hashtable.");
			}
			
			
			if (null != htContactDetail) {
				
				//Will print out all data relating a contact
				//#mdebug contactoutput
				try {
					System.out.println("---details set---");

					Enumeration keys = htContactDetail.keys();
					while(keys.hasMoreElements()) {
						Object obj = keys.nextElement();
						System.out.println(obj+":"+htContactDetail.get(obj));
					}
				} catch(Exception e) {
					System.out.println("error:"+e.getMessage());
				}
				//#enddebug
				
				if (htContactDetail.containsKey(K_DETAIL_KEY)) {
					String detailKey = (String)htContactDetail.get(K_DETAIL_KEY);
					Object detailValue = null;
					long detailID = UNKNOWN_DETAIL_ID;
					
					if (htContactDetail.containsKey(K_DETAIL_VALUE)) {
						detailValue = htContactDetail.get(K_DETAIL_VALUE);
					}
					
					if (htContactDetail.containsKey(K_DETAIL_ID)) {
						detailID= ((Long)htContactDetail.get(K_DETAIL_ID)).longValue();
					}
				
					try {
						// now we take a look what kind of detail we have
						// and increment the counters
						if (null != detailKey) {
							if (detailKey.equals(KEY_NAME)) {
								//#debug debug
								System.out.println("VCard Name: " + detailValue);
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										nameDeleted = true;
									}
								} else {
									String[] vcard = TextUtilities.getSubstrings(((String)detailValue), 
																				';', '\\', false);
									 
									if (null != vcard) {							
										for (int j = 0; j < vcard.length; j++) {
											switch (j) {
												case 0:
													lastName = TextUtilities.unescapeTokens(
																			vcard[0], '\\');
													break;
												case 1:
													firstName = TextUtilities.unescapeTokens(
															vcard[1], '\\');
													break;
												case 2:
													middleNames = TextUtilities.unescapeTokens(
															vcard[2], '\\');
													break;
											}
										}
									}
									
									//#debug debug
									System.out.println("NAMES: " + firstName + " " + middleNames + " " + lastName);
								}
							} else if (detailKey.equals(KEY_NICKNAME)) {
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										nicknameDeleted = true;
									}
								} else {
									nickname = TextUtilities.unescapeTokens((String) detailValue, '\\');
								}
							} else if (detailKey.equals(KEY_DATE)) {
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										dateOfBirthDeleted = true;
									}
								} else {
									String dob = (String) detailValue;
									
									if (null != dob) {
										String[] dateParts = TextUtilities.getSubstrings(dob, '-',
																					' ', true);
										
										if (null != dateParts) {
											try {
												yearOfBirth = Integer.parseInt(dateParts[0]);
											} catch (Exception e) {
												//#debug error
												System.out.println("Exception " + e);
											}
											
											try {
												monthOfBirth = Integer.parseInt(dateParts[1]);
											} catch (Exception e) {
												//#debug error
												System.out.println("Exception " + e);
											}
											
											try {
												dayOfBirth = Integer.parseInt(dateParts[2]);
											} catch (Exception e) {
												//#debug error
												System.out.println("Exception " + e);
											}
										}
									}
								}
							} else if (detailKey.equals(KEY_URL)) {
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										//#debug debug
										System.out.println("DELETED URL ENTRY!!!");
										urlDeleted = true;
									}
								} else {
									url = (String) detailValue;
									
									if (htContactDetail.containsKey(K_DETAIL_TYPE)) {
										urlType = (String) htContactDetail.get(K_DETAIL_TYPE);
									}
								}
							} else if (detailKey.equals(KEY_ORG)) {							
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										organizationDeleted = true;
									}
								} else {
									
									String[] orgDetails = TextUtilities.getSubstrings(((String) detailValue),
																				';', '\\', false);
									
									if (null != orgDetails) {
										if (orgDetails.length > 0) {
											organization = TextUtilities.unescapeTokens(
																	orgDetails[0], '\\');										
										}
										if (orgDetails.length > 1) {
											department = TextUtilities.unescapeTokens(
													orgDetails[1], '\\');
										}
									}
								}
							} else if (detailKey.equals(KEY_TITLE)) {							
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										titleDeleted = true;
									}
								} else {
									title = (String) detailValue;
								}
							} else if (detailKey.equals(KEY_PRESENCE)) {
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										wasStatusTextDeleted = true;
									}
								} else {
									statusText = (String) detailValue;
									
									if (htContactDetail.containsKey(K_DETAIL_TYPE)) {
										statusTextNetwork = (String) htContactDetail.get(K_DETAIL_TYPE);
									}
								}
							} else if (detailKey.equals(KEY_PHOTO)) {
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										wasAvatarDeleted = true;
									}
								} else {
									//Use "avatarURL = detailValue;" instead if data is stored in val
									//else this if stored in "bytesurl"
									avatarURL = (String) detailValue;
									
									//#debug debug
									System.out.println("avatarURL:"+avatarURL);
									
									/*try {
										avatarURL = (String) htContactDetail.get(K_PHOTO_URL);
									} catch (Exception ee) {
										avatarURL = null;
										
										//#debug error
										System.out.println("Could not cast photoUrl.");
									}*/
								}
							} else if (detailKey.equals(KEY_IMADDRESS)) {
								imAddressDetailIDs[indexIMAddress] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										imAddressDeleted[indexIMAddress] = true;
									}
								} else {
									imAddresses[indexIMAddress] = (String) detailValue;
									
									try {
										imAddressServices[indexIMAddress] = (String) htContactDetail.get(K_TYPE);
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast detail type.");
									}
								}								
								indexIMAddress++;
							} else if (detailKey.equals(KEY_EMAIL)) {
								emailDetailIDs[indexEmail] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										emailDeleted[indexEmail] = true;
									}
								} else {
									emails[indexEmail] = (String) detailValue;
									
									try {
										emailTypes[indexEmail] = (String) htContactDetail.get(K_TYPE);
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast detail type.");
									}
									
									try {
										emailPrefs[indexEmail] = K_ORDER_PREF.equals(htContactDetail.get(K_ORDER));
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast order.");
									}									
								}
								
								indexEmail++;
							} else if (detailKey.equals(KEY_NOTE)) {
								notesDetailIDs[indexNotes] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										notesDeleted[indexNotes] = true;
									}
								} else {
									notes[indexNotes] = (String) detailValue;
								}
								
								indexNotes++;
							} else if (detailKey.equals(KEY_PHONE)) {
								phoneDetailIDs[indexPhone] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										phoneDeleted[indexPhone] = true;
									}
								} else {
									phones[indexPhone] = (String) detailValue;
									
									try {
										phoneTypes[indexPhone] = (String) htContactDetail.get(K_TYPE);
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast detail type.");
									}
									
									try {
										phonePrefs[indexPhone] = K_ORDER_PREF.equals(htContactDetail.get(K_ORDER));
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast order.");
									}
								}
								
								indexPhone++;
							} else if (detailKey.equals(KEY_ADDRESS)) {
								addressDetailIDs[indexAddress] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										addressDeleted[indexAddress] = true;
									}
								} else {	
									String[] addressParts = TextUtilities.getSubstrings(((String) detailValue), ';', 
															'\\', false);
									
									if ((null != addressParts) && (addressParts.length > 0)) {
										addresses[indexAddress] = new String[ADDRESS_PARTS_LENGTH];
									
										System.arraycopy(addressParts, 0, addresses[indexAddress], 
																	0, addressParts.length);
										addressParts = null;
									}
									
									if (null != addresses[indexAddress]) {
										for (int j = 0; j <addresses[indexAddress].length; j++) {
											addresses[indexAddress][j] = TextUtilities.unescapeTokens(
																			addresses[indexAddress][j], '\\'); 
										}
									}
											
									try {
										addressTypes[indexAddress] = (String) htContactDetail.get(K_TYPE);
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast detail type.");
									}
								}
								
								indexAddress++;
							} else if (detailKey.equals(KEY_SOCIAL_NETWORK)) {
								socialNetworkDetailIDs[indexInternetAddress] = detailID;
								
								// if there is a deleted flag we mark the detail as deleted
								if (htContactDetail.containsKey(K_DELETED)) {
									if (((Boolean) htContactDetail.get(K_DELETED)).booleanValue()) {
										socialNetworkDeleted[indexInternetAddress] = true;
									}
								} else {
									socialNetworkAccountNames[indexInternetAddress] = (String) detailValue;
									
									try {
										socialNetworkNames[indexInternetAddress] = 
														(String) htContactDetail.get(K_TYPE);
									} catch (Exception ee) {
										//#debug error
										System.out.println("Could not cast detail type.");
									}
								}
								
								indexInternetAddress++;
							}
						}
					} catch (Exception e) {
						//#debug error
						System.out.println("Could not write detail: " + detailKey + e);
					}
				}
			}
		}
	}

	public void setContactId(long cid) {
		this.contactID = cid;
	}
	
	public long getContactId() {
		return contactID;
	}
	
	public void setContactDeleted() {
		contactDeleted = true;
	}
	
	public boolean isContactDeleted() {
		return contactDeleted;
	}
	
	public char getSyncToPhone() {
		return syncToPhone;
	}
	
	public long getUserId() {
		return userID;
	}
	
	public int getFriendshipState() {
		return friendshipState;
	}
	
	public void setName(String firstName, String middleNames, String lastName) {
		updateVcardNameForContact(firstName, middleNames, lastName);
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getMiddleNames() {
		return middleNames;
	}

	public String getLastName() {
		return lastName;
	}
	
	public void setNameDeleted() {
		deleteFieldFromContact(KEY_NAME);
	}
	
	public boolean isNameDeleted() {
		return nameDeleted;
	}
	
	public void setNickname(String nickname) {
		updateVcardNicknameForContact(nickname);
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNicknameDeleted() {
		deleteFieldFromContact(KEY_NICKNAME);
	}
	
	public boolean isNicknameDeleted() {
		return nicknameDeleted;
	}
	
	public String getAvatarURL() {
		return avatarURL;
	}	
	
	public boolean isAvatarDeleted() {
		return wasAvatarDeleted;
	}
	
	public void setDateOfBirth(int year, int month, int day) {
		updateVcardBirthdayForContact(year, month, day);
	}
	
	public int getYearOfBirth() {
		return yearOfBirth;
	}
	
	public int getMonthOfBirth() {
		return monthOfBirth;
	}
	
	public int getDayOfBirth() {
		return dayOfBirth;
	}
	
	public void setDateOfBirthDeleted() {
		deleteFieldFromContact(KEY_DATE);
	}
	
	public boolean isDateOfBirthDeleted() {
		return dateOfBirthDeleted;
	}
	
	public long[] getGroupIDs() {
		return groupIDs;
	}
	
	/**
	 * 
	 * Add a phone number to a contact.
	 * 
	 * @param number The phone number for the contact.
	 * @param type The type of the number.
	 * 
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the newly created field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int addVcardPhoneToContact(String number, String type, boolean preferred) {
		return updateVcardPhoneForContact(UNKNOWN_DETAIL_ID, 
											number, type, preferred);
	}
	
	/**
	 * 
	 * Update a phone number of a contact.
	 * 
	 * @param number The phone number for the contact.
	 * @param type The type of the number.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int updateVcardPhoneForContact(long detailID, String number, String type, boolean preferred)
	{
		Hashtable cDetail = getHashtableForDetail(detailID,ContactChanges.KEY_PHONE, number, type, null, preferred ? TYPE_INTEGER_PREFERRED : TYPE_INTEGER_STANDARD, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	public int deleteVcardPhoneForContact(long detailID) {
		return deleteFieldFromContact(detailID, ContactChanges.KEY_PHONE);
	}
		
	public long[] getPhoneDetailIDs() {
		return phoneDetailIDs;
	}
	
	public String[] getPhones() {
		return phones;
	}
	
	public String[] getPhoneTypes() {
		return phoneTypes;
	}
	
	public boolean[] getPhonePrefs() {
		return phonePrefs;
	}

	public boolean[] isPhoneDeleted() {
		return phoneDeleted;
	}

	/**
	 * 
	 * Add an email to a contact.
	 * 
	 * @param email The email for the contact.
	 * @param type The type of the email.
	 * 
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the newly created field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int addVcardEmailToContact(String email, String type, boolean preferred) {
		return updateVcardEmailForContact(UNKNOWN_DETAIL_ID, 
											email, type, preferred);
	}
	
	/**
	 * 
	 * Update an email of a contact.
	 * 
	 * @param email The email for the contact.
	 * @param type The type of the email.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int updateVcardEmailForContact(long detailID, String email,String type, boolean preferred) 
	{
		Hashtable cDetail = getHashtableForDetail(detailID,ContactChanges.KEY_EMAIL, email,type, null, preferred ? TYPE_INTEGER_PREFERRED : TYPE_INTEGER_STANDARD, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	public int deleteVcardEmailForContact(long detailID) {
		return this.deleteFieldFromContact(detailID, ContactChanges.KEY_EMAIL);
	}
		
	public long[] getEmailDetailIDs() {
		return emailDetailIDs;
	}
	
	public String[] getEmails() {
		return emails;
	}

	public String[] getEmailTypes() {
		return emailTypes;
	}

	public boolean[] getEmailPrefs() {
		return emailPrefs;
	}
	
	public boolean[] isEmailDeleted() {
		return emailDeleted;
	}	

	/**
	 * 
	 * Add an address to a contact.
	 * 
	 * @param street The street.
	 * @param city The city.
	 * @param state The state.
	 * @param zipCode The zipCode.
	 * @param country The country.
	 * 
	 * @param type The type of the address.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the newly created field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int addVcardAddressToContact(String extra, 
										String street,
										String city,
										String state,
										String zipCode,
										String country, 
										String type) {
		return updateVcardAddressForContact(UNKNOWN_DETAIL_ID, 
										    extra,
											street,
											city,
											state,
											zipCode,
											country,
											type);
	}
	
	/**
	 * 
	 * Update an address of a contact.
	 * 
	 * @param street The street.
	 * @param city The city.
	 * @param state The state.
	 * @param zipCode The zipCode.
	 * @param country The country.
	 * @param type The type of the number.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int updateVcardAddressForContact(long detailID,
											String extra,
											String street,
											String city,
											String state,
											String zipCode,
											String country, 
											String type) {
		extra = TextUtilities.escapeTokens(extra, '\\');
		street = TextUtilities.escapeTokens(street, '\\');
		city = TextUtilities.escapeTokens(city, '\\');
		state = TextUtilities.escapeTokens(state, '\\');
		zipCode = TextUtilities.escapeTokens(zipCode, '\\');
		country = TextUtilities.escapeTokens(country, '\\');
		
		StringBuffer address = new StringBuffer();
		address.append(";");
		address.append(((null == street) ? "" : street) + ";");
		address.append(((null == extra) ? "" : extra) + ";");
		address.append(((null == city) ? "" : city) + ";");
		address.append(((null == state) ? "" : state) + ";");
		address.append(((null == zipCode) ? "" : zipCode) + ";");
		address.append(((null == country) ? "" : country));
		
		Hashtable cDetail = getHashtableForDetail(detailID,
									ContactChanges.KEY_ADDRESS, 
									address.toString(),
									type, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	public int deleteVcardAddressForContact(long detailID)
	{
		return this.deleteFieldFromContact(detailID, ContactChanges.KEY_ADDRESS);
	}
	
	public long[] getAddressDetailIDs() {
		return addressDetailIDs;
	}
	
	public String[][] getAddresses() {
		return addresses;
	}

	public String[] getAddressTypes() {
		return addressTypes;
	}
	
	public boolean[] isAddressDeleted() {
		return addressDeleted;
	}
	
	public long[] getImAddressDetailIDs() {
		return imAddressDetailIDs;
	}
	
	public String[] getImAddressServices() {
		return imAddressServices;
	}
	
	public String[] getImAddresses() {
		return imAddresses;
	}
	
	public boolean[] isImAddressDeleted() {
		return imAddressDeleted;
	}

	public long[] getSocialNetworkDetailIDs() {
		return socialNetworkDetailIDs;
	}

	public String[] getSocialNetworkNames() {
		return socialNetworkNames;
	}	

	public String[] getSocialNetworkAccountNames() {
		return socialNetworkAccountNames;
	}

	public String[] getSocialNetworkAccountUrls() {
		return socialNetworkAccountUrls;
	}

	public boolean[] isSocialNetworkDeleted() {
		return socialNetworkDeleted;
	}

	public void setUrl(String url, String urlType) {
		updateVcardURLForContact(UNKNOWN_DETAIL_ID, 
				url);
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getUrlType() {
		return urlType;
	}
	
	public void setUrlDeleted() {
		deleteFieldFromContact(KEY_URL);
	}
	
	public boolean isUrlDeleted() {
		return urlDeleted;
	}

	public void setTitle(String title) {
		updateVcardTitleForContact(title);
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitleDeleted() {
		deleteFieldFromContact(KEY_TITLE);
	}
	
	public boolean isTitleDeleted() {
		return titleDeleted;
	}
		
	public void setOrganization(String organization, 
											String department) {
		updateVcardOrganizationForContact(organization, 
												department);
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public String getDepartment() {
		return department;
	}

	public void setOrganizationDeleted() {
		deleteFieldFromContact(KEY_ORG);
	}
	
	public boolean isOrganizationDeleted() {
		return organizationDeleted;
	}
	
	/**
	 * 
	 * Add a note to a contact.
	 * 
	 * @param notes The notes for the contact.
	 * 
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the newly created field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int addVcardNotesToContact(String notes) {
		return updateVcardNotesForContact(UNKNOWN_DETAIL_ID, 
											notes);
	}
	
	/**
	 * 
	 * Update a note of a contact.
	 * 
	 * @param notes The notes for the contact.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	public int updateVcardNotesForContact(long detailID, 
											String notes) {
		Hashtable cDetail = getHashtableForDetail(detailID,
								ContactChanges.KEY_NOTE, notes,
									null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	public int deleteVcardNotesForContact(long detailID) {
		return deleteFieldFromContact(detailID, ContactChanges.KEY_NOTE);
	}
	
	public long[] getNotesDetailIDs() {
		return notesDetailIDs;
	}
	
	public String[] getNotes() {
		return notes;
	}
	
	public boolean[] isNotesDeleted() {
		return notesDeleted;
	}

	public void setStatusText(String statusText) {
		updateVcardStatusTextForContact(statusText);
	}

	public String getStatusText() {
		return statusText;
	}
	
	public String getStatusTextNetwork() {
		return statusTextNetwork;
	}
	
	public void setStatusTextDeleted() {
		deleteFieldFromContact(KEY_PRESENCE);
	}	
	
	public boolean isStatusTextDeleted() {
		return wasStatusTextDeleted;
	}
	
	public long getServerRevisionAnchor() {
		return serverRevisionAnchor;
	}

	public void setServerRevisionAnchor(long serverRevisionAnchor) {
		//#debug debug
		System.out.println("SETTING SERVER REVISION ANCHOR: " + serverRevisionAnchor);
		
		this.serverRevisionAnchor = serverRevisionAnchor;
	}

	public long getCurrentServerRevision() {
		return currentServerRevision;
	}

	public void setCurrentServerRevision(long currentServerRevision) {
		//#debug debug
		System.out.println("SETTING CURRENT SERVER REV: " + currentServerRevision);
		
		this.currentServerRevision = currentServerRevision;
	}
	
	public int getNumberOfPages() {
		return numberOfPages;
	}
	
	public void setNumberOfPages(int numberOfPages) {
		//#debug debug
		System.out.println("SETTING NUM PAGES: " + numberOfPages);
		
		this.numberOfPages = numberOfPages;
	}
	
	/**
	 * 
	 * Add the name for updating of a contact to the request.
	 * 
	 * @param detailID The ID of the detail to be edited.
	 * @param firstName The first name.
	 * @param middleNames The middle names.
	 * @param lastName The last name.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardNameForContact(String firstName, 
							String middleNames, String lastName) {
		firstName = TextUtilities.escapeTokens(firstName, '\\');
		middleNames = TextUtilities.escapeTokens(middleNames, '\\');
		lastName = TextUtilities.escapeTokens(lastName, '\\');
		
		StringBuffer sb = new StringBuffer();
		if (null != lastName) {
			sb.append(lastName);
			lastName = null;
		}
		sb.append(";");
		if (null != firstName) {
			sb.append(firstName);
			firstName = null;
		}
		sb.append(";");
		if (null != middleNames) {
			sb.append(middleNames);
			middleNames = null;
		}
		sb.append(";");

		Hashtable cDetail = getHashtableForDetail(
				UNKNOWN_DETAIL_ID,
				ContactChanges.KEY_NAME, sb.toString(),
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Add the name for updating of a contact to the request.
	 * 
	 * @param detailID The ID of the detail to be edited.
	 * @param nickname The nickname to update.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardNicknameForContact(String nickname) {
		nickname = TextUtilities.escapeTokens(nickname, '\\');
		
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID,
				ContactChanges.KEY_NICKNAME, nickname,
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Update the birthday of a contact.
	 * 
	 * @param year Birth year of the contact.
	 * @param month Birth month of the contact.
	 * @param day Birth day of the contact.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardBirthdayForContact(int year, int month,
											int day) {
		String bday = year + "-" + ((month < 10) ? "0" : "") + month + 
							 "-" + ((day < 10) ? "0" : "") + day;
		
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID, 
				ContactChanges.KEY_DATE, bday,
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Update a URL of a contact.
	 * 
	 * @param url The URL for the contact.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardURLForContact(long detailID, 
											String url) {
		Hashtable cDetail = getHashtableForDetail(detailID,
				ContactChanges.KEY_URL, url,
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Add the title for updating of a contact to the request.
	 * 
	 * @param detailID The ID of the detail to be edited.
	 * @param title The working title of the contact.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardTitleForContact(String title) {
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID, ContactChanges.KEY_TITLE, title,
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Add the organization for updating of a contact to the request.
	 * 
	 * @param detailID The ID of the detail to be edited.
	 * @param organization The working organization of the contact.
	 * @param department The department of the contact.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardOrganizationForContact(String organization,
												 String department) {
		organization = TextUtilities.escapeTokens(organization, '\\');
		department = TextUtilities.escapeTokens(department, '\\');
		
		StringBuffer sb = new StringBuffer();
		if (null != organization) {
			sb.append(organization);
		}
		sb.append(';');
		if (null != department) {
			sb.append(department);
		}
		
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID,
				ContactChanges.KEY_ORG, sb.toString(),
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Add the status text for updating of a contact to the request.
	 * 
	 * @param detailID The ID of the detail to be edited.
	 * @param statusText The status text to update.
	 * 
	 * @return The index of the field added. Needs to be 
	 * known when the response comes in to be able to match 
	 * the detail id to the updated field or in case
	 * of the name where no detail ID is created matched
	 * to the position.
	 * 
	 */
	private int updateVcardStatusTextForContact(String statusText) {
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID,
				ContactChanges.KEY_PRESENCE, statusText,
				null, null, null, null, null);

		store.addElement(cDetail);
		return store.size() - 1;
	}

	/**
	 * 
	 * Adds a field for deletion in the request. The detail ID needs
	 * to be passed in order to be able to delete the field.
	 * 
	 * @param detailID The detail ID gives indication which field 
	 * should be deleted.
	 * 
	 * @return The index of the detailID deleted.
	 * 
	 */
	public int deleteFieldFromContact(long detailID, String key) {
		Hashtable cDetail = getHashtableForDetail(detailID, key, null,
													null, null, null, null,
														null);
		store.addElement(cDetail);
		
		return store.size() - 1;
	}
	
	/**
	 * 
	 * Adds a field for deletion in the request. The fieldName needs
	 * to be passed in order to be able to delete the field. This
	 * deletes fields which have no detail ID like vcard.name,
	 * vcard.title, etc.
	 * 
	 * @param fieldName The fieldName of the request. Only fields that
	 * do not have an ID are allowed in this method. Field names can
	 * ne found in {@link ContactsChanges}: e.g. 
	 * ContactsChanges.KEY_NAME.
	 * 
	 * @return The index of the detailID deleted.
	 * 
	 */
	public int deleteFieldFromContact(String fieldName) {
		Hashtable cDetail = getHashtableForDetail(UNKNOWN_DETAIL_ID, fieldName, null,
													null, null, null, null,
														null);
		store.addElement(cDetail);
		
		return store.size() - 1;
	}	
	
	/**
	 * 
	 * Creates a contact detail-hashtable to be used for
	 * adding updating or deleting a contact detail.
	 * 
	 * @param detailID The ID of the detail, or 
	 * UNKNOWN_DETAIL_ID if the detail is being added or
	 * the detail does not have a detail id (e.g. name fields).
	 * @param key The key, e.g. vcard.name.
	 * @param val The value for the key.
	 * @param type The type for the key. E.g. cell for a mobile 
	 * phone number.
	 * @param alt The alt value, used in rare cases.
	 * @param bytes The bytes for a photo.
	 * @param bytesMime The mime of the photo.
	 * 
	 * @return A Hashtable resembling the contact detail.
	 * 
	 */
	private Hashtable getHashtableForDetail(long detailID, 
									String key, String val,
									String type, String alt, Integer order,
									byte[] bytes, String bytesMime) {
		Hashtable ht = new Hashtable();
		
		if (null != key) {
			ht.put(K_DETAIL_KEY, key);
		}
		if (null != val) {
			ht.put(K_DETAIL_VALUE, val);
		}
		if (null != type) {
			ht.put(K_DETAIL_TYPE, type);
		}
		if (null != alt) {
			ht.put(K_DETAIL_ALT, alt);
		}
		if (null != order) {
			ht.put(K_ORDER, order);
		}
		if (null != bytes) {
			ht.put(K_DETAIL_BYTES, bytes);
		}
		if (null != bytesMime) {
			ht.put(K_DETAIL_MIME, bytesMime);
		}
		if (UNKNOWN_DETAIL_ID != detailID) {
			ht.put(K_DETAIL_ID, new Long(detailID));
		}
		
		return ht;
	}
	
	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		if (UNKNOWN_DETAIL_ID != contactID) {
			ht.put(KEY_CONTACT_ID, new Long(contactID));
			
			if (contactDeleted) {
				ht.put(KEY_DELETED, new Boolean(true));
			}
		}
		
		if (null != store) {
			ht.put(K_DETAIL_LIST, store);
		}
		
		return ht;
	}
}
