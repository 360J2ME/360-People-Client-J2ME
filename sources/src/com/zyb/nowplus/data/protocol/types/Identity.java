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

import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;

/**
 * 
 * <p>Used for dealing with all calls and responses dealing with identities. The
 * class has 3 constructors:</p>
 * 
 * <p>Identity(Hashtable ht) resembles a response to either a getMyIdentities or a
 * getAvailableIdentities-call. The request needed to be made is either done by using
 * ServerRequest.GET + ServerRequest.IDENTITIES or by making the call 
 * ServerRequest.GET + ServerRequest.MY_IDENTITIES. The callback via itemsReceived()
 * will be of type ServiceObject.IDENTITIES.</p>
 * 
 * <p>Identity(String pluginid, String network, String username, String password) 
 * is the constructor for making a call to the method validateIdentityCredentials. The
 * call is done by passing ServerRequest.VALIDATE + ServerRequest.IDENTITIES in
 * the CommunicationManagerImpl's sendRequest-method.</p>
 * 
 * <p>The response for the validateIdentityCredentials-class is passed to the constructor
 * Identity(boolea identityCreated) by the hessian parser. By calling isIdentityCreated
 * the model can find out if the identity was set on the backend. The identity holding
 * the boolean is passed back as the ServiceObject at position 0 of a 1-length array
 * using the itemsReceived-method of the ResponseListener. The type of the callback
 * is ServiceObject.ADD_IDENTITY_RESULT.</p>
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public class Identity implements ServiceObject {
	private static final String /*TYPE_NO_AUTH = "nothing",
								TYPE_WEB_BASED = "url",
								TYPE_CREDENTIALS = "credentials",
								TYPE_WEB_BASED_AND_CREDENTIALS = "credentials_url",
								TYPE_CREDENTIALS_AND_SERVER = "credentials_and_server",
								TYPE_CREDENTIALS_AND_SERVER_AND_CONTACT_DETAIL = 
									"credentials_and_server_and_contact_detail",*/
									
								KEY_IDENTITY = "identityid",
								KEY_CAPABILITIES = "identitycapabilitylist",
								KEY_CAPABILITY_ID = "capabilityid",
								KEY_NAME = "name",
								KEY_DISPLAY_NAME = "displayname",
								KEY_ACTIVE = "active",								
								KEY_PLUGIN_ID = "pluginid",
								KEY_NETWORK = "network",
								KEY_NETWORK_URL = "networkurl",
								KEY_ICON = "icon",
								KEY_ICON_MIME = "iconmime",
								KEY_CAPABILITY_STATUS = "identitycapabilitystatus",
								KEY_PLUGIN = "plugin",
								KEY_USERNAME = "username",
								KEY_PASSWORD = "password",
								KEY_IDENTITY_ID = "identityid",
								KEY_TIMESTAMP = "timestamp",
								KEY_STATUS = "status",
								
								K_ENABLE = "enable",
								K_DISABLE = "disable"; /*,
								K_RELOAD = "reload",
								K_DELETE = "delete",
								K_SUSPENDED = "suspended",
								K_UNSUSPENDED = "unsuspended",
								K_LOGIN = "login",
								K_LOGOUT = "logout";*/
	
	public static final int T_NO_AUTH = 0,
							T_WEB_BASED = 1,
							T_CREDENTIALS = 2,
							T_WEB_BASED_AND_CREDENTIALS = 3,
							T_CREDENTIALS_AND_SERVER = 4,
							T_CREDENTIALS_AND_SERVER_AND_CONTACT_DETAIL = 5,
							
							T_ENABLE = 1,
							T_DISABLE = 2,
							T_RELOAD = 3,
							T_DELETE = 4,
							T_SUSPENDED = 5,
							T_UNSUSPENDED = 6,
							T_LOGIN = 7,
							T_LOGOUT = 8;
	
	private String identityID;
	private String name;
	private String displayName;
	private boolean active;
	private String pluginid;
	private String network;
	private String networkUrl;
	private String statusStr;
	private byte[] iconBytes;
	private String iconMime;
	private String[] capabilities;
	
	// Fields only used by the validateIdentityCredentials-request and in the response to getMyIdentities
	private String username, password;
	
	// only used as a result to a validateIdentityCredentials/DeleteIdentity call.
	private boolean operationSuccessful;
	
	/**
	 * 
	 * Constructor for parsing a getAvailableIdentities or getMyIdentities response
	 * from the backend.
	 * 
	 * @param ht The Hashtable to parse.
	 * 
	 */
	public Identity(Hashtable ht) {		
		if (null != ht) {
			try {
				identityID = (String) ht.get(KEY_IDENTITY);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				name = (String) ht.get(KEY_NAME);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				username = (String) ht.get(KEY_USERNAME);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				displayName = (String) ht.get(KEY_DISPLAY_NAME);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				Boolean b = (Boolean) ht.get(KEY_ACTIVE);
				active = (b != null) && b.booleanValue();
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				pluginid = (String) ht.get(KEY_PLUGIN_ID);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				network = (String) ht.get(KEY_NETWORK);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}

			try {
				networkUrl = (String) ht.get(KEY_NETWORK_URL);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				iconBytes = (byte[]) ht.get(KEY_ICON);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				iconMime = (String) ht.get(KEY_ICON_MIME);
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
			
			try {
				Vector vCapabilities = (Vector) ht.get(KEY_CAPABILITIES);
				
				if (null != vCapabilities) {
					capabilities = new String[vCapabilities.size()];
					
					for (int i = 0; i < vCapabilities.size(); i++) {
						Hashtable capability = (Hashtable) vCapabilities.elementAt(i);
						
						if (null == capability) {
							continue;
						}
						String capID = (String) capability.get(KEY_CAPABILITY_ID);
						capabilities[i] = capID;
						
						//#debug debug
						System.out.println("Capability " + capID);
					}
					
					vCapabilities = null;
				}
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
		}
	}
	
	
	/**
	 * 
	 * Constructor used for doing the validateIdentityCredentials-call to the 
	 * backend. 
	 * 
	 * @param pluginid The plugin to register against. Can be retrieved via 
	 * getAvailableIdentities.
	 * @param network The network to link against. E.g. google...
	 * @param username The username in the service.
	 * @param password The password in the service.
	 * @param capabilities The capabilities to set to true.
	 * 
	 */
	public Identity(String pluginid, String network, String username, String password,
					String[] capabilities) {
		this.pluginid = pluginid;
		this.network = network;
		this.username = username;
		this.password = password;
		this.capabilities = capabilities;
	}
	
	/**
	 * 
	 * Constructor used for doing the DeleteIdentity-call to the 
	 * backend. 
	 * 
	 * @param pluginid The plugin to delete from. Can be retrieved via 
	 * getMyIdentities.
	 * @param network The network to unlink against. E.g. google...
	 * @param identityID The id of the identity. E.g. rudynorff@gmail.com.
	 * 
	 */
	public Identity(String pluginid, String network, String identityID) {
		this.pluginid = pluginid;
		this.network = network;
		this.identityID = identityID;
	}
	
	/**
	 * 
	 * Constructor used for doing the SetIdentityStatus-call to the 
	 * backend. Used to set an identity to enabled, disabled, deleted,
	 * suspended, unsuspended, logged in, logged out or used to reload
	 * and identity.
	 * 
	 * @param pluginid The plugin to delete from. Can be retrieved via 
	 * getMyIdentities.
	 * @param network The network to unlink against. E.g. google...
	 * @param identityID The id of the identity. E.g. rudynorff@gmail.com.
	 * @param status The status code of the social network. Can be: T_ENABLE,
	 * T_DISABLE, T_RELOAD, T_DELETE, T_SUSPENDED, T_UNSUSPENDED, T_LOGIN, T_LOGOUT.
	 * @param password The password of the identity. Optional. Can be null.
	 * 
	 */
	public Identity(String pluginid, String network, String identityID, int status, String password) {
		this.pluginid = pluginid;
		this.network = network;
		this.identityID = identityID;
		this.password = password;
		
		switch (status) {
			case T_ENABLE:
				statusStr = K_ENABLE;
				break;
			case T_DISABLE:
				statusStr = K_DISABLE;
				break;
			default:
				//#debug error
				System.out.println("BRUNO KILLED IT!");
				break;
/*			case T_RELOAD:
				statusStr = K_RELOAD;
				break;
			case T_DELETE:
				statusStr = K_DELETE;
				break;
			case T_SUSPENDED:
				statusStr = K_SUSPENDED;
				break;
			case T_UNSUSPENDED:
				statusStr = K_UNSUSPENDED;
				break;
			case T_LOGIN:
				statusStr = K_LOGIN;
				break;
			case T_LOGOUT:
				statusStr = K_LOGOUT;
				break;*/
		}
	}
	
	
	/**
	 * 
	 * Constructor for receiving the response of a validateIdentityCredentials- or
	 * deleteIdentity-call.
	 * If true the identity was added/deleted to/from the user.
	 * 
	 *  @param operationSuccessful The identity was created if true.
	 * 
	 */
	public Identity(boolean operationSuccessful) {
		this.operationSuccessful = operationSuccessful;
		
		//#debug info
		System.out.println("Identity was created/removed: " + operationSuccessful);
	}
	
	
	public String getIdentityID() {
		return identityID;
	}
	
	public String getUsername() {
		return (username == null) ? identityID : username;
	}
	
	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String getPluginid() {
		return pluginid;
	}

	public void setNetwork(String network)
	{
		this.network = network;
	}
	public String getNetwork() {
		return network;
	}

	public String getNetworkUrl() {
		return networkUrl;
	}

	public byte[] getIconBytes() {
		return iconBytes;
	}

	public String getIconMime() {
		return iconMime;
	}
	
	public String getStatus() {
		return statusStr;
	}
	
	public String[] getCapabilities() {
		return capabilities;
	}
	
	/**
	 * 
	 * Returns true if the identity was created for the user after a
	 * validateIdentityCredentials-call or if the identity was deleted
	 * from the user using deleteIdentity.
	 * 
	 * @return True if the identity was created/deleted.
	 * 
	 */
	public boolean isOperationSuccessful() {
		return operationSuccessful;
	}


	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		if (null != pluginid) {
			ht.put(KEY_PLUGIN, pluginid);
			ht.put(KEY_PLUGIN_ID, pluginid);
		}
		if (null != network) {
			ht.put(KEY_NETWORK, network);
		}
		if (null != username) {
			ht.put(KEY_USERNAME, username);
		}
		String userID = RPGConnection.userID;
		if ((null != password) && (null != userID)) {
			//#debug debug
			System.out.println("Creating encrypted Pass with uID " + userID);
			long ts = ((long) System.currentTimeMillis() / 1000);
			byte[] cryptedPass = Toolkit.getEncryptedPassword(
											ts, userID, password);
			
			if (null != cryptedPass) {
				ht.put(KEY_PASSWORD, cryptedPass);
				ht.put(KEY_TIMESTAMP, new Long(ts));
			} else {
				//#debug error
				System.out.println("Encrypted password was null!!");
			}
			cryptedPass = null;
		}
		if (null != identityID) {
			ht.put(KEY_IDENTITY_ID, identityID);
		}
		if (null != statusStr) {
			ht.put(KEY_STATUS, statusStr);
		}
		
		
		// only if we have all values to set up a new
		// community
		if ((null != pluginid) && (null != network) && 
				(null != password) && (null != username)) {
			if (null != capabilities) {
				Hashtable htCapabilities = new Hashtable();
				
				for (int i = 0; i < capabilities.length; i++) {
					if (null == capabilities[i]) {
						continue;
					}
					
					htCapabilities.put(capabilities[i], new Boolean(true));
				}
					
				ht.put(KEY_CAPABILITY_STATUS, htCapabilities);
			}
		}
		
		return ht;
	}

}
