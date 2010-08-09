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
package com.zyb.nowplus;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Displayable;

/**
 * Interface to the 'MIDletness' of a MIDlet. By using this interface, the model
 * is independent of the actual Now+ MIDlet.
 */
public interface MIDletContext 
{
	public static final String APP_NAME = "People_Client";

	//#if polish.blackberry
	public static final String APP_KEY_ID = "";// add your application key ID
    public static final String APP_KEY_SECRET = "";//add your application key secret
    //#else
	//#= public static final String APP_KEY_ID = "";// add your application key ID
    //#= public static final String APP_KEY_SECRET = "";// add your application key secret
    //#endif
	
	/**
	 * Displays a displayable
	 */
	public void setCurrent(Displayable disp);

	/**
	 * Gets the Mobile Subscriber ISDN number.
	 */
	public String getMsisdn();
	
	/**
	 * Gets the current language set on the device.
	 */
	public String getCurrentLanguage();
	
	/**
	 * Gets a device identifier. On BB, this is the bb pin.
	 */
	public String getDeviceId();
	
	/**
	 * Performs a platform request with the given url.
	 */
	public boolean platformRequest(String url) throws ConnectionNotFoundException;
		
	/**
	 * Exits the MIDlet.
	 */
	public void exit(final boolean deleteStorageBefore);
	
	/**
	 * Checks if the given files are present.
	 */
	public boolean checkFiles(String[] files);
	
	/**
	 * Delete all record stores.
	 */
	public void deleteRecordStores(String[] filter);
	
	/**
	 * Gets the current displayable
	 * @return
	 */
	public Displayable getCurrent();
	
	/**
	 * 
	 * True if it is a Vodafone customer and we 
	 * support Tcp.
	 * 
	 * @return True if we support Tcp, false if 
	 * we support Http.
	 * 
	 */
	public boolean supportsTcp();
}
