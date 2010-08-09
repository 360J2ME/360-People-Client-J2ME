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
//#condition polish.blackberry

package com.zyb.util;

import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.WLANInfo;

/**
 * @author Jens Vesti
 */
public class BlackBerryConnectionSuffix
{
	public static String connSuffixStr;

    public static void checkConnectionSuffixStr()
	{
    	connSuffixStr = null;
    	
		if (DeviceInfo.isSimulator()) {
			//#debug info
			System.out.println("Device is a simulator");

			connSuffixStr = ";ConnectionTimeout=60000;deviceside=true";
		}
        else if (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) {            
        	//#debug info
        	System.out.println("Device is connected via Wifi.");       
        	
        	connSuffixStr = ";ConnectionTimeout=60000;interface=wifi";       
        }
		else if ((CoverageInfo.getCoverageStatus() & CoverageInfo.COVERAGE_DIRECT) == CoverageInfo.COVERAGE_DIRECT) {
			//#debug info
			System.out.println("Direct coverage");
			
			String uid = null;
			ServiceRecord[] serviceRecords = ServiceBook.getSB().findRecordsByCid("WPTCP");

			for (int i = 0; i < serviceRecords.length; i++) {
				if (serviceRecords[i] != null && serviceRecords[i].isValid() && !serviceRecords[i].isDisabled()) {
					//#debug info
					System.out.println("Service " + serviceRecords[i].getName() + " " + serviceRecords[i].getUid());
					
					if (serviceRecords[i].getUid() != null && serviceRecords[i].getUid().length() != 0) {
						if ((serviceRecords[i].getUid().toLowerCase().indexOf("wifi") == -1)
							&& (serviceRecords[i].getUid().toLowerCase().indexOf("mms") == -1)) {
							uid = serviceRecords[i].getUid();
							break;
						}
					}
				}
			}

			if (uid != null) {
				connSuffixStr = ";ConnectionTimeout=60000;deviceside=true;ConnectionUID=" + uid;// WAP2
			}
			else {
				connSuffixStr = ";ConnectionTimeout=60000;deviceside=true";
			}
		}
		else if ((CoverageInfo.getCoverageStatus() & CoverageInfo.COVERAGE_MDS) == CoverageInfo.COVERAGE_MDS) {
			//#debug info
			System.out.println("MDS coverage found");

			connSuffixStr = ";ConnectionTimeout=60000;deviceside=false";
		}
		
		//#debug info
		System.out.println("BlackBerry connection suffix: " + connSuffixStr);
	}
}
