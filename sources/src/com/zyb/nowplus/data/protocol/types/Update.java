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

/**
 * 
 * Resembles the update request/response to check
 * if there is a new update of the client available
 * for download on the backend.
 * 
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public class Update implements ServiceObject {
	private static final int T_PEOPLE_CLIENT = 1;
	private static final String K_VERSION = "version",
								K_TITLE = "title",
								K_MESSAGE = "message",
								K_URL = "url",
								K_FORCE = "force",
								K_APP_TYPE = "apptype",
								K_USER_AGENT = "useragent";
	
	private String versionNumber,
					title,
					message,
					url;
	
	private boolean isForcedUpdate, isUpdateAvailable;
	
	/**
	 * 
	 * Constructor for checking whether an update 
	 * is available.
	 * 
	 * @param versionNumber The versionnumber in x.y.z form.
	 * 
	 */
	public Update(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	/**
	 * 
	 * Constructor for parsing the incoming hessian response.
	 * 
	 * @param ht The hashtable of the incoming response.
	 * 
	 */
	public Update(Hashtable ht) {		
		if (null == ht) {
			return;
		}
		
		//#debug info
		System.out.println("Reading Update response from server: " + ht.toString());
		
		if (ht.containsKey(K_VERSION)) {
			try {
				versionNumber = (String) ht.get(K_VERSION);
			} catch (Exception e) {}
		}
		
		if (ht.containsKey(K_TITLE)) {
			try {
				title = (String) ht.get(K_TITLE);
			} catch (Exception e) {}
		}
		
		if (ht.containsKey(K_MESSAGE)) {
			try {
				message = (String) ht.get(K_MESSAGE);
			} catch (Exception e) {}
		}
		
		if (ht.containsKey(K_URL)) {
			isUpdateAvailable = true;
			
			try {
				url = (String) ht.get(K_URL);
			} catch (Exception e) {}
		} else {
			isUpdateAvailable = false;
		}
		
		if (ht.containsKey(K_FORCE)) {
			isForcedUpdate = 
				((Boolean) ht.get(K_FORCE)).booleanValue();
		}
	}	

	public String getVersionNumber() {
		return versionNumber;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getUrl() {
		return url;
	}

	public boolean isForcedUpdate() {
		return isForcedUpdate;
	}
	
	public boolean isUpdateAvailable() {
		return isUpdateAvailable;
	}

	/**
	 * 
	 * Constructs a new request to find out if we
	 * have a new version of the client on the server.
	 * 
	 */
	public Hashtable toHashtable() {
		Hashtable ht = new Hashtable();
		
		if (null != versionNumber) {
			ht.put(K_VERSION, versionNumber);
		}
		
		Hashtable htClientParams = new Hashtable();
		htClientParams.put(K_APP_TYPE, new Integer(T_PEOPLE_CLIENT));
		
		try {
			
		String platform;
		//#if polish.blackberry
			platform = getBlackberryUserAgent();
			
			//#debug info
			System.out.println("retrieved blackberry current user agent:"+platform);
			
		//#else
			//#ifdef update.user.agent:defined
			//#message user agent is ${update.user.agent}
			//#= platform = ${update.user.agent};
			//#else
			//#message real user agent will be used
			platform = System.getProperty("microedition.platform");
			//#endif
		//#endif
			
			htClientParams.put(K_USER_AGENT, platform);
		} catch (Exception e) {}
		
		ht.put("clientparams", htClientParams);
		
		return ht;
	}
	
	//#if polish.blackberry
	
	// update.user.agent="BlackBerry9000/4.6.0.266 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/120"
	private String getBlackberryUserAgent()
	{
		String version = net.rim.device.api.system.DeviceInfo
				.getPlatformVersion();

		if (version == null || version.equals(""))
		{
			// for Simulator or older version of blackberry platforms
			net.rim.device.api.system.ApplicationDescriptor[] appDescriptors = net.rim.device.api.system.ApplicationManager
					.getApplicationManager().getVisibleApplications();

			for (int i = 0; i < appDescriptors.length; i++)
				if (appDescriptors[i].getModuleName().trim().equalsIgnoreCase(
						"net_rim_bb_ribbon_app"))
				{
					version = appDescriptors[i].getVersion();

					break;
				}
		}

		return "BlackBerry"
				+ net.rim.device.api.system.DeviceInfo.getDeviceName() + "/"
				+ version + " Profile/"
				+ System.getProperty("microedition.profiles")
				+ " Configuration/"
				+ System.getProperty("microedition.configuration")
				+ " VendorID/"
				+ net.rim.device.api.system.Branding.getVendorId();

	}
	//#endif

}
