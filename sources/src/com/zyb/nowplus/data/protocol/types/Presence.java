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

import com.zyb.nowplus.business.domain.Channel;

public class Presence implements ServiceObject {
	public static final String KEY_ONLINE = "online",
								KEY_IDLE = "idle",
								KEY_INVISIBLE = "invisible",
								KEY_OFFLINE = "offline",
								
								KEY_AVAILABILITY = "availability";
	
	public static final String 	KEY_PC = "pc",
								KEY_MOBILE = "mobile",
								KEY_ALL = "all";	// all networks online/offline/etc
	
	
	// needed variables for the get/set presence requests
	private String[] names;
	private String[][] networks;
	private int[][] presences;
	
	/**
	 * 
	 * Constructor for setting presence of the client's
	 * user.
	 * 
	 */
	public Presence(String[] networks, 
								int[] presenceStates) {
		this.networks = new String[1][];
		this.networks[0] = networks;
		
		this.presences = new int[1][];
		this.presences[0] = presenceStates;
	}
	
	
	/*
	 * Constructor for the getPresence-response.
	 * 
	 */
	public Presence(Hashtable htUsers) {
		if (null == htUsers) return;
		
		//#debug debug
		System.out.println("Users\n\n" + htUsers.toString());
		
		names = new String[htUsers.size()];
		networks = new String[htUsers.size()][];
		presences = new int[htUsers.size()][];
				
		// loop through each user
		int i = 0;
		String key = null;
		for (Enumeration keys = htUsers.keys(); keys.hasMoreElements(); i++) {
			try {
				// write user ID to array
				key = (String) keys.nextElement();
				
				// find out if we have a network in the string and
				// split it out
				if (null != key) {
					int charLocation = key.indexOf(':');
					
					if ((charLocation != -1) && 
							(key.charAt(charLocation + 1) == ':')) {
						names[i] = key.substring(charLocation + 2);
					} else {
						names[i] = key;
					}
				} 
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not cast user id.");
			}
			
			if (null != key) {
				Hashtable htPresence = null;
				
				try {
					htPresence = (Hashtable) htUsers.get(key);
					
				} catch (Exception e) {
					//#debug error
					System.out.println("Could not cast presence table.");
				}
				
				if (null != htPresence) {
					//#debug info
					System.out.println("Got presence table for user/contact " + names[i]);
					
					int numChannelsForUser = htPresence.size();
					
					// if the map is empty it means that there is an offline push
					// for all channels the user is in
					if (numChannelsForUser == 0) {
						networks[i] = new String[1]; 
						presences[i] = new int[1];
						
						networks[i][0] = KEY_ALL;
						presences[i][0] = Channel.PRESENCE_OFFLINE;
					} else {
						//#debug debug
						System.out.println("Number of channels is " + numChannelsForUser);
						
// commented out to prevent out of bounds exception
						// if we have zyb and mobile in the table, they mean the
						// same for the mobile client
//						if (htPresence.containsKey(KEY_PC) && 
//								htPresence.containsKey(KEY_MOBILE)) {
//							//#debug info
//							System.out.println("Reducing channel");
//							numChannelsForUser--;
//						}

						networks[i] = new String[numChannelsForUser]; 
						presences[i] = new int[numChannelsForUser]; 
						
						int j = 0;
						
						// loop through each presence of a user
						for (Enumeration pres = htPresence.keys(); 
										pres.hasMoreElements(); j++) {
							String network = null;
							String presence = null;
							
							try {
								network = (String) pres.nextElement();
								networks[i][j] = network;
								
								//#debug debug
								System.out.println("Adding channel " + network + " for user " + names[i]);
							} catch (Exception e) {
								//#debug error
								System.out.println("Could not cast channel" + e);
							}
							
							try {
								presence = (String) htPresence.get(network);
								
								if (presence.equals(KEY_ONLINE)) {
									presences[i][j] = Channel.PRESENCE_ONLINE;
								} else if (presence.equals(KEY_IDLE)) {
									presences[i][j] = Channel.PRESENCE_IDLE;
								} else if (presence.equals(KEY_INVISIBLE)) {
									presences[i][j] = Channel.PRESENCE_INVISIBLE;
								} else {
									presences[i][j] = Channel.PRESENCE_OFFLINE;
								}
								
								//#debug debug
								System.out.println("Adding presence " + presence + " to channel " + network + " for user " + names[i]);
							} catch (Exception e) {
								//#debug error
								System.out.println("Could not cast presence");
							}
							
							network = null;
							presence = null;
						}
					}
				}
				
				htPresence = null;
			}
		}
		
		//#mdebug debug
		System.out.println("Names: ");
		for (i = 0; i < names.length; i++) {
			System.out.println(names[i] + ", ");
		}
		
		System.out.println("Networks: ");
		for (i = 0; i < networks.length; i++) {
			for (int j = 0; j < networks[i].length; j++) {
				System.out.println("\t" + networks[i][j] + ", ");
			}
		}
		
		System.out.println("Presences: ");
		for (i = 0; i < presences.length; i++) {
			for (int j = 0; j < presences[i].length; j++) {
				if (presences[i][j] == Channel.PRESENCE_ONLINE) {
					System.out.println("\tOnline, ");
				} else if (presences[i][j] == Channel.PRESENCE_OFFLINE) {
					System.out.println("\tOffline, ");
				} else if (presences[i][j] == Channel.PRESENCE_IDLE) {
					System.out.println("\tIdle, ");
				} else if (presences[i][j] == Channel.PRESENCE_INVISIBLE) {
					System.out.println("\tInvisible, ");
				}
				
			}
		}
		//#enddebug
	}
	
	
	public String[] getNames() {
		return names;
	}


	public void setNames(String[] names) {
		this.names = names;
	}
	
	/**
	 * 
	 * Gets the networks for an identifier at the given index.
	 * 
	 * @param index The index of the identifier to get the networks
	 * for.
	 * 
	 * @return The networks found.
	 * 
	 */
	public String[] getNetworks(int index) {
		if ((null == names) || (null == networks) ||
				(index >= networks.length) || (index < 0)) {
			return null;
		}
		
		return networks[index];
	}
	
	/**
	 * 
	 * Gets all networks for all users.
	 * 
	 * @return All networks for all users.
	 * 
	 */
	public String[][] getNetworks() {
		return networks;
	}
	
/*	public void setNetworks(String[][] networks) {
		this.networks = networks;
	}*/
	
	/**
	 * Sets the networks for setting the own presence.
	 * 
	 * @param networks The networks to set the presence for.
	 * 
	 */
	public void setNetworks(String[] networks) {
		this.networks = new String[1][];
		this.networks[0] = networks;
	}

	/**
	 * 
	 * Returns all presences for all users.
	 * 
	 * @return All presences for all users.
	 * 
	 */
	public int[][] getPresences() {
		return presences;
	}
	
	/**
	 * 
	 * Returns presences for the user at the given index.
	 * 
	 * @param index The index of the identifier to get the
	 * presence for.
	 * @return The presences for the user at the given index
	 * or null if the given index is out of bounds.
	 */
	public int[] getPresences(int index) {
		if ((null == names) || (null == presences) ||
				(index >= presences.length) || (index < 0)) {
			return null;
		}

		return presences[index];
	}
	
	/**
	 * 
	 * Sets the own presence.
	 * 
	 * @param presences The presences as integers. The correct
	 * mappings can be found in 
	 * {@link com.zyb.nowplus.business.domain.Channel Channel}.
	 * 
	 */
	public void setPresences(int[] presences) {
		this.presences = new int[1][];
		this.presences[0] = presences;
		
	}

	/**
	 * 
	 * Used for setting the availability of the client's user to the
	 * backend.
	 * 
	 */
	public Hashtable toHashtable() {
		Hashtable htPresence = new Hashtable();
		
		Hashtable ht = new Hashtable();
		
		if ((null != presences) && (null != networks)
	     && (presences.length == 1) && (networks.length == 1)
	     && (null != presences[0]) && (null != networks[0])
		 &&	(presences[0].length == networks[0].length)) {
						
			for (int i = 0; i < presences[0].length; i++) {
				// if we are in the nowplus-network, we will not alter 
				// the identifier, otherwise we will take network::identifier
				
				String identifier = null;
				if (null != networks[0][i]) {
					identifier = networks[0][i]; // + "::" + names[i];
				} else {
					identifier = KEY_MOBILE; // + "::" + names[i];
				}
				
				switch(presences[0][i]) {
					case Channel.PRESENCE_ONLINE:
						ht.put(identifier, KEY_ONLINE);
						break;
					case Channel.PRESENCE_IDLE:
						ht.put(identifier, KEY_IDLE);
						break;
					case Channel.PRESENCE_INVISIBLE:
						ht.put(identifier, KEY_INVISIBLE);
						break;
					default:
						ht.put(identifier, KEY_OFFLINE);
						break;
				}
			}
		}
		
		htPresence.put(KEY_AVAILABILITY, ht);
		return htPresence;
	}
}
