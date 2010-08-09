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

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;
import com.zyb.util.TextUtilities;




public class NowPlusUserProfile implements ServiceObject {
	// is the contact of male, female or unknown gender?
	public static final byte FEMALE = 0, MALE = 1, UNKNOWN = -1;
	
	// identifies the key keyword in a contactdetail
	private static final String K_DETAIL_LIST = "detaillist",
								K_DETAIL_VALUE = "val",
								K_DETAIL_KEY = "key",
								K_DETAIL_TYPE = "type",	
								K_DETAIL_BYTES = "bytes",
								K_DETAIL_BYTES_MIME = "bytesmime",
								K_PHOTO_URL = "bytesurl",
								K_TYPE = "type",
								K_STORE = "store",
								K_DETAIL = "detail",
								K_DETAILLIST = "detaillist",
								K_CONTACT_ID = "contactid",
								K_USER_ID = "userid";
	

	// define keys that are in the hashtable
	private static final String KEY_NAME = "vcard.name", 
								KEY_NICKNAME = "vcard.nickname", 
								KEY_IMADDRESS = "vcard.imaddress", 
								KEY_PRESENCE = "presence.text",
								KEY_EMAIL = "vcard.email", 
								KEY_PHONE = "vcard.phone", 
								KEY_PHOTO = "photo", 
								KEY_ADDRESS = "vcard.address", 
								KEY_SOCIAL_NETWORK = "vcard.internetaddress", 
								KEY_URL = "vcard.url";
					
	private long contactID, userID;
	private String firstName, middleNames, lastName, nickname;
	private Date dateOfBirth;
	private String[] phones, phoneTypes;
	private String[] emails, emailTypes;
	private String[] urls, urlTypes;
	private String[][] addresses;
	private String[] addressTypes;
	private String avatarURL, presence, presenceNetwork, bytesMime;
	private String[] imAddresses, imAddressServices;
	private String[] socialNetworkNames, socialNetworkAccountNames, socialNetworkAccountUrls;			
	private byte[] bytes;
	private String location, locationImageUrl;
	private String[] workDetails;
	private String notes;
	
	public NowPlusUserProfile() {}
	
	public NowPlusUserProfile(Hashtable ht) {
		userID = UNKNOWN;	// marks whether a user is a now+ user
		
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
			
			if (ht.containsKey(K_USER_ID)) {
				try {
					userID = ((Long) ht.get(K_USER_ID)).longValue();
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast user id into long.");
				}
			}
			if (userID == 0) {
				userID = UNKNOWN;
			}
			
			
			if (ht.containsKey(K_DETAILLIST)) {
				try {
					contactDetails = (Vector) ht.get(K_DETAILLIST);
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
		int counterURL = 0;
		
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
						} else if (detailValue.equals(KEY_URL)) {
							counterURL++;
						}
					}
				}
			}
		}
		
		
		// initialize array-sizes
		if (0 != counterIMAddress) {
			imAddresses = new String[counterIMAddress];
			imAddressServices = new String[counterIMAddress];
		}
		
		if (0 != counterEmail) {
			emails = new String[counterEmail];
			emailTypes = new String[counterEmail];
		}
		
		if (0 != counterPhone) {
			phones = new String[counterPhone];
			phoneTypes = new String[counterPhone];
		}

		if (0 != counterAddress) {
			addresses = new String[counterAddress][5];
			addressTypes = new String[counterAddress];
		}
		
		if (0 != counterInternetAddress) {
			socialNetworkAccountNames = new String[counterInternetAddress];
			socialNetworkNames = new String[counterInternetAddress];
		}
		
		if (0 != counterURL) {
			urls = new String[counterURL];
			urlTypes = new String[counterURL];
		}
	}		
		
	
	private void populateContactDetails(final Vector contactDetails) {
		if (null == contactDetails) {
			return;
		}
		
		Hashtable htContactDetail = null;
		
		// we need these to keep the index
		int indexIMAddress = 0;
		int indexEmail = 0;
		int indexPhone = 0;
		int indexAddress = 0;
		int indexInternetAddress = 0;
		int indexURL = 0;
		
		//#debug debug
		System.out.println("--------------New contact----------------");
		
		
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
				//#mdebug debug
				try
				{
					System.out.println("---details set---");

					Enumeration keys = htContactDetail.keys();
					while(keys.hasMoreElements())
					{
						Object obj = keys.nextElement();
						System.out.println(obj+":"+htContactDetail.get(obj));
					}
				}
				catch(Exception e)
				{
					System.out.println("error:"+e.getMessage());
				}
				//#enddebug
				
				
				
				if (htContactDetail.containsKey(K_DETAIL_KEY)) {
					String detailKey = (String)htContactDetail.get(K_DETAIL_KEY);
					String detailValue = null;
					
					try {
						detailValue = (String)htContactDetail.get(K_DETAIL_VALUE);
					} catch (Exception e) {
						//#debug error
						System.out.println("Could not cast into String.");
					}
				
					try {
						// now we take a look what kind of detail we have
						// and increment the counters
						if (null != detailKey) {
							if (detailKey.equals(KEY_NAME)) {
								//#debug debug
								System.out.println("VCard Name: " + detailValue);
								
								String[] vcard = TextUtilities.getSubstrings(detailValue, ';',
																				'\\', false);
								 
								if (null != vcard) {							
									for (int j = 0; j < vcard.length; j++) {
										switch (j) {
											case 0:
												lastName = vcard[0];
												break;
											case 1:
												firstName = vcard[1];
												break;
											case 2:
												middleNames = vcard[2];
												break;
										}
									}
								}
								
								//#debug debug
								System.out.println("NAMES: " + firstName + " " + middleNames + " " + lastName);
							} else if (detailKey.equals(KEY_NICKNAME)) {
								nickname = detailValue;
							} else if (detailKey.equals(KEY_PRESENCE)) {
								presence = detailValue;
								
								if (htContactDetail.containsKey(K_DETAIL_TYPE)) {
									presenceNetwork = (String) htContactDetail.get(K_DETAIL_TYPE);
								}
							} else if (detailKey.equals(KEY_PHOTO)) {
								//Use "avatarURL = detailValue;" instead if data is stored in val
								//else this if stored in "bytesurl"
								try {
									avatarURL = (String) htContactDetail.get(K_PHOTO_URL);
								} catch (Exception ee) {
									avatarURL = null;
									
									//#debug error
									System.out.println("Could not cast photoUrl.");
								}
							} else if (detailKey.equals(KEY_IMADDRESS)) {
								imAddresses[indexIMAddress] = detailValue;
								
								try {
									imAddressServices[indexIMAddress] = (String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								indexIMAddress++;
							} else if (detailKey.equals(KEY_EMAIL)) {
								emails[indexEmail] = detailValue;
								
								try {
									emailTypes[indexEmail] = (String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								
								indexEmail++;
							} else if (detailKey.equals(KEY_PHONE)) {
								phones[indexPhone] = detailValue;
								
								try {
									phoneTypes[indexPhone] = (String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								
								indexPhone++;
							} else if (detailKey.equals(KEY_ADDRESS)) {
								addresses[indexAddress][0] = detailValue;
								addresses[indexAddress][1] = "";
								addresses[indexAddress][2] = "";
								addresses[indexAddress][3] = "";
								addresses[indexAddress][4] = "";
								try {
									addressTypes[indexAddress] = (String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								
								indexAddress++;
							} else if (detailKey.equals(KEY_SOCIAL_NETWORK)) {
								socialNetworkAccountNames[indexInternetAddress] = detailValue;
								
								try {
									socialNetworkNames[indexInternetAddress] = 
													(String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								
								indexInternetAddress++;
							} else if (detailKey.equals(KEY_URL)) {
								urls[indexURL] = detailValue;
								
								try {
									urlTypes[indexURL] = (String) htContactDetail.get(K_TYPE);
								} catch (Exception ee) {
									//#debug error
									System.out.println("Could not cast detail type.");
								}
								
								indexURL++;
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
	
	public void setUserId(long uid) {
		this.userID = uid;
	}
	
	public long getUserId() {
		return userID;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = null;
		this.firstName = firstName;
	}
		
	public String getFirstName()
	{
		return firstName;
	}
	
	public void setMiddleNames(String middleNames) {
		this.middleNames = null;
		this.middleNames = middleNames;
	}
		
	public String getMiddleNames() {
		return middleNames;
	}
	
	public void setLastName(String lastName) {
		this.lastName = null;
		this.lastName = lastName;
	}
		
	public String getLastName() {
		return lastName;
	}
	
	public void setNickname(String nickname) {
		this.nickname = null;
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = null;
		this.dateOfBirth = dateOfBirth;
	}
	
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}
	
	public void setPhones(String[] phones, String[] phoneTypes) {
		this.phones = phones;
		this.phoneTypes = phoneTypes;
	}

	public String[] getPhones() {
		return phones;
	}
	
	public String[] getPhoneTypes() {
		return phoneTypes;
	}
	
	public void setEmails(String[] emails, String[] emailTypes) {
		this.emailTypes = emailTypes;
		this.emails = emails;
	}
	
	public String[] getEmails() {
		return emails;
	}

	public String[] getEmailTypes() {
		return emailTypes;
	}

	public void setUrls(String[] urls, String[] urlTypes) {
		this.urls = urls;
		this.urlTypes = urlTypes;
	}
	
	public String[] getUrls() {
		return urls;
	}
	
	public String[] getUrlTypes() {
		return urlTypes;
	}
	
	public void setAddresses(String[][] addresses, String[] addressTypes) {
		this.addressTypes = addressTypes;
		this.addresses = addresses;
	}
	
	public String[][] getAddresses() {
		return addresses;
	}
	
	public String[] getAddressTypes() {
		return addressTypes;
	}
	
	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getAvatarURL() {
		return avatarURL;
	}	
		
	public void setPresence(String presence) {
		this.presence = presence;
	}
	
	public String getPresence() {
		return presence;
	}	
	
	public String getPresenceNetwork()
	{
		return presenceNetwork;
	}

	public void setImAddresses(String[] imAddresses,
								String[] imAddressServices) {
		this.imAddresses = imAddresses;
		this.imAddressServices = imAddressServices;
	}
	
	public String[] getImAddresses() {
		return imAddresses;
	}
	
	public String[] getImAddressServices() {
		return imAddressServices;
	}

	public void setSocialNetworks(String[] socialNetworkNames,
				String[] socialNetworkAccountNames,
				String[] socialNetworkAccountUrls) {
		this.socialNetworkNames = socialNetworkNames;
		this.socialNetworkAccountNames = socialNetworkAccountNames;
		this.socialNetworkAccountUrls = socialNetworkAccountUrls;
	}

	public String[] getSocialNetworkAccountUrls() {
		return socialNetworkAccountUrls;
	}
	
	public String[] getSocialNetworkAccountNames() {
		return socialNetworkAccountNames;
	}
	
	public String[] getSocialNetworkNames() {
		return socialNetworkNames;
	}
		
	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocationImageUrl(String locationImageUrl) {
		this.locationImageUrl = locationImageUrl;
	}

	public String getLocationImageUrl() {
		return locationImageUrl;
	}
	
	public void setWorkDetails(String[] workDetails)
	{
		this.workDetails = workDetails;
	}
	
	public String[] getWorkDetails()
	{
		return workDetails;
	}
	
	public void setNotes(String notes)
	{
		this.notes = notes;
	}
	
	public String getNotes()
	{
		return notes;
	}
	
	public void setPhoto(byte[] bytes, String bytesMime) {
		this.bytes = bytes;
		this.bytesMime = bytesMime;
	}
	
	public String[] getFolders() {
		return null;
	}
	
	public boolean isConnected() {
		// TODO replace with the real indication if the contact is connected to me
		return (userID % 2 == 1);
	}
	
	public Hashtable toHashtable() {
		Hashtable htContact = new Hashtable();
		Vector detailList = new Vector();
		
		if (null != lastName) {
			Hashtable htName = new Hashtable();
			htName.put(K_DETAIL_KEY, KEY_NAME);
			htName.put(K_DETAIL_VALUE, lastName);
			detailList.addElement(htName);
		}
		
		if (null != nickname) {
			Hashtable htNickname = new Hashtable();
			htNickname.put(K_DETAIL_KEY, KEY_NICKNAME);
			htNickname.put(K_DETAIL_VALUE, nickname);
			detailList.addElement(htNickname);
		}
		
		if ((null != imAddresses) && (null != imAddressServices)) {
			if (imAddresses.length == imAddressServices.length) {
				Hashtable htIMAddresses = null;
				
				for (int i = 0; i < imAddresses.length; i++) {
					htIMAddresses = new Hashtable();
					htIMAddresses.put(K_STORE, imAddressServices[i]);
					htIMAddresses.put(K_DETAIL_KEY, KEY_IMADDRESS);
					htIMAddresses.put(K_DETAIL_VALUE, imAddresses[i]);
					detailList.addElement(htIMAddresses);
				}
			}
		}
		
		if ((null != emails) && (null != emailTypes)) {
			if (emails.length == emailTypes.length) {
				Hashtable htEMails = null;
				
				for (int i = 0; i < emails.length; i++) {
					htEMails = new Hashtable();
					htEMails.put(K_TYPE, emailTypes[i]);
					htEMails.put(K_DETAIL_KEY, KEY_EMAIL);
					htEMails.put(K_DETAIL_VALUE, emails[i]);
					detailList.addElement(htEMails);
				}
			}
		}	
		
		if ((null != phones) && (null != phoneTypes)) {
			if (phones.length == phoneTypes.length) {
				Hashtable htPhones = null;
				
				for (int i = 0; i < phones.length; i++) {
					htPhones = new Hashtable();
					htPhones.put(K_TYPE, phoneTypes[i]);
					htPhones.put(K_DETAIL_KEY, KEY_PHONE);
					htPhones.put(K_DETAIL_VALUE, phones[i]);
					detailList.addElement(htPhones);
				}
			}
		}	
		
		if ((null != addresses) && (null != addressTypes)) {
			if (addresses.length == addressTypes.length) {
				Hashtable htAddress = null;
				
				for (int i = 0; i < addresses.length; i++) {
					htAddress = new Hashtable();
					htAddress.put(K_TYPE, addressTypes[i]);
					htAddress.put(K_DETAIL_KEY, KEY_ADDRESS);
					htAddress.put(K_DETAIL_VALUE, addresses[i]);
					detailList.addElement(htAddress);
				}
			}
		}	
		
		if ((null != socialNetworkNames) && 
				(null != socialNetworkAccountNames)) {
			if (socialNetworkNames.length == 
						socialNetworkAccountNames.length) {
				Hashtable htSNs= null;
				
				for (int i = 0; i < socialNetworkNames.length; i++) {
					htSNs = new Hashtable();
					htSNs.put(K_TYPE, socialNetworkNames[i]);
					htSNs.put(K_DETAIL_KEY, KEY_SOCIAL_NETWORK);
					htSNs.put(K_DETAIL_VALUE, 
							socialNetworkAccountNames[i]);
					detailList.addElement(htSNs);
				}
			}
		}
		
		if ((null != urls) && (null != urlTypes)) {
			if (urls.length == urlTypes.length) {
				Hashtable htURLs= null;
				
				for (int i = 0; i < urls.length; i++) {
					htURLs = new Hashtable();
					htURLs.put(K_TYPE, urlTypes[i]);
					htURLs.put(K_DETAIL_KEY, KEY_URL);
					htURLs.put(K_DETAIL_VALUE, urls[i]);
					detailList.addElement(htURLs);
				}
			}
		}
		
		if (null != bytes) {
			Hashtable htPhoto = new Hashtable();
			
			htPhoto.put(K_DETAIL_KEY, "photo");
			
			if (null != bytes) {
				htPhoto.put(K_DETAIL_BYTES, bytes);
			}
			
			if (null != bytesMime) {
				htPhoto.put(K_DETAIL_BYTES_MIME, bytesMime);
			}
			
			detailList.addElement(htPhoto);
		}
		
		htContact.put(K_DETAIL_LIST, detailList);
		
		return htContact;
	}
	
	public boolean equals(Object o)
	{
		NowPlusUserProfile that = (NowPlusUserProfile) o;
		return (this.contactID == that.contactID)
		&& (this.userID == that.userID)
		&& HashUtil.equals(this.nickname, that.nickname)
		&& HashUtil.equals(this.firstName, that.firstName)
		&& HashUtil.equals(this.middleNames, that.middleNames)
		&& HashUtil.equals(this.lastName, that.lastName)
		&& ArrayUtils.equals(this.phones, that.phones)
		&& ArrayUtils.equals(this.phoneTypes, that.phoneTypes)
		&& ArrayUtils.equals(this.emails, that.emails)
		&& ArrayUtils.equals(this.emailTypes, that.emailTypes)
		&& ArrayUtils.equals(this.urls, that.urls)
		&& ArrayUtils.equals(this.urlTypes, that.urlTypes)
		&& ArrayUtils.equals(this.addresses, that.addresses)
		&& ArrayUtils.equals(this.addressTypes, that.addressTypes)
		&& HashUtil.equals(this.notes, that.notes);
	}
	
	//#mdebug error
	public String toString()
	{
		return "Contact[contactID=" + contactID
		 + ",userID=" + userID
		 + ",nickname=" + nickname
		 + ",firstName=" + firstName
		 + ",middleNames=" + middleNames
		 + ",lastName=" + lastName
		 + ",phones=" + ArrayUtils.toString(phones)
		 + ",phoneTypes=" + ArrayUtils.toString(phoneTypes)
		 + ",emails=" + ArrayUtils.toString(emails)
		 + ",emailTypes=" + ArrayUtils.toString(emailTypes)
		 + ",urls=" + ArrayUtils.toString(urls)
		 + ",urlTypes=" + ArrayUtils.toString(urlTypes)		 
		 + ",addresses=" + ArrayUtils.toString(addresses)
		 + ",addressTypes=" + ArrayUtils.toString(addressTypes)		
		 + ",notes=" + notes	 
		 + "]";
	}	
	//#enddebug
}
